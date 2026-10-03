# Pin CD Docker builds to the CI-tested SHA

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1042 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1042-cd-tested-sha-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1046 merges** (queue: #1044 → #1051 → #1046 → #1042) |

## Objetivo

`.github/workflows/cd.yml` is triggered by `workflow_run` after CI on `main`,
but the `build-and-publish` job checks out the default branch tip (no `ref:`)
and publishes `latest` in the same push. A later merge or bot commit between
CI success and CD checkout yields a signed GHCR image that CI never tested.
Pin checkout and image tags to `github.event.workflow_run.head_sha`, move
`latest` only after the SHA tag succeeds, and keep skipping non-success CI.

## What Changes

- In `.github/workflows/cd.yml` job `build-and-publish`, set `actions/checkout`
  `ref` to the triggering CI head SHA on `workflow_run` (fallback `github.sha`
  for tag / `workflow_dispatch`).
- Resolve an explicit publish SHA and tag the image with that SHA (do not rely
  on metadata-action’s default `github.sha` under `workflow_run`, which is the
  tip of the default branch, not the tested commit).
- Split publish so the SHA-tagged (immutable) push succeeds first; only then
  move/push `latest` to the same digest.
- Keep (and assert) the existing job `if` that skips publish when
  `workflow_run.conclusion != 'success'`.
- Add a static YAML unittest proving the three Acceptance Criteria.
- Document the pin-to-tested-SHA contract in DevSecOps docs + CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| CD MUST build and sign the exact commit SHA that CI tested on `main` | CU76; #1042 AC | New (checkout + tag pin) |
| Mutable `latest` MUST move only after the SHA-tagged image push succeeds | #1042 AC; release integrity | Changed (split publish) |
| CD MUST NOT publish when the triggering CI run did not conclude `success` | CU76; existing `cd.yml` `if`; #1042 AC | Made explicit (keep + test) |

## Capabilities

### New Capabilities

- `cd-pin-tested-sha`: CD `workflow_run` path checks out and tags the CI-tested
  SHA; `latest` is moved only after that SHA push; non-success CI skips publish.

### Modified Capabilities

- (none under `openspec/specs/` today cover CD pin-to-SHA behavior)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Image contents unchanged; only which git SHA is built |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `.github/workflows/cd.yml` checkout, tag, latest ordering |
| Scripts / tests | yes | New/extended static unittest for CD invariants |
| Docs | yes | DevSecOps CD section + CHANGELOG |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (existing Actions versions retained unless a pin requires
  a documented bump)

### Architecture review

No application-architecture change. Strengthens the existing CI→CD
`workflow_run` gate so published artifacts match tested commits (supply-chain
/ release integrity for CU76). Related drift source: wiki publish may still
commit to `main` after CD; that must not change which SHA CD built.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/208-devsecops/README.md` | Document checkout `ref` = CI `head_sha`, SHA tag, `latest` after SHA, skip on non-success |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note CD pin-to-tested-SHA for #1042 if CU text lacks it |
| `CHANGELOG.md` | `[Unreleased]` devops/ci entry for #1042 |

## Out of Scope

- Changing CI job graph, Trivy/cosign steps, or GHCR credentials.
- Reworking wiki/report publish jobs beyond documenting they must not redefine
  the image build SHA (their `ref: main` checkout for docs push stays unless
  implement finds a one-line consistency fix that does not expand scope).
- Frontend image release / semver (#1043).
- Production compose (#1044) or Dependabot Swing/smol-toml (#1046).
- Starting implement / PR before **#1046** merges.
