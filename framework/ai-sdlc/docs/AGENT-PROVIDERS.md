# Agent providers

`aisdlc agent probe` reports which CLI can authenticate. `aisdlc agent run` launches one
headless session. The default order is `anthropic`, then `cursor`.

Anthropic is available when `claude` is on `PATH` and either `ANTHROPIC_API_KEY` is set
or `claude auth status` exits 0. Cursor is available when `agent` or `cursor-agent` is on
`PATH` and either `CURSOR_API_KEY` is set or `agent status` exits 0.

Override the order with `[agents] order` in the profile, `AISDLC_AGENT_ORDER`, or
`--order`. `--provider cursor` (or `AISDLC_AGENT_PROVIDER`) forces that provider and does
not fall back. A run records provider, model, latency, exit code and token counts in
`.aisdlc/evidence/agent-runs.jsonl`. The prompt is not recorded.

# Code knowledge

RepoLens is the default `[knowledge]` provider. `aisdlc knowledge search` shells out to
the configured command. The kit does not import RepoLens. Each call records tool,
latency, exit code and output size in `.aisdlc/evidence/knowledge.jsonl`. Navigation
tokens from agent sessions stay in the RepoLens usage log (`repolens report`).
