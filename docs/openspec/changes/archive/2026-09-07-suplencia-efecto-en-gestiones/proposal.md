# Dar efecto práctico a las suplencias de escribano

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #836 |
| Use Case | CU22 – Registrar Suplencia (#175); CU59 – Consultar Suplencias (#212); CU48 – Dar alta escribano (#201); CU51 – Modificar escribano (#204) |
| Branch | `feat/836_suplencia-efecto-en-gestiones` |
| Gate 1 status | pending |

## Objetivo

El sistema ya permite registrar una suplencia completa (`Suplencia`,
`SuplenciaController`, pantalla `frontend/src/app/dashboard/suplencias`), pero
nada la consulta: `GestionController.applyGestionFields` asigna el
`fkIdPersonaEscribano` solicitado directamente, sin revisar si ese escribano
tiene una `Suplencia` activa como `fkIdSuplantado` para la fecha de creación
(RF-89, "Asignar suplente a una gestión"). Registrar una suplencia hoy no
tiene ningún efecto sobre a quién se le asignan los casos nuevos mientras esa
suplencia está vigente (`openspec/explore.md`, hallazgo 6).

Además, dar de alta o modificar la credencial de escribano de una persona
(`Persona.registroEscribano`, ya existente en el modelo) no tiene lugar
dedicado en la pantalla de personas
(`frontend/src/app/dashboard/personas/page.tsx`) — el campo no aparece en
ningún formulario, aunque CU48 y CU51 lo describen explícitamente como
"ingresar/modificar el número de registro del escribano".

## What Changes

- Al crear o editar una gestión (`POST /api/v1/gestiones/complete-case`,
  `PUT /api/v1/gestiones/{id}/complete-case`), si el escribano solicitado
  tiene una `Suplencia` activa como `fkIdSuplantado` para la fecha de la
  gestión, el sistema SHALL asignar la gestión al `fkIdSuplente` de esa
  suplencia en su lugar, y SHALL dejar constancia del redireccionamiento en
  `observaciones` de la gestión (escribano solicitado, escribano asignado,
  suplencia aplicada).
- Si el escribano solicitado no tiene una suplencia activa para esa fecha, el
  comportamiento no cambia: se asigna tal como se solicitó.
- La pantalla de personas (`frontend/src/app/dashboard/personas/page.tsx`)
  SHALL incluir una sección para dar de alta o modificar el número de
  registro de escribano (`registroEscribano`) de una persona existente,
  según CU48 y CU51.

**BREAKING CHANGES:** Ninguno — `GestionDeEscritura`, `Persona` y `Suplencia`
se mantienen sin cambios de esquema; este cambio agrega una consulta de
negocio antes de asignar el escribano, y un campo de formulario ya soportado
por el modelo mo pero no expuesto en la UI.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Si el escribano solicitado tiene una suplencia activa (fecha de inicio ≤ fecha de la gestión ≤ fecha de fin) como suplantado, la gestión se asigna al suplente. | CU22, CU59, RF-89 | New |
| El redireccionamiento por suplencia se registra en las observaciones de la gestión. | RF-89 | New |
| El número de registro de escribano de una persona se puede dar de alta o modificar desde la pantalla de personas. | CU48, CU51, RF-88 | Made explicit |

## Capabilities

### New Capabilities
- `gestion-asignacion-suplencia`: al crear o editar una gestión, redirige la
  asignación de escribano al suplente cuando el escribano solicitado tiene
  una suplencia activa para esa fecha.
- `persona-credencial-escribano`: permite dar de alta o modificar el número
  de registro de escribano de una persona desde la pantalla de personas.

### Modified Capabilities
Ninguna — no existe una capability principal para `Gestion` ni `Persona` en
`openspec/specs/`; estas son las primeras especificaciones formales de estas
reglas de negocio.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevo `GestionSuplenciaService` (consulta `SuplenciaRepository` por suplantado + fecha), invocado desde `GestionController.applyGestionFields`; nuevo método de repositorio en `SuplenciaRepository`; `PersonaController`/`PersonaService` ya soportan `registroEscribano` sin cambios (el campo ya existe en `Persona` y en `DtoPersona`). |
| `frontend` | yes | `frontend/src/app/dashboard/personas/page.tsx` agrega campo/sección "Registro de escribano"; `frontend/src/app/dashboard/gestiones/page.tsx` muestra un aviso cuando la gestión creada fue redirigida a un suplente. |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `GestionDeEscritura`, `Persona`, `Suplencia` (sin cambios de
  esquema).
- Endpoints (comportamiento cambiado, misma firma):
  `POST /api/v1/gestiones/complete-case` y
  `PUT /api/v1/gestiones/{id}/complete-case` ahora pueden asignar un
  escribano distinto al solicitado cuando hay una suplencia activa;
  `POST /api/v1/personas` y `PUT /api/v1/personas/{id}` sin cambios de
  contrato (ya aceptan `registroEscribano`, solo se expone en la UI).
- Database (Flyway `V{n}`): ninguna — todos los campos usados
  (`registroEscribano`, `fecha_inicio`/`fecha_fin` de `Suplencia`) ya existen.
- Configuration / `.env`: none.
- Dependencies: none new.

### Architecture review

Sigue el layering existente (`service` para la regla de negocio de
redirección, `api` para invocarla al crear/editar la gestión). No introduce
un patrón arquitectónico nuevo, no requiere un nuevo ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | Documentar el efecto de la suplencia sobre la asignación de nuevas gestiones. |
| `docs/100-business/102-use-cases/CU48 – Dar alta escribano.md` | Referenciar la pantalla de personas como lugar donde se realiza el paso 7 (ingresar el registro de escribano). |
| `docs/100-business/102-use-cases/CU51 – Modificar escribano.md` | Referenciar la pantalla de personas como lugar donde se realiza el paso 5 (modificar el registro de escribano). |
| `CHANGELOG.md` | Entry: las suplencias activas redirigen la asignación de nuevas gestiones; alta/edición de registro de escribano desde personas. |

## Out of Scope

- Reasignar automáticamente las gestiones ya existentes cuando se registra
  una nueva suplencia — este cambio solo afecta a gestiones creadas o
  editadas después de que la suplencia esté vigente.
- Cualquier flujo de notificación (email, push) al suplente cuando se le
  redirige una gestión — fuera de alcance de este hallazgo de negocio.
- El motor de estados / workflow de la gestión (`WorkflowValidationService`,
  ya cubierto por el change `gestion-workflow-y-bitacora`) — este cambio solo
  afecta a quién queda como `fkIdPersonaEscribano`, no al estado de la
  gestión.
