# Factory foundation (#2121) and harness port (#2122)

The canonical root is [`framework/ai-sdlc`](../../../framework/ai-sdlc/README.md).
Decisions: [ADR-001](../../../framework/ai-sdlc/docs/adr/ADR-001-canonical-root.md),
[ADR-002](../../../framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md),
[ADR-003](../../../framework/ai-sdlc/docs/adr/ADR-003-standards.md).

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

TelemetryPort (`aisdlc/telemetry.py`, #2125): off by default; optional OpenTelemetry NoOp path when enabled with `exporter = "none"`; prompts/diffs/secrets redacted; lead-time/cost hooks on stage and cost records. FeatureFlagPort stub (`aisdlc/flags.py`) mirrors OpenFeature shape without a hard SDK dependency. No HTTP server was added. A manifest path check is not a sandbox.
