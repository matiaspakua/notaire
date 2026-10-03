> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #585 (open since 2026-07-02), widened by the Owner. Evidence gathered on `main` (2026-10-03):

| Candidate | Evidence it is orphaned |
|-----------|-------------------------|
| `deprecated-src.old/` | 422 tracked files, ~7.6 MB, `main/java` 309, `main/resources` 111; referenced by no `pom.xml`, workflow, script or live doc (only prose in the CHANGELOG and a retirement spec); issue #585 calls it dead weight |
| `testing/integration/http/02…07-*.sh` | zero references anywhere |
| `01-auth.sh`, `08-items.sh`, `test-all-endpoints.sh` | referenced only by `api-test/README.md` as manual scripts; not called by `run.sh`, `test-all-endpoints-v2.sh` or CI; use legacy Spanish endpoint names |
| `COMPOSE_FILES` in `test_image_pins_and_dependabot.py` | defined, never used (found while adding the #1191 image-pin test) |
| Junk-named tracked files (`.bak`, `.orig`, `.tmp`, `.log`, `.DS_Store`, `__pycache__`) | none tracked |

## Goals / Non-Goals

**Goals:** delete the above; add an orphan guard for `testing/`; keep every build, workflow and guard green.

**Non-Goals:** `testing/e2e-swing/`, the coverage script, history rewriting.

## Decisions

1. **Delete, do not archive in the tree.** Git history is the archive; the commit that removes
   `deprecated-src.old/` is named in the PR so it is findable. #585 offered "docs/archive or a
   tagged branch": a tag is another outward-facing artifact for files nobody has used since the
   migration, so none is created.
2. **Guard by reachability, not by list.** The test finds callers by script basename inside
   `testing/`, so a future orphan fails without anyone updating a list. Exempt: `scripts/test.sh`
   (external callers: Constitution step 14, preflight, agent rules) and
   `scripts/generate-coverage-report.sh` (CI).
3. **Separate commits**: red guard, deletion of the tree, deletion of the scripts, doc updates.
4. **`e2e-swing` left alone**: its retirement spec allows it and `test_dependabot_hygiene.py` and
   `test_repo_hygiene.py` assert it; removing it is a spec change to decide separately.

## Riesgos / Trade-offs

- [Deleting something a developer runs by hand] → zero references; history retains it; revert is one command.
- [IDE or tooling expects the tree] → not in any module list or build; `mvn -q validate` is run after.
- [Large diff hides an unrelated change] → deletions are pure `git rm` in their own commits.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Pre-migration tree is gone | static | `scripts/test_repo_hygiene.py` |
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
