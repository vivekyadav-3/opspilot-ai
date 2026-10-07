package com.opspilot.service;

import com.opspilot.dto.DashboardSummaryResponse;
import com.opspilot.entity.Priority;
import com.opspilot.entity.Ticket;
import com.opspilot.entity.TicketCategory;
import com.opspilot.entity.TicketStatus;
import com.opspilot.repository.IncidentRepository;
import com.opspilot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final IncidentRepository incidentRepository;

    public DashboardSummaryResponse getDashboardSummary() {
        long totalTickets = ticketRepository.count();

        // Count non-resolved tickets
        long openTickets = totalTickets
                - ticketRepository.countByStatus(TicketStatus.RESOLVED)
                - ticketRepository.countByStatus(TicketStatus.CLOSED);

        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED)
                + ticketRepository.countByStatus(TicketStatus.CLOSED);

        long criticalIncidents = ticketRepository.countByPriority(Priority.CRITICAL);

        // Tickets due within 30 minutes or already past due
        LocalDateTime atRiskThreshold = LocalDateTime.now().plusMinutes(30);
        long slaAtRiskTickets = ticketRepository.countSlaAtRiskOrBreachedTickets(atRiskThreshold);

        long activeIncidents = incidentRepository.countByStatusNot(TicketStatus.RESOLVED);

        // Aggregations for charts
        List<Ticket> allTickets = ticketRepository.findAll();
        Map<String, Long> categoryMap = new HashMap<>();
        Map<String, Long> statusMap = new HashMap<>();

        for (TicketCategory cat : TicketCategory.values()) {
            categoryMap.put(cat.name(), 0L);
        }
        for (TicketStatus stat : TicketStatus.values()) {
            statusMap.put(stat.name(), 0L);
        }

        for (Ticket t : allTickets) {
            if (t.getCategory() != null) {
                categoryMap.put(t.getCategory().name(), categoryMap.getOrDefault(t.getCategory().name(), 0L) + 1);
            }
            if (t.getStatus() != null) {
                statusMap.put(t.getStatus().name(), statusMap.getOrDefault(t.getStatus().name(), 0L) + 1);
            }
        }

        return DashboardSummaryResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .criticalIncidents(criticalIncidents)
                .slaAtRiskTickets(slaAtRiskTickets)
                .resolvedTickets(resolvedTickets)
                .activeIncidents(activeIncidents)
                .ticketsByCategory(categoryMap)
                .ticketsByStatus(statusMap)
                .build();
    }
}
