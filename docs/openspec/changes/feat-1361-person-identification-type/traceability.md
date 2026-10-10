# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1361 | open → in progress |
| Use Case | RF-37 – Alta de clientes; CU17 – Dar alta persona; CU41 – Modificar cliente | exists |
| Specification | `docs/openspec/changes/feat-1361-person-identification-type/` | Gate 1 draft |
| Branch | `feat/1361_person_identification_type` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| New person starts on DNI | `personas-identification-type.test.tsx`, TS-0015 #1361 | passing |
| Create with CUIT is listed as CUIT | TS-0015 #1361, `PersonIdentificationTypeIntegrationTest` | passing |
| Omitted type defaults to DNI | `PersonIdentificationTypeIntegrationTest`, Bruno `people/01` | passing |
| Unknown type id is rejected | `PersonIdentificationTypeIntegrationTest`, Bruno `people/08` | passing |
| Untyped person must pick a type on save | `personas-identification-type.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `backend-api/openapi/openapi.yaml` | yes | branch commit |
| `backend-api/api-test/COVERAGE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1361-person-identification-type` |
| 2 | Failing tests written, test cases designed | yes | JUnit 3/5, Vitest 3/3 and TS-0015 #1361 failing on the pre-change code |
| 3 | Suite green, coverage held, docs updated | yes | backend and frontend `verify.sh` green; Bruno and chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the branch backend; Bruno ran against the branch jar on :8081.
