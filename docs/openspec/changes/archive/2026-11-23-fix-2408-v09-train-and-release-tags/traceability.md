# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2408 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure (release trains) | exists |
| Specification | `docs/openspec/changes/fix-2408-v09-train-and-release-tags/` | written |
| Branch | `fix/2408_v09_train_and_release_tags` | created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | `v0.8.0`, `v0.9.0` | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| release 0.9.0 resolves its train | `workspace/tests/test_product_release.py::TrainAndTagTest` (new assertion) | passing |
| release 0.8.0 stays addressable | `workspace/tests/test_product_release.py::TrainAndTagTest` (new assertion) | passing |
| unmapped minor fails closed | `workspace/tests/test_product_release.py::TrainAndTagTest::test_unmapped_minor_fails_closed` | passing |
| policy mirror self-test stays green | `workspace/sdlc/test_milestone_policy.py::NomenclatureTests::test_open_trains_are_version_first_and_ordered` | passing (expected tuple extended) |
| coherence passes with `--tag v0.9.0` | `workspace/sdlc/check-product-version-coherence.py --tag v0.9.0` | passing |
| missing 0.8.0 tag is published | `git ls-remote --tags origin refs/tags/v0.8.0` + `gh release view v0.8.0` | pending |
| assets are backfilled without clobber | `gh release view <tag> --json assets` | pending |
| pages history tracks the current cut | `github-page/lib/releases.json` `productVersion` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/305-project-management/MILESTONE-NOMENCLATURE.md` | yes (v0.9 train row) | pending |
| `docs/300-development/305-project-management/RELEASES.md` | yes (worked example for both cuts) | pending |
| `github-page/lib/releases.json` | yes (regenerated, product version `0.9.0`) | pending |
| `docs/openspec/specs/release-trains/spec.md` | yes (synced by `openspec archive`) | pending |
| `CHANGELOG.md` | no — fleet's single `[Unreleased]` writer (#1693 fence) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2408, delta spec `release-trains` |
| 2 | Failing tests written, test cases designed | yes | new assertions failed before the train was added, then 30 tests passed |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- **v0.9.0 image SBOMs omitted.** CD run `45067381490` stopped at `release-identity`, so no
  backend/frontend image was ever published for the tag's tree. `sbom-backend.cdx.json` and
  `sbom-frontend.cdx.json` cannot be generated from a published digest, and
  `RELEASE-BACKFILL.md` forbids rebuilding or fabricating them. The three tree-derived assets
  (`product-release.json`, `sbom-database.cdx.json`, `sbom-notaire.cdx.json`) are attached; the
  omission is recorded in the Issue and PR. v0.8.0's coherent tag tree lets CD publish images
  and all five assets normally.
