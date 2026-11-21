# ADR-006: The dashboard is a contract client; RepoLens is a separate system

**Status:** Proposed. The UI host is an Owner decision (FACTORY-UI-SPEC.md, "Owner decision"); this ADR records the choice the branch implements and keeps the static option open.
**Issue:** [#2384](https://github.com/matiaspakua/notaire/issues/2384) (CU76).
**Builds on:** ADR-002 (ports and trust), ADR-003 (standards), the factory UI spec (#2251).

## Context

The factory dashboard from #2381 worked, but it read and wrote the kit's files directly
(`.aisdlc/evidence`, `.aisdlc/runs`, `.claude/`). It sent no token, no scope and no CSRF header,
and recorded no actor. It showed demo runs that never happened and offered play, pause and resume,
which the contract does not have. It served one project only, with that project's paths
hard-coded. RepoLens lived inside the kit folder although the kit never imports it.

Three questions follow:

1. Where does a rich UI belong when the kit core must stay stdlib-only?
2. How do several people, projects, repositories and machines share one dashboard?
3. How do the kit and RepoLens stay compatible once they live in separate repositories?

## Decision

### 1. The dashboard is an optional client of `aisdlc serve`

`framework/ai-sdlc/factory-dashboard` is an optional Node companion. It is not part of the
stdlib-only core and installing the kit does not require it. It speaks two HTTP contracts and
nothing else:

| Contract | What the dashboard uses |
|----------|-------------------------|
| `openapi/factory-control.openapi.yaml` (`aisdlc serve`) | every `/factory/*` read and write |
| RepoLens HTTP API (`repolens serve`) | `search`, `orphans`, `metrics`, `stats`, `usage` (read only) |

It never opens a file of a workspace. The kit stays the system of record and enforces scope,
CSRF, `If-Match` and the actor. A static UI served by `aisdlc serve /ui` remains possible, because
that UI would use the same contract.

### 2. Users are kit tokens; workspaces are endpoints

- **Users.** `aisdlc serve-token add --identity <who> --scope read|write|owner` creates one named
  token per person in `.aisdlc/serve.tokens` (mode 0600, git-ignored). The kit records
  `ui:<identity>` as the actor of every write. `GET /factory/session` tells a client who the caller
  is, so it shows read-only states before a submit fails.
- **Sessions.** A person signs in to a workspace with their own token. The dashboard server keeps
  the token. The browser gets an `HttpOnly; SameSite=Strict` cookie and a per-session CSRF value.
- **Workspaces.** `dashboard.json` lists workspaces. Each one is a kit endpoint plus an optional
  RepoLens endpoint. Projects, repositories and machines combine without shared storage. A project
  groups repositories inside one kit (`/factory/projects/{id}/repos`). RepoLens labels each search
  hit with its repository.
- **Providers.** AI providers are catalog entries (`/factory/catalogs/providers`) with `model`,
  `endpoint` and a credential. A credential is only ever `env:NAME`. The kit refuses a literal
  secret with `literal_secret`.

### 3. The coach lives in the kit and only the owner reads it

`aisdlc coach` and `GET /factory/coach` read the session stores of local harnesses (Claude Code,
Codex, OpenCode, Antigravity) read-only. They report counts, model and tool names, and the
anti-patterns of the engineering audit. Prompt and response text never leave the process. The
path needs the `owner` scope, because it describes how the host's user works.

### 4. RepoLens moves to `framework/repolens`

RepoLens is a separate system with its own package, tests, `verify.sh` and changelog. The kit
reaches it only through the `[knowledge] command` (a CLI), the MCP server, and its HTTP API. Each
side checks its own boundary. The kit has no RepoLens tree and imports no `repolens`. RepoLens
imports no `aisdlc` and names no adopter in its package, runners or launcher.

## Consequences

- One contract serves the CLI, the dashboard and any future UI. A feature the dashboard needs is a
  contract change first: `/factory/session`, `POST /factory/catalogs/{kind}` and `fields` on PUT
  came from this ADR.
- The dashboard needs Node 22 or later. Teams that cannot run Node keep the CLI and `aisdlc serve`.
- Live updates are polling (5 s for runs). The kit has no push channel yet. An event stream on
  `aisdlc serve` would be a contract change.
- Sessions live in the dashboard process. A restart signs everyone out.
- Extracting either system into its own repository is a directory copy plus its wiring.
  `repolens setup` rewrites the wiring of a repository.

## Alternatives considered

- **Keep reading the kit's files from the dashboard.** Rejected. It bypasses authorization and the
  actor, and it binds the UI to one disk layout and one machine.
- **Give the browser the kit token.** Rejected. A script on the page could read it. A server-side
  session keeps the token off the browser.
- **Put the coach in the dashboard.** Rejected. It would need file access to the host's home and a
  second place that knows the harness formats. The kit already runs on that host.
