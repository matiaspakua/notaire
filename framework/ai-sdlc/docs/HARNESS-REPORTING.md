# Harness reporting contract (#2255)

The kit records a **RunRequest** (`kind: run.request`) when an operator starts a
run against an **approved** workflow version + digest. Status after that event is
`queued` (or `requested` only for an incomplete chain). The kit **never** sets
`running` from the request alone — `aisdlc foreman plan|route` stays
`executes=false`.

## Who may advance a run

Only a harness adapter appending hash-chained evidence ledger rows may move a
run past `queued`. Reference adapter: **local-worker** via:

```bash
aisdlc harness report --run <id> --event run.started --adapter local-worker
aisdlc harness report --run <id> --event step.started --step build
aisdlc harness report --run <id> --event step.finished --step build --step-status pass
aisdlc harness report --run <id> --event run.finished --ok true
```

## Event names and fields

| `kind` | Required fields | Effect on derived status |
|--------|-----------------|--------------------------|
| `run.request` | `run_id`, `project`, `workflow{id,version,digest}`, `requested_by` | → `queued` |
| `run.started` | `run_id`, `adapter` | → `running` |
| `step.started` | `run_id`, `step` | step status `running` |
| `step.finished` | `run_id`, `step`, `step_status` | updates step; `budget_overrun` → run `blocked` |
| `run.finished` | `run_id`, `ok` | → `succeeded` / `failed`; `cancelled` / `blocked` flags honored |

`step_status` values: `pass` | `fail` | `skip` | `todo`. **Skip is never pass.**

## Step inputs and outputs (#2435)

A `step.started` or `step.finished` event may carry what the step **received** (`inputs`) and what
it **produced** (`outputs`), so anyone can inspect a stage afterwards from the dashboard or
`aisdlc run show`. Pass them as a JSON file:

```bash
aisdlc harness report --run <id> --event step.started --step red --io-file in.json
aisdlc harness report --run <id> --event step.finished --step red --step-status pass --io-file out.json
```

```json
{"inputs": {"request": "Fix the pages metrics", "prompt": "Write the failing tests first.",
            "files": ["workspace/ci/generate.sh"],
            "links": [{"label": "Issue #760", "url": "https://github.com/org/repo/issues/760"}]},
 "outputs": {"summary": "8 failing tests", "commits": ["0123abcd"], "files": ["workspace/tests/test_generate.py"]}}
```

Both sides share one shape: `request`, `prompt`, `summary` (text; 4,000, 20,000 and 4,000
characters at most), `files` (repository-relative paths, no `..`), `commits` (7 to 40 lowercase hex)
and `links` (`label` plus an `http` or `https` `url`); lists hold 200 items at most. Anything else is
refused with `invalid_body` and nothing is appended. Later events overwrite only the keys they
carry. The values are evidence in the hash-chained ledger, not a secret store: never put a
credential in a prompt.

## Hash-chain append rule

Every event is written with `evidence.append_record` into `ledger.jsonl`. Each
row carries `prev` = SHA-256 of the previous line (`genesis` for the first).
`aisdlc evidence verify` must remain green after harness reports. Adapters must
not rewrite, delete, or reorder prior rows.

## Adapter validation

`aisdlc harness report` accepts adapter ids returned by `aisdlc harness list` **plus**
the kit's built-in reference adapter `local-worker`. The reference adapter is always
accepted even when no `[harness.adapters]` are declared in the profile — it is the
default for `--adapter` and needs no registration.

Supplying any other unknown adapter exits with code 2 and error code `unknown_adapter`,
naming the known adapter ids; no ledger record is written.

## Honesty

- Cancel via HTTP returns **501** with the capabilities.runner reason until a
  real Runner port is proven.
- Agents must never commit or approve through project/run paths.
- Budget overrun on a step marks the run `blocked`.
