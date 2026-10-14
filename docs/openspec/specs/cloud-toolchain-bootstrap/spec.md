# cloud-toolchain-bootstrap Specification

## Purpose
TBD - created by archiving change feat-1659-cloud-toolchain-bootstrap. Update Purpose after archive.

## Requirements

### Requirement: Cloud sessions bootstrap the Constitution toolchain

A `SessionStart` hook MUST install, only when `CLAUDE_CODE_REMOTE=true`, the tools the gates need that are missing (Temurin 26, `openspec`, `kustomize`, Trivy) into a user-writable cache and export the environment through `CLAUDE_ENV_FILE`. It MUST be idempotent, MUST exit 0 even when a download fails, and its tool versions MUST equal the pins in `.cursor/install.sh`, `security/tools/ensure_local_tools.sh` and `pom.xml`.

#### Scenario: hook is registered for session start

- **WHEN** `.claude/settings.json` is read
- **THEN** a `SessionStart` command runs `session-start-toolchain.sh`

#### Scenario: pins match their owners

- **WHEN** the hook's JDK, openspec and Trivy versions are compared with `pom.xml`, `.cursor/install.sh` and `security/tools/ensure_local_tools.sh`
- **THEN** they are equal

#### Scenario: outside cloud sessions the hook does nothing

- **WHEN** the hook runs without `CLAUDE_CODE_REMOTE=true`
- **THEN** it exits 0 and writes nothing to `CLAUDE_ENV_FILE`

#### Scenario: an install failure does not block the session

- **WHEN** the hook runs in a cloud session and a download fails
- **THEN** it exits 0 and prints one warning naming the tool

#### Scenario: environment is exported

- **WHEN** the hook runs in a cloud session with a `CLAUDE_ENV_FILE`
- **THEN** the file sets `LC_ALL`, `OPENSPEC_TELEMETRY` and a `PATH` that starts with the cache bin directory
