# Circuito legal posterior a la firma de escritura

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #832 |
| Use Case | CU06 – Firmar escritura (#159); CU07 – Generar testimonio (#160); CU08 – Verificar Testimonio (#161); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio (#165); CU44 – Reingresar testimonio (#197) |
| Branch | `feat/832_escritura-post-firma-legal-cycle` |
| Gate 1 status | pending |

## Objetivo

El backend ya persiste `Escritura` (con estado `Sin Firmar`/`Firmada`/`Inscripta`/...),
`Testimonio` y `MovimientoTestimonio` (con `fechaIngreso`, `fechaSalida`,
`fechaInscripcion`, `inscripta`, `numeroCarton`), pero solo a través de
endpoints CRUD genéricos (`PUT /{id}` para cambiar cualquier campo, incluido
`estado`, sin ninguna regla). No existe ninguna pantalla ni acción de negocio
reconocible para firmar una escritura, generar o verificar un testimonio,
presentarlo/seguirlo en inscripción, retirarlo/reingresarlo, ni emitir su
copia impresa. Es la mitad del trámite con efectos legales frente al registro
de la propiedad, y hoy se resuelve fuera del sistema (`openspec/explore.md`,
hallazgo 2).

## What Changes

- Nueva acción de negocio "Firmar escritura" (CU06): valida que la escritura
  esté en estado `Sin Firmar` y que tenga folio(s) asignado(s) antes de
  pasarla a `Firmada`, en lugar de permitir el cambio de estado vía PUT
  genérico sin reglas.
- Nueva pantalla y endpoint para generar el testimonio de una escritura
  firmada (CU07) y para verificarlo — marcarlo observado/no observado con
  observaciones (CU08).
- Nueva pantalla y endpoints para el circuito de movimientos del testimonio:
  presentar/ingresar para inscripción (CU11, `fechaIngreso`), registrar la
  inscripción (RF-32, `fechaInscripcion`/`inscripta`), retirar (CU12,
  `fechaSalida`) y reingresar (CU44, nuevo `MovimientoTestimonio`).
- Nuevo endpoint de emisión de copia impresa del testimonio (RF-94),
  reutilizando el patrón JasperReports existente (`ReporteController`).
- La pantalla de escrituras (`frontend/src/app/dashboard/escrituras`) expone
  la acción "Firmar" en lugar de solo permitir editar el campo estado.

**BREAKING CHANGES:** Ninguno — los endpoints CRUD existentes de `Escritura`,
`Testimonio` y `MovimientoTestimonio` se mantienen; este cambio añade acciones
de negocio y pantallas nuevas sobre el mismo modelo de datos.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Una escritura solo puede firmarse si está en estado "Sin Firmar" y tiene folio(s) asignado(s). | CU06, RF-27 | Made explicit |
| Un testimonio solo puede generarse a partir de una escritura firmada. | CU07, RF-31 | Made explicit |
| Verificar un testimonio registra si quedó observado y, de ser así, el motivo. | CU08 | Made explicit |
| Presentar un testimonio a inscripción registra la fecha de ingreso al registro. | CU11, RF-30/RF-92 | Made explicit |
| Registrar la inscripción marca el testimonio como inscripto con su fecha. | RF-32 | Made explicit |
| Retirar un testimonio inscripto registra la fecha de salida y el número de cartón. | CU12, RF-33 | Made explicit |
| Reingresar un testimonio ya retirado genera un nuevo movimiento, sin perder el historial del anterior. | CU44, RF-33 | Made explicit |
| Se puede emitir una copia impresa (PDF) de un testimonio verificado. | RF-94 | New |

## Capabilities

### New Capabilities
- `escritura-firma`: acción de negocio para firmar una escritura, con las
  validaciones de estado y folio del CU06.
- `testimonio-generacion-verificacion`: generar el testimonio de una
  escritura firmada (CU07) y verificarlo (CU08), incluida la emisión de la
  copia impresa (RF-94).
- `testimonio-movimiento-inscripcion`: circuito de movimientos de un
  testimonio — ingresar para inscripción (CU11), registrar inscripción
  (RF-32), retirar (CU12) y reingresar (CU44).

### Modified Capabilities
_None — no existe spec previo para estos capabilities; el modelo de datos
(`Escritura`, `Testimonio`, `MovimientoTestimonio`) y sus repositorios no
cambian de forma, solo se les agrega una capa de acciones de negocio._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevos endpoints de acción (`POST /api/v1/escrituras/{id}/firmar`, `POST /api/v1/testimonios/{id}/generar`, `POST /api/v1/testimonios/{id}/verificar`, `POST /api/v1/movimientos-testimonio/{id}/ingresar-inscripcion`, `.../registrar-inscripcion`, `.../retirar`, `.../reingresar`, `GET /api/v1/reportes/testimonio/{id}/copia`); nuevos servicios de validación sobre `EscrituraService`, `TestimonioJpaController`/repository, `MovimientoTestimonioJpaController`/repository. |
| `frontend` | yes | Acción "Firmar" en la pantalla de escrituras; nuevas pantallas de testimonios y de movimientos de testimonio (ingreso/inscripción/retiro/reingreso); botón de copia impresa. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Escritura`, `Testimonio`, `MovimientoTestimonio` (sin cambios de
  esquema; se agregan servicios de negocio sobre las entidades existentes).
- Endpoints (nuevos): `POST /api/v1/escrituras/{id}/firmar`;
  `POST /api/v1/testimonios/{id}/generar`;
  `POST /api/v1/testimonios/{id}/verificar`;
  `POST /api/v1/movimientos-testimonio/{id}/ingresar-inscripcion`;
  `POST /api/v1/movimientos-testimonio/{id}/registrar-inscripcion`;
  `POST /api/v1/movimientos-testimonio/{id}/retirar`;
  `POST /api/v1/movimientos-testimonio/{id}/reingresar`;
  `GET /api/v1/reportes/testimonio/{id}/copia` (PDF, JasperReports).
- Database (Flyway `V{n}`): `V17__add_verificado_to_testimonios.sql` —
  agrega `testimonios.verificado` (boolean, aditivo). `Escritura` y
  `MovimientoTestimonio` ya tienen todos los demás campos que este circuito
  necesita; `Testimonio.observado` por sí solo no alcanza para distinguir
  "aún no verificado" de "verificado, no observado", que CU11 requiere
  para aceptar un testimonio a inscripción.
- Configuration / `.env`: none.
- Dependencies: none new (reutiliza JasperReports, ya usado por
  `ReporteController`).

### Architecture review

Sigue el layering existente (`service` para las reglas de negocio, `api`
para los controllers de acción, DTOs explícitos) sobre el modelo `negocio`/
`repository` ya persistido. No introduce un patrón arquitectónico nuevo, no
requiere ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU06 – Firmar escritura (Esta Junto a Preparar Escritura).md` | Confirmar que el flujo de firma descrito coincide con el endpoint de acción implementado. |
| `docs/100-business/102-use-cases/CU07 – Generar testimonio.md`, `CU08 – Verificar Testimonio.md`, `CU11 – Ingresar para inscripción.md`, `CU12 – Retirar testimonio.md`, `CU44 – Reingresar testimonio.md` | Anotar el endpoint/pantalla nuevo en Referencias Cruzadas si el documento lo registra. |
| `CHANGELOG.md` | Entry: se puede firmar una escritura, generar/verificar su testimonio, seguir su inscripción registral y emitir copia impresa desde el sistema. |

## Out of Scope

- Vincular la escritura al folio que ocupa y la copia al testimonio de
  origen — issue #838 (CU87), que este cambio no bloquea pero tampoco
  resuelve.
- El bloque de protocolo notarial sin desarrollo (cuadernos, carpetas de
  trámite, protocolo auxiliar, minuta de inscripción) — issue #839.
- Cambios al motor de definición de workflows de estados (CU83) — issue #833.
