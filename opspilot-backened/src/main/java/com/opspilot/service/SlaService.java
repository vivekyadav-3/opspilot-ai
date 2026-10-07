package com.opspilot.service;

import com.opspilot.entity.Priority;
import com.opspilot.entity.Ticket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SlaService {

    /**
     * Calculates the SLA deadline based on AI-determined priority and urgency
     * score.
     */
    public LocalDateTime calculateSlaDeadline(Priority priority, Integer urgencyScore) {
        LocalDateTime now = LocalDateTime.now();
        int score = (urgencyScore != null) ? urgencyScore : 50;

        if (priority == Priority.CRITICAL) {
            // If extreme urgency (>85), SLA is 15 minutes, otherwise 30 minutes
            int minutes = (score >= 85) ? 15 : 30;
            return now.plusMinutes(minutes);
        } else if (priority == Priority.HIGH) {
            // High priority: 2 hours (or 1 hour if high urgency score)
            int hours = (score >= 70) ? 1 : 2;
            return now.plusHours(hours);
        } else if (priority == Priority.MEDIUM) {
            // Medium priority: 4 hours
            return now.plusHours(4);
        } else {
            // Low priority: 24 hours
            return now.plusHours(24);
        }
    }

    /**
     * Calculates remaining minutes before SLA breach.
     * Returns negative value if already breached.
     */
    public Long calculateRemainingMinutes(Ticket ticket) {
        if (ticket.getSlaDueAt() == null) {
            return null;
        }
        return Duration.between(LocalDateTime.now(), ticket.getSlaDueAt()).toMinutes();
    }

    /**
     * Returns true if SLA is within 30 minutes of breaching or already breached.
     */
    public boolean isSlaAtRisk(Ticket ticket) {
        Long remaining = calculateRemainingMinutes(ticket);
        return remaining != null && remaining <= 30;
    }
}
