# ADR-003: What the external specifications change, and what they do not

**Status:** Accepted. TelemetryPort and FeatureFlagPort stubs land in #2125; the kit remains stdlib-only at runtime.

## OpenFeature

The useful shape is a provider, an evaluation context, hooks, and a result that carries a default plus a reason. Evaluation of an optional flag must not throw.

That shape is implemented in `aisdlc/flags.py` (#2137): `JsonFileProvider` (flat JSON or flagd-shaped variants) plus `InMemoryProvider`, hooks, and `evaluate`. Ordinary settings stay config. Approval, merge rights, security gates, and allowlists stay policy and fail closed. Keys under `core.*` / `security.*` cannot override invariants. Required gates cannot be skipped via `gate.<name>.skip`. `factory.max_open_prs` may lower the profile ceiling, never raise it. Flags default off (`[flags] enabled = false`). A flag's default may be false. "Fail open" is the wrong lesson.

OFREP and the numeric error-code list are not verified against a pinned OpenFeature SDK. This is not a claim of full SDK conformance — it is the smallest real in-process provider path.

## OpenAPI

This CLI has no HTTP server. The factory still publishes an **OpenAPI 3.1.0** control
description at `openapi/factory-control.openapi.yaml` (#2136) so CLI JSON payloads and any
future HTTP adapter share one contract. OAS 3.2 is not used. The adopter application API
description stays outside the kit and is never imported.

`aisdlc contract check` validates structure; `bin/check-contract-breaking.sh` runs oasdiff when
installed, with accepts under `openapi/accepted-breaking-changes.d/`. `config show --json` keys
are covered by the `FactoryConfig` schema.

## OpenTelemetry

`aisdlc/telemetry.py` is the TelemetryPort. Default: `[telemetry] enabled = false` (stdlib no-op). When enabled with `exporter = "none"`, the port binds the OpenTelemetry API's `NoOpTracerProvider` via `importlib` if the optional packages are present — a real no-op SDK path, not a hand-rolled fake. No OTLP exporter is configured in this slice. JSON config output is not a span. Prompts, diffs, and secrets are redacted from attributes. High-cardinality free text is not used as metric attributes. Stage duration and cost USD emit as cheap lead-time/cost hooks into the port; the evidence ledger remains the source of truth.

Proven against the optional SDK (`framework/ai-sdlc/optional/telemetry.txt`): `OTEL_SDK_DISABLED=true` yields non-recording spans; `TRACEPARENT` is not injected on the no-op / disabled paths. GenAI semantic conventions are a draft in a separate repository. Nothing here claims conformance to them.

## Method tools

| Tool | What was checked here | Status |
|------|----------------------|--------|
| OpenSpec | `openspec --version` prints 1.14.0. `.cursor/install.sh` pins `@fission-ai/openspec@1.14.0`. The adapter argv is `openspec validate {change} --strict`, which this repository already runs. | Working as a printed command. Not executed by `plugin list`. |
| GitHub Spec Kit | Pinned `specify-cli==1.3.2`. Adapter under `plugins/spec-kit/`. **Called:** CLI `specify extension search` and Python `ExtensionCatalog.search` (see `plugins/spec-kit/CALLED-API.md`). Dry plan prints argv; Notaire keeps the id out of `[plugins] allow` until an Owner opts in. | Adapter present; not on allow. |
| AWS AI-DLC | Boundary design in `plugins/aws-ai-dlc/DESIGN.md` (#2124): owns **no** aisdlc stages as orchestrator; `auto_trust_hooks = false` enforced by `config validate`. Not installed; not available on Notaire. | Boundary declared; unavailable. |
