# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.1](https://github.com/matiaspakua/notaire/compare/notaire-v0.1.0...notaire-v0.1.1) (2026-10-07)


### Bug Fixes

* **security:** skip admin/admin seed outside dev/test environments ([#1305](https://github.com/matiaspakua/notaire/issues/1305)) ([7f7731e](https://github.com/matiaspakua/notaire/commit/7f7731e0fafae5a3f6a6cca6f82088d8c2349c91)), closes [#1249](https://github.com/matiaspakua/notaire/issues/1249)

## [Unreleased]

### Changed

- **BREAKING — a bare testimony is created as the copy of an existing deed (slice of #655)** (issue #655, CU07/CU08): `POST /api/v1/testimonio` rejects a body without `deed` (also `{}`) or without `deed.idDeed` with 400 naming the field, and answers 404 for an unknown deed, instead of storing a testimony that copies nothing; the contract marks `deed` required on POST (`TestimonyCreateRequest`, same fields as `DtoTestimony`). `PUT` is unchanged. The Owner chose requiring the deed over removing the endpoint: the UI generates testimonies through `POST /testimonio/generar`, and the Playwright seeds, Bruno and backend tests that use the bare create already send a deed (the Bruno `testimony-movements` fixture now seeds one). One accepted oasdiff entry under #655; the three entries of #1332, #1333 and #1334, already on `main`, are removed.
- **Local AI model registry and the Ternary Bonsai preset** (issue #1377, CU76): model presets move from hard-coded `case` blocks in `local-ai/setup-omlx-codex.sh` into `local-ai/models.yaml`, resolved by `local-ai/models_config.py`; `qwen3-coder` and `gpt-oss` resolve to the same values as before, and `ternary-bonsai` (Ternary Bonsai 2 27B, 2-bit) with the `omlx-bonsai` profile is added. Tests that need a live oMLX server or the downloaded model skip with an explicit reason.
- **BREAKING — a submitted document is created with its type and its trámite (slice of #655)** (issue #655, CU04/CU72): `POST /api/v1/documento-presentado` rejects a body without `typeId` or `procedureId` (also `{}`) with 400 naming the field, instead of storing an empty, orphan document; the contract marks both required on create. A document is presented for a trámite of a gestión: without that link it never reached the case summary, the documentation-complete check, the CU09 debt report, the gestión column of CU42 expiries or CU10 movements. `PUT` stays partial and the column stays nullable, so documents stored before this rule are untouched; the Documentos dialog requires type, gestión and trámite to create and lets a document without a trámite be linked when edited. Two accepted oasdiff entries under #655.
- **The accepted OpenAPI breaking-change list is empty and stays that way** (issue #1315, CU76): its 5 entries (4 from #655 for `/api/v1/pagos`, 1 from #596 for `GET /api/v1/historial`) accepted breaks already on `main`. They matched nothing and could only hide a later break with the same text, so they are removed. New `workspace/sdlc/check-accepted-breaking-changes.py` runs `oasdiff breaking` (JSON, no ignore list) between the base and the revision. It fails on any entry that no current breaking change matches, using the oasdiff err-ignore rule. `openapi-contract.yml` runs it on pull requests (oasdiff 1.33.0, checksum-verified) and `preflight.sh` runs it locally. An entry therefore lives only in the pull request that introduces its break, and the next pull request must remove it. The rule is in `docs/200-architecture/208-devsecops/README.md` and the list header.
- **Bruno CI step retries the CLI install** (issue #567, CU76): the Playwright E2E workflow keeps the unpinned `npx @usebruno/cli`, but it now installs the CLI first with up to 3 attempts, 60 s apart, before running the suite. Run 37697055951 on PR #1328 failed before any request ran: npm returned `ETARGET` for `tldts-core@^7.4.17`, because `tldts@7.4.17` was published about two minutes before its own `tldts-core@7.4.17`. A transient registry error like this is now retried instead of failing the suite.
- **BREAKING — a property is created and updated with its cadastral designation (slice of #655)** (issue #655, CU69/CU82): `POST /api/v1/inmueble` and `PUT /api/v1/inmueble/{id}` reject a missing, null, empty or blank `cadastralDesignation` with 400 naming the field (`{}` used to answer 201). The nomenclatura catastral identifies the property; PUT replaces every field, so it requires it too. The Inmuebles form, which already labelled it required, keeps Save disabled while it is blank. Four accepted oasdiff entries under #655.
- **Empty root `package-lock.json` removed** (issue #1285, CU76): it declared no packages and no root `package.json` exists; every Node project (`frontend`, `github-page`, `testing/e2e`) keeps its own lockfile.
- **BREAKING — `GET /api/v1/historial` is paginated (slice of #596)** (issue #596, CU13): the history list (one row per state change, the fastest-growing table) answers a Spring Data page (default 20, sorted by `idHistory`) instead of the whole table. The OpenAPI artifact documents optional `page`, `size` and `sort` query parameters (springdoc `@ParameterObject`) rather than a required `pageable` object. No frontend screen reads this list: the per-management history (`GET /api/v1/gestiones/{id}/historial`, `GET /api/v1/historial/gestion/{id}`) is unchanged. The response-type change is listed in `backend-api/openapi/accepted-breaking-changes.txt`. `HistoryPaginationIntegrationTest` and Bruno `history/03` cover it. #596 stays open for the remaining growing tables.
- **Scripts organized into modules, slice 4: `scripts/` is gone** (issue #1307, CU76, ADR-026): the 12 cross-area guards (CI workflow invariants, repo hygiene, JDK 26, semver, pipeline invariants, GHCR publish, DAST/backup assets and others) and the 5 real tests that lived in `scripts/tests/` move to `workspace/tests/`; the 17 discovery wrappers and the `test_guard_wrappers` meta-guard are deleted, since every `tests/` directory is now discovered directly by `workspace/sdlc/preflight.sh` and `sdlc-process.yml` (the `guard-wrapper-coverage` spec is rewritten for that). The backup sentinel reference points at `infra/scripts/backup-postgres.sh`. `workspace/tests/test_scripts_layout.py` fails if any file is tracked under `scripts/` or any moved path is referenced.
- **Scripts organized into modules, slice 3: SDLC and CI scripts to `workspace/`** (issue #1307, CU76, ADR-026): `preflight.sh`, `run_pipeline.sh`, `validate-sdlc-plan.sh`, the `check-agent-rules`, `check-commit-messages`, `check-sdlc-exception`, `check-tdd-evidence` and `check-heavy-ci` gates, `seed-openspec-change.sh`, `install-git-hooks.sh` and `validate-cu-api-matrix.py` move to `workspace/sdlc/`; the four report generators move to `workspace/ci/`. The never-called `validate-cu-api-matrix.sh` wrapper is deleted. Every workflow, the pre-push hook, agents, docs and the guards that built script paths from segments point at the new locations. Run `bash workspace/sdlc/preflight.sh` (the installed pre-push hook already does).
- **Scripts organized into modules, slice 2: module tools** (issue #1307, CU76, ADR-026): `export-openapi.sh`, `generate-coverage-snapshot.py` and `spotless-fallback.sh` move to `backend-api/tools/`, `fetch-user-manual.sh` to `docs/tools/`, `generate_e2e_coverage_report.py` to `testing/tools/`, `enable-gh-secure.sh` to `security/`; their guards (and the Dependabot, image-pin, prod-compose and dev-stack guards) move to the module `tests/` directories, with the Bruno sample fixture, and their `scripts/tests/` wrappers are deleted. Scripts resolve the repository root from their new depth; workflows, preflight, agents and docs point at the new paths.
- **BREAKING — `GET /api/v1/people` is paginated (slice of #596)** (issue #596, CU18/CU54/CU61): the endpoint answers a Spring Data page (`content`, `totalElements`, `size`, `number`; default 20 per page, sorted by id) instead of the whole table. `usePersonas` reads it through `apiGetPaged` like budgets; Bruno `people/02-list` and Playwright `TS-0051` assert the page shape, and `PeoplePaginationIntegrationTest` covers `size` and the default. #596 stays open for the remaining list endpoints.
- **Scripts organized into modules, slice 1: `workspace/stack/`** (issue #1307, CU76, ADR-026): `start.sh`, `stop.sh`, `logs.sh`, `start-all.sh` and `setup-pgadmin.sh` move from `scripts/` to `workspace/stack/`; every reference (docs, agents, `.aisdlc/project.yml`, compose and e2e files, guards) is updated and `workspace/tests/test_scripts_layout.py` fails on any stale path. `stop.sh`, `logs.sh` and `setup-pgadmin.sh` used their own folder as the repository root, so they only worked from `scripts/`; they now resolve the real root. Run `bash workspace/stack/start.sh` from now on.
- **Workflow-tracker concept archived out of `frontend/`** (issue #1304, CU83/CU76): `frontend/poc_motion_js/poc.md`, a Perplexity chat export that conceived the animated workflow viewer (now implemented, see `FRONTEND-WORKFLOW-TRACKER.md`), moves to `docs/000-archive/200-architecture/`; the design doc links to it and the ESLint ignore for the folder is dropped.
- **`run_pipeline.sh` keeps only the latest run** (issue #1302, CU76): it deletes the previous `reports/pipeline/<timestamp>/` folders before creating the new one, so the git-ignored folder no longer accumulates stale dashboards; `reports/` itself stays.
- **Phase 1 of #1197, slice 7: the Foreman reads the module manifest** (issue #1292, CU76, ADR-026): `workspace/modules.py` lists modules in dependency order, maps changed paths to the modules and dependents they affect, and runs each module's `verify.sh` (`list`, `affected`, `verify <module>|--all [--dry-run]`); `workspace` declares `scripts/` and `.github/` as its tooling (`extra_paths`) because the remaining guards in `scripts/` all read CI workflows, so they stay with the unifier instead of moving. `AGENTS.md` and the cloud-foreman agent document it.
- **Phase 1 of #1197, slice 6: OpenSpec moves into `docs/openspec/`** (issue #1292, CU76, ADR-026): the OpenSpec tree is part of the knowledge base. The CLI hard-codes the folder name, so `docs/` is now its root and every caller runs it from `docs/` (session hook, `foreman.sh`, `seed-openspec-change.sh`). Active paths and relative links were rewritten; archived changes, the CHANGELOG and applied Flyway migrations keep their historical text, and the links guard now skips `docs/openspec/changes/archive` and `docs/openspec/schemas` (templates link from their copy location). `workspace/tests/test_openspec_location.py` pins the location and that the CLI resolves from `docs/`.
- **Phase 1 of #1197, slice 5: docs generators and guards move into `docs/`** (issue #1292, CU76, ADR-026): `generate_erd.py` and `generate_data_dictionary.py` now live in `docs/tools/`, and the docs guards (`test_docs_links`, `test_business_docs_traceability`, `test_changelog_structure`, `test_erd_current_schema`, `test_data_dictionary_sync`, `test_generate_data_dictionary`) in `docs/tests/`, with their `scripts/tests/` wrappers removed; `docs/verify.sh` runs them and preflight plus `sdlc-process.yml` discover `docs/tests`. A new seam `database-schema` records that the generators read the live PostgreSQL schema through `psql`. `scripts/audit-api-ui.sh`, a one-off June audit nothing calls, moves to `deprecated/scripts/` for the Owner to keep or delete.
- **Phase 1 of #1197, slice 4: per-area guards move into their module** (issue #1292, CU76, ADR-026): the infra guards (`test_infra_standalone`, `test_infra_prometheus_hardening`, `test_staging_kustomize`, `test_performance_test_assets`) now live in `infra/tests/` and the testing guards (`test_testing_standalone`, `test_e2e_reliability`) in `testing/tests/`, with their CI discovery wrappers removed from `scripts/tests/`; each module's `verify.sh` runs them, and preflight plus `sdlc-process.yml` discover the new directories. The infra layout guard now allows `infra/tests/` for guards (it still forbids Playwright specs there). Cross-area guards stay in `scripts/` until the workspace slice.
- **Phase 1 of #1197, slice 3: security module** (issue #1292, CU76, ADR-026): the protect-main ruleset (desired state, apply and assert scripts) and its guard move from `scripts/` to `security/`; `security/MODULE.md` maps every security control and why GitHub-fixed ones (`SECURITY.md`, `CODEOWNERS`, `dependabot.yml`, the CodeQL/Trivy/ZAP workflows) stay where their tool reads them. The guard runs in preflight and `sdlc-process.yml`. Run `bash security/apply-protect-main-ruleset.sh` from the new path.
- **Phase 1 of #1197, slice 2: contracts between modules** (issue #1292, CU76, ADR-026): `contracts/seams.yaml` lists the six seams (HTTP API, Actuator metrics and credentials, container names, PostgreSQL exporter credentials, Flyway migrations directory) with provider, consumers, the names both sides keep and the files that rely on them; `contracts/tests/test_seams.py` fails when a name disappears from an evidence file. `contracts` joins the module manifest. The workspace and contracts guards now run in preflight and in `sdlc-process.yml` (slice 1's guard was not wired to either).
- **Phase 1 of #1197, slice 1: module manifest** (issue #1292, CU76, ADR-026): `workspace/modules.yaml` declares the seven modules (`backend-api`, `frontend`, `infra`, `testing`, `local-ai`, `docs`, `workspace`) with responsibility, fleet, verify command and dependencies; each has a `MODULE.md` (contract, seams, what it must not do) and a `verify.sh` that runs only its own build, test, format and lint; `workspace/tests/test_modules_manifest.py` fails on a missing file, unknown dependency or cycle. No production code changes.
- **Backend logs go to Loki only** (issue #1286, CU77): the backend no longer writes log files (`logging.file.*` and the Logback `FILE`/`ASYNC_FILE` appenders are removed, which stopped `backend-api/logs/` and `spring.log` from appearing); the JSON console output already scraped by Promtail is the single log path, and `LoggingConfigurationTest` fails if a file appender or `logging.file.*` key returns.
- **Login account lockout covered end to end** (issue #689, CU78): `TS-0100` drives the real login form with wrong credentials for a unique throwaway username until the backend locks it, then checks that the lockout message shows, the page stays on `/login`, a further attempt stays locked and the form has no horizontal scroll at 320 and 1024 px; the test never locks a real account and fails when the lockout cannot appear (negative control run).
- **Dead entity-to-DTO mapping deleted (slice 2a of #577)** (issue #1282, CU76): 27 `getDto`/`setAtributo(s)`/`toDto` methods on 18 `business` entities had no production caller and are removed with their tests and leftover `@JsonIgnore` annotations (the OpenAPI export is unchanged); `EntityDtoMappingRatchetTest` fails if an entity gains a mapping method and lists the 27 live ones (`src/test/resources/architecture/entity-dto-mapping.txt`) that the next slices remove. #577 stays open.
- **Updating a submitted document keeps its progress** (issue #1241, CU72): `PUT /documento-presentado/{id}` built a new document from the request, so editing erased the name, the prepared, released, flagged and reentered flags, the trámite link, the dates and the notes; it now applies only the fields the request carries onto the stored document, and recomputes the due date only when the type or the entry date is sent.
- **Controllers return records, not JPA entities (slice 1 of #577)** (issue #1279, CU76): the 14 handlers of the Folio, AuxiliaryProtocol, Notebook, DocumentCostTemplate, Procedure, Deed, Management and Budget controllers answer with response records, so a related notary carries only name, identification and registration number (no tax id, address, phone, email or birth date) and the entity internals `atributos`, `dto` and `dtoDocument` are gone; 17 entity schemas leave the OpenAPI contract and `oasdiff` reports no breaking change. `ControllerSignatureArchitectureTest` fails on an entity in a handler signature and ratchets the 69 loosely typed returns; `DtoFlag` and `DtoIdentification` are deleted. #577 stays open: entity `getDto()`/`setAtributo()` removal, repository-calling controllers and the `dto` package follow.
- **notaire-shared retired** (issue #1255, CU76): the shared DTO module had a single consumer after the Swing client was removed, so its 56 classes moved into `backend-api` under the same packages (no import or JSON change; the exported OpenAPI document is identical), the unused `SharedModuleMetrics` and `notaire_shared_version` gauge were deleted, the module left the root reactor, the Dockerfile and the tooling, and the folder is archived in `deprecated/notaire-shared/` (ADR-025). External services consume the REST API through the OpenAPI contract. `scripts/test_notaire_shared_retired.py` fails if the module comes back.
- **Toolchain on JDK 26** (issue #1276, CU76): the POM, every workflow and both backend Dockerfiles use JDK 26 with the images pinned to `maven:3.10.0-eclipse-temurin-26-alpine` and `eclipse-temurin:26.0.2.1_1-jre-alpine` (a Dependabot bump had left a floating tag that failed the #1045 guard and blocked every push); javac 26 needed one explicit generic in `AuditPointcutCoverageTest`; `scripts/test_jdk26_toolchain.py` fails if any version drifts. JDK 26 is not an LTS release.
- **Documentos linked to a trámite and visible from the gestión** (issue #773, slice; CU03/CU04/CU72): the Documentos screen sent `tipoId`, `fecha` and `entregado` and read `fkDocumentType` and `dateEntry` while the API uses `typeId`, `date`, `delivered` and `type`, so documents were stored without type or date and listed without them; the screen and hooks now follow the API, the create form can link the document to a gestión's trámite, the list returns and shows `procedureId`, and the gestión case summary lists the documents of its trámites with their status flags; E2E `TS-0099`. #773 stays open for listing the required documents automatically when a trámite is confirmed.
- **E2E gestión helper fixed** (issue #1236, CU76): `createGestionSinTramite` sent `fkIdNotaryPerson`, which the API ignores, so the gestión had no notary and could not be loaded by id; it now sends `notaryPersonId`, and `scripts/test_e2e_reliability.py` fails if any E2E source sends the stale field.

- **E2E artifacts untracked** (issue #1237, CU76): `testing/e2e/node_modules` (2925 files), the Playwright report and results and the generated `e2e-admin-token.txt` fixture were committed by mistake in #1212 and #1216 despite `.gitignore`; they are removed from the index (files stay on disk, history is not rewritten) and `scripts/test_testing_standalone.py` fails if a tracked path lives under an ignored E2E directory.

- **Gestión case summary** (issue #774, slice; CU07/CU11/CU12/CU70): `GET /gestiones/{id}/resumen-caso` returns the escrituras of a gestión's trámites with their testimonios (verified, flagged, registry state SIN_INGRESAR, INGRESADO, INSCRIPTO or RETIRADO from the latest movement) and copias, and the gestiones screen has a `Resumen del caso` dialog that also shows the financial summary (`resumen-financiero`, previously not reachable from the UI); E2E `TS-0098`, Bruno `managements/05b`. #774 stays open for creating the linked records from the gestión and the full first-case journey.
- **Create a presupuesto from a template** (issue #797, CU01/CU39): the create form has an optional procedure-type selector; saving creates the budget and loads that type's template items into it (a type without template still creates the budget and warns); the amount field explains it is the property value while the total comes from the items; E2E case added to `presupuesto-plantilla.spec.ts`.
- **Upcoming document expirations (CU42)** (issue #802, CU42): `GET /documento-presentado/proximos-vencimientos?dias=N` lists the unreleased expiring documents due within N days (1 to 365, default 30) with the CU42 data and the days remaining, and the new `/dashboard/proximos-vencimientos` screen shows them; E2E `TS-0097`, Bruno `submitted-documents/08` and `09`, CU-API matrix row updated, and the E2E spec-count guard is now a floor.
- **Reentry of a testimony captures its CU44 data** (issue #851, CU44): `POST /movimiento-testimonio/{id}/reenter` takes an optional body with the cartón number, an observed-by-registry flag and notes (notes required when observed) and stores them on the new movement (Flyway `V42` adds `observed_by_registry`); the testimony-movement screen asks for them in a dialog.
- **docs(data-model):** `Diccionario de Datos.md` regenerated from the Flyway schema by the new `scripts/generate_data_dictionary.py` (36 tables, current column names, types, nullability, defaults, foreign keys and ON DELETE actions; human descriptions kept, four missing tables added) and `scripts/test_data_dictionary_sync.py` fails CI when it drifts from the schema; the ERD CSV `Observaciones` column is repopulated from it (#1222).

- **BusinessController god class removed** (issue #900, CU76): the 5,337-line singleton had two production-reachable
  methods (identification-type name/id lookups); they became `business/IdentificationTypeLookup` (unit-tested, exact
  matching instead of `contains`), the class and its three coverage excludes were deleted, and
  `scripts/test_no_business_controller.py` keeps it removed.

- **Documentation audit** (issue #921, CU76 / CU77): `docs/300-development/DOCUMENTATION-AUDIT-2026-10.md`
  (inventory, ownership map, measured findings, prioritized roadmap); `scripts/test_docs_links.py` fails on broken
  relative links; design-system links fixed. Follow-ups: #1222 (dictionary), #1226 (license).

- **CU84 on the Use Case template, requirements CSV fixed** (issue #956, CU84): CU84 now has Referencias Cruzadas
  and GitHub ID rows; the two malformed "Login al sistema" rows in `requerimientos.csv` became one row with
  requirement issue #1224; `scripts/test_business_docs_traceability.py` guards the shape.

- **ERD artifacts regenerated for the English schema** (issue #1021, CU76): `scripts/generate_erd.py` rebuilds the
  PlantUML sources, SVG renders and relational CSV from the migrated database; a database-free guard
  (`scripts/test_erd_current_schema.py`) rejects retired Spanish table names and disagreement between artifacts.
  The dictionary's stale column names are tracked in #1222.

- **Top-level guards wired into CI** (issue #1209, CU76): every `scripts/test_*.py` guard now has a
  wrapper in `scripts/tests/` and a meta-guard fails when one is missing; the kustomize guard skips
  when `kustomize` is absent; repeated `###` headings under `[Unreleased]` merged.

- **Constitution wording for the Playwright suite** (issue #1210, CU76): §4, §5 step 15, §7 and §13
  name `testing/e2e`; no process step changed. The stale-path guard now covers `CONSTITUTION.md`.
- **Playwright UI E2E suite moved to `testing/e2e`** (issue #1192, CU76, phase 2 of #1190):
  the 52 specs, helpers, reporter and config moved from `frontend/tests/e2e` with their own
  `package.json`, lockfile, `tsconfig.json` and ESLint config; Playwright removed from `frontend/`.
  Run with `cd testing/e2e && npm test` or `bash testing/scripts/run.sh e2e`. `playwright-e2e.yml`,
  `preflight.sh`, `run_pipeline.sh` repointed (job and artifact names unchanged); the reliability rules
  moved to `scripts/test_e2e_reliability.py`.

- **BREAKING — gestión status writes require workflow transitions** (issue #804,
  CU02 / CU53 / CU16 / CU83): `PUT /api/v1/gestiones/{id}` and
  `PUT /api/v1/gestiones/{id}/complete-case` reject a changed
  `managementStatusId` / `statusManagementId` with HTTP 400; clients must use
  `POST /api/v1/gestiones/{id}/transition`. Complete-case create validates the
  initial status against workflow start nodes when a `WorkflowDefinition`
  exists. Legal next destinations remain derived from
  `GET /api/v1/gestiones/{id}/workflow-trace` (UI already filters). History
  (bitácora) from #806 is preserved on create and on successful `/transition`
  / archive. Guarded by `ManagementWorkflowStatusWriteEnforcementIntegrationTest`
  and `ManagementStatusWriteGuardTest`.

- **Dashboard/table theme tokens** (issue #960, CU76 / RF #78): replace
  hardcoded `#RRGGBB` colors in `dashboard/layout.tsx`, `dashboard/page.tsx`, and
  `components/ui/table.tsx` with `theme.colors.*` style props or semantic
  Tailwind (`text-foreground`, `text-muted-foreground`, `text-primary`,
  `text-destructive`). Table header/hover opacity preserved via
  `color-mix` from `theme.colors.neutral[100]`. Guarded by
  `hex-hygiene.test.ts`.

- **Pipeline passes on a clean `main` again** (issue #1185, CU76): 45 OpenSpec changes
  whose issues are closed were archived (delta specs folded into `openspec/specs/`);
  `scripts/seed-openspec-change.sh` no longer relies on GNU-only `sed -i` (its self-test
  failed on macOS); the `[Unreleased]` section was regrouped so each `###` heading appears
  once, with all entries preserved. Guard: `scripts/test_changelog_structure.py`.

- **JPA NamedQuery name strings Englishized** (issue #1022): rename Spanish
  `@NamedQuery(name=…)` identifiers and matching `createNamedQuery` call sites
  under `backend-api` to English entity prefixes and method tails (e.g.
  `Escritura.findByFechaEscrituracion` → `Deed.findByDeedDate`); fix latent
  `Persona.*` vs `Person.*` mismatch. Where English names collide with Spring
  Data repository methods, JPQL named *parameters* (not entity/field paths) are
  Englishized so binding still works. Schema and API unchanged. Guarded by
  `NamedQueryEnglishNamesHygieneTest`.

- **BREAKING — REST create conventions** (issue #1065, CU76 / ADR-023):
  `POST /api/v1/minutas-inscripcion` now returns `201 Created` (was `200`) with
  a `Location` header. Sample creates on `POST /api/v1/pagos` and
  `POST /api/v1/folio` also emit `Location` via shared `CreatedResponses`.
  Path renames remain phased per ADR-023 (not big-bang).

- **Repo hygiene** (issue #1050, CU76): remove global `*.txt` gitignore ban
  (keep `*.local.txt`); ignore and untrack `.serena/`; verify CODEOWNERS has
  no `frontend-swing` (#1046); relocate the ~13 MB Manual de Usuario PDF to
  GitHub Release `docs-manuals` (`scripts/fetch-user-manual.sh`); add
  ADR-022 deferring `git filter-repo` history rewrite (related #585/#682).
  Guarded by `scripts/test_repo_hygiene.py`.

- **Migrate Next.js edge interceptor to `proxy.ts`** (issue #1056, CU84):
  apply official `@next/codemod middleware-to-proxy` so
  `frontend/src/middleware.ts` becomes `frontend/src/proxy.ts` with
  `export function proxy`. Route-guard semantics, `/api/**` skip, UX
  status/role cookies, and #1051 CSP nonce headers are unchanged; `next build`
  no longer emits the middleware-convention deprecation warning.

- **ProductionCredentialsGuard** aligns with least-privilege prod compose
  (issue #1044): optional pgAdmin/Grafana/exporter credentials are skipped when
  blank/unset, while literal `admin` is still rejected when those services are
  configured. Optional property defaults in `application.properties` are empty.

- **Frontend ESLint is a blocking CI gate** (issue #1048, CU76):
  `.github/workflows/frontend-ci.yml` no longer runs `npm run lint` with
  `continue-on-error: true` (obsolete #701 advisory). Local
  `scripts/preflight.sh` MAP documents the same blocking semantics
  (`eslint src --max-warnings=0`). jsx-a11y remains enabled via
  `eslint-config-next/core-web-vitals`. Guarded by
  `scripts/tests/test_frontend_eslint_blocking.py`.

- **Bruno API suite in English and idempotent** (issue #1035, CU76):
  `backend-api/api-test/` folders, files, requests, tests, variables and
  test data are now English (following the backend domain names); the
  environment is renamed `Developmen` → `Development` (CI and
  `scripts/preflight.sh` updated). Every fixture uses per-run unique values
  and is deleted by the suite, so consecutive runs against the same
  database pass (164 requests / 291 tests, twice) without leaking rows.
  Each request cites its Use Case(s) and RF(s) in a `Traceability:` line.
- **Hexagonal architecture pilot on the payment/budget slice** (issue #984,
  CU15/CU47, [ADR-021](docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md)):
  the financial workflow was restructured into Ports & Adapters — `domain.payment`
  (no Spring, no JPA), `application.port.in/out.payment`, `application.usecase.payment`,
  `adapter.in.web.payment` and `adapter.out.persistence.payment`. `PaymentController`
  is now a thin inbound adapter over use-case ports, `@Transactional` moved from the
  controller to the use cases, and `PUT /api/v1/pagos/{id}` binds a `PaymentUpdateRequest`
  record instead of the JPA entity. `PaymentService`, `StatusPayment`,
  `BudgetResumenService` and `PaymentMapper` were deleted rather than deprecated;
  `BudgetController`, `ManagementArchiveDebtService` and
  `ManagementResumenFinancieroService` consume the inbound ports. The `AuditAspect`
  pointcut was widened to `adapter.in.web..*Controller` so the business audit trail
  follows the controller. **No behavior change**: URLs, request/response payloads,
  status codes, persisted data and DB schema are identical, and the pattern is
  deliberately scoped to this one slice pending review.

- **Flyway Integration**: Migrated from init-db scripts to Flyway versioned migrations
  - Scripts moved to `backend-api/src/main/resources/db/migration/`
  - V1: Initial schema (24 tables)
  - V2: Initial reference data and admin user

### Fixed

- **List pages are usable on phones** (issue #1356, RNF-05/RNF-06): at 390px the shared `DataTable` showed three columns and pushed the edit and delete buttons off-screen with no scroll affordance, and search inputs were clipped. Below 768px every `DataTable` (29 pages) now renders each row as a card: the identifying field as the title, the other columns as label/value pairs and the actions as a visible 44px button row; desktop keeps the table. The search inputs on personas, escrituras and presupuestos are full width on phones. `data-table-mobile.test.tsx` and Playwright `TS-0109` cover it.
- **DELETE endpoints return 204 No Content as documented (slice of #1315)** (issue #1315, CU15/CU13 and the other CRUD use cases): 17 `DELETE` handlers (`/pagos/{id}`, `/historial/{id}`, `/copia`, `/documento-presentado`, `/tipo-folio`, `/item`, `/gestiones`, `/estado-gestion`, `/tipo-identificacion`, `/tramites`, `/inmuebles`, `/sustitucion`, `/testimonio`, `/movimiento-testimonio`, `/workflow-definition`, `/workflow-node`, `/workflow-transition`) answered 200 with an empty body while the OpenAPI contract documented 204. They now answer 204. `DELETE /roles/usuarios/{idUser}` already answered 204 but was documented as 200; the contract now documents 204 (accepted oasdiff entry under #1315; no response changes). `DeleteStatusMatchesContractTest` compares the documented and returned success status of every `@DeleteMapping`. The frontend needs no change: `apiDelete` only checks `res.ok`. Bruno delete requests assert 204. `DELETE /plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}` stays 200, documented and implemented consistently.
- **Report PDFs work again, generated in-house from the current schema** (issue #567, CU01, CU03, CU09, CU13, CU42): the budget, budget-with-properties, procedure documents, management history, document expiry and document debt reports (`/api/v1/reportes/presupuesto/{id}`, `presupuesto-inmuebles/{id}`, `lista-documentos-tramite`, `historial-gestion/{id}`, `documentos-por-vencer/{id}`, `consultar-deuda-documentos`) were JasperReports templates querying the legacy MySQL schema and always answered 500. `ReportDocumentFactory` now builds them from the current domain (budget total and balance from the CU47 summary) and the new `ReportRenderer` port renders them with Apache PDFBox 3.0.8. A missing budget, procedure type, management, submitted document or management number is a 404; OpenAPI documents 200/400/404/500 for the six endpoints. The frontend document debt report sent `numeroGestion` instead of `numberManagement` and always got 400.
- **Paged endpoints document their real paging parameters (#596)** (issue #596, CU74): `GET /api/v1/people`, `/presupuestos`, `/escrituras`, `/tramites` and `/gestiones` were documented with one *required* `pageable` query object, which is not what clients send. They now use springdoc `@ParameterObject`, so the OpenAPI artifact lists optional `page`, `size` and `sort` with their defaults, like `/historial` and `/audit-log`. Runtime behaviour is unchanged. `oasdiff`: 0 breaking errors; 5 warnings (`request-parameter-removed` for the never-usable `pageable` object); `accepted-breaking-changes.txt` untouched. `PagedEndpointsOpenApiParametersIntegrationTest` guards all 7 paged endpoints.
- **Submitted document requests validated at the boundary (slice of #655)** (issue #655, CU04): `POST /api/v1/documento-presentado` and `PUT /api/v1/documento-presentado/{id}` answer 400 for a `date` that is not a real `yyyy-MM-dd` day (it used to be silently dropped, and the shared lenient `SimpleDateFormat` accepted `2026-02-30` and was not thread-safe) and 404 for an unknown `typeId` or `procedureId` (an unknown type was ignored and, on update, an unknown procedure unlinked the document). `PUT` on a stored document no longer fails with 500 (lazy `DocumentType` read outside a transaction, `java.sql.Date.toInstant`), and the due date is computed in calendar days. `SubmittedDocumentRequestValidationIntegrationTest` and Bruno `submitted-documents/10`–`11` cover it; the OpenAPI artifact documents the rules and the new 400/404 responses without schema tightening (`oasdiff`: no breaking changes). #655 stays open for the remaining controllers.
- **Management status no longer loads its whole history (slice of #595)** (issue #595, CU13): `ManagementStatus.historyList` was an EAGER, unbounded `@OneToMany`, so loading any status, or any history row, management or procedure that references one, pulled every history row of that status. It is now `LAZY`; the only reader (history deletion) already runs in a transaction. `ManagementStatusHistoryFetchIntegrationTest` pins it. #595 stays open for the other 28 EAGER mappings.
- **Payment requests validated at the boundary (slice of #655)** (issue #655, CU15): `POST /api/v1/pagos` requires `idBudget` and a positive `amount`, and `PUT /api/v1/pagos/{id}` rejects a non-positive `amount`, through `@Valid` bean validation; the update used to answer 404 for `amount <= 0`. `PaymentControllerTest` and Bruno `payments/08a` cover it, and the OpenAPI artifact now documents the constraints. **Contract tightening:** `oasdiff` reports these 4 changes as breaking (`idBudget`/`amount` required on POST, `exclusiveMinimum: 0` on `amount` for POST and PUT), but no request that used to succeed is rejected, so they are accepted in the new `backend-api/openapi/accepted-breaking-changes.txt`, which `openapi-contract.yml` and `scripts/preflight.sh` pass to `oasdiff --err-ignore`; any break not listed there still fails the gate. #655 stays open for the remaining controllers.
- **Release Please can open its release PR** (issue #1264, CU76): `release-please.yml` uses the default `GITHUB_TOKEN`, relying on the repository setting that allows GitHub Actions to create pull requests, and `release-please-config.json` sets `bootstrap-sha` to the #1043 commit so pre-release merge commits are not parsed. A PR opened by `GITHUB_TOKEN` triggers no other workflow, so `docs/300-development/RELEASE.md` tells the Owner to close and reopen the release PR to run the required checks.
- **No `admin/admin` seed outside dev/test** (issue #1249, CU78/CU84): `DataInitializer` skips creating the initial admin user and logs an error when `APP_ADMIN_PASSWORD` is blank or `admin` and `app.environment` is not `development`, `dev`, `local` or `test`; `DataInitializerTest` covers both paths. ADR-019 and `.env.example` document the rule. The legacy Flyway `V2` seed is out of scope.
- **check-sdlc-exception.sh false failure** (issue #1228, CU76): the diff is captured before matching, so `grep -q`
  closing the pipe can no longer SIGPIPE `git diff` and report a PR with an OpenSpec change as having none.
- **DeedManagement / Person DTO mapping null-safety** (issue #853, CU76):
  `DeedManagement.getDto()`, `getDtoNotary()`, and `setAtributos()` tolerate null
  management status, notary, and identification type without NPE (return/omit
  null instead of 500); `Person.getDto()` tolerates null identification type and
  null `DeedManagementList`; id/full constructors initialize empty procedure and
  history lists like the default constructor. Expanded
  `DeedManagementEntityTest` + `PersonEntityTest` coverage for all cited paths.

- **Plain gestión notary assignment consults active Substitution** (issue #805,
  CU22 / CU02): residual after #836 — `POST`/`PUT /api/v1/gestiones` now call
  `ManagementSubstitutionService.resolveNotary` (same as complete-case) and
  append a redirection note when an active substitution covers the requested
  notary. Integration coverage for plain create/update; TS-0092 toast detects
  the English note marker.

- **Person identification uniqueness enforced at the database** (issue #799, CU17, CU18):
  Flyway `V40` adds unique index `uq_people_identification_type_number` on
  `people (fk_id_tipo_identificacion, identification_number)`, with a fail-fast pre-check when
  duplicate groups already exist. Aligns with the #835 service-layer reject (HTTP 409); race
  `DataIntegrityViolationException` on that key is remapped to `DuplicatePersonException` so the
  existing frontend toast path keeps working. JPA `@UniqueConstraint` mirrors the index.

- **`estado-actual` returned an arbitrary row when history dates tied** (issue #1198, CU13):
  `GET /api/v1/gestiones/{id}/estado-actual` now breaks date ties by the higher history id, so
  a create and an update in the same millisecond no longer show the previous status. This was
  also the cause of an intermittent failure in `ManagementHistorialOrphanWriteIntegrationTest`.

- **SubmittedDocument DocumentType mapping and getDto null-guard** (issue #801,
  CU72): replace the Integer `@Column` for `fk_id_document_type` with a
  `@ManyToOne DocumentType documentType` association; fix
  `DocumentType.submittedDocumentCollection` `mappedBy` and drop cascade-all on
  that collection. Legacy `getDto()` null-guards optional procedure (and
  nullable Boolean flags) so null-procedure documents no longer NPE. No Flyway
  change (column already present since V29).

- **Gestión History orphan status writes** (issue #806, CU13 / CU02 / CU53):
  plain `POST`/`PUT /gestiones` and `PUT .../complete-case` now append History
  via `ManagementBitacoraService` when status is first set or changes (no row
  when create has no status). `GET .../estado-actual` falls back to the entity
  status when History is empty (404 if management missing or status null).
  Bitácora UI remains TS-0028 / `useHistorial` on gestiones.

- **Frontend i18n page coverage** (issue #1059, CU76 / ADR-015): wire remaining
  dashboard gap pages (workflows list/editor, roles, suplencias, reportes,
  items) and login leftovers (connection/lockout/validation/welcome/forgot/footer)
  through next-intl catalogs; extend `i18n.test.ts` required-key gate; TS-0040
  asserts EN titles on roles and workflows.

- **`GET /testimonio/{id}` and `GET /movimiento-testimonio` failed when deed was null**
  (issue #953, CU07/CU08/CU12): `Testimony.fkIdDeed` was `@ManyToOne(optional =
  false)`, so Hibernate INNER JOINed `deeds` and findById/list missed valid rows
  with a null FK; set `optional = true` to match the nullable column.

- **Persona form swallowed non-409 backend validation** (issue #945, CU17/CU61):
  present create/update errors via `presentPersonaSaveError` so HTTP 400
  messages (e.g. blank identification) appear in toast and FormField errors;
  keep curated localized 409 duplicate-document UX; stabilize Dedup-EDGE in
  TS-0015.

- **Suplencias/Reportes unreachable from navigation; duplicate admin pages**
  (issue #1058, CU22/CU59/CU24/CU25/CU50/CU23): add sidebar + dashboard home
  entries for `/dashboard/suplencias` and `/dashboard/reportes`; merge richer
  admin Items UI into canonical `/dashboard/items`; redirect
  `/dashboard/administracion/{items,auditoria}` to canonical routes; E2E
  discovers modules via sidebar (`nav-*` test ids) instead of deep `goto`.

- **Frontend Vitest branch coverage floor undercut on main** (issue #976, CU76):
  root cause was an aspirational 6% branch floor (2026-07-29) later briefly
  undercut (5.85% in 2026-09) as coverage denominators grew; subsequent unit
  tests restored branches above 6%. Re-measured on `main` @ `68dc2cac`
  (Statements 15.09% / Branches 10.52% / Functions 11.97% / Lines 15.55%) and
  raised raise-only floors to 14 / 9 / 10 / 14 with ~1pp headroom; documented
  policy in `code-quality.md` + frontend testing guides; guard test
  `vitest-coverage-thresholds.test.ts`.

- **CI bots no longer commit reports to main** (issue #1041, CU76):
  `ci.yml` / `cd.yml` / `playwright-e2e.yml` report jobs publish via
  `actions/upload-artifact` + `$GITHUB_STEP_SUMMARY` (and optional Pages
  mirror under `/cicd-reports/`); they no longer `git commit`/`git push` into
  `docs/wiki/cicd-reports/`. That path is gitignored and untracked; report
  jobs drop `contents: write` (CD `release` keeps write for GitHub Releases).
  Guarded by `scripts/test_no_bot_report_commits.py`.

- **CD publishes the CI-tested SHA, not tip of main** (issue #1042, CU76):
  `.github/workflows/cd.yml` `build-and-publish` checks out
  `workflow_run.head_sha` (fallback `github.sha` for tag/dispatch), tags the
  image with that explicit publish SHA, and moves `latest` only after the
  SHA-tagged push succeeds. Non-success CI still skips publish. Guarded by
  `scripts/test_cd_pin_tested_sha.py`.

- **Weekly k6 load-test script restored** (issue #1047, CU74/CU76):
  `performance-test/k6/load-test.js` is back for the English login DTO
  (`name`/`password`), covers gestiones/presupuestos/tramites with Bearer JWT,
  enforces CU74 thresholds (p95 ≤ 2000ms, `http_req_failed` rate &lt; 1%), and
  writes `summary.json` for the Performance workflow artifact. Asset unittest
  `scripts/test_performance_test_assets.py` guards the contract; upload step
  no longer ignores a missing summary.

- **Icon-only dashboard buttons expose accessible names** (issue #1057, CU76 /
  WCAG 2.1 SC 4.1.2): seventeen edit/delete/resumen icon Buttons on personas,
  escrituras, pagos, presupuestos, and administración (usuarios, roles,
  conceptos, documentos, trámites) now set translated `aria-label`s so screen
  readers and Playwright `getByRole('button', { name })` can identify them.
  Guarded by a static unit inventory test and E2E `TS-0096`; CU21 edit flows in
  `TS-0016` are unskipped.

- **CRUD screens show backend validation messages** (issue #1054, CU15/CU20):
  mutation failures on ~15 dashboard pages use a shared `presentMutationError`
  helper so users see the API `message`/`error` text (400/404/409/422/500) in
  toasts instead of generic “error al guardar/eliminar” copy. When the body
  includes bean-validation style `field: msg` detail that matches a form
  control, the matching `FormField` shows the error and the control is marked
  `aria-invalid`. Authenticated 401 remains on the session-expiry path (#1053).
  Covered by unit tests and Playwright `TS-0095` (focused #615 slice).

- **Admin screens blocked for non-admin users** (issue #1052, CU78): navigating
  to `/dashboard/administracion/**` without an admin-capable role redirects to
  `/dashboard?forbidden=1` with an access-denied message (edge proxy +
  administración layout). Login sets a non-credential `notaire-auth-role`
  cookie for the edge check. Covered by unit tests and Playwright `TS-0094`.
  Backend RBAC remains #559; HttpOnly token migration shipped in #1051.

- **Expired sessions redirect to login with a clear message** (issue #1053, CU84):
  when an authenticated API call returns HTTP `401`, the Next.js client clears
  local auth state and navigates to `/login?expired=1`, showing that the session
  expired. Login credential failures and non-401 errors do not trigger this path.
  Covered by unit tests and Playwright `TS-0093` (also closes the E2E gap in #690).

- **Monetary amounts use `BigDecimal` / `NUMERIC` (issue #1061, CU15):**
  entities, shared DTOs, payment domain (`BudgetCharges`, status/summary),
  ports, controllers, and receipt PDF formatting no longer use `float`/`Float`
  for money. Flyway `V39` converts remaining `real` money columns to
  `NUMERIC(19,2)` (percentage `NUMERIC(7,4)`). Unit tests cover exact cent
  sums (`10.10 + 20.20`) and overpayment against an exact tenths balance
  (`0.1 + 0.2` vs `0.3`). Layout floats (`WorkflowNode` positions) unchanged.
- **E2E suite no longer collides across workers or leaks seed rows**
  (issue #1037, CU76): `uniqueId()` gives each Playwright worker its own
  residue class, so parallel workers never send the same `E2E<n>` document
  number (`409` on seed). The first-case tutorial now links its case to its
  own quote by client name; picking the "last" option linked it to the
  seeded budget (the list is sorted newest first), so global teardown could
  not delete the seed budget and persona. Teardown failures now log the
  response body.
- **Concurrent case creation no longer fails on duplicate folder numbers**
  (issue #1038, CU85): procedure folder numbers came from `max(number) + 1`,
  so two cases opened at the same time got the same number and the second
  `POST /api/v1/gestiones/complete-case` failed with `400` on
  `uq_carpeta_tramite_numero`. They now come from the
  `procedure_folder_number_seq` sequence (Flyway `V38`), started after the
  highest existing number.
- **Budget-template DELETE is persisted** (issue #1036, CU39/CU49):
  `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}`
  returned 200 but the row survived, because the cascade-ALL parent lists
  re-persisted it on flush.
- **`POST`/`PUT /api/v1/tramites` lost persisted Deed/Property/DeedManagement/
  Budget state on nested FK references** (issue #981, CU82): the endpoint
  accepted the raw `Procedure` entity, so a request like `{"fkIdDeed":
  {"idDeed": 96}}` deserialized into a transient placeholder instead of
  re-fetching the real, persisted row — silently corrupting every FK
  association passed this way and blocking CU82's minuta-de-inscripción
  golden path, which reads the linked Deed's real status. Both endpoints
  now accept a plain-id `ProcedureRequest` (`idProcedureType`, `idProperty`,
  `idDeed`, `idManagement`, `idBudget`, `notes`), matching the pattern
  already used by `RegistrationDraftController`, and resolve each id
  against its own repository before persisting (`idProcedureType` required,
  `400` if missing; `404` if a provided id does not resolve). **Breaking**
  for any client still sending the old nested-object shape — no production
  UI caller existed yet, only test fixtures, updated in the same change.
  Also fixes two more regressions from the domain-schema-to-English rename
  (#973) found while verifying this fix end-to-end: `Procedure.java`'s
  `@JoinTable` for `personList` still declared the old table name
  `tramites_personas` (renamed to `person_procedures` in Slice 6's `V31`),
  silently breaking every `DELETE /api/v1/tramites/{id}` with a `409`
  (`relation "tramites_personas" does not exist`); and `Notebook.java`'s
  `number`/`notes` fields still mapped to the old column names
  `numero`/`observaciones` (renamed in Slice 1's `V26`), breaking `POST
  /api/v1/cuadernos` with a `500` and CU80's cuaderno-creation flow
  entirely. A systematic sweep cross-checking every entity's mapping
  against the live schema confirms no further mismatches remain.
- **Bruno API test suite audit uncovered four silent-delete/write defects**
  (issue #952, CU76): `Item.fkIdPresupuesto` was annotated `@JsonIgnore`,
  which blocks the field on both read and write — `POST/PUT /api/v1/items`
  silently dropped the budget FK instead of persisting it; changed to
  `@JsonProperty(access = WRITE_ONLY)`. Separately, `DELETE` on
  `/api/v1/historial/{id}`, `/api/v1/items/{id}`, `/api/v1/pagos/{id}`, and
  `/api/v1/tramites/{id}` silently no-op'd for rows loaded fresh from the
  database: Spring Data's default `isNew()` infers "new" from a primitive
  `@Version` field of `0`, which is indistinguishable from an
  already-persisted row that was never updated — `Historial`, `Item`,
  `Pago`, and `Tramite` now implement `Persistable<Integer>` with an
  explicit `isNew()`. `historial` delete had a second, independent cause:
  Hibernate's cascade processing on the stale, eagerly-fetched
  `EstadoDeGestion.historialList` collection silently cancelled the direct
  delete unless the entity was first unlinked from it. Also migrated
  `InmuebleController` off the legacy `InmuebleJpaController` onto
  `InmuebleRepository`, completed the `historial`, `items`, `pagos`,
  `tramites`, and `inmueble` Bruno folders with full CRUD lifecycles, and
  renamed `auth/` to `00-auth/` so its login/rate-limit fixtures run first.
  Full suite now at 149 requests / 266 tests passing
  (`backend-api/api-test/COVERAGE.md`).
- **Silent-delete `isNew()` bug extended to 30 more entities** (issue #957):
  the same root cause behind #952's `historial`/`items`/`pagos`/`tramites`
  fix — Spring Data's default `isNew()` misreading a primitive `@Version` of
  `0` as "new" — was present on every other surrogate-key entity (`Rol`,
  `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`,
  `EstadoDeGestion`, `WorkflowDefinition`/`Node`/`Transition`, `Persona`,
  `Usuario`, `Escritura`, `GestionDeEscritura`, `Presupuesto`, `Testimonio`,
  `Cuaderno`, `Folio`, `Inmueble`, `MinutaInscripcion`,
  `MovimientoTestimonio`, `DocumentoPresentado`, `RegistroAuditoria`,
  `Suplencia`, `Concepto`, `Copia`) and on the 5 `@EmbeddedId`
  composite-key join entities (`FoliosCopias`, `PlantillaCostoDocumento`,
  `PlantillaPresupuesto`, `PlantillaTramite`, `TramitesPersonas`), where
  `DELETE` would return `200`/`204` but silently leave the row in place. All
  30 now implement `Persistable<Integer>` or `Persistable<XxxPK>` (composite
  keys use a `@Transient boolean isNew` flag flipped by `@PostLoad`/
  `@PrePersist`, since a client-assigned `@EmbeddedId` is never null). Also
  verified the two riskiest `EAGER`+`CascadeType.ALL` cascades
  (`Concepto.plantillaPresupuestoList`, `Presupuesto.pagoList`) correctly
  cascade-delete their children now that `isNew()` is fixed.
- **`CheckboxField` click target excluded the gap between the input and its label**
  (issue #930, CU08): the shared `CheckboxField` pattern (`frontend/src/theme/form-patterns.tsx`)
  rendered a wrapping `<div>` with a separate `<input>` and `<label htmlFor>`, leaving the
  `theme.spacing[3]` gap between them with no click handler — clicks landing there (e.g. on the
  element's bounding-box center) did nothing. Caused
  `TS-0031-testimonio-generacion-verificacion-feature.spec.ts`'s "Observado" checkbox test to
  silently fail to toggle state. Fixed by wrapping the input and label text in a single native
  `<label>` element, so any click within the row toggles the checkbox.
- **`TS-0012-escritura-folio-firma.spec.ts` regressed after the #892 folio-picker fix
  shipped** (issue #892, CU06): the test chained `.locator("button").first()` off
  `getByTestId("select-folio-escritura")`, but that testid is applied directly to the
  Radix `SelectTrigger` button, so the nested lookup never resolved. Also closed the
  Radix listbox (rendered in a portal above the dialog) with `Escape` before clicking
  "Cancelar", since a still-open dropdown intercepted that click. Test-only fix, no
  application code changed.
- **`tipo-documento-vencimiento-config.spec.ts` checkbox locator regression** (issue #928):
  the spec chained `.getByRole("checkbox")` off a `getByTestId(...)` that already resolves
  to the checkbox element itself, so the nested role lookup timed out. Test-only fix.
- **No way to justify a numbering gap when creating/editing an escritura** (issue #950,
  CU86): `NumeracionEscrituraService` already accepted a non-blank `observaciones` value
  as justification for a non-sequential `numero`, but the escritura form had no
  `observaciones` field and `handleSave`'s catch block discarded the backend's specific
  `SaltoNumeracionSinJustificarException` message behind a generic "error saving" toast.
  Added the missing `observaciones` `FormField` to `/dashboard/escrituras` and now surface
  `extractApiError(err)` in the toast.

- **Flaky E2E coverage for the pagos presupuesto picker/saldo pendiente display**
  (issue #796, CU15): `TS-0014-pagos-saldo-picker.spec.ts` located the saldo
  pendiente text via an ambiguous `getByRole("dialog").getByText(/saldo|pendiente/i)`,
  which could also match the picker's loading placeholder or a seeded persona
  surname containing "Saldo", causing intermittent strict-mode failures. Added a
  dedicated `data-testid="saldo-pendiente-amount"` to the saldo display in
  `pagos/page.tsx` and updated the spec to target it directly; also fixed two
  assertions that compared the raw input amount against the locale-formatted
  currency string, and aligned a `select-persona` option click with the
  `evaluate(el => el.click())` workaround already used elsewhere in the suite
  for that Radix dropdown.

- **`02-demo-two-full-cases.spec.ts` broke after the presupuesto picker landed**
  (issue #796): the demo script's Pago step still filled a `presupuesto id`
  text field that the picker (see above) removed. Updated it to drive
  `select-presupuesto-pago` and pick the option by client surname, same as
  `TS-0014-pagos-saldo-picker.spec.ts`.

- **Archiving a gestión with pending debt was incorrectly blocked (HTTP 400)**
  (issue #914, CU16): commit 52776cc9 (issue #169) changed `GestionArchiveDebtService.archivar`
  to reject the request outright when `saldoPendiente > 0`, contradicting CU16's documented
  behavior — archiving should succeed and persist `deudaPendienteAlArchivar=true`, warning the
  user without blocking. Broke `GestionArchiveIntegrationTest` deterministically. Reverted to
  the warn-not-block behavior; the "Verificar deuda pendiente" (RF-22) rule is now enforced
  purely as a pre-confirmation warning via `GET /gestiones/{id}/saldo-pendiente`.

- **Creating a `Pago` against the seeded test presupuesto returned 400 instead of 201**
  (issue #914): `backend-api/src/test/resources/data.sql`'s seed presupuesto had no
  `monto_inmueble`, so `PagoService.procesarPago`'s saldo-pendiente validation (issue #848)
  always computed a saldo of `$0.00`, rejecting any payment against it. Added a
  `monto_inmueble` value to the seed row large enough to cover the payments asserted in
  `BusinessWorkflowIntegrationTest` and `RemainingControllersIntegrationTest`.
- **`PersonaController` create/update leaked unhandled 500s on non-duplicate persistence errors**
  (issue #912): PR #905 (#835) removed the generic `catch (Exception e) -> 409` fallback from
  `createPersona`/`updatePersona`, leaving only the `PersonaDuplicadaException` branch. Restored
  the fallback so any other persistence failure surfaces as 409 instead of an unhandled 500.
- **Payments exceeding a presupuesto's saldo pendiente returned a generic 400 instead of a
  specific 409** (issue #848, CU15): `PagoService.procesarPago` already rejected overpayments
  but duplicated the saldo calculation inline and threw a plain `IllegalArgumentException`,
  which `PagoController` mapped to `400 Bad Request` like any other validation error —
  indistinguishable from malformed input. Refactored to reuse the existing
  `calcularSaldoPendiente` method and introduced `SaldoPendienteExcedidoException`, mapped to
  `409 Conflict` in both `POST /pagos` and `POST /pagos/params`. The `/dashboard/pagos` form
  now shows a specific "saldo excedido" message on 409 instead of the generic save-error toast.

- **`CheckboxField` double-toggled on every click, making checkboxes appear unresponsive**
  (issue #839): the shared `CheckboxField` component in `theme/form-patterns.tsx` had both
  a wrapper `onClick={() => onChange(!checked)}` and the native `<input onChange>` firing on
  the same click, so `onChange` ran twice and canceled itself out. Discovered while testing
  the new Cuadernos de Folios screen. Removed the redundant wrapper handler; the native input
  already handles clicks and label-triggered toggles. Fixes checkbox interaction across every
  page that uses `CheckboxField` (testimonios, documentos, personas, tramites,
  documentos-entidades-externas, cuadernos).

- **Gestión form's presupuesto picker showed only `Presupuesto #{id}`, no client identity**
  (issue #889, CU02): `/dashboard/gestiones`'s nueva-Gestión modal rendered each presupuesto
  option as a bare id, even though `Presupuesto.persona` has been returned by the API since
  #883 and is already typed on the frontend — a user had no way to tell which presupuesto
  belonged to the client they were working with. Discovered while driving the real UI
  end-to-end to seed a demo case. Fixed by rendering `fullName(p.persona)` and
  `formatCurrency(p.monto)` alongside the id when a client is associated, falling back to the
  id-only label otherwise. Frontend-only change, no API/schema change.

- **Presupuesto creation/edit from the UI silently dropped the client association** (issue
  #883, CU01): `PresupuestoController.create`/`.update` bound directly to the raw
  `Presupuesto` JPA entity, whose client relation field is `fkIdPersona`, while the
  frontend (and `DtoPresupuesto`) send/expect `persona`. Jackson silently dropped the
  unknown `persona` key on write, and `GET` responses returned `fkIdPersona` instead of
  `persona` on read — so every Presupuesto created or edited from `/dashboard/presupuestos`
  lost its client link, and the apellido search (CU60) couldn't find it either. Fixed by
  adding `@JsonProperty("persona")` to `Presupuesto.getFkIdPersona()`/`.setFkIdPersona()`,
  mirroring the existing `@JsonProperty("monto")` alias already used on the same class for
  `montoInmueble`. No entity/DTO/schema change.

- **BREAKING: `Inmueble.valuacionFiscal` type mismatch blocked all Inmueble creation** (issue
  #879, CU69): `Inmueble.valuacionFiscal` (and `DtoInmueble.valuacionFiscal`) was declared
  `String` while the Flyway-owned `inmuebles.valuacion_fiscal` column is `real`; Hibernate
  always bound it as VARCHAR, so every `POST /api/v1/inmueble` failed against the real
  Postgres schema with `ERROR: column "valuacion_fiscal" is of type real but expression is
  of type character varying`, regardless of value (H2-based tests didn't enforce this,
  hiding the bug). Changed both fields to `Float` (matching the existing
  `Presupuesto.montoInmueble` convention) and updated the Next.js Inmueble form
  (`/dashboard/inmuebles`) to send/parse a number instead of a string. `valuacionFiscal`
  is now a JSON number, not a string, in both the request and response body. Investigating
  this surfaced an unrelated, pre-existing NPE on `PUT /api/v1/inmueble/{id}`
  (`InmuebleJpaController.edit`, `tramiteList` null), tracked separately as issue #880.

- **Payment method (`metodoPago`) was collected by CU15 but never persisted** (issue #792,
  CU15): `PagoController.procesarPago` accepted `metodoPago` in the request body but
  `PagoService`/`Pago` had no field to store it, so the value was silently dropped. Added
  nullable `pagos.metodo_pago` column (`V16` migration), threaded `metodoPago` through
  `Pago` (entity, `getDto()`/`setAtributos`), `DtoPago`, and `PagoService.procesarPago`/
  `editarPago`, so it now round-trips on create, edit, and retrieval.

- **BREAKING: Contradictory Presupuesto↔Tramite cardinality resolved** (issue #798):
  `Presupuesto` and `Tramite` declared foreign keys to each other —
  `presupuestos.fk_id_tramite` (`Presupuesto.fkIdTramite`) and
  `tramites.fk_id_presupuesto` (`Tramite.fkIdPresupuesto`) — but only the latter was
  ever written by the live modern path (`GestionController.applyTramiteDependencies`,
  CU02); `Presupuesto.fkIdTramite` was set only by the deprecated `ControllerNegocio`
  god class and its `PresupuestoJpaController`/`TramiteJpaController` helpers. Removed
  `Presupuesto.fkIdTramite` (field, getter/setter, `DtoPresupuesto.tramite` and its
  accessors) and every legacy call site that wrote it, leaving the single, consistent
  relation: one Presupuesto has many Tramites, each Tramite belongs to at most one
  Presupuesto (`Presupuesto.tramiteList` / `Tramite.fkIdPresupuesto`). Flyway `V14`
  drops `presupuestos.fk_id_tramite`, refusing to run if any row still holds a
  non-null value (data-loss guard); paired `R14` rollback script restores the column.
  Consumers of `DtoPresupuesto`'s removed `tramite` field must migrate to reading a
  Tramite's own `fkIdPresupuesto` instead. Discovered mid-implementation that
  `frontend-swing` (~6 Swing screens) also read/wrote this field; per existing
  project-wide direction to deprecate that module rather than invest in it, it is now
  excluded from the root Maven reactor (`pom.xml`) and CI (`ci.yml`,
  `scripts/preflight.sh`) instead of migrated.

- **Dashboard sidebar overflowed horizontally below 768px** (issue #699): `AppSidebar` rendered
  as a fixed 288px-wide `<aside>` with no responsive behavior, leaving no room for content at
  mobile widths (e.g. 320px). It now collapses to a hamburger-triggered off-canvas drawer below
  the `md` (768px) breakpoint, with a backdrop that dismisses it; desktop behavior (static,
  always visible) is unchanged. Un-skips the `test.fixme()` this issue tracked in
  `tests/e2e/mobile-viewport.spec.ts` and adds two more covering the drawer open/close cycle
  and the desktop no-hamburger case.

- **Dead `X-Notaire-User` header removed from the frontend** (issue #678): the backend's
  `AuditoriaAspect` has attributed audit records from the verified JWT identity
  (`SecurityContextHolder`), not from any client-supplied header, since #555 — but
  `frontend/src/lib/api-client.ts` kept sending `X-Notaire-User` on every request anyway, with
  no effect. Removed the dead header and its `actingUser()` helper, and corrected `CLAUDE.md`/
  `infra/README.md`, which still described audit attribution as header-based.

- **postgres-exporter reused the app's own admin DB credentials** (issue #675): `infra/docker-compose.yml`'s
  `postgres-exporter` service connected with `POSTGRES_USER`/`POSTGRES_PASSWORD` — the same
  credentials as the application itself — so a compromised metrics exporter had full read/write
  access to every application table. Added Flyway migration `V12` creating a dedicated
  `notaire_exporter` role granted only `pg_monitor` (PostgreSQL's built-in read-only statistics
  role), wired through `POSTGRES_EXPORTER_USER`/`POSTGRES_EXPORTER_PASSWORD` in both
  `docker-compose.yml` and `infra/docker-compose.yml`, and extended `ProductionCredentialsGuard`
  to reject a default exporter password in production. Grafana anonymous auth (also part of this
  finding) was already disabled by #672. `sslmode=disable` on the exporter connection is
  unchanged — `notary-postgres` has no TLS configured, so flipping it now would break the
  connection outright; tracked separately by #684.

- **Prometheus ran as root with the host's docker.sock mounted** (issue #674): `infra/docker-compose.yml`
  gave the `prometheus` container `user: root` plus a read-write bind mount of
  `/var/run/docker.sock`, even though `prometheus.yml` only ever scrapes static targets (no
  `docker_sd_configs`) — a compromised container had a direct path to full host compromise for
  no operational benefit. Removed both; the image's built-in non-root user is sufficient.

- **Wildcard `Authorization` header in CORS config allowed credential theft** (issue #673):
  `SecurityAndCorsConfig` hardcoded `.allowedHeaders("*")`, silently ignoring the existing (but
  unwired) `cors.allowed-headers` property, so any origin could read the `Authorization` header
  back via a CORS preflight response. Wired the property through with an explicit default
  (`Content-Type,Authorization`), and added a startup guard that refuses to boot
  in production if `cors.allowed-headers` or `cors.allowed-origins` still resolve to `*`.

- **Dead `X-Notaire-User` left in the `cors.allowed-headers` default** (issue #731): #673's
  default (above) originally included `X-Notaire-User`, which #678 had already removed from the
  frontend (and #555 from the backend's audit attribution) — an unused allow-listed header that
  no client actually sends. Dropped it from both `SecurityAndCorsConfig`'s `@Value` default and
  `application.properties`; no functional change.

- **Swagger/OpenAPI publicly accessible in production** (issue #671): `SecurityAndCorsConfig`
  now denies `/swagger-ui/**`, `/swagger-ui.html`, and `/v3/api-docs/**` when
  `app.environment=production` (the same signal `ProductionCredentialsGuard` already uses),
  while leaving them reachable in dev/test. Previously any anonymous visitor could enumerate
  the entire API surface and execute live requests via Swagger's "Try it out" in production.

- **Default credentials committed to version control** (issue #672): `infra/grafana/grafana.ini`
  no longer hardcodes `admin_user`/`admin_password` in plaintext — Grafana now gets its admin
  credentials exclusively from `GF_SECURITY_ADMIN_USER`/`GF_SECURITY_ADMIN_PASSWORD`
  (already wired to `GRAFANA_ADMIN_USER`/`GRAFANA_ADMIN_PASSWORD` in `.env`).
  `ProductionCredentialsGuard` now also rejects default pgAdmin and Grafana credentials in
  production, not just backend/DB/actuator/app-admin. `.env.example` defaults are explicitly
  marked as insecure placeholders that must change before a production run.

- **Order-dependent H2 integration test failures** (issue #661): `RepositoryIntegrationTest`
  (base for `GestionDeEscrituraRepositoryIntegrationTest`, `PagoRepositoryIntegrationTest`,
  `RegistroAuditoriaRepositoryIntegrationTest`) and `EstadoDeGestionReferentialIntegrityTest`
  were the only integration test bases without `@Transactional`, so their inserts committed
  permanently to the shared H2 instance instead of rolling back per test. Under
  `runOrder=alphabetical`, this broke `WorkflowTransitionIntegrationTest` and
  `WorkflowTraceApiH2IntegrationTest` when they ran later. Both classes now get
  `@Transactional`, matching every other integration test base in the suite.

- **Contradicting JaCoCo coverage-floor numbers** (issue #588): `CLAUDE.md`/`code-quality.md`
  said the enforced floor was 28%/14%, `pom.xml`'s own comment said ~78%/~62%, and the actually
  enforced `<minimum>` values were 70%/25% — three different numbers for the same gate. Also
  fixed a dead exclusion path referencing the nonexistent `com/licensis/notaire/servicios/*`
  package (real package is `service`), which meant `AdministradorJpa`/`AdministradorReportes`/
  `AdministradorSesion`/`AdministradorValidaciones`/`Conexion` were silently counted in the
  coverage gate instead of excluded as intended. Re-measured real coverage after the fix:
  ~84% line / ~74% branch. Added `JacocoCoverageConfigConsistencyTest` to guard against this
  drifting out of sync again.

- **Login attempt double-counting** (found while implementing #560): `UsuarioController.login()`
  fell through to its "usuario no encontrado" branch even when a username **was** matched but
  the password was wrong or the account was inactive, executing that branch's logic in addition
  to the matched-user branch. Fixed by returning immediately once a matching user has been
  handled.

- **E2E coverage reports were a fabricated static template** (issue #587): the
  `.github/workflows/playwright-e2e.yml` "Business Coverage Report" job wrote an identical
  hardcoded markdown block to `docs/wiki/cicd-reports/e2e-coverage-*.md` on every run,
  including a stale action item ("Fix backend 500 errors on testimonio endpoints") that had
  sat unchanged for 6+ weeks regardless of actual results. Replaced with
  `scripts/generate_e2e_coverage_report.py`, which parses the real Playwright JSON reporter
  output (`test-results/results.json`) and Bruno CLI JSON output (`bruno-results.json`) to
  report actual pass/fail/skip counts and the actual failing test titles, with no fabricated
  numbers when a results file is missing.

- **`generate_e2e_coverage_report.py` crashed on the real Bruno CLI output** (issue #658,
  found on #587's own first real CI run): `_bruno_section()` assumed `bruno-results.json` is a
  JSON object with a top-level `"summary"` key; the real `@usebruno/cli` output is a JSON array
  of per-iteration objects, each with its own `"summary"`. Verified the fix against the actual
  artifact from the failing job rather than guessing the schema a second time, and added a
  regression test using that real (trimmed) sample. Also found and fixed the reason Playwright
  results always showed "not found": `playwright-e2e.yml`'s "Run Playwright E2E tests" step
  passed `--reporter=html,json,junit` on the CLI, which fully overrides
  `playwright.config.ts`'s own `reporter` array — silently dropping the `outputFile` paths this
  script depends on, and the custom `tests/e2e/reporters/coverage-report.ts` business-coverage
  reporter entirely. Removed the CLI override so the config's reporters (which already have the
  correct paths) take effect. Bruno failures are now also surfaced in the report's Action Items
  section, not just Playwright ones — the discovery run had 137 failing Bruno tests silently
  reported as "no action items" before this fix.

- **Flyway single source of truth**: Removed dual schema source (init-db + Flyway)
  - Removed `init-db:/docker-entrypoint-initdb.d` volume mount from docker-compose.yml
  - PostgreSQL now starts empty; Flyway applies all V1→V11 migrations on startup
  - Created V11 migration to fix `conceptos.version` for "Documentacion" (id=3)
  - Rewrote `BaseIntegrationTest.java` to enable Flyway instead of copying init-db scripts
  - Renamed `InitDbSchemaValidationIntegrationTest` → `FlywaySchemaValidationIntegrationTest`
  - Archived `init-db/` directory to `docs/archive/init-db/`
  - Updated all documentation, agent configs, and `.claude/rules/database-migrations.md`
  - See `.claude/rules/database-migrations.md` for new migration workflow

- Updated Docker Compose to remove init-db volume mounts
- Configured Spring Boot to use Flyway with `spring.flyway.*` properties

### Added

- **Guard: every REST endpoint has a UI consumer or an allowlist reason** (issue #1250, CU76, CONSTITUTION §4): `contracts/tests/test_api_reachability.py` scans `frontend/src` (tests and comments excluded) for API paths, method-aware for the `api-client` helpers, and fails on any OpenAPI endpoint the UI does not call unless `contracts/api-reachability-allowlist.yaml` lists it with a reason; stale entries (endpoint removed or now called) fail too. The allowlist starts with the 53 endpoints unreferenced today, the 10 from #1250 plus 43 the method-aware scan adds, all marked for triage; #1250 stays open for that triage.
- **Workflow tracker post-signing reingreso loop (strategy b)** (issue #841,
  CU83 / CU06 / CU07 / CU11 / CU44): seed `ManagementStatus` 11–13 and replace
  Firmada→Inscripta on the standard workflow with Generado → Ingresado →
  Retirado; `GET .../workflow-trace` returns additive `testimonyMovements` with
  derived `returnedObserved`; dashboard `WorkflowTracker` shows a secondary
  movement timeline and reingreso badge on the inscription node.

- **Document type enabled and returned on admin form** (issue #800, CU27 / CU32 /
  CU04 / CU72): residual catalog fields after #837 — create/edit checkboxes for
  `enabled` (default true) and `returned` (default false); `DtoDocumentType` and
  entity mapping round-trip `returned`; create no longer overwrites an explicit
  `enabled=false`. Vitest + Playwright + `DocumentTypeReferentialIntegrityTest`.

- **`testing/` prepared as a standalone QA repository, phase 1** (issue #1191, CU76 / CU75;
  umbrella #1190): one runner (`testing/scripts/run.sh integration|database`, `test.sh` kept as the
  stable entry point) replaces nine overlapping scripts; a new black-box **database V&V suite**
  starts an empty PostgreSQL 16.15, applies the application's Flyway migrations with the Flyway
  12.4.0 CLI and checks history, configuration, the V12 exporter role, seed data, schema, renamed
  tables, idempotence and tamper detection, with its own `database-vv.yml` workflow and a matching
  `preflight.sh --full` gate. The cURL suite moved to `testing/integration/` and honours
  `BASE_URL`; the stack smoke script, previously never run, now works on macOS and asserts
  authorization. New guides under `testing/docs/` (preparation, configuration, definition,
  operation). Removed: `run-all-tests.sh`, `scripts/test-all.sh`, `scripts/run-comprehensive-tests.sh`,
  the duplicate root `generate-coverage-report.sh` and the committed 2026-04 reports. k6 stays in
  `infra/`; Playwright moves in phase 2 (#1192). Guard: `scripts/test_testing_standalone.py`.

- **Configurable dev stack ports and container names** (issue #1186, CU76): host ports
  (`POSTGRES_PORT`, `BACKEND_PORT`, `PGADMIN_PORT`, `FRONTEND_PORT`) and container names
  (`NOTAIRE_<SERVICE>_CONTAINER_NAME`) in `docker-compose.yml` are overridable from `.env`
  so parallel stacks can coexist; defaults unchanged and `scripts/start.sh` follows the
  configured ports. Guard: `scripts/test_dev_stack_isolation.py`. The observability stack
  only supports the default names.

- **`infra/` prepared as a standalone repository** (issue #1179, CU77; related #302):
  observability stack moved to `infra/observability/`, Kustomize and the reverse-proxy
  config to `infra/deploy/`, k6 to `infra/performance/`; `deploy/` and `performance-test/`
  removed. `nginx.conf` now has one source (`infra/deploy/kustomize/base/nginx.conf`) shared
  by `docker-compose.prod.yml` and the generated Kubernetes ConfigMap. Infra scripts are
  self-contained (`infra/scripts/common.sh`, `infra/.env.example`); compose project name
  pinned to `infra` so existing volumes survive. New infra guides under `infra/docs/`
  (preparation, configuration, definition, operation); `docs/` links to them. Stale
  `infra/tests/e2e` and `infra/CREDENTIALS.md` removed (folded into the guides).
  Guard: `scripts/test_infra_standalone.py`. Moved paths are listed in the PR.

- **ADR-023 REST resource naming** (issue #1065, CU76): English resource nouns
  matching established `/api/v1` paths, plural collections, `/search`, action
  sub-resources, and `201`+`Location` for creates; ADR-003 remains versioning-only.

- **CU-API-MATRIX English refresh + CI validator** (issue #1064, CU76): rename
  22 stale Spanish controller class names to current `adapter.in.web` English
  types; add missing resources `/carpetas`, `/cuadernos`, `/minutas-inscripcion`,
  `/plantilla-costos-documento`, `/protocolo-auxiliar`, `/roles`,
  `/tipo-identificacion`, `/tramites` (CU80–CU82/CU85 + inventory rows);
  normalize `Bruno_Test` (paths/`MISSING`/`N/A`, `#953` on gaps — no new Bruno
  fills); add `scripts/validate-cu-api-matrix.py` with unittest coverage, wired
  into `scripts/preflight.sh` and `sdlc-process.yml`.

- **E2E feature-gap skip tracker hygiene** (issue #1146, CU76): Vitest guard
  requires `#\d+` on the fourteen static `test.skip`s in TS-0014/16/17/20 and
  locks the inventory count; `E2E-TEST-MAPPING.md` lists each skip with owning
  CU (product work stays on those CUs; #1146 is citation hygiene only).

- **Staging Kustomize deploy manifests** (issue #901, CU77): `deploy/kustomize/`
  base + `overlays/staging` mirroring `docker-compose.prod.yml` (postgres,
  backend, frontend, reverse-proxy; no pgAdmin); ClusterIP data plane; Secret
  placeholders only; GHCR SHA image tags; static guard
  `scripts/test_staging_kustomize.py`. CD (`cd.yml`) remains publish-only —
  no fake cluster deploy. Docs: `209-deployment`, `DEPLOYMENT-PLAN`, CU77, SAD §11.

- **DAST, OpenAPI contract, and backup/restore CI gates** (issue #1067, CU76/CU78/CU75):
  weekly OWASP ZAP baseline (`dast-zap.yml`, warn-first report artifact; Trivy retained);
  committed `backend-api/openapi/openapi.yaml` + PR `openapi-contract.yml` (export freshness
  via `scripts/export-openapi.sh`, breaking diffs via `oasdiff`);
  `backup-restore-smoke.yml` skips with an explicit #256 block until
  `scripts/backup-postgres.sh` exists. Guarded by `scripts/test_dast_contract_backup_assets.py`.

- **Bruno API coverage for sixteen previously uncovered controllers** (issue #953,
  CU76): OpenCollection folders for roles, workflows (+ validate), copies,
  submitted-documents, testimonies, testimony-movements, managements, notebooks,
  auxiliary-protocol, procedure-folders, document-cost-templates, reports
  (representative PDFs), and registration-drafts (404/action surface); suite now
  297 requests / 508 tests, idempotent double `bru run`; docs/matrix updated.

- **Frontend GHCR publish + semver releases** (issue #1043, CU76): CD matrix
  publishes `ghcr.io/<owner>/notaire/frontend` from `frontend/Dockerfile`
  with CycloneDX SBOM, cosign sign, and SBOM attest (parity with backend);
  release-please automates `v*` tags / GitHub Releases, rolls Keep a Changelog
  sections, and bumps root/module Maven versions plus `frontend/package.json`
  from the tag. CD `release` job attaches SBOM assets only (no duplicate
  release notes). Runbook: `docs/300-development/RELEASE.md`. Guarded by
  `scripts/test_frontend_ghcr_publish.py` and
  `scripts/test_semver_release_process.py`.

- **Production docker-compose** (issue #1044, CU78/CU75):
  `docker-compose.prod.yml` with postgres + backend + frontend + nginx reverse
  proxy; no pgAdmin; reverse-proxy-only host ports; `${VAR:?}` required secrets;
  `ENVIRONMENT=production`; least-privilege backend env; Flyway
  baseline-on-migrate off. Guarded by `scripts/test_prod_compose.py`. Deployment
  guide and CU78/CU75 updated. TLS remains #254; backups remain #256.

- **CodeQL and GitHub Security Lab baseline** (issue #1135, CU78):
  `.github/workflows/codeql.yml` analyzes Java, JavaScript/TypeScript, and
  GitHub Actions on pull requests and on `main`. Dependabot also watches
  `frontend/`. `SECURITY.md` points at private vulnerability reporting.
  `scripts/enable-gh-secure.sh` is the admin step for secret-scanning push
  protection and Dependabot security updates; branch protection stays opt-in.
- **STRIDE threat model for authentication & audit trail** (issue #1028):
  added `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md`
  with numbered security requirements (`SR-01`..`SR-10`) and an explicit
  Mitigated/Partial/Open status for each threat against the login and
  audit-trail subsystems, replacing the informal risk register for those
  two areas. Documentation only, no behavior change.
- **Vincular escritura a folio y validar copia de testimonio ya inscripto**
  (issue #838, CU87): `POST`/`PUT /api/v1/folio` accept an optional
  `escrituraId`; linking sets the folio's `estado` to `Utilizado` and
  rejects (`409`) linking a folio already `Utilizado` by a different
  escritura (re-saving the same escritura is idempotent). `POST
  /api/v1/copia` now rejects (`409`) creating a copia when its source
  testimonio has a `MovimientoTestimonio` with `inscripta = true`. The
  folios admin screen lets the Escribano pick an unlinked, `Firmada`
  escritura when creating/editing a folio, and the escrituras screen
  shows the linked folio.
- **Suplencias con efecto práctico en la asignación de gestiones** (issue
  #836, CU22/CU48/CU51): a `GestionDeEscritura` created or edited
  (`POST`/`PUT .../complete-case`) for an escribano with an active
  `Suplencia` (fecha de la gestión dentro de `fechaInicio`–`fechaFin`) is
  now redirected to the suplente automatically, leaving a trace in
  `observaciones` and a toast notification on the gestiones screen. The
  personas screen now exposes a "Registro de escribano" field
  (alta/edición) so any Persona can be enabled as a suplente.
- **Cargar presupuesto desde plantilla y catálogo de ítems** (issue #834,
  CU39/CU71): the presupuestos screen now offers, per presupuesto, a
  dialog to (1) pick a `TipoDeTramite` and load its `PlantillaPresupuesto`
  conceptos as ítems in one step (`POST
  /api/v1/presupuestos/{id}/items-desde-plantilla?tipoTramiteId=X`, 400 if
  the tipo de trámite has no plantilla), and (2) add copies of existing
  catalog `Item`s to the presupuesto (`POST
  /api/v1/presupuestos/{id}/items-desde-catalogo`). Replaces the previous
  free-text `montoInmueble` field as the only way to price a presupuesto.
- **Recibo de pago en PDF** (issue #23, CU15/RF-21): new
  `GET /api/v1/reportes/recibo-pago/{idPago}` endpoint generates a PDF recibo
  (cliente, fecha, concepto(s), total abonado) for an existing pago, 404 if
  not found. Wired to an "Emitir recibo" action on the pagos screen.
- **Minuta de Inscripción** (issue #839, CU82): generate a minuta de
  inscripción from a signed (`Firmada`) escritura and track it through the
  registry circuit — `Generada` → `Presentada` → `Observada`/`Inscripta`.
  Generation is blocked until the escritura's inmueble has its datos
  registrales complete (`matricula`, `tomoFolioFinca`, `linderos`, added to
  `Inmueble` alongside the existing catastral data). Adds
  `POST/PUT /api/v1/minutas-inscripcion/**` endpoints, a
  `GET /api/v1/reportes/minuta-inscripcion/{id}` PDF report for the
  normalized inscription form, and a "Minutas de Inscripción" dashboard
  screen to generate, present, observe and inscribe.
- **Administrar carpetas de trámite** (issue #839, CU85): iniciar un
  trámite genera automáticamente su carpeta de trámite (una por trámite,
  numeración única, estado "Activa"). Adds `CarpetaTramite` entity and
  `GET /api/v1/carpetas/{id}`, `GET /api/v1/carpetas?gestionId=&tramiteId=`,
  `PUT /api/v1/carpetas/{id}/espera` (requires a `motivo`). Archiving a
  gestión (CU16) now cascades to all its carpetas, transitioning them to
  "Archivada"; if any carpeta is still "Espera" unresolved, the archive
  request is rejected (HTTP 409) unless explicitly confirmed
  (`?confirmado=true`). Adds a "Ver carpetas" action to the gestiones
  screen.
- **Vencimiento y responsable en tipos de documento** (issue #837, CU27, CU32,
  CU42): a `TipoDeDocumento` can now declare `vence`, `diasVencimiento`
  (required when `vence` is checked), and `quienEntrega`. When a
  `DocumentoPresentado` is created, these fields — plus a computed
  `fechaVencimiento` (`fechaIngreso + diasVencimiento`) — are copied from its
  `TipoDeDocumento`, giving CU42's "próximos vencimientos" report real data to
  work with. Adds a "Vence"/"Días de vencimiento"/"Quién entrega" section to
  the Tipos de Documento admin form.
- **Costos de documentos en el presupuesto** (issue #823, CU27/CU39): the
  cost (`importeAPagar`) of a `DocumentoPresentado` in a trámite is now
  included in its presupuesto's total. Adds `PlantillaCostoDocumento`
  (`POST`/`GET /api/v1/plantilla-costos-documento`) so a `TipoDeTramite`'s
  presupuesto template can define an expected fixed or variable
  (percentage) cost per `TipoDeDocumento` — exactly one of the two must be
  set. Adds a "Costos de Documentos" section to the plantillas de
  presupuesto admin screen.
- **Descuentos y recargos en ítems de presupuesto** (issue #822, CU45/CU71): a
  new `Item.tipo` field (`NORMAL`/`DESCUENTO`/`RECARGO`, default `NORMAL`)
  plus a required `motivo` when the type is `DESCUENTO` or `RECARGO`,
  validated both client- and server-side. `PagoService`'s total calculation
  now subtracts `DESCUENTO` items and adds `RECARGO` items. Adds
  `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos` to list
  only discount/surcharge items for a presupuesto, and a "Tipo"/"Motivo" UI
  on the ítems admin screen with a descuentos/recargos report section.
- **Estado de pago por presupuesto** (issue #821, CU15, CU47): adds
  `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`, returning
  `SIN_PAGOS`, `PARCIAL`, or `SALDADO` derived from the existing saldo
  pendiente calculation. Additive change — existing pago/saldo endpoints are
  unchanged. The Pagos screen now shows an estado badge next to the saldo
  pendiente for the selected presupuesto.

- **Protocolo Auxiliar** (issue #839, CU81): marking a `TipoDeFolio` as
  `esAuxiliar` lets escribanos start a new `Escritura` directly on an available
  Protocolo Auxiliar folio (a folio of that type not yet linked to an
  `Escritura`), without opening a `CarpetaTramite`/`Tramite` — the ágil
  circuit of CU81. Numbering is `MAX(Escritura.numero) + 1` scoped to
  auxiliar folios only, independent from Protocolo Principal's sequence.
  Adds `GET /api/v1/protocolo-auxiliar/folios-disponibles` and
  `POST /api/v1/protocolo-auxiliar/escrituras`, a "Protocolo Auxiliar"
  checkbox on the existing Tipos de Folio admin form, and a new
  Protocolo Auxiliar screen under Protocolo.

- **Cuadernos de Folios** (issue #839, CU80): allows escribanos to group folios
  into cuadernos of exactly 10 strictly consecutive folios belonging to the same
  registro notarial, assigning a sequential number per year/escribano and marking
  the folios as `Asignado a cuaderno`. Damaged or annulled folios (`Errose`,
  `no pasó`) require an `observaciones` justification. Adds
  `GET/POST /api/v1/cuadernos` and `GET /api/v1/cuadernos/{id}/caratula` (PDF via
  JasperReports) with a new `Cuadernos` screen under Protocolo.

- **Persona duplicate-document validation** (issue #835, CU17, CU18): creating or
  editing a `Persona` with a `numeroIdentificacion` already registered to another
  person is rejected with `409 Conflict` (`PersonaDuplicadaException`), whose body
  now carries `idPersonaExistente` so the frontend can offer a direct link to the
  existing person. The personas page shows a toast with a "Ver persona existente"
  action and preserves the in-progress form data instead of discarding it.

- **Folio picker in Escritura form** (issue #892, CU06): Allows users to assign a folio
  to an escritura before signing. Unblocks E2E demo completion and enables full workflow
  validation (Escritura Firmada → Testimonio → Verificación → Inscripción → Retiro).
  Prerequisite for CU09–CU12. Backend already supported `Escritura.folios[]` field;
  this change wires the UI selector (`select-folio-escritura`) and submission payload.

- **Workflow engine y bitácora conectados al flujo real de gestión** (issue #833,
  CU13, CU16, CU83): `POST /api/v1/gestiones/{id}/transicionar`
  (`GestionTransitionService`) valida cada cambio de estado de una gestión contra
  el `WorkflowDefinition`/`WorkflowNode`/`WorkflowTransition` de su tipo de trámite
  (CU83) y rechaza transiciones no permitidas por el grafo. `GestionArchiveDebtService.archivar`
  ahora delega esa misma validación de transición (destino "Archivada") antes de
  archivar. Cada alta, transición válida y archivado registra una entrada en la
  bitácora vía `GestionBitacoraService`, expuesta en `GET /api/v1/gestiones/{id}/historial`
  (CU13). La pantalla `/dashboard/gestiones` agrega las acciones "Cambiar estado"
  (selector limitado a los destinos válidos del workflow) y "Ver bitácora", y
  muestra el mensaje de rechazo cuando una transición no está permitida. New
  Playwright specs (`gestion-cambiar-estado.spec.ts`, `gestion-bitacora.spec.ts`)
  cover the golden path, invalid-transition and viewport edge cases.

- **Circuito legal posterior a la firma de escritura: testimonio, inscripción y retiro**
  (issue #832, CU06, CU07, CU08, CU11, CU12, CU44): `POST /api/v1/escrituras/{id}/firmar`
  transitions a "Sin Firmar" escritura with folio(s) assigned to "Firmada"
  (`EscrituraFirmaService`). `POST /api/v1/testimonios/{id}/generar` and
  `.../verificar` (`TestimonioGeneracionVerificacionService`, migration `V17`) generate
  a testimonio from a firmada escritura and record verification (observado/no
  observado + motivo); `GET /api/v1/reportes/testimonio/{id}/copia` issues the printed
  copy (JasperReports) only for verified testimonios. `MovimientoTestimonioService`
  adds the Registro de la Propiedad circuit: `ingresar-inscripcion`,
  `registrar-inscripcion`, `retirar` and `reingresar`, each validating the required
  preconditions (verificado, movimiento abierto, inscripto, retirado) and returning
  404 for a non-existent testimonio. New Playwright specs cover firma, generación/
  verificación de testimonio and the movimiento-de-inscripción circuit.

- **Pago ↔ presupuesto ↔ gestión financial summary exposed end-to-end** (issue #820,
  CU-47, CU-02, RF-21): `Pago.getPresupuesto()` is no longer `@JsonIgnore` — payment
  responses now go through `DtoPagoResponse` (via `PagoMapper`) and include the
  associated `idPresupuesto`. Added `GET /api/v1/presupuestos/{id}/resumen`
  (`PresupuestoResumenService`) returning the gestión número/encabezado, presupuesto
  total, saldo pendiente, and the full payment list for a presupuesto — 404 for an
  unknown id. Added `GET /api/v1/gestiones/{id}/resumen-financiero`
  (`GestionResumenFinancieroService`) aggregating total presupuestado, total cobrado
  and saldo across every presupuesto reachable through a gestión's trámites. The CU47
  "Ver resumen" dialog on `/dashboard/presupuestos` calls the presupuesto-scoped
  endpoint and shows total, saldo and the payment table without extra navigation
  (`usePresupuestoResumen` in `frontend/src/hooks/usePresupuestos.ts`).

- **Pending-debt verification when archiving a gestión** (issue #819, CU-16, RF-22, RF-37):
  archiving a gestión now aggregates the pending balance (`PagoService.calcularSaldoPendiente`)
  across all `presupuesto`s reachable through its `tramite`s, exposes it via
  `GET /api/v1/gestiones/{id}/saldo-pendiente`, and records whether debt was outstanding at
  archive time on the gestión (`gestiones_de_escrituras.deuda_pendiente_al_archivar`, migration
  `V15`) via `POST /api/v1/gestiones/{id}/archivar`. The gestión screen surfaces a non-blocking
  debt warning in the archive confirmation dialog. New `GestionArchiveDebtService` in
  `backend-api`; new archive action + `useSaldoPendiente`/`useArchivarGestion` hooks in
  `frontend/src/app/dashboard/gestiones/page.tsx`.

- **AUTH-001 HTTP/integration test gaps closed: rate limiting, wrong password, expired token**
  (issues #685, #686, #687): `backend-api/api-test/auth/` gained a chained rate-limit test
  (5 failed logins against a per-run-randomized username, then asserts the lockout response)
  and a tightened wrong-password test; `JwtAuthIntegrationTest` gained an expired-token test
  using a genuinely expired, validly-signed JWT generated via the real `JwtTokenService`. Along
  the way, corrected two of the issues' own assumptions against verified real behavior: the
  login endpoint never returns 401 for bad credentials or 423 for lockout — it returns
  `200 {valido:false}` and `429 {valido:false, message}` respectively (see
  `docs/05-api/ERROR-HANDLING-STRATEGY.md`), matching the already-passing
  `LoginRateLimitIntegrationTest`/`JwtAuthIntegrationTest`. No production code changed.

- **Status-aware login error messages** (issue #756): the login page previously showed the
  same generic "can't connect to server" toast for a 429 account lockout, a genuine network
  failure, and (separately) invalid credentials, even though the backend already returns a
  distinct status and `message` field for the lockout case. Added `ApiError` (status + body) to
  `frontend/src/lib/api-client.ts`; the login page now reads the 429 response's `message` and
  shows it instead of the generic error. `frontend-swing`'s `Login.java` has the identical
  (worse) problem — documented in place rather than fixed, since propagating the HTTP status
  through that Swing/`RestClient` call chain is a larger change than this issue scoped.

- **Case-insensitive username and JWT structure HTTP tests** (issues #692, #693): closed two
  gaps in the AUTH-001 HTTP/Bruno test coverage. `backend-api/api-test/usuarios/09-13-*.yml`
  verifies `POST /api/v1/usuarios/login` treats a username the same regardless of case
  (lowercase/uppercase/mixed), matching `UsuarioController`'s existing `equalsIgnoreCase`
  lookup. `14-16-*.yml` verifies the returned `token` is a well-formed
  `header.payload.signature` JWT whose decoded payload has `sub` (the username) and a future
  `exp` claim, matching `JwtTokenService.generateToken()`. No production code changed.

- **k6 load-test suite** (issue #594): no performance/load testing existed anywhere in the
  repository. Added `performance-test/k6/load-test.js`, covering the highest-traffic read
  endpoints (`gestiones`, `presupuestos`, `tramites`) with baseline thresholds (`p(95)<500ms`,
  error rate `<1%`), authenticating via the existing JWT login endpoint. Wired into a new
  scheduled (weekly, not per-PR) `.github/workflows/performance-test.yml` job so it doesn't
  gate every PR.

- **Security response headers on the Next.js frontend** (issue #562): `frontend/next.config.ts`
  had no `headers()` callback. Added `Content-Security-Policy`, `X-Frame-Options: DENY`,
  `X-Content-Type-Options: nosniff`, and `Strict-Transport-Security` to every route.

- **HistorialMapper unit tests** (issue #589): `service.mappers.HistorialMapper` had zero test
  coverage despite not being excluded from the JaCoCo gate. Added `HistorialMapperTest` covering
  the happy path and each nullable foreign key individually.

- **Login rate limiting / account lockout** (issue #560): `POST /api/v1/usuarios/login`
  now locks a username out for a configurable duration (`security.login.lockout-duration-ms`,
  default 15 minutes) after a configurable number of consecutive failed attempts
  (`security.login.max-attempts`, default 5), returning `429 Too Many Requests`. A
  successful login resets the counter. Implemented in the new
  `com.licensis.notaire.security.LoginAttemptService`.

- **Jakarta Bean Validation on request boundaries** (issue #561): `UsuarioController`'s
  `createUsuario`/`updateUsuario` now validate the request body (`@NotBlank nombre`, `tipo`),
  and `ReporteController`'s path/query parameters are validated (`@Positive` on ID-like
  parameters, `@NotBlank` on `nombreTipoTramite`, `@Min(1)/@Max(12)` on `mes`), returning a
  clean `400` instead of a generic `500` or silently accepting malformed input.
  `GlobalExceptionHandler` now handles `MethodArgumentNotValidException` (body validation) and
  `ConstraintViolationException` (`@RequestParam`/`@PathVariable` validation) consistently.
  Full rollout across the remaining controllers is tracked as a follow-up.

- **Mobile/tablet viewport coverage in Playwright E2E** (issue #610): `frontend/playwright.config.ts`
  previously only ran against `devices["Desktop Chrome"]`, with zero specs asserting layout at
  the 320px/768px/1024px breakpoints mandated by `.claude/rules/ui-ux-design.md`. Added a
  `mobile` project (`devices["iPhone SE"]`) and `tests/e2e/mobile-viewport.spec.ts`, which
  asserts no horizontal overflow on the login page at 320px and 768px and on the dashboard at
  320px after login.

- **ADR-007**: Database Schema Versioning with Flyway
  - Added architecture decision record for Flyway implementation
  - Documented migration strategy and best practices

- **SAR-007**: Flyway Implementation Solution Architecture Report
  - Detailed technical analysis and implementation plan
  - Testing strategy for database migrations
  - AI Agent Guidelines section

- **Flyway Skill for AI Agents**: `.claude/skills/flyway/SKILL.md`
  - Comprehensive guide for implementing Flyway migrations
  - Examples, best practices, and common patterns
  - Project-specific conventions and templates

- **Database Migrations Rules**: `.claude/rules/database-migrations.md`
  - Mandatory rules for all database changes
  - Anti-patterns to avoid
  - Rollback strategies and emergency procedures

- **Database Migrations README**: `backend-api/src/main/resources/db/migration/README.md`
  - Quick reference for developers
  - Common patterns and templates
  - Testing and validation commands

### Deprecated

- `init-db/01-schema.sql` - Superseded by Flyway migration
- `init-db/02-data.sql` - Superseded by Flyway migration

### Removed

- **JasperReports** (issue #567): the `jasperreports` 3.5.3 dependency, the six `.jasper` templates, their byte-identical copy under `backend-api/resources/reportes/` and the unused `.jrxml` files for CU24/CU25/CU50. The CU24/CU25/CU50 endpoints stay placeholders and are no longer described as Jasper templates.
- **Orphaned test scripts** (issue #585, CU76): deleted nine cURL scripts under
  `testing/integration/http/` that no runner called (`01-auth` … `08-items`, `test-all-endpoints.sh`)
  and the unused `COMPOSE_FILES` constant in `test_image_pins_and_dependabot.py`. New guard: a
  reachability check for `testing/` scripts. `deprecated-src.old/` is deliberately kept as
  historical data.

- **Swing E2E leftovers** (issue #811, CU76 / ADR-012): durable retirement of
  Robot Swing E2E — hygiene fails if `e2e-swing.yml` or Maven `-pl frontend-swing`
  / `deprecated-frontend-swing` returns in workflows; `testing/e2e-swing/`
  hard-deprecated in place; live setup/testing docs no longer teach Swing
  build/run. Active UI E2E remains Playwright.

- **BREAKING — unused payment params create** (issue #1065, CU76):
  `POST /api/v1/pagos/params` removed (no UI/Bruno callers). Use
  `POST /api/v1/pagos` (JSON body).

### Security

- **Tomcat and Jackson patched past their HIGH/CRITICAL CVEs** (issue #1327, CU78): Spring Boot 4.1.1, the latest release, still manages `tomcat-embed-core` 11.0.24 (CRITICAL CVE-2026-65182 security-constraint bypass, CVE-2026-65905 authentication bypass, CVE-2026-68525 unauthorized access via FORM auth) and Jackson 2.21.5 / 3.1.5 (HIGH CVE-2026-68497, -89407, -89425, -91776, -91777). The root `pom.xml` overrides Boot's own version properties and stays on each managed line: `tomcat.version` 11.0.26, `jackson-2-bom.version` 2.21.7, `jackson-bom.version` 3.1.7. `trivy fs` now reports no HIGH or CRITICAL finding for `backend-api`. `PatchedDependencyFloorTest` reads the version each library reports and fails below the fixed release on its line, so the floor holds after Boot catches up and the overrides are removed. The commons-collections, commons-beanutils and itext findings were already gone with JasperReports (#567). The API is unchanged.
- **Vulnerable report libraries removed** (issue #567): dropping JasperReports 3.5.3 (2009) also removes its transitive itext 2.1.0, bcprov-jdk14 136, commons-collections 2.1 and commons-beanutils 1.8.0. Report PDFs are written by Apache PDFBox 3.0.8 (Apache License 2.0) with text-only content.
- **Management history writes are ADMIN-only** (issue #1250, CU13/CU78): `PUT` and `DELETE /api/v1/historial/{id}` used to accept any authenticated user, so an employee could rewrite or delete the audit trail of state changes. They now require `ROLE_ADMIN` (403 otherwise); reading and recording history are unchanged. `HistoryWriteAuthorizationIntegrationTest` and Bruno `rbac/08a`–`08b` cover it, the OpenAPI artifact documents the 403, and `API-AUTHENTICATION-GUIDE.md` lists the rule. `contracts/api-reachability-allowlist.yaml` now records the Owner's triage of the 53 UI-unreachable endpoints (buckets A–E).
- **Logout revokes the JWT server-side (slice of #676)** (issue #676, CU84): a token copied before logout stayed valid until it expired (24 h). Each token now carries a `jti`; `POST /api/v1/usuarios/logout` records the presented token's id (Bearer or cookie) in the new `revoked_tokens` table (Flyway V43) until the token expires, and `JwtAuthenticationFilter` rejects revoked tokens with 401. Other sessions of the same user are unaffected; expired rows are purged on the next logout. The logout contract is unchanged (only its OpenAPI description). `JwtLogoutRevocationIntegrationTest` and `TokenRevocationServiceTest` cover it. #676 stays open for password-change revocation and the refresh/lifetime decision.
- **API errors no longer echo internal exception text** (issue #579, slice 1, CU76): eleven controllers caught `Exception` and returned `e.getMessage()` as the 409/500 body. A request missing a required field (for example `POST /api/v1/tipo-folio` with `{}`) got the raw PostgreSQL error back: table and column names, the constraint, and `Failing row contains (...)` with the row's values. `PUT /api/v1/conceptos/{id}` exposed the entity class name. The 23 sites (Concept, DocumentType, FolioType, ManagementStatus, Person, ProcedureType, Testimony, TestimonyMovement, WorkflowDefinition, WorkflowNode, WorkflowTransition) now return the standard `ErrorResponse` through `adapter/in/web/support/ErrorResponses` with the same status code. The message is generic unless it is an application-authored `NotaireException`, and the cause is logged on the server. `ControllerExceptionMessageLeakTest` covers the behaviour and fails if a controller echoes a caught exception's message again. Status-code alignment (409/400/500) is the rest of #579.

- **Server-side authorization for administrative endpoints** (issue #559, CU78; **BREAKING** for non-administrator API clients of these endpoints): any authenticated user could list users and create an `ESCRIBANO` account. The JWT filter now resolves the authority from the stored user on each request (`ROLE_ADMIN` for Administrador, Admin and Escribano, `ROLE_USER` otherwise; deleted or inactive users get 401), `/usuarios` (except login and logout), `/roles` and `/audit-log` require `ROLE_ADMIN`, and mutations of workflow definitions and the administrative catalogs require `ROLE_ADMIN` while reads stay open; non-administrators receive 403. `/dashboard/auditoria` is now an administrator route in the UI; `RbacIntegrationTest`, Bruno `rbac/` and Playwright `TS-0094` cover it. #559 stays open for per-permission rules from `roles_permisos` and for business resources.

- **Runtime backend URL proxy + remove login URL leak** (issue #1055, CU78):
  replace build-time `next.config` `/api/v1` rewrites with an App Router Route
  Handler BFF driven by server-only `BACKEND_URL`; stop baking Docker-internal
  hosts via `NEXT_PUBLIC_API_URL`; remove the public login “Backend: …” line.
  HttpOnly cookie forwarding (#1051) preserved; edge `proxy.ts` still skips
  `/api/**` (#1056).

- **Pin container images + Dependabot docker** (issue #1045, CU78): pin compose,
  infra, Dockerfile, and CI postgres images to minor tags (no `:latest`, no bare
  `sonarqube:community`, no major-only postgres); keep npm `/frontend`; add
  Dependabot docker for `/backend-api` and `/frontend`. Guarded by
  `scripts/test_image_pins_and_dependabot.py`. ADR-017 / DevSecOps / infra docs
  updated.

- **Protect `main` with ruleset** (issue #1040, CU76/CU78): extend ruleset
  `protect-main` (id `24128115`) for PR-only merges, required checks
  `CI` / `Frontend CI` / `Playwright E2E` / `Code Lint` / `PR Validation`, and
  keep force-push/deletion blocked; `bypass_actors` empty after #1041. Thin
  suite aggregator jobs publish the four missing check-run names; desired
  state + admin apply/assert scripts under `scripts/rulesets/` and
  `scripts/apply-protect-main-ruleset.sh` / `assert-protect-main-ruleset.sh`.
  Guarded by `scripts/test_protect_main_ruleset.py`. **Admin must run**
  `bash scripts/apply-protect-main-ruleset.sh --apply` after merge.

- **Dependabot hygiene** (issue #1046, CU78): deleted dead
  `deprecated-frontend-swing/` (EOL `log4j:log4j:1.2.17` alerts) and pinned
  frontend `smol-toml` via npm `overrides` to `^1.9.0` (GHSA-7w5x-hrqm-74c2;
  patched ≥1.7.1). Guarded by `scripts/test_dependabot_hygiene.py`. Live
  CODEOWNERS/README/ADR-005/SAD references updated. `#585` (`deprecated-src.old`)
  remains separate / out of scope.

- **HttpOnly JWT cookie + production CSP nonce** (issue #1051, CU78/CU84):
  login sets `notaire-auth-token` (HttpOnly, SameSite=Lax, Secure via
  `COOKIE_SECURE`); logout clears it; `JwtAuthenticationFilter` accepts cookie
  or Bearer; frontend stops persisting JWT in `localStorage` and uses
  `credentials: 'include'` through the Next proxy; production CSP uses
  nonce-based `script-src` without `'unsafe-eval'`.

- **Audit-log HTTP mutations denied end-to-end** (issue #1060 residual, CU73/CU78):
  confirms GET-only `/api/v1/audit-log` (POST already removed in #1124); unit +
  integration coverage for PUT/DELETE → 405; Bruno negatives under
  `api-test/audit-records/`; OpenAPI tag is consult-only; threat model SR-07
  notes the HTTP forge vector is closed.
- **Request DTOs replace JPA `@RequestBody` entity binding** (issue #1068, CU78):
  thirteen write endpoints now bind validated request records (client-writable
  fields only) and return response DTOs, so clients cannot mass-assign
  server-managed fields (`id`, `version`, status/audit). Affected resources:
  property, identification type, person, budget, deed, item, history, copy,
  substitution, procedure template, budget template, and management CRUD.
  `POST /api/v1/audit-log` is removed — audit rows are written only by
  `AuditoriaAspect` (aligns with #1060). Bruno bodies updated to flat FK ids.
- **Dead default credentials removed** (issue #1069, CU78): dropped the unused
  `spring.security.user.*` keys (`admin`/`admin`) from `application.properties`
  and deleted the legacy Swing-era `config.properties` (plain-text database
  credentials) that no code read. Actuator auth is unchanged (#1069)

## [1.0.0-SNAPSHOT] - 2026-04-14

### Added

- Initial release of Notaire microservices refactoring
- Spring Boot 4.0.4 backend API
- PostgreSQL 16 database
- REST API endpoints for all domain entities
- Swagger/OpenAPI documentation
- Unit tests with 80%+ coverage requirement
- Integration tests with Testcontainers
- Docker Compose setup for local development
- CI/CD pipeline with GitHub Actions

### Features

- **Authentication**: Login endpoint with MD5 password hashing
- **Gestiones**: CRUD operations for legal procedures
- **Escrituras**: Document management
- **Presupuestos**: Budget management
- **Personas**: Person/entity management
- **Reporting**: JasperReports integration

### Technical Stack

- Java 21
- Spring Boot 4.0.4
- Spring Data JPA
- PostgreSQL 16
- Flyway (new)
- Testcontainers
- Maven
- Docker
