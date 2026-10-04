# bruno-cli-cloud-install Specification

## Purpose

TBD - created by archiving change chore-bruno-cli-install-pin. Update Purpose after archive.

## Requirements

### Requirement: Cloud install pins Bruno CLI ≥4.2.0 on PATH

`.cursor/install.sh` SHALL install `@usebruno/cli@4.2.0` (npm global) after the
OpenSpec CLI block and SHALL ensure the `bru` command is available on PATH
(typically via `/usr/local/bin/bru`). The pin SHALL be at least 4.2.0 so
OpenCollection collections are supported (Bruno 2.x only understands `bruno.json`).

#### Scenario: install.sh pins @usebruno/cli@4.2.0

- **WHEN** a reviewer inspects `.cursor/install.sh` on this change
- **THEN** the script contains `npm install -g @usebruno/cli@4.2.0` after the
  OpenSpec install lines

#### Scenario: bru symlink block present after OpenSpec

- **WHEN** a reviewer inspects the Bruno block in `.cursor/install.sh`
- **THEN** the script attempts to link `$HOME/.local/bin/bru` or the package
  `bin/bru.js` to `/usr/local/bin/bru` and invokes `command -v bru`

### Requirement: Environment checklist documents Bruno CLI ≥4.2.0

`docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` SHALL list
Bruno CLI ≥4.2.0 in the base toolchain table with a `bru --version` verify
command, and SHALL include `bru` in the PATH / CLI expectations list.

#### Scenario: checklist lists Bruno CLI ≥4.2.0

- **WHEN** a Cloud Agent or human reads the ENVIRONMENT-CHECKLIST toolchain table
- **THEN** Bruno CLI ≥4.2.0 is documented with verify `bru --version`
