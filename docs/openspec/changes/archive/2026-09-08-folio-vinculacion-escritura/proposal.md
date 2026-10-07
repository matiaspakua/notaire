# Vincular escritura a folio y validar copia/testimonio ya inscripto

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #838 |
| Use Case | CU87 – Vincular Escritura a Folio y Copia a Testimonio |
| Branch | `feat/838_folio-vinculacion-escritura` |
| Gate 1 status | pending |

## Objetivo

Los folios y las escrituras se administran cada uno por su lado; no existe
ninguna acción en el sistema que registre que una escritura ocupa determinado
folio, dejando ese vínculo — que es el protocolo notarial propiamente dicho —
sin ningún camino para armarse a través del producto (CU87).

## What Changes

- `Folio` pasa a poder vincularse a la `Escritura` que ocupa: se agrega
  `escrituraId` a `FolioController.FolioRequest` (alta y edición), y
  `Folio.setAtributos`/`getDto` empiezan a leer y devolver el campo
  `escritura` de `DtoFolio` (hoy existe en el DTO pero ninguno de los dos
  métodos lo toca).
- Al vincular un folio a una escritura, el folio pasa automáticamente a
  `estado = "Utilizado"` (el valor ya existente para "en uso", ver
  `FolioController.ESTADO_UTILIZADO`; no se introduce un estado nuevo
  "Ocupado" — ver Reglas de negocio).
- **BREAKING (contrato de datos)**: `POST/PUT /api/v1/folio` rechaza con 409
  un `escrituraId` que corresponda a un folio ya vinculado a otra escritura
  activa (`estado = "Utilizado"`).
- `CopiaController.create` valida que el `Testimonio` de origen no tenga
  ningún `MovimientoTestimonio` con `inscripta = true`; si lo tiene, rechaza
  la copia con 409. El vínculo `Copia → Testimonio` en sí ya existe y
  funciona (`Copia.fkIdTestimonio`, DTO-wired) — este cambio agrega
  exclusivamente la validación de negocio que falta.
- Frontend: la página de administración de folios (`administracion/folios`)
  agrega un selector de escritura (mostrando escrituras `estado = "Firmada"`
  sin folio vinculado) al alta/edición de folio.
- Frontend: se corrige el tipo `Escritura.folio?: Folio` (singular, y nunca
  poblado porque el backend serializa `folios` en plural y la entidad tiene
  `folioList` con `@JsonIgnore`) por `Escritura.folios?: Folio[]`, alineado
  con `DtoEscritura.getFolios()`, y se corrige la columna de folio en
  `escrituras/page.tsx` para que muestre datos reales en lugar de `"—"`
  permanente.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Un folio se vincula a la escritura que ocupa; el vínculo se registra desde el folio (columna `fk_id_escritura`, ya existente en el esquema). | CU87 | Made explicit — la columna existe desde antes pero nunca se escribía ni leía. |
| Vincular un folio a una escritura marca el folio como `Utilizado`. | CU87 (AC 2: "el sistema... actualiza el estado del folio a 'Ocupado'") | Changed — se reutiliza el valor de estado `Utilizado` ya existente en vez de introducir `Ocupado`, para no duplicar un concepto que `FolioController` ya modela (`ESTADO_UTILIZADO`, chequeo de conflicto en `update`/`delete`). Ver Out of Scope. |
| No se puede vincular un folio que ya está `Utilizado` (ocupado por otra escritura) a una nueva escritura. | CU87 (AC: "folio ya vinculado (rechazo)") | New. |
| No se puede generar una copia de un testimonio que ya tiene un movimiento marcado `inscripta = true`. | CU87 (AC: "copia de un testimonio ya inscripto") | New. |

## Capabilities

### New Capabilities
- `folio-vinculacion-escritura`: vincular un folio a la escritura que lo
  ocupa, con la validación de folio ya vinculado y el cambio de estado a
  `Utilizado`.

### Modified Capabilities
- `openspec/specs/copia-emision` does not yet exist as a spec — this is the
  first spec-driven change touching `Copia`, so its validation rule is
  captured as a new capability, not a modification:

### New Capabilities (cont.)
- `copia-validacion-testimonio-inscripto`: rechazar el alta de una copia
  cuando el testimonio de origen ya tiene un movimiento inscripto.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `FolioController`, `Folio.setAtributos`/`getDto`, `CopiaController` |
| `frontend` | yes | `administracion/folios/page.tsx`, `escrituras/page.tsx`, `types/index.ts` |
| `frontend-swing` | no | — |
| `notaire-shared` | no | `DtoFolio.escritura` already exists; no DTO shape change |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Folio` (write `fkIdEscritura`, read it back into the DTO),
  no new entity.
- Endpoints: `POST /api/v1/folio`, `PUT /api/v1/folio/{id}` (add
  `escrituraId` to the request body), `POST /api/v1/copia` (add validation,
  no shape change).
- Database (Flyway `V{n}`): none — `folios.fk_id_escritura` already exists
  in the schema (`Folio.java` already maps it); no migration needed.
- Configuration / `.env`: none.
- Dependencies: none.
- BREAKING: `POST/PUT /api/v1/folio` gains a new optional field
  (`escrituraId`) and a new 409 rejection path when it points at a folio
  already `Utilizado` — additive, not breaking for existing clients that
  omit the field.

### Architecture review

Follows the existing architecture: business rule lives in the `api`
controller layer (`FolioController`, `CopiaController`), consistent with how
`FolioController` already validates `ESTADO_UTILIZADO` on update/delete and
`TipoDeDocumentoController` validates in-use state. No new pattern, no ADR
required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU87 – Vincular Escritura a Folio y Copia a Testimonio.md` | Confirm/update the flow description to match the implemented scope (folio↔escritura linking + copia/testimonio validation; cuaderno-level chain query deferred). |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | Note that a folio can now be linked to an escritura at creation/edit time. |
| `CHANGELOG.md` | `[Unreleased]` — vincular folio a escritura; validar copia de testimonio ya inscripto. |

## Out of Scope

- **Estado literal "Ocupado"**: the Issue's acceptance criteria mention
  `estado = "Ocupado"`; this change reuses the existing `"Utilizado"` value
  instead of introducing a parallel status meaning the same thing (folio not
  free to reassign). If the business later needs `Nuevo`/`Utilizado`/`Ocupado`
  as three distinct states, that is a separate, explicit follow-up.
- **Cuaderno → folio → escritura → copias/testimonios chain query**: the
  Issue's AC 4 requires viewing the full protocol chain starting from a
  `Cuaderno`. No `Cuaderno` entity exists in the codebase yet — it is CU80,
  tracked under Candidato 8 (issue #839, block RF-74 to RF-95). This change
  only wires the `Folio ↔ Escritura` and validates `Copia ↔ Testimonio`
  links; the cuaderno-rooted chain view is deferred to that change.
- **`Testimonio ↔ Escritura` linking**: already fully implemented
  (`Testimonio.fkIdEscritura` is `optional = false`, DTO-wired end to end via
  `TestimonioController`) — out of scope because there is nothing to fix.
- **Multiple folios per escritura in the UI**: `DtoEscritura.folios` is
  already a list at the DTO/entity level (`Escritura.folioList`), so the
  data model supports an escritura occupying several folios. This change
  wires the single-folio linking flow (link one folio to one escritura from
  the folio side); a UI to link several folios to the same escritura in one
  action is not built here — each folio is linked individually, which the
  data model already allows.
