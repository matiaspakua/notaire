# ADR-030: Full Ports & Adapters architecture for backend and frontend

**Status:** Accepted — Owner directive of 2026-10-14 (epic #1693, child #1694)  
**Date:** 2026-10-14  
**Deciders:** Owner (product), Engineering  
**Related:** ADR-021 (pilot), ADR-002 (module structure), ADR-023 (REST naming), ADR-025/026, ADR-027 (Mermaid), ADR-029 (tenancy), #984, #993, #995, #1695, #1696  
**Supersedes:** the scope limit of ADR-021 ("payment slice only"; "decide whether to extend") and the layered `api → service → repository` structure of ADR-002 for `backend-api`.

## Context

Notaire shall be an exemplary Ports & Adapters system: modular, extensible and generic. The Owner made this the
top priority of the project (#1693).

ADR-021 proved the pattern on the payment slice and left open whether to extend it. Waves #993 and #995 added
ports for several domains. A measurement of `main` @ `3c1f99ce` (details and commands in #1693) shows the result
is partial in the backend and absent in the frontend:

- 38 web controllers: 25 use the application layer, 27 touch `repository` or `jpa` directly, only 6 are clean;
  31 import JPA entities; 31 web classes carry `@Transactional`.
- `application`: 81 of 108 files import JPA entities, 24 import Spring Data repositories. 41 of 60 port files
  expose entities, so ports do not speak domain types.
- `domain/` holds 7 pure files; the real model is `business/` with 38 `@Entity` classes, 34 of 49 files coupled
  to Spring or Jackson. `jpa` (35 files), `repository` (37) and `service` (3) remain.
- Frontend: 22 files under `app/` and 43 of 49 hooks import `api-client` directly; no ports, no domain module,
  no composition root, no boundary lint.
- Nothing enforces any boundary, so leaks grow back.

## Decision

Adopt **full Ports & Adapters in both tiers**, enforced by tests that can only ratchet toward zero, and reach it
by strangler slices that keep the REST contract unchanged.

### 1. Backend (`backend-api`, root package `com.licensis.notaire`)

Dependencies point inward only: `adapter` → `application` → `domain`.

| Package | Contents | May depend on |
|---|---|---|
| `domain.<context>` | Entities, value objects, domain services, domain exceptions. Plain Java. | JDK only |
| `application.port.in.<context>` | Inbound ports: one interface per use case, plus command and result records. A catalog's CRUD may share two cohesive interfaces, query and manage. | `domain` |
| `application.port.out.<context>` | Outbound ports: what the use cases need (repositories, gateways, clock, ids), in domain terms. | `domain` |
| `application.usecase.<context>` | Use-case classes implementing inbound ports, using outbound ports. `@Service` and `@Transactional` only. | `domain`, ports |
| `adapter.in.web.<context>` | Controllers, web DTOs, web mappers, request validation. Call inbound ports only. | inbound ports, `domain` types they map |
| `adapter.out.persistence.<context>` | `<Name>JpaEntity`, `<Name>JpaRepository`, `<Name>Mapper`, `<Name>PersistenceAdapter` implementing outbound ports. | outbound ports, `domain` |
| `adapter.out.<other>` | PDF, storage, notification and other infrastructure adapters. | outbound ports, `domain` |
| `config` | Spring wiring, security configuration, OpenAPI. The composition root. | everything |

Rules:

1. **Controllers call inbound ports only.** No `repository`, `jpa`, `EntityManager`, entity or `@Transactional` in
   an inbound adapter. Request validation (`@Valid`) and HTTP status mapping stay there.
2. **Use cases own the transaction.** `@Transactional` on the use case. Use cases of one context call another
   context only through its inbound ports, never its repositories.
3. **Ports speak domain types.** A port never mentions a JPA entity, a Spring Data type or a web DTO.
4. **Persistence entities live inside the adapter.** `domain.Person` is not a JPA class; `PersonJpaEntity` is, and
   a mapper converts. Replacing JPA, or testing with an in-memory adapter, touches only `adapter.out.persistence`.
5. **Domain exceptions carry no HTTP status.** The inbound adapter maps them (the global exception handler).
   `NotaireException(int statusCode, …)` is a transport leak to be retired slice by slice.
6. **Framework use in `application` is an allow-list:** `@Service` and `@Transactional` (kept from ADR-021 by the
   Owner). Any other Spring, JPA, Hibernate or Jackson type is a violation.
7. **Cross-cutting concerns are adapters.** Authentication filters and authority resolution are inbound
   adapters; the audit aspect and observability are outbound/cross-cutting adapters. They never hold business
   rules.
8. **Legacy packages are closed.** `jpa`, `service`, the top-level `repository`, `business` (as model) and the
   top-level `dto` receive no new dependents and are deleted when empty.

### 2. Frontend (`frontend/src`)

| Area | Contents | May import |
|---|---|---|
| `core/<context>/domain` | Types, value objects, pure rules and validation. No React, no `fetch`. | nothing outside `core` (plus `zod`) |
| `core/<context>/application` | Use cases as functions or classes, and `ports.ts` with the interfaces they need. | `core` |
| `adapters/http/<context>` | Implements the ports with `lib/api-client`; maps transport shapes to domain types. | `core`, `lib/api-client` |
| `adapters/ui` (`app/`, `components/`, `hooks/`) | Pages, components and React Query hooks that call use cases. | `core/**/application`, `theme`, `store` |
| `composition/` | Builds the use cases with the http adapters; exposed through one React provider. | everything |

Rules:

1. `app/`, `components/` and `hooks/` never import `lib/api-client` or call `fetch`.
2. `core/**` never imports React, Next, `adapters/**` or `lib/**`.
3. Hooks get use cases from the composition provider, so tests replace an adapter without module mocking.
4. A backend contract change is absorbed by `adapters/http` mappers, not by pages.

### 3. Enforcement (the refactor can only move forward)

- **Backend:** ArchUnit `HexagonArchitectureTest` (#1695) with a shrink-only baseline. R1 enforces backend
  rule 1; R2 enforces rules 3 and 6; R3 enforces the pure domain of rule 4; R4 and R5 keep the two adapter
  families independent; R6 enforces rule 8. Rules 2, 5 and 7 are checked in review until a rule is added.
- **Frontend:** import-boundary lint with a shrink-only baseline (#1696).
- Each slice deletes its entries from the baselines; a new violation or a stale entry fails the build.
- Still to add as slices land: a rule that the model is out of `business` and a no-cycles rule between
  use-case slices (tracked on #1693).

### 4. Migration method

Strangler, one bounded context per slice, never a big bang. A slice is **done** only when all of these hold:

1. Characterization tests of the current REST behaviour exist first and stay green (status codes and success
   bodies unchanged; the OpenAPI diff is empty).
2. The context has inbound ports, outbound ports and use cases; its controllers call ports only.
3. Its model is in `domain.<context>`, pure; its persistence entities, repositories and mappers are in
   `adapter.out.persistence.<context>`.
4. The legacy classes of that context (`jpa`, `service`, `repository`, `business`) are deleted, not left behind.
5. Its ArchUnit baseline entries are removed (`-Darchitecture.baseline=shrink`) and the frontend counterpart,
   when the context has a UI, follows the same steps in `core` and `adapters`.
6. The slice archives its OpenSpec change and updates the SAD only where the picture changed.

Suggested order, from the measured data: (a) the 13 controllers that bypass the application layer entirely
(workflow, catalogs such as folio, document type, identification type, management status, property, submitted
document, budget template), (b) users and roles, together with #1683, (c) the 14 mixed controllers, (d) model
extraction per aggregate, (e) deletion of `jpa`, `service` and `dto`.

## Slice log (what each slice taught)
- **Slice 1, identification types (#1700):** the first strict-form slice, reference implementation of the
  conventions above.
  - The legacy JPA entity stays in `business` while other aggregates (Person, Procedure, DeedManagement)
    reference it; the persistence adapter maps it to the pure domain type. The entity moves into
    `adapter.out.persistence` when its last legacy user migrates.
  - Domain factories separate creation (validates) from restoration (does not), so legacy rows never fail on read.
  - Domain exceptions carry no status. A controller-scoped advice with highest precedence maps them and delegates
    to the global handler, so the 404 and 409 bodies stay byte-identical; without the precedence the generic
    handler would take them.
  - The adapter flushes inside `deleteById` so a foreign key violation becomes the domain exception instead of
    surfacing at commit.
  - In-memory port adapters live in `src/test/.../testing`; use-case tests run with no Spring and no mocks.
  - The ArchUnit allow-list for `@Transactional` had to include its attribute types (`Propagation`,
    `Isolation`): 84 baseline entries were noise and were removed.
  - Behaviour note: a delete that fails for a reason other than a foreign key violation now answers 500 instead of
    409, which is the accurate status.



- **Slice 2, property (#1702):** replaced a fake hexagon. `PropertyUseCase`, `PropertyService` and
  `PropertyRepositoryPort` were pass-through wrappers over the JPA entity and nothing called the use case, because
  the controller injected the repository. Lessons:
  - "Has a port" is not the criterion: count interfaces that speak domain types, not files.
  - A domain-typed port can live next to the old entity-returning one (`PropertyCatalogPort` beside
    `PropertyRepositoryPort`) while eleven other classes migrate; the old port is debt that shrinks with them.
  - Moving the repository call into a `@Transactional` use case can change the exception class the controller sees.
    Here a create without the required address changed from 500 to 400 with the standard constraint message,
    because the exception is now translated to a data-integrity violation. The characterization test caught it;
    it was accepted deliberately, as a correction, and called out in the PR.
  - The HTTP error mapping of a failed create (`ErrorResponses.createFailed`) stays in the controller for now;
    removing the try/catch is the separate #579 work.



- **Slice 3, folio types (#1703):** the first catalog with an edit lock and optimistic locking.
  - Domain exceptions can share a base class (`FolioTypeException`) so the controller's remaining ad-hoc catch can
    let them through with one `catch`, until #579 removes the catches.
  - Optimistic locking moved into the use case as an explicit version comparison. The order of the checks
    (not found, then locked, then stale version) is part of the contract and is pinned by tests.
  - A "referenced" failure (the store refused a delete) is a subtype of "in use", so the advice can answer it with
    its own legacy plain-text body while a generic in-use answers the `{"error": …}` body.
  - The name rule is "not null", not "not blank": that is what the database column enforced, and tightening it
    would change behaviour. Tightening is a separate decision.
  - Tests that built the controller with mocked repositories were replaced by tests over the ports; the leak
    test (SQL errors never reach the client) was kept and rewritten, because that guarantee is worth keeping.
  - The same shape (search, in-use, locked when in use, delete guard) now exists twice (identification types
    and folio types) and appears again in document type and management status; extract the shared rule after
    the fourth, not before.
  - Lesson on process: read build exit codes and fresh log summaries, not `surefire-reports`, which keeps stale
    files from earlier runs when test compilation fails.



- **Slice 4, document types (#1705):** third catalog on the locked-when-in-use shape; usage spans **two** tables
  (procedure templates and submitted documents).
  - More fields than folio (`expires`, `dueDays`, `deliveredBy`, `enabled`, `returned`); create defaults for the
    three wire flags stay in the inbound adapter so OpenAPI clients keep the same omit-means-default behaviour.
  - Explicit version check in the use case is a **deliberate correction**: the legacy controller copied
    `dto.version` onto the entity and a stale client version still answered 200. Characterization first pinned
    that, then the assertion moved to 409.
  - A characterization put that does not dirty any field does not bump `@Version`; the stale-version scenario
    must change a field on the first update (same hazard as any JPA optimistic-lock test).
  - Shared lock/in-use rule now exists three times (identification, folio, document). Extract after management
    status makes four, not before.
  - Legacy `business.DocumentType` stays for submitted documents, case summary, reports, reentry and
    required-documentation services.



- **Slice 5, management status (#1719):** fourth catalog on the locked-when-in-use shape; usage is **one** table
  (deed managements). Fields are only `name` and `notes`.
  - Explicit version check in the use case is the same **deliberate correction** as document types: the legacy
    controller copied `dto.version` onto the entity and a stale client version still answered 200.
  - Shared lock/in-use rule now exists four times (identification, folio, document, management status). Extract
    is a follow-up after this green merge; do not diverge the shape in the meantime.
  - Legacy `business.ManagementStatus` stays for deed management, history and workflow consumers.



- **Extract, shared catalog lock (#1722):** after four green catalogs, folio / document / management status
  `update` and `delete` call `application.usecase.catalog.CatalogLockSupport`. Domain exception types and
  messages stay catalog-owned; the helper only owns load → lock → version → save/delete.
  - Identification adopts the **delete** half only (name uniqueness stays local on create/update). Its catalog
    port’s `deleteById` is void, so the call site always reports deleted after the load+lock checks.
  - Characterization suites for folio, document, management status and identification remain the HTTP contract;
    no status or body drift.



- **Submitted document (#1726):** instance aggregate (not a catalog). Controller was repo-only; now
  ports-only with domain `SubmittedDocument`, due inheritance in the use case, and a thin
  `ProcedureLookupPort`. Legacy `business.SubmittedDocument` remains for reports / external-entity /
  other consumers.



- **Folio instance (#1727):** instance aggregate (folio types already clean in #1703). Controller was the
  largest folio R1 hotspot (~7 edges); now ports-only with domain `Folio`, Utilizado lock and deed-link
  forcing in the use cases, `FolioCatalogPort`, and thin person/deed lookup ports. Legacy
  `FolioRepositoryPort` remains for AuxiliaryProtocol `startDeed`; available-auxiliary listing goes
  through the domain catalog so `FolioResponse` stays domain-only.



- **Workflow definition (#1728):** catalog-like aggregate (CU70). Controller was repo-only (~4 R1 edges,
  including an unused transition repository injection); now ports-only with domain `WorkflowDefinition`,
  delete-guard when nodes exist in the use case, and `WorkflowDefinitionCatalogPort`. Legacy
  `WorkflowDefinitionRepositoryPort` remains for other consumers. Sibling node/transition slices stay
  separate (#1729 / #1730).



- **Workflow node (#1729):** largest remaining workflow R1 hotspot (~6 edges). Controller was repo-only;
  now ports-only with domain `WorkflowNode` / `WorkflowNodeKind`, delete-guard when transitions exist,
  and `WorkflowNodeCatalogPort`. Reuses `WorkflowDefinitionCatalogPort` and `ManagementStatusCatalogPort`
  for create lookups. Legacy `WorkflowNodeRepositoryPort` remains for other consumers.



- **Workflow transition (#1730):** last workflow-trio R1 hotspot (~4 edges). Controller was repo-only;
  now ports-only with domain `WorkflowTransition`, and `WorkflowTransitionCatalogPort`. Reuses
  `WorkflowDefinitionCatalogPort` and `WorkflowNodeCatalogPort` for create/update lookups. Legacy
  `WorkflowTransitionRepositoryPort` remains for other consumers.



- **Users + roles (#1731):** security-sensitive dual-controller slice (~4 R1 edges each). Controllers
  were repo-only; now ports-only with domain `User` / `Role`, catalog ports, and `PasswordHasherPort`
  (BCrypt + legacy MD5 upgrade in `AuthenticateUserUseCase`). JWT/cookies/metrics/lockout stay in the
  HTTP adapter. Role `modules` are stored/returned as today; server-side grant enforcement is fenced
  for sibling [#1683](https://github.com/matiaspakua/notaire/issues/1683). Legacy repository ports remain.



- **Frontend first catalog — identification types (#1732):** first concrete FE hexagon after the #1696
  lint ratchet. `core/identification-type` (domain + gateway port + use cases), `adapters/http/identification-type`,
  and `composition/identification-type` wire the catalog; hooks and the admin page no longer import
  `api-client` (eslint suppressions shrink by two). `ApiError` moved to `lib/api-error` so UI can keep
  HTTP-status handling without importing the client. Other FE catalogs remain on the suppressions baseline.



- **Frontend second catalog — management statuses (#1769):** mirrors backend-clean management status
  (#1719) into `core/management-status`, `adapters/http/management-status`, and `composition/management-status`.
  Admin page and `useManagementStatuses` no longer import `api-client` (eslint suppressions shrink by two).



- **Frontend templates admin (#1788):** `core/procedure-template`, `core/budget-template`,
  `core/document-cost-template`, HTTP adapters, and composition. Templates page and
  `useProcedureTemplate` no longer import `api-client` (eslint suppressions shrink by three).



- **Frontend third catalog — document types (#1777):** `core/document-type`, `adapters/http/document-type`,
  and `composition/document-type`. Documents admin page and `useDocuments` no longer import `api-client`
  (eslint suppressions shrink by two).



- **Frontend fourth catalog — concepts (#1781):** `core/concept`, `adapters/http/concept`, and
  `composition/concept`. Concepts admin page and `useConcepts` no longer import `api-client`
  (eslint suppressions shrink by two).



- **Frontend folios admin (#1785):** `core/folio-type` + `core/folio`, HTTP adapters, and composition
  roots. The mixed folios administration page (types + instances + available notaries) no longer
  imports `api-client` (eslint suppressions shrink by one).



- **Workflow validation (#1770):** last repo-only web controller after the workflow trio. Controller
  now calls `ValidateWorkflowUseCase` only; graph load uses `Workflow*CatalogPort`; validation runs on
  domain `WorkflowNode` / `WorkflowTransition`. ArchUnit R1 clears its three repository edges.



- **Management mixed controller (#1772):** largest remaining R1 hotspot (~17 edges plus
  `TransitionManagementWebMapper`). Controller and transition web mapper are ports-only via
  `ManageManagementsUseCase` / `QueryManagementsUseCase` and outbound `ManagementOpsPort`
  (`ManagementOpsAdapter`). Application services stay free of new R2 entity/repository edges;
  entity access stays in the outbound adapter. Domain `DeedManagement` extraction remains deferred.



- **History ports-only (#1775):** next mixed/repo-like R1 hotspot after Management. `HistoryController`
  calls `QueryHistoriesUseCase` / `ManageHistoriesUseCase` only; persistence and CU13 chronological order
  live behind `HistoryOpsPort`. ArchUnit R1 clears its six baseline edges. REST `/api/v1/history` unchanged.



- **ProcedureType ports-only (#1791):** first Procedure* R1 hotspot (~4 edges). `ProcedureTypeController`
  calls `QueryProcedureTypesUseCase` / `ManageProcedureTypesUseCase` only; CRUD, in-use, and CU83 workflow
  assignment live behind `ProcedureTypeOpsPort`. ArchUnit R1 clears its four baseline edges.



- **Copy ports-only (#1779):** mid-size mixed R1 hotspot (~5 edges). `CopyController` calls
  `QueryCopiesUseCase` / `ManageCopiesUseCase` only; person/testimony FK resolution and CU87 conflict
  live behind `CopyOpsPort`. ArchUnit R1 clears its five baseline edges. REST `/api/v1/copies` unchanged.



- **Frontend procedures admin (#1796):** `core/procedure-type`, `adapters/http/procedure-type`, and
  `composition/procedure-type`. Procedures admin page and `useProcedureTypes` no longer import
  `api-client` (eslint suppressions shrink; procedure-template remains on #1788).



- **Testimony ports-only (#1783):** mid-size mixed R1 hotspot (~5 edges). `TestimonyController` calls
  `QueryTestimoniesUseCase` / `ManageTestimoniesUseCase` only; CRUD, generate/verify, and #1364 in-use
  delete live behind `TestimonyOpsPort`. ArchUnit R1 clears its five baseline edges.



- **Procedure ports-only (#1801):** Procedure* aggregate R1 hotspot (~4 edges). `ProcedureController`
  calls `QueryProceduresUseCase` / `ManageProceduresUseCase` only; association hydration (#981) and
  persistence live behind `ProcedureOpsPort`. ArchUnit R1 clears its four baseline edges.


- **ProcedureFolder ports-only (#1795):** smallest Procedure* R1 hotspot (1 edge). `ProcedureFolderController`
  calls `QueryProcedureFoldersUseCase` / `ManageProcedureFoldersUseCase` only; entity access and DTO
  mapping live behind `ProcedureFolderOpsPort`. ArchUnit R1 clears its ProcedureFolder entity edge.



- **Frontend workflows admin (#1792):** `core/workflow`, `adapters/http/workflow`, and
  `composition/workflow`. Workflows list/editor pages, `useWorkflow`, and `useManagementWorkflow`
  no longer import `api-client` (eslint suppressions shrink by four).


- **Concept ports-only (#1815):** small mixed R1 hotspot (~2 edges). `ConceptController` calls
  `QueryConceptsUseCase` / `ManageConceptsUseCase` only; BudgetTemplate in-use checks and persistence
  live behind `ConceptOpsPort`. ArchUnit R1 clears Concept/BudgetTemplateRepository edges.

## Options considered

- **Pragmatic hexagon: ports and use cases, but JPA entities double as the domain model.** Cheaper (no mappers
  or duplicate classes) and what most of the backend already approximates. Rejected: the domain stays coupled to
  Hibernate and Jackson, the 41 entity-exposing ports stay, and it cannot serve as an exemplary reference.
- **Strict hexagon (chosen).** Costs a mapper per aggregate and duplicate classes, and buys a pure domain,
  replaceable persistence and tests without a database.
- **Application layer with no framework annotations at all, Spring only in `@Configuration` classes.** Purer.
  Rejected by the Owner on #1693 in favour of ADR-021's `@Service` and `@Transactional`, enforced as an allow-list.
- **Big-bang rewrite.** Rejected again for the reasons in ADR-021: unreviewable, no way to validate the port
  design incrementally.
- **Hexagon for the backend only.** Rejected: the frontend is where most coupling to the API shape lives.

## Consequences

### Easier

- A use case is tested without Spring or a database; a second driving adapter (CLI, batch, queue) reuses the
  inbound ports; JPA, the PDF engine or the HTTP client can be replaced behind a port.
- The boundaries are executable: the baselines make progress measurable and regressions impossible.
- New contributors and agents follow one procedure per slice.

### Harder

- More files per feature (port, use case, mapper, adapter) and duplicated classes (domain and persistence entity).
- A long transition with mixed conventions; whoever touches an unmigrated context must follow the slice
  procedure or leave it untouched.
- Mappers must be tested, or they hide bugs; they are written by hand and covered by unit tests. MapStruct is not
  adopted without a separate decision.

## Open points

- The no-cycles rule and the model-extraction rule are added to the ArchUnit suite when the first slice that
  needs them lands.
- The exact shape of the frontend composition provider is validated by the first frontend slice and recorded
  here if it changes.
- Multi-notary hosting is out of scope (ADR-029): one installation serves one notary.
