<!-- Governed by CONSTITUTION.md. CU76, issue #2336. -->

## Purpose

Make repolens cheaper and more accurate for agents: callers resolved through static types, answers from a resident service instead of a cold process per call, vectors that move between machines, one question across several repositories, and smaller tool schemas.

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

Every index run MUST register its store on the machine, and a search MUST be able to merge the answers of several registered repositories, labelled by repository.

#### Scenario: registry

- **WHEN** two repositories are indexed
- **THEN** `repolens repos` lists both with their store and commit

#### Scenario: federated search

- **WHEN** `repolens search --repos all` (or the MCP `search` with `repos`) runs
- **THEN** hits of every repository are merged by rank and each names its repository

### Requirement: Tool schemas cost fewer tokens per turn

The MCP tool list and server instructions MUST stay within a measured byte budget.

#### Scenario: schema budget

- **WHEN** the MCP server lists its tools
- **THEN** the tool list and instructions together stay under the budget the test pins
