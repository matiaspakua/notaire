> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #580 exists and names CU76
- [x] 1.2 CU76 already exists
- [x] 1.3 This slice is FolioType only
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR. ADR-030 already covers the clean record
- [x] 1.6 WIP label via `add-issue-wip-label.sh 580` (SKIPPED on 403 is honest)

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 Branch `cursor/580-folio-type-mapper-47dc`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: field copy, apply, and no getDto on the entity
- [x] 3.2 `FolioTypeLegacyMapperTest` written first
- [x] 3.3 No new HTTP test
- [x] 3.4 The test failed to compile: mapper class missing
- [x] 3.5 The cases map to that test

## 4. Implementación

- [x] 4.1 Add `FolioTypeLegacyMapper`
- [x] 4.2 `Folio.getDto` copies folio-type fields
- [x] 4.3 Delete `getDto` and `setAttributes` from `business.FolioType`

## 5. Actualizar tests existentes

- [x] 5.1 `EntitiesBasicTest` calls the mapper
- [x] 5.2 Assertions on name are unchanged
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `FolioTypeLegacyMapperTest` and `EntitiesBasicTest` passed
- [x] 6.2 JaCoCo floor unchanged
- [x] 6.3 Full backend gates run inside `preflight.sh --full`
- [x] 6.4 Bruno unchanged
- [x] 6.5 No skipped tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 n/a — no UI surface. Mapping only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 No HTTP or use-case text changes
- [x] 8.2 OpenAPI unchanged
- [x] 8.3 CHANGELOG n/a
- [x] 8.4 Nothing to archive
- [x] 8.5 No duplicated permanent doc
- [x] 8.6 `preflight.sh --full` is the gate

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Refs #580`
- [ ] 9.3 No secrets
- [ ] 9.4 Record the SHA

## 10. Pull Request y validación CI

- [ ] 10.1 Archive the change
- [ ] 10.2 Push
- [ ] 10.3 Open the PR
- [ ] 10.4 Local preflight is the proof (#2001)
- [ ] 10.5 Owner reviews
- [ ] 10.6 Record the PR number

## 11. Deploy

- [ ] 11.1 Owner merges
- [ ] 11.2 No image publish on this pull request
- [ ] 11.3 Record the merge later

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Mapper test is the smoke
- [ ] 12.2 Revert is safe
- [ ] 12.3 Do not close #580
- [ ] 12.4 Record later

## Definition of Done

- [ ] FolioType entity has no transport methods
- [ ] Failing test observed
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
