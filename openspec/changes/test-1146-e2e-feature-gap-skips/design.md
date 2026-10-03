> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#1066 already annotated intentional skips in TS-0014/16/17/20 with `#1146` and
added a Vitest citation check in `frontend/src/tests/unit/e2e-test-reliability.test.ts`.
Live inventory on updated `main` (2026-10-03):

| Suite | Static `test.skip` | Owning CUs |
|-------|--------------------|------------|
| TS-0014 | 2 | CU15-GW03, CU47-GW01 |
| TS-0016 | 3 | CU23-GW01, CU48-GW01, CU48-GW02 (CU21 unskipped #1057) |
| TS-0017 | 2 | CU59-GW01, CU59-GW02 |
| TS-0020 | 7 | CU24, CU25, CU39, CU42, CU62, CU65, CU66 |
| **Total** | **14** | |

`E2E-TEST-MAPPING.md` still says “All 11 skipped tests” and collapses rows —
Gate 3 docs drift.

## Goals / Non-Goals

**Goals:**

- Hygiene check requires `#\d+` on every static skip in the four suites and
  locks the inventory count at 14 until product work removes skips deliberately.
- Mapping + CU76 + CHANGELOG accurately describe ownership and tracker role.
- #1146 closable as tracker hygiene without shipping product UI.

**Non-Goals:**

- Implementing missing UI (filters, detail buttons, search, plantillas, etc.).
- Unskipping scenarios without real assertions.
- Changing Playwright config / retries (#1066).
- Touching `local-ai/`.

## Decisions

1. **Strengthen existing Vitest file rather than a new suite file**
   - Why: #1066 already owns `e2e-test-reliability.test.ts`; keep Gate 2
     checks co-located. Extend the feature-gap test to assert per-file counts
     and `#\d+` on each skip title.
   - Alternative: separate `e2e-feature-gap-skips.test.ts` — rejected as
     unnecessary file churn for one assertion block.

2. **#1146 is citation tracker; owning CUs own product delivery**
   - Why: issue body and acceptance criteria. Mapping rows name owning CU and
     “product tracking” separately from the #1146 citation.

3. **Do not unskip in this PR**
   - Why: AC and coordinator decisions — false-green risk if asserts are faked.

4. **Exact count of 14 in the hygiene check**
   - Why: prevents silent deletion of debt or under-documentation; when a
     product PR unskips, it must update the Vitest count and mapping together.

## Riesgos / Trade-offs

- [Exact count churn] → Mitigate: document in mapping that count updates are
  required when unskipping; hygiene test failure message names the file.
- [Product issues not filed per CU] → #1146 remains the citation until product
  issues exist; mapping notes owning CU for follow-up filing.

## Testing Strategy (Gate 2)

- **Level**: Vitest unit (static file read) — no Playwright stack required for
  the hygiene gate.
- **TDD**: temporarily break a skip title / count expectation, observe fail,
  restore and green.
- **Command**: `cd frontend && npx vitest run src/tests/unit/e2e-test-reliability.test.ts`

## Regression Strategy

- Existing tests affected: `e2e-test-reliability.test.ts` (extended assertions).
- Full suite command: n/a backend code — run frontend Vitest unit for this
  change; `bash scripts/preflight.sh` before push.
- HTTP/Bruno API suite: n/a — no API change.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface. This change only documents and statically checks existing
intentional `test.skip` debt; it does not add or alter Playwright scenarios.
Do not unskip without product UI in a separate CU-owned change.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: docs + Vitest only; no runtime deploy coupling
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): Vitest hygiene green on CI; mapping shows 14

## Rollback Strategy

- Revert safe: yes — pure docs + static test assertions
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none (no production behavior)

## Migration Plan

1. Gate 1 artifacts + validate.
2. Tighten Vitest hygiene (TDD red → green).
3. Sync E2E-TEST-MAPPING + CU76 + CHANGELOG.
4. Preflight, push, draft PR.

## Open Questions

None — decisions supplied by coordinator assignment.
