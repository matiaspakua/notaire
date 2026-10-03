> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1043 (audit-2026-09, CU76): publish the frontend image and introduce
versioned releases. See proposal.md — Objetivo.

Verified on `origin/main` (2026-10-03, tip `ce97e114` after fetch):

| Location | Finding |
|----------|---------|
| `.github/workflows/cd.yml` | Publishes **only** `IMAGE_NAME: …/backend` from `backend-api/Dockerfile`; Trivy CycloneDX SBOM, cosign sign, cosign attest; `release` job on `refs/tags/v*`; no frontend job |
| `frontend/Dockerfile` | Present; multi-stage Node 22 alpine; `output: "standalone"` in `next.config.ts`; healthcheck via wget |
| `git ls-remote --tags origin` | **Empty** |
| `gh release list` | **Empty** |
| Root `pom.xml` | `<version>1.0-SNAPSHOT</version>` |
| `frontend/package.json` | `"version": "0.1.0"` |
| `CHANGELOG.md` | Keep a Changelog with active `[Unreleased]` (many entries; no versioned release sections yet for a cut) |
| #681 | **CLOSED** 2026-09-25; owner note: backend SBOM/cosign done; frontend unpublished tracked in #1043 |
| Prod compose | `docker-compose.prod.yml` still `build:` frontend locally — pull-from-GHCR optional later |
| Serialize queue | `#1046 → #1042 → #1041 → #1040 → #1043` (or coordinator schedule after #1040) |

## Goals / Non-Goals

**Goals:**

- Frontend image on GHCR with SBOM + cosign sign + SBOM attest, parity with backend.
- Automated semver release (`v*`) + documented operator path.
- CHANGELOG `[Unreleased]` → versioned section on cut.
- Maven + npm versions derived from release tag.
- Preserve #1042 pin-to-tested-SHA and #1041/#1040 constraints once those land.
- Prove contracts with failing-then-green static unittests.

**Non-Goals:**

- Re-doing backend signing (#681).
- Product UI / Playwright product specs (heavy CI Playwright job still must pass).
- Switching prod compose to GHCR pulls (optional follow-up).
- History rewrite or inventing tags for past merges.
- Implementing before #1040 (unless coordinator explicitly reorders).

## Decisions

1. **Extend `cd.yml` with a frontend publish path (matrix or sibling job)**
   - Prefer a **matrix** or duplicated job `build-and-publish-frontend` that
     mirrors backend steps with:
     - `IMAGE_NAME: ${{ github.repository }}/frontend`
     - `file: ./frontend/Dockerfile`, `context: ./frontend` (or `.` if
       Dockerfile expects monorepo root — verify at implement; current
       Dockerfile `COPY package*.json` implies **context `./frontend`**)
     - Same Trivy CycloneDX → upload → cosign sign → cosign attest
     - Same `permissions` (`packages: write`, `id-token: write`)
   - Why: AC1 requires parity with backend; matrix keeps one workflow.
   - Alternative rejected: separate `cd-frontend.yml` — doubles
     `workflow_run` gates and drifts from backend.
   - **Must** apply #1042 checkout/`publish_sha`/`latest`-after-SHA pattern to
     the frontend path when implementing on a main that already has #1042.

2. **Release automation: release-please (chosen at implement)**
   - **Implemented:** `googleapis/release-please-action@v4` with
     `release-please-config.json` + `.release-please-manifest.json` (bootstrap
     `0.1.0`). Tag-triggered-only was the rejected alternative for version
     bumps; CD still runs on `v*` for image publish.
   - Config:

     - Opens a release PR on Conventional Commits
     - Updates `CHANGELOG.md` (Keep a Changelog compatible)
     - Bumps versions in root `pom.xml` (+ modules if needed) and
       `frontend/package.json`
     - Creates GitHub Release + `vX.Y.Z` tag on merge
   - **Acceptable alternative:** tag-triggered workflow: operator (or script)
     creates `vX.Y.Z` → workflow rolls CHANGELOG, sets Maven/npm versions,
     commits/tags if needed, creates GH Release; existing `cd.yml` `on.push.tags: v*`
     already publishes images + softprops release notes — extend rather than
     duplicate release notes if release-please owns them.
   - Why prefer release-please: matches Conventional Commits already required
     by Constitution; automates Unreleased→version; reduces manual SNAPSHOT
     edits.
   - Bootstrap: first release from `1.0-SNAPSHOT` / `0.1.0` → document
     choosing `v1.0.0` (or `v0.1.0`) explicitly in the release runbook; do not
     invent many historical tags.

3. **Version derivation contract**
   - Tag `vX.Y.Z` ↔ Maven `${project.version}=X.Y.Z` ↔ npm `"version": "X.Y.Z"`.
   - Between releases: **main stays at last released versions** until the next
     release PR merges (documented in `docs/300-development/RELEASE.md`).
   - Use release-please `extra-files` so Maven and npm cannot drift.

4. **CD `release` job coexistence**
   - Today `cd.yml` `release` uses `softprops/action-gh-release` on `v*` with
     `generate_release_notes: true`.
   - If release-please already creates the GitHub Release, **disable or skip**
     softprops to avoid duplicate releases; if tag-triggered only, keep/extend
     softprops and attach SBOMs for **both** images when available.
   - Attach frontend SBOM artifact to the release when practical.

5. **TDD via static unittests**
   - `scripts/test_frontend_ghcr_publish.py` (name flexible): assert `cd.yml`
     references `frontend/Dockerfile`, frontend image name, SBOM + cosign sign
     - attest steps for that image.
   - `scripts/test_semver_release_process.py`: assert release-please workflow
     **or** tag-triggered release workflow exists; assert documented version
     bump targets (`pom.xml`, `frontend/package.json`) are wired (config paths
     or script references); assert CHANGELOG process is referenced in docs or
     release config.
   - Prove red on current `origin/main` (no frontend IMAGE; no release-please /
     empty tags) before implementing.

6. **Serialize after #1040**
   - Queue `#1046 → #1042 → #1041 → #1040 → #1043`.
   - Reasons: `cd.yml` thrash with #1042/#1041; ruleset (#1040) must allow
     release-please PRs and define whether Actions may create tags/releases;
     bot bypass removal after #1041 must not block the release bot if one is used
     — configure least-privilege (contents write on release workflow only).

## Riesgos / Trade-offs

- **[Risk] Concurrent edits to `cd.yml` with #1042/#1041** → Mitigate by
  implementing only after those merge; rebase onto updated main.
- **[Risk] release-please vs Keep a Changelog `[Unreleased]` format drift** →
  Configure release-please changelog sections or a post-step that preserves
  Keep a Changelog headings; document the chosen format.
- **[Risk] Maven multi-module version skew** → Bump root reactor version (and
  any hardcoded child versions if not `${project.version}`); verify with
  `mvn help:evaluate` or a script in the release job.
- **[Risk] Frontend build needs `NEXT_PUBLIC_API_URL` at image build** → Existing
  Dockerfile ARG default; CD MUST pass production-appropriate build-args
  (document; do not hardcode secrets). Prefer runtime/config rewrite already
  used in compose when possible.
- **[Risk] First release from SNAPSHOT surprises consumers** → Document bootstrap
  version choice; smoke GHCR tags after first cut.
- **[Risk] Ruleset blocks tag push / release PR** → Coordinate with #1040
  desired-state (required checks on PR; Actions permissions for releases).
- **[Trade-off] Matrix vs two jobs** → Matrix is DRY; two jobs are clearer for
  artifact names (`sbom` vs `sbom-frontend`) — either OK if tests assert both
  images are signed.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Frontend Dockerfile is CD build context | unit | `scripts/test_frontend_ghcr_publish.py` |
| Frontend tags mirror backend conventions | unit | same (YAML tag wiring / IMAGE_NAME) |
| CycloneDX SBOM for frontend | unit | same (trivy format cyclonedx + frontend image-ref) |
| Cosign sign frontend | unit | same |
| Cosign attest frontend SBOM | unit | same |
| Automated release workflow exists | unit | `scripts/test_semver_release_process.py` |
| Release process documented | review / doc assert | same or docs checklist + string presence |
| Unreleased rolled on release | unit / config assert | release-please config or roll script referenced |
| Maven version matches tag | unit | release config/script targets root `pom.xml` |
| npm version matches tag | unit | release config/script targets `frontend/package.json` |

- New unit tests: stdlib unittest + PyYAML (match existing CI invariant scripts).
- New integration tests: none required for Gate 2 (workflow/config); Gate 5
  smoke uses real GHCR/release after merge.
- Coverage impact (JaCoCo): none (no Java product code).

## Regression Strategy

- Existing tests affected: any script that assumes `cd.yml` has a single
  backend-only publish job (`test_ci_workflow_invariants.py`, #1042 pin tests,
  report-job tests) — update assertions to allow frontend matrix/job without
  weakening backend pin/success gates.
- Full suite: new scripts green; `bash scripts/preflight.sh` as applicable;
  heavy CI `bash scripts/check-heavy-ci.sh <pr>`.
- Confirm backend publish path still SBOM/signs after the edit.
- HTTP/Bruno: n/a for product API delta.

## Playwright Strategy

- No UI product change. No new Playwright scenarios.
- PR still must pass repository heavy CI (includes Playwright) before merge.
- Mark product E2E n/a for this capability; do not skip the PR Playwright job.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling:
  1. Merge workflow + release tooling to `main` via PR (ruleset from #1040)
  2. Cut first release (`vX.Y.Z`) via the documented process
  3. Tag push / release triggers CD → backend **and** frontend images published,
     signed, attested
  4. GitHub Release visible; CHANGELOG versioned; Maven/npm = `X.Y.Z`
- Configuration or `.env` keys: none for publish; document optional GHCR pull
  for compose later
- Feature flag: no
- Smoke test after deploy (Gate 5): GHCR shows frontend package with digests;
  `cosign verify` / attest inspect succeeds; Release + tag exist; CHANGELOG has
  version section; `pom.xml` + `package.json` versions match tag

## Rollback Strategy

- Revert safe: yes — revert workflow/release config (+ tests/docs); no schema
  change
- Database rollback: none needed
- Data written under the new behavior after revert: GHCR images and GitHub
  Releases/tags already published remain (immutable); operators may leave them
  or mark releases as prerelease/draft if mistaken
- Blast radius if rollback delayed: medium for mistaken version bumps on main —
  fix with a follow-up release PR; do not force-push tags if #1040 blocks it

## Migration Plan

1. Wait for queue `#1046 → #1042 → #1041 → #1040` (or coordinator schedule).
2. Copy this draft into `openspec/changes/ci-1043-frontend-image-semver-releases/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh ci-1043-frontend-image-semver-releases`.
4. Branch `cursor/ci-1043-frontend-image-semver-69d3` → failing unittests →
   implement CD frontend publish + release automation + docs → green → PR
   `Closes #1043`.
5. After merge: cut bootstrap release; Gate 5 smoke both images + versions.

## Open Questions

- Exact first version number (`v1.0.0` vs `v0.1.0`) — choose at implement with
  owner preference; document in runbook (does not change ACs).
- release-please vs pure tag-triggered — default release-please; switch only if
  Maven multi-module bump proves awkward; record choice in design at implement
  if it differs.
- Whether prod compose should switch to GHCR `image:` in a follow-up issue —
  out of scope here.
