# Release foundation completion: blocking version/SBOM coherence, runtime identity, reliable assets, governance spelling

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1591 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/semver-sbom-foundation-393d` |
| Gate 1 status | passed |
| SURFACE | backend + CI/CD + infra + docs (no UI surface) |

## Objetivo

PR #1572 (issue #1571, closed) defined the product release record and the
CycloneDX bills, but left them advisory: drift is only caught by a unit test
that runs on pull requests, the running system reports a version nothing
validates, the `v0.2.0` GitHub Release was published with **no assets**
(`gh release view v0.2.0` lists none), and compose/Kustomize still pin
placeholder tags. This change makes the release identity blocking, observable
and reliably attached, and aligns the governance wording (KISS, bounded Boy
Scout) before the next release-please cut.

## What Changes

- **Blocking coherence check.** New `workspace/sdlc/check-product-version-coherence.py`,
  a thin CLI over `workspace/release/product_release.py`. It compares root and
  backend Maven versions, `frontend/package.json`, the release-please manifest,
  `release/product-release.json`, the highest Flyway id, the changelog head,
  release tag format, the generated SBOM documents, the five-asset list, and
  the compose/Kustomize image identity. Wired as a named gate in
  `workspace/sdlc/preflight.sh`, therefore in `run_pipeline.sh --full`, and in
  CI (`sdlc-process.yml`, `ci.yml` build job, `cd.yml`). Needs no GitHub token
  and no network.
- **Packaged version drives telemetry.** `service.version` on the OpenTelemetry
  resource and a `serviceVersion` field on every JSON log line come from the
  Maven-filtered `app.version` inside the jar. CI validates the **packaged**
  `app.version` against `release/product-release.json`. Reading
  `product-release.json` at runtime is **rejected**: the file is not packaged
  into the backend image.
- **Compose and Kustomize release identity.** Backend and frontend images in
  `docker-compose.yml`, `docker-compose.prod.yml`, and the staging overlay use
  the `X.Y.Z` GHCR tag. release-please rewrites them through
  `x-release-please-version` markers (new `extra-files`), so no tag is bumped
  by hand. The staging overlay also carries `app.kubernetes.io/version`.
- **Reliable five-asset attachment.** release-please creates the tag with
  `GITHUB_TOKEN`, and GitHub does not start `push: tags` workflows for tags
  that token creates — the cause of the empty `v0.2.0` assets. `release-please.yml`
  now dispatches `cd.yml` on the new tag; `cd.yml` fails closed unless all five
  assets exist before upload and exactly five are on the Release afterwards.
- **Non-destructive backfill runbook.** `docs/300-development/RELEASE-BACKFILL.md`
  attaches the five assets to `v0.2.0` from the already published image digests
  and defines what legacy `notaire-v0.1.1` / `notaire-v0.1.2` can truthfully get.
  No tag moves, no rebuild, no `--clobber`, no release edit.
- **ADR-028** (drafted with this change as Proposed): release and version
  identity.
- **Governance spelling.** `CONSTITUTION.md` P2 becomes "KISS — Keep It Simple";
  P11 adds the bounded Boy Scout principle. The digest in
  `CONSTITUTION-AGENT-CARD.md`, `AGENTS.md`, `.claude/rules/ai-agent-workflow.md`
  and the AI SDLC / process docs follow, under the 8,000-token always-loaded
  budget, guarded by a test.
- **Predecessor reconciliation.** #1571 acceptance criteria are mapped to what
  #1572 delivered and what this change closes (design.md, Predecessor
  reconciliation). `chore-1571-product-release` is archived at Gate 5 of this
  change, not edited now.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Backend, frontend, manifest, record and Flyway id must agree on every commit that reaches a gate; drift fails closed | CU76, #1571, CONSTITUTION §11 | Changed — advisory test becomes a named blocking gate |
| A release tag is `vX.Y.Z` and equals the tree version; `notaire-v*` is accepted only for the two legacy versions | CU76, CONSTITUTION §11 | Made explicit |
| The running version is the packaged `app.version`; the record file is validated against it in CI and never read at runtime | CU76, ADR-016 | New |
| Every `v*` release carries exactly five assets: `sbom-notaire.cdx.json`, `sbom-database.cdx.json`, `sbom-backend.cdx.json`, `sbom-frontend.cdx.json`, `product-release.json` | CU76, `docs/300-development/SBOM.md` | Made explicit (enforced) |
| Backfill never moves a tag, rewrites notes, changes timestamps, or overwrites an asset | CU76, CONSTITUTION §11 | New |
| Images are referenced by released semver in compose and Kustomize; no hand-edited tag | CU76, #1043 | Changed |
| Principles use the standard spelling KISS; Boy Scout is bounded by P10 and by files the change already touches | CONSTITUTION §2, §12 | Changed |
| The local gates need no GitHub credential | CONSTITUTION §13 (preflight mirrors CI) | Made explicit |

## Capabilities

### New Capabilities

- `release-version-coherence`: the blocking check across Maven, npm, record, Flyway, tag, SBOM documents, asset list and image identity, and where it runs.
- `release-runtime-identity`: packaged `app.version` as OpenTelemetry `service.version` and log identity, plus compose/Kustomize release identity.
- `release-asset-attachment`: tag-triggered five-asset attachment and the non-destructive backfill.
- `governance-principles`: KISS spelling and the bounded Boy Scout principle across governance digests under the context budget.

### Modified Capabilities

- (none) — `product-release` lives in the still-active `chore-1571-product-release` change and is not yet under `docs/openspec/specs/`; this change adds enforcement capabilities beside it instead of editing another change's delta.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `application.properties` (`service.version` resource attribute), `logback-spring.xml` (`serviceVersion`), telemetry tests. No Java production class expected |
| `frontend` | no | Version already comes from `package.json`; only validated |
| `frontend-swing` | no | Retired module |
| `infra` / observability | yes | Kustomize staging overlay tags and version label; `docker-compose*.yml` image names; `infra/tests` updated |
| CI/CD (`.github/workflows`) | yes | `cd.yml`, `release-please.yml`, `sdlc-process.yml`, `ci.yml`; `release-please-config.json` |
| `workspace` | yes | New checker, shared constants in `workspace/release/product_release.py`, preflight wiring, tests |
| `docs` | yes | ADR-028, runbook, release/SBOM docs, Constitution, digests |
| `github-page` | no | Out of scope (runbook lives in `docs/`) |

### Surface area

- Entities: none
- Endpoints: none. `GET /actuator/info` is unchanged from #1572
- Database (Flyway `V{n}`): none
- Configuration / `.env`: one property `management.opentelemetry.resource-attributes.service.version`; no new `.env` key, no secret
- Dependencies: test-scope OpenTelemetry SDK testing artifact only if the span-level test needs it; none at runtime
- BREAKING: none for API clients. Operators who pinned `sha-PLACEHOLDER` in the staging overlay must use `X.Y.Z`

### Architecture review

Follows existing architecture: telemetry through Spring Boot configuration
properties (ADR-016), Flyway untouched (ADR-007), module reads only through
contracts (ADR-026: the checker lives in `workspace`, which may read all
modules). This is architectural for release identity, so **ADR-028** is
required and drafted with this change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-028-release-and-version-identity.md` | New ADR, drafted now as Proposed; Accepted on merge |
| `docs/200-architecture/202-ADR/README.md` | Index row for ADR-028 |
| `docs/300-development/RELEASE-BACKFILL.md` | New non-destructive backfill runbook for `v0.2.0` and legacy tags |
| `docs/300-development/RELEASE.md` | Dispatch-on-tag, fail-closed asset gate, marker-driven tags, link to the runbook |
| `docs/300-development/SBOM.md` | Five-asset contract is enforced; where the check runs |
| `docs/300-development/305-project-management/RELEASES.md` | Check command and tag rules point at the new checker |
| `docs/200-architecture/207-monitoring/README.md` | `service.version` and `serviceVersion` log field |
| `docs/200-architecture/209-deployment/README.md` | Staging overlay uses released semver, not SHA placeholders |
| `infra/deploy/kustomize/README.md` | KISS spelling, semver tag and version label |
| `CONSTITUTION.md` | P2 KISS, new P11 bounded Boy Scout, §13 tooling-map row, last-reviewed line |
| `CONSTITUTION-AGENT-CARD.md`, `AGENTS.md`, `.claude/rules/ai-agent-workflow.md` | Principles digest synced, within budget |
| `docs/300-development/304-ai-sdlc-cloud/README.md`, `docs/300-development/INGENIERIA.md`, `docs/openspec/NOTAIRE-ADAPTATIONS.md`, `local-ai/sdlc/prompts/05-implement.md` | Replace the old KIS spelling (text only; no local-ai tooling run) |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Acceptance bullets for blocking coherence, runtime identity, attachment and backfill |
| `docs/300-development/303-testing/INVENTORY.md` | Regenerated after the new tests land |
| `CHANGELOG.md` | `[Unreleased]` entry for the gate, identity, attachment and runbook |

## Out of Scope

- Cutting `v0.3.0` or merging any release-please PR; the cut waits for this change on `main`.
- Renaming milestones or changing trains (#1569).
- Attaching a Release page or Pages history for the backfill (Pages already reads the changelog).
- Editing `chore-1571-product-release` artifacts now; it is archived at Gate 5 here.
- A Personal Access Token or GitHub App for release-please; revisit only if the dispatch workaround proves insufficient.
- Rewriting historical archived changes or `docs/openspec/specs/` prose that still says "KIS".
- Prometheus metric tags carrying the version (cardinality), and any UI work.
