# AGENTS.md — Notaire Agent Reference

Single entry point for every coding agent (Claude Code, OpenCode, Codex, GitHub Copilot,
Cursor). `CLAUDE.md` only imports this file. All agents enforce the mandatory development
workflow defined in `CONSTITUTION.md` and `.claude/rules/ai-agent-workflow.md`.

## ⚠️ MANDATORY WORKFLOW

**`CONSTITUTION.md` at the repo root is the highest authority for this process.**
It is agent-agnostic and prevails over this file. Read it before any change.

**Before ANY code change, READ and FOLLOW the AI Agent Development Workflow:**

```text
@CONSTITUTION.md
@.claude/rules/ai-agent-workflow.md
@.claude/skills/ai-agent-workflow/SKILL.md
```

Specifications are produced with **OpenSpec** (schema `notaire-sdlc`, which encodes the
Constitution). OpenSpec lives in `docs/openspec/` and the CLI resolves its root from the
working directory, so **run every `openspec` command from `docs/`**:

```bash
cd docs
openspec new change "<kebab-case-name>"      # scaffolds the mandatory artifacts
openspec status --change "<name>"            # artifact build order
openspec instructions <artifact> --change "<name>"
openspec validate "<name>" --strict          # structural checks
bash workspace/sdlc/validate-sdlc-plan.sh           # Constitution checks (--list explains them)
```

The Constitution reaches every agent through the `openspec` CLI itself
(`docs/openspec/config.yaml` → `context` and `rules`). No change is Done until the plan is
complete and every Quality Gate has passed.

### Quick workflow summary

```text
0. Verify Issue + Use Case (Caso de Uso) — MANDATORY, no exceptions
0.5 Specification via OpenSpec (Gate 1) — proposal, traceability, specs, design, tasks
1. Create branch from updated main: <type>/<#>_<description>
1.5 Move issue to IN PROGRESS
2. TDD — write failing tests FIRST (watch them fail), then implement
3. Implement (make tests pass)
4. Refactor — KIS, SRP, remove dead/duplicate code
5. Run ALL tests: unit + integration + E2E Playwright
6. Commit (Conventional Commits + Closes #issue)
7. Push to remote
8. Update documentation (business + engineering; archive outdated → docs/000-archive/)
9. Create PR + Close Issue
```

Full details: `.claude/rules/ai-agent-workflow.md`.

Part of this workflow is enforced by Claude Code hooks in `.claude/settings.json`
(session-start OpenSpec status, push-to-main guard); see `.claude/rules/hooks.md`.

---

## Skills and agents catalogs

`.claude/skills/` is the only skill catalog and `.claude/agents/` the only agent catalog
(see `CONSTITUTION.md` §10). Other tools read them from there.

### Available agents

| Agent | File | Role |
|-------|------|------|
| **efficiency_config_agent** | `.claude/agents/efficiency_config_agent.md` | **Primary coding agent.** Implementation, debugging, refactoring. Enforces the CONSTITUTION.md workflow. |
| **Code Reviewers** | `.claude/agents/code-reviewer.md` | Code review: correctness, security, KIS/SRP, workflow compliance. |
| **java-architect** | `.claude/agents/java-architect.md` | Java/Spring Boot architecture decisions, package structure, migration from legacy `jpa`. |
| **devops-engineer** | `.claude/agents/devops-engineer.md` | Docker, CI/CD, observability (Prometheus/Grafana/Loki), scripts. |
| **Security Auditors** | `.claude/agents/security-auditor.md` | OWASP Top 10, auth/authz, dependency CVEs, configuration security. |
| **Sync Issues and Code** | `.claude/agents/sync_issues_and_code.md` | GitHub issue ↔ code sync: Use Case validation, IN PROGRESS state, PR linkage. |

### Cursor Cloud AI SDLC fleet

Orchestrated autonomous loop (issue → OpenSpec → TDD → PR → CI → merge) on **Cursor Cloud**.
Architecture, models, handoffs, env checklist, and validation:
[`docs/300-development/304-ai-sdlc-cloud/`](docs/300-development/304-ai-sdlc-cloud/).
**Does not use `local-ai/`.**

| Agent | File | Role |
|-------|------|------|
| **cloud-foreman** | `.claude/agents/cloud-foreman.md` | Orchestrator: pick issue, Gate 1, dispatch, PR, CI watch, merge. |
| **openspec-planner** | `.claude/agents/openspec-planner.md` | Gate 1 specs, triage, analyst/product-owner skills. |
| **backend-implementer** | `.claude/agents/backend-implementer.md` | Backend TDD + implement (`backend`, `java`, Flyway). |
| **frontend-design** | `.claude/agents/frontend-design.md` | Next.js UI + design system + Playwright. |
| **testing-qa** | `.claude/agents/testing-qa.md` | Gate 2 test design; JUnit/Vitest/Bruno/Playwright. |

The specialists above (`java-architect`, `devops-engineer`, `security-auditor`,
`code-reviewer`, `sync_issues_and_code`, `efficiency_config_agent`) are dispatched from the
foreman per [`fleet-manifest.yaml`](docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml).

---

## Modules (the Foreman's map)

`workspace/modules.yaml` lists every module (responsibility, fleet, verify command,
dependencies); each has a `MODULE.md` and a `verify.sh`; seams between modules are in
`contracts/seams.yaml` (ADR-026). The Foreman uses:

```bash
python3 workspace/modules.py list                      # dependency order
python3 workspace/modules.py affected <changed path>   # modules a change touches + dependents
python3 workspace/modules.py verify <module>|--all     # run the module's own checks
```

---

## Rules & Standards (always enforced)

```text
@.claude/rules/general.md
@.claude/rules/programming.md
@.claude/rules/code-quality.md
@.claude/rules/refactoring.md
@.claude/rules/ai-agent-workflow.md
@.claude/rules/ui-ux-design.md
```

| Rule | File |
|------|------|
| Development Workflow | `.claude/rules/ai-agent-workflow.md` |
| General | `.claude/rules/general.md` |
| Programming | `.claude/rules/programming.md` |
| Code Quality | `.claude/rules/code-quality.md` |
| UI/UX Design | `.claude/rules/ui-ux-design.md` |
| DB Migrations | `.claude/rules/database-migrations.md` |
| Refactoring | `.claude/rules/refactoring.md` |
| Hooks | `.claude/rules/hooks.md` |

---

## Project Overview

Multi-module Maven project refactoring a Java Swing monolith to microservices. Spring Boot
4.1.0, Java 26, PostgreSQL 16.

**Modules:**

- `backend-api` — Spring Boot REST API (main development target)
- `notaire-shared` — **Retired** (ADR-025, #1255). DTOs now live in `backend-api`
  (`com.licensis.notaire.dto`); the folder is archived in `deprecated/`. External
  services consume the REST API (OpenAPI), never Java DTO classes.
- `frontend-swing` — **Removed.** The legacy Swing GUI client was deprecated and
  deleted from the repository; do not recreate it. All new client work belongs
  in `frontend/` (Next.js).

## Build & Run

```bash
mvn clean install                       # all modules
mvn clean install -pl backend-api -am   # backend only (with shared dependency)

bash workspace/stack/start.sh                   # DB + backend (Docker)
bash workspace/stack/stop.sh
bash workspace/stack/logs.sh
bash workspace/stack/start-all.sh               # application + observability/quality infra
bash infra/scripts/start-infra.sh       # infra only (app must be up first)

cd backend-api && mvn spring-boot:run   # backend directly (needs local PostgreSQL on 5432)
```

**Ports:** Backend API `8080`, PostgreSQL `5432`, pgAdmin `5050`.

## Credentials & Environment (`.env`)

All service credentials live in a **single, git-ignored `.env` file at the repo root**
(copy from `.env.example`). Both `docker-compose.yml` (app) and
`infra/observability/docker-compose.yml` (observability) read from it. Never hard-code
secrets in compose files or docs — add a key to `.env(.example)` instead.

## Observability & Quality Infrastructure (`infra/`)

Runs alongside the app and is wired to it (see `infra/README.md`):

- **Prometheus** (`:9090`) scrapes the backend `/actuator/prometheus`
  (Basic auth `ACTUATOR_USER`/`ACTUATOR_PASSWORD`) and `postgres-exporter`.
- **Grafana** (`:3001`) — provisioned dashboards `notaire-backend`, `notaire-postgres`,
  `notaire-logs`.
- **Loki + Promtail** — the backend writes structured JSON logs to stdout only (Logback
  `LogstashEncoder`, no log files); Promtail ships them. Query
  `{container_name="notary-backend"}`.
- **SonarQube** (`:9000`) — run `bash infra/scripts/run-sonar.sh` to analyze the backend.
- **Homer** (`:8888`) — landing page linking every service.

**Business audit:** the `AuditoriaAspect` records create/update/delete operations (and
logins) into `registro_auditoria`, attributing the acting user from the authenticated JWT
identity (`SecurityContextHolder`), not from any client-supplied header. Read-only GETs are
not audited. Surfaced in the UI at `/dashboard/auditoria`.

### Key URLs (local)

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Grafana | http://localhost:3001 |
| Prometheus | http://localhost:9090 |
| pgAdmin | http://localhost:5050 |
| SonarQube | http://localhost:9000 |
| Homer | http://localhost:8888 |

## Testing Commands

```bash
mvn test -pl backend-api                                   # all tests
mvn test -pl backend-api -Dtest=PresupuestoEntityTest      # single class
mvn test -pl backend-api -Dtest=PresupuestoEntityTest#shouldCreatePresupuestoWithRequiredFields
mvn test -pl backend-api -Dtest="**/unit/*"                # unit only
mvn test -pl backend-api -Dtest="**/integration/*"         # integration (H2 tests run standalone)

# Coverage (enforced ratchet floor via JaCoCo on `mvn verify`; 80% is the target)
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api  # backend-api/target/site/jacoco/index.html

mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
mvn verify -pl backend-api                                 # all checks

bash testing/scripts/test.sh                               # HTTP integration (API running)
bash testing/scripts/run.sh database                       # database V&V (Docker only)
cd testing/e2e && npx playwright test                      # E2E
```

## ⚠️ CI Preflight — run BEFORE every push

**`mvn verify` is NOT sufficient to predict CI.** Spotless is deliberately unbound from the
Maven lifecycle (see #705), so it only runs in CI's "Code Lint" job — a branch can be fully
green locally and still fail CI on formatting. Validate with the preflight script, which
mirrors every CI gate:

```bash
bash workspace/sdlc/install-git-hooks.sh   # once per clone: pre-push runs the gates automatically
bash workspace/sdlc/preflight.sh --fix     # auto-fix formatting/lint, then verify
bash workspace/sdlc/preflight.sh           # all blocking gates except server-backed suites
bash workspace/sdlc/preflight.sh --full    # adds Playwright E2E + Bruno API tests + Docker build/smoke test
bash workspace/sdlc/preflight.sh --list    # local check -> CI job mapping
```

**When you add or change a gate in `.github/workflows/`, update `workspace/sdlc/preflight.sh` in
the same PR.** Full details: `docs/300-development/CI-PREFLIGHT.md`.

## Backend Architecture (`backend-api`)

Package root: `com.licensis.notaire`

| Package | Role |
|---------|------|
| `api` | REST controllers (`@RestController`), one per domain entity |
| `service` | Thin services (`EscrituraService`, `PersonaService`, `RegistroAuditoriaService`) |
| `jpa` | Legacy-style JPA controllers — heavy data-access classes wrapping entity queries |
| `negocio` | Domain/entity classes (`@Entity`) — the core data model |
| `repository` | Spring Data JPA repositories (`JpaRepository`) — **use for new code** |
| `config` | Spring configuration beans |

**Key note:** the `jpa` package contains `*JpaController` classes (not REST controllers),
large data-access classes migrated from the original monolith. They are being superseded by
`repository`. New code uses `repository`, not `jpa`.

**Database:** PostgreSQL 16 via Docker. ORM: Hibernate (PostgreSQLDialect),
`ddl-auto=none` (Hibernate never creates/alters the schema).

> ✅ **Flyway is the single source of truth.** Docker starts PostgreSQL empty and Flyway
> applies V1→V11+ sequentially. The old `init-db/` scripts are archived at
> `docs/000-archive/init-db/`. The guard test is `FlywaySchemaValidationIntegrationTest` —
> run `mvn test -Ppg-integration`. See `.claude/rules/database-migrations.md`.

**Reports:** JasperReports (`.jasper`/`.jrxml`) in `src/main/resources/reportes/`; the
`ReporteController` generates them.

**Tests:** under `src/test/java/.../unit/` and `integration/`. `ApiH2IntegrationTest` uses
H2 in-memory; `ApiIntegrationTest` requires a running PostgreSQL.

## Key Conventions

- DTOs named `DtoEntityName` (e.g., `DtoUsuario`, `DtoPersona`)
- REST URLs: `/api/v1/resource` (plural nouns)
- Test methods: `shouldXxxYyy` with `@DisplayName`; use AssertJ (`assertThat(...)`)
- No wildcard imports; import order: java → javax → third-party → own packages
- Line limit 120 chars, 4-space indent

## Frontend Architecture (`frontend`)

**Stack:** Next.js 16, React 19, TypeScript, Tailwind CSS

- `src/components/ui/` — base UI components (Button, Input, Card, etc.)
- `src/theme/` — **centralized design system** (tokens, utilities, form patterns)
- `src/app/` — pages and routes
- `src/store/` — Zustand stores (auth, UI state)
- `src/hooks/` — custom React hooks
- `src/lib/` — utilities (API client, formatters, validators)

### Design System & Form Development

**MANDATORY**: all forms use the centralized design system.

1. **Theme system**: `@/theme/tokens.ts` — single source of truth for colors, spacing,
   typography
2. **Form patterns**: `FormContainer`, `FormSection`, `FormField`, `FormActions` from
   `@/theme/form-patterns.tsx`
3. **Rules**: `@.claude/rules/ui-ux-design.md` — Apple design language standards
4. **Skill**: `@.claude/skills/frontend-design/SKILL.md` — implementation patterns
5. **Docs**: `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`

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
- Buttons: `variant="default"` (primary), `"secondary"` (cancel), `"destructive"` (dangerous)
- Inputs: always include labels (not placeholders), helper text for complex fields
- Responsive: mobile-first, test on 320px (mobile), 768px (tablet), 1024px (desktop)
- Accessibility: color contrast ≥4.5:1, keyboard navigation, focus indicators

## Git Workflow

### Branch naming: `<type>/<issue-number>_<description>`

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

### Commit format: [Conventional Commits](https://www.conventionalcommits.org/)

```text
<type>(<scope>): <description>

[optional body]

Closes #<issue-number>
```

## Violations (all agents enforce these)

- ❌ Code without an associated issue + Use Case
- ❌ Implement before writing failing tests (TDD)
- ❌ Commit directly to `main`
- ❌ Skip or `@Disabled` tests without justification; leave failing tests
- ❌ Leave dead code or duplicate code
- ❌ Leave documentation out of date
- ❌ Hardcode credentials or secrets
- ❌ Push without creating PR
- ❌ Skip E2E Playwright tests for UI changes
- ❌ Schema change without creating a new Flyway migration
- ❌ Backend: Swing imports, direct DB access from controllers
- ❌ Frontend: JDBC/SQL, business logic in event handlers
- ❌ General: ignored exceptions, wildcard imports
