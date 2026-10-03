# frontend-ghcr-publish Specification

## Purpose
Publish the Next.js frontend container to GHCR with SBOM generation, cosign
keyless signing, and SBOM attestation, matching the backend supply-chain path
already shipped for #681. Source: #1043; CU76.
## Requirements
### Requirement: CD builds and pushes the frontend image to GHCR

The CD pipeline MUST build `frontend/Dockerfile` and push the resulting image
to `ghcr.io/${{ github.repository }}/frontend` (or the repository’s established
GHCR naming for frontend) with tags comparable to the backend image (SHA,
semver when on a `v*` tag, and `latest` on the default-branch publish path
subject to the pin-to-tested-SHA ordering from #1042 once that is on `main`).

#### Scenario: Frontend Dockerfile is the CD build context

- **WHEN** CD runs a frontend image publish job (or matrix entry)
- **THEN** the Docker build uses context/file under `frontend/`
  (`./frontend/Dockerfile` or equivalent documented path) and pushes to the
  frontend GHCR repository name (not only `…/backend`)

#### Scenario: Frontend image tags mirror backend conventions

- **WHEN** CD successfully publishes the frontend image after CI success on
  `main` or on a `v*` tag / `workflow_dispatch`
- **THEN** published tags include an immutable SHA tag for the publish commit
  and, when applicable, semver and `latest` according to the same rules as the
  backend publish path (including SHA-before-`latest` once #1042 is merged)

### Requirement: Frontend image has SBOM, cosign signature, and SBOM attestation

Every published frontend image digest MUST have a CycloneDX SBOM artifact,
be signed with cosign keyless/OIDC, and carry a cosign CycloneDX attestation
of that SBOM — the same controls applied to the backend image in `cd.yml`.

#### Scenario: CycloneDX SBOM is generated for the frontend image

- **WHEN** CD has pushed a frontend image digest
- **THEN** a Trivy (or equivalent) step produces a CycloneDX SBOM for that
  digest and uploads it as a workflow artifact (distinct from a vulnerability
  scan mislabeled as SBOM)

#### Scenario: Frontend image is cosign-signed keyless

- **WHEN** CD publishes a frontend image digest
- **THEN** a cosign sign step signs
  `${REGISTRY}/${FRONTEND_IMAGE}@${digest}` with keyless OIDC (`id-token: write`)

#### Scenario: Frontend SBOM is attested with cosign

- **WHEN** the frontend CycloneDX SBOM file exists after publish
- **THEN** `cosign attest` attaches that SBOM as a CycloneDX predicate to the
  frontend image digest

