# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Case-insensitive username and JWT structure HTTP tests** (issues #692, #693): closed two
  gaps in the AUTH-001 HTTP/Bruno test coverage. `backend-api/api-test/usuarios/09-13-*.yml`
  verifies `POST /api/v1/usuarios/login` treats a username the same regardless of case
  (lowercase/uppercase/mixed), matching `UsuarioController`'s existing `equalsIgnoreCase`
  lookup. `14-16-*.yml` verifies the returned `token` is a well-formed
  `header.payload.signature` JWT whose decoded payload has `sub` (the username) and a future
  `exp` claim, matching `JwtTokenService.generateToken()`. No production code changed.

- **k6 load-test suite** (issue #594): no performance/load testing existed anywhere in the
  repository. Added `performance-test/k6/load-test.js`, covering the highest-traffic read
  endpoints (`gestiones`, `presupuestos`, `tramites`) with baseline thresholds (`p(95)<500ms`,
  error rate `<1%`), authenticating via the existing JWT login endpoint. Wired into a new
  scheduled (weekly, not per-PR) `.github/workflows/performance-test.yml` job so it doesn't
  gate every PR.

- **Security response headers on the Next.js frontend** (issue #562): `frontend/next.config.ts`
  had no `headers()` callback. Added `Content-Security-Policy`, `X-Frame-Options: DENY`,
  `X-Content-Type-Options: nosniff`, and `Strict-Transport-Security` to every route.

- **HistorialMapper unit tests** (issue #589): `service.mappers.HistorialMapper` had zero test
  coverage despite not being excluded from the JaCoCo gate. Added `HistorialMapperTest` covering
  the happy path and each nullable foreign key individually.

- **Login rate limiting / account lockout** (issue #560): `POST /api/v1/usuarios/login`
  now locks a username out for a configurable duration (`security.login.lockout-duration-ms`,
  default 15 minutes) after a configurable number of consecutive failed attempts
  (`security.login.max-attempts`, default 5), returning `429 Too Many Requests`. A
  successful login resets the counter. Implemented in the new
  `com.licensis.notaire.security.LoginAttemptService`.

- **Jakarta Bean Validation on request boundaries** (issue #561): `UsuarioController`'s
  `createUsuario`/`updateUsuario` now validate the request body (`@NotBlank nombre`, `tipo`),
  and `ReporteController`'s path/query parameters are validated (`@Positive` on ID-like
  parameters, `@NotBlank` on `nombreTipoTramite`, `@Min(1)/@Max(12)` on `mes`), returning a
  clean `400` instead of a generic `500` or silently accepting malformed input.
  `GlobalExceptionHandler` now handles `MethodArgumentNotValidException` (body validation) and
  `ConstraintViolationException` (`@RequestParam`/`@PathVariable` validation) consistently.
  Full rollout across the remaining controllers is tracked as a follow-up.

- **Mobile/tablet viewport coverage in Playwright E2E** (issue #610): `frontend/playwright.config.ts`
  previously only ran against `devices["Desktop Chrome"]`, with zero specs asserting layout at
  the 320px/768px/1024px breakpoints mandated by `.claude/rules/ui-ux-design.md`. Added a
  `mobile` project (`devices["iPhone SE"]`) and `tests/e2e/mobile-viewport.spec.ts`, which
  asserts no horizontal overflow on the login page at 320px and 768px and on the dashboard at
  320px after login.

### Fixed

- **Dashboard sidebar overflowed horizontally below 768px** (issue #699): `AppSidebar` rendered
  as a fixed 288px-wide `<aside>` with no responsive behavior, leaving no room for content at
  mobile widths (e.g. 320px). It now collapses to a hamburger-triggered off-canvas drawer below
  the `md` (768px) breakpoint, with a backdrop that dismisses it; desktop behavior (static,
  always visible) is unchanged. Un-skips the `test.fixme()` this issue tracked in
  `tests/e2e/mobile-viewport.spec.ts` and adds two more covering the drawer open/close cycle
  and the desktop no-hamburger case.

- **Dead `X-Notaire-User` header removed from the frontend** (issue #678): the backend's
  `AuditoriaAspect` has attributed audit records from the verified JWT identity
  (`SecurityContextHolder`), not from any client-supplied header, since #555 — but
  `frontend/src/lib/api-client.ts` kept sending `X-Notaire-User` on every request anyway, with
  no effect. Removed the dead header and its `actingUser()` helper, and corrected `CLAUDE.md`/
  `infra/README.md`, which still described audit attribution as header-based.

- **postgres-exporter reused the app's own admin DB credentials** (issue #675): `infra/docker-compose.yml`'s
  `postgres-exporter` service connected with `POSTGRES_USER`/`POSTGRES_PASSWORD` — the same
  credentials as the application itself — so a compromised metrics exporter had full read/write
  access to every application table. Added Flyway migration `V12` creating a dedicated
  `notaire_exporter` role granted only `pg_monitor` (PostgreSQL's built-in read-only statistics
  role), wired through `POSTGRES_EXPORTER_USER`/`POSTGRES_EXPORTER_PASSWORD` in both
  `docker-compose.yml` and `infra/docker-compose.yml`, and extended `ProductionCredentialsGuard`
  to reject a default exporter password in production. Grafana anonymous auth (also part of this
  finding) was already disabled by #672. `sslmode=disable` on the exporter connection is
  unchanged — `notary-postgres` has no TLS configured, so flipping it now would break the
  connection outright; tracked separately by #684.

- **Prometheus ran as root with the host's docker.sock mounted** (issue #674): `infra/docker-compose.yml`
  gave the `prometheus` container `user: root` plus a read-write bind mount of
  `/var/run/docker.sock`, even though `prometheus.yml` only ever scrapes static targets (no
  `docker_sd_configs`) — a compromised container had a direct path to full host compromise for
  no operational benefit. Removed both; the image's built-in non-root user is sufficient.

- **Wildcard `Authorization` header in CORS config allowed credential theft** (issue #673):
  `SecurityAndCorsConfig` hardcoded `.allowedHeaders("*")`, silently ignoring the existing (but
  unwired) `cors.allowed-headers` property, so any origin could read the `Authorization` header
  back via a CORS preflight response. Wired the property through with an explicit default
  (`Content-Type,Authorization`), and added a startup guard that refuses to boot
  in production if `cors.allowed-headers` or `cors.allowed-origins` still resolve to `*`.

- **Dead `X-Notaire-User` left in the `cors.allowed-headers` default** (issue #731): #673's
  default (above) originally included `X-Notaire-User`, which #678 had already removed from the
  frontend (and #555 from the backend's audit attribution) — an unused allow-listed header that
  no client actually sends. Dropped it from both `SecurityAndCorsConfig`'s `@Value` default and
  `application.properties`; no functional change.

- **Swagger/OpenAPI publicly accessible in production** (issue #671): `SecurityAndCorsConfig`
  now denies `/swagger-ui/**`, `/swagger-ui.html`, and `/v3/api-docs/**` when
  `app.environment=production` (the same signal `ProductionCredentialsGuard` already uses),
  while leaving them reachable in dev/test. Previously any anonymous visitor could enumerate
  the entire API surface and execute live requests via Swagger's "Try it out" in production.

- **Default credentials committed to version control** (issue #672): `infra/grafana/grafana.ini`
  no longer hardcodes `admin_user`/`admin_password` in plaintext — Grafana now gets its admin
  credentials exclusively from `GF_SECURITY_ADMIN_USER`/`GF_SECURITY_ADMIN_PASSWORD`
  (already wired to `GRAFANA_ADMIN_USER`/`GRAFANA_ADMIN_PASSWORD` in `.env`).
  `ProductionCredentialsGuard` now also rejects default pgAdmin and Grafana credentials in
  production, not just backend/DB/actuator/app-admin. `.env.example` defaults are explicitly
  marked as insecure placeholders that must change before a production run.

- **Order-dependent H2 integration test failures** (issue #661): `RepositoryIntegrationTest`
  (base for `GestionDeEscrituraRepositoryIntegrationTest`, `PagoRepositoryIntegrationTest`,
  `RegistroAuditoriaRepositoryIntegrationTest`) and `EstadoDeGestionReferentialIntegrityTest`
  were the only integration test bases without `@Transactional`, so their inserts committed
  permanently to the shared H2 instance instead of rolling back per test. Under
  `runOrder=alphabetical`, this broke `WorkflowTransitionIntegrationTest` and
  `WorkflowTraceApiH2IntegrationTest` when they ran later. Both classes now get
  `@Transactional`, matching every other integration test base in the suite.

- **Contradicting JaCoCo coverage-floor numbers** (issue #588): `CLAUDE.md`/`code-quality.md`
  said the enforced floor was 28%/14%, `pom.xml`'s own comment said ~78%/~62%, and the actually
  enforced `<minimum>` values were 70%/25% — three different numbers for the same gate. Also
  fixed a dead exclusion path referencing the nonexistent `com/licensis/notaire/servicios/*`
  package (real package is `service`), which meant `AdministradorJpa`/`AdministradorReportes`/
  `AdministradorSesion`/`AdministradorValidaciones`/`Conexion` were silently counted in the
  coverage gate instead of excluded as intended. Re-measured real coverage after the fix:
  ~84% line / ~74% branch. Added `JacocoCoverageConfigConsistencyTest` to guard against this
  drifting out of sync again.

- **Login attempt double-counting** (found while implementing #560): `UsuarioController.login()`
  fell through to its "usuario no encontrado" branch even when a username **was** matched but
  the password was wrong or the account was inactive, executing that branch's logic in addition
  to the matched-user branch. Fixed by returning immediately once a matching user has been
  handled.

- **E2E coverage reports were a fabricated static template** (issue #587): the
  `.github/workflows/playwright-e2e.yml` "Business Coverage Report" job wrote an identical
  hardcoded markdown block to `docs/wiki/cicd-reports/e2e-coverage-*.md` on every run,
  including a stale action item ("Fix backend 500 errors on testimonio endpoints") that had
  sat unchanged for 6+ weeks regardless of actual results. Replaced with
  `scripts/generate_e2e_coverage_report.py`, which parses the real Playwright JSON reporter
  output (`test-results/results.json`) and Bruno CLI JSON output (`bruno-results.json`) to
  report actual pass/fail/skip counts and the actual failing test titles, with no fabricated
  numbers when a results file is missing.

- **`generate_e2e_coverage_report.py` crashed on the real Bruno CLI output** (issue #658,
  found on #587's own first real CI run): `_bruno_section()` assumed `bruno-results.json` is a
  JSON object with a top-level `"summary"` key; the real `@usebruno/cli` output is a JSON array
  of per-iteration objects, each with its own `"summary"`. Verified the fix against the actual
  artifact from the failing job rather than guessing the schema a second time, and added a
  regression test using that real (trimmed) sample. Also found and fixed the reason Playwright
  results always showed "not found": `playwright-e2e.yml`'s "Run Playwright E2E tests" step
  passed `--reporter=html,json,junit` on the CLI, which fully overrides
  `playwright.config.ts`'s own `reporter` array — silently dropping the `outputFile` paths this
  script depends on, and the custom `tests/e2e/reporters/coverage-report.ts` business-coverage
  reporter entirely. Removed the CLI override so the config's reporters (which already have the
  correct paths) take effect. Bruno failures are now also surfaced in the report's Action Items
  section, not just Playwright ones — the discovery run had 137 failing Bruno tests silently
  reported as "no action items" before this fix.

- **Flyway single source of truth**: Removed dual schema source (init-db + Flyway)
  - Removed `init-db:/docker-entrypoint-initdb.d` volume mount from docker-compose.yml
  - PostgreSQL now starts empty; Flyway applies all V1→V11 migrations on startup
  - Created V11 migration to fix `conceptos.version` for "Documentacion" (id=3)
  - Rewrote `BaseIntegrationTest.java` to enable Flyway instead of copying init-db scripts
  - Renamed `InitDbSchemaValidationIntegrationTest` → `FlywaySchemaValidationIntegrationTest`
  - Archived `init-db/` directory to `docs/archive/init-db/`
  - Updated all documentation, agent configs, and `.claude/rules/database-migrations.md`
  - See `.claude/rules/database-migrations.md` for new migration workflow

### Added

- **ADR-007**: Database Schema Versioning with Flyway
  - Added architecture decision record for Flyway implementation
  - Documented migration strategy and best practices

- **SAR-007**: Flyway Implementation Solution Architecture Report
  - Detailed technical analysis and implementation plan
  - Testing strategy for database migrations
  - AI Agent Guidelines section

- **Flyway Skill for AI Agents**: `.claude/skills/flyway/SKILL.md`
  - Comprehensive guide for implementing Flyway migrations
  - Examples, best practices, and common patterns
  - Project-specific conventions and templates

- **Database Migrations Rules**: `.claude/rules/database-migrations.md`
  - Mandatory rules for all database changes
  - Anti-patterns to avoid
  - Rollback strategies and emergency procedures

- **Database Migrations README**: `backend-api/src/main/resources/db/migration/README.md`
  - Quick reference for developers
  - Common patterns and templates
  - Testing and validation commands

### Changed

- **Flyway Integration**: Migrated from init-db scripts to Flyway versioned migrations
  - Scripts moved to `backend-api/src/main/resources/db/migration/`
  - V1: Initial schema (24 tables)
  - V2: Initial reference data and admin user

### Deprecated

- `init-db/01-schema.sql` - Superseded by Flyway migration
- `init-db/02-data.sql` - Superseded by Flyway migration

### Fixed

- Updated Docker Compose to remove init-db volume mounts
- Configured Spring Boot to use Flyway with `spring.flyway.*` properties

## [1.0.0-SNAPSHOT] - 2026-04-14

### Added

- Initial release of Notaire microservices refactoring
- Spring Boot 4.0.4 backend API
- PostgreSQL 16 database
- REST API endpoints for all domain entities
- Swagger/OpenAPI documentation
- Unit tests with 80%+ coverage requirement
- Integration tests with Testcontainers
- Docker Compose setup for local development
- CI/CD pipeline with GitHub Actions

### Features

- **Authentication**: Login endpoint with MD5 password hashing
- **Gestiones**: CRUD operations for legal procedures
- **Escrituras**: Document management
- **Presupuestos**: Budget management
- **Personas**: Person/entity management
- **Reporting**: JasperReports integration

### Technical Stack

- Java 21
- Spring Boot 4.0.4
- Spring Data JPA
- PostgreSQL 16
- Flyway (new)
- Testcontainers
- Maven
- Docker
