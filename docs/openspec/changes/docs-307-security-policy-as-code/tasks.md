> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #307 exists and is linked to CU78
- [x] 1.2 Use Case CU78 exists; this change adds a pointer to the program
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR not required — tool choice is in `SECURITY-TOOLS.md`, no new architecture
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit` is not available to this agent)

## 2. Crear branch

- [x] 2.1 `git fetch origin main` and fast-forward local `main`
- [x] 2.2 Branch `cursor/security-policy-307-81d6` (cloud-agent template; see exceptions)
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: policy shape, planted secrets, placeholders, clean tree, CI wiring, dashboard, lockout alert, threat-model markers
- [x] 3.2 Write `security/tests/test_security_policy.py`
- [x] 3.3 Integration tests: n/a — no API or database change
- [x] 3.4 Run `python3 security/tests/test_security_policy.py` and observe failure (7 errors, 2 failures) before the scanner, policy, docs, and dashboard existed
- [x] 3.5 Every `#### Scenario:` maps to a test in design.md

## 4. Implementación

- [x] 4.1 Add `security/policy/security-policy.yml` and `security/scan_secrets.py`
- [x] 4.2 Add the Sec by Design docs, system threat model, toolchain, monitoring, OWASP map, and PlantUML trust boundaries
- [x] 4.3 Fail the CI security job on the secret scan
- [x] 4.4 Add Grafana `notaire-security` and `LoginLockoutSpike`
- [x] 4.5 Correct SR-06, SAD §8.1, and the security README
- [x] 4.6 Point CU78, the fleet architecture, and `security/verify.sh` at the gate

## 5. Actualizar tests existentes

- [x] 5.1 Grafana status tests keep their specialist set; the new link is additive
- [x] 5.2 No assertion was weakened
- [x] 5.3 No test was removed

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a, no backend code
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a, no backend code
- [x] 6.3 `mvn verify -pl backend-api` — n/a, no backend code
- [x] 6.4 Bruno suite — n/a, no API change
- [x] 6.5 No `@Disabled` tests added
- [x] 6.6 `bash security/verify.sh` and the infra dashboard/alert tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no Next.js product screen
- [x] 7.3 n/a — no layout change
- [x] 7.4 n/a — no UI surface. Grafana JSON and a documentation card are not product screens

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md
- [x] 8.2 OpenAPI unchanged
- [x] 8.3 `CHANGELOG.md` Unreleased security entry
- [x] 8.4 Nothing archived; the auth threat model stays, with SR-06 corrected
- [x] 8.5 Program is the index; the proposal does not duplicate the register
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` — run before push

## 9. Commits atómicos

- [ ] 9.1 Commit in Conventional Commits format
- [ ] 9.2 Commit message ends with `Closes #307`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHA in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/security-policy-307-81d6`
- [ ] 10.2 Open the PR referencing Issue #307 and CU78
- [ ] 10.3 Required workflows run on the PR
- [ ] 10.4 Gate 4 waits on CI
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 CD publishes images only if product paths require it; this change's smoke is the scanner
- [ ] 11.3 Record the merge commit when it exists

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, `python3 security/scan_secrets.py` on `main` exits 0
- [ ] 12.2 Revert remains safe (no schema)
- [ ] 12.3 Close issue #307 when the PR merges
- [ ] 12.4 Archive the change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E — security and infra guards only; Java and Playwright are n/a
- [x] Coverage at or above the JaCoCo ratchet floor — unchanged, no Java edit
- [x] Playwright E2E green for UI changes — n/a, no UI surface
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
