# Intent metrics

Every factory intent can leave an append-only record so a later run of the same
intent without a knowledge backend can be compared with one that used RepoLens.

The log is `.aisdlc/metrics/intents.jsonl` unless `AISDLC_INTENTS_LOG` is set.
Lines follow `schemas/intent-run.schema.json`. `schemas/intent-run.example.jsonl`
is an example (`example: true`) and is not a measurement.

## Commands

```bash
aisdlc intent start --id implement --type implement --issue 12 --harness cursor --json
aisdlc intent tool --kind rg
aisdlc intent end --run <id> --outcome success --input-tokens 1000 --output-tokens 100
aisdlc intent run --id implement --type implement -- aisdlc gate pre-push

AISDLC_KNOWLEDGE=none aisdlc intent run --id implement --type implement -- aisdlc gate pre-push
aisdlc metrics intents summary --json
aisdlc metrics intents list --json
aisdlc metrics intents export --format csv
aisdlc metrics intents compare --a knowledge=repolens --b knowledge=none
```

`AISDLC_KNOWLEDGE=none` makes `aisdlc knowledge` return without running the
configured command. The intent record then has `knowledge` `none`.

## What is captured automatically

- `intent run` records start, end, duration, child exit code (`success` or `fail`),
  the git HEAD when one exists, and names from `git diff --name-only HEAD`.
- `aisdlc knowledge search` and `aisdlc knowledge impact` add a tool event when
  `AISDLC_INTENT_RUN` or `.aisdlc/metrics/current-run` names an open run.
- On `intent end`, if knowledge is not `none`, tool counts in `.repolens/usage.jsonl`
  between the start and end timestamps are added. The factory reads that file.
  It does not import RepoLens.

## What stays manual

- Harness, provider, and model, unless passed to `intent start`.
- Input, output, and cached tokens. They stay null when the harness does not
  report them. This recorder does not read a provider transcript.
- Cost. A `--cost-usd` value is stored as reported. Otherwise cost is
  `input * price + output * price` from `[cost.prices."<model>"]` (USD per
  million tokens) when both token counts and a price row exist. Otherwise null.
- Preflight restarts and retries. They stay 0 unless `intent end` is given
  `--preflight-restarts` or `--retries`.
- Hook-counted `rg`, `grep`, `read`, and `shell` calls. Point the harness hook
  at `aisdlc intent tool --kind rg` (or `grep`, `read`, `shell`).

`compare` prints `insufficient sample` for a metric when either side has fewer
than two numeric values.
