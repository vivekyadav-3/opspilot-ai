package com.opspilot.service;

import com.opspilot.entity.Incident;
import com.opspilot.entity.Priority;
import com.opspilot.entity.Ticket;
import com.opspilot.entity.TicketStatus;
import com.opspilot.repository.IncidentRepository;
import com.opspilot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentCorrelationService {

    private final TicketRepository ticketRepository;
    private final IncidentRepository incidentRepository;

    @Value("${opspilot.incident.correlation.threshold:3}")
    private int correlationThreshold;

    @Value("${opspilot.incident.correlation.window-minutes:30}")
    private int windowMinutes;

    /**
     * Checks if the newly classified ticket triggers or joins an active major
     * incident.
     */
    @Transactional
    public void correlateTicket(Ticket newTicket) {
        if (newTicket.getCategory() == null) {
            return;
        }

        // 1. Check if there's already an active Incident in this category
        Optional<Incident> activeIncidentOpt = incidentRepository
                .findFirstByCategoryAndStatusNotOrderByCreatedAtDesc(
                        newTicket.getCategory(),
                        TicketStatus.RESOLVED);

        if (activeIncidentOpt.isPresent()) {
            Incident incident = activeIncidentOpt.get();
            newTicket.setIncident(incident);
            ticketRepository.save(newTicket);

            incident.setAffectedUsersCount(incident.getAffectedUsersCount() + 1);
            incidentRepository.save(incident);

            log.info("Correlated Ticket #{} to existing Incident #{} ({})",
                    newTicket.getId(), incident.getId(), incident.getTitle());
            return;
        }

        // 2. Otherwise, check if recent unlinked tickets in this category hit the
        // threshold
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);
        List<Ticket> recentTickets = ticketRepository.findByCategoryAndCreatedAtAfterAndIncidentIsNull(
                newTicket.getCategory(),
                windowStart);

        if (recentTickets.size() >= correlationThreshold) {
            // Threshold reached! Create a new correlated Incident
            String incidentTitle = String.format("%s SERVICE OUTAGE / HIGH INCIDENT SPIKE", newTicket.getCategory());

            Incident incident = Incident.builder()
                    .title(incidentTitle)
                    .description(
                            String.format("Automated correlation: %d similar %s tickets reported within %d minutes.",
                                    recentTickets.size(), newTicket.getCategory(), windowMinutes))
                    .category(newTicket.getCategory())
                    .priority(Priority.CRITICAL)
                    .status(TicketStatus.IN_PROGRESS)
                    .assignedTeam(newTicket.getAssignedTeam())
                    .affectedUsersCount(recentTickets.size())
                    .build();

            Incident savedIncident = incidentRepository.save(incident);

            // Link all recent tickets to this new incident
            for (Ticket t : recentTickets) {
                t.setIncident(savedIncident);
                ticketRepository.save(t);
            }

            log.warn("🚨 AUTOMATIC INCIDENT CREATED: Incident #{} from {} correlated tickets",
                    savedIncident.getId(), recentTickets.size());
        }
    }
}
