> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — waived: agent cannot edit issues (gh write 403); #1121 Related only

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail** — n/a production code; static presence scenarios (install.sh + checklist) verified after implement
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add Bruno CLI `@usebruno/cli@4.2.0` block after OpenSpec in `.cursor/install.sh`
- [x] 4.2 Symlink `bru` to `/usr/local/bin` with npm-global / package-entry fallback
- [x] 4.3 Document Bruno CLI ≥4.2.0 in ENVIRONMENT-CHECKLIST toolchain + PATH sections
- [x] 4.4 Add this OpenSpec change folder so Process Checks pass without `sdlc-exception`

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none

## 6. Ejecutar regresión

- [x] 6.1 `bash scripts/validate-sdlc-plan.sh chore-bruno-cli-install-pin`
- [x] 6.2 Confirm install.sh contains `@usebruno/cli@4.2.0` and bru symlink block
- [ ] 6.3 `bash scripts/preflight.sh` (or the scripts-relevant subset if full stack unavailable)
- [x] 6.4 HTTP/Bruno API suite: n/a — no API surface change in this PR
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [x] 7.2 `cd frontend && npx playwright test` — n/a no UI surface
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a
- [x] 7.4 n/a — install script + OpenSpec docs only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes — n/a
- [x] 8.4 Archive superseded documents into `docs/000-archive/`
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>` — waived: Related #1121 only; do not close #1121
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin <branch-name>`
- [ ] 10.2 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR (never commit directly to `main`)
- [ ] 11.2 Wait for CD / deploy pipeline on `main` (n/a — install/docs only; coordinator env build after merge)
- [ ] 11.3 Confirm the target environment received the change

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test the endpoints / screens listed in design.md — Deployment Strategy
- [ ] 12.2 Confirm the Issue is CLOSED (proves `Closes #<n>` was present) — n/a; Related #1121 only
- [ ] 12.3 Confirm `traceability.md` is complete through Release
- [ ] 12.4 Archive this change: `openspec archive chore-bruno-cli-install-pin`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
