package com.opspilot.service;

import com.opspilot.dto.CreateTicketRequest;
import com.opspilot.dto.TicketResponse;
import com.opspilot.entity.*;
import com.opspilot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final AiDecisionRepository aiDecisionRepository;
    private final TicketEventRepository ticketEventRepository;
    private final TriageService triageService;
    private final SlaService slaService;

    public TicketResponse createTicket(CreateTicketRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // 1. Create ticket in initial NEW state
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(creator)
                .status(TicketStatus.NEW)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // Record creation event in audit timeline
        recordEvent(savedTicket, "TICKET_CREATED", null, "NEW", "Ticket created by " + creator.getName(),
                creator.getName());

        // 2. Trigger asynchronous Jev AI background triage
        triageService.processTicketAsync(savedTicket.getId());

        return mapToResponse(savedTicket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, TicketStatus newStatus, String performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);
        recordEvent(saved, "STATUS_CHANGE", oldStatus.name(), newStatus.name(), "Status updated to " + newStatus,
                performedBy);

        return mapToResponse(saved);
    }

    @Transactional
    public TicketResponse escalateTicket(Long ticketId, String reason, String performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        ticket.setIsEscalated(true);
        ticket.setPriority(Priority.CRITICAL);
        // Reduce SLA to 15 min on escalation
        ticket.setSlaDueAt(LocalDateTime.now().plusMinutes(15));

        Ticket saved = ticketRepository.save(ticket);
        recordEvent(saved, "ESCALATION", "false", "true", "Ticket escalated: " + reason, performedBy);

        return mapToResponse(saved);
    }

    @Transactional
    public TicketResponse assignTeam(Long ticketId, Long teamId, String performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        ticket.setAssignedTeam(team);
        ticket.setStatus(TicketStatus.ASSIGNED);

        Ticket saved = ticketRepository.save(ticket);
        recordEvent(saved, "ASSIGNMENT", null, team.getName(), "Assigned to " + team.getName(), performedBy);

        return mapToResponse(saved);
    }

    public List<TicketEvent> getTicketTimeline(Long ticketId) {
        return ticketEventRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        Long remainingMinutes = slaService.calculateRemainingMinutes(ticket);

        TicketResponse.TicketResponseBuilder builder = TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .createdById(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null)
                .createdByName(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getName() : null)
                .status(ticket.getStatus())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .incidentId(ticket.getIncident() != null ? ticket.getIncident().getId() : null)
                .slaDueAt(ticket.getSlaDueAt())
                .slaRemainingMinutes(remainingMinutes)
                .isEscalated(ticket.getIsEscalated())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt());

        // Include AI decision if available
        aiDecisionRepository.findByTicketId(ticket.getId()).ifPresent(ai -> {
            builder.aiConfidence(ai.getConfidence());
            builder.urgencyScore(ai.getUrgencyScore());
            builder.aiSuggestedTeam(ai.getSuggestedTeamName());
        });

        return builder.build();
    }

    private void recordEvent(Ticket ticket, String type, String oldVal, String newVal, String desc,
            String performedBy) {
        TicketEvent event = TicketEvent.builder()
                .ticket(ticket)
                .eventType(type)
                .oldValue(oldVal)
                .newValue(newVal)
                .description(desc)
                .performedBy(performedBy)
                .build();
        ticketEventRepository.save(event);
    }
}
