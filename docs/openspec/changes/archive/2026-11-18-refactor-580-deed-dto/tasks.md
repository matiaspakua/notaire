> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #580 exists and names CU76
- [x] 1.2 CU76 already exists
- [x] 1.3 This slice is Deed only
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR
- [x] 1.6 WIP label via `add-issue-wip-label.sh 580` (SKIPPED on 403 is honest)

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 Branch `cursor/580-deed-mapper-47dc`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Field copy and no getDto
- [x] 3.2 `DeedLegacyMapperTest` written first
- [x] 3.3 No new HTTP test
- [x] 3.4 The test failed to compile: mapper class missing
- [x] 3.5 The cases map to that test

## 4. Implementación

- [x] 4.1 Add `DeedLegacyMapper`
- [x] 4.2 Testimony copies the deed fields
- [x] 4.3 Delete `getDto`

## 5. Actualizar tests existentes

- [x] 5.1 `DeedEntityTest` calls the mapper
- [x] 5.2 Ratchet line removed
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 Mapper, entity, and ratchet tests pass
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full` exit 0 before push

## 7. Ejecutar Playwright

- [x] 7.1 No UI change; the full preflight still runs Playwright

## 8. Gate 3 — Documentación permanente

- [x] 8.1 This change folder
- [x] 8.2 No product doc change

## 9. Commits atómicos

- [x] 9.1 Conventional commit naming the entity

## 10. Pull Request y validación CI

- [ ] 10.1 PR opened with `Refs #580`

## 11. Deploy

- [ ] 11.1 Owner merges
- [ ] 11.2 No image publish on this pull request

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Mapper test is the smoke
- [ ] 12.2 Revert is safe
- [ ] 12.3 Do not close #580

## Definition of Done

- [ ] Deed entity has no getDto
- [ ] Failing test observed
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
