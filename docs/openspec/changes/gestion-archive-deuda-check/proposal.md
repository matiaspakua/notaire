# Verificar Deuda Antes de Archivar Gestión

| Field | Value |
|-------|-------|
| GitHub Issue | #169 |
| Use Case | CU16 – Archivar Gestión (#169) |
| Branch | `feat/169_gestion-archive-deuda-check` |
| Gate 1 Status | Ready |

## Objetivo

CU16 (Archivar Gestión) debe advertir al usuario cuando la gestión tiene deuda pendiente antes de confirmar el archivo. Actualmente, el sistema permite archivar gestiones con saldo pendiente sin previo aviso.

**Regla de Negocio:** Cuando se intenta archivar una gestión, se debe verificar si hay presupuestos con saldo pendiente. Si los hay, mostrar advertencia al usuario con el monto de deuda antes de confirmar el archivo, pero el archivo se completa igualmente (no se bloquea).

## What Changes

- Backend: `GestionArchiveDebtService` calcula el saldo pendiente agregado de la gestión antes de archivar (expuesto vía `GET /saldo-pendiente`)
- Backend: El archivado **no se bloquea** por deuda; se persiste `deudaPendienteAlArchivar = true` cuando el saldo es positivo
- Frontend: `gestiones/page.tsx` consulta el saldo pendiente y muestra advertencia con opción de confirmar/cancelar antes de archivar
- Backend: Tests verifican que el archivo se completa con o sin deuda, registrando correctamente `deudaPendienteAlArchivar`

## Capabilities

### New Capabilities
- `gestion-archive-deuda-check`: Verificar deuda antes de archivar gestión
