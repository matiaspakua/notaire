# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #804 | open (in-progress label ACL denied for bot) |
| Use Case | CU02, CU53, CU16, CU83 | exists / updated |
| Related | #833 (transition + UI), #806 (bitácora — preserved) | referenced |
| Specification | `openspec/changes/feat-804-enforce-workflow-transitions/` | Gate 1 complete |
| Branch | `cursor/feat-804-enforce-workflow-transitions-69d3` | active |
| Tasks | `tasks.md` | implementation + docs done; merge pending |
| Commits | `ea80c244` openspec; `06493144` failing IT; `544f4432` feat+docs; `758a767a`/`f3b5bcbc` tasks+PR | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1199 | draft |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh 1199`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Transición válida se aplica | `ManagementTransitionControllerIntegrationTest#shouldApplyValidTransition` | existing |
| Transición inválida es rechazada | `...#shouldRejectInvalidTransition` | existing |
| Gestión sin workflow definido rechaza cualquier transición | `...#shouldRejectTransitionWhenNoWorkflowDefinition` | existing |
| Plain PUT that changes status is rejected | `ManagementWorkflowStatusWriteEnforcementIntegrationTest#shouldRejectPlainPutThatChangesStatus` | passing |
| Complete-case PUT that changes status is rejected | `...#shouldRejectCompleteCasePutThatChangesStatus` | passing |
| Plain PUT that keeps the same status succeeds | `...#shouldAllowPlainPutThatKeepsSameStatus` | passing |
| Complete-case create with start-node status succeeds | `...#shouldAllowCompleteCaseCreateWithStartNodeStatus` | passing |
| Complete-case create with status outside the workflow is rejected | `...#shouldRejectCompleteCaseCreateWithStatusOutsideWorkflow` | passing |
| Workflow-trace lists transitions usable as legal next states | `...#shouldExposeLegalNextDestinationsViaWorkflowTrace` | passing |
| UI filtered destinations | TS-0011 / TS-0029 | confirm / CI |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md` | yes | `544f4432` |
| `docs/100-business/102-use-cases/CU53 - Modificar Gestión.md` | yes | `544f4432` |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | yes | `544f4432` |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | `544f4432` |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | yes | `544f4432` |
| `CHANGELOG.md` | yes | `544f4432` |
| `backend-api/api-test/COVERAGE.md` | yes | `544f4432` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh feat-804-enforce-workflow-transitions` |
| 2 | Failing tests written, test cases designed | yes | TDD red: 5 failures before fix |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` — 1970 tests, 0 failures; JaCoCo checks met |
| 4 | CI green, review approved, no conflicts | pending | draft PR #1199 |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).
Repo-wide `preflight` SDLC noise from stale open changes referencing closed issues (#801, #806, #1191) is outside this change.
