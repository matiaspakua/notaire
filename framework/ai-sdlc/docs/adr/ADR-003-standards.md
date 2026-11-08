# ADR-003: What the external specifications change, and what they do not

**Status:** Accepted as design constraints. No new SDK is added in this slice.

## OpenFeature

The useful shape is a provider, an evaluation context, hooks, and a result that carries a default plus a reason. Evaluation of an optional flag must not throw.

That shape is not implemented. Ordinary settings (paths, timeouts, the pull-request cap) are config. Approval, merge rights, security gates, and allowlists are policy. They are validated config and they fail closed. They are not feature flags. A flag's default may be false. "Fail open" is the wrong lesson.

OFREP and the numeric error-code list were not verified against a pinned SDK. They are not implemented.

## OpenAPI

This CLI has no HTTP server. It does not need an OpenAPI document to be a CLI.

The product HTTP contract stays in the adopter's OpenAPI file. A future factory HTTP API, if one is justified, would be a separate description. This slice does not pick OAS 3.2. The installed toolchain is not being asked to parse 3.2.

`config show --json` is a CLI payload, not an OpenAPI operation.

## OpenTelemetry

No OpenTelemetry SDK is imported. No exporter is configured. JSON config output is not a span.

A later telemetry port would default to a no-op. Spans, metrics, and logs would be opt-in. Prompts, diffs, and secrets would stay off those signals. High-cardinality names would not be metric attributes. Whether a child process reads `TRACEPARENT`, and whether `OTEL_SDK_DISABLED` is honored, would be tested against the SDK that gets added. This slice does not claim either behavior.

GenAI semantic conventions are a draft in a separate repository. Nothing here says the kit conforms to them.

## Method tools

| Tool | What was checked here | Status |
|------|----------------------|--------|
| OpenSpec | `openspec --version` prints 1.14.0. `.cursor/install.sh` pins `@fission-ai/openspec@1.14.0`. The adapter argv is `openspec validate {change} --strict`, which this repository already runs. | Working as a printed command. Not executed by `plugin list`. |
| GitHub Spec Kit | Not installed. Upstream documents an extensions API. This slice does not call it and does not claim the API is absent. | Planned. |
| AWS AI-DLC | Not installed. It is an orchestrator of its own. Loading it beside this kit would run two lifecycles. | Planned. Not a harness adapter. |
