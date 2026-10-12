> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1591 exists, is open, and is linked to CU76 (verified with `gh issue view 1591`)
- [x] 1.2 CU76 exists at `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md`; its acceptance bullets are updated in group 8
- [x] 1.3 Acceptance Criteria defined as scenarios in four delta specs (`release-version-coherence`, `release-runtime-identity`, `release-asset-attachment`, `governance-principles`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR-028 drafted under `docs/200-architecture/202-ADR/` (Proposed; Accepted on merge)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1591 --add-label "in-progress"`) — may 403 for the fleet token; if so record it in `traceability.md` Exceptions and use `Refs #1591` as the sync channel
- [ ] 1.7 Owner confirms the Constitution amendment (P2, P11) in the PR review, per §12 (dedicated, reviewed change)

## 2. Crear branch

- [ ] 2.1 Fetch current `main`; this branch was created from it by the Cursor fleet (`git fetch origin main && git merge origin/main` to stay conflict-free)
- [ ] 2.2 Work on `cursor/semver-sbom-foundation-393d` (fleet-suffix form allowed by CONSTITUTION §4); one Issue, one branch
- [ ] 2.3 Branch name is recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Cases enumerated in `design.md` Testing Strategy: happy path, edge, error and auth-free for every scenario
- [ ] 3.2 Write `workspace/tests/test_product_version_coherence.py` (`CoherentTreeTest`, `VersionDriftTest`, `TagValidationTest`, `SbomAndAssetDriftTest`, `PackagedVersionTest`, `AuthFreeWiringTest`, `ReleaseCutSimulationTest`) with a temp-tree fixture builder
- [ ] 3.3 Write `workspace/tests/test_release_runtime_identity.py` (`RuntimeIdentityWiringTest`, `ImageIdentityTest`)
- [ ] 3.4 Write `workspace/tests/test_release_asset_attachment.py` (`DispatchOnTagTest`, `FailClosedAttachTest`, `DeliveryOrderingTest`, `BackfillRunbookTest`, `LegacyBackfillPolicyTest`)
- [ ] 3.5 Write `workspace/tests/test_governance_principles.py` (`KissSpellingTest`, `BoyScoutPrincipleTest`, `DigestBudgetTest`)
- [ ] 3.6 Write Java tests: `ProductVersionTelemetryTest`, `ProductVersionLogIdentityTest` (unit) and `ProductVersionTelemetryIntegrationTest` (integration)
- [ ] 3.7 Tighten `infra/tests/test_staging_kustomize.py` and `infra/tests/test_prod_compose.py` to the semver identity (red before the manifests change)
- [ ] 3.8 Run them and **observe them fail**: `python3 -m unittest workspace.tests.test_product_version_coherence workspace.tests.test_release_runtime_identity workspace.tests.test_release_asset_attachment workspace.tests.test_governance_principles` and `mvn test -pl backend-api -Dtest=ProductVersionTelemetryTest,ProductVersionLogIdentityTest,ProductVersionTelemetryIntegrationTest`; paste the failure output in `traceability.md`
- [ ] 3.9 Confirm every `#### Scenario:` in the four delta specs maps to a test named in `traceability.md`
- [ ] 3.10 Auth-free cases run with `GH_TOKEN`, `GITHUB_TOKEN`, `GH_ENTERPRISE_TOKEN` unset and a failing `gh` shim on `PATH`

## 4. Implementación

- [ ] 4.1 Add `RELEASE_ASSETS` and `tag_is_valid()` to `workspace/release/product_release.py` and keep the CLI behavior of `--check`, `--write`, `--write-pages`, `--write-sbom` unchanged
- [ ] 4.2 Add `workspace/sdlc/check-product-version-coherence.py` (`--root`, `--tag`, `--packaged`), stdlib only, no `subprocess`, no token read
- [ ] 4.3 Wire `preflight.sh`: named step after `sdlc plan validation`, packaged-version step after `backend compile`, and a `--list` row; confirm `run_pipeline.sh` reaches it through `preflight.sh --full`
- [ ] 4.4 Wire CI: step in `sdlc-process.yml` (before the self-tests), packaged step after compile in `ci.yml` job `build`, new first job `release-identity` in `cd.yml` with `--tag` only on tag refs, `build-and-publish` `needs: release-identity`
- [ ] 4.5 Add `management.opentelemetry.resource-attributes.service.version=${app.version}` to `application.properties`
- [ ] 4.6 Add `springProperty` `APP_VERSION` and the `serviceVersion` custom field to `logback-spring.xml`
- [ ] 4.7 Do **not** add a bean that reads `release/product-release.json`; confirm the backend Dockerfile still copies no `release/` path
- [ ] 4.8 Add `image: ghcr.io/matiaspakua/notaire/{backend,frontend}:X.Y.Z # x-release-please-version` beside `build:` in `docker-compose.yml` and `docker-compose.prod.yml`
- [ ] 4.9 Staging overlay: `newTag: X.Y.Z # x-release-please-version` for both images and `app.kubernetes.io/version` in its labels; update `infra/deploy/kustomize/README.md`
- [ ] 4.10 `release-please-config.json`: add the three files as `type: generic` extra-files; verify with a release-please dry run before the next cut
- [ ] 4.11 `release-please.yml`: `actions: write`, step id `release` outputs, and the dispatch step guarded by `release_created == 'true'`
- [ ] 4.12 `cd.yml` release job: five-file presence assertion from `RELEASE_ASSETS`, `fail_on_unmatched_files: true`, post-upload exact-set assertion, job-level `concurrency` (not workflow-level)
- [ ] 4.13 Write `docs/300-development/RELEASE-BACKFILL.md` with: preconditions and token scope, read-only audit, before/after capture, worktree at the tag, checker with `--root` and `--tag`, bill generation, additive image tag, upload of missing assets only (no `--clobber`), verification and abort rules, and the legacy table
- [ ] 4.14 Execute the `v0.2.0` backfill (Owner, or agent with an explicit Owner go-ahead); attach the before/after JSON to `traceability.md`
- [ ] 4.15 `CONSTITUTION.md`: P2 → "KISS — Keep It Simple"; append P11 (bounded Boy Scout); §13 tooling-map row for the coherence gate; update the last-reviewed line
- [ ] 4.16 Sync digests: `CONSTITUTION-AGENT-CARD.md` (≤3 KB), `AGENTS.md` principles line, `.claude/rules/ai-agent-workflow.md`; replace the old spelling in `docs/300-development/304-ai-sdlc-cloud/README.md` if present, `docs/300-development/INGENIERIA.md`, `docs/openspec/NOTAIRE-ADAPTATIONS.md`, `infra/deploy/kustomize/README.md`, `local-ai/sdlc/prompts/05-implement.md` (text only)
- [ ] 4.17 `python3 workspace/ci/agent-context-budget.py --max-tokens 8000` stays green

## 5. Actualizar tests existentes

- [ ] 5.1 `infra/tests/test_staging_kustomize.py` (SHA-or-placeholder expectation, about lines 308-325) and `infra/tests/test_prod_compose.py`: expect the released semver tag; the old expectation described the pre-release placeholder state
- [ ] 5.2 Confirm `workspace/tests/test_product_release.py`, `test_cd_pin_tested_sha.py`, `test_frontend_ghcr_publish.py`, `test_ci_workflow_invariants.py`, `test_run_pipeline_invariants.py`, `test_agent_context_budget.py` still pass without weakening
- [ ] 5.3 No test is removed; record any removal and its reason here

## 6. Ejecutar regresión

- [ ] 6.1 `python3 -m unittest discover -s workspace/tests` and the module suites listed in `preflight.sh` (`infra/tests`, `docs/tests`, `contracts/tests`, `security/tests`, `testing/tests`, `local-ai/sdlc/tests`)
- [ ] 6.2 `mvn test -pl backend-api` — unit + integration, including the three new Java tests
- [ ] 6.3 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.4 `mvn verify -pl backend-api` — all quality gates; run `bash backend-api/tools/spotless-fallback.sh check` in a worktree
- [ ] 6.5 `bash testing/scripts/test.sh` — HTTP/Bruno API suite (no endpoint change expected)
- [ ] 6.6 `python3 workspace/sdlc/check-product-version-coherence.py` and `python3 workspace/release/product_release.py --check`
- [ ] 6.7 No `@Disabled` or skipped tests without documented, approved justification (the kustomize render test skips only when `kustomize` is absent, as today)

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface: no route, form or component changes
- [ ] 7.2 `cd testing/e2e && npx playwright test` still runs unchanged inside `run_pipeline.sh`
- [ ] 7.3 n/a — no layout change at 320px, 768px or 1024px
- [ ] 7.4 Record "n/a — no UI surface" here with the reason: the change is CI, deployment descriptors, backend properties and documentation

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in `proposal.md` Documentation Impact, including ADR-028 to Accepted and its README index row
- [ ] 8.2 n/a — no endpoint changed; OpenAPI untouched
- [ ] 8.3 `CHANGELOG.md` `[Unreleased]`: gate, runtime identity, attachment reliability, runbook, KISS/Boy Scout
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` if any are found; none expected
- [ ] 8.5 No duplication: docs point at `SBOM.md`, `RELEASES.md` and the runbook instead of copying them
- [ ] 8.6 `python3 workspace/sdlc/generate-testing-inventory.py` then `python3 workspace/sdlc/validate-testing-inventory.py`
- [ ] 8.7 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit order: failing tests; checker and preflight; telemetry; identity manifests; workflows; runbook; Constitution amendment and digests (separate commit); docs
- [ ] 9.2 Intermediate commits end with `Refs #1591`; only the last slice ends with `Closes #1591`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes (Boy Scout limited to touched lines)
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/semver-sbom-foundation-393d` (retry on network error only)
- [ ] 10.2 Open the PR titled `[#1591] chore(release): complete version coherence, runtime identity and asset attachment`, referencing Issue and CU76
- [ ] 10.3 Wait for `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`, `sdlc-process.yml`; confirm `bash workspace/sdlc/check-heavy-ci.sh <pr>` exits 0
- [ ] 10.4 Gate 4 — CI green, Owner review (including the Constitution amendment), no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the `main` CD run is green with the `release-identity` job first and both images published
- [ ] 11.3 Run (or confirm) the `v0.2.0` backfill after the merge, then merge the next release-please PR; its tag must start `cd.yml` through the dispatch step
- [ ] 11.4 Record the merge commit and the next release tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `v0.2.0` lists five assets with unchanged `publishedAt`, `targetCommitish` and tag commit; both digests still pass `cosign verify`; published backend shows `serviceVersion` in logs and `notaire.version` on `/actuator/info`
- [ ] 12.2 Smoke at the next cut: a `workflow_dispatch` CD run exists for the new tag and its Release holds exactly five assets
- [ ] 12.3 Verify the rollback path (remove `needs: release-identity`, revert) is still valid as described in `design.md`
- [ ] 12.4 Close #1591 after 12.1 and 12.2, referencing the PR; run `python3 workspace/sdlc/check-board-status-label-coherence.py --scope pr`
- [ ] 12.5 Archive this change and `chore-1571-product-release`: `openspec archive chore-1591-release-foundation-completion` and `openspec archive chore-1571-product-release`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright n/a, recorded in group 7
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
