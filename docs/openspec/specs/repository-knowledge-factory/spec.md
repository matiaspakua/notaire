# repository-knowledge-factory Specification

## Purpose

repolens as the repository-knowledge tool of the AI SDLC factory: every agent harness starts it, the index follows each agent's worktree, branch and edits, and its use is measured so the factory can improve it.

## Requirements

### Requirement: Every harness starts the same repolens server

`repolens setup` MUST register the MCP server with Claude Code, Codex, OpenCode and Cursor in their native files, merging into existing entries, and the committed registrations MUST equal what it generates.

#### Scenario: setup registers every harness once

- **WHEN** `repolens setup` runs twice in a repository with other servers and hooks configured
- **THEN** each harness file registers repolens, the other entries are kept, and the second run changes nothing

#### Scenario: the committed wiring has not drifted

- **WHEN** `repolens setup --dry-run` runs at the repository root
- **THEN** it reports nothing to update, and every harness starts the same launcher command

### Requirement: The index follows each worktree, branch and edit

Each worktree MUST have its own index, refreshed in the background before tool calls, serialised across processes, and seeded from a shared content-addressed vector cache.

#### Scenario: an agent's edit is visible to the next query

- **WHEN** a file changes and a refresh is due
- **THEN** a background refresh indexes it and the long-lived engine answers from the new graph

#### Scenario: switching branches re-indexes only the difference

- **WHEN** the worktree switches to a branch that adds one file and removes another, and back
- **THEN** only those files are extracted or removed and each answer matches the current branch

#### Scenario: a new worktree reuses the shared vectors

- **WHEN** a second worktree with the same files is indexed
- **THEN** it embeds no text and takes every vector and the projection from the shared cache

#### Scenario: a second indexer steps aside

- **WHEN** an index run holds a store and another starts without waiting
- **THEN** the second returns busy at once and the store is not touched

### Requirement: Use is measured and reported

The MCP server MUST log every tool call and the Claude Code hook MUST log navigation calls; `repolens report` MUST compute adoption, hit rate, grep fallback, navigation tokens and latency with recommendations, and its gate line MUST never fail the factory.

#### Scenario: the server builds on first start and logs each call

- **WHEN** the MCP server starts on an unindexed repository and answers a search
- **THEN** the index is built in the background and the usage log holds the refresh and the call with its latency, size and files

#### Scenario: the report measures real sessions

- **WHEN** hook events show one session that used repolens and one that did not
- **THEN** the report gives adoption, hit rate and fallback for them, and recommendations for every threshold crossed

### Requirement: The factory profile names repolens

The AI SDLC profile MUST list repolens on the stages where agents navigate code, catalog it, record its usage report as an advisory gate, and preflight MUST run the module gate when the module changes.

#### Scenario: stages, catalog and gate

- **WHEN** the Foreman's dispatch plan and the signal stage run
- **THEN** plan, red and pre-pr list `mcp=repolens`, the catalog entry validates, and the usage gate passes as advisory
