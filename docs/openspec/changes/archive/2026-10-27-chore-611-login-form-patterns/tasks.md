> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU84`)
- [x] 1.2 Use Case documentation exists (`docs/100-business/102-use-cases/CU84 - Login.md`)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR not required (follows ADR-011)
- [x] 1.6 Issue claimed via `claim-issue.py` (`status:in-progress`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/611_login-form-patterns`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: FormField wrap, FormActions submit, existing CU84 cases
- [ ] 3.2 Extend `login-page.test.tsx` with pattern-contract assertions
- [ ] 3.3 n/a — no backend integration tests
- [ ] 3.4 Run Vitest and **observe them fail** before implementation
- [ ] 3.5 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementación

- [ ] 4.1 Rebuild login page with `FormContainer` / `FormField` / `FormActions`
- [ ] 4.2 Sentence-case labels; preserve testids and CU84 behaviour

## 5. Actualizar tests existentes

- [ ] 5.1 Keep all existing login-page assertions green
- [ ] 5.2 Do not weaken assertions
- [ ] 5.3 n/a — no obsolete tests

## 6. Ejecutar regresión

- [ ] 6.1 n/a backend — frontend-only change
- [ ] 6.2 n/a JaCoCo
- [ ] 6.3 n/a `mvn verify`
- [ ] 6.4 n/a Bruno
- [ ] 6.5 `bash frontend/verify.sh` green

## 7. Ejecutar Playwright

- [ ] 7.1 Spot-check login-dependent E2E (`TS-0094` or equivalent) if stack available
- [ ] 7.2 Preserve testids so suite stays green
- [ ] 7.3 Spot-check 320 / 768 / 1024 on `/login`
- [ ] 7.4 UI surface present — not n/a

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `FRONTEND-DESIGN-SYSTEM.md` + `CHANGELOG.md`
- [ ] 8.2 n/a OpenAPI
- [ ] 8.3 `CHANGELOG.md` Changed entry
- [ ] 8.4 n/a archive
- [ ] 8.5 No duplication
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` before final push

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits (plan → test → impl → docs)
- [ ] 9.2 Messages reference `Closes #611` on the closing commit / PR body
- [ ] 9.3 No secrets / unrelated skill edits
- [ ] 9.4 Record SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive OpenSpec change in the PR (`openspec archive … --yes`)
- [ ] 10.2 Push branch
- [ ] 10.3 Open PR `[#611] chore(frontend): login uses FormContainer patterns`
- [ ] 10.4 Wait for required workflows
- [ ] 10.5 Gate 4 — CI green + review

## 11. Deploy

- [ ] 11.1 Deploy with normal release train

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke `/login` after deploy
- [ ] 12.2 Confirm #611 closed via `Closes #611`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
