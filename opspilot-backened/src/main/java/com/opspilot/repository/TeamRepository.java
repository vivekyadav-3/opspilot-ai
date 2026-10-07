package com.opspilot.repository;

import com.opspilot.entity.Team;
import com.opspilot.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findBySpecialization(TicketCategory specialization);
    Optional<Team> findByName(String name);
}
