package com.opspilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.dto.JevTriageRequest;
import com.opspilot.dto.JevTriageResponse;
import com.opspilot.entity.Priority;
import com.opspilot.entity.Ticket;
import com.opspilot.entity.TicketCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class JevClient {

    private static final Logger log = LoggerFactory.getLogger(JevClient.class);

    @Value("${jev.api.url}")
    private String jevApiUrl;

    @Value("${jev.api.key}")
    private String jevApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Classify a ticket using Jev AI. Falls back to heuristic if API fails.
     */
    public JevTriageResponse classify(Ticket ticket) {
        try {
            // Build state context for Jev
            Map<String, Object> state = new HashMap<>();
            state.put("title", ticket.getTitle());
            state.put("description", ticket.getDescription() != null ? ticket.getDescription() : "");
            if (ticket.getCreatedBy() != null) {
                state.put("userRole", ticket.getCreatedBy().getRole().name());
            }

            // Define structured Jev primitives
            Map<String, Object> questions = new HashMap<>();

            questions.put("category", Map.of(
                    "type", "choice",
                    "options", List.of("NETWORK", "SOFTWARE", "HARDWARE", "SECURITY",
                            "DATABASE", "EMAIL", "VPN", "PERFORMANCE", "ACCESS", "OTHER")));

            questions.put("priority", Map.of(
                    "type", "choice",
                    "options", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")));

            questions.put("assigned_team", Map.of(
                    "type", "choice",
                    "options", List.of("Network Operations", "DevOps", "IT Support",
                            "Database Administration", "Security Operations")));

            questions.put("escalate", Map.of(
                    "type", "noul",
                    "statement", "This ticket is critical and requires immediate escalation"));

            questions.put("urgency_score", Map.of(
                    "type", "score",
                    "rubric", "Rate urgency from 0 (minor request) to 100 (complete business-critical outage)"));

            JevTriageRequest request = JevTriageRequest.builder()
                    .state(state)
                    .questions(questions)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jevApiKey);

            HttpEntity<JevTriageRequest> entity = new HttpEntity<>(request, headers);

            String endpoint = jevApiUrl.contains("/systemone") ? jevApiUrl : (jevApiUrl + "/v1/triage");
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    endpoint,
                    entity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Jev AI triage success for ticket #{}", ticket.getId());
                return parseJevResponse(response.getBody());
            }
        } catch (Exception ex) {
            log.warn("Jev AI API call failed for ticket #{}: {}. Using heuristic fallback.",
                    ticket.getId(), ex.getMessage());
        }

        return fallbackClassification(ticket);
    }

    @SuppressWarnings("unchecked")
    private JevTriageResponse parseJevResponse(Map<String, Object> body) {
        try {
            Map<String, Object> answers = (Map<String, Object>) body.getOrDefault("answers", Collections.emptyMap());

            String catStr = (String) answers.getOrDefault("category", "OTHER");
            String priStr = (String) answers.getOrDefault("priority", "MEDIUM");
            String teamStr = (String) answers.getOrDefault("assigned_team", "IT Support");
            Boolean escalate = Boolean.parseBoolean(String.valueOf(answers.getOrDefault("escalate", "false")));
            Number urgency = (Number) answers.getOrDefault("urgency_score", 50);
            Number conf = (Number) body.getOrDefault("confidence", 0.85);

            return JevTriageResponse.builder()
                    .category(TicketCategory.valueOf(catStr.toUpperCase()))
                    .priority(Priority.valueOf(priStr.toUpperCase()))
                    .assignedTeamName(teamStr)
                    .escalationRequired(escalate)
                    .urgencyScore(urgency.intValue())
                    .confidence(conf.doubleValue())
                    .rawAnswers(answers)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Jev AI structured response: {}", e.getMessage());
            return fallbackClassification(null);
        }
    }

    private JevTriageResponse fallbackClassification(Ticket ticket) {
        String text = ticket != null
                ? (ticket.getTitle() + " " + (ticket.getDescription() != null ? ticket.getDescription() : "")).toLowerCase()
                : "";

        TicketCategory category = TicketCategory.SOFTWARE;
        Priority priority = Priority.MEDIUM;
        String team = "IT Support";
        int urgency = 45;
        boolean escalate = false;

        // Check for outage / critical crash terms first
        boolean isSevereOutage = text.contains("oom") || text.contains("crash") || text.contains("downtime")
                || text.contains("down") || text.contains("outage") || text.contains("100%") || text.contains("unresponsive");

        if (text.contains("redis") || text.contains("cache") || text.contains("db") || text.contains("database") || text.contains("sql") || text.contains("postgres") || text.contains("mongo")) {
            category = TicketCategory.DATABASE;
            priority = isSevereOutage ? Priority.CRITICAL : Priority.HIGH;
            team = isSevereOutage ? "Database Administration" : "Database Administration";
            urgency = isSevereOutage ? 95 : 75;
            escalate = isSevereOutage;
        } else if (text.contains("vpn") || text.contains("network") || text.contains("wifi") || text.contains("dns") || text.contains("gateway")) {
            category = TicketCategory.NETWORK;
            priority = isSevereOutage ? Priority.CRITICAL : Priority.HIGH;
            team = "Network Operations";
            urgency = isSevereOutage ? 95 : 80;
            escalate = isSevereOutage;
        } else if (text.contains("hack") || text.contains("breach") || text.contains("phishing") || text.contains("security") || text.contains("cve")) {
            category = TicketCategory.SECURITY;
            priority = Priority.CRITICAL;
            team = "Security Operations";
            urgency = 98;
            escalate = true;
        } else if (text.contains("password") || text.contains("login") || text.contains("access") || text.contains("locked") || text.contains("2fa")) {
            category = TicketCategory.ACCESS;
            priority = Priority.LOW;
            team = "IT Support";
            urgency = 30;
        } else if (text.contains("email") || text.contains("outlook") || text.contains("mail")) {
            category = TicketCategory.EMAIL;
            priority = Priority.MEDIUM;
            team = "IT Support";
            urgency = 40;
        } else if (text.contains("slow") || text.contains("performance") || text.contains("lag") || text.contains("latency") || text.contains("cpu") || text.contains("memory")) {
            category = TicketCategory.PERFORMANCE;
            priority = isSevereOutage ? Priority.CRITICAL : Priority.MEDIUM;
            team = "DevOps";
            urgency = isSevereOutage ? 90 : 55;
            escalate = isSevereOutage;
        } else if (text.contains("hardware") || text.contains("laptop") || text.contains("monitor") || text.contains("printer")) {
            category = TicketCategory.HARDWARE;
            priority = Priority.MEDIUM;
            team = "IT Support";
            urgency = 40;
        } else if (isSevereOutage) {
            category = TicketCategory.SOFTWARE;
            priority = Priority.CRITICAL;
            team = "DevOps";
            urgency = 90;
            escalate = true;
        }

        return JevTriageResponse.builder()
                .category(category)
                .priority(priority)
                .assignedTeamName(team)
                .escalationRequired(escalate)
                .urgencyScore(urgency)
                .confidence(0.88)
                .reasoning("Determined via OpsPilot keyword heuristics (Jev AI unavailable)")
                .rawAnswers(Map.of("note", "Heuristic fallback"))
                .build();
    }
}
