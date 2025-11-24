# Microservices Application

A complete microservices-based application built with Spring Boot, React, PostgreSQL, and Traefik reverse proxy. The system includes authentication, user management, and IoT device management services with role-based access control.

## 🏗️ Docker Architecture Overview
![Alt text](/Deployment%20Diagram.png "Title")
```
┌─────────────────────────────────────────────────────────────────┐
│                       React Frontend                            │
│                      (Docker Container)                         │
│                          Port 3000                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP Requests
                            │ (localhost:80)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Traefik Reverse Proxy                        │
│                     (API Gateway - Port 80)                     │
└───────────────┬──────────────┬──────────────┬───────────────────┘
                │              │              │              
                │ /auth/*      │ /users/*     │ /devices/*   
                ▼              ▼              ▼              
            ┌─────────────┐ ┌─────────────┐ ┌─────────────┐     
            │    Auth     │ │    User     │ │   Device    │     
            │  Service    │ │  Service    │ │  Service    │   
            │  :8080      │ │  :8080      │ │  :8080      │   
            └──────┬──────┘ └──────┬──────┘ └──────┬──────┘   
                   │               │               │           
                   │ PostgreSQL    │ PostgreSQL    │ PostgreSQL
                   ▼               ▼               ▼           
            ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   
            │   Auth DB   │ │   User DB   │ │  Device DB  │   
            │  :5432      │ │  :5432      │ │  :5432      │   
            └─────────────┘ └─────────────┘ └─────────────┘   
All containers run in: microservices-network (Docker Network)
```

**Key Points:**
- Frontend runs in Docker container on port 3000 (Nginx serving React build)
- Frontend makes API calls to Traefik on port 80
- Traefik routes backend requests to appropriate microservices
- All containers communicate via Docker network
- Frontend is NOT behind Traefik (accessed directly)

## 📁 Project Structure

```
.
├── Auth Microservice/              # Authentication & Authorization Service
│   ├── src/main/java/com/example/auth_service/
│   │   ├── controllers/           # REST API endpoints
│   │   ├── entities/              # JPA entities
│   │   ├── repositories/          # Database repositories
│   │   ├── services/              # Business logic
│   │   ├── filters/               # JWT authentication filter
│   │   ├── config/                # Security & Swagger config
│   │   └── dtos/                  # Data Transfer Objects
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── User Management Microservice/   # User Profile Management Service
│   ├── src/main/java/org/example/user_management_microservice/
│   │   ├── controllers/
│   │   ├── entities/
│   │   ├── repositories/
│   │   ├── services/
│   │   ├── filters/
│   │   ├── config/
│   │   └── dtos/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Device Management Microservice/ # IoT Device Management Service
│   ├── src/main/java/org/example/device_management_microservice/
│   │   ├── controllers/
│   │   ├── entities/
│   │   ├── repositories/
│   │   ├── services/
│   │   ├── filters/
│   │   ├── config/
│   │   └── dtos/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── Front/                          # React Frontend
│   ├── public/
│   ├── src/
│   │   ├── components/            # React components
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Users.jsx
│   │   │   ├── Devices.jsx
│   │   │   ├── Profile.jsx
│   │   │   └── Navbar.jsx
│   │   ├── api/
│   │   │   └── axios.js           # API configuration (baseURL: http://localhost)
│   │   ├── App.jsx
│   │   └── App.css
│   ├── Dockerfile                 # Multi-stage build (build + nginx)
│   ├── nginx.conf                 # Nginx configuration
│   └── package.json
│
├── docker-compose.yml              # All services configuration
└── README.md                       # This file
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

