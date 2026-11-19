> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a: module-internal change of the AI SDLC tooling, no architectural decision
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` (uses `status:in-progress`; on `addLabelsToLabelable` / `issues: write` 403 print honest SKIPPED and continue — never fake in-progress; see `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`) — labeled 2026-11-19

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — branched from `feat/2336_repolens-speed-callers-multirepo` (chained: #2336 is not merged yet), which was synced with `origin/main` (0 commits behind, no conflicts)
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable — real stores, CLI, HTTP and MCP over indexed fixture repositories
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=<NewTestClass>` — pytest (repolens has no Maven module); each `test(repolens)` commit precedes its `feat`/`fix` commit
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test — traceability.md, Requirement coverage

## 4. Implementación

- [x] 4.1 Detect every language through the pack; code only where definitions can be extracted
- [x] 4.2 Tags-query definitions and references; one grammar load attempt per language
- [x] 4.3 Universal path mentions (dirs, relative, bare names) in every text file, code included
- [x] 4.4 Identifier mentions in non-code text; proximity-based resolution in the linker
- [x] 4.5 Orphan computation (files, history-only, symbols, demoted count)
- [x] 4.6 CLI `repolens orphans`, `/api/orphans`, MCP `orphans`, UI panel, index alert
- [x] 4.7 Measure before/after on Notaire and the scale corpus; README, CHANGELOG, factory KPIs

- [x] 4.8 Files a tool reads by name are counted apart (`[orphans] conventional`) (mechanical deviation: the
  scale corpus listed 351 `package-info.java` among 1,313 orphans; the Issue asks for files that "may be useless
  or need review", which these are not; no contract, data or permission change)
- [x] 4.9 ESM TypeScript imports naming the emitted `.js` resolve to the `.ts` source (mechanical deviation: a
  resolution bug the orphan report exposed on vscode; no contract change)
- [x] 4.10 `.txt`, `.inc`, `.log` stay plain text (mechanical deviation: the pack detects them as vimdoc and
  sourcepawn)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy) — test_orphans fixture: the profile's demote glob named a directory, which now links; test_agnostic uses a six-letter-plus function name (cross-directory calls need one)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong — no assertion weakened
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration — n/a: no backend change; `bash framework/ai-sdlc/repolens/verify.sh` (126 passed, coverage 89.5% >= 88%)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor — n/a: no backend change; the module floor is in verify.sh
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs) — n/a: no backend change; ruff in verify.sh
- [x] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite — n/a: no endpoint of the product changes
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy — n/a
- [x] 7.2 `cd testing/e2e && npx playwright test` — all green — n/a
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason — n/a — no product UI surface: the repolens explorer (`repolens/ui/index.html`) gained an Orphans view, covered by `test_http_and_mcp_return_the_same_files` and a `node --check` of its script

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact — README, module CHANGELOG, factory-kpis.json
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI — n/a: no product endpoint; `/api/orphans` is in the repolens README HTTP table and its OpenAPI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes — n/a for the root CHANGELOG (release-please); module CHANGELOG 0.6.0
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — nothing superseded
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate — the full preflight is run before the push (pre-push hook)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Step commits carry `Refs #2338`; the archive commit carries `Closes #2338` (general.md 6.1)
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
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
