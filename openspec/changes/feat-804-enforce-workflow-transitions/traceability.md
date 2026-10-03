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
| Issue | #804 | open (in-progress label ACL denied for bot) |
| Use Case | CU02, CU53, CU16, CU83 | exists |
| Related | #833 (transition + UI), #806 (bitácora — preserve) | referenced |
| Specification | `openspec/changes/feat-804-enforce-workflow-transitions/` | Gate 1 complete |
| Branch | `cursor/feat-804-enforce-workflow-transitions-69d3` | active |
| Tasks | `tasks.md` | 0/N implementation pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | pending (coordinator: `bash scripts/check-heavy-ci.sh <pr>`) | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Transición válida se aplica | `ManagementTransitionControllerIntegrationTest#shouldApplyValidTransition` | existing |
| Transición inválida es rechazada | `...#shouldRejectInvalidTransition` | existing |
| Gestión sin workflow definido rechaza cualquier transición | `...#shouldRejectTransitionWhenNoWorkflowDefinition` | existing |
| Plain PUT that changes status is rejected | `ManagementWorkflowStatusWriteEnforcementIntegrationTest` (new) | pending |
| Complete-case PUT that changes status is rejected | same | pending |
| Plain PUT that keeps the same status succeeds | same | pending |
| Complete-case create with start-node status succeeds | same | pending |
| Complete-case create with status outside the workflow is rejected | same | pending |
| Workflow-trace lists transitions usable as legal next states | `WorkflowTraceApiH2IntegrationTest` (+ assert) | pending |
| UI filtered destinations | TS-0011 / TS-0029 | confirm |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU02 – Iniciar Gestión.md` | pending | — |
| `docs/100-business/102-use-cases/CU53 - Modificar Gestión.md` | pending | — |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | pending | — |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | pending | — |
| `CHANGELOG.md` | pending | — |
| `backend-api/api-test/COVERAGE.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh feat-804-enforce-workflow-transitions` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. `in-progress` label could not be applied (GitHub GraphQL: Resource not accessible by integration).
