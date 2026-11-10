> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #2119: `PaymentsPage.updateQuery` copies `searchParams`, sets/deletes one
key, and `replace`s. The unit test freezes one `URLSearchParams` for the whole
render and never applies `replace`, so typing `to` after `from` incorrectly
expects a query that drops `from`. Product code is correct per the issue.

## Goals / Non-Goals

**Goals:**
- Navigation mock feeds each `replace` URL query string back into `searchParams`.
- Assertions expect both days together, then `to` alone after clearing `from`.
- Vitest on `payments-page-actions.test.tsx` passes with the honest mock.

**Non-Goals:**
- Changing `updateQuery` or the payments page UI.
- Inventory generation (#2117).
- Reopening #2114.

## Decisions

- **Fix the test, not the page** — Issue evidence shows `updateQuery` preserves
  keys; the mock is the defect. Prefer correcting mock + expectations over a
  product workaround that would mask the bug.
- **Apply `replace` into `searchParams`** — Parse the path/`?` query from the
  first `replace` argument and assign a new `URLSearchParams`, matching Next's
  feedback loop without a full router.
- **Keep other cases** — Budget filter address tests already call `replace`
  once per action from an empty query; an updating mock stays compatible.

## Riesgos / Trade-offs

- [Mock updates without re-render] → Assertions only check `replace` call args;
  React does not re-read `useSearchParams` mid-handler for the same keystroke
  sequence under test. Risk accepted: we mirror the product's copy-from-current
  params semantics for successive events.
- [URLSearchParams key order] → Prefer asserting the full string
  `from=…&to=…` as produced by sequential `set` calls; if order differs, assert
  via parsed params equality instead of weakening the requirement.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| typing both days keeps both keys in the address | unit | `frontend/src/tests/unit/payments-page-actions.test.tsx` |
| clearing the start day leaves the end day | unit | same |
| exceeded balance / invalid range (existing) | unit | same (regression) |

- New unit tests: extend the existing "keeps the typed days…" case (no new file).
- New integration tests: n/a — frontend unit only.
- Coverage impact: no product lines changed; Vitest floor held.

## Regression Strategy

- Existing tests affected: `payments-page-actions.test.tsx` date-range address
  case and any case that relies on frozen `searchParams` after `replace`
  (budget clear/set still starts from empty and remains valid).
- Full suite command: `bash workspace/sdlc/preflight.sh --full` (includes Vitest).
- HTTP/Bruno API suite: n/a for this test-only change (still run via preflight when required).
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface change; unit-test mock/assertion fix only. Existing CU47
E2E coverage for payments date filters is unchanged.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: standard frontend image via existing CD
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): payments page still filters by date range in URL

## Rollback Strategy

- Revert safe: yes — test-only commit; revert restores the dishonest mock
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: low (tests only)

## Migration Plan

1. Observe red with corrected expectations against frozen mock (or green after
   mock fix first if expectations alone already fail).
2. Wire `replace` → `searchParams` and green the suite.
3. Preflight `--full`, PR, archive change.

## Open Questions

None.
