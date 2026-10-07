package com.opspilot.controller;

import com.opspilot.dto.CreateTicketRequest;
import com.opspilot.dto.TicketResponse;
import com.opspilot.entity.TicketEvent;
import com.opspilot.entity.TicketStatus;
import com.opspilot.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @RequestBody CreateTicketRequest request,
            @RequestParam(defaultValue = "1") Long userId) {
        TicketResponse response = ticketService.createTicket(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        String performedBy = payload.getOrDefault("performedBy", "SUPPORT_AGENT");
        TicketStatus status = TicketStatus.valueOf(statusStr.toUpperCase());

        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status, performedBy));
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<TicketResponse> escalateTicket(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload) {
        String reason = (payload != null) ? payload.getOrDefault("reason", "Manual escalation") : "Manual escalation";
        String performedBy = (payload != null) ? payload.getOrDefault("performedBy", "TEAM_LEAD") : "TEAM_LEAD";

        return ResponseEntity.ok(ticketService.escalateTicket(id, reason, performedBy));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTeam(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Long teamId = Long.valueOf(payload.get("teamId").toString());
        String performedBy = payload.getOrDefault("performedBy", "ADMIN").toString();

        return ResponseEntity.ok(ticketService.assignTeam(id, teamId, performedBy));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<TicketEvent>> getTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketTimeline(id));
    }
}
