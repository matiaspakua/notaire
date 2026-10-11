> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5, §6.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1413 exists and is linked to CU77
- [x] 1.2 CU77 documentation exists
- [x] 1.3 Acceptance Criteria are scenarios in `specs/local-tools-no-login/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 ADR-016 amended (no new ADR file)
- [x] 1.6 Issue already labeled `in-progress`

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 `cursor/1413-local-tools-no-login-69d3`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases from the spec scenarios
- [x] 3.2 Write `infra/tests/test_local_tools_no_login.py`
- [x] 3.3 Run it and observe it fail
- [x] 3.4 Every scenario maps to a test (`traceability.md`)

## 4. Implementación

- [x] 4.1 Update `grafana.ini` for anonymous Admin and no login form
- [x] 4.2 Bind Grafana/Sonar ports to `127.0.0.1`; set `SONAR_FORCEAUTHENTICATION=false`
- [x] 4.3 Register the guard in `infra/verify.sh`

## 5. Update existing tests

- [x] 5.1 Existing infra guards still pass

## 6. Run regression

- [x] 6.1 `bash infra/verify.sh`
- [x] 6.2 `bash workspace/sdlc/validate-sdlc-plan.sh chore-1413-local-tools-no-login`
- [ ] 6.3 Preflight as far as the environment allows

## 7. Playwright

- [x] 7.1 n/a — no product UI change

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 ADR-016, `infra/docs/CONFIGURATION.md`, `OPERATION.md`, `MODULE.md`, `start-infra.sh`
- [x] 8.2 Monitoring README pointer if needed
- [x] 8.3 CHANGELOG deferred (#1518 lock)

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; final commit carries `Closes #1413`

## 10. Pull Request and CI validation

- [ ] 10.1 Push and open PR targeting `main`
- [ ] 10.2 Hold merge until heavy-CI script exits 0; do not contend with #1518 Playwright

## 11. Deploy

- [ ] 11.1 Recreate Grafana and SonarQube containers locally

## 12. Gate 5 — Smoke and close

- [ ] 12.1 Open Grafana and Sonar without login on localhost
- [ ] 12.2 Close #1413 after merge and smoke

## Definition of Done

- [ ] Guards green, local no-login + loopback, prod unchanged, docs updated
