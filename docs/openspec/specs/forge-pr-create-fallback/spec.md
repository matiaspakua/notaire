# forge-pr-create-fallback Specification

## Purpose
TBD - created by archiving change improve-2231-forge-pr-create-fallback. Update Purpose after archive.

## Requirements

### Requirement: Document ambient gh createPullRequest 403 and preferred PR path

Permanent cloud fleet docs SHALL describe that ambient Cursor `gh` may lack
GraphQL `createPullRequest` (HTTP 403) even when branch push works. Docs SHALL
state the preferred agent path: push a `cursor/<desc>` tip (and optionally also
the constitutional `type/#_desc` branch), then open the PR via ManagePullRequest
or Cursor PR tools. Docs SHALL state the Owner compare-link fallback with exact
title and body (`Closes #<n>`) when forge tools fail. Docs MUST NOT claim fleet
App PR scopes were granted unless the Owner configured them; optional
`FLEET_APP_ID` / `FLEET_APP_PRIVATE_KEY` for `fleet-token.sh` MAY remain
documented for Issue labels (#2221).

#### Scenario: docs name createPullRequest friction

- **WHEN** an agent reads `FLEET-IDENTITY.md` or `FLEET-LESSONS-HEX-1693.md`
- **THEN** the text mentions `createPullRequest` and ambient `gh` / 403

#### Scenario: docs prefer ManagePullRequest on cursor tip

- **WHEN** ambient `gh pr create` fails with permission denied
- **THEN** docs instruct push of a `cursor/` tip and open via ManagePullRequest
  or Cursor PR tools

#### Scenario: docs give Owner compare-link fallback

- **WHEN** ManagePullRequest / Cursor PR tools also fail
- **THEN** docs instruct an Owner compare URL with exact PR title and body

#### Scenario: docs keep FLEET_APP optional without claiming PR scopes granted

- **WHEN** an agent reads fleet identity docs about secrets
- **THEN** `FLEET_APP_ID` / `FLEET_APP_PRIVATE_KEY` are optional for labels /
  `fleet-token.sh`, and docs do not claim PR-create scopes were already granted

### Requirement: Agent workflow points at the PR-create fallback path

The steward skill, ai-agent-workflow skill or rule, or cloud-foreman agent
definition SHALL include one short paragraph (or equivalent) pointing agents at
the durable cloud docs path for forge PR-create friction.

#### Scenario: steward or workflow points at cloud docs

- **WHEN** an agent follows steward, ai-agent-workflow, or cloud-foreman for PR open
- **THEN** the instructed path references `304-ai-sdlc-cloud` / FLEET-IDENTITY or
  FLEET-LESSONS for ManagePullRequest / compare fallback
