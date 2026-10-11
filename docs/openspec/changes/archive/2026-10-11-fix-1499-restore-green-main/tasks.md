> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5, §6.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1499 exists and is linked to CU77
- [x] 1.2 CU77 documentation exists
- [x] 1.3 Acceptance Criteria are scenarios in `specs/green-main/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No new ADR (ADR-016 covers SonarQube)
- [x] 1.6 Issue moved to IN PROGRESS (`in-progress` label + claim comment)

## 2. Create branch

- [x] 2.1 `git fetch origin` and branch from `origin/main`
- [x] 2.2 `fix/1499_restore_green_main`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases from the spec scenarios
- [x] 3.2 Write the gitignore guard, reports page test; use the existing Flyway, reachability and architecture tests
- [x] 3.3 Run it and observe it fail
- [x] 3.4 Every scenario maps to a test (`traceability.md`)

## 4. Implementation

- [x] 4.1 `.gitignore` anchor and the restored reports page
- [x] 4.2 Reachability allowlist and test parameter name
- [x] 4.3 Controller baseline names; V45 amendment
- [x] 4.4 Register the guard in `infra/verify.sh`

## 5. Update existing tests

- [x] 5.1 Existing `infra/tests/test_*.py` still pass

## 6. Run regression

- [ ] 6.1 `bash infra/verify.sh`
- [ ] 6.2 `bash workspace/sdlc/preflight.sh`
- [ ] 6.3 Full backend and frontend suites on the fix branch

## 7. Playwright

- [x] 7.1 Not applicable (no product UI change)

## 8. Gate 3 — Update permanent documentation

- [ ] 8.1 `CHANGELOG.md` (V45 note)

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; the final commit carries `Closes #1499`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open the PR targeting `main`
- [ ] 10.2 PR is `MERGEABLE`, CI green

## 11. Deploy

- [ ] 11.1 Databases that applied the old V45 run flyway repair

## 12. Gate 5 — Smoke and close

- [ ] 12.1 Fresh stack starts; /dashboard/reports loads
- [ ] 12.2 Close #1499 after merge and smoke test

## Definition of Done

- [ ] Guards green, all gates green on the fix branch, docs updated
