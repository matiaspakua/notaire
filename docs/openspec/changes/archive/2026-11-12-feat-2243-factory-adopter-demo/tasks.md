> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [ ] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` (SKIPPED if issues:write 403; honest continue per FLEET-IDENTITY)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Write the unit tests for every scenario in the delta spec
- [ ] 3.3 Write the integration tests where applicable (kit Repo CLI helpers)
- [ ] 3.4 Run them and **observe them fail** — `python3 -m unittest` under `framework/ai-sdlc/tests` (n/a mvn — kit-only)
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add `aisdlc/demo.py` seed helper (catalogs + workflow governance to approved)
- [ ] 4.2 Wire CLI `aisdlc demo seed [--from-stages] [--workflow ID]`
- [ ] 4.3 Seed Notaire adopter catalogs (skills + approved workflow from Foreman stages)
- [ ] 4.4 Document kit + Notaire demo path (commands + UI entry); FACTORY-UI-SPEC Phase E note
- [ ] 4.5 Kit independence / CU76 doctor-validate-report stay green

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `bash framework/ai-sdlc/verify.sh` — kit unit suite (n/a mvn backend for this tip)
- [ ] 6.2 Coverage: kit unittest green; JaCoCo n/a (no backend-api change)
- [ ] 6.3 `bash workspace/sdlc/preflight.sh --full` — full local gate before PR
- [ ] 6.4 HTTP/Bruno API suite: n/a — no product API change
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — kit demo/docs only; no Notaire product UI surface
- [ ] 7.2 n/a — no Playwright product specs in this tip
- [ ] 7.3 n/a — no product viewports
- [ ] 7.4 n/a — Phase E is kit + adopter catalogs/docs only

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 No product Swagger change
- [ ] 8.3 `CHANGELOG.md` Unreleased: omit (fence / release-please / #2271)
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Completing commit/PR body includes `Closes #2243`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Workers never merge; Foreman / Owner merges

## 11. Cerrar Issue

- [ ] 11.1 Completing commit / PR uses `Closes #2243` only (no sibling closes)
- [ ] 11.2 Confirm acceptance criteria satisfied in PR body
- [ ] 11.3 Handoff `asli-84-handoff.md` for coordinator

## 12. Actualizar documentación de Use Case (si aplica)

- [ ] 12.1 CU76 remains the governing Use Case; no CU text change required beyond demo evidence
- [ ] 12.2 Link permanent FACTORY-ADOPTER-DEMO docs from framework index if needed

## Definition of Done

- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a kit demo — kit unittests cover)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
