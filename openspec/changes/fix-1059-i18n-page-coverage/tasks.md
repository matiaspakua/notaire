> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1059 OPEN; CU76
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update pointer at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — ADR-015 exists; pointer update only (no new ADR)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1059 --add-label "in-progress"`) — label ACL may 403 for integration

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/fix-1059-i18n-page-coverage-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Seed/fill OpenSpec change and run `bash scripts/validate-sdlc-plan.sh fix-1059-i18n-page-coverage`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: required namespaces/keys; login leftovers; es/en sync; optional EN titles
- [ ] 3.2 Extend `frontend/src/tests/unit/i18n.test.ts` with required gap-page + login leftover keys **before** adding catalogs — observe fail
- [ ] 3.3 Adjust `login-page.test.tsx` expectations if they pin leftover Spanish literals — observe fail where applicable
- [ ] 3.4 Run them and **observe them fail** — `cd frontend && npx vitest run src/tests/unit/i18n.test.ts`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add/extend `messages/es.json` + `messages/en.json`: `administracion.roles`, `administracion.workflows` (+ editor), `suplencias`, `reportes`, expand `items`, extend `login`, `auditoria.allModules`, shared `common.active`/`inactive` if needed
- [ ] 4.2 Wire `administracion/workflows/page.tsx` and `workflows/[id]/page.tsx` with `useTranslations`
- [ ] 4.3 Wire `suplencias/page.tsx` and `reportes/page.tsx`
- [ ] 4.4 Complete `administracion/roles/page.tsx` remaining hardcoded strings
- [ ] 4.5 Wire `dashboard/items/page.tsx` to expanded `items` namespace; leave redirect stubs untouched
- [ ] 4.6 Wire login leftovers + auditoria `allModules` leftover
- [ ] 4.7 Keep design-system layout/tokens unchanged — translation only

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected (`i18n.test.ts`, `login-page.test.tsx`, TS-0040)
- [ ] 5.2 Update assertions to use translated EN/ES expectations; do not weaken coverage
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — n/a if no backend delta (sanity optional)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a if no backend code
- [ ] 6.3 `mvn verify -pl backend-api` — n/a if no backend code; CI verifies
- [ ] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change)
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Optionally extend `TS-0040-l10n-language-switching-qa.spec.ts` with EN titles on 1–2 gap pages; update `E2E-TEST-MAPPING.md`
- [ ] 7.2 `cd frontend && npx playwright test TS-0040` (or full suite when stack is up)
- [ ] 7.3 Verify affected screens at 320px, 768px and 1024px (layout unchanged)
- [ ] 7.4 n/a only if no UI — this change HAS UI surface (string wiring)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger — n/a
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible i18n coverage
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1059`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1059-i18n-page-coverage-69d3`
- [ ] 10.2 Open draft PR via ManagePullRequest titled `[#1059] fix(frontend): i18n page coverage + login leftovers`, base `main`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Address review feedback; do **not** merge (coordinator/user owns merge)

## 11. Merge y cierre

- [ ] 11.1 Do **not** merge — leave draft PR for review
- [ ] 11.2 After merge (human/foreman): confirm Issue #1059 closed via `Closes #1059`
- [ ] 11.3 Update `traceability.md` with PR URL + head SHA; write `/workspace/implement-1059-status.md`

## 12. Post-merge

- [ ] 12.1 n/a until merge — smoke login + one gap page ES/EN after deploy
- [ ] 12.2 Confirm no regressions in language switcher / TS-0040
- [ ] 12.3 Archive OpenSpec change after merge per project practice

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76 + #1059)
- [x] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor (n/a backend delta)
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5) — merge deferred
- [ ] `traceability.md` complete from Issue through Release
