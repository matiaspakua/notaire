# AGENTS.md — {{project}}

Entry point for every coding agent. Read [`AGENT-CARD.md`](AGENT-CARD.md) now and
[`CONSTITUTION.md`](CONSTITUTION.md) when deciding a gate, an exception or Done.

- Commands for this stack: `aisdlc gate --list` (from `aisdlc.toml`).
- Before a push: `aisdlc gate pre-push`. Before a pull request: `aisdlc gate pre-pr`.
- Role definitions: `.aisdlc/agents/`. Load rules and skills on demand, not up front.
