# Phase 1 — Triage issue #{{ISSUE}} (analysis only)

You read code and fill in TWO files. Nothing else: no branches, no edits to
repository files, no `git add`. Do NOT create anything under `openspec/` —
a later phase writes the specification; the foreman reverts it here.

## Steps

1. Read the issue at the end of this prompt.
2. For EVERY file or class the issue mentions, open it and read the relevant
   lines (`sed -n '80,100p' <file>`). A grep hit is not reading. Find where a
   name is mentioned with exactly `git grep -n -i "<name>"` — one name, no
   options added; `find` and `grep -r` crawl `node_modules/`.
3. Find the existing tests for those classes:
   `git grep -ln "<ClassName>" -- backend-api/src/test frontend/src testing/e2e/tests`.
   Open them. The issue may be partly stale: something it asks for may already
   be true on `main` and already tested.
4. For each acceptance criterion of the issue decide:
   - `DONE` — already true on `main`; you cite the test method that proves it.
   - `TODO` — still missing; you say which new test will prove it.
5. Fill the two files below (the foreman created them as skeletons).

## File 1: `{{IO}}/triage.env`

Replace each `?` — keep the keys exactly, one per line, no quotes, no comments.

| Key | Allowed values | How to decide |
|---|---|---|
| `TYPE` | feat, fix, refactor, test, docs, chore, ci, design | copy the issue title prefix (`chore(security): …` → `chore`); the foreman rejects anything else |
| `KIND` | code, docs, ci | `code` if any file under `backend-api/` or `frontend/` changes (incl. resources), or if a test must prove the change (see below); `ci` for `.github/`, `scripts/`, `infra/`; else `docs` |
| `TEST_SURFACE` | backend, frontend | pre-filled `backend`: where the proving tests run. Change it only if they are frontend tests |
| `UI_CHANGE` | yes, no | a page or component a user sees changes |
| `API_CHANGE` | yes, no | a REST request/response shape or status code changes |
| `DB_CHANGE` | yes, no | `yes` ONLY if you list a new `db/migration/V<n>__*.sql` in Files to Edit. Editing `.properties` or datasource config is `no` |
| `SLUG` | 2–6 lowercase words joined by `_` | e.g. `remove_dead_default_credentials` |

`ISSUE`, `USE_CASE` (and `TYPE` when the title has a prefix) are pre-filled by the foreman — leave them. The foreman
derives the branch name, the OpenSpec change name and the surface from your
answers.

## File 2: `{{IO}}/triage.md`

Keep the four `##` headings exactly. Replace the example lines.

- `## Evidence` — one bullet per confirmed problem: `` `path:line` — what is wrong ``.
- `## Acceptance Criteria` — numbered. Take the criteria ONLY from the issue's
  `## Acceptance Criteria` checklist (you may split one checkbox into several
  lines). Items under "Technical Notes" or "Related" are OUT of scope — other
  issues own them. Each line is exactly one of:
  - `N. TODO — <criterion> — proven by: new test <TestClass>#<shouldMethod>`
  - `N. DONE — <criterion> — proven by: <ExistingTestClass>#<method>`
  - `N. TODO — <criterion> — proven by: command <shell command that must pass>`
    (only for things no unit test can see, e.g. a Gitleaks/Trivy scan,
    `bash scripts/preflight.sh`, a file removed outside `backend-api/` and
    `frontend/` — `command test ! -e Jenkinsfile` — or a stale mention removed
    from docs — `command ! git grep -qi jenkins -- docs README.md`). `grep`, `find`, `cat`, `ls`, `git ls-tree`
    and the like are NOT proofs: they succeed whether or not the criterion
    holds. Any `new test` needs `KIND=code`

  Almost everything IS unit-testable — prefer a test over a command:
  - config key removed → a test loads `application.properties` into
    `java.util.Properties` and asserts the key is absent
  - resource file removed → a test asserts
    `getClass().getResource("/config.properties")` is null
  - startup guard → a test builds the bean and asserts it throws
  - rule about a repository docs/data file (e.g. a `.csv` or `.md` under
    `docs/` or the repo root) → a backend test reads the file from the repo
    root (`Path.of("..", "docs", "x.csv")`) and asserts the rule, like
    `JacocoCoverageConfigConsistencyTest` reads `CONSTITUTION.md`. Use
    `KIND=code`; Files to Edit lists the docs file, the test is new
  Example: `1. TODO — dead spring.security.user.* keys removed — proven by: new test BackendResourcesHygieneTest#shouldNotDefineSpringSecurityUser`
  At least one TODO. If everything is DONE, write `{{IO}}/BLOCKED.md`
  explaining that the issue is already resolved, with the evidence, and stop.
- `## Files to Edit` — one existing repository path per line, `- path`, no
  backticks, no comments. Only files you will change or delete (not tests).
- `## Risks` — bullets.

When both files are filled, stop and print a 3-line summary.

## Issue #{{ISSUE}}

{{ISSUE_TEXT}}
