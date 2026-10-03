# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1046 | open (Gate 1 draft only; implement after #1044 and #1051) |
| Use Case | CU78 – Security, Privacy and Compliance | exists |
| Related | #585 OPEN (`src.old` / `deprecated-src.old` — out of scope); audit-2026-09; GHSA-7w5x-hrqm-74c2; Log4j 1.x GHSAs on `log4j:log4j:1.2.17` | referenced |
| Specification | `openspec/changes/fix-1046-dependabot-alerts/` (draft: `internal/openspec-1046/`) | Gate 1 draft ready |
| Branch | `cursor/fix-1046-dependabot-alerts-69d3` | pending (do not create/push yet) |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| package.json declares a smol-toml override | hygiene script / file assert | pending |
| lockfile resolves smol-toml to a patched version | hygiene script / lockfile parse | pending |
| npm install keeps the override | `npm ci` + `npm ls smol-toml` | pending |
| deprecated-frontend-swing directory is absent | hygiene script / path assert | pending |
| no active frontend-swing module returns | root `pom.xml` + tree assert | pending |
| no log4j:log4j dependency remains in tracked POMs | hygiene script / ripgrep assert | pending |
| CODEOWNERS has no live frontend-swing path | hygiene script / file assert | pending |
| root README does not list deprecated-frontend-swing as present | hygiene script / review checklist | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | pending | — |
| `README.md` | pending | — |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md` | pending | — |
| `docs/200-architecture/201-SAD/sad.md` | pending | — |
| `.github/CODEOWNERS` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | draft ready (internal) | `internal/openspec-1046/` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Planning-only prep does not move the issue to IN PROGRESS (label ACL /
serialize queue). Implement waits for #1044 and #1051.
