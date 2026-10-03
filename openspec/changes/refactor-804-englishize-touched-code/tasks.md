> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1203 exists and linked to CU02/CU53/CU16/CU83
- [x] 1.2 Use Case documentation exists (behavior unchanged)
- [x] 1.3 Acceptance Criteria recorded in design/traceability (`skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (language hygiene)
- [ ] 1.6 Move Issue #1203 to IN PROGRESS when labels allow

## 2. Crear branch

- [x] 2.1 Branch already exists from Englishize work
- [x] 2.2 `cursor/refactor-804-englishize-touched-code-69d3`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-804-englishize-touched-code`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate: OpenAPI stale, Process Checks, tie-date IT, Playwright Spanish matcher
- [x] 3.2 Observe CI red on those four surfaces
- [x] 3.3 No new product tests required — fix existing
- [x] 3.4 Confirm acceptance rows in `traceability.md` map to tests/commands

## 4. Implementación

- [ ] 4.1 Add this OpenSpec change folder and validate
- [ ] 4.2 Fix `shouldReturnLaterHistoryRowWhenDatesTie` to seed History (not PUT status)
- [ ] 4.3 Update Playwright matchers to `/is not allowed/i`
- [ ] 4.4 Regenerate `backend-api/openapi/openapi.yaml` via `export-openapi.sh`
- [ ] 4.5 Englishize remaining Spanish DisplayNames in the touched IT class

## 5. Actualizar tests existentes

- [ ] 5.1 Integration + Playwright assertions updated without weakening behavior checks
- [ ] 5.2 Document old expectations wrong because messages/Englishize + #804 PUT guard
- [x] 5.3 No obsolete tests removed

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api -Dtest=ManagementHistorialOrphanWriteIntegrationTest`
- [ ] 6.2 Coverage gate via CI after push
- [ ] 6.3 `mvn verify` / preflight as environment allows
- [x] 6.4 Bruno already green on #1201 head
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 Update TS-0011 / TS-0028 English error matchers
- [ ] 7.2 Re-check Playwright CI job after push
- [x] 7.3 n/a new responsive UI
- [x] 7.4 Record: UI copy assertion only

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Commit regenerated OpenAPI yaml
- [x] 8.2 CU docs n/a
- [x] 8.3 No duplicate permanent docs
- [ ] 8.4 `fix-1201-status.md` with new head SHA

## 9. Commits atómicos

- [ ] 9.1 Conventional commits for OpenSpec + CI fixes
- [ ] 9.2 Reference #1203 (and related #804)
- [x] 9.3 No secrets
- [ ] 9.4 Record SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Push to existing draft PR #1201 branch
- [x] 10.2 Do not merge; do not touch #1202
- [ ] 10.3 Wait for required workflows
- [ ] 10.4 Gate 4 pending review
- [x] 10.5 PR number #1201 recorded

## 11. Deploy

- [ ] 11.1 Owner merges via PR when ready — never push to `main`
- [ ] 11.2 Confirm deploy workflow after merge
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke after merge
- [ ] 12.2 Rollback = revert PR (safe)
- [ ] 12.3 Close Issue #1203 referencing PR #1201
- [ ] 12.4 Archive this change: `openspec archive refactor-804-englishize-touched-code`

## Definition of Done

- [x] Issue linked to Use Cases
- [x] Specification written (`skip_specs` justified)
- [x] Failures observed on CI (Gate 2 evidence)
- [ ] Implementation makes CI green (Gate 3–4)
- [ ] Coverage gate held
- [ ] Playwright matchers updated; CI re-checked
- [ ] OpenAPI committed
- [ ] Commits conventional; pushed to #1201 branch
- [ ] Draft PR updated by push; not merged by agent
- [ ] Issue closed after merge (Gate 5 — owner)
