> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1599 exists and is linked to CU76 (labels 403 for fleet tokens; human paste later)
- [x] 1.2 Use Case CU76 exists; dispatch bullet will cite #1599 at Gate 3
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/release-asset-attachment/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No new ADR; ADR-028 decision 8 is tightened in place
- [ ] 1.6 Move the Issue to IN PROGRESS — blocked 403 (`addLabelsToLabelable`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` (fast-forward to `d073d2ad`)
- [x] 2.2 `git checkout -b cursor/fix-release-please-cd-dispatch-393d`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate: GH_REPO (or `--repo`) required; existing dispatch/tag-filter cases stay
- [x] 3.2 Write `DispatchOnTagTest.test_dispatch_names_repository_without_git_checkout`
- [x] 3.3 n/a — no Java/HTTP integration tests
- [x] 3.4 Observed fail: `AssertionError: False is not true : dispatch must set GH_REPO...`
- [x] 3.5 Every `#### Scenario:` maps to a DispatchOnTagTest method

## 4. Implementación

- [x] 4.1 Set `GH_REPO: ${{ github.repository }}` on the dispatch step in `release-please.yml`
- [x] 4.2 Re-ran dispatch unit tests: 8 tests OK

## 5. Actualizar tests existentes

- [x] 5.1 Kept `test_release_please_dispatches_cd_on_tag` and `test_cd_keeps_tag_filters`
- [x] 5.2 Did not weaken `actions: write` or `release_created` assertions
- [x] 5.3 No tests removed

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest workspace.tests.test_release_asset_attachment workspace.tests.test_semver_release_process docs.tests.test_changelog_structure` — 17 tests OK
- [x] 6.2 n/a — no Java change; do not run `mvn test -pl backend-api` for this delta
- [x] 6.3 n/a — JaCoCo unchanged
- [x] 6.4 n/a — SpotBugs/Checkstyle unchanged
- [x] 6.5 n/a — Bruno/HTTP suite, no endpoint change
- [x] 6.6 No `@Disabled` tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no Playwright spec to add
- [x] 7.3 n/a — no layout change
- [x] 7.4 Record "n/a — no UI surface": workflow + docs only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 RELEASE.md, ADR-028, CU76 as listed in proposal.md
- [x] 8.2 n/a — no OpenAPI change
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` Fixed entry for #1599
- [x] 8.4 No documents to archive
- [x] 8.5 No duplication — point at ADR-028 / RELEASE.md
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` process gates before push (Java/frontend skipped: no product code)

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; test observation then implementation
- [ ] 9.2 Last commit ends with `Closes #1599`
- [ ] 9.3 No secrets, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-release-please-cd-dispatch-393d`
- [ ] 10.2 Open PR `[#1599] fix(release): name repository on CD dispatch`
- [ ] 10.3 Wait for required workflows; `bash workspace/sdlc/check-heavy-ci.sh <pr>` before merge
- [ ] 10.4 Gate 4 — CI green, no conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 n/a — this change does not publish images
- [ ] 11.3 Record merge SHA in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Next `v*` cut: `workflow_dispatch` CD on the tag and five Release assets
- [ ] 12.2 Additive `v0.2.1` backfill when SHA images exist or Actions can dispatch
- [ ] 12.3 Rollback = revert of this PR; still valid
- [ ] 12.4 Close #1599 after merge; archive this OpenSpec change after smoke

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a — no UI)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
