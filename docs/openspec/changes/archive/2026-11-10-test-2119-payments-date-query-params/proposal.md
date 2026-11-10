# Fix payments date-query unit test so both days stay in the address

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2119 |
| Use Case | CU47 – Payment consultation by budget and date range |
| Branch | `test/2119_payments-date-query-params` |
| Gate 1 status | ready |

## Objetivo

`payments-page-actions.test.tsx` freezes `useSearchParams` and then expects each
keystroke to replace the whole query. The page's `updateQuery` copies current
search params, so typing both days must keep `from` and `to` together. The page
is fine; the test mock and expectations are wrong.

## What Changes

- Make the Next.js navigation mock apply each `router.replace` URL back into
  `useSearchParams` for the next call.
- Correct the date-range address assertions: both days →
  `?from=2026-02-01&to=2026-04-30`; clearing `from` → `?to=2026-04-30`.
- Do **not** change `PaymentsPage.updateQuery` unless a correctly mocked failing
  test shows the page drops a key.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Inclusive date bounds stay in the URL together when both are set | CU47; #1557 / #2114 | Made explicit in unit expectations |
| Clearing one bound must leave the other in the address | CU47; #2119 AC | Made explicit |
| Unit mocks that drive the URL must mirror `updateQuery`'s copy-then-set behavior | CU76 / CU47 test honesty | New (test contract) |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `payments-page-tests`: Date-range address updates must assert both query keys
  when both days are typed, and must keep the remaining key when one is cleared;
  the navigation mock must feed `replace` back into `useSearchParams`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | Unit test mock + assertions in `payments-page-actions.test.tsx` only |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

## Documentation Impact

| Document | Action |
|----------|--------|
| `docs/openspec/specs/payments-page-tests/spec.md` | Sync via archive of this delta |
| `CHANGELOG.md` | Note under Unreleased (test fix; no user-visible product change) |
| CU47 permanent use-case doc | No content change (behavior already correct) |
