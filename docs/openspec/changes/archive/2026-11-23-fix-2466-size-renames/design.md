> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`repo.numstat()` currently runs `git diff --numstat --no-renames base...HEAD`, which treats a renamed file as a full deletion of the old path and a full addition of the new path. The fix uses `git diff -M --numstat -z` to detect renames. With `-z`, a rename row contains NUL-separated old and new paths; a non-rename row has an empty first field.

## Goals / Non-Goals

**Goals:**
- Pure renames count 0 lines in `aisdlc size`.
- Renames with edits count only the edited lines, reported under the new path.
- `[size] exclude` matches the new path of renamed files.

**Non-Goals:**
- Copy detection (`-C` flag).
- Changes to other kit gates (commits, tdd, protected, scope, evidence).

## Decisions

1. **Use `-M --numstat -z` instead of `--no-renames`**: Git's `-M` flag detects renames by similarity (default 50% threshold). The `-z` delimiter gives us NUL-separated old/new paths, making rename detection unambiguous. A row where both old and new paths are present is a rename; a row with only a new path is a regular change.
2. **Report the new path**: The new path is what the developer sees in the working tree and what `[size] exclude` patterns target. The old path is only useful for rename detection.
3. **Parse in `repo.numstat()`**: Keep the change localized to the data-source function rather than scattering rename logic across callers.

## Riesgos / Trade-offs

- [Risk] `-M` adds overhead on large repos → Mitigation: rename detection is a standard git diff option; the performance impact is negligible for the PR-sized diffs this gate processes.
- [Risk] Git's 50% similarity threshold may miss aggressive renames → Mitigation: acceptable for review-budget purposes; a file changed >50% is not a pure rename anyway.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Pure rename counts 0 lines | unit | `framework/ai-sdlc/tests/test_diffchecks.py` |
| Rename with edits counts only edited lines | unit | `framework/ai-sdlc/tests/test_diffchecks.py` |
| Over the budget (existing) | unit | `framework/ai-sdlc/tests/test_diffchecks.py` |
| Excluded files do not count (existing) | unit | `framework/ai-sdlc/tests/test_diffchecks.py` |

- Tests run with: `cd framework/ai-sdlc/tests && python3 -m unittest test_diffchecks`
- TDD: write rename-only and rename-with-edit tests first, observe them fail, then implement.

## Regression Strategy

- Existing tests affected: `test_diffchecks.py` existing scenarios (over budget, excluded files) must continue passing.
- Full suite command: `cd framework/ai-sdlc/tests && python3 -m unittest test_diffchecks`
- No backend-api or frontend tests affected.

## Playwright Strategy

n/a — no UI surface. This change affects only the CLI `aisdlc size` command and internal `repo.numstat()`.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: n/a — framework code only, no runtime deployment.
- Configuration or `.env` keys to add: n/a
- Feature flag: no
- Smoke test after deploy (Gate 5): Run `aisdlc size` on a branch with a renamed file and verify 0 lines counted.

## Rollback Strategy

- Revert safe: yes — simple git revert of the commit restores the previous `--no-renames` behavior.
- Database rollback: none needed.
- Data written under the new behavior after revert: n/a — no data written.
- Blast radius if rollback is delayed: n/a — only affects gate counting, not runtime.

## Open Questions

None.
