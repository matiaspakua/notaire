> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134 closed. #2182 stage knobs and #2184 ForgePort read-only lead-time landed. Gap audit ASLI-47 owns memory catalog ↔ context budget as the next slice.

## Goals / Non-Goals

**Goals:**
- `[context].memory` pack ids resolve against memory catalog (fail closed)
- `aisdlc budget` includes pack `ref` paths in the token estimate
- Notaire declares ≥1 real memory pack
- List/validate/show/budget never execute packs

**Non-Goals:**
- Isolator / required=true / portal / Context Lake
- Replacing `workspace/ci/agent-context-budget.py` (may coexist)
- Changing bytes/4 heuristic

## Decisions

1. **Field name** — `[context] memory = ["id", …]` (list of catalog memory ids). Parallel to stage `agents`/`skills`/`mcp` lists.
2. **Resolution** — reuse `catalogs.list_entries(ctx, "memory")`; unknown id → validate/doctor/budget error. Missing `[catalogs] dir` with non-empty memory → error (never silent pass).
3. **Budget measure** — union of `[context].files` and each pack's `ref` path; if `ref` is a file, count that file; if a directory, count regular files one level deep (non-hidden). Dedupe by resolved path. Configured when `max_tokens` is set and (`files` or `memory` non-empty).
4. **config show** — redacted view gains `context: { max_tokens, files, memory }` when any are set.
5. **Notaire** — catalog entry `agent-card` → `CONSTITUTION-AGENT-CARD.md`; `[context]` declares `memory = ["agent-card"]` plus the existing always-loaded files so `aisdlc budget` mirrors #1259.

## Riesgos / Trade-offs

- [Drift vs agent-context-budget.py] → Notaire `[context].files` mirrors ALWAYS_LOADED; document both; do not delete the CI script in this slice.
- [Directory pack explosion] → only one-level files under a dir ref; no recursive walk.
- [Execute creep] → tests assert hostile summary/ref strings are never run.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| omit memory → validate ok | unit | test_memory_context_budget.py |
| unknown pack fails validate/doctor | unit | test_memory_context_budget.py |
| budget counts pack ref + files (dedupe) | unit | test_memory_context_budget.py |
| missing pack ref fails budget | unit | test_memory_context_budget.py |
| missing catalogs dir with memory fails | unit | test_memory_context_budget.py |
| list/show never execute | unit | test_memory_context_budget.py |
| config show includes context.memory | unit | test_memory_context_budget.py |

## Regression Strategy

- Existing `BudgetTest` in test_checks.py stays green
- Kit: `python3 -m unittest discover -s framework/ai-sdlc/tests`
- Notaire: `config validate`, `doctor`, `budget`, `catalog list --kind memory`
- HTTP/Bruno/Playwright: n/a

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

Adopters with empty memory catalogs need no change. Optional: declare `memory` packs when ready.

## Open Questions

None.
