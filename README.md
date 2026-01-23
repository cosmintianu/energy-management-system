# Energy Management System

A distributed, event-driven microservices platform for managing IoT energy devices, monitoring real-time consumption, and providing intelligent support through AI-powered chat. Built with Spring Boot, React, PostgreSQL, RabbitMQ, and Traefik.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Services](#services)
- [Event-Driven Communication](#event-driven-communication)
- [Authentication](#authentication)
- [Monitoring Pipeline](#monitoring-pipeline)
- [Deployment](#deployment)
- [Database Schemas](#database-schemas)

---

## Overview

The Energy Management System enables users to register IoT energy devices, monitor their consumption in real-time, and receive automated alerts when devices exceed configured thresholds. The platform is built on a microservices architecture with the following key capabilities:

- **User Authentication** — JWT-based authentication with role-based access control (Admin/Client)
- **Device Management** — Full CRUD operations for IoT devices with ownership tracking
- **Real-time Monitoring** — Live energy consumption tracking with hourly aggregation
- **Smart Alerts** — Automatic overconsumption notifications delivered via WebSocket
- **AI Support** — Integrated Google Gemini-powered support assistant
- **Analytics** — Historical consumption charts with daily breakdowns

---

## Architecture

![Deployment Diagram](./Deployment%20Diagram.png)

### Design Decisions

| Decision | Rationale |
|----------|-----------|
| Separate databases per service | Data isolation and independent scaling |
| Topic-based sync exchange | Services subscribe only to relevant events |
| Hash-based load balancing | Consistent device-to-replica routing for aggregation |
| Shared monitoring database | All replicas need atomic hourly aggregation |
| WebSocket for notifications | Real-time push without polling overhead |

---

## Tech Stack

### Backend

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.4.0 |
| Language | Java 21 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 16 |
| ORM | Hibernate / JPA |
| API Docs | Springdoc OpenAPI 2.7.0 |
| Messaging | RabbitMQ 3.x |

### Frontend

| Component | Technology |
|-----------|------------|
| Framework | React 18.2 |
| Build Tool | Vite 7.2 |
| HTTP Client | Axios 1.6 |
| Routing | React Router 6.20 |
| Charts | Recharts 3.5 |
| Server | Nginx |

### WebSocket & AI Service

| Component | Technology |
|-----------|------------|
| Framework | FastAPI |
| Async Messaging | aio-pika |
| AI Integration | Google Gemini 2.5 Flash |

### Infrastructure

| Component | Technology |
|-----------|------------|
| API Gateway | Traefik 3.6 |
| Containers | Docker + Docker Compose |
| CI/CD | GitLab CI |
| Cloud | AWS EC2 |

---

## Getting Started

### Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Git

### Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd <project-directory>

# Build and start all services
docker-compose up -d --build

# Verify all containers are running (wait ~60 seconds)
docker-compose ps
```

### Access Points

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Traefik Dashboard | http://localhost:8081 |
| RabbitMQ Management | http://localhost:15672 |
| Auth API Docs | http://localhost/auth/swagger-ui.html |
| User API Docs | http://localhost/users/swagger-ui.html |
| Device API Docs | http://localhost/devices/swagger-ui.html |

---

## Services

### Auth Service

Handles user credentials, JWT generation, and role management.

**Base Path:** `/auth`

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/register` | POST | No | Create new account |
| `/login` | POST | No | Authenticate, receive JWT |
| `/users` | GET | Admin | List all users |
| `/users/{username}/role` | PUT | Admin | Update user role |
| `/users/{username}` | DELETE | Admin | Delete user |

### User Service

Manages user profiles separate from authentication credentials.

**Base Path:** `/users`

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/` | POST | Yes | Create profile |
| `/` | GET | Yes | List all profiles |
| `/{id}` | GET | Yes | Get profile by ID |
| `/{id}` | PUT | Yes | Update profile |
| `/{id}` | DELETE | Admin | Delete profile |

### Device Service

CRUD operations for IoT energy devices with ownership tracking.

**Base Path:** `/devices`

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/` | POST | Yes | Register device |
| `/` | GET | Yes | List devices (filtered by role) |
| `/{id}` | GET | Yes | Get device details |
| `/{id}` | PUT | Yes | Update device |
| `/{id}` | DELETE | Yes | Remove device |

### Monitoring Service

Aggregates energy data and exposes historical consumption.

**Base Path:** `/monitoring`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/device/{id}/daily?date=YYYY-MM-DD` | GET | Get 24-hour consumption |

**Response Format:**
```json
[
  { "hour": 0, "totalEnergy": 0.15 },
  { "hour": 1, "totalEnergy": 0.10 },
  ...
  { "hour": 23, "totalEnergy": 0.25 }
]
```

### WebSocket Service

Real-time notifications and AI-powered support chat.

**Base URL:** `ws://localhost:8000`

| Room | Path | Purpose |
|------|------|---------|
| Notifications | `/ws/notifications` | Overconsumption alerts |
| Chat | `/ws/chat` | User messaging |
| Support | `/ws/support` | AI assistant |

---

## Event-Driven Communication

### RabbitMQ Topology

```
                      sync.events.exchange (topic)
                                 │
            ┌────────────────────┼────────────────────┐
            │                    │                    │
    sync.user.*    sync.device.*           (bindings)
            │                    │
            ▼                    ▼
   sync.device-svc.queue   sync.monitoring-svc.queue
            │                    │
            ▼                    ▼
     Device Service        Monitoring Service
```

### User Sync Flow

1. User Management Service creates a profile
2. Publishes `USER.CREATED` event with username
3. Device Service receives event and creates local user record
4. Devices can now be linked to owners

### Device Sync Flow

1. Device Service creates/updates/deletes a device
2. Publishes `DEVICE.CREATED|UPDATED|DELETED` event
3. Monitoring Service receives and updates its local device table
4. Enables overconsumption threshold checking per device

---

## Authentication

### JWT Flow

1. User calls `POST /auth/login` with credentials
2. Auth Service validates and returns JWT (15-minute TTL)
3. Frontend stores token in `localStorage`
4. All subsequent API calls include `Authorization: Bearer <token>`
5. Each microservice validates the JWT independently

### Roles

| Role | Capabilities |
|------|-------------|
| **Admin** | Full access to all resources and users |
| **Client** | Own profile and devices only |

---

## Monitoring Pipeline

### Data Flow

```
Device Simulator
       │
       ▼
simulator.data.queue
       │
       ▼
Load Balancer Service ─── hash(deviceId) % 3
       │
       ├─── monitoring.ingestion.queue.1 ─── Monitoring Replica 1
       ├─── monitoring.ingestion.queue.2 ─── Monitoring Replica 2
       └─── monitoring.ingestion.queue.3 ─── Monitoring Replica 3
                                                      │
                                                      ▼
                                              monitoring_db (shared)
                                                      │
                                            (if threshold exceeded)
                                                      ▼
                                         notif.overconsumption.queue
                                                      │
                                                      ▼
                                            WebSocket Service
                                                      │
                                                      ▼
                                            Connected Clients
```

### Overconsumption Notification Payload

```json
{
  "deviceId": "550e8400-e29b-41d4-a716-446655440000",
  "hourStart": "2025-01-23T14:00:00Z",
  "currentConsumption": 2.5,
  "maxAllowed": 2.0,
  "ownerUsername": "john_doe",
  "message": "Device exceeded hourly consumption limit"
}
```

---

## Deployment

### Local Development

```bash
docker-compose up -d --build
```

### Production

The `docker-compose.prod.yml` uses pre-built images from GitLab Container Registry:

```bash
docker login registry.gitlab.com
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Shared secret for JWT signing |
| `JWT_EXPIRATION` | Token TTL in milliseconds (default: 900000) |
| `DB_PASSWORD` | PostgreSQL password |
| `RABBITMQ_HOST` | Message broker hostname |
| `GEMINI_API_KEY` | Google Gemini API key (optional) |

### CI/CD Pipeline

The GitLab CI pipeline includes four stages:

1. **Build** — Compile all services (Maven/npm)
2. **Test** — Run unit tests with PostgreSQL/RabbitMQ containers
3. **Docker** — Build and push images to GitLab Registry
4. **Deploy** — SSH to AWS EC2, pull images, restart containers (manual trigger)

---

## Database Schemas

### Auth Service

```sql
CREATE TABLE auth_user (
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50) NOT NULL
);
```

### User Service

```sql
CREATE TABLE users (
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(255),
    address     VARCHAR(255)
);
```

### Device Service

```sql
CREATE TABLE device (
    id              UUID PRIMARY KEY,
    description     VARCHAR(255),
    address         VARCHAR(255),
    max_consumption FLOAT,
    owner_username  VARCHAR(255)
);
```

### Monitoring Service

```sql
CREATE TABLE monitored_devices (
    device_id       VARCHAR(255) PRIMARY KEY,
    max_consumption FLOAT,
    owner_username  VARCHAR(255)
);

CREATE TABLE hourly_energy (
    id              BIGSERIAL PRIMARY KEY,
    device_id       VARCHAR(255) NOT NULL,
    hour_start      TIMESTAMP NOT NULL,
    total_energy    FLOAT NOT NULL
);
```

---

## Useful Commands

```bash
# View logs for a specific service
docker-compose logs -f auth-service

# Restart a single service
docker-compose restart device-service

# Stop and remove all containers
docker-compose down

# Reset everything including data
docker-compose down -v
```

---

## Project Structure

```
.
├── Auth Microservice/           # JWT authentication
├── User Management Microservice/# User profiles
├── Device Management Microservice/# IoT devices
├── Monitoring Microservice/     # Energy aggregation
├── Load Balancer Service/       # Hash-based routing
├── Device Data Simulator/       # Test data generator
├── Websocket and Support Service/# Notifications + AI chat
├── Frontend/                    # React SPA
├── docker-compose.yml           # Local development
├── docker-compose.prod.yml      # Production
└── .gitlab-ci.yml               # CI/CD pipeline
```

---

*Developed as part of the Distributed Systems course at Technical University of Cluj-Napoca.*
