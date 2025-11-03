# Shopifake Gateway

The API Gateway for the Shopifake microservices architecture. Routes requests to backend services with built-in CORS policy, health monitoring, and API documentation.

## Features

- **Spring Boot 3.5.6** with Java 21
- **Spring Cloud Gateway** for API routing
- **Profile-aware CORS policy** (see [CORS Documentation](docs/CORS-CONFIGURATION.md))
- **Actuator** for health monitoring
- **Swagger/OpenAPI** for API documentation
- **Lombok** for cleaner code
- **GitHub Actions** CI/CD with Test, Checkstyle, JaCoCo
- **Multi-stage Docker** build
- **DevContainer** ready for DevPod / VS Code Remote / Cursor (works locally with IntelliJ too)

## Quick Start

> 💡 **DevContainer:** This template works with DevPod / VS Code Remote Containers - clone this template and create workspace from Git.

### 1. Clone and Setup

```bash
git clone <this-repo>
cd shopifake-gateway
```

### 2. Customize

- Update `pom.xml`: `groupId`, `artifactId`, `name`, `description`
- Rename package from `com.shopifake.microservice` to your own

### 3. Run

```bash
./mvnw spring-boot:run
```

### 4. Access

- Health: `http://localhost:8080/actuator/health`
- Swagger: `http://localhost:8080/swagger-ui.html`

## Environment Profiles

| Profile | CORS | Logging | Use Case |
|---------|------|---------|----------|
| **dev** | Permissive (`*`) | DEBUG | Local development |
| **test** | Permissive (`*`) | WARN | Unit/Integration tests |
| **prod** | Strict (configured origins) | WARN | Production |

### CORS Configuration

The gateway uses a **profile-aware CORS policy** that adapts to each environment:

**Development (`dev` profile):**
- Allows all origins (`*`)
- Allows all headers
- No credentials required
- Perfect for local testing with any frontend

**Test (`test` profile):**
- Same permissive policy as dev
- Allows integration tests from any origin

**Production (`prod` profile):**
- **Strict origin control** via `CORS_ALLOWED_ORIGINS` env var
- Supports single or multiple origins (comma-separated)
- Limited headers: `Authorization`, `Content-Type`
- Credentials support configurable via `CORS_ALLOW_CREDENTIALS`

Example production values:
```yaml
# Single origin
CORS_ALLOWED_ORIGINS=https://app.example.com

# Multiple origins
CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com,https://mobile.example.com

# Allow credentials (cookies, auth headers)
CORS_ALLOW_CREDENTIALS=true
```

## Configuration

### Environment Configuration

**Local Development:**
- No additional configuration required
- Gateway runs on port 8080 by default

**Production:**
- Environment variables injected via Kubernetes

### Key Variables

**Production/Staging (Kubernetes):**
```yaml
# Example ConfigMap
env:
  - name: PORT
    value: "8080"
  - name: CORS_ALLOWED_ORIGINS
    value: "https://prod-app.example.com"
  - name: CORS_ALLOW_CREDENTIALS
    value: "false"
```

## Development

### Commands

```bash
# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Build
./mvnw clean package

# Code quality
./mvnw checkstyle:check
./mvnw jacoco:report
```

## Docker

### Build & Run

```bash
docker build -t shopifake-gateway .

docker run -p 8080:8080 \
  -e CORS_ALLOWED_ORIGINS=https://app.example.com \
  shopifake-gateway
```

## CI/CD

### Pipeline Includes

- ✅ Linting (Checkstyle)
- ✅ Testing with coverage (JaCoCo)
- ✅ Maven build
- ✅ Docker build & push to GitHub Container Registry

### Setup

1. **Settings → Actions → General**
2. Enable **"Read and write permissions"**

## Project Structure

```
src/main/
├── java/com/shopifake/microservice/
│   ├── Application.java
│   └── config/
│       └── CorsConfig.java
└── resources/
    ├── application.yml                     # Base config
    ├── application-dev.yml                 # Development
    ├── application-test.yml                # Testing
    └── application-prod.yml                # Production
```