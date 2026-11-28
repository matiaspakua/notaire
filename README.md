# 📜 Notaire — Modernization of a notary management system

<div align="center">

[![Java](https://img.shields.io/badge/Java-26-%23ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/26/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-%236DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-%23000000?logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19-%2361DAFB?logo=react&logoColor=white)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%234169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-%232496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-%233178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4-%2306B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)

[![CI - Build & Test](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/ci.yml)
[![CD - Docker](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/cd.yml)
[![PR Validation](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/pr-validation.yml)
[![Frontend CI](https://github.com/matiaspakua/notaire/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/frontend-ci.yml)
[![Playwright E2E](https://github.com/matiaspakua/notaire/actions/workflows/playwright-e2e.yml/badge.svg)](https://github.com/matiaspakua/notaire/actions/workflows/playwright-e2e.yml)
[![Trivy](https://img.shields.io/badge/Security-Trivy-%231904DA?logo=trivy&logoColor=white)](https://trivy.dev/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Migration of a Java 1.6 / Swing monolith to a modern architecture:
REST API (Spring Boot 4) + web frontend (Next.js 16), with CI/CD and
production-grade observability.

</div>

## Contents

- [Project vision](#-project-vision)
- [Architecture](#️-architecture)
- [Repository layout](#-repository-layout)
- [Technology stack](#-technology-stack)
- [Quick start](#-quick-start)
- [Testing and quality](#-testing-and-quality)
- [Observability and security](#-observability-and-security)
- [AI SDLC factory and RepoLens](#-ai-sdlc-factory-and-repolens)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Project vision

The original system was a **Java 1.6 Swing monolith**: business logic,
data access, and the graphical interface were tightly coupled, the MySQL 5
database had no version control, passwords used MD5, and there were no
containers and no automated tests.

**Notaire** is the result of moving it to:

- **Backend**: REST API with **Spring Boot 4.1.2** and **Java 26**.
- **Frontend**: **Next.js 16** + **React 19** + **TypeScript** + **Tailwind CSS 4**, with a custom design system inspired by Apple.
- **Active client**: the Next.js frontend only. The legacy Swing client was removed from the tree (#1046); do not recreate it.
- **Database**: **PostgreSQL 16** with **Flyway** as the only source of truth for the schema.
- **Infrastructure**: multi-stage Docker Compose, 19 GitHub Actions workflows, Prometheus/Grafana/Loki observability.

## 🏗️ Architecture

```mermaid
flowchart TD
    classDef client fill:#e1f5fe,stroke:#0288d1,color:#01579b
    classDef backend fill:#fff3e0,stroke:#f57c00,color:#e65100
    classDef data fill:#e8f5e9,stroke:#388e3c,color:#1b5e20
    classDef infra fill:#f3e5f5,stroke:#7b1fa2,color:#4a148c

    BROWSER["🌐 Browser"]:::client --> NEXT["Next.js 16 (React 19)"]:::client
    NEXT -->|"/api/v1/* proxy"| API["Spring Boot 4 REST API"]:::backend
    API --> SVC["Services"]:::backend --> REPO["Spring Data JPA"]:::backend --> PG[("PostgreSQL 16")]:::data
    API -.-> OBS["Prometheus / Loki / Grafana"]:::infra
    DOCKER["Docker Compose"]:::infra --> API
    DOCKER --> NEXT
    DOCKER --> PG
```

The full diagram (Building Block View, Runtime View, Deployment View) is in the
**[SAD (arc42)](docs/200-architecture/201-SAD/sad.md)**.

**Delivery status (live sources, not archived dashboards):**
[Delivery Board](https://github.com/users/matiaspakua/projects/1) ·
[`docs/github/README.md`](docs/github/README.md) ·
[phase plan](docs/300-development/DEVELOPMENT-PLAN.md#5-architecture-migration-roadmap).
Historical snapshots: [`docs/000-archive/github/`](docs/000-archive/github/).

## 📁 Repository layout

```text
notaire/
├── backend-api/          # Spring Boot 4 REST API (Java 26)
├── frontend/              # Next.js 16 web app
├── ops/system.yaml        # System descriptor published to infrastructure
├── docs/                  # Documentation (see docs/README.md)
│   ├── 100-business/      # Requirements, use cases, data model
│   ├── 200-architecture/  # SAD, ADRs, design, diagrams, security, deploy
│   ├── 300-development/   # Setup, standards, testing
│   └── 000-archive/       # Historical or superseded documentation
├── infra/                 # git submodule → matiaspakua/infrastructure (observability, deploy, k6)
├── framework/
│   └── ai-sdlc/           # git submodule → draclab/ai_sdlc @ aisdlc-v1.1.0
├── github-page/           # Informational/portfolio site (Next.js, deployed to GitHub Pages)
├── docs/openspec/               # SDLC specifications (schema notaire-sdlc)
├── docker-compose.yml      # Development stack (published ports, pgAdmin)
├── docker-compose.prod.yml # Production stack (#1044; only the reverse proxy on the host)
├── CONSTITUTION.md         # Highest authority for the development process
└── CLAUDE.md / AGENTS.md   # Guide for AI agents
```

## 🧰 Technology stack

| Layer | Technologies |
|:-----|:------------|
| **Backend** | Java 26, Spring Boot 4.1.2 (Web, Data JPA, Security, Actuator), SpringDoc OpenAPI |
| **Persistence** | PostgreSQL 16, Flyway, HikariCP |
| **PDF reports** | Apache PDFBox (generated in-process, no templates) |
| **Frontend** | Next.js 16, React 19, TypeScript 5.7, Tailwind CSS 4, TanStack Query, shadcn/ui, Zustand |
| **Testing** | JUnit 5 + Mockito + Testcontainers (backend), Vitest + Playwright (frontend), Bruno (API) |
| **Quality** | JaCoCo, Checkstyle, SpotBugs, Trivy |
| **Observability** | Micrometer, Prometheus, Loki + Promtail, Grafana |
| **CI/CD** | GitHub Actions (19 workflows), multi-stage Docker |

Full detail and the reason for each choice: [ADRs](docs/200-architecture/202-ADR/).

## 🚀 Quick start

### Prerequisites

```bash
# Backend
Java 26 / Maven 3.9+ / Docker + Compose

# Frontend (local development without Docker)
Node.js 22+ / npm 10+
```

### With Docker (recommended)

```bash
git clone --recurse-submodules https://github.com/matiaspakua/notaire.git
cd notaire
# If you already cloned without submodules:
#   git submodule update --init --recursive
cp .env.example .env
bash workspace/stack/start.sh          # starts PostgreSQL + backend
bash scripts/health.sh         # checks that everything is up
```

### In development (without Docker for the app/frontend)

```bash
cd backend-api && mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd frontend && npm run dev
# PostgreSQL 16 must be running on :5432
```

### Available services

| Service | URL |
|:---------|:----|
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |
| Frontend | http://localhost:3000 |
| pgAdmin | http://localhost:5050 |

Full stack (Prometheus, Grafana, SonarQube, Homer): `bash workspace/stack/start-all.sh` — see [`infra/README.md`](infra/README.md).

## 🧪 Testing and quality

```bash
mvn test -pl backend-api                 # backend unit + integration
cd testing/e2e && npm test              # Playwright E2E
bash workspace/sdlc/preflight.sh                # the pull-request gate: unit-level suites this branch touches
bash workspace/sdlc/preflight.sh --all          # every suite, server-backed included: the release gate
```

The pre-push hook runs the pull-request gate and never repeats a commit that already passed (#2413, #2493).
GitHub does not re-run the suites on a pull request; `--all` before a release and the tag CI run the rest.

Coverage floors (raise-only): backend JaCoCo **85% lines / 70% branches**, frontend Vitest
**87% statements / 79% branches / 85% functions / 88% lines**. The Owner's target is more than 90%
across the pyramid (#2046) — see [`code-quality.md`](.claude/rules/code-quality.md).

The full testing strategy (unit, integration, API, E2E, CU↔API matrix):
[`docs/300-development/303-testing/`](docs/300-development/303-testing/).
Local ↔ CI mapping of each gate: [`docs/300-development/CI-PREFLIGHT.md`](docs/300-development/CI-PREFLIGHT.md).

## 🔍 Observability and security

- **Observability**: metrics (Prometheus), structured JSON logs (Loki),
  dashboards (Grafana) — see [`docs/200-architecture/207-monitoring/`](docs/200-architecture/207-monitoring/).
- **Security**: BCrypt, Spring Security, CORS, input validation, Trivy
  scanning — see [`docs/200-architecture/206-security/`](docs/200-architecture/206-security/)
  (HTTPS/TLS runbook, installation isolation checklist, security tools).
- **DevSecOps / deployment**: [`docs/200-architecture/208-devsecops/`](docs/200-architecture/208-devsecops/) and [`docs/200-architecture/209-deployment/`](docs/200-architecture/209-deployment/).

## 🏭 AI SDLC factory and RepoLens

Notaire is built by a governed AI SDLC factory. Every change goes through an Issue, an OpenSpec
plan, failing tests first, gates and evidence:

- **Kit** — git submodule [`framework/ai-sdlc/`](framework/ai-sdlc/README.md) →
  [`draclab/ai_sdlc`](https://github.com/draclab/ai_sdlc) pinned to tag `aisdlc-v1.1.0`
  (adopter profile stays in [`.aisdlc/aisdlc.toml`](.aisdlc/aisdlc.toml)).
- **Infrastructure** — git submodule [`infra/`](infra/README.md) →
  [`matiaspakua/infrastructure`](https://github.com/matiaspakua/infrastructure); Notaire publishes
  [`ops/system.yaml`](ops/system.yaml) and runtime metrics/logs/traces
  ([system contract](https://github.com/matiaspakua/infrastructure/blob/main/docs/SYSTEM-CONTRACT.md)).
- **AI SDLC Factory** — [`framework/ai-sdlc/factory-dashboard/`](framework/ai-sdlc/factory-dashboard/README.md):
  the factory drawn as a production line. Each station shows its gates and cost, plus what the
  run's steps received and produced.
- **RepoLens** — git submodule [`framework/repolens/`](framework/repolens/) →
  [`draclab/repolens`](https://github.com/draclab/repolens) (MIT): the code knowledge graph agents
  use to find code; measured in
  [`bench/results/`](framework/repolens/bench/results/factory-learning-log.md).
- **Results** — [factory results of 2026-11-23](framework/ai-sdlc/docs/retrospectives/2026-11-23-factory-results.md),
  and the [AI SDLC case study on GitHub Pages](https://matiaspakua.github.io/notaire/ai-sdlc/).

## 📚 Documentation

All documentation lives under [`docs/`](docs/README.md), organized in three
areas (business, architecture, development) plus a historical archive:

| Area | Location | Contents |
|:-----|:----------|:----------|
| Business | `docs/100-business/` | 121 requirements (RF/RNF), 87 use cases, data model |
| Architecture | `docs/200-architecture/` | SAD (arc42), 20 ADRs, API/frontend design, diagrams, security, monitoring, deploy |
| Development | `docs/300-development/` | Setup, standards, testing, [phase roadmap](docs/300-development/DEVELOPMENT-PLAN.md) |
| Delivery | `docs/github/` | [Delivery Board, milestones, labels](docs/github/README.md) |
| Archive | `docs/000-archive/` | Historical or superseded documentation (including old MIGRATION-* notes) |

Recommended entry point: [`docs/README.md`](docs/README.md).

## 🤝 Contributing

Every change follows the mandatory process defined in
**[`CONSTITUTION.md`](CONSTITUTION.md)** and
**[`.claude/rules/ai-agent-workflow.md`](.claude/rules/ai-agent-workflow.md)**:
Issue + Use Case (Caso de Uso) → branch (`<type>/<issue>_<description>`) → TDD → tests →
commit (Conventional Commits, `Closes #<issue>`) → `workspace/sdlc/preflight.sh` → PR.

- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/)
- **Branches**: `<type>/<issue-number>_<description>`
- **Java**: Google Style, Checkstyle, SpotBugs
- **TypeScript**: strict mode
- **PRs**: reviewed (human + AI code review)

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE).

---

<div align="center">

**Developed by [Matías Miguez](https://github.com/matiaspakua)**

</div>
