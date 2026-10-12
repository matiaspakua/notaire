# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1591 | open (verified with `gh issue view 1591`) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists; acceptance bullets updated in task 8.1 |
| Specification | `docs/openspec/changes/chore-1591-release-foundation-completion/` | Gate 1 |
| Branch | `cursor/semver-sbom-foundation-393d` | from `main` at `8feb51b1` |
| Tasks | `tasks.md` | 1.1-1.5 ticked; 1.6-12.5 pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending — the next release-please cut is the Gate 5 smoke; `v0.2.0` assets via the runbook |
| Smoke test | | pending |

## Requirement coverage

Every scenario of the four delta specs. Tests are planned names from
`design.md` Testing Strategy; none exists yet.

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Constitution P2 is KISS | `KissSpellingTest` in `workspace/tests/test_governance_principles.py` | pending |
| Governed files carry no old spelling | `KissSpellingTest` | pending |
| Exemptions are explicit | `KissSpellingTest` | pending |
| P11 states the bounds | `BoyScoutPrincipleTest` in `workspace/tests/test_governance_principles.py` | pending |
| Principle numbers are stable | `BoyScoutPrincipleTest` | pending |
| Digests mention both principles | `DigestBudgetTest` in `workspace/tests/test_governance_principles.py` | pending |
| Context budget holds | `DigestBudgetTest` (delegates to `workspace/ci/agent-context-budget.py`; also existing `test_agent_context_budget.py`) | pending |
| Digest does not copy the Constitution | `DigestBudgetTest` | pending |
| Release creation dispatches delivery on the tag | `DispatchOnTagTest` in `workspace/tests/test_release_asset_attachment.py` | pending |
| No release created, no dispatch | `DispatchOnTagTest` | pending |
| Manually pushed tags still publish | `DispatchOnTagTest` | pending |
| Missing image bill stops the upload | `FailClosedAttachTest` in `workspace/tests/test_release_asset_attachment.py` | pending |
| Unmatched pattern fails the step | `FailClosedAttachTest` | pending |
| Post-condition lists exactly five assets | `FailClosedAttachTest` | pending |
| Same-ref runs are serialised | `FailClosedAttachTest` | pending |
| Identity check gates the build | `DeliveryOrderingTest` in `workspace/tests/test_release_asset_attachment.py` | pending |
| Incoherent tag publishes nothing | `DeliveryOrderingTest` (job graph) with `TagValidationTest` (rejection) | pending |
| Runbook forbids destructive commands | `BackfillRunbookTest` in `workspace/tests/test_release_asset_attachment.py` | pending |
| Uploads skip assets already present | `BackfillRunbookTest` (documented guard command and no overwrite flag) | pending |
| Before and after proof | `BackfillRunbookTest` | pending |
| Image bills come from the published digest | `BackfillRunbookTest` | pending |
| Legacy product bill is not fabricated | `LegacyBackfillPolicyTest` in `workspace/tests/test_release_asset_attachment.py` | pending |
| Legacy gap is recorded | `LegacyBackfillPolicyTest` | pending |
| Resource attribute equals the packaged version | `ProductVersionTelemetryTest` in `backend-api/src/test/java/com/licensis/notaire/unit/observability/` | pending |
| Spans inherit the attribute | `ProductVersionTelemetryIntegrationTest` in `backend-api/src/test/java/com/licensis/notaire/integration/` | pending |
| No runtime dependency on the record file | `RuntimeIdentityWiringTest` in `workspace/tests/test_release_runtime_identity.py` | pending |
| Version follows the Maven filter | `RuntimeIdentityWiringTest` (filtered resource config) and `PackagedVersionTest` | pending |
| Log line has the version | `ProductVersionLogIdentityTest` in `backend-api/src/test/java/com/licensis/notaire/unit/observability/` | pending |
| Version is constant, not per-request state | `ProductVersionLogIdentityTest` | pending |
| Image tags equal the product version | `ImageIdentityTest` in `workspace/tests/test_release_runtime_identity.py` | pending |
| Placeholder tags are rejected | `ImageIdentityTest` | pending |
| Version label on staging workloads | `ImageIdentityTest` (static overlay) and `infra/tests/test_staging_kustomize.py` (rendered, skipped without kustomize) | pending |
| release-please covers every identity file | `ImageIdentityTest` | pending |
| Coherent tree passes | `CoherentTreeTest` in `workspace/tests/test_product_version_coherence.py` | pending |
| Maven and npm drift fails | `VersionDriftTest` in `workspace/tests/test_product_version_coherence.py` | pending |
| Backend parent drift fails | `VersionDriftTest` | pending |
| Stale database schema in the record fails | `VersionDriftTest` | pending |
| Changelog behind the manifest fails | `VersionDriftTest` | pending |
| Snapshot version fails | `VersionDriftTest` | pending |
| Matching tag passes | `TagValidationTest` in `workspace/tests/test_product_version_coherence.py` | pending |
| Tag ahead of the tree fails | `TagValidationTest` | pending |
| Legacy tag keeps working | `TagValidationTest` | pending |
| Legacy prefix on a new version fails | `TagValidationTest` | pending |
| Malformed tags fail | `TagValidationTest` | pending |
| Generated bills match the tree | `SbomAndAssetDriftTest` in `workspace/tests/test_product_version_coherence.py` | pending |
| Attach list missing an asset fails | `SbomAndAssetDriftTest` | pending |
| Extra asset name fails | `SbomAndAssetDriftTest` | pending |
| Packaged version matches | `PackagedVersionTest` in `workspace/tests/test_product_version_coherence.py` | pending |
| Unfiltered token fails | `PackagedVersionTest` | pending |
| Packaged version drift fails | `PackagedVersionTest` | pending |
| Preflight and pipeline invoke the check | `AuthFreeWiringTest` in `workspace/tests/test_product_version_coherence.py` (parses `preflight.sh`, `run_pipeline.sh`; extracts and executes the preflight command) | pending |
| Auth-free execution | `AuthFreeWiringTest` (token-less env, failing `gh` shim on `PATH`) | pending |
| CI runs the same command | `AuthFreeWiringTest` (parses `sdlc-process.yml`, `ci.yml`, `cd.yml`) | pending |
| A release-please cut stays coherent | `ReleaseCutSimulationTest` in `workspace/tests/test_product_version_coherence.py` (bumps every configured extra-file in a temp copy, then runs the check with the `v` tag) | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | yes | pending |
| `docs/200-architecture/202-ADR/README.md` | yes | pending |
| `docs/300-development/RELEASE-BACKFILL.md` | no | pending |
| `docs/300-development/RELEASE.md` | no | pending |
| `docs/300-development/SBOM.md` | no | pending |
| `docs/300-development/305-project-management/RELEASES.md` | no | pending |
| `docs/200-architecture/207-monitoring/README.md` | no | pending |
| `docs/200-architecture/209-deployment/README.md` | no | pending |
| `infra/deploy/kustomize/README.md` | no | pending |
| `CONSTITUTION.md` | no | pending |
| `CONSTITUTION-AGENT-CARD.md` | no | pending |
| `AGENTS.md` | no | pending |
| `.claude/rules/ai-agent-workflow.md` | no | pending |
| `docs/openspec/NOTAIRE-ADAPTATIONS.md` | no | pending |
| `docs/300-development/INGENIERIA.md` | no | pending |
| `local-ai/sdlc/prompts/05-implement.md` | no | pending |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | no | pending |
| `docs/300-development/303-testing/INVENTORY.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1591 verified open; 54 scenarios in four delta specs; `openspec validate chore-1591-release-foundation-completion --strict` and `bash workspace/sdlc/validate-sdlc-plan.sh chore-1591-release-foundation-completion` run by the planner |
| 2 | Failing tests written, test cases designed | no | cases designed in `design.md`; tests not yet written |
| 3 | Suite green, coverage held, docs updated | no | pending implementation |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending; smoke is the `v0.2.0` backfill proof plus the next release cut |

## Related / Refs

- Predecessor: #1571 (closed) and PR #1572 (merged 2026-10-12, merge commit `20ae00b1`); change `docs/openspec/changes/chore-1571-product-release/` stays active until Gate 5 here, then both are archived. Reconciliation table: `design.md`.
- Evidence of the empty release: `gh release view v0.2.0 --json assets` returns no assets; tag `v0.2.0` is commit `c58534d1`; the backend package in GHCR already holds the `c58534d` and `latest` tags used by the backfill.
- Legacy releases `notaire-v0.1.1`, `notaire-v0.1.2` also hold no assets.
- Constraint from the Issue: do not merge the release-please cut until this lands on `main`.
- Related: #1043 (semver process), #1042 (CD pins tested SHA), #1257 (path-scoped CI), #1259 (context budget), #1550 (board coherence), #1569 (roadmap coordination).

## Planned Files

| Path | Role |
|------|------|
| `workspace/sdlc/check-product-version-coherence.py` | the blocking check |
| `workspace/release/product_release.py` | shared `RELEASE_ASSETS`, `tag_is_valid` |
| `workspace/tests/test_product_version_coherence.py` | Gate 2 |
| `workspace/tests/test_release_runtime_identity.py` | Gate 2 |
| `workspace/tests/test_release_asset_attachment.py` | Gate 2 |
| `workspace/tests/test_governance_principles.py` | Gate 2 |
| `backend-api/src/test/java/com/licensis/notaire/unit/observability/ProductVersionTelemetryTest.java` | Gate 2 |
| `backend-api/src/test/java/com/licensis/notaire/unit/observability/ProductVersionLogIdentityTest.java` | Gate 2 |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProductVersionTelemetryIntegrationTest.java` | Gate 2 |
| `backend-api/src/main/resources/application.properties`, `logback-spring.xml` | identity |
| `docker-compose.yml`, `docker-compose.prod.yml`, `infra/deploy/kustomize/overlays/staging/kustomization.yaml` | release identity |
| `release-please-config.json` | marker-driven extra-files |
| `.github/workflows/cd.yml`, `release-please.yml`, `sdlc-process.yml`, `ci.yml` | delivery, dispatch, gates |
| `workspace/sdlc/preflight.sh` | named local gate |

## Exceptions

| Item | Reason | Owner |
|------|--------|-------|
| `in-progress` label on #1591 | May 403 for the fleet token (task 1.6) | human / next sync |
| Archive-hygiene gate on this branch | `check-openspec-archive-hygiene.py` fails a PR that touches an active change while task groups 3-8 are unticked; a plan-only push is therefore not PR-ready. Do not open the PR until implementation ticks them | foreman |
| Constitution amendment in the same PR | §12 requires a dedicated reviewed change; this PR is that change and keeps the amendment in its own commit | Owner |
| Playwright not run | No UI surface, recorded in tasks group 7 | recorded in design.md |
| ADR-028 drafted at Gate 1 | The Constitution (§5 step 5) wants the ADR before implementation; Status is Proposed until merge | Owner |
