# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1963 | in-progress |
| Use Case | CU-78 / CU-76 / ADR-029 / ADR-030 | exists |
| Specification | `docs/openspec/changes/chore-1963-wave9-top10-critical/` | passed |
| Branch | `chore/1963_wave9_top10_critical` | created |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Close-set linkage

| Issue | Role | Status |
|-------|------|--------|
| #1866 | Hex R2 document child | pending verify/close |
| #1683 | Module grants | pending |
| #254 | HTTPS/TLS | pending |
| #1684 | Isolation checklist | pending |
| #566 | CI fail/ratchet | pending |
| #655 | Bean Validation | pending |
| #595 | EAGER→LAZY | pending |
| #1921 | 400 vs 404 | pending |
| #1920 | Empty accepted-breaking | pending |
| #1315 | OpenAPI coherent | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Granted module allows access | pending | pending |
| Missing module is denied | pending | pending |
| Invalid payment update value | pending | pending |
| Missing payment | pending | pending |
| Empty effective ignore list | pending | pending |
| Concept items collection is LAZY | pending | pending |
| Default JWT secret fails the checklist | pending | pending |
| Trivy CRITICAL fails CI | pending | pending |
| HTTPS profile loads SSL properties | pending | pending |
| Empty create body on previously unvalidated controller | pending | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| Isolation / HTTPS / CI security docs | pending | pending |
| `CHANGELOG.md` | pending | pending |
| README / GitHub Pages (release follow-on) | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1963, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
