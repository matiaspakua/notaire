> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #1266 exists, labeled, linked to CU76
- [ ] 1.2 Use Case documentation exists
- [ ] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR — n/a unless the design says otherwise
- [ ] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [ ] 2.1 `git fetch origin main`
- [ ] 2.2 `git checkout -b ci/1266_k6_runner_network`
- [ ] 2.3 Branch name recorded in `traceability.md`
- [ ] 2.4 Run `bash scripts/validate-sdlc-plan.sh ci-1266-k6-runner-network`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Add the failing guard: the workflow must not use the container action
- [ ] 3.2 Run `python3 -m unittest scripts.test_performance_test_assets` and observe it fail
- [ ] 3.3 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Install k6 with `grafana/setup-k6-action` and run `k6 run` on the runner
- [ ] 4.2 Dispatch the workflow on the branch and read the result
- [ ] 4.3 Guard green
- [ ] 4.4 Guards green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [ ] 6.1 Backend tests — n/a (no Java touched)
- [ ] 6.2 Coverage gate — n/a
- [ ] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 n/a - no permanent document describes the container action
- [ ] 8.2 `CHANGELOG.md` — n/a, not user visible
- [ ] 8.3 `docs/300-development/CI-PREFLIGHT.md` — n/a

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1266`; others `Refs #1266`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin ci/1266_k6_runner_network`
- [ ] 10.2 Open PR `[#1266] ci(performance): run the k6 load test on the runner`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1266 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive ci-1266-k6-runner-network`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1266` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
