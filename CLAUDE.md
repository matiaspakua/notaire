# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ⚠️ MANDATORY WORKFLOW

**`CONSTITUTION.md` at the repo root is the highest authority for this process.**
It is agent-agnostic and prevails over this file. Read it before any change.

**Before ANY code change, READ and FOLLOW the AI Agent Development Workflow:**

```
@CONSTITUTION.md
@.claude/rules/ai-agent-workflow.md
@.claude/skills/ai-agent-workflow/SKILL.md
```

Specifications are produced with OpenSpec (schema `notaire-sdlc`, which encodes
the Constitution): `openspec new change "<name>"`, then
`bash scripts/validate-sdlc-plan.sh` to reject an incomplete plan.

### Quick Workflow Summary

```
0. Verify Issue + Use Case (Caso de Uso) — MANDATORY, no exceptions
0.5 Specification via OpenSpec (Gate 1) — proposal, traceability, specs, design, tasks
1. Create branch from updated main: <type>/<#>_<description>
1.5 Move issue to IN PROGRESS
2. TDD — write failing tests FIRST, then implement
3. Implement (make tests pass)
4. Refactor — KIS, SRP, remove dead/duplicate code
5. Run ALL tests: unit + integration + E2E Playwright
6. Commit (Conventional Commits + Closes #issue)
7. Push to remote
8. Update documentation (business + engineering; archive outdated)
9. Create PR + Close Issue
```

### Full Workflow Details

See `.claude/rules/ai-agent-workflow.md` for complete workflow with:
- Step-by-step instructions
- Branch naming conventions
- Test requirements
- Commit message format
- PR creation
- Quality gates

---

## Rules & Standards (always enforced)

@.claude/rules/general.md
@.claude/rules/programming.md
@.claude/rules/code-quality.md
@.claude/rules/refactoring.md
@.claude/rules/ai-agent-workflow.md
@.claude/rules/ui-ux-design.md

## Project Overview

Multi-module Maven project refactoring a Java Swing monolith to microservices. Spring Boot 4.1.0, Java 21, PostgreSQL 16.

**Modules:**
- `backend-api` — Spring Boot REST API (main development target)
- `notaire-shared` — Shared DTOs and common code
- `frontend-swing` — **Removed.** The legacy Swing GUI client was deprecated and
  deleted from the repository; do not recreate it. All new client work belongs
  in `frontend/` (Next.js).

## Build & Run Commands

```bash
# Build all modules
mvn clean install

# Build backend only (with shared dependency)
mvn clean install -pl backend-api -am

# Start database + backend (Docker)
bash scripts/start.sh
# Stop
bash scripts/stop.sh
# Logs
bash scripts/logs.sh

# Start the FULL system: application + observability/quality infra
bash scripts/start-all.sh        # = start.sh then start-infra.sh
bash infra/scripts/start-infra.sh      # infra only (app must be up first)

# Run backend directly (needs local PostgreSQL on 5432)
cd backend-api && mvn spring-boot:run
```

## Credentials & Environment (`.env`)

All service credentials live in a **single, git-ignored `.env` file at the repo
root** (copy from `.env.example`). Both `docker-compose.yml` (app) and
`infra/docker-compose.yml` (observability) read from it. Never hard-code
secrets in compose files or docs — add a key to `.env(.example)` instead.

## Observability & Quality Infrastructure (`infra/`)

Runs alongside the app and is wired to it (see `infra/README.md`):
- **Prometheus** (`:9090`) scrapes the backend `/actuator/prometheus`
  (Basic auth `ACTUATOR_USER`/`ACTUATOR_PASSWORD`) and `postgres-exporter`.
- **Grafana** (`:3001`) — provisioned dashboards `notaire-backend`,
  `notaire-postgres`, `notaire-logs`.
- **Loki + Promtail** — aggregate the backend's structured JSON logs
  (Logback `LogstashEncoder`); query `{container_name="notary-backend"}`.
- **SonarQube** (`:9000`) — run `bash infra/scripts/run-sonar.sh` to analyze the backend.
- **Homer** (`:8888`) — landing page linking every service.

**Business audit:** the `AuditoriaAspect` records create/update/delete
operations (and logins) into `registro_auditoria`, attributing the acting user
from the authenticated JWT identity (`SecurityContextHolder`), not from any
client-supplied header. Read-only GETs are not audited. Surfaced in the UI at
`/dashboard/auditoria`.

**Ports:** Backend API `8080`, PostgreSQL `5432`, pgAdmin `5050`
**Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Testing Commands

```bash
# All tests
mvn test -pl backend-api

# Single test class or method
mvn test -pl backend-api -Dtest=PresupuestoEntityTest
mvn test -pl backend-api -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields

# Unit tests only
mvn test -pl backend-api -Dtest="**/unit/*"

# Integration tests only (require running PostgreSQL; H2-based tests work standalone)
mvn test -pl backend-api -Dtest="**/integration/*"

# Coverage check (enforced ratchet floor via JaCoCo on `mvn verify`; 80% is the target)
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api  # HTML report at backend-api/target/site/jacoco/index.html

# Code quality
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
mvn verify -pl backend-api  # all checks

# HTTP integration tests (requires running API)
bash testing/scripts/test.sh
```

## ⚠️ CI Preflight — run BEFORE every push

**`mvn verify` is NOT sufficient to predict CI.** Spotless is deliberately
unbound from the Maven lifecycle (see #705), so it only runs in CI's "Code Lint"
job — a branch can be fully green locally and still fail CI on formatting.

Always validate with the preflight script, which mirrors every CI gate:

```bash
bash scripts/install-git-hooks.sh   # once per clone: pre-push runs the gates automatically
bash scripts/preflight.sh --fix     # auto-fix formatting/lint, then verify
bash scripts/preflight.sh           # all blocking gates except server-backed suites
bash scripts/preflight.sh --full    # adds Playwright E2E + Bruno API tests + Docker build/smoke test (needs stack up)
bash scripts/preflight.sh --list    # local check -> CI job mapping
```

**When you add or change a gate in `.github/workflows/`, update
`scripts/preflight.sh` in the same PR** — otherwise the local/CI gap reopens.

Full details: `docs/300-development/CI-PREFLIGHT.md`

## Backend Architecture (`backend-api`)

Package root: `com.licensis.notaire`

| Package | Role |
|---------|------|
| `api` | REST controllers (`@RestController`), one per domain entity |
| `service` | Thin services (`EscrituraService`, `PersonaService`, `RegistroAuditoriaService`) |
| `jpa` | Legacy-style JPA controllers — heavy data-access classes wrapping entity queries |
| `negocio` | Domain/entity classes (`@Entity`) — the core data model |
| `repository` | Spring Data JPA repositories (`JpaRepository`) |
| `config` | Spring configuration beans |

**Key architectural note:** The `jpa` package contains `*JpaController` classes (not REST controllers) — these are large data-access classes migrated from the original monolith. They are being superseded by the `repository` package (Spring Data repos). New code should use `repository`, not `jpa`.

**Database:** PostgreSQL 16 via Docker. ORM: Hibernate (PostgreSQLDialect), `ddl-auto=none` (Hibernate never creates/alters the schema).

> ✅ **Flyway is the single source of truth.** The Docker stack now uses Flyway
> as the sole mechanism to create the database schema. PostgreSQL starts empty;
> Flyway applies V1→V11+ sequentially. The old `init-db/` scripts have been
> archived at `docs/archive/init-db/`. The guard test is
> `FlywaySchemaValidationIntegrationTest` — run `mvn test -Ppg-integration`.
> See `.claude/rules/database-migrations.md`.

**Reports:** JasperReports (`.jasper`/`.jrxml`) in `src/main/resources/reportes/`. The `ReporteController` handles report generation.

**Tests:** Organized under `src/test/java/.../unit/` and `integration/`. `ApiH2IntegrationTest` uses H2 in-memory; `ApiIntegrationTest` requires a running PostgreSQL.

## Key Conventions

- DTOs named `DtoEntityName` (e.g., `DtoUsuario`, `DtoPersona`)
- REST URLs: `/api/v1/resource` (plural nouns)
- Test methods: `shouldXxxYyy` with `@DisplayName`; use AssertJ (`assertThat(...)`)
- No wildcard imports; import order: java → javax → third-party → own packages
- Line limit 120 chars, 4-space indent

## Frontend Architecture (`frontend`)

**Stack:** Next.js 16, React 19, TypeScript, Tailwind CSS

**Key Directories:**
- `src/components/ui/` — Base UI components (Button, Input, Card, etc.)
- `src/theme/` — **Centralized design system** (tokens, utilities, form patterns)
- `src/app/` — Page components and routes
- `src/store/` — Zustand stores (auth, UI state)
- `src/hooks/` — Custom React hooks
- `src/lib/` — Utilities (API client, formatters, validators)

### Design System & Form Development

**MANDATORY**: All forms must use the centralized design system.

When working on frontend forms:
1. **Use the theme system**: `@/theme/tokens.ts` — single source of truth for all colors, spacing, typography
2. **Follow form patterns**: Use `FormContainer`, `FormSection`, `FormField`, `FormActions` from `@/theme/form-patterns.tsx`
3. **Reference the rules**: `@.claude/rules/ui-ux-design.md` — Apple design language standards
4. **Use the skill**: `@.claude/skills/frontend-design/SKILL.md` — Implementation patterns and examples

**Key Theme Files:**
- `src/theme/tokens.ts` — Design tokens (colors, spacing, typography, shadows, etc.)
- `src/theme/index.ts` — Utilities and hooks for using tokens
- `src/theme/form-patterns.tsx` — Reusable form component patterns
- `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` — Full design system documentation

**Form Development Pattern:**
```tsx
import { FormContainer, FormField, FormSection, FormActions, FormHeader } from "@/theme/form-patterns";
import { theme } from "@/theme/tokens";

export function MyForm() {
  return (
    <FormContainer>
      <FormHeader title="Form Title" description="Description" />
      
      <FormSection title="Section 1">
        <FormField label="Field" required>
          <Input placeholder="..." />
        </FormField>
      </FormSection>

      <FormActions align="right">
        <Button variant="secondary">Cancel</Button>
        <Button variant="default">Submit</Button>
      </FormActions>
    </FormContainer>
  );
}
```

**Conventions:**
- No hardcoded colors, spacing, or dimensions — use theme tokens exclusively
- All forms follow `FormContainer` → `FormSection` → `FormField` structure
- Buttons: use `variant="default"` (primary), `variant="secondary"` (cancel), `variant="destructive"` (dangerous)
- Inputs: always include labels (not placeholders), helper text for complex fields
- Responsive: mobile-first, test on 320px (mobile), 768px (tablet), 1024px (desktop)
- Accessibility: color contrast ≥4.5:1, keyboard navigation, focus indicators

## Git Workflow

### Branch Naming: `<type>/<issue-number>_<description>`

| Type | When to Use | Example |
|------|-------------|---------|
| `feat` | New feature | `feat/253_user_auth` |
| `fix` | Bug fix | `fix/254_login_timeout` |
| `refactor` | Code refactor | `refactor/255_cleanup` |
| `test` | Tests only | `test/256_new_tests` |
| `docs` | Documentation | `docs/257_readme` |
| `chore` | Maintenance | `chore/258_deps` |
| `ci` | CI/CD | `ci/259_workflow` |
| `design` | Design/UI updates | `design/260_theme_updates` |

### Commit Format: [Conventional Commits](https://www.conventionalcommits.org/)

```
<type>(<scope>): <description>

[optional body]

Closes #<issue-number>
```

### Never
- ❌ Commit directly to `main`
- ❌ Skip tests
- ❌ Leave failing tests
- ❌ Hardcode credentials

## Prohibited

- Backend: Swing imports, direct DB access from controllers
- Frontend: JDBC/SQL, business logic in event handlers
- General: Hardcoded credentials, ignored exceptions, wildcard imports
