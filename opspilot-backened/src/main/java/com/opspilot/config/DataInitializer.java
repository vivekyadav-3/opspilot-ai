package com.opspilot.config;

import com.opspilot.entity.*;
import com.opspilot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users...");
            User employee = User.builder()
                    .name("Sarah Chen")
                    .email("sarah.chen@company.com")
                    .password(passwordEncoder.encode("password123"))
                    .department("Engineering")
                    .role(UserRole.EMPLOYEE)
                    .build();
            userRepository.save(employee);

            User agent = User.builder()
                    .name("Alex Mercer")
                    .email("alex.mercer@company.com")
                    .password(passwordEncoder.encode("password123"))
                    .department("IT Operations")
                    .role(UserRole.IT_AGENT)
                    .build();
            userRepository.save(agent);

            User manager = User.builder()
                    .name("Elena Rostova")
                    .email("elena.r@company.com")
                    .password(passwordEncoder.encode("admin123"))
                    .department("Infrastructure")
                    .role(UserRole.IT_MANAGER)
                    .build();
            userRepository.save(manager);
        }

        if (teamRepository.count() == 0) {
            log.info("Seeding IT resolution teams...");
            teamRepository.save(Team.builder()
                    .name("Network Operations")
                    .description("L2/L3 Network, Routing, VPN and Connectivity Team")
                    .specialization(TicketCategory.NETWORK)
                    .build());

            teamRepository.save(Team.builder()
                    .name("Database Administration")
                    .description("PostgreSQL, Oracle, and Redis high-availability cluster tier")
                    .specialization(TicketCategory.DATABASE)
                    .build());

            teamRepository.save(Team.builder()
                    .name("Security Operations")
                    .description("SOC, Identity Access, threat mitigation and incident response")
                    .specialization(TicketCategory.SECURITY)
                    .build());

            teamRepository.save(Team.builder()
                    .name("IT Support")
                    .description("Global Helpdesk, workstation hardware, user accounts and onboarding")
                    .specialization(TicketCategory.HARDWARE)
                    .build());

            teamRepository.save(Team.builder()
                    .name("DevOps")
                    .description("Kubernetes, CI/CD pipelines, cloud infra and telemetry")
                    .specialization(TicketCategory.SOFTWARE)
                    .build());
        }

        if (ticketRepository.count() == 0) {
            log.info("Seeding demonstration tickets for instant dashboard overview...");
            User reporter = userRepository.findAll().get(0);
            Team netTeam = teamRepository.findByName("Network Operations").orElse(null);
            Team dbTeam = teamRepository.findByName("Database Administration").orElse(null);

            Ticket t1 = Ticket.builder()
                    .title("Frankfurt DataCenter gateway packet loss spiking to 40%")
                    .description("Intermittent connectivity to European RDS cluster. Latency has increased from 12ms to 450ms across all worker nodes.")
                    .createdBy(reporter)
                    .status(TicketStatus.IN_PROGRESS)
                    .category(TicketCategory.NETWORK)
                    .priority(Priority.CRITICAL)
                    .assignedTeam(netTeam)
                    .slaDueAt(LocalDateTime.now().plusMinutes(25))
                    .isEscalated(true)
                    .build();
            ticketRepository.save(t1);

            Ticket t2 = Ticket.builder()
                    .title("PostgreSQL production read-replica timeout on billing queries")
                    .description("Connection pool exhaustion observed on pool-us-east-1. Several API endpoints returning 504 Gateway Timeout.")
                    .createdBy(reporter)
                    .status(TicketStatus.ASSIGNED)
                    .category(TicketCategory.DATABASE)
                    .priority(Priority.HIGH)
                    .assignedTeam(dbTeam)
                    .slaDueAt(LocalDateTime.now().plusHours(2))
                    .isEscalated(false)
                    .build();
            ticketRepository.save(t2);

            Ticket t3 = Ticket.builder()
                    .title("Single Sign-On (SSO) redirect loop for remote workers")
                    .description("Okta SAML assertions failing intermittently with 401 Unauthorized for developers connecting from home.")
                    .createdBy(reporter)
                    .status(TicketStatus.ASSIGNED)
                    .category(TicketCategory.ACCOUNT_ACCESS)
                    .priority(Priority.MEDIUM)
                    .assignedTeam(teamRepository.findByName("Security Operations").orElse(null))
                    .slaDueAt(LocalDateTime.now().plusHours(4))
                    .isEscalated(false)
                    .build();
            ticketRepository.save(t3);
        }
    }
}
