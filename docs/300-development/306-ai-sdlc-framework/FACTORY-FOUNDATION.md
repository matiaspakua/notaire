# Factory foundation (#2121) and harness port (#2122)

**Multi-session plan:** continue from [`FACTORY-ROADMAP.md`](FACTORY-ROADMAP.md)
(mirror of the store master plan; docs [#2140](https://github.com/matiaspakua/notaire/issues/2140)).
Epic [#2134](https://github.com/matiaspakua/notaire/issues/2134) **P0–P3 spine done**
(#2125–#2142). Validation fixes [#2153](https://github.com/matiaspakua/notaire/issues/2153) /
[#2155](https://github.com/matiaspakua/notaire/issues/2155) /
[#2157](https://github.com/matiaspakua/notaire/issues/2157); preflight evidence
[#2159](https://github.com/matiaspakua/notaire/issues/2159) (`aisdlc gate signal`).
ASLI-31 / [#2161](https://github.com/matiaspakua/notaire/issues/2161) closes the epic
docs; next work is Deferred only (Wilson / ForgePort write) —
no portal. Isolator sole-committer (#2200) and ApprovalPort runtime (#2205) landed.

**Sole-committer dry-run** ([#2167](https://github.com/matiaspakua/notaire/issues/2167) /
[#2169](https://github.com/matiaspakua/notaire/issues/2169) /
[#2175](https://github.com/matiaspakua/notaire/issues/2175)): `aisdlc committer` is
**axiom-2 policy evidence** (who authored commits since base). It is **not** an OS
Isolator and never claims sandbox isolation. Notaire’s Owner `runtime_names` /
`runtime_emails` are set (#2175), so the check is **evaluable**. The pre-pr gate stays
**advisory** (`required = false`); Owner may flip `required = true` in a follow-up for
fail-closed.

**Isolator OS + Grant expire** ([#2173](https://github.com/matiaspakua/notaire/issues/2173) /
[#2198](https://github.com/matiaspakua/notaire/issues/2198),
[ADR-004](../../../framework/ai-sdlc/docs/adr/ADR-004-isolator-runtime.md)):
`aisdlc isolator status|probe|run` prefers Linux `bwrap`; `sandbox=true` only after
a successful probe; missing `bwrap` → honest `sandbox=false` / `no-evaluable`.
Grant optional `expires_at` / `ttl_seconds` fail closed. YAML ≠ OS isolation.

**Isolator sole-committer enforcement** ([#2200](https://github.com/matiaspakua/notaire/issues/2200)):
`aisdlc isolator wrap` + git `core.hooksPath` custody blocks `git commit` unless
Owner/runtime holds `AISDLC_COMMIT_GRANT` (agent never receives it). Works
**without bwrap**; `enforces_committer=true` only after a proven interception probe.
`sandbox` stays independent. Do not invent Owner `runtime_*`;
`[committer] required` stays `false`.

**Grant profile schema** ([#2192](https://github.com/matiaspakua/notaire/issues/2192)) +
**write-set policy dry-run** ([#2194](https://github.com/matiaspakua/notaire/issues/2194)) +
**expire** ([#2198](https://github.com/matiaspakua/notaire/issues/2198)):
optional `[[grants]]` (worktree, write_set, required_checks, budget, expire) —
validate/list/show plus `aisdlc grant check` (pass / fail / no-evaluable; expire
fail-closed). Expire / write-set policy alone is **not** Isolator OS complete.
Unknown check refs fail closed; omit = no-op. Notaire ships optional `plan-sample`
Grant with expire documented as comments only (CI often has no bwrap).

**Foreman stage-knob routing** ([#2196](https://github.com/matiaspakua/notaire/issues/2196)):
`aisdlc foreman plan|route [--stage] [--json]` resolves a read-only dispatch plan
from `[[stages]]` knobs (#2182). Cloud Foreman and the kit foreman template must
consult it before dispatch. Never executes agents/MCP; never Isolator/sandbox.
Foreman ops habit is **partially kit-backed** (routing surface only).

**ApprovalPort custody runtime** ([#2205](https://github.com/matiaspakua/notaire/issues/2205),
design [#2190](https://github.com/matiaspakua/notaire/issues/2190),
[ADR-005](../../../framework/ai-sdlc/docs/adr/ADR-005-approval-port.md)):
`aisdlc approval mint|verify|status` via OpenSSH `ssh-keygen -Y`. Token + channel
authority; agent never self-approves (`agent_can_approve=false`; mint refuses
agent). `approvals_live=true` **only** after a proven mint→verify probe; without
local/dev keys (or with `AISDLC_APPROVAL=0`) doctor stays `no-evaluable`. Notaire
opts in via env / `[approval]` paths — **not** a production HSM claim. No fake
approvals. Wilson L1+ auto-promotion remains **refused** until Owner-chosen
multi-stage evidence volume exists (autonomy stays `no-evaluable`).

The canonical root is [`framework/ai-sdlc`](../../../framework/ai-sdlc/README.md).
Decisions: [ADR-001](../../../framework/ai-sdlc/docs/adr/ADR-001-canonical-root.md),
[ADR-002](../../../framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md),
[ADR-003](../../../framework/ai-sdlc/docs/adr/ADR-003-standards.md),
[ADR-004](../../../framework/ai-sdlc/docs/adr/ADR-004-isolator-runtime.md),
[ADR-005](../../../framework/ai-sdlc/docs/adr/ADR-005-approval-port.md).

This slice does not move `workspace/sdlc`, `local-ai/sdlc`, agent prompts, or skills.
The map of what moved and what did not is `.aisdlc/migration-inventory.toml`.
`status = planned` means the files are still at `source` without a port reading them yet.

```bash
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml config validate
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml config show --redacted
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml plugin list
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml plugin plan --id openspec --var change=demo
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml inventory check
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml harness list
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml harness show --id cursor-cloud --json
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml harness show --id local-foreman --json
```

## Harness port (#2122)

`[[harness.adapters]]` in `.aisdlc/aisdlc.toml` names two runtimes:

| id | runtime | source (stays in place) |
|----|---------|-------------------------|
| `cursor-cloud` | `cursor-cloud` | `docs/300-development/304-ai-sdlc-cloud/fleet-manifest.yaml` |
| `local-foreman` | `local-omlx` | `.aisdlc/project.yml` |

They are different adapters. Neither is a commercial Harness product.
`sandbox` is always `false` in the CLI output: a permission listed in a source is not isolation.

Extra HarnessPort stubs (#2188) — Claude Code (`claude-code`), OpenAI Codex
(`openai-codex`), and Agent Client Protocol (`agent-client-protocol`) — ship as
`kind = "harness"` plugins under `framework/ai-sdlc/plugins/`. `harness list`
surfaces them with `stub=true`, `sandbox=false`, and `available=false` while they
stay out of `[plugins] allow` (Spec Kit opt-in pattern). Dry `plugin plan` prints
`aisdlc-harness-describe <id>` only when allowed; list/validate/plan never launch
a runtime. Notaire keeps them opted out until the Owner reviews the dry plan.

OpenSpec in this environment is CLI 1.14.0, pinned in `.cursor/install.sh`. The adapter prints
`openspec validate <change> --strict` and does not run it.

Spec Kit (`github-spec-kit`, #2123) is an adapter under `framework/ai-sdlc/plugins/spec-kit/`.
Pinned `specify-cli==1.3.2`. The extensions API that was called is recorded in
`framework/ai-sdlc/plugins/spec-kit/CALLED-API.md` (`specify extension search` /
`ExtensionCatalog.search`). Dry plan argv:

```bash
python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml plugin plan --id github-spec-kit --var query=git
```

That plan exits 2 on Notaire until the id is added to `[plugins] allow`.

AWS AI-DLC (`aws-ai-dlc`, #2124) is a **boundary** plugin under
`framework/ai-sdlc/plugins/aws-ai-dlc/`. `DESIGN.md` states it owns **no** aisdlc
stages as orchestrator (Construction/Operation forbidden). `auto_trust_hooks = false`
is required; `config validate` rejects `auto_trust_hooks = true`. The plugin is
declared with `trusted = false` and is not available on `plugin list`.

`docs/300-development/303-testing/INVENTORY.md` stays where generators and reviewers already
read it (#2117). The migration row marks it as a hotspot and does not change its path.

Product OpenAPI, product specs, and `frontend/src` are `class = stay`.

Follow-ups, not done here:
Owner opt-in: add `github-spec-kit` to `[plugins] allow` after reviewing the dry plan.
AWS AI-DLC stays unavailable until a later Issue adds a non-orchestrating dry plan.
[#2117](https://github.com/matiaspakua/notaire/issues/2117) stays the generated-inventory hotspot.
A hosted OTLP collector / product application telemetry stay out of scope.

Lifecycle (#2135): `aisdlc install|update|uninstall` copies the kit, scaffolds a profile, updates
idempotently, and uninstalls kit-managed paths only (`--purge` for scaffold/evidence). Never writes
`frontend/`, `backend-api/`, or product OpenAPI paths.

TelemetryPort (`aisdlc/telemetry.py`, #2125): off by default; optional OpenTelemetry NoOp path when enabled with `exporter = "none"`; prompts/diffs/secrets redacted; lead-time/cost hooks on stage and cost records. FeatureFlagPort (`aisdlc/flags.py`, #2137) loads JSON/flagd flags in-process; required gates and PR ceilings stay fail-closed. Factory control OpenAPI 3.1.0 lives under `framework/ai-sdlc/openapi/` (#2136); no HTTP server was added. A manifest path check is not a sandbox.

Catalogs CRUD (#2139): `aisdlc catalog` manages skills/MCP/agents/memory JSON entries under `[catalogs] dir` with digests; list/validate never execute. Memory packs may be referenced from `[context].memory` and participate in `aisdlc budget` (#2186); unknown packs fail closed.

Telemetry UI (#2142): `aisdlc report --html PATH` writes a static read-only HTML monitor from the evidence ledger (gates, stages, cost); optional `--lead-time-fixture` embeds the #2138 event-pair sample. No HTTP server, no policy write path, no prompts/diffs/secrets in the page.

Self-improve (#2141): `aisdlc improve propose` turns ledger friction into a proposal artifact (gated-PR draft only). `validate` rejects agent self-approval and requires Owner merge for protected candidates. `autonomy` reports Wilson lower bound; first incident demotes. The kit never commits and never merges.
