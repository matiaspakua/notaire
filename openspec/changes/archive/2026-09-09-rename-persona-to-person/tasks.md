# Tasks: Rename Persona entity to Person

## 1. Gate 1 — Prerequisites

- [x] Issue #974 created, linked to epic #973
- [x] `proposal.md`, `design.md` written; `skip_specs: true` (no behavior change)
- [x] `bash scripts/validate-sdlc-plan.sh` passes for this change

## 2. Crear branch

- [x] `refactor/974_rename-persona-to-person` created from up-to-date `main`

## 3. Gate 2 — Escribir tests

- [ ] Rename existing `Persona`-related unit/integration tests to `Person`
      naming, updated to compile against the not-yet-renamed code (expected
      to fail to compile until Step 4 — this is the "red" step for a pure
      rename: the rename itself is the change under test)

## 4. Implementación

- [ ] New Flyway migration `V{n}__rename_persona_to_person.sql`
- [ ] Rename `negocio.Persona` → `Person`, `dto.DtoPersona` → `DtoPerson`,
      `repository.PersonaRepository` → `PersonRepository`,
      `api.PersonaController` → `PersonController`
- [ ] Rename fields per `proposal.md` field mapping table
- [ ] Update all dependent entities' references (`TramitesPersonas`,
      `Usuario`, `GestionDeEscritura`, `Folio`, `Suplencia`, `Copia`,
      `Testimonio`)
- [ ] REST path `/api/v1/personas` → `/api/v1/people`, JSON fields translated
- [ ] Frontend: `Persona` type → `Person`, pages/components, i18n keys

## 5. Actualizar tests existentes

- [ ] All backend unit/integration tests referencing `Persona` compile and
      pass under new names
- [ ] Playwright E2E specs referencing personas UI/fixtures updated

## 6. Ejecutar regresión

- [ ] `mvn verify -pl backend-api` green
- [ ] `mvn test -Ppg-integration` green (Flyway schema validation)

## 7. Ejecutar Playwright

- [ ] Full Playwright E2E suite green

## 8. Gate 3 — Documentación permanente

- [ ] `CHANGELOG.md` `[Unreleased]` entry added
- [ ] `docs/` left untouched (Spanish, out of epic scope)

## 9. Commits atómicos

- [ ] Migration + backend rename in one commit, frontend rename in another,
      test renames grouped with the layer they cover

## 10. Pull Request y validación CI

- [ ] `bash scripts/run_pipeline.sh` exit 0
- [ ] PR opened referencing #974, `Closes #974`
- [ ] `gh pr view --json mergeable,mergeStateStatus` confirms `MERGEABLE`

## 11. Deploy

- [ ] Merge triggers standard deploy; Flyway migration applies on backend
      startup

## 12. Gate 5 — Smoke test y cierre

- [ ] Post-deploy smoke test (Docker build + smoke) green
- [ ] Issue #974 closed

## Definition of Done

All boxes above checked, full quality gates green, `docs/` unchanged, PR
merged, issue #974 closed, epic #973 updated with slice 1 status.
