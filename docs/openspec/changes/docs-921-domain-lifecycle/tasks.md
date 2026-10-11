> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #921 exists and is open, linked to CU76 and CU77
- [x] 1.2 Use Case documentation exists; this change does not alter product behaviour
- [x] 1.3 Acceptance criteria live in the design testing table (`skip_specs: true`)
- [x] 1.4 Impact analysis recorded in `proposal.md`
- [x] 1.5 No new ADR — ADR-014 and ADR-027 already cover the decisions
- [ ] 1.6 Issue stays open: the audit's remaining acceptance criteria are not done

## 2. Crear branch

- [x] 2.1 Branch `cursor/lifecycle-engineering-docs-853b` exists from `main`
- [x] 2.2 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Case: merge checkout + stale base + OpenSpec path only on main → fail
- [x] 3.2 Case: merge checkout + OpenSpec path on the PR → pass
- [x] 3.3 Observed the first case fail with `✓ PR carries an OpenSpec change` before the fix
- [x] 3.4 Docs-only product surface: no new Java test

## 4. Implementación

- [x] 4.1 Write `CICLO-DE-VIDA.md` and `INGENIERIA.md` and link them
- [x] 4.2 `check-sdlc-exception.sh` diffs `HEAD^1` when `HEAD` is a merge commit
- [x] 4.3 `sdlc-process.yml` sets `BASE_SHA` from that same parent for the other process checks

## 5. Actualizar tests existentes

- [x] 5.1 Existing `SdlcExceptionTest` cases kept; no assertion weakened
- [x] 5.2 No obsolete test removed

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest workspace.tests.test_pr_checks`
- [x] 6.2 `python3 docs/tests/test_docs_links.py` and `python3 docs/tests/test_business_docs_traceability.py`
- [x] 6.3 `bash workspace/sdlc/validate-sdlc-plan.sh docs-921-domain-lifecycle`
- [x] 6.4 Maven, Bruno, and Playwright — n/a, no product code or UI

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Lifecycle, engineering method, and the indexes updated
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` Unreleased notes the process-check fix
- [x] 8.4 Nothing archived; legacy PlantUML stays with a pointer
- [x] 8.5 The lifecycle doc is the single state description; use cases are not copied into it

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Messages reference #921 without `Closes` — the audit issue stays open
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed
- [x] 10.2 Pull request #1492
- [ ] 10.3 Process Checks green on the commit that contains this change folder
- [ ] 10.4 Review approved

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [x] 11.2 CD image publish — n/a, docs and a workflow-only change
- [ ] 11.3 Record the merge commit after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Process Checks on the merge commit show this change folder, not a foreign one
- [x] 12.2 Rollback is a revert, as written in `design.md`
- [ ] 12.3 Do not close #921
- [ ] 12.4 Archive this change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with acceptance criteria
- [x] Specification written (Gate 1) and checked with `validate-sdlc-plan.sh`
- [x] The new process-check test was written first and observed failing (Gate 2)
- [x] Docs tests and `test_pr_checks` green; product suites not in scope
- [x] JaCoCo — n/a, no Java change
- [x] Playwright — n/a, no UI
- [x] Permanent documentation updated (Gate 3)
- [x] Commits conventional and reference #921
- [ ] Pull request CI green and review approved (Gate 4)
- [ ] Merged and the audit issue still open (Gate 5)
- [x] `traceability.md` records the chain through the pull request; merge and release stay pending
