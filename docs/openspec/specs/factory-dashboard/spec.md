# factory-dashboard Specification

## Purpose

The factory dashboard is a client of the AI SDLC factory-control contract that serves several workspaces, users and
AI providers, coaches the local agent harnesses, and reaches a detached RepoLens.

## Requirements

### Requirement: The dashboard reads and writes only through the factory-control contract

The dashboard MUST obtain projects, runs, events, catalogs and capabilities from `aisdlc serve` and MUST send every
write there with the signed-in user's token, the CSRF header and `If-Match`. It MUST NOT write profile files itself
and MUST NOT show a run as running unless the kit reports it.

#### Scenario: a catalog edit

- **WHEN** a signed-in user saves an agent
- **THEN** the dashboard sends `PUT /factory/catalogs/agents/{id}` with the user's bearer token, `X-AISDLC-CSRF` and `If-Match`, and writes no file

#### Scenario: the kit is not reachable

- **WHEN** the workspace's `aisdlc serve` does not answer
- **THEN** the dashboard answers a problem with code `kit_unreachable` and the start command, and the UI shows the error state

#### Scenario: no fabricated run

- **WHEN** the kit reports no run
- **THEN** the runs list is empty and the production line shows its empty state

### Requirement: One dashboard serves several workspaces

The dashboard MUST read a list of workspaces, each an `aisdlc serve` endpoint with an optional RepoLens endpoint, and
MUST scope every request to one workspace.

#### Scenario: two workspaces

- **WHEN** two workspaces are configured
- **THEN** both are listed with their reachability, and each lists the projects and repositories its kit reports

### Requirement: Several users act with their own scopes

`aisdlc serve` MUST accept several named tokens, each with a scope, and MUST record the matching identity as the
actor of every write. The dashboard MUST sign a user in with their own token, keep it server-side and never return
it to the browser.

#### Scenario: two named tokens

- **WHEN** two tokens with different identities and scopes are configured
- **THEN** each authenticates with its own scope and a write records its own identity as the actor

#### Scenario: a read-only user

- **WHEN** a user signed in with a read token attempts a write
- **THEN** the write is refused as forbidden, naming the required scope

### Requirement: The coach analyses local agent harnesses

`aisdlc coach` MUST report, per local harness (Claude Code, OpenCode, Codex, Gemini/AGY, Junie CLI), the sessions,
user turns, models and tool calls, and MUST flag anti-patterns (mega-sessions, repeated prompts, prompt bloat,
runaway tool loops, unguarded autonomy, no planning turn). The report MUST NOT contain prompt or response text.
The Junie reader MUST NOT read `EnvironmentVariablesUpdatedEvent` (contains secrets). It MUST be available from
the CLI and, to the owner, from `GET /factory/coach`.

#### Scenario: a Claude Code session

- **WHEN** a Claude Code session log with a repeated prompt and a long tool loop is analysed
- **THEN** the report counts its turns, models and tool calls and flags both anti-patterns, without prompt text

#### Scenario: through the contract

- **WHEN** `GET /factory/coach` is called with the owner's token
- **THEN** it returns the same report as `aisdlc coach --json`

#### Scenario: another user's token

- **WHEN** `GET /factory/coach` is called with a read token
- **THEN** it is refused as forbidden, naming the owner scope, because the report describes the host's user

### Requirement: RepoLens is a detached system

RepoLens MUST live in its own directory, MUST NOT import the AI SDLC kit or name Notaire paths in its package, and the
dashboard MUST reach it only through its HTTP API.

#### Scenario: a knowledge search

- **WHEN** a user searches knowledge in a workspace with a RepoLens endpoint
- **THEN** the dashboard proxies the query to that RepoLens and returns its hits labelled by repository

#### Scenario: a literal secret

- **WHEN** a user saves a provider whose `api_key` is a literal value instead of `env:NAME`
- **THEN** the kit refuses it with `literal_secret` and stores nothing

#### Scenario: independence

- **WHEN** the RepoLens package is scanned
- **THEN** it imports nothing from `aisdlc` and names no Notaire path
