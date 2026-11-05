> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2003 exists, labeled `status:in-progress`, and linked to CU78
- [x] 1.2 Use Case CU78 documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR. The change records controls that already exist
- [x] 1.6 Issue created with `status:in-progress`. A later label edit returned 403

## 2. Crear branch

- [x] 2.1 `git fetch origin main` on an up-to-date `main`
- [x] 2.2 Branch `cursor/security-assessment-portfolio-6eaf` (cloud agent branch form)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Scenarios: register ids, cadence triggers, stale phrases, CodeQL trigger, site map, STRIDE explorer
- [x] 3.2 Unit tests written in `security/tests/` and `github-page/tests/`
- [x] 3.3 No new Java integration test. No API change
- [x] 3.4 New tests observed failing before the assessment, register, cadence file, and skill existed. The CodeQL assertion failed because `pull_request` is already absent
- [x] 3.5 Each delta scenario maps to a test in `design.md`

## 4. Implementación

- [x] 4.1 Write the assessment, correct the program and threat model, add the YAML register
- [x] 4.2 Add the Security section, the cadence board, the STRIDE explorer, and the factory security-gates page
- [x] 4.3 Add `notaire-cybersec` and `SECURITY-GATES.md`
- [x] 4.4 Point stale issue numbers at #2004–#2011

## 5. Actualizar tests existentes

- [x] 5.1 `test_security_gate_surface.py` no longer expects CodeQL on `pull_request`
- [x] 5.2 Site-map tests expect the Security entry. Assertions were wrong relative to #2001, not weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 No `backend-api` production change. Java suite not re-run
- [x] 6.2 JaCoCo unchanged
- [x] 6.3 `mvn verify` not required for this diff
- [x] 6.4 Bruno unchanged. No API change
- [x] 6.5 No `@Disabled` added

## 7. Ejecutar Playwright

- [x] 7.1 `github-page/e2e/site.spec.ts` includes the Security routes
- [x] 7.2 `cd github-page && npm run build && npx playwright test e2e/site.spec.ts` — 67 passed. `e2e/demo.spec.ts` needs `npm run build:demo` (the frontend export) and was not part of this proof
- [x] 7.3 Product viewports are n/a. The product UI did not change. Portfolio viewports are 375 and 1280
- [x] 7.4 Product `testing/e2e` is n/a — no product UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Assessment, program, tools, threat model, OWASP map, factory security gates
- [x] 8.2 OpenAPI unchanged
- [x] 8.3 `CHANGELOG.md` Unreleased note
- [x] 8.4 Nothing archived to `docs/000-archive/`
- [x] 8.5 Factory doc points at the assessment and does not copy the register
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` was run on this branch. Security gates, backend verify, pages build, pages Playwright, Bruno, database V&V, and `docker compose build` passed. Exit code 1: product Playwright had no browser binary (installed after the run) and `docker compose up` conflicted with the already running `notary-postgres` container. The pull request states that.

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Commit message ends with `Closes #2003` only if the issue should close. This issue stays open until the Owner merges. Use `Refs #2003`
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs recorded after the commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive docs-2003-security-assessment --yes`
- [ ] 10.2 Push the branch
- [ ] 10.3 PR title `[#2003] docs(security): assessment, portfolio explorer, and agent gates`
- [x] 10.4 GitHub product workflows do not run on the pull request (#2001). Local gates are the proof
- [ ] 10.5 Gate 4 is the Owner's review
- [ ] 10.6 Record the PR number after it exists

## 11. Deploy

- [ ] 11.1 The Owner merges. This change does not push to `main`
- [ ] 11.2 `cd.yml` does not publish on a merge to `main`. Pages deploy does
- [ ] 11.3 Record the merge after the Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, open the published `/security/` route
- [ ] 12.2 Revert is safe. No database change
- [ ] 12.3 The Owner closes #2003 after merge
- [ ] 12.4 Record deploy evidence after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: security unit tests, backend verify, pages unit tests, and the pages smoke. Product Playwright and compose smoke did not exit 0 in the same `--full` run (environment).
- [x] Coverage at or above the JaCoCo ratchet floor (unchanged)
- [ ] Playwright E2E green for the portfolio smoke
- [x] Permanent documentation updated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, review ready for the Owner (Gate 4)
- [ ] Merged, Pages deployed, Issue closed (Gate 5) — Owner
- [ ] `traceability.md` complete from Issue through Release
