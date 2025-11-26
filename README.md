# Energy Management System 

A complete, event‑driven microservices application built with Spring Boot, React, PostgreSQL, RabbitMQ, and Traefik. The system is structured around independent services for authentication, user management, device management, and energy monitoring, all exposed behind a single API gateway.

The backend is split into three core domain services (Auth, User Management, Device Management) plus a Monitoring service and a Device Data Simulator. Each service owns its own PostgreSQL database and communicates over HTTP (via Traefik) or asynchronously through RabbitMQ. User and device data are synchronized between services using a topic‑based event bus, ensuring loose coupling and eventual consistency.

The React frontend (served by Nginx in its own container) talks only to Traefik on port 80 and provides:
- Registration and login with JWT‑based authentication and role‑based access control (ADMIN / CLIENT).
- User and device management screens tailored to the current user’s role.
- Energy monitoring views where users (and admins) can inspect per‑device historical consumption as daily line or bar charts, powered by aggregated measurements stored by the Monitoring service.

## 🏗️ Docker Architecture Overview
![Alt text](/Deployment%20Diagram.png "Title")

**Key Points:**
- Frontend runs in Docker container on port 3000 (Nginx serving React build)
- Frontend makes API calls to Traefik on port 80
- Traefik routes backend requests to appropriate microservices
- All containers communicate via Docker network
- Frontend is NOT behind Traefik (accessed directly)

## 📁 Project Structure
```
.
├── Auth Microservice/                      # Authentication & Authorization Service
│   ├── src/main/java/com/example/auth_service/
│   │   ├── controllers/                   # REST API endpoints
│   │   ├── entities/                      # JPA entities
│   │   ├── repositories/                  # Database repositories
│   │   ├── services/                      # Business logic
│   │   ├── filters/                       # JWT authentication filter
│   │   ├── config/                        # Security, Swagger, RabbitMQ config
│   │   └── dtos/                          # Data Transfer Objects
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── User Management Microservice/           # User Profile Management Service
│   ├── src/main/java/org/example/user_management_microservice/
│   │   ├── controllers/
│   │   ├── entities/
│   │   ├── repositories/
│   │   ├── services/                      # Includes UserEventPublisher (user sync producer)
│   │   ├── filters/
│   │   ├── config/                        # Includes SyncRabbitConfig (topic exchange for sync)
│   │   └── dtos/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Device Management Microservice/         # IoT Device Management Service
│   ├── src/main/java/org/example/device_management_microservice/
│   │   ├── controllers/
│   │   ├── entities/                      # Device, User (local copy for sync)
│   │   ├── repositories/
│   │   ├── services/                      # DeviceService, DeviceSyncPublisher, SyncEventListener
│   │   ├── filters/
│   │   ├── config/                        # Security, Swagger, SyncRabbitConfig (topic exchange)
│   │   └── dtos/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Monitoring Microservice/                # Energy Monitoring & Aggregation Service
│   ├── src/main/java/org/example/monitoring/
│   │   ├── controllers/                   # MonitoringController (/monitoring/device/{id}/daily)
│   │   ├── entities/                      # HourlyEnergy, MonitoredDevice
│   │   ├── repositories/                  # HourlyEnergyRepository, MonitoredDeviceRepository
│   │   ├── services/                      # MonitoringService, MonitoringQueryService, DeviceSyncConsumer
│   │   ├── config/                        # SyncRabbitConfig (topic exchange), Rabbit/DB config
│   │   └── dtos/                          # HourlyEnergyDTO
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Device Data Simulator/                  # Synthetic IoT data generator
│   ├── src/main/java/org/example/device_simulator/
│   │   ├── services/                      # SimulatorService (sends to device.data.queue)
│   │   ├── config/                        # RabbitMQ config (queue name only)
│   │   └── models/                        # DeviceMeasurement, etc.
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Front/                                  # React Frontend
│   ├── public/
│   ├── src/
│   │   ├── components/                     # React components
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx               # Registration flow (auth + user profile)
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Users.jsx
│   │   │   ├── Devices.jsx                # Device list + "View Consumption" button
│   │   │   ├── DeviceConsumption.jsx      # Daily energy charts per device
│   │   │   ├── Profile.jsx
│   │   │   └── Navbar.jsx
│   │   ├── api/
│   │   │   └── axios.js                   # API configuration (baseURL: http://localhost)
│   │   ├── App.jsx                        # Routing (includes /devices/:deviceId/consumption)
│   │   └── App.css
│   ├── Dockerfile                         # Multi-stage build (build + nginx)
│   ├── nginx.conf                         # Nginx configuration
│   └── package.json
│
├── docker-compose.yml                      # All services (including RabbitMQ, Monitoring, Simulator)
└── README.md                               # Project documentation
```

## 🚀 Services Overview

### 1. **Authentication Service** (`/auth`)
- **Container**: `auth-service`
- **Port**: 8080 (internal)
- **Database**: `auth_microservice_db`
- **Responsibilities**:
  - User registration and login
  - JWT token generation and validation
  - Role management (ADMIN, CLIENT)
  - User role promotion/demotion

**Endpoints**:
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login (returns JWT)
- `GET /auth/users` - Get all users (Admin only)
- `PUT /auth/users/{username}/role` - Update user role (Admin only)
- `DELETE /auth/users/{username}` - Delete user (Admin only)

**Swagger**: http://localhost/auth/swagger-ui.html

### 2. **User Management Service** (`/users`)
- **Container**: `user-service`
- **Port**: 8080 (internal)
- **Database**: `user_management_microservice_db`
- **Responsibilities**:
  - User profile management
  - User details (email, address)
  - Profile updates

**Endpoints**:
- `POST /users` - Create user profile
- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `PUT /users/{id}` - Update user profile
- `DELETE /users/{id}` - Delete user (Admin only)

**Swagger**: http://localhost/users/swagger-ui.html

### 3. **Device Management Service** (`/devices`)
- **Container**: `device-service`
- **Port**: 8080 (internal)
- **Database**: `device_management_microservice_db`
- **Responsibilities**:
  - IoT device management
  - Device ownership tracking
  - Device CRUD operations

**Endpoints**:
- `POST /devices` - Create device
- `GET /devices` - Get all devices (Admin sees all, Client sees own)
- `GET /devices/{id}` - Get device by ID
- `PUT /devices/{id}` - Update device
- `DELETE /devices/{id}` - Delete device

**Swagger**: http://localhost/devices/swagger-ui.html

### 4. **React Frontend**
- **Container**: `ems-frontend`
- **Port**: 3000 (exposed)
- **Technology**: React 18 + Nginx
- **API Base URL**: http://localhost (Traefik)
- **Features**:
  - User authentication (login/register)
  - Dashboard
  - User management (Admin only)
  - Device management
  - Profile management
  - Role-based access control

### 5. Monitoring Service (`/monitoring`)
- **Container**: `monitoring-service`
- **Port**: 8080 (internal)
- **Database**: `monitoring_db`
- **Responsibilities**:
  - Consume raw device measurements from RabbitMQ
  - Aggregate energy data into hourly buckets per device
  - Expose historical consumption per device and day

**Endpoints**:
- `GET /monitoring/device/{deviceId}/daily?date=YYYY-MM-DD`  
  Returns 24 entries (hours 0–23) with total energy in kWh for each hour.


### 6. Device Data Simulator
- **Container**: `device-data-simulator`
- **Responsibilities**:
  - Generate realistic device energy measurements
  - Send JSON messages to the device data queue in RabbitMQ
- **Notes**:
  - Sends to a simple queue using the default exchange
  - Used for demo/testing, not exposed via HTTP

**Configuration**:
- Queue name: `device.data.queue`
- Producer: `rabbitTemplate.convertAndSend("device.data.queue", json)`
- Consumer: `@RabbitListener(queues = "device.data.queue")`


## 🔄 Event-Driven Synchronization

The system uses RabbitMQ and a topic exchange to synchronize devices and users between services.

### Exchanges, Queues, and Routing Keys

- **Exchange (Topic)**: `sync.events.exchange`

**Publishers and routing keys**:
- User Management Service:
  - Publishes user events
  - Routing key: `sync.user.created`
- Device Management Service:
  - Publishes device events
  - Routing key: `sync.device.created`

**Queues per service**:
- Device Management Service:
  - Queue: `sync.device-service.queue`
  - Binding: `sync.#` (receives all sync events)
- Monitoring Service:
  - Queue: `sync.monitoring-service.queue`
  - Binding: `sync.device.created` (receives only device events)

This avoids multiple consumers competing on the same queue and ensures each service gets the right events.

### User Sync Flow

Goal: keep a minimal copy of users in the Device Management DB (only username).

**Flow**:
1. Frontend:
   - Calls `/auth/register` (Auth Service creates credentials).
   - Calls `/auth/login` and gets JWT.
   - Calls `/users` (User Management Service creates user profile).

2. User Management Service:
   - After creating the profile, publishes a `USER.CREATED` event to `sync.events.exchange` with routing key `users`.
   - Event payload (simplified):  
     `{ "type": "USER", "event": "CREATED", "id": "<username>" }`

3. Device Management Service:
   - Listens on `sync.device-service.queue`.
   - For `USER.CREATED` events, checks if the username exists; if not, creates a `User` entity with that username.
   - Result: Device Service has a local `users` table to link devices to owners.

Frontend change:
- The manual call to `/devices/sync/users` in `Register.jsx` is no longer needed; sync is fully event-driven.

### Device Sync Flow

Goal: duplicate device IDs into the Monitoring DB to validate measurements and aggregate per device.

**Flow**:
1. Device Management Service:
   - After creating a device, publishes a `DEVICE.CREATED` event to `sync.events.exchange` with routing key `devices`.
   - Event payload:  
     `{ "type": "DEVICE", "event": "CREATED", "id": "<device-uuid>" }`

2. Monitoring Service:
   - Listens on `sync.monitoring-service.queue`.
   - For `DEVICE.CREATED` events, inserts the device ID into its local `devices` table if it does not exist.

3. When a new measurement arrives from the simulator:
   - Monitoring Service first checks if the device ID exists in the local `devices` table.
   - If not, it ignores or logs the measurement.
   - If yes, it aggregates into hourly energy records.


## 📡 Device Data & Monitoring

### Device Data Queue

- Queue: `device.data.queue`
- Exchange: default (empty name), using queue name only
- Producer (Simulator):
  - Sends JSON messages representing device measurements (deviceId, timestamp, energy kWh)
- Consumer (Monitoring Service):
  - Listens on `device.data.queue`
  - Converts incoming measurements into hourly aggregates stored in `monitoring_db`

### Monitoring API for Historical Consumption

- Endpoint: `GET /monitoring/device/{deviceId}/daily?date=YYYY-MM-DD`
- Returns: list of objects with:
  - `hour` (0–23)
  - `totalEnergy` (kWh for that hour)

Used by the frontend to render charts (line/bar) of daily energy consumption.

**Example response**:

```json
[
{ "hour": 0, "totalEnergy": 0.15 },
{ "hour": 1, "totalEnergy": 0.10 },
...
{ "hour": 23, "totalEnergy": 0.25 }
]

```

## 💻 Frontend: Energy Consumption Charts

The React frontend allows viewing per-device historical energy:

- On the Devices page:
  - Each device card has a “View Consumption” button.
  - This opens `/devices/:deviceId/consumption`.

- DeviceConsumption page:
  - Lets the user:
    - Pick a day from a date picker.
    - Toggle between line and bar chart.
  - Fetches data via:
    - `GET /monitoring/device/{deviceId}/daily?date=YYYY-MM-DD`
  - Displays:
    - 24-hour chart (OX = hours, OY = energy [kWh])
    - Total daily consumption
    - Peak hour
    - Average per hour

Role behavior:
- **ADMIN** can view consumption for any device.
- **CLIENT** sees only devices they own and their corresponding charts.

## 🔐 Authentication & Authorization

### JWT Token
- All protected endpoints require JWT Bearer token
- Token expiration: 15 minutes (900000ms)
- Token contains: username, role

### User Roles
- **ADMIN**: Full access to all resources
- **CLIENT**: Limited access (own profile and devices only)

### Authentication Flow
1. User registers via `/auth/register`
2. User logs in via `/auth/login` → receives JWT token
3. Frontend stores token in `localStorage`
4. Frontend includes token in `Authorization: Bearer <token>` header
5. Each microservice validates JWT independently

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.0
- **Language**: Java 17+
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15
- **ORM**: Hibernate/JPA
- **API Documentation**: Springdoc OpenAPI (Swagger) 2.7.0
- **Validation**: Jakarta Validation

### Frontend
- **Framework**: React 18
- **HTTP Client**: Axios
- **Router**: React Router DOM
- **Styling**: Custom CSS
- **Web Server**: Nginx (in Docker)

### Infrastructure
- **Reverse Proxy**: Traefik 2.10
- **Containerization**: Docker & Docker Compose
- **API Gateway**: Traefik on Port 80
- **Network**: Docker Bridge Network

## 📋 Prerequisites

- Docker (20.10+)
- Docker Compose (2.0+)
- Git

## ⚙️ Setup Instructions

### 1. Clone the Repository

```
git clone <repository-url>
cd <project-directory>
```

### 2. Configure Environment Variables

The `docker-compose.yml` already contains default configuration. For production, update these variables:

```
# In docker-compose.yml, update JWT_SECRET for all services
JWT_SECRET=your-secret-key-at-least-256-bits-long-here-change-in-production
```

### 3. Build and Start All Services

```
# Build all services (backend + frontend)
docker-compose build

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 4. Verify Services

Wait 30-60 seconds for all services to start, then check:

```
# Check if all containers are running
docker-compose ps

# Expected output: All services should show "Up" status
```

### 5. Access the Application

- **Frontend**: http://localhost:3000
- **Auth Service Swagger**: http://localhost/auth/swagger-ui.html
- **User Service Swagger**: http://localhost/users/swagger-ui.html
- **Device Service Swagger**: http://localhost/devices/swagger-ui.html


## 🎯 Usage Guide

### 1. Register a New User

1. Go to http://localhost:3000
2. Click "Register"
3. Enter username and password
4. Click "Register" button

### 2. Login

1. Enter credentials on login page
2. Click "Login"
3. You'll be redirected to Dashboard

### 3. Manage Users (Admin Only)

1. Navigate to "Users" from navbar
2. View all users
3. Edit user profiles
4. Promote/Demote users between CLIENT and ADMIN roles
5. Delete users

### 4. Manage Devices

1. Navigate to "Devices" from navbar
2. Click "Add Device" to create new device
3. Edit or delete existing devices
4. **CLIENT** users see only their devices
5. **ADMIN** users see all devices

### 5. Update Profile

1. Navigate to "Profile" from navbar
2. Click "Edit Profile"
3. Update email and address
4. Click "Save"

## 🔧 Docker Commands

### Start All Services
```
docker-compose up -d
```

### Stop All Services
```
docker-compose down
```

### Rebuild a Specific Service
```
# Backend service
docker-compose build --no-cache auth-service
docker-compose up -d auth-service

# Frontend
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

### View Logs
```
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f frontend
```

### Clean Up Everything
```
# Stop and remove containers, networks
docker-compose down

# Remove volumes (⚠️ deletes all data)
docker-compose down -v
```

## 🧪 Testing with Swagger

### 1. Access Swagger UI

All Swagger UIs are accessible through Traefik:
- **Auth Service**: http://localhost/auth/swagger-ui.html
- **User Service**: http://localhost/users/swagger-ui.html
- **Device Service**: http://localhost/devices/swagger-ui.html

### 2. Get JWT Token

1. Go to Auth Service Swagger: http://localhost/auth/swagger-ui.html
2. Find the `/auth/login` endpoint
3. Click "Try it out"
4. Enter credentials:
   ```
   {
     "username": "your-username",
     "password": "your-password"
   }
   ```
5. Click "Execute"
6. Copy the `token` from the response

### 3. Authorize in Swagger

1. Click the "Authorize" button (🔒 icon) at the top right
2. Enter: `Bearer <your-token>` (replace `<your-token>` with actual token)
3. Click "Authorize"
4. Click "Close"

### 4. Test Protected Endpoints

Now you can test all endpoints that require authentication across all three services!

## 📊 Database Schemas

### Auth Service Database

**Table: `auth_user`**
```
- id (UUID, PRIMARY KEY)
- username (VARCHAR, UNIQUE)
- password (VARCHAR, hashed with BCrypt)
- role (VARCHAR: ADMIN or CLIENT)
```

### User Service Database

**Table: `users`**
```
- id (UUID, PRIMARY KEY)
- username (VARCHAR, UNIQUE)
- email (VARCHAR)
- address (VARCHAR)
```

### Device Service Database

**Table: `device`**
```
- id (UUID, PRIMARY KEY)
- description (VARCHAR)
- address (VARCHAR)
- max_hourly_consumption (FLOAT)
- owner_username (VARCHAR)
```

## 🔄 API Request Flow Example

### Example: Client Creates a Device

```
1. Frontend (localhost:3000): 
   User clicks "Add Device" button
   
2. Frontend makes request: POST http://localhost/devices
   Headers: { Authorization: Bearer <jwt-token> }
   Body: { description: "Smart Light", address: "Living Room", ... }
   
3. Request goes to Traefik (localhost:80)
   
4. Traefik routes to: device-service:8080/devices
   
5. Device Service:
   - JWT Filter validates token
   - Extracts username and role from token
   - SecurityConfig checks @PreAuthorize permissions
   - Controller sets ownerUsername to current user (CLIENT)
   - DeviceService saves to database
   
6. Response: 201 Created → Traefik → Frontend
   
7. Frontend displays success message
```

