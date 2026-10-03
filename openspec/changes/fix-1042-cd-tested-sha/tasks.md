> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1042 OPEN; CU76; labels bug/DEVOPS/priority:high/audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update notes at implement if needed
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (workflow integrity only; no new ADR)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1042 --add-label "in-progress"`) — attempted; label ACL 403 for integration (documented)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after #1046 merge (`dbc15d30`)
- [x] 2.2 `git checkout -b cursor/fix-1042-cd-tested-sha-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1042/` into `openspec/changes/fix-1042-cd-tested-sha/` and run `bash scripts/validate-sdlc-plan.sh fix-1042-cd-tested-sha`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/cd-pin-tested-sha/spec.md` (checkout pin; SHA tag; latest after SHA; skip non-success)
- [x] 3.2 Write failing `scripts/test_cd_pin_tested_sha.py` asserting pre-change `cd.yml` lacks checkout `ref` with `workflow_run.head_sha`, pushes `latest` in the same tag set as SHA, while preserving the success `if` assert
- [x] 3.3 Run them and **observe them fail** on the pre-change tree (`python3 scripts/test_cd_pin_tested_sha.py` — 4 FAIL, success-if OK)
- [x] 3.4 Confirm every `#### Scenario:` in the delta spec maps to at least one test or explicit checklist item
- [x] 3.5 Integration tests — n/a for workflow YAML (document n/a)

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: `build-and-publish` checkout still has no `ref:`; metadata still includes `latest` in the same push; job `if` still gates on `workflow_run.conclusion == 'success'`
- [x] 4.2 Set checkout `with.ref: ${{ github.event.workflow_run.head_sha || github.sha }}` on `build-and-publish`
- [x] 4.3 Add Resolve publish SHA step; wire immutable tags to that SHA (do not rely on tip `github.sha` / bare `type=sha` under `workflow_run`)
- [x] 4.4 Split publish: push SHA-tagged (immutable) image first; only then tag/push `latest` to the same digest
- [x] 4.5 Keep job-level success `if` unchanged; do not weaken wiki/report conclusion guards
- [x] 4.6 Make the CD pin tests pass without weakening AC asserts

## 5. Actualizar tests existentes

- [x] 5.1 Identify any scripts that parse `cd.yml` tags/checkout (`test_ci_workflow_invariants.py`, report-job tests)
- [x] 5.2 Update them without weakening assertions if they assume a single multi-tag push — n/a (no cd.yml parsers assumed tip checkout; `test_report_job_needs_dependencies.py` pre-existing KeyError on pr-validation `publish-report` unrelated)
- [x] 5.3 Remove tests made genuinely obsolete only if the old expectation was tip-checkout — none

## 6. Ejecutar regresión

- [x] 6.1 `python3 scripts/test_cd_pin_tested_sha.py` — green
- [x] 6.2 Related workflow unit tests green (`test_ci_workflow_invariants.py` OK; concurrency n/a)
- [x] 6.3 Backend `mvn verify` — n/a for YAML-only (note); still run heavy gate on PR
- [x] 6.4 HTTP/Bruno — n/a for API delta
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (CD workflow + static tests + docs)
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip)
- [x] 7.3 If unexpected E2E edits appear, serialize with other Playwright-heavy PRs
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for CD pin-to-tested-SHA
- [x] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced — n/a
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `fix(cd): pin publish to workflow_run head SHA`, `test: …`, `docs: …`)
- [ ] 9.2 Every commit message ends with `Closes #1042`
- [x] 9.3 No secrets, no commented-out code, no unrelated workflow rewrites
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1042-cd-tested-sha-69d3`
- [ ] 10.2 Open the PR titled `[#1042] fix(cd): pin Docker publish to CI-tested SHA`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR for the merge’s CI `head_sha` (not an intervening tip)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: next CD `workflow_run` logs show checkout/publish SHA = CI `head_sha`; GHCR SHA tag exists; `latest` digest matches; non-success CI does not publish
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1042-cd-tested-sha`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Test cases designed; unit tests written first and observed failing (Gate 2)
- [ ] Implementation passes the applicable suite; heavy CI green (Gate 3/4)
- [x] Coverage gate unaffected / held (no Java delta)
- [x] Playwright: n/a product E2E; PR Playwright job still required green
- [x] Permanent documentation updated and consistent (no duplication)
- [ ] Commits atomic, Conventional Commits, `Closes #1042`
- [ ] Pull Request created, CI/CD green, code review approved (Gate 4)
- [ ] Merged to `main` via PR; CD pin verified; Issue closed (Gate 5)
