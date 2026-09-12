---
title: E2E Coverage Report - 2026-09-10
---

# E2E Coverage Report

**Date:** 2026-09-10
**Trigger:** schedule

## Playwright E2E Results

- **Total:** 523
- **Passed:** 422
- **Failed:** 85
- **Skipped:** 16
- **Flaky:** 0

## Bruno API Test Results

- **Total requests:** 150
- **Passed requests:** 123
- **Failed requests:** 27
- **Total tests/assertions:** 268
- **Passed tests:** 223
- **Failed tests:** 45

## Action Items

- `carpetas-de-tramite.spec.ts` — golden path: iniciar trámite genera carpeta activa, se pone en espera y se archiva con la gestión
- `carpetas-de-tramite.spec.ts` — edge path: poner en espera sin motivo muestra un error visible
- `carpetas-de-tramite.spec.ts` — edge path: archivar una gestión con carpeta en espera exige confirmación explícita
- `carpetas-de-tramite.spec.ts` — carpetas dialog is usable at 320px (mobile)
- `carpetas-de-tramite.spec.ts` — carpetas dialog is usable at 768px (tablet)
- `carpetas-de-tramite.spec.ts` — carpetas dialog is usable at 1024px (desktop)
- `folios-vinculacion.spec.ts` — CU87-GW01: golden path — crear folio vinculado a escritura firmada → folio Utilizado y escritura muestra el folio
- `folios-vinculacion.spec.ts` — CU87-EDGE03: crear folio sin vincular escritura sigue funcionando (campo opcional)
- `presupuesto-catalogo-items.spec.ts` — agrega un ítem del catálogo al presupuesto y lo muestra en el desglose
- `presupuesto-catalogo-items.spec.ts` — permite combinar un ítem de plantilla y uno de catálogo en el mismo presupuesto
- `presupuesto-plantilla.spec.ts` — CU39 - el diálogo de ítems es usable a 320px (mobile)
- `presupuesto-plantilla.spec.ts` — CU39 - el diálogo de ítems es usable a 768px (tablet)
- `presupuesto-plantilla.spec.ts` — CU39 - el diálogo de ítems es usable a 1024px (desktop)
- `presupuesto-plantilla.spec.ts` — carga los ítems de la plantilla del tipo de trámite en el presupuesto
- `presupuesto-plantilla.spec.ts` — muestra un error cuando el tipo de trámite no tiene plantilla configurada
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-VP: módulo de presupuestos es usable a 320px (mobile)
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-VP: módulo de presupuestos es usable a 768px (tablet)
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-VP: módulo de presupuestos es usable a 1024px (desktop)
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-GW01: crear persona via API y presupuesto via UI — aparece en la lista con nombre del cliente
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-GW02: lista de presupuestos muestra el nombre del cliente en cada fila
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-SD01 (#796): el resumen del presupuesto muestra el saldo pendiente
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-SD02 (#821): tras registrar un pago parcial vía API el resumen refleja el nuevo saldo
- `TS-0010-presupuesto-workflow.spec.ts` — TS-0010-MOD01: editar un presupuesto existente actualiza los datos en la lista
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU02-GW01: golden path — gestión seeded via API aparece en la tabla con número y estado inicial
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU02-GW03: picker de presupuesto muestra el nombre del cliente asociado (#889)
- `TS-0011-gestiones-crud-workflow.spec.ts` — golden path — gestión en tabla → bitácora 1 entrada → cambiar estado → bitácora 2 entradas
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU13-GW01: gestión recién creada muestra exactamente 1 entrada en bitácora con el estado inicial
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU13-GW02: tras cambiar estado la bitácora acumula una segunda entrada
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU83-GW01: transición a destino válido actualiza el estado visible en la tabla
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU83-GW02: el picker sólo ofrece destinos válidos del workflow (no el estado actual)
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU16-GW01 edge path: archivar gestión cuyo estado no tiene transición a "Archivada" muestra error
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU19-GW01: seleccionar cliente en el filtro dispara GET /gestiones/cliente/{id}
- `TS-0011-gestiones-crud-workflow.spec.ts` — CU19-GW02: gestión seeded con cliente específico aparece al filtrar por ese cliente
- `TS-0011-gestiones-crud-workflow.spec.ts` — gestiones table is visible and usable at 320px (mobile)
- `TS-0011-gestiones-crud-workflow.spec.ts` — gestiones table is visible and usable at 768px (tablet)
- `TS-0011-gestiones-crud-workflow.spec.ts` — gestiones table is visible and usable at 1024px (desktop)
- `TS-0014-pagos-workflow.spec.ts` — CU15-SALDO-02 (#796): Selecting a presupuesto shows its client name and saldo pendiente
- `TS-0014-pagos-workflow.spec.ts` — CU15-SALDO-03 (#796): Saldo updates when presupuesto selection changes
- `TS-0014-pagos-workflow.spec.ts` — CU15-SALDO-04 (#848): Submitting a monto over saldo pendiente shows a specific rejection message
- `TS-0014-pagos-workflow.spec.ts` — CU15-GW02: Given form open, When fill and submit, Then pago is registered
- `TS-0014-pagos-workflow.spec.ts` — CU15-RECIBO-01 (#23): Given pago exists, When click emitir recibo, Then PDF is downloaded
- `TS-0014-pagos-workflow.spec.ts` — ESTADO-01: New presupuesto without payments shows SIN_PAGOS badge
- `TS-0014-pagos-workflow.spec.ts` — ESTADO-02: Presupuesto with a partial payment shows PARCIAL badge
- `TS-0014-pagos-workflow.spec.ts` — ESTADO-03: Presupuesto fully paid shows SALDADO badge
- `TS-0015-personas-clientes-workflow.spec.ts` — CU17-GW02: crear persona sin marcar es-cliente → persona aparece en tabla, NO como cliente
- `TS-0015-personas-clientes-workflow.spec.ts` — CU17-GW03: crear persona con es-cliente marcado (CU17 + CU18) → aparece en tabla con flag cliente
- `TS-0015-personas-clientes-workflow.spec.ts` — CU18-GW01: persona creada con checkbox es-cliente aparece como cliente en la vista
- `TS-0015-personas-clientes-workflow.spec.ts` — CU18-GW02: persona sin checkbox es-cliente puede ser promovida a cliente via edición (CU41)
- `TS-0015-personas-clientes-workflow.spec.ts` — Dedup-GW01: crear segunda persona con mismo DNI → error de duplicado visible
- `TS-0015-personas-clientes-workflow.spec.ts` — Dedup-GW02: toast de duplicado incluye enlace a la persona existente
- `TS-0015-personas-clientes-workflow.spec.ts` — Dedup-EDGE: crear persona sin DNI → error de validación en el form
- `TS-0015-personas-clientes-workflow.spec.ts` — CU61-GW01: buscar por apellido → tabla muestra resultados coincidentes
- `TS-0015-personas-clientes-workflow.spec.ts` — CU61-GW02: buscar por DNI → tabla muestra resultado exacto
- `TS-0015-personas-clientes-workflow.spec.ts` — VP-mobile-320: duplicate-DNI error toast visible at this breakpoint
- `TS-0015-personas-clientes-workflow.spec.ts` — VP-tablet-768: duplicate-DNI error toast visible at this breakpoint
- `TS-0015-personas-clientes-workflow.spec.ts` — VP-desktop-1024: duplicate-DNI error toast visible at this breakpoint
- `TS-0017-suplencias-workflow.spec.ts` — CU22-GW02: Given form open, When fill and submit, Then suplencia created
- `TS-0018-reingreso-documentacion-workflow.spec.ts` — golden path: reingresar a document shows a success toast
- `TS-0018-reingreso-documentacion-workflow.spec.ts` — edge path: a gestión without trámites shows the empty state
- `TS-0028-gestion-historial-feature.spec.ts` — golden path: transitioning a gestión adds a new bitácora entry
- `TS-0028-gestion-historial-feature.spec.ts` — edge path: a gestión with only its initial estado shows a single bitácora entry
- `TS-0028-gestion-historial-feature.spec.ts` — edge path: archiving a gestión whose estado cannot transition to Archivada shows a visible error
- `TS-0028-gestion-historial-feature.spec.ts` — bitácora view is usable at 320px (mobile)
- `TS-0028-gestion-historial-feature.spec.ts` — bitácora view is usable at 768px (tablet)
- `TS-0028-gestion-historial-feature.spec.ts` — bitácora view is usable at 1024px (desktop)
- `TS-0029-gestion-estado-transition-feature.spec.ts` — golden path: transitioning to a valid destination updates the estado
- `TS-0029-gestion-estado-transition-feature.spec.ts` — edge path: only valid workflow destinations are offered
- `TS-0029-gestion-estado-transition-feature.spec.ts` — cambiar estado action is usable at 320px (mobile)
- `TS-0029-gestion-estado-transition-feature.spec.ts` — cambiar estado action is usable at 768px (tablet)
- `TS-0029-gestion-estado-transition-feature.spec.ts` — cambiar estado action is usable at 1024px (desktop)
- `TS-0033-documentos-entidades-externas-feature.spec.ts` — golden path: registering a movement updates the document and shows a success toast
- `TS-0033-documentos-entidades-externas-feature.spec.ts` — edge path: a gestión without entidad externa documents shows the empty state
- `TS-0051-api-full-cycle-integration.spec.ts` — GET /api/v1/personas — list all personas
- `TS-0051-api-full-cycle-integration.spec.ts` — POST /api/v1/personas — create a persona
- `TS-0051-api-full-cycle-integration.spec.ts` — PUT /api/v1/personas/{id} — update a persona
- `TS-0051-api-full-cycle-integration.spec.ts` — Complete CREATE → READ → UPDATE → DELETE cycle
- `TS-0071-first-case-tutorial-onboarding.spec.ts` — a new user can set up and follow a complete first case
- `TS-0072-cuadernos-protocolo-workflow.spec.ts` — golden path: creating a cuaderno from 10 consecutive folios shows it in the list
- `TS-0072-cuadernos-protocolo-workflow.spec.ts` — edge path: selecting a count that is not a multiple of ten shows a validation error
- `TS-0072-cuadernos-protocolo-workflow.spec.ts` — edge path: including a damaged folio without observaciones shows a validation error
- `TS-0090-demo-two-full-cases.spec.ts` — seeds Caso A and Caso B end to end through the UI
- `TS-0092-gestion-suplencia-redirect.spec.ts` — CU48-GW01: alta de persona con registro de escribano se guarda correctamente
- `TS-0092-gestion-suplencia-redirect.spec.ts` — CU51-GW01: editar persona y modificar su registro de escribano
- `TS-0092-gestion-suplencia-redirect.spec.ts` — GW01: gestión creada para un escribano con suplencia activa se redirige al suplente
- `TS-0092-gestion-suplencia-redirect.spec.ts` — EDGE: gestión creada para un escribano sin suplencia activa no dispara aviso de redirección
- Bruno API tests: 45 failing assertion(s) — see the `bruno-results` artifact for per-request detail.

---
*Report generated by Playwright E2E Pipeline from actual test results (issue #587)*
