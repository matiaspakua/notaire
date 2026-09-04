# Cargar vencimiento y responsable en tipos de documento

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #837 |
| Use Case | CU27 – Ingresar nuevo tipo de documento; CU32 – Modificar tipo de documento; CU42 – Informar próximos vencimientos |
| Branch | `feat/837_tipo-documento-vencimiento-config` |
| Gate 1 status | pending |

## Objetivo

El catálogo de tipos de documento (`TipoDeDocumento`) modela si un documento
vence, cuántos días dura y quién es responsable de entregarlo/devolverlo, pero
la pantalla de administración solo permite cargar el nombre, y los documentos
presentados creados a partir de un tipo nunca heredan esos datos. Como
consecuencia, "informar próximos vencimientos" (CU42) es estructuralmente
imposible porque el dato del que depende nunca se carga en ningún documento
presentado real.

## What Changes

- La pantalla de administración de tipos de documento
  (`frontend/src/app/dashboard/administracion/documentos/page.tsx`) agrega
  los campos `vence` (checkbox), `diasVencimiento` (numérico, visible solo si
  `vence`) y `quienEntrega` (texto), además del `nombre` ya existente, para
  alta (CU27) y modificación (CU32).
- `DocumentoPresentadoController.toEntity()` deja de hardcodear
  `vence = false` y `quienEntrega = ""` en cada alta: al crear un documento
  presentado, copia `vence`, `diasVencimiento` y `quienEntrega` del
  `TipoDeDocumento` asociado (`request.tipoId()`), y calcula
  `fechaVencimiento` como `fechaIngreso + diasVencimiento` cuando `vence` es
  `true`.
- Ningún endpoint nuevo: se reutilizan `POST/PUT /api/v1/tipo-de-documento` y
  `POST/PUT /api/v1/documento-presentado`, que ya aceptan/exponen estos
  campos a nivel de DTO/entidad.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Al dar de alta o modificar un tipo de documento, el usuario debe poder indicar si vence, en cuántos días y quién es responsable de entregarlo | CU27, CU32 | Made explicit (ya modelado en el backend, ausente en la UI) |
| Al crear un documento presentado, `vence`, `diasVencimiento` y `quienEntrega` se heredan del tipo de documento seleccionado, y `fechaVencimiento` se calcula a partir de `fechaIngreso` cuando corresponde | CU42 (dato precondición) | New — hoy `DocumentoPresentadoController` fuerza `vence=false` sin excepción |

## Capabilities

### New Capabilities
- `tipo-documento-vencimiento-config`: alta/edición de `vence`,
  `diasVencimiento` y `quienEntrega` en la pantalla de administración de
  tipos de documento.
- `documento-presentado-herencia-vencimiento`: un documento presentado hereda
  `vence`/`diasVencimiento`/`quienEntrega` de su tipo de documento al crearse,
  y calcula `fechaVencimiento`.

### Modified Capabilities
(none — ambas son capabilities nuevas; no existe ninguna delta spec previa
bajo `openspec/specs/` para estas áreas)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `DocumentoPresentadoController.toEntity()` deja de hardcodear `vence`/`quienEntrega`; lee el `TipoDeDocumento` asociado para heredar esos campos y calcular `fechaVencimiento` |
| `frontend` | yes | `administracion/documentos/page.tsx` agrega campos `vence`, `diasVencimiento`, `quienEntrega` al formulario de alta/edición |
| `frontend-swing` | no | eliminado del repositorio (CLAUDE.md — Project Overview) |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `TipoDeDocumento` (sin cambios de esquema — campos ya existen);
  `DocumentoPresentado` (sin cambios de esquema — `vence`, `diasVencimiento`,
  `quienEntrega`, `fechaVencimiento` ya existen como columnas).
- Endpoints: `POST/PUT /api/v1/tipo-de-documento` (sin cambio de contrato —
  el DTO `DtoTipoDeDocumento` ya expone estos campos);
  `POST/PUT /api/v1/documento-presentado` (sin cambio de contrato externo —
  cambia únicamente el valor que el backend asigna internamente a partir del
  tipo de documento).
- Database (Flyway `V{n}`): ninguna — todas las columnas usadas ya existen.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva.
- BREAKING: no — ningún contrato de API cambia; solo se completa un valor
  que antes se hardcodeaba a `false`/`""`.

### Architecture review

Sigue el layering existente: `frontend` usa `FormField`/`theme` tokens sobre
el patrón de formulario ya establecido en la misma pantalla; `backend-api`
mantiene la lógica dentro de `api.DocumentoPresentadoController`, que ya
inyecta `TipoDeDocumentoRepository` para el mapeo de respuesta (`toResponse`)
y ahora lo reutiliza también en `toEntity`. No introduce un patrón
arquitectónico nuevo — ADR: n/a.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU27 – Ingresar nuevo tipo de documento.md` | documentar los campos `vence`, `diasVencimiento`, `quienEntrega` en el flujo de alta |
| `docs/100-business/102-use-cases/CU32 – Modificar tipo de documento.md` | documentar los mismos campos en el flujo de edición |
| `docs/100-business/102-use-cases/CU42 – Informar próximos vencimientos.md` | anotar que esta change resuelve el bloqueo de datos (precondición), sin implementar el reporte/listado de CU42 en sí (ver Out of Scope) |
| `CHANGELOG.md` | entrada `[Unreleased]` — alta/edición de vencimiento y responsable en tipos de documento; herencia automática en documentos presentados |

## Out of Scope

- El reporte/listado "próximos vencimientos" de CU42 en sí (una pantalla o
  endpoint que liste todos los `documentos presentados` por vencer, con las
  columnas descritas en CU42) — esta change resuelve únicamente el bloqueo de
  datos que lo hace imposible hoy; construir esa pantalla/reporte queda para
  un issue de seguimiento posterior, ya que es una feature de alcance mayor
  (listado transversal, no una carga puntual de datos).
- Actualizar `DocumentoPresentado`s ya existentes con los valores heredados de
  su tipo de documento (backfill) — solo se aplica a los documentos
  presentados creados después de este cambio.
- Notificaciones (email, push) de vencimientos próximos — no está en el
  alcance de este hallazgo de negocio.
