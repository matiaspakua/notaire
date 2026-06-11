# Frequently Asked Questions (FAQ)

## General Questions

### Q: What is Notaire?
**A**: Notaire is a modern software system for notarial offices in Latin America. It manages legal procedures (trámites), budgets (presupuestos), documentation, users, and provides auditing and reporting capabilities.

### Q: Where should I start if I'm new?
**A**: Start with [GETTING-STARTED.md](GETTING-STARTED.md) — a 5-minute intro. Then check [NAVIGATION.md](NAVIGATION.md) to find documentation for your specific role.

### Q: Where's the documentation for old `docs/business/` directory?
**A**: That directory has been consolidated into `docs/01-business/`. All references have been updated. Use the newer structure — it's better organized.

### Q: I found duplicate documentation. Which should I use?
**A**: Report it as a bug! The system should have one source of truth per topic. Use [NAVIGATION.md](NAVIGATION.md) to find the canonical location.

---

## Architecture Questions

### Q: Why Spring Boot and not Node.js?
**A**: See [ADR-001](02-architecture/01-adr/ADR-001-java-and-spring-boot-for-backend.md) — enterprise stability, maturity, and ecosystem support.

### Q: Why Next.js for the frontend?
**A**: See [ADR-005](02-architecture/01-adr/ADR-005-modern-frontend-migration.md) — modernization from legacy Swing GUI. Next.js offers performance, TypeScript safety, and rapid development.

### Q: Why PostgreSQL?
**A**: See [ADR-010](02-architecture/01-adr/) (if available) or [02-architecture/03-design/DATABASE-DESIGN.md](02-architecture/03-design/DATABASE-DESIGN.md) — reliability, JSON support, and scaling.

### Q: What happened to the Swing GUI?
**A**: It's been superseded by the Next.js frontend. See [ADR-005](02-architecture/01-adr/ADR-005-modern-frontend-migration.md) for the migration rationale. Legacy documentation is in [archive/e2e-swing/](archive/e2e-swing/).

### Q: How do I understand the system architecture?
**A**: Read [02-architecture/02-overview/sad.md](02-architecture/02-overview/sad.md) (Software Architecture Document) — complete overview in 15 minutes.

### Q: Where's the ER diagram?
**A**: [02-architecture/02-overview/ER-DIAGRAM.md](02-architecture/02-overview/ER-DIAGRAM.md)

### Q: What's the project structure?
**A**: [02-architecture/02-overview/PROJECT-STRUCTURE.md](02-architecture/02-overview/PROJECT-STRUCTURE.md)

---

## Development Questions

### Q: How do I set up my development environment?
**A**: Follow [03-development/01-setup/README.md](03-development/01-setup/README.md) — step-by-step guide for macOS, Linux, and Windows.

### Q: What are the coding standards?
**A**: [03-development/04-code-standards/README.md](03-development/04-code-standards/README.md) — Java, TypeScript, testing conventions.

### Q: How do I add a new feature?
**A**: Follow the workflow in [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md):
1. Create GitHub issue with Use Case reference
2. Create feature branch: `feat/<issue-number>_description`
3. Write tests first (TDD)
4. Implement
5. Run all tests (unit + integration + E2E)
6. Create PR
7. Merge

### Q: Where are the API docs?
**A**: [05-api/REST-API-REFERENCE.md](05-api/REST-API-REFERENCE.md) — all endpoints, request/response examples.

### Q: How do I write tests?
**A**: See [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md) for overall approach, then:
- **Unit tests**: [03-development/04-code-standards/TESTING.md](03-development/04-code-standards/TESTING.md)
- **E2E tests**: [03-development/03-testing/E2E-TEST-PLAN.md](03-development/03-testing/E2E-TEST-PLAN.md)

### Q: How do I write a Playwright test?
**A**: [03-development/03-testing/PLAYWRIGHT-QUICKSTART.md](03-development/03-testing/PLAYWRIGHT-QUICKSTART.md) — quick reference.

### Q: What's the test coverage requirement?
**A**: 80% is the target (enforced by JaCoCo). Check [03-development/03-testing/](03-development/03-testing/) for details.

### Q: How do I run tests?
**A**: See [03-development/01-setup/README.md](03-development/01-setup/README.md) — build commands section.

### Q: How do I handle errors?
**A**: [03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md](03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md) — patterns and anti-patterns.

### Q: How do database migrations work?
**A**: [03-development/04-code-standards/DATABASE-MIGRATIONS.md](03-development/04-code-standards/DATABASE-MIGRATIONS.md) — Flyway guide.

### Q: How do I document my code?
**A**: [03-development/04-code-standards/DOCUMENTATION.md](03-development/04-code-standards/DOCUMENTATION.md) — Javadoc, README, ADR guidelines.

---

## Testing Questions

### Q: What types of tests should I write?
**A**: See [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md) — test pyramid: unit → integration → E2E.

### Q: How do I write a unit test?
**A**: [03-development/04-code-standards/TESTING.md](03-development/04-code-standards/TESTING.md) — JUnit 5, Mockito examples.

### Q: How do I write an integration test?
**A**: [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md) — database, REST controller tests.

### Q: How do I write an E2E test?
**A**: [03-development/03-testing/E2E-TEST-PLAN.md](03-development/03-testing/E2E-TEST-PLAN.md) — Playwright for the frontend.

### Q: What's the E2E test coverage?
**A**: [03-development/03-testing/GAP-ANALYSIS.md](03-development/03-testing/GAP-ANALYSIS.md) — see what's covered and what's missing.

### Q: Are there E2E tests for Swing?
**A**: The Swing GUI is deprecated. E2E tests now use Playwright (Next.js). Legacy Swing testing docs are in [archive/e2e-swing/](archive/e2e-swing/).

---

## Operations Questions

### Q: How do I deploy to production?
**A**: [04-operations/README.md](04-operations/README.md) — deployment procedures and checklists.

### Q: How do I set up monitoring?
**A**: [04-operations/01-monitoring/README.md](04-operations/01-monitoring/README.md) — Prometheus, Grafana, alerts.

### Q: How do I read logs?
**A**: [04-operations/03-logging/README.md](04-operations/03-logging/README.md) — Loki, structured logging, query examples.

### Q: What metrics are available?
**A**: [04-operations/01-monitoring/PROMETHEUS.md](04-operations/01-monitoring/PROMETHEUS.md) — Prometheus metrics documentation.

### Q: How do I set up alerts?
**A**: [04-operations/01-monitoring/ALERTS.md](04-operations/01-monitoring/ALERTS.md) — alert rules, escalation procedures.

### Q: How do I handle a security incident?
**A**: [04-operations/03-security/README.md](04-operations/03-security/README.md) — security policies and incident response.

### Q: What are the security requirements?
**A**: [04-operations/03-security/README.md](04-operations/03-security/README.md) and [04-operations/03-security/OWASP-TOP-10.md](04-operations/03-security/OWASP-TOP-10.md).

### Q: How do I scale the system?
**A**: [04-operations/02-scaling/](04-operations/02-scaling/) — Kubernetes, load balancing, database scaling.

### Q: What's the disaster recovery plan?
**A**: [04-operations/05-disaster-recovery/](04-operations/05-disaster-recovery/) — backup, recovery procedures, RTO/RPO targets.

---

## Business & Requirements Questions

### Q: What are the system requirements?
**A**: [01-business/01-requirements/](01-business/01-requirements/) — RF-01 to RF-95 (functional), RNF-01 to RNF-24 (non-functional).

### Q: Where are the use cases?
**A**: [01-business/02-use-cases/](01-business/02-use-cases/) — all 78 use cases (CU-01 to CU-78) documented.

### Q: What does use case X do?
**A**: Open [01-business/02-use-cases/CU-XX.md](01-business/02-use-cases/) and find the file. Each use case has scope, actors, activities, and acceptance criteria.

### Q: What's the data model?
**A**: [01-business/04-data-model/04_MD - Modelo de Datos/Diccionario de Datos.md](01-business/04-data-model/04_MD%20-%20Modelo%20de%20Datos/Diccionario%20de%20Datos.md) — entity definitions and relationships.

### Q: Where's the user manual?
**A**: [01-business/05-manuals/C_Manual de Usuario/Manual de Usuario Notaire.doc.md](01-business/05-manuals/) — for end users.

### Q: Where's the system manual?
**A**: [01-business/05-manuals/A_Manual de Sistema/](01-business/05-manuals/) — for system administrators.

### Q: What's the roadmap?
**A**: [06-learning/ROADMAP.md](06-learning/ROADMAP.md) — planned features and timeline.

---

## Design Questions

### Q: What's the design system?
**A**: [02-architecture/03-design/DESIGN-SYSTEM.md](02-architecture/03-design/DESIGN-SYSTEM.md) — colors, typography, components for the frontend.

### Q: What design tokens should I use?
**A**: [02-architecture/03-design/DESIGN-SYSTEM.md](02-architecture/03-design/DESIGN-SYSTEM.md) — reference all available tokens.

### Q: Why is the database designed this way?
**A**: [02-architecture/03-design/DATABASE-DESIGN.md](02-architecture/03-design/DATABASE-DESIGN.md) — schema rationale and normalization.

### Q: What's the API design philosophy?
**A**: [02-architecture/03-design/API-DESIGN.md](02-architecture/03-design/API-DESIGN.md) — REST patterns, naming conventions, error handling.

---

## Troubleshooting

### Q: The app won't start. What do I do?
**A**: 
1. Check Docker is running: `docker ps`
2. Check logs: `bash scripts/logs.sh`
3. Check the setup guide: [03-development/01-setup/README.md](03-development/01-setup/README.md)
4. Check database: `psql -h localhost -U notaire -d notaire`

### Q: Tests are failing. What do I do?
**A**:
1. Run tests individually: `mvn test -Dtest=MyTest`
2. Check for environmental issues (database running, ports available)
3. Review [03-development/03-testing/TEST_STRATEGY.md](03-development/03-testing/TEST_STRATEGY.md)
4. Check recent git changes that might have broken tests

### Q: I can't connect to the database.
**A**:
1. Is Docker running? `docker ps`
2. Is the postgres container up? `docker logs notaire-postgres`
3. Check credentials in `.env` file
4. Check [03-development/01-setup/README.md](03-development/01-setup/README.md) setup section

### Q: The API is returning 500 errors.
**A**:
1. Check backend logs: `bash scripts/logs.sh backend`
2. Check database: Is it accessible? Are migrations applied?
3. Check recent PRs that touched the endpoint
4. See [03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md](03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md)

### Q: Performance is slow. What do I check?
**A**:
1. Check Grafana dashboards: [04-operations/01-monitoring/PROMETHEUS.md](04-operations/01-monitoring/PROMETHEUS.md)
2. Check database queries: [04-operations/03-logging/LOG-QUERIES.md](04-operations/03-logging/LOG-QUERIES.md)
3. Check for N+1 query problems: [03-development/04-code-standards/](03-development/04-code-standards/)
4. Check caching strategy: [02-architecture/03-design/](02-architecture/03-design/)

### Q: E2E tests are flaky. What do I do?
**A**:
1. Check [03-development/03-testing/E2E-TEST-PLAN.md](03-development/03-testing/E2E-TEST-PLAN.md) for best practices
2. Check test isolation: are tests leaving data behind?
3. Check timing: are waits sufficient?
4. Check [03-development/03-testing/PLAYWRIGHT-QUICKSTART.md](03-development/03-testing/PLAYWRIGHT-QUICKSTART.md) for debugging tips

---

## Documentation Questions

### Q: Where should I document something?
**A**: See [NAVIGATION.md](NAVIGATION.md) — find the canonical location for your topic type.

### Q: How do I document a decision?
**A**: Create an ADR (Architecture Decision Record). See [02-architecture/01-adr/](02-architecture/01-adr/) for examples and template.

### Q: How do I update documentation?
**A**: 
1. Find the canonical source (use [NAVIGATION.md](NAVIGATION.md))
2. Edit that ONE file
3. Verify links from other docs still work
4. Update related documentation if needed
5. Commit with message: `docs: update <topic>`

### Q: I found outdated documentation. What do I do?
**A**:
1. If it's related to Swing GUI: archive it in [archive/](archive/)
2. If it's deprecated: update it with "DEPRECATED" notice and link to current version
3. If it's wrong: fix it immediately
4. Open a PR with explanation of changes

---

## Getting More Help

- **Can't find something?** Check [NAVIGATION.md](NAVIGATION.md) — complete map of all docs
- **Need to propose a change?** See [.claude/rules/ai-agent-workflow.md](../.claude/rules/ai-agent-workflow.md) — create an issue with Use Case reference
- **Found a bug in code?** Create a GitHub issue with reproduction steps
- **Documentation is unclear?** Open an issue with "docs:" label

---

**Can't find your question here?** Open a GitHub issue describing what you need, and we'll add it to this FAQ.

**Last updated**: June 11, 2026
