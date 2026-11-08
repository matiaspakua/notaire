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
`openspec validate <change> --strict` and does not run it. Spec Kit and AWS AI-DLC are planned
files under `framework/ai-sdlc/plugins/planned/`. They are not on `plugin list`.

`docs/300-development/303-testing/INVENTORY.md` stays where generators and reviewers already
read it (#2117). The migration row marks it as a hotspot and does not change its path.

Product OpenAPI, product specs, and `frontend/src` are `class = stay`.

Follow-ups, not done here: [#2123](https://github.com/matiaspakua/notaire/issues/2123) Spec Kit,
[#2124](https://github.com/matiaspakua/notaire/issues/2124) AWS AI-DLC,
[#2125](https://github.com/matiaspakua/notaire/issues/2125) a real telemetry SDK.
[#2117](https://github.com/matiaspakua/notaire/issues/2117) stays the generated-inventory hotspot.

No OpenTelemetry SDK is imported. No feature-flag SDK is imported. No HTTP server was added.
A manifest path check is not a sandbox.
