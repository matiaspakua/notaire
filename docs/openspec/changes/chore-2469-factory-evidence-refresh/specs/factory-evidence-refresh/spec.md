## ADDED Requirements

### Requirement: Bench results record the RepoLens version that produced them

Every retrieval bench result SHALL carry `repolens_version`, the version of the RepoLens package that ran it, next to
the corpus commit.

#### Scenario: a retrieval run is stamped

- **WHEN** `retrieval_bench.py` writes a result
- **THEN** the result's metadata carries `repolens_version` equal to `repolens.__version__`

### Requirement: One command refreshes the factory evidence

`python3 workspace/ci/refresh-factory-evidence.py` SHALL run, in order: the RepoLens index refresh, the retrieval and
holdout benches, the bench report, the Pages bench snapshot, the Junie usage import for every run, and the factory
metrics snapshot. Paid agent benchmarks SHALL run only with `--agent`, and the GitHub history only with `--history`.
`--dry-run` SHALL print the steps without running them. A failing step SHALL stop the refresh with its exit code.

#### Scenario: the default refresh is free

- **WHEN** `refresh-factory-evidence.py --dry-run` runs
- **THEN** it lists the free steps in order and no agent bench

#### Scenario: agent benches are opt-in

- **WHEN** `refresh-factory-evidence.py --dry-run --agent` runs
- **THEN** the steps include the agent bench

### Requirement: Stale engines and evidence are reported

`refresh-factory-evidence.py --check` SHALL report each stale item and exit 1 when any is stale: a bench result whose
`repolens_version` differs from the checkout's RepoLens version (or is missing), and a checkout whose
`framework/repolens` or `framework/ai-sdlc` lacks commits that `origin/main` has. Session start SHALL print the same
report and never block.

#### Scenario: results from an older RepoLens are stale

- **GIVEN** a bench result stamped with a version different from the checkout's
- **WHEN** `--check` runs
- **THEN** it names the result and exits 1

#### Scenario: an engine behind origin/main is stale

- **GIVEN** a checkout whose `framework/repolens` lacks a commit `origin/main` has
- **WHEN** `--check` runs
- **THEN** it says RepoLens is behind `origin/main` and exits 1

#### Scenario: everything current

- **GIVEN** stamped results at the current version and engines level with `origin/main`
- **WHEN** `--check` runs
- **THEN** it exits 0

### Requirement: A re-measured bench refreshes without manual edits

The Pages bench snapshot SHALL accept any headline that is a factor label (`5.0x more`, `41x faster`, `+12%`,
`same`) and reject anything else. A task set that declares `chart_tag` SHALL pass it into its results, so the bench
report keeps the task set's chart file names.

#### Scenario: a new latency factor validates

- **WHEN** the latency headline changes from `41x faster` to `42x faster` after a refresh
- **THEN** the snapshot validates

#### Scenario: a malformed headline is rejected

- **WHEN** a headline reads `fast!`
- **THEN** validation fails

#### Scenario: the tuning set keeps its chart names

- **GIVEN** a task set with `chart_tag: ""`
- **WHEN** its retrieval bench runs
- **THEN** the result carries `chart_tag` and the report writes `retrieval-*.svg`
