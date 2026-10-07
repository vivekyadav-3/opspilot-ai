package com.opspilot.repository;

import com.opspilot.entity.Incident;
import com.opspilot.entity.TicketCategory;
import com.opspilot.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatusNot(TicketStatus status);

    Optional<Incident> findFirstByCategoryAndStatusNotOrderByCreatedAtDesc(
            TicketCategory category,
            TicketStatus status);

    long countByStatusNot(TicketStatus status);
}
