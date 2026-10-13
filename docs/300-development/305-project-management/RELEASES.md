# Releases and trains

A **milestone** answers “when is this issue scheduled?” A **tag** answers
“what did we ship?” They were easy to confuse because both use a `v` prefix.
They are different clocks.

| Clock | Example | Closes when |
|-------|---------|-------------|
| Release gate train | `2026-11 · Release blockers` | A person closes the milestone after its exit criteria, not when a tag is pushed |
| Product semver | `v0.1.2` today; `vX.Y.Z` from the next cut | release-please merges the release PR |
| Database schema | Flyway `V46` | A migration is applied. The number is not the semver |

The board has no Released column. Shipped work is already **Done** when its
issue closes. The changelog and the GitHub Release are the record of which
cut contained that issue. Adding a fourth status would copy the tag.

## What was wrong

Reviewed against the 0.2.0 release-please pull request and the live releases:

1. **The tag never published.** `include-component-in-tag` defaulted on, so
   cuts are `notaire-v0.1.1` and `notaire-v0.1.2`. CD listened for `v*`.
   The v0.1.2 GitHub Release has an empty asset list: no SBOM was attached,
   and the semver Docker tag was never applied. Image publish still happened
   on `main` via `workflow_run`, under SHA tags only.
2. **The backend was not in the release.** Root `pom.xml` and the backend
   parent stayed at `1.0-SNAPSHOT`. release-please rewrites a Maven version
   only when that string already equals the last released version, so the
   extra-file entries were skipped. OpenAPI was hardcoded `1.0.0`.
   `app.version` fell through to `1.0-SNAPSHOT` because Maven filtering and
   Spring placeholders share `${}`.
3. **The database was not in the release.** Flyway is the schema source of
   truth and was invisible on the tag. Forcing the schema id to equal the
   semver would break Flyway. The schema is recorded beside the product version.
4. **Trains and tags shared a spelling.** The former `v1.0 — Production Ready`
   and `v1.1 — Hardening` priority buckets are now closed history milestones.
   Open trains use dates and purposes, so no live milestone resembles a SemVer
   tag. [MILESTONES.md](MILESTONES.md) is the schedule; this file maps a
   product series to its release gate train.
5. **Delivery is broader than one release gate.** Frontend product, quality,
   platform, and legacy trains may all contribute to a cut. The release record
   names the gate train; it does not move or close the other milestones.
6. **The SBOM was unnamed.** Trivy wrote an image bill and the release job
   was supposed to attach it. Nothing defined the bill of the product, and
   the attach step never ran (see 1).
7. **Nothing showed the version.** Actuator info did not carry the product
   version, the shell did not, and GitHub Pages told the evolution story
   without listing cuts.
8. **The AI SDLC scheduled lanes and never said what a release is.** A
   `release-please--branches--*` branch is not a lane an agent should edit.

## Rules

- One product version. Backend Maven version, frontend npm version, the
  release-please manifest, and `release/product-release.json` are that version.
- The database component is `components.database.schemaVersion` (highest
  Flyway `V*`). It is allowed to differ from the semver. It must be updated
  in the same change as a new migration (`python3 workspace/release/product_release.py --write`).
- `0.x` and `1.0.x` advance gate train `2026-11 · Release blockers`. They do not close it.
  `v1.0.0` is a human decision when release blockers are closed
  or explicitly deferred.
- `1.1.x` advances `2027-01 · Quality & traceability`. Any other minor fails
  `train_for()` until this file and the function name the new train.
- Tags from the next cut are `vX.Y.Z` (`include-component-in-tag: false`).
  CD still accepts `notaire-v*` so 0.1.1 and 0.1.2 can be republished.
- A release does not move the board and does not close RF, CU, or RNF anchors.
- The foreman does not implement on a release-please branch. Process fixes
  land on their own issue branch, then release-please opens the next cut.

## Where the version is visible

| Surface | What it shows |
|---------|----------------|
| `release/product-release.json` | Backend, frontend, database schema |
| GitHub Release | Changelog plus the SBOM assets in [SBOM.md](../SBOM.md) |
| `/actuator/info` | `notaire.version` and `notaire.components.databaseSchema` |
| OpenAPI `info.version` | Product version |
| Frontend sidebar | Product version from `package.json` |
| GitHub Pages `/` and `/docs/releases/` | Cut history and the three components of the current version |

Pages history is generated at site build from `CHANGELOG.md` and the product
record (`prebuild` in `github-page/package.json`). The project-history timeline
stays the narrative; it is not rewritten when a tag is cut.

## Check

```bash
python3 workspace/sdlc/check-product-version-coherence.py
python3 workspace/release/product_release.py --check
python3 -m unittest workspace.tests.test_product_release
```

Tags must be `vX.Y.Z` equal to the tree version; legacy `notaire-v0.1.1` and
`notaire-v0.1.2` are shape-only exceptions (#1591).
