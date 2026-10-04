> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #585 (open since 2026-07-02), widened by the Owner. Evidence gathered on `main` (2026-10-03):

| Candidate | Evidence it is orphaned |
|-----------|-------------------------|
| `deprecated-src.old/` | Kept: the Owner treats it as historical data (PR #1207); it was a candidate in #585 and is explicitly **not** removed |
| `testing/integration/http/02…07-*.sh` | zero references anywhere |
| `01-auth.sh`, `08-items.sh`, `test-all-endpoints.sh` | referenced only by `api-test/README.md` as manual scripts; not called by `run.sh`, `test-all-endpoints-v2.sh` or CI; use legacy Spanish endpoint names |
| `COMPOSE_FILES` in `test_image_pins_and_dependabot.py` | defined, never used (found while adding the #1191 image-pin test) |
| Junk-named tracked files (`.bak`, `.orig`, `.tmp`, `.log`, `.DS_Store`, `__pycache__`) | none tracked |

## Goals / Non-Goals

**Goals:** delete the orphaned scripts and constant; add an orphan guard for `testing/`; keep every build, workflow and guard green.

**Non-Goals:** `testing/e2e-swing/`, the coverage script, history rewriting.

## Decisions

1. **Keep `deprecated-src.old/`.** It is referenced by no build file and duplicates class names of the real
   codebase, which is why #585 proposed removing it, but the Owner keeps it as historical data. The first
   version of this change deleted it (commit `f20b9d7d`); a later commit restores it byte for byte, and the
   hygiene check that required its absence was removed. Deleted history is not rewritten.
2. **Guard by reachability, not by list.** The test finds callers by script basename inside
   `testing/`, so a future orphan fails without anyone updating a list. Exempt: `scripts/test.sh`
   (external callers: Constitution step 14, preflight, agent rules) and
   `scripts/generate-coverage-report.sh` (CI).
3. **Separate commits**: red guard, deletion of the scripts, doc updates, then the restore of the tree.
4. **`e2e-swing` left alone**: its retirement spec allows it and `test_dependabot_hygiene.py` and
   `test_repo_hygiene.py` assert it; removing it is a spec change to decide separately.

## Riesgos / Trade-offs

- [Deleting something a developer runs by hand] → zero references; history retains it; revert is one command.
- [Large diff hides an unrelated change] → deletions are pure `git rm` in their own commits.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No orphaned script under testing | static | `scripts/test_testing_standalone.py` |
| Removed cURL scripts are gone | static | same |
| No live reference to a removed path | static | `scripts/test_testing_standalone.py` (extended legacy-reference scan) |

- New unit tests (`src/test/java/.../unit/`): n/a (no Java touched)
- New integration tests: n/a
- Coverage impact (JaCoCo): none

## Regression Strategy

- Existing tests affected: none expected; `mvn -q -pl backend-api -am validate` proves the build does not read the removed tree.
- Full suite command: `bash scripts/preflight.sh`, then `bash scripts/run_pipeline.sh`.
- HTTP/Bruno API suite: untouched; `run.sh integration` still passes.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR; deletions in their own commits.
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `run.sh integration` and `run.sh database` green on merged `main`; CI and CD green.

## Rollback Strategy

- Revert the PR: restores every deleted file. Nothing at runtime depends on them.
