# Notaire worker brief (read fully, it is short)

You are the **worker** agent on Notaire. A **foreman** script drives you one
phase at a time and checks your output with deterministic gates. You do only
the phase you are given. Do not jump ahead, do not redo earlier phases.

This brief is a faithful summary of `CONSTITUTION.md`. Do NOT read
CONSTITUTION.md or `.claude/rules/*` end-to-end — they are too long for your
context. Open a specific section only if the phase prompt tells you to.

## Project in one screen

- Monorepo. `backend-api/` = Spring Boot 4.1, Java 21, PostgreSQL 16.
  `frontend/` = Next.js 16, React 19, TypeScript, Tailwind. DTOs live in `backend-api` (`dto` package).
- Backend package root `com.licensis.notaire`:
  `api` (REST controllers) · `service` · `repository` (Spring Data — use this for
  new code) · `negocio` (entities) · `jpa` (LEGACY, never add to it) · `config`.
- REST URLs `/api/v1/<plural-noun>`. DTOs: Java records named `Dto<Entity>...`.
  Controllers never take a JPA entity as `@RequestBody`; use a DTO + `@Valid`.
- Money is `BigDecimal` (scale 2), never float/double.
- Database schema = Flyway only: new file
  `backend-api/src/main/resources/db/migration/V{next}__snake_description.sql`.
  NEVER edit an existing `V*` migration. Idempotent SQL (`IF NOT EXISTS`).
- Frontend: colors/spacing only from `frontend/src/theme/tokens.ts`; forms use
  `FormContainer → FormSection → FormField → FormActions`
  (`@/theme/form-patterns`). All user text goes through i18n.
- Secrets only in `.env` (git-ignored). Never hardcode credentials.

## Code rules

- Java: 4 spaces, max 120 chars/line, no wildcard imports, import order
  java → javax → third-party → `com.licensis`. Constructor injection.
  Methods ≤ 30 lines, ≤ 4 params. `Optional` for nullable returns.
  Catch specific exceptions; never swallow one.
- Comments explain WHY, never WHAT. No commented-out code. Remove dead code
  you touch. Do not refactor code unrelated to the issue.
- Match the style of the surrounding file. Read a neighbour file before
  creating a new one of the same kind.

## Test rules (TDD is mandatory)

- Write the failing test FIRST, run it, see it fail, then implement.
- JUnit 5 + AssertJ + Mockito. Name: `shouldXxxWhenYyy`, with `@DisplayName`.
  AAA layout (Arrange / Act / Assert).
- Unit tests: `backend-api/src/test/java/com/licensis/notaire/unit/...`
  Integration (H2/Spring): `backend-api/src/test/java/com/licensis/notaire/integration/...`
- Frontend component tests: `frontend/src/**/*.test.tsx` (Vitest).
  Playwright E2E: `testing/e2e/tests/TS-nnnn-<workflow>.spec.ts`.
- A test exercises real code/resources. Classpath lookups: `getClass().getResource("/x")`
  (leading slash) but `getClassLoader().getResource("x")` (NO slash — with a slash it
  is always null, a vacuous test).
- Never `@Disabled`/`.skip` a test, never weaken an assertion to go green,
  never accept HTTP 500 as a passing result.
- Commands:
  - one backend class: `mvn -q -B test -pl backend-api -Dtest=ClassName`
  - frontend unit: `cd frontend && npx vitest run <path>`
  - lint/format fix: `bash scripts/preflight.sh --fix`

## Git rules

- The foreman owns branches: you never run `git checkout`, `git switch`,
  `git branch` or `git push` (the foreman pushes).
- You are already on the right branch. Never commit to `main`, never push to
  `main`, never force-push, never rewrite history, never `git stash`.
- Commits are atomic, Conventional Commits:
  `<type>(<scope>): <description>` — types: feat fix docs refactor test chore ci.
  Body line references the issue: `Refs #<n>`. Only the FINAL implementation
  commit ends with `Closes #<n>`.
- Never commit `.localai/`, `.env`, build output, logs, or `*.backup` files.

## OpenSpec (the specification)

Every change lives in `docs/openspec/changes/<change>/` with
`proposal.md`, `traceability.md`, `specs/<capability>/spec.md`, `design.md`,
`tasks.md`. Get exact instructions for an artifact with:
`openspec instructions <artifact> --change <change>`.
Checks: `openspec validate <change> --strict` and
`bash scripts/validate-sdlc-plan.sh <change>`.
A worked, approved example: `docs/openspec/changes/archive/2026-09-19-fix-bruno-login-field-names/`.
Markdown lint (MD040): only the OPENING fence gets a language (```` ```text ````);
the closing fence is always a bare ```` ``` ````.

## How to behave

- **Do only the current phase, then stop.** Never start the next phase, never
  ask "should I proceed?". Files you change outside the phase's scope are
  reverted by the foreman and count as a failure.
- When the foreman gives you a skeleton file, edit it in place and keep its
  headings, keys and line formats exactly.

- Search with `git grep -n "<text>"` and list files with `git ls-files | grep <name>`:
  they read only tracked files. `grep -r` and `find .` walk `node_modules/` and `target/`,
  take seconds and flood your context with minified code.
- Small steps. After each edit, run the narrowest command that proves it.
- Never run the FULL backend/frontend suite — the foreman runs it after you and
  sends you any failure. You run only single test classes (`-Dtest=...`).
- Long commands: if the tool says a command is still running, wait on THAT
  session. Never start the same command again, never `sleep && mvn` — two Maven
  runs on one `target/` corrupt each other.
- Edit files ONLY with the harness edit tool (`apply_patch` does not work here, `sed` line numbers
  shift after the first edit, and `cat >` rewrites drop the parts you did not read):
      python3 {{EDIT}} path/to/File.java <<'EOF'
      <<<<<<< OLD
      exact existing lines, copied from the file
      =======
      the new lines (leave empty to delete)
      >>>>>>> NEW
      EOF
  It changes nothing unless OLD occurs exactly once, so it is safe to rerun. Several blocks may
  follow each other. New files: `cat > path <<'EOF'` is fine. Change only lines the spec names.
- After every edit check `git diff --numstat`. If a file is wrong, `git checkout -- <file>` and redo
  it from scratch — never stack corrective edits.
- Before EVERY commit, run the phase's test command: each commit must compile and pass on its own.
- Stage only what the current commit is about, right before that commit.
- Stay inside your phase: CHANGELOG and docs belong to the docs phase only.
- If a command fails, read the error and fix the cause. Do not retry blindly.
- Write facts, not claims: every "passed" you write must come from a command
  you actually ran in this phase. If something is n/a, say n/a and why.
- If you are truly blocked (missing info, contradictory requirement), write the
  reason to `.localai/<issue>/BLOCKED.md` and stop. The foreman will help.
- Finish every phase with a 3–6 line summary: what changed, what you ran,
  the result.
