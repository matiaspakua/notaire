<!-- Governed by CONSTITUTION.md. CU76, issue #2297. -->

## Purpose

A local code knowledge graph (repolens) that scans repositories, links code, tests, docs, routes and tables into a typed graph, and answers retrieval questions for people and AI agents over a CLI, an HTTP API and an MCP server.

## ADDED Requirements

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

- **WHEN** `bash framework/ai-sdlc/graph-db/verify.sh` runs on a clean checkout
- **THEN** lint, tests (coverage at or above 88%), the wheel build and the smoke search all succeed

#### Scenario: the module is declared in the manifest

- **WHEN** `python3 workspace/modules.py list` runs
- **THEN** `repolens` is listed with its path, its `verify` command and no dependencies
