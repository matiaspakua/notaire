# Design

## Context

The oasdiff gate ignores an Owner-accepted list. Slice 1 (#1330) required that list to be empty on main and failed any entry whose break was already in the base. With one shared file, every accepting pull request edits the same region and main rewrites it after each merge, so conflicts are guaranteed.

## Goals / Non-Goals

**Goals:** accepting a break touches only the pull request's own file; the effective list on main stays empty; entries stay justified and live; same rule in CI and preflight.

**Non-Goals:** migrating the open pull requests (each moves its entries when it next merges main); changing which changes oasdiff calls breaking.

## Decisions

- **One file per pull request** in `backend-api/openapi/accepted-breaking-changes.d/`, named `<issue>-<slug>.txt`, entries under `# #<issue>` comments. A README (not read by the gate) documents the rule and keeps the directory in git.
- **Ownership by base comparison.** A `*.txt` whose bytes equal the file at the base ref (`git show <ref>:<path>`) was merged. Its break is in the base spec, so ignoring it would only hide a later break with the same text: it is left out of the ignore list instead of failing the pull request. Any other file (new, or edited on the branch) is the pull request's and is validated strictly (live entries, issue comment, name).
- **Assembly.** `--write-ignore` writes the pull request's entries to `.openapi-ci/accepted-breaking-changes.txt`; `oasdiff breaking --err-ignore` reads only that file. On main every file is merged, so the effective list is empty.
- **Cleanup without conflicts.** Merged files are listed with a note; `--prune` (preflight `--fix`) deletes them. Two branches deleting the same unchanged file merge cleanly, and a leftover file is inert.
- **Legacy file rejected** so an edit to the old list cannot be silently ignored.
- **Stacked on #1382**, which rewrites the same workflow steps (oasdiff binary instead of the Docker action).

Alternatives considered: keep the single file (conflicts on every merge); concatenate all files and fail on stale ones (every open pull request must push a deletion after each merge); auto-delete merged files from CI on main (needs a bot push to main, which the ruleset and token do not allow).

## Riesgos / Trade-offs

- A pull request that edits a merged file makes it its own; the old entries then fail as stale. The message and README say to add a new file instead.
- Merged files can accumulate on main until someone prunes them; they are inert (never ignored).
- CI needs the base ref: the job checks out with `fetch-depth: 0` and fetches the base.

## Testing Strategy

`workspace/tests/test_check_accepted_breaking_changes.py`: fake oasdiff in a throwaway git repository whose `base` tag plays origin/main (pull-request files, merged files, prune, names, comments, legacy list), a real-oasdiff JSON-shape test, repository state and CI/preflight wiring. `test_dast_contract_backup_assets.py` reads the directory. Tests written first and observed failing.

## Regression Strategy

workspace, contracts, docs and security `verify.sh`; real oasdiff run with #1374's 7 entries moved to one file (check OK, assembled diff clean, without the list the diff fails).

## Playwright Strategy

Not applicable: no UI change.

## Deployment Strategy

CI only; effective on merge. Merge after #1382.

## Rollback Strategy

Revert the merge commit; the single list and the slice-1 stale check come back unchanged.
