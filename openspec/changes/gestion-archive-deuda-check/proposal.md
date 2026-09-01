# Verificar Deuda Antes de Archivar Gestión

| Field | Value |
|-------|-------|
| GitHub Issue | #169 |
| Use Case | CU16 – Archivar Gestión (#169) |
| Branch | `feat/169_gestion-archive-deuda-check` |
| Gate 1 Status | Ready |

## Objetivo

CU16 (Archivar Gestión) debe verificar que la gestión no tenga deuda pendiente antes de permitir el archivo. Actualmente, el sistema permite archivar gestiones con saldo pendiente sin previo aviso.

**Regla de Negocio:** Cuando se intenta archivar una gestión, se debe verificar si hay presupuestos con saldo pendiente. Si los hay, mostrar advertencia al usuario con monto de deuda antes de confirmar el archivo.

## What Changes

- Backend: `GestionService.archivar()` ahora calcula deuda total antes de permitir archivo
- Backend: Si deuda > 0, lanza excepción con monto pendiente
- Frontend: `gestiones/page.tsx` captura excepción y muestra advertencia con opción de confirmar/cancelar
- Backend: Tests verifican que no se permite archivo con deuda

## Capabilities

### New Capabilities
- `gestion-archive-deuda-check`: Verificar deuda antes de archivar gestión
