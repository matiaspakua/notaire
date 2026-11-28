<!-- Governed by CONSTITUTION.md. CU76, issue #2512. -->

## Purpose

Notaire obtains the portable AI SDLC kit from its independent product repository
at a published release tag while keeping the stable `framework/ai-sdlc` path and
adopter profile under `.aisdlc/`.

## ADDED Requirements

### Requirement: AI SDLC path is a git submodule pinned to a release tag

Notaire MUST record `framework/ai-sdlc` as a git submodule of
`https://github.com/draclab/ai_sdlc.git` and MUST pin the recorded commit to the
published release tag `aisdlc-v1.1.0` (or a later Owner-approved release).
Notaire MUST NOT vendor a second copy of the kit tree and MUST NOT publish
`aisdlc-v*` release tags from the Notaire repository.

#### Scenario: gitmodules points at the kit product

- **WHEN** a developer inspects `.gitmodules`
- **THEN** an entry with `path = framework/ai-sdlc` names the `draclab/ai_sdlc` repository

#### Scenario: pin matches aisdlc-v1.1.0

- **WHEN** the submodule is initialized
- **THEN** `framework/ai-sdlc` resolves to the commit of tag `aisdlc-v1.1.0` and the kit reports version 1.1.0

#### Scenario: profile invocation path unchanged

- **WHEN** a developer runs `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml gate --list`
- **THEN** the command runs from the submodule path against the Notaire adopter profile
