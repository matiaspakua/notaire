> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2121 names CU76
- [x] 1.2 CU76 exists
- [x] 1.3 Scenarios are in the delta spec
- [x] 1.4 Impact analysis names no product module
- [x] 1.5 ADRs are under `framework/ai-sdlc/docs/adr/`
- [ ] 1.6 IN PROGRESS label — the token may not add labels. No label was added by hand

## 2. Crear branch

- [x] 2.1 Branched from updated `main`
- [x] 2.2 `cursor/ai-sdlc-factory-foundation-b7c4`
- [x] 2.3 Recorded here. The people-form branch was not reused

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cap, redaction, sidecar time, partial validate, argv plan, inventory honesty
- [x] 3.2 `test_factory.py` and `test_migration_inventory.py`
- [x] 3.3 No API test
- [x] 3.4 Factory tests were run red on missing behavior, then green
- [x] 3.5 Scenarios map to those tests

## 4. Implementación

- [x] 4.1 Config and plugin commands
- [x] 4.2 OpenSpec argv adapter and example plugin
- [x] 4.3 Migration inventory and ADRs

## 5. Actualizar tests existentes

- [x] 5.1 Kit suite re-run
- [x] 5.2 Independence test still forbids adopter paths in the kit
- [x] 5.3 None removed

## 6. Ejecutar regresión

- [x] 6.1 Covered by `preflight.sh --full` before push
- [x] 6.2 JaCoCo unchanged
- [x] 6.3 `mvn verify` inside preflight
- [x] 6.4 Bruno inside preflight
- [x] 6.5 No `@Disabled`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 Product Playwright still runs inside preflight
- [x] 7.3 n/a — no UI surface
- [x] 7.4 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 README, foundation note, ADRs
- [x] 8.2 No endpoint change
- [x] 8.3 CHANGELOG line
- [x] 8.4 Nothing archived to `docs/000-archive/`
- [x] 8.5 Product docs were not copied into the kit
- [x] 8.6 Preflight is the push gate

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Closes #2121` on the PR
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs after commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive after the suite is green
- [ ] 10.2 Push only after preflight exits 0
- [ ] 10.3 Open the PR if fewer than three pull requests are open
- [ ] 10.4 GitHub does not re-run the product suite (#2001)
- [ ] 10.5 Owner reviews
- [ ] 10.6 Record the PR number

## 11. Deploy

- [ ] 11.1 Owner merges
- [ ] 11.2 Release-only CD
- [ ] 11.3 After merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After the Owner deploys
- [ ] 12.2 Revert is safe
- [ ] 12.3 Owner closes #2121
- [ ] 12.4 After merge

## Definition of Done

- [x] Issue and Use Case
- [x] Specification
- [x] Tests
- [ ] Full preflight
- [x] JaCoCo unchanged
- [x] No new UI
- [x] Docs and changelog
- [ ] PR after preflight
- [ ] Owner merge
