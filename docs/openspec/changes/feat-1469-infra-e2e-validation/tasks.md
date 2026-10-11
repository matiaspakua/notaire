> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5, §6.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1469 exists and is linked to CU77
- [x] 1.2 CU77 documentation exists
- [x] 1.3 Acceptance Criteria are scenarios in `specs/infra-signal-validation/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No new ADR (ADR-016 covers SonarQube)
- [x] 1.6 Issue moved to IN PROGRESS (`in-progress` label + claim comment)

## 2. Create branch

- [x] 2.1 `git fetch origin` and branch from `origin/main`
- [x] 2.2 `feat/1469_infra_e2e_validation`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases from the spec scenarios
- [ ] 3.2 Write `infra/tests/test_verify_signals.py`
- [ ] 3.3 Run it and observe it fail
- [ ] 3.4 Every scenario maps to a test (`traceability.md`)

## 4. Implementation

- [ ] 4.1 `infra/scripts/verify_signals.py`
- [ ] 4.2 `infra/scripts/verify-signals.sh` with `--with-e2e`
- [ ] 4.3 Register the test in `infra/verify.sh`
- [ ] 4.4 Register the guard in `infra/verify.sh`

## 5. Update existing tests

- [ ] 5.1 Existing `infra/tests/test_*.py` still pass

## 6. Run regression

- [ ] 6.1 `bash infra/verify.sh`
- [ ] 6.2 `bash workspace/sdlc/preflight.sh`
- [ ] 6.3 Live run on the local stack including the Playwright suite

## 7. Playwright

- [x] 7.1 Not applicable (no product UI change)

## 8. Gate 3 — Update permanent documentation

- [ ] 8.1 `infra/docs/OPERATION.md`
- [ ] 8.2 `infra/docs/DEFINITION.md`
- [ ] 8.3 `CHANGELOG.md` n/a

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; the final commit carries `Closes #1469`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open the PR targeting `main`
- [ ] 10.2 PR is `MERGEABLE`, CI green

## 11. Deploy

- [ ] 11.1 Run the validation on the local stack

## 12. Gate 5 — Smoke and close

- [ ] 12.1 All signals reported on the live stack
- [ ] 12.2 Close #1469 after merge and smoke test

## Definition of Done

- [ ] Guards green, live validation run recorded, docs updated
