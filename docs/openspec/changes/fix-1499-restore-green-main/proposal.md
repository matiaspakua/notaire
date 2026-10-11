# Restore a green main after the English translation

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1499 |
| Use Case | CU77 – Operations Monitoring and Incident Management (SDLC/CI gate health) |
| Branch | `fix/1499_restore_green_main` |
| Gate 1 status | Pending |

## Objetivo

`main` fails its own checks after the #1490 translation. Fix every failure at its root cause without weakening a test.

## What Changes

- `.gitignore`: anchor `reports/` to the repo root. The unanchored rule hid `frontend/src/app/dashboard/reports/`, so the reports page was never committed after the rename. Add the page back and a guard test.
- `contracts/`: align the reachability allowlist and its test with the renamed path parameter `{idSubmittedDocument}`.
- `controller-loose-returns.txt`: rename five entries to the translated handler names (same size; the baseline may only shrink).
- `V45__rename_remaining_spanish_identifiers.sql`: handle a missing `identificaciones` table (Constitution §12 exception, Owner-approved 2026-10-11).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Applied migrations are never edited. | `.claude/rules/database-migrations.md` | Exception for V45 only, approved by the Owner (§12) |
| Every endpoint is reachable from the UI or allowlisted. | CONSTITUTION §4 | Made explicit |

## Capabilities

### New Capabilities
- `green-main`: the repository's own gates pass on a clean checkout of `main`.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | Yes | V45 migration; test baseline file. |
| `frontend` | Yes | Restored `/dashboard/reports` page and its test. |
| `frontend-swing` | No | Retired. |
| `infra` / `workspace` / `contracts` | Yes | `.gitignore`, one guard test, allowlist. |
| CI/CD | No | |

### Surface area

- Database: V45 amended (not a new migration; see design). No schema difference on databases that already applied it.
- Endpoints: none new; the page restores the UI consumer of the report endpoints.
- Compatibility: databases that applied the original V45 need `flyway repair` (checksum changed).

### Architecture review

No ADR; this repairs a regression. The §12 exception is recorded here and in the commit and PR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Note the V45 amendment and `flyway repair`. |

## Out of Scope

- Renaming the `reportes` i18n namespace; other #1490 follow-ups.
