# Tasks — chore-1261-deprecated-owner-decision

> Governed by CONSTITUTION.md — groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1261 + CU76 verified
- [x] 1.2 OpenSpec proposal / traceability / specs / design / tasks
- [x] 1.3 `openspec validate --strict`

## 2. Crear branch

- [x] 2.1 Branch `cursor/docs-1261-owner-decision-pack-cf98` from updated main

## 3. Gate 2 — TDD

- [x] 3.1 Add failing `workspace/tests/test_adr022_owner_decision_pack.py`
- [x] 3.2 Observe failure on current ADR-022 / Pages content

## 4. Implementación

- [x] 4.1 Amend ADR-022 Pending Owner decision (Option A/B/C)
- [x] 4.2 Update REPO-SPLIT-PLAN P0.6 row
- [x] 4.3 Link ADR-022 on Pages Architecture
- [x] 4.4 CHANGELOG Unreleased
- [x] 4.5 Make unit test green

## 5. Actualizar tests existentes

- [x] 5.1 New guard test green; no product test updates required

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest workspace.tests.test_adr022_owner_decision_pack`
- [x] 6.2 `bash workspace/sdlc/validate-sdlc-plan.sh` (after archiving closed changes)

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI workflow

## 8. Gate 3 — Documentación permanente

- [x] 8.1 ADR-022, REPO-SPLIT-PLAN, Pages, CHANGELOG updated

## 9. Commits atómicos

- [x] 9.1 Conventional commits; `Refs #1261` `Refs #1197` (not Closes)

## 10. Pull Request y validación CI

- [ ] 10.1 Push; PR #1429; Process / PR Validation green
- [ ] 10.2 Mark ready when CI green

## 11. Deploy

- [ ] 11.1 Squash-merge via PR; Pages deploy on main

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Confirm Pages Architecture shows ADR-022 link after deploy
- [ ] 12.2 Leave #1261 OPEN for Owner choice

## Definition of Done

- [x] ADR-022 documents Pending Owner decision with Option A/B/C and #1261
- [x] Pages Architecture links ADR-022
- [x] Unit guard green
- [ ] SDLC plan validation green on the PR tip
- [ ] PR merged; #1261 still open awaiting Owner
