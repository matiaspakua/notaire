---
title: Notaire — Functional / Business Rules Explore Report
date: 2026-08-11
mode: opsx:explore (thinking artifact, not a spec)
---

# Notaire — Functional / Business Rules Explore Report

> Companion to `openspec/explore_report.md` (technical/architecture pass). This pass
> deliberately ignores CI, security, and code-quality tooling and instead traces
> **business rules**: does the modeled domain (entities, relations, workflows) match
> what the UI actually lets a user do, and does the code enforce what the docs claim
> it enforces? Findings are grounded in code read on 2026-08-11 — cite file:line, not
> this report, when acting on them.

## 1. Method

For each business area: read the JPA entity relations (`negocio/`), the service/
controller that's supposed to enforce rules, and the actual Next.js page a user
touches. A finding only makes this report if there's a concrete gap between at least
two of those three layers — a rule the entity models but the UI never exercises, or a
UI that implies an enforced rule the backend never checks. This is not a full sweep of
all 68 CUs; it's a deep read of the financial (Presupuesto/Pago/Item) and case-lifecycle
(Gestión/Workflow/Testimonio) domains, plus a spot-check on Personas. §7 lists what
wasn't covered.

## 2. Shape of the domain vs. the UI

```
Modeled in negocio/ (backend)                    Reachable from a dashboard page
──────────────────────────────                   ────────────────────────────────
Persona ──< Presupuesto >── Tramite               ✅ presupuestos (flat monto only)
              │                                    ❌ no tramite picker
              ├──< Item (itemized breakdown)        ❌ no UI at all
              └──< Pago (payments)                  ✅ pagos (no saldo shown)

TipoDeTramite ──< PlantillaPresupuesto >── Concepto ✅ administracion/plantillas
              (never consumed by Presupuesto create)

GestionDeEscritura ──> EstadoDeGestion              ✅ gestiones (unrestricted dropdown)
WorkflowDefinition ──< WorkflowNode >── EstadoDeGestion  ✅ admin workflow builder
              ──< WorkflowTransition                (validates its own graph only —
                                                       never consulted on real transitions)

Persona ──< Suplencia (notary substitution)         ✅ suplencias (pure CRUD)
              (never checked during case assignment)

Testimonio ──< MovimientoTestimonio (registry        ❌ no UI at all
   ingreso/salida/inscripción)

Persona.numeroIdentificacion                         no uniqueness, DB or app level
```

The pattern that repeats across every finding below: **the domain model and the admin
CRUD layer are richer than the actual task-completion workflows.** Templates, items,
workflow graphs, and substitution records can all be *administered*, but the
day-to-day screens that create a budget, move a case forward, or assign a notary don't
*consume* them.

## 3. Financial domain: Presupuesto / Item / Pago / PlantillaPresupuesto

### 3.1 Over-payment is not just unvalidated — it's treated as expected behavior

- `PagoController.getSaldoPendiente` (`GET /api/v1/pagos/presupuesto/{id}/saldo`)
  exposes `pagoService.calcularSaldoPendiente`, but nothing in `PagoService` or
  `PagoController.create`/`createFromParams` calls it before persisting a new `Pago`.
  A payment for any amount, against any presupuesto, in any state, is accepted.
- `frontend/src/tests/unit/business-logic-hooks.test.ts:161-187` has a standalone,
  test-local `calcularSaldo(total, pagos)` helper (not imported from any real page)
  with a case literally named `"overpaid presupuesto has negative saldo"` asserting
  `calcularSaldo(200, [{monto:300}])` returns `-100` and treating that as correct.
  There is no corresponding test anywhere asserting a payment *should be rejected*
  when it would overpay. The one place the business rule is codified as a test
  encodes "allow it silently," not "prevent it."
- Net effect: nothing in the stack — not the entity, not the service, not the
  controller, not the tests — treats overpayment as an error state. If that's
  intentional (e.g., overpayments become credit), that intent isn't recorded
  anywhere; if it's not intentional, it's a live gap a notary staff member could hit
  by typo alone, since §3.3 shows there's no visibility into the running saldo while
  entering a payment.

### 3.2 `metodoPago` is collected and silently discarded

- `frontend/src/types/index.ts:194` declares `metodoPago?: string;` on the `Pago`
  type, and `frontend/src/app/dashboard/pagos/page.tsx` renders it as a real form
  field (with an i18n label `t("methods.efectivo")` etc.) that a user fills in when
  recording a payment.
- Grepping the Java entity (`negocio/Pago.java`), the DTO (`dto/DtoPago.java`), and
  every Flyway migration under `backend-api/src/main/resources/db/migration/` for
  `metodo_pago` / `metodoPago` returns nothing. There is no column, no field, no
  mapping.
- Result: a user selects "Efectivo" / "Transferencia" / etc. when recording a
  payment, the form submits it, and it vanishes — never persisted, never retrievable.
  This isn't a missing feature so much as a UI that actively misleads the person
  entering data into believing the payment method is recorded when it never was.

### 3.3 The payment form has no relation-awareness

- `frontend/src/app/dashboard/pagos/page.tsx` (116 lines, read in full): "Presupuesto
  ID" is a raw numeric `<Input>` — no dropdown, no search-by-persona, no existence
  check before submit. "Monto" is a numeric input with no `max`, no display of the
  presupuesto's current saldo pendiente, and no call to the saldo endpoint from §3.1
  anywhere in the component.
- A user recording a payment has no way to see, from that screen, how much is
  actually owed. Combined with §3.1 (no server-side cap either), the *only* thing
  standing between a correct payment and an overpayment is the operator's own
  memory of the number.

### 3.4 Items and budget templates exist as a domain concept and an admin feature — and are never connected to creating a budget

- `negocio/Presupuesto.java:100-107` models the full intended shape: a `pagoList`, a
  `tramiteList`, and an `itemList` (`@OneToMany` to `Item`, cascading), plus
  `DtoPresupuesto` mapping (`Presupuesto.java:250-254`) that converts incoming
  `DtoItem`s into persisted `Item` rows. This is a real, wired persistence path.
- `PlantillaPresupuestoController` (`GET /api/v1/plantillas-presupuesto/tramite/{id}`)
  and a full admin screen at `frontend/src/app/dashboard/administracion/plantillas`
  let staff define, per `TipoDeTramite`, which `Concepto`s (line items) and default
  amounts a budget for that transaction type should contain — a template system with
  its own CRUD lifecycle.
- But `frontend/src/app/dashboard/presupuestos/page.tsx`'s create/edit form has
  exactly one financial field: `editing.monto`, a single flat number the user types
  by hand (`page.tsx:229-234`). There is no tramite-type selector that would let the
  UI fetch `PlantillaPresupuesto` rows, no itemized line list, no "generate from
  template" action, and no call anywhere in the presupuestos page to
  `/api/v1/plantillas-presupuesto/*` or an items endpoint.
- So the templates a notary defines to standardize pricing per transaction type have
  no path into an actual budget. Two staff members pricing the same `TipoDeTramite`
  have no shared source of truth to draw from except memory — the exact problem
  `PlantillaPresupuesto` was modeled to solve.

### 3.5 `Presupuesto` has two contradictory relations to `Tramite`

- `negocio/Presupuesto.java:96-98` has `fkIdTramite` — a single `@ManyToOne Tramite`
  (one budget belongs to one transaction).
- `negocio/Presupuesto.java:103-104` *also* has `tramiteList` — a
  `@OneToMany(mappedBy = "fkIdPresupuesto") List<Tramite>` (many transactions belong
  to one budget).
- Both map through the same FK direction conceptually (`Presupuesto`↔`Tramite`), but
  express opposite cardinalities. Neither is populated by the frontend (§3.4 — no
  tramite selection exists in the create form at all), so today this is latent
  rather than actively producing bad data, but it means the domain doesn't actually
  know whether "a budget belongs to one transaction" or "a budget can span several"
  is the real rule — and whichever path first gets wired to the UI will silently pick
  an answer nobody decided on.

## 4. Case lifecycle: Gestión, Workflow, and Testimonio

### 4.1 A full workflow engine exists — and never gates a real case's state

- `WorkflowDefinition` / `WorkflowNode` / `WorkflowTransition` model a directed graph
  of legal states with `INITIAL`/`FINAL` node types, and `WorkflowValidationService`
  (`service/WorkflowValidationService.java`) validates that graph: exactly one
  `INITIAL` node, at least one `FINAL` node, no unreachable nodes (DFS reachability
  check, lines 17-58). There's an admin page to build these graphs.
- `GestionDeEscritura` (the actual case entity) has a single field for its state:
  `fkIdEstadoDeGestion`, a plain `@ManyToOne EstadoDeGestion` (`negocio/
  GestionDeEscritura.java:71-72`) — not a reference to a `WorkflowNode`, not
  constrained by any `WorkflowTransition`.
- `frontend/src/app/dashboard/gestiones/page.tsx:216-219` changes a case's state via
  a `<Select>` populated from `useEstadosGestion()` — **every** `EstadoDeGestion` in
  the system, unfiltered. There is no lookup of "which transitions are legal from the
  case's current state" anywhere in the page or in `GestionController`
  (`api/v1/gestiones`).
- Net effect: the workflow builder can express "a case must go Draft → Under Review →
  Signed → Registered, never backward, never skipping," and `WorkflowValidationService`
  will confirm that graph is well-formed — but nothing stops a user from moving a real
  case directly from Draft to Registered, or from Registered back to Draft, from the
  actual gestiones screen. The engine validates its own diagram, not real-world
  transitions. This matches (and gives concrete mechanism to) the technical report's
  §4.4 finding that CU69-78 (Workflow) is untraced in `CU-API-MATRIX.csv` — it's not
  just untracked, it's functionally disconnected from the entity it's meant to govern.

### 4.2 Suplencia (notary substitution) is CRUD with no consumers

- `Suplencia` (`negocio/Suplencia.java`), its controller, service, and repository are
  a complete, isolated CRUD stack — create/read/update/delete a substitution record
  (presumably: notary A covers notary B's cases from date X to Y).
- Grepping every other service and controller in the backend for any reference to
  `Suplencia` turns up nothing except its own CRUD stack and one leftover
  registration in the legacy `AdministradorJpa` static provider
  (`service/AdministradorJpa.java:95`).
- Concretely: `GestionDeEscritura.fkIdPersonaEscribano` (`negocio/
  GestionDeEscritura.java:94-95`, `optional = false` — every case *must* have an
  assigned notary) is set directly, with nothing checking whether that notary has an
  active `Suplencia` record that should redirect the case to their substitute. The
  substitution feature can record that notary A is out and notary B is covering, but
  that fact has zero effect on which notary ends up assigned to a new case.

### 4.3 Testimonio registry movement has a backend and no front door

- `MovimientoTestimonio` (`negocio/MovimientoTestimonio.java:56-79`) models a real
  legal lifecycle step for a notarial deed: `fechaIngreso`/`fechaSalida` (entry/exit
  from the property registry), `fechaInscripcion`, an `inscripta` boolean, and a
  `numeroCarton` — this is the "deed sent to registry, came back inscribed" step of
  the CU07/CU08 area the functional baseline (`docs/01-business/
  00-FUNCTIONAL-BASELINE.md`, referenced in the technical explore report §4.4)
  already flagged as UI-pending.
- `MovimientoTestimonioController` exists and is a real REST controller. Grepping all
  of `frontend/src` for `MovimientoTestimonio` or `movimientoTestimonio` returns
  nothing — no page, no hook, no fetch call.
- This confirms the functional-baseline gap with a specific entity: the backend can
  record that a testimonio left for the registry and came back inscribed, but no
  screen lets a notary or staff member actually do that. Today, tracking that step
  necessarily happens outside the system entirely (paper, spreadsheet, memory).

## 5. Identity: Persona has no duplicate protection

- `negocio/Persona.java:124` declares `numeroIdentificacion` (national ID / DNI) as a
  plain `@Column`, no `unique = true`, no `@Column(unique = true)` constraint.
- Every Flyway migration under `backend-api/src/main/resources/db/migration/` was
  grepped for `numero_identificacion` alongside `UNIQUE` — the only hit is the
  `V2__initial_data.sql` seed insert (`:92`), not a constraint definition. No unique
  index exists at the DB level either.
- No repository method (`existsByNumeroIdentificacion`, or similar) or service-layer
  check was found guarding `PersonaService`'s create path.
- In a notary system, the `Persona` record is the anchor every `Presupuesto`,
  `Tramite`, and `Gestion` hangs off of. Without a uniqueness guarantee, the same
  client entered twice (typo, different staff member, re-entry after a search
  miss) becomes two disconnected identities — their transaction history silently
  splits, and nothing in the system would ever surface that split to a user.

## 6. Why this pattern keeps recurring

Every finding above has the same shape: a rule is expressed as a **standalone,
well-built admin CRUD feature or entity relation**, but the **task-oriented workflow
screen** that would need to *consume* that rule to enforce it was built independently
and never wired back. Templates, workflow graphs, substitution records, and the
saldo-pendiente endpoint all exist and, in isolation, work correctly — the gap is
consistently at the integration seam between "manage the rule" and "apply the rule,"
not in the rule's own implementation. That's a different category of problem than the
technical report's architecture-drift findings (§4.2 there): those are about *how*
code reaches the database; these are about whether the *right two pieces of code ever
call each other* at all.

## 7. Escritura / Copia / Folio: administered, but never assigned to each other

The first pass only grep-spot-checked `protocolo`/`copias`. Reading `Folio.java`,
`Copia.java`, `FoliosCopias.java`, `EscrituraController.java`, `FolioController.java`,
`CopiaController.java`, and all three frontend pages (`escrituras`, `copias`,
`protocolo`, `administracion/folios`) in full turns up a correction to an easy
assumption plus a real gap the correction doesn't erase.

**Correction first, since it matters for the CU cross-reference in §11:**
`/dashboard/protocolo` *is* read-only (folio listing, PDF report links) — but that's
correct for its purpose (it maps to CU63 "Buscar Folios", a consult screen). Folio
*creation* lives at a separate screen, `/dashboard/administracion/folios`, which has a
full create/edit form (`numero`, `año`, `estado`, a `tipoFolioId` select backed by its
own nested Tipo-de-Folio CRUD, and an `escribanoId` select backed by
`GET /escrituras/escribanos-disponibles`) plus delete, all guarded by an "in use"
check before edit/delete is allowed. Folio administration is real and reachable —
correcting what a grep-only pass would have flagged as missing entirely.

**What's still missing, verified this pass:**

- **Dual JPA mapping onto the same `folios_copias` table.** `Folio.copiaList` is a
  `@ManyToMany(fetch = LAZY)` with an explicit `@JoinTable(name = "folios_copias", ...)`.
  Separately, `FoliosCopias` is its own `@Entity` with an `@EmbeddedId` composite key
  over the *same* `folios_copias` table, plus its own `@Version` optimistic-lock
  column. Two independent JPA paths model the same physical join table — one with
  optimistic locking, one without. Today this is latent: neither `FolioController` nor
  `CopiaController` exposes an endpoint that writes to either path (both are grepped
  in full — plain CRUD + `search`/`in-use` only, no cross-assignment action). But if a
  future change wires one path while the other stays live via the legacy
  `ControllerNegocio`/`jpa` route, they can silently desync or bypass each other's lock.
- **No UI or modern REST endpoint links Folio ↔ Escritura, or Copia ↔ Folio/Testimonio,
  anywhere.** `escrituras/page.tsx` displays `e.folio?.numero` as a read-only table
  column, but its create/edit form (`EMPTY = { numero, fechaEscrituracion, cuerpo,
  estado }`) only renders inputs for `numero` and `fechaEscrituracion` — `cuerpo` (the
  deed's body text) and `estado` are declared in the default object but never given a
  form field, so they can never be set through the UI at all. `copias/page.tsx`
  displays `c.testimonio?.numero` read-only, but its form (`numero`, `fechaImpresion`,
  `fechaRetiro`, `observaciones`) has no testimonio, folio, or persona selector —
  `Copia.fkIdPersona` and `Copia.fkIdTestimonio` are both entity-level relations with
  no UI path to populate them.
- **Net effect:** the "protocolo" — a notary's official, regulatorily-required record
  linking specific deeds to specific folio numbers — can have its Folio rows and its
  Escritura rows each administered independently, but the assignment that actually
  makes it a protocolo (which deed occupies which folio, which copy came from which
  testimonio) has no path through the product's own UI or its modern REST controllers.
  It would have to happen through the legacy `jpa`/`ControllerNegocio` path (already
  flagged as bypassing Spring transactions in the technical report's P0-2) or direct
  DB/API access outside the application.
- This also means CU06 "Firmar escritura" has no mechanism: there's no dedicated
  sign/state-transition endpoint on `EscrituraController` (grepped in full — only
  standard CRUD + `escribanos-disponibles` + `buscar`), and the one field that would
  represent "signed" (`estado`) has no UI input at all. See §11 for the full CU
  cross-reference.

## 8. DocumentoPresentado / TipoDeDocumento: administrable in name only

- **`administracion/documentos` is a real CRUD screen for `TipoDeDocumento`** (create,
  edit, delete with an "in use" guard, search) — correcting a prior working
  assumption that no admin UI existed for this catalog at all. But reading the
  187-line file in full: the create/edit form has exactly one field, `nombre`. The
  business-relevant columns the entity actually models —
  `habilitado`, `vence`, `diasVencimiento`, `quienEntrega`, `devuelto` — have no
  corresponding input anywhere in the form. A staff member can name a document type;
  they cannot configure whether it expires, how many days until it does, or who's
  responsible for returning it, through any screen in the product.
- **`DocumentoPresentadoController.toEntity()`** (create path) hardcodes
  `entity.setNombre("")`, `entity.setQuienEntrega("")`, `entity.setPreparado(false)`,
  `entity.setVence(false)` rather than defaulting from the associated
  `TipoDeDocumento` row — so even if the fields above *could* be configured on the
  type catalog, a newly created `DocumentoPresentado` wouldn't inherit them anyway.
  The two settings are disconnected in both directions.
- **`DocumentoPresentado.fkIdTipoDocumento`** is a plain `@Column private Integer`,
  not a JPA relation — yet `TipoDeDocumento.documentoPresentadoCollection` declares
  `@OneToMany(mappedBy = "fkIdTipoDocumento")` pointing at that non-relational field
  name. This is structurally suspect (a `mappedBy` target that isn't itself a mapped
  relation), flagged with appropriate hedging since it wasn't confirmed by executing
  the mapping — but if Hibernate does fail to resolve it at bootstrap, or silently
  no-ops the collection, that's worth a targeted runtime check before relying on
  `TipoDeDocumento.documentoPresentadoCollection` for anything.
- **Two getters for the same nullable field, one unsafe.** `getFkIdTipoDocumento()`
  returns primitive `int` (unboxing a `null` throws `NullPointerException`);
  `getFkIdTipoDocumentoNullable()` returns `Integer`. `DocumentoPresentadoController`
  correctly uses the nullable variant. The legacy god-class `ControllerNegocio` still
  calls `DocumentoPresentado.getDto()` in five places (lines ~1837, 1892, 2351, 4524,
  4556), and that method — read in full — does
  `dtoTramite = fkIdTramite.getDto(); dtoDocumentoPresentado.setFkTramite(dtoTramite);`
  with **no null check** on `fkIdTramite` (an `optional = true` relation), plus builds
  a `DtoTipoDeDocumento` that's constructed but never populated or attached (dead
  code in the same method). Any `DocumentoPresentado` created cleanly through the
  modern controller — with a `null` `fkIdTramite`, which the relation explicitly
  allows — will throw an NPE the moment it's touched by the still-live legacy path.
  This is the same modern-vs-legacy integration bug shape as the technical report's
  P0-2, but concrete and reproducible on this specific entity.
- Contrast: `Tramite.getDto()`'s equivalent call on its own optional relation
  (`fkIdInmueble`) *is* null-guarded (`if (fkIdInmueble != null) {...} else
  {miDto.setInmueble(null);}`) — see §10. The unguarded pattern in
  `DocumentoPresentado.getDto()` isn't how every entity in the codebase handles this;
  it's inconsistent even within the same legacy class family.

## 9. Historial: neither an audit trail nor a maintained log — it's write-orphaned

The question this pass set out to answer: is `Historial` a real append-only record of
`GestionDeEscritura` state changes, or just a free-text log staff fill in by hand? The
honest answer, read directly from `GestionController.java` in full: **it's neither,
today, because nothing in the real case-management flow ever writes to it.**

- `GestionController.applyGestionFields` (static, called by both `createCompleteCase`
  — "CU02" — and `updateCompleteCase` — "CU02") is the *only* place
  `GestionDeEscritura.fkIdEstadoDeGestion` is ever written. Neither
  `createCompleteCase` nor `updateCompleteCase` calls `historialRepository.save(...)`
  anywhere — confirmed by reading both methods in full and grepping the whole
  controller for `historialRepository.save` (the only hit is a comment-free absence;
  the repository is used only for reads).
- `HistorialController` is a fully independent, disconnected CRUD surface: raw
  `GET`/`GET {id}`/`GET /gestion/{idGestion}` (labeled "CU13")/`POST`/`PUT`/`DELETE`
  directly against `HistorialRepository`, with no validation that a posted
  `fkIdGestion` matches the case's actual current state, and no call to it from
  anywhere in the gestión create/update flow.
- **`GestionController.getEstadoActual`** (`GET /{id}/estado-actual`) computes "current
  state" by fetching `historialRepository.findByFkIdGestionIdGestion(id)` and taking
  the max-dated row (`historiales.stream().max(Comparator.comparing(Historial::getFecha))`).
  Since the real write path never populates a `Historial` row, **this endpoint returns
  404/empty for every single gestión created through the actual product flow.** It's a
  derived read over a table its own system never writes to.
- No frontend page references `Historial` at all (confirmed: no `historial` directory
  anywhere under `frontend/src/app/dashboard`). The only reachable path to see or
  record a `Historial` entry is direct API calls to `HistorialController` — not
  something a notary staff member would ever do.
- So: `Historial`'s shape (timestamp + state + observaciones per row) is *capable* of
  being an append-only audit trail. In practice it's inert — not written
  automatically as an audit trail would be, and not populated manually either, since
  there's no UI surface for it. This directly compounds §4.1's finding: gestión state
  changes aren't just unconstrained by the workflow engine, they're also unaudited —
  the one mechanism that could show *who changed a case's state and when* never fires.

## 10. Inmuebles: the one domain in this pass that's wired correctly end-to-end

Worth stating as a deliberate contrast to §7–9 and to the earlier report's §6 pattern,
not just another gap:

- `Inmueble` (`negocio/Inmueble.java`) has `tramiteList` (`@OneToMany`); `Tramite` has
  the inverse `fkIdInmueble` (`@ManyToOne`, `@JoinColumn(name = "fk_id_inmueble")`).
  `Tramite.getDto()` null-guards this relation correctly before calling `.getDto()` on
  it (`if (fkIdInmueble != null) {...} else {miDto.setInmueble(null);}`) — the safe
  pattern the `DocumentoPresentado.getDto()` call in §8 lacks.
- `frontend/src/app/dashboard/inmuebles/page.tsx` is a genuine, complete CRUD screen
  (create/edit/delete) against `InmuebleController`.
- `frontend/src/app/dashboard/gestiones/page.tsx` has an optional
  `data-testid="select-inmueble-gestion"` picker, populated from the same
  `inmuebles` list and rendering `${i.domicilio} #${i.idInmueble}`, wired into the
  complete-case create/update form (`CompleteCaseRequest.inmuebleId` →
  `CaseDependencies.inmueble` → `applyGestionFields` sets it on the `Tramite`).
- That's the full loop: modeled relation → null-safe DTO conversion → standalone
  admin CRUD screen → actually consumed by the task-completion screen that creates a
  real case. None of the other domains read this pass (§3, §4, §7–9) close that loop.
- **Relation to `Presupuesto`: there isn't one.** `Presupuesto.java` has no
  `fkIdInmueble` or equivalent, and the inmueble picker only appears in the gestiones
  form, never in `presupuestos/page.tsx`. This isn't a bug — nothing in the domain or
  the CUs claims a budget should reference a specific property directly, it flows
  through `Tramite` instead — but it's worth recording as a checked absence rather
  than an assumed one, since it was explicitly asked about.

## 11. Full CU-by-CU cross-reference (code and UI, not `CU-API-MATRIX.csv`)

Cross-checked all 78 use-case documents under `docs/01-business/02-use-cases/03_CU -
Casos de Uso/` (the CU catalog runs to CU78, not CU68 — CU69–73 are business use
cases for Inmuebles/Copias/Items/Documentos Presentados/Auditoría; CU74–78 are
non-functional/process use cases — performance, DB migrations, test infra, ops
monitoring, security — not mappable to a single screen by design, and already covered
by the technical report's §4.1–4.5) against the actual inventory of 31 backend REST
controllers and 26 frontend dashboard routes, verifying every non-obvious mapping by
reading the relevant controller/page rather than trusting the CU title alone.

Legend: **✅** reachable end-to-end through the product's own UI (may still have
functional gaps documented elsewhere in this report) · **⚠️** reachable but materially
incomplete (a screen exists but is missing the field/action the CU implies) · **🚫**
no UI path at all (backend may or may not exist) · **⛔** the CU document itself marks
the use case deprecated/removed.

| CU | Title | Status | Evidence |
|---|---|---|---|
| CU01 | Preparar Presupuesto | ✅ | `presupuestos` + `PresupuestoController` (flat `monto` only — §3.4) |
| CU02 | Iniciar Gestión | ✅ | `gestiones` complete-case + `GestionController` |
| CU03 | Lista documentos y certificados necesarios | 🚫 | `PlantillaTramiteController` (tipo-tramite↔tipo-documento) exists; zero frontend references anywhere |
| CU04 | Registrar documentación cliente | ✅ | `documentos` + `DocumentoPresentadoController` (create hardcodes empty defaults — §8) |
| CU05 | Preparar escritura | ⚠️ | `escrituras` create form only has `numero`+`fecha`; `cuerpo` (deed body) has no input anywhere (§7) |
| CU06 | Firmar escritura | 🚫 | No sign/transition endpoint on `EscrituraController`; `estado` has no UI field at all (§7) |
| CU07 | Generar testimonio | 🚫 | `TestimonioController` is plain CRUD; no frontend page exists for it anywhere |
| CU08 | Verificar Testimonio | 🚫 | `MovimientoTestimonioController` exists; zero frontend references |
| CU09 | Registrar deudas documentos de Cliente | ⚠️ | Only a read-only "Deuda de Documentos" PDF report by gestión number (`reportes` page) — not a registration action |
| CU10 | Registrar movimientos documentación de entidades externas | 🚫 | Same `MovimientoTestimonio` backend-only cluster as CU08 |
| CU11 | Ingresar para inscripción | 🚫 | `inscripcion`-related fields exist only on `Escritura`/`MovimientoTestimonio`; zero frontend references |
| CU12 | Retirar testimonio | 🚫 | Same `MovimientoTestimonio` cluster |
| CU13 | Ver historial de gestión | 🚫 | `HistorialController` exists, no frontend page; its own read endpoint is broken (§9) |
| CU14 | Consultar estado gestión | ⛔ | Document title itself reads "(ELIMINADO *)" |
| CU15 | Procesar pago | ✅ | `pagos` + `PagoController` (no overpayment cap, no saldo visibility — §3.1–3.3) |
| CU16 | Archivar Gestión | ⚠️ | No `archivar` reference anywhere in the backend; only reachable, if at all, via the unconstrained estado dropdown (§4.1) |
| CU17 | Dar Alta persona | ✅ | `personas` + `PersonaController` (no duplicate protection — §5) |
| CU18 | Dar Alta Cliente | ✅ | Same screen, `Persona.esCliente` flag |
| CU19 | Buscar gestiones de un Cliente | ✅ | `gestiones` `clienteFilter` select + `useGestionesByCliente` |
| CU20 | Dar alta usuario | ✅ | `administracion/usuarios` + `UsuarioController` |
| CU21 | Modificar Usuario | ✅ | Same screen |
| CU22 | Registrar Suplencia | ✅ | `suplencias` + `SuplenciaController` (CRUD works, never consumed — §4.2) |
| CU23 | Ver registro de actividades de usuario | ✅ | `auditoria` + `RegistroAuditoriaController` |
| CU24 | Generar libro de índices | ✅ | `reportes` → `/reportes/libro-indice` |
| CU25 | Generar Declaración Jurada del mes | ✅ | `reportes` → `/reportes/declaracion-jurada-mensual` |
| CU26 | Ingresar nuevo tipo de trámite | ✅ | `administracion/tramites` + `TipoDeTramiteController` |
| CU27 | Ingresar nuevo tipo de documento | ⚠️ | `administracion/documentos` CRUD exists, only exposes `nombre` (§8) |
| CU28 | Ingresar nuevos folios | ✅ | `administracion/folios` full create form (§7 — corrects a grep-only assumption) |
| CU29 | Ingresar nuevo concepto | ✅ | `administracion/conceptos` + `ConceptoController` |
| CU30 | Ingresar nuevo estado de Gestión | ✅ | `administracion/estados-gestion` (any state pickable regardless of workflow graph — §4.1) |
| CU31 | Modificar tipo de trámite | ✅ | `administracion/tramites` edit |
| CU32 | Modificar tipo de documento | ⚠️ | Same name-only caveat as CU27 |
| CU33 | Modificar folio | ✅ | `administracion/folios` edit (disabled once `estado="Utilizado"`) |
| CU34 | Modificar concepto | ✅ | `administracion/conceptos` edit |
| CU35 | Modificar estado de Gestión | ✅ | `administracion/estados-gestion` edit |
| CU36 | Ingresar tipos de folio | ✅ | Nested Tipo-de-Folio CRUD inside `administracion/folios` |
| CU37 | Eliminar concepto | ✅ | `administracion/conceptos` delete |
| CU38 | Eliminar tipo de documento | ✅ | `administracion/documentos` delete, in-use guarded |
| CU39 | Crear Plantilla Presupuesto | ✅ | `administracion/plantillas` (never consumed by presupuesto create — §3.4) |
| CU40 | Modificar tipo de folio | ✅ | Nested Tipo-de-Folio edit |
| CU41 | Modificar Cliente | ✅ | `personas` edit |
| CU42 | Informar próximos vencimientos | 🚫 | Zero backend endpoint, zero frontend reference, despite `vence`/`diasVencimiento` existing on `TipoDeDocumento` for exactly this |
| CU43 | Reingresar documentación | ⚠️ | No dedicated resubmission action; only a generic edit of an existing row |
| CU44 | Reingresar testimonio | 🚫 | Same `MovimientoTestimonio` cluster |
| CU45 | Modificar presupuesto | ✅ | `presupuestos` edit |
| CU46 | Ver detalle cliente | ✅ | `personas` detail view |
| CU47 | Consultar Pago | ✅ | `pagos` list/search |
| CU48 | Dar alta escribano | 🚫 | "Escribano" = a `Persona` with `registroEscribano` set, but `personas` page never exposes that field |
| CU49 | Eliminar Plantilla Presupuesto | ✅ | `administracion/plantillas` delete |
| CU50 | Generar Declaración Jurada de Rentas | ✅ | `reportes` → `/reportes/declaracion-jurada-rentas` |
| CU51 | Modificar escribano | 🚫 | Same gap as CU48 — no field to modify `registroEscribano` anywhere |
| CU52 | Modificar Escritura | ⚠️ | Edit form has the same `numero`+`fecha`-only fields as create (§7) |
| CU53 | Modificar Gestión | ✅ | `gestiones` `updateCompleteCase` |
| CU54 | Modificar Persona | ✅ | Generic `personas` edit (duplicate of CU21/CU41 pattern) |
| CU55 | Modificar Plantilla Presupuesto | ✅ | `administracion/plantillas` edit |
| CU56 | Registrar inscripcion | 🚫 | Same as CU11 — backend-only fields, no UI |
| CU57 | Eliminar tipo de trámite | ✅ | `administracion/tramites` delete |
| CU58 | Eliminar tipo de folio | ✅ | Nested Tipo-de-Folio delete |
| CU59 | Consultar Suplencias | ✅ | `suplencias` list |
| CU60 | Buscar Presupuesto | ✅ | `presupuestos` search |
| CU61 | Buscar persona o cliente | ✅ | `personas` search |
| CU62 | Buscar Escritura | ✅ | `escrituras` `searchNumero` + `/escrituras/buscar` |
| CU63 | Buscar Folios | ✅ | `protocolo` (correctly read-only consult screen) |
| CU64 | Buscar Tipo de tramite | ✅ | `administracion/tramites` search |
| CU65 | Buscar Tipos de documentos | ✅ | `administracion/documentos` search |
| CU66 | Buscar Conceptos | ✅ | `administracion/conceptos` search |
| CU67 | Buscar Estados de Gestión | ✅ | `administracion/estados-gestion` search |
| CU68 | Buscar tipos de folios | ✅ | Nested Tipo-de-Folio search |
| CU69 | Gestión de Inmuebles | ✅ | Fully wired incl. gestión picker (§10) |
| CU70 | Gestión de Copias | ⚠️ | CRUD exists, can't set the testimonio/folio/persona relations it displays (§7) |
| CU71 | Gestión de Items | ⚠️ | Standalone admin CRUD, never connected to Presupuesto creation (§3.4) |
| CU72 | Gestión de Documentos Presentados | ✅ | `documentos` + `DocumentoPresentadoController` (getDto()/unsafe-getter caveats — §8) |
| CU73 | Registro de Auditoría | ✅ | `auditoria` + `RegistroAuditoriaController` (overlaps CU23) |
| CU74–78 | Performance, DB Migrations, Testing Infra, Ops Monitoring, Security | — | Non-functional/process CUs, not screen-mappable by design; see `explore_report.md` §4.1–4.5 |

**Summary:** of the 73 business-facing use cases (CU01–73), 13 have no UI path at all
(CU03, 06, 07, 08, 10, 11, 12, 13, 42, 44, 48, 51, 56 — almost all clustering into two
groups: the `MovimientoTestimonio`/inscripción registry-movement lifecycle, and the
`registroEscribano`/testimonio-generation gap), 9 are reachable but materially
incomplete (CU05, 09, 16, 27, 32, 43, 52, 70, 71), 1 is explicitly deprecated by its
own document (CU14), and the remaining ~50 are reachable end-to-end through the
product's UI — though several of those carry the business-rule gaps documented in
§3–§10 even where the screen itself exists. The two biggest clusters of missing
screens — testimonio/registry-movement (CU07/08/10/12/44/56/11) and the
notary-substitution-adjacent escribano management (CU48/51) — both represent legally
material notarial-practice steps (registering a deed with the property registry,
credentialing who's allowed to notarize) that today have no product surface at all.

## 12. Why this pattern keeps recurring

Every finding above has the same shape: a rule is expressed as a **standalone,
well-built admin CRUD feature or entity relation**, but the **task-oriented workflow
screen** that would need to *consume* that rule to enforce it was built independently
and never wired back. Templates, workflow graphs, substitution records, and the
saldo-pendiente endpoint all exist and, in isolation, work correctly — the gap is
consistently at the integration seam between "manage the rule" and "apply the rule,"
not in the rule's own implementation. That's a different category of problem than the
technical report's architecture-drift findings (§4.2 there): those are about *how*
code reaches the database; these are about whether the *right two pieces of code ever
call each other* at all. §10 (Inmuebles) is the one domain checked across both passes
where that seam is actually closed — worth treating as a reference implementation for
what "wired correctly" looks like in this codebase, rather than assuming the pattern
is universal.

## 13. What this pass still didn't cover

Two passes now: the first covered the financial (Presupuesto/Item/Pago/
PlantillaPresupuesto) and case-lifecycle (Gestión/Workflow/Testimonio) domains plus a
Personas spot-check; this one added Escritura/Copia/Folio, DocumentoPresentado/
TipoDeDocumento, Historial, Inmuebles, and a full CU-by-CU cross-reference. Still
untouched, worth a follow-up pass if this line of investigation continues:

- Reportes/JasperReports generation logic itself (only the routing/wiring to reach
  each report was checked here, not the report content or query correctness).
- RegistroAuditoria's actual coverage — CLAUDE.md claims it records create/update/
  delete plus logins attributing the JWT identity, but that claim wasn't verified
  against `AuditoriaAspect`'s actual pointcuts this pass.
- Roles/permissions (`administracion/roles`, `RolController`) — exists as a screen,
  not cross-checked against the technical report's P0-4 finding that no RBAC
  enforcement exists anywhere in the backend; worth confirming whether "roles" here
  are just labels with no `@PreAuthorize`-style effect.
- The individual body text of each of the 78 CU documents (acceptance criteria,
  actor lists, alternate flows) — this pass matched CU title/intent against actual
  code and UI reachability, not a line-by-line spec conformance check per CU.

---
*Generated via `/opsx:explore`. Grounded in repo state as read on 2026-08-11 —
re-verify specifics (file paths, line numbers) before acting on them, since this
snapshot ages the moment new commits land.*
