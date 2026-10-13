# Management status history collection is fetched lazily (slice of #595)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #595 |
| Use Case | CU13 – Gestionar historial de gestion |
| Branch | `fix/595_management_status_history_lazy` |
| Gate 1 status | draft |

## Objetivo

`ManagementStatus.historyList` was an EAGER `@OneToMany` with no bound: loading any status (directly, or through every history row, management and procedure that references one) pulled every history row of that status, each with its own eager associations. The collection grows with every state change. Make it lazy so it is only read when asked for. The other 28 explicit EAGER mappings stay in #595.

## What Changes

- `ManagementStatus.historyList` changes from `FetchType.EAGER` to `FetchType.LAZY`.
- `ManagementStatusHistoryFetchIntegrationTest` pins that finding or listing statuses does not load the collection and that it is still readable inside a persistence context.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Loading a management status does not load its history | #595 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `jpa-fetch-strategy`: JPA associations that grow with usage are not fetched eagerly.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `ManagementStatus` |
| `frontend` | no | — |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints: none (the collection is `@JsonIgnore`)
- Entities: `ManagementStatus` (fetch strategy only); Flyway: none
- Configuration: none

### Architecture review

No architecture change. Every reader of the collection runs inside a persistence context: `HistoryController.delete` is `@Transactional`, and the legacy `ManagementStatusJpaController` / `HistoryJpaController` use their own `EntityManager`. `spring.jpa.open-in-view=false`, so a reader outside a transaction would fail loudly instead of silently loading.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
