> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5, §6.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1509 exists and is linked to CU78
- [x] 1.2 CU78 documentation exists
- [x] 1.3 Acceptance Criteria are scenarios in `specs/infra-rules/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No new ADR (ADR-016 covers SonarQube)
- [x] 1.6 Issue moved to IN PROGRESS (`in-progress` label + claim comment)

## 2. Create branch

- [x] 2.1 `git fetch origin` and branch from `origin/main`
- [x] 2.2 `fix/1509_security_reports_infra_rules`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases from the spec scenarios
- [x] 3.2 Reproduce the two red guards of `infra/tests/test_infra_standalone.py` on `main`
- [x] 3.3 Run it and observe it fail
- [x] 3.4 Every scenario maps to a test (`traceability.md`)

## 4. Implementation

- [x] 4.1 Inline nginx config via compose `configs`; delete the second nginx.conf
- [x] 4.2 Overridable reports mount with the exemption marker; `.env.example` key
- [x] 4.3 Verify live that :8890 serves the index and /reports/
- [x] 4.4 Register the guard in `infra/verify.sh`

## 5. Update existing tests

- [x] 5.1 Existing `infra/tests/test_*.py` still pass

## 6. Run regression

- [ ] 6.1 `bash infra/verify.sh`
- [ ] 6.2 `bash workspace/sdlc/preflight.sh`
- [ ] 6.3 `bash infra/verify.sh` green

## 7. Playwright

- [x] 7.1 Not applicable (no product UI change)

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 `CHANGELOG.md` n/a (local tooling); `infra/.env.example` documents the new key


## 9. Atomic commits

- [ ] 9.1 Conventional Commits; the final commit carries `Closes #1509`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open the PR targeting `main`
- [ ] 10.2 PR is `MERGEABLE`, CI green

## 11. Deploy

- [ ] 11.1 Recreate the security-reports container

## 12. Gate 5 — Smoke and close

- [ ] 12.1 Index and /reports/ answer 200
- [ ] 12.2 Close #1509 after merge and smoke test

## Definition of Done

- [ ] Guards green, guards green and service verified, docs updated
