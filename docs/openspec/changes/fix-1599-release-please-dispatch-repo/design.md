> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

#1591 added a dispatch step after release-please creates a tag, because a tag
created with `GITHUB_TOKEN` does not start `push: tags` workflows. The step is:

```yaml
- name: Dispatch CD on release tag
  if: steps.release.outputs.release_created == 'true'
  env:
    GH_TOKEN: ${{ github.token }}
  run: |
    TAG="${{ steps.release.outputs.tag_name }}"
    gh workflow run cd.yml --ref "$TAG"
```

There is no `actions/checkout` on that job. `gh` then runs `git` to discover the
repo and exits 1. Evidence: Actions run 38366521356 on 2026-10-13, tag `v0.2.1`,
Release assets `[]`. `workflow_run` CD on `main` still skips the attach job
because `github.ref` is not a tag.

## Goals / Non-Goals

**Goals:**

- Dispatch `cd.yml` on the created tag using only the repository token.
- Name the repository so `gh` does not need a working tree.
- Keep the `release_created` guard and `actions: write`.

**Non-Goals:**

- Checking out the repository solely to please `gh`.
- A PAT / GitHub App for release-please (ADR-028 already rejected unless this
  dispatch still fails after `GH_REPO`).
- Changing CD attach, identity, or extra-files.
- PDF report work.

## Decisions

1. **Set `GH_REPO: ${{ github.repository }}` on the existing step.** That is the
   documented way to point `gh` at a repo without git. Alternative: `gh workflow
   run -R ${{ github.repository }}`. Equivalent; env is one line and keeps the
   run script unchanged. Alternative: add `actions/checkout` first — extra step
   for a problem `GH_REPO` already solves.
2. **Do not add a PAT.** ADR-028 still stands; this bug was missing repo
   identity, not missing token scopes on GitHub-hosted runners.
3. **Repair `v0.2.1` additively** after CD can run on the tag (or after SHA
   images exist for `d073d2ad`). No tag move, no `--clobber`. Fleet tokens cannot
   `workflow_dispatch` (403); the workflow fix is what future cuts use.

## Riesgos / Trade-offs

- [Fleet token cannot dispatch CD] → Mitigation: land the workflow fix; Owner
  or Actions on the next cut starts CD. Additive backfill remains in
  `RELEASE-BACKFILL.md`.
- [GH_REPO typo leaves the same git error] → Mitigation: unit test asserts the
  exact `${{ github.repository }}` expression.
- [Concurrent `workflow_run` CD on main] → Attach job still skipped on branch
  refs; tag dispatch is the attach path. No change to that split.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Dispatch names the repository without a git checkout | unit | `DispatchOnTagTest.test_dispatch_names_repository_without_git_checkout` |
| Release creation dispatches delivery on the tag | unit | `DispatchOnTagTest.test_release_please_dispatches_cd_on_tag` (existing) |
| No release created, no dispatch | unit | existing `release_created` guard assertion |
| Manually pushed tags still publish | unit | `DispatchOnTagTest.test_cd_keeps_tag_filters` (existing) |

- New unit tests (`src/test/java/.../unit/`): n/a — Python workflow guard only
- New integration tests (`src/test/java/.../integration/`): n/a
- Coverage impact (JaCoCo ratchet floor; 80% target): none (no Java change)

TDD: add the GH_REPO assertion, observe it fail on current YAML, then set the env.

## Regression Strategy

- Existing tests affected: `workspace/tests/test_release_asset_attachment.py` —
  tighten dispatch, do not weaken `actions: write` or tag filters.
- Full suite command: `python3 -m unittest workspace.tests.test_release_asset_attachment` plus `bash workspace/sdlc/preflight.sh` before push. Java `mvn verify` is unchanged and not required for this CI-only delta.
- HTTP/Bruno API suite: n/a — no endpoint change
- Legacy paths at risk: none

## Playwright Strategy

n/a - no UI surface. This change edits `.github/workflows/release-please.yml`
and documentation only.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not run for this change

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge to `main`; the next release-please tag
  creation run picks up the env. `v0.2.1` is repaired additively, not by moving
  the tag.
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): next `v*` Release has a `workflow_dispatch`
  CD run on that tag and lists exactly five assets. Until then, `v0.2.1`
  backfill when SHA images for `d073d2ad` exist.

## Rollback Strategy

- Revert safe: yes — reverting removes `GH_REPO` and restores the `v0.2.1`
  failure mode; no data written
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: none; worst case is another empty
  Release until backfill

## Migration Plan

1. Merge this PR to `main`.
2. Do not merge a release-please cut until this is on `main` (already true for
   `v0.2.1`; next cut waits).
3. Backfill `v0.2.1` five assets without moving the tag once image digests for
   `d073d2ad` exist, or when Actions can dispatch `cd.yml --ref v0.2.1`.
