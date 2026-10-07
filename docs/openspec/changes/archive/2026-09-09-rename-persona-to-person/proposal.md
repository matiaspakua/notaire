# Proposal: Rename Persona entity to Person

| Field | Value |
|---|---|
| GitHub Issue | #974 |
| Epic | #973 — Translate domain model to English |
| Use Case | N/A — technical refactor, no business-behavior change |

## Objetivo

Traducir al inglés la entidad de dominio `Persona` (JPA entity, tabla/columnas de
base de datos, DTO, repository, controller, contrato REST) y sus referencias en
frontend y tests, como primer slice vertical de la épica #973. Sin cambio de
comportamiento observable para el usuario final.

## What Changes

- Nueva migración Flyway que renombra la tabla `persona` y sus columnas a inglés
  (nunca se edita una migración existente).
- `Persona` → `Person`, `DtoPersona` → `DtoPerson`, `PersonaRepository` →
  `PersonRepository`, `PersonaController` → `PersonController`.
- Campos: `idPersona`→`id`, `nombre`→`firstName`, `apellido`→`lastName`,
  `numeroIdentificacion`→`identificationNumber`, `fechaNacimiento`→`birthDate`,
  `nacionalidad`→`nationality`, `estadoCivil`→`maritalStatus`,
  `numeroNupcias`→`marriageCount`, `ocupacion`→`occupation`,
  `domicilio`→`address`, `telefono`→`phone`, `eMail`→`email`,
  `esCliente`→`isClient`, `registroEscribano`→`notaryRegistrationNumber`,
  `sexo`→`sex`, `cuit`→`taxId`.
- Entidades dependientes (`TramitesPersonas`, `Usuario`, `GestionDeEscritura`,
  `Folio`, `Suplencia`, `Copia`, `Testimonio`) actualizan sus referencias
  `Persona`/`persona` a `Person`/`person`.
- REST contract: `/api/v1/personas` → `/api/v1/personas` se mantiene solo si
  rompería contratos externos sin versión; en este caso se traduce a
  `/api/v1/people`, con JSON fields traducidos.
- Frontend: tipo `Persona` → `Person`, componentes/páginas/i18n keys
  correspondientes.
- Tests backend (unit + integration) y E2E Playwright renombrados/traducidos.

## Reglas de negocio

Ninguna regla de negocio cambia. Es un renombre estructural: mismos datos,
mismas validaciones, mismos flujos, solo cambian nombres de tabla/columna/
clase/campo/endpoint/i18n-key.

## Capabilities

- `person-domain-rename` (renombre estructural, sin nuevo comportamiento)

## Impact Analysis

### Módulos afectados

- `backend-api` — `negocio.Persona`, `dto.DtoPersona`, `repository.PersonaRepository`,
  `api.PersonaController`, y toda entidad con FK a `Persona`.
- `backend-api` — nueva migración Flyway `V{n}__rename_persona_to_person.sql`.
- `frontend` — `src/types/index.ts` (`Persona`), páginas/componentes bajo
  `dashboard/personas`, i18n keys en `messages/en.json` y `messages/es.json`.
- Tests unit/integration bajo `backend-api/src/test`, E2E bajo `frontend/tests/e2e`.

## Documentation Impact

- `CHANGELOG.md` — entrada `[Unreleased]` documentando el rename.
- `docs/` NO se traduce (permanece en español, fuera de alcance de la épica #973).
- Nombres de Casos de Uso en `docs/` se mantienen en español; solo se
  actualiza, si aplica, alguna referencia técnica a nombres de campos/endpoints
  si el documento los cita explícitamente.
