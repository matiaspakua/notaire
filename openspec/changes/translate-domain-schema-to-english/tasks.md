> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**. This change ships as
> multiple independent slices (one PR per table cluster, per `design.md` —
> Migration Plan). Groups 2-11 below are written for **Slice 1 (leaf tables)**
> concretely; each subsequent slice repeats the same group 2-11 pattern against
> its own table cluster and gets its own branch/PR — see §13 for the full slice
> roster and the repeat-per-slice checklist.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #973 exists, labeled (`BACKEND`, `DB`, `TEST`, `REFACTOR`), open
- [x] 1.2 Narrow issue #973's description/acceptance criteria to the schema-only scope (per `explore.md`) before starting Slice 1 — the epic currently describes already-completed Java/API/frontend work as outstanding
- [x] 1.3 Confirm no Use Case applies (technical/naming-only change, no behavior change) — record this explicitly in the PR, do not silently omit it
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No ADR required (see `design.md` — Architecture review: repeats existing `people`-slice precedent)
- [x] 1.6 `gh issue edit 973 --add-label "in-progress"`

## 2. Crear branch (Slice 1)

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b refactor/973_rename_leaf_reference_tables`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Confirm `FlywaySchemaValidationIntegrationTest` exists and currently passes against the *old* Spanish schema (baseline)
- [ ] 3.2 After writing Slice 1's migration + annotation changes locally but before running the suite, confirm the test would fail if only one side (migration OR annotations) were applied — proves the test actually detects a mismatch
- [x] 3.3 No new test classes needed — this is schema-only; `FlywaySchemaValidationIntegrationTest` is the existing regression harness that must stay green end-to-end
- [x] 3.4 `mvn test -pl backend-api -Dtest=FlywaySchemaValidationIntegrationTest -Ppg-integration` — passed via the `pre-push` hook's preflight run (Docker available there), confirming schema/entity alignment

## 4. Implementación (Slice 1 — leaf reference tables)

Tables: `tipos_identificacion`, `tipos_de_documento`,
`tipos_de_folio`, `tipos_de_tramite`, `roles`, `estados_de_gestion`,
`registro_auditoria`, `cuadernos` (see `design.md` — Migration Plan, step 1).
`identificaciones` was dropped from this slice during implementation: it has
no corresponding Flyway-managed table (pre-existing entity/schema drift,
unrelated to this rename) — see the migration file's own header comment.

- [x] 4.1 Check next free Flyway version: `ls backend-api/src/main/resources/db/migration/ | sort -V | tail -1`
- [x] 4.2 Write `V{n}__rename_leaf_reference_tables_to_english.sql`: `ALTER TABLE ... RENAME TO ...` + `ALTER TABLE ... RENAME COLUMN ...` + `ALTER SEQUENCE ... RENAME TO ...` for each of the 9 tables, following the `V25` pattern (never edit an existing migration)
- [x] 4.3 For `registro_auditoria`, grep `infra/` and `AuditoriaAspect` for raw SQL references before renaming — update any found (see `design.md` — Riesgos)
- [x] 4.4 Update `@Table`/`@Column` annotations in the corresponding `business/*.java` entities (`Identification`, `IdentificationType`, `DocumentType`, `FolioType`, `ProcedureType`, `Role`, `ManagementStatus`, `AuditRecord`, `Notebook`) to the new English names
- [x] 4.5 Grep `src/test/` and `backend-api/api-test/` for any raw SQL references to the old table/column names in these 9 tables; update if found

## 5. Actualizar tests existentes

- [x] 5.1 Identify tests referencing these 9 entities (unit + integration) — expect zero source changes needed since Java field names are unchanged, only annotation targets moved
- [x] 5.2 If any test hardcodes a raw SQL identifier found in 4.5, update it and document why the old expectation assumed the Spanish name
- [x] 5.3 No tests expected to become obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (expect no change — no new/removed code paths)
- [x] 6.3 `mvn verify -pl backend-api` — Checkstyle, SpotBugs
- [x] 6.4 `mvn test -pl backend-api -Dtest=FlywaySchemaValidationIntegrationTest -Ppg-integration` — passed (pg-integration tests green in preflight)
- [ ] 6.5 `bash testing/scripts/test.sh` — HTTP/Bruno API suite — not run by `preflight.sh` in non-`--full` mode; CI's `playwright-e2e.yml` runs it, required to be green before merge
- [x] 6.6 No `@Disabled` tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface (see `design.md` — Playwright Strategy: REST contract/frontend field names are unaffected by this schema-only rename)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Grep `docs/200-architecture/` for the old Spanish table names from this slice; update any hit to the new English name
- [x] 8.2 No OpenAPI/Swagger changes expected (no endpoint/DTO change)
- [x] 8.3 `CHANGELOG.md`: n/a — not user-visible
- [x] 8.4 No documents to archive for this slice
- [x] 8.5 Confirm no duplicated documentation was introduced
- [x] 8.6 `bash scripts/preflight.sh --fix` — ran automatically via the `pre-push` hook, all 14 applicable gates passed

## 9. Commits atómicos

- [ ] 9.1 One commit for the migration + annotation updates (atomic — they must land together per `design.md` — Deployment Strategy)
- [ ] 9.2 Separate commit only if 4.3/4.5 required unrelated raw-SQL fixes outside the entities in this slice
- [ ] 9.3 Every commit ends with `Closes #973` only on the commit that completes the **last** slice; intermediate slices reference `#973` without `Closes` (per `.claude/rules/general.md` §6.1 — atomic commits, only the closing commit carries `Closes`)
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin refactor/973_rename_leaf_reference_tables`
- [ ] 10.2 Open PR titled `[#973] refactor(db): rename leaf reference tables to English`
- [ ] 10.3 Wait for `ci.yml`, `pr-validation.yml` green (no `frontend-ci.yml`/`playwright-e2e.yml` impact expected, but they still run)
- [ ] 10.4 Gate 4 — CI green, code owner merge counts as review approval, no conflicts, docs complete
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm `cd.yml` published the image
- [ ] 11.3 Record merge commit/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: verify a CRUD endpoint for one entity in this slice (e.g. `GET /api/v1/roles`) on the deployed environment
- [ ] 12.2 Confirm rollback path (new reverse-rename migration) is documented and available if needed
- [ ] 12.3 Do **not** close issue #973 yet — it stays open until every slice in §13 is merged; reference this PR in a progress comment on #973 instead
- [ ] 12.4 Do not archive this change yet — repeat groups 2-12 for the next slice using the same `openspec/changes/translate-domain-schema-to-english/` change (do not scaffold a new change per slice); archive only after the final slice (§13) closes #973

## 13. Remaining slices (repeat groups 2-12 per slice)

Each row is its own branch + PR, following the exact group 2-12 pattern
above with its own table list substituted into group 4. Update this table's
Status column as each slice merges.

| Slice | Tables | Status |
|---|---|---|
| 1 | `tipos_identificacion`, `tipos_de_documento`, `tipos_de_folio`, `tipos_de_tramite`, `roles`, `estados_de_gestion`, `registro_auditoria`, `cuadernos` (`identificaciones` dropped — no live table) | implemented, pushed, pg-integration green locally, PR pending |
| 2 | `inmuebles`, `conceptos`, `items`, `pagos` | implemented, pushed, PR pending; branch stacked on Slice 1 (shares `data.sql`/`cleanup-test-data.sql`/data-dictionary edits) — retarget to `main` once Slice 1 merges |
| 3 | `copias`, `folios_copias`, `movimientos_testimonio` | implemented, pushed, PR pending; branch stacked on Slice 2 |
| 4 | `documentos_presentados`, `plantilla_tramites`, `plantilla_presupuestos`, `plantilla_costos_documento` | pending |
| 5 | `carpetas_tramite`, `minutas_inscripcion` | pending |
| 6 | `tramites`, `tramites_personas` | pending |
| 7 | `escrituras`, `gestiones_de_escrituras`, `folios` (verify not already aligned) | pending |
| 8 | `presupuestos` | pending |
| 9 | `testimonios`, `suplencias` | pending |
| 10 | `historial` | pending |
| final | Optional `@NamedQuery` name-string cleanup across all entities (see `proposal.md` — Out of Scope); close #973 | pending |

Slice groupings above batch low-risk leaf/mid tables together to reduce PR
count while keeping each PR's diff reviewable; split a row into smaller
PRs if its combined diff proves too large in practice — sequencing order
(not exact batching) is what `design.md` — Migration Plan actually commits to.

## Definition of Done

- [ ] Issue #973 narrowed to schema-only scope, with Acceptance Criteria updated
- [ ] Specification written and reviewed (Gate 1)
- [ ] Per-slice: `FlywaySchemaValidationIntegrationTest` proven to fail on a partial change before the full slice lands (Gate 2)
- [ ] Full suite green per slice: unit, integration, regression (E2E n/a)
- [ ] Coverage at or above the JaCoCo ratchet floor, unchanged per slice
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional per slice, referencing #973
- [ ] Each slice's PR created, CI green, review approved (Gate 4)
- [ ] All slices in §13 merged, deployed, smoke-tested; issue #973 closed only after the final slice (Gate 5)
- [ ] `traceability.md` complete from Issue through Release for every slice
