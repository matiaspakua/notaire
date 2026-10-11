# Budget payment collection is fetched lazily (slice of #595)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #595 |
| Use Case | CU74 – Performance and Caching Strategy |
| Branch | `cursor/fix-595_budget-payment-list-lazy-69d3` |
| Gate 1 status | passed |

## Objetivo

`Budget.paymentList` is an unbounded EAGER `@OneToMany`: loading any budget pulls every payment for that budget. Payments grow with collections over the life of a matter. Make the collection lazy so it loads only when a caller reads it inside a persistence context. The prior #595 slice (`ManagementStatus.historyList`, PR #1318) already shipped; the remaining EAGER `@OneToMany` collections stay on #595.

## What Changes

- `Budget.paymentList` changes from `FetchType.EAGER` to `FetchType.LAZY`.
- `BudgetPaymentListFetchIntegrationTest` pins that finding or listing budgets does not initialize the collection and that it remains readable inside a persistence context.
- CHANGELOG entry is held on this PR (tip conflict risk with open English PRs); add under Fixed when the lock is claimed or on a follow-up tip commit.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Loading a budget does not load its payment collection | #595, CU74 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `jpa-fetch-strategy`: JPA associations that grow with usage are not fetched eagerly (add lazy budget payments; history slice already specified in living `fix-595-management-status-history-lazy`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `Budget` fetch mapping + integration test |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Budget.paymentList` (fetch strategy only); Flyway: none
- Endpoints: none (getter is `@JsonIgnore` / `@XmlTransient`)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (`spring.jpa.open-in-view=false` already)
- Dependencies: none
- BREAKING for API clients: no

### Architecture review

No architecture change. Readers of the collection already run inside a persistence context: legacy `BudgetJpaController` / `PaymentJpaController` use their own `EntityManager`. The getter is `@JsonIgnore`, so JSON serialization never touches it. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry — **held** on this PR; claim when tip CHANGELOG lock is free of competing open PRs |

## Out of Scope

- Remaining EAGER `@OneToMany` collections on #595: `DocumentType.procedureTemplateList`, `Concept.budgetTemplateList` (later one-collection-per-PR slices).
- Explicit `@ManyToOne(EAGER)` mappings (default JPA behavior; not this slice).
- Archiving living shipped packs (`fix-595-management-status-history-lazy`, `fix-596-*`, `fix-655-*`) — peer 5 hygiene, not this change.
