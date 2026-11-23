# Releases and trains

A **milestone** answers “when is this issue scheduled?” A **tag** answers
“what did we ship?” They were easy to confuse because both use a `v` prefix.
They are different clocks.

| Clock | Example | Closes when |
|-------|---------|-------------|
| Release train | `v0.3 · Hexagonal core & security (2026-11)` | A person closes the milestone after its exit criteria, not when a tag is pushed |
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
- Each minor has one train titled `vX.Y · <Theme> (<YYYY-MM>)`; every `vX.Y.z`
  patch advances it ([MILESTONE-NOMENCLATURE.md](MILESTONE-NOMENCLATURE.md)).
  `train_for()` matches the `vX.Y · ` prefix and fails closed for a minor
  with no train. Tags do not close milestones.
- `v1.0.0` is a human decision once `v0.3`…`v0.6` are closed or explicitly
  deferred.
- Tags from the next cut are `vX.Y.Z` (`include-component-in-tag: false`).
  CD still accepts `notaire-v*` so 0.1.1 and 0.1.2 can be republished.
- A release does not move the board and does not close RF, CU, or RNF anchors.
- The foreman does not implement on a release-please branch. Process fixes
  land on their own issue branch, then release-please opens the next cut.

## Worked example: the 0.8.0 / 0.9.0 cuts (#2408)

The two clocks diverged in November 2026 and show why they are separate:

- `0.8.0` was cut (`18428a4d`) but release-please published no tag or Release.
  The fix commits that added the `v0.8` train and the Pages history came later
  while the tree still said `0.8.0`; that coherent tree is the valid place to
  tag `v0.8.0`. Tagging the original release commit would fail the coherence
  gate because its tree has no `v0.8` train.
- `0.9.0` was cut and tagged, but no `v0.9 · …` train existed. CD stopped at
  `release-identity` (`no milestone titled v0.9 · … for 0.9.0`), so no images
  or Release assets were published. Adding the train to the three mirrors
  (`RELEASE_TRAINS`, `milestone_policy.py`, the nomenclature table) restores
  coherence; the existing tag and Release are repaired additively, never
  recreated or moved.

The rule that matters: a tag is valid only when the *tagged tree* is coherent.
A train fix that lands after a cut belongs to the fix's release, not retroactively
to the cut it describes.

### The release branch proves its train (#2448)

0.10.0 nearly repeated the 0.9.0 failure: the release PR read `CLEAN` with no
`v0.10 · …` train, because no CI runs on the bot release branch (#2001) and CD
checks coherence only once the tag exists. The Release Please job now runs
`check-product-version-coherence.py --tag v<next>` on the release branch after
the OpenAPI sync. A missing train fails that job, naming the three mirrors to
edit, while the cut can still be fixed on `main`. Add the next minor's train
before merging the release PR.

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
