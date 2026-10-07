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
public class JevTriageRequest {
    private String ticketId;
    private String title;
    private String description;
    private Map<String, Object> state;
    private Map<String, Object> questions;

    public JevTriageRequest(Long ticketId, String title, String description) {
        this.ticketId = String.valueOf(ticketId);
        this.title = title;
        this.description = description;
    }
}
