# AGENTS.md — {{project}}

Entry point for every coding agent. Read [`AGENT-CARD.md`](AGENT-CARD.md) now and
[`CONSTITUTION.md`](CONSTITUTION.md) when deciding a gate, an exception or Done.

- Commands for this stack: `aisdlc gate --list` (from `aisdlc.toml`).
- Before a push: `aisdlc gate pre-push`. Before a pull request: `aisdlc gate pre-pr`.
- Role definitions: `.aisdlc/agents/`. Load rules and skills on demand, not up front.

## Repository knowledge

RepoLens is the default mechanism for code knowledge and navigation. The factory
calls it through the command in `[knowledge]` (`aisdlc knowledge`); it does not
import the package. Use `repolens search` when the file or symbol is unknown,
`rg` for an exact symbol or path, `repolens impact` for callers, and
`repolens miss` when you fall back. Adoption, tool calls, tokens and latency
are recorded by `repolens report` and by `aisdlc knowledge` (tool, latency,
exit code, output size — not the query text).
