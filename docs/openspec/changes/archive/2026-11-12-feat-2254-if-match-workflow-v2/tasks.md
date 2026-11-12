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
- [x] 1.6 Move the Issue to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh <n>` (uses `status:in-progress`; on `addLabelsToLabelable` / `issues: write` 403 print honest SKIPPED and continue — never fake in-progress; see `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Write the unit tests for every scenario in the delta spec
- [ ] 3.3 Write the integration tests where applicable
- [ ] 3.4 Run them and **observe them fail** — `bash framework/ai-sdlc/verify.sh` focused / `python -m unittest`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Envelope: `schema_version`, `created_at`, server-derived `created_by` + actor helper
- [ ] 4.2 `aisdlc migrate` workflow v1→v2 idempotent + evidence record
- [ ] 4.3 Workflow body v2, pin digests on `request_validation`, digest guard on `approve`, `new_version` → draft
- [ ] 4.4 If-Match / expected_version on catalog update + CLI `--if-match`
- [ ] 4.5 Serve `--write` PUT with CSRF + write/owner scope + OpenAPI additive fields

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — n/a kit-only; use `bash framework/ai-sdlc/verify.sh`
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a kit-only
- [ ] 6.3 `mvn verify -pl backend-api` — n/a kit-only
- [ ] 6.4 `bash integration-test/scripts/test.sh` — n/a kit-only (no product API change)
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [ ] 7.2 `cd testing/e2e && npx playwright test` — n/a — no UI surface
- [ ] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a
- [ ] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes — omit if fence held
- [ ] 8.4 Archive superseded documents into `docs/000-archive/`
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>` — implementation commit closes; docs may Refs
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Workers never merge; coordinator/Owner merges after green CI

## 11. Merge y cierre

- [ ] 11.1 Squash-merge (or repo default) only by authorized merger — **not this worker**
- [ ] 11.2 Confirm the Issue auto-closed via `Closes #<n>`
- [ ] 11.3 Update `traceability.md` with merge SHA and CI URL

## 12. Gate 5 — Release / smoke

- [ ] 12.1 Ships with kit; no separate Notaire deploy for this slice
- [ ] 12.2 Smoke: `aisdlc doctor` / focused verify on a fixture profile after merge
- [ ] 12.3 Record smoke evidence in `traceability.md`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] OpenSpec change complete (proposal, specs, design, tasks) and Gate 1 validated
- [ ] Tests written first and observed failing; then green
- [ ] `preflight.sh --full` passed locally
- [ ] Permanent docs updated; PR opened with `Closes #2254`
