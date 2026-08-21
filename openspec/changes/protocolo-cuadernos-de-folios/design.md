> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Folio` (`negocio/Folio.java`) already models `numero`, `anio`,
`fkIdPersonaEscribano` and `estado` (free-text: e.g. `Nuevo`, `Utilizado`).
There is no `Cuaderno` entity anywhere in the codebase — this is the first
change to introduce it. `TipoDeFolio` and `Folio` already follow the
`negocio` (entity) / `repository` (Spring Data) / `api` (REST controller)
layering; reports are generated via JasperReports in `ReporteController`
(`.jasper`/`.jrxml` under `src/main/resources/reportes/`). See proposal.md —
Objetivo, What Changes.

## Goals / Non-Goals

**Goals:**
- Model the `Cuaderno` grouping of ten consecutive folios with correlative
  numbering per registro/año.
- Validate the consecutiveness and multiple-of-ten constraints at creation.
- Generate the official carátula as a printable document.

**Non-Goals:**
- Any of the other four protocolo areas from Issue #839 (carpetas de
  trámite, protocolo auxiliar, minuta de inscripción, numeración de
  escrituras) — each is its own change.
- Physical printing integration — the carátula is produced as a downloadable
  PDF, same as every other report in the system.

## Decisions

- **New `Cuaderno` entity with `Folio.fkIdCuaderno` (owning side on `Folio`).**
  Alternative considered: model the grouping only as a range (`folioDesde`,
  `folioHasta`) on `Cuaderno` without a back-reference on `Folio` — rejected
  because folios are not guaranteed contiguous by ID once damaged/anulado
  folios are excluded from a future cuaderno, so an explicit FK per folio is
  the only way to know membership unambiguously and to query "folios
  disponibles" (not yet assigned) directly.
- **Correlative numbering computed server-side at creation time, not
  user-supplied.** Alternative considered: let the user type the cuaderno
  number like `Escritura.numero` today — rejected deliberately: CU80's
  explicit excepción 3.1 requires the system to recalculate on conflict,
  i.e. numbering is a system responsibility here, not user input (contrast
  with `protocolo-numeracion-escrituras`, where the number stays
  user-entered and the system only validates it — that is Escritura's
  existing convention, not this capability's).
- **Carátula via JasperReports, following `ReporteController`.** Alternative
  considered: a plain server-rendered HTML/PDF — rejected to stay consistent
  with the project's single reporting mechanism, avoiding a second reporting
  stack for one document type.

## Riesgos / Trade-offs

- [Folios agrupados en un cuaderno con "Errose"/"no pasó" mezclan justificación
  a nivel cuaderno, no a nivel folio individual] → Mitigation: la
  justificación se guarda en `Cuaderno.observaciones`; si en el futuro se
  necesita una justificación por folio, es una extensión aditiva del modelo,
  no un rediseño.
- [La carátula depende del vínculo `Folio ↔ Escritura` de otro change
  (`folio-vinculacion-escritura`, #838) para listar los trámites otorgados] →
  Mitigation: declarado explícitamente en proposal.md — Out of Scope; la
  carátula se genera igual, con esa sección vacía si el otro change no está
  desplegado.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Alta de cuaderno con rango válido | integration | `CuadernoControllerTest#shouldCreateCuadernoFromConsecutiveFolios` |
| Cantidad de folios no múltiplo de diez | integration | `CuadernoControllerTest#shouldRejectCuadernoWhenFolioCountNotMultipleOfTen` |
| Rango de folios discontinuo | integration | `CuadernoControllerTest#shouldRejectCuadernoWithNonConsecutiveFolios` |
| Folio ya asignado a otro cuaderno | integration | `CuadernoControllerTest#shouldRejectCuadernoWithFolioAlreadyAssigned` |
| Lote con folio dañado o anulado justificado | integration | `CuadernoControllerTest#shouldCreateCuadernoWithJustifiedDamagedFolio` |
| Estado de folio tras generar el cuaderno | unit | `CuadernoTest#shouldMarkFoliosAsAsignadoACuaderno` |
| Primer cuaderno del año para un registro | unit | `CuadernoTest#shouldAssignNumberOneToFirstCuadernoOfYear` |
| Conflicto de numeración | unit | `CuadernoTest#shouldRecalculateNextAvailableCuadernoNumber` |
| Emisión de carátula de un cuaderno existente | integration | `CuadernoControllerTest#shouldGenerateCaratulaForExistingCuaderno` |
| Emisión de carátula de un cuaderno inexistente | integration | `CuadernoControllerTest#shouldReturnNotFoundForMissingCuadernoCaratula` |

- New unit tests (`src/test/java/.../unit/`): `CuadernoTest` (numbering and state transition logic).
- New integration tests (`src/test/java/.../integration/`): `CuadernoControllerTest` (8 cases above).
- Coverage impact: new entity + controller fully covered by the tests above; expected to hold or raise the JaCoCo ratchet floor.

## Regression Strategy

- Existing tests affected: `FolioControllerTest` — none of its existing
  assertions change; `fkIdCuaderno` is a new nullable field, additive.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk: none — `jpa` package and `frontend-swing` do not
  touch `Folio`.

## Playwright Strategy

- Specs to add under `frontend/tests/e2e/`: `cuadernos.spec.ts`.
- Golden path covered: seleccionar diez folios disponibles consecutivos,
  generar el cuaderno, descargar la carátula.
- Edge / error paths covered: intentar generar un cuaderno con una cantidad
  de folios no múltiplo de diez (mensaje de error visible); intentar generar
  un cuaderno con un folio ya asignado (mensaje de error visible).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: yes — `V{n}__create_cuadernos_table.sql`
  (nueva tabla `cuadernos`; columna `fk_id_cuaderno` nullable en `folios`).
- Deployment order / coupling: migración y código se despliegan juntos (un
  solo deploy); la migración es aditiva, no requiere ventana de
  mantenimiento.
- Configuration or `.env` keys to add: ninguna.
- Feature flag: no.
- Smoke test after deploy (Gate 5): generar un cuaderno de prueba con diez
  folios consecutivos vía UI, confirmar que los folios cambian a "Asignado a
  cuaderno" y que la carátula se descarga correctamente; `GET
  /actuator/health` en verde.

## Rollback Strategy

- Revert safe: yes — código aditivo; revertir el código deja la tabla
  `cuadernos` y la columna `fk_id_cuaderno` sin uso pero sin romper ninguna
  funcionalidad existente.
- Database rollback: `R{n}` opcional para eliminar `cuadernos` y la columna
  `fk_id_cuaderno` si se decide no continuar; no es obligatorio revertir el
  esquema para revertir el código (Flyway nunca revierte automáticamente).
- Data written under the new behavior after revert: los cuadernos ya creados
  y los folios marcados "Asignado a cuaderno" quedan en la base sin ser
  accesibles vía API hasta un nuevo deploy.
- Blast radius if rollback is delayed: ninguno — ninguna otra capability
  depende de `Cuaderno`.

## Open Questions

Ninguna.
