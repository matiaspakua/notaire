# Technology stack and architecture assessment

> **Issue:** #1976 · **Assessed:** 2026-11-01 against `main` · **Rendered:** GitHub Pages
> [`/docs/stack/`](https://matiaspakua.github.io/notaire/docs/stack/)

This page lists every technology Notaire uses, the job it does, and where the
architecture stands. **Versions are not repeated here.** They live in
[`pom.xml`](../../pom.xml), [`backend-api/pom.xml`](../../backend-api/pom.xml),
[`frontend/package.json`](../../frontend/package.json) and the Compose files, and the
Pages site reads them from there at build time. The counts in the assessment are a
snapshot taken on the date above. The Pages site recomputes them on every deploy.

## Patterns

| Pattern | What it means in Notaire | Decision |
|---|---|---|
| Ports & Adapters (hexagonal) | Use cases depend on port interfaces. Web, persistence, PDF and other adapters implement them. ArchUnit enforces the rules R1–R7 in the build. | [ADR-030](202-ADR/ADR-030-hexagonal-architecture-full-adoption.md) |
| Database as code | Flyway migrations are the only source of the schema. Hibernate never changes it in production. | [ADR-007](202-ADR/ADR-007-database-schema-versioning-flyway.md) |
| Code-first API with contract checks | springdoc generates OpenAPI from the controllers, and the generated file is committed. CI checks that it is current, runs oasdiff for breaking changes, regenerates the frontend types and checks that every endpoint is reachable. | [ADR-020](202-ADR/ADR-020-openapi-exposure-policy.md), [ADR-023](202-ADR/ADR-023-rest-resource-naming.md) |
| Architecture decision records | 31 ADRs record each decision and its status. | [`202-ADR/`](202-ADR/) |
| Spec-driven, gated SDLC | Every change goes through an OpenSpec proposal, design, spec and tasks, then Gates 1–5. People and AI agents follow the same rules. | [`CONSTITUTION.md`](../../CONSTITUTION.md) |

## Backend

| Technology | Role |
|---|---|
| Java | Language. The release is pinned in the root `pom.xml`. |
| Spring Boot | Starters for web, validation, data JPA, security, actuator, Flyway, AspectJ and OpenTelemetry. |
| Spring Security + jjwt | Stateless JWT sessions (HS256), bcrypt passwords, and revoked-token and login-attempt tables (V43, V48). |
| Spring Data JPA / Hibernate | Persistence behind the out-ports. `ddl-auto` defaults to `none`. |
| springdoc-openapi | Generates the OpenAPI document and serves Swagger UI under the exposure policy of ADR-020. |
| Apache PDFBox | PDF reports (`adapter/out/pdf`). |
| Lombok, Jackson, logstash-logback-encoder | Less boilerplate, JSON, structured logs. |
| Spotless, Checkstyle, SpotBugs, JaCoCo | Formatting, static analysis and coverage in the Maven build. |

## Frontend

| Technology | Role |
|---|---|
| Next.js (App Router) + React + TypeScript | The office staff UI. |
| Tailwind CSS + Radix UI + lucide | Styling, accessible primitives, icons. |
| TanStack Query | Server state. |
| Zustand | Auth session store (`src/store/auth-store.ts`). |
| next-intl | Spanish and English messages (`frontend/messages/`). |
| React Flow (xyflow) + dagre | Workflow editor and progress graphs. |
| openapi-typescript | Generates `src/types/api.generated.ts` from the committed contract. |
| OpenTelemetry web SDK | Browser fetch traces to the collector. |
| ESLint import rules | Keep the frontend hexagon closed: no `fetch` in UI code, no React in `core/`. |

## Data

| Technology | Role |
|---|---|
| PostgreSQL | The only production database (image pinned in `docker-compose.yml`). |
| Flyway | Versioned SQL migrations `V{n}__*.sql`. `R{n}__*.sql` files are hand-applied rollbacks that Flyway skips. |
| H2 | Only used to export the OpenAPI document in CI. |
| Testcontainers PostgreSQL | Integration tests on a real database, migrations included. |

## Testing

| Layer | Tool | Where |
|---|---|---|
| Architecture | ArchUnit | `backend-api/src/test/.../architecture/` |
| Unit and integration | JUnit 5, Spring Boot Test, Testcontainers | `backend-api/src/test` |
| Frontend unit | Vitest + Testing Library | `frontend/src/**/*.test.ts(x)` |
| API | Bruno (YAML OpenCollection), run by `npx @usebruno/cli` in the E2E workflow | `backend-api/api-test` |
| End to end | Playwright against the full stack | `testing/e2e` |
| Performance | k6 | `performance-test.yml` |
| Security | CodeQL, OWASP ZAP baseline, Dependabot | `.github/` |

## Observability and infrastructure

| Technology | Role |
|---|---|
| Docker Compose | The app stack (postgres, backend, frontend, pgAdmin) and the observability stack. |
| OpenTelemetry Collector + Tempo | Traces from the backend and the browser. |
| Micrometer + Prometheus + Alertmanager | Metrics and alerts. Exporters: postgres, blackbox, node, cAdvisor. |
| Loki + Promtail | Logs. |
| Grafana | Dashboards. |
| SonarQube | Local quality dashboard. |
| GitHub Actions | 19 workflows: CI, CD, Pages, OpenAPI contract, E2E, DAST, CodeQL, performance, database V&V, SDLC process and others. |
| release-please | Product versions and the changelog. |

## AI tooling

Claude Code (`.claude/` rules, skills and agents), Cursor (`.cursor/`), GitHub Copilot
(`copilot-setup-steps.yml`) and OpenCode (`opencode.yml`) all work through the same
OpenSpec changes and gates as a human contributor. See [`AGENTS.md`](../../AGENTS.md).

## Assessment (snapshot 2026-11-01)

**Strengths**

- **The hexagon is enforced, not just drawn.** ArchUnit runs rules R1–R7 with an
  empty violation baseline
  (`backend-api/src/test/resources/architecture/hexagon-baseline.txt`). The backend
  has 86 in-ports, 58 out-ports and 78 use-case services. The frontend mirrors this:
  33 core contexts and 34 HTTP adapters behind ESLint rules.
- **The schema is fully code-owned.** 53 migrations, `validate-on-migrate`,
  `clean-disabled`, `out-of-order=false`, and an entity-to-schema drift test
  (`FlywaySchemaValidationIntegrationTest`).
- **The API contract is a gate, not documentation.** 147 paths and 231 operations.
  Export drift, oasdiff breaking changes, generated types and UI reachability are
  each checked in CI.
- **Tests span every layer.** 352 backend test classes, 197 frontend test files,
  102 Playwright specs and 383 Bruno requests. The Bruno suite found 12 backend
  defects, listed in `api-test/COVERAGE.md`.

**Gaps**

- **The migration to the hexagon is not finished.** 9 contexts have a `domain/`
  package against 22 use-case contexts, and the legacy `jpa`, `repository`,
  `business` and `dto` packages still hold 176 files (#576).
- **Some CI jobs start the backend with `ddl-auto=update`.** This can hide a missing
  migration in those runs. Filed as a separate issue.
- **Backups are not automated yet.** `backup-restore-smoke.yml` skips itself until
  #256 lands.
- **Some text has gone stale.** Parts of `db/migration/README.md`, the
  database-migrations rule and the committed `github-page/public/metrics.json` still
  show older figures. Filed as separate issues.

## Related

- [SAD](201-SAD/sad.md) · [ADR index](202-ADR/README.md)
- [Frontend OpenAPI types](203-design/FRONTEND-OPENAPI-TYPES.md)
- [API test coverage](../../backend-api/api-test/COVERAGE.md)
