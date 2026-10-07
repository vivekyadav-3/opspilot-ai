package com.opspilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.dto.JevTriageResponse;
import com.opspilot.entity.*;
import com.opspilot.repository.AiDecisionRepository;
import com.opspilot.repository.TeamRepository;
import com.opspilot.repository.TicketEventRepository;
import com.opspilot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TriageService {

    private final JevClient jevClient;
    private final SlaService slaService;
    private final IncidentCorrelationService correlationService;
    private final TicketRepository ticketRepository;
    private final TeamRepository teamRepository;
    private final AiDecisionRepository aiDecisionRepository;
    private final TicketEventRepository ticketEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Transactional
    public void processTicketAsync(Long ticketId) {
        log.info("Starting async background AI triage for ticket #{}", ticketId);
        try {
            Optional<Ticket> optTicket = Optional.empty();
            for (int attempt = 0; attempt < 5; attempt++) {
                optTicket = ticketRepository.findById(ticketId);
                if (optTicket.isPresent()) break;
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            if (optTicket.isEmpty()) {
                log.warn("Ticket #{} not found for triage processing", ticketId);
                return;
            }
            Ticket ticket = optTicket.get();

            // 1. Set status to AI_ANALYZING
            ticket.setStatus(TicketStatus.AI_ANALYZING);
            ticketRepository.save(ticket);

            // 2. Classify via Jev AI (or heuristic fallback)
            JevTriageResponse aiResponse = jevClient.classify(ticket);

            if (aiResponse == null) {
                log.warn("AI Triage response was null for ticket #{}. Defaulting.", ticketId);
                aiResponse = JevTriageResponse.builder()
                        .category(TicketCategory.OTHER)
                        .priority(Priority.MEDIUM)
                        .confidence(0.5)
                        .urgencyScore(50)
                        .assignedTeamName("IT Support")
                        .reasoning("Fallback classification")
                        .build();
            }

            // 3. Update Ticket fields based on AI decision
            ticket.setCategory(aiResponse.getCategory());
            ticket.setPriority(aiResponse.getPriority());

            if (Boolean.TRUE.equals(aiResponse.getEscalationRequired())) {
                ticket.setIsEscalated(true);
            }

            // 4. Calculate SLA
            LocalDateTime deadline = slaService.calculateSlaDeadline(
                    aiResponse.getPriority(),
                    aiResponse.getUrgencyScore());
            ticket.setSlaDueAt(deadline);

            // 5. Team Assignment
            Optional<Team> matchedTeam = Optional.empty();
            if (aiResponse.getAssignedTeamName() != null) {
                matchedTeam = teamRepository.findByName(aiResponse.getAssignedTeamName());
            }
            if (matchedTeam.isEmpty() && aiResponse.getCategory() != null) {
                matchedTeam = teamRepository.findBySpecialization(aiResponse.getCategory());
            }
            matchedTeam.ifPresent(ticket::setAssignedTeam);

            ticket.setStatus(TicketStatus.ASSIGNED);
            Ticket savedTicket = ticketRepository.save(ticket);

            // 6. Persist AiDecision record
            String rawJson = "{}";
            try {
                if (aiResponse.getRawAnswers() != null) {
                    rawJson = objectMapper.writeValueAsString(aiResponse.getRawAnswers());
                }
            } catch (Exception ignored) {}

            AiDecision aiDecision = AiDecision.builder()
                    .ticket(savedTicket)
                    .suggestedCategory(aiResponse.getCategory())
                    .suggestedPriority(aiResponse.getPriority())
                    .suggestedTeamName(aiResponse.getAssignedTeamName())
                    .confidence(aiResponse.getConfidence() != null ? aiResponse.getConfidence() : 0.85)
                    .urgencyScore(aiResponse.getUrgencyScore() != null ? aiResponse.getUrgencyScore() : 50)
                    .escalationRecommended(aiResponse.getEscalationRequired())
                    .rawJsonResponse(rawJson)
                    .build();
            aiDecisionRepository.save(aiDecision);

            // 7. Record TicketEvent audit
            TicketEvent event = TicketEvent.builder()
                    .ticket(savedTicket)
                    .eventType("AI_TRIAGED")
                    .oldValue("AI_ANALYZING")
                    .newValue("ASSIGNED")
                    .description(String.format("Jev AI classified as %s (%s, %d%% confidence)",
                            aiResponse.getCategory(), aiResponse.getPriority(),
                            (int)((aiResponse.getConfidence() != null ? aiResponse.getConfidence() : 0.85) * 100)))
                    .performedBy("JEV_AI_AGENT")
                    .build();
            ticketEventRepository.save(event);

            log.info("Ticket #{} successfully triaged. Category: {}, Priority: {}, Team: {}",
                    ticketId, aiResponse.getCategory(), aiResponse.getPriority(),
                    ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : "Unassigned");

            // 8. Correlate with ongoing or new Major Incidents
            correlationService.correlateTicket(savedTicket);

        } catch (Exception e) {
            log.error("Unhandled error during async AI triage for ticket #{}: {}", ticketId, e.getMessage(), e);
            ticketRepository.findById(ticketId).ifPresent(t -> {
                t.setStatus(TicketStatus.NEEDS_HUMAN_REVIEW);
                ticketRepository.save(t);
            });
        }
    }
}
