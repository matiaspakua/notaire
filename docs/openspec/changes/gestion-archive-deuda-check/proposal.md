# Warn about pending debt before archiving a management

| Field | Value |
|-------|-------|
| GitHub Issue | #169 |
| Use Case | CU16 – Archivar Gestión (#169) |
| Branch | `feat/169_gestion-archive-deuda-check` |
| Gate 1 Status | Ready |

## Objetivo

CU16 (Archivar Gestión) warns the user when a management has pending debt before the archive is confirmed. The system currently archives managements with a pending balance without a warning.

**Regla de Negocio:** When a user tries to archive a management, the system checks whether its budgets have a pending balance. If they do, it shows the user the debt amount before confirming the archive, and the archive still completes (it is not blocked).

## What Changes

- Backend: `ManagementArchiveDebtService` calculates the aggregate pending balance of the management before archiving (exposed via `GET /outstanding-balance`)
- Backend: Archiving is **not blocked** by debt; it persists `pendingDebtAtArchiving = true` when the pending balance is positive
- Frontend: `managements/page.tsx` reads the pending balance and shows a warning with confirm or cancel before archiving
- Backend: Tests verify that the archive completes with or without debt, recording `pendingDebtAtArchiving` correctly

## Capabilities

### New Capabilities
- `gestion-archive-deuda-check`: Warn about pending debt before archiving a management
