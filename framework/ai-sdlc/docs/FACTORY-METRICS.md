# Factory metrics: time, cost and RepoLens per intent

`aisdlc metrics factory` answers three questions for each intent, each run and the whole history (#2447):

1. How long did the intent take?
2. How many tokens and how much money did it use?
3. What does RepoLens change in cost, speed and accuracy?

The same document is served at `GET /factory/metrics/factory`. The dashboard shows it at the top of
**Metrics**, and GitHub Pages publishes a snapshot on **Results** (`github-page/lib/factory-metrics-snapshot.json`).

## Where each number comes from

| Number | Source | Notes |
|---|---|---|
| Wall time | Evidence ledger: first to last event of the intent's runs | Includes waiting, for example a gate or a merge |
| Active time, time per stage | Ledger: each `step.started` to its `step.finished` | Every attempt counts, so a retried step counts twice |
| Corrections | Ledger: `step.finished` with `step_status=fail` | One per failed step attempt |
| Tokens | Cost rows (`.aisdlc/evidence/cost.jsonl`) attributed to the run | Input, output, cache read and cache write are kept apart |
| USD | Cost rows: priced from `[cost.prices]`, or kept as the provider reported | `null` when nothing is recorded; `fully_priced` is false when a row has no price |
| RepoLens calls | `.repolens/usage.jsonl` entries inside the run's window | Calls and tokens served; it does not tell which agent called |
| RepoLens effect | `framework/repolens/bench/results/agent-*.json` | The same model and tasks with and without RepoLens; `runs_per_arm` is the sample size |
| Live A/B | `aisdlc metrics intents` paired runs (`AISDLC_KNOWLEDGE=none`) | Needs at least two runs on each side before it is a measurement |

## Recording real usage

```bash
# a worker's own sessions, attributed to the step whose window contains them
aisdlc usage import --source junie --path ~/.junie/logs --run issue-760

# the Foreman's Claude Code session, split across every run; the rest is overhead
aisdlc usage import --source claude-code --path ~/.claude/projects/<project>/<session>.jsonl --all-runs

# one row by hand
aisdlc cost --agent foreman --model claude-opus-5-5 --input 1200 --output 300 --cache-read 50000 \
  --run issue-760 --step green --source manual

# a run started before intents existed
aisdlc intent attach --id int-... --run issue-2399
```

Junie logs end with an `llm_cost_trajectory_snapshot`. The import reads the session totals and the
reported cost from it. When the task text names an issue (`GitHub issue #N`), the session goes only
to that issue's run (`issue-N`, `issue-N-rK`).

Claude Code transcripts carry `message.usage` per request. Each request is counted once.
`--all-runs` gives a request to the run whose window contains it. Overlapping windows split the
request evenly. A request between runs is overhead (`run: null`, `step: overhead`), which is
included in the overall cost.

Each import is idempotent: every row carries a `key`, and an import skips the keys it already
recorded.

## Prices

`[cost.prices.<model>]` takes `input`, `output` and optional `cache_read`, `cache_write` (5-minute
TTL) and `cache_write_1h`, in USD per million tokens. Cache writes default to the documented
multipliers of the input price: 1.25x for the 5-minute TTL and 2x for the 1-hour TTL. Cache reads
need their own price, because the ratio differs by model. A row with cache reads but no
`cache_read` price stays unpriced.

## Reading the numbers honestly

- Junie reports `total_cost=0.0` for its plan, so its tokens are real but its USD is $0. The money
  is the Foreman's Claude usage.
- Overhead is real cost: planning, reviews, merges and pull requests between runs.
- A Foreman run window can overlap another run's. `--all-runs` splits shared requests evenly and
  marks the row as split, so the split is visible.
- The RepoLens effect is a benchmark result. The factory-level effect needs paired live runs:
  start comparable intents with `AISDLC_KNOWLEDGE=none` and with RepoLens.

## Publishing to GitHub Pages

The ledger, the cost rows and the usage log are local. On the factory's machine, run:

```bash
python3 workspace/ci/generate-factory-metrics.py   # writes github-page/lib/factory-metrics-snapshot.json
```

Commit the snapshot. The file changes only when the data does.
