# Triage — Hallazgos 2 a 9 (`explore.md`)

> Fuente: `openspec/explore.md`, secciones 2 a 9.
> Fecha: 2026-08-20
> Modo: **draft candidate list** — pendiente de confirmación del usuario antes
> de crear ningún issue en GitHub.

## Candidato 1 — Circuito legal posterior a la firma: firmar, testimoniar, verificar, presentar a inscripción, retirar (finding 2)

- **Caso de Uso**: CU06 – Firmar escritura (#159); CU07 – Generar testimonio
  (#160); CU08 – Verificar Testimonio (#161); CU11 – Ingresar para inscripción
  (#164); CU12 – Retirar testimonio (#165); CU44 – Reingresar testimonio
  (#197)
- **RF**: RF-27 (firmar escritura), RF-31 (generar testimonio), RF-30/RF-92
  (presentar/seguir inscripción), RF-32 (registrar testimonios inscriptos),
  RF-33 (retirar/reingresar testimonio), RF-94 (emitir copia impresa)
- **Tamaño**: L · **Prioridad**: `priority:high`
- **Descripción**: Ninguno de los pasos posteriores a preparar una escritura
  tiene pantalla hoy — firmar, generar testimonio, verificarlo, presentar y
  seguir la inscripción registral, retirar/reingresar el testimonio, emitir
  copia impresa. Es la mitad del trámite con efectos legales frente al
  registro de la propiedad, y vive hoy fuera del sistema.
- **Nota de alcance**: son 6 pasos secuenciales de un mismo circuito
  regulatorio. Se agrupan en un único hallazgo porque `explore.md` los
  documenta como una sola cadena, pero al pasar a `/opsx:propose` puede
  convenir dividir en fases (firma+testimonio / inscripción / retiro-copia)
  para no bloquear todo el circuito en un solo cambio.

## Candidato 2 — Historial de gestión vacío, estados sin reglas y archivado no reconocible (finding 3)

- **Caso de Uso**: CU13 – Ver historial de gestión (#166); CU83 – Definir
  Workflow de Estados y Transiciones (#451, #453, #454, #455); CU16 –
  Archivar Gestión (#169, #819)
- **RF**: RF-24 (historial/bitácora de gestión), sin RF explícito para la
  validación de transiciones de estado (ver `explore.md` §3)
- **Tamaño**: L · **Prioridad**: `priority:high`
- **Descripción**: (a) la bitácora de una gestión nunca se escribe desde el
  flujo real de alta/edición — queda vacía para cualquier caso real; (b) el
  motor de definición de workflows (CU83) existe pero la pantalla real de
  gestión no lo consulta, así que cualquier estado puede pasar a cualquier
  otro sin restricción; (c) archivar una gestión (CU16) no es una acción
  reconocible, solo un cambio de estado manual sin las reglas de (b).
- **Nota de alcance**: son tres gaps relacionados (bitácora, motor de
  transiciones, archivado) que comparten la misma raíz — la pantalla de
  gestión no consulta las reglas ya modeladas. Evaluar si conviene un solo
  cambio (conectar CU83 + bitácora a la pantalla de gestión) o dos.

## Candidato 3 — Presupuestar no usa plantillas ni catálogo de ítems (finding 4)

- **Caso de Uso**: CU39 – Crear Plantilla Presupuesto (#192); CU55 –
  Modificar Plantilla Presupuesto (#208); CU49 – Eliminar Plantilla
  Presupuesto (#202); CU71 – Gestión de Items (#300)
- **RF**: RF-04, RF-64 a RF-67 (plantillas de presupuesto por tipo de
  trámite), RF-07 (agregar ítems adicionales)
- **Tamaño**: M · **Prioridad**: `priority:medium`
- **Descripción**: existen plantillas de precios por tipo de trámite y un
  catálogo de ítems reutilizables, pero cargar un presupuesto real hoy es
  escribir un monto único a mano — no hay selector de tipo de trámite que
  traiga la plantilla ni forma de agregar ítems del catálogo. Dos personas
  cotizando el mismo tipo de trámite no tienen fuente común de precio.

## Candidato 4 — Sin validación de cliente duplicado al dar de alta (finding 5)

- **Caso de Uso**: CU17 – Dar Alta persona (#170); CU18 – Dar Alta Cliente
  (#171)
- **RF**: RF-37
- **Tamaño**: S · **Prioridad**: `priority:high`
- **Descripción**: no hay validación que impida cargar dos veces a la misma
  persona (mismo tipo y número de documento) al dar de alta un cliente. El
  impacto es directo: presupuestos, gestiones y pagos quedan repartidos entre
  dos fichas sin vínculo, sin ningún aviso.

## Candidato 5 — Suplencias registradas sin efecto práctico en la asignación de gestiones (finding 6)

- **Caso de Uso**: CU22 – Registrar Suplencia (#175); CU59 – Consultar
  Suplencias (#212); CU48 – Dar alta escribano (#201); CU51 – Modificar
  escribano (#204)
- **RF**: RF-87 a RF-90 (suplencias), RF-88 (registrar suplentes del
  escribano)
- **Tamaño**: M · **Prioridad**: `priority:medium`
- **Descripción**: se puede registrar una suplencia (pantalla completa y
  funcional), pero nada la usa — al crear una gestión el escribano
  responsable se asigna directamente, sin revisar si tiene una suplencia
  activa que debería redirigir el caso (RF-89). Además, dar de alta o
  modificar la credencial de escribano de una persona no tiene lugar
  dedicado en la pantalla de personas.

## Candidato 6 — Tipos de documento no pueden cargar vencimiento ni responsable, bloqueando el aviso de vencimientos (finding 7)

- **Caso de Uso**: CU27 – Ingresar nuevo tipo de documento (#180); CU32 –
  Modificar tipo de documento (#185); CU42 – Informar próximos vencimientos
  (#195)
- **RF**: RF-55 (catálogo de tipos de documento), RF-16 (informar próximos
  vencimientos)
- **Tamaño**: S · **Prioridad**: `priority:medium`
- **Descripción**: el catálogo de tipos de documento modela si un documento
  vence, cuántos días dura y quién es responsable de entregarlo/devolverlo,
  pero la pantalla de administración solo permite cargar el nombre. Como
  consecuencia directa, "informar próximos vencimientos" (CU42) es
  estructuralmente imposible porque el dato del que depende nunca se carga.

## Candidato 7 — No hay forma de vincular una escritura a su folio, ni una copia a su testimonio (finding 8)

- **Caso de Uso**: CU87 – Vincular Escritura a Folio y Copia a Testimonio
  (**nuevo**, creado en esta triage); CU28 – Ingresar nuevos folios (#181);
  CU05 – Preparar escritura (#158); CU07 – Generar testimonio (#160); CU80 –
  Administrar Cuadernos de Folios (#311)
- **RF**: RF #94 (Administrar folios), RF #96 (Control de numeración
  correlativa de folios), RF #121 (Control de numeración de escrituras)
- **Tamaño**: M · **Prioridad**: `priority:medium`
- **Descripción**: folios, cuadernos y escrituras se administran cada uno
  por su lado; no existe ninguna acción que registre que una escritura
  ocupa determinado folio, ni que una copia/testimonio proviene de
  determinada escritura. CU87 (nuevo) documenta esa acción de ensamblaje,
  que es el protocolo notarial propiamente dicho.
- **Documento creado**: `docs/100-business/102-use-cases/CU87 – Vincular
  Escritura a Folio y Copia a Testimonio.md`

## Candidato 8 — Bloque del SRS sin desarrollo: cuadernos, carpetas de trámite, protocolo auxiliar, minuta de inscripción, control de numeración de escrituras (finding 9, RF-74 a RF-95)

- **Caso de Uso**: CU80 – Administrar Cuadernos de Folios (#311, Cuadernos);
  CU85 – Administrar Carpetas de Trámite (**nuevo**, Carpetas de Trámite);
  CU81 – Gestión de Trámites en Protocolo Auxiliar (#312, Protocolo
  Auxiliar); CU82 – Generar Minuta de Inscripción (#313, Minuta de
  Inscripción); CU86 – Controlar Numeración Correlativa de Escrituras
  (**nuevo**, Control de numeración de escrituras)
- **RF**: RF-74 a RF-77 (cuadernos), RF-78 a RF-80 / #104-106 (carpetas de
  trámite), RF-85/86/93 (protocolo auxiliar), RF-91 (minuta de inscripción),
  RF-95 / #121 (control de numeración de escrituras). La numeración
  correlativa de **folios** (RF-70/71/72, #96/97/98) queda fuera de este
  candidato porque ya está cubierta por CU28/CU80.
- **Tamaño**: L · **Prioridad**: `priority:high`
- **Descripción**: bloque completo de ~20 RF sin backend ni pantalla — a
  diferencia de los demás hallazgos, aquí no hay ni siquiera una
  administración aislada que funcione sola. Con CU85 y CU86 creados, las 5
  áreas de negocio del hallazgo quedan con Caso de Uso propio.
- **Documentos creados**: `docs/100-business/102-use-cases/CU85 –
  Administrar Carpetas de Trámite.md`, `docs/100-business/102-use-cases/CU86
  – Controlar Numeración Correlativa de Escrituras.md`

---

## Estado

| Candidato | Issue GitHub | Estado |
|---|---|---|
| 1. Circuito legal post-firma | [#832](https://github.com/matiaspakua/notaire/issues/832) | propose completo — `openspec/changes/escritura-post-firma-legal-cycle/` |
| 2. Historial/estados/archivado de gestión | [#833](https://github.com/matiaspakua/notaire/issues/833) | propose completo — `openspec/changes/gestion-workflow-y-bitacora/` |
| 3. Presupuestar sin plantillas ni ítems | [#834](https://github.com/matiaspakua/notaire/issues/834) | propose completo — `openspec/changes/presupuesto-plantillas-y-catalogo-items/` |
| 4. Cliente duplicado sin validación | [#835](https://github.com/matiaspakua/notaire/issues/835) | propose completo — `openspec/changes/persona-validacion-duplicados/` |
| 5. Suplencias sin efecto práctico | [#836](https://github.com/matiaspakua/notaire/issues/836) | propose completo — `openspec/changes/suplencia-efecto-en-gestiones/` |
| 6. Tipos de documento sin reglas propias | [#837](https://github.com/matiaspakua/notaire/issues/837) | propose completo — `openspec/changes/tipo-documento-vencimiento-config/` |
| 7. Vincular escritura↔folio, copia↔testimonio | [#838](https://github.com/matiaspakua/notaire/issues/838) | propose completo — `openspec/changes/folio-vinculacion-escritura/` |
| 8. Bloque SRS sin desarrollo (RF-74 a RF-95) | [#839](https://github.com/matiaspakua/notaire/issues/839) | propose completo — 5 cambios independientes: `openspec/changes/protocolo-cuadernos-de-folios/`, `openspec/changes/protocolo-carpetas-de-tramite/`, `openspec/changes/protocolo-auxiliar-tramites/`, `openspec/changes/protocolo-minuta-inscripcion/`, `openspec/changes/protocolo-numeracion-escrituras/` |

Con esto, los 8 candidatos de este reporte tienen su propose completo y validado (`scripts/validate-sdlc-plan.sh`). Próximo paso: `/opsx:apply` change por change, siguiendo `docs/000-archive/engineering-loop.md` y `CONSTITUTION.md`.
