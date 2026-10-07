package com.opspilot.dto;

import com.opspilot.entity.Priority;
import com.opspilot.entity.TicketCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JevTriageResponse {
    private TicketCategory category;
    private Priority priority;
    private Double confidence;
    private String reasoning;
    private String assignedTeamName;
    private Boolean escalationRequired;
    private Integer urgencyScore;
    private Map<String, Object> rawAnswers;
}
