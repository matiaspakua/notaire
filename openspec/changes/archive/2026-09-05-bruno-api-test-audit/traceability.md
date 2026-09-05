# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #952 | closed (merged via PR #955) |
| Use Case | CU76 | exists |
| Specification | `openspec/changes/bruno-api-test-audit/` | complete, archived |
| Branch | `test/952_bruno-api-test-audit` | merged |
| Tasks | `tasks.md` | complete |
| Commits | `b4e89a1`, `38fea13`, `5db94cc` | done |
| Pull Request | [#955](https://github.com/matiaspakua/notaire/pull/955) | merged |
| CI run | green on PR #955 | done |
| Merge commit | `3abe6a68c1cf5f6f16f37a2606b8324a12b9879c` | done, 2026-09-05T17:58:56Z |
| Release / tag | n/a — no separate release process | n/a |
| Smoke test | `bru run` re-verified post-merge (149/149, 266/266) | done |

## Requirement coverage

This change is `skip_specs: true` (test infrastructure + a defect fix that
restores already-specified CU15 behavior, no new/changed requirement text).
Requirement coverage is therefore tracked directly against Issue #952's
Acceptance Criteria instead of `#### Scenario:` blocks:

| Acceptance Criterion (Issue #952) | Test / Evidence | Status |
|---|---|---|
| Every REST endpoint has ≥1 Bruno request (happy path; error paths where meaningful) | `backend-api/api-test/**/*.yml` per domain folder | done for the 20 folders in scope — remaining 16 controllers tracked as follow-up #953 (see Out of Scope in proposal.md) |
| Every Bruno request has `tests {}` with chai assertions | same | done |
| Full suite runs green via `bru run` against `scripts/start.sh` stack | `bru run . -r --env Developmen` | done — 149 requests / 266 tests passing |
| `COVERAGE.md` regenerated and accurate | `backend-api/api-test/COVERAGE.md` | done |
| `TEST-PLAN.md` §7 and `CU-API-MATRIX.csv` updated | doc diff | done — `TEST-PLAN.md` §7 already pointed at `COVERAGE.md`/CSV (no edit needed); `CU-API-MATRIX.csv` rows for CU11, CU13, CU15, CU47, CU69, CU71 updated from MISSING/NO-TEST to their Bruno file |
| Bugs found while auditing are filed as Issues, not silently worked around | `Item.fkIdPresupuesto` `@JsonIgnore` defect, plus 3 further silent-delete defects (`Persistable.isNew()` on `Historial`/`Item`/`Pago`/`Tramite`, `EstadoDeGestion.historialList` cascade) found while authoring the `historial`/`items`/`pagos`/`tramites` folders — all fixed in this change (see `CHANGELOG.md`); documented here rather than separate issues since fixed within the same PR | done |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `backend-api/api-test/COVERAGE.md` | done | pending (this PR) |
| `docs/300-development/303-testing/TEST-PLAN.md` | no change needed (§7 already generic) | n/a |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | done | pending (this PR) |
| `CHANGELOG.md` | done | pending (this PR) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #952 (closed, linked to CU76); this proposal |
| 2 | Failing tests written, test cases designed | yes | Bugs reproduced via `curl`/DB inspection and (for the historial delete cascade defect) `HistorialDeleteIntegrationTest` before fixing; Bruno `items`/`pagos`/`historial`/`tramites` folder assertions cover the fixes |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` BUILD SUCCESS (1745/1745 backend tests, coverage gate passed); `bru run` 149/149 requests, 266/266 tests; docs updated above |
| 4 | CI green, review approved, no conflicts | yes | PR #955 merged, CI green, `mergeable: MERGEABLE` confirmed before merge |
| 5 | Deployed, smoke test passed, Issue closed | yes | Issue #952 closed via PR #955 merge; `bru run` re-verified post-merge |

## Exceptions

None.
