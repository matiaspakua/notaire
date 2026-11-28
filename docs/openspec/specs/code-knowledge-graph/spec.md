# code-knowledge-graph Specification

## Purpose

A local code knowledge graph (repolens) that scans repositories, links code, tests, docs, routes and tables into a typed graph, and answers retrieval questions for people and AI agents over a CLI, an HTTP API and an MCP server.
## Requirements
### Requirement: Retrieval ranks the relevant files first

A natural-language question or an identifier MUST return the files that implement, call or test the subject, ranked by keyword, semantic and graph evidence, without sending any code off the machine.

#### Scenario: a question about behaviour finds the implementing files

- **WHEN** a question names behaviour but not the class that implements it
- **THEN** the implementing file and its related files are in the top results

#### Scenario: test files rank lower unless the question is about tests

- **WHEN** a question does not mention tests, specs or coverage
- **THEN** the production file ranks above its test file, and the test file ranks first when the question names it

#### Scenario: an identifier resolves exactly

- **WHEN** the query is a symbol name, a `path#Symbol` reference or `path:line`
- **THEN** the matching node is returned without a similarity search

### Requirement: Indexing is incremental and keeps the graph available

Indexing MUST not re-read unchanged files, MUST publish the graph before the slow embedding step, and MUST not rewrite derived nodes that did not change.

#### Scenario: a run with no changes does nothing

- **WHEN** the index runs and no file changed
- **THEN** no file is parsed, the graph is not relinked and the stored index time is unchanged

#### Scenario: the graph is published before embedding

- **WHEN** the embedding step starts after a changed run
- **THEN** another process can already open the store and answer queries

#### Scenario: relinking keeps unchanged derived nodes

- **WHEN** the linker runs again over an unchanged store
- **THEN** derived nodes and their keyword entries keep their rows, and a removed node disappears from both

### Requirement: One graph behind every interface

The CLI, the HTTP API and the MCP server MUST answer from the same engine and the same store.

#### Scenario: the three interfaces agree

- **WHEN** the same question is asked through the CLI, the HTTP API and the MCP `search` tool
- **THEN** each returns the same ranked files

### Requirement: The module verifies and registers itself

The module MUST ship a `verify.sh` that lints, tests with a coverage floor, builds the wheel and runs a smoke search, and MUST be declared in `workspace/modules.yaml`.

#### Scenario: the module gate passes

- **WHEN** `bash framework/repolens/verify.sh` runs on a clean checkout
- **THEN** lint, tests (coverage at or above 88%), the wheel build and the smoke search all succeed

#### Scenario: the module is declared in the manifest

- **WHEN** `python3 workspace/modules.py list` runs
- **THEN** `repolens` is listed with its path, its `verify` command and no dependencies

### Requirement: `repolens setup` wires the Junie CLI alongside every other harness

`repolens setup` SHALL register the RepoLens MCP server with every supported harness: Claude Code, Cursor, Codex, OpenCode, and Junie. The Junie configuration file `.junie/mcp/mcp.json` SHALL use the same launcher command and arguments as the other harnesses.

#### Scenario: setup writes Junie MCP config

- **WHEN** `repolens setup` runs on a repository
- **THEN** `.junie/mcp/mcp.json` is created with `{"mcpServers": {"repolens": {"command": ..., "args": [...]}}}`
  using the same launcher as `.cursor/mcp.json`

#### Scenario: dry-run reports Junie drift

- **WHEN** `repolens setup --dry-run` runs and `.junie/mcp/mcp.json` is missing or stale
- **THEN** the output lists `junie` with the files it would update, without writing them

#### Scenario: committed wiring matches the generator

- **WHEN** `repolens setup --dry-run` runs on the committed wiring
- **THEN** no drift is reported for the `junie` target

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

### Requirement: RepoLens path is a git submodule of the product repository

Notaire MUST record `framework/repolens` as a git submodule whose URL is the independent RepoLens product repository, and MUST NOT vendor a second copy of that tree in the monorepo.

#### Scenario: gitmodules points at the product

- **WHEN** a developer inspects `.gitmodules`
- **THEN** an entry with `path = framework/repolens` names the `draclab/repolens` repository

#### Scenario: module verify still runs from the same path

- **WHEN** the submodule is initialized and `bash framework/repolens/verify.sh` runs
- **THEN** the RepoLens module gate executes from that path as before

#### Scenario: harness wiring still resolves the launcher

- **WHEN** `workspace.tests.test_repolens_integration` runs on an initialized checkout
- **THEN** the launcher under `framework/repolens/bin/repolens` and `MODULE.md` remain reachable

#### Scenario: fresh clone instructions initialize the submodule

- **WHEN** a developer follows the README clone instructions
- **THEN** the instructions use `--recurse-submodules` or an explicit `git submodule update --init`

