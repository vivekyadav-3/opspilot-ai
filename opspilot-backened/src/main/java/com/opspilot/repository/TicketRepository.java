package com.opspilot.repository;

import com.opspilot.entity.Priority;
import com.opspilot.entity.Ticket;
import com.opspilot.entity.TicketCategory;
import com.opspilot.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByAssignedTeamIdOrderByCreatedAtDesc(Long teamId);

    // Find recent tickets by category for correlation engine
    List<Ticket> findByCategoryAndCreatedAtAfterAndIncidentIsNull(
            TicketCategory category,
            LocalDateTime afterTime);

    // Count tickets by status
    long countByStatus(TicketStatus status);

    // Count tickets by priority
    long countByPriority(Priority priority);

    // Tickets where SLA is breached or at risk (due within 30 min and not resolved)
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND t.slaDueAt <= :threshold")
    List<Ticket> findSlaAtRiskOrBreachedTickets(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND t.slaDueAt <= :threshold")
    long countSlaAtRiskOrBreachedTickets(@Param("threshold") LocalDateTime threshold);
}
