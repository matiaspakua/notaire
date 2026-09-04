> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Inmueble` (`negocio/Inmueble.java`) only models `nomenclaturaCatastral`,
`valuacionFiscal`, `domicilio` and `observaciones` — it has no matrícula,
tomo/folio/finca or linderos, which CU82 requires to be shown on the minuta
and validated before it can be generated. `Escritura`
(`negocio/Escritura.java`) already has `matriculaInscripcion` and
`fechaInscripcion` fields, but they model only a single flat final-inscription
snapshot — there is no state machine, no presentation date, no registro entry
number, and no "Observado" tracking, so they cannot represent CU82's full
circuit (Generada → Presentada → Observada / Inscripta). `MovimientoTestimonio`
(`negocio/MovimientoTestimonio.java`) already models a similar
ingreso/salida/inscripción shape, but it is scoped to physical `Testimonio`
carton movements (`numeroCarton`), a different concern from tracking a
registry submission tied to an `Escritura`+`Inmueble` — reusing it would
overload its purpose. `Tramite` (`negocio/Tramite.java`) already has
`fkIdInmueble`, `fkIdEscritura` and `fkIdGestion`, so "gestión de inmuebles
con escritura aprobada" (CU82 paso 1) can be expressed as "trámite con
inmueble y escritura no nula, escritura firmada". `ReporteController`
(`api/ReporteController.java`) already has the JasperReports pattern this
change follows for the printable "formulario normalizado" (paso 3). See
proposal.md — Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Let `Inmueble` carry the registral data (matrícula, tomo/folio/finca,
  linderos) CU82 requires.
- Generate a Minuta de Inscripción from an approved escritura on an
  inmueble, validating completeness of catastral/registral data first.
- Print the minuta in the normalized registry form.
- Track the registry circuit: presentación, observación, inscripción
  definitiva.

**Non-Goals:**
- Reimplementing CU44 (subsanación y reingreso del testimonio) — this
  change only records the "Observado" state and subsanación date; the
  correction/reentry circuit itself is out of scope.
- Fetching catastral data from an external registry (CU69) — assumed
  already loaded on `Inmueble`.
- Any of the other four remaining protocolo areas from Issue #839.

## Decisions

- **New `MinutaInscripcion` entity, one-to-one with `Escritura`, rather than
  extending `Escritura.matriculaInscripcion`/`fechaInscripcion` in place.**
  Alternative considered: add the circuit fields (estado, fecha
  presentación, número entrada registral, etc.) directly onto `Escritura` —
  rejected because `Escritura` already models the escritura's own lifecycle
  (`estado` = firmada/sin firmar) and mixing in the registry circuit's
  separate state machine would violate SRP; a dedicated entity also makes
  "no minuta for this escritura" the natural default (no row), matching how
  only escrituras with `fkIdInmueble` need one.
- **Registral data (`matricula`, `tomoFolioFinca`, `linderos`) lives on
  `Inmueble`, not on `MinutaInscripcion`.** These identify the property
  itself and do not change between escrituras/minutas over that inmueble;
  putting them on `MinutaInscripcion` would duplicate and risk drifting
  them across minutas for the same inmueble. `MinutaInscripcion` holds only
  the workflow-specific data (número, precio de la operación, estado,
  fechas, observaciones del registro).
- **`estado` on `MinutaInscripcion` as a `String` constant, matching the
  existing `Escritura.estado`/`Tramite` pattern**, not a new enum type —
  consistent with how state is modeled elsewhere in `negocio` (see
  `ConstantesNegocio`), avoiding introducing a new pattern for one entity.
- **Do not reuse `MovimientoTestimonio` for the registry circuit.**
  Alternative considered: add an `inscripta`/observaciones-style tracking
  row on `MovimientoTestimonio` for this too — rejected: it already has a
  distinct purpose (physical testimonio carton delivery, `numeroCarton`)
  unrelated to `Inmueble`/matrícula; conflating them would make both harder
  to reason about (SRP).
- **New JasperReports template following `ReporteController`'s existing
  pattern**, not a reused/generic template — CU82 explicitly requires a
  "formulario normalizado" specific to registry submission.

## Riesgos / Trade-offs

- [Los datos catastrales/registrales de `Inmueble` pueden estar
  incompletos para inmuebles ya cargados antes de este cambio, bloqueando
  la generación de minutas hasta completarlos] → Mitigation: comportamiento
  esperado y explícito (CU82 excepción 2.1); el mensaje de error debe listar
  los campos faltantes para que el Gestor los complete antes de reintentar.
- [Un inmueble podría, en teoría, tener más de una escritura pendiente de
  minuta simultáneamente si participa en varios trámites] → Mitigation: la
  relación uno-a-uno es entre `MinutaInscripcion` y `Escritura`, no entre
  `MinutaInscripcion` e `Inmueble`, así que cada escritura tiene su propia
  minuta sin conflicto.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Cargar datos registrales de un inmueble | integration | `InmuebleControllerTest#shouldSaveMatriculaTomoFolioFincaYLinderos` |
| Generar minuta con datos completos | integration | `MinutaInscripcionControllerTest#shouldGenerateMinutaWhenDataIsComplete` |
| Intento de generar minuta con datos incompletos | integration | `MinutaInscripcionControllerTest#shouldRejectGenerationWhenDataIsIncomplete` |
| Imprimir la minuta en formulario normalizado | integration | `ReporteControllerTest#shouldGenerateMinutaInscripcionReport` |
| Registrar presentación | integration | `MinutaInscripcionControllerTest#shouldRegisterPresentacion` |
| Registrar observación | integration | `MinutaInscripcionControllerTest#shouldRegisterObservacion` |
| Registrar inscripción definitiva | integration | `MinutaInscripcionControllerTest#shouldRegisterInscripcionDefinitiva` |

- New unit tests (`src/test/java/.../unit/`): `MinutaInscripcionServiceTest`
  (validación de datos completos, transiciones de estado).
- New integration tests (`src/test/java/.../integration/`):
  `InmuebleControllerTest` (extended), `MinutaInscripcionControllerTest`,
  `ReporteControllerTest` (extended).
- Coverage impact: new entity + service + controller + report fully
  covered; expected to hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `InmuebleControllerTest` — existing assertions
  unaffected; new fields default to `null`/empty. `ReporteControllerTest` —
  existing report tests unaffected; new test appended.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `Inmueble` or the escritura circuit in a way this change modifies.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `minuta-inscripcion.spec.ts`.
- Golden path covered: completar datos registrales de un inmueble, generar
  la minuta desde una escritura aprobada, registrar presentación y luego
  inscripción definitiva.
- Edge / error paths covered: intentar generar la minuta con datos
  catastrales incompletos (mensaje de error visible); registrar una
  observación del Registro.
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes —
  `V{n}__add_datos_registrales_inmueble_and_create_minutas_inscripcion.sql`
  (columnas `matricula`, `tomo_folio_finca`, `linderos` en `inmuebles`;
  nueva tabla `minutas_inscripcion` con FK única a `escrituras`).
- Deployment order / coupling: migración y código se despliegan juntos (un
  solo deploy); la migración es aditiva, no requiere ventana de
  mantenimiento.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): completar datos registrales de un
  inmueble, generar una minuta desde una escritura aprobada, descargar el
  reporte, registrar presentación e inscripción definitiva;
  `GET /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — código aditivo; revertir el código deja las columnas y
  la tabla nuevas sin uso pero sin romper ninguna funcionalidad existente.
- Database rollback: `R{n}` opcional para eliminar `minutas_inscripcion` y
  las columnas nuevas de `inmuebles` si se decide no continuar; no es
  obligatorio revertir el esquema para revertir el código.
- Data written under the new behavior after revert: las minutas ya
  generadas y sus datos de circuito quedan en la base sin ser accesibles
  vía el nuevo endpoint hasta un nuevo deploy.
- Blast radius if rollback is delayed: bajo — ningún otro cambio propuesto
  bajo el Issue #839 depende de `MinutaInscripcion`.

## Open Questions

Ninguna.
