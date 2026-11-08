# spec-kit-method-plugin Specification

## Purpose
Pinned Spec Kit method adapter: dry-plan specify extension search; call record in plugins/spec-kit/CALLED-API.md; out of allow on Notaire until Owner opt-in.

## Requirements

### Requirement: Spec Kit pin and extensions API are recorded

The Spec Kit adapter MUST pin specify-cli 1.3.2 and MUST record that `specify extension search` (CLI) and ExtensionCatalog.search (Python) were called. It MUST NOT claim the API is absent.

#### Scenario: call record

- **WHEN** a reviewer opens plugins/spec-kit/CALLED-API.md
- **THEN** the pin and both API call forms are present

### Requirement: Spec Kit stays out of allow until opted in

On the Notaire profile, github-spec-kit MUST be declared and MUST remain out of [plugins] allow so plugin plan exits 2. config validate MUST still pass.

#### Scenario: Notaire profile

- **WHEN** plugin list and plugin plan run on Notaire
- **THEN** github-spec-kit is unavailable and plan exits 2 while config validate exits 0

### Requirement: Dry plan prints extension search when allowed

When github-spec-kit is trusted and allowlisted, plugin plan MUST print `specify extension search <query>` without executing specify.

#### Scenario: allowlisted dry plan

- **WHEN** plan runs with query=git on an allowlisted profile
- **THEN** stdout is exactly that argv line
