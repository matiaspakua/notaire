> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — kit ADR `framework/ai-sdlc/docs/adr/ADR-006-dashboard-contract-client.md` (Proposed: the UI host is the Owner's call)
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` (uses `status:in-progress`; on `addLabelsToLabelable` / `issues: write` 403 print honest SKIPPED and continue — never fake in-progress; see `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`) — the Issue carries the branch; WIP label step left to the Foreman

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — branch continues `feat/2381_factory_dashboard` (PR #2383) after merging `origin/main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=<NewTestClass>` — kit `python3 -m pytest tests/test_coach.py` (8 failed against HEAD in a throwaway worktree), dashboard `pnpm test` failed before `app.ts`; no backend change
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Multi-token serve auth with per-token actor (kit)
- [x] 4.2 Coach module: CLI and GET /factory/coach (kit)
- [x] 4.3 Dashboard server: workspaces, sessions, kit and RepoLens clients, no file writes, no demo data
- [x] 4.4 Dashboard UI: workspace switcher, sign-in, honest runs, catalogs, knowledge, coach, design tokens, states
- [x] 4.5 Move RepoLens to framework/repolens and update the wiring
- [x] 4.6 ADR, README, SESSION-FEATURES review, verify, commit, push

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration — n/a for the backend (unchanged); the pre-push preflight ran `mvn verify` green on this branch
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor — n/a (backend unchanged)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs) — n/a (backend unchanged)
- [x] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite — preflight ran the HTTP integration and Bruno suites green
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy — `framework/ai-sdlc/factory-dashboard/tests/e2e/dashboard.spec.ts` (Playwright + axe, kit-owned per FACTORY-UI-SPEC)
- [x] 7.2 `cd testing/e2e && npx playwright test` — all green — dashboard `pnpm e2e` 7/7 green; product e2e is untouched by this change
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px — 360px, 390px, 1440px screenshots in light and dark; phone width asserted in e2e
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason — UI surface exists; see 7.1

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI — `openapi/factory-control.openapi.yaml` 1.2.0: `/factory/session`, `/factory/coach`, `fields`, `literal_secret`
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes — kit and RepoLens changelogs; the root CHANGELOG is written by Release Please
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — nothing superseded outside the dashboard folder; the old dashboard is removed, not archived
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate — pre-push preflight: see traceability for the run and its environment-only failures

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>` — every commit carries `Refs #2384`; `Closes` goes in the PR body so the Issue closes after Gate 5
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Deviations from the approved change

| # | Deviation | Kind | Record |
|---|-----------|------|--------|
| D1 | `GET /factory/coach` needs the `owner` scope, not `read` as the spec scenario said | **Decision — Owner to confirm** | The report describes how the host's user works; a teammate's read token should not see it. Option B (read scope) is a one-line change in `serve.py` `GET_SCOPE`. The delta spec now states owner scope. |
| D2 | Contract additions beyond the proposal: `GET /factory/session`, `fields` on PUT, `literal_secret`; `POST /factory/catalogs/{kind}` was declared but not served | Mechanical | Additive (1.1.0 → 1.2.0); needed by the approved objective (dashboard writes through the contract with the user's identity). |
| D3 | `aisdlc serve` token files are git-ignored through `.aisdlc/.gitignore` | Mechanical | Security fix found while testing; no contract change. |
| D4 | Coach rule `unguarded_autonomy` added to the audit list | Mechanical | Maps the audit's "unsandboxed host execution" finding. |
| D5 | ADR-006 instead of "no ADR" | Mechanical | Records decisions 1–5 of design.md; status Proposed. |
| D6 | Dashboard module and preflight gate; Pages snapshots regenerated and factory-line copy made honest | Mechanical | Needed for the gates this change must pass; `workspace/sdlc/preflight.sh` is owner-protected, so the Owner merges. |
| D7 | Look and feel of commit `57a0ea32` | Mechanical | Owner instruction in chat; faint text and pill inks darkened for WCAG AA. |

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

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
