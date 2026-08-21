> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Escritura`, `Testimonio` and `MovimientoTestimonio` are already persisted
(`negocio/Escritura.java`, `Testimonio.java`, `MovimientoTestimonio.java`)
with every field this circuit needs (`estado`, `fechaIngreso`, `fechaSalida`,
`fechaInscripcion`, `inscripta`, `numeroCarton`). They are reachable only
through generic CRUD endpoints (`EscrituraController`, `TestimonioController`,
`MovimientoTestimonioController`), so any client can set any field to any
value, including `estado`, without going through the rules in proposal.md.
`ReporteController` already generates PDFs with JasperReports from a
`.jrxml` template plus a data source built from an entity. See
`proposal.md` — Objetivo for the business motivation.

## Goals / Non-Goals

**Goals:**
- Add business-action endpoints (sign, generate, verify, move) that enforce
  the state/precondition rules in proposal.md on top of the existing
  entities and repositories.
- Add the frontend action ("Firmar") and the two new screens (testimonios,
  movimientos de testimonio) needed to reach those endpoints, per the
  UI-endpoint traceability rule in `.claude/rules/ai-agent-workflow.md`.
- Add a PDF copy-emission endpoint for a verified testimonio, reusing the
  `ReporteController` pattern.

**Non-Goals:**
- Restricting or removing the existing generic CRUD endpoints on
  `Escritura`, `Testimonio`, `MovimientoTestimonio` — out of scope for this
  change (see proposal.md — BREAKING CHANGES).
- Linking an escritura to the specific folio it occupies, or a copy to its
  originating testimonio (issue #838 / CU87).
- Any change to the workflow/transition engine (CU83, issue #833).

## Decisions

- **New `service` classes per capability (`EscrituraFirmaService`,
  `TestimonioGeneracionVerificacionService`,
  `MovimientoTestimonioService`) instead of adding methods to the existing
  JPA controllers.** The `jpa` package is legacy and being superseded by
  `repository` + `service` (CLAUDE.md — Backend Architecture); putting new
  business rules there would extend a pattern the project is migrating
  away from. Alternative considered: add validation methods directly to
  `EscrituraJpaController`/`TestimonioJpaController` — rejected because it
  mixes new business logic into legacy data-access classes and increases
  their surface instead of shrinking it.
- **Action endpoints as `POST /api/v1/<resource>/{id}/<action>` instead of
  overloading the generic `PUT /{id}`.** A dedicated action endpoint can
  enforce its own preconditions and reject invalid combinations explicitly,
  where a generic PUT has no way to distinguish "the client wants to sign"
  from "the client wants to correct a typo." Alternative considered:
  keep a single PUT and validate transitions there — rejected because it
  would still allow every other field to change silently in the same call,
  and does not match CU06/CU07/CU08/CU11/CU12/CU44's separate action
  semantics.
- **Reuse the existing `ReporteController` JasperReports pattern for the
  copy PDF instead of introducing a new reporting library.** Consistent
  with the project's established reporting stack; no new dependency needed
  (proposal.md — Dependencies: none new).

## Riesgos / Trade-offs

- [Los endpoints CRUD genéricos siguen permitiendo poner `estado` en
  cualquier valor, evitando las reglas nuevas] → Mitigación: fuera de
  alcance de este cambio (ver Non-Goals); se documenta como limitación
  conocida y queda para un cambio posterior que restrinja o audite el PUT
  genérico.
- [Concurrencia: dos firmas simultáneas sobre la misma escritura podrían
  ambas leer estado "Sin Firmar" antes de que la primera transacción
  confirme] → Mitigación: la transición se ejecuta dentro de una
  transacción (`@Transactional`) que relee el estado antes de escribir;
  la segunda solicitud falla la validación de precondición al confirmar.
- [El circuito de movimientos (ingreso/inscripción/retiro/reingreso) asume
  que el movimiento "más reciente" identifica el estado actual del
  testimonio; un dato corrupto o cargado fuera de orden por el PUT
  genérico podría dejarlo en un estado inconsistente] → Mitigación: cada
  acción valida explícitamente la precondición sobre el movimiento más
  reciente y rechaza si no se cumple, en lugar de asumir consistencia.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Firma exitosa de una escritura lista | unit | `EscrituraFirmaServiceTest#shouldSignEscrituraWhenUnsignedWithFolio` |
| Rechazo por escritura ya firmada | unit | `EscrituraFirmaServiceTest#shouldRejectSignWhenAlreadySigned` |
| Rechazo por falta de folio asignado | unit | `EscrituraFirmaServiceTest#shouldRejectSignWhenNoFolioAssigned` |
| Generación exitosa desde escritura firmada | unit | `TestimonioGeneracionServiceTest#shouldGenerateTestimonioFromSignedEscritura` |
| Rechazo de generación desde escritura no firmada | unit | `TestimonioGeneracionServiceTest#shouldRejectGenerationWhenEscrituraNotSigned` |
| Verificación sin observaciones | unit | `TestimonioVerificacionServiceTest#shouldVerifyWithoutObservations` |
| Verificación con observaciones | unit | `TestimonioVerificacionServiceTest#shouldVerifyWithObservations` |
| Rechazo de verificación de testimonio inexistente | unit | `TestimonioVerificacionServiceTest#shouldRejectVerificationWhenTestimonioNotFound` |
| Emisión de copia PDF de testimonio verificado | integration | `TestimonioCopiaReportIntegrationTest#shouldGenerateCopiaPdfForVerifiedTestimonio` |
| Rechazo de emisión de copia de testimonio no verificado | integration | `TestimonioCopiaReportIntegrationTest#shouldRejectCopiaWhenNotVerified` |
| Ingreso exitoso registra fecha de ingreso | unit | `MovimientoTestimonioServiceTest#shouldRegisterIngresoInscripcion` |
| Rechazo de ingreso de testimonio ya ingresado sin retirar | unit | `MovimientoTestimonioServiceTest#shouldRejectIngresoWhenAlreadyOpen` |
| Registro exitoso marca inscripto con fecha | unit | `MovimientoTestimonioServiceTest#shouldRegisterInscripcion` |
| Rechazo de registrar inscripción sin ingreso previo | unit | `MovimientoTestimonioServiceTest#shouldRejectInscripcionWithoutIngreso` |
| Retiro exitoso registra fecha de salida y número de cartón | unit | `MovimientoTestimonioServiceTest#shouldRegisterRetiro` |
| Rechazo de retiro de testimonio no inscripto | unit | `MovimientoTestimonioServiceTest#shouldRejectRetiroWhenNotInscripto` |
| Reingreso exitoso crea nuevo movimiento preservando historial | unit | `MovimientoTestimonioServiceTest#shouldCreateNewMovementOnReingreso` |
| Rechazo de reingreso de testimonio no retirado previamente | unit | `MovimientoTestimonioServiceTest#shouldRejectReingresoWhenNotWithdrawn` |

- New unit tests (`src/test/java/.../unit/`): `EscrituraFirmaServiceTest`,
  `TestimonioGeneracionServiceTest`, `TestimonioVerificacionServiceTest`,
  `MovimientoTestimonioServiceTest` (Mockito, repository mocked).
- New integration tests (`src/test/java/.../integration/`):
  `EscrituraFirmaControllerIntegrationTest`,
  `TestimonioAccionesControllerIntegrationTest`,
  `MovimientoTestimonioControllerIntegrationTest`,
  `TestimonioCopiaReportIntegrationTest` (PostgreSQL-backed, follow existing
  `ApiIntegrationTest` pattern).
- Coverage impact: additive-only new service/controller code with unit +
  integration coverage on every branch keeps the change at or above the
  JaCoCo ratchet floor (`mvn jacoco:check -pl backend-api`); no existing
  code loses coverage.

## Regression Strategy

- Existing tests affected: none require behavior changes — the generic
  CRUD endpoints and their existing tests (`EscrituraJpaController`,
  `TestimonioJpaController`, `MovimientoTestimonioJpaController` test
  suites) are untouched, since this change only adds new action endpoints
  and services alongside them.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`
- Legacy paths at risk (e.g. `jpa` package, `frontend-swing`): none — new
  code lives in `service`/`api`, `frontend-swing` no longer exists
  (CLAUDE.md — Project Overview).

## Playwright Strategy

- Specs to add/update under `frontend/tests/e2e/`:
  `escritura-firma.spec.ts`, `testimonio-generacion-verificacion.spec.ts`,
  `testimonio-movimiento-inscripcion.spec.ts`.
- Golden path covered: firmar una escritura con folio asignado → generar su
  testimonio → verificarlo sin observaciones → emitir copia → ingresar a
  inscripción → registrar inscripción → retirar.
- Edge / error paths covered: firmar sin folio o ya firmada (botón
  deshabilitado o error visible); verificar con observaciones; emitir
  copia antes de verificar (bloqueado); reingresar sin retiro previo
  (bloqueado).
- Viewports: 320px (mobile) / 768px (tablet) / 1024px (desktop)
- Command: `cd frontend && npx playwright test`

## Deployment Strategy

- Flyway migration required: no — `Escritura`, `Testimonio` y
  `MovimientoTestimonio` ya tienen todos los campos necesarios (proposal.md
  — Database).
- Deployment order / coupling: backend y frontend se despliegan juntos
  (los nuevos endpoints son consumidos únicamente por las pantallas
  nuevas); no requiere orden especial ni ventana de compatibilidad.
- Configuration or `.env` keys to add: none.
- Feature flag: no — el alcance es aditivo (nuevos endpoints/pantallas) y
  no reemplaza ningún flujo existente.
- Smoke test after deploy (Gate 5): firmar una escritura de prueba en el
  ambiente desplegado, generar y verificar su testimonio, y emitir la
  copia PDF; confirmar que `GET /actuator/health` sigue en verde.

## Rollback Strategy

- Revert safe: yes — el cambio es aditivo (nuevos endpoints/servicios/
  pantallas); revertir el commit no afecta los endpoints CRUD existentes
  ni el esquema.
- Database rollback: none needed — no hay migración Flyway asociada.
- Data written under the new behavior after revert: los registros de
  `Escritura`/`Testimonio`/`MovimientoTestimonio` creados o actualizados a
  través de las acciones nuevas quedan tal cual en la base (mismos campos
  que ya existían); un revert deja de exponer las acciones pero no
  corrompe ni pierde esos datos.
- Blast radius if rollback is delayed: bajo — mientras el problema no sea
  de integridad de datos, el peor caso es que el circuito legal siga sin
  pantalla dedicada, que es la situación actual sin este cambio.

## Migration Plan

n/a — no requiere rollout escalonado más allá del despliegue conjunto de
backend y frontend descrito en Deployment Strategy.
