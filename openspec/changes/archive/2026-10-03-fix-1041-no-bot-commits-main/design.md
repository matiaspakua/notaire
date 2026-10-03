> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1041 (audit-2026-09, CU76, priority:high): CI bots commit reports to
`main`, polluting ~25% of history and requiring `contents: write`.

Verified on `origin/main` (2026-10-03, tip `7cc076d5` after fetch):

| Location | Finding |
|----------|---------|
| `ci.yml` job `publish-reports` | `contents: write`; copies markdown into `docs/wiki/cicd-reports/`; `CI Bot` commit with `[skip ci]`; `git pull --rebase` + `git push` to `${{ github.ref_name }}` |
| `cd.yml` job `publish-report` | `contents: write`; writes `cd-report.md` under wiki path; commit + push to `main` |
| `playwright-e2e.yml` job `coverage-report` | `contents: write`; writes `e2e-coverage-$DATE.md`; commit + `git push origin HEAD:main \|\| echo "Push skipped"` |
| `pr-validation.yml` | **Already fixed** — comment block cites #1117/#1041; no report commit job; artifacts + PR comment only; top-level `contents: read` |
| `deploy-github-page.yml` | Good pattern: `contents: read`, `pages: write`, uploads Pages artifact — no git commit of reports |
| `cd.yml` job `release` | Legitimate `contents: write` for GitHub Releases — **keep** |
| Tracked reports | 166 files under `docs/wiki/cicd-reports/` |
| History | ~464 `CI Bot` commits / ~1641 total on `main` |
| Related queue | `#1051 → #1046 → #1042 → #1041` — this draft is prep-only until #1042 merges |

## Goals / Non-Goals

**Goals:**

- Eliminate all workflow steps that `git commit` / `git push` generated CI/CD/E2E
  reports into the application repository.
- Replace discoverability with workflow artifacts, `$GITHUB_STEP_SUMMARY`, and
  (preferred when useful) GitHub Pages — never git commits.
- Untrack + gitignore `docs/wiki/cicd-reports/`.
- Drop `contents: write` from report-publish jobs only.
- Prove ACs with a failing-then-green static YAML/unittest suite.
- Document the new publish channels in DevSecOps + CHANGELOG.

**Non-Goals:**

- Rewriting git history to erase past bot commits.
- Changing CD image pin / `latest` ordering (#1042).
- Product JWT/CSP (#1051) or Dependabot Swing/smol-toml (#1046).
- Turning GitHub’s separate Wiki product into a mandatory store (optional later).
- Implementing before #1042 merges.

## Decisions

1. **Remove commit/push steps; keep generate + artifact upload**
   - In `ci.yml` `publish-reports`, `cd.yml` `publish-report`, and
     `playwright-e2e.yml` `coverage-report`: delete `git config` / `git add` /
     `git commit` / `git pull --rebase` / `git push` steps (and any
     “Prepare reports for wiki” copy that only exists to stage a commit).
   - Ensure each report path still produces a durable artifact via
     `actions/upload-artifact` (CI/CD already upload markdown in
     `generate-reports` / `generate-report`; Playwright already uploads
     Playwright/Bruno reports — add or keep a dedicated markdown coverage
     artifact if the coverage job’s only output was the git file).
   - Append the report (or a short link + excerpt) to `$GITHUB_STEP_SUMMARY`
     so humans see it on the run page without cloning.

2. **Prefer Pages / discard for human browsing — not git**
   - **Primary:** workflow artifacts + job summary (always).
   - **Secondary (recommended if cheap):** extend `deploy-github-page.yml`
     (or a small follow-up job there) to `gh run download` / download-artifact
     the latest CI/CD/E2E markdown into `github-page/public/` before
     `upload-pages-artifact` — same pattern as Playwright report download
     today. No `contents: write` on the app repo.
   - **Rejected:** keep committing into `docs/wiki/cicd-reports/` “for the
     wiki site” — that is the bug.
   - **Rejected for this issue:** force-push history rewrite.

3. **Untrack + ignore `docs/wiki/cicd-reports/`**
   - Add `docs/wiki/cicd-reports/` to root `.gitignore`.
   - Remove tracked files from the index (`git rm -r --cached
     docs/wiki/cicd-reports/` or delete the tree in the same PR). Historical
     content remains in git history; working tree / future commits must not
     reintroduce it.
   - Leave a short `docs/wiki/README.md` (or DevSecOps pointer) explaining
     where reports live now — do not recreate the report dump directory as
     tracked content.

4. **Least privilege: drop report-job `contents: write`**
   - After commit removal, set those jobs to `contents: read` (or inherit
     workflow-level `contents: read`).
   - **Keep** `cd.yml` `release` job `contents: write` for
     `softprops/action-gh-release` (or equivalent).
   - Assert `pr-validation.yml` remains without report commits and without
     elevating to `contents: write` for wiki publish.

5. **TDD via static workflow unittest**
   - Add `scripts/test_no_bot_report_commits.py` (pattern:
     `scripts/test_ci_workflow_invariants.py`) asserting for
     `ci.yml` / `cd.yml` / `playwright-e2e.yml` / `pr-validation.yml`:
     - No step run script contains `git commit` paired with
       `docs/wiki/cicd-reports` (or `CI Bot` + report path).
     - Report jobs do not set `contents: write` (except documenting CD
       `release` as allowed).
     - `.gitignore` contains `docs/wiki/cicd-reports/`.
     - No tracked files under that path in the tree under test (or index
       empty for that prefix — assert via path walk of the working tree
       checked into the PR).
   - Update `test_ci_workflow_invariants.py` docstring / any assert that
     still describes coverage-report as committing to `docs/wiki/`.
   - Prove red on current `origin/main` before editing workflows.

6. **Serialize after #1042**
   - Coordinator queue `#1051 → #1046 → #1042 → #1041`. Avoid concurrent
     heavy-CI PRs that touch the same workflow files.

## Exact no-bot-commits fix (implement checklist)

1. **`ci.yml`**
   - Rewrite or slim `publish-reports`: download artifacts → write step
     summary → upload artifact if needed → **no git**.
   - Job permissions: remove `contents: write`.
2. **`cd.yml`**
   - Same for `publish-report`; keep `release` permissions unchanged.
3. **`playwright-e2e.yml`**
   - `coverage-report`: generate markdown to a temp/workspace path → upload
     artifact + summary → **no git**; drop `contents: write`.
4. **`pr-validation.yml`**
   - Re-verify no commit steps; leave NOTE referencing #1041.
5. **`.gitignore`**
   - Add `docs/wiki/cicd-reports/`.
6. **Tree cleanup**
   - Remove tracked report files from the repo in this PR.
7. **Optional Pages**
   - If timeboxed: download latest markdown artifacts into
     `github-page/public/cicd-reports/` during Pages deploy; otherwise
     document artifact+summary as sufficient for AC1 and defer Pages
     polish only if AC wording requires a browsable site (AC allows
     “artifacts / job summary / GitHub Pages” — any one channel beyond
     “not commits” is enough if artifacts+summary ship; prefer adding
     Pages when it is a small extension of existing deploy).
8. **Tests + docs**
   - Land failing tests first; then green; update DevSecOps/ADR/CHANGELOG.

## Riesgos / Trade-offs

- **[Risk] Consumers bookmarked `docs/wiki/cicd-reports/*.md` in the repo** →
  Document new locations; Pages or Actions UI replaces deep links; old
  files remain in git history for archaeology.
- **[Risk] Artifact retention expires** → Set retention (e.g. 30 days,
  matching existing CI artifacts) and rely on Pages for longer-lived
  snapshots if needed.
- **[Risk] `test_ci_workflow_invariants` / report `needs` tests break** →
  Update expectations; do not weaken dependency coverage asserts for
  generate-report jobs.
- **[Risk] Race with #1042 editing `cd.yml`** → Serialize: implement #1041
  only after #1042 merges; rebase onto updated main.
- **[Trade-off] Delete vs keep empty directory** → Prefer ignore + remove
  tracked files; no empty tracked stub required.
- **[Trade-off] Rename jobs `Publish to Wiki`** → Optional rename to
  `Publish report artifact` for clarity; not required for AC.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| No CI/CD/E2E/PR report git commits | unit | `scripts/test_no_bot_report_commits.py` |
| Report jobs lack `contents: write` | unit | same |
| `docs/wiki/cicd-reports/` ignored + untracked | unit | same (+ path walk) |
| Artifacts / summary still produced | unit + review | assert upload-artifact / STEP_SUMMARY steps exist; PR checklist |
| Existing Pages deploy still `contents: read` | unit | existing `test_ci_workflow_invariants.py` |
| DevSecOps docs updated | review | PR checklist |

- New unit tests: stdlib unittest + PyYAML.
- New integration tests: none required (workflow config).
- Coverage impact (JaCoCo): none (no Java).

## Regression Strategy

- Existing tests affected: `test_ci_workflow_invariants.py` (Playwright
  coverage-report narrative); possibly
  `test_report_job_needs_dependencies.py` if job graphs change — update
  without weakening `needs` coverage.
- Full suite: `python3 scripts/test_no_bot_report_commits.py`;
  `python3 scripts/test_ci_workflow_invariants.py`;
  `bash scripts/preflight.sh` as applicable; heavy CI
  `bash scripts/check-heavy-ci.sh <pr>`.
- HTTP/Bruno: n/a for product API delta.

## Playwright Strategy

- No UI product change. No new Playwright product scenarios.
- Workflow file `playwright-e2e.yml` **is** in scope (remove commit step).
- PR still must pass repository heavy CI Playwright job before merge.
- Mark product E2E n/a for this capability; do not skip the PR Playwright job.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: merge workflow + ignore + tree cleanup → next
  CI/CD/E2E on `main` must **not** create CI Bot commits → reports visible
  as artifacts/summary/(Pages)
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): after next green CI+CD+E2E on `main`,
  `git log --author='CI Bot' -5` shows no new `docs: add … report` commits;
  Actions run shows uploaded report artifacts / summary; jobs lack
  `contents: write` for report publish

## Rollback Strategy

- Revert safe: yes — revert workflow/ignore/docs/test commits; re-tracking
  historical report files is optional and discouraged
- Database rollback: none needed
- Data written under the new behavior after revert: Actions artifacts from
  interim runs remain in GitHub’s artifact store per retention
- Blast radius if rollback delayed: low — worst case is operators looking in
  the old wiki path; artifacts still hold reports

## Migration Plan

1. Wait for **#1042** merge to `main` (after #1051 and #1046).
2. Copy this draft into `openspec/changes/fix-1041-no-bot-commits-main/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1041-no-bot-commits-main`.
4. Branch `cursor/fix-1041-no-bot-commits-main-69d3` → failing no-bot-commit
   tests → remove commits + permissions + ignore/untrack → green tests →
   docs → PR `Closes #1041`.
5. After merge: Gate 5 observe next CI/CD/E2E runs produce **zero** new
   report commits on `main`.

## Open Questions

- Whether to wire markdown into Pages in the same PR or ship
  artifacts+summary first — AC accepts either; prefer same-PR Pages only if
  the download hook stays small.
- Whether to delete the on-disk historical report files in the PR or only
  `--cached` untrack — deleting from the tree is clearer for “untracked”.
