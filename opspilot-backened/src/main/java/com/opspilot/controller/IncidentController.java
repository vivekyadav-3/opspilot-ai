package com.opspilot.controller;

import com.opspilot.entity.Incident;
import com.opspilot.entity.TicketStatus;
import com.opspilot.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentRepository incidentRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllIncidents() {
        List<Incident> list = incidentRepository.findAll();
        List<Map<String, Object>> result = list.stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getIncidentById(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(inc -> ResponseEntity.ok(toMap(inc)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(@PathVariable Long id) {
        return incidentRepository.findById(id).map(incident -> {
            incident.setStatus(TicketStatus.RESOLVED);
            incident.setResolvedAt(LocalDateTime.now());
            Incident saved = incidentRepository.save(incident);
            return ResponseEntity.ok(toMap(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(Incident inc) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", inc.getId());
        m.put("title", inc.getTitle());
        m.put("description", inc.getDescription());
        m.put("category", inc.getCategory() != null ? inc.getCategory().name() : null);
        m.put("priority", inc.getPriority() != null ? inc.getPriority().name() : null);
        m.put("status", inc.getStatus() != null ? inc.getStatus().name() : null);
        m.put("assignedTeamName", inc.getAssignedTeam() != null ? inc.getAssignedTeam().getName() : null);
        m.put("ticketCount", inc.getAffectedUsersCount() != null ? inc.getAffectedUsersCount() : 1);
        m.put("detectedAt", inc.getCreatedAt());
        m.put("resolvedAt", inc.getResolvedAt());
        return m;
    }
}
