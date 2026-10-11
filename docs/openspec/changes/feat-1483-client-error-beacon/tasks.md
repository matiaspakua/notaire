> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5, §6.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1483 exists and is linked to CU77
- [x] 1.2 CU77 documentation exists
- [x] 1.3 Acceptance Criteria are scenarios in `specs/client-error-beacon/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No new ADR (ADR-016 covers the log pipeline)
- [x] 1.6 Claim attempted; `gh` integration cannot add labels/comments (token scope) — noted in PR

## 2. Create branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 `cursor/feat-1483_client-error-beacon-69d3`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases from the spec scenarios
- [x] 3.2 Write `frontend/src/tests/unit/client-error-beacon.test.ts`
- [x] 3.3 Run Vitest and observe failures (`/opt/cursor/artifacts/1483-red.log`)
- [x] 3.4 Every scenario maps to a test (`traceability.md`)

## 4. Implementation

- [x] 4.1 Add `frontend/src/lib/client-error-beacon.ts` (validate, cap, rate-limit, log)
- [x] 4.2 Add `frontend/src/app/api/client-errors/route.ts`
- [x] 4.3 Optional `reportClientError` helper + best-effort hook from `api-client`
- [x] 4.4 Make Vitest green (`/opt/cursor/artifacts/1483-green.log`)

## 5. Update existing tests

- [x] 5.1 Existing frontend unit tests still pass
- [x] 5.2 No backend test changes

## 6. Run regression

- [x] 6.1 `cd frontend && npm test`
- [x] 6.2 `bash workspace/sdlc/validate-sdlc-plan.sh feat-1483-client-error-beacon`
- [x] 6.3 Frontend-scoped preflight (typecheck, eslint, vitest, build) — evidence `/opt/cursor/artifacts/1483-preflight-frontend.log`

## 7. Playwright

- [x] 7.1 n/a — no product UI surface (library + API route only)

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 `docs/200-architecture/207-monitoring/README.md` — beacon + LogQL tip
- [x] 8.2 OpenAPI n/a (Next.js route, not Spring)
- [x] 8.3 CHANGELOG held (serialize)

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; closing commit carries `Closes #1483`
- [ ] 9.2 PR/commit also references epic #1461 without closing it

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open PR targeting `main`
- [ ] 10.2 Hold merge until `check-heavy-ci.sh` exits 0

## 11. Deploy

- [ ] 11.1 Redeploy/restart frontend container so the route is live

## 12. Gate 5 — Smoke and close

- [ ] 12.1 POST once; confirm Loki/Grafana sees `client_error` for `notaire-frontend`
- [ ] 12.2 Close #1483 after merge; leave epic #1461 open until remaining slices done

## Definition of Done

- [ ] Vitest green for beacon scenarios; monitoring README updated; #1483 closable
  without inventing Owner issues or closing epic #1461 early
