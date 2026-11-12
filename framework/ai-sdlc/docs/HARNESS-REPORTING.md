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

## Hash-chain append rule

Every event is written with `evidence.append_record` into `ledger.jsonl`. Each
row carries `prev` = SHA-256 of the previous line (`genesis` for the first).
`aisdlc evidence verify` must remain green after harness reports. Adapters must
not rewrite, delete, or reorder prior rows.

## Honesty

- Cancel via HTTP returns **501** with the capabilities.runner reason until a
  real Runner port is proven.
- Agents must never commit or approve through project/run paths.
- Budget overrun on a step marks the run `blocked`.
