# Publish frontend GHCR image and introduce semver releases

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1043 |
| Use Case | **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/ci-1043-frontend-image-semver-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1040** (queue: #1046 → #1042 → #1041 → #1040 → #1043) or as coordinator schedules |

## Objetivo

CD builds, SBOMs, cosign-signs, and publishes only the backend image. The Next.js
frontend has a working `frontend/Dockerfile` (standalone) but no GHCR publish path.
`git tag` and GitHub Releases are empty; Maven stays at `1.0-SNAPSHOT` and npm at
`0.1.0`, so there is no versioned, deployable frontend artifact and no rollback
tag. Close the gap left by closed #681 (backend only) and introduce an automated
semver release process that rolls `CHANGELOG.md` `[Unreleased]` into versions and
derives Maven/npm versions from the release tag.

## What Changes

- Extend `.github/workflows/cd.yml` so the frontend image is built from
  `frontend/Dockerfile`, SBOM'd (CycloneDX), cosign-signed (keyless OIDC),
  SBOM-attested, and pushed to GHCR as `${{ github.repository }}/frontend`
  with the same tag/attest pattern as backend (supersedes frontend part of #681).
- Introduce an automated semver release process (release-please **or**
  tag-triggered workflow — see design.md Decision 2) that creates `v*` tags /
  GitHub Releases and rolls `CHANGELOG.md` `[Unreleased]` into a versioned
  section per Keep a Changelog / Constitution §11.
- On release, derive Maven root (and reactor) version and `frontend/package.json`
  `version` from the release tag (`vX.Y.Z` → `X.Y.Z`).
- Document the release process in DevSecOps / development docs + CHANGELOG.
- Static unittests proving frontend publish + release/version contracts.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Frontend container image MUST be published to GHCR with SBOM + cosign sign + SBOM attestation, parity with backend | CU76; #1043 AC; #681 close note | New (frontend path) |
| Releases MUST follow SemVer and `v*` tags; CHANGELOG `[Unreleased]` MUST roll into a versioned section | CONSTITUTION.md §11; Keep a Changelog; #1043 AC | New (automation) |
| Maven and npm package versions MUST be derived from the release tag | #1043 AC | New |
| CD publish of frontend MUST reuse the same success / pin-to-tested-SHA contracts already required for backend (#1042) once that lands | CU76; #1042; serialize queue | Made explicit (coupling) |

## Capabilities

### New Capabilities

- `frontend-ghcr-publish`: CD builds, SBOMs, cosign-signs, attests, and pushes
  the Next.js frontend image to GHCR like the backend.
- `semver-versioned-releases`: Automated semver release process (documented)
  that cuts `v*` releases, rolls CHANGELOG `[Unreleased]`, and sets Maven/npm
  versions from the tag.

### Modified Capabilities

- (none under `openspec/specs/` today cover frontend GHCR publish or semver
  release automation)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | maybe | Root/reactor Maven version bump on release only; Dockerfile path unchanged |
| `frontend` | yes | `package.json` version on release; Dockerfile used by CD (may add `VERSION` build-arg if needed); no product UI |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | maybe | Inherits root Maven version if reactor bump includes it |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | `cd.yml` frontend publish (+ optional release-please / release workflow) |
| Scripts / tests | yes | Static unittests for CD frontend publish + release/version wiring |
| Docs | yes | DevSecOps release docs, CU76 note, CHANGELOG, possibly README badges |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none required for publish; document any GHCR pull
  credentials only if prod compose switches from build to pull (optional, not AC)
- Dependencies: GitHub Actions (release-please or equivalent); no runtime app deps
- BREAKING for API clients: no

### Architecture review

No application-architecture change. Extends existing DevSecOps supply-chain
pattern (#681 backend) to the frontend image and operationalizes Constitution
§11 Release Rules. No new ADR required unless implement chooses a release-please
config that alters default-branch versioning policy in a non-obvious way — if so,
add a short ADR under `docs/200-architecture/202-ADR/` at implement time.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/208-devsecops/README.md` | Document frontend GHCR image name, SBOM/cosign parity, semver release process, version derivation |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note frontend publish + versioned releases for #1043 if CU text lacks it |
| `CHANGELOG.md` | `[Unreleased]` entry for #1043; process itself must keep rolling Unreleased on future cuts |
| `README.md` (optional) | GHCR frontend image / release badge if already present for backend |
| New or updated release runbook under `docs/300-development/` or DevSecOps | How to cut a release / what release-please (or tag flow) does |

## Out of Scope

- Backend SBOM/cosign (already done; #681 closed).
- CD pin-to-tested-SHA (#1042) — implement **after** that lands; this change
  MUST preserve/extend the pin for both images, not reintroduce tip checkout.
- Stop CI bot report commits (#1041) and protect-main ruleset (#1040) —
  serialize before this change; release automation must respect the ruleset
  (PR-only merges; tag/release permissions as designed in #1040).
- Switching `docker-compose.prod.yml` from `build:` to GHCR `image:` pull
  (nice follow-up; not required by #1043 AC).
- TLS termination (#254), backups (#256), product UI features.
- Rewriting git history or inventing past semver tags for closed work.
