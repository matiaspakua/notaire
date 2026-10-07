# Semver release runbook (issue #1043)

Automated Semantic Versioning for Notaire via **release-please**, with CD
publishing **both** backend and frontend images to GHCR on `v*` tags.

Governed by [CONSTITUTION.md](../../CONSTITUTION.md) §11 Release Rules and
[CU76](../100-business/102-use-cases/CU76%20–%20Quality%20Assurance%20and%20Testing%20Infrastructure.md).

## What the automation does

| Piece | Role |
|-------|------|
| `.github/workflows/release-please.yml` | On push to `main`, opens/updates a release PR; after that PR merges, creates the GitHub Release and `vX.Y.Z` tag |
| `release-please-config.json` | Changelog path, `include-v-in-tag`, Maven/npm `extra-files` bumps |
| `.release-please-manifest.json` | Last released version (bootstrap `0.1.0`) |
| `.github/workflows/cd.yml` | On `v*` (and CI success on `main` / dispatch): build, SBOM, cosign sign+attest, push `ghcr.io/<org>/notaire/backend` and `…/frontend` |

release-please **owns** GitHub Release creation. The CD `release` job only
**attaches** CycloneDX SBOM assets (`sbom-backend.cdx.json`,
`sbom-frontend.cdx.json`) to the existing release (no duplicate notes).

## Version derivation contract

On the release commit for tag `vX.Y.Z`:

| Artifact | Version |
|----------|---------|
| Git tag | `vX.Y.Z` |
| Root `pom.xml` `<version>` | `X.Y.Z` |
| `backend-api` parent `<version>` | `X.Y.Z` |
| `frontend/package.json` `"version"` | `X.Y.Z` |
| `CHANGELOG.md` | New `## [X.Y.Z] - YYYY-MM-DD` (or release-please equivalent) from prior unreleased commits; fresh unreleased section remains for new work |

Between releases, main stays at the last released versions until the next
release PR merges (release-please interim PR may bump versions on that branch
only).

## Release PR permissions (#1264)

The workflow uses the default `GITHUB_TOKEN`. It can open the release PR only
while the repository setting **Settings → Actions → General → Allow GitHub
Actions to create and approve pull requests** is enabled; turning it off makes
every `Release Please` run fail with "GitHub Actions is not permitted to create
or approve pull requests".

A PR opened or updated with `GITHUB_TOKEN` does not trigger other workflows, so
the release PR starts without the checks protect-main requires (#1040). Before
merging it, the Owner closes and reopens the release PR (or pushes a commit to
its branch) so `CI`, `Frontend CI`, `Playwright E2E`, `Code Lint` and
`PR Validation` run. Switching the action's `token` to a fine-grained token or
GitHub App would remove this manual step.

`release-please-config.json` sets `bootstrap-sha` to the commit that introduced
release-please (`8ab8a6e`, #1043), so older merge commits are not parsed into
the first release notes.

## Bootstrap (first cut)

Tags/releases were empty before #1043. Manifest starts at `0.1.0` (aligned with
the prior frontend package version). Recommended first public cut:

1. Ensure Conventional Commits on `main` since the last manifest version.
2. Merge the release-please PR (title like `chore(main): release 0.2.0` or
   `1.0.0` depending on commit types — use `feat!:` / `BREAKING CHANGE` for
   major; prefer an explicit `v1.0.0` when the team wants a 1.x line).
3. Confirm the tag `vX.Y.Z` and GitHub Release exist.
4. Confirm CD published both images for that tag SHA; verify cosign:
   `cosign verify ghcr.io/<org>/notaire/frontend@<digest>` (and backend).
5. Confirm `CHANGELOG.md` has a versioned section and Maven/npm match `X.Y.Z`.

To force a specific first version, edit `.release-please-manifest.json` and/or
use release-please’s bootstrap options before the first release PR — document
the chosen tag in the PR that cuts it.

## Operator steps (ongoing)

1. Land features/fixes on `main` via PRs with Conventional Commits
   (`feat:`, `fix:`, `feat!:`, …). **protect-main** (#1040) requires PR checks;
   release-please does **not** need a durable Actions bypass.
2. Wait for `Release Please` workflow on `main` to open/update the release PR.
3. Review the release PR (CHANGELOG + version bumps in `pom.xml` /
   `frontend/package.json`). Merge it through the normal required checks.
4. release-please creates `vX.Y.Z` + GitHub Release on the following run.
5. Tag push triggers CD → backend **and** frontend images (SHA + semver tags;
   `latest` only on the default-branch publish path, SHA-before-latest per #1042).
6. Smoke: GHCR packages, cosign verify/attest, CHANGELOG section, Maven/npm =
   `X.Y.Z`.

## Frontend image notes

- Dockerfile: `frontend/Dockerfile` (Node 22 alpine, Next.js standalone).
- CD context: `./frontend`. Upstream API base is **runtime** `BACKEND_URL`
  (server-only Route Handler BFF, issue #1055) — do not bake Docker-internal
  hosts into `NEXT_PUBLIC_*` build args. Compose/env sets `BACKEND_URL` per
  environment after image publish.
- Prod compose may still `build:` locally; switching to GHCR `image:` pull is
  a follow-up, not required by #1043.

## protect-main interaction (#1040)

- Release PRs are normal PRs: required checks `CI`, `Frontend CI`,
  `Playwright E2E`, `Code Lint`, `PR Validation`.
- Tag + Release creation uses `contents: write` on the release-please workflow
  only (least privilege). No durable ruleset bypass for Actions bots.

## Related

- DevSecOps pipeline: [208-devsecops/README.md](../200-architecture/208-devsecops/README.md)
- CD pin-to-tested-SHA: issue #1042
- Static guards: `workspace/tests/test_frontend_ghcr_publish.py`,
  `workspace/tests/test_semver_release_process.py`
