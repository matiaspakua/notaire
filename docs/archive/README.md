# Archived Documentation

This directory contains **deprecated or historical documentation** that is no longer actively maintained but preserved for reference and historical context.

## When to Use This Directory

- ✅ **DO** use for understanding historical decisions (why we moved away from X)
- ✅ **DO** read related ADRs to understand the rationale for deprecation
- ❌ **DON'T** use for current implementation (use active docs instead)
- ❌ **DON'T** maintain archived files (they're frozen in time)

---

## Contents

### testing-reports-2026-06/ — Point-in-time Testing/Audit Reports
**Status**: 🔴 **SUPERSEDED** (as of 2026-06-17)

Dated, point-in-time session/phase reports from the June 2026 testing
initiative (Phases 4-7, API/UI alignment audits, session summaries). All
findings in these reports were either resolved (tracked via closed GitHub
issues #426-#436, #450-#455, #510, #512) or superseded by the live state of
the codebase. Kept for historical context only — do not treat any
percentages, endpoint counts, or "remaining gaps" in these files as current;
re-measure with `mvn jacoco:report -pl backend-api` and `npx playwright test`
instead.

**What replaced it?**: `.claude/rules/code-quality.md` (live coverage
policy), `CLAUDE.md` Testing Commands section, and `docs/testing/` for
current test/UC traceability.

### e2e-swing/ — Legacy Swing GUI E2E Testing
**Status**: 🔴 **DEPRECATED** (as of 2026-06-11)

The original Swing frontend client has been superseded by the modern Next.js frontend.

**What was it?**: Java Swing GUI application for notaries to access the system.

**Why deprecated?**: 
- Outdated UI/UX patterns
- Difficult to maintain
- No longer aligned with modern web standards
- See [ADR-005: Modern Frontend Migration](../02-architecture/01-adr/ADR-005-modern-frontend-migration.md)

**What replaced it?**: Next.js + React frontend ([03-development/01-setup/README.md](../03-development/01-setup/README.md))

**E2E Testing**: 
- Old: Swing-specific tests (in this archive)
- New: Playwright-based tests ([03-development/03-testing/E2E-TEST-PLAN.md](../03-development/03-testing/E2E-TEST-PLAN.md))

**What to do if you need Swing info**:
1. Check [ADR-005](../02-architecture/01-adr/ADR-005-modern-frontend-migration.md) for migration rationale
2. Reference old code in git history: `git log --grep="swing" --oneline`
3. Contact the team for context (this is legacy code, not actively used)

---

### outdated-plans/ — Old Plans No Longer Active
**Status**: 🟡 **ARCHIVED** (reference only)

Contains old planning documents and proposals that are no longer active.

**Contents**:
- `FRONTEND-GAPS-AND-PLAN.md` — Old frontend improvement plan (superseded by Next.js migration)

**What to do**: Read only for historical context. Don't follow these plans — they're outdated.

---

### ERROR-HANDLING-STRATEGY-code-standards.md — Superseded API Error Doc
**Status**: 🔴 **SUPERSEDED** (as of 2026-08-02)

Formerly `docs/03-development/04-code-standards/ERROR-HANDLING-STRATEGY.md`.
Described only the legacy ad-hoc `try/catch` error pattern and a narrower
`{"error": "..."}` response shape, which conflicted with the parallel
`docs/05-api/ERROR-HANDLING-STRATEGY.md` (issue #600).

**What replaced it?**: [`docs/05-api/ERROR-HANDLING-STRATEGY.md`](../05-api/ERROR-HANDLING-STRATEGY.md) — merges both documents against the actual `GlobalExceptionHandler`/`ErrorResponse` implementation.

---

### Other Archived Content

Additional deprecated documents may be found here. When encountering archived documentation:

1. **Check the filename or header** for deprecation notice
2. **Read the related ADR** (if one exists) to understand why it was deprecated
3. **Look for the active version** in [../NAVIGATION.md](../NAVIGATION.md)
4. **Ask the team** if you're unsure whether something is truly deprecated

---

## How Deprecation Works

When something is deprecated, we:

1. **Keep the original** in `docs/archive/` for historical reference
2. **Mark it clearly** with "DEPRECATED" notice
3. **Link to the replacement** (active documentation or ADR)
4. **Never delete** (preserves history)
5. **Don't maintain** (no updates to archived files)

---

## Finding What You Need

| If you're looking for... | Go to... |
|---|---|
| Current Swing GUI documentation | ❌ Doesn't exist (system is deprecated) |
| Why we moved from Swing to Next.js | [ADR-005](../02-architecture/01-adr/ADR-005-modern-frontend-migration.md) |
| Modern frontend documentation | [03-development/](../03-development/) |
| Modern E2E testing | [03-development/03-testing/E2E-TEST-PLAN.md](../03-development/03-testing/E2E-TEST-PLAN.md) |
| Historical context about Swing | This directory (archive/e2e-swing/) |

---

## Cleaning Up the Archive

To keep the archive manageable:

- Files are **never deleted** (history preservation)
- Files are **organized by deprecation reason** (legacy, outdated, superseded)
- **README.md files explain each section** (like this one)
- **ADR links provide rationale** for why things were deprecated

---

## Contact & Questions

If you find yourself using archived documentation frequently:
- **You might be looking for the wrong docs** — check [../NAVIGATION.md](../NAVIGATION.md)
- **The active version might be hard to find** — report as a documentation bug
- **You need historical context** — check the related ADR first

---

**Note**: The code for deprecated features may still exist in git history. Use `git log` and `git show` to reference old implementations.

**Last updated**: June 11, 2026
**Deprecation reason**: Consolidation of documentation structure and removal of Swing GUI references
