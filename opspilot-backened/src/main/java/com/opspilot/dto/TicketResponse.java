package com.opspilot.dto;

import com.opspilot.entity.Priority;
import com.opspilot.entity.TicketCategory;
import com.opspilot.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private Long createdById;
    private String createdByName;
    private TicketStatus status;
    private TicketCategory category;
    private Priority priority;
    private Long assignedTeamId;
    private String assignedTeamName;
    private Long incidentId;
    private LocalDateTime slaDueAt;
    private Long slaRemainingMinutes;
    private Boolean isEscalated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    // AI Classification summary if present
    private Double aiConfidence;
    private Integer urgencyScore;
    private String aiSuggestedTeam;
}
