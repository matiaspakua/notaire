# Design — release foundation completion

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

PR #1572 (issue #1571, closed, merged 2026-10-12) added
`workspace/release/product_release.py`, `release/product-release.json`, the
CycloneDX generators, `include-component-in-tag: false`, and the `app.version`
Maven filter. Evidence on `main` at `8feb51b1`:

- `python3 workspace/release/product_release.py --check` passes, but it is not
  a named step in `preflight.sh`, `run_pipeline.sh`, or any workflow. The only
  enforcement is `test_product_release.py`, reached through the generic
  `unittest discover -s workspace/tests` line, which runs on pull requests.
  Nothing runs it on the release-please branch (its PR starts no checks,
  RELEASE.md "Release PR permissions") or against a tag.
- `gh release view v0.2.0` lists **no assets** (published 2026-10-12T20:55:10Z,
  tag commit `c58534d1`). `gh run list --workflow cd.yml` shows only
  `workflow_run` runs on `main` after the cut and none for ref `v0.2.0`.
  release-please ran at 20:54:20 with `github.token`; a tag created with that
  token does not start `push: tags` workflows. #1571 fixed the **filter**
  (`v*`), not the **trigger**.
- `notaire-v0.1.1` and `notaire-v0.1.2` also have no assets; their trees have
  Maven `1.0-SNAPSHOT`, so they can never satisfy the coherence rule.
- The backend Dockerfile copies only `pom.xml`, `backend-api/pom.xml` and
  `backend-api/src`. `release/product-release.json` is not in the image.
  `app.version=@project.version@` is filtered into the jar.
- OpenTelemetry already sets `service.name` through
  `management.opentelemetry.resource-attributes.service.name`; there is no
  `service.version`. `logback-spring.xml` emits `service` and `environment`
  custom fields and ignores Logstash's `@version`.
- Compose files `build:` locally with no `image:`; the staging overlay pins
  `sha-PLACEHOLDER`, and `infra/tests/test_staging_kustomize.py` accepts SHA or
  placeholder tags only.
- The principles table reads "KIS" (P2); AGENTS.md has no principles digest;
  the card is 2,079 bytes (limit 3,072) and the always-loaded set is about
  2,300 of 8,000 estimated tokens.

## Goals / Non-Goals

**Goals:**

- One named, blocking, credential-free coherence gate that runs locally and in CI before anything is published.
- Telemetry and logs report the version that is packaged, and CI proves it equals the record.
- Compose and Kustomize name released images and move with release-please.
- Every `v*` Release gets exactly five assets, or the pipeline fails visibly.
- `v0.2.0` is repaired without touching history; legacy tags get only what is true.
- KISS and bounded Boy Scout are stated once in the Constitution and mirrored in the digests within budget.

**Non-Goals:**

- Cutting `v0.3.0`, merging a release-please PR, or renaming milestones.
- A PAT or GitHub App; rebuilding or re-signing existing images.
- Runtime reads of the record file; a version metric tag; any UI or API contract change.
- Rewriting archived or capability-spec prose that still says KIS.

## Decisions

1. **The checker is a thin CLI over the existing module.** `workspace/sdlc/check-product-version-coherence.py`
   loads `workspace/release/product_release.py` (same `importlib` pattern as the
   current tests) and reuses `collect`, `check`, `product_sbom` and
   `database_sbom`. New logic added to `product_release.py` is limited to
   constants shared with CD and tests: `RELEASE_ASSETS` (the five names),
   `tag_is_valid(tag, version)`. Alternative: a second implementation in the
   issue's suggested `workspace/sdlc/` only. Lost: two readers of the same
   sources drift (the failure this change removes). The issue's path is kept
   as the single entry point.
2. **Tag rule is explicit and fail-closed.** `vX.Y.Z` must equal the tree
   version; `notaire-vX.Y.Z` is valid only when `X.Y.Z` is a key of
   `LEGACY_COMPONENT_TAGS` and is then checked for shape only. Pre-release
   suffixes are rejected because release-please emits none. Alternative:
   accept any `notaire-v*`. Lost: it would let a post-0.1.2 release
   re-introduce the prefix that silently skipped assets.
3. **Where it blocks.** `preflight.sh`: a named `run "product version coherence"`
   placed right after `sdlc plan validation` (cheap, first), and a second
   `run "packaged app.version"` after `backend compile` using
   `backend-api/target/classes/application.properties`. `run_pipeline.sh` gets
   it through `preflight.sh --full` (its header already says it composes
   preflight; no duplicate call, and `--list` gets a mapping row).
   CI: `sdlc-process.yml` (PR, before the self-test step), `ci.yml` build job
   after compile (packaged check), and a new first job `release-identity` in
   `cd.yml` that runs on every trigger and passes `--tag "$GITHUB_REF_NAME"`
   only when `GITHUB_REF_TYPE` is `tag`; `build-and-publish` `needs` it.
   Alternative: only add the call in the unit test. Lost: Constitution §13
   requires preflight and CI to mirror, and a test is invisible on the release
   branch and on tags.
4. **Auth-free by construction.** The checker reads files only, uses the
   standard library plus the module already importable, never imports
   `subprocess`, and ignores `GH_TOKEN`/`GITHUB_TOKEN`. The existing
   board-coherence check (`gh` required, skipped without auth) is the contrary
   example; this gate must never be skippable that way.
5. **Telemetry from the packaged property, not from JSON.**
   `management.opentelemetry.resource-attributes.service.version=${app.version}`
   in `application.properties`. `${app.version}` is resolved by Spring from the
   Maven-filtered value already in the jar. Logs add
   `<springProperty scope="context" name="APP_VERSION" source="app.version"/>`
   and `"serviceVersion":"${APP_VERSION}"` in the existing `customFields`.
   Rejected: the issue's `NotaireResourceProvider` bean that reads
   `product-release.json`. It would fail or fall back at runtime because the
   file is outside the image, would add a Java class for what a property does,
   and would be a second place that decides the version. Rejected also: MDC
   per-request version, because the value is process-constant and an MDC entry
   adds per-request work and a leak path for nothing.
6. **CI validates the artifact, not the file path.** The packaged check
   compares `app.version` in `target/classes/application.properties` with
   `release/product-release.json`, and rejects the unfiltered
   `@project.version@` token. This is what "derived from the packaged
   `app.version` that CI validates against the record" means. Alternative:
   copy the record into the jar. Lost: couples the image build context to
   `release/` and adds a second runtime source.
7. **Image identity uses release-please markers.** Backend and frontend
   `image:` lines in both compose files and `newTag` plus the
   `app.kubernetes.io/version` label in the staging overlay end with
   `# x-release-please-version`; `release-please-config.json` lists the three
   files as `type: generic` extra-files. Tags come from CD's existing
   `docker/metadata-action` `type=semver`, which the identity job proves equals
   Maven and npm. Alternatives: a Kustomize `images` patch generated in CD (a
   second writer), `latest` or SHA tags (not stable, not atomic with the cut),
   or YAML-path extra-files (more brittle on comments and anchors than the
   marker). The identity check rejects any backend/frontend GHCR reference not
   equal to the product version.
8. **Dispatch on the tag.** `release-please.yml` gains `actions: write` and a
   step, only when `release_created == 'true'`, running
   `gh workflow run cd.yml --ref "$TAG_NAME"` with `GH_TOKEN: ${{ github.token }}`.
   Dispatch is one of the two events GitHub exempts from the
   "token-created events do not start workflows" rule. `cd.yml` already has
   `workflow_dispatch` and its release job already accepts `refs/tags/v`. The
   `push: tags` filters stay for human-pushed tags. Alternatives: a PAT or App
   token (a secret to rotate and an Owner action), `on: release` (also not
   triggered by the repository token), or `workflow_run` on the release-please
   workflow (cannot see the tag as `github.ref`).
9. **Attach fails closed.** The prepare step asserts the five files exist
   (names from one list, `RELEASE_ASSETS`), `softprops/action-gh-release` runs
   with `fail_on_unmatched_files: true`, and a final step reads the Release
   with `gh release view --json assets` and fails unless the set equals the
   five names. The release job gets `concurrency: group: release-assets-${{ github.ref }}`
   with `cancel-in-progress: false`. Concurrency is **job-level**, not
   workflow-level: a workflow-level group would drop queued `main` publishes.
10. **Backfill reuses published digests.** `RELEASE-BACKFILL.md` resolves each
    image digest from the GHCR tag equal to the tagged commit SHA (the
    `c58534d…` tag exists on the backend package), generates the image bills
    with Trivy on that digest, generates the product and database bills with
    `product_release.py --root <worktree at the tag> --write-sbom`, adds the
    `0.2.0` tag to the same digest (`docker buildx imagetools create --tag`,
    additive), and uploads only missing assets with `gh release upload` and no
    `--clobber`. Before/after JSON of `tagName`, `targetCommitish`,
    `publishedAt` and the tag commit proves nothing moved. Rejected: re-running
    `cd.yml` at `v0.2.0` (rebuilds images, produces a new digest, re-signs),
    `--clobber` (destructive by definition), deleting and recreating the
    Release (changes the timestamp and breaks links).
11. **Legacy policy is truth-bound.** `notaire-v0.1.1`/`0.1.2` get the
    database bill from the tagged tree and an image bill only if an image for
    the tag commit exists. No product bill or record, since Maven was
    `1.0-SNAPSHOT` and the record did not exist. The runbook table lists each
    omission. Alternative: generate a product bill with the current script.
    Lost: it would attest a coherence that never existed.
12. **Governance wording.** P2 → "KISS — Keep It Simple". P11 is appended
    (not inserted) so `P4`/`P7` citations in `docs/openspec/config.yaml`,
    the schema and older ledgers stay valid. P11 text is bounded by the five
    limits in the spec. The "standard KISS spelling" is the four letters; the
    expansion stays the issue's neutral "Keep It Simple" rather than the
    pejorative long form. Digests get one line each pointing at CONSTITUTION §2;
    a test fails if the bare word KIS reappears in governed files.
13. **ADR-028** records decisions 1–12 at architecture level: semver is one
    atomic unit of backend, frontend and database; trains stay milestones;
    legacy `notaire-v*` is supported for exactly `0.1.1` and `0.1.2`; 0.x →
    1.0 transition (0.x and 1.0.x advance the production train, `v1.0.0` is an
    Owner decision, a new minor needs a train mapping first); runtime version
    comes from the artifact; delivery trigger and fail-closed assets. Drafted
    now as Proposed because the Constitution requires the ADR before
    implementation (§5 step 5).

### Predecessor reconciliation (#1571 / PR #1572)

| #1571 acceptance criterion | Delivered by #1572 | Gap found | Closed by |
|----------------------------|--------------------|-----------|-----------|
| Record has backend, frontend, database | `release/product-release.json`, `collect()` | none | — |
| Versions coherent with the manifest | `check()` and a unit test | Not a named gate; no run on the release branch or on tags | Decisions 1–4 |
| Product SBOM defined; image bills still attach | CycloneDX generators, docs, `release` job list | `v0.2.0` has no assets; asset list not validated; partial set can upload | Decisions 8–10, scenarios in `release-asset-attachment` |
| `vX.Y.Z` going forward; legacy still triggers CD | config flag, both tag filters | Trigger never fires for release-please tags | Decision 8 |
| Actuator, OpenAPI, sidebar show the version | contributor, OpenAPI info, sidebar | Telemetry and logs carry no version | Decisions 5–6 |
| Pages semver history | `prebuild` history | none | — |
| Docs on trains, board, AI SDLC | RELEASES.md and friends | none | — |
| #1571 tasks 10.3–12.4 (CI, deploy, smoke, close, archive) | PR merged, Issue closed | Smoke of an attached asset set never happened | tasks 11 and 12 here; `chore-1571-product-release` is archived at Gate 5 of this change |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Dispatch on the tag still does not start CD (permissions, `workflow_dispatch` missing at the tag ref) | Scenario tests read the permission and the trigger; the first real cut (`v0.3.0`) is the smoke test and Gate 5 waits for it; the runbook doubles as the manual path |
| `x-release-please-version` markers are ignored on a line shape release-please does not parse | A test asserts every marked line is a bare semver after the marker; release-please's own dry run (`--dry-run` on the config) is a task before the cut; failure surfaces as the release PR failing the coherence gate, not a silent skip |
| Compose/Kustomize reference `X.Y.Z` before its images exist (window between the release PR merge and CD finishing) | The overlay is applied manually and CD publishes on the same tag; the runbook adds the `0.2.0` tag now so today's tree is deployable |
| The in-image `service.version` placeholder resolves to the literal token in an IDE run without Maven filtering | Runtime stays unchanged by design; the packaged check rejects the token in CI; dev runs show the token, which is visible rather than wrong-looking |
| Adding a required `needs` to `build-and-publish` blocks every `main` publish if the check is wrong | The job is stdlib-only and fast; it runs on `main` today's tree in the test; rollback is removing the `needs` line |
| Spans test needs an OpenTelemetry testing artifact | Test scope only; if refused, assert the resource bean and rely on the SDK contract that spans carry their resource (recorded in the test) |
| Backfill uploads from a developer machine | Runbook requires `gh` auth with `contents: write` only, runs read-only audits first, and has an abort rule if any before/after field differs |
| Constitution is amended in the same branch as tooling | §12 requires a dedicated PR reviewed by the Owner for the amendment; this change's PR is that PR and the Constitution diff is a separate commit |
| `workspace/tests` already large; two more files add runtime | New tests build tiny temp trees and run in under a second each |

## Testing Strategy

TDD (P1): every test below is written first and observed failing, as in
tasks group 3. Fixtures build a minimal temp repo (pom, backend pom, package
json, manifest, record, migrations, changelog, release-please config, compose,
overlay) so drift cases do not touch the real tree.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Coherent tree passes | unit | `CoherentTreeTest` in `workspace/tests/test_product_version_coherence.py` |
| Maven and npm drift fails | unit | `VersionDriftTest` in `workspace/tests/test_product_version_coherence.py` |
| Backend parent drift fails | unit | `VersionDriftTest` |
| Stale database schema in the record fails | unit | `VersionDriftTest` |
| Changelog behind the manifest fails | unit | `VersionDriftTest` |
| Snapshot version fails | unit | `VersionDriftTest` |
| Matching tag passes | unit | `TagValidationTest` in `workspace/tests/test_product_version_coherence.py` |
| Tag ahead of the tree fails | unit | `TagValidationTest` |
| Legacy tag keeps working | unit | `TagValidationTest` |
| Legacy prefix on a new version fails | unit | `TagValidationTest` |
| Malformed tags fail | unit | `TagValidationTest` |
| Generated bills match the tree | unit | `SbomAndAssetDriftTest` in `workspace/tests/test_product_version_coherence.py` |
| Attach list missing an asset fails | unit | `SbomAndAssetDriftTest` |
| Extra asset name fails | unit | `SbomAndAssetDriftTest` |
| Packaged version matches | unit | `PackagedVersionTest` in `workspace/tests/test_product_version_coherence.py` |
| Unfiltered token fails | unit | `PackagedVersionTest` |
| Packaged version drift fails | unit | `PackagedVersionTest` |
| Preflight and pipeline invoke the check | integration | `AuthFreeWiringTest` in `workspace/tests/test_product_version_coherence.py` (parses `preflight.sh`, `run_pipeline.sh`; extracts and executes the preflight command) |
| Auth-free execution | integration | `AuthFreeWiringTest` (token-less env, failing `gh` shim on `PATH`) |
| CI runs the same command | integration | `AuthFreeWiringTest` (parses `sdlc-process.yml`, `ci.yml`, `cd.yml`) |
| A release-please cut stays coherent | integration | `ReleaseCutSimulationTest` in `workspace/tests/test_product_version_coherence.py` (bumps every configured extra-file in a temp copy, then runs the check with the `v` tag) |
| Resource attribute equals the packaged version | unit | `ProductVersionTelemetryTest` in `backend-api/src/test/java/com/licensis/notaire/unit/observability/` |
| Spans inherit the attribute | integration | `ProductVersionTelemetryIntegrationTest` in `backend-api/src/test/java/com/licensis/notaire/integration/` |
| No runtime dependency on the record file | unit | `RuntimeIdentityWiringTest` in `workspace/tests/test_release_runtime_identity.py` |
| Version follows the Maven filter | unit | `RuntimeIdentityWiringTest` (filtered resource config) and `PackagedVersionTest` |
| Log line has the version | unit | `ProductVersionLogIdentityTest` in `backend-api/src/test/java/com/licensis/notaire/unit/observability/` |
| Version is constant, not per-request state | unit | `ProductVersionLogIdentityTest` |
| Image tags equal the product version | unit | `ImageIdentityTest` in `workspace/tests/test_release_runtime_identity.py` |
| Placeholder tags are rejected | unit | `ImageIdentityTest` |
| Version label on staging workloads | integration | `ImageIdentityTest` (static overlay) and `infra/tests/test_staging_kustomize.py` (rendered, skipped without kustomize) |
| release-please covers every identity file | unit | `ImageIdentityTest` |
| Release creation dispatches delivery on the tag | unit | `DispatchOnTagTest` in `workspace/tests/test_release_asset_attachment.py` |
| No release created, no dispatch | unit | `DispatchOnTagTest` |
| Manually pushed tags still publish | unit | `DispatchOnTagTest` |
| Missing image bill stops the upload | unit | `FailClosedAttachTest` in `workspace/tests/test_release_asset_attachment.py` |
| Unmatched pattern fails the step | unit | `FailClosedAttachTest` |
| Post-condition lists exactly five assets | unit | `FailClosedAttachTest` |
| Same-ref runs are serialised | unit | `FailClosedAttachTest` |
| Identity check gates the build | unit | `DeliveryOrderingTest` in `workspace/tests/test_release_asset_attachment.py` |
| Incoherent tag publishes nothing | integration | `DeliveryOrderingTest` (job graph) with `TagValidationTest` (rejection) |
| Runbook forbids destructive commands | unit | `BackfillRunbookTest` in `workspace/tests/test_release_asset_attachment.py` |
| Uploads skip assets already present | unit | `BackfillRunbookTest` (documented guard command and no overwrite flag) |
| Before and after proof | unit | `BackfillRunbookTest` |
| Image bills come from the published digest | unit | `BackfillRunbookTest` |
| Legacy product bill is not fabricated | unit | `LegacyBackfillPolicyTest` in `workspace/tests/test_release_asset_attachment.py` |
| Legacy gap is recorded | unit | `LegacyBackfillPolicyTest` |
| Constitution P2 is KISS | unit | `KissSpellingTest` in `workspace/tests/test_governance_principles.py` |
| Governed files carry no old spelling | unit | `KissSpellingTest` |
| Exemptions are explicit | unit | `KissSpellingTest` |
| P11 states the bounds | unit | `BoyScoutPrincipleTest` in `workspace/tests/test_governance_principles.py` |
| Principle numbers are stable | unit | `BoyScoutPrincipleTest` |
| Digests mention both principles | unit | `DigestBudgetTest` in `workspace/tests/test_governance_principles.py` |
| Context budget holds | unit | `DigestBudgetTest` (delegates to `workspace/ci/agent-context-budget.py`; also existing `test_agent_context_budget.py`) |
| Digest does not copy the Constitution | unit | `DigestBudgetTest` |

- New unit tests (`src/test/java/.../unit/`): `ProductVersionTelemetryTest`, `ProductVersionLogIdentityTest`.
- New integration tests (`src/test/java/.../integration/`): `ProductVersionTelemetryIntegrationTest`.
- New Python guards: the four files under `workspace/tests/` named above.
- Auth-free cases (explicit): the checker runs with `GH_TOKEN`, `GITHUB_TOKEN` and `GH_ENTERPRISE_TOKEN` unset and a `gh` shim that records a call and exits 1; the test asserts rc 0 and no recorded call. The preflight command line is extracted from `preflight.sh` and run in the same environment. Preflight must still be runnable without `gh` (existing steps already skip).
- Coverage impact: Java additions are configuration only; the JaCoCo floor (80% line / 65% branch) is unaffected. Python guards are outside JaCoCo.

## Regression Strategy

- Existing tests affected, and why:
  - `workspace/tests/test_product_release.py`: `ReleaseAutomationTest` asserts the `release` job blob contains `sbom-database.cdx.json`, `sbom-notaire.cdx.json` and `product_release.py`; still true. Keep as is.
  - `workspace/tests/test_cd_pin_tested_sha.py`, `test_frontend_ghcr_publish.py`, `test_ci_workflow_invariants.py`: assert `cd.yml` and workflow structure; adding a `needs` and a job must keep the SHA pin, matrix, latest-after-SHA order, and required check names.
  - `infra/tests/test_staging_kustomize.py` (lines ~308-325) asserts SHA or placeholder tags. That expectation is now wrong: the overlay uses the released semver by design. Update it to require `X.Y.Z` equal to the record, never loosen to accept `latest`.
  - `infra/tests/test_prod_compose.py`: backend/frontend gain `image:` while keeping `build:`; assert both and the tag.
  - `workspace/tests/test_agent_context_budget.py`: unchanged; must stay green.
  - `check-agent-rules.sh`: new backticked paths in digests must exist.
  - `docs/tests` and the ADR README index test (if any) for the new ADR row.
- Full suite command: `bash workspace/sdlc/preflight.sh --full`, then `bash workspace/sdlc/run_pipeline.sh` before the PR; `mvn verify -pl backend-api` for the Java tests.
- HTTP/Bruno API suite: `bash testing/scripts/test.sh` is unaffected (no endpoint changes); run in the pipeline.
- Legacy paths at risk: none in `jpa`; `deprecated/` is untouched. `local-ai/sdlc/prompts/05-implement.md` is edited as text only; `local-ai/sdlc/tests` must stay green and no harness is run.

## Playwright Strategy

n/a - no UI surface. The change touches CI, deployment descriptors, backend
configuration and documentation; the sidebar version label from #1572 is
unchanged and already covered by `app-sidebar.test.tsx`.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: not applicable
- Edge / error paths covered: not applicable
- Viewports: not applicable
- Command: `cd testing/e2e && npx playwright test` still runs unchanged in the pipeline

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: (1) merge this change to `main`; (2) run the
  backfill runbook for `v0.2.0` (adds assets and the `0.2.0` image tag; no
  code coupling); (3) only then merge the next release-please PR — its tree
  carries the marker-driven compose/Kustomize bumps, and its tag must start CD
  through the dispatch step. Do not merge a release-please cut before (1).
- Configuration or `.env` keys to add: none; `service.version` is a property
  derived from `app.version`. No secret.
- Feature flag: no
- Smoke test after deploy (Gate 5): (a) `gh release view v0.2.0 --json assets`
  lists the five names and `publishedAt`/`targetCommitish` equal the "before"
  capture; (b) `cosign verify` on both digests still passes; (c) a local run of
  the published backend image shows `serviceVersion` in a JSON log line and
  `/actuator/info` `notaire.version` equal to `0.2.0`; (d) at the next release,
  `cd.yml` shows a `workflow_dispatch` run on the new tag and the Release has
  five assets.

## Rollback Strategy

- Revert safe: yes. The change is configuration, workflow, documentation and
  tests. Reverting restores the advisory check and the placeholder tags.
- Database rollback: none needed
- Data written under the new behavior after revert: GitHub Release assets and
  the added `0.2.0` image tag remain. They are additive and truthful, so they
  are left in place. A wrong asset is removed with an explicit, separately
  approved `gh release delete-asset` — the runbook never does it.
- Blast radius if rollback is delayed: a wrong `needs`/check in `cd.yml` would
  block image publishing on `main`; remove the `needs: release-identity` line
  first (one-line rollback) and keep the rest.

## Migration Plan

1. Write failing tests (group 3), commit them first.
2. Add the checker and shared constants; wire preflight, then `sdlc-process.yml`, `ci.yml`.
3. Add the property, the logback field and the Java tests; run `mvn verify -pl backend-api`.
4. Add markers, `image:` lines, label and `extra-files`; update the kustomize and compose tests.
5. Edit `cd.yml` (identity job, fail-closed attach, post-condition, job-level concurrency) and `release-please.yml` (permission, dispatch).
6. Write the runbook; dry-run its read-only steps against `v0.2.0`; execute the upload as the Owner (or with an explicit Owner go-ahead).
7. Amend the Constitution and digests; run the budget check and the guard test.
8. Update the permanent docs, regenerate the testing inventory, add the changelog entry.
9. Run `bash workspace/sdlc/preflight.sh --fix` then `bash workspace/sdlc/run_pipeline.sh`.

## Open Questions

- Should `release-identity` also run on `workflow_run` events for `main`, or only on tags and dispatch? This design runs it on every trigger because it is cheap and catches a drifted `main`; the Owner may narrow it to tags without changing any spec scenario.
- Does the Owner want the `v0.2.0` upload executed by the agent with a scoped token, or by the Owner from the runbook? The runbook is written to be executable by either; the choice does not change the specification.
