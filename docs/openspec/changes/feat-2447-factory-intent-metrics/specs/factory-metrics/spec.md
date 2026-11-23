## ADDED Requirements

### Requirement: Cost rows are attributable to a run step

`aisdlc cost` SHALL accept `--run`, `--step`, `--cache-read`, `--cache-write` and `--source`, and price
cache tokens from `[cost.prices]` when the model has `cache_read` and `cache_write` prices. A provider
cost passed with `--usd` SHALL be kept as reported. A row without a price SHALL stay unpriced (`usd`
null), never 0.

#### Scenario: a priced row with cache tokens

- **GIVEN** a price table with input, output, cache_read and cache_write for a model
- **WHEN** a cost is recorded with input, output and cache-read tokens for run `r1`, step `red`
- **THEN** the row carries the run, the step and a USD amount that includes the cache-read price

### Requirement: Usage is imported from worker logs

`aisdlc cost import --source junie|claude-code --path P --run R` SHALL attribute token usage to the
run's steps by time window. Junie logs SHALL be read from their last token snapshot. Claude Code
messages SHALL be counted once per request. Importing twice SHALL add nothing.

#### Scenario: Junie sessions are attributed to their steps

- **GIVEN** a run whose `red` step ran from 10:00 to 10:05 and a Junie log that started at 10:01
- **WHEN** the Junie usage is imported for the run
- **THEN** one cost row for step `red` carries that log's tokens, and a second import adds nothing

### Requirement: Factory metrics per intent and overall

`aisdlc metrics factory --json` SHALL report, per intent and per run, wall time, active step time,
time per stage, corrections, tokens, USD with whether it is fully priced, and the RepoLens calls and
tokens served in the run's window. It SHALL report overall totals and medians, and the RepoLens effect
from benchmark summaries with their sample sizes. `GET /factory/metrics/factory` SHALL return the
same document.

#### Scenario: an intent's time and cost

- **GIVEN** an intent with one run of two steps and cost rows for both steps
- **WHEN** the factory metrics are built
- **THEN** the intent shows its wall and active time, its per-stage seconds, its summed tokens and its USD

#### Scenario: no data stays visible as no data

- **GIVEN** a run with no cost rows
- **WHEN** the factory metrics are built
- **THEN** its tokens are zero and its USD is null, not zero
