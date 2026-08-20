# Triage — Circuito de pagos de una gestión (finding 1 de `explore.md`)

> Fuente: `openspec/explore.md`, sección 1 "Cobranza" (subsecciones 1.1–1.5).
> Fecha: 2026-08-19
> Modo: **candidate list confirmada por el usuario** — se procede a crear los
> issues de GitHub. Cada uno queda registrado abajo con su número real en
> cuanto se crea.

## Candidato 1 — Verificar deuda pendiente al archivar una gestión y modelar el circuito de pago en el ciclo de vida

- **Caso de Uso**: CU16 – Archivar Gestión (#169)
- **RF**: RF-22 "Abonar presupuestos en cuotas" (issue #22); RF-37 "Archivar trámite" (issue #37, no confirmado — ver nota)
- **Tamaño**: M · **Prioridad**: `priority:high`
- **Roadmap**: Fundacional — los candidatos 3 (cuotas) y 5 (costos de documento) dependen de que exista un punto de verificación de saldo/deuda antes de cerrar una gestión.

## Candidato 2 — Exponer la relación pago ↔ presupuesto ↔ gestión y el resumen financiero de una gestión

- **Caso de Uso**: CU47 – Consultar Pago (#200); CU02 – Iniciar Gestión (#155)
- **RF**: RF-20 "Abonar trámite" (issue #20, no confirmado); RF-21 "Registrar quién abona el trámite" (*"el costo y saldo del trámite se calcula en base al [presupuesto]"*)
- **Tamaño**: M · **Prioridad**: `priority:high`
- **Descripción**: Un pago ya se guarda contra un presupuesto, pero esa
  relación no es visible ni consultable end-to-end (ver `explore.md` 1.2:
  "no hay forma de ver, desde una gestión, cuánto se presupuestó, cuánto se
  cobró y cuánto falta"). Se debe poder registrar un pago viéndolo asociado
  a su presupuesto y, transitivamente, a la gestión y al cliente, y consultar
  desde una gestión el total presupuestado / cobrado / saldo agregando todos
  sus presupuestos y trámites.
- **Relación con issues existentes**: complementa (no duplica) #796 (picker
  de presupuesto + saldo en el formulario de cobro) y #792 (persistir
  método de pago) — ambos abiertos, mismo módulo.
- **Roadmap**: Base para los candidatos 3, 4 y 5 (todos necesitan que el
  saldo/costo de una gestión sea calculable de punta a punta).

## Candidato 3 — Pagos parciales / en cuotas con seguimiento de plan (finding 1.3)

- **Caso de Uso**: CU15 – Procesar pago (#168); CU47 – Consultar Pago (#200)
- **RF**: RF-22 "Abonar presupuestos en cuotas" (issue #22)
- **Tamaño**: M · **Prioridad**: `priority:medium`
- **Descripción**: RF-22 exige poder abonar en cuotas sin montos fijos
  predefinidos y advertir de deuda al finalizar la gestión. Hoy cobrar "en
  partes" es posible de forma implícita (pagos repetidos) pero sin plan,
  cronograma, ni alerta de cuotas pendientes.
- **Depende de**: Candidato 2 (saldo/resumen financiero) y Candidato 1
  (verificación de deuda al archivar, que es donde se dispara la
  advertencia final de RF-22).

## Candidato 4 — Descuentos y recargos con motivo estructurado (finding 1.4)

- **Caso de Uso**: CU45 – Modificar presupuesto (#198); CU71 – Gestión de Items (#300)
- **RF**: ninguno lo exige explícitamente hoy (ver `explore.md` 1.4 — ni
  "descuento" ni "recargo" aparecen en ningún CU ni RF del SRS). Se ancla en
  CU45/CU71, que son los que hoy administran los ítems de un presupuesto.
- **Tamaño**: M · **Prioridad**: `priority:medium`
- **Descripción**: Un ítem de presupuesto hoy es un monto o porcentaje con
  observación de texto libre — no distingue un descuento de un recargo ni
  tiene motivo estructurado. Agregar un tipo de ítem (normal / descuento /
  recargo) con motivo, para poder reportar cuánto se descontó/recargó y por
  qué.
- **Nota Gate 0**: Como ningún RF cubre esto, el issue cita solo CU45/CU71
  como Caso de Uso — cumple la regla de "Issue vinculado a CU-XX, RF-XX o
  RNF-XX" (CU-XX es suficiente). Ambos CU deberán actualizarse para
  documentar el nuevo tipo de ítem.

## Candidato 5 — Costos adicionales de documentos (sellos, impuestos) vinculados al presupuesto y al saldo (finding 1.5)

- **Caso de Uso**: CU27 – Ingresar nuevo tipo de documento (#180); CU39 – Crear Plantilla Presupuesto (#192)
- **RF**: RF-04 "Editar plantillas de presupuestos" (issue #6, *"gastos fijos y variables como impuestos y sellos"*); RF-17 "Seguimiento de documentos" (issue #19, deudas/impuestos/vencimientos)
- **Tamaño**: L · **Prioridad**: `priority:medium`
- **Descripción**: La fecha de pago de un documento existe como dato suelto,
  sin monto ni vínculo al presupuesto o al saldo de la gestión. Este
  candidato conecta ese costo (sello/impuesto de un documento específico)
  con el presupuesto que lo debería reflejar, para que no quede fuera de
  cualquier cálculo de saldo o reporte de cobranza.
- **Depende de**: Candidato 2 (resumen financiero de gestión, que es donde
  este costo adicional debe terminar sumando).

---

## Estado

| Candidato | Issue GitHub | Estado |
|---|---|---|
| 1. Verificar deuda pendiente al archivar | [#819](https://github.com/matiaspakua/notaire/issues/819) | creado |
| 2. Exponer relación pago↔presupuesto↔gestión + resumen financiero | [#820](https://github.com/matiaspakua/notaire/issues/820) | creado |
| 3. Pagos parciales / cuotas | [#821](https://github.com/matiaspakua/notaire/issues/821) | creado |
| 4. Descuentos y recargos con motivo | [#822](https://github.com/matiaspakua/notaire/issues/822) | creado |
| 5. Costos adicionales de documentos | [#823](https://github.com/matiaspakua/notaire/issues/823) | creado |
