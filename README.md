# ⚡ OpsPilot AI: Autonomous IT Incident Command & Triage Platform

[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg?logo=react)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.0-purple.svg?logo=vite)](https://vitejs.dev/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

> **OpsPilot** is an autonomous IT incident command center that leverages asynchronous AI natural language understanding to reduce incident Mean Time to Acknowledge (**MTTA**) from 45 minutes to under **2 seconds**.

---

## 🌟 Key Capabilities

* 🧠 **Sub-Second Autonomous AI Triage**: Ingests unstructured technical incident reports, analyzes semantic intent, and assigns Category, Severity, Urgency Score, and Specialized Team routing in real time.
* 🚨 **Proactive Major Outage Correlation Engine**: Detects multi-ticket incident spikes within rolling time windows and automatically correlates them into clustered **Major Outages** with parent-child ticket associations.
* ⏱️ **Dynamic SLA Response Engine**: Dynamically calculates response deadlines based on quantitative urgency scores (15-minute Critical SLAs vs. standard tier).
* 🔄 **Lifecycle Audit Trail & Timeline**: Immutable event logging (`TICKET_CREATED`, `AI_TRIAGED`, `IN_PROGRESS`, `ESCALATED`, `RESOLVED`) for compliance, post-mortems, and audits.
* 🧑‍💻 **Human-in-the-Loop Incident Controls**: On-call engineers retain full override authority with quick-action controls (**Start Working**, **Resolve Incident**, **Escalate**).
* 🛡️ **Fault-Tolerant AI Architecture**: External cloud AI integration with resilient local semantic heuristic fallback mechanisms for zero downtime.

---

## 🏗️ Architecture Overview

```text
┌────────────────────────────────────────────────────────┐
│               OpsPilot React Frontend SPA              │
│       (Incident Command Dashboard · Dark Mode Glass)   │
└───────────────────────────┬────────────────────────────┘
                            │ REST / JSON
┌───────────────────────────▼────────────────────────────┐
│              Spring Boot 3 Enterprise Backend          │
│                                                        │
│  ┌────────────────────┐      ┌──────────────────────┐  │
│  │ Ticket Controller  │      │ Triage Thread Pool   │  │
│  │ & REST API Layer   │      │ (@Async TaskExec)    │  │
│  └─────────┬──────────┘      └──────────┬───────────┘  │
│            │                            │              │
│  ┌─────────▼──────────┐      ┌──────────▼───────────┐  │
│  │ Outage Correlation │      │ Jev AI Client Engine │  │
│  │ & Clustering       │      │ (Cloud + Fallback)   │  │
│  └─────────┬──────────┘      └──────────┬───────────┘  │
│            │                            │              │
│  ┌─────────▼────────────────────────────▼───────────┐  │
│  │  JPA Data Layer · Audit Timeline · Dynamic SLAs   │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## 📁 Repository Structure

```
opspilot-ai/
├── opspilot-backened/           # Spring Boot 3 Java Backend
│   ├── src/main/java/com/opspilot/
│   │   ├── config/              # Security, Async Thread Pools, Data Seed
│   │   ├── controller/          # REST Controllers (Tickets, Incidents, Dashboard)
│   │   ├── dto/                 # Request/Response Data Transfer Objects
│   │   ├── entity/              # JPA Entities (Ticket, Incident, Team, Event, AiDecision)
│   │   ├── repository/          # Spring Data JPA Repositories
│   │   └── service/             # Triage, SLA, Correlation & AI Client Services
│   └── pom.xml
├── opspilot-frontend/           # React 18 + Vite Frontend
│   ├── src/
│   │   ├── components/          # Dashboard Navbar, TicketTable, Modals, Stats
│   │   ├── pages/               # Incident Command Center, Correlated Outages
│   │   └── api.js               # Centralized Axios REST client
│   └── package.json
└── README.md
```

---

## 🚀 Quickstart Guide

### Prerequisites
* **Java 17+**
* **Maven 3.8+**
* **Node.js 18+** & **npm**

### 1. Backend Setup
```bash
cd opspilot-backened

# Set optional environment variables or use safe defaults
export JEV_API_KEY="your_api_key_here"

# Build and run
mvn clean spring-boot:run
```
*Backend API boots at `http://localhost:8080`.*
*Embedded H2 console accessible at `http://localhost:8080/h2-console`.*

### 2. Frontend Setup
```bash
cd opspilot-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```
*Frontend Command Dashboard runs at `http://localhost:5173`.*

---

## 🔒 Security & Privacy Notice
All production secrets and API tokens are parameterized through environment variables (`JEV_API_KEY`, `JWT_SECRET`). No secret keys or private credentials are committed to this repository.

---

## 📄 License
MIT © Vivek Yadav
