# Emitir recibo de pago en PDF

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #23 |
| Use Case | CU15 – Procesar pago / RF-21 |
| Branch | `feat/23_recibo-de-pago-pdf` |
| Gate 1 status | passed |

## Objetivo

El paso final de CU15 (entregar comprobante de pago al cliente) no tiene
ninguna forma de emitirse: `POST /api/v1/pagos` persiste el pago pero no existe
ningún endpoint que genere un recibo imprimible, pese a que RF-21 lo exige
explícitamente y el resto del circuito de cobranza ya está resuelto.

## What Changes

- Nuevo endpoint `GET /api/v1/reportes/recibo-pago/{idPago}` que genera un PDF
  de recibo para un pago ya persistido.
- El recibo detalla: cliente que abona (persona del presupuesto), fecha de
  pago, concepto(s) abonado(s) (ítems del presupuesto asociado) y total
  abonado (monto del pago).
- Reutiliza el patrón ya existente de `ReporteService.generarPdfTextoSimple` /
  `buildPdf` (generación de PDF crudo sin plantilla `.jasper` compilada), igual
  que `generarReporteCopiaTestimonio` y `generarReporteMinutaInscripcion`, en
  lugar de introducir una plantilla Jasper nueva.
- Frontend: botón "Emitir recibo" en la pantalla de cobranza que abre el PDF
  del endpoint nuevo (UI traceability obligatoria).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Todo pago registrado debe poder emitir un recibo con cliente, fecha, concepto(s) y total abonado | RF-21, CU15 paso final | Made explicit (ya normado, nunca implementado) |
| El recibo se genera a partir de un pago ya existente; no se puede emitir un recibo para un `idPago` inexistente (404) | CU15 | New (regla de guardia, análoga a `generarReporteCopiaTestimonio`) |

## Capabilities

### New Capabilities
- `pago-recibo`: generación de un comprobante/recibo en PDF para un pago ya registrado, detallando cliente, fecha, concepto(s) y total.

### Modified Capabilities
_Ninguna — no se altera el contrato de `POST /api/v1/pagos` ni de ningún endpoint existente._

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Nuevo método en `ReporteService` + nuevo endpoint en `ReporteController` |
| `frontend` | yes | Botón "Emitir recibo" en la pantalla de cobranza/pagos que invoca el nuevo endpoint |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: ninguna nueva; se leen `Pago`, `Presupuesto`, `Persona`, `Item` (ya existentes).
- Endpoints: `GET /api/v1/reportes/recibo-pago/{idPago}` (nuevo).
- Database (Flyway `V{n}`): ninguna — no requiere cambio de esquema.
- Configuration / `.env`: ninguna.
- Dependencies: ninguna nueva (reutiliza JasperReports ya presente y el helper de PDF crudo existente).

### Architecture review

Sigue el patrón arquitectónico existente: controlador REST delgado
(`ReporteController`) delegando a un servicio (`ReporteService`), sin acceso
directo a base de datos desde el controlador, reutilizando el helper de
generación de PDF ya usado por `generarReporteCopiaTestimonio` y
`generarReporteMinutaInscripcion`. No es un cambio arquitectónico — no requiere
ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | Marcar el paso de emisión de comprobante como implementado, referenciando el nuevo endpoint |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | Agregar la fila del nuevo endpoint `GET /api/v1/reportes/recibo-pago/{idPago}` mapeado a CU15 |
| `backend-api/api-test/COVERAGE.md` | Agregar cobertura Bruno del nuevo endpoint bajo el recurso `reportes` |
| `CHANGELOG.md` | Entrada en `[Unreleased]`: "Emitir recibo de pago en PDF (CU15/RF-21)" |

## Out of Scope

- Impresión física o envío por email del recibo — solo generación de PDF vía
  endpoint, igual que el resto de los reportes existentes.
- Plantilla Jasper visual (`.jrxml`/`.jasper`) — se usa el patrón de PDF crudo
  ya establecido para reportes simples; una plantilla Jasper con diseño
  gráfico queda fuera de alcance y puede tratarse en un hallazgo futuro si se
  requiere.
