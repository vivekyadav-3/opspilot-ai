package com.opspilot.repository;

import com.opspilot.entity.AiDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiDecisionRepository extends JpaRepository<AiDecision, Long> {

    Optional<AiDecision> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);
}
