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
  #796, abierto)*
- **No existe tope que impida cobrar de más.** El sistema no rechaza (ni
  advierte) un pago que exceda lo adeudado — un pago mal tipeado se acepta
  igual que uno correcto, sin que nadie se entere en el momento.
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
  cliente y el personal piensan el trámite. *(Issue #820, abierto — spec en
  progreso: `openspec/changes/payment-financial-tracking`)*

### 1.3 Pagos parciales / en cuotas: el SRS los exige y no existe ningún circuito *(Issue #821, abierto — bloqueado por #820)*

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

### 1.4 Descuentos y recargos: no existen ni como concepto, tampoco en el SRS *(Issue #822, abierto — bloqueado por #820)*

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

### 1.5 Costos adicionales de documentos (sellos, impuestos): se puede marcar "pagado" sin monto ni conexión al presupuesto *(Issue #823, abierto — bloqueado por #820)*

- El SRS pide que la plantilla de presupuesto contemple "gastos fijos y
  variables como impuestos y sellos" (RF-06) y que el seguimiento de
  documentos informe sobre deudas y vencimientos asociados, avisando si una
  deuda fue cancelada y el documento liberado, o si sigue pendiente de pago
  o retiro (RF-19).
- El catálogo de tipos de documento sí registra, a nivel de dato, si un
  documento tiene fecha de pago — pero **ese dato es una fecha suelta, sin
  monto ni vínculo con el presupuesto o los pagos de la gestión**. Se puede
  marcar "pagado el 12/03" para un certificado, pero ese pago no suma al
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

## 8. El protocolo notarial no se puede armar desde el sistema

- Folios y escrituras se administran cada uno por su lado (altas, búsquedas,
  numeración) y funcionan bien de forma aislada.
- **Pero no hay ninguna acción para vincular una escritura con el folio que
  ocupa, ni una copia con el testimonio del que proviene.** El protocolo —el
  registro oficial y de cumplimiento regulatorio que dice "esta escritura
  vive en tal folio, esta copia salió de tal testimonio"— no tiene ningún
  camino para armarse a través del producto. Cada pieza existe; el ensamblaje
  entre ellas no.

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

---

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
el diagrama de estados de una gestión.*
