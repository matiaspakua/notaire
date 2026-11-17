> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1244 exists and names CU76
- [x] 1.2 Use Case CU76 already exists
- [x] 1.3 The acceptance item in this slice is the stale 500-ERROR column
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No ADR
- [x] 1.6 WIP label via `add-issue-wip-label.sh 1244` (SKIPPED on 403 is honest)

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 Branch `cursor/1244-matrix-status-47dc`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Case: a Bruno path plus `500-ERROR` is a contradiction
- [x] 3.2 Unit test reads the committed CSV
- [x] 3.3 No integration test
- [x] 3.4 Observe the test fail
- [x] 3.5 The case maps to that test

## 4. Implementación

- [x] 4.1 Set the eleven Bruno-backed `500-ERROR` rows to OK
- [x] 4.2 Replace notes that claim the base GET returns 500

## 5. Actualizar tests existentes

- [x] 5.1 Existing matrix validator tests stay green
- [x] 5.2 No assertion is weakened
- [x] 5.3 No test is removed

## 6. Ejecutar regresión

- [x] 6.1 Backend suite runs inside `preflight.sh --full`
- [x] 6.2 JaCoCo floor unchanged
- [x] 6.3 Quality gates run inside preflight
- [x] 6.4 Bruno runs inside preflight. No collection change
- [x] 6.5 No skipped tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 n/a — no UI surface. Matrix and a unit test only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 The CSV is the permanent record
- [x] 8.2 OpenAPI unchanged
- [x] 8.3 CHANGELOG n/a
- [x] 8.4 Nothing to archive
- [x] 8.5 The matrix remains the single status table
- [x] 8.6 `preflight.sh --full` is the gate

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Refs #1244` because the remaining acceptance items stay open
- [ ] 9.3 No secrets
- [ ] 9.4 Record the SHA

## 10. Pull Request y validación CI

- [ ] 10.1 Archive the change
- [ ] 10.2 Push
- [ ] 10.3 Open the PR
- [ ] 10.4 Local preflight is the proof (#2001)
- [ ] 10.5 Owner reviews. Agents do not merge
- [ ] 10.6 Record the PR number

## 11. Deploy

- [ ] 11.1 Owner merges
- [ ] 11.2 No image publish on this pull request
- [ ] 11.3 Record the merge later

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The unit test is the smoke for this slice
- [ ] 12.2 Revert is safe
- [ ] 12.3 Do not close #1244
- [ ] 12.4 Record later

## Definition of Done

- [ ] Slice specified
- [ ] Failing test observed
- [ ] CSV corrected
- [ ] `preflight.sh --full` exit 0
- [ ] PR open. #1244 stays open
