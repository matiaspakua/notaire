# repository-knowledge-coverage Specification

## Purpose

Treat every file of a repository as knowledge whatever its language or format, and report what relates to nothing.

## Requirements

### Requirement: Any parsable language yields definitions and references

A file whose language has a tree-sitter grammar MUST yield its definitions (with containment) and its call and type references: through the hand-written spec when one exists, else through the language's tags query, else through the generic walker. A file whose grammar cannot be loaded MUST be indexed as text, with one load attempt per language per process. A text file MUST keep its detected language name.

#### Scenario: a language without a hand-written spec

- **WHEN** a Dart or Elixir file defines a class and a function and calls the function from another file
- **THEN** both definitions are nodes and the call is a `calls` edge

#### Scenario: a grammar without a tags query

- **WHEN** a shell script defines a function
- **THEN** the function is a node of the file

#### Scenario: an ESM TypeScript import

- **WHEN** a TypeScript file imports `./format.js` and the source is `format.ts`
- **THEN** the import resolves to `format.ts` and calls through it are linked

#### Scenario: a format with no definitions

- **WHEN** a Terraform or Groovy build file is indexed
- **THEN** it is a text node with the language name the pack detects, and its mentions are linked

### Requirement: Any text links the files, directories and symbols it names

Every text file, code included, MUST link the repository files and directories it names (full, `./` and `../` relative, and bare file names) and every non-code text file MUST link the code symbols it names (CamelCase, snake_case or dotted identifiers that resolve to a type or function). An ambiguous name MUST resolve to the candidates nearest the mentioning file, and to nothing when more than three remain.

#### Scenario: paths in any form

- **WHEN** a JSON file names `.claude/skills/review`, a script names `./lib/common.sh` and a YAML file names `docker-compose.yml`
- **THEN** each links the directory or file it names

#### Scenario: an ambiguous bare name

- **WHEN** a file names `index.ts` and several directories hold an `index.ts`
- **THEN** only the one in the nearest directory is linked, or none when more than three are equally near

#### Scenario: identifiers in configuration

- **WHEN** a YAML file names `CodeVerificationService`
- **THEN** it links the class

### Requirement: Orphans are reported

repolens MUST list the files with no relation to any other file (structure such as containment and directories does not count; a shared route, table or tag does; a shared library import does not) and, separately, the files related only through git history (co-change, issue tags). Symbols with no relation MUST be listed on request. Paths under `[ranking] demote` and files a tool or runtime reads by name (`[orphans] conventional`: `package-info.java`, `LICENSE`, `__init__.py`...) MUST be counted, not listed, unless requested. The list MUST be available from the CLI, the HTTP API, the MCP server and the UI, and an index run MUST report how many orphans there are.

#### Scenario: an isolated file

- **WHEN** a file is named by no other file and names none
- **THEN** `repolens orphans` lists it

#### Scenario: related only through history

- **WHEN** a file's only relation is a co-change with another file
- **THEN** it is listed as related only through git history, not as isolated

#### Scenario: a file a tool reads by name

- **WHEN** `LICENSE` or `__init__.py` relates to nothing
- **THEN** it is counted as conventional, listed with `--all`, and listed as an orphan when `[orphans] conventional` is empty

#### Scenario: every interface

- **WHEN** orphans are requested from the CLI, `/api/orphans`, the MCP `orphans` tool and the UI
- **THEN** each returns the same files

#### Scenario: the index run alerts

- **WHEN** `repolens index` finishes and orphans exist
- **THEN** it prints how many and the command that lists them

### Requirement: Coverage and orphans are measured on real repositories

The change MUST record before/after numbers on Notaire and the scale corpus: isolated and history-only files, the share of reported orphans whose file name no other file contains, retrieval recall and MRR, and index time.

#### Scenario: measured

- **WHEN** `bench/orphan_bench.py` runs on both corpora
- **THEN** the numbers are written to `bench/results/` and quoted in the README
