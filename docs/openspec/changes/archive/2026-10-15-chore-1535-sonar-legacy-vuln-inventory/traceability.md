# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1535 (slice 1 — inventory) | open (umbrella) |
| Use Case | CU78 – Security and Compliance | exists |
| Specification | `docs/openspec/changes/archive/2026-10-15-chore-1535-sonar-legacy-vuln-inventory/` | archived |
| Branch | `cursor/chore-1535-sonar-legacy-vuln-inventory-2ea7` | created |
| Tasks | `tasks.md` | implementation complete; CI pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Exporter lists vulnerability-type issues | `infra/tests/test_sonar_legacy_inventory.py` | verified |
| Exporter is unit-tested without live Sonar | same (fixtures) | verified |
| Every vulnerability row is classified | `all_vulnerabilities_classified` + inventory markdown | verified (seed has 0 VULNERABILITY rows; hotspot/bug classified) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md` | yes | pending |
| `.claude/rules/code-quality.md` | yes | pending |
| `CHANGELOG.md` | yes — Unreleased Changed | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1535, archived change |
| 2 | Failing tests first | yes | unittest red then green |
| 3 | Suite green, docs, CHANGELOG | yes | local unittest 12 OK |
| 4 | CI green | pending | pending |
| 5 | Deployed / umbrella comment | pending | pending |

## Exceptions

- Umbrella #1535 stays open — slice 1 uses `Refs #1535` (not Closes).
- Peer [#1754](https://github.com/matiaspakua/notaire/pull/1754) may also edit CHANGELOG (Bug Fixes); this PR adds Changed only.
