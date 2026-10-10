# Point topology docs at live Owner tracker #1443 after #1197 auto-close

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1443 |
| Use Case | CU76 |
| Branch | `cursor/docs-1197-owner-tracker-reopen-cf98` |
| Gate 1 status | passed |
| Related | #1197 (auto-closed by #1442), #1438, #1226 |

## Objetivo

Squash-merge of #1442 auto-closed parent #1197 because a commit contained `Closes packaging gap for #1197`.
Agents cannot reopen. Live Owner work must track on **#1443**.

## What Changes

- ADR-024 Deciders / note → #1443 as live tracker; keep historical #1197 citations.
- REPO-SPLIT-PLAN issue map umbrella → #1443.
- Unit guard `test_adr024_owner_tracker.py`.
- CHANGELOG Unreleased.
- `skip_specs: true` (docs hygiene only).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Live Owner topology tracker must stay open until decisions are recorded | #1443, CU76 | Made explicit |

## Capabilities

### New Capabilities
- (none — `skip_specs: true`)

### Modified Capabilities
- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `docs` | yes | ADR-024, REPO-SPLIT-PLAN, OpenSpec |
| `workspace` | yes | unit guard |

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| ADR-024 | Deciders + auto-close note → #1443 |
| REPO-SPLIT-PLAN.md | Issue map umbrella → #1443 |
| CHANGELOG.md | Unreleased |

## Out of Scope

- Recording Owner A/B/C or ADR-024 choice
- Reopening #1197 (API 403)
- Closing #1443 from this PR
