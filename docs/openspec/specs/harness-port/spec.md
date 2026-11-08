# harness-port Specification

## Purpose
Profile-declared harness adapters (Cursor Cloud fleet, local oMLX foreman) readable through `aisdlc harness` without moving sources or claiming a sandbox.

## Requirements

### Requirement: Harness list separates Cursor Cloud from the local foreman

`aisdlc harness list` MUST report each profile adapter with its runtime, source path, readable flag, and MUST set sandbox and commercial_harness_product to false.

#### Scenario: two adapters on Notaire

- **WHEN** harness list runs with the Notaire profile
- **THEN** cursor-cloud and local-foreman appear with distinct runtimes and sandbox=false

### Requirement: Harness show reads sources in place

`aisdlc harness show --id` MUST read the declared source without moving it. A missing source or a runtime mismatch MUST exit 1. An unknown id MUST exit 2.

#### Scenario: fleet roles

- **WHEN** show runs for cursor-cloud
- **THEN** foreman_id and role ids come from fleet-manifest.yaml and the file remains at its path

#### Scenario: local foreman

- **WHEN** show runs for local-foreman
- **THEN** agent and profile come from .aisdlc/project.yml and runtime is local-omlx
