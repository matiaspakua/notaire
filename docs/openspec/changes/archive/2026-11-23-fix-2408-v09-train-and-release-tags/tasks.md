> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2408 exists, labeled, and linked to CU76
- [x] 1.2 Use Case documentation exists and is accurate (CU76 in the requirements catalog)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR: the train map already has a single source of truth
- [x] 1.6 Issue moved to IN PROGRESS via `bash workspace/sdlc/add-issue-wip-label.sh 2408`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/2408_v09_train_and_release_tags`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Test cases: train resolution, policy mirror, real-tree coherence, fail-closed minor
- [x] 3.2 Write the failing train assertions in `workspace/tests/test_product_release.py`
- [x] 3.3 No integration tests needed: the coherence checker is the gate contract
- [x] 3.4 Observe the tests fail before implementing
- [x] 3.5 Every delta-spec scenario maps to a test (see `traceability.md`)

## 4. Implementación

- [x] 4.1 Add `v0.9 · Factory control plane (2027-03)` to `RELEASE_TRAINS`
- [x] 4.2 Add the same train to `milestone_policy.py` (`SCHEDULED`, roadmap window, description)
- [x] 4.3 Add the row to `MILESTONE-NOMENCLATURE.md`
- [x] 4.4 Create the GitHub milestone with the exact title via `gh api` (#30)
- [x] 4.5 Regenerate `github-page/lib/releases.json` with `--write-pages`
- [x] 4.6 Verify `check-product-version-coherence.py --tag v0.9.0` passes
- [x] 4.6b Extend the hardcoded expected `SCHEDULED` tuple in `workspace/sdlc/test_milestone_policy.py` (preflight self-test found it stale)
- [ ] 4.7 Push `v0.8.0` at a coherent 0.8.0-versioned commit and create its GitHub Release
- [ ] 4.8 Backfill assets without `--clobber`: `v0.8.0` via CD (coherent tag tree → all five), `v0.9.0` three tree-derived assets; document the two image-SBOM omissions (no image was ever published for that tag)

## 5. Actualizar tests existentes

- [x] 5.1 Existing train tests were already failing on `main`; keep their assertions unchanged
- [x] 5.2 No assertion weakened
- [x] 5.3 No test removed
- [x] 5.4 `test_milestone_policy.py` expected tuple updated to include the new train (assertion strengthened, not weakened)

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest workspace.tests.test_product_release workspace.tests.test_product_version_coherence`
- [x] 6.2 Coverage floors unchanged (no production logic branch added)
- [ ] 6.3 `bash workspace/sdlc/preflight.sh --full` (local stack uses `NOTAIRE_API_RATE_LIMIT_MAX=20000` to avoid the #2389 admin-user 429 flake; no tracked file changes)
- [ ] 6.4 `python3 security/run_gates.py --profile local` via `run_pipeline.sh` (the local gates pass inside preflight; the pipeline's Docker phase had a container-name collision with another worktree)
- [x] 6.5 No `@Disabled` or skipped tests without justification

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E specs: no UI surface
- [ ] 7.2 `cd testing/e2e && npx playwright test` via full preflight
- [x] 7.3 Not applicable, no affected screens
- [x] 7.4 n/a — no UI surface; change is release metadata only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Nomenclature, RELEASES.md and generated Pages history updated
- [x] 8.2 No OpenAPI change: no endpoints
- [x] 8.3 `CHANGELOG.md` intentionally left to the fleet's single `[Unreleased]` writer (fence #1693); release notes live in `RELEASES.md`
- [x] 8.4 No documents archived beyond this OpenSpec change (archived in the PR, 10.1)
- [x] 8.5 No duplicated information: train title lives in the three mirrors only
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` n/a — no Java/frontend source changed; spotless and eslint checks passed in the full preflight

## 9. Commits atómicos

- [ ] 9.1 Conventional commits, one for the train fix and one for docs/generated history
- [ ] 9.2 Every commit message ends with `Closes #2408`
- [ ] 9.3 No secrets, commented-out code or unrelated changes
- [ ] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes`
- [ ] 10.2 `git push -u origin fix/2408_v09_train_and_release_tags`
- [ ] 10.3 Open the PR referencing #2408 and CU76
- [ ] 10.4 Required workflows observed
- [ ] 10.5 Gate 4 — CI green, no conflicts, docs complete
- [ ] 10.6 PR number recorded in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 Tag `v0.8.0` pushed and Releases verified
- [ ] 11.3 Merge commit and tag recorded in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the coherence gate and verify both Releases with `gh release view`
- [ ] 12.2 Rollback path: revert the code commit; no database impact
- [ ] 12.3 Close #2408 referencing the PR
- [ ] 12.4 Record smoke evidence in the archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
