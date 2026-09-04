# Administrar cuadernos de folios

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #839 |
| Use Case | CU80 – Administrar Cuadernos de Folios |
| Branch | `feat/839_protocolo-cuadernos-de-folios` |
| Gate 1 status | pending |

## Objetivo

Los folios notariales deben agruparse de a diez en cuadernos correlativos con
carátula oficial para cumplir la normativa del protocolo, pero el sistema no
tiene ninguna acción para generar cuadernos ni emitir su carátula: hoy los
folios se administran individualmente y sin esa agrupación reglamentaria
(CU80). Este cambio implementa una de las cinco áreas del bloque de protocolo
sin desarrollo cubierto por el Issue #839; se propone y se implementa por
separado de las otras cuatro porque, más allá del dominio "protocolo", no
comparten código entre sí (ver Technical Notes del Issue).

## What Changes

- Nueva entidad `Cuaderno`: agrupa exactamente 10 `Folio` consecutivos de un
  mismo registro notarial, con número correlativo (1..N) por año y registro.
- `Folio` gana una referencia opcional a su `Cuaderno` (`fkIdCuaderno`); al
  agruparse en un cuaderno, el folio pasa a `estado = "Asignado a cuaderno"`.
- Nuevo endpoint para generar cuadernos a partir de un rango de folios
  disponibles y estrictamente consecutivos (múltiplo exacto de 10).
- Nuevo endpoint para emitir la carátula de un cuaderno (JasperReports, mismo
  patrón que `ReporteController`): año, registro del escribano, número de
  cuaderno, rango de folios y detalle de escrituras/trámites otorgados en
  esos folios.
- Nueva pantalla de administración de cuadernos (listar folios disponibles
  ordenados, seleccionar rango, generar cuaderno, descargar carátula).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un cuaderno agrupa exactamente 10 folios consecutivos de un mismo registro notarial. | CU80 (paso 2) | New. |
| La cantidad de folios a agrupar debe ser múltiplo exacto de 10, y la numeración estrictamente consecutiva y sin faltantes. | CU80 (paso 2, excepción 2.1) | New. |
| Al generar un cuaderno, cada folio agrupado pasa a `estado = "Asignado a cuaderno"`. | CU80 (paso 3) | New. |
| El número de cuaderno es correlativo (1 a N) por registro notarial y año; ante conflicto, se recalcula el siguiente disponible. | CU80 (paso 3, excepción 3.1) | New. |
| Un folio en estado dañado ("Errose") o anulado ("no pasó") puede incluirse en el lote si se registra una justificación/observación. | CU80 (excepción 2.2) | New. |
| La carátula de un cuaderno incluye año, registro del escribano, número de cuaderno, rango de folios y detalle de escrituras/trámites otorgados. | CU80 (paso 4) | New. |

## Capabilities

### New Capabilities
- `cuadernos-de-folios`: generar cuadernos de 10 folios consecutivos,
  numerarlos correlativamente y emitir su carátula oficial.

### Modified Capabilities
(ninguna — no existe spec previo para `Folio` como capability propia; el
campo `fkIdCuaderno` es un detalle de implementación de la nueva capability,
no un cambio de comportamiento de una capability existente.)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nueva entidad `Cuaderno`, `CuadernoController`, `CuadernoRepository`, campo `fkIdCuaderno` en `Folio`, plantilla JasperReports de carátula |
| `frontend` | yes | Nueva pantalla `administracion/cuadernos` |
| `frontend-swing` | no | — |
| `notaire-shared` | yes | Nuevo `DtoCuaderno` |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Cuaderno` (nueva), `Folio` (agrega `fkIdCuaderno`).
- Endpoints: `POST /api/v1/cuadernos` (generar), `GET /api/v1/cuadernos`,
  `GET /api/v1/cuadernos/{id}`, `GET /api/v1/cuadernos/{id}/caratula` (PDF).
- Database (Flyway `V{n}`): nueva tabla `cuadernos` (`id_cuaderno`, `numero`,
  `anio`, `fk_id_persona_escribano`, `version`) + columna
  `fk_id_cuaderno` nullable en `folios`.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva — reutiliza JasperReports ya presente en el
  proyecto (`ReporteController`).
- No es BREAKING: `Folio` gana un campo opcional, no se modifica ningún
  contrato existente.

### Architecture review

Sigue la arquitectura existente: entidad en `negocio`, repositorio en
`repository` (Spring Data, no `jpa` legacy), controller REST en `api`, reporte
vía JasperReports siguiendo el patrón de `ReporteController`. No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU80 – Administrar Cuadernos de Folios.md` | Confirmar que el flujo documentado coincide con lo implementado; completar el campo GitHub ID si aplica. |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | Nota: un folio agrupado en un cuaderno queda en `estado = "Asignado a cuaderno"`. |
| `CHANGELOG.md` | `[Unreleased]` — generar cuadernos de folios y emitir su carátula. |

## Out of Scope

- **Impresión física directa**: este cambio genera el PDF de la carátula
  reglamentaria; la impresión en papel oficial queda a cargo del usuario con
  su propio dispositivo, como ya ocurre con el resto de los reportes del
  sistema (`ReporteController`).
- **Vínculo automático cuaderno → escritura/trámite en la carátula**: la
  carátula lista las escrituras/trámites otorgados en los folios del cuaderno
  usando el vínculo `Folio ↔ Escritura` de `folio-vinculacion-escritura`
  (issue #838); si ese cambio no está desplegado aún, la carátula se genera
  igual pero con el detalle de trámites vacío para folios sin escritura
  vinculada — no es un bloqueante de este cambio.
- **Las otras cuatro áreas del Issue #839** (carpetas de trámite, protocolo
  auxiliar, minuta de inscripción, numeración de escrituras) se implementan
  como cambios OpenSpec independientes: `protocolo-carpetas-de-tramite`,
  `protocolo-auxiliar-tramites`, `protocolo-minuta-inscripcion`,
  `protocolo-numeracion-escrituras`.
