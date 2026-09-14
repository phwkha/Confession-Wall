# AGENTS.md — Confession Wall Guidelines

> Guide and operational constraints for AI agents working in this repository.

---

## 1. Project Overview

**Confession Wall** is a fullstack application allowing users to share anonymous confessions, view a real-time feed, and interact via heart likes.
- **Repository Type:** Multi-module / Monorepo (Frontend + Backend + Infrastructure)
- **Primary Languages:** Java 17, JavaScript (ES Modules, JSX)
- **Deployment:** Docker Multi-stage Builds, Docker Compose, Jenkins Pipeline

---

## 2. Architecture & Tech Stack

| Component | Technology | Details |
| :--- | :--- | :--- |
| **Backend** | Java 17, Spring Boot 3.2.4 | Spring Data JPA, Hibernate, Bean Validation (`jakarta.validation`), H2 (Test), PostgreSQL Driver |
| **Frontend** | React 18, Vite 5, Tailwind CSS 3.4 | Axios (`src/services/api.js`), Lucide React icons |
| **Database** | PostgreSQL 15 (Alpine) | Docker named volume `postgres_data`, database `confession_db` |
| **Reverse Proxy** | Nginx Alpine | Serves React SPA & proxies `/api/` to `backend:8080` |
| **CI/CD** | Jenkins (`Jenkinsfile`), Docker Hub | Automated Maven unit testing & container build pipeline |

---

## 3. Directory Layout

```text
.
├── AGENTS.md                  # Instructions for AI agents (this file)
├── README.md                  # Project documentation for developers
├── docker-compose.yml         # Container orchestration (postgres, backend, frontend)
├── Jenkinsfile                # CI/CD pipeline definition
├── .env.example               # Template for environment variables
├── backend/                   # Spring Boot 3 REST API
│   ├── Dockerfile
│   ├── mvnw / mvnw.cmd        # Maven wrapper
│   ├── pom.xml                # Backend dependencies & build config
│   └── src/
│       ├── main/java/com/example/confessionwall/
│       │   ├── controller/    # REST Endpoints (ConfessionController)
│       │   ├── dto/           # Request/Response DTOs & Validation
│       │   ├── exception/     # Global exception handlers
│       │   ├── model/         # JPA Entities (Confession)
│       │   ├── repository/    # Spring Data JPA Repositories
│       │   └── service/       # Business logic interfaces & implementations
│       └── test/              # JUnit 5, Mockito & MockMvc tests
└── frontend/                  # React 18 + Vite SPA
    ├── Dockerfile
    ├── nginx.conf             # Nginx reverse proxy configuration
    ├── package.json           # Frontend dependencies & scripts
    ├── tailwind.config.js     # Tailwind CSS theme setup
    ├── vite.config.js         # Vite bundler configuration
    └── src/
        ├── components/        # React UI components (Card, Form, List, Header)
        ├── services/          # API client (Axios instance & endpoint methods)
        └── App.jsx            # Main application layout
```

---

## 4. Key Commands

### Docker Compose (Full Stack)
```bash
# Start all services in background with fresh build
docker compose up -d --build

# View container logs
docker compose logs -f [backend|frontend|postgres]

# Check service status & health
docker compose ps

# Stop containers and keep volumes
docker compose down
```

### Backend (Local Development & Testing)
```bash
cd backend

# Run unit and integration tests (uses in-memory H2)
./mvnw clean test

# Run application locally (requires active PostgreSQL or H2 config)
./mvnw spring-boot:run

# Package executable JAR
./mvnw clean package -DskipTests
```

### Frontend (Local Development)
```bash
cd frontend

# Install dependencies
npm install

# Start Vite dev server (defaults to http://localhost:5173)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

---

## 5. Coding Conventions & Standards

### Backend (Spring Boot 3)
- **Layered Architecture:** Strictly maintain separation of concerns:
  `Controller -> Service -> Repository -> Model`.
- **Validation:** Always validate incoming payloads using Jakarta Bean Validation (`@NotBlank`, `@Size`, etc.) inside DTO classes under `com.example.confessionwall.dto`.
- **Error Handling:** Use `GlobalExceptionHandler` with `@RestControllerAdvice`. Return uniform error structures via `ErrorResponse`.
- **No Direct Entity Exposure:** Avoid returning or receiving raw database entities in endpoints where DTOs provide better encapsulation.
- **Database Migrations / DDL:** Hibernate `ddl-auto` is configured in `application.yml`. Never modify table definitions in production environments without considering existing data integrity.

### Frontend (React & Tailwind)
- **Component Design:** Keep components modular and single-responsibility under `src/components/`.
- **Styling:** Use standard Tailwind CSS utility classes. Avoid inline styles or custom ad-hoc CSS unless strictly necessary.
- **API Calls:** Centralize all backend API requests in `src/services/api.js`. Never invoke raw `fetch` or direct `axios` instances inside individual UI components.
- **Icons:** Use `lucide-react` for UI icons.

---

## 6. Constraints & Safety Rules for Agents

1. **Security & Secrets:**
   - NEVER commit `.env` or plain-text passwords/credentials to version control.
   - Always update `.env.example` when introducing new environment variables.
2. **Network & Ports:**
   - In production / Docker Compose, `postgres` (5432) and `backend` (8080) ports MUST NOT be exposed directly to the public host. Communication occurs strictly within `confession-network`.
   - Only the frontend Nginx reverse proxy port (`${FRONTEND_PORT:-80}`) should be bound to the host.
3. **Testing Integrity:**
   - Run `./mvnw clean test` whenever backend logic or API models are modified to ensure zero regressions.
   - Do not disable, skip, or comment out existing unit tests without explicit user instruction.
4. **Documentation:**
   - Maintain existing docstrings, README references, and comments when modifying functionality.
