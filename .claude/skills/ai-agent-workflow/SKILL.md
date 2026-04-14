# AI Agent Development Skill

## Overview

This skill provides comprehensive guidance for AI coding agents (OpenCode, Claude Code, GitHub Copilot) to implement changes in the Notaire project following industry best practices.

## When to Use This Skill

Invoke this skill when:
- Implementing new features
- Fixing bugs
- Refactoring code
- Writing tests
- Creating documentation
- ANY code change requested by user

## Core Workflow

### The Golden Rule

> **Every user request → One Issue → One Branch → One PR**

### Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    AI AGENT DEVELOPMENT WORKFLOW                         │
└─────────────────────────────────────────────────────────────────────────┘

    START ──► [1. Check/Create Issue] ──► [2. Create Branch]
                                                    │
                                                    ▼
                                       [3. Implement Changes]
                                                    │
                                                    ▼
                                       [4. Write & Update Tests]
                                         (MANDATORY - ALL CHANGES)
                                                    │
                                                    ▼
                                       [5. Run Tests & Verify]
                                                    │
                                    ┌────────────────┴────────────────┐
                                    │                                     │
                               PASS                              FAIL
                                    │                                     ▼
                                    ▼                              [FIX & RETEST]
                            [6. Commit Changes]
                                    │
                                    ▼
                            [7. Push to Remote]
                                    │
                                    ▼
                            [8. Create PR + Close Issue]
                                    │
                                    ▼
                                  END
```

## Step 1: Issue Management

### Check Existing Issue

```bash
# Search for existing issue
gh issue list --state open --search "user authentication"

# View specific issue
gh issue view 253
```

### Create New Issue (If Needed)

```bash
gh issue create \
  --title "feat: Add user authentication with JWT" \
  --body "$(cat <<'EOF'
## Description
Implement JWT-based authentication for the Notaire API.

## User Story
As an API client, I want to authenticate with username/password
and receive a JWT token so that I can access protected endpoints.

## Acceptance Criteria
- [ ] POST /api/v1/auth/login returns JWT on valid credentials
- [ ] POST /api/v1/auth/refresh refreshes expired tokens
- [ ] Protected endpoints return 401 without valid token
- [ ] Tokens expire after configured time

## Technical Notes
- Use Spring Security with JWT
- Store refresh tokens securely
- Implement rate limiting for login attempts
EOF
)" \
  --label "feature" \
  --assignee "@me"
```

**Output**: Store issue number (e.g., `253`)

## Step 2: Create Branch

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
| `add` | Adding new resources |

### Examples

```bash
# ✅ Correct
feat/253_jwt_authentication
fix/254_login_timeout
refactor/255_move_to_service
test/256_user_service_tests

# ❌ Wrong
my-feature
feature-user
bugfix-123
```

### Command

```bash
git checkout -b feat/253_jwt_authentication
```

## Step 3: Implement Changes

### Best Practices

1. **Understand First**: Read existing code before modifying
2. **Small Changes**: Make incremental changes
3. **Follow Conventions**: Use project standards
4. **Clean Code**: Write readable, maintainable code

### Reference Files

| Topic | Reference |
|-------|-----------|
| Java Style | `.claude/rules/programming.md` |
| Code Quality | `.claude/rules/code-quality.md` |
| Naming | `.claude/rules/programming.md#naming-conventions` |
| Error Handling | `.claude/rules/programming.md#error-handling` |

## Step 4: Write Tests (MANDATORY)

### Test Requirements by Change Type

| Your Change | Required Tests |
|-------------|----------------|
| New feature | Unit tests + Integration tests |
| Bug fix | Tests that reproduce & verify fix |
| Refactor | Same tests as before (verify behavior) |
| API endpoint | Controller tests + Integration |
| Database | Repository tests |
| Service layer | Service unit tests |

### Test Structure

```
src/test/java/com/licensis/notaire/
├── unit/
│   └── service/
│       └── UserServiceTest.java
├── integration/
│   └── api/
│       └── UserControllerIntegrationTest.java
└── TestBase.java
```

### Test Naming

```java
@DisplayName("Should return user when valid ID is provided")
void shouldReturnUserWhenValidIdProvided() { }

@DisplayName("Should throw EntityNotFoundException when user not found")
void shouldThrowExceptionWhenUserNotFound() { }
```

### Reference

- `.claude/skills/testing/SKILL.md`
- `.claude/rules/programming.md#testing`

## Step 5: Run Tests

### Commands

```bash
# All backend tests
mvn test -pl backend-api

# With coverage
mvn test -pl backend-api && mvn jacoco:check -pl backend-api

# Single test class
mvn test -pl backend-api -Dtest=UserServiceTest

# Specific test method
mvn test -pl backend-api -Dtest=UserServiceTest#shouldReturnUserWhenValidId

# Code quality
mvn verify -pl backend-api  # All checks
```

### Quality Gates

| Check | Threshold |
|-------|-----------|
| Tests | All must pass |
| Coverage | ≥ 80% (JaCoCo) |
| Checkstyle | No violations |
| SpotBugs | No warnings |

### Iterate Until Passing

```
FAIL → Fix code → Run tests → FAIL → Fix → Run → ... → PASS
```

## Step 6: Commit Changes

### Conventional Commits Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Examples

```bash
# Feature
git commit -m "feat(auth): add JWT authentication

Implemented login endpoint that returns JWT tokens.
Added token refresh mechanism.
Configured Spring Security for JWT validation.

Closes #253"

# Bug fix
git commit -m "fix(auth): resolve session timeout issue

The session was expiring prematurely after 5 minutes
instead of the configured 30 minutes. Fixed the token
expiration calculation.

Closes #254"

# Tests
git commit -m "test(service): add UserService unit tests

Added comprehensive unit tests:
- shouldCreateUserWithValidData
- shouldThrowExceptionWhenEmailInvalid
- shouldHashPasswordBeforeSaving

Closes #256"

# Refactor
git commit -m "refactor(service): extract validation logic

Moved email validation from controller to service layer
for better reuse and testability.

Closes #255"
```

### DO NOT

- ❌ Commit without tests
- ❌ Commit broken code
- ❌ Leave TODO comments
- ❌ Commit secrets or credentials
- ❌ Use past tense in commit message

## Step 7: Push to Remote

```bash
# First push (sets upstream)
git push -u origin feat/253_jwt_authentication

# Subsequent pushes
git push
```

## Step 8: Create Pull Request

### PR Title Format

```
[#<issue>] <type>: <description>
```

Example: `[253] feat: add user authentication with JWT`

### PR Description Template

```markdown
## Summary
<!-- 1-3 bullet points of what changed -->

## Changes

### Added
<!-- New features -->

### Modified
<!-- Changed behavior -->

### Fixed
<!-- Bug fixes -->

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Coverage: __%

## Checklist
- [ ] Code follows conventions
- [ ] No hardcoded secrets
- [ ] Documentation updated
- [ ] No TODO comments

## Issue Reference
Fixes #<issue-number>
```

### Commands

```bash
# Create PR
gh pr create \
  --title "[253] feat: add user authentication with JWT" \
  --body "Fixes #253"

# Or with full description
gh pr create --title "[253] feat: add user authentication" \
  --body "$(cat <<'EOF'
## Summary
- Added JWT-based authentication
- Implemented login and token refresh endpoints
- Configured Spring Security for JWT validation

## Testing
- 15 unit tests added
- 5 integration tests added
- Coverage: 87%

Fixes #253
EOF
)"
```

### Close Issue

Add to PR body:
```
Closes #253
```

Or run:
```bash
gh issue close 253
```

---

## Quick Reference

### Complete Workflow (Copy-Paste)

```bash
# 1. Check issue (or create)
gh issue list --search "task"

# 2. Create branch
git checkout -b feat/XXX_description

# 3. Implement (your changes here)

# 4. Write tests

# 5. Run tests
mvn test -pl backend-api && mvn jacoco:check -pl backend-api

# 6. Commit
git add .
git commit -m "feat(scope): description

Closes #XXX"

# 7. Push
git push -u origin feat/XXX_description

# 8. Create PR
gh pr create --title "[XXX] feat: description" --body "Fixes #XXX"
```

---

## Branch Types Quick Reference

| Type | Prefix | Example |
|------|--------|---------|
| Feature | `feat/` | `feat/253_user_auth` |
| Bug Fix | `fix/` | `fix/254_timeout_bug` |
| Refactor | `refactor/` | `refactor/255_cleanup` |
| Test | `test/` | `test/256_new_tests` |
| Docs | `docs/` | `docs/257_readme` |
| Chore | `chore/` | `chore/258_deps` |
| CI | `ci/` | `ci/259_workflow` |

---

## Industry Best Practices

### References

1. **Conventional Commits**: https://www.conventionalcommits.org/
2. **GitHub Flow**: https://docs.github.com/en/get-started/quickstart/github-flow
3. **TDD Best Practices**: `.claude/skills/testing/references/tdd-iron-laws.md`
4. **Clean Code**: `.claude/rules/programming.md`

### Key Principles

1. **Small PRs**: Merge often, small changes
2. **Test First**: Write tests before code (TDD)
3. **CI/CD**: All tests must pass before merge
4. **Code Review**: Every change needs review
5. **Document**: Update docs with changes

---

## Integration with AI Agents

### OpenCode (RTK)

Reference in commands:
```
@.claude/rules/ai-agent-workflow.md
@.claude/skills/ai-agent-workflow/SKILL.md
```

### Claude Code

Add to CLAUDE.md:
```
Always follow the workflow in .claude/rules/ai-agent-workflow.md
```

### GitHub Copilot

The project documentation provides context for Copilot suggestions. Ensure workflow awareness when using Copilot.

---

## Troubleshooting

### Issue Already Exists

```bash
# Find the issue number
gh issue list --search "authentication"

# Use existing number for branch
git checkout -b feat/253_existing_issue
```

### Tests Failing

1. Read the failure message carefully
2. Fix the test or the implementation
3. Re-run: `mvn test -pl backend-api`
4. Iterate until all pass

### PR Conflicts

```bash
# Rebase on main
git fetch origin main
git rebase origin/main

# Resolve conflicts
# Stage resolved files
git add .
git rebase --continue

# Force push
git push --force-with-lease
```

### Wrong Branch

```bash
# Stash changes
git stash

# Switch to correct branch
git checkout correct-branch

# Apply changes
git stash pop
```
