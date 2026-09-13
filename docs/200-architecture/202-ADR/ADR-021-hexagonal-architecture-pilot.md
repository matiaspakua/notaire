# ADR-021: Hexagonal Architecture Pilot (Payment Slice)

## Status

Accepted — 2026-09-13 (issue #984)

**Scope of this decision: the payment/budget financial slice only.** The rest of
`backend-api` keeps the layered `api → service → repository` structure described
in ADR-002 until a follow-up decision extends or rolls back this pilot.

## Context

`backend-api` inherits a layered structure from the Swing monolith migration:
`api` (REST controllers) → `service` (business logic) → `repository` / `jpa`
(data access). Two structural problems keep showing up in the financial code:

1. **Business rules are tied to JPA and to Spring MVC.** `PaymentService` operated
   directly on `Payment` and `Budget` entities, so the overpayment rule
   (issue #848) and the discount/surcharge arithmetic (issue #822) could only be
   tested with a persistence context or a mocked repository. The old
   `PaymentController` bound the JPA `Payment` entity as a `@RequestBody`, which
   let persistence concerns leak all the way to the wire.
2. **Rules get duplicated.** The same "pending balance" computation existed in
   `PaymentService`, `BudgetResumenService` and the two management summary
   services, with slightly different null handling in each.

The financial workflow (CU15 "Procesar Pago", CU47 "Consultar Estado de Pago") is
the highest-value place to fix this: it has the densest business rules, it is
fully covered by existing tests, and its REST contract is consumed by the
dashboard, so any regression is immediately visible.

## Decision

Restructure the payment slice — and only the payment slice — as
**Hexagonal Architecture (Ports & Adapters)**, using a strangler refactor that
keeps the REST contract byte-for-byte identical.

### Package layout

Per `.claude/skills/hexagonal-arch/SKILL.md`:

| Package | Contents | Allowed dependencies |
|---------|----------|----------------------|
| `domain.payment` | `PaymentDetails`, `PaymentStatus`, `BudgetCharges`, `ChargeLine` | JDK only — no Spring, no JPA |
| `application.port.in.payment` | `ProcessPaymentUseCase`, `EditPaymentUseCase`, `DeletePaymentUseCase`, `QueryPaymentsUseCase`, `GetPaymentStatusUseCase`, `GetBudgetSummaryUseCase` + their command/result records | `domain` |
| `application.port.out.payment` | `PaymentRepositoryPort`, `BudgetLookupPort` + `NewPayment`, `PaymentChanges`, `BudgetDescriptor` | `domain` |
| `application.usecase.payment` | `ProcessPaymentService`, `EditPaymentService`, `DeletePaymentService`, `PaymentQueryService`, `PaymentStatusService`, `BudgetSummaryService` | ports + `domain` |
| `adapter.in.web.payment` | `PaymentController`, `PaymentWebMapper` | inbound ports + DTOs |
| `adapter.out.persistence.payment` | `PaymentPersistenceAdapter`, `BudgetLookupAdapter` | outbound ports + JPA entities/repositories |

Dependencies point inward only. The JPA `Payment` and `Budget` entities are now
touched in exactly two classes — the two outbound adapters.

### Composition

Wiring stays Spring: use cases are `@Service` beans, adapters are `@Repository`
beans, and everything is constructor-injected. No hand-rolled container and no
separate composition-root class, which would add ceremony without buying anything
in a Spring Boot application.

### Transaction boundary

`@Transactional` moved from the controller to the use cases. The transaction now
starts where the business operation starts, not where HTTP arrives.

## Options Considered

- **Leave the layered structure and only add tests.** Rejected: it does not remove
  the JPA entity from the request body, does not make the business rules testable
  without mocks, and leaves the duplicated pending-balance logic in place.
- **Big-bang hexagonal rewrite of `backend-api`.** Rejected: ~40 controllers and
  ~60 services, no way to validate the port design before committing to it, and an
  unreviewable diff. Contradicts the strangler approach used throughout this
  migration (ADR-001).
- **Hexagonal for one vertical slice (chosen).** Bounded, reversible, and it lets
  the port design be validated against two independent implementations before any
  other slice adopts it.
- **Keep a deprecated `PaymentService` facade delegating to the use cases.**
  Rejected: a facade that nothing needs is dead code, and keeping it invites new
  callers to bypass the ports. The two remaining consumers
  (`ManagementArchiveDebtService`, `ManagementResumenFinancieroService`) were
  rewired to the inbound ports instead, which is a two-line change each.

## Trade-off Analysis

**What this buys.** Business rules become testable with no framework at all:
`BudgetChargesTest` and the use-case tests run against in-memory port fakes, which
is why `ProcessPaymentServiceTest` can cover the overpayment rule without a
persistence context. The port design is validated by having two implementations of
each outbound port (the JPA adapter and an in-memory fake), which is the cheapest
way to catch a port that is really just a repository in disguise.

**What this costs.** More types for the same behaviour: six use-case classes and
two ports where there was one `PaymentService`. Two mapping hops
(entity → domain → DTO) instead of one. This is the price of the isolation above,
and it is only justified where the rules are dense — which is why the pilot is
scoped to one slice rather than applied by default.

**What is deliberately not hexagonal.** `BudgetCharges.total()` reproduces the
legacy arithmetic exactly, including compounding percentage charges over the
running total. `BudgetSummaryService` derives the total as
`pendingBalance + totalPaid` rather than recomputing it, so the floating-point
result is bit-identical to the previous implementation. A refactor must not
quietly change financial numbers; correcting that arithmetic, if it is wrong, is a
separate issue with its own use case.

## Consequences

### Easier

- Unit-testing financial rules without Spring, JPA or mocks.
- Adding a second driving adapter (batch job, CLI, message consumer) — it depends
  on the inbound ports, not on the controller.
- Swapping persistence for the payment slice without touching business rules.

### Harder

- Navigating the slice: six files where a reader used to open one service.
- Mixed conventions in one codebase until the pilot is extended or rolled back.
  Anyone touching `service/` must check whether the collaborator they want already
  exists as a port.

### Needs follow-up

- **`PaymentWebMapper` is public as a transitional seam.** `BudgetController` still
  lives in the legacy `api` package while it consumes `GetBudgetSummaryUseCase`.
  The mapper goes back to package-private when the budget slice is migrated.
- **`ManagementResumenFinancieroService` and `ManagementArchiveDebtService` remain
  layered services** that consume payment inbound ports. They are the natural
  candidates for the next slice.
- **Decide whether to extend the pattern.** The pilot should be reviewed after one
  more slice. If the extra indirection is not paying for itself outside dense
  business-rule code, keep it confined to financial workflows rather than adopting
  it project-wide.

## Implementation Details

### REST contract is unchanged

Every URL, request payload, response payload and status code is identical to the
pre-refactor controller, including the details that look like bugs:

- `DELETE /api/v1/pagos/{id}` returns **200**, not 204.
- An overpayment returns **409** with an empty body
  (`PendingBalanceExceededException`).
- `PaymentStatus` serialises as `NoPayments`, `PARTIAL`, `PAID` — mixed case,
  because those constants are the published contract.

The one internal change at the edge: `PUT /api/v1/pagos/{id}` binds a
`PaymentUpdateRequest` record instead of the JPA `Payment` entity. The accepted
JSON fields are the same; the entity simply no longer leaks into the API layer.

### Audit coverage

`AuditAspect`'s pointcut was package-scoped to `com.licensis.notaire.api..*Controller`.
Moving a controller out of that package would have silently dropped it from the
business audit trail (ADR-013) with no failing test. The pointcut now matches both
`api..*Controller` and `adapter.in.web..*Controller`, and
`AuditPointcutCoverageTest` pins this location-agnostically so the next controller
move cannot regress it.

### Tests

The existing suites were retargeted rather than rewritten, so they act as
characterization tests of the refactor — same `@DisplayName`s, same assertions,
same exception messages, none weakened:

| Before | After | Drives |
|--------|-------|--------|
| `unit/PaymentServiceTest` | `unit/PaymentUseCaseTest` | use cases over real adapters, mocked repositories |
| `service/unit/PaymentServiceTest` | `adapter/out/persistence/payment/PaymentPersistenceAdapterTest` | the outbound adapters, mocked repositories |
| `unit/PaymentControllerTest` | `adapter/in/web/payment/PaymentControllerTest` | the controller over mocked inbound ports |
| `unit/BudgetResumenServiceTest` | `unit/BudgetSummaryDtoTest` | `BudgetSummaryService` + web mapping to `DtoBudgetResumen` |
| `integration/PaymentServiceIntegrationTest` | `integration/PaymentUseCaseIntegrationTest` | use-case beans end-to-end over real JPA adapters |

New tests added by the pilot: `domain/payment/BudgetChargesTest` (framework-free
domain arithmetic) and `application/usecase/payment/*Test` (use cases over
in-memory port fakes `InMemoryPaymentRepository` / `InMemoryBudgetLookup`).

### Deleted

`service/PaymentService`, `service/StatusPayment`, `service/BudgetResumenService`,
`service/mappers/PaymentMapper`, `api/PaymentController` — all fully superseded,
removed rather than deprecated.

## Related ADRs

- [ADR-001](ADR-001-microservices-architecture.md) — three-tier architecture and the strangler migration approach
- [ADR-002](ADR-002-module-structure.md) — module and package structure this pilot deviates from, for one slice
- [ADR-006](ADR-006-testing-strategy.md) — testing strategy the retargeted suites follow
- [ADR-010](ADR-010-error-handling.md) — error-response contract preserved by the inbound adapter
- [ADR-013](ADR-013-audit-trail.md) — audit aspect whose pointcut had to follow the controller
