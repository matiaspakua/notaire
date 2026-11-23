> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

CD checks coherence on the tag (`cd.yml` *Release identity coherence*). A tag created without a train therefore produces a Release with no images, and the tagged tree cannot be repaired without a new tag. 0.9.0 hit this; 0.10.0 would too.

## Goals / Non-Goals

**Goals:** - 0.10.0 cuts with images and all five assets.
- The next missing train is visible on the Release Please run, before merge.
**Non-Goals:** - Images for `v0.9.0`.
- Running the full preflight on the bot release PR.

## Decisions

- **Gate in the Release Please job, not in the unit suite.** A unit test asserting the next minor's train would turn every unrelated PR red right after a release; the job fails exactly where the cut is prepared.
- **Train title** names what 0.10.0 ships: the production-line dashboard, step inputs/outputs and the change-aware preflight gates. Window 2027-03 keeps GA in 2027-04.
- **Latest flag** is fixed with `gh release edit v0.9.0 --latest`; the Release is not recreated (`RELEASE-BACKFILL.md`).

## Riesgos / Trade-offs

- The new step fails the Release Please job until a train exists: intended, and the message names the files to edit.
- External state (milestone, Latest flag) is not in git; recorded in `tasks.md` with the commands used.

## Testing Strategy

Unit: `train_for` resolves `0.10.z`; a minimal `0.10.0` tree passes coherence for `v0.10.0`; the nomenclature tuple includes the train; `release-please.yml` runs the coherence step after the OpenAPI sync with `--tag "v$VERSION"`. Real check: coherence `--tag v0.10.0` on the regenerated release branch.

## Regression Strategy

Existing train, coherence and release-workflow tests in `workspace/tests` and `workspace/sdlc/test_milestone_policy.py` stay green; `train_for("1.1.0")` still raises.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
