> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Verify open GitHub Issue #1862 and its CU76 linkage.
- [x] 1.2 Confirm existing CU76 documentation covers this developer-quality change.
- [x] 1.3 Define four acceptance scenarios in `specs/local-cicd-pipeline/spec.md`.
- [x] 1.4 Confirm affected modules and architecture review in `proposal.md`.
- [x] 1.5 Record that no ADR is required for shell orchestration semantics.
- [x] 1.6 Move the Issue to IN PROGRESS with the repository status label.

## 2. Crear branch

- [x] 2.1 Fetch and merge updated `origin/main` into the existing dedicated branch.
- [x] 2.2 Confirm `chore/1862_tiered_cicd_run_pipeline_sonarqube` follows convention.
- [x] 2.3 Record the branch in `traceability.md`.

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate successful, unavailable, fast-mode and incompatible-option cases.
- [x] 3.2 Add four failing invariants to `workspace/tests/test_run_pipeline_invariants.py`.
- [x] 3.3 Run `python3 workspace/tests/test_run_pipeline_invariants.py` and observe failure.
- [x] 3.4 No Java integration test applies; the actual shell pipeline is the integration test.
- [x] 3.5 Map every delta-spec scenario to the invariant test in `traceability.md`.

## 4. Implementación

- [x] 4.1 Make `--with-infra` require a ready and passing SonarQube quality gate.
- [x] 4.2 Skip application-stack startup and DAST in `--fast` mode while recording skips.
- [x] 4.3 Reject incompatible `--skip-sonar` combinations at option parsing.
- [x] 4.4 Preserve full default behavior, dashboard output and aggregate failure semantics.

## 5. Actualizar tests existentes

- [x] 5.1 Keep composition, dashboard and exit-status invariants green.
- [x] 5.2 Review script help and permanent plan for matching behavior.
- [x] 5.3 No obsolete tests exist; do not remove coverage.

## 6. Ejecutar regresión

- [x] 6.1 `python3 workspace/tests/test_run_pipeline_invariants.py` passes.
- [x] 6.2 `bash -n workspace/sdlc/run_pipeline.sh` passes.
- [x] 6.3 `bash workspace/sdlc/validate-sdlc-plan.sh chore-1862-tiered-cicd-pipeline` passes.
- [x] 6.4 `bash workspace/sdlc/preflight.sh --full` passes.
- [x] 6.5 `bash workspace/sdlc/run_pipeline.sh` passes and writes its dashboard.

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface changed; no E2E spec is added.
- [x] 7.2 Full pipeline executes existing Playwright regression.
- [x] 7.3 Viewport validation is n/a because UI output is unchanged.
- [x] 7.4 Record the no-UI decision in design and traceability.

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/300-development/PROJECT-CICD-PIPELINE-IMPROVEMENT.md` with implemented local behavior and separate remote roadmap.
- [x] 8.2 No API or OpenAPI change applies.
- [x] 8.3 `CHANGELOG.md` is n/a because this is not a user-visible release change.
- [x] 8.4 No permanent document is superseded.
- [x] 8.5 Confirm the plan does not duplicate permanent documentation.
- [x] 8.6 Run `bash workspace/sdlc/preflight.sh --full` before push.

## 9. Commits atómicos

- [x] 9.1 Commit test and implementation changes as focused Conventional Commits.
- [x] 9.2 Reference #1862; only closing PR text uses `Closes #1862`.
- [x] 9.3 Exclude untracked board-helper files and secrets.
- [ ] 9.4 Record commit SHAs in `traceability.md`.

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR with `cd docs && openspec archive chore-1862-tiered-cicd-pipeline --yes`.
- [ ] 10.2 Push branch and publish progress to #1862.
- [ ] 10.3 Open `[#1862] ci: harden local pipeline mode contracts` with CU76 evidence.
- [ ] 10.4 Wait for required CI workflows and preserve dashboard evidence in the PR.
- [ ] 10.5 Gate 4 requires CI green, review and no merge conflicts.
- [ ] 10.6 Record PR number in archived traceability.

## 11. Deploy

- [ ] 11.1 Merge through the Pull Request only.
- [ ] 11.2 No CD artifact is expected; this change runs on developer workstations.
- [ ] 11.3 Record merge and release evidence in archived traceability.

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run documented local fast-mode smoke after merge.
- [ ] 12.2 Confirm a revert remains safe.
- [ ] 12.3 Close #1862 only after its remaining remote-CI scope is completed.
- [ ] 12.4 Record deploy and smoke evidence in archived traceability.

## Definition of Done

- [x] Issue linked to CU76 with acceptance criteria.
- [x] Specification written and reviewed (Gate 1).
- [x] Failing tests observed before implementation (Gate 2).
- [x] Full local pipeline green with dashboard evidence (Gate 3); Sonar phase skipped (not running locally).
- [ ] Existing JaCoCo coverage unaffected.
- [x] No new Playwright coverage is required because no UI behavior changes.
- [ ] Permanent CI/CD documentation updated without duplication.
- [ ] Commits are atomic and conventional, referencing #1862.
- [ ] PR created with CI green and Gate 4 review.
- [ ] Merged, deployed where applicable, and smoke tested before issue closure.
- [ ] Traceability ledger completed through PR; deploy remains pending issue completion.
