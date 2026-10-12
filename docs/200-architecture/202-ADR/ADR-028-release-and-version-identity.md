# ADR-028: Release and Version Identity

**Status:** Proposed (Accepted when the PR for #1591 merges)  
**Date:** 2026-10-12  
**Deciders:** Engineering, Owner review (issue #1591 / CU76)  
**Related:** ADR-007 (Flyway), ADR-016 (observability), ADR-026 (module separation), #1043, #1571, #1572  
**Supersedes:** nothing. Completes the product-release decisions recorded in #1571.

## Context

One release-please cut produces one git tag. Notaire ships three things under
it: the Spring Boot backend (Maven), the Next.js frontend (npm) and the
PostgreSQL schema (Flyway). Delivery trains are dated milestones. #1572 defined
the record, the SBOM bills and the version surfaces, but left four gaps:

- the check that keeps the sources equal is not a named, blocking gate;
- the `v0.2.0` Release holds no assets, because a tag created with the
  repository token does not start `push: tags` workflows;
- the version reported by telemetry and logs was never defined, and the record
  file is not inside the backend image;
- compose and Kustomize still carry placeholder image tags.

## Decision

1. **Semver is one atomic unit of backend, frontend and database.** A tag
   `vX.Y.Z` names one commit where the root and backend Maven versions,
   `frontend/package.json`, `.release-please-manifest.json` and
   `release/product-release.json` all read `X.Y.Z`. The Flyway schema id is
   recorded beside it and is never required to equal the semver (ADR-007).
   The three never ship under separate versions because a schema change
   without its matching code is not deployable, and release-please can bump all
   four sources in one commit.
2. **Trains stay milestones, not tags.** The train is derived from the semver
   by `train_for()` and documented in `RELEASES.md`. A train title is never a
   tag, a release does not close catalog issues, and the board keeps its three
   columns.
3. **Legacy `notaire-v*` tags are supported for exactly two versions.**
   `notaire-v0.1.1` and `notaire-v0.1.2` keep their tags. CD still accepts the
   prefix for a manually pushed tag, and the checker validates the shape of a
   legacy tag without comparing it to the current tree. A later version with
   that prefix is rejected. Legacy releases get only the assets their tree can
   prove; the backfill runbook lists each omission.
4. **0.x to 1.0.** `0.x` and `1.0.x` advance the `2026-11 · Release blockers`
   train and do not close it. `v1.0.0` is an Owner decision when the blockers
   are closed or deferred. A minor with no train mapping fails closed until the
   map and `RELEASES.md` name its train. Breaking changes before 1.0 bump the
   minor (`bump-minor-pre-major`).
5. **The running version comes from the artifact.** The backend reports the
   Maven-filtered `app.version` that is packaged in the jar as the
   OpenTelemetry `service.version` and as `serviceVersion` on every JSON log
   line. CI validates the packaged `app.version` against
   `release/product-release.json`. The record file is **not** read at runtime
   because it is not packaged; copying it into the image was rejected as a
   second runtime source of the version.
6. **One blocking check, no credentials.** `workspace/sdlc/check-product-version-coherence.py`
   compares every version source, the release tag, the generated SBOM documents,
   the five-asset list and the image identity. It runs in local preflight, in the
   process checks, after the backend build, and before any image is built in CD.
   It reads the working tree only.
7. **Image identity moves with the cut.** Backend and frontend images are
   referenced by `X.Y.Z` in both compose files and the staging overlay through
   `x-release-please-version` markers, so release-please rewrites them in the
   same cut. CD tags images from the pushed semver tag, which the check proves
   equals Maven and npm.
8. **Delivery is started explicitly and attaches five assets or fails.**
   `release-please.yml` dispatches `cd.yml` on the tag it creates, which is the
   event GitHub allows for the repository token. The release job fails unless
   exactly `sbom-notaire.cdx.json`, `sbom-database.cdx.json`,
   `sbom-backend.cdx.json`, `sbom-frontend.cdx.json` and `product-release.json`
   are attached.
9. **Repair is additive.** A missing asset set is backfilled from the images
   already published for the tagged commit, with no rebuild, no tag move, no
   release edit and no overwrite. Adding the semver image tag to an existing
   digest is allowed because it changes no digest.

## Options considered

| Option | Verdict |
|--------|---------|
| Separate versions for backend, frontend and schema | Rejected: three tags to coordinate and no single deployable identity |
| Release train as the product version | Rejected: milestones close on delivery state, tags are immutable history |
| Read `product-release.json` at runtime for `service.version` | Rejected: not in the image; a second source of truth; extra class for a property |
| PAT or GitHub App so release-please tags start CD | Rejected for now: a secret to rotate; dispatch works with the repository token; revisit if dispatch proves insufficient |
| Re-run CD at `v0.2.0` to produce assets | Rejected: rebuilds images and creates a new digest |
| `--clobber` or delete-and-recreate for backfill | Rejected: destructive; changes timestamps and links |
| Hand-edited image tags in compose and Kustomize | Rejected: drifts from the cut |
| Product bill for legacy tags from the current script | Rejected: attests a coherence those tags never had |

## Consequences

- Positive: drift fails before publish; every release has five assets or a red
  pipeline; logs and traces name the release; deployment files follow the cut.
- Negative: the `release-identity` job sits on the path of every CD run; a
  wrong rule would block publishing (one-line rollback documented in the
  change design).
- Negative: `x-release-please-version` markers are a convention release-please
  must keep honouring; a dry run is part of the change tasks.
- Neutral: legacy releases stay partial by design and say so.
- Follow-up: if the Owner moves to a PAT or GitHub App, delete the dispatch
  step; the `push: tags` filters already cover that path.

## References

- Runbook: `docs/300-development/RELEASE.md`, `docs/300-development/RELEASE-BACKFILL.md`
- SBOM contract: `docs/300-development/SBOM.md`
- Trains and tags: `docs/300-development/305-project-management/RELEASES.md`
- Change record: `docs/openspec/changes/chore-1591-release-foundation-completion/`
