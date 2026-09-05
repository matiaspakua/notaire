---
title: AI Agent Development Workflow
description: Mandatory workflow for AI coding agents (OpenCode, Claude, GitHub Copilot)
alwaysApply: true
---

# AI Agent Development Workflow

This rule defines the mandatory workflow that **ALL AI coding agents** must follow when implementing changes in the Notaire project.

## Applicable Agents

This rule applies to:

- **OpenCode**: All `/oc` commands and interactions
- **Claude Code**: All Claude AI interactions
- **GitHub Copilot**: All Copilot suggestions and completions
- **Any other AI coding assistant** modifying this codebase

---

## Workflow Overview

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AI AGENT DEVELOPMENT WORKFLOW                             │
└─────────────────────────────────────────────────────────────────────────────┘

   ┌─────────────┐
   │   START     │
   │  User Task  │
   └──────┬──────┘
          │
          ▼
   ┌─────────────────────────┐
   │ 0. CHECK ISSUE          │
   │    + USE CASE (Caso Uso)│◄──────────────────────────────────┐
   └──────┬──────────────────┘                                   │
          │                                                       │
    ┌─────┴─────┐                                                │
    │ YES       │ NO                                              │
    ▼           ▼                                                 │
┌────────┐  ┌──────────────────────────┐                         │
│ Get    │  │ CREATE ISSUE + USE CASE  │─────────────────────────┘
│ Issue# │  │  on GitHub               │
└────┬───┘  └────────┬─────────────────┘
     │               │
     └───────┬───────┘
             ▼
   ┌─────────────────────┐
   │ 1. CREATE BRANCH    │
   │ <type>/<#>-<desc>   │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 1.5 MOVE ISSUE TO   │
   │  IN PROGRESS        │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 2. TDD: WRITE       │◄── Tests FIRST, then implement
   │  FAILING TESTS      │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 3. IMPLEMENT        │
   │  Make tests pass    │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 4. REFACTOR         │◄── Clean code, remove dead code,
   │  + Code quality     │    remove duplicates, KIS + SRP
   └──────────┬──────────┘
              ▼
   ┌─────────────────────────────────────┐
   │ 5. RUN ALL TESTS                    │
   │  Unit + Integration + E2E Playwright│
   └──────────┬──────────────────────────┘
              │
         ┌────┴────┐
         │ PASS?   │
         └────┬────┘
        NO    │ YES
         ▼    │
   ┌─────────┐│
   │ FIX &   ││
   │ RETEST  │┘
   └────┬────┘
        │
        ▼
   ┌─────────────────────┐
   │ 6. COMMIT           │
   │    (Conventional)   │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 7. PUSH TO REMOTE   │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 8. UPDATE DOCS      │
   │  Business + Tech    │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 9. CREATE PR        │
   │    + Close Issue    │
   └──────────┬──────────┘
              │
              ▼
         ┌────────┐
         │  END   │
         └────────┘
```

---

## Step-by-Step Workflow

### Step 0: Pre-Condition — Issue + Use Case Verification

**MANDATORY** Before ANY code change:

```bash
# Check if issue exists
gh issue list --state open --search "<task description>"

# If NOT exists, CREATE IT
gh issue create \
  --title "<descriptive title>" \
  --body "## Description
<detailed description>

## Use Case (Caso de Uso)
<name and ID of the associated use case — MANDATORY>

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Technical Notes
<any relevant technical details>" \
  --label "feature" \
  --assignee "@me"
```

#### Use Case Association (MANDATORY — no exceptions)

Every issue **MUST** be linked to an existing Use Case (Caso de Uso) in the project documentation:

1. Check `docs/` for the relevant Use Case.
2. If no Use Case exists → **create it first** in the documentation, update all related documents (architecture, requirements, etc.), then create the issue referencing it.
3. Record the Use Case ID/name in the issue body.

**Output Required**: Issue number (e.g., `#253`) + Use Case reference.

---

### Step 1: Create Feature Branch

Always pull from main before creating the branch:

```bash
git checkout main && git pull origin main
git checkout -b <type>/<issue-number>_<short-description>
```

**Branch Naming Convention**: `<type>/<issue-number>_<short-description>`

| Type | Use Case |
|------|----------|
| `feat` | New feature or functionality |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no behavior change) |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `chore` | Maintenance tasks |
| `ci` | CI/CD changes |
| `add` | Adding new files or resources |
| `design` | Design/UI updates |

**Examples**:

```bash
# Correct
feat/253_user_authentication
fix/254_login_timeout
refactor/255_move_service_layer

# WRONG
my-branch
feature-user
fix-bug-123
```

---

### Step 1.5: Move Issue to IN PROGRESS

After creating the branch and starting work, update the GitHub issue status:

```bash
# Move issue to IN PROGRESS (using project board label or status field)
gh issue edit <issue-number> --add-label "in-progress"
```

---

### Step 2: TDD — Write Failing Tests First

**MANDATORY**: Apply TDD (Test-Driven Development) for ALL changes.

Order of work:

1. **Analyze the issue** — understand fully before writing any code.
2. **Write tests** that define the expected behavior.
3. **Run tests** — they MUST fail at this point (no implementation yet).
4. Proceed to Step 3 only when tests are written and failing.

This ensures tests drive the design and implementation is provably correct.

```bash
# Verify tests fail before implementing
mvn test -pl backend-api -Dtest=YourNewTestClass
# Expected: FAILURE (that's correct at this stage)
```

#### Test Requirements

| Change Type | Minimum Tests |
|-------------|---------------|
| New feature | Unit tests + Integration tests |
| Bug fix | Tests that reproduce the bug first |
| Refactor | Same tests as before (verify behavior unchanged) |
| New API endpoint | Controller tests + Integration tests + OpenAPI docs |
| Database change | Repository tests + Flyway migration |
| UI change | E2E Playwright tests for every screen/form |

#### Test Location

- **Unit tests**: `src/test/java/.../unit/`
- **Integration tests**: `src/test/java/.../integration/`
- **E2E tests**: `frontend/tests/` (Playwright)

#### Test Naming Convention

```java
@Test
@DisplayName("Should return user when valid ID provided")
void shouldReturnUserWhenValidIdProvided() { }
```

---

### Step 3: Implement Changes

Implement only what is needed to make the failing tests pass. No more.

1. Read existing code to understand context before modifying.
2. Follow project conventions (see `.claude/rules/`).
3. Apply clean code principles — no shortcuts.
4. Keep changes focused and minimal (YAGNI).

#### Special cases

**New REST endpoint**:

- Implement the endpoint.
- Write controller + integration tests.
- Document in OpenAPI/Swagger (`@Operation`, `@ApiResponse`).
- Verify the endpoint appears correctly in Swagger UI.
- Ensure the endpoint is called from the UI (see UI traceability below).

**Database change**:

- Use Flyway migration (new `V{n}__description.sql`).
- Flyway is the single source of truth (init-db archived at `docs/archive/init-db/`).
- Run `mvn test -Ppg-integration` to validate alignment.
- See `.claude/rules/database-migrations.md`.

**UI change**:

- Follow `.claude/rules/ui-ux-design.md` and the design system.
- Write E2E Playwright tests for every screen and form.
- Test golden path AND edge cases.

#### UI Endpoint Traceability (MANDATORY)

Every REST endpoint **MUST** be invoked from the UI at least once. Traceability must be documented in a simple, standard, and clear way:

- Add a comment or entry in `docs/` mapping each endpoint to the UI screen/action that calls it.
- Use the API test collection (Bruno) to confirm the endpoint is exercised end-to-end.

---

### Step 4: Refactor and Code Quality

After implementation, before running the full test suite:

1. **Remove dead code** — any code that is never executed or reachable.
2. **Remove duplicate code** — refactor to a single reusable unit.
3. **Reduce cyclomatic and cognitive complexity** — break up complex methods.
4. **No comments** unless strictly necessary (prefer self-explanatory code).
5. **Apply KIS** (Keep It Simple) — simplest solution that passes all tests.
6. **Apply SRP** (Single Responsibility Principle) — in both code and tests.

```bash
# Check for style violations and static bugs
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
```

---

### Step 5: Run All Tests

**Run the complete test suite before committing. Never skip tests.**

```bash
# Backend unit + integration tests
mvn test -pl backend-api

# Coverage check
mvn jacoco:check -pl backend-api

# All quality checks
mvn verify -pl backend-api

# E2E Playwright (frontend must be running)
cd frontend && npx playwright test
```

**Quality Gates** (all must pass):

- All unit and integration tests pass.
- Coverage ≥ 80% (JaCoCo).
- No Checkstyle violations.
- No SpotBugs warnings.
- All E2E Playwright tests pass.

If any check fails → fix and retest. **No exceptions**.

---

### Step 6: Commit Changes

**Commit Message Format**: [Conventional Commits](https://www.conventionalcommits.org/)

```text
<type>(<scope>): <description>

[optional body]

Closes #<issue-number>
```

**Rules**:

- Imperative mood: "add" not "added"
- First line ≤ 72 characters
- Always reference the issue: `Closes #<number>`

**Examples**:

```bash
git commit -m "feat(api): add document creation endpoint

Implements POST /api/v1/documentos with full validation.
Documented in OpenAPI. E2E test added for the form flow.

Closes #253"

git commit -m "fix(usuarios): resolve user edit failing with 500

Missing DTO mapping caused NPE on update. Added null check
and integration test to reproduce the scenario.

Closes #254"
```

**DO NOT**:

- ❌ Commit without tests
- ❌ Commit failing code
- ❌ Leave commented-out code
- ❌ Commit secrets or credentials
- ❌ Skip tests with `@Disabled` without explicit justification

---

### Step 7: Push to Remote

**MANDATORY — before every push, ensure the branch is conflict-free with `main`.** A PR left in `CONFLICTING`/`DIRTY` mergeable state blocks CI and merge, and stale conflicts compound the longer they sit.

```bash
git fetch origin
git merge origin/main --no-edit   # or: git rebase origin/main
# Resolve any conflicts, then re-run the affected test suites
# (unit + integration + pg-integration, at minimum) before pushing.
git push -u origin <branch-name>
```

If a PR already exists and shows a merge conflict (check with
`gh pr view <number> --json mergeable,mergeStateStatus`), resolve it
immediately — merge `main` into the feature branch, fix conflicts, re-run
tests, commit the merge, and push. Do not open a new PR or move to the next
finding while an existing PR is left in a conflicting state.

---

### Step 8: Update Documentation

**MANDATORY** after every change:

1. Review and update all affected documentation (business and engineering).
2. Ensure documents are consistent, readable, and up to date.
3. **Avoid duplication** — centralize information in the most coherent location; remove duplicates.
4. If documentation is missing → add it.
5. If documentation is outdated or no longer applicable → move it to `docs/000-archive/`.
6. If a new Use Case was created in Step 0 → verify all related documents reference it correctly.

```bash
# Docs live under docs/
ls docs/
# Archive old docs here:
# docs/000-archive/
```

---

### Step 8.5: Run the Full Pipeline (mandatory before opening a PR)

`bash scripts/run_pipeline.sh` is the single, dashboarded gate that must be
green before Step 9. It composes `scripts/validate-sdlc-plan.sh` +
`scripts/preflight.sh --full` (which covers everything Step 5 lists, plus
Docker build/smoke, Bruno API tests and Playwright E2E against the live
stack), adds a markdown-lint pass over the `*.md` files this branch changed,
and writes an HTML dashboard at `reports/pipeline/<timestamp>/index.html`
(git-ignored) linking every sub-report (JaCoCo, Playwright, ESLint,
Checkstyle) plus a plain-text log.

```bash
bash scripts/run_pipeline.sh
```

Do not proceed to Step 9 until it exits 0.

---

### Step 9: Create Pull Request and Close Issue

**PR Title Format**: `[#<issue-number>] <type>: <description>`

**PR Description Template**:

```markdown
## Summary
<1-3 bullet points describing changes>

## Changes
### Added
### Modified
### Fixed

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] E2E Playwright tests pass
- [ ] Coverage ≥ 80%

## Documentation
- [ ] Docs updated / archived
- [ ] OpenAPI updated (if endpoint changed)
- [ ] Use Case referenced

## Checklist
- [ ] Code follows project conventions
- [ ] No hardcoded credentials
- [ ] No dead code
- [ ] No TODO comments left
- [ ] KIS and SRP applied

## Issue Reference
Fixes #<issue-number>
```

**Commands**:

```bash
gh pr create \
  --title "[#253] feat: add document creation" \
  --body "$(cat <<'EOF'
## Summary
- Added POST /api/v1/documentos endpoint
- Added E2E test for the creation form

Fixes #253
EOF
)"
```

**MANDATORY — verify mergeability immediately after creating the PR.** Do not consider the task done, and do not move on to other work, until this check passes:

```bash
gh pr view <number> --json mergeable,mergeStateStatus
```

If `mergeable` is not `MERGEABLE` or `mergeStateStatus` is `CONFLICTING`/`DIRTY`, fix it right away: merge `main` into the branch, resolve conflicts, re-run the affected test suites, commit, and push — then re-check. Every unit of work must end with a PR that is conflict-free and a clean branch (no uncommitted or unpushed changes).

---

## Quick Reference

```bash
# 0. Check/create issue (verify Use Case first)
gh issue list --search "task"
gh issue create --title "..." --body "..."

# 1. Create branch (always from updated main)
git checkout main && git pull origin main
git checkout -b feat/253_task_description

# 1.5 Move issue to IN PROGRESS
gh issue edit 253 --add-label "in-progress"

# 2. Write failing tests first (TDD)
mvn test -pl backend-api -Dtest=YourNewTest  # must FAIL

# 3. Implement

# 4. Refactor (remove dead code, simplify, KIS/SRP)
mvn checkstyle:check -pl backend-api

# 5. Run all tests
mvn verify -pl backend-api
cd frontend && npx playwright test

# 6. Commit
git commit -m "feat(scope): description

Closes #253"

# 7. Push
git push -u origin feat/253_task_description

# 8. Update docs

# 8.5 Run the full pipeline (mandatory before opening a PR)
bash scripts/run_pipeline.sh   # must exit 0

# 9. Create PR
gh pr create --title "[253] feat: description" --body "Fixes #253"
```

---

## Integration with AI Agents

### Claude Code

Add to `CLAUDE.md`:

```text
@.claude/rules/ai-agent-workflow.md
```

### OpenCode (RTK)

Reference in commands:

```toml
[workflow]
require_issue = true
require_tests = true
auto_close_issue = true
```

### GitHub Copilot

The workflow documentation in `/docs` serves as context for Copilot suggestions.

---

## Industry Best Practices References

- [Conventional Commits](https://www.conventionalcommits.org/) - Commit message format
- [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) - Branching strategy
- [Test-Driven Development](https://martinfowler.com/bliki/TestDrivenDevelopment.html) - TDD methodology
- [Clean Code](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882) - Robert C. Martin
- [Conventional Branches](https://conventionalbranch.org/) - Branch naming

---

## Violations

**AI Agents MUST NOT**:

1. ❌ Make changes without an associated issue
2. ❌ Make changes without a linked Use Case (Caso de Uso)
3. ❌ Commit directly to `main` or `master`
4. ❌ Skip TDD — implement before writing tests
5. ❌ Skip writing or updating tests
6. ❌ Commit code with failing tests
7. ❌ Leave hardcoded credentials or secrets
8. ❌ Push directly without creating PR
9. ❌ Ignore code quality tools (Checkstyle, SpotBugs)
10. ❌ Skip E2E Playwright tests for UI changes
11. ❌ Leave dead code or duplicate code
12. ❌ Leave documentation out of date
13. ❌ Skip the documentation review step

---

## Exceptions

Only in **extreme circumstances** with **explicit human approval**:

1. Emergency hotfixes (security critical)
2. Documentation-only changes (minor spelling/formatting)
3. One-time migration scripts

Document any exception in the commit message.
