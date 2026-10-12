> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1561 exists, labeled DOC, DEVOPS, priority:high, TAREA, linked to RNF-21 and RNF-22
- [x] 1.2 RNF-21 and RNF-22 already exist as catalog issues
- [x] 1.3 Acceptance Criteria are the scenarios in `specs/delivery-schedule/spec.md`
- [x] 1.4 Impact Analysis is in `proposal.md` (docs, github-page, workspace)
- [x] 1.5 No ADR — process documentation, SAD §11.3 corrected in place
- [ ] 1.6 `in-progress` label — blocked, this token cannot edit issue labels (HTTP 403)

## 2. Crear branch

- [x] 2.1 `main` matches `origin/main` at the start of the change
- [x] 2.2 Branch `cursor/project-management-roadmap-4e9a` (Cloud agent branch template)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Scenarios enumerated in the delta spec
- [x] 3.2 `workspace/sdlc/test_milestone_policy.py`
- [x] 3.3 No integration test — GitHub mutations are not available to this token
- [x] 3.4 Observed `ModuleNotFoundError: No module named 'milestone_policy'` before the module existed
- [x] 3.5 Each scenario maps to a test method in `design.md`

## 4. Implementación

- [x] 4.1 `workspace/sdlc/milestone_policy.py` — classify, lane, priority
- [x] 4.2 `workspace/sdlc/apply_milestone_policy.py` — `--check`, `--dry-run`, `--apply`
- [x] 4.3 `docs/github/PROJECT-MANAGEMENT.md` and snapshot JSON
- [x] 4.4 GitHub Pages `/docs/management/` chart
- [x] 4.5 SAD §11.3, fleet pick rule, foreman pick rule
- [x] 4.6 `--apply` attempted; milestone, label, and project mutations returned HTTP 403

## 5. Actualizar tests existentes

- [x] 5.1 No existing product tests cover milestone assignment
- [x] 5.2 Nothing to weaken
- [x] 5.3 No obsolete tests

## 6. Ejecutar regresión

- [x] 6.1 n/a — no `backend-api` change. Policy tests: `python3 -m unittest workspace/sdlc/test_milestone_policy.py`
- [x] 6.2 n/a — JaCoCo ratchet does not cover `workspace/sdlc`
- [x] 6.3 n/a — no Java sources
- [x] 6.4 n/a — no HTTP contract change
- [x] 6.5 No skipped product tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI. Pages chart is `github-page/`
- [x] 7.2 n/a — product Playwright suite does not mount `github-page/`
- [x] 7.3 n/a — verified with the Pages app build and a browser pass of `/docs/management/`
- [x] 7.4 Product UI: n/a. Docs chart: verified separately

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Handbook, README, SAD, fleet, foreman, indexes, changelog
- [x] 8.2 No OpenAPI change
- [x] 8.3 `CHANGELOG.md` Unreleased
- [x] 8.4 Nothing archived; the old milestone names are explained in the handbook
- [x] 8.5 Handbook is the schedule; README and SAD point at it
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` before push

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Commits end with `Closes #1561`
- [x] 9.3 No secrets
- [x] 9.4 Policy commit `52c07721` recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Push the branch
- [ ] 10.2 PR `[#1561] docs(pm): schedule milestones and agent lanes`
- [ ] 10.3 CI on the docs-scoped workflows
- [ ] 10.4 Gate 4
- [ ] 10.5 PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 GitHub Pages deploys from `main` after CI. No cluster apply.

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, `/docs/management/` renders the chart
- [ ] 12.2 A token with Issues and Projects write runs `--apply`, then `--check` exits 0
- [ ] 12.3 Close #1561 only after `--check` is green; until then the issue stays open because the live board is not updated

## Definition of Done

- [x] Policy tests green
- [x] Permanent docs name the trains, the history, and the lanes
- [ ] Live GitHub milestones match the policy (`--check` exits 0)
- [ ] PR merged
