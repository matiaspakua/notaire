# Administrar carpetas de trámite

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #839 |
| Use Case | CU85 – Administrar Carpetas de Trámite |
| Branch | `feat/839_protocolo-carpetas-de-tramite` |
| Gate 1 status | pending |

## Objetivo

Cuando se inicia una gestión, el sistema no genera ninguna carpeta de trámite
que agrupe la documentación asociada, ni tiene forma de reflejar su ciclo de
vida (activa, en espera, archivada): hoy la única noción parecida es el campo
`numero` de `GestionDeEscritura`, documentado en su propio Javadoc como
"número de carpeta" pero sin entidad, estado ni vínculo real con la
documentación del trámite (CU85). Este cambio implementa una de las cinco
áreas del bloque de protocolo sin desarrollo cubierto por el Issue #839; se
propone y se implementa por separado de las otras cuatro porque, más allá del
dominio "protocolo", no comparten código entre sí (ver Technical Notes del
Issue).

## What Changes

- Nueva entidad `CarpetaTramite`: número único, estado (`Activa` / `Espera` /
  `Archivada`), motivo de espera, vínculo a la `GestionDeEscritura` y al
  `Tramite` que agrupa.
- Al iniciarse un trámite dentro de una gestión, el sistema genera
  automáticamente su carpeta en estado `Activa`. Si la gestión agrupa más de
  un trámite, se genera una carpeta por trámite, todas vinculadas a la misma
  gestión.
- Nuevo endpoint para poner una carpeta en espera, registrando el motivo.
- Nuevo endpoint para consultar el estado de una carpeta (número, estado
  actual, gestión y trámite asociados).
- `GestionArchiveDebtService.archivar` (CU16) pasa también todas las carpetas
  de los trámites de esa gestión a `Archivada`; si alguna carpeta sigue en
  estado `Espera` sin motivo resuelto, el sistema alerta y exige confirmación
  explícita antes de archivar.
- Nueva pantalla de consulta de carpetas de trámite (por gestión o por
  trámite), con acción para ponerlas en espera.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Al iniciar un trámite dentro de una gestión, el sistema genera automáticamente su carpeta con número único, en estado "Activa". | CU85 (paso 2) | New. |
| Si la gestión agrupa más de un trámite, se genera una carpeta por trámite, cada una con su propio número único, todas vinculadas a la misma gestión. | CU85 (excepción 2.1) | New. |
| La carpeta puede consultarse en cualquier momento, mostrando número, estado actual, gestión y trámite(s) asociados. | CU85 (paso 3) | New. |
| La carpeta puede ponerse en estado "Espera", registrando el motivo. | CU85 (paso 4) | New. |
| Al archivar la gestión asociada (CU16), la carpeta pasa a estado "Archivada". | CU85 (paso 5) | New. |
| Si se intenta archivar una gestión con una carpeta en estado "Espera" sin motivo resuelto, el sistema alerta y solicita confirmación explícita antes de archivar. | CU85 (excepción 5.1) | New. |

## Capabilities

### New Capabilities
- `carpetas-de-tramite`: generar y administrar el ciclo de vida (activa,
  espera, archivada) de la carpeta de cada trámite.

### Modified Capabilities
(ninguna — no existe spec previo para `GestionDeEscritura`/archivado como
capability propia; el hook de `GestionArchiveDebtService.archivar` es un
detalle de implementación de la nueva capability.)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nueva entidad `CarpetaTramite`, `CarpetaTramiteController`, `CarpetaTramiteRepository`, `CarpetaTramiteService` (generación automática + hook de archivado sobre `GestionArchiveDebtService`) |
| `frontend` | yes | Nueva pantalla de consulta/administración de carpetas de trámite |
| `frontend-swing` | no | — |
| `notaire-shared` | yes | Nuevo `DtoCarpetaTramite` |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `CarpetaTramite` (nueva); ninguna modificación de `Tramite` ni
  `GestionDeEscritura` (el vínculo es `CarpetaTramite → Tramite`/`Gestion`,
  no al revés, para no tocar contratos existentes).
- Endpoints: `GET /api/v1/carpetas/{id}`, `GET
  /api/v1/carpetas?gestionId=&tramiteId=`, `PUT /api/v1/carpetas/{id}/espera`
  (body: motivo).
- Database (Flyway `V{n}`): nueva tabla `carpetas_tramite` (`id_carpeta`,
  `numero`, `estado`, `motivo_espera`, `fk_id_gestion`, `fk_id_tramite`,
  `version`).
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.
- No es BREAKING: no modifica ningún contrato existente; la generación
  automática de carpetas se dispara desde el flujo de alta de trámite ya
  existente, sin cambiar su firma pública.

### Architecture review

Sigue la arquitectura existente: entidad en `negocio`, repositorio en
`repository` (Spring Data), controller REST en `api`, lógica de negocio en
`service` (nuevo `CarpetaTramiteService`, siguiendo el patrón de
`GestionArchiveDebtService`). No requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de Trámite.md` | Completar el campo GitHub ID; confirmar que el flujo documentado coincide con lo implementado. |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | Nota: archivar una gestión ahora también archiva las carpetas de sus trámites, con alerta si alguna está en "Espera". |
| `CHANGELOG.md` | `[Unreleased]` — generar y administrar carpetas de trámite. |

## Out of Scope

- **Documento físico/digitalización de la carpeta**: este cambio modela el
  ciclo de vida administrativo de la carpeta (activa/espera/archivada); la
  gestión documental (adjuntar archivos, escaneos) ya existe vía
  `DocumentoPresentado` y no se modifica aquí.
- **Cambiar el significado de `GestionDeEscritura.numero`**: ese campo queda
  como está (su Javadoc lo describe como "número de carpeta" en un sentido
  informal previo); `CarpetaTramite.numero` es la numeración formal por
  trámite que introduce este cambio, y no reemplaza ni migra el campo
  existente.
- **Las otras cuatro áreas del Issue #839** (cuadernos de folios, protocolo
  auxiliar, minuta de inscripción, numeración de escrituras) se implementan
  como cambios OpenSpec independientes: `protocolo-cuadernos-de-folios`,
  `protocolo-auxiliar-tramites`, `protocolo-minuta-inscripcion`,
  `protocolo-numeracion-escrituras`.
