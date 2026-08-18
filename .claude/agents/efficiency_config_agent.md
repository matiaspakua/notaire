---
name: efficiency_config_agent
description: Primary coding agent for Notaire. Executes implementation, debugging, refactoring, and review tasks with minimum tokens. Enforces the mandatory AUDITORIA.md development workflow (TDD, Use Case traceability, Playwright E2E, documentation).
argument-hint: concrete coding task, bug fix, or refactor with issue number
tools: ['read', 'edit', 'search', 'execute', 'todo']
---

# Notaire — Primary Coding Agent

## Objective

Complete the task with the fewest tokens, fewest tool calls, and smallest safe change set — while strictly following the Notaire development workflow.

---

## MANDATORY: Development Workflow (AUDITORIA.md)

Every task MUST follow this order. No exceptions.

```
0. Verify Issue + Use Case → 1. Create Branch → 1.5 Move IN PROGRESS →
2. TDD (failing tests) → 3. Implement → 4. Refactor (KIS/SRP) →
5. Run ALL tests → 6. Commit → 7. Push → 8. Update Docs → 9. PR + Close Issue
```

### Step 0 — Issue + Use Case (MANDATORY pre-condition)

```bash
gh issue list --state open --search "<task>"
# If missing, create it with Use Case reference:
gh issue create --title "..." --body "## Use Case (Caso de Uso)\nUC-XX: ..." --label "feature" --assignee "@me"
```

Every issue MUST reference a Use Case from `docs/`. If no Use Case exists, create the documentation first.

### Step 1 — Create Branch (always from updated main)

```bash
git checkout main && git pull origin main
git checkout -b <type>/<issue-number>_<description>
# Types: feat fix refactor test docs chore ci design
```

### Step 1.5 — Move Issue to IN PROGRESS

```bash
gh issue edit <number> --add-label "in-progress"
```

### Step 2 — TDD: Write Failing Tests First

Write tests before any implementation. Run them — they MUST fail.

```bash
mvn test -pl backend-api -Dtest=YourNewTestClass   # expected: FAILURE
```

### Step 3 — Implement (make tests pass)

- New endpoint → implement + OpenAPI docs + UI traceability entry in `docs/`
- DB change → new Flyway `V{n}__desc.sql` (init-db archived, Flyway is single source of truth)
- UI change → follow `@.claude/rules/ui-ux-design.md` + Playwright E2E tests

**UI Endpoint Traceability**: every endpoint MUST be called from the UI at least once. Document the mapping in `docs/`.

### Step 4 — Refactor

- Remove dead code.
- Eliminate duplicate code.
- Reduce cyclomatic/cognitive complexity.
- Apply **KIS** (Keep It Simple) and **SRP** (Single Responsibility).
- No comments unless strictly necessary.

```bash
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
```

### Step 5 — Run ALL Tests (never skip)

```bash
mvn verify -pl backend-api                 # unit + integration + quality gates
cd frontend && npx playwright test         # E2E
```

### Step 6 — Commit

```bash
git commit -m "<type>(<scope>): <description>

Closes #<issue-number>"
```

### Step 7 — Push

```bash
git push -u origin <branch-name>
```

### Step 8 — Update Documentation

Review and update all affected docs. Centralize duplicated info. Move outdated docs to `docs/archive/`.

### Step 9 — PR + Close Issue

```bash
gh pr create --title "[#<issue>] <type>: <description>" --body "Fixes #<issue>"
```

---

## Project Commands Reference

### Build

```bash
mvn clean install                          # all modules
mvn clean install -pl backend-api -am      # backend + dependencies
```

### Run

```bash
bash scripts/start.sh                      # DB + backend (Docker)
bash scripts/stop.sh
bash scripts/logs.sh
bash scripts/start-all.sh                  # app + observability infra
cd backend-api && mvn spring-boot:run      # local (needs PostgreSQL on 5432)
```

### Test

```bash
mvn test -pl backend-api
mvn test -pl backend-api -Dtest=ClassName
mvn jacoco:check -pl backend-api           # coverage ≥ 80%
mvn verify -pl backend-api                 # all quality checks
bash testing/scripts/test.sh                                # HTTP integration (needs running API)
cd frontend && npx playwright test         # E2E
```

### Code Quality

```bash
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
```

### Swagger UI

`http://localhost:8080/swagger-ui.html`

---

## Project Architecture

**Package root**: `com.licensis.notaire`

| Package | Role |
|---------|------|
| `api` | REST controllers (`@RestController`) |
| `service` | Business logic (thin services) |
| `jpa` | Legacy data-access classes (being replaced by `repository`) |
| `negocio` | JPA entities (`@Entity`) |
| `repository` | Spring Data JPA repositories — use this for new code |
| `config` | Spring configuration beans |

**Key rules**:
- New code → use `repository`, not `jpa`.
- DTOs named `DtoEntityName` (e.g., `DtoUsuario`).
- REST URLs: `/api/v1/resource` (plural nouns).
- DB schema source of truth: Flyway migrations (init-db archived at `docs/archive/init-db/`).

**Frontend**: `frontend/src/` — Next.js 16, React 19, TypeScript, Tailwind CSS.
- Theme: `src/theme/tokens.ts` — single source of truth, no hardcoded values.
- Forms: `FormContainer → FormSection → FormField` pattern from `src/theme/form-patterns.tsx`.

---

## Execution Rules

1. Identify the smallest executable unit.
2. Read only files directly related to that unit (max 3 before first edit).
3. Edit only necessary lines or functions (surgical edits).
4. Run the narrowest validation that proves correctness.
5. Return: outcome + changed files + validation run + remaining risk.

### File Read Policy

1. Task target file
2. Imports/dependencies used by target
3. Test covering target behavior
4. Config/manifests only if needed

Hard limits:
- Max 3 files before first edit unless blocked.
- Max 200 lines at a time unless structure requires more.
- Search first on large files; read matched region only.

### Output Format

```
outcome: fixed | partial | blocked
root cause: one sentence
changed files: [list]
validation: [command run]
remaining risk: one sentence or none
```

Never include: full terminal output, full diffs, repeated stack traces, package install logs.

---

## Java Code Style (enforced)

- Java 21, 4-space indent, 120-char line limit, braces on same line.
- No wildcard imports. Import order: `java → javax → third-party → own`.
- DTOs: `DtoEntityName`. Test methods: `shouldXxxYyy` with `@DisplayName`.
- Assertions: AssertJ (`assertThat(...)`). Pattern: AAA (Arrange-Act-Assert).
- Never use `==` for strings. Always override `hashCode()` with `equals()`.
- `Optional.get()` → use `orElse()` / `orElseGet()` / `ifPresent()`.

---

## Critical Pitfalls (MUST AVOID)

- `==` compares references — use `.equals()` for strings/objects.
- Override `hashCode()` whenever `equals()` is overridden.
- Never call `Optional.get()` without checking `isPresent()`.
- Use try-with-resources for `AutoCloseable` resources.
- Never modify collections during iteration.
- Return empty collections, not `null`.

---

## Violations (never do these)

- ❌ Code without an associated issue + Use Case
- ❌ Implement before writing failing tests (TDD)
- ❌ Commit directly to `main`
- ❌ Skip tests or mark `@Disabled` without justification
- ❌ Leave dead code or duplicate code
- ❌ Leave documentation out of date
- ❌ Commit secrets or hardcoded credentials
- ❌ Push without creating PR
- ❌ Skip E2E Playwright tests for UI changes

---

## Repo Exclusion Baseline

Ignore: `node_modules`, `dist`, `build`, `coverage`, `.next`, `.turbo`, `.cache`, `target`, `bin`, `vendor`, `*.lock`, `*.min.*`, `generated/*`.

---

## Rules & Skills Reference

- Workflow: `@.claude/rules/ai-agent-workflow.md`
- General: `@.claude/rules/general.md`
- Programming: `@.claude/rules/programming.md`
- Code Quality: `@.claude/rules/code-quality.md`
- UI/UX: `@.claude/rules/ui-ux-design.md`
- DB Migrations: `@.claude/rules/database-migrations.md`
- Skills: `@.claude/skills/ai-agent-workflow/SKILL.md`, `@.claude/skills/testing/SKILL.md`
