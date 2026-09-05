# Notaire — Índice de Hallazgos de Negocio

> Índice único de trazabilidad hallazgo↔Issue. Regla del proyecto: todo
> hallazgo de negocio debe tener un Issue de GitHub asociado, sin
> excepciones (`CONSTITUTION.md` §4). **La descripción detallada, criterios
> de aceptación y notas técnicas de cada hallazgo viven en su Issue de
> GitHub** — este archivo es solo el índice resumido; no duplicar
> contenido acá.
>
> Método: cruce de los 78+ Casos de Uso (`docs/100-business/102-use-cases/`)
> contra los 95 Requerimientos Funcionales del SRS (`requerimientos.csv`),
> el diagrama de estados del ciclo de vida de una gestión
> (`docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml`)
> y una validación end-to-end de que una gestión puede iniciarse y
> completarse usando los módulos hoy implementados. Seis pasadas
> acumuladas desde 2026-08-11 hasta 2026-09-05 (historial de pasadas al
> final del archivo).

## Pendientes (abiertos)

| Título | Descripción breve | Issue | Criticidad |
|---|---|---|---|
| Recibo de pago nunca se emite | Paso final de CU15 (entregar comprobante al cliente) sin ninguna forma de imprimirse o emitirse | [#23](https://github.com/matiaspakua/notaire/issues/23) | Alta |
| `DELETE` silencioso en ~30 entidades | `Persistable.isNew()` mal inferido sobre `version` primitivo hace que varias entidades no se borren pese a devolver `200 OK` | [#957](https://github.com/matiaspakua/notaire/issues/957) | Alta |
| Presupuestar sin plantillas ni catálogo de ítems | Plantillas de precio y catálogo de ítems existen pero no se usan al armar un presupuesto real | [#834](https://github.com/matiaspakua/notaire/issues/834) | Media |
| Suplencias sin efecto práctico en gestiones | Registrar una suplencia no redirige casos nuevos al escribano suplente | [#836](https://github.com/matiaspakua/notaire/issues/836) | Media |
| Protocolo notarial no se puede armar desde el sistema | Sin acción para vincular escritura↔folio ni copia↔testimonio | [#838](https://github.com/matiaspakua/notaire/issues/838) | Media |
| Workflow tracker no modela el bucle de reingreso de testimonio | El motor de workflow representa un único "estado actual"; no puede capturar reintentos de inscripción | [#841](https://github.com/matiaspakua/notaire/issues/841) | Media |
| CU84/CSV sin trazabilidad RF↔GitHub ID | `CU84 - Login.md` no sigue el template estándar; 2 filas del CSV de requerimientos sin `GitHub_ID` válido | [#956](https://github.com/matiaspakua/notaire/issues/956) | Baja |
| Change de OpenSpec huérfano sin limpiar | `gestion-archive-deuda-check` quedó sin archivar tras revertirse su código bajo #914 | [#169](https://github.com/matiaspakua/notaire/issues/169) | Baja |

## Resueltos (mergeados y archivados)

| Título | Descripción breve | Issue | Criticidad |
|---|---|---|---|
| Proceso de negocio no verificaba deuda al archivar | `GestionArchiveDebtService` agregado al archivado de gestión | [#819](https://github.com/matiaspakua/notaire/issues/819) | Alta |
| Persona duplicada sin validación | Sin chequeo de tipo+número de documento repetido al dar de alta cliente | [#835](https://github.com/matiaspakua/notaire/issues/835) | Alta |
| Circuito legal post-firma sin pantalla | Firma, testimonio, verificación, inscripción y retiro (CU06–CU12) sin UI | [#832](https://github.com/matiaspakua/notaire/issues/832) | Alta |
| Historial/motor de estados sin conectar a la pantalla real | Transiciones de gestión no validaban contra `WorkflowDefinition`; sin bitácora | [#833](https://github.com/matiaspakua/notaire/issues/833) | Alta |
| Bloque completo del SRS sin desarrollo (protocolo) | Cuadernos, carpetas de trámite, protocolo auxiliar, minuta de inscripción, numeración correlativa — ~20 RF sin ningún desarrollo | [#839](https://github.com/matiaspakua/notaire/issues/839) | Alta |
| Escritura sin picker de folio | Bloqueaba la firma de escritura y la demo E2E completa | [#892](https://github.com/matiaspakua/notaire/issues/892) | Alta |
| `GET /gestiones/cliente/{id}` devolvía lista vacía hardcodeada | Encontrado en auditoría de suite Playwright | [#943](https://github.com/matiaspakua/notaire/issues/943) | Alta |
| Cobranza sin resumen a nivel de gestión | Pago se registraba contra presupuesto suelto, sin resumen por caso | [#820](https://github.com/matiaspakua/notaire/issues/820) | Media |
| Método de pago no persistido | "Efectivo"/"transferencia" se perdía al guardar el pago | [#792](https://github.com/matiaspakua/notaire/issues/792) | Media |
| Sin tope que impida cobrar de más | Único chequeo real era `monto <= 0`, sin validar contra saldo pendiente | [#848](https://github.com/matiaspakua/notaire/issues/848) | Media |
| Saldo pendiente no visible al cobrar | CU15/CU47 exigen mostrar saldo antes de cobrar; no existía | [#796](https://github.com/matiaspakua/notaire/issues/796) | Media |
| Pagos parciales/en cuotas sin circuito | RF-22 exige cobro en cuotas y advertencia de deuda al cerrar; no existía ninguno de los dos | [#821](https://github.com/matiaspakua/notaire/issues/821) | Media |
| Descuentos y recargos sin motivo estructurado | Sin forma de marcar un ítem como descuento/recargo con motivo | [#822](https://github.com/matiaspakua/notaire/issues/822) | Media |
| Costos de documentos sin conexión al presupuesto | `importeAPagar` de `DocumentoPresentado` no sumaba a ningún total/saldo | [#823](https://github.com/matiaspakua/notaire/issues/823) | Media |
| Tipos de documento sin reglas propias (vencimiento) | Pantalla solo permitía cargar el nombre; sin vencimiento, plazo ni responsable | [#837](https://github.com/matiaspakua/notaire/issues/837) | Media |
| `Historial.fecha` con precisión de fecha-solo | Rompía el orden de la bitácora cuando dos eventos ocurrían el mismo día | [#946](https://github.com/matiaspakua/notaire/issues/946) | Media |
| Columnas DNI/CUIT de personas siempre vacías | Mismatch de nombre de campo entre backend y tipo del frontend | [#939](https://github.com/matiaspakua/notaire/issues/939) | Media |
| Falta campo "observaciones" en formulario de escritura | Impedía justificar saltos en la numeración correlativa | [#950](https://github.com/matiaspakua/notaire/issues/950) | Media |
| Demo E2E — tipo de Inmueble mal mapeado | Bloqueaba Caso B de la demo end-to-end | [#879](https://github.com/matiaspakua/notaire/issues/879) | Media |
| Demo E2E — asociación de presupuesto rota | Bloqueaba flujo completo de demo | [#883](https://github.com/matiaspakua/notaire/issues/883) | Media |
| Demo E2E — labels de pickers inconsistentes | Bloqueaba selección en formularios de la demo | [#889](https://github.com/matiaspakua/notaire/issues/889) | Baja |

## Historial de pasadas

1. **2026-08-11** — relevamiento técnico-funcional inicial (`explore_functional_report.md`).
2. **2026-08-19** — pasada exhaustiva de cobranza (pagos parciales, descuentos, recargos, costos adicionales) y diagrama de estados de una gestión.
3. **2026-08-21** — triage de los 13 hallazgos originales consolidado en este documento; revisión del motor de workflow contra el diagrama de estados y la pantalla principal (agregó el hallazgo del bucle de reingreso, hoy #841).
4. **2026-08-26** — consolidación del sub-hallazgo de sobrepago (#848) de vuelta en este documento.
5. **2026-09-05** — verificación completa de `backend-api/` y `frontend/` contra `main`; confirmó la resolución de la mayoría de los hallazgos de cobranza y del bloque de protocolo; agregó el hallazgo del change de OpenSpec huérfano (#169) y el de `DELETE` silencioso transversal (#957).
6. **2026-09-05** — validación de trazabilidad RF↔CU (95/95 Requerimientos Funcionales cubiertos, con la excepción documental de CU84/#956) y validación end-to-end de que una gestión puede iniciarse y completarse hoy usando pantallas reales para cada paso del ciclo de vida (CU02–CU16), no solo API. Reescritura completa de este archivo a formato de índice (contenido detallado migrado a cada Issue de GitHub).
