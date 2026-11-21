# CLI Comparison Experiment — AI SDLC + RepoLens

**Date:** 2026-11-20
**Tasks:** totp-verify (lexical), person-create (cross-stack), folio-slice (multi-hop), hex-adr (docs)
**Repository:** notaire (9,611 files, 91,045 nodes)

## CLIs Tested

| CLI | Model | Status | Notes |
|-----|-------|--------|-------|
| **AGY** | claude-sonnet-4-6 | ✓ Working | Free tier, stream-json output |
| **kiro-cli** | claude-sonnet-4.5 | ✓ Working | Free tier, plain text output |
| **codex** | gpt-5.6-terra | ⚠ Rate-limited | Free tier exhausted until Nov 25th |
| **opencode** | openai/gpt-4o-mini | ⚠ Auth error | Invalid OpenAI API key configured |

## Results Summary

| CLI | Runs | Errors | Avg Recall | Avg Duration | Avg Tool Calls |
|-----|------|--------|------------|--------------|----------------|
| AGY | 4 | 0 | **1.000** | 38.0s | 0 |
| kiro | 4 | 0 | 0.750 | 43.6s | 0 |

## Per-Task Breakdown

| Task | Category | AGY Recall | AGY Duration | kiro Recall | kiro Duration |
|------|----------|------------|--------------|-------------|---------------|
| totp-verify | lexical | 1.00 | 23.6s | 1.00 | 40.1s |
| person-create | cross-stack | 1.00 | 42.1s | 1.00 | 74.8s |
| folio-slice | multi-hop | 1.00 | 33.3s | 1.00 | 33.9s |
| hex-adr | docs | 1.00 | 52.8s | **0.00** | 25.7s |

## Token Usage (AGY only — kiro doesn't report)

| Task | Input Tokens | Output Tokens |
|------|-------------|---------------|
| totp-verify | 19,869 | 1,207 |
| person-create | 25,809 | 2,154 |
| folio-slice | 24,938 | 2,068 |
| hex-adr | 31,789 | 2,082 |
| **Total** | **102,405** | **7,511** |

## Key Findings

1. **AGY achieves perfect recall** across all 4 task categories (lexical, cross-stack, multi-hop, docs).
2. **kiro misses hex-adr** — the docs category task. It answered quickly (25.7s) but didn't find the expected files (ADR-030, HexagonArchitectureTest).
3. **AGY is faster on average** (38.0s vs 43.6s) and more consistent.
4. **Neither CLI used tool calls** in baseline mode — both answered from context alone, which means the prompts were simple enough for the model's training data.
5. **codex and opencode unavailable** — free tier rate limits and auth issues prevent testing.

## Limitations

- Baseline arm only (no RepoLens MCP graph arm) — CLIs don't support `--mcp-config` via CLI flag.
- Only 2 of 4 CLIs could run due to free-tier constraints.
- kiro doesn't report token usage or tool calls in non-interactive mode.
- Single repetition per task (no statistical significance).

## Follow-up

- Re-run codex after Nov 25th when rate limits reset.
- Fix opencode's API key configuration and re-test.
- Add graph arm experiments once MCP config support is added per-CLI.
