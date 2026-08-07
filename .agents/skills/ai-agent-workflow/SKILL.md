# AI Agent Development Skill

## Overview

This skill provides comprehensive guidance for AI coding agents (OpenCode, Codex, GitHub Copilot) to implement changes in the Notaire project following the mandatory development process defined in `AUDITORIA.md`.

## When to Use This Skill

Invoke this skill when:
- Implementing new features
- Fixing bugs
- Refactoring code
- Writing tests
- Creating documentation
- ANY code change requested by the user

## Core Workflow

### The Golden Rule

> **Every task → One Use Case → One Issue → One Branch → One PR**

### Workflow Summary

```
START
  │
  ▼
[0. CHECK ISSUE + USE CASE] — create both if missing
  │
  ▼
[1. CREATE BRANCH] — from updated main, conventional naming
  │
  ▼
[1.5 MOVE ISSUE TO IN PROGRESS]
  │
  ▼
[2. TDD: WRITE FAILING TESTS FIRST] — MANDATORY
  │
  ▼
[3. IMPLEMENT] — make tests pass
  │
  ▼
[4. REFACTOR] — KIS, SRP, remove dead/duplicate code
  │
  ▼
[5. RUN ALL TESTS] — unit + integration + E2E Playwright
  │
  ├── FAIL → FIX & RETEST
  │
  ▼
[6. COMMIT] — Conventional Commits + Closes #issue
  │
  ▼
[7. PUSH TO REMOTE]
  │
  ▼
[8. UPDATE DOCUMENTATION] — business + engineering docs
  │
  ▼
[9. CREATE PR + CLOSE ISSUE]
  │
  ▼
END
```

---

## Step 0: Issue + Use Case Verification

### Check Existing Issue

```bash
gh issue list --state open --search "user authentication"
gh issue view 253
```

### Create New Issue (with Use Case reference)

Every issue MUST reference a Use Case (Caso de Uso). If no Use Case exists, create the documentation first.

```bash
gh issue create \
  --title "feat: Add user authentication with JWT" \
  --body "$(cat <<'EOF'
## Description
Implement JWT-based authentication for the Notaire API.

## Use Case (Caso de Uso)
UC-12: Autenticación de usuario

## Acceptance Criteria
- [ ] POST /api/v1/auth/login returns JWT on valid credentials
- [ ] Protected endpoints return 401 without valid token
- [ ] Tokens expire after configured time

## Technical Notes
- Use Spring Security with JWT
EOF
)" \
  --label "feature" \
  --assignee "@me"
```

**Output**: Issue number (e.g., `253`) + Use Case reference.

---

## Step 1: Create Branch

Always pull from main first:

```bash
git checkout main && git pull origin main
git checkout -b feat/253_jwt_authentication
```

### Branch Naming Convention

```
<type>/<issue-number>_<short-description>
```

| Type | When to Use |
|------|-------------|
| `feat` | New feature or functionality |
| `fix` | Bug fix |
| `refactor` | Code refactoring (no behavior change) |
| `test` | Adding or updating tests |
| `docs` | Documentation changes |
| `chore` | Maintenance, dependencies |
| `ci` | CI/CD pipeline changes |
| `design` | Design/UI updates |

---

## Step 1.5: Move Issue to IN PROGRESS

```bash
gh issue edit 253 --add-label "in-progress"
```

---

## Step 2: TDD — Write Failing Tests First

**MANDATORY order**: write tests → watch them fail → implement → watch them pass → refactor.

```bash
# Write your test class first, then verify it fails
mvn test -pl backend-api -Dtest=YourNewTestClass
# Expected: FAILURE — this is correct at this stage
```

### Test Requirements

| Change Type | Required Tests |
|-------------|----------------|
| New feature | Unit tests + Integration tests |
| Bug fix | Tests reproducing the bug first |
| Refactor | Same tests as before (verify behavior) |
| New API endpoint | Controller tests + Integration + OpenAPI |
| Database change | Repository tests + Flyway migration |
| UI screen/form | E2E Playwright tests |

### Test Structure

```
src/test/java/com/licensis/notaire/
├── unit/
│   └── service/UserServiceTest.java
├── integration/
│   └── api/UserControllerIntegrationTest.java
└── TestBase.java

frontend/tests/
└── e2e/
    └── usuarios.spec.ts
```

### Test Naming

```java
@DisplayName("Should return user when valid ID is provided")
void shouldReturnUserWhenValidIdProvided() { }

@DisplayName("Should throw EntityNotFoundException when user not found")
void shouldThrowExceptionWhenUserNotFound() { }
```

---

## Step 3: Implement Changes

Implement only what is needed to make the failing tests pass.

### New REST Endpoint Checklist

- [ ] Implement the endpoint in the controller.
- [ ] Add service + repository methods as needed.
- [ ] Write controller tests + integration tests.
- [ ] Document in OpenAPI (`@Operation`, `@ApiResponse`).
- [ ] Verify endpoint appears in Swagger UI.
- [ ] Ensure the endpoint is called from the UI at least once (**UI traceability**).
- [ ] Add entry to `docs/` mapping the endpoint to the UI screen that calls it.

### Database Change Checklist

- [ ] Create new Flyway migration `V{n}__description.sql` in `db/migration/`.
- [ ] Flyway is the single source of truth (init-db archived at `docs/archive/init-db/`).
- [ ] Run `mvn test -Ppg-integration` to validate alignment.
- [ ] See `.Codex/rules/database-migrations.md`.

### UI Change Checklist

- [ ] Follow `.Codex/rules/ui-ux-design.md` and design system.
- [ ] Write E2E Playwright tests for every screen and form.
- [ ] Test golden path and edge cases.
- [ ] Test on mobile (320px), tablet (768px), desktop (1024px).

---

## Step 4: Refactor and Code Quality

Before running the full test suite:

1. Remove all dead code.
2. Remove duplicate code — refactor to a single reusable unit.
3. Reduce cyclomatic and cognitive complexity.
4. Remove unnecessary comments — code must be self-explanatory.
5. Apply **KIS** (Keep It Simple): simplest solution that satisfies the tests.
6. Apply **SRP** (Single Responsibility Principle): in both code and tests.

```bash
mvn checkstyle:check -pl backend-api
mvn spotbugs:check -pl backend-api -DskipSpotBugs=false
```

---

## Step 5: Run All Tests

```bash
# Backend
mvn test -pl backend-api
mvn jacoco:check -pl backend-api
mvn verify -pl backend-api

# E2E (frontend must be running)
cd frontend && npx playwright test
```

### Quality Gates

| Check | Threshold |
|-------|-----------|
| Unit + Integration tests | All must pass |
| Coverage | ≥ 80% (JaCoCo) |
| Checkstyle | No violations |
| SpotBugs | No warnings |
| E2E Playwright | All must pass |

---

## Step 6: Commit

```bash
git commit -m "feat(auth): add JWT authentication

Implemented login endpoint returning JWT tokens.
E2E test added for the login form flow.
OpenAPI documentation updated.

Closes #253"
```

**DO NOT**:
- ❌ Commit without tests
- ❌ Commit broken code
- ❌ Leave commented-out code
- ❌ Commit secrets or credentials

---

## Step 7: Push to Remote

```bash
git push -u origin feat/253_jwt_authentication
```

---

## Step 8: Update Documentation

**MANDATORY** after every change:

1. Review and update all affected docs (business + engineering).
2. Ensure consistency and accuracy.
3. Centralize duplicated info; remove redundant documents.
4. Move outdated docs to `docs/archive/`.
5. If a new Use Case was created in Step 0 → verify all related docs reference it.

---

## Step 9: Create Pull Request

### PR Title
```
[#253] feat: add user authentication with JWT
```

### PR Template

```markdown
## Summary
- JWT-based authentication implemented
- Login and token refresh endpoints added

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] E2E Playwright tests pass
- [ ] Coverage ≥ 80%

## Documentation
- [ ] Docs updated
- [ ] OpenAPI updated
- [ ] Use Case referenced

## Checklist
- [ ] No dead code
- [ ] No hardcoded secrets
- [ ] KIS and SRP applied

Fixes #253
```

```bash
gh pr create \
  --title "[253] feat: add user authentication with JWT" \
  --body "Fixes #253"
```

---

## Quick Reference

```bash
# 0. Check/create issue (verify Use Case first)
gh issue list --search "task"

# 1. Create branch from updated main
git checkout main && git pull origin main
git checkout -b feat/253_description

# 1.5 Move to IN PROGRESS
gh issue edit 253 --add-label "in-progress"

# 2. Write failing tests first
mvn test -pl backend-api -Dtest=YourTest  # must FAIL

# 3. Implement

# 4. Refactor (KIS, SRP, remove dead code)
mvn checkstyle:check -pl backend-api

# 5. Run all tests
mvn verify -pl backend-api
cd frontend && npx playwright test

# 6. Commit
git commit -m "feat(scope): description

Closes #253"

# 7. Push
git push -u origin feat/253_description

# 8. Update docs

# 9. Create PR
gh pr create --title "[253] feat: description" --body "Fixes #253"
```

---

## Industry Best Practices

1. [Conventional Commits](https://www.conventionalcommits.org/)
2. [Conventional Branches](https://conventionalbranch.org/)
3. [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow)
4. [Test-Driven Development](https://martinfowler.com/bliki/TestDrivenDevelopment.html)
5. Clean Code — Robert C. Martin
