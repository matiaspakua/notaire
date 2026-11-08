# frontend-boundary-lint Specification

## Purpose

TBD - created by archiving change test-1696-frontend-boundary-lint. Update Purpose after archive.

## Requirements

### Requirement: UI code cannot reach the API client or fetch

Files under app, components and hooks MUST NOT import lib/api-client or call fetch, except the violations recorded in the baseline.

#### Scenario: new violation fails

- **WHEN** a hook, page or component imports lib/api-client in a file or count not covered by the baseline
- **THEN** npm run lint fails

#### Scenario: adapters may import the client

- **WHEN** a file under adapters/http imports lib/api-client
- **THEN** no rule fires

### Requirement: The core is pure

Files under core MUST NOT import React, Next, TanStack, lib, adapters or UI code, and MUST NOT use fetch or browser globals.

#### Scenario: core stays pure

- **WHEN** a core file imports react or uses window
- **THEN** the rule fires

### Requirement: The baseline only shrinks

The baseline MUST list only files that still exist and only the boundary rules, and a fixed violation MUST be removable with the prune command.

#### Scenario: baseline is well formed

- **WHEN** the baseline test runs
- **THEN** it passes only if every entry is an existing file and a boundary rule
