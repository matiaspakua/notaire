> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

People, budgets, deeds and managements already return a page. Payments still returned the table. The screen already has a pager component used by people.

## Goals / Non-Goals

**Goals:**

- One page of payments, default 20, sort `idPayment,desc`.
- Unknown sort properties fall back to `idPayment`.
- The list screen uses the pager. Budget and two-date filters do not.

**Non-Goals:**

- Paging `/payments/budget/{id}` or `/payments/date`.

## Decisions

The controller takes Spring `Pageable`, as the other paged lists do. The port takes `page`, `size` and `sort` so the use case stays free of Spring Data. Allowed sort fields are `idPayment`, `date`, `amount` and `paymentMethod`.

## Riesgos / Trade-offs

- [A one-sided date filter now narrows the current page only] → both dates still go to `GET /payments/date`.
- [Bruno expected an array] → it reads `content`.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Default request returns `content` and `totalElements` | unit | `PaymentControllerTest` |
| The list screen requests `page=0&size=20&sort=idPayment,desc` | unit | `payments-page-actions.test.tsx` |
| The created payment is inside `content` | API | `payments/02-get-all.yml` |

TDD: the controller test did not compile until `findPage` existed, then returned 500 until MockMvc knew how to bind `Pageable`.

## Regression Strategy

- Existing payment controller, use-case and page tests.
- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

Existing `TS-0014-payments-workflow.spec.ts` creates a payment and reads the list. Newest-first keeps that payment on page 1.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full`

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: the payments list page

## Rollback Strategy

- Revert safe: yes
- Database rollback: none

## Migration Plan

One pull request. Further lists are later slices of #596.

## Open Questions

None.
