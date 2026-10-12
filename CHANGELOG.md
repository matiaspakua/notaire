# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2](https://github.com/matiaspakua/notaire/compare/notaire-v0.1.1...notaire-v0.1.2) (2026-10-10)


### Bug Fixes

* **a11y:** text tokens reach WCAG AA contrast ([#1341](https://github.com/matiaspakua/notaire/issues/1341)) ([#1390](https://github.com/matiaspakua/notaire/issues/1390)) ([7f9f73b](https://github.com/matiaspakua/notaire/commit/7f9f73b44f4e1557505027d04477cfcdf27a1379))
* **ui:** list rows render as cards on phones ([#1356](https://github.com/matiaspakua/notaire/issues/1356)) ([#1391](https://github.com/matiaspakua/notaire/issues/1391)) ([01b6360](https://github.com/matiaspakua/notaire/commit/01b6360a4edcb5dccd60d4664b87dce47af823a6))

## [0.1.1](https://github.com/matiaspakua/notaire/compare/notaire-v0.1.0...notaire-v0.1.1) (2026-10-07)


### Bug Fixes

* **security:** skip admin/admin seed outside dev/test environments ([#1305](https://github.com/matiaspakua/notaire/issues/1305)) ([7f7731e](https://github.com/matiaspakua/notaire/commit/7f7731e0fafae5a3f6a6cca6f82088d8c2349c91)), closes [#1249](https://github.com/matiaspakua/notaire/issues/1249)

## [Unreleased]

### Breaking

- **English HTTP paths, dashboard routes, and remaining schema names** (#1490, CU75):
  public resources move to English segments (`POST /api/v1/users/login`, `/api/v1/managements`,
  `/dashboard/people`, and the rest of the former Spanish paths). There is no Spanish alias.
  Flyway `V45` renames the leftover Spanish table and columns (`identifications.number`,
  `people.birth_date`, workflow `name` / `node_type` / `position_x`, and foreign keys such as
  `fk_id_person` and `fk_id_deed_management`). Login JSON uses `valid`, `idUser`, `name`,
  `type`, and `person`. Remaining DTO JSON names are English (`testimonies`, `testimonyMovements`,
  `budget`, `budgets`, `folioType`, `deedManagement`, `users`, `copies`). Report and
  notebook calls use English names: `GET /api/v1/notebooks/{id}/cover`, query parameters
  `year`, `month`, `procedureTypeId`, and `procedureTypeName`, and path variables such as
  `{idBudget}` and `{idManagement}`. The JSON field `heading` replaces `encabezado`
  (`managementHeading` on budget summaries). Upcoming expirations take the query
  parameter `days`. Management summaries and workflow traces use the JSON
  field `currentStatus`. The current-status read operation id is
  `getCurrentStatus`. `R45` reverses the schema
  renames. Message catalogs stay bilingual, and stored status and catalog labels stay as recorded.
- **English HTTP paths, dashboard routes, and remaining schema names** (#1490, CU75):
  public resources move to English segments (`POST /api/v1/users/login`, `/api/v1/managements`,
  `/dashboard/people`, and the rest of the former Spanish paths). There is no Spanish alias.
  Flyway `V45` renames the leftover Spanish table and columns (`identifications.number`,
  `people.birth_date`, workflow `name` / `node_type` / `position_x`, and foreign keys such as
  `fk_id_person` and `fk_id_deed_management`). Login JSON uses `valid`, `idUser`, `name`,
  `type`, and `person`. Remaining DTO JSON names are English (`testimonies`, `testimonyMovements`,
  `budget`, `budgets`, `folioType`, `deedManagement`, `users`, `copies`). Report and
  notebook calls use English names: `GET /api/v1/notebooks/{id}/cover`, query parameters
  `year`, `month`, `procedureTypeId`, and `procedureTypeName`, and path variables such as
  `{idBudget}` and `{idManagement}`. The JSON field `heading` replaces `encabezado`
  (`managementHeading` on budget summaries). Upcoming expirations take the query
  parameter `days`. Management summaries and workflow traces use the JSON
  field `currentStatus`. The current-status read operation id is
  `getCurrentStatus`. `R45` reverses the schema
  renames. Message catalogs stay bilingual, and stored status and catalog labels stay as recorded.






### Changed

- **Seeded procedure headings** (#1490, CU75): the procedure heading lists
  `Compraventa inmueble Lote 12` and `Donación a herederos`.
- **Seeded management headings** (#1490, CU75): the management heading lists
  `Compraventa Lote 12 — Familia Pérez` and `Donación — Sucesión Gómez`.
- **Seeded history notes** (#1490, CU75): the history notes list
  `Apertura de la gestión`, `Trámite de compraventa en curso`,
  `Documentación completa recibida`, and `Trámite de donación en curso`.
- **Seeded management-status notes** (#1490, CU75): the status notes list
  `Inicio Gestion`, `Post-signing: testimony generated from signed deed`,
  `Post-signing: testimony submitted for inscription`, and `Post-signing:
  testimony withdrawn after inscription cycle`.
- **Seeded workflow transitions** (#1490, CU75): the transition description
  lists `Se asignan trámites y comienza la gestión`, `Toda la documentación
  requerida fue presentada`, `Se redacta la escritura`, `Las partes firman la
  escritura`, `La gestión se archiva sin escritura`, `Testimony generated
  after deed signing`, `Testimony submitted for inscription`, and `Testimony
  withdrawn after inscription cycle`.
- **Seeded workflow description** (#1490, CU75): the workflow description
  names `Ciclo de vida estándar de una gestión de escritura`. The workflow
  name `Workflow de Gestión Estándar` stays.
- **Seeded procedure-type notes** (#1490, CU75): the procedure-type notes
  list `Tramite de compraventa de inmuebles`, `Tramite de donacion`,
  `Tramite de hipoteca`, `Tramite de sucesion`, and `Otorgamiento de poderes`.
- **Seeded document delivery origin** (#1490, CU75): the document-type
  origin lists `Cliente`, `Entidad`, and `Entidad Externa`.
- **Seeded concept labels** (#1490, CU75): the concept name lists `IVA`,
  `Honorarios`, `Documentacion`, and `Protocolo`.
- **Seeded document types** (#1490, CU75): the document-type name lists
  `Libre Deuda Municipal`, `Informe de Dominio`, `Certificado Catastral`,
  and `Fotocopia DNI`.
- **Seeded folio types** (#1490, CU75): the folio-type name also lists
  `De documento`, `De actuacion`, and `De certificacion`. `Protocolo Principal`,
  `Protocolo Auxiliar`, and `Testimonio` stay.
- **Person demographic labels** (#1490, CU75): sex names `Masculino` and
  `Femenino`. Marital status names `Soltero` and `Casado`.
- **User type labels** (#1490, CU75): the user-type column names
  `Escribano`, `Empleado`, `EMPLEADO`, `ADMIN`, `ADMINISTRADOR`, and
  `ESCRIBANO`.
- **Budget item types** (#1490, CU75): the item-type column names
  `NORMAL`, `DESCUENTO`, and `RECARGO`. The default stays `NORMAL`.
- **Role unassign response** (#1490, CU75): the 204 description says
  `Role unassigned`. Stored role names `ADMIN`, `ESCRIBANO`, and
  `SECRETARIA` stay.
- **Role diagram labels** (#1490, CU75): the role diagram names `ADMIN`,
  `ESCRIBANO`, and `SECRETARIA`. The stored user type `Escribano` stays.
- **Procedure-folder status labels** (#1490, CU75): the procedure-folder
  status names `Activa`, `Espera`, and `Archivada`.
- **Registration-draft status labels** (#1490, CU75): the registration-draft
  status names `Generada`, `Presentado para inscripción`, `Observado`, and
  `Inscripto`. Deed status `Inscripta` stays.
- **Data dictionary status labels** (#1490, CU75): deed status names
  `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, and `Inscripta`.
  Management-status examples name `Iniciada`, `En Tramite`, and
  `Testimonio Generado`. Folio status names `Nuevo`, `Utilizado`,
  `Errose`, `no pasó`, and `Asignado a cuaderno`. Stored concept
  `Honorarios` and procedure type `Compraventa` stay.
- **Constitution workflow steps** (#1490, CU75): the SDLC step titles say
  `Refine requirement`, `Design test cases`, `Full regression`, and
  `Run the full pipeline`. E2E tests are described as `self-contained`.
  The Gate 3 heading `Actualizar documentación permanente` stays, along
  with `Use Case (Caso de Uso)` and `hallazgo`.
- **Zero-value concept fixture** (#1490, CU75): the concept whose value is
  zero is named `Exempt`. Seeded concept `Honorarios` stays, and the
  sample street `Av. Corrientes` stays.
- **Demo case addresses** (#1490, CU75): the seeded property street is
  `Demo Avenue`, and the step titles say `Case`. Place abbreviation
  `CABA`, name `María`, and procedure type `Compraventa` stay. Visible
  matchers `domicilio` and `guardar|crear` stay.
- **API coverage defect names** (#1490, CU75): the defect notes name
  `expires`, `DtoDocumentType.isExpires()`, `characters`,
  `associatesProperties`, `enabled`, `findFirstByFkIdPersonIdPerson`,
  `password`, and `fkIdBudget`. Stored status `Firmada` stays.
- **Retired query comments** (#1490, CU75): the budget filter comment names
  `status`, and the identification-type comment names the column
  `characters`. Catalog keys `estadoFilter` and `resumen.numeroGestion`
  stay. The pagination check still rejects a `numero` query.
- **Application tour search queries** (#1490, CU75): the status search
  uses the stored label `Iniciada`, and the workflow search uses
  `Workflow`. The seeded name `Workflow de Gestión Estándar` stays.
- **Flyway guide examples** (#1490, CU75): the migration guide examples
  reference `users(id)`, `phone`, `mobile_phone`, and `procedure_types`.
  Applied migrations `V1` and `V26` keep `usuarios`, `id_usuario`,
  `tipos_de_tramite`, `habilitado`, and `asocia_inmuebles`.
- **Unknown identification-type person** (#1490, CU75): the person posted
  with an unknown identification type is named `Unknown Type E2E`. The
  request still answers 400. The first name `Ana` stays, and omitting
  the type still defaults to `DNI`.
- **H2 seed address and email** (#1490, CU75): the seeded notary lives at
  `Main Avenue 123` with email `jcgarcia@notary.com`. The name
  `Juan Carlos Garcia`, marital status `Casado`, and type `Escribano`
  stay. Applied migration `V2` keeps `Av. Principal 123` and
  `jcgarcia@notaria.com`.
- **Fiscal valuation designation** (#1490, CU75): the property saved for the
  fiscal valuation check uses the designation `Appraised house`. Visible
  labels `Domicilio` and `Nomenclatura Catastral` stay.
- **Concept edit fixture** (#1490, CU75): a concept that is in use is
  edited as `NameModified` and still answers 409. Stored concept names
  stay.
- **Doc-traceability fixture heading** (#1490, CU75): the temporary design
  file in the PR check is headed `# Management`. The claimed path stays
  `docs/200-architecture/203-design/GESTION.md`.
- **Budget balance accessors** (#1490, CU75): the deprecated budget methods
  are `getBalance` and `setBalance`. The JSON field stays `saldo`. The
  catalog key `resumen.saldo` stays.
- **Workflow active checkbox** (#1490, CU75): the workflow form checkbox
  test id is `checkbox-active-workflow`. The catalog namespace
  `administracion.workflows` stays. The visible label still comes from
  `common.active`.
- **Administrator seed name** (#1490, CU75): a newly created administrator
  person is stored as `Admin System`. The login name `admin`, the type
  `Escribano`, and the identification type `DNI` stay. An administrator
  that already exists is left unchanged.
- **Testimony seed helper** (#1490, CU75): the deed-required design names
  `createTestimony`. The retired helper name `createTestimonio` stays out
  of the seed. TS-0032 still posts a deed on the bare create.
- **Success-toast default** (#1490, CU75): the unused helper default is
  `success`. The deed toast matcher keeps `/creada|éxito/i`. Visible toasts
  such as `inscripción registrada` stay. Button matchers such as
  `confirmar|guardar|crear` stay.
- **Synthetic person occupation** (#1490, CU75): person fixtures use `Clerk`.
  The stored user type `Empleado` stays, along with `EMPLEADO`, `Soltero`,
  and `Masculino`.
- **Budget-procedure survey citation** (#1490, CU75): the living spec names
  `docs/openspec/explore.md`. The archived cardinality spec keeps
  `openspec/explore_functional_report.md`.
- **Workflow tracker E2E filename** (#1490, CU75): the design cites
  `testing/e2e/tests/TS-0138-workflow-tracker.spec.ts`. The e2e-suite
  spec keeps the historical name `workflow-tracker.spec.ts`.
- **Person validation E2E filename** (#1490, CU75): the living spec names
  `TS-0015-people-clients-workflow.spec.ts`. The archived #945 spec keeps
  `TS-0015-personas-clientes-workflow.spec.ts`.
- **Report query fixture** (#1490, CU75): the document-list hook encodes
  `Purchase/Sale & more`. The stored procedure type `Compraventa` stays.
- **Runtime proxy edge file** (#1490, CU75): the living spec names
  `frontend/src/proxy.ts`. The archived #1055 spec keeps
  `middleware.ts` or `proxy.ts`.
- **E2E sequence pointers** (#1490, CU75): the Playwright headers cite
  `seq01-ingresar-gestión-de-escritura.puml`,
  `seq02-ingresar-gestión-de-trámite.puml`,
  `seq05-preparar-escritura.puml`,
  `seq07-inscribir-escritura.puml`, and
  `seq17-buscar-gestiones-de-un-cliente.puml`. The sequence titles stay.
- **RBAC notary filename** (#1490, CU75): the Bruno request file is
  `04-employee-create-notary.yml`. The stored type `ESCRIBANO` stays.
- **Document-type unit filename** (#1490, CU75): the form-defaults unit
  test is `document-type-enabled-returned.test.ts`. The archived #800
  design keeps `tipo-documento-enabled-returned.test.ts`.
- **Open Playwright filenames** (#1490, CU75): open change notes cite
  `TS-0098-management-case-summary-feature.spec.ts`,
  `TS-0099-document-procedure-link.spec.ts`, and
  `TS-0019-properties-valuation-workflow.spec.ts`. The change slug
  `feat-774-gestion-resumen-caso` stays. The archived feat-773 design
  keeps `TS-0099-documento-tramite-link.spec.ts`.
- **Testimony-movements screen comment** (#1490, CU75): the dialog comment
  names the testimony-movements screen. The official title
  `CU08 – Movimientos de testimonio` stays. The Spanish catalog value
  `Movimientos de testimonio` stays.
- **Auxiliary deed body** (#1490, CU75): the auxiliary-protocol edge path
  fills `Deed body`. The golden path still fills `E2E test deed body`.
  The Spanish catalog helper `Contenido de la escritura` stays.
  `docs/100-business` stays.
- **Icon-button users screen** (#1490, CU75): the living spec
  `icon-button-accessible-names` names the users administration screen.
  The default-locale labels “Editar” and “Eliminar” and the selector
  `/editar/i` stay. The archived spec keeps `edit on usuarios`.
- **Corrected notes** (#1490, CU75): synthetic testimony and payment notes in
  tests say `Corrected`, `Corrected number E2E`, and `First copy`. Visible
  labels such as `Eliminar` and `Testimonio actualizado` stay.
- **Error-toast exemplars** (#1490, CU75): the living spec `api-error-toasts`
  cites `CU20 – Dar alta usuario` and `CU21 – Modificar Usuario`, and names
  the CU26–CU30 base-catalog pages. The archived spec keeps `usuarios` and
  `tablas base`.
- **Upcoming-expiration purpose** (#1490, CU75): the living spec
  `documento-presentado-herencia-vencimiento` cites the official title
  `CU42 – Informar próximos vencimientos`. The archived spec keeps
  `"informar próximos vencimientos"`.
- **Management-status histories accessor** (#1490, CU75): `DtoManagementStatus`
  names the set `histories` (`getHistories` / `setHistories`). The JSON key
  stays `historials`.
- **Retired association ignores** (#1490, CU75): Jackson ignore lists on
  `Copy`, `Deed`, `Folio`, `Testimony`, and `IdentificationType` no longer
  name `fkIdEscritura`, `fkIdTestimonio`, `fkIdTipoIdentificacion`,
  `foliosCopiasCollection`, `movimientoTestimonioList`, `copiaList`, or
  `copia`. Those names match no current field, so the serialized shape stays.
  `folioList`, `tramiteList`, and `testimonioList` stay.
- **Retired validation detail** (#1490, CU75): the engineering-sequences
  design names the detail CU20 still writes, `campo: mensaje`.
  The live validation text is `field: message`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired item type and reason** (#1490, CU75): the engineering-sequences
  design names the fields CU71 still writes, `tipo` and `motivo`.
  The live fields are `type` and `reason`. Stored item types `NORMAL`,
  `DESCUENTO`, and `RECARGO` stay. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired budget id** (#1490, CU75): the engineering-sequences
  design names the path variable CU45 still writes, `idPresupuesto`.
  The live path variable is `idBudget`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired procedure-type query** (#1490, CU75): the engineering-sequences
  design names the query parameter CU39 still writes, `tipoTramiteId`.
  The live query parameter is `procedureTypeId`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired submitted-document id** (#1490, CU75): the engineering-sequences
  design names the path variable CU10 still writes, `idDocumentoPresentado`.
  The live path variable is `idSubmittedDocument`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired status log service** (#1490, CU75): the engineering-sequences
  design names the class CU13 still writes, `ManagementBitacoraService`.
  The live class is `ManagementStatusLogService`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired due-date formula** (#1490, CU75): the engineering-sequences
  design names the formula CU42 still writes,
  `fechaVencimiento = fechaIngreso + diasVencimiento`.
  The live fields are `dateDue`, `dateEntry`, and `dueDays`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Retired reentered flag** (#1490, CU75): the engineering-sequences
  design names the flag CU43 still writes, `reingresado=true`.
  The live field is `reentered`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired current status field** (#1490, CU75): the engineering-sequences
  design names the field CU83 still writes, `statusActual`.
  The live field is `currentStatus`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired substitution dates** (#1490, CU75): the engineering-sequences
  design names the fields CU22 still writes, `fechaInicio` and `fechaFin`.
  The live fields are `dateStart` and `dateEnd`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired deed link field** (#1490, CU75): the engineering-sequences
  design names the field CU87 still writes, `escrituraId`.
  The live field is `deedId`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired budget foreign key** (#1490, CU75): the engineering-sequences
  design names the column CU71 still writes, `fk_id_presupuesto`.
  The live column is `fk_id_budget`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired amount to pay** (#1490, CU75): the engineering-sequences
  design names the field CU27 still writes, `DocumentoPresentado.importeAPagar`.
  The live field is `amountToPay`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired document cost template** (#1490, CU75): the engineering-sequences
  design names the type CU39 still writes, `PlantillaCostoDocumento`.
  The live class is `DocumentCostTemplate`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired budget template** (#1490, CU75): the engineering-sequences
  design names the type CU39 still writes, `PlantillaPresupuesto`.
  The live class is `BudgetTemplate`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired management status** (#1490, CU75): the engineering-sequences
  design names the type CU83 still writes, `EstadoDeGestion`.
  The live class is `ManagementStatus`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired procedure type** (#1490, CU75): the engineering-sequences
  design names the type CU83 and CU39 still write, `TipoDeTramite`.
  The live class is `ProcedureType`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired submitted document** (#1490, CU75): the engineering-sequences
  design names the type CU43 and CU42 still write, `DocumentoPresentado`.
  The live class is `SubmittedDocument`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired procedure template** (#1490, CU75): the engineering-sequences
  design names the type CU43 still writes, `PlantillaTramite`.
  The live class is `ProcedureTemplate`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired document services** (#1490, CU75): the engineering-sequences
  design names the classes CU43 and CU10 still write,
  `ReingresoDocumentacionService` and `DocumentoEntidadExternaService`.
  The live classes are `DocumentReentryService` and
  `ExternalEntityDocumentService`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Retired case create** (#1490, CU75): the engineering-sequences design
  names the path and table EVALUACION-DOCUMENTACION still writes,
  `POST /api/v1/gestion` and `gestiones_de_escrituras`. The live call
  stays in `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Short management create and status** (#1490, CU75): the engineering-sequences
  design names the paths CU13 still writes, `POST /gestiones` and
  `GET /gestiones/{id}/estado-actual`. The live calls stay in
  `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Short management updates** (#1490, CU75): the engineering-sequences
  design names the paths CU83 and CU13 still write, `PUT /gestiones/{id}`
  and `PUT /gestiones/{id}/complete-case`. The live calls stay in
  `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Admin screen** (#1490, CU75): the engineering-sequences design names
  the screen CU78 still writes, `/dashboard/administracion/**`.
  The live screen stays in `admin-route-guard`. The label key
  `administracion` stays. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Plain management update** (#1490, CU75): the engineering-sequences
  design names the path CU22 still writes, `PUT /api/v1/gestiones`.
  The live call stays in `gestion-asignacion-suplencia`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Audit screen** (#1490, CU75): the engineering-sequences design names
  the screens CU23 still writes, `/dashboard/auditoria` and
  `/dashboard/administracion/auditoria`. The live screen stays in
  `dashboard-nav-canonical-routes`. The label key `auditoria` stays.
  The heading `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Reports screen** (#1490, CU75): the engineering-sequences design names
  the screen CU24, CU25, and CU50 still write, `/dashboard/reportes`.
  The live screen stays in `dashboard-nav-canonical-routes`. The label
  key `reportes` stays. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Substitutions screen** (#1490, CU75): the engineering-sequences design
  names the screen CU22 and CU59 still write, `/dashboard/suplencias`.
  The live screen stays in `dashboard-nav-canonical-routes`. The label
  key `suplencias` stays. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Document re-entry screen** (#1490, CU75): `DOC-GESTION` names
  `/dashboard/document-reentry`. The engineering-sequences design names
  the screen CU43 still writes, `/dashboard/reingreso-documentacion`.
  The heading `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **External-entity documents screen** (#1490, CU75): `DOC-GESTION` names
  `/dashboard/external-entity-documents`. The engineering-sequences design
  names the screen CU10 still writes,
  `/dashboard/documentos-entidades-externas`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Budget screen** (#1490, CU75): the living
  `presupuesto-from-template` spec names `/dashboard/budgets`. The
  engineering-sequences design names the screen CU01, CU39, and CU71
  still write, `/dashboard/presupuestos`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Upcoming expirations screen** (#1490, CU75): the living
  `upcoming-expirations` spec names `/dashboard/upcoming-expirations`.
  The engineering-sequences design names the screen CU42 still writes,
  `/dashboard/proximos-vencimientos`. The archived proposal keeps that
  screen. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Payment consult paths** (#1490, CU75): the engineering-sequences
  design names the paths CU47 still writes,
  `GET /api/v1/presupuestos/{id}/resumen` and
  `GET /api/v1/pagos/presupuesto/{id}/estado`. The live calls stay in
  `pago-presupuesto-gestion-summary` and `pagos-parciales-cuotas`.
  The heading `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Discounts and surcharges path** (#1490, CU75): the living
  `descuentos-recargos-presupuesto` spec names
  `GET /api/v1/items/budget/{idBudget}/discounts-surcharges`. The
  engineering-sequences design names the path CU45 still writes,
  `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos`.
  The archived design keeps that path. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Catalog items path** (#1490, CU75): the living
  `presupuesto-catalogo-items` spec names
  `POST /api/v1/budgets/{id}/items-from-catalog`. The
  engineering-sequences design names the path CU71 still writes,
  `POST /api/v1/presupuestos/{id}/items-desde-catalogo`. The archived
  proposal keeps that path. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Upcoming expirations query** (#1490, CU75): the living
  `upcoming-expirations` spec names the query `days` on
  `GET /api/v1/submitted-documents/upcoming-expirations`. The
  engineering-sequences design names the path CU42 still writes,
  `GET /api/v1/documento-presentado/proximos-vencimientos?dias=N`.
  The archived change keeps `dias`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Budget template path** (#1490, CU75): the engineering-sequences
  design names the path CU01 and CU39 still write,
  `POST /api/v1/presupuestos/{id}/items-desde-plantilla`. The live call
  stays in `presupuesto-from-template`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **Rejected status PUT paths** (#1490, CU75): `DOC-GESTION` names the
  live updates `PUT /api/v1/managements/{id}` and
  `PUT /api/v1/managements/{id}/complete-case`. A status change on either
  answers 400. The engineering-sequences design names the paths CU53 still
  writes, `PUT /api/v1/gestiones/{id}` and
  `PUT /api/v1/gestiones/{id}/complete-case`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Management history paths** (#1490, CU75): `DOC-GESTION` names the
  live reads `GET /api/v1/managements/{id}/history` and
  `GET /api/v1/managements/{id}/current-status`. The engineering-sequences
  design names the paths CU13 still writes,
  `GET /api/v1/gestiones/{id}/historial` and
  `GET /api/v1/gestiones/{id}/estado-actual`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Workflow-trace path** (#1490, CU75): `DOC-GESTION` names the live
  legal-next read `GET /api/v1/managements/{id}/workflow-trace`. The
  engineering-sequences design names the path CU83 still writes,
  `GET /api/v1/gestiones/{id}/workflow-trace`. The heading
  `Riesgos / Trade-offs` stays. `docs/100-business` stays.
- **Complete-case path citation** (#1490, CU75): the
  engineering-sequences design names the path CU22 still writes,
  `PUT /api/v1/gestiones/complete-case`. The live call stays in
  `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Management transition path citation** (#1490, CU75): the
  engineering-sequences design names the path CU53 and CU83 still write,
  `POST /api/v1/gestiones/{id}/transition`. The live call stays in
  `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **External-entity document path citation** (#1490, CU75): the
  engineering-sequences design names the path CU10 still writes,
  `GET/PUT /api/v1/gestiones/{id}/documentos-entidades-externas[/{idDocumentoPresentado}]`.
  The live call stays in `DOC-GESTION`. The heading `Riesgos / Trade-offs`
  stays. `docs/100-business` stays.
- **k6 script path** (#1490, CU75): the living load-test spec names
  `infra/performance/k6/load-test.js`, the file the weekly workflow runs.
  The archived change keeps `performance-test/k6/load-test.js`.
- **DAST protected path** (#1490, CU75): the unauthenticated baseline and
  the security catalog probe `GET /api/v1/people`. The retired path
  `/api/v1/personas` stays out of that gate. Health still expects 200.
- **Testimony re-entry path citation** (#1490, CU75): the engineering-sequences
  design names the path CU44 still writes,
  `POST /api/v1/movimiento-testimonio/{id}/reenter`. The live call stays in
  `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **Document re-entry path citation** (#1490, CU75): the engineering-sequences
  design names the path CU43 still writes,
  `GET/POST /api/v1/gestiones/{id}/reingreso-documentacion`. The live call
  stays in `DOC-GESTION`. The heading `Riesgos / Trade-offs` stays.
  `docs/100-business` stays.
- **English source-surface purpose** (#1490, CU75): the living spec says
  the domain model maps database identifiers to English names. The
  historical login contrast `POST /api/v1/usuarios/login` stays, along
  with the forbidden segments `usuarios`, `gestiones`, `tramites`,
  `presupuestos`, and `escrituras`.
- **Withdraw comment citation** (#1490, CU75): the testimony-movement
  hook names the retired `/retirar` path and `numeroCarton`. The live
  call stays `/testimony-movements/{id}/withdraw` with `cardNumber`.
  The message key `fields.numeroCarton` stays.
- **SAD API design collections** (#1490, CU75): section 8.7 names the
  English collections `/managements`, `/people`, and `/deeds`. The
  parenthetical no longer calls those paths a Spanish domain.
- **September 2026 audit index citation** (#1490, CU75): the #1065 row
  names the unused `POST /pagos/params` path recorded on baseline `5a8fa14`.
  The column `State (at audit)` stays. The later notes on #1040 and #1042 stay.
- **October 2026 assessment citations** (#1490, CU75): the baseline
  measurements name `pagos/fecha`, `presupuestos/persona`,
  `historial/gestion`, `reportes/documentos-por-vencer`, `usuarios/persona`,
  `/gestiones`, `/pagos`, `/folio`, `/inmueble`, `/copia`, `/buscar`, and
  `/usuarios`. Official titles `Documentos Presentados` and
  `Plantillas de Trámite` stay. Stored roles `EMPLEADO` and `ESCRIBANO` stay.
  The baseline commit `d4473af2` stays.
- **Retired path citations in ADR-023** (#1490, CU75): the naming
  decision cites `/folio`, `/inmueble`, `/copia`, `/buscar`, `/firmar`,
  `/archivar`, and the removed `POST /pagos/params`. It prefers `/search`
  over `/buscar`. The preferred example
  `PUT /registration-drafts/{id}/submit` stays, along with
  registration-draft create. The live create `POST /api/v1/payments` stays.
  The requirement that `POST /api/v1/payments/params` shall not exist stays.
- **English registration-draft consequence** (#1490, CU75): ADR-023 says
  clients must treat registration-draft create as `201`. The spec id
  `minuta-inscripcion` stays, along with the gloss `Minuta de Inscripción`
  and `POST /api/v1/registration-drafts`.
- **English client actor id** (#1490, CU75): the case-process diagram
  names the client actor `client`. The label `Client` stays. The actor
  ids `gestor` and `escribano` stay, along with `escribano[Escribano]`.
- **English field-label comments** (#1490, CU75): the management-edit
  note names the number field, and the concept-form note quotes
  `Nombre`, `Descripción`, and `Valor base ($)`. The button `Editar`
  stays, along with `porcentaje`, `nuevo concepto`, and `Nuevo concepto`.
  The official title `CU53 - Modificar gestión` stays.
- **English is-client comments** (#1490, CU75): the people-workflow
  notes name the is-client checkbox. The visible label `Es cliente`
  stays, along with the toast match `/creada/i` and the row match
  `/^cliente$|^sí$/i`.
- **English required-documents comment** (#1490, CU75): the checklist
  dialog says it is ADMIN only, like the rest of the administration
  section. The visible section name `Administración` stays.
- **English use-case module pointers** (#1490, CU75): included use
  cases point at `Budget`, `Clients`, `Deeds`, `Concepts`, and
  `Procedures`. Official titles stay, including `Buscar Presupuesto`,
  `Buscar gestiones de un Cliente`, `Buscar Escritura`,
  `Buscar Conceptos`, `Buscar Tipo de trámite`, and `Buscar Folios`.
  The pointer `(from Folios)` stays, along with actor `Escribano`.
- **English property-title fixture** (#1490, CU75): the procedure-template
  unit catalog names the synthetic document `Property title`. Seed names
  `Libre Deuda Municipal` and `Informe de Dominio` stay, along with
  `Cliente`, `Entidad`, `Compraventa`, and the visible label
  `Administración → Trámites`.
- **English catalog Playwright filenames** (#1490, CU75): the
  budget-template delete note names `TS-0131-budget-template.spec.ts`.
  The catalog-constraint note names
  `TS-0134-document-type-expiration-config.spec.ts` and
  `TS-0130-budget-catalog-items.spec.ts`. The incomplete-PUT task list
  names `TS-0135-folio-type-edit-version.spec.ts` and
  `TS-0136-folios-linking.spec.ts`. The heading `Riesgos / Trade-offs`
  stays, along with `Crear branch`, `Implementación`, and
  `Ejecutar Playwright`. Spec ids stay.
- **English checklist fixture notes** (#1490, CU75): required-document
  samples say `Original and copy` and `Certified copy`. Created names
  say `Procedure checklist E2E`, `Document checklist E2E`,
  `FolioTypeTest`, and `AuxiliaryFolioTypeTest`. The visible labels
  `Administración → Trámites`, `Documentos requeridos`, and
  `Documentos necesarios` stay. Stored `Cliente` and `Protocolo Auxiliar`
  stay, along with `tipos de trámite`.
- **English retired test filenames** (#1490, CU75): the copies
  change names `TS-0132-copies-required-fields.spec.ts`. The
  people-pagination change names `people-pagination.test.tsx` and
  `TS-0015-people-clients-workflow.spec.ts`. Headings
  `Módulos afectados` and `Riesgos / Trade-offs` stay. Official titles
  stay, including `CU08 – Copias de testimonio`,
  `CU82 – Minuta de inscripción`,
  `CU43 – Reingreso de documentación`, `CU18 – Dar Alta Cliente`,
  `CU54 – Modificar Persona`, and `CU61 – Buscar persona`. Task
  headings `Crear branch`, `Implementación`, `Ejecutar regresión`,
  `Ejecutar Playwright`, `Commits atómicos`, and
  `Pull Request y validación CI` stay.
- **English lifecycle proposals** (#1490, CU75): the assessment
  and domain-lifecycle proposals name a management and the
  create-management sequence. Headings `Objetivo`, `Reglas de negocio`,
  and `Módulos afectados` stay. Stored statuses `Documentación completa`
  and `Testimonio Retirado` stay. Filenames `GESTION.md` and
  `CICLO-DE-VIDA.md` stay. `POST /api/v1/managements/complete-case`,
  `deed_managements`, and `deeds.status` stay.
- **English remaining proposals** (#1490, CU75): the archive-debt
  proposal names `ManagementArchiveDebtService`,
  `pendingDebtAtArchiving`, and `managements/page.tsx`. The
  submitted-document proposal names the Documents screen. The
  property proposal names the Properties page and the Properties
  dialog. Headings `Objetivo`, `Regla de Negocio`, `Reglas de negocio`,
  and `Módulos afectados` stay. Official titles stay, including
  `CU16 – Archivar Gestión`, `CU04 – Registrar documentación cliente`,
  `CU72 – Gestionar documentos presentados`,
  `CU69 – Gestión de Inmuebles`, and
  `CU82 – Datos registrales del inmueble`. The visible label
  `nomenclatura catastral` stays. The branch
  `feat/169_gestion-archive-deuda-check` and the change slug
  `gestion-archive-deuda-check` stay. `fk_id_tramite` and
  `GET /outstanding-balance` stay.
- **English change proposals** (#1490, CU75): the case-summary
  proposal names a management, the managements screen,
  `financial-summary`, and `ManagementFinancialSummaryService`.
  The paged-list proposal names `budgets`, `deeds`, `procedures`,
  and `managements`. The incomplete-PUT proposal names
  `is required`. Headings `Objetivo`, `Reglas de negocio`, and
  `Módulos afectados` stay. Official titles stay, including
  `CU07 – Generar testimonio` and `CU70 – Gestión de Copias`.
  The visible action `Resumen del caso` stays, along with the
  dialog labels `Escrituras`, `Testimonios`, `Copias`, and
  `Pagos`, and `Administración → Trámites`. The branch
  `feat/774_gestion_workflow_links` stays. Stored states
  `SIN_INGRESAR`, `INGRESADO`, `INSCRIPTO`, and `RETIRADO` stay.
- **English change-task notes** (#1490, CU75): the case-summary
  task list names a managements pull request and the managements
  screen. The assessment task list names the create-management
  diagram. The branch `feat/774_gestion_workflow_links` and the
  change slug `feat-774-gestion-resumen-caso` stay. Template
  headings stay, including `Crear branch`, `Commits atómicos`, and
  `Implementación`. The filename `GESTION.md` stays.
- **English change-design notes** (#1490, CU75): the case-summary
  design names the managements screen, `useManagementSummary`, and
  `financial-summary`. The required-documents design names the
  required-documents screen and the procedure-types page. The
  property design says properties require `cadastralDesignation`.
  The submitted-document design names the Documents screen and
  `DocumentReentryService`. Official titles stay, including
  `CU07 – Generar testimonio` and `CU70 – Gestión de Copias`. The
  heading `Riesgos / Trade-offs` stays. The column
  `properties.nomenclature` and `fk_id_tramite` stay. The filename
  `TS-0098-gestion-resumen-caso-feature.spec.ts` stays. The proposal
  heading `Objetivo` stays, along with `Documentos necesarios` in
  that proposal.
- **English change-contract requirements** (#1490, CU75): the
  archive-debt change spec names a management, `pendingBalance`, and
  `pendingDebtAtArchiving`. The incomplete-PUT delta names
  `is required` and the live paths `folio-types`,
  `management-statuses`, `document-types`, `procedure-types`,
  `concepts`, `workflow-definition`, and `testimony-movements`. The
  people-list delta names the people screen. The change slugs
  `gestion-archive-deuda-check`,
  `fix-655-put-rejects-incomplete-bodies`, and
  `fix-596-people-pagination` stay. Stored status `Archivada` stays.
  The official filename `CU16 – Archivar Gestión.md` stays. The
  historical test `GestionArchiveIntegrationTest` stays. `usePeople`
  and `GET /api/v1/people` stay.
- **English property-designation requirements** (#1490, CU75): the
  properties delta names the Properties dialog and a cadastral
  designation. The change slug
  `fix-655-property-cadastral-designation-required` stays.
  `cadastralDesignation`, `POST /api/v1/properties`, and
  `PUT /api/v1/properties/{id}` stay. The visible label
  `nomenclatura catastral` in TS-0090 stays.
- **English open-change requirements** (#1490, CU75): the case-summary
  spec and the submitted-document delta name a management and a
  procedure. The change slugs `feat-774-gestion-resumen-caso` and
  `fix-655-submitted-document-required-fields` stay. Stored states
  `INSCRIPTO`, `SIN_INGRESAR`, and `RETIRADO` stay. The official title
  `CU07 – Generar testimonio` stays. `POST /api/v1/submitted-documents`,
  `typeId`, and `procedureId` stay.
- **English E2E header comments** (#1490, CU75): the file headers
  name a procedure and the administration screens. Visible labels
  stay, including `/administración/i`, the link `Administración`,
  `Trámites`, `nav-administracion`, and `nav-auditoria`. The official
  title `CU53 - Modificar gestión` stays. The column title
  `Tipo de Trámite` stays.
- **English document-link requirements** (#1490, CU75):
  `document-procedure-link` and `required-docs-on-confirm` name a
  procedure and a management. The Documents screen stays the screen
  name in that prose. Spec ids stay. `ProcedureTemplate`,
  `POST /api/v1/managements/complete-case`, and
  `GET /api/v1/managements/{id}/case-summary` stay.
- **Named-query prefix** (#1490, CU75): the createNamedQuery scenario
  forbids the retired prefix `Persona.`. The live prefix is `Person.*`.
  Spec id `jpa-namedquery-english-names` stays.
- **Historical user-manual filename** (#1490, CU75): the PDF is
  `Manual de Usuario Notaire.doc.pdf`, in `C_Manual de Usuario`, and the
  release asset is `Manual-de-Usuario-Notaire.doc.pdf`. The fetch script,
  ADR-022, the repo-hygiene spec, and the hygiene test use that path.
  The TypeScript type `UserAccount` stays.
- **Folio-type name in the data dictionary** (#1490, CU75): the stored
  name is `Testimonio`, listed with `Protocolo Principal` and
  `Protocolo Auxiliar`, in the dictionary and the ERD. English prose such
  as `Testimony issued from a deed` stays. The filenames
  `Diccionario de Datos.md` and `Modelo Relacional Escribania - Entidades.csv`
  stay.
- **Constraint-error use-case citation** (#1490, CU75): the record cites
  `CU07 – Testimonio`. The titles `Movimientos de testimonio` and
  `Personas` stay. The proposal heading `Objetivo` stays.
- **Official use-case titles in open change records** (#1490, CU75):
  the people-pagination record cites `CU54 – Modificar Persona`. The
  testimony record cites `CU07 – Generar testimonio` and
  `CU08 – Verificar Testimonio`. The delete-status record cites
  `CU21 – Modificar Usuario`. Official titles `Dar Alta Cliente`,
  `Buscar persona`, and `Procesar pago` stay, along with the original
  wording `Gestionar historial de gestion`. Required headings
  `Objetivo` and `Reglas de negocio` stay.
- **English transaction-guide sample** (#1490, CU75): the sample service
  is `DeedService`. The lazy-field search path is
  `com/licensis/notaire/business/`. The return type `Page<Deed>` stays.
- **English payment use-case titles in ADR-021** (#1490, CU75): the
  hexagonal pilot cites `CU15 "Procesar pago"` and
  `CU47 "Consultar pago"`. Class name `PaymentService` stays.
- **English cleanup comments and role fixture** (#1490, CU75): the
  cleanup comments name `people`, `deed_managements`, and `deeds`. The
  role description is `Administrator`. The synthetic role name is
  `OPERATOR`. The role field `modulos` and module key `administracion`
  stay. `GuiConstants.ADMINISTRATOR` stays `Administrador`.
- **English H2 seed phrases** (#1490, CU75): the concept is `Base fee`.
  The budget heading is `Test budget`. The procedure-type note is
  `Compraventa procedure`. Stored labels `De documento`, `Iniciada`,
  `Inicio Gestion`, `Compraventa`, `PENDIENTE`, `Casado`, `Escribano`,
  and `DNI` stay. Applied migration `V2` stays.
- **English hook fixture phrases** (#1490, CU75): the budget-template
  procedure type is `Base fee`. The folio type is `Protocol`. Save-error
  describe titles name `presentPersonSaveError`. The fixture last name is
  `Existing`. The audit query-key title names `audit`. Stored statuses
  `En proceso` and `Compraventa` stay. Visible fallbacks
  `Error al guardar la persona` and `Ver persona existente` stay. The
  nav label `Protocolo` stays.
- **English audit column keys** (#1490, CU75): the audit table keys are
  `module` and `detail`. The procedure-template note is
  `Observation test`. Message key `fields.modulo` stays. The role
  field `modulos` and test id `check-modulo-` stay.
- **English cost fixture phrases** (#1490, CU75): the document type is
  `Previous deed`. Display names say `costs`. The printed-copy comment
  names a verified testimony. Delivered-by `Cliente` stays. Stored
  status `Firmada` stays. Operation id `getCostosByTypeProcedure` stays.
- **English document note phrases** (#1490, CU75): the upcoming-expiration
  note is `Request a copy`. The partial-update note is `previous note`.
  Stored document name `Certificado de dominio` stays, along with heading
  `Compraventa Pérez`, delivered-by `Cliente`, and the name `Deed original`.
- **English load-test check names and dialog locals** (#1490, CU75): k6
  check names are `managements`, `budgets`, and `procedures`. The
  reentry dialog local is `detailDialog`. Item-form prose names stored
  types `DESCUENTO` and `RECARGO`, and the surcharge case saves without
  a reason. Paths `/api/v1/managements`, `/api/v1/budgets`, and
  `/api/v1/procedures` stay. Login fields `name` and `password` stay.
  Visible text `Certificado de Dominio`, `no tiene trámites`,
  `documento reingresado`, `Descuento`, `Recargo`, and
  `motivo es obligatorio` stay. The change slug
  `descuentos-recargos-budget` stays.
- **English catalog fixture phrases** (#1490, CU75): substitution notes
  are `Sick leave`. The management heading fixture is `Heading E2E`.
  The folio-type name is `Folio type E2E`. Playwright detail values are
  `Activated from Playwright` and `Archived from Playwright`. The
  movement note is `Withdrawn from the registry`. Procedure notes are
  `Donation procedure`. Document-type names are `Power Expires`,
  `Identity card Expires`, and `Contract Expires`. The delivered-by
  fixture is `Property Registry`. The dialog local is `detailDialog`.
  Visible headers `Carátula`, `Inicio`, `Trámites`, and
  `Tipo de Trámite` stay, along with `días`, `creado`,
  `movimiento registrado`, and `no hay documentos`. Stored labels
  `Donacion`, `Escribano`, `Certificado de Dominio`, `Entidad Externa`,
  `Cliente`, `Soltero`, `Masculino`, `Efectivo`, and `Nuevo` stay.
  The dead field `detalle` and the keys `vence`, `diasVencimiento`,
  and `quienEntrega` stay.
- **English person and concept fixture labels** (#1490, CU75): stubbed
  people are `Referenced` and `Mobile`. The company fixture is
  `Company`. The concept value is `New Concept Test`. The
  missing-person search is `ZZZZDoesNotExist999XY`. Visible phrases
  `No se puede eliminar`, `Error al eliminar la persona`,
  `Nuevo concepto`, and `no hay` stay. Stored types `DNI`, `CUIT`,
  and `Pasaporte` stay, along with statuses `INICIADA` and
  `EFECTIVO`. The form field key `nombre` stays.
- **English workflow fixture phrases** (#1490, CU75): the transition
  description is `Status advance`. The condition is
  `status.documentsComplete == true`. The special-character query is
  `Purchase/Sale (special) & more`. Person fixtures are `Create`,
  `Promote`, `Edit`, `First`, `Second`, `NoId`, `Sample`, and
  `Company`. Stored type `Compraventa` stays, along with person names
  `Lucía`, `María`, and `Pedro`, and the visible toast match
  `/suplente/i`.
- **English case-summary state and status names** (#1490, CU75): the
  managements page state is `caseSummaryId`. Identification-type
  display names use `/identification-types`. Synthetic status names
  are `StatusFree_InUse`, `StatusUsed_InUse`, `StatusEditBlocked`,
  `StatusEditAllowed`, and `StatusDeleteBlocked`. Message keys under
  `gestiones` stay. Stored statuses stay.
- **English synthetic person labels** (#1490, CU75): the substitution
  fixture first name is `Substituted IT`. Redirect last names are
  `Notary`, `Substituted`, `Substitute`, `Client`, `NoSubstitution`,
  and `ClientNorm`. Date locals are `today` and `tomorrow`. Edit
  fixtures are `Absent1360` and `Substitute1360`. People-search
  fixtures are `Client`, `IsClient`, `SearchLastName`, and
  `SearchDni`. Payment balance locals are `lastName1` and
  `lastName2`. The visible toast match `/suplente/i` stays, along
  with stored role `Escribano` and person names `Lucía` and `María`.
- **English substituted parameters** (#1490, CU75): substitution lookups
  take `substituted` and `substitutedId`. The methods stay
  `findByFkIdSubstituted`, `findByFkIdSubstitutedIdPerson`, and
  `findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual`.
  Message key `fields.suplente` stays. The visible toast match
  `/suplente/i` stays.
- **English started status and substitute parameters** (#1490, CU75):
  the workflow fixture status is `Started`. History creation defaults to
  `Started`. Substitution parameters are `idSubstitute` and
  `idSubstituted`. The redirect fixture is `substitute`. The
  identification-type fixture is `Identity card`. Stored types `DNI` and
  `CUIT` stay. The visible toast match `/suplente/i` stays. Stored
  management status `Iniciada` stays.
- **English synthetic status names** (#1490, CU75): the query fixture
  status is `STARTED`. The repository identification type is
  `Professional`. The complete-case fixtures are `Status 1360` and
  `Procedure 1360`. Stored budget status `PENDIENTE` stays, along with
  identification type `DNI` and stored management status `Iniciada`.
- **English testing practice names** (#1490, CU75): the master plan
  section is `Identification`. Critical examples include `managements`.
  Process practices are `Organization / knowledge base`, `Identification`,
  `Traceability`, and `Pyramid / fail-fast`. The testing ADR cites
  `pyramid / traceability`. The topology note names
  `managements/budgets/documents/dashboard`. The merge-gate note says
  `budget pickers`. The missing-cadastral check says
  `cadastral designation` and `Bruno without a cadastral designation`.
  Retired filenames such as `crud-gestiones.spec.ts` stay. The visible
  label `/nomenclatura catastral/i` stays. The planned route sketch in
  ADR-005 stays.
- **English registry path names** (#1490, CU75): the role section is
  `RoleController`. The documents-due path variable is
  `{idSubmittedDocument}`. The auxiliary-deed check reads `idDeed`.
  Path variables `{idRole}` and `{idUser}` stay. The PDF filename
  `documentos_vencer.pdf` stays. The live path
  `/api/v1/reports/documents-due/` stays.
- **English living-spec contract names** (#1490, CU75): the tie-break
  scenarios say `a management`. The tie test requests `current-status`.
  The Bruno controller list names `Management`, `AuxiliaryProtocol`, and
  `Report`. Reliability fixtures are `procedure-type`. Person save prose
  says `person create` and `the new-person dialog`. The field example is
  `name: must not be blank`. Spec ids `estado-actual-tie-break`,
  `estado-actual-tie-test`, `bruno-zero-coverage-controllers`,
  `e2e-test-reliability`, `persona-validation-error-toast`, and
  `api-error-toasts` stay. The Playwright filename
  `TS-0015-personas-clientes-workflow.spec.ts` stays, along with `DNI`.
- **English report collection names** (#1490, CU75): the current-status
  collection is `05-current-status.yml`. The report collections are
  `11-documents-due-unknown-404.yml`, `12-document-debt.yml`, and
  `13-document-debt-unknown-404.yml`. The documents-due path variable is
  `{idSubmittedDocument}`. The living scenario is `Unknown documents-due id`.
  Spec id `reportes-404` stays. PDF filenames `documentos_vencer.pdf` and
  `deuda_documentos_` stay. The live paths `/current-status`,
  `/reports/documents-due`, and `/reports/document-debt` stay.
- **English optional heading and live deed-status constant** (#1490, CU75):
  the omitted-client budget heading is `Budget CU01 pg optional`. The
  template concept is `Honorarios Create E2E`. The deed-status comment
  names `BusinessConstants.DeedUnsigned`. Stored status `BORRADOR` stays,
  along with the seed-like name `Honorarios E2E` and the visible status
  `/Sin Firmar/i`.
- **English payment edit locals and registry note id** (#1490, CU75):
  the payment method update test is `shouldUpdatePaymentMethodWhenEditingPayment`.
  Null-field edit tests are `shouldHandleEditPaymentWithNullAmount`,
  `shouldHandleEditPaymentWithNullDate`, and `shouldHandleEditPaymentWithNullNotes`.
  Negative amount rejection is `shouldRejectNegativeAmountInEditPayment`.
  The registration-draft note test id is `input-notes-registry`. Stored payment
  methods `Efectivo` and `Transferencia` stay, along with message keys
  `fields.observacionesRegistro` and `observar`, and the visible status
  `/Generada/i`.
- **English credential parameters and status locals** (#1490, CU75):
  the admin login parameter is `password`. The route-guard login
  parameter is `password`. Invalid login fixtures are `invalid_user`
  and `invalid_password`. Navigation and helper roles use `employee`.
  The paid-status test is `shouldCalculateStatusPaymentPaid`. Payment
  edit is `shouldEditPayment`. Registration-draft rejection is
  `shouldRejectSubmissionWhenNotGenerated`. The auxiliary folio local
  is `auxiliaryType`. The auth fixture is `employeeUser` with name
  `employee1`. Report filename titles name the monthly and tax sworn
  statements. Stored role `EMPLEADO` stays, along with stored status
  `Generada`, visible text `Complete usuario y contraseña`, the button
  label `/iniciar escritura/i`, and PDF filenames `ddjj_mensual_` and
  `ddjj_rentas_`. Message keys `fields.contrasenia` and
  `fields.nuevaContrasenia` stay.
- **English found locals and report dialog keys** (#1490, CU75): the
  procedure-type edit local is `foundProcedureType`. The person-procedure
  delete local is `found`. The folio status constant is `STATUS_USED`.
  Sworn-statement tests are `shouldGenerateMonthlySwornStatementPdf`,
  `shouldGenerateTaxSwornStatementPdf`,
  `shouldRejectMonthlySwornStatementWithMonthOutOfRange`, and
  `shouldRejectInvalidMonthForSwornStatements`. Protocol dialog keys are
  `index-book`, `monthly-sworn-statement`, and `tax-sworn-statement`.
  Role module locals are `MODULE_VALUES` and `toggleModule`. The stored
  folio status `Utilizado` stays, along with PDF filenames `libro_indice_`,
  `declaracion_jurada_mensual_`, `declaracion_jurada_rentas_`, and
  `ddjj_mensual_`. The role field `modulos` stays, along with module
  values `administracion` and message keys `fields.modulos`,
  `reports.libroIndice`, `reports.ddjjMensual`, and `reports.ddjjRentas`.
- **English sign and delivery locals** (#1490, CU75): the deeds page
  uses `UNSIGNED_STATUS`, `signMutation`, and `signId`. The delivery
  flag is `allDelivered`. Audit lists are `records`. The stored status
  `Sin Firmar` stays, along with message keys `errorFirmar`,
  `firmarEscritura`, and `firmarConfirmTitle`.
- **English verification identifiers** (#1490, CU75): the testimony
  controller field is `generationVerificationService`. The verification
  test class is `TestimonyVerificationServiceTest`. Withdrawal rejection
  is `shouldRejectWithdrawalWhenTestimonyNotFound`. The JPA nested class
  is `LoadJpaList`. The audit fixture is `record2`. The index-book test
  is `shouldGenerateIndexBookPdf`. The coverage guard is
  `shouldNotReferenceNonexistentServicesPackageInPomExclusions`. The
  forbidden package fragment `servicios` stays, along with operation id
  `getCostosByTypeProcedure` and the report path `/api/v1/reports/index-book`.
- **English transition fixtures** (#1490, CU75): workflow fixtures use
  `Initial`, `Intermediate`, and `Unreachable`. The history note is
  `Initial status change`. Notebook locals are `firstBatch`,
  `secondBatch`, `withReassignedFolio`, and `discontinuous`. The
  substitution lookup takes `substitution`, and the fetch comment says
  the entity serializes while the entity manager is closed. Stored
  status `En proceso` stays, along with folio statuses `New` and
  `Errose`.
- **English date parameters and note fixtures** (#1490, CU75): date
  checks use `startDate`, `endDate`, and `today`. Note fixtures use
  `notes`. Substitution comments name the notary and substitute and the
  start and end date range. The tracker case degrades without
  post-signing nodes. Punctuation checks `¿` and `¡` stay, along with
  the visible labels `Suplencias`, `nueva suplencia`, and `registrada`,
  the nav key `suplencias`, and the stored statuses `Testimonio Generado`
  and `Gestion con Escritura Firmada`.
- **English sample names** (#1490, CU75): budget and people fixtures
  use `Ana Latest` and `Bruno Recent`. Person validation uses `Other`,
  `Duplicate`, and `New`. Summary fixtures use `Summary IT`,
  `CaseSummary`, and `Summary Management IT`. The report display says
  procedure-document list. Dedup fixtures use `Duplicate`, `Another`,
  and `Paged`. Person names `Ana`, `Bruno`, and `Perez` stay, along with
  the default-locale checks `/creada/i` and `/ya existe/i`.
- **English workflow stub heading** (#1490, CU75): the tracker comment
  quotes the seeded label `Testimonio Ingresado a Inscripcion`. The E2E
  stub uses `Gestion con Escritura Firmada`, `Testimonio Generado`,
  `Testimonio Ingresado a Inscripcion`, and `Testimonio Retirado`. The
  synthetic heading is `Reentry loop fixture`. Stored statuses `Iniciada`
  and `En Tramite` stay.
- **English workflow assignment copy** (#1490, CU75): the assignment
  request names the active workflow, an integer, and unassigning the
  current one. The conflict says the selected workflow is not active.
  The operation assigns or unassigns a workflow on a procedure type.
  The inactive fixture is `Inactive workflow`. Stored procedure type
  `Compraventa` stays.
- **English reentry comments** (#1490, CU75): the Postgres reentry test
  names `submitted_documents`, `released`, and `observed`, and its display
  says against the real schema. The concept regression comment names
  `enabled`. Stored document type `Certificado de Dominio` and stored
  role `Cliente` stay. The message key `liberado` stays.
- **English CU10 comments** (#1490, CU75): the external-entity read model
  describes the course of events, the start date, the notary in charge,
  and external entities. The management query display says start date.
  The receipt display says PDF receipt. Stored procedure type
  `Compraventa` stays.
- **English hash and movement names** (#1490, CU75): the legacy MD5 helper
  uses `plainText`, `md5Hex`, `lowNibble`, and `highNibble`. Testimony
  movement tests say `shouldListTestimonyMovementsWhenRequested`,
  `shouldReturnEmptyListOfMovements`,
  `shouldCreateCopyWhenTestimonyHasNoMovements`, and
  `shouldListTestimonyMovementsWhenSchemaMatchesEntities`. The movement
  list variable is `movements`. The receipt heading is
  `Budget receipt E2E`. Stored status `PENDIENTE` stays, along with the
  message key `movimientosTestimonio`.
- **English leftover fixture words** (#1490, CU75): document costs use
  `amountsToPay`. Payment notes say `Test payment`. Deed search uses
  `number`. The non-numeric management path is `not-a-number`. Synthetic
  trace statuses are `Trace Started`, `Trace In Progress`, and
  `Trace Closed`. Document fixtures are `Ownership report` and
  `Debt clearance`. The debt comment says `Total debt`. Stored status
  `Archivada` stays.
- **English numbering names** (#1490, CU75): auxiliary deed numbering uses
  `findMaxAuxiliaryDeedNumber` and `calculateNextAuxiliaryNumber`. The
  withdrawal test is `shouldRejectWithdrawalWhenNotInscribed`. Concept
  fixtures are `ConceptFree_InUse`, `ConceptUsed_InUse`,
  `ConceptEditBlocked`, `ConceptEditAllowed`, and `ConceptDeleteBlocked`.
  Comments name `idPerson`, `HistoryDeleteIntegrationTest`,
  `identification type`, `type=1`, and `folio types`.
- **English local identifiers** (#1490, CU75): the payments page uses
  `PaymentsPage`, `PAYMENT_FIELD_NAMES`, `paymentReceipt`, and
  `handleIssueReceipt`. Reports take `idPayment`. Managements use
  `STATUS_ARCHIVED`. Document lists use `documentTypes`. Auth tests say
  `type ADMIN`, `type ADMINISTRADOR`, and `type EMPLEADO`. Property,
  payment, history, and auxiliary-folio tests use English method and
  variable names. Stored status `Archivada`, seed label
  `Protocolo Auxiliar`, filename `recibo_pago_`, and message keys
  `pagos`, `estadoSinPagos`, `emitirRecibo`, `saldoExcedido`,
  `selectTipo`, and `editDocumento` stay.
- **English helper names** (#1490, CU75): deed tests use `folioWithNotary` and
  `shouldRejectSaveWhenGapIsNotJustified`. Archive tests use
  `createProcedureTypeWithWorkflowToArchived` and `transition`. Template
  tests use `createProcedureTypeWithTemplate` and `conceptName`. The stamp
  duty fixture is `Stamp duty`. The amount field test id is
  `input-amount-to-pay`. Stored status `Archivada` and seed concept
  `Honorarios` stay, along with the message key `importeAPagar`.
- **English property words** (#1490, CU75): the property form state uses
  `volumeFolioLandRecord` and `boundaries`. Test ids are
  `input-cadastral-designation`, `input-fiscal-appraisal`, and
  `input-boundaries`. Fixtures use `Without designation`, `Another street`,
  `Null`, `Empty`, `Blank`, `North, South, East, West`, `Fake Street`,
  `Street without designation`, `Siempreviva Avenue`, `Volume 1 Folio 2`,
  `DEED-001`, and `not a payment`. The registration-draft display says
  `cadastral/registry`. Message keys `inmuebles`, `nomenclatura`,
  `domicilio`, `valuacionFiscal`, `matricula`, `tomoFolioFinca`,
  `linderos`, and `registralData` stay, along with `Pérez`, `Soltero`,
  `Firmada`, and `Efectivo`.
- **English address fixtures** (#1490, CU75): property and person fixtures use
  `Fake Street`, `Main Street`, `Street`, `Apartment`, `Floor`,
  `Corrientes Avenue`, and `Designation`. The extra concept is
  `Additional fee`. Payment notes are `First payment`, `Edited`, and
  `Partial payment`. Sample names are `roleValid655` and `typeValid655`.
  Process-payment tests are `ProcessPaymentTests`. Stored methods
  `Efectivo`, `Transferencia`, and `Cheque` stay, along with `Pérez` and
  the role field `modulos`.
- **Testing process architecture (MTP + living inventory)** (#1511, CU76):
  `TEST-PLAN.md` is the Master Test Plan for all levels; added
  `PROCESS-ARCHITECTURE.md` and generated `INVENTORY.md` with
  `validate-testing-inventory.py` in preflight. ADR-006 and Constitution §7
  document enforced JaCoCo 80%/65% and Vitest raise-only floors. Nine unprefixed
  Playwright specs renamed to `TS-0130`–`TS-0138`.
- **English fixture names** (#1490, CU75): person fixtures use `Client`,
  `Lawyer`, and `Criminal`. Budget headings use `Updatable budget` and a
  `Budget` prefix. The management mock heading is `Management` and the
  transition sample is `In progress`. Audit samples use module `DOCUMENTS`.
  Helpers are `managementWithoutStatus`, `recordWithoutUser`,
  `shouldCalculateStatusWithNoPayments`, `findByModule`, and record methods
  without `Registros`. Stored statuses `PENDIENTE`, `BORRADOR`, and
  `APROBADO` stay, along with `NoPayments`, `Pérez`, and the role
  `Escribano`. Delivered-by `Cliente` stays.
- **English signing identifiers** (#1490, CU75): the signing service comment
  says `Sign a deed`. The controller field is `deedSigningService`. Tests
  name `createSignedDeed`, `nextNumber`, `next`, `nextAuxiliary`,
  `nextPrincipal`, and `count`. The gap justification is
  `Voided deed No. 6 to 8`. The integration class is
  `DeedSigningControllerIntegrationTest`. Stored statuses `Firmada` and
  `Sin Firmar` stay, along with `Protocolo Auxiliar` and `Protocolo Principal`.
- **English entity round-trips** (#1490, CU75): budget and deed setter fixtures
  use `pending`, `approved`, `signed`, `unsigned`, `voided`, and `not_passed`.
  Nested tests are `PrepareBudgetTests`, `PrepareDeedTests`, and
  `SetAttributesTests`. The concept round-trip method is `getDtoAndSetAttributes`.
  The deed comment names `setProcedures`. Stored heading `Compraventa` and
  constructor status `FIRMADA` stay.
- **English mixed phrases** (#1490, CU75): fixtures use English phrases
  (`Discount without reason`, `Discount for early payment`,
  `Surcharge for late payment`, `Item without a type`,
  `Verified by the notary`, and `hash-already-rotated-by-the-operator`).
  The missing-deed mock uses the live message `Deed not found with ID: 999`.
  The initializer display name is `default admin user`. Stored types
  `DESCUENTO` and `RECARGO` stay.
- **English updated phrases** (#1490, CU75): tests name updates in English
  (`Updated name`, `Updated name ok`, `Updated item`, `Updated payment`,
  `Updated description`, and `Updated management IT`). Deed bodies say
  `Signed deed for a testimony movement` and `Signed deed for a testimony copy`.
  The helper is `createSignedDeed`. The notebook result is `created`.
  Stored status `Firmada` stays.
- **English synthetic phrases** (#1490, CU75): tests use English phrases and
  local names (`Name`, `No data available`, `Client A`, `Employee B`,
  `PAID`, `CANCELLED`, `Approved by the client`, `notes only`,
  `budget cycle IT`, `procedure cycle IT`, and `template cycle IT`).
  The budget repository names `statuses`, `uniqueHeading`, `created`, and
  `approved`. The workflow-trace comment names the procedure type and
  statuses. Stored labels `PENDIENTE`, `BORRADOR`, `APROBADO`, `RECHAZADO`,
  `En proceso`, and `Compraventa` stay, along with `Juan García` and
  `Ana López`.
- **English synthetic labels** (#1490, CU75): form and picker fixtures use
  English (`Name`, `The name is required`, `Without hyphens`, `Notes`,
  `Too long`, `Status`, `Choose a status`, `Client`, `Ana Latest`,
  `Bruno Recent`, `New Compraventa`, and `Status E2E 42`). Sale headings
  use `Sale property`, `parcel sale`, `Sale budget`, `Compraventa property`,
  and `Urban Compraventa property`. Stored labels `CUIT`, `Borrador`,
  `Aprobado`, `Rechazado`, `Iniciada`, `Pendiente`, `Hipoteca`, `Donación`,
  `APROBADO`, `FIRMADA`, and `Compraventa` stay, along with `Juan`.
- **English synthetic chrome fixtures** (#1490, CU75): tests name dialog and
  header props in English (`People`, `Client management`, `New person`,
  `Delete person`, `Edit budget`, `Budget summary`, `Items`, `Template`,
  and `Managements`). Table fixtures use `Name`, `Internal`, `Edit`, and
  `Nothing`. Deed fixtures use `Deed for Compraventa`, `Deed of sale`,
  `Sale deed`, `Notarial body`, `Expenses`, and `Before delete`. Catalog
  labels `Cerrar`, `Saltar al contenido`, `Honorarios`, `Comprador`, and
  `Poder` stay, along with stored `Compraventa` and `Firmada` and the
  person names `Juan García` and `Ana López`.
- **English synthetic notes** (#1490, CU75): tests use English notes and
  headings (`New name`, `Listed document`, `Missing grantor signature`,
  `Missing survey plan`, `Missing cadastral certificate`, `without client`,
  and `Case`). The last management number query aliases `latestManagement`.
  The live reentry table name is `submitted_documents` (Flyway keeps the
  historical `documentos_presentados` rename). The cadastral label
  `nomenclatura catastral` stays.
- **English type fixture names** (#1490, CU75): Java tests name synthetic
  types in English (`Type_unused_`, `Type #655`, `Case summary type`,
  `Type Trace Test`, and `Base fee`). The traceability registry names the
  live classes `BudgetEntityTest`, `DeedEntityTest`, `PaymentEntityTest`,
  `ProcessPaymentServiceTest`, `ProcedureTypeWorkflowAssignmentTest`, and
  `AuditRecordServiceTest`. The untyped person fixture is `No Type`.
  Stored labels `Compraventa`, `Firmada`, `DNI`, and `Pasaporte` stay.
  The named-query hygiene list still forbids `TipoDeTramite.` and
  `DocumentoPresentado.`.
- **English remaining fixture names** (#1490, CU75): Java tests use English
  synthetic names (`Archived management`, `Notarial power`, `Grantor`,
  `FindThisStatus`, `Procedure_unused_`, `Updated notes`, `Cycle IT Status`,
  `Missing status`, `Substitute IT`, and `Ana Notary`). Stored label
  `Archivada` stays. Test methods say `shouldReturnAllManagementStatuses`
  and `shouldReturnCurrentStatusFromHistoryWhenRowsExist`.
- **English synthetic test fixtures** (#1490, CU75): Java tests use English
  notes and names (`Updated`, `Pending signature`, `Full cycle`,
  `First semester`, `With client`, `Notary office`, `Active`, `Protocol`,
  `Substitute`, and `Special folio`). Stored labels `Honorarios` and
  `Protocolo Auxiliar` stay, along with the legacy budget status `Activo`
  and the given names `Juan` and `Pedro`.
- **English named-query and history specs** (#1490, CU75): the history-order
  scenario says History, not Bitácora. The named-query spec lists the
  forbidden Spanish prefixes `Escritura.`, `Usuario.`, `Presupuesto.`, and
  `Persona.`, and the tail `Folio.findByNumero`. English prefixes `Deed` and
  `Person` stay allowed.
- **English workflow test fixtures** (#1490, CU83): workflow tests use
  English identifiers (`nodeIntermediate`, `statusInProgress`, `status1`)
  and synthetic names (`Start`, `In progress`, `Finished`, `Start → Review`,
  `Cost type`, `Procedure type`). Stored label `Cliente` stays.
- **English folio status comment** (#1490, CU28): the folio class states the
  business rule in English and names the stored statuses `Nuevo`,
  `Utilizado`, `Errose`, `no pasó`, and `Asignado a cuaderno`. The state
  diagram note quotes `Documentacion Completa`.
- **English workflow-tracker seed names** (#1490, CU83): the dashboard
  workflow tracker quotes the seeded statuses `Testimonio Generado`,
  `Testimonio Ingresado a Inscripcion`, `Testimonio Retirado`,
  `Documentacion Completa`, and `Gestion con Escritura Inscripta`.
  Procedure types stay `Compraventa`, `Donacion`, and `Hipoteca`. The
  workflow name `Workflow de Gestión Estándar` stays.
- **English business-constant names** (#1490, CU75): `BusinessConstants` field
  names are English (`DeedSigned`, `ManagementStatusInitial`,
  `DocumentationComplete`, `UserEmployee`, and the rest of that interface).
  Stored values stay, including `Firmada`, `Iniciada`, `Documentacion Completa`,
  `Empleado`, `Folio Errose`, `Modificar Persona`, and `Escribano`. Deed status
  `Inscripta` stays.
- **English registration-draft statuses** (#1490, CU82): comments, the
  lifecycle diagram, and the engineering gap row name the stored statuses
  `Generada`, `Presentado para inscripción`, `Observado`, and `Inscripto`.
  Constant names are `RegistrationDraftGenerated`, `RegistrationDraftSubmitted`,
  `RegistrationDraftObserved`, and `RegistrationDraftRegistered`. Deed status
  `Inscripta` stays. Released changelog text that recorded the older
  participles stays.
- **English sequence check** (#1490, #1508): `test_gestion_sequences.py`
  requires the English sentences for no return from `Observado` and for
  re-entry that does not move the management.
- **Flyway `V45` amended (#1499, Owner-approved exception).** It failed on every fresh database because the legacy table `identificaciones` was never created by Flyway. It now renames the table where it exists and creates `identifications` where it does not. A database that already applied the original `V45` must run `flyway repair` once (checksum changed). The reports page (`/dashboard/reports`) is restored; it had been hidden by an unanchored `.gitignore` rule.

- **Testimony and document re-entry sequences** (CU08, CU11, CU43, CU44, #921):
  `GESTION.md` (`DOC-GESTION`) verifies a testimony before entry, records
  inscription before withdrawal, and sequences re-entry of a testimony and
  of a document. Neither re-entry moves the management. The engineering
  catalog is `EVALUACION-INGENIERIA.md`. The step text is English. The
  archive query flag stays `confirmado`.
- **English incomplete-body Bruno assertions** (#1490, CU75):
  the incomplete PUT collections expect `is required`, matching
  `RequiredFields`. The requests stay `PUT /api/v1/testimonies/{id}`,
  `PUT /api/v1/procedures/{id}`, and
  `PUT /api/v1/procedure-types/{id}/workflow`. Released changelog text
  that records the earlier Spanish phrase stays.
- **English business-finding index** (#1490, CU75):
  `docs/openspec/explore.md` states the finding↔Issue record in English.
  The filename stays. Change slugs stay, including
  `folio-vinculacion-escritura`, `gestion-archive-deuda-check`, and
  `presupuesto-plantillas-y-catalogo-items`. Finding identifiers stay,
  including `GestionArchiveDebtService`, `importeAPagar`,
  `ManagementHistory.fecha`, `"Efectivo"`, and `"observaciones"`.
  Issue numbers and the use-case filename `CU84 - Login.md` stay.
- **English source comments** (#1490, CU75): the redirect comment in
  `frontend/next.config.ts` says administration pages, and the status-map
  comment names the catalog namespace `administracion.estadosGestion`. Stored
  management statuses such as `en tramite` and `gestion con escritura firmada`
  stay. Canonical routes `/dashboard/administration/items` and
  `/dashboard/administration/audit` stay.
- **English leftover requirement titles** (#1490, CU75):
  `frontend-i18n-page-coverage`, `icon-button-accessible-names`,
  `k6-load-test-restore`, `report-pdfs`, and `api-request-dto-binding` name
  the live contract in English. Spec ids stay. Label keys `suplencias`,
  `reportes`, `auditoria`, and `administracion` stay, along with module keys
  `personas` and `tramites`, default-locale names “Editar” and “Eliminar”,
  and the selector `/editar/i`. The document-debt PDF text is `Payment pending`.
  Server-side auditing is `AuditAspect`. The SAD roadmap label is `Live status`.
  Stored role `Escribano` stays.
- **English case-process design** (#1490, CU75):
  `docs/200-architecture/203-design/GESTION.md` states the central process in
  English. The filename stays. Official use-case titles stay (`CU01 Preparar
  presupuesto`, `CU02 Iniciar gestión`, `CU15 Procesar pago`). Stored nodes
  stay (`Iniciada`, `En Tramite`, `Documentacion Completa`, `Gestion con
  Escritura Sin Firmar`, `Gestion con Escritura Firmada`, `Testimonio
  Generado`, `Testimonio Ingresado a Inscripcion`, `Testimonio Retirado`,
  `Archivada`). Seeded edge descriptions stay, including `Se asignan trámites
  y comienza la gestión` and `La gestión se archiva sin escritura`. The
  archive query flag `confirmado` stays. Registration-draft stored statuses
  are `Generada`, `Presentado para inscripción`, `Observado`, and `Inscripto`.
  Deed statuses `Anulada`, `No Paso`, and `Inscripta` stay. Actor `Escribano`
  stays. Stored procedure types `Sucesion` and `Poder` stay.
- **English document, reentry, budget-person, and navigation requirement prose** (#1490, CU75):
  `submitted-document-update`, `testimony-reentry-data`,
  `budget-person-response`, `backend-test-isolation`, `admin-route-guard`,
  `dashboard-nav-canonical-routes`, and `next-middleware-to-proxy` state
  requirements in English. Spec ids stay. Live names stay: `procedureId`,
  `cardNumber`, `observedByRegistry`, `personId`, `amount`,
  `pendingBalance`, `/dashboard/administration`, `/dashboard/substitutions`,
  and `frontend/src/proxy.ts`. Message key `reingresar` stays, along with
  sidebar label keys `suplencias`, `reportes`, `auditoria`, and
  `administracion`, test ids `nav-suplencias`, `nav-reportes`, and
  `btn-reenter-testimony`, and stored roles `ADMINISTRADOR`, `ESCRIBANO`,
  and `EMPLEADO`. Official title `Login al sistema` stays.
- **English payment, template, and property-appraisal requirement prose** (#1490, CU75):
  `pago-limite-saldo-pendiente`, `pago-presupuesto-picker-saldo`, `pagos`,
  `presupuesto-from-template`, `procedure-fk-hydration`, and
  `inmueble-valuacion-fiscal` state requirements in English. Spec ids stay.
  Live names stay: `amount`, `pendingBalance`, `paymentMethod`, `idBudget`,
  `procedureTypeId`, `fiscalAppraisal`, and `properties.fiscal_valuation`.
  Stored label `Efectivo` stays, along with the message keys `saldoExcedido`
  and `saldo.pending`. Official titles `Procesar pago` and
  `Gestión de Inmuebles` stay. Stored deed status `Firmada` stays.
- **English authorization, payment-summary, and archive requirement prose** (#1490, CU75):
  `api-authorization`, `pago-presupuesto-gestion-summary`,
  `presupuesto-tramite-relation`, `gestion-asignacion-suplencia`,
  `deed-management-entity-null-safety`, and `gestion-archive-debt-check`
  state requirements in English. Spec ids stay. Live names stay:
  `/api/v1/procedure-types`, `idBudget`, `pendingBalance`,
  `managementHeading`, `fk_id_budget`, `procedureList`, `fkIdSubstitute`,
  `notaryPersonId`, `pendingDebtAtArchiving`, `getDto()`, and
  `personNotary`. Stored labels `Administrador`, `Escribano`, `EMPLEADO`,
  `ESCRIBANO`, and `Archivada` stay, along with the archive query flag
  `confirmado` and the notes prefix
  `Management redirected by active substitution`. Official titles
  `Iniciar Gestión` and `Modificar presupuesto` stay.
- **English workflow, history, registry, and notebook requirement prose** (#1490, CU75):
  `gestion-workflow-transicion`, `gestion-bitacora`, `minuta-inscripcion`,
  `testimonio-movimiento-inscripcion`, `tipo-documento-vencimiento-config`,
  and `cuadernos-de-folios` state requirements in English. Spec ids stay.
  Live names stay: `ProcedureType`, `ManagementHistory`, `dateEntry`,
  `dateExit`, `dateRegistration`, `registered`, `cardNumber`, `expires`,
  `dueDays`, `deliveredBy`, `registrationNumber`, `idsFolio`, and
  `idNotary`. Stored labels `Archivada`, `Generada`,
  `Presentado para inscripción`, `Observado`, `Inscripto`,
  `Asignado a cuaderno`, `Errose`, and `no pasó` stay, along with the role
  `Escribano` and the filenames `minuta_inscripcion_` and `notebook_`.
- **English protocol, payment, folder, and person requirement prose** (#1490, CU75):
  `protocolo-auxiliar`, `pagos-parciales-cuotas`, `carpetas-de-tramite`,
  `descuentos-recargos-presupuesto`, and `persona-validacion-duplicados`
  state requirements in English. Spec ids stay. Live names stay:
  `isAuxiliary`, `idFolio`, `NoPayments`, `PARTIAL`, `PAID`, `amount`,
  `identificationNumber`, and `fk_id_identification_type`. Stored labels
  `Protocolo Auxiliar`, `Protocolo Principal`, `Activa`, `Espera`,
  `Archivada`, `NORMAL`, `DESCUENTO`, and `RECARGO` stay, along with the
  archive query flag `confirmado`.
- **English deed, folio, and document requirement prose** (#1490, CU75):
  Six more living specs state requirements in English and name `registered`,
  `expires`, `dueDays`, `deliveredBy`, `dateEntry`, `dateDue`, `amountToPay`,
  and `deedId`. `GET /api/v1/folios/{id}` stays the read path. Spec ids stay.
  Stored labels `Firmada`, `Utilizado`, `Protocolo Auxiliar`, `Protocolo Principal`,
  and `no pasó` stay, along with the role `Escribano`.
- **English OpenSpec requirement prose** (#1490, CU75):
  `escritura-firma`, `presupuesto-plantilla`, `presupuesto-catalogo-items`,
  `pago-recibo`, and `persona-credencial-escribano` state requirements in
  English and name `Budget`, `{idPayment}`, and `notaryRegistrationNumber`.
  Spec ids stay. Stored statuses `Sin Firmar` and `Firmada`, the role
  `Escribano`, and the receipt filename `recibo_pago_` stay.
- **English use-case diagram structure** (#1490, CU75):
  Module titles, actor names, and association labels in
  `Casos de Uso/*.puml` are English. Official `CUxx` titles and the filenames
  stay. The stored role `Escribano` stays. The folder name `Casos de Uso` stays.
- **English CU sequence steps** (#1490, CU75):
  The 83 use-case sequence diagrams under `Secuencias/` describe each step in
  English. The participant is `Notaire system`. CU titles and filenames stay.
  The stored role `Escribano` stays, along with `Protocolo Auxiliar`,
  `Asignados a cuaderno`, `Presentado`, and `Finalizado / Entregado`.
- **Case-process documentation and a traceability check** (CU76, CU02, #921):
  `GESTION.md` is the step-by-step analysis of a gestión. The legacy activity
  diagram matches the seeded workflow. `check-doc-traceability.py` rejects an
  OpenSpec change whose traceability claims a permanent document the diff does
  not touch (Constitution §8).
- **Documents-due report allowlist uses the English path parameter** (CU76, #1490):
  `GET /api/v1/reports/documents-due/{idSubmittedDocument}`.
- **E2E reliability inventory uses the English spec names** (CU76, #1490):
  payments, users/notaries, substitutions, and reports.
- **English management state diagrams** (#1490, CU75):
  `workflow-gestion.puml` calls `ManagementController` at
  `POST /api/v1/managements` and inserts into `deed_managements` and
  `audit_records`. The activity diagram is in English. The stored status
  `No Pasó` and the role `Escribano` stay. The folder name
  `Diagrama de Estados` stays.
- **English login sequences** (#1490, CU75):
  The seven login diagrams call `UserController` at `POST /api/v1/users/login`
  and `GET /api/v1/users`. The JSON field is `valid`. The lockout message is
  `Account temporarily locked after too many failed attempts.` CU titles in
  the other sequence files stay.
- **English architecture diagrams** (#1490, CU75):
  The core model, package diagram, target architecture, and legacy Swing
  diagram name `DeedManagement`, `Person`, `Budget`, `adapter.in.web`, and
  `business`. The target stack is Java 26. The stored role `Escribano` stays.
  `dni` stays on the person sketch. Sequence filenames under `Secuencias/` stay.
- **English OpenSpec adaptations guide** (#1490, CU75):
  `docs/openspec/NOTAIRE-ADAPTATIONS.md` describes installation, gates, and
  orchestration in English. The backend prerequisite is Java 26. Required
  headings `Objetivo`, `Reglas de negocio`, `Módulos afectados`, and
  `Actualizar documentación permanente` stay. Archived change slugs stay.
- **English security asset names** (#1490, CU75):
  The system threat model, cybersecurity program, monitoring note, and
  trust-boundary diagram name `Person`, `Deed`, `Testimony`, `Budget`,
  `Payment`, and `audit_records`. Owner prose says Security Officer and
  Development Team. The policy owner `Oficial de Seguridad`, CU78, and the
  actor list stay. The GitHub page's 2026 sample clicks `New procedure`.
  The 2014 monolith sample still shows `escrituras`.
- **English SDD presentation** (#1490, CU75):
  The slide deck and `docs/presentations/README.md` describe OpenSpec and
  Spec Kit in English. Archived paths, `Property.valuacionFiscal`, and the
  name Matías Miguez stay. The phrase `Use Case (Caso de Uso)` stays.
- **English local-model setup scripts** (#1490, CU75):
  `setup-omlx-codex.sh` and `stop-omlx.sh` report progress in English.
  Presets, profiles, ports, and the `: applied` patch marker stay.
- **English local-model guide** (#1490, CU75):
  `local-ai/README.md` describes the oMLX stack, presets, and Metal memory
  limit in English. Model names, profiles, and commands stay. The Cursor Cloud
  fleet still does not use `local-ai/`.
- **English documentation index and backend README** (#1490, CU75):
  `docs/README.md` and `backend-api/README.md` name the lifecycle, the method,
  and the REST service in English. Filenames `CICLO-DE-VIDA.md` and
  `INGENIERIA.md` stay. The constitution step `Actualizar documentación permanente` stays.
- **English postgres business metrics** (#1490, CU75):
  The exporter queries count `deeds`, `budgets`, `users`, and `people`.
  The postgres dashboard reads `deeds_total`, `budgets_total`, `users_total`,
  and `people_total`. Deed status stays the stored label. The exporter role
  remains `pg_monitor` only.
- **English repository README** (#1490, CU75):
  The root README and the frontend module table name managements, budgets,
  people, deeds, payments, and administration. The stack is Java 26 and
  Spring Boot 4.1.1. The name Matías Miguez stays. The required phrase
  `Use Case (Caso de Uso)` stays.
- **English migration samples** (#1490, CU75):
  Flyway skill and database-migration examples insert into `concepts` and
  index `users(username)`. The deed-lifecycle note uses the stored status
  `Firmada`. Applied migrations V1–V44 stay unchanged.
- **English diagram and workflow samples** (#1490, CU75):
  The PlantUML skill examples use `Person`, `Deed`, `deeds`, and `DeedDto`.
  The workflow skill sample is `users.spec.ts` and `User authentication`.
  The person name `Ana García` stays. The required heading `Use Case (Caso de Uso)` stays.
- **English testimony labels** (#1490, CU75):
  The English catalog says `Testimonies`, `Generate testimony`, and
  `No testimonies registered`. Message keys stay `testimonios`. The Spanish
  catalog stays `Testimonios`. The copy filename `testimonio_*_copia.pdf` stays.
- **English lifecycle pointers** (#1490, CU75):
  The legacy state-diagram folder, the design index, the SAD runtime view,
  the system threat model, and the GitHub board note name management, deed,
  and budget. Stored statuses and the folder name `Diagrama de Estados` stay.
- **English lifecycle and engineering method** (#1490, CU75):
  `CICLO-DE-VIDA.md` and `INGENIERIA.md` are written in English. Sequences
  call `POST /api/v1/managements/{id}/transition`, `GET /api/v1/deeds`, and
  `POST /api/v1/payments`. A payment over the balance is an empty 409.
  Stored statuses such as `Iniciada`, `En Tramite`, `Firmada`, and
  `Workflow de Gestión Estándar` stay. The guide title and the name
  Matías Miguez stay.
- **English names kept with status badges and the app-shell skeleton** (#1490, CU75):
  The #1366 `StatusBadge` and the #1359 app-shell skeleton land on the English
  screens. Management badges read `currentStatus`. Testimony badges use
  `testimonyStatus`. Stored statuses such as `Pendiente` stay.
- **English names kept with locale formatting and deed pickers** (#1490, CU75):
  The #1355 `useFormat` hook and the #1340 deed picker land on the English
  screens. Testimony, registration-draft, and folio forms use `DeedPicker`
  against `/deeds`. `usePeople()` and `useDeeds()` stay removed. Stored
  statuses `Firmada` and `Sin Firmar` stay.
- **English skipped-case titles** (#1490, CU75):
  Skipped and smoke titles say reports, deeds, concepts, copies, users, and
  administration. Icon alts `Agregar`, `Cancelar`, and `Guardar`, and the
  visible labels `declaración jurada` and `Descargar PDF`, stay.
- **English agent skill prose** (#1490, CU75):
  The motion-design skill and the Bruno collection skill are written in
  English. Library names, the install commands, and the Bruno file layout stay.
- **English entity debug names and test titles** (#1490, CU75):
  `User.toString` starts with `User[` and `Concept.toString` starts with
  `Concept[`. Tour and workflow titles say deeds, properties, copies,
  reports, concepts, users, payments, and folders. Visible Spanish labels
  and stored statuses stay.
- **English helper and architecture names** (#1490, CU75):
  E2E helpers and comments name `User` and `History`. The SAD domain
  diagram, the security data table, and the API coverage notes use those
  class names. The Java architect sample uses `users` and `DtoUser`. The
  stored status `Sin Firmar` and the default history status `Iniciado` stay.
- **English test titles and PDF fixtures** (#1490, CU75):
  User and history tests name the live types `User` and `History`, and the
  user field `active`. PDF samples use `No concepts`, `Properties`,
  `History`, and `Notes`. Accent fixtures `Símbolos` and `línea`, the
  procedure name `Compraventa`, and the status `En Tramite` stay.
- **English persistence and report names** (#1490, CU75):
  Creating a history row reports `Could not create an entity of type: History`.
  Orphan checks name `User`, `History`, and `DeedManagement`. The management
  history PDF is titled `History of management`. The English catalog says
  `Case type` for document costs. Stored status labels stay.
- **Spanish catalog nouns restored** (#1490, CU75):
  `es.json` again says `Usuario`, `Escritura`, `Testimonio`, `Copia`, `Pago`,
  and `Historial` where class names had replaced those words. The English
  catalog stays in English. A copy rejected because of a registered movement
  reports `The testimony already has a registered movement and does not accept new copies.`
- **English lazy-association names** (#1490, CU75):
  The JPA lazy-loading guide lists the live collections `testimonyMovementList`,
  `procedureList`, `itemList`, `auditRecordList`, and `nodes`. Samples join
  `Testimony.testimonyMovementList`. Java display names say `typeFolioId`,
  `budgetId`, and concepts. Stored statuses such as `Nuevo`, `Utilizado`,
  and `Errose` stay.
- **English login contract in the security guides** (#1490, CU75):
  The authentication, validation, and SQL-injection guides, plus the login
  test reports, use the live field `valid` and the JSON names `name` and
  `password`. Samples use `User`, `users.username`, and `deed_managements.number`.
  Administrator write paths match `SecurityAndCorsConfig`. A locked account
  is documented as HTTP 429. Stored role names such as `Escribano` stay.
- **English Playwright case identifiers** (#1490, CU75):
  Folio and deed selects use `select-folio-type` and `select-folio-deed`.
  Case fixtures name `folioType` and `copyNumber`. Seed failures, payment
  locals, API suite titles, and the management picker accessible name are
  English. Stored statuses such as `Firmada` and `Sin Firmar` stay, and
  default-locale labels such as `/número/i` and `/archivar de todos modos/i` stay.
- **English validation messages and DTO guide** (#1490, CU75):
  A missing auxiliary folio reports `The auxiliary folio where the deed starts is required`.
  A workflow node update with no fields reports `provide at least one field to update`.
  An invalid management-status DTO reports `Invalid DTO`. The budget search
  description says `for example`. The DTO mapping guide uses the live fields
  `budgetTemplatePK`, `idProcedureType`, `valid`, and `operationDetail`.
  Stored labels such as `Protocolo Auxiliar`, `Compraventa`, and `Honorarios` stay.
- **English audit and Playwright identifiers** (#1490, CU75):
  The audit module filter is `moduleFilter` and its test id is
  `select-audit-module`. Playwright uses the live ids `select-status-folio`,
  `select-deed-testimony`, and `select-document-type`. Seed locals use
  `procedureType`, `documentType`, and `status`. Stored labels such as
  `Nuevo`, `Errose`, `Utilizado`, `Compraventa`, and `Honorarios` stay.
  Default-locale labels such as `nuevo cuaderno` stay.
- **English Java test identifiers** (#1490, CU75):
  External-entity, document-reentry, and management-transition tests use
  English method and variable names such as `createExternalEntityDocument`
  and `intermediateStatus`. Budget search coverage calls `status=PENDIENTE`.
  Person name lookup stubs use `name` and `lastName`. Stored labels such as
  `Compraventa`, `Certificado de Dominio`, `Cliente`, and `Entidad Externa`
  stay.
- **English payment and workflow identifiers** (#1490, CU75):
  The payment status type is `PaymentStatus` and its cache key is `status`.
  Workflow traces use `management-workflow-trace`. The budget setter is
  `setSelectedProcedureTypeId`. Editor node data uses `type`, which the
  node component already reads. The viewer fallback label is `Node`.
  Message keys such as `fields.estado` and `estadosGestion` stay. Stored
  status samples such as `PENDIENTE` stay.
- **English testimony and request fixtures** (#1490, CU75):
  Testimony samples use `number`, `verified`, `idTestimonyMovement`, and
  `registered`. Withdraw and reentry inputs use `cardNumber`, and the
  reentry field test id is `input-reentry-card-number`. Document reentry
  samples use `idSubmittedDocument` and `reentered`. API client samples
  post `number`. Message keys such as `reingresado` and
  `fields.numeroCarton` stay.
- **English external-document identifiers** (#1490, CU75):
  The external-entity document cache key is `externalEntityDocuments` /
  `management`. The movement input is `idSubmittedDocument`. Playwright
  create buttons use the live ids such as `btn-new-person` and
  `btn-new-deed`. Default-locale labels such as `Nueva persona` and
  `Certificado de Dominio` stay.
- **English summary locals** (#1490, CU75):
  Budget and payment screens store the summary in `summary` and the
  payment state in `paymentStatus`. Message keys such as `resumen.title`,
  `gestiones.resumen`, and `estadoSinPagos` stay. The archive confirmation
  assertion matches `Archivar de todos modos`.
- **English screen test ids** (#1490, CU75):
  Playwright uses the live ids `btn-new-management`, `btn-new-budget`,
  `select-budget-management`, `btn-confirm-transition`, and
  `btn-place-on-hold`. Default-locale labels such as `Gestiones`,
  `Presupuestos`, and `Nueva gestión` stay.
- **English API collection filenames** (#1490, CU75):
  Bruno report requests use English filenames such as `01-index-book.yml`,
  `06-budget.yml`, and `10-management-history.yml`. The case summary
  request is `05b-case-summary.yml`. `CU-API-MATRIX.csv` points at those
  paths. Seeded `Compraventa` stays.
- **English Playwright identifiers** (#1490, CU75):
  Payment and people specs use `lastName`, `balanceDisplay`, and
  `check-is-client`. The people checkbox still reads
  `t("fields.esCliente")`. The default-locale label
  `Buscar por apellido...` stays.
- **English icon filenames** (#1490, CU75):
  Public icons use English names such as `save.png`, `delete.png`,
  `users.png`, `management-statuses.png`, and `budget-templates.png`.
  The folder `icons/modules` replaces `icons/modulos`. Message-catalog
  keys such as `estadosGestion` and `usuarios` stay.
- **English withdraw test id and history samples** (#1490, CU75):
  The testimony withdraw button test id is `btn-confirm-withdraw`.
  History samples live in `items-history.test.ts`. Deed-sign and
  testimony-movement fixtures use the live English sentences, and the
  stored status `Sin Firmar` stays. Search mocks match `/search`.
  Default-locale delete fallbacks such as `Error al eliminar` stay.
- **English mutation-error samples** (#1490, CU75):
  The local budget-total sample uses `calculateTotal`, `price`, and
  `quantity`. Mutation-error fixtures use the live English sentences
  `The payment amount ($10.00) cannot exceed the outstanding balance ($5.00)`
  and `The submitted data duplicates an existing record`. The default-locale
  plantilla fallback stays Spanish because the budgets page passes
  `t("items.errorNoPlantilla")`.
- **English report test names** (#1490, CU75):
  `NotebookController` stores `reportService`. Payment logs say
  `Editing payment` and `Deleting payment`. Report tests are
  `ReportServicePaymentReceiptTest`, `PaymentReceiptReportIntegrationTest`,
  and `ReportsUseCaseIntegrationTest`. The audit filter uses the module
  `Concepts`. PDF filenames such as `recibo_pago_` and `lista_documentos.pdf`
  stay Spanish. Person names and `Compraventa` stay.
- **English test fixture prose** (#1490, CU75):
  Backend and Playwright fixtures use English free-text values such as
  `Test deed`, `Test payment`, and `Standard workflow`. Stored labels and
  person names stay, including `Compraventa`, `Firmada`, and `García`.
  Visible Spanish form labels such as `observaciones` and `valuación fiscal`
  stay because the default locale is Spanish.
- **English E2E seed identifiers** (#1490, CU75):
  Playwright seed env vars are `E2E_SEED_PERSON_ID`, `E2E_SEED_BUDGET_ID`,
  `E2E_SEED_CONCEPT_ID`, `E2E_SEED_PROCEDURE_TYPE_ID`, `E2E_SEED_USER_ID`,
  and `E2E_SEED_MANAGEMENT_STATUS_ID`. `TestData` keys match the live
  payloads (`firstName`, `heading`, `propertyAmount`). Stored labels stay,
  including `Firmada`, `Soltero`, `Masculino`, `EMPLEADO`, `Habilitado`,
  `Efectivo`, `Pendiente`, and `DNI`. Spanish form labels such as
  `contraseña` stay because the default locale is Spanish.
- **English frontend-test and error-handling samples** (#1490, CU75):
  `FRONTEND-TESTING-GUIDE.md` names `managementsKeys`,
  `substitutions-reports.test.ts`, and the current `TS-nnnn` specs.
  The auth sample uses `name`, `type`, and `valid`. The seeded login is
  `users.username` / `users.password` / `users.user_type` after V37; the
  V2 script still inserts into `usuarios`. Button assertions stay
  `Nuevo` and `Cancelar` because the default locale is Spanish.
  `BACKEND-ERROR-HANDLING-STRATEGY.md` shows a `heading` validation
  message and the login field `valid`. CU73 in the auth threat model
  is the audit log. The October assessment names CU72 and CU79 in English.
- **English testing-pattern samples** (#1490, CU75):
  `docs/300-development/303-testing/TESTING-PATTERNS.md` uses `Budget`,
  `BudgetService`, and `BudgetRepositoryPort`. Management create JSON is
  `number`, `heading`, and `notes`. Budget create JSON is `date`, `heading`,
  `status` (`BORRADOR`), `propertyAmount`, and `personId`. Error samples
  read `ErrorResponse.message`. The people-form sample submits `firstName`
  and `lastName`. Visible Spanish labels on the live screen stay
  (`Nombre`, `Apellido`, `Guardar`), and message keys stay
  `personas.fields.nombre`.
- **English development setup guide** (#1490, CU75):
  `docs/300-development/301-setup/README.md` is English. The JDK line matches
  the root `pom.xml` (Java 26). The package tree names `PersonController`,
  `DtoPerson`, and `adapter.in.web`. Index examples are `people.id`,
  `deeds.id`, and `budgets.id`. The example test class is `BudgetEntityTest`.
- **English E2E test inventory** (#1490, CU75):
  `TEST-PLAN.md` and `E2E-TEST-MAPPING.md` name the suites in English
  (budgets, deed managements, documents, deeds, payments). Visible UI
  strings the tests still assert stay, including the heading `Presupuestos`
  and the quoted gaps “ver”, “libro índices”, and “declaración jurada”.
  Nav keys `suplencias` and `reportes` stay. Retired spec filenames such as
  `presupuestos.spec.ts` stay. CSV headers `CU_Nombre`, `Modulo`, `Entidad`,
  `Operacion`, and `Notas` stay because the matrix validator reads them.
- **English test-case writeups** (#1490, CU75):
  The nine cases under `docs/300-development/303-testing/test-cases/` use
  English filenames and prose (`TC-001-prepare-budget-success.md` through
  `TC-009-register-document-debts-success.md`). The stored deed status
  `Firmada` stays. Use-case ids CU01 and CU04 through CU09 stay.
- **English software architecture document** (#1490, CU75):
  The SAD uses current names (`deed_managements`, `procedures`, `budgets`,
  `User`) and the platform in the root `pom.xml` (Java 26, Spring Boot 4.1.1).
  The core diagram uses current columns, including `procedures.fk_id_budget`.
  `budgets.fk_id_tramite` stays as the column V14 removed. Stored labels
  `Escribano` and `ESCRIBANO` stay. The `Casos de Uso` folder name stays.
- **English compensation diagram** (#1490, CU75):
  The compensation spreadsheet and diagram use current table names
  (`people`, `deed_managements`, `procedures`) and current foreign keys
  (`fk_id_person`, `fk_id_notary_person`, `fk_origin_node_id`). Actions are
  `Restrict`, `Cascade`, and `Null`. `identifications` stays labeled as the
  legacy table Flyway does not create. Stored labels `Protocolo Auxiliar`,
  `Errose`, and `No Pasó` stay. The removed column `budgets.fk_id_tramite`
  stays in the legend. Filenames stay as recorded.
- **English ERD spreadsheet** (#1490, CU75):
  `Modelo Relacional Escribania - Entidades.csv` uses English headers
  (`Entity`, `Attribute`, `Description`) and English type labels (`Strong`,
  `Integer`, `Text`, `yes` / `no`). Description cells match the data
  dictionary. `users` is `Weak / strong`, as in the dictionary index. Stored
  labels such as `No Pasó` and `Workflow de Gestión Estándar` stay.
  `docs/tools/generate_erd.py` writes those English labels on the next
  regeneration.
- **English data-dictionary descriptions** (#1490, CU75):
  Column descriptions, the table index, the compensation notes, and sections
  1, 3, 6.2, and 6.3 of the data dictionary are English. Stored labels such
  as `No Pasó`, `Workflow de Gestión Estándar`, `Honorarios`, and `DNI` stay.
  Historical names `identificaciones`, `nombre`, `numero`, and `fk_id_tramite`
  stay where they name the pre-rename schema.
- **English data-dictionary column names** (#1490, CU75):
  The data dictionary, the ERD, and the compensation diagram use the V45
  column names (`fk_id_budget`, `birth_date`, `node_type`, and the rest).
  Headings, table summaries, and section 6.1 are English. Stored labels such
  as `Honorarios`, `BORRADOR`, and `Workflow de Gestión Estándar` stay.
  `identificaciones` is not a Flyway-created table. Flag cells use `Yes`.
- **English ADR index** (#1490, CU75):
  The ADR index is English. Dates for ADR-005, ADR-006, and ADR-007 match each
  decision file. The platform line matches the root `pom.xml`: Java 26 and
  Spring Boot 4.1.1. Message keys such as `suplencias` and `reportes` stay.
- **English Flyway schema-versioning decision** (#1490, CU75):
  ADR-007 is English. Dependencies are `spring-boot-starter-flyway` and
  `flyway-database-postgresql`. `V1__initial_schema.sql`, `V2__initial_data.sql`,
  and `V45__rename_remaining_spanish_identifiers.sql` exist. Applied migration
  filenames V1–V44 stay. Seeded label `Workflow de Gestión Estándar` stays.
  `spring.flyway.clean-disabled` stays `true`.
- **English database-migration decision** (#1490, CU75):
  ADR-004 is English. The live database is PostgreSQL 16 with Flyway, Hibernate
  `ddl-auto` `none`, and table `budgets`. The Liquibase sketch and the MySQL
  table `presupuesto` stay as that original plan. Applied migrations V1–V44 stay
  immutable. Stored statuses such as `BORRADOR` stay.
- **English catalog-item identifiers** (#1490, CU75):
  The budget controller field is `budgetCatalogItemsService`. The budgets page
  uses `catalogItems`, `addFromCatalogMutation`, and `handleAddFromCatalog`.
  The add button test id is `btn-add-from-catalog`. Message keys
  `items.agregarCatalogo`, `items.addedFromCatalogo`, and `items.catalogoSection`
  stay. Stored budget status `PENDIENTE` stays.
- **English API versioning decision** (#1490, CU75):
  ADR-003 is English. The live budget controller is `BudgetController` at
  `/api/v1/budgets`. Errors use `ErrorResponse` (`timestamp`, `status`, `error`,
  `message`, `path`, `traceId`, `details`). Stored statuses such as `BORRADOR`
  and `PENDIENTE` stay. There is no `/api/v2` surface.
- **English module-structure decision** (#1490, CU75):
  ADR-002 is English. Samples name current types (`ReportController`,
  `DeedManagementRepository`, `User`, `SecurityAndCorsConfig`). The coverage
  exclusion `service.Administrador*` stays. `frontend-swing` and `notaire-shared`
  stay retired.
- **English frontend-migration decision** (#1490, CU75):
  ADR-005 is English. The original planned routes (`gestiones`, `clientes`,
  `escrituras`, `presupuestos`, `protocolos`, `administracion`, `reportes`) stay
  as that design. Shipped dashboard routes are English. Message keys such as
  `suplencias` and `reportes` stay.
- **English development and testing indexes** (#1490, CU75):
  The development index and the testing inventory are English. Commands, suite
  counts, and the coverage exclusion `service.Administrador*` stay.
- **English testing strategy and workflow tracker** (#1490, CU75):
  ADR-006 is English. The workflow tracker uses management, procedure, history,
  and `workflowDefinitionName` on `DtoProcedureType`. Seeded labels such as
  `Workflow de Gestión Estándar`, `Iniciada`, and `Compraventa` stay.
- **English service-migration decision and API examples** (#1490, CU75):
  ADR-001 is English. The API design index shows budget JSON as `idBudget`,
  `number`, `date`, and `status` (`BORRADOR` stays), and the error body as
  `status`, `error`, `message`, and `path`.
- **English workflow, i18n, and CI decisions** (#1490, CU75):
  ADR-012, ADR-014, ADR-015, the architecture index, and the legacy class-diagram
  note are English. Workflow fields are `positionX`, `positionY`, `originNode`,
  `destinationNode`, `condition`, and `description`. Message keys such as
  `suplencias` and `administracion.usuarios` stay.
- **English architecture type names and audit decision** (#1490, CU75):
  The SAD, ADRs, and security notes name the current classes (`ManagementController`,
  `AuditAspect`, `DeedManagementRepository`) and tables (`audit_records`,
  `deed_managements`, `users`). ADR-013 and the use-case diagram index are
  English. Stored type `Escribano` stays. Legacy `.puml` filenames stay.
- **English endpoint registry and API test guide** (#1490, CU75):
  The REST endpoint registry names the current controllers (`DeedController`,
  `ManagementController`, `AuditRecordController`) and the current path
  parameters. The manual API guide uses English and the `audit-log` path.
  Archived change folders stay historical.
- **English Playwright titles and remaining API descriptions** (#1490, CU75):
  Playwright suite and test titles use English. Payment date parameters are
  start date and end date. A verified testimony and the cadastral designation
  are described in English. Visible Spanish UI text, stored statuses such as
  `Sin Firmar` and `Observado`, and `Protocolo Auxiliar` stay.
- **English audit comments, management update locals, and flagged-testimony description** (#1490, CU75):
  Audit comments name `audit_records.module` and `operation_detail`. Deed
  management update parameters use `updatedManagement`. The flagged-testimony
  OpenAPI description is English. Testimony movement test titles use entry,
  inscription, and withdrawal. Role JSON `modulos` stays. Applied migration
  `V1` table names stay.
- **English testimony messages and leftover identifiers** (#1490, CU75):
  Testimony movement errors say withdrawn, registration entry, and observed
  re-entry. Substitute lookup parameters, folio and procedure update locals,
  property form state, and audit page components use English names. Message
  keys such as `fields.valuacionFiscal`, `fields.domicilio`, and
  `fields.matricula` stay. The deprecated JSON field `saldo` stays.
- **English test identifiers for templates, substitutions, and properties** (#1490, CU75):
  Java test locals and method names use template, substitute, property, budget,
  procedure, and amount. Fixture notes asserted by those tests use the same
  English words. The named-query hygiene method name stays. Stored samples such
  as `Protocolo Auxiliar` stay.
- **English payment wording and protocol locals** (#1490, CU75):
  Payment logs, comments, and test titles say pending balance and amount.
  Protocol pages use `notebooks`, `availableFolios`, and `deedBody`. Query
  keys are `notebooks` and `auxiliary-protocol`. Message keys such as
  `protocolo.cuadernos` and `fields.cuerpo` stay. The deprecated JSON field
  `saldo` stays.
- **English API coverage notes and leftover test titles** (#1490, CU75):
  Bruno descriptions and `COVERAGE.md` filter notes use the live English
  paths (`person`, `search`, `management`, `budget`, `current-status`,
  `procedure-types`). Copy failure logs say `copy`. Test suite titles use
  Managements, Deeds, and People. Stored catalog samples such as `Protocolo`
  stay.
- **English persistence messages and update locals** (#1490, CU75):
  budget-template conflict errors, payment and template failure logs, and
  persistence `println` lines are English. Update flags use `updated`.
  `findDeedManagementById` and `searchMovementsByTestimony` replace the
  Spanish method names. Damaged folio statuses stay `Errose` and `no pasó`.
- **English name for the deleted-record JPA exception** (#1490, CU75):
  `ClassEliminatedException` is now `ClassDeletedException`. Call sites and
  unit tests use the new name. The deprecated tree keeps the old class.
  Thrown messages that already said the record was deleted stay.
- **English delete locals, success logs, and leftover test comments** (#1490, CU75):
  JPA delete flags use `removed`, and item and payment success logs are English.
  Remaining Playwright and integration-test comments use English use-case names.
  Stored payment-status codes `SIN_PAGOS`, `PARCIAL`, and `SALDADO` stay.
  UI matchers that follow the Spanish catalogs stay.
- **English use-case titles in the coverage matrix** (#1490, CU75):
  the Playwright coverage reporter, `CU-API-MATRIX.csv`, and the leftover
  health-check titles use English use-case names, modules, and priorities.
  Matrix path variables match the live API (`idPerson`, `idPayment`,
  `idSubmittedDocument`). CSV column headers stay. Message catalogs stay.
- **English logout, budget-search, and leftover API wording** (#1490, CU75):
  the logout summary and description, the budget-search status parameter,
  workflow create validation messages, and the "in use" operation summaries
  are English. Conflict errors for a concept, folio type, or procedure type
  that is already in use say the record cannot be modified. Stored status
  codes and message-catalog keys stay.
- **English REST API reference** (#1490, CU75):
  `docs/200-architecture/203-design/REST-API-REFERENCE.md` uses English
  resource names and the current JSON fields (`name`, `password`, `valid`,
  `idUser`, `firstName`, `lastName`). The audit path is `/audit-log`.
  Stored status codes and PDF filenames stay.
- **English end-to-end comments** (#1490, CU75): Playwright file headers
  and step comments use English use-case names. Catalog button labels,
  placeholders such as `Buscar por name...`, and stored types
  `DESCUENTO` and `RECARGO` stay.
- **English startup and OpenAPI messages** (#1490, CU75): JWT secret
  failures, the production-credentials guard, the login failure log, and
  the OpenAPI info, server, and error descriptions are English. Stored
  budget status codes such as `PENDIENTE` stay.
- **English PDF report labels** (#1490, CU75): in-house report headings,
  column names, and empty-state messages are English (`Pending balance`,
  `Properties`, `Is archived`, `Payment pending`, `Total owed`). Stored
  catalog names inside a report, such as `Honorarios escribanía`, stay.
  PDF download filenames stay. The renderer still draws accented text.
- **English sentinels, test titles, and API tags** (#1490, CU75):
  `INVALID_OBJECT_ID` and `INITIAL_DTO_ID` replace the Spanish sentinel
  constants. Playwright titles, helper comments, and workflow seed locals
  use English names. OpenAPI tags and history/audit descriptions are
  English (`Concepts`, `Notebooks`, `Folio type`, `Procedure type`,
  `Document cost template`). Stored labels such as `PENDIENTE`, `Nuevo`,
  `Archivada`, `Activa`, and `Entidad Externa` stay. Message catalogs stay
  bilingual.
- **English end-to-end spec filenames** (#1490, CU75): Playwright and Robot
  specs use English filenames, such as `TS-0010-budget-workflow.spec.ts`
  and `procedure-folders.spec.ts`. The living test index matches those
  names. Archived plans keep the filenames they were written with.
- **English test method names and frontend component locals** (#1490, CU75):
  JUnit method names, page components, React Query keys, and local helpers
  use English names. Message catalog keys such as `estadoFilter`,
  `tiposDeFolio`, and `costosDocumento.fields.tipoCosto` stay. Stored labels
  such as `Nuevo`, `Utilizado`, `DESCUENTO`, `RECARGO`, `ADMINISTRADOR`, and
  `ESCRIBANO` stay.
- **English frontend locals and column keys** (#1490, CU75): Data table
  column ids, substitution date inputs, and React Query keys for management
  statuses, procedure types, and procedure templates use English names.
  Local test fixtures use English field names. Message catalog keys such as
  `fields.estado`, `fields.suplente`, and `administracion.estadosGestion` stay.
  Stored labels such as `APROBADO`, `Activo`, `En trámite`, and `Honorarios` stay.
  PDF filenames stay.
- **English comments** (#1490, CU75): Class and method comments in the
  live backend and a few frontend notes use English. Stored workflow
  labels such as `Generada`, `Presentada`, `Observada`, `Inscripta`,
  `Sin Firmar`, and `Firmada` stay inside those comments. Message
  catalog keys stay.
- **English API descriptions** (#1490, CU75): OpenAPI summaries and
  descriptions use English. Missing-field validation says `is required`.
  Delete conflicts say `Cannot delete`. Stored statuses such as `Generada`,
  `Firmada`, `Sin Firmar`, and `Utilizado` stay. PDF filenames stay.
  Message catalog keys stay.
- **English test titles** (#1490, CU75): JUnit display names and nested
  test class names use English type names. Stored labels such as
  `Protocolo Auxiliar`, `Protocolo Principal`, `Generada`, `Presentado`,
  `Observado`, `Utilizado`, and `Sin Firmar` stay inside those titles.
  Message catalog keys stay.
- **English list locals and auxiliary-protocol operation** (#1490, CU75):
  list locals use English names. Starting an auxiliary-protocol deed is
  `startDeed`. Budget JSON uses `procedures`. Deed-number and CORS messages
  are English. The PDF filename `lista_documentos.pdf` stays. Message catalog
  keys stay.
- **English locals and debug labels** (#1490, CU75): `mi`-prefixed locals,
  `created`, and `modified` replace the Spanish names. Entity `toString`
  labels are English (`number`, `year`, `status`, `lastName`). Message
  catalog keys stay.
- **English JPA relationship names** (#1490, CU75): generated locals,
  orphan messages, and `toString` labels use English relationship names
  (`fkIdPerson`, `budgetTemplateList`, `auditRecordList`, `DeedManagements`).
  Creating a budget template returns `fkIdProcedureType` and `fkIdConcept`.
  Jackson `@JsonIgnoreProperties` values stay as they are. Message catalog
  keys stay.
- **English case-summary key and JPA helper names** (#1490, CU75):
  the management case-summary cache key is `caseSummary`. Involved-person
  mapping is `getInvolvedPersonDto`. Audit lookup is `searchAuditRecordsByUser`.
  Message catalog keys stay.
- **English singleton accessors** (#1490, CU75): JPA and session
  singletons use `getInstance()` and the field `instance`.
- **English document-reentry and deed-management fields** (#1490, CU75):
  required-document JSON is `requiredDocuments`. Deed-management JSON uses
  `involvedClients`, `referenceClient`, `associatedProcedures`, `archiveNumber`,
  and `binderNumber`. Putting a folder on hold is `putOnHold`. The archive
  conflict body uses `waitingFolderNumbers`. The archive query flag `confirmado`
  stays, as do stored statuses `Activa`, `Espera`, and `Archivada`.
- **English financial-summary and persistence type names** (#1490, CU75):
  `ManagementFinancialSummaryService`, `DtoBudgetSummary`, `DtoPendingBalance`,
  and `PersistenceConstants` replace the Spanish type names. Financial-summary
  JSON fields are `totalBudgeted` and `totalCollected`. Message catalog keys
  `totalPresupuestado`, `totalCobrado`, and `saldoPendiente` stay.
- **English status-log and document-reentry type names** (#1490, CU75):
  `ManagementStatusLogService`, `ManagementStatusLogPort`, and `DocumentReentryService`
  replace the Spanish type names. External-entity document operations are
  `getExternalEntityDocuments` and `registerExternalEntityDocumentMovement`.
  Message catalog key `viewBitacora` stays.
- **English validation messages and API descriptions** (#1490, CU75): exception
  and validation text, PDF labels, and OpenAPI response descriptions (`Not found`,
  `Created`, `Conflict`) are English. The status log parameter is `statusLog`.
  External-entity document locals use `detail` and `registerMutation`. Stored
  labels stay, including folio status `Asignado a cuaderno`, damaged statuses
  `Errose` and `no pasó`, and `Protocolo Auxiliar`. Message catalog keys stay.
- **English copy dates and testimony-movement handlers** (#1490, CU75): the copy
  form uses `datePrinting` and `dateWithdrawal`. Testimony movement handlers
  use enter, register, withdraw, and re-enter. Document re-entry posts
  `reentry` and caches under `documentReentry`. Message catalog keys stay.
- **English document, testimony, and folder-hold locals** (#1490, CU75): the
  submitted-document form uses `typeId`, `date`, and `delivered`. Testimony
  verification passes `flagged`. Re-entry passes `observedByRegistry`. The
  testimony query key is `testimonies`. Putting a folder on hold passes
  `reason`. Message catalog keys and the archive query flag `confirmado` stay.
- **English document-type cache key and registration-draft fields** (#1490, CU75):
  the document-type query key is `documentTypes`. Registration-draft form state
  uses the English wire names (`dateSubmission`, `registryEntryNumber`,
  `registryNotes`, `dateCorrection`, `dateReception`, `finalRegistrationNumber`).
  Management transition passes `statusDestination`. Message catalog keys and
  stored statuses (`Generada`, `Firmada`, `Inscripto`) stay.
- **English Playwright and unit test titles** (#1490, CU75): `describe` / `test` / `it`
  titles in `testing/e2e` and the frontend unit tests name the flows in English.
  Selectors that match Spanish screen copy, stored statuses (`Firmada`, `Archivada`,
  `SIN_PAGOS`), and message catalog keys stay as recorded.
- **English comments on archive, person, budget, and procedure types** (#1490, CU75):
  javadoc on the archive-debt service, deed-management and person persistence,
  budget lookup, user password checks, testimony movements, document re-entry,
  and the procedure entity is English. Stored statuses `Archivada` and `Espera` stay.
- **Stored status and role labels kept** (#1490, CU75): `BusinessConstants` and audit
  `GuiConstants` values stay the recorded Spanish labels (`Iniciada`, `Firmada`,
  `Documentacion Completa`, `Escribano`). The initial administrator type is `Escribano`
  again. Frontend hook and page comments are English. Behavior of those labels is unchanged.
- **English page-local identifiers** (#1490, CU75): frontend and Playwright locals,
  parameters, and component names use English stems (`managementId`, `number`,
  `PeoplePage`). Message catalog keys, `{numero}` / `{dias}` / `{tramite}`
  placeholders, stored status labels, and JSON names that are still Spanish
  (`documentsNecesarios`, `listaClientesInvolucrados`) stay as recorded.
- **English `data-testid` values** (#1490, CU75): dashboard controls and the Playwright
  specs that look them up use English ids (`btn-new-budget`, `btn-save-management`,
  `select-procedure-type`). Message catalog keys are unchanged.
- **English Playwright helper names** (#1490, CU75): setup functions and payload
  types in `testing/e2e/tests/setup/api-helpers.ts` use English names
  (`createPerson`, `createBudget`, `createCompleteCaseManagement`,
  `createDocumentType`). Spec-local seed helpers follow the same names.
  Spec filenames are unchanged.
- **English caller fields for complete-case and budget creation** (#1490, CU75):
  `CreateCompleteManagementInput` and the Playwright complete-case helper use
  `budgetId`, `notaryId`, `statusManagementId`, `procedureTypeId`, and `propertyId`.
  `useCreateBudget` returns `budget` and takes `procedureTypeId`. Message catalog keys
  and stored labels such as folio status `Nuevo` stay as recorded.
- **Process Checks diff the pull request, not a stale base** (CU76, #921):
  `sdlc-process.yml` and `check-sdlc-exception.sh` use the merge commit's first parent.
  An OpenSpec path that landed on `main` after the PR opened no longer satisfies the gate.
- **OpenSpec hygiene after #1477 / #1480**: archive closed-issue tree
  `feat-1462-otel-metrics-exporters` and shipped Gate 1 trees
  `docs-1445-archive-closed-1472` / `docs-1479-modules-owner-tracker` so `validate-sdlc-plan` stays green.
- **Module-ownership surfaces point at live Owner tracker #1445** (CU76, #1479):
  `MODULE-OWNERSHIP.md`, Modules Pages, and Docs home no longer present closed #1197 as the
  live umbrella.
- **Pages Architecture links enterprise readiness reports** (CU76, #1242): deep-links to
  `ASSESSMENT-2026-10.md` and `DOCUMENTATION-AUDIT-2026-10.md`.
- **OpenSpec hygiene**: archive closed-issue trees for #1408 / #1367 / #1466 and shipped
  `docs-1445-owner-phase0-evidence` so `validate-sdlc-plan` stays green.
- **Owner evidence on Pages + ADR-024** (CU76): Architecture Owner pack shows
  P0.1–P0.5 Done and links `REPO-METRICS-BASELINE.md` (path-scoped **6/17**); Modules
  links plan + baseline; ADR-024 measured evidence records dorny/paths-filter count.
- **OpenSpec hygiene after #1457 / #1458**: archive shipped Gate 1 trees
  `docs-1445-archive-1456-gate1` and `docs-1256-metrics-path-filter`.
- **Repo metrics path-scoping count** (CU76, #1256): `repo-metrics.py` counts
  `dorny/paths-filter` workflows (P0.2) as well as classic `on.paths`; baseline regenerated
  (6/17 path-scoped).
- **OpenSpec hygiene after #1456**: archive shipped `docs-1445-archive-1368-motion`.
- **OpenSpec hygiene**: archive closed-issue trees `fix-1368-motion-tokens` and
  `fix-1354-hardcoded-ui-strings`, plus shipped `docs-1445-archive-owner-decisions`, so
  `validate-sdlc-plan` stays green.
- **OpenSpec hygiene**: archive shipped Owner-decisions Pages change and closed-issue trees #1352/#1353/#1357/#1365 so `validate-sdlc-plan` stays green.
- **Pages Architecture lists Owner decision pack** (CU76): `/docs/architecture/` names
  blocking issues #1445 (ADR-024), #1438 (deprecated/ A/B/C), and #1226 (LICENSE) with
  deep-links; unit guard pins the pack; archive completed `docs-1445-archive-hygiene-1452`.
- **OpenSpec hygiene after #1452**: archive shipped `docs-1445-archive-pages-umbrella`.
- **OpenSpec hygiene after #1451**: archive shipped `docs-1445-pages-owner-umbrella`.
- **Pages Architecture surfaces live Owner umbrella #1445** (CU76): ADR-024 entry on
  `/docs/architecture/` names the open tracker (same pattern as ADR-022 → #1438); unit
  guard pins the string; archive completed `docs-1445-archive-closed-umbrella` OpenSpec.
- **OpenSpec active-set hygiene**: archive `docs-1197-repository-topology` (tied to closed
  umbrella) and the merged `docs-1445-openspec-archive` tree so `validate-sdlc-plan.sh` /
  `workspace/verify` stay green.
- **OpenSpec hygiene + business README Pages link**: archive shipped docs-1441/1443/1445
  packaging changes; `docs/100-business/README.md` links the public Business Docs page.
- **Live Owner topology tracker is #1445** (CU76): ADR-024 / REPO-SPLIT-PLAN point at #1445 after
  prior umbrellas were closed by GitHub keyword parsing in packaging PRs. Keep #1445 open until
  Owner decisions are recorded.

- **Business Docs on GitHub Pages** (#1441, CU76, #1197): curated `/docs/business/` page with
  deep-links into `docs/100-business/` (requirements, use cases, actors, traceability, manuals);
  Docs chrome nav and home card include Business; unit guard in `workspace/tests/`.
- **P0.6 Owner decision tracker refreshed** (#1197, CU76): ADR-022 / REPO-SPLIT-PLAN / Pages Architecture point at the current open Owner issue after prior trackers were auto-closed by merges.
- **Live Owner tracker for `deprecated/` decision is #1435** (issue #1197 P0.6, CU76): ADR-022 / REPO-SPLIT-PLAN / Pages Architecture point at #1435 after #1261 was auto-closed by packaging PR #1429.
- **Heavy-CI gate accepts `gh pr checks` status `skipping`** (issue #1197 / #1257, CU76): `check-heavy-ci.sh` treats path-scoped skips the same as `skip`/`skipped` so docs-only PRs are mergeable after suite aggregators go green.
- **ADR-024 measured evidence refreshed after Phase 0** (issue #1197, CU76): path-scoped CI, ~2k agent context, OpenAPI-generated frontend types, and Playwright shards recorded; ADR-022 notes that #1261 packaging does not complete the Owner decision.
- **Owner decision packaging for `deprecated/` / history purge** (issue #1261, CU76, #1197 P0.6): ADR-022 adds a Pending Owner decision section (Option A/B/C); GitHub Pages Architecture links ADR-022; agents must not delete `deprecated/` or rewrite history until the Owner records a choice.
- **Frontend API types generated from OpenAPI** (issue #1260, CU76, #1197 P0.5):
  `openapi-typescript` writes `frontend/src/types/api.generated.ts`; `npm run openapi:types:check`
  fails Frontend CI on drift. Gestiones, presupuestos, documentos (and dashboard counts via those
  hooks) use generated schema aliases from `src/types/api.ts`.
- **Always-loaded agent context ≤8k tokens** (issue #1259 / #1197 P0.4, CU76): slim `AGENTS.md` with an on-demand rule/skill table; add `CONSTITUTION-AGENT-CARD.md` digest (full Constitution on demand); set `alwaysApply: false` on large frontend/Java/refactoring rules and the frontend-design skill; add `workspace/ci/agent-context-budget.py` + unit test; refresh `REPO-METRICS-BASELINE.md` (~2k tokens est.).
- **Playwright E2E runs as a three-shard matrix** (issue #1258, CU76, #1197 P0.3):
  `playwright-e2e.yml` shards UI E2E with `--shard=i/3`, merges blob reports in
  `e2e-merge-reports` (check name stays `UI E2E Tests (Playwright)` for
  `check-heavy-ci.sh`), and fails closed if any shard failed. CI
  `playwright.config.ts` adds the blob reporter; invariants guard the matrix.
  `check-heavy-ci.sh` accepts path-scoped `skip`/`skipped` for docs-only PRs.
- **Path-scoped CI skips Java and E2E on docs-only PRs** (issue #1257, CU76, #1197 P0.2):
  `ci.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, and `openapi-contract.yml` add a
  `Path filter` (`dorny/paths-filter`) job; leaf jobs gate on filter outputs; suite
  aggregators accept intentional `skipped` so required check names stay green.
  `push` to `main` / dispatch / schedule still run the full suite. Guards in
  `workspace/tests/test_ci_workflow_invariants.py`; documented in `CI-PREFLIGHT.md`
  and `CONSTITUTION.md`.
- **Constitution AI SDLC guardrails** (issues #1414–#1417 / CU76): Tooling Map and §10 now require heavy-CI merge authority (`check-heavy-ci.sh`), Playwright serialization, Englishize of touched non-i18n Spanish, and issue-status sync via PR `Closes`/`Refs` when Issues write is 403; Impact Analysis uses `workspace/modules.yaml`; active diagrams are Mermaid (ADR-027); OpenSpec path and Pages `/docs/` are explicit.
- **GitHub Pages Technical Docs tab + #1197 Phase 0 documentation readiness** (issues #1414, #1415, #1416, #1417, CU76): `github-page` adds a Docs navigation surface (`/docs/`) for module ownership, architecture (SAD/ADR), testing and DevSecOps; Mermaid is the canonical active diagram language (ADR-027); `MODULE-OWNERSHIP.md` and `workspace/ci/repo-metrics.py` + `REPO-METRICS-BASELINE.md` document in-repo folder ownership and the topology metrics baseline without splitting repositories; SAD Project #4 link corrected to Delivery Board #1. Deploy workflow unchanged.
- **Cloud Agent install pins Temurin JDK 26** (issue #1401, CU76): `.cursor/install.sh` idempotently installs Temurin 26 and sets `JAVA_HOME` / `/usr/local/bin/java` so Cursor Cloud recurring builds match repo `java.version=26` and CI (tip installs were failing with `release version 26 not supported` on JDK 21 images).
- **GitHub Pages timeline appends Sept–Oct 2026 Cloud AI SDLC** (issue #1400, CU76): `github-page` Project History keeps every prior era (2014 → Aug 2026) and adds a September–October 2026 entry for the Cursor Cloud fleet, OpenSpec Gate 1, and heavy-CI delivery; AI Era / AI Tools copy mentions OpenSpec and Cursor Cloud; the schema dual-source Fun Fact notes Flyway-only resolution. Deploy workflow unchanged.
- **Constitution Impact Analysis drops retired `notaire-shared`** (issue #1263, CU76): §5 step 4 lists only live product modules (`backend-api`, `frontend`). The module was retired under #1255 / ADR-025; leaving it in the Constitution taught agents to treat a dead module as current. `workspace/tests/test_notaire_shared_retired.py` now includes `CONSTITUTION.md` in its non-live docs guard.

- **Accepted OpenAPI breaking changes: one file per pull request** (issue #1315, CU76): `backend-api/openapi/accepted-breaking-changes.txt` is replaced by `backend-api/openapi/accepted-breaking-changes.d/<issue>-<slug>.txt`. Every pull request that accepted a break edited the same lines of the single list and `main` emptied them after each merge, so every merge conflicted with every other open pull request (#1335, #1342, #1370-#1374 each needed two conflict-only merges from `main` in one evening). `workspace/sdlc/check-accepted-breaking-changes.py` now assembles the list: a file unchanged on the base branch belongs to a merged pull request, is never ignored and is listed for deletion (`--prune`, `preflight.sh --fix`); every other file must hold entries that still match a current break, under a `# #<issue>` comment, in a file named after the issue; those entries alone are written to `.openapi-ci/accepted-breaking-changes.txt`, which `oasdiff breaking --err-ignore` reads in `openapi-contract.yml` and `preflight.sh`. The legacy single list is rejected. The #1315 rule holds: entries are justified and the effective list on `main` is empty. Rules in the directory's README and `docs/200-architecture/208-devsecops/README.md`.
- **Expiring-documents report and manual testimony-movement editing are API-only (Owner decision)** (issues #1363, #1364, #1250, CU42/CU44): the Owner decided on 2026-10-09 (UI/UX epic #1336) that `GET /api/v1/reportes/documentos-por-vencer/{id}` gets no UI download action, and that `POST/PUT/DELETE /api/v1/movimiento-testimonio` stay API-only because the withdraw and re-enter actions record movements. `contracts/api-reachability-allowlist.yaml` moves them from buckets E and C to A with the decision in the reason, and `test_api_reachability.py` fails if any entry still waits for an Owner reclassification. `GET /api/v1/movimiento-testimonio` stays UI backlog for the testimony movement history panel (#1364).
- **BREAKING — a bare testimony is created as the copy of an existing deed (slice of #655)** (issue #655, CU07/CU08): `POST /api/v1/testimonio` rejects a body without `deed` (also `{}`) or without `deed.idDeed` with 400 naming the field, and answers 404 for an unknown deed, instead of storing a testimony that copies nothing; the contract marks `deed` required on POST (`TestimonyCreateRequest`, same fields as `DtoTestimony`). `PUT` is unchanged. The Owner chose requiring the deed over removing the endpoint: the UI generates testimonies through `POST /testimonio/generar`, and the Playwright seeds, Bruno and backend tests that use the bare create already send a deed (the Bruno `testimony-movements` fixture now seeds one). One accepted oasdiff entry under #655; the three entries of #1332, #1333 and #1334, already on `main`, are removed.
- **Local AI model registry and the Ternary Bonsai preset** (issue #1377, CU76): model presets move from hard-coded `case` blocks in `local-ai/setup-omlx-codex.sh` into `local-ai/models.yaml`, resolved by `local-ai/models_config.py`; `qwen3-coder` and `gpt-oss` resolve to the same values as before, and `ternary-bonsai` (Ternary Bonsai 2 27B, 2-bit) with the `omlx-bonsai` profile is added. Tests that need a live oMLX server or the downloaded model skip with an explicit reason.
- **BREAKING — a submitted document is created with its type and its trámite (slice of #655)** (issue #655, CU04/CU72): `POST /api/v1/documento-presentado` rejects a body without `typeId` or `procedureId` (also `{}`) with 400 naming the field, instead of storing an empty, orphan document; the contract marks both required on create. A document is presented for a trámite of a gestión: without that link it never reached the case summary, the documentation-complete check, the CU09 debt report, the gestión column of CU42 expiries or CU10 movements. `PUT` stays partial and the column stays nullable, so documents stored before this rule are untouched; the Documentos dialog requires type, gestión and trámite to create and lets a document without a trámite be linked when edited. Two accepted oasdiff entries under #655.
- **The accepted OpenAPI breaking-change list is empty and stays that way** (issue #1315, CU76): its 5 entries (4 from #655 for `/api/v1/pagos`, 1 from #596 for `GET /api/v1/historial`) accepted breaks already on `main`. They matched nothing and could only hide a later break with the same text, so they are removed. New `workspace/sdlc/check-accepted-breaking-changes.py` runs `oasdiff breaking` (JSON, no ignore list) between the base and the revision. It fails on any entry that no current breaking change matches, using the oasdiff err-ignore rule. `openapi-contract.yml` runs it on pull requests (oasdiff 1.33.0, checksum-verified) and `preflight.sh` runs it locally. An entry therefore lives only in the pull request that introduces its break, and the next pull request must remove it. The rule is in `docs/200-architecture/208-devsecops/README.md` and the list header.
- **Bruno CI step retries the CLI install** (issue #567, CU76): the Playwright E2E workflow keeps the unpinned `npx @usebruno/cli`, but it now installs the CLI first with up to 3 attempts, 60 s apart, before running the suite. Run 37697055951 on PR #1328 failed before any request ran: npm returned `ETARGET` for `tldts-core@^7.4.17`, because `tldts@7.4.17` was published about two minutes before its own `tldts-core@7.4.17`. A transient registry error like this is now retried instead of failing the suite.
- **PR CI pulls images through `mirror.gcr.io`** (issue #1380, CU76): shared GitHub runners hit Docker Hub's anonymous pull rate limit (429 `toomanyrequests`), which failed the Bruno, Playwright and OpenAPI jobs of #1374 twice before any test ran and left the Testcontainers job hanging. Service containers now use `mirror.gcr.io/library/postgres:16.15-alpine`, Testcontainers gets `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX=mirror.gcr.io/`, BuildKit mirrors `docker.io` for the image build (Dockerfiles unchanged), the OpenAPI gate runs the checksum-verified oasdiff 1.33.0 binary with the same `--fail-on ERR --err-ignore` rule instead of the Docker-based action, and `npx playwright install` is retried 3 times. `workspace/tests/test_ci_docker_hub_mirror.py` guards it.
- **BREAKING — a property is created and updated with its cadastral designation (slice of #655)** (issue #655, CU69/CU82): `POST /api/v1/inmueble` and `PUT /api/v1/inmueble/{id}` reject a missing, null, empty or blank `cadastralDesignation` with 400 naming the field (`{}` used to answer 201). The nomenclatura catastral identifies the property; PUT replaces every field, so it requires it too. The Inmuebles form, which already labelled it required, keeps Save disabled while it is blank. Four accepted oasdiff entries under #655.
- **BREAKING — copies, registration drafts and document re-entries require their fields (slice of #655)** (issue #655, CU08/CU82/CU43): `POST /api/v1/copia` and `PUT /api/v1/copia/{id}` require `number` and `datePrinting` (NOT NULL columns; `{}` answered 500 and a missing number silently stored 0), `POST /api/v1/minutas-inscripcion` requires `idDeed`, and `POST /api/v1/gestiones/{id}/reingreso-documentacion` requires `idProcedure` and `idDocumentType` (a null id reached `findById` and answered 500). A missing field now answers 400 naming it and nothing is stored; the contract marks the fields required. The copies dialog labels the print date required and keeps Save disabled until the number and print date are filled. `oasdiff`: 7 `request property became required` breaks accepted under #655 (no request without them ever succeeded); the 7 stale entries already on `main` are removed. `RequiredRequestIdsIntegrationTest`, Bruno `copies/08`, `registration-drafts/06`, `managements/11` and Playwright `copias-required-fields.spec.ts` cover it.
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
- **One design-token source for colour, and the half-built dark mode is gone** (issue #1365, RNF-05/RNF-09, CU76):
  `globals.css` adds text-safe `success`, `warning` and `info` tokens (WCAG AA on white and on their /10 tint),
  mapped in `@theme`; `tokens.ts` mirrors them and writes the brand `#0071E3` once (`BRAND_PRIMARY`). The 54 raw
  Tailwind palette classes in 13 files are now semantic (Badge variants, notices, pagos balance panel, workflow
  editor legend and errors, admin selects, the `useThemeClasses` builders used by testimonios, ReenterDialog
  and documentos-entidades-externas); the 14 differently coloured dashboard module tiles share one
  `bg-primary/10 text-primary` style, and `ring-primary-300` / `border-error-500`, which generated no CSS, are `ring-ring/40` / `border-destructive`. The partial
  `.dark` block is removed (owner decision 2026-10-09). Guarded by `design-tokens.test.ts` and Playwright TS-0119.
- **One motion system, and route changes no longer wait for an exit animation** (issue #1368, RNF-05/RNF-03):
  `globals.css` defines `--motion-*` durations (fast 120ms, base 180ms, slow 240ms, exit 180ms, page 160ms) and
  easings with `duration-fast|base|slow|exit|page` and `ease-standard|emphasized|exit` utilities, mirrored by
  `theme/motion.ts` for motion/react. The 22 ad-hoc `duration-NNN` / `transition-all` classes and
  `.apple-button { transition: all }` (with its hover scale) are gone; dialogs enter in 240ms emphasized and leave in
  180ms; pages fade up 4px in 160ms without `AnimatePresence mode="wait"` (was 200ms exit + 400ms enter); staggered
  entrances are capped at 6 x 30ms. Guarded by `motion-tokens.test.ts` and Playwright TS-0120.
- **Grouped sidebar (#1367).** The sidebar's flat 22-link list is now Inicio plus six collapsible sections with headings: Clientes, Trámites, Documentación, Escrituras y protocolo, Gestión and Administración. The browser remembers collapsed sections, and the section holding the current page always opens. The dashboard module grid is built from the same config, in sidebar order. Auditoría is now hidden from non-admins, matching its route guard. Opening Documentos necesarios no longer also highlights Documentos.
- **Identification type in the person form (#1361).** The person dialog has a required "Tipo de identificación" select before the number, with DNI preselected for a new person (Owner decision); a person stored without a type must pick one before saving. The list shows the type before the number ("CUIT 30712345678") and the number field is labelled "Número de documento". `POST /people` and `PUT /people/{id}` now answer 400 for an identification type id that is not in the catalog (create used to store DNI silently, update kept the old type); an omitted type on create still means DNI, now found by name instead of by id 1.
- **Dates, numbers and money follow the UI language** (#1355, RNF-05, ADR-015): every screen formatted values in `es-AR`, so the English UI showed `8/7/2026` and `$ 75.000,00`, and the dashboard and its counters each had their own locale switch. A new `useFormat()` hook (on `lib/format.ts` and the calendar-date helpers of #1339) formats dates, timestamps, numbers and ARS amounts for the active language. Spanish uses es-AR (`8/7/2026`, `$ 75.000,00`). English uses en-GB, so dates keep the day first as the Owner decided (`08/07/2026`), and money reads `ARS 75,000.00`. All 49 screen call sites (including the #1491 deed picker) moved to the hook, and guard tests keep locale names and the es-AR-only helpers out of screens. `use-format.test.tsx` and Playwright `TS-0040` cover it.







### Fixed

- **Deleting a testimony no longer erases its movement history** (issue #1364):
  `DELETE /api/v1/testimonies/{id}` answered 204 and cascade-deleted the testimony's registry
  movements and copies. It now answers 409 with the reason and keeps everything when the
  testimony has movements or copies.
- **Budget no longer loads its whole payment list (slice of #595)** (issue #595, CU74): `Budget.paymentList` was an EAGER, unbounded `@OneToMany`, so finding or listing a budget pulled every payment for that budget. It is now `LAZY`; the getter stays `@JsonIgnore`, and legacy JPA controllers already read it inside an `EntityManager`. `BudgetPaymentListFetchIntegrationTest` pins it. #595 stays open for the remaining EAGER `@OneToMany` mappings (`procedureTemplateList`, `budgetTemplateList`).
- **Spanish UI copy, E2E locators and management create restored after the English rename** (issue #1502, RNF-04): the #1495 identifier rename also rewrote 27 Spanish values in `frontend/messages/es.json`, so users saw "Person actualizada", "Property creado", "Nuevo Notebook", "Concept creado", "Substitution registrada" and a "Role" column. The values are restored from the pre-rename catalog (sentence-case fixes that stay Spanish are kept; `en.json` untouched), Playwright `TS-0023` expects "Rol" again, and Vitest `es-catalog-spanish.test.ts` fails if an English entity name from the rename map reaches an es.json value. The same rename had rewritten the Spanish UI text that Playwright locators match (`/fecha/` became `/date/`, `/presupuesto/` became `/budget/`, `nav-gestiones` became `nav-managements`) in 21 specs, and left the management create sending `procedureTypeId` where `CompleteCaseRequest` reads `typeProcedureId` (UI creates answered 400); both are fixed (`toCompleteCaseRequest`, Vitest `useManagements.create-wire.test.tsx`), `TS-0072` expects the English notebook validation messages, and the CU39 no-template E2E case picks its procedure type by exact name, so main's E2E jobs pass again.
- **No more hardcoded Spanish (or English) UI strings** (issue #1354, RNF-05/ADR-015): in English the breadcrumb, several column headers, form labels and placeholders, the protocol report buttons and badges, the payment balance panel and the empty-table message were still Spanish, and the language switcher's screen-reader name was English in the Spanish UI. The breadcrumb now reads its labels from the catalogs (administration sub-routes have their own), about 50 literals moved to `messages/es.json`/`en.json`, and a Vitest static scan fails if a literal UI string is added again. Playwright TS-0040 checks the English screens.
- **Personas search waits for you to stop typing** (issue #1357, RF-39 / RNF-03): the search sent one `/people/search` request per keystroke (17 for a 17-character surname) and the table flashed between keystrokes. `useSearchPersonas` now debounces the criteria 300ms, keys the cache by primitive criteria only, and keeps the previous results visible while the next ones load. Vitest `personas-search` and Playwright TS-0015 (#1357, one request per typed surname) cover it.
- **The dashboard workflow graph is reachable by screen readers and its motion stops** (issue #1353, RF-23/CU76): the graph was an image (`role="img"`) containing focusable step buttons, so screen readers couldn't reach the steps (axe `nested-interactive`), and the active-edge dot and current-step pulse looped forever. The graph is now a named group described as a flow diagram, each step is a button named with its state, and both animations stop after 4.8 seconds (WCAG 2.2.2). Vitest `workflow-tracker-a11y.test.tsx` and Playwright TS-0035 cover it.
- **Dashboard counts no longer flash 0 while loading** (issue #1358, RNF-03): since #1397 the three stat cards count with `size=1` page requests instead of downloading lists of 1000, but each card still showed `0` until its count arrived (and after an error). The cards now show a skeleton (announced as loading) until the total arrives, a dash if it fails, and the exact total formatted for the locale. Playwright TS-0118 checks the counts against the API, that only `size=1` requests are made, and the skeleton.
- **Dialogs and confirm dialogs animate in and out** (issue #1349, RNF-05/RNF-06): the shadcn classes `animate-in`, `fade-in` and `zoom-in-95` had no CSS behind them (tw-animate-css was not installed), so dialogs popped in and vanished mid-click. `tw-animate-css` is now a devDependency imported in `globals.css`. Overlays fade, and content fades and scales from 0.95 on enter (200ms) and reverses on exit (150ms) through Radix `data-state`. The table and login fades now run too. Under `prefers-reduced-motion` the existing global rule leaves no perceptible motion. Playwright TS-0117 covers it.
- **The mobile navigation drawer behaves as a modal sheet** (issue #1350, RNF-10/CU76): below 768px the drawer left focus on the toggle, ignored Escape, let Tab reach the page behind it, had no close button, let the page scroll, and its toggle had no `aria-expanded` and an English-only name. It is now a Radix dialog sheet: focus starts on the first navigation link, Tab stays inside, Escape, the new translated close button or a tap outside close it and return focus to the toggle, and the page does not scroll behind it. The toggle exposes `aria-expanded`/`aria-controls` and the translated `navigation.openMenu`. Playwright TS-0041 covers it.
- **Form errors and helper text are linked to their field and announced** (issue #1351, RNF-08/CU76): `FormField` rendered its error (prefixed with a ⚠️ emoji) and helper text as plain text inside the label, so screen readers read them as part of the field's name, never as its description, and `aria-invalid` depended on each page. FormField now gives the error and helper text ids and shares them through a context read by `Input` and `SelectTrigger` (a native control is wired directly): the control gets `aria-describedby`, `aria-invalid` and `aria-required`, and the error is a `role="alert"` paragraph with a lucide icon instead of the emoji. `form-field.test.tsx` and Playwright TS-0015/TS-0095 cover it.
- **The managements list shows each case's header and start date, and the procedure count has its own title** (issue #1348, RF-22/RF-40): the column titled "Tipo de Trámite" showed the procedure count, and the header and start date that identify a case were hidden, although `GET /gestiones` returns them. Columns are now Número (with the id as secondary text), Carátula (truncated, full text on hover), Inicio (calendar date), Trámites and Estado. Playwright TS-0116 covers it.
- **Budget statuses use one vocabulary: the stored codes, validated, migrated and translated** (issue #1346, RF-02/RF-06/CU60, Owner default 2026-10-10): `budgets.status` was free text, and the UI hardcoded BORRADOR/APROBADO/RECHAZADO/FACTURADO as Spanish literals while fixtures wrote "Pendiente" or "Pending". On the dev DB 1434 of 1536 budgets were "Pendiente": the filter could not select them and their edit dialog showed a blank status. The status is now one of `BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO`. POST/PUT `/presupuestos` and `GET /presupuestos/buscar?status=` accept any letter case and use the code; anything else answers 400 naming the accepted values (OpenAPI enum, accepted breaking change). Flyway V44 maps the stored Spanish and English spellings and keeps unknown values upper-cased. The UI lists, filters and edits every status with translated labels (`presupuestos.status.*`), and a legacy value never leaves the edit select blank. Fixtures send codes. `BudgetStatusVocabularyTest`, `BudgetStatusMigrationIntegrationTest`, `budget-status.test.ts`, api-test `budgets/10-create-invalid-status.yml` and Playwright TS-0115 cover it.
- **The dashboard hero shows the newest case that has a workflow, and the dead "view all" button is gone** (issue #1347, RF-23/CU14): the hero traced the first management in backend order (seed management 1001, the oldest), and "Ver todos los servicios" had no action. The hero now walks the 20 newest managements in parallel batches of 5 and shows the first whose workflow trace exists (draft and test cases often have none), names it `#<number> — <header>` (translated fallback instead of the English `Management #n`), and shows an empty state linking to the managements list when none has a workflow. The button is removed, because the grid it headed already lists every module. `dashboard-hero.test.tsx` and Playwright TS-0035 cover it.
- **Budgets are chosen with a searchable picker instead of the first 1000 (slice 6 of #1340)** (issue #1340, CU02/CU15): the new-management and payment forms listed budgets from `GET /presupuestos?size=1000` (newest first), so on the dev DB (about 1300 budgets) the oldest could not be chosen and each dialog rendered 1000 options. A new `BudgetPicker` lists the newest 20 budgets on open and, as the user types, finds a budget by its number or by its client's name or document (`/people/search`, then `/presupuestos/persona/{id}`); options read `#<number> — <client>` with the amount as secondary text, and the selected budget is loaded by id. The ARIA combobox shell of `PersonPicker` is now the shared `SearchCombobox`, so both pickers keep one keyboard and announcement implementation. `usePresupuestos()` is removed and a unit test keeps it out of pages. `GET /api/v1/presupuestos/persona/{idPerson}` is now called by the UI, so it leaves the API reachability allowlist (it was #1250 bucket D, "likely redundant"). `budget-picker.test.tsx` and Playwright `TS-0114` cover it.
- **Filter selects have accessible names** (issue #1343, RNF-07): axe reported critical `button-name`/`select-name` violations for the client filter on gestiones, the status filters on presupuestos and folios, the module filter on auditoria and the workflow selector on estados-gestion, which screen readers announced as an unnamed combobox or button. They now have `aria-label`s from new keys (`gestiones.clienteFilter`, `presupuestos.estadoFilter`, `administracion.folios.estadoFilter`, `administracion.estadosGestion.workflowFilter`, `auditoria.moduleFilter`, es/en). `select-accessible-names.test.ts` fails on any `SelectTrigger` or `select` in `src/app` without a name or a labelled `FormField`, and Playwright `TS-0042` finds the filters by role and name.
- **The people list is paginated server-side; every person is reachable again (slice 2 of #1340)** (issue #1340, CU18/CU54/CU61): `/dashboard/personas` requested `GET /people?size=1000`, so on a database with more people everyone after the 1000th was missing from the list, and the duplicate-document toast lost its "Ver persona existente" link when the existing person was one of them. The list now shows one server page (20, 50 or 100 rows, newest first, so a person just created is on page 1) with the total and the shared `Pagination` footer, and keeps `page` and `size` in the URL through a new `useUrlPagination` hook. A search still lists every match from `/people/search`. The duplicate toast loads the existing person by id when it is not on the page, and toast actions are clickable while a dialog is open (Radix set `pointer-events: none` on `body`, so the link could be seen but not clicked). A page past the end moves to the last page. The person pickers on gestiones and presupuestos and the dashboard counter are later slices. `personas-pagination.test.tsx` and Playwright `TS-0110` cover it.
- **Text contrast reaches WCAG AA on every page** (issue #1341, RNF-09): with the #1337 colours rendering, axe reported `color-contrast` on 35 of 36 dashboard routes (476 nodes at 1440px, 117 at 390px): table headers at 3.3:1, delete icons at 4.2:1, the sidebar subtitle at 4.4:1, the required-field asterisk at 3.4:1 and field errors at 3.6:1. `neutral[600]` is now #6E6E73, `--sidebar-muted` 40% and `--destructive` 0 74% 45%; a new `error[700]` colours error text and the asterisk, and a new `text-primary-text` colours links on light grays while buttons keep the brand blue. axe now reports 0 nodes on all 36 routes at both widths. `token-contrast.test.ts` checks each text token against each light background, and Playwright `TS-0107` checks every visible text on six routes at 1440px and 390px.
- **List pages are usable on phones** (issue #1356, RNF-05/RNF-06): at 390px the shared `DataTable` showed three columns and pushed the edit and delete buttons off-screen with no scroll affordance, and search inputs were clipped. Below 768px every `DataTable` (29 pages) now renders each row as a card: the identifying field as the title, the other columns as label/value pairs and the actions as a visible 44px button row; desktop keeps the table. The search inputs on personas, escrituras and presupuestos are full width on phones. `data-table-mobile.test.tsx` and Playwright `TS-0109` cover it.
- **Every form dialog is announced with its title, and the close button is localized** (issue #1344, RNF-06): 39 of the 41 `DialogContent` blocks rendered their heading through `FormSection`/`FormHeader` without a Radix `Dialog.Title`, so `role=dialog` had no accessible name, and the close button's hidden text was "Cerrar" in English too. `FormSection` and `FormHeader` take `dialogTitle`, which renders the same heading as `Dialog.Title` (`asChild`, no visual change), and each dialog marks its title. `DialogContent` names the close button with `common.close` ("Cerrar"/"Close") and drops Radix's default `aria-describedby`, since no dialog renders a description. `dialog.test.tsx` (including a scan that every `DialogContent` in `src/app` has a title) and Playwright `TS-0106` cover it.
- **The audit log is paginated server-side; older records are reachable again (slice 1 of #1340)** (issue #1340, RF-44/RNF-03): list screens asked page endpoints for `size=1000` and dropped `totalElements`, so `/dashboard/auditoria` silently hid every record after the 1000th (over 3000 on the dev DB) and rendered 1000 animated rows. It now shows one page (20, 50 or 100 rows, newest first) with the total, keeps `page`, `size` and `module` in the URL, and sends the module filter to the server; the text search is labelled as searching the current page. New `apiGetPage` (keeps the totals and normalises the audit-log page shape), a shared `Pagination` footer (labelled `nav`, range and total, first/previous/next/last, rows per page) and `DataTable` `pagination`/`isFetching` props (`aria-busy` while a page loads). `pagination.test.tsx`, `audit-log-pagination.test.tsx` and Playwright `TS-0104` cover it. People, managements, budgets and deeds follow in later slices.
- **Calendar dates show and round-trip the stored day in any browser zone** (issues #1339, #1338, RNF-08/RF-06/RF-16/RF-17/RF-29): the backend serializes date-only fields as midnight in its JVM zone (2026-09-05 leaves a Madrid server as `2026-09-04T22:00:00.000Z`). `formatDate` read that instant in the browser zone, so every date showed a day early in Argentina; edit dialogs bound it to `<input type="date">`, which stayed empty, or split it on `T` and saved the previous day back; "today" defaults used the UTC day. New `frontend/src/lib/dates.ts` reads an instant as the day of its nearest UTC midnight (correct for any server zone from UTC-12 to UTC+12) and takes `yyyy-MM-dd` literally. `formatDate` uses it, the edit dialogs of presupuestos, pagos, escrituras, suplencias, documentos, documentos-entidades-externas and copias pre-fill through `toDateInputValue`, today defaults use the business time zone (`America/Argentina/Buenos_Aires`, owner decision on #1339), and audit-log timestamps use `formatInstant` in that zone (management history keeps calendar-date display: its TIMESTAMP column holds the day's midnight). `dates.test.ts` (with a guard against `split("T")` and `toISOString()` day slicing), Playwright `TS-0102` and `TS-0103` (Buenos Aires, Madrid, UTC) cover it.
- **Delete failures say why: a referenced record shows an "in use" warning with the backend's reason** (issue #1345, RF-38/RF-53): delete handlers caught every failure as a generic "Error al eliminar", so a 409 such as "Cannot delete: person is referenced by other records." was hidden. New `presentDeleteError` (in `lib/mutation-error.ts`, wired through `useDeleteError`) turns 409 into a warning with `common.errors.inUse` and the server reason, 404 into `common.errors.notFound`, and anything else into the server message or the page fallback. All 18 pages with delete actions use it, including when their `/{id}/in-use` pre-check fails, and a positive pre-check now warns instead of erroring. `delete-error.test.ts` (mapping plus a scan that no page toasts the generic delete error directly) and Playwright `TS-0105` cover it.
- **People are chosen with a searchable picker instead of the first 1000 (slice 3 of #1340)** (issue #1340, CU01/CU02/CU19/CU39): the budget client, the management notary and the management client filter listed `GET /people?size=1000`, so anyone created after the 1000th person could not be chosen and new budgets and managements for new clients could not be created. A new `PersonPicker` combobox searches `/people/search` as you type (debounced, by name or document number), lists the newest people when empty, loads the current person by id in edit forms, and is keyboard and screen-reader friendly (ARIA combobox, `aria-activedescendant`, live status, Escape closes only the popup). The dashboard people count now shows the real total. `person-picker.test.tsx` and Playwright `TS-0111` cover it.
- **The managements list is paginated server-side (slice 4 of #1340)** (issue #1340, CU02/CU19): `/dashboard/gestiones` requested `GET /gestiones?size=1000`, so managements after the 1000th were missing and the dev database (593 managements) rendered every row. Leaving that screen outlasted the 10 s navigation wait of the TS-0070 tour, which failed on every run against the #1394 build and passes with this change. The list now shows one server page (20, 50 or 100 rows, newest first) with the total, the shared `Pagination` footer and `page`/`size` in the URL; the client filter still lists every management of that client. The dashboard managements counter shows the real total; its workflow panel reads only the first management in backend order (`size=1`), as before, instead of the whole list. Option lookups in the Playwright specs are scoped to the open listbox, because the footer's page-size `<select>` also has options. `gestiones-pagination.test.tsx` and Playwright `TS-0112` cover it.
- **The budgets and deeds lists are paginated server-side and their filters work (slice 5 of #1340)** (issue #1340, CU01/CU60/CU06/CU07): `/dashboard/presupuestos` and `/dashboard/escrituras` requested `size=1000`, so budgets after the 1000th were missing and the dashboard counted at most 1000 budgets. Both now show one server page (newest first) with the total, the shared `Pagination` footer and `page`/`size` in the URL; the dashboard budgets counter shows the real total. The budget status filter sent `?estado=` and the deed search `?numero=`, which the backend ignores (it reads `status` and `number`), so both listed every row; they now send the right parameter. A budget number typed in the search box is read from the server, so it is found on any page; a client name filters the rows shown, because the backend has no budget search by name. `presupuesto-plantilla` GW01 uses its own procedure type instead of `.last()`. `budgets-deeds-pagination.test.tsx` and Playwright `TS-0113` cover it.
- **Primary, muted and destructive colours render again (Tailwind v4 theme mapping)** (issue #1337, RNF-05/RNF-09): `globals.css` declared the shadcn HSL tokens in `:root`, but Tailwind v4 only generates colour utilities for colours registered in `@theme`, so `bg-primary`, `text-primary-foreground`, `text-muted-foreground`, `text-destructive`, `bg-secondary`, `border-border`, `ring-ring` and the rest compiled to nothing (43 files): primary buttons were white, the active sidebar item had no pill, delete icons were black. An `@theme inline` block now maps the 19 tokens (`hsl(var(--x))`, opacity modifiers compile to `color-mix()`). The brand primary is #0071E3 (owner decision, 2026-10-09): white text on it is 4.7:1, up from 3.9:1. `theme-css-tokens.test.ts` and Playwright `TS-0101` cover it.
- **DELETE endpoints return 204 No Content as documented (slice of #1315)** (issue #1315, CU15/CU13 and the other CRUD use cases): 17 `DELETE` handlers (`/pagos/{id}`, `/historial/{id}`, `/copia`, `/documento-presentado`, `/tipo-folio`, `/item`, `/gestiones`, `/estado-gestion`, `/tipo-identificacion`, `/tramites`, `/inmuebles`, `/sustitucion`, `/testimonio`, `/movimiento-testimonio`, `/workflow-definition`, `/workflow-node`, `/workflow-transition`) answered 200 with an empty body while the OpenAPI contract documented 204. They now answer 204. `DELETE /roles/usuarios/{idUser}` already answered 204 but was documented as 200; the contract now documents 204 (accepted oasdiff entry under #1315; no response changes). `DeleteStatusMatchesContractTest` compares the documented and returned success status of every `@DeleteMapping`. The frontend needs no change: `apiDelete` only checks `res.ok`. Bruno delete requests assert 204. `DELETE /plantilla-presupuestos/tipo-tramite/{id}/concepto/{id}` stays 200, documented and implemented consistently.
- **The budget template concept DELETE returns 204 No Content too (follow-up of #1315)** (issue #1315, CU49): `DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idProcedureType}/concepto/{idConcept}`, the last DELETE answering 200, now answers 204 and documents 204/404, per the Owner rule that every DELETE returns 204. `DeleteStatusMatchesContractTest` now also fails when any DELETE documents a success status other than 204. The frontend only checks `res.ok`; Bruno `budget-templates/05` expects 204. One accepted oasdiff entry under #1315; the seven entries of #1332, #1333 and #1334, already on `main`, are removed.
- **Report PDFs work again, generated in-house from the current schema** (issue #567, CU01, CU03, CU09, CU13, CU42): the budget, budget-with-properties, procedure documents, management history, document expiry and document debt reports (`/api/v1/reportes/presupuesto/{id}`, `presupuesto-inmuebles/{id}`, `lista-documentos-tramite`, `historial-gestion/{id}`, `documentos-por-vencer/{id}`, `consultar-deuda-documentos`) were JasperReports templates querying the legacy MySQL schema and always answered 500. `ReportDocumentFactory` now builds them from the current domain (budget total and balance from the CU47 summary) and the new `ReportRenderer` port renders them with Apache PDFBox 3.0.8. A missing budget, procedure type, management, submitted document or management number is a 404; OpenAPI documents 200/400/404/500 for the six endpoints. The frontend document debt report sent `numeroGestion` instead of `numberManagement` and always got 400.
- **Paged endpoints document their real paging parameters (#596)** (issue #596, CU74): `GET /api/v1/people`, `/presupuestos`, `/escrituras`, `/tramites` and `/gestiones` were documented with one *required* `pageable` query object, which is not what clients send. They now use springdoc `@ParameterObject`, so the OpenAPI artifact lists optional `page`, `size` and `sort` with their defaults, like `/historial` and `/audit-log`. Runtime behaviour is unchanged. `oasdiff`: 0 breaking errors; 5 warnings (`request-parameter-removed` for the never-usable `pageable` object); `accepted-breaking-changes.txt` untouched. `PagedEndpointsOpenApiParametersIntegrationTest` guards all 7 paged endpoints.
- **Empty or incomplete PUT bodies answer 400 (slice of #655)** (issue #655, CU07/CU53/CU26, Owner decision Oct 9): `PUT /api/v1/testimonio/{id}` with `{}` answered 200 and overwrote the stored `number`, `flagged` and `verified` with 0/false; it now requires all three (NOT NULL columns) and answers 400 naming every missing one (`number: es obligatorio; flagged: es obligatorio; verified: es obligatorio`). `PUT /api/v1/tramites/{id}` requires `idProcedureType` like `POST` (which now answers the standard `ErrorResponse` too). `PUT /api/v1/tipo-tramite/{id}/workflow` requires the `workflowDefinitionId` key: `{}` silently unassigned the workflow, an explicit `null` still unassigns, and a non-integer id answers 400 instead of 500. Validation runs before the 404 lookup, through the new `RequiredFields.check()`. The UI needed no change: it never calls the testimony or procedure PUT and always sends `workflowDefinitionId` (pinned by a vitest guard). **Contract tightening:** the request schemas mark those fields required and the three PUTs document 400; `oasdiff` reports 6 breaking entries, accepted in `backend-api/openapi/accepted-breaking-changes.d/655-put-rejects-incomplete-bodies.txt` (with the `version` entry, 7 in all) because no request without those fields worked as intended. `IncompletePutBodiesIntegrationTest` and Bruno `testimonies/04a`, `procedures/04a`, `procedure-types/04a`–`04b` cover it.
- **A missing or stale version on update answers 400 / 409, not 500 (slice of #655)** (issue #655, CU07/CU12/CU64, Owner decision Oct 9): `PUT /api/v1/testimonio/{id}` copied `version` from the body, so an omitted version read as 0 and a stale one hit the optimistic lock, both answering 500. It now requires `version` (400 `version: es obligatorio`) and answers **409** ("The record was modified by another user; reload it and try again") when the sent version is not the stored one. `ErrorResponses.updateFailed` and `GlobalExceptionHandler` map every optimistic-lock failure to that 409, so the catalog, workflow-definition and testimony-movement PUTs with a stale version answer 409 instead of 500; those seven PUTs now document 409 (non-breaking), and `PUT /movimiento-testimonio/{id}` checks the version before saving. **UI fix:** the folio-type edit dialog sent only `name` and `isAuxiliary`, so every edit after the first failed and `notes`/`enabled` were reset; it now sends the loaded row. **Contract tightening:** one accepted oasdiff entry (`version` became required on `PUT /testimonio/{id}`; the UI never calls it). `StaleOrMissingVersionIntegrationTest`, `GlobalExceptionHandlerOptimisticLockTest`, Bruno `testimonies/04b`–`04c`, `testimony-movements/04b` and Playwright `folio-type-edit-version` cover it.
- **Catalog creates and updates answer 400 for constraint errors (#579 slice 2, catalogs)** (issue #579, CU26/CU27/CU29/CU30/CU36): `POST` and `PUT /{id}` of `/api/v1/conceptos`, `/estado-gestion`, `/tipo-de-documento`, `/tipo-folio` and `/tipo-tramite` answered 409 (create) or 500 (update) when the body violated a database constraint (e.g. `{}` and the NOT NULL `name`), while `GlobalExceptionHandler` answers 400 for the same error. They now answer 400 with the standard `ErrorResponse` and `The submitted data violates a database constraint`, through new `ErrorResponses.createFailed`/`updateFailed` (cause chain with a `DataIntegrityViolationException`, a Hibernate `ConstraintViolationException` or SQLState class 23); other failures keep 409/500, and DELETE keeps 409 for rows still in use. PUT documents 400; no breaking change. The seven accepted oasdiff entries already on `main` are removed. The other controller groups follow in their own PRs. **Duplicates answer 409 (Owner decision Oct 9):** a unique-constraint violation (SQLState 23505, a Hibernate `ConstraintViolationException` of kind `UNIQUE`, or Spring `DuplicateKeyException`) answers 409 with `The submitted data duplicates an existing record`, both in `ErrorResponses` and in `GlobalExceptionHandler`; NOT NULL, foreign-key and check violations keep 400. `PUT /api/v1/roles/{id}` (role name), `POST /api/v1/minutas-inscripcion` (one draft per deed) and `POST /api/v1/cuadernos` (number per year and notary) now document 409, and `PUT /roles/{id}` also documents the 400 its bean validation already returned. `UniqueConstraintConflictIntegrationTest` covers it; no breaking change.
- **Testimony and person creates and updates answer 400 for constraint errors (#579 slice 4)** (issue #579, CU06/CU07/CU08): `POST` and `PUT /{id}` of `/api/v1/testimonio`, `/movimiento-testimonio` and `/people` answered 409 (creates, person update) or 500 (testimony and movement updates) when the data violated a database constraint, e.g. `POST /movimiento-testimonio {}` and the NOT NULL entry date. They now answer 400 with the standard `ErrorResponse` and `The submitted data violates a database constraint` through `ErrorResponses.createFailed`/`updateFailed`; other failures keep their status and a duplicate person keeps its 409 with `existingPersonId`. Any other unique-constraint violation answers 409 through the shared helper (Owner decision Oct 9, from the catalog slice). The three `PUT` operations document 400; no breaking change.
- **Workflow creates and updates answer 400 for missing or invalid data (#579 slice 3, #655)** (issues #579 and #655, CU76/CU83): `POST` and `PUT /{id}` of `/api/v1/workflow-definition`, `/workflow-node` and `/workflow-transition` answered 409 for a NOT NULL violation on create, 500 when a required id was missing (the null id reached the repository), when an update broke a constraint or when a node `type` was unknown, and 200 for a node update with nothing to change. They now answer 400 with the standard `ErrorResponse`: constraint errors through `ErrorResponses.createFailed`/`updateFailed`, missing ids and unknown types through the new `RequiredFields` helper (`"field: reason"` message, no change to the shared `DtoWorkflow*` schemas). Node create requires `workflowDefinitionId`, `statusManagementId` and `type`; node update requires one of `type`, `positionX`, `positionY`; transition create requires `workflowDefinitionId`, `originNodeId` and `destinationNodeId`, and transition update the two node ids. The three `PUT` operations document 400 and the 400 descriptions name the required fields; no breaking change. The workflow editor is unaffected.
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
- **Management lists and picker beyond 1000 cases** (#1340, CU10/CU43/CU04): the
  external-entity documents and documentation re-entry screens read one server page of
  managements (newest first, page in the URL) and the document form picks its management
  with a searchable `ManagementPicker` (case number, client name or document). They loaded
  `GET /gestiones?size=1000`, so every case after the 1000th was missing.
- **Touch targets** (#1369, RNF-06/RNF-07): on touch screens (coarse pointer) small buttons
  and the rows-per-page select are at least 44x44 and row actions sit 8px apart, so taps no
  longer miss or hit the neighbouring delete button. Desktop keeps the 32px compact buttons.
- **The bitácora lists same-day entries in the order they were recorded** (issue #1383, CU13): `history.event_date` only keeps the day and nothing broke ties, so `GET /gestiones/{id}/historial` returned same-day entries in whatever order the database gave, `GET /historial/gestion/{id}` was not sorted at all, and the workflow trace could pick the wrong current step. Every history read path (bitácora, history by management, workflow trace, current state, history report) now uses one order: event date, then `idHistory`. This was behind the recurring TS-0011/TS-0028 e2e flakes. JUnit `HistoryOrderTieBreakerIntegrationTest` and Bruno `history/05` cover it.
- **Deeds are chosen with a searchable picker instead of the first 1000 (slice 8b of #1340)** (issue #1340, CU07/CU08/CU87): the testimony, registration-draft and folio forms read `GET /escrituras?size=1000` and kept the signed deeds in the browser, so once the deeds table passes 1000 rows a newly signed deed could not be chosen. A new `DeedPicker` (shared `SearchCombobox`) lists the 20 newest signed deeds on open (`GET /escrituras?status=Firmada`, #1476), finds any signed deed by number (`GET /escrituras/buscar`) and loads the current deed by id; unsigned deeds are never offered. The folio form still hides a deed already linked to another folio. `useEscrituras()`, the unused `usePersonas()` and the size=1000 helper `apiGetPaged` are removed, and a guard test fails if a source asks for a whole table again. `deed-picker.test.tsx`, `no-whole-table-loads.test.ts` and Playwright `TS-0123` cover it.
- **Deleting an identification type deleted the people that used it (#1361).** `DELETE /api/v1/identification-types/{id}` answered 204 and, through a cascade on the entity, removed every person with that type. It now answers 409 with the reason ("the identification type is in use by people") while people use the type, and the cascade is gone.
- **Removing a required document from a procedure type really removes it** (issue #1362, CU55): `DELETE /api/v1/procedure-templates/{type}/{document}` answered 204 but the row stayed, because the document type's eagerly loaded, cascading template list re-persisted it. It is now a bulk delete. Documentos necesarios also showed every required document with a blank name: it read nested objects the API never sends; names now come from the document-type catalog.
- **An identification type's number length is validated** (issue #1526, CU17): `characters` is required and must be a whole number from 1 to 50 on POST and PUT `/api/v1/identification-types`. A missing value answered 500 and any text was stored; both now answer 400. The admin form uses the same rule. Accepted OpenAPI break: `accepted-breaking-changes.d/1526-identification-type-characters.txt`.






### Added

- **Testimony edit, delete and movement history** (issue #1364, CU07 / CU44 / RF-31): on
  /dashboard/testimonies each row can open **Ver movimientos** (the testimony's registry
  movements, newest first, with the withdrawal card number), **Editar** (number and notes) and
  **Eliminar** (with confirmation). A testimony another user changed, or one with movements, says
  why it can't be saved or deleted. The history comes with the testimony list; manual movement
  correction stays API-only.

- **Required documents seeded on complete-case** (issue #773, CU02 / CU03 / CU04 / CU72):
  when `POST /api/v1/managements/complete-case` (or PUT that creates the first procedure)
  confirms a trámite, the API materializes one pending `SubmittedDocument` per
  `ProcedureTemplate` row for that procedure type (`delivered=false`, `reentered=false`).
  Seeding is idempotent on `(procedureId, documentTypeId)` and does not wipe docs when
  PUT only updates an existing procedure. `GET /api/v1/managements/{id}/case-summary`
  lists the checklist; the management summary dialog shows it. Covered by
  `RequiredDocumentationOnConfirmServiceTest`,
  `ManagementCompleteCaseRequiredDocsIntegrationTest`, and Playwright TS-0141.
- **Skip to content and focus on route change** (issue #1352, RNF-10, CU76): every dashboard page starts
  with a "Saltar al contenido" / "Skip to content" link that appears on the first Tab and moves focus to
  `<main id="main-content">` (WCAG 2.4.1). After a client-side navigation, focus moves to the new page's `<h1>`
  instead of staying on the clicked sidebar link (WCAG 2.4.3). The first load and `?page=`/filter changes leave
  focus alone. Guarded by `dashboard-layout.test.tsx` and Playwright TS-0108.
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
- **Deed list status filter** (#1340, CU06/CU07/CU82): `GET /api/v1/escrituras` takes an
  optional `status` (Sin Firmar, Firmada, Anulada, No Paso, Inscripta; any letter case,
  anything else answers 400) and returns one page of deeds in that status, so deed pickers
  no longer need the whole list to find signed deeds. Non-breaking.

- **App-shell skeleton instead of a blank first paint** (issue #1359, RNF-03, RNF-05): the dashboard layout returned `null` until the client had mounted and read the stored session, so the server HTML of every dashboard URL was empty and each hard load painted a white page before the shell popped in. It now renders `components/layout/AppShellSkeleton.tsx` (busy `role="status"` region labelled `common.loading`, desktop sidebar block as wide as `AppSidebar`, header row, content blocks), which the real shell replaces without layout shift; the redirect to `/login` is unchanged. New shared `components/ui/skeleton.tsx` (`aria-hidden`, `motion-safe:animate-pulse`, a static tint under reduced motion) is used by `DataTable`'s loading rows and cards and by the workflow hero; the stat skeleton pulses only with motion allowed, and a guard keeps plain `animate-pulse` out of components. Vitest `app-shell-skeleton.test.tsx` and Playwright `TS-0125` cover it.
- **One status badge on every status column** (issue #1366, RF-23, RNF-09): budgets, cases and testimonies showed status as plain text, folios the raw stored value, and protocol and users ad-hoc badges where color was the only difference. New `components/shared/StatusBadge.tsx` with `lib/status-maps.ts` maps a domain (`budget`, `management`, `testimony`, `folio`, `availability`, `user`) and a stored value to a tone (neutral, info, success, warning, danger), a decorative lucide icon and a text label, so no status relies on color alone (WCAG 1.4.1). Budget labels reuse `presupuestos.status.*` (#1346); management statuses keep their catalog name and only the seeded names get a tone; folio labels are new (`status.folio.*`, es/en); a value outside a vocabulary shows as stored in a neutral badge, an empty one as a dash. Wired on presupuestos, gestiones, testimonios, protocolo, administracion/usuarios and administracion/folios. Vitest `status-badge.test.tsx` and Playwright `TS-0124` cover it.
- **Full management edit** (issue #1360, CU53, CU22/CU59, RF-22): "Editar" on `/dashboard/managements` showed only the number and saved `PUT /managements/{id}` with `{ number }`, so a wrong notary, budget, procedure type or property meant deleting the management and losing its history. New `GET /api/v1/managements/{id}/complete-case` returns the case values in the shape `PUT /managements/{id}/complete-case` accepts (404 for an unknown id; additive). The edit dialog now shows every field prefilled from it and saves through the PUT, sending the heading and notes back unchanged; the status is read-only there (CU83, changed through "Cambiar estado"), and the substitution toast appears only when the save adds a redirect note. The edit reuses the create's wire-name mapper (#1502). `useUpdateManagement()` is gone; `PUT /managements/{id}` is allowlisted as bucket D. JUnit `ManagementCompleteCaseReadIntegrationTest`, Vitest `useManagements.complete-case.test.tsx`, Bruno `procedure-folders/05b-05d` and Playwright `TS-0126` cover it.
- **Identification type admin catalog (#1361).** Administración has a "Tipos de identificación" page (ADMIN only) to list, create, edit and delete the identification types the person form offers, with each number's length. A name that another type already has, in any letter case, is refused with 409 ("Ya existe un tipo de identificación con ese nombre"), so the DNI default stays unambiguous. `POST`, `PUT` and `DELETE /api/v1/identification-types` leave the reachability allowlist.
- **Required documents per procedure type** (issue #1362, CU55, RF-53): Administración → Trámites has a "Documentos requeridos" action per procedure type to list, add (only document types not listed yet; a duplicate shows a message), edit the notes of and remove (with confirmation) the documents that procedure requires. POST/PUT/DELETE `/procedure-templates` leave the reachability allowlist. Playwright TS-0142, Bruno procedure-templates 03-09.






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

- **Security reports on the local infra hub** (CU78, #1506): the case catalog
  HTML is served at `http://localhost:8890` and linked from Homer. The CI
  security job runs without waiting for the Java build and writes the case
  summary on the Actions run.
- **Local security gates in the AI SDLC** (CU78, #1500): the security case
  catalog runs before a pull request from `workspace/sdlc/run_pipeline.sh`
  (Semgrep, secret scan, Trivy, unauthenticated DAST baseline). CI runs the
  same catalog and keeps CodeQL and OWASP ZAP.
- **Sec by Design program** (CU78, #307): system threat model (STRIDE and
  LINDDUN), OWASP Top 10 map, toolchain selection, security Grafana dashboard,
  login-lockout alert, and a failing secret scan in CI.
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
