# Notaire — Hallazgos de Negocio (Business Layer)

> Alcance: **solo negocio**. No se listan bugs técnicos, de arquitectura ni de
> stack (para eso ver `openspec/explore_functional_report.md` y
> `explore_report.md`). Cada hallazgo aquí se formula como "¿qué puede o no
> puede hacer hoy el personal de la escribanía?", con la CU/RF y (si existe)
> el issue de GitHub como referencia de trazabilidad — sin nombres de clases,
> anotaciones ni detalles de implementación.
>
> Método: cruce de los 78+ Casos de Uso (`docs/100-business/102-use-cases/`),
> los 118 Requerimientos Funcionales del SRS (`requerimientos.csv`, excluyendo
> los ~24 no-funcionales de rendimiento/interfaz/seguridad/desarrollo), el
> diagrama de estados del ciclo de vida de una gestión
> (`docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml`)
> y el estado real de pantallas/API ya relevado en las exploraciones previas
> (`explore_functional_report.md`, generado 2026-08-11), con una segunda
> pasada exhaustiva el 2026-08-19 centrada en cobranza (pagos parciales,
> descuentos, recargos, costos adicionales) y una verificación de que el
> resto de los hallazgos sigue vigente (sin cambios en los módulos afectados
> desde la primera pasada). **No** se usó el estado abierto/cerrado de los 95
> issues `requerimiento-funcional` como señal — son placeholders de
> trazabilidad del SRS (todos quedan abiertos independientemente de si la
> funcionalidad ya existe), no un tracker de "hecho / no hecho".
>
> **Consolidación (2026-08-21):** este documento incorpora ahora el triage
> completo de sus 13 hallazgos originales — antes repartido en
> `explore_1.1_issues.md` (hallazgos 1.1–1.5) y `explore_2-9_issues.md`
> (hallazgos 2–9), ambos retirados tras la fusión. Cada hallazgo lleva su
> ficha de triage (Caso de Uso, RF, tamaño, prioridad, dependencias)
> inmediatamente después de su descripción, y la Tabla maestra de estado
> reemplaza a las dos tablas "Estado" que existían por separado.
>
> **Revisión de código (2026-08-21):** se cruzó el diagrama de estados
> (`transicion-de-estados.puml`) contra el motor de workflow real
> (`WorkflowNode`/`WorkflowTransition`/`WorkflowValidationService`/
> `WorkflowTraceService`) y contra el diagrama animado de la pantalla
> principal (`WorkflowTracker.tsx` en `dashboard/page.tsx`), a pedido
> explícito de verificar si ese diagrama animado puede imitar el ciclo
> completo de una gestión. Los hallazgos 2 y 3 (ya propuestos como #832 y
> #833) siguen vigentes y sin cambios. Surgió un hallazgo nuevo, el 10, que
> ninguno de los dos cubre.
>
> **Consolidación (2026-08-26):** el triage del último sub-hallazgo de 1.2
> sin Issue ("no existe tope que impida cobrar de más") vivía en un archivo
> aparte, `explore_1.2_overpayment_issue.md`, creado sólo porque en ese
> momento era el único punto pendiente de esta pasada. Confirmada la
> creación del Issue #848 y completado su `/opsx:propose`
> (`openspec/changes/pago-limite-saldo-pendiente/`), ese archivo se fusionó
> de vuelta en la sección 1.2 (abajo) y se eliminó — este documento vuelve a
> ser la única fuente de trazabilidad hallazgo↔Issue, sin excepción: **regla
> del proyecto, todo hallazgo de negocio debe tener un Issue de GitHub
> asociado, sin excepciones** (ver `CONSTITUTION.md` §4 y
> `docs/300-development/OPENSPEC-CONSTITUTION-BRIDGE.md`).
>
> **Cierre de Issue #820 (2026-08-26):** `openspec archive
> payment-financial-tracking` se ejecutó — `tasks.md` y `traceability.md`
> del change se reconstruyeron con evidencia real (PR #845, merge commit
> `a2a17f8`) antes de archivar, y su spec quedó sincronizada en
> `openspec/specs/pago-presupuesto-gestion-summary/spec.md`. La Tabla
> maestra de abajo también se reordenó por prioridad (en vez del orden de
> aparición de los hallazgos) para que sirva como orden de ejecución de
> `/opsx:apply`.

## Resumen ejecutivo

El patrón dominante no es "falta código" — es que **una regla de negocio se
construye una sola vez, como administración aislada, y nunca se vuelve a
consultar en la pantalla donde el personal realmente hace el trabajo.**
Plantillas de precios, sustituciones de escribano, motor de estados, catálogo
de vencimientos de documentos: todos existen como pantallas de "administrar
X" que funcionan por sí solas, pero ninguna alimenta la pantalla de tarea
diaria que debería consumirla. Aparte de ese patrón, hay un bloque completo
de práctica notarial (protocolo auxiliar, cuadernos, carpetas de trámite,
minuta de inscripción, control de numeración) que directamente no tiene
ningún desarrollo — ni pantalla administrativa ni nada.

Los cinco hallazgos de mayor impacto:

1. **El proceso de negocio en sí mismo no contempla el dinero.** El diagrama
   de estados que describe el ciclo de vida completo de una gestión —desde
   "Solicita iniciar una gestión" hasta "Archivar gestión"— pasa de
   *crear el presupuesto* a *iniciar la gestión* y, más adelante, a
   *archivar la gestión*, sin un solo estado o acción intermedia relacionada
   con dinero. No hay "cobrar", "registrar pago parcial", "aplicar descuento
   o recargo" ni "verificar deuda antes de archivar" en ningún punto del
   flujo modelado — pese a que el propio SRS exige advertir sobre deuda
   pendiente al cerrar una gestión (RF-22). No es solo que falten pantallas:
   **el proceso de negocio, tal como está diseñado, nunca decidió dónde va
   la plata.**
2. **Cobrar no dice cuánto se debe, ni cómo se cobró, ni entrega comprobante,
   ni sabe cobrar en cuotas, descuentos o recargos.**
   El flujo de pago (CU15/RF-18/20/21) no muestra el saldo pendiente al
   cobrar, no impide cobrar de más, descarta el método de pago ingresado, y
   nunca genera el recibo que el propio caso de uso exige entregar al
   cliente. Además, el pago en cuotas que el SRS exige explícitamente
   (RF-22) no tiene ningún circuito que lo sostenga, y no existe forma
   estructurada de cargar un descuento o un recargo con su motivo — ni
   siquiera como concepto en el propio SRS.
3. **Después de firmar una escritura, el sistema pierde el rastro legal.**
   Firma, generación de testimonio, verificación, inscripción en el registro
   y retiro del testimonio (CU06–CU12, RF-27 a RF-33) — la parte
   regulatoriamente más sensible del trámite notarial — no tiene ninguna
   pantalla que el personal pueda usar.
4. **El historial de una gestión no se escribe nunca, y cualquier caso puede
   saltar a cualquier estado.** El motor de flujos de estado (CU83) puede
   definir qué transiciones son válidas, pero ninguna pantalla real lo
   consulta; y la bitácora de cambios de una gestión (CU13/RF-24) queda
   vacía porque el flujo real de alta/edición de gestión nunca escribe en
   ella.
5. **Un bloque entero del SRS —protocolo auxiliar, cuadernos, carpetas de
   trámite, minuta de inscripción, control de numeración correlativa
   (RF-74 a RF-95, ~20 requerimientos)— no tiene ningún desarrollo**, ni
   siquiera una pantalla administrativa aislada.

---

## Tabla maestra de estado

Los 14 hallazgos de este documento (1.1–1.5, 2–9 y 10), su Issue de GitHub y
el estado de su `/opsx:propose`. `resuelto` = mergeado a `main` y archivado
en `openspec/changes/archive/`; `propose completo` = artefactos Gate 1
listos en `openspec/changes/`, pendiente de `/opsx:apply`.

**Orden:** por prioridad (`priority:high` → `priority:medium` → sin
prioridad), y dentro de cada prioridad, `propose completo, pendiente de
apply` primero — así la tabla sirve directamente como **orden de ejecución
de `/opsx:apply`**, un change por vez, en sesiones separadas. La fila 1.2
del documento original (que agrupaba 5 Issues con prioridades distintas) se
desagregó acá para que cada Issue se pueda ordenar y ejecutar por separado;
la sección 1.2 más abajo sigue siendo la lectura narrativa conjunta.

### Listos para `/opsx:apply` — `priority:high`

| Orden | Hallazgo | Issue | Change |
|---|----------|-------|--------|
| 1 | 2 — Circuito legal post-firma sin pantalla | [#832](https://github.com/matiaspakua/notaire/issues/832) | `openspec/changes/escritura-post-firma-legal-cycle/` |
| 2 | 3 — Historial/estados/archivado de gestión sin reglas | [#833](https://github.com/matiaspakua/notaire/issues/833) | `openspec/changes/gestion-workflow-y-bitacora/` |
| 3 | 5 — Cliente duplicado sin validación | [#835](https://github.com/matiaspakua/notaire/issues/835) | `openspec/changes/persona-validacion-duplicados/` |
| 4 | 9 — Bloque SRS sin desarrollo (RF-74 a RF-95) | [#839](https://github.com/matiaspakua/notaire/issues/839) | 5 cambios independientes: `protocolo-cuadernos-de-folios/`, `protocolo-carpetas-de-tramite/`, `protocolo-auxiliar-tramites/`, `protocolo-minuta-inscripcion/`, `protocolo-numeracion-escrituras/` |

Ejecutar 1 y 2 primero si se puede: son prerequisito de negocio del
hallazgo 10 (`#841`, ver más abajo), que hoy no tiene `/opsx:propose`.

### Listos para `/opsx:apply` — `priority:medium`

| Orden | Hallazgo | Issue | Change |
|---|----------|-------|--------|
| 5 | 1.2 — Sin tope que impida cobrar de más | [#848](https://github.com/matiaspakua/notaire/issues/848) | `openspec/changes/pago-limite-saldo-pendiente/` |
| 6 | 1.2 — Sin saldo visible al cobrar (picker + saldo) | [#796](https://github.com/matiaspakua/notaire/issues/796) | `openspec/changes/pago-presupuesto-picker-saldo/` |
| 7 | 1.3 — Pagos parciales / en cuotas sin circuito | [#821](https://github.com/matiaspakua/notaire/issues/821) | `openspec/changes/pagos-parciales-cuotas/` |
| 8 | 1.4 — Descuentos y recargos sin motivo estructurado | [#822](https://github.com/matiaspakua/notaire/issues/822) | `openspec/changes/descuentos-recargos-presupuesto/` |
| 9 | 1.5 — Costo de documentos sin conexión al presupuesto | [#823](https://github.com/matiaspakua/notaire/issues/823) | `openspec/changes/costos-documentos-presupuesto/` |
| 10 | 4 — Presupuestar sin plantillas ni catálogo de ítems | [#834](https://github.com/matiaspakua/notaire/issues/834) | `openspec/changes/presupuesto-plantillas-y-catalogo-items/` |
| 11 | 6 — Suplencias sin efecto práctico | [#836](https://github.com/matiaspakua/notaire/issues/836) | `openspec/changes/suplencia-efecto-en-gestiones/` |
| 12 | 7 — Tipos de documento sin reglas propias | [#837](https://github.com/matiaspakua/notaire/issues/837) | `openspec/changes/tipo-documento-vencimiento-config/` |
| 13 | 8 — Sin vínculo escritura↔folio, copia↔testimonio | [#838](https://github.com/matiaspakua/notaire/issues/838) | `openspec/changes/folio-vinculacion-escritura/` |

### Bloqueados — falta `/opsx:propose`

| Hallazgo | Issue | Prioridad | Motivo |
|----------|-------|-----------|--------|
| 10 — Motor de workflow no representa el bucle de reingreso post-firma | [#841](https://github.com/matiaspakua/notaire/issues/841) | `priority:medium` | Depende de que #832/#833 (arriba) se apliquen primero — sin `/opsx:propose` todavía |
| 1.2 — Recibo de pago nunca se emite | [#23](https://github.com/matiaspakua/notaire/issues/23) | sin prioridad asignada | Abierto, sin `/opsx:propose` iniciado |

### Ya resueltos (no requieren `/opsx:apply`)

| Hallazgo | Issue | Estado |
|----------|-------|--------|
| 1.1 — Diagrama de estados sin paso de dinero | [#819](https://github.com/matiaspakua/notaire/issues/819) | resuelto y archivado — spec `gestion-archive-debt-check` |
| 1.2 — Sin resumen financiero por gestión | [#820](https://github.com/matiaspakua/notaire/issues/820) | resuelto, mergeado (PR #845, `a2a17f8`) y archivado — spec `pago-presupuesto-gestion-summary` |
| 1.2 — Método de pago no se persistía | [#792](https://github.com/matiaspakua/notaire/issues/792) | resuelto y archivado — spec `pagos` |

Los 13 hallazgos originales tienen su `/opsx:propose` completo y validado
(`bash scripts/validate-sdlc-plan.sh`). El hallazgo 10 ya tiene Issue
(#841) pero todavía no tiene `/opsx:propose` — depende de que #832/#833 se
apliquen primero. Próximo paso: `/opsx:apply` change por change, siguiendo
el orden de arriba, en sesiones separadas, siguiendo `CONSTITUTION.md` — el
merge a `main` requiere aprobación humana explícita en cada caso.

---

## 1. Cobranza: se cobra "a ciegas", sin comprobante, y el proceso mismo no la contempla

### 1.1 El diagrama de estados de una gestión no tiene ningún paso de dinero *(Issue #819, resuelto y archivado — spec `gestion-archive-debt-check`)*

El diagrama que documenta el ciclo de vida completo de una gestión
(`transicion-de-estados.puml`) describe con detalle cada acción del trámite
—alta de cliente y persona, búsqueda o creación de presupuesto, espera de
documentación, preparación y firma de escritura, generación e inscripción
del testimonio, reingresos, retiro de testimonios, archivo de la gestión—
pero **en ningún punto aparece un paso de cobro, pago parcial, deuda o
saldo**:

```
:Buscar presupuesto / :Crear presupuesto;
       │
       ▼
:Iniciar gestión;                       ◀── nunca se cobra nada acá
       │
      ...  (documentación, escritura, firma, testimonio, inscripción)
       │
       ▼
:Archivar gestión;                      ◀── ni acá: no hay verificación
stop                                        de saldo/deuda antes de cerrar
```

Esto es distinto de "falta una pantalla de cobro": es que **el propio
proceso de negocio, tal como está diagramado, nunca decidió en qué momento
del trámite se cobra, cómo se registra una deuda pendiente, ni qué pasa si
una gestión se archiva sin estar saldada.** El SRS sí exige ese control
explícitamente — RF-22 ("Abonar presupuestos en cuotas") dice que "se debe
advertir de cualquier deuda al momento de finalizar la gestión" — pero esa
advertencia no tiene ningún lugar en el flujo modelado donde podría
engancharse, porque "archivar gestión" (RF-34/35) es, como ya señala el
hallazgo 3, apenas un cambio de estado sin reglas. El circuito de pago vive,
en el mejor de los casos, como una pantalla aislada de "cobrar" (CU15) sin
ninguna conexión con el ciclo de vida de la gestión que le da origen.

**Ficha de triage** — Caso de Uso: CU16 – Archivar Gestión (#169). RF: RF-22
"Abonar presupuestos en cuotas" (issue #22); RF-37 "Archivar trámite"
(issue #37, no confirmado). Tamaño: M · Prioridad: `priority:high`.
Roadmap: fundacional — los hallazgos 1.3 (cuotas) y 1.5 (costos de
documento) dependen de que exista un punto de verificación de saldo/deuda
antes de cerrar una gestión.

### 1.2 Al cobrar, no se ve cuánto se debe, no se controla el método, no se emite comprobante

```
Cliente pide pagar
       │
       ▼
Recepcionista anota el Nº de presupuesto de memoria
       │                              ▲
       ▼                              │ no hay dato para
Ingresa el monto a cobrar             │ verificar cuánto
       │                              │ se debe realmente
       ▼                              │
Sistema acepta el monto,        ──────┘
sea cual sea (incluso > saldo)
       │
       ▼
Elige método de pago (efectivo / transferencia)  ──▶ se pierde, no se guarda
       │
       ▼
Se guarda el pago
       │
       ▼
Recibo para el cliente  ──▶ NO EXISTE — no hay ninguna forma de emitirlo
```

- **No hay visibilidad del saldo pendiente al momento de cobrar** (RF-20
  "Abonar presupuestos", CU15 paso 5). El caso de uso exige mostrar el
  presupuesto, el trámite y la gestión a la que pertenece antes de cobrar;
  hoy el operador ingresa un número de presupuesto de memoria, sin ver
  cliente, gestión ni saldo. Lo mismo exige CU47 ("Consultar pago") al
  mostrar el detalle de un presupuesto — el "saldo" es un dato que el SRS
  espera ver en dos pantallas distintas y no existe en ninguna. *(Issue
  #796, propose completo — `openspec/changes/pago-presupuesto-picker-saldo/`,
  pendiente de apply)*
- **No existe tope que impida cobrar de más.** El sistema no rechaza (ni
  advierte) un pago que exceda lo adeudado — un pago mal tipeado se acepta
  igual que uno correcto, sin que nadie se entere en el momento. El propio
  Javadoc de `procesarPago` afirma *"valida que el monto no exceda el
  total"*, pero el único chequeo real es `monto <= 0`
  (`PagoService.java:29-73`). *(Issue #848, propose completo —
  `openspec/changes/pago-limite-saldo-pendiente/`, pendiente de apply)*
- **El método de pago que se le pide al cliente se pierde.** El personal
  registra "efectivo" o "transferencia" y ese dato no llega a ningún lado —
  el sistema deja creer que quedó registrado cuando no es así. *(Issue #792,
  resuelto y archivado — spec `pagos`)*
- **El recibo común nunca se genera** (RF-21, CU15 pasos 12–14: "Genera el
  recibo... donde figura fecha de pago, número de presupuesto, cliente,
  gestión asociada"). No hay ninguna forma de imprimir o entregar un
  comprobante de pago — el paso final del propio caso de uso simplemente no
  existe en el producto. *(Issue #23, abierto, sin trabajo iniciado)*
- **No hay forma de ver, desde una gestión, cuánto se presupuestó, cuánto se
  cobró y cuánto falta.** El pago se registra contra un presupuesto suelto;
  nada resume esa información al nivel del caso (gestión), que es como el
  cliente y el personal piensan el trámite. *(Issue #820, resuelto,
  mergeado — PR #845, `a2a17f8` — y archivado; spec
  `pago-presupuesto-gestion-summary`)*

**Ficha de triage (Issue #848)** — Caso de Uso: CU15 – Procesar Pago (#168).
RF: RF-18 "Abonar trámite" (issue #20), sub-requerimiento RF-18.2 "Abonar
presupuestos". Tamaño: S · Prioridad: `priority:medium`. Descripción: no
hay redondeo/propina que justifique aceptar un pago mayor al saldo — es un
control faltante en `PagoService.procesarPago`, expuesto vía REST
(`PagoController`) y consumido por
`frontend/src/app/dashboard/pagos/page.tsx`. Relación con issues
existentes: complementa (no duplica) #796 (que da visibilidad al saldo pero
no impide excederlo) y #820 (resumen a nivel gestión, no validación en el
momento del cobro). Roadmap: independiente — no bloquea ni es bloqueado por
#821/#822/#823.

**Ficha de triage (Issue #820)** — Caso de Uso: CU47 – Consultar Pago
(#200); CU02 – Iniciar Gestión (#155). RF: RF-20 "Abonar trámite" (issue
#20, no confirmado); RF-21 "Registrar quién abona el trámite" (*"el costo y
saldo del trámite se calcula en base al [presupuesto]"*). Tamaño: M ·
Prioridad: `priority:high`. Descripción: un pago ya se guarda contra un
presupuesto, pero esa relación no es visible ni consultable end-to-end; se
debe poder consultar desde una gestión el total presupuestado / cobrado /
saldo agregando todos sus presupuestos y trámites. Relación con issues
existentes: complementa (no duplica) #796 (picker de presupuesto + saldo en
el formulario de cobro) y #792 (persistir método de pago) — ambos del mismo
módulo. Roadmap: base para los hallazgos 1.3, 1.4 y 1.5 (todos necesitan que
el saldo/costo de una gestión sea calculable de punta a punta) — aunque,
tras el `propose` de los tres, ninguno resultó bloqueado en la práctica: cada
uno reutiliza `PagoService.calcularSaldoPendiente`/`calcularTotalPresupuesto`
ya existentes en `main`, sin esperar a que #820 se implemente (ver Out of
Scope de cada propuesta). **Estado real (verificado 2026-08-26 vía
`gh issue view 820` y `git log`): mergeado y cerrado — PR #845, commit
`a2a17f8`.** El change se reconstruyó (`tasks.md`/`traceability.md`
actualizados con evidencia real de PR/merge commit) y se archivó con
`openspec archive payment-financial-tracking` el 2026-08-26 —
`openspec/changes/archive/2026-08-26-payment-financial-tracking/`; su spec
quedó sincronizada en `openspec/specs/pago-presupuesto-gestion-summary/spec.md`.

### 1.3 Pagos parciales / en cuotas: el SRS los exige y no existe ningún circuito *(Issue #821, propose completo — `openspec/changes/pagos-parciales-cuotas`, pendiente de apply; no bloqueado por #820 — ver Out of Scope de la propuesta)*

- RF-22 es explícito: *"Los presupuestos pueden abonarse por completo o en
  cuotas sin montos fijos predefinidos... Se debe advertir de cualquier
  deuda al momento de finalizar la gestión."* Es decir, el SRS no solo pide
  poder cobrar en partes — pide que el sistema **avise activamente si queda
  saldo pendiente al cerrar el caso**.
- Hoy no existe ni lo uno ni lo otro. Cobrar "en partes" solo es posible de
  manera implícita e informal (repetir el paso de cobro varias veces contra
  el mismo presupuesto), sin que el sistema calcule ni muestre el saldo
  restante (1.2), sin plan ni cronograma de cuotas, sin fecha de
  vencimiento de cada cuota, y sin ningún recordatorio o alerta de pago
  pendiente. Y la advertencia de deuda al finalizar que pide RF-22
  simplemente no puede existir mientras "archivar gestión" siga siendo un
  cambio de estado libre (hallazgo 3) y no un cierre de caso con reglas.
- Ni siquiera a nivel de caso de uso está desarrollado el circuito: CU15 y
  CU47 citan RF-22 como referencia cruzada, pero ninguno de los dos
  describe un paso de "definir plan de cuotas" o "consultar cuotas
  pendientes" — el requerimiento está declarado en el SRS pero nunca se
  bajó a un flujo operativo concreto, ni en el producto ni en su
  documentación de casos de uso.

**Ficha de triage** — Caso de Uso: CU15 – Procesar pago (#168); CU47 –
Consultar Pago (#200). RF: RF-22 "Abonar presupuestos en cuotas" (issue
#22). Tamaño: M · Prioridad: `priority:medium`. Dependencias declaradas en
triage: hallazgo 1.2 (saldo/resumen financiero) y 1.1 (verificación de
deuda al archivar, donde se dispara la advertencia final de RF-22) —
confirmado en el `propose` que este cambio reutiliza
`calcularSaldoPendiente` ya existente y no requiere que #820 esté
implementado primero.

### 1.4 Descuentos y recargos: no existen ni como concepto, tampoco en el SRS *(Issue #822, propose completo — `openspec/changes/descuentos-recargos-presupuesto`, pendiente de apply; no bloqueado por #820 — ver Out of Scope de la propuesta)*

- Un presupuesto se arma con ítems que llevan un monto o un porcentaje y una
  observación de texto libre — no hay forma de marcar un ítem como
  "descuento" o "recargo", ni un campo de motivo estructurado (mora,
  urgencia, promoción, ajuste comercial, etc.). Cualquier ajuste al alza o
  a la baja depende de que la persona que carga el presupuesto escriba a
  mano, en un campo de observaciones, por qué lo hizo — sin que el sistema
  distinga ese ítem de un concepto normal ni pueda reportarlo como tal más
  adelante.
- Esto no es solo una funcionalidad faltante en el producto: **"descuento",
  "recargo" y "sobrecargo" no aparecen mencionados en ningún Caso de Uso
  del proyecto**, ni como paso de un flujo ni como referencia cruzada a un
  RF. A diferencia de las cuotas (que sí están en el SRS aunque sin
  desarrollo — 1.3), acá el vacío es anterior: la escribanía necesita poder
  aplicar descuentos y recargos con motivo (algo habitual en la práctica
  notarial — pronto pago, urgencia, mora, cliente frecuente) pero esa
  necesidad todavía no quedó capturada ni en la documentación de negocio.
- Consecuencia práctica: no hay manera de reportar, por gestión o por
  período, cuánto se descontó y por qué, ni cuánto se recargó y por qué —
  información que un escribano necesitaría tanto para su propia
  contabilidad como para justificar una diferencia de precio ante un
  cliente que lo reclame.

**Ficha de triage** — Caso de Uso: CU45 – Modificar presupuesto (#198);
CU71 – Gestión de Items (#300). RF: ninguno lo exige explícitamente hoy —
se ancla en CU45/CU71, que son los que hoy administran los ítems de un
presupuesto (Gate 0: CU-XX solo es suficiente cuando ningún RF cubre el
hallazgo). Tamaño: M · Prioridad: `priority:medium`. Nota: ambos CU
debieron actualizarse en el `propose` para documentar el nuevo tipo de
ítem.

### 1.5 Costos adicionales de documentos (sellos, impuestos): el monto existe pero no se conecta al presupuesto *(Issue #823, propose completo — `openspec/changes/costos-documentos-presupuesto`, pendiente de apply; no bloqueado por #820 — ver Out of Scope de la propuesta)*

> **Corrección (grounding, propose completo):** el párrafo original de este
> hallazgo afirmaba que el dato de pago de un documento era "una fecha
> suelta, sin monto". Al hacer el `propose`, se verificó contra
> `DocumentoPresentado.java` que el campo `importeAPagar` (monto) **ya
> existe** junto a `fechaPago` — el problema real, más acotado, es que ese
> monto nunca se suma a ningún cálculo de total o saldo. Ver
> `openspec/changes/costos-documentos-presupuesto/proposal.md` — Objetivo.

- El SRS pide que la plantilla de presupuesto contemple "gastos fijos y
  variables como impuestos y sellos" (RF-06) y que el seguimiento de
  documentos informe sobre deudas y vencimientos asociados, avisando si una
  deuda fue cancelada y el documento liberado, o si sigue pendiente de pago
  o retiro (RF-19).
- `DocumentoPresentado` sí registra, a nivel de dato, tanto `fechaPago` como
  `importeAPagar` (monto) para un documento — **pero ninguno de los dos está
  vinculado al presupuesto o a los pagos de la gestión**. Se puede cargar el
  monto y la fecha de pago de un certificado, pero ese monto no suma al
  total del presupuesto, no descuenta saldo, no aparece en el resumen de
  cobranza de la gestión (1.2) y no se refleja en ningún recibo. Es el
  mismo patrón que el resto del documento señala en la sección "Por qué se
  repite este patrón": un dato administrado de forma aislada que nunca se
  conecta al circuito financiero real de la gestión.
- Resultado de negocio: los costos adicionales por sellos, impuestos u
  otros conceptos ligados a un documento específico (no al trámite en
  general) quedan fuera de cualquier presupuesto, de cualquier saldo y de
  cualquier reporte de cobranza — se pagan y se controlan, si acaso, fuera
  del sistema.

**Ficha de triage** — Caso de Uso: CU27 – Ingresar nuevo tipo de documento
(#180); CU39 – Crear Plantilla Presupuesto (#192). RF: RF-04 "Editar
plantillas de presupuestos" (issue #6, *"gastos fijos y variables como
impuestos y sellos"*); RF-17 "Seguimiento de documentos" (issue #19,
deudas/impuestos/vencimientos). Tamaño: L · Prioridad: `priority:medium`.
Dependencia declarada en triage: hallazgo 1.2 (resumen financiero de
gestión, donde este costo adicional debe terminar sumando) — confirmado en
el `propose` que no bloquea, ya que `GestionArchiveDebtService` (#819) ya
agrega el saldo por presupuesto y heredará este cambio automáticamente.

## 2. Después de la firma, el trámite notarial "desaparece" del sistema

El ciclo legal posterior a preparar una escritura —firmarla, generar el
testimonio, verificarlo, presentarlo para inscripción en el registro,
retirarlo una vez inscripto— es una secuencia obligatoria desde el punto de
vista regulatorio. Hoy:

| Paso del negocio | Caso de Uso / RF | Estado |
|---|---|---|
| Firmar la escritura | CU06 / RF-27 | Sin ninguna acción disponible — "firmada" no es algo que el personal pueda marcar |
| Generar el testimonio de una escritura | CU07 / RF-31 | Sin pantalla |
| Verificar un testimonio | CU08 | Sin pantalla |
| Presentar / seguir la inscripción en el registro | CU11, RF-30, RF-92 | Sin pantalla |
| Registrar testimonios ya inscriptos | RF-32 | Sin pantalla |
| Retirar / reingresar un testimonio | CU12, CU44, RF-33 | Sin pantalla |
| Emitir copia impresa del testimonio | RF-94 | Sin pantalla |

Ninguno de estos pasos tiene una pantalla que un escribano o su personal
pueda usar hoy. Para una escribanía, esto es la mitad "seria" del trámite —
la que tiene efectos legales frente a terceros (el registro de la
propiedad) — y actualmente vive fuera del sistema: en papel, en memoria, o
en una planilla aparte.

**Ficha de triage (Issue #832)** — Caso de Uso: CU06 – Firmar escritura
(#159); CU07 – Generar testimonio (#160); CU08 – Verificar Testimonio
(#161); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio
(#165); CU44 – Reingresar testimonio (#197). RF: RF-27 (firmar escritura),
RF-31 (generar testimonio), RF-30/RF-92 (presentar/seguir inscripción),
RF-32 (registrar testimonios inscriptos), RF-33 (retirar/reingresar
testimonio), RF-94 (emitir copia impresa). Tamaño: L · Prioridad:
`priority:high`. Nota de alcance: son 6 pasos secuenciales de un mismo
circuito regulatorio, agrupados en un único hallazgo porque este documento
los describe como una sola cadena; el `propose` evaluó dividir en fases
(firma+testimonio / inscripción / retiro-copia) para no bloquear todo el
circuito en un solo cambio — ver `openspec/changes/escritura-post-firma-legal-cycle/`.

## 3. El historial de una gestión no registra nada, y los estados no tienen reglas

- **La bitácora de una gestión (CU13, "Ver historial de gestión", RF-24)
  siempre está vacía** para cualquier caso creado por el flujo real de alta o
  edición de gestión — ese flujo nunca escribe en ella. Es un historial de
  auditoría que, para el trabajo real del día a día, no audita nada.
- **Cualquier gestión puede pasar de cualquier estado a cualquier otro,**
  sin restricción. Existe una funcionalidad de administración de flujos de
  estado (CU83, "Definir Workflow de Estados y Transiciones" — validar que
  un flujo tenga un estado inicial, uno o más finales, y que no haya estados
  inalcanzables) pero **nada en la pantalla real de gestión consulta esas
  reglas.** El escribano puede definir "Borrador → En Revisión → Firmada →
  Registrada, nunca hacia atrás", y el sistema deja mover un caso real de
  Borrador a Registrada directamente, o de Registrada de nuevo a Borrador.
  La herramienta de definir el proceso no protege el proceso real.
- **Archivar una gestión (CU16, RF-34/35) no es una acción reconocible** —
  si es alcanzable, es solo cambiando el estado a mano por el mismo selector
  sin reglas de arriba, no como un cierre de caso intencional.

**Ficha de triage (Issue #833)** — Caso de Uso: CU13 – Ver historial de
gestión (#166); CU83 – Definir Workflow de Estados y Transiciones (#451,
#453, #454, #455); CU16 – Archivar Gestión (#169, #819). RF: RF-24
(historial/bitácora de gestión), sin RF explícito para la validación de
transiciones de estado. Tamaño: L · Prioridad: `priority:high`. Nota de
alcance: tres gaps relacionados (bitácora, motor de transiciones,
archivado) que comparten la misma raíz — la pantalla de gestión no
consulta las reglas ya modeladas — ver
`openspec/changes/gestion-workflow-y-bitacora/`.

## 4. Presupuestar sigue dependiendo de la memoria del personal

- Existen plantillas de precios por tipo de trámite (RF-04, RF-64 a RF-67,
  CU39/CU55/CU49 "Plantilla Presupuesto") donde se puede definir qué
  conceptos e importes debería llevar el presupuesto de, por ejemplo, una
  compraventa. Existe también un catálogo de "ítems" reutilizables
  (CU71, RF-07 "Agregar ítems adicionales").
- **Ninguna de las dos cosas se usa al armar un presupuesto real.** Cargar un
  presupuesto hoy es escribir un monto único a mano — no hay selector de
  tipo de trámite que traiga la plantilla, ni forma de agregar ítems de la
  lista. Dos personas cotizando el mismo tipo de trámite no tienen ninguna
  fuente común de precio salvo acordarse — exactamente el problema que la
  plantilla fue creada para resolver.

**Ficha de triage (Issue #834)** — Caso de Uso: CU39 – Crear Plantilla
Presupuesto (#192); CU55 – Modificar Plantilla Presupuesto (#208); CU49 –
Eliminar Plantilla Presupuesto (#202); CU71 – Gestión de Items (#300). RF:
RF-04, RF-64 a RF-67 (plantillas de presupuesto por tipo de trámite), RF-07
(agregar ítems adicionales). Tamaño: M · Prioridad: `priority:medium`. Ver
`openspec/changes/presupuesto-plantillas-y-catalogo-items/` — sin
solapamiento con 1.5 (#823), que no toca `TipoDeDocumento`/
`DocumentoPresentado`.

## 5. Un cliente puede quedar duplicado sin que nadie lo note

- No hay ninguna validación que impida cargar dos veces a la misma persona
  (mismo tipo y número de documento de identidad) al dar de alta un cliente
  (CU17/CU18, RF-37). Un typo, una carga por dos empleados distintos, o una
  búsqueda fallida que termina en alta nueva, produce dos identidades
  separadas para el mismo cliente real.
- El impacto de negocio es directo: los presupuestos, gestiones y pagos de
  ese cliente quedan repartidos entre dos fichas sin vínculo, y nada en el
  sistema avisa que eso ocurrió — el historial del cliente queda
  silenciosamente incompleto cada vez que se lo busca.

**Ficha de triage (Issue #835)** — Caso de Uso: CU17 – Dar Alta persona
(#170); CU18 – Dar Alta Cliente (#171). RF: RF-37. Tamaño: S · Prioridad:
`priority:high`. Ver `openspec/changes/persona-validacion-duplicados/`.

## 6. Suplencias: se puede registrar, pero no cambia nada en la práctica

- El sistema permite registrar que un escribano suplente cubre a otro
  durante un período (CU22/CU59, RF-87 a RF-90). Es una pantalla completa y
  funcional en sí misma.
- **Pero nada usa esa información.** Al crear una gestión, el escribano
  responsable se asigna directamente — el sistema nunca revisa si ese
  escribano tiene una suplencia activa que debería redirigir el caso al
  suplente (RF-89, "Asignar suplente a una gestión"). Registrar una
  suplencia no tiene ningún efecto sobre a quién se le asignan los casos
  nuevos mientras esa suplencia está vigente.
- **Dar de alta o modificar un escribano como tal (CU48/CU51, RF-88
  "Registrar suplentes del escribano") no tiene un lugar dedicado** en la
  pantalla de personas — la credencial de escribano no es algo que el
  personal pueda gestionar explícitamente hoy.

**Ficha de triage (Issue #836)** — Caso de Uso: CU22 – Registrar Suplencia
(#175); CU59 – Consultar Suplencias (#212); CU48 – Dar alta escribano
(#201); CU51 – Modificar escribano (#204). RF: RF-87 a RF-90 (suplencias),
RF-88 (registrar suplentes del escribano). Tamaño: M · Prioridad:
`priority:medium`. Ver `openspec/changes/suplencia-efecto-en-gestiones/`.

## 7. Los tipos de documento no pueden llevar sus propias reglas

- El catálogo de tipos de documento (CU27/CU32, RF-55) modela — a nivel de
  dato — si un documento vence, cuántos días dura, y quién es responsable de
  entregarlo o devolverlo. **Pero la pantalla de administración solo permite
  cargar el nombre del tipo de documento** — ninguno de esos otros datos se
  puede completar desde el producto.
- Consecuencia directa: **"Informar próximos vencimientos" (CU42, RF-16)
  no tiene ni pantalla ni forma de calcularse**, porque el dato de vencimiento
  que necesitaría nunca se carga en primer lugar. No es solo una pantalla
  faltante — es una funcionalidad estructuralmente imposible mientras el
  campo del que depende no se pueda completar.
- Registrar la documentación de un cliente al día de hoy siempre crea un
  registro con esos campos vacíos por defecto, sin que el personal los haya
  decidido — el sistema completa "no vence", "sin responsable de entrega" en
  silencio.

**Ficha de triage (Issue #837)** — Caso de Uso: CU27 – Ingresar nuevo tipo
de documento (#180); CU32 – Modificar tipo de documento (#185); CU42 –
Informar próximos vencimientos (#195). RF: RF-55 (catálogo de tipos de
documento), RF-16 (informar próximos vencimientos). Tamaño: S · Prioridad:
`priority:medium`. Ver `openspec/changes/tipo-documento-vencimiento-config/`.

## 8. El protocolo notarial no se puede armar desde el sistema

- Folios y escrituras se administran cada uno por su lado (altas, búsquedas,
  numeración) y funcionan bien de forma aislada.
- **Pero no hay ninguna acción para vincular una escritura con el folio que
  ocupa, ni una copia con el testimonio del que proviene.** El protocolo —el
  registro oficial y de cumplimiento regulatorio que dice "esta escritura
  vive en tal folio, esta copia salió de tal testimonio"— no tiene ningún
  camino para armarse a través del producto. Cada pieza existe; el ensamblaje
  entre ellas no.

**Ficha de triage (Issue #838)** — Caso de Uso: CU87 – Vincular Escritura a
Folio y Copia a Testimonio (**nuevo**, creado en el triage); CU28 –
Ingresar nuevos folios (#181); CU05 – Preparar escritura (#158); CU07 –
Generar testimonio (#160); CU80 – Administrar Cuadernos de Folios (#311).
RF: RF #94 (Administrar folios), RF #96 (Control de numeración correlativa
de folios), RF #121 (Control de numeración de escrituras). Tamaño: M ·
Prioridad: `priority:medium`. Documento creado:
`docs/100-business/102-use-cases/CU87 – Vincular Escritura a Folio y Copia
a Testimonio.md`. Ver `openspec/changes/folio-vinculacion-escritura/`.

## 9. Bloque completo del SRS sin ningún desarrollo

Los siguientes requerimientos (~20, RF-74 a RF-95) no tienen ni backend ni
pantalla — a diferencia de los hallazgos anteriores, aquí no hay ni siquiera
una administración aislada que funcione por sí sola. Es una porción entera
de la práctica notarial documentada en el SRS que todavía no se empezó:

| Área de negocio | RF | Qué debería permitir hacer |
|---|---|---|
| Cuadernos | RF-74 a RF-77 | Generar, numerar cuadernos y su carátula |
| Carpetas de Trámite | RF-78 a RF-80 | Generar la carpeta física/lógica de un trámite y sus estados |
| Protocolo Auxiliar | RF-85, RF-86, RF-93 | Manejar el circuito de trámite en protocolo auxiliar y sus diferencias con el protocolo principal |
| Minuta de Inscripción | RF-91 | Generar la minuta que acompaña la inscripción registral |
| Control de numeración | RF-70, RF-95 | Asegurar numeración correlativa de folios y de escrituras, y detectar folios dañados o no usados (RF-71, RF-72) |

Estas áreas corresponden a trámites y controles que un notario real necesita
para cumplir con la reglamentación del Colegio Notarial y el registro de la
propiedad — hoy se resuelven, si acaso, completamente fuera del sistema.

**Ficha de triage (Issue #839)** — Caso de Uso: CU80 – Administrar
Cuadernos de Folios (#311, Cuadernos); CU85 – Administrar Carpetas de
Trámite (**nuevo**); CU81 – Gestión de Trámites en Protocolo Auxiliar
(#312); CU82 – Generar Minuta de Inscripción (#313); CU86 – Controlar
Numeración Correlativa de Escrituras (**nuevo**). RF: RF-74 a RF-77
(cuadernos), RF-78 a RF-80 / #104-106 (carpetas de trámite), RF-85/86/93
(protocolo auxiliar), RF-91 (minuta de inscripción), RF-95 / #121 (control
de numeración de escrituras) — la numeración correlativa de **folios**
(RF-70/71/72, #96/97/98) queda fuera de este hallazgo porque ya está
cubierta por CU28/CU80. Tamaño: L · Prioridad: `priority:high`. Documentos
creados: `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de
Trámite.md`, `docs/100-business/102-use-cases/CU86 – Controlar Numeración
Correlativa de Escrituras.md`. Con CU85 y CU86 creados, las 5 áreas de
negocio quedan con Caso de Uso propio y se propusieron como 5 cambios
independientes (ver Tabla maestra de estado).

---

## 10. El motor de workflow no puede representar el circuito legal post-firma, ni siquiera después de aplicar #832 y #833

Esta revisión partió de una pregunta puntual: ¿la pantalla principal —el
diagrama de estados animado que muestra el progreso de una gestión— puede
en principio llegar a imitar el ciclo completo descrito en
`transicion-de-estados.puml`? La respuesta, tras leer el motor de workflow
de punta a punta (`WorkflowNode`, `WorkflowTransition`,
`WorkflowValidationService`, `WorkflowTraceService`,
`WorkflowTracker.tsx`), es: **hoy no, y tampoco después de que se apliquen
los hallazgos 2 (#832) y 3 (#833) que ya están propuestos** — hay un tercer
gap, más profundo, que ninguno de los dos cubre.

```
transicion-de-estados.puml (fragmento post-firma):

  :Firmar escritura;
  :Generar testimonio;
  :Ingresar para inscripción;
       │
       ▼
  ┌─────────────────────┐
  │ ¿Volvió observado?   │◀── LOOP: puede repetirse
  └─────────────────────┘        N veces por testimonio
       │ sí
       ▼
  :Registrar reingreso;
  :Ingresar para inscripción;  ──┘
       │ no
       ▼
  :Retirar testimonio;   (repite por c/testimonio de la gestión)
```

- **`WorkflowNode` está atado 1 a 1 a un `EstadoDeGestion` existente** — así
  lo dice la propia documentación de CU83 ("cada uno vinculado a un
  `EstadoDeGestion` existente") y lo confirma el código
  (`WorkflowNode.fkEstadoDeGestionId`). El catálogo real de
  `EstadoDeGestion` (10 filas, sembradas en `V2__initial_data.sql`) no tiene
  ningún estado para "testimonio generado", "testimonio verificado",
  "ingresado a inscripción", "retirado" ni "reingresado" — son sub-pasos del
  ciclo legal que #832 va a modelar sobre `Escritura`/`Testimonio`/
  `MovimientoTestimonio`, no sobre `Gestion.estado`.
- **El bucle de reingreso no es un estado, es un evento que se repite.** El
  propio `MovimientoTestimonio` (que #832 usa deliberadamente, ver su
  design.md — Decisions) modela cada ingreso/retiro/reingreso como una fila
  nueva, precisamente porque un testimonio puede reingresar varias veces sin
  perder el rastro de los intentos anteriores. `WorkflowNode`/`Historial`,
  en cambio, modelan **un único estado "actual" por gestión** — no hay forma
  de que un grafo de nodos mutuamente excluyentes represente "este
  testimonio ya reingresó 3 veces" sin, en el mejor de los casos, perder esa
  cuenta cada vez que se revisita el mismo nodo.
- **Consecuencia concreta para la pantalla principal:** `WorkflowTracker.tsx`
  (el diagrama animado en `WorkflowHero`, `dashboard/page.tsx`) es
  arquitectónicamente genérico — dibuja cualquier grafo que le llegue desde
  `WorkflowTraceService.buildTrace`. Eso significa que, una vez aplicado
  #833 (que hace que `Historial` se escriba de verdad), si un administrador
  configurara un `WorkflowDefinition` con más nodos, el tracker sí mostraría
  más progreso que hoy. Pero **no existe combinación de nodos que reproduzca
  el bucle de reingreso**, porque el modelo de datos subyacente no tiene
  dónde guardar "cuántas veces" sin convertir cada reingreso en un estado
  distinto (lo cual tampoco resolvería el bucle, solo lo aplanaría a una
  secuencia fija con un tope arbitrario de reingresos).
  - Dato adicional, menor: el `WorkflowDefinition` de demostración
    sembrado hoy (`V10__seed_workflow_demo_data.sql`) sólo cubre 7 nodos —
    alta → documentación → escritura redactada → firma → inscripción →
    archivo-sin-escritura — y sólo está asignado a 3 `TipoDeTramite`; ni
    siquiera alcanza la granularidad que el modelo actual ya permitiría.
    Esto no es un bug, es data de demo incompleta, y queda subsumido por
    #833 en la medida en que #833 exige que cada `TipoDeTramite` real tenga
    su propio `WorkflowDefinition` correctamente configurado — pero vale
    dejarlo anotado porque es lo primero que un administrador va a notar al
    entrar a la pantalla configurada hoy.

Esto no invalida a #832 ni a #833 — ambos siguen siendo prerequisito
correcto y necesario (sin ellos, ni siquiera la parte no-cíclica del
diagrama se puede reflejar). Es un gap adicional, más chico en superficie
pero conceptualmente distinto: **el modelo de "un nodo = un estado actual"
del motor de workflow no alcanza para representar un sub-flujo con
reintentos**, y alguien va a tener que decidir — al implementar #832 o en un
cambio aparte — si eso se resuelve (a) agregando estados a
`EstadoDeGestion` para el circuito post-firma y aceptando que el tracker
muestre una versión aplanada/sin-loop del reingreso, o (b) mostrando
`MovimientoTestimonio` como una línea de tiempo secundaria, separada del
grafo de `WorkflowNode`, en vez de forzarlo dentro del mismo modelo. Esa
decisión de diseño queda fuera del alcance de esta exploración.

**Ficha de triage (Issue #841)** — Caso de Uso: CU83 –
Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU06 –
Firmar escritura (#159); CU07 – Generar testimonio (#160); CU11 – Ingresar
para inscripción (#164); CU44 – Reingresar testimonio (#197). RF: sin RF
explícito — es un gap de modelo de datos, no un requerimiento del SRS.
Tamaño: M · Prioridad: `priority:medium`. Roadmap: depende de que #832 y
#833 se apliquen primero (ninguno de los dos lo bloquea a nivel de
propuesta, pero como decisión de diseño conviene resolverlo sabiendo cómo
quedó el circuito de #832 en código real, no solo en el proposal). No
bloquea a #832 ni a #833 — es un refinamiento posterior de la misma área.

## Por qué se repite este patrón

En casi todos los hallazgos de negocio (1 a 8) la forma es la misma: **la
regla se modeló y se puede administrar sola, pero la pantalla donde el
personal hace el trabajo del día a día nunca la consulta.** Plantillas de
presupuesto, catálogo de vencimientos, suplencias, motor de estados,
fecha de pago de un documento (1.5): cada uno es una funcionalidad de
"configurar/registrar la regla" completa y correcta en sí misma. Ninguna
está conectada al lugar donde esa regla debería aplicarse en el momento de
hacer el trabajo real. La cobranza (1) es el caso más extremo de este
patrón porque ocurre en dos niveles a la vez: falta la pantalla que
consulte el saldo (como en los demás hallazgos), **y además falta el paso
en el propio proceso de negocio que decida cuándo y cómo se cobra** (1.1) —
no es solo que nadie mire el dato, es que el diagrama que define el
trabajo diario nunca previó que hubiera un dato que mirar. El hallazgo #9
es distinto en naturaleza: ahí no hay ni siquiera la mitad "configurar la
regla" — es una porción del negocio que todavía no se empezó a construir.
Descuentos y recargos (1.4) son un tercer caso, más temprano aún: ni
siquiera están nombrados en la documentación de negocio existente.

## Qué no cubrió esta pasada

- Se revisaron los 118 RF del SRS (excluyendo los no funcionales), el
  diagrama de estados completo de una gestión, y los Casos de Uso de
  cobranza (CU15, CU47) y sus referencias cruzadas en detalle. El resto de
  los 78+ CU se comparó a nivel de título/intención contra lo alcanzable
  hoy, sin releer línea por línea cada criterio de aceptación, actor o
  flujo alternativo.
- No se evaluaron los reportes/DDJJ en detalle más allá de confirmar que
  existen y son alcanzables (aparecen correctos a nivel de negocio).
- No se relevó el módulo de auditoría de usuarios (CU23) más allá de
  confirmar que la pantalla existe.

---
*Generado vía `/opsx:explore`, alcance de negocio solamente. Construido sobre
`openspec/explore_functional_report.md` (relevamiento técnico-funcional del
2026-08-11), con una segunda pasada el 2026-08-19 centrada en el circuito
completo de pagos (cuotas, descuentos, recargos, costos adicionales) y en
el diagrama de estados de una gestión. Triage de los 13 hallazgos originales
consolidado en este mismo documento el 2026-08-21 (antes en
`explore_1.1_issues.md` y `explore_2-9_issues.md`, ambos retirados). Tercera
pasada el 2026-08-21, revisión de código del motor de workflow contra el
diagrama de estados y la pantalla principal — agregó el hallazgo 10. Cuarta
consolidación el 2026-08-26: se fusionó de vuelta el triage del último
sub-hallazgo de 1.2 (`explore_1.2_overpayment_issue.md`, Issue #848, ya con
`/opsx:propose` completo) y se retiró ese archivo — este documento es la
única fuente de trazabilidad hallazgo↔Issue del proyecto.*
