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
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail** — `bash scripts/check-agent-rules.sh` (expect FAIL on obsolete `refactoring.md`)
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Extend `scripts/check-agent-rules.sh` with obsolete-marker checks scoped to `.claude/rules/refactoring.md`
- [x] 4.2 Rewrite `.claude/rules/refactoring.md` for Spring Boot 4.1 / Java 21 / PostgreSQL 16 / `com.licensis.notaire` / Next.js / `Dto*` / repository-over-jpa (no Swing target)
- [x] 4.3 Update `docs/300-development/DEVELOPMENT-PLAN.md` so Swing is not named as a target client; point at live status sources
- [x] 4.4 Align SAD phase table (`docs/200-architecture/201-SAD/sad.md`) with Swing removal / Phase 6 reality
- [x] 4.5 Link `README.md` Documentación / status section to the single live sources (`docs/github/README.md`, DEVELOPMENT-PLAN)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/`
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin <branch-name>`
- [x] 10.2 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [x] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [x] 12.2 Verify the rollback path is still available as described in design.md
- [x] 12.3 Close the GitHub Issue, referencing the PR
- [x] 12.4 Archive the change: `openspec archive <change-name>`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [x] PR created, CI green, review approved (Gate 4)
- [x] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [x] `traceability.md` complete from Issue through Release

## Closure note (2026-10-04)

Rules rewrite, DEVELOPMENT-PLAN, SAD phase table and README links are on `main`; `check-agent-rules.sh` rejects obsolete markers. The checklist was ticked at archive time from that evidence.
