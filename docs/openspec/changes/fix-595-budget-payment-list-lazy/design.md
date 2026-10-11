> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #595 inventories explicit EAGER mappings; four are `@OneToMany` collections. The history collection slice shipped in PR #1318. This slice is the next growing fan-out: `Budget.paymentList`. The getter is `@JsonIgnore`; only legacy JPA controllers mutate the in-memory set while holding an `EntityManager`.

## Goals / Non-Goals

**Goals:**

- Stop unbounded payment fan-out when loading budgets.
- Pin the contract with an integration test that fails while the mapping is EAGER.

**Non-Goals:**

- Other EAGER mappings on #595 (`DocumentType.procedureTemplateList`, `Concept.budgetTemplateList`, explicit `@ManyToOne(EAGER)`).
- Archiving sibling OpenSpec packs.
- Changing payment APIs or cascade rules.

## Decisions

- Explicit `FetchType.LAZY` (same pattern as #1318) so intent is visible next to sibling mappings that are already LAZY (`procedureList`, `itemList`).
- No `JOIN FETCH` / `@EntityGraph` added: no REST or report path needs the collection outside the legacy JPA controllers that already open an `EntityManager`.
- Hold `CHANGELOG.md` on this PR to avoid tip conflict with open English work; behavior is still documented in this change pack.

## Riesgos / Trade-offs

- [Risk] A future reader outside a transaction gets `LazyInitializationException` → Mitigation: `open-in-view` is already false; the integration test documents on-demand reads inside a persistence context.
- [Risk] Legacy `BudgetJpaController` / `PaymentJpaController` orphan checks touch the collection → Mitigation: they use their own `EntityManager` and already call `getPaymentList()` while managed; existing payment/budget integration coverage remains the regression net.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Finding a budget | integration | `BudgetPaymentListFetchIntegrationTest` |
| Listing budgets | integration | `BudgetPaymentListFetchIntegrationTest` |
| Payments read on demand | integration | `BudgetPaymentListFetchIntegrationTest` |

- New unit tests (`src/test/java/.../unit/`): none required (mapping contract is persistence-level)
- New integration tests (`src/test/java/.../integration/`): `BudgetPaymentListFetchIntegrationTest` (TDD: observed failing first)
- Coverage impact (JaCoCo ratchet floor; 80% target): neutral / slight gain from new test

## Regression Strategy

- Existing tests affected: none expected to weaken; `BudgetEntityTest` constructs an unsaved budget and only asserts `getPaymentList()` is non-null.
- Full suite command: `mvn verify -pl backend-api`
- HTTP/Bruno API suite: path-skip if no API surface; otherwise `bash integration-test/scripts/test.sh`
- Legacy paths at risk: `jpa.BudgetJpaController`, `jpa.PaymentJpaController` — covered by existing payment/budget integration tests

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: n/a — no UI surface
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a — mapping-only backend change; JSON never serializes `paymentList`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: code-only mapping change
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): load a budget via API; confirm no `LazyInitializationException` in logs; payment flows still work

## Rollback Strategy

- Revert safe: yes — revert the mapping commit
- Database rollback: none needed
- Data written under the new behavior after revert: none (fetch strategy only)
- Blast radius if rollback is delayed: possible `LazyInitializationException` only if a new out-of-session reader appears

## Migration Plan

1. Land Gate 1 + apply on branch `cursor/fix-595_budget-payment-list-lazy-69d3`.
2. Merge via PR with `Refs #595` (slice; issue stays open).
3. Follow-up tip CHANGELOG Fixed entry when lock is free.
4. Later slices: `DocumentType.procedureTemplateList`, then `Concept.budgetTemplateList`.
