> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1563 exists and names CU76
- [x] 1.2 Use Case CU76 already exists
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR. The change is not architectural
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 1563` (SKIPPED on 403 is honest)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/1563-reachability-specificity-47dc`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 The scenario is the date route versus the id route
- [x] 3.2 Write the unit test in `contracts/tests/test_api_reachability.py`
- [x] 3.3 No integration test. The guard is the integration of OpenAPI and the frontend tree
- [x] 3.4 Run the new test and observe it fail
- [x] 3.5 The scenario maps to that test

## 4. Implementación

- [x] 4.1 In `reachable()`, keep only the exact fits with the fewest wildcards
- [x] 4.2 Add allowlist reasons for every endpoint the stricter scan reports

## 5. Actualizar tests existentes

- [x] 5.1 `test_every_endpoint_is_called_from_the_ui_or_allowlisted` and `test_allowlist_has_no_stale_entries` must stay green
- [x] 5.2 Do not weaken those assertions
- [x] 5.3 No test is obsolete

## 6. Ejecutar regresión

- [x] 6.1 Backend unit and integration run inside `preflight.sh --full`
- [x] 6.2 JaCoCo floor unchanged
- [x] 6.3 Backend quality gates run inside `preflight.sh --full`
- [x] 6.4 Bruno runs inside `preflight.sh --full`. No API change
- [x] 6.5 No `@Disabled` or skipped tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface. No Playwright spec is added
- [x] 7.2 n/a — no UI surface. The product Playwright suite is unchanged
- [x] 7.3 n/a — no UI surface. No screen changes
- [x] 7.4 n/a — no UI surface. Scanner and allowlist only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 The allowlist reasons are the permanent record
- [x] 8.2 OpenAPI unchanged
- [x] 8.3 `CHANGELOG.md` n/a — not user visible
- [x] 8.4 No document is superseded
- [x] 8.5 The allowlist is the single list of API-only endpoints
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` is the gate

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Closes #1563` on the commit that finishes the acceptance criteria
- [ ] 9.3 No secrets
- [ ] 9.4 Record the commit SHA in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR
- [ ] 10.2 Push the branch
- [ ] 10.3 Open the PR
- [ ] 10.4 GitHub does not re-run the suite on the pull request (#2001). Local preflight is the proof
- [ ] 10.5 Ready for the Owner to review. Agents do not merge
- [ ] 10.6 Record the PR number after it exists

## 11. Deploy

- [ ] 11.1 Merge is the Owner's step
- [ ] 11.2 No image publish on this pull request
- [ ] 11.3 Record the merge after the Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke is the reachability test
- [ ] 12.2 Revert is safe
- [ ] 12.3 The Owner closes the issue by merging
- [ ] 12.4 Record smoke after merge

## Definition of Done

- [ ] Issue linked to CU76
- [ ] Gate 1 passed
- [ ] Failing test observed
- [ ] `preflight.sh --full` exit 0
- [ ] Allowlist updated
- [ ] PR open for the Owner
