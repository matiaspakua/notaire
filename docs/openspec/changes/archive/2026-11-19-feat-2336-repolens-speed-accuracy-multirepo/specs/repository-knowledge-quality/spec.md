<!-- Governed by CONSTITUTION.md. CU76, issue #2336. -->

## Purpose

Make repolens cheaper and more accurate for agents: callers resolved through static types, answers from a resident service instead of a cold process per call, vectors that move between machines, one question across several repositories, exact lookups, and smaller tool schemas and answers.

## ADDED Requirements

### Requirement: Java calls are resolved through the receiver's static type

A call MUST link to the method of its receiver's type (field, parameter, local, `var`, `this`, `super`, static, chained return types, generic elements, lambda parameters, method references, Spring Data entities). A receiver whose type has no such method in the repository MUST NOT be linked to a same-named repository method.

#### Scenario: same-named methods on several classes

- **WHEN** `folio.getDto()` is called and `Folio` and `Deed` both declare `getDto`
- **THEN** only `Folio.getDto` is linked

#### Scenario: chains, generics, lambdas and method references

- **WHEN** a call goes through `a.getFolio().getDto()`, `list.get(0)`, `optional.orElseThrow()`, `for (var x : xs)`, `xs.forEach(x -> ...)` or `Folio::getDto`
- **THEN** the method of the resolved type is linked

#### Scenario: a library method is not linked

- **WHEN** `repository.save(x)` is inherited from Spring Data and a repository class declares `save`
- **THEN** no `calls` edge goes to that repository class

#### Scenario: measured against the compiler

- **WHEN** `bench/caller_bench.py` compares `calls` edges with the javac-resolved call graph
- **THEN** precision, recall and F1 are reported and are higher than 0.4.5's

### Requirement: Impact follows dynamic dispatch

`impact` on a method MUST include callers of the interface or superclass method it overrides.

#### Scenario: callers through a port

- **WHEN** `impact PortImpl.apply` runs and a service calls `port.apply()` on the interface
- **THEN** the service's file is listed

### Requirement: CLI queries are answered by a resident per-worktree service

A CLI query MUST reuse a resident service of its worktree when one runs, start one otherwise, refresh it when `HEAD` moves, and work in-process when the service is disabled or unavailable.

#### Scenario: the second query is served by the service

- **WHEN** two CLI searches run in a row in a worktree
- **THEN** the second is answered by the resident service with the same text as in-process

#### Scenario: the service follows HEAD and stops when idle

- **WHEN** `HEAD` moves, or no query arrives for the idle time
- **THEN** the next answer reflects the new commit, or the service exits

#### Scenario: disabled

- **WHEN** `REPOLENS_DAEMON=0` is set
- **THEN** the query runs in-process exactly as before

### Requirement: Vectors are portable and can be skipped per path

The shared vector cache MUST export to a file and import into another machine's cache, and paths matching `[embed] skip` MUST NOT be embedded while staying in keyword and graph search.

#### Scenario: a new machine imports the cache

- **WHEN** a cache exported on one machine is imported on another and a store is indexed there
- **THEN** the store embeds no text the exported cache held

#### Scenario: skipped paths

- **WHEN** a file matches `[embed] skip`
- **THEN** its nodes have no vector and are still found by keyword search

### Requirement: One question can span several indexed repositories

Every index run MUST register its store on the machine, and a search MUST be able to merge the answers of several registered repositories, labelled by repository. Each repository's answers MUST be weighted by its relevance to the question when the stores share an embedding space, and a repository that cannot place a hit in the top `k` MUST NOT be searched.

#### Scenario: registry

- **WHEN** two repositories are indexed
- **THEN** `repolens repos` lists both with their store and commit

#### Scenario: federated search

- **WHEN** `repolens search --repos all` (or the MCP `search` with `repos`) runs
- **THEN** hits of every repository are merged by rank and each names its repository

#### Scenario: the repository that matches the question ranks first

- **WHEN** one repository's relevance to the question is clearly higher than another's in the same embedding space
- **THEN** its hits rank first, equally relevant repositories interleave, and stores in different spaces are interleaved unweighted

#### Scenario: a repository out of reach is not searched

- **WHEN** a repository's weight cannot place its first hit in the merged top `k`
- **THEN** its search is not run and the merged answer is unchanged

#### Scenario: federated recall

- **WHEN** `bench/federation_bench.py` runs each task set against every registered store
- **THEN** recall@10 equals the owning store's own recall

### Requirement: Tool schemas cost fewer tokens per turn

The MCP tool list and server instructions MUST stay within a measured byte budget.

#### Scenario: schema budget

- **WHEN** the MCP server lists its tools
- **THEN** the tool list and instructions together stay under the budget the test pins

### Requirement: Lookups resolve exactly

`node`, `impact`, `path`, `outline`, `snippet` and `neighbors` MUST resolve a reference only to the node it names (id, path, `path:line`, name or qualified name). A reference that names no node MUST answer that it was not found, with the closest real symbols.

#### Scenario: an unknown name

- **WHEN** `impact` runs on a name no node has
- **THEN** it answers `not found` with the closest symbols (CLI and MCP) instead of the impact of another node

### Requirement: Answers name each file once

`impact` and `node` MUST list each related file once with its symbols, and count paths under `[ranking] demote` instead of listing them; MCP tools MUST return each answer once as plain text.

#### Scenario: impact and node

- **WHEN** several symbols of one file depend on the node
- **THEN** the file's path appears once with the symbols as `name:line`, and demoted paths appear as a count

#### Scenario: one copy per answer

- **WHEN** an MCP tool answers
- **THEN** the answer carries no structured copy and the tool list declares no output schema
