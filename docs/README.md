# Notaire System Documentation

Welcome to the Notaire notarial management system documentation. This guide covers system design, architecture, implementation, testing, operations, and API specifications.

## Quick Navigation

**New to the system?** Start here:
- [Getting Started Guide](06-learning/GETTING-STARTED.md) — Setup and first steps (5 min)
- [System Overview](02-architecture/02-overview/README.md) — High-level architecture (15 min)
- [FAQ & Troubleshooting](FAQ.md) — Common questions answered

**Looking for specific information?** See [NAVIGATION.md](NAVIGATION.md) — complete map of all documentation by use case.

---

## Documentation Structure

| Directory | Contents | Audience |
|-----------|----------|----------|
| **01-business/** | Business requirements, use cases, data model, user manuals | Product managers, business analysts, end users |
| **02-architecture/** | System design, architecture decisions, design patterns | Architects, senior engineers, tech leads |
| **03-development/** | Setup guides, coding standards, testing strategies, CI/CD | Developers, QA engineers, DevOps engineers |
| **04-operations/** | Monitoring, logging, security, deployment runbooks | Operations, SRE, security engineers |
| **05-api/** | REST API specifications, endpoint documentation, examples | Backend developers, API consumers |
| **06-learning/** | Quick references, roadmaps, onboarding materials | All team members, new contributors |
| **archive/** | Deprecated/historical documentation (Swing GUI, old plans) | Historical reference |

---

## Key Features Documented

### Business Processes
- **Use Cases (CU-01 to CU-78)** — Notarial office workflows from budget preparation to archive
- **Requirements (RF-01 to RF-95, RNF-01 to RNF-24)** — Functional and non-functional requirements
- **Data Model** — Entity definitions, relationships, schemas

### Technical Stack
- **Backend**: Spring Boot 3.x, Java 21, PostgreSQL 16
- **Frontend**: Next.js 15, React 19, TypeScript
- **Infrastructure**: Docker, Kubernetes, Prometheus/Grafana, Loki
- **Testing**: JUnit 5, Mockito, Playwright E2E
- **CI/CD**: GitHub Actions, SonarQube, Trivy

### Architecture Highlights
- **Three-tier architecture**: PostgreSQL → Spring Boot REST API → Next.js Frontend
- **Microservices-ready**: Stateless backend, horizontal scaling
- **Observability**: Prometheus metrics, Grafana dashboards, Loki logs, OpenTelemetry tracing
- **Security**: HTTPS/TLS, authentication, audit logging (AUDITORIA), secrets management

---

## Getting Started

### For Developers
1. Clone the repository
2. Follow [03-development/01-setup/README.md](03-development/01-setup/README.md)
3. Read [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md) for context
4. See [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md) for coding rules

### For Operators
1. Review [04-operations/README.md](04-operations/README.md)
2. Understand [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md)
3. Check [04-operations/01-monitoring/README.md](04-operations/01-monitoring/README.md)

### For Product Managers
1. Start with [01-business/README.md](01-business/README.md)
2. Review [Use Cases (CU-01 to CU-78)](01-business/02-use-cases/)
3. Check [Roadmap](06-learning/ROADMAP.md)

---

## Important Notes

### ⚠️ Documentation Organization
- **Old `docs/business/` directory has been consolidated** into `docs/01-business/`
- All references updated to use the new `01-business` path
- **Do not use the old `business/` directory** — it no longer exists

### 🛠️ Deprecated Components
- **Swing GUI Frontend**: Replaced with modern Next.js frontend
  - See [ADR-005](02-architecture/01-adr/ADR-005-modern-frontend-migration.md) for rationale
  - Legacy documentation archived in `docs/archive/`

### 📚 Recommended Reading Order
1. This README (you are here)
2. [System Overview](02-architecture/02-overview/README.md)
3. [Architecture Decision Records](02-architecture/01-adr/README.md) (pick relevant ones)
4. Your role-specific documentation (see table above)

---

## Documentation Standards

All documentation follows these principles:

- **Single Source of Truth**: One canonical location per topic (no duplicates)
- **Progressive Disclosure**: Basic concepts first, advanced details later
- **Audience-First**: Documentation organized by who needs it
- **Maintainability**: ADRs explain "why", not just "what"
- **Link Density**: Cross-references between related topics

---

## Contributing to Documentation

When updating documentation:
1. Update in one location only (the canonical source)
2. Update cross-references if you move files
3. Keep ADRs accurate (append new decisions, don't delete history)
4. Use consistent formatting and terminology
5. Link to related documentation and ADRs

See [NAVIGATION.md](NAVIGATION.md) for the complete documentation map.

---

**Last Updated**: June 11, 2026  
**Maintainer**: Development Team  
**Questions?** See [FAQ.md](FAQ.md) or open an issue.
