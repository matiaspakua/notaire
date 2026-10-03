> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1185, CU76. Evidence from the #1179 pipeline run: 37 `Issue #N exists but is CLOSED`
problems (45 of 52 active changes belong to closed-COMPLETED issues), one self-test failing
only on macOS, and 8 duplicate-heading hits in `CHANGELOG.md`.

## Goals / Non-Goals

**Goals:**

- `run_pipeline.sh` and the pre-push hook pass on a clean checkout.
- No gate disabled, loosened or skipped.

**Non-Goals:**

- Changing validator rules; touching changes with open issues; fixing why PRs append duplicate headings.

## Decisions

1. **Archive, do not relax the validator.** The rule is correct: finished work belongs in
   `archive/`. Every candidate was verified `stateReason=COMPLETED` before archiving.
2. **`openspec archive <name> -y` per change; fall back to `--skip-specs` only on a spec
   conflict**, recording the change in the "Skipped spec sync" list below so it is never silent.
3. **Portable in-place edit helper in the seed script** (`sed -E … file > file.new && mv`)
   instead of `sed -i`, which differs between BSD and GNU. Rejected: `sed -i.bak` (leaves files),
   requiring GNU sed (breaks macOS).
4. **Regroup the CHANGELOG mechanically**: collect each `###` block of a release, concatenate
   the bodies per heading in Keep-a-Changelog order (Added, Changed, Deprecated, Removed,
   Fixed, Security), preserving entry order within a heading. Verified by comparing the
   multiset of entry lines before and after. Rejected: relaxing MD024 further.

## Skipped spec sync

45 changes were archived: 44 with their delta specs folded into `openspec/specs/`, and one with
`--skip-specs`:

| Change | Why the spec sync was skipped |
|--------|-------------------------------|
| `chore-validate-sdlc-no-bc` | Its delta MODIFIES a requirement of a spec that does not exist under `openspec/specs/`; the CLI only allows ADDED requirements for a new spec and aborts. The change's artifacts are archived intact. |

## Riesgos / Trade-offs

- [Archiving folds delta specs into `openspec/specs/` and could conflict] → per-change,
  recorded fallback; `openspec validate --strict` after the sweep.
- [Regrouping reorders entries inside `[Unreleased]`] → content-preserving check; chronological
  order within a heading is kept.
- [A closed issue whose work never merged] → checked `stateReason` for all 45; PRs for the
  two changes of this session are known merged.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No active change has a closed issue | static, live `gh` | `scripts/validate-sdlc-plan.sh` (red now: 37 problems) |
| Archived changes keep their content | static | directory diff of moved files |
| Seed fills values on BSD and GNU | unit | `scripts/tests/test_validate_sdlc_plan.py` (red now on macOS) |
| Unique headings per release | static | `scripts/test_changelog_structure.py` (written red first) |
| No entry lost | static | same, compared against `git show HEAD:CHANGELOG.md` |
| Pipeline passes without bypass | pipeline | `bash scripts/run_pipeline.sh` |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: n/a
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: `scripts/tests/test_validate_sdlc_plan.py` goes green; all
  `scripts/test_*.py` guards must stay green.
- Full suite command: `bash scripts/preflight.sh`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno API suite: untouched, run by the pipeline.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; archive, seed fix and CHANGELOG are separate commits.
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): a clean checkout of `main` passes `validate-sdlc-plan.sh`
  and `preflight.sh`; a normal `git push` is not blocked by the hook.

## Rollback Strategy

- Revert the PR: archived changes return to `openspec/changes/`, the CHANGELOG regrouping and
  seed edit are undone. Nothing at runtime is affected.
