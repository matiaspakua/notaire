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

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AI AGENT DEVELOPMENT WORKFLOW                             │
└─────────────────────────────────────────────────────────────────────────────┘

   ┌─────────────┐
   │   START     │
   │  User Task  │
   └──────┬──────┘
          │
          ▼
   ┌─────────────────────┐
   │ 1. CHECK ISSUE      │◄─────────────────────────────────────┐
   │    Exists?          │                                      │
   └──────┬──────────────┘                                      │
          │                                                      │
    ┌─────┴─────┐                                               │
    │ YES       │ NO                                             │
    ▼           ▼                                                │
┌────────┐  ┌─────────────────┐                                  │
│ Get    │  │ CREATE ISSUE    │                                  │
│ Issue# │  │ on GitHub       │─────────────────────────────────┘
└────┬───┘  └────────┬────────┘
     │               │
     └───────┬───────┘
             ▼
   ┌─────────────────────┐
   │ 2. CREATE BRANCH     │
   │ <type>/<#>-<desc>   │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 3. IMPLEMENT        │
   │    Changes          │
   └──────────┬──────────┘
              │
              ▼
   ┌─────────────────────┐
   │ 4. WRITE TESTS      │◄── Mandatory for all changes
   │    Unit + Integration│
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 5. RUN TESTS        │
   │    mvn test         │
   └──────────┬──────────┘
              │
         ┌────┴────┐
         │ PASS?    │
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
   │ 7. PUSH TO REMOTE  │
   └──────────┬──────────┘
              ▼
   ┌─────────────────────┐
   │ 8. CREATE PR        │
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

### Step 0: Pre-Condition - Issue Verification

**MANDATORY** Before ANY code change:

```bash
# Check if issue exists
gh issue list --state open --search "<task description>"

# If NOT exists, CREATE IT
gh issue create \
  --title "<descriptive title>" \
  --body "## Description
<detailed description>

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Technical Notes
<any relevant technical details>" \
  --label "feature" \
  --assignee "@me"
```

**Output Required**: Store the issue number (e.g., `#253`)

---

### Step 1: Create Feature Branch

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

**Examples**:
```bash
# Correct branch names
feat/253_user_authentication
fix/254_login_timeout
refactor/255_move_service_layer
test/256_add_user_tests
docs/257_update_api_docs

# WRONG - Don't do this
my-branch
feature-user
fix-bug-123
```

**Command**:
```bash
git checkout -b <type>/<issue-number>_<description>
```

---

### Step 2: Implement Changes

1. Read existing code to understand context
2. Follow project conventions (see `.claude/rules/`)
3. Apply clean code principles
4. Keep changes focused and minimal

**Reference**: See `.claude/rules/programming.md`, `.claude/rules/code-quality.md`

---

### Step 3: Write Tests (MANDATORY)

**This step is MANDATORY for ALL changes.**

#### Test Requirements

| Change Type | Minimum Tests |
|-------------|---------------|
| New feature | Unit tests + Integration tests |
| Bug fix | Unit tests that reproduce the bug |
| Refactor | Same tests as before (no behavior change) |
| API change | Controller tests + Integration tests |
| Database change | Repository tests |

#### Test Location
- **Unit tests**: `src/test/java/.../unit/`
- **Integration tests**: `src/test/java/.../integration/`
- **E2E tests**: `integration-test/` (Robot Framework)

#### Test Naming Convention
```
should[ExpectedBehavior]When[Condition]
```

**Example**:
```java
@Test
@DisplayName("Should return user when valid ID provided")
void shouldReturnUserWhenValidIdProvided() {
    // test code
}
```

**Reference**: See `.claude/rules/programming.md#testing`

---

### Step 4: Run Tests

**Always run tests BEFORE committing.**

```bash
# Backend tests (this project)
mvn test -pl backend-api

# With coverage check
mvn test -pl backend-api && mvn jacoco:check -pl backend-api

# Single test class
mvn test -pl backend-api -Dtest=UserServiceTest

# All tests including frontend
mvn test
```

**Quality Gates** (enforced by CI):
- Minimum 80% code coverage (JaCoCo)
- All tests must pass
- No checkstyle violations
- No SpotBugs warnings

---

### Step 5: Commit Changes

**Commit Message Format**: [Conventional Commits](https://www.conventionalcommits.org/)

```
<type>(<scope>): <description>

[optional body]

[optional footer with issue reference]
```

**Format Rules**:
- Use imperative mood: "add" not "added" or "adds"
- First line ≤ 72 characters
- Reference issue in footer: `Closes #<issue-number>`

**Examples**:
```bash
# Feature
git commit -m "feat(api): add user authentication endpoint

Implemented JWT-based authentication with login and token refresh.
Added rate limiting for login attempts.

Closes #253"

# Bug fix
git commit -m "fix(auth): resolve session timeout issue

The session was expiring prematurely due to incorrect refresh logic.
Added proper token rotation.

Closes #254"

# Test
git commit -m "test(service): add unit tests for UserService

Added tests for:
- shouldCreateUserWithValidData
- shouldThrowExceptionWhenEmailInvalid
- shouldHashPasswordBeforeSaving

Closes #256"
```

**DO NOT**:
- ❌ Commit without tests
- ❌ Commit broken code
- ❌ Leave commented code
- ❌ Commit secrets or credentials

---

### Step 6: Push to Remote

```bash
# Push branch
git push -u origin <branch-name>

# Example
git push -u origin feat/253_user_authentication
```

---

### Step 7: Create Pull Request

**PR Title Format**: `[#<issue-number>] <type>: <description>`

**Example**:
```
[253] feat: add user authentication with JWT
```

**PR Description Template**:
```markdown
## Summary
<1-3 bullet points describing changes>

## Changes
### Added
- New feature description

### Modified
- Changed behavior description

### Fixed
- Bug fix description

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Coverage ≥ 80%

## Checklist
- [ ] Code follows project conventions
- [ ] No hardcoded credentials
- [ ] Documentation updated
- [ ] No TODO comments left

## Issue Reference
Fixes #<issue-number>
```

**Commands**:
```bash
# Create PR using GitHub CLI
gh pr create \
  --title "[#253] feat: add user authentication" \
  --body "$(cat <<'EOF'
## Summary
- Implemented JWT-based authentication
- Added login and token refresh endpoints

## Testing
- Unit tests: 15 tests added
- Integration tests: 5 tests added
- Coverage: 85%

Fixes #253
EOF
)"
```

---

### Step 8: Close Associated Issue

After PR is created, close the issue:

```bash
# Close the issue (automatically done with "Fixes #number" in PR body)
gh issue close <issue-number>
```

Or add to PR body: `Closes #<issue-number>`

---

## Quick Reference Commands

```bash
# 1. Check/create issue
gh issue list --search "task"
gh issue create --title "..." --body "..."

# 2. Create branch
git checkout -b feat/253_task_description

# 3. Implement & test
# ... make changes ...
mvn test -pl backend-api

# 4. Commit
git add .
git commit -m "feat(scope): description

Closes #253"

# 5. Push & PR
git push -u origin feat/253_task_description
gh pr create --title "[253] feat: description" --body "Fixes #253"
```

---

## Integration with AI Agents

### OpenCode (RTK)

Add to `.rtk/config.toml` or workflow:

```toml
[workflow]
require_issue = true
require_tests = true
auto_close_issue = true
```

### Claude Code

Add to `CLAUDE.md`:
```
@.claude/rules/ai-agent-workflow.md
```

### GitHub Copilot

The workflow documentation in `/docs` serves as context for Copilot suggestions.

---

## Industry Best Practices References

- [Conventional Commits](https://www.conventionalcommits.org/) - Commit message format
- [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) - Branching strategy
- [Test-Driven Development](https://martinfowler.com/bliki/TestDrivenDevelopment.html) - TDD methodology
- [Clean Code](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882) - Robert C. Martin
- [Trunk-Based Development](https://trunkbaseddevelopment.com/) - Branching model
- [Semantic Versioning](https://semver.org/) - Version numbering

---

## Violations

**AI Agents MUST NOT**:

1. ❌ Make changes without an associated issue
2. ❌ Commit directly to `main` or `master`
3. ❌ Skip writing or updating tests
4. ❌ Commit code with failing tests
5. ❌ Leave hardcoded credentials or secrets
6. ❌ Push directly without creating PR
7. ❌ Ignore code quality tools (checkstyle, SpotBugs)
8. ❌ Skip coverage requirements (80% minimum)

**Consequences of Violations**:
- CI/CD pipeline will fail
- PR will be rejected
- Human review will flag violations

---

## Exceptions

Only in **extreme circumstances** and with **explicit human approval** may these rules be bypassed:

1. Emergency hotfixes (security critical)
2. Documentation-only changes (minor)
3. One-time scripts or migrations

Document any exception in the commit message.
