> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`product_release.train_for()` derives a train from `RELEASE_TRAINS` and fails closed. A test keeps
that tuple equal to `milestone_policy.SHIPPED ∪ SCHEDULED`. The `v0.8` train was added in #2401,
but the `0.9.0` cut landed without a `v0.9` train: `main` now fails `product version coherence`,
and CD run `45067381490` stopped at `release-identity`, so v0.9.0 has neither images nor assets.
Separately, the `0.8.0` release commit `18428a4d` was merged on 2026-11-22 but release-please
never created the tag or Release; it predates the `v0.8` train fix, so tagging that exact tree
would fail coherence.

## Goals / Non-Goals

**Goals:**

- Coherent `0.9.0` tree and a green coherence gate on `main` and for `--tag v0.9.0`.
- A pushed `v0.8.0` tag and GitHub Release whose tree is coherent.
- Both Releases carry the five documented assets and keep their notes/dates/targets.
- Pages release history reports `0.9.0`.

**Non-Goals:**

- Moving or recreating the existing `v0.9.0` tag or Release.
- Changing CD triggers, permissions or the release-please configuration.
- Merging unrelated open feature branches.

## Decisions

1. **Add a `v0.9` train, not a generic fallback.** `train_for` stays fail-closed so a future minor
   is an explicit decision. Chosen title: `v0.9 · Factory control plane (2027-03)` — it covers the
   production line, intents, live state, store, config-as-code and telemetry shipped in 0.9.0.
   Alternative rejected: matching `v0.8` by prefix, which would give two minors one train and
   break the one-train-per-minor rule.
2. **Tag `v0.8.0` at the last coherent 0.8.0-versioned commit.** The release commit predates the
   `v0.8` train fix. The later fix commits (`55d876f`, `1ffd2cd`) keep the tree at `0.8.0` and add
   the train; the merge `94bf5610` is the last 0.8.0-versioned commit on `main`. Tagging there
   keeps the tag on an unmodified, coherent 0.8.0 tree.
3. **Backfill assets, never recreate Releases.** Generate product/database SBOMs from the tagged
   tree and upload only missing assets with `gh release upload` (no `--clobber`). Image SBOMs are
   generated from published digests when available; if none were published, record that honestly
   instead of fabricating a bill.
4. **Regenerate Pages history** with `product_release.py --write-pages`; it is generated content
   committed to the repo and currently stale at `0.8.0`.

## Riesgos / Trade-offs

- [The `v0.9` train title is a new naming decision] → Keep it inside `v0.9 · …` so the prefix
  contract holds; document it in the nomenclature table and create the exact GitHub milestone.
- [The `v0.8.0` tag is not on the literal release commit] → The chosen commit has the same product
  version and schema, and is the first coherent 0.8.0 tree; record the SHA and rationale in
  traceability and the Issue.
- [CD on the existing `v0.9.0` tag still reads the old tree] → Do not move the tag. Backfill the
  Release assets; future cuts inherit the fixed train. Note the CD gap in the Issue.
- [Uploading an existing asset would mutate a Release] → Check the asset list first and upload
  only missing names; never pass `--clobber`.

## Testing Strategy

TDD: the new assertions are written and observed failing before the train is added.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| release 0.9.0 resolves its train | unit | `workspace/tests/test_product_release.py::TrainAndTagTest` |
| train tuple mirrors the milestone policy | unit | `workspace/tests/test_product_release.py::TrainAndTagTest` |
| product version coherence passes on the real tree and with `--tag v0.9.0` | unit / gate | `workspace/tests/test_product_version_coherence.py::CoherentTreeTest`, `workspace/sdlc/check-product-version-coherence.py` |
| a missing minor still fails closed | unit | `workspace/tests/test_product_release.py::TrainAndTagTest` |

- New unit tests: two assertions in `test_product_release.py`, one gate invocation with `--tag`.
- Coverage impact: none (test-only additions plus constants).

## Regression Strategy

- Existing tests affected: `test_train_is_the_milestone_with_the_version_prefix`,
  `test_release_trains_match_the_milestone_policy`, `test_real_repository_passes` — all currently
  fail on `main`; they must pass unchanged once the train exists.
- Full suite command: `python3 -m unittest discover -s workspace/tests -p 'test_*.py'` and
  `bash workspace/sdlc/preflight.sh --full`.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface; the change is release metadata and generated Pages JSON. Preflight still
runs the existing Playwright suite.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: merge the train fix, then push `v0.8.0`, then backfill both
  Releases, then regenerate the Pages history (the Pages deploy runs on push to `main`).
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): `check-product-version-coherence.py --tag v0.9.0` and
  `gh release view` for both tags, plus the published Releases page.

## Rollback Strategy

- Revert safe: yes for the code change (constants and tests only); the published tag/Release
  state is not reverted — an incorrect publication is repaired by a new forward fix.
- Database rollback: none needed.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: coherence gate stays red for every PR.

## Migration Plan

1. Write failing tests for the `v0.9` train.
2. Add the train to all three mirrors, create the GitHub milestone, regenerate Pages JSON.
3. Run the coherence gate and the Python test suite.
4. Full preflight, commit, push, PR, merge.
5. Tag/publish and backfill from the merged `main`.

## Open Questions

None: the user confirmed release-integrity scope and the train title is covered by the
`v0.9 · …` prefix contract.
