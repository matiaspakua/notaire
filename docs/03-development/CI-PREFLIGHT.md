# CI Preflight — make CI failures impossible to push

## The problem this solves

CI spreads its gates across four workflows:

| Workflow | Gates |
|---|---|
| `ci.yml` | Build & Compile, Unit Tests, Integration Tests, Coverage Gate, Security Scan |
| `pr-validation.yml` | Validate PR, Quick Build, Dependency Analysis, **Code Lint (Checkstyle + Spotless)**, Branch Naming |
| `frontend-ci.yml` | TypeScript Check, ESLint, Unit Tests (Vitest), Build (Next.js) |
| `playwright-e2e.yml` | Build Backend/Frontend, API Tests (Bruno), UI E2E Tests (Playwright) |

**Not all of these are reachable from the commands developers normally run.** The
clearest example, and the one that motivated this tooling: Spotless is
deliberately *not* bound to any Maven lifecycle phase in `backend-api/pom.xml`
(see #705 — binding it breaks the shallow-checkout jobs, because `ratchetFrom`
needs full git history). It runs only from `pr-validation.yml`'s lint job. So:

```bash
mvn verify -pl backend-api   # ✅ green locally
git push                     # ❌ "Code Lint" fails in CI
```

That happened on PR #721 and is the exact gap `scripts/preflight.sh` closes.

## Usage

Install once per clone:

```bash
bash scripts/install-git-hooks.sh
```

That sets `core.hooksPath=.githooks`, so the hooks are version-controlled, apply
to every worktree, and update on `git pull`. From then on `git push` runs the
gates automatically and refuses to push a branch CI would reject.

Run it by hand any time:

```bash
bash scripts/preflight.sh            # every blocking gate except server-backed suites
bash scripts/preflight.sh --fix      # auto-fix what is fixable, then verify
bash scripts/preflight.sh --fast     # format/lint/compile/typecheck only
bash scripts/preflight.sh --full     # adds Playwright E2E + HTTP API suite
bash scripts/preflight.sh --list     # local check -> CI job mapping
```

`--full` needs the stack running (`bash scripts/start.sh`): backend on `:8080`,
frontend on `:3000`.

### What `--fix` fixes automatically

| Problem | Fix applied |
|---|---|
| Java formatting (trailing whitespace, unused imports, tabs, missing EOF newline) | `mvn spotless:apply` (or the fallback below) |
| Auto-fixable ESLint violations | `eslint src --fix` |

Everything else — failing tests, coverage below the ratchet floor, type errors —
is reported for you to fix, never silently patched.

## Bypassing (deliberately)

```bash
PREFLIGHT_SKIP=1 git push …    # skip the gates for one push (WIP spike, etc.)
PREFLIGHT_FAST=1 git push …    # format/lint/compile/typecheck only
git push --no-verify …         # git's built-in hook bypass
```

## The git-worktree caveat

Spotless resolves the repository through JGit, which walks parent directories
looking for a `.git` **directory**. Inside a git worktree, `.git` is a **file**
holding a `gitdir:` pointer, so the plugin aborts with:

```
Cannot find git repository in any parent directory
```

CI checks out normally and is unaffected, but this makes the format gate
unrunnable from a worktree — precisely where you'd want it. `preflight.sh`
detects this and falls back to `scripts/spotless-fallback.sh`, which implements
the same four rules configured in the pom:

```xml
<removeUnusedImports/> <trimTrailingWhitespace/> <endWithNewline/>
<indent><spaces>true</spaces><spacesPerTab>4</spacesPerTab></indent>
```

against the same file set (`.java` under `backend-api/` differing from
`origin/main` — Spotless ratchets per-file, so a touched file is formatted in
full). It was validated against PR #721 by confirming it flags exactly the two
files CI flagged, with the same diff.

Unused-import removal in the fallback is conservative: an import is dropped only
when its simple name appears nowhere else in the file. Wildcard and static
imports are never touched (`checkstyle.xml` already bans wildcards).

## Keeping local and CI in sync

`preflight.sh` mirrors CI by convention, not by magic — nothing enforces that
they agree. **When you add or change a gate in `.github/workflows/`, update
`preflight.sh` in the same PR.** Each check in the script names the CI job it
mirrors, and `--list` prints the mapping, so drift is easy to spot in review.

Checks that CI runs with `|| true` (Checkstyle) are non-blocking here too, so
local severity matches CI severity rather than being stricter.
