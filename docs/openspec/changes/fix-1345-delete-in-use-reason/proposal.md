# Delete failures show the backend's in-use reason as a warning

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1345 |
| Use Case | RF-38 – Modificación de clientes; RF-53 – Administrar tablas base; CU76 |
| Branch | `fix/1345_delete_in_use_reason` |
| Gate 1 status | draft |

## Objetivo

Delete handlers on several pages caught every failure with `catch { toast.error(t("errorDelete")) }`, and the others showed the raw server text as an error. When a record is still referenced, the backend answers 409 with a reason (`PersonController`: "Cannot delete: person is referenced by other records."), but the user only saw "Error al eliminar" and couldn't tell why or what to do.

## What Changes

- `lib/mutation-error.ts`: `presentDeleteError(err, { fallback, inUse, notFound })`. 409 becomes `toast.warning(common.errors.inUse)` with the server reason (JSON `message` or a short plain-text body) as the description. 404 shows `common.errors.notFound`. Other failures show the server message or the fallback, and 401 is left to the session-expiry handler.
- New `hooks/useDeleteError.ts` wires the translated copy; the 18 pages with delete handlers call `showDeleteError(err, t("errorDelete"))`, including the failure of the `/{id}/in-use` pre-checks.
- The in-use pre-checks (concepts, document types, procedure types, management states, folio types) warn instead of erroring.
- i18n `common.errors.inUse` and `common.errors.notFound` (es/en).
- Vitest `delete-error.test.ts` (mapping plus a static scan of `src/app`); Playwright `TS-0105`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A delete refused because the record is referenced is a warning that says why | #1345, RF-38, RF-53 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-error-feedback`: Mutation failures tell the user what happened and what to do.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | mutation-error, useDeleteError, 18 dashboard pages, messages |
| `testing` | yes | Playwright TS-0105 |
| `backend-api` | no | 409 bodies unchanged |

### Surface area

- Routes: every dashboard page with a delete action
- API: DELETE endpoints' 404/409 responses (unchanged contract)

### Architecture review

No architecture change: one presentation helper and a hook next to the existing `presentMutationError` (#1054).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
