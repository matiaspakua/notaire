# Development setup

## Prerequisites

| Tool | Minimum version |
|------------|----------------|
| Java JDK | 26 |
| Maven | 3.9+ |
| Docker | 24+ |
| PostgreSQL client | 15+ |

The root `pom.xml` sets Java 26 and Spring Boot 4.1.1.

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/matiaspakua/notaire.git
cd notaire
```

### 2. Configure environment variables

Create a `.env` file at the repository root:

```bash
# Database
POSTGRES_DB=notaire
POSTGRES_USER=notaire
POSTGRES_PASSWORD=your_password

# API
API_PORT=8080
```

### 3. Start the environment

```bash
# Start Docker with PostgreSQL
bash workspace/stack/start.sh

# Follow logs
bash workspace/stack/logs.sh

# Stop
bash workspace/stack/stop.sh
```

`workspace/stack/start.sh` uses `docker compose` when that command exists, and `docker-compose` otherwise.

### 4. Build the project

```bash
mvn clean install
```

### 5. Start the frontend (Next.js)

The active web client lives in `frontend/`. The Swing desktop modules
(`frontend-swing` / `deprecated-frontend-swing`) were **removed** (#1046); do not
look for them on disk. Requires the backend at `http://localhost:8080`.

```bash
cd frontend
cp .env.local.example .env.local   # adjust API URL if needed
npm install
npm run dev                         # http://localhost:3000
```

See [`frontend/README.md`](../../../frontend/README.md) for the full stack, scripts,
and use-case module coverage.

## Module structure

```bash
notaire/
├── backend-api/                # REST API (Spring Boot) — Maven reactor module
│   ├── src/main/java/
│   └── pom.xml
├── frontend/                    # Web client (Next.js) — active development
└── pom.xml                      # Parent POM (backend-api only)
```

Swing Robot E2E under `testing/e2e-swing/` is hard-deprecated (#811); active UI
E2E is Playwright (`cd testing/e2e && npm test`).

## Development commands

### Build

```bash
# Build everything
mvn clean install

# Build one module and the modules it depends on
mvn clean install -pl backend-api -am

# Skip tests
mvn clean install -DskipTests
```

### Tests

```bash
# Run every test
mvn test

# Tests for one module
mvn test -pl backend-api

# One test class
mvn test -Dtest=BudgetEntityTest

# Coverage report
mvn jacoco:report
```

### Run

```bash
# Backend
cd backend-api && mvn spring-boot:run

# API docs
# http://localhost:8080/swagger-ui.html
```

## Package structure

### Backend API

```text
com.licensis.notaire/
├── adapter/in/web/      # REST controllers (PersonController, BudgetController, ...)
├── application/         # Use cases
├── audit/
├── business/
├── config/              # Bean configuration
├── domain/
├── dto/                 # DtoPerson and the other API DTOs
├── exception/
├── jpa/                 # Legacy controllers
├── observability/
├── repository/
├── security/            # JWT and security filters
└── service/
```

DTOs such as `DtoPerson` live in `backend-api` (`com.licensis.notaire.dto`). The
`notaire-shared` module was retired (ADR-025). External services use the REST API
(OpenAPI). See [DTO-MAPPING-GUIDE.md](../302-code-standards/DTO-MAPPING-GUIDE.md).

## Database

### Schema

Flyway owns the schema (migrations in `backend-api/src/main/resources/db/migration/`).
Applied files V1–V44 stay immutable. V45 renames the remaining Spanish identifiers.
The historical `init-db/` scripts are archived at `docs/000-archive/init-db/`.

### Indexes

Confirm the indexes you need exist, including:

- `people.id`
- `deeds.id`
- `budgets.id`
- Foreign keys that queries use often

## Troubleshooting

### Port already in use

```bash
# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Docker does not start

```bash
docker ps
docker compose logs
docker compose down -v
docker compose up -d
```

Use `docker-compose` in those commands when the machine does not have `docker compose`.

### Maven build fails

```bash
# Clear the local cache
mvn dependency:purge-local-repository
mvn clean install -U
```

## Repo hygiene (ignore rules & manuals)

- Do **not** commit `.serena/` (local AI tooling). It is gitignored (#1050).
- Needed text assets such as `testing/e2e-swing/requirements.txt` remain
  trackable for ignore-rule hygiene (#1050) even though the Swing Robot suite is
  retired (#811); there is no global `*.txt` ban (use `*.local.txt` for scratch notes).
- The historical user-manual **PDF** is a GitHub Release asset (`docs-manuals`),
  not an ordinary git blob. Fetch with `bash docs/tools/fetch-user-manual.sh`
  (see [ADR-022](../../200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md)
  and [`docs/100-business/105-manuals/`](../../100-business/105-manuals/)).
