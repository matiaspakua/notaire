# refactor: move workflow transport mapping off the entities

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2329 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2329-workflow-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`WorkflowDefinition`, `WorkflowNode`, and `WorkflowTransition` built their transport DTOs. The trace service called the node and transition methods. This slice moves all three off the entities.

## What Changes

- Add `WorkflowLegacyMapper` with one `toDto` for each entity.
- The trace service calls the mapper.
- Delete the three entity methods.
- Shrink the ratchet by those three lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A definition DTO carries id, name, description, active, and version | existing `toDto` | Made explicit |
| A node DTO leaves the definition, status, and type empty when the link is missing | existing `toDto` | Made explicit |
| A transition DTO leaves the endpoints empty when the link is missing | existing `toDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, trace service, three entities |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients. Controllers already map these types themselves.

### Write set

- `WorkflowLegacyMapper.java`
- `WorkflowTraceService.java`
- `WorkflowDefinition.java`
- `WorkflowNode.java`
- `WorkflowTransition.java`
- `WorkflowLegacyMapperTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. Located with `rg` because the symbols were known. This is not a sampled locate pair.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |

## Out of Scope

- `DeedManagement#getDto`, `Procedure#getDto`, `Person#setAttributes`, and `User#setAttributes`. #2329 stays open.
