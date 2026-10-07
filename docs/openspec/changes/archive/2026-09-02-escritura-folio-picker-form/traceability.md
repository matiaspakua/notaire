# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #892 → spec.md → design.md → tasks.md → Commits → PR → Merge → Demo Smoke Test
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #892 | open, in-progress |
| Use Case | CU06 — Firmar Escritura | active |
| Functional Requirement | RF-27 — Firmar Escritura (prereq: folio) | active |
| Specification | `openspec/changes/escritura-folio-picker-form/` | draft (Gate 1) |
| Branch | `feat/892_escritura-folio-picker` | pending |
| Tests | `TS-0012-escritura-folio-firma.spec.ts` | pending (TDD) |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Demo smoke test | pending | pending |
| Release / tag | none planned — continuous deploy | n/a |

## Requirement Coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1: Folio selector populated from "Nuevo" folios | `TS-0012-escritura-folio-firma.spec.ts` | pending |
| US2: User selects folio, escritura created with folio assigned | `TS-0012-escritura-folio-firma.spec.ts` | pending |
| US3: Firma succeeds (no 400 error) after folio assignment | `TS-0012-escritura-folio-firma.spec.ts` | pending |
| US4: Demo Case A progresses from Escritura to Testimonio | `02-demo-two-full-cases.spec.ts` (Case A, lines 176–191) | pending |

## Gate Checklist

| Gate | Condition | Status | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria + Use Case | ✅ green | Issue #892, `spec.md` with Given-When-Then scenarios, linked to CU06/RF-27 |
| 2 | Failing tests written; test cases designed | ⏳ pending | Will write `TS-0012-...` and confirm red against pre-change code |
| 3 | Suite green, coverage held, docs updated | ⏳ pending | Will run `run_pipeline.sh` before opening PR |
| 4 | CI green, review approved, no conflicts | ⏳ pending | |
| 5 | Deployed, demo smoke test passed, Issue closed | ⏳ pending | |

## Permanent Documentation Updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | (to be added in commit 2) |
| `openspec/explore.md` | yes | `65a90e1` (demo findings) |

## Exceptions

None taken.
