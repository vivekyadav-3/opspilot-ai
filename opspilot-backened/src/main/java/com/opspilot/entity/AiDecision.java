package com.opspilot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_decisions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory suggestedCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority suggestedPriority;

    private String suggestedTeamName;

    private String suggestedSlaTier;

    @Column(name = "escalation_recommended")
    private Boolean escalationRecommended;

    @Column(name = "urgency_score")
    private Integer urgencyScore;

    @Column(nullable = false)
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String rawJsonResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
