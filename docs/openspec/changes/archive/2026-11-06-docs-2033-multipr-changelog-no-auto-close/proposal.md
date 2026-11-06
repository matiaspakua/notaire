# Prevent release-please from closing multi_pr epics via CHANGELOG

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2033 |
| Use Case | CU76 |
| Branch | `docs/2033_multipr-changelog-no-auto-close` |
| Gate 1 status | passed |

## Objetivo

Release Please closes open multi_pr epics when Unreleased CHANGELOG entries use `#N` / `closes #N` issue links.

## Why

0.2.6 / 0.2.7 release notes closed #579 while GEH slices were still in flight.

## What Changes

Document the rule in steward + agent workflow; add a docs/tests guard that `[Unreleased]` must not contain `closes #<n>`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Unreleased CHANGELOG must not use `closes #N` while work continues. | Issue #2033; CU76 | New |

## Capabilities

### New Capabilities

- `multipr-changelog-hygiene`: Unreleased must not auto-close issues via `closes #N`.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | — |
| `frontend` | yes | — |
| `frontend-swing` | no | — |
| `infra` / observability | yes | — |
| CI/CD (`.github/workflows`) | yes | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows the existing architecture; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `.claude/skills/steward/SKILL.md` | multi_pr CHANGELOG rule |
| `.claude/rules/ai-agent-workflow.md` | short pointer |
| `CHANGELOG.md` | Unreleased note for #2033 |

## Out of Scope

["Changing release-please-config schema", "Rewriting historical released CHANGELOG entries"]
