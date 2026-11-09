# harness-port Specification

## Purpose
Profile-declared harness adapters (Cursor Cloud fleet, local oMLX foreman) readable through `aisdlc harness` without moving sources or claiming a sandbox.

## Requirements

### Requirement: Harness list separates Cursor Cloud from the local foreman

`aisdlc harness list` MUST report each profile adapter with its runtime, source path,
readable flag, and MUST set sandbox and commercial_harness_product to false. When
harness-kind plugin stubs are declared, they MUST appear in the same list with the
same sandbox honesty.

#### Scenario: two adapters on Notaire

- **WHEN** harness list runs with the Notaire profile
- **THEN** cursor-cloud and local-foreman appear with distinct runtimes and sandbox=false

#### Scenario: Notaire surfaces stubs out of allow

- **WHEN** harness list runs with the Notaire profile after the stubs are declared
- **THEN** claude-code, openai-codex, and agent-client-protocol appear with sandbox=false
  and plugin list shows them available=false until Owner opts in

### Requirement: Harness show reads sources in place

`aisdlc harness show --id` MUST read the declared source without moving it. A missing source or a runtime mismatch MUST exit 1. An unknown id MUST exit 2.

#### Scenario: fleet roles

- **WHEN** show runs for cursor-cloud
- **THEN** foreman_id and role ids come from fleet-manifest.yaml and the file remains at its path

#### Scenario: local foreman

- **WHEN** show runs for local-foreman
- **THEN** agent and profile come from .aisdlc/project.yml and runtime is local-omlx

### Requirement: Extra harness stubs are discoverable without claiming a sandbox

The kit MUST ship thin harness-kind plugin stubs for Claude Code (`claude-code`),
OpenAI Codex (`openai-codex`), and Agent Client Protocol (`agent-client-protocol`).
When an adopter declares those plugins in the profile, `aisdlc harness list` MUST
include them alongside `[[harness.adapters]]`, MUST set `sandbox` and
`commercial_harness_product` to false, and MUST NOT execute remote agents.

#### Scenario: stubs on harness list with sandbox false

- **WHEN** the profile declares the three harness stubs and `harness list --json` runs
- **THEN** each stub id appears with `sandbox=false` and `commercial_harness_product=false`

#### Scenario: existing adapters remain distinct

- **WHEN** harness list runs on a profile that also declares cursor-cloud and local-foreman
- **THEN** those adapters still appear with their runtimes and are not replaced by the stubs

### Requirement: Harness show reads stub adapter sources in place

`aisdlc harness show --id` MUST resolve a harness-kind plugin stub by id and read
its adapter YAML without moving it. A missing source or runtime mismatch MUST exit 1.

#### Scenario: show claude-code stub

- **WHEN** show runs for `claude-code`
- **THEN** runtime is `claude-code`, sandbox is false, and `adapter.yml` remains at its path

### Requirement: Stub dry plan never executes a remote agent

`plugin plan` for a harness stub MUST print a describe-only argv when the plugin is
trusted and allowed, and MUST refuse (exit 2) while the id is out of `[plugins] allow`.
List, validate, and plan MUST never spawn Claude Code, Codex, or ACP.

#### Scenario: dry plan refused while out of allow

- **WHEN** plugin plan is called for `claude-code` while it is out of allow
- **THEN** the command exits 2 and no runtime process is started

#### Scenario: dry plan prints describe argv when allowed

- **WHEN** `claude-code` is trusted and allowed and plan runs
- **THEN** stdout is a single describe argv line and the runtime is not started
