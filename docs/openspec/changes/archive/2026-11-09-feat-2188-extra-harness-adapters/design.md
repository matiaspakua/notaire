> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134 closed. #2182 / #2184 / #2186 landed. Gap audit ASLI-48 owns Extra
HarnessPort adapters (Claude Code / Codex / ACP) as inventory + dry plan stubs.

## Goals / Non-Goals

**Goals:**
- Three harness-kind plugin stubs in the kit with adapter YAML
- `harness list` / `show` surface them with `sandbox=false` always
- Notaire leaves them out of `[plugins] allow` (opt-in)
- List / validate / dry plan never execute remote agents

**Non-Goals:**
- Isolator / required=true / portal / Context Lake
- Real Claude Code / Codex / ACP session launch
- Claiming OS isolation from YAML

## Decisions

1. **Plugin kind** — `kind = "harness"` (already in `PLUGIN_KINDS`). Spec Kit allow
   pattern: `trusted = false`, id absent from `[plugins] allow`.
2. **Ids** — `claude-code`, `openai-codex`, `agent-client-protocol` (ACP).
3. **Adapter YAML** — each plugin ships `adapter.yml` with `runtime:` matching the
   plugin manifest; discoverability path stays under `framework/ai-sdlc/plugins/<id>/`.
4. **Harness list merge** — `list_adapters` returns profile `[[harness.adapters]]`
   first, then harness-kind plugins not already listed by id. Every row sets
   `sandbox=false` and `commercial_harness_product=false`. Plugin stubs add
   `stub=true` and `available` (trusted ∧ allow).
5. **Show** — resolve id from harness.adapters **or** harness-kind plugins; read
   adapter source in place; runtime mismatch fails closed.
6. **Dry plan** — `command = ["aisdlc-harness-describe", "{id}"]` (literal id in
   argv). Prints only when allowed; never spawns a runtime.
7. **Notaire** — declare three `[[plugins.entries]]` with `trusted = false`; do
   **not** add to allow or to `[[harness.adapters]]` (opt-in later). Existing
   cursor-cloud / local-foreman unchanged.
8. **Inventory** — migrated harness-adapter rows for the three plugin dirs; keep
   ≥1 planned row elsewhere so inventory stays honest.

## Riesgos / Trade-offs

- [List noise] → stubs are explicit inventory; `available=false` until Owner opts in.
- [Execute creep] → tests assert plan/list never spawn; command is describe-only argv.
- [Sandbox confusion] → docs + tests assert `sandbox=false` forever in this slice.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| stubs appear on harness list with sandbox=false | unit | test_extra_harness_stubs.py |
| unavailable until allow | unit | test_extra_harness_stubs.py |
| dry plan refused out of allow; prints describe when allowed | unit | test_extra_harness_stubs.py |
| show reads stub adapter.yml | unit | test_extra_harness_stubs.py |
| Notaire list includes stubs + existing adapters | adopter | test_harness_adoption.py |
| config validate / doctor green with stubs out of allow | adopter | test_harness_adoption.py |

## Regression Strategy

- Existing `test_harness.py` / Spec Kit / AWS AI-DLC suites stay green
- Kit: `python3 -m unittest discover -s framework/ai-sdlc/tests`
- Notaire: `harness list`, `plugin list`, `config validate`, `doctor`

## Playwright Strategy

n/a — no UI surface

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

Adopters without the three plugin entries need no change. Optional: declare + allow
when ready to dry-plan.

## Open Questions

None.
