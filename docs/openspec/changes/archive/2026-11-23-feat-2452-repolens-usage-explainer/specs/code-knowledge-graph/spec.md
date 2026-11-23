## ADDED Requirements

### Requirement: Usage telemetry sees the calls agents make through a shell

The usage hook SHALL record a shell command that runs a RepoLens query as a RepoLens call of that session, with the files its answer names, and a shell search (`rg`, `grep`, `git grep`, `find`, `fd`, `git ls-files`) as a grep or glob call. It SHALL NOT record a read of a file outside the repository, and the report SHALL leave such reads already in a log out of the hit rate. `repolens setup` SHALL register the hook for the shell tool.

#### Scenario: a RepoLens search run through Bash counts for the session

- **WHEN** the hook receives a Bash payload whose command runs `repolens search` and whose output lists ranked results
- **THEN** it records a `repolens:search` event with the session id and the result files

#### Scenario: a shell grep counts as a fallback

- **WHEN** the hook receives a Bash payload whose command runs `rg` or `git grep`
- **THEN** it records a `grep` event for that session

#### Scenario: a read outside the repository is not navigation

- **WHEN** an agent reads a file whose absolute path is outside the repository, or the log already holds such a read
- **THEN** no event is recorded for it and the hit rate does not count it

#### Scenario: setup registers the hook for Bash

- **WHEN** `repolens setup` writes the Claude Code settings
- **THEN** the PostToolUse matcher includes `Bash`

### Requirement: Questions about code rank code above prose

When a question names a code construct and no documentation word, search SHALL weight documentation nodes by the `doc_factor` tuning value; `doc_factor = 1.0` SHALL disable it. Retrieval recall@10 and MRR on the tuning and held-out task sets SHALL NOT drop.

#### Scenario: a component question ranks the component first

- **WHEN** a question asks for "the station detail panel component" and a spec section and the component both match
- **THEN** the component ranks above the spec sections

#### Scenario: a documentation question keeps documentation

- **WHEN** the question also names documentation (spec, docs, README, ADR, guide)
- **THEN** documentation nodes keep their full weight

### Requirement: The explorer explains each answer and the measured impact

The explorer SHALL show, for each search, which hits matched the question directly and which the graph walk reached (with the relation), the time of each retrieval stage and the answer size in tokens. An Overview view SHALL show the pipeline with this store's counts, the usage report of this store and, when a benchmark folder is configured, each benchmark's arms (recall, tool calls, time, tokens, cost) with their caveats. `repolens serve --bench <dir>` SHALL configure that folder.

#### Scenario: a search explains itself

- **WHEN** someone searches in the explorer
- **THEN** each hit is labelled matched or reached through a named relation, and the page shows stage timings and the answer's tokens

#### Scenario: the overview shows measured impact

- **WHEN** the server runs with `--bench` naming a folder of benchmark results
- **THEN** `/api/metrics` lists each benchmark with its kind and arms, and the Overview shows them beside the live usage report

#### Scenario: no benchmark folder

- **WHEN** no benchmark folder is configured
- **THEN** the Overview shows the live numbers and says how to add benchmark results
