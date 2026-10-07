package com.opspilot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalTickets;
    private long openTickets;
    private long criticalIncidents;
    private long slaAtRiskTickets;
    private long resolvedTickets;
    private long activeIncidents;

    // Breakdown for analytics charts
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByStatus;
}
