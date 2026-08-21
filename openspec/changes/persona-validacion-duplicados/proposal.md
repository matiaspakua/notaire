# Validar persona duplicada al dar de alta

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #835 |
| Use Case | CU17 – Dar Alta persona (#170); CU18 – Dar Alta Cliente (#171) |
| Branch | `feat/835_persona-validacion-duplicados` |
| Gate 1 status | pending |

## Objetivo

`Persona` identifica a una persona por su `TipoIdentificacion` y
`numeroIdentificacion`, y el repositorio ya expone
`findByNumeroIdentificacion` (`PersonaRepository`), usado hoy solo para
búsqueda (`PersonaService.buscar`). Pero `PersonaService.save()` —
invocado por `POST /api/v1/personas` (CU17/CU18) — persiste cualquier
`Persona` sin comprobar si ya existe una con el mismo tipo y número de
documento; el formulario de alta
(`frontend/src/app/dashboard/personas/page.tsx`) tampoco hace ninguna
verificación antes de enviar. El resultado: se puede cargar dos veces a
la misma persona física, y presupuestos, gestiones y pagos quedan
repartidos entre dos fichas sin vínculo entre sí, sin ningún aviso
(`openspec/explore.md`, hallazgo 5).

## What Changes

- `POST /api/v1/personas` (alta) SHALL rechazar la creación cuando ya
  existe una `Persona` con el mismo `TipoIdentificacion` y
  `numeroIdentificacion`, respondiendo con un error que identifica a la
  persona existente en lugar de crear un duplicado silencioso.
- `PUT /api/v1/personas/{id}` (edición) SHALL aplicar la misma validación
  al cambiar el tipo o número de documento de una persona existente,
  excluyendo a la propia persona editada de la comprobación.
- El formulario de alta/edición de persona muestra el error de forma
  clara y ofrece navegar a la ficha de la persona existente en lugar de
  perder los datos ya cargados en el formulario.

**BREAKING CHANGES:** Ninguno — `Persona` y `PersonaRepository` se
mantienen sin cambios de esquema; este cambio agrega una validación de
negocio antes de guardar, que hoy no existe.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No pueden coexistir dos personas con el mismo tipo y número de identificación. | CU17, CU18 | New |
| La validación de duplicado en edición excluye a la propia persona (permite guardar sin cambiar su documento). | CU17, CU18 | New |
| Un documento de identidad es válido para ambas categorías (persona y cliente): `esCliente` no forma parte de la clave de duplicado. | CU17, CU18 | Made explicit |

## Capabilities

### New Capabilities
- `persona-validacion-duplicados`: valida, al crear o editar una persona,
  que no exista ya otra persona con el mismo tipo y número de
  identificación.

### Modified Capabilities
Ninguna — no existe una capability principal para `Persona` en
`openspec/specs/`; esta es la primera especificación formal de una regla
de negocio sobre su alta/edición.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `PersonaService.save()` valida duplicados antes de persistir (usando `PersonaRepository.findByNumeroIdentificacion` ya existente, filtrando por tipo de identificación); `PersonaController` traduce el rechazo a `409 Conflict` con el ID de la persona existente. |
| `frontend` | yes | `frontend/src/app/dashboard/personas/page.tsx` muestra el error de duplicado con un enlace a la ficha existente. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Persona` (sin cambios de esquema).
- Endpoints (comportamiento cambiado, misma firma):
  `POST /api/v1/personas` y `PUT /api/v1/personas/{id}` ahora pueden
  responder `409 Conflict` cuando detectan un duplicado.
- Database (Flyway `V{n}`): ninguna — `numero_identificacion` y
  `fk_id_tipo_identificacion` ya existen; la validación se hace a nivel
  de aplicación, no de constraint de base (permite un mensaje de error
  claro con el ID de la persona existente).
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Sigue el layering existente (`service` para la regla de negocio, `api`
para traducir el rechazo a un código HTTP). No introduce un patrón
arquitectónico nuevo, no requiere un nuevo ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | Documentar la validación de duplicado como parte del flujo de alta. |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | Referenciar la misma validación (comparte el alta de `Persona`). |
| `CHANGELOG.md` | Entry: el alta/edición de persona rechaza documentos duplicados. |

## Out of Scope

- Fusionar (merge) personas ya duplicadas en la base de datos hoy — este
  cambio previene nuevos duplicados, no corrige los existentes.
- Cualquier constraint de unicidad a nivel de base de datos (Flyway) — la
  validación es de aplicación para poder devolver un mensaje de error con
  contexto (ID de la persona existente) en lugar de un error de
  constraint genérico.
