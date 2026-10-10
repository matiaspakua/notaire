# Owner-accepted OpenAPI breaking changes (one file per pull request)

`openapi-contract.yml` and `workspace/sdlc/preflight.sh` fail on every ERR-level
breaking change `oasdiff` reports between the base spec and the revision. A
break the Owner accepts is listed here, **one file per pull request**, so two
open pull requests never edit the same lines (issue #1315).

## Adding an accepted break

Only when the break is intended and reviewed, in the pull request that
introduces it:

1. Create `<issue>-<slug>.txt` in this directory, for example
   `655-put-rejects-incomplete-bodies.txt`. Never edit another pull request's
   file.
2. Copy each line `oasdiff` prints as `<METHOD> <path> <change text>`.
3. Put the lines under a `# #<issue>` comment that says why no working client
   breaks.
4. Record the break in `CHANGELOG.md`, comment on the issue, and get Owner
   approval in review.

```text
# #596: no frontend screen reads this list; the per-management history is unchanged.
GET /api/v1/historial the response's body `type` changed from `array<object>` to `object` for status `200`
```

## What the gate does

`workspace/sdlc/check-accepted-breaking-changes.py` (CI step "Accepted breaking
changes: assemble and check", and preflight):

- A file that exists **unchanged on the base branch** belongs to a merged pull
  request. Its break is already in the base spec, so its entries are never
  ignored: they could only hide a later break with the same text. The check
  lists it for deletion; `bash workspace/sdlc/preflight.sh --fix` (or the
  checker's `--prune`) deletes it. Two branches deleting the same file merge
  cleanly, and leaving it costs nothing.
- Every other `*.txt` belongs to the pull request. Each entry must match a
  breaking change `oasdiff` reports now (case-insensitive METHOD + path and the
  change text, the `--err-ignore` rule), sit under a `# #<issue>` comment, and
  the file name must start with the issue number. Anything else fails.
- Only those entries are written to `.openapi-ci/accepted-breaking-changes.txt`,
  the file `oasdiff breaking --err-ignore` reads. On `main` every file is
  merged, so the effective list is **empty by default**.

This README is not read by the gate (only `*.txt` files are).
