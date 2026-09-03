# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Costos de documentos en el presupuesto** (issue #823, CU27/CU39): the
  cost (`importeAPagar`) of a `DocumentoPresentado` in a trámite is now
  included in its presupuesto's total. Adds `PlantillaCostoDocumento`
  (`POST`/`GET /api/v1/plantilla-costos-documento`) so a `TipoDeTramite`'s
  presupuesto template can define an expected fixed or variable
  (percentage) cost per `TipoDeDocumento` — exactly one of the two must be
  set. Adds a "Costos de Documentos" section to the plantillas de
  presupuesto admin screen.
- **Descuentos y recargos en ítems de presupuesto** (issue #822, CU45/CU71): a
  new `Item.tipo` field (`NORMAL`/`DESCUENTO`/`RECARGO`, default `NORMAL`)
  plus a required `motivo` when the type is `DESCUENTO` or `RECARGO`,
  validated both client- and server-side. `PagoService`'s total calculation
  now subtracts `DESCUENTO` items and adds `RECARGO` items. Adds
  `GET /api/v1/items/presupuesto/{idPresupuesto}/descuentos-recargos` to list
  only discount/surcharge items for a presupuesto, and a "Tipo"/"Motivo" UI
  on the ítems admin screen with a descuentos/recargos report section.
- **Estado de pago por presupuesto** (issue #821, CU15, CU47): adds
  `GET /api/v1/pagos/presupuesto/{idPresupuesto}/estado`, returning
  `SIN_PAGOS`, `PARCIAL`, or `SALDADO` derived from the existing saldo
  pendiente calculation. Additive change — existing pago/saldo endpoints are
  unchanged. The Pagos screen now shows an estado badge next to the saldo
  pendiente for the selected presupuesto.

- **Protocolo Auxiliar** (issue #839, CU81): marking a `TipoDeFolio` as
  `esAuxiliar` lets escribanos start a new `Escritura` directly on an available
  Protocolo Auxiliar folio (a folio of that type not yet linked to an
  `Escritura`), without opening a `CarpetaTramite`/`Tramite` — the ágil
  circuit of CU81. Numbering is `MAX(Escritura.numero) + 1` scoped to
  auxiliar folios only, independent from Protocolo Principal's sequence.
  Adds `GET /api/v1/protocolo-auxiliar/folios-disponibles` and
  `POST /api/v1/protocolo-auxiliar/escrituras`, a "Protocolo Auxiliar"
  checkbox on the existing Tipos de Folio admin form, and a new
  Protocolo Auxiliar screen under Protocolo.

- **Cuadernos de Folios** (issue #839, CU80): allows escribanos to group folios
  into cuadernos of exactly 10 strictly consecutive folios belonging to the same
  registro notarial, assigning a sequential number per year/escribano and marking
  the folios as `Asignado a cuaderno`. Damaged or annulled folios (`Errose`,
  `no pasó`) require an `observaciones` justification. Adds
  `GET/POST /api/v1/cuadernos` and `GET /api/v1/cuadernos/{id}/caratula` (PDF via
  JasperReports) with a new `Cuadernos` screen under Protocolo.

- **Persona duplicate-document validation** (issue #835, CU17, CU18): creating or
  editing a `Persona` with a `numeroIdentificacion` already registered to another
  person is rejected with `409 Conflict` (`PersonaDuplicadaException`), whose body
  now carries `idPersonaExistente` so the frontend can offer a direct link to the
  existing person. The personas page shows a toast with a "Ver persona existente"
  action and preserves the in-progress form data instead of discarding it.

- **Folio picker in Escritura form** (issue #892, CU06): Allows users to assign a folio
  to an escritura before signing. Unblocks E2E demo completion and enables full workflow
  validation (Escritura Firmada → Testimonio → Verificación → Inscripción → Retiro).
  Prerequisite for CU09–CU12. Backend already supported `Escritura.folios[]` field;
  this change wires the UI selector (`select-folio-escritura`) and submission payload.

- **Workflow engine y bitácora conectados al flujo real de gestión** (issue #833,
  CU13, CU16, CU83): `POST /api/v1/gestiones/{id}/transicionar`
  (`GestionTransitionService`) valida cada cambio de estado de una gestión contra
  el `WorkflowDefinition`/`WorkflowNode`/`WorkflowTransition` de su tipo de trámite
  (CU83) y rechaza transiciones no permitidas por el grafo. `GestionArchiveDebtService.archivar`
  ahora delega esa misma validación de transición (destino "Archivada") antes de
  archivar. Cada alta, transición válida y archivado registra una entrada en la
  bitácora vía `GestionBitacoraService`, expuesta en `GET /api/v1/gestiones/{id}/historial`
  (CU13). La pantalla `/dashboard/gestiones` agrega las acciones "Cambiar estado"
  (selector limitado a los destinos válidos del workflow) y "Ver bitácora", y
  muestra el mensaje de rechazo cuando una transición no está permitida. New
  Playwright specs (`gestion-cambiar-estado.spec.ts`, `gestion-bitacora.spec.ts`)
  cover the golden path, invalid-transition and viewport edge cases.

- **Circuito legal posterior a la firma de escritura: testimonio, inscripción y retiro**
  (issue #832, CU06, CU07, CU08, CU11, CU12, CU44): `POST /api/v1/escrituras/{id}/firmar`
  transitions a "Sin Firmar" escritura with folio(s) assigned to "Firmada"
  (`EscrituraFirmaService`). `POST /api/v1/testimonios/{id}/generar` and
  `.../verificar` (`TestimonioGeneracionVerificacionService`, migration `V17`) generate
  a testimonio from a firmada escritura and record verification (observado/no
  observado + motivo); `GET /api/v1/reportes/testimonio/{id}/copia` issues the printed
  copy (JasperReports) only for verified testimonios. `MovimientoTestimonioService`
  adds the Registro de la Propiedad circuit: `ingresar-inscripcion`,
  `registrar-inscripcion`, `retirar` and `reingresar`, each validating the required
  preconditions (verificado, movimiento abierto, inscripto, retirado) and returning
  404 for a non-existent testimonio. New Playwright specs cover firma, generación/
  verificación de testimonio and the movimiento-de-inscripción circuit.

- **Pago ↔ presupuesto ↔ gestión financial summary exposed end-to-end** (issue #820,
  CU-47, CU-02, RF-21): `Pago.getPresupuesto()` is no longer `@JsonIgnore` — payment
  responses now go through `DtoPagoResponse` (via `PagoMapper`) and include the
  associated `idPresupuesto`. Added `GET /api/v1/presupuestos/{id}/resumen`
  (`PresupuestoResumenService`) returning the gestión número/encabezado, presupuesto
  total, saldo pendiente, and the full payment list for a presupuesto — 404 for an
  unknown id. Added `GET /api/v1/gestiones/{id}/resumen-financiero`
  (`GestionResumenFinancieroService`) aggregating total presupuestado, total cobrado
  and saldo across every presupuesto reachable through a gestión's trámites. The CU47
  "Ver resumen" dialog on `/dashboard/presupuestos` calls the presupuesto-scoped
  endpoint and shows total, saldo and the payment table without extra navigation
  (`usePresupuestoResumen` in `frontend/src/hooks/usePresupuestos.ts`).

- **Pending-debt verification when archiving a gestión** (issue #819, CU-16, RF-22, RF-37):
  archiving a gestión now aggregates the pending balance (`PagoService.calcularSaldoPendiente`)
  across all `presupuesto`s reachable through its `tramite`s, exposes it via
  `GET /api/v1/gestiones/{id}/saldo-pendiente`, and records whether debt was outstanding at
  archive time on the gestión (`gestiones_de_escrituras.deuda_pendiente_al_archivar`, migration
  `V15`) via `POST /api/v1/gestiones/{id}/archivar`. The gestión screen surfaces a non-blocking
  debt warning in the archive confirmation dialog. New `GestionArchiveDebtService` in
  `backend-api`; new archive action + `useSaldoPendiente`/`useArchivarGestion` hooks in
  `frontend/src/app/dashboard/gestiones/page.tsx`.

- **AUTH-001 HTTP/integration test gaps closed: rate limiting, wrong password, expired token**
  (issues #685, #686, #687): `backend-api/api-test/auth/` gained a chained rate-limit test
  (5 failed logins against a per-run-randomized username, then asserts the lockout response)
  and a tightened wrong-password test; `JwtAuthIntegrationTest` gained an expired-token test
  using a genuinely expired, validly-signed JWT generated via the real `JwtTokenService`. Along
  the way, corrected two of the issues' own assumptions against verified real behavior: the
  login endpoint never returns 401 for bad credentials or 423 for lockout — it returns
  `200 {valido:false}` and `429 {valido:false, message}` respectively (see
  `docs/05-api/ERROR-HANDLING-STRATEGY.md`), matching the already-passing
  `LoginRateLimitIntegrationTest`/`JwtAuthIntegrationTest`. No production code changed.

- **Status-aware login error messages** (issue #756): the login page previously showed the
  same generic "can't connect to server" toast for a 429 account lockout, a genuine network
  failure, and (separately) invalid credentials, even though the backend already returns a
  distinct status and `message` field for the lockout case. Added `ApiError` (status + body) to
  `frontend/src/lib/api-client.ts`; the login page now reads the 429 response's `message` and
  shows it instead of the generic error. `frontend-swing`'s `Login.java` has the identical
  (worse) problem — documented in place rather than fixed, since propagating the HTTP status
  through that Swing/`RestClient` call chain is a larger change than this issue scoped.

- **Case-insensitive username and JWT structure HTTP tests** (issues #692, #693): closed two
  gaps in the AUTH-001 HTTP/Bruno test coverage. `backend-api/api-test/usuarios/09-13-*.yml`
  verifies `POST /api/v1/usuarios/login` treats a username the same regardless of case
  (lowercase/uppercase/mixed), matching `UsuarioController`'s existing `equalsIgnoreCase`
  lookup. `14-16-*.yml` verifies the returned `token` is a well-formed
  `header.payload.signature` JWT whose decoded payload has `sub` (the username) and a future
  `exp` claim, matching `JwtTokenService.generateToken()`. No production code changed.

- **k6 load-test suite** (issue #594): no performance/load testing existed anywhere in the
  repository. Added `performance-test/k6/load-test.js`, covering the highest-traffic read
  endpoints (`gestiones`, `presupuestos`, `tramites`) with baseline thresholds (`p(95)<500ms`,
  error rate `<1%`), authenticating via the existing JWT login endpoint. Wired into a new
  scheduled (weekly, not per-PR) `.github/workflows/performance-test.yml` job so it doesn't
  gate every PR.

- **Security response headers on the Next.js frontend** (issue #562): `frontend/next.config.ts`
  had no `headers()` callback. Added `Content-Security-Policy`, `X-Frame-Options: DENY`,
  `X-Content-Type-Options: nosniff`, and `Strict-Transport-Security` to every route.

- **HistorialMapper unit tests** (issue #589): `service.mappers.HistorialMapper` had zero test
  coverage despite not being excluded from the JaCoCo gate. Added `HistorialMapperTest` covering
  the happy path and each nullable foreign key individually.

- **Login rate limiting / account lockout** (issue #560): `POST /api/v1/usuarios/login`
  now locks a username out for a configurable duration (`security.login.lockout-duration-ms`,
  default 15 minutes) after a configurable number of consecutive failed attempts
  (`security.login.max-attempts`, default 5), returning `429 Too Many Requests`. A
  successful login resets the counter. Implemented in the new
  `com.licensis.notaire.security.LoginAttemptService`.

- **Jakarta Bean Validation on request boundaries** (issue #561): `UsuarioController`'s
  `createUsuario`/`updateUsuario` now validate the request body (`@NotBlank nombre`, `tipo`),
  and `ReporteController`'s path/query parameters are validated (`@Positive` on ID-like
  parameters, `@NotBlank` on `nombreTipoTramite`, `@Min(1)/@Max(12)` on `mes`), returning a
  clean `400` instead of a generic `500` or silently accepting malformed input.
  `GlobalExceptionHandler` now handles `MethodArgumentNotValidException` (body validation) and
  `ConstraintViolationException` (`@RequestParam`/`@PathVariable` validation) consistently.
  Full rollout across the remaining controllers is tracked as a follow-up.

- **Mobile/tablet viewport coverage in Playwright E2E** (issue #610): `frontend/playwright.config.ts`
  previously only ran against `devices["Desktop Chrome"]`, with zero specs asserting layout at
  the 320px/768px/1024px breakpoints mandated by `.claude/rules/ui-ux-design.md`. Added a
  `mobile` project (`devices["iPhone SE"]`) and `tests/e2e/mobile-viewport.spec.ts`, which
  asserts no horizontal overflow on the login page at 320px and 768px and on the dashboard at
  320px after login.

### Fixed

- **`CheckboxField` click target excluded the gap between the input and its label**
  (issue #930, CU08): the shared `CheckboxField` pattern (`frontend/src/theme/form-patterns.tsx`)
  rendered a wrapping `<div>` with a separate `<input>` and `<label htmlFor>`, leaving the
  `theme.spacing[3]` gap between them with no click handler — clicks landing there (e.g. on the
  element's bounding-box center) did nothing. Caused
  `TS-0031-testimonio-generacion-verificacion-feature.spec.ts`'s "Observado" checkbox test to
  silently fail to toggle state. Fixed by wrapping the input and label text in a single native
  `<label>` element, so any click within the row toggles the checkbox.

- **Flaky E2E coverage for the pagos presupuesto picker/saldo pendiente display**
  (issue #796, CU15): `TS-0014-pagos-saldo-picker.spec.ts` located the saldo
  pendiente text via an ambiguous `getByRole("dialog").getByText(/saldo|pendiente/i)`,
  which could also match the picker's loading placeholder or a seeded persona
  surname containing "Saldo", causing intermittent strict-mode failures. Added a
  dedicated `data-testid="saldo-pendiente-amount"` to the saldo display in
  `pagos/page.tsx` and updated the spec to target it directly; also fixed two
  assertions that compared the raw input amount against the locale-formatted
  currency string, and aligned a `select-persona` option click with the
  `evaluate(el => el.click())` workaround already used elsewhere in the suite
  for that Radix dropdown.

- **`02-demo-two-full-cases.spec.ts` broke after the presupuesto picker landed**
  (issue #796): the demo script's Pago step still filled a `presupuesto id`
  text field that the picker (see above) removed. Updated it to drive
  `select-presupuesto-pago` and pick the option by client surname, same as
  `TS-0014-pagos-saldo-picker.spec.ts`.

- **Archiving a gestión with pending debt was incorrectly blocked (HTTP 400)**
  (issue #914, CU16): commit 52776cc9 (issue #169) changed `GestionArchiveDebtService.archivar`
  to reject the request outright when `saldoPendiente > 0`, contradicting CU16's documented
  behavior — archiving should succeed and persist `deudaPendienteAlArchivar=true`, warning the
  user without blocking. Broke `GestionArchiveIntegrationTest` deterministically. Reverted to
  the warn-not-block behavior; the "Verificar deuda pendiente" (RF-22) rule is now enforced
  purely as a pre-confirmation warning via `GET /gestiones/{id}/saldo-pendiente`.

- **Creating a `Pago` against the seeded test presupuesto returned 400 instead of 201**
  (issue #914): `backend-api/src/test/resources/data.sql`'s seed presupuesto had no
  `monto_inmueble`, so `PagoService.procesarPago`'s saldo-pendiente validation (issue #848)
  always computed a saldo of `$0.00`, rejecting any payment against it. Added a
  `monto_inmueble` value to the seed row large enough to cover the payments asserted in
  `BusinessWorkflowIntegrationTest` and `RemainingControllersIntegrationTest`.
- **`PersonaController` create/update leaked unhandled 500s on non-duplicate persistence errors**
  (issue #912): PR #905 (#835) removed the generic `catch (Exception e) -> 409` fallback from
  `createPersona`/`updatePersona`, leaving only the `PersonaDuplicadaException` branch. Restored
  the fallback so any other persistence failure surfaces as 409 instead of an unhandled 500.
- **Payments exceeding a presupuesto's saldo pendiente returned a generic 400 instead of a
  specific 409** (issue #848, CU15): `PagoService.procesarPago` already rejected overpayments
  but duplicated the saldo calculation inline and threw a plain `IllegalArgumentException`,
  which `PagoController` mapped to `400 Bad Request` like any other validation error —
  indistinguishable from malformed input. Refactored to reuse the existing
  `calcularSaldoPendiente` method and introduced `SaldoPendienteExcedidoException`, mapped to
  `409 Conflict` in both `POST /pagos` and `POST /pagos/params`. The `/dashboard/pagos` form
  now shows a specific "saldo excedido" message on 409 instead of the generic save-error toast.

- **`CheckboxField` double-toggled on every click, making checkboxes appear unresponsive**
  (issue #839): the shared `CheckboxField` component in `theme/form-patterns.tsx` had both
  a wrapper `onClick={() => onChange(!checked)}` and the native `<input onChange>` firing on
  the same click, so `onChange` ran twice and canceled itself out. Discovered while testing
  the new Cuadernos de Folios screen. Removed the redundant wrapper handler; the native input
  already handles clicks and label-triggered toggles. Fixes checkbox interaction across every
  page that uses `CheckboxField` (testimonios, documentos, personas, tramites,
  documentos-entidades-externas, cuadernos).

- **Gestión form's presupuesto picker showed only `Presupuesto #{id}`, no client identity**
  (issue #889, CU02): `/dashboard/gestiones`'s nueva-Gestión modal rendered each presupuesto
  option as a bare id, even though `Presupuesto.persona` has been returned by the API since
  #883 and is already typed on the frontend — a user had no way to tell which presupuesto
  belonged to the client they were working with. Discovered while driving the real UI
  end-to-end to seed a demo case. Fixed by rendering `fullName(p.persona)` and
  `formatCurrency(p.monto)` alongside the id when a client is associated, falling back to the
  id-only label otherwise. Frontend-only change, no API/schema change.

- **Presupuesto creation/edit from the UI silently dropped the client association** (issue
  #883, CU01): `PresupuestoController.create`/`.update` bound directly to the raw
  `Presupuesto` JPA entity, whose client relation field is `fkIdPersona`, while the
  frontend (and `DtoPresupuesto`) send/expect `persona`. Jackson silently dropped the
  unknown `persona` key on write, and `GET` responses returned `fkIdPersona` instead of
  `persona` on read — so every Presupuesto created or edited from `/dashboard/presupuestos`
  lost its client link, and the apellido search (CU60) couldn't find it either. Fixed by
  adding `@JsonProperty("persona")` to `Presupuesto.getFkIdPersona()`/`.setFkIdPersona()`,
  mirroring the existing `@JsonProperty("monto")` alias already used on the same class for
  `montoInmueble`. No entity/DTO/schema change.

- **BREAKING: `Inmueble.valuacionFiscal` type mismatch blocked all Inmueble creation** (issue
  #879, CU69): `Inmueble.valuacionFiscal` (and `DtoInmueble.valuacionFiscal`) was declared
  `String` while the Flyway-owned `inmuebles.valuacion_fiscal` column is `real`; Hibernate
  always bound it as VARCHAR, so every `POST /api/v1/inmueble` failed against the real
  Postgres schema with `ERROR: column "valuacion_fiscal" is of type real but expression is
  of type character varying`, regardless of value (H2-based tests didn't enforce this,
  hiding the bug). Changed both fields to `Float` (matching the existing
  `Presupuesto.montoInmueble` convention) and updated the Next.js Inmueble form
  (`/dashboard/inmuebles`) to send/parse a number instead of a string. `valuacionFiscal`
  is now a JSON number, not a string, in both the request and response body. Investigating
  this surfaced an unrelated, pre-existing NPE on `PUT /api/v1/inmueble/{id}`
  (`InmuebleJpaController.edit`, `tramiteList` null), tracked separately as issue #880.

- **Payment method (`metodoPago`) was collected by CU15 but never persisted** (issue #792,
  CU15): `PagoController.procesarPago` accepted `metodoPago` in the request body but
  `PagoService`/`Pago` had no field to store it, so the value was silently dropped. Added
  nullable `pagos.metodo_pago` column (`V16` migration), threaded `metodoPago` through
  `Pago` (entity, `getDto()`/`setAtributos`), `DtoPago`, and `PagoService.procesarPago`/
  `editarPago`, so it now round-trips on create, edit, and retrieval.

- **BREAKING: Contradictory Presupuesto↔Tramite cardinality resolved** (issue #798):
  `Presupuesto` and `Tramite` declared foreign keys to each other —
  `presupuestos.fk_id_tramite` (`Presupuesto.fkIdTramite`) and
  `tramites.fk_id_presupuesto` (`Tramite.fkIdPresupuesto`) — but only the latter was
  ever written by the live modern path (`GestionController.applyTramiteDependencies`,
  CU02); `Presupuesto.fkIdTramite` was set only by the deprecated `ControllerNegocio`
  god class and its `PresupuestoJpaController`/`TramiteJpaController` helpers. Removed
  `Presupuesto.fkIdTramite` (field, getter/setter, `DtoPresupuesto.tramite` and its
  accessors) and every legacy call site that wrote it, leaving the single, consistent
  relation: one Presupuesto has many Tramites, each Tramite belongs to at most one
  Presupuesto (`Presupuesto.tramiteList` / `Tramite.fkIdPresupuesto`). Flyway `V14`
  drops `presupuestos.fk_id_tramite`, refusing to run if any row still holds a
  non-null value (data-loss guard); paired `R14` rollback script restores the column.
  Consumers of `DtoPresupuesto`'s removed `tramite` field must migrate to reading a
  Tramite's own `fkIdPresupuesto` instead. Discovered mid-implementation that
  `frontend-swing` (~6 Swing screens) also read/wrote this field; per existing
  project-wide direction to deprecate that module rather than invest in it, it is now
  excluded from the root Maven reactor (`pom.xml`) and CI (`ci.yml`,
  `scripts/preflight.sh`) instead of migrated.

- **Dashboard sidebar overflowed horizontally below 768px** (issue #699): `AppSidebar` rendered
  as a fixed 288px-wide `<aside>` with no responsive behavior, leaving no room for content at
  mobile widths (e.g. 320px). It now collapses to a hamburger-triggered off-canvas drawer below
  the `md` (768px) breakpoint, with a backdrop that dismisses it; desktop behavior (static,
  always visible) is unchanged. Un-skips the `test.fixme()` this issue tracked in
  `tests/e2e/mobile-viewport.spec.ts` and adds two more covering the drawer open/close cycle
  and the desktop no-hamburger case.

- **Dead `X-Notaire-User` header removed from the frontend** (issue #678): the backend's
  `AuditoriaAspect` has attributed audit records from the verified JWT identity
  (`SecurityContextHolder`), not from any client-supplied header, since #555 — but
  `frontend/src/lib/api-client.ts` kept sending `X-Notaire-User` on every request anyway, with
  no effect. Removed the dead header and its `actingUser()` helper, and corrected `CLAUDE.md`/
  `infra/README.md`, which still described audit attribution as header-based.

- **postgres-exporter reused the app's own admin DB credentials** (issue #675): `infra/docker-compose.yml`'s
  `postgres-exporter` service connected with `POSTGRES_USER`/`POSTGRES_PASSWORD` — the same
  credentials as the application itself — so a compromised metrics exporter had full read/write
  access to every application table. Added Flyway migration `V12` creating a dedicated
  `notaire_exporter` role granted only `pg_monitor` (PostgreSQL's built-in read-only statistics
  role), wired through `POSTGRES_EXPORTER_USER`/`POSTGRES_EXPORTER_PASSWORD` in both
  `docker-compose.yml` and `infra/docker-compose.yml`, and extended `ProductionCredentialsGuard`
  to reject a default exporter password in production. Grafana anonymous auth (also part of this
  finding) was already disabled by #672. `sslmode=disable` on the exporter connection is
  unchanged — `notary-postgres` has no TLS configured, so flipping it now would break the
  connection outright; tracked separately by #684.

- **Prometheus ran as root with the host's docker.sock mounted** (issue #674): `infra/docker-compose.yml`
  gave the `prometheus` container `user: root` plus a read-write bind mount of
  `/var/run/docker.sock`, even though `prometheus.yml` only ever scrapes static targets (no
  `docker_sd_configs`) — a compromised container had a direct path to full host compromise for
  no operational benefit. Removed both; the image's built-in non-root user is sufficient.

- **Wildcard `Authorization` header in CORS config allowed credential theft** (issue #673):
  `SecurityAndCorsConfig` hardcoded `.allowedHeaders("*")`, silently ignoring the existing (but
  unwired) `cors.allowed-headers` property, so any origin could read the `Authorization` header
  back via a CORS preflight response. Wired the property through with an explicit default
  (`Content-Type,Authorization`), and added a startup guard that refuses to boot
  in production if `cors.allowed-headers` or `cors.allowed-origins` still resolve to `*`.

- **Dead `X-Notaire-User` left in the `cors.allowed-headers` default** (issue #731): #673's
  default (above) originally included `X-Notaire-User`, which #678 had already removed from the
  frontend (and #555 from the backend's audit attribution) — an unused allow-listed header that
  no client actually sends. Dropped it from both `SecurityAndCorsConfig`'s `@Value` default and
  `application.properties`; no functional change.

- **Swagger/OpenAPI publicly accessible in production** (issue #671): `SecurityAndCorsConfig`
  now denies `/swagger-ui/**`, `/swagger-ui.html`, and `/v3/api-docs/**` when
  `app.environment=production` (the same signal `ProductionCredentialsGuard` already uses),
  while leaving them reachable in dev/test. Previously any anonymous visitor could enumerate
  the entire API surface and execute live requests via Swagger's "Try it out" in production.

- **Default credentials committed to version control** (issue #672): `infra/grafana/grafana.ini`
  no longer hardcodes `admin_user`/`admin_password` in plaintext — Grafana now gets its admin
  credentials exclusively from `GF_SECURITY_ADMIN_USER`/`GF_SECURITY_ADMIN_PASSWORD`
  (already wired to `GRAFANA_ADMIN_USER`/`GRAFANA_ADMIN_PASSWORD` in `.env`).
  `ProductionCredentialsGuard` now also rejects default pgAdmin and Grafana credentials in
  production, not just backend/DB/actuator/app-admin. `.env.example` defaults are explicitly
  marked as insecure placeholders that must change before a production run.

- **Order-dependent H2 integration test failures** (issue #661): `RepositoryIntegrationTest`
  (base for `GestionDeEscrituraRepositoryIntegrationTest`, `PagoRepositoryIntegrationTest`,
  `RegistroAuditoriaRepositoryIntegrationTest`) and `EstadoDeGestionReferentialIntegrityTest`
  were the only integration test bases without `@Transactional`, so their inserts committed
  permanently to the shared H2 instance instead of rolling back per test. Under
  `runOrder=alphabetical`, this broke `WorkflowTransitionIntegrationTest` and
  `WorkflowTraceApiH2IntegrationTest` when they ran later. Both classes now get
  `@Transactional`, matching every other integration test base in the suite.

- **Contradicting JaCoCo coverage-floor numbers** (issue #588): `CLAUDE.md`/`code-quality.md`
  said the enforced floor was 28%/14%, `pom.xml`'s own comment said ~78%/~62%, and the actually
  enforced `<minimum>` values were 70%/25% — three different numbers for the same gate. Also
  fixed a dead exclusion path referencing the nonexistent `com/licensis/notaire/servicios/*`
  package (real package is `service`), which meant `AdministradorJpa`/`AdministradorReportes`/
  `AdministradorSesion`/`AdministradorValidaciones`/`Conexion` were silently counted in the
  coverage gate instead of excluded as intended. Re-measured real coverage after the fix:
  ~84% line / ~74% branch. Added `JacocoCoverageConfigConsistencyTest` to guard against this
  drifting out of sync again.

- **Login attempt double-counting** (found while implementing #560): `UsuarioController.login()`
  fell through to its "usuario no encontrado" branch even when a username **was** matched but
  the password was wrong or the account was inactive, executing that branch's logic in addition
  to the matched-user branch. Fixed by returning immediately once a matching user has been
  handled.

- **E2E coverage reports were a fabricated static template** (issue #587): the
  `.github/workflows/playwright-e2e.yml` "Business Coverage Report" job wrote an identical
  hardcoded markdown block to `docs/wiki/cicd-reports/e2e-coverage-*.md` on every run,
  including a stale action item ("Fix backend 500 errors on testimonio endpoints") that had
  sat unchanged for 6+ weeks regardless of actual results. Replaced with
  `scripts/generate_e2e_coverage_report.py`, which parses the real Playwright JSON reporter
  output (`test-results/results.json`) and Bruno CLI JSON output (`bruno-results.json`) to
  report actual pass/fail/skip counts and the actual failing test titles, with no fabricated
  numbers when a results file is missing.

- **`generate_e2e_coverage_report.py` crashed on the real Bruno CLI output** (issue #658,
  found on #587's own first real CI run): `_bruno_section()` assumed `bruno-results.json` is a
  JSON object with a top-level `"summary"` key; the real `@usebruno/cli` output is a JSON array
  of per-iteration objects, each with its own `"summary"`. Verified the fix against the actual
  artifact from the failing job rather than guessing the schema a second time, and added a
  regression test using that real (trimmed) sample. Also found and fixed the reason Playwright
  results always showed "not found": `playwright-e2e.yml`'s "Run Playwright E2E tests" step
  passed `--reporter=html,json,junit` on the CLI, which fully overrides
  `playwright.config.ts`'s own `reporter` array — silently dropping the `outputFile` paths this
  script depends on, and the custom `tests/e2e/reporters/coverage-report.ts` business-coverage
  reporter entirely. Removed the CLI override so the config's reporters (which already have the
  correct paths) take effect. Bruno failures are now also surfaced in the report's Action Items
  section, not just Playwright ones — the discovery run had 137 failing Bruno tests silently
  reported as "no action items" before this fix.

- **Flyway single source of truth**: Removed dual schema source (init-db + Flyway)
  - Removed `init-db:/docker-entrypoint-initdb.d` volume mount from docker-compose.yml
  - PostgreSQL now starts empty; Flyway applies all V1→V11 migrations on startup
  - Created V11 migration to fix `conceptos.version` for "Documentacion" (id=3)
  - Rewrote `BaseIntegrationTest.java` to enable Flyway instead of copying init-db scripts
  - Renamed `InitDbSchemaValidationIntegrationTest` → `FlywaySchemaValidationIntegrationTest`
  - Archived `init-db/` directory to `docs/archive/init-db/`
  - Updated all documentation, agent configs, and `.claude/rules/database-migrations.md`
  - See `.claude/rules/database-migrations.md` for new migration workflow

<!-- markdownlint-disable-next-line MD024 -- pre-existing second "Added" block within [Unreleased] -->
### Added

- **ADR-007**: Database Schema Versioning with Flyway
  - Added architecture decision record for Flyway implementation
  - Documented migration strategy and best practices

- **SAR-007**: Flyway Implementation Solution Architecture Report
  - Detailed technical analysis and implementation plan
  - Testing strategy for database migrations
  - AI Agent Guidelines section

- **Flyway Skill for AI Agents**: `.claude/skills/flyway/SKILL.md`
  - Comprehensive guide for implementing Flyway migrations
  - Examples, best practices, and common patterns
  - Project-specific conventions and templates

- **Database Migrations Rules**: `.claude/rules/database-migrations.md`
  - Mandatory rules for all database changes
  - Anti-patterns to avoid
  - Rollback strategies and emergency procedures

- **Database Migrations README**: `backend-api/src/main/resources/db/migration/README.md`
  - Quick reference for developers
  - Common patterns and templates
  - Testing and validation commands

### Changed

- **Flyway Integration**: Migrated from init-db scripts to Flyway versioned migrations
  - Scripts moved to `backend-api/src/main/resources/db/migration/`
  - V1: Initial schema (24 tables)
  - V2: Initial reference data and admin user

### Deprecated

- `init-db/01-schema.sql` - Superseded by Flyway migration
- `init-db/02-data.sql` - Superseded by Flyway migration

<!-- markdownlint-disable-next-line MD024 -- pre-existing second "Fixed" block within [Unreleased] -->
### Fixed

- Updated Docker Compose to remove init-db volume mounts
- Configured Spring Boot to use Flyway with `spring.flyway.*` properties

## [1.0.0-SNAPSHOT] - 2026-04-14

### Added

- Initial release of Notaire microservices refactoring
- Spring Boot 4.0.4 backend API
- PostgreSQL 16 database
- REST API endpoints for all domain entities
- Swagger/OpenAPI documentation
- Unit tests with 80%+ coverage requirement
- Integration tests with Testcontainers
- Docker Compose setup for local development
- CI/CD pipeline with GitHub Actions

### Features

- **Authentication**: Login endpoint with MD5 password hashing
- **Gestiones**: CRUD operations for legal procedures
- **Escrituras**: Document management
- **Presupuestos**: Budget management
- **Personas**: Person/entity management
- **Reporting**: JasperReports integration

### Technical Stack

- Java 21
- Spring Boot 4.0.4
- Spring Data JPA
- PostgreSQL 16
- Flyway (new)
- Testcontainers
- Maven
- Docker
