# Sistema de Gestión Notarial — Notaire

## Diccionario de Datos (Modelo Relacional en 3ra Forma Normal)

**Versión:** 2.4  
**Motor de Base de Datos:** PostgreSQL 16  
**Mecanismo de Migración:** Flyway (V1 a V14)  
**Fecha de actualización:** 18 de Agosto de 2026  
**Estado:** Sincronizado con el esquema PostgreSQL activo, las migraciones Flyway vigentes y la modelización JPA del backend. Auditoría exhaustiva completada (2026-08-18): 32/32 tablas Flyway documentadas, 61 FKs coherentes, `identificaciones` (heredada) archivada. Incluye la corrección de cardinalidad Presupuesto–Trámite (V14), workflows (V7/V8) y roles/permisos (V9).

---

## 1. Introducción y Arquitectura del Modelo de Datos

El presente **Diccionario de Datos** documenta formalmente la totalidad de las tablas, columnas, tipos de datos, restricciones y relaciones de clave foránea que componen la base de datos relacional del sistema **Notaire**.

### Principios de Normalización y Evolución del Esquema:
1. **Tercera Forma Normal (3FN):**  
   - Desacoplamiento de identificaciones civiles/tributarias (`identificaciones`, `tipos_identificacion`).
   - Eliminación de dependencias transitivas en líneas presupuestarias y documentales: los atributos maestros residen en `conceptos` y `tipos_de_documento`, manteniendo en `items` y `documentos_presentados` únicamente los valores efectivos y atestados de la transacción.
2. **Subflujo de Workflows Notariales (V7, V8):**  
   - Tablas `workflow_definition`, `workflow_node` y `workflow_transition` para modelar y ejecutar máquinas de estados dinámicas sobre `estados_de_gestion` y asociarlas a `tipos_de_tramite`.
3. **Control de Acceso Basado en Roles (V9):**  
   - Modelo de seguridad ampliado con tablas `roles` y `roles_permisos`, vinculando cada usuario con un rol granular (`fk_id_rol`).
4. **Resolución de Cardinalidad Presupuesto–Trámite (V14):**  
   - Eliminación de la clave foránea circular en `presupuestos` (`fk_id_tramite` deprecada y eliminada en V14); la relación canónica es `tramites.fk_id_presupuesto` (1:N, donde un presupuesto puede originar o abarcar trámites vinculados).
5. **Alineación de Movimientos de Testimonio y Folios (V3, V4, V5, V6, V13):**  
   - Identificador `id_movimiento_testimonio`, fechas de tracto registral, y soporte para documentos autónomos (`documentos_presentados.fk_id_tramite` nullable).

---

## 2. Índice General de Tablas (32 tablas, Flyway V1–V14)

**Nota sobre Entidades Heredadas:** La entidad `identificaciones` se documentó en versiones previas pero nunca fue materializada en las migraciones Flyway activas (V1–V14). Existe solo en los scripts archivados de inicialización (`docs/archive/init-db/`) y fue utilizada en el modelo JPA legacy. Se considera un componente de normalización 3FN planificado pero no implementado. Véase la sección "Notas Técnicas" al pie para detalles.

| Nº | Tabla | Paquete / Módulo | Tipo Entidad | Descripción |
|---|---|---|---|---|
| 1 | [conceptos](#1-conceptos) | Presupuestos | Fuerte | Catálogo maestro de honorarios, aranceles y sellados |
| 2 | [copias](#2-copias) | Protocolos | Débil | Ejemplares impresos y certificados de testimonios |
| 3 | [documentos_presentados](#3-documentos_presentados) | Documentación | Débil | Documentos y certificados tramitados por gestión o autónomos |
| 4 | [escrituras](#4-escrituras) | Protocolos | Fuerte | Escrituras públicas matrices otorgadas en protocolos |
| 5 | [estados_de_gestion](#5-estados_de_gestion) | Gestión Notarial | Fuerte | Catálogo maestro de estados del ciclo notarial |
| 6 | [folios](#6-folios) | Protocolos | Fuerte | Hojas de protocolo numeradas provistas por el Colegio |
| 7 | [folios_copias](#7-folios_copias) | Protocolos | Asociativa | Relación M:N entre folios especiales y copias emitidas |
| 8 | [gestiones_de_escrituras](#8-gestiones_de_escrituras) | Gestión Notarial | Fuerte | Carpetas de gestión y expedientes de trámites |
| 9 | [historial](#9-historial) | Gestión Notarial | Débil | Trazabilidad y auditoría de cambios de estado de gestiones |
| 10 | [inmuebles](#10-inmuebles) | Gestión Notarial | Fuerte | Bienes inmuebles y especificaciones catastrales |
| 11 | [items](#11-items) | Presupuestos | Débil | Desglose arancelario de líneas de cada presupuesto |
| 12 | [movimientos_testimonio](#12-movimientos_testimonio) | Protocolos | Débil | Asientos de presentación y tracto registral ante el Registro |
| 13 | [pagos](#13-pagos) | Presupuestos | Débil | Recibos de cobro y entregas dinerarias a cuenta |
| 14 | [personas](#14-personas) | Sujetos | Fuerte | Sujetos de derecho (clientes, escribanos, otorgantes) |
| 15 | [plantilla_presupuestos](#15-plantilla_presupuestos) | Presupuestos | Asociativa | Conceptos arancelarios sugeridos por tipo de trámite |
| 16 | [plantilla_tramites](#16-plantilla_tramites) | Documentación | Asociativa | Requisitos documentales obligatorios por tipo de trámite |
| 17 | [presupuestos](#17-presupuestos) | Presupuestos | Fuerte | Cotización económica que fundamenta el trámite |
| 18 | [registro_auditoria](#18-registro_auditoria) | Seguridad | Débil | Bitácora de auditoría de transacciones de usuarios |
| 19 | [roles](#19-roles) | Seguridad | Fuerte | Perfiles y roles de seguridad en el sistema |
| 20 | [roles_permisos](#20-roles_permisos) | Seguridad | Asociativa | Permisos funcionales asignados a cada rol |
| 21 | [suplencias](#21-suplencias) | Sujetos | Asociativa | Períodos de suplencia y licencias notariales |
| 22 | [testimonios](#22-testimonios) | Protocolos | Débil | Testimonios notariales expedidos de escrituras matrices |
| 23 | [tipos_de_documento](#23-tipos_de_documento) | Documentación | Fuerte | Catálogo maestro de tipos de documento y certificados |
| 24 | [tipos_de_folio](#24-tipos_de_folio) | Protocolos | Fuerte | Clasificación de hojas de protocolo |
| 25 | [tipos_de_tramite](#25-tipos_de_tramite) | Gestión Notarial | Fuerte | Catálogo maestro de actos jurídicos notariales |
| 26 | [tipos_identificacion](#26-tipos_identificacion) | Sujetos | Fuerte | Catálogo de tipos de documento de identidad |
| 27 | [tramites](#27-tramites) | Gestión Notarial | Fuerte | Instancia particular de acto notarial en ejecución |
| 28 | [tramites_personas](#28-tramites_personas) | Gestión Notarial | Asociativa | Personas intervinientes y sus roles jurídicos |
| 29 | [usuarios](#29-usuarios) | Seguridad | Débil / Fuerte | Cuentas de acceso y credenciales de operadores |
| 30 | [workflow_definition](#30-workflow_definition) | Workflow | Fuerte | Definición maestra de grafos de flujos de trabajo |
| 31 | [workflow_node](#31-workflow_node) | Workflow | Débil | Nodos de estado dentro de un flujo de trabajo |
| 32 | [workflow_transition](#32-workflow_transition) | Workflow | Débil | Transiciones dirigidas y reglas de guarda entre estados |

---

## 3. Sincronización con Flyway y Compensación

### Revisión del esquema activo

La base de datos actual refleja la evolución real del sistema a través de Flyway V1–V14. Los cambios relevantes para la integridad del modelo son:

- V1: esquema base relacional con entidades de sujetos, protocolo, trámites, presupuestos y documentación.
- V3/V4: se corrigen columnas faltantes en `items`, `tipos_de_folio` y `testimonios`.
- V5: se normaliza `movimientos_testimonio` para que coincida con el nombre de la entidad JPA (`id_movimiento_testimonio`, `fecha_ingreso`, `inscripta`, `numero_carton`).
- V6: `documentos_presentados.fk_id_tramite` pasa a ser opcional para soportar documentos autónomos.
- V7/V8: se incorporan `workflow_definition`, `workflow_node`, `workflow_transition` y la referencia desde `tipos_de_tramite` al workflow.
- V9: se incorporan `roles` y `roles_permisos`, y se enlaza `usuarios` con `fk_id_rol`.
- V13: `tramites.nombre` y `tramites.numero` pasan a ser opcionales para coincidir con las entidades de negocio.
- V14: se elimina la FK redundante `presupuestos.fk_id_tramite`; la relación canónica quedó en `tramites.fk_id_presupuesto` (1:N).

### Matriz de entidades, PK/FK y mecanismo de compensación

| Entidad | PK principal | FK relevantes | Mecanismo de compensación | Observación de sincronía |
|---|---|---|---|---|
| `conceptos` | `id_concepto` | — | Sin compensación | Tabla maestra, no depende de otras entidades |
| `copias` | `id_copia` | `fk_id_testimonio`, `fk_id_persona` | `I: Impedir`, `M: Impedir`, `B: Impedir` (RESTRICT por defecto) | Efectúa copias de testimonios |
| `documentos_presentados` | `id_documento_presentado` | `fk_id_tramite`, `fk_id_tipo_documento` | `I: Null` si no hay trámite, `M: Impedir`, `B: Impedir` | Compatible con V6: trámite opcional |
| `escrituras` | `id_escritura` | — | Sin compensación | Matriz protocolares |
| `estados_de_gestion` | `id_estado_gestion` | — | Sin compensación | Catálogo de estados |
| `folios` | `id_folio` | `fk_id_escritura`, `fk_id_tipo_folio`, `fk_id_persona_escribano` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Folio del protocolo |
| `folios_copias` | `fk_id_folio + fk_id_copia` | `fk_id_folio`, `fk_id_copia` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Tabla asociativa |
| `gestiones_de_escrituras` | `id_gestion` | `fk_id_persona_escribano`, `fk_id_estado_de_gestion` | `I: Impedir`, `M: Impedir`, `B: Impedir` / `SET NULL` en estado si se deja nulo | Agrupa trámites |
| `historial` | `id_historial` | `fk_id_gestion`, `fk_id_estado_gestion` | `I: Impedir`, `M: Impedir`, `B: Cascada` en gestión | Histórico de estados |
| `inmuebles` | `id_inmueble` | — | Sin compensación | Bien inmueble |
| `items` | `id_item` | `fk_id_presupuesto` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Límite de presupuesto |
| `movimientos_testimonio` | `id_movimiento_testimonio` | `fk_id_testimonio` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Corresponde a V5 |
| `pagos` | `id_pago` | `fk_id_presupuesto` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Liquidación de cobros |
| `personas` | `id_persona` | `fk_id_tipo_identificacion` | `I: Null` si corresponde, `M: Impedir`, `B: Impedir` | Entidad central del sistema |
| `plantilla_presupuestos` | `fk_id_tipo_tramite + fk_id_concepto` | `fk_id_tipo_tramite`, `fk_id_concepto` | `I: Impedir`, `M: Impedir`, `B: Cascada` en `conceptos` | Plantilla arancelaria |
| `plantilla_tramites` | `fk_id_tipo_tramite + fk_id_tipo_documento` | `fk_id_tipo_tramite`, `fk_id_tipo_documento` | `I: Impedir`, `M: Impedir`, `B: Cascada` en documento | Requisitos documentales |
| `presupuestos` | `id_presupuesto` | `fk_id_persona` | `I: Impedir`, `M: Impedir`, `B: Impedir` | La FK a trámite fue removida en V14 |
| `registro_auditoria` | `id_registro_auditoria` | `fk_id_usuario` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Auditoría de transacciones |
| `roles` | `id_rol` | — | Sin compensación | Catálogo de perfiles |
| `roles_permisos` | `fk_id_rol + modulo` | `fk_id_rol` | `I: Impedir`, `M: Impedir`, `B: Cascada` | V9; permisos por módulo |
| `suplencias` | `id_suplencia` | `fk_id_suplantado`, `fk_id_suplente` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Cobertura de escribanos |
| `testimonios` | `id_testimonio` | `fk_id_escritura` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Testimonio generado desde escritura |
| `tipos_de_documento` | `id_tipo_documento` | — | Sin compensación | Catálogo maestra de documentos |
| `tipos_de_folio` | `id_tipo_folio` | — | Sin compensación | Catálogo maestra de folios |
| `tipos_de_tramite` | `id_tipo_tramite` | `fk_workflow_definition_id` | `I: Null`, `M: Impedir`, `B: Impedir` (por defecto, no cascade) | V8: workflow opcional |
| `tipos_identificacion` | `id_tipo_identificacion` | — | Sin compensación | Catálogo de documentos de identidad |
| `tramites` | `id_tramite` | `fk_id_tipo_tramite`, `fk_id_gestion`, `fk_id_escritura`, `fk_id_presupuesto`, `fk_id_inmueble` | `I: Impedir` / `Null` según columna, `M: Impedir`, `B: Impedir` o `SET NULL` según caso | Relación canónica con presupuesto en V14 |
| `tramites_personas` | `fk_id_tramite + fk_id_persona_cliente` | `fk_id_tramite`, `fk_id_persona_cliente` | `I: Impedir`, `M: Impedir`, `B: Cascada` en trámite | Tabla asociativa de participación |
| `usuarios` | `id_usuario` | `fk_id_persona`, `fk_id_rol` | `I: Impedir` / `Null`, `M: Impedir`, `B: Impedir` | Acceso y autenticación |
| `workflow_definition` | `id_workflow_definition` | — | Sin compensación | Definición del grafo de estados |
| `workflow_node` | `id_workflow_node` | `fk_workflow_definition_id`, `fk_estado_gestion_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` en workflow | Nodos del flujo |
| `workflow_transition` | `id_workflow_transition` | `fk_workflow_definition_id`, `fk_nodo_origen_id`, `fk_nodo_destino_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Transiciones del flujo |

> La regla general del esquema vigente es que la mayoría de las relaciones usan restricciones por defecto de PostgreSQL (RESTRICT / NO ACTION), y solo se habilitan compensaciones explícitas con ON DELETE CASCADE o columnas anulables cuando la migración lo define.

## 4. Especificación Detallada de Tablas

### 1. `conceptos`
Catálogo maestro de conceptos arancelarios, honorarios profesionales, aportes y tasas notariales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_concepto` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del concepto arancelario |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista (Hibernate) |
| `nombre` | TEXT | No | No | Sí | — | — | Denominación del concepto (e.g., Honorarios, Aporte Caja Notarial) |
| `valor` | REAL | No | No | Sí | 0.0 | — | Importe fijo de referencia en moneda de curso legal |
| `porcentaje` | INTEGER | No | No | Sí | 0 | — | Porcentaje estándar aplicable sobre el monto del acto |
| `habilitado` | BOOLEAN | No | No | Sí | true | — | Indica si el concepto está disponible para presupuestos |
| `concepto_fijo` | BOOLEAN | No | No | Sí | true | — | `true` si es importe fijo, `false` si es liquidación porcentual |

---

### 2. `copias`
Ejemplares impresos en hojas especiales expedidos a partir de un testimonio notarial matriz.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_copia` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del ejemplar de copia |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número correlativo de copia emitida |
| `fecha_impresion` | DATE | No | No | Sí | — | — | Fecha de expedición e impresión de la copia |
| `fecha_retiro` | DATE | No | No | No | NULL | — | Fecha en que fue retirada por el interesado |
| `observaciones` | TEXT | No | No | No | NULL | — | Registro de entrega o atestaciones |
| `fk_id_testimonio` | INTEGER | No | Sí | Sí | — | `testimonios(id_testimonio)` | Testimonio matriz originario |
| `fk_id_persona` | INTEGER | No | Sí | Sí | — | `personas(id_persona)` | Persona a quien se le expide o entrega la copia |

---

### 3. `documentos_presentados`
Documentos, constancias y certificados gestionados para un trámite o generados de manera autónoma (V6).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_documento_presentado` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la pieza documental |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre descriptivo del documento presentado |
| `numero_carton` | INTEGER | No | No | No | NULL | — | Número de cartón de mesa de entrada de organismo externo |
| `fecha_ingreso` | DATE | No | No | No | NULL | — | Fecha en que se ingresó o solicitó ante organismo externo |
| `fecha_salida` | DATE | No | No | No | NULL | — | Fecha de devolución por parte del organismo |
| `preparado` | BOOLEAN | No | No | Sí | false | — | `true` si fue confeccionado en la escribanía |
| `vence` | BOOLEAN | No | No | Sí | false | — | Indica si el documento está sujeto a caducidad |
| `fecha_vencimiento` | DATE | No | No | No | NULL | — | Fecha límite de validez legal |
| `dias_vencimiento` | INTEGER | No | No | No | NULL | — | Plazo de vigencia en días |
| `importe_a_pagar` | REAL | No | No | No | NULL | — | Monto real de sellado o tasa liquidada |
| `fecha_pago` | DATE | No | No | No | NULL | — | Fecha de efectivización del pago de la tasa |
| `liberado` | BOOLEAN | No | No | Sí | false | — | `true` si se emitió constancia de libre deuda |
| `fecha_liberado` | DATE | No | No | No | NULL | — | Fecha de acreditación de liberación |
| `observado` | BOOLEAN | No | No | Sí | false | — | `true` si el organismo formuló observaciones técnicas |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas técnicas o requisitos de subsanación |
| `entregado` | BOOLEAN | No | No | No | false | — | `true` si ya fue entregado a la entidad requirente |
| `reingresado` | BOOLEAN | No | No | No | false | — | `true` si fue reingresado tras subsanar (V6 nullable) |
| `quien_entrega` | TEXT | No | No | Sí | — | — | Origen de provisión (`Cliente` o `Entidad Externa`) |
| `fk_id_tramite` | INTEGER | No | Sí | No | NULL | `tramites(id_tramite)` | Trámite al que pertenece (V6: opcional) |
| `fk_id_tipo_documento` | INTEGER | No | Sí | No | NULL | `tipos_de_documento(id_tipo_documento)` | Tipo de documento maestro |

---

### 4. `escrituras`
Documento formal matriz otorgado en el protocolo notarial debidamente protocolizado.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_escritura` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador interno de la escritura matriz |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número correlativo anual de escritura dentro del protocolo |
| `fecha_escrituracion` | DATE | No | No | Sí | — | — | Fecha de otorgamiento y celebración del acto |
| `cuerpo` | TEXT | No | No | Sí | — | — | Texto legal íntegro de la escritura protocolar |
| `estado` | TEXT | No | No | Sí | — | — | Estado de la escritura (`Preparada`, `Firmada`, `No Pasó`, `Errose`) |
| `matricula_inscripcion`| TEXT | No | No | No | NULL | — | Matrícula registral otorgada por el Registro |
| `fecha_inscripcion` | DATE | No | No | No | NULL | — | Fecha de inscripción definitiva |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas marginales y atestados notariales |

---

### 5. `estados_de_gestion`
Catálogo de estados operacionales por los que puede transitar una gestión notarial.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_estado_gestion` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del estado de gestión |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre del estado (e.g., `Generado`, `En proceso`, `Listo para firmar`) |
| `observaciones` | TEXT | No | No | No | NULL | — | Definición funcional y condiciones |

---

### 6. `folios`
Hojas protocolares provistas por el Colegio Notarial para asentar escrituras públicas.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_folio` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del folio notarial |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número oficial correlativo impreso en el folio |
| `anio` | INTEGER | No | No | Sí | — | — | Año calendario del protocolo correspondiente |
| `estado` | TEXT | No | No | Sí | — | — | Estado (`Disponible`, `Usado`, `Errose`, `No Pasó`) |
| `observaciones` | TEXT | No | No | No | NULL | — | Justificación de contingencias o atestados |
| `fk_id_persona_escribano`| INTEGER | No | Sí | Sí | — | `personas(id_persona)` | Escribano responsable titular del registro |
| `fk_id_tipo_folio` | INTEGER | No | Sí | Sí | — | `tipos_de_folio(id_tipo_folio)` | Clasificación de uso del folio |
| `fk_id_escritura` | INTEGER | No | Sí | No | NULL | `escrituras(id_escritura)` | Escritura en la que fue utilizado |

---

### 7. `folios_copias`
Tabla asociativa que vincula las hojas de testimonio con las copias expedidas.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_folio` | INTEGER | Sí | Sí | Sí | — | `folios(id_folio)` | Folio especial de testimonio |
| `fk_id_copia` | INTEGER | Sí | Sí | Sí | — | `copias(id_copia)` | Copia expedida |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |

---

### 8. `gestiones_de_escrituras`
Expediente o carpeta física que agrupa uno o varios trámites notariales afines.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_gestion` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la gestión |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número correlativo anual de la gestión |
| `fecha_inicio` | DATE | No | No | Sí | — | — | Fecha de apertura de la carpeta |
| `encabezado` | TEXT | No | No | Sí | — | — | Carátula descriptiva del objeto de la gestión |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas operativas de tramitación |
| `numero_bibliorato` | INTEGER | No | No | No | NULL | — | Número de bibliorato físico de guarda |
| `numero_archivo` | INTEGER | No | No | No | NULL | — | Número correlativo de archivo final otorgado al archivar |
| `fk_id_persona_escribano`| INTEGER | No | Sí | Sí | — | `personas(id_persona)` | Escribano a cargo de la gestión |
| `fk_id_estado_de_gestion`| INTEGER | No | Sí | No | NULL | `estados_de_gestion(id_estado_gestion)` | Estado operativo consolidado |

---

### 9. `historial`
Registro histórico cronológico de las transiciones de estado de una gestión.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_historial` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del registro histórico |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fecha` | DATE | No | No | Sí | CURRENT_DATE | — | Fecha del cambio de estado |
| `observaciones` | TEXT | No | No | No | NULL | — | Motivo o atestación del cambio de estado |
| `fk_id_gestion` | INTEGER | No | Sí | Sí | — | `gestiones_de_escrituras(id_gestion)` | Gestión que transitó de estado |
| `fk_id_estado_gestion`| INTEGER | No | Sí | Sí | — | `estados_de_gestion(id_estado_gestion)` | Estado alcanzado |

---

### 10. `inmuebles`
Bienes inmuebles y sus determinaciones catastrales y registrales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_inmueble` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del inmueble |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nomenclatura` | TEXT | No | No | No | NULL | — | Nomenclatura catastral oficial |
| `matricula` | TEXT | No | No | No | NULL | — | Matrícula del Registro de la Propiedad |
| `valuacion_anio` | INTEGER | No | No | No | NULL | — | Año de la valuación fiscal |
| `valuacion_fiscal` | REAL | No | No | No | NULL | — | Monto fiscal oficial de tasación |
| `partida` | INTEGER | No | No | No | NULL | — | Número de partida inmobiliaria |
| `circunscripcion` | TEXT | No | No | No | NULL | — | Circunscripción |
| `seccion` | TEXT | No | No | No | NULL | — | Sección |
| `zona` | TEXT | No | No | No | NULL | — | Zona |
| `manzana` | TEXT | No | No | No | NULL | — | Manzana |
| `parcela` | TEXT | No | No | No | NULL | — | Parcela |
| `poligono` | TEXT | No | No | No | NULL | — | Polígono |
| `unidad_funcional` | TEXT | No | No | No | NULL | — | Unidad funcional (PH) |
| `domicilio` | TEXT | No | No | No | NULL | — | Dirección física del inmueble |
| `localidad` | TEXT | No | No | No | NULL | — | Localidad de ubicación |
| `observaciones` | TEXT | No | No | No | NULL | — | Linderos y especificaciones |

---

### 11. `items`
Desglose arancelario de conceptos liquidados en un presupuesto (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_item` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del ítem |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Concepto o partida liquidada |
| `valor` | REAL | No | No | Sí | 0.0 | — | Monto resultante de la línea |
| `porcentaje` | INTEGER | No | No | Sí | 0 | — | Porcentaje aplicado en la liquidación |
| `concepto_fijo` | BOOLEAN | No | No | Sí | true | — | `true` si es importe fijo, `false` si es porcentual |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas justificativas de la partida (V3/V4) |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `presupuestos(id_presupuesto)` | Presupuesto al que pertenece |

---

### 12. `movimientos_testimonio`
Registro del tracto y asientos de presentación registral del testimonio (V5).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_movimiento_testimonio`| SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del movimiento registral (V5) |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fecha_ingreso` | DATE | No | No | Sí | — | — | Fecha de ingreso del movimiento (V5) |
| `fecha_salida` | DATE | No | No | No | NULL | — | Fecha de devolución del Registro (V5) |
| `fecha_inscripcion`| DATE | No | No | No | NULL | — | Fecha en que se perfeccionó la inscripción (V5) |
| `inscripta` | BOOLEAN | No | No | Sí | false | — | `true` si la inscripción resultó favorable (V5) |
| `numero_carton` | INTEGER | No | No | Sí | 0 | — | Número de cartón de presentación (V5) |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas y despachos registrales |
| `fk_id_testimonio` | INTEGER | No | Sí | No | NULL | `testimonios(id_testimonio)` | Testimonio objeto del trámite |

---

### 13. `pagos`
Recibos de cobro imputados a un presupuesto notarial.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_pago` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del recibo |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fecha` | DATE | No | No | Sí | — | — | Fecha de realización del pago |
| `monto` | REAL | No | No | Sí | 0.0 | — | Importe percibido |
| `observaciones` | TEXT | No | No | No | NULL | — | Medio de pago y constancias |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `presupuestos(id_presupuesto)` | Presupuesto cancelado |

---

### 14. `personas`
Entidad unificada para personas humanas y jurídicas que intervienen en la escribanía.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_persona` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la persona |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombres de pila o razón social |
| `apellido` | TEXT | No | No | Sí | — | — | Apellido o denominación societaria |
| `numero_identificacion`| TEXT | No | No | Sí | — | — | Identificación civil principal |
| `cuit` | TEXT | No | No | No | NULL | — | Clave tributaria CUIT / CUIL |
| `sexo` | TEXT | No | No | No | NULL | — | Sexo / Género legal |
| `fecha_nacimiento` | DATE | No | No | No | NULL | — | Fecha de nacimiento |
| `estado_civil` | TEXT | No | No | No | NULL | — | Estado civil |
| `numero_nupcias` | INTEGER | No | No | No | NULL | — | Número de nupcias si contrajo matrimonio |
| `ocupacion` | TEXT | No | No | No | NULL | — | Ocupación laboral |
| `profesion` | TEXT | No | No | No | NULL | — | Profesión u oficio |
| `domicilio` | TEXT | No | No | No | NULL | — | Domicilio real / legal |
| `localidad` | TEXT | No | No | No | NULL | — | Localidad |
| `provincia` | TEXT | No | No | No | NULL | — | Provincia |
| `telefono` | TEXT | No | No | No | NULL | — | Teléfono fijo |
| `celular` | TEXT | No | No | No | NULL | — | Teléfono celular |
| `email` | TEXT | No | No | No | NULL | — | Correo electrónico principal |
| `e_mail` | TEXT | No | No | No | NULL | — | Correo electrónico secundario/histórico |
| `registro_escribano`| INTEGER | No | No | No | NULL | — | Número de Registro Notarial (escribanos) |
| `es_escribano` | BOOLEAN | No | No | Sí | false | — | `true` si es escribano habilitado |
| `es_cliente` | BOOLEAN | No | No | Sí | false | — | `true` si es cliente de la escribanía |
| `observaciones` | TEXT | No | No | No | NULL | — | Legajo y antecedentes de la persona |
| `fk_id_tipo_identificacion`| INTEGER | No | Sí | No | NULL | `tipos_identificacion(id_tipo_identificacion)` | Tipo de identificación principal |

---

### 15. `plantilla_presupuestos`
Tabla asociativa M:N que parametriza conceptos arancelarios estándar por tipo de trámite.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_tipo_tramite` | INTEGER | Sí | Sí | Sí | — | `tipos_de_tramite(id_tipo_tramite)` | Tipo de trámite |
| `fk_id_concepto` | INTEGER | Sí | Sí | Sí | — | `conceptos(id_concepto)` | Concepto presupuestado |
| `observaciones` | TEXT | No | No | No | NULL | — | Reglas particulares de cómputo |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |

---

### 16. `plantilla_tramites`
Tabla asociativa M:N que estipula los requisitos documentales y certificados por tipo de trámite.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_tipo_tramite` | INTEGER | Sí | Sí | Sí | — | `tipos_de_tramite(id_tipo_tramite)` | Tipo de trámite |
| `fk_id_tipo_documento`| INTEGER | Sí | Sí | Sí | — | `tipos_de_documento(id_tipo_documento)` | Documento/Certificado requerido |
| `observaciones` | TEXT | No | No | No | NULL | — | Instrucciones específicas de presentación |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |

---

### 17. `presupuestos`
Cotización arancelaria emitida a un cliente. En V14 se eliminó la FK redundante hacia trámite (`fk_id_tramite`), estableciendo que la relación canónica es `tramites.fk_id_presupuesto`.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_presupuesto` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del presupuesto |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número correlativo del presupuesto |
| `fecha` | DATE | No | No | Sí | — | — | Fecha de emisión |
| `encabezado` | TEXT | No | No | Sí | — | — | Título descriptivo de la cotización |
| `observaciones` | TEXT | No | No | No | NULL | — | Condiciones y plazos de validez |
| `estado` | TEXT | No | No | Sí | 'Emitido' | — | Estado (`Borrador`, `Emitido`, `Aceptado`, `Abonado`, `Cancelado`) |
| `monto_inmueble` | REAL | No | No | No | NULL | — | Base imponible inmobiliaria informada |
| `fk_id_persona` | INTEGER | No | Sí | No | NULL | `personas(id_persona)` | Cliente solicitante |

---

### 18. `registro_auditoria`
Log no repudiable de eventos de seguridad y transacciones de negocio.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_registro_auditoria` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del evento auditado |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fecha` | TIMESTAMP | No | No | Sí | CURRENT_TIMESTAMP | — | Marca temporal exacta de la transacción |
| `modulo` | TEXT | No | No | Sí | — | — | Módulo funcional afectado |
| `detalle_operacion` | TEXT | No | No | Sí | — | — | Detalle de datos modificados o acción ejecutada |
| `fk_id_usuario` | INTEGER | No | Sí | No | NULL | `usuarios(id_usuario)` | Operador responsable de la acción |

---

### 19. `roles`
Catálogo de roles de seguridad del sistema (introducido en Flyway V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_rol` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del rol |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre único del rol (`ADMIN`, `ESCRIBANO`, `SECRETARIA`, etc.) |
| `descripcion` | TEXT | No | No | No | NULL | — | Alcances y responsabilidades del rol |
| `activo` | BOOLEAN | No | No | Sí | true | — | Indica si el rol está habilitado |

---

### 20. `roles_permisos`
Permisos por módulo asignados a cada rol (introducido en Flyway V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_rol` | INTEGER | Sí | Sí | Sí | — | `roles(id_rol)` | Rol al cual se le concede el permiso |
| `modulo` | TEXT | Sí | No | Sí | — | — | Identificador del módulo o acción autorizada |

---

### 21. `suplencias`
Designación de suplencias y coberturas de licencias entre escribanos.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_suplencia` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la suplencia |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fecha_inicio` | DATE | No | No | Sí | — | — | Fecha inicial de la suplencia |
| `fecha_fin` | DATE | No | No | No | NULL | — | Fecha de finalización |
| `observaciones` | TEXT | No | No | No | NULL | — | Motivo o atestación legal |
| `fk_id_suplantado` | INTEGER | No | Sí | No | NULL | `personas(id_persona)` | Escribano titular bajo licencia |
| `fk_id_suplente` | INTEGER | No | Sí | No | NULL | `personas(id_persona)` | Escribano suplente interviniente |

---

### 22. `testimonios`
Testimonios solemnes expedidos de escrituras públicas matrices (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_testimonio` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del testimonio |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | Sí | — | — | Número de testimonio (1º, 2º testimonio) |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas registrales o de entrega |
| `fecha_inscripcion`| DATE | No | No | No | NULL | — | Fecha de registración |
| `fecha_retiro` | DATE | No | No | No | NULL | — | Fecha de entrega al cliente |
| `fecha_ingreso_libro`| DATE | No | No | No | NULL | — | Fecha de asiento en libro |
| `numero_carpeta` | INTEGER | No | No | No | NULL | — | Número de carpeta registral |
| `numero_expediente`| INTEGER | No | No | No | NULL | — | Número de expediente del Registro |
| `observado` | BOOLEAN | No | No | Sí | false | — | `true` si fue observado (V3/V4) |
| `reingresado` | BOOLEAN | No | No | Sí | false | — | `true` si fue reingresado |
| `fk_id_escritura` | INTEGER | No | Sí | No | NULL | `escrituras(id_escritura)` | Escritura matriz de origen |

---

### 23. `tipos_de_documento`
Catálogo maestro de documentos, títulos antecedentes y certificados registrales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_tipo_documento` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Denominación del documento/certificado |
| `devuelto` | BOOLEAN | No | No | Sí | false | — | `true` si el original debe restituirse al cliente |
| `vence` | BOOLEAN | No | No | Sí | false | — | `true` si el certificado tiene vigencia temporal |
| `dias_vencimiento` | INTEGER | No | No | No | NULL | — | Plazo legal de vigencia en días |
| `importe_a_pagar` | REAL | No | No | No | NULL | — | Tasa arancelaria estándar |
| `habilitado` | BOOLEAN | No | No | Sí | true | — | `true` si está activo para plantillas |
| `quien_entrega` | TEXT | No | No | Sí | — | — | Origen (`Cliente` o `Entidad Externa`) |

---

### 24. `tipos_de_folio`
Catálogo maestro de tipos de folios notariales (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_tipo_folio` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de folio |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre (`Protocolo Principal`, `Protocolo Auxiliar`, `Testimonio`) |
| `observaciones` | TEXT | No | No | No | NULL | — | Notas del tipo de folio (V3/V4) |
| `habilitado` | BOOLEAN | No | No | Sí | true | — | `true` si está activo para tandas de folios (V3/V4) |

---

### 25. `tipos_de_tramite`
Catálogo maestro de actos jurídicos notariales (V8).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_tipo_tramite` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del tipo de trámite |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Denominación del trámite (e.g., `Compraventa`, `Poder`) |
| `observaciones` | TEXT | No | No | No | NULL | — | Normativa y descripción notarial |
| `habilitado` | BOOLEAN | No | No | Sí | true | — | `true` si permite nuevas gestiones |
| `se_archiva` | BOOLEAN | No | No | Sí | true | — | `true` si requiere archivo físico con bibliorato |
| `se_inscribe` | BOOLEAN | No | No | Sí | false | — | `true` si requiere inscripción registral |
| `asocia_inmuebles` | BOOLEAN | No | No | Sí | false | — | `true` si involucra inmuebles |
| `fk_workflow_definition_id`| INTEGER | No | Sí | No | NULL | `workflow_definition(id_workflow_definition)` | Workflow asignado al tipo de trámite (V8) |

---

### 26. `tipos_identificacion`
Catálogo maestro de documentos de identidad reconocidos.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_tipo_identificacion`| SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre (`DNI`, `CUIT`, `CUIL`, `Pasaporte`, `CI`, `LC`, `LE`) |
| `caracteres` | TEXT | No | No | Sí | — | — | Formato o máscara alfanumérica |

---

### 27. `tramites`
Instancia particular de trámite o negocio jurídico (V13/V14).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_tramite` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del trámite |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `numero` | INTEGER | No | No | No | NULL | — | Número correlativo de trámite (V13 opcional) |
| `nombre` | TEXT | No | No | No | NULL | — | Carátula descriptiva (V13 opcional) |
| `observaciones` | TEXT | No | No | No | NULL | — | Instrucciones u observaciones |
| `fk_id_tipo_tramite` | INTEGER | No | Sí | No | NULL | `tipos_de_tramite(id_tipo_tramite)` | Tipo de trámite |
| `fk_id_gestion` | INTEGER | No | Sí | No | NULL | `gestiones_de_escrituras(id_gestion)` | Gestión que lo agrupa (nulo en aux.) |
| `fk_id_escritura` | INTEGER | No | Sí | No | NULL | `escrituras(id_escritura)` | Escritura notarial resultante |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `presupuestos(id_presupuesto)` | Presupuesto económico base (V14) |
| `fk_id_inmueble` | INTEGER | No | Sí | No | NULL | `inmuebles(id_inmueble)` | Inmueble objeto del acto (si aplica) |

---

### 28. `tramites_personas`
Tabla asociativa que vincula personas con el trámite e indica su rol jurídico.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_tramite` | INTEGER | Sí | Sí | Sí | — | `tramites(id_tramite)` | Trámite en el que participa |
| `fk_id_persona_cliente`| INTEGER | Sí | Sí | Sí | — | `personas(id_persona)` | Persona que interviene |
| `observaciones` | TEXT | No | No | Sí | '' | — | Rol notarial (`Comprador`, `Vendedor`, `Donante`, `Apoderado`) |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |

---

### 29. `usuarios`
Cuentas de operadores del sistema (V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_usuario` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del usuario |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre de usuario (login único) |
| `contrasenia` | TEXT | No | No | Sí | — | — | Hash seguro de la contraseña |
| `tipo` | TEXT | No | No | Sí | — | — | Rol descriptivo legacy |
| `estado` | BOOLEAN | No | No | Sí | true | — | `true` si la cuenta está activa |
| `fk_id_persona` | INTEGER | No | Sí | No | NULL | `personas(id_persona)` | Persona física asociada |
| `fk_id_rol` | INTEGER | No | Sí | No | NULL | `roles(id_rol)` | Rol de seguridad asignado (V9) |

---

### 30. `workflow_definition`
Definición de grafos de flujos de trabajo notariales (introducido en Flyway V7).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_workflow_definition`| SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del workflow |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `nombre` | TEXT | No | No | Sí | — | — | Nombre descriptivo del workflow |
| `descripcion` | TEXT | No | No | No | NULL | — | Descripción funcional del proceso |
| `activo` | BOOLEAN | No | No | Sí | false | — | `true` si el workflow está habilitado para uso |

---

### 31. `workflow_node`
Nodos del grafo de workflow vinculados a estados de gestión (introducido en Flyway V7).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_workflow_node` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del nodo |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fk_workflow_definition_id`| INTEGER | No | Sí | Sí | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_estado_gestion_id` | INTEGER | No | Sí | Sí | — | `estados_de_gestion(id_estado_gestion)` | Estado de gestión representado por el nodo |
| `tipo` | TEXT | No | No | Sí | — | — | Tipo de nodo: `INITIAL`, `INTERMEDIATE`, `FINAL` |
| `posicion_x` | REAL | No | No | No | NULL | — | Coordenada visual X en el diagramador |
| `posicion_y` | REAL | No | No | No | NULL | — | Coordenada visual Y en el diagramador |

---

### 32. `workflow_transition`
Transiciones dirigidas entre nodos de workflow con condiciones de guarda (introducido en Flyway V7).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_workflow_transition`| SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la transición |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fk_workflow_definition_id`| INTEGER | No | Sí | Sí | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_nodo_origen_id` | INTEGER | No | Sí | Sí | — | `workflow_node(id_workflow_node)` | Nodo origen (ON DELETE CASCADE) |
| `fk_nodo_destino_id` | INTEGER | No | Sí | Sí | — | `workflow_node(id_workflow_node)` | Nodo destino (ON DELETE CASCADE) |
| `condicion` | TEXT | No | No | No | NULL | — | Condición lógica de habilitación de la transición |
| `descripcion` | TEXT | No | No | No | NULL | — | Descripción de la acción que dispara el avance |

---

## 4. Matriz de Integridad Referencial Consolidada

| Tabla Origen | Columna FK | Tabla Destino | Columna PK | Acción ON DELETE |
|---|---|---|---|---|
| `identificaciones` | `fk_id_tipo_identificacion` | `tipos_identificacion` | `id_tipo_identificacion` | RESTRICT |
| `identificaciones` | `fk_id_persona` | `personas` | `id_persona` | CASCADE |
| `usuarios` | `fk_id_persona` | `personas` | `id_persona` | RESTRICT |
| `usuarios` | `fk_id_rol` | `roles` | `id_rol` | SET NULL |
| `roles_permisos` | `fk_id_rol` | `roles` | `id_rol` | CASCADE |
| `registro_auditoria` | `fk_id_usuario` | `usuarios` | `id_usuario` | RESTRICT |
| `suplencias` | `fk_id_suplantado` | `personas` | `id_persona` | RESTRICT |
| `suplencias` | `fk_id_suplente` | `personas` | `id_persona` | RESTRICT |
| `workflow_node` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_node` | `fk_estado_gestion_id` | `estados_de_gestion` | `id_estado_gestion` | RESTRICT |
| `workflow_transition` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_transition` | `fk_nodo_origen_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `workflow_transition` | `fk_nodo_destino_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `tipos_de_tramite` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | SET NULL |
| `gestiones_de_escrituras` | `fk_id_persona_escribano` | `personas` | `id_persona` | RESTRICT |
| `gestiones_de_escrituras` | `fk_id_estado_de_gestion` | `estados_de_gestion` | `id_estado_gestion` | SET NULL |
| `historial` | `fk_id_gestion` | `gestiones_de_escrituras` | `id_gestion` | CASCADE |
| `historial` | `fk_id_estado_gestion` | `estados_de_gestion` | `id_estado_gestion` | RESTRICT |
| `plantilla_tramites` | `fk_id_tipo_tramite` | `tipos_de_tramite` | `id_tipo_tramite` | CASCADE |
| `plantilla_tramites` | `fk_id_tipo_documento` | `tipos_de_documento` | `id_tipo_documento` | CASCADE |
| `plantilla_presupuestos` | `fk_id_tipo_tramite` | `tipos_de_tramite` | `id_tipo_tramite` | CASCADE |
| `plantilla_presupuestos` | `fk_id_concepto` | `conceptos` | `id_concepto` | CASCADE |
| `tramites` | `fk_id_tipo_tramite` | `tipos_de_tramite` | `id_tipo_tramite` | RESTRICT |
| `tramites` | `fk_id_gestion` | `gestiones_de_escrituras` | `id_gestion` | SET NULL |
| `tramites` | `fk_id_escritura` | `escrituras` | `id_escritura` | SET NULL |
| `tramites` | `fk_id_presupuesto` | `presupuestos` | `id_presupuesto` | RESTRICT |
| `tramites` | `fk_id_inmueble` | `inmuebles` | `id_inmueble` | SET NULL |
| `tramites_personas` | `fk_id_tramite` | `tramites` | `id_tramite` | CASCADE |
| `tramites_personas` | `fk_id_persona_cliente` | `personas` | `id_persona` | RESTRICT |
| `documentos_presentados` | `fk_id_tramite` | `tramites` | `id_tramite` | CASCADE |
| `documentos_presentados` | `fk_id_tipo_documento` | `tipos_de_documento` | `id_tipo_documento` | RESTRICT |
| `presupuestos` | `fk_id_persona` | `personas` | `id_persona` | RESTRICT |
| `items` | `fk_id_presupuesto` | `presupuestos` | `id_presupuesto` | CASCADE |
| `pagos` | `fk_id_presupuesto` | `presupuestos` | `id_presupuesto` | CASCADE |
| `folios` | `fk_id_persona_escribano` | `personas` | `id_persona` | RESTRICT |
| `folios` | `fk_id_tipo_folio` | `tipos_de_folio` | `id_tipo_folio` | RESTRICT |
| `folios` | `fk_id_escritura` | `escrituras` | `id_escritura` | SET NULL |
| `testimonios` | `fk_id_escritura` | `escrituras` | `id_escritura` | CASCADE |
| `movimientos_testimonio` | `fk_id_testimonio` | `testimonios` | `id_testimonio` | CASCADE |
| `copias` | `fk_id_testimonio` | `testimonios` | `id_testimonio` | CASCADE |
| `copias` | `fk_id_persona` | `personas` | `id_persona` | RESTRICT |
| `folios_copias` | `fk_id_folio` | `folios` | `id_folio` | RESTRICT |
| `folios_copias` | `fk_id_copia` | `copias` | `id_copia` | CASCADE |

---

## 6. Notas Técnicas

### 6.1 Entidad Heredada: `identificaciones`

**Estado:** Archivada, no materializada en Flyway V1–V14.

La entidad `identificaciones` fue documentada y existe en los scripts de inicialización heredados (`docs/archive/init-db/orig/01_initial_schema.sql`), pero nunca fue creada mediante las migraciones Flyway. Esta tabla era una propuesta de normalización 3FN para permitir múltiples documentos de identidad por persona (DNI, CUIT, Pasaporte, etc.).

**Decisión:** En la fase de migración a Flyway (V1–V14), se abandonó esta normalización en favor de la simplificación operacional: cada persona mantiene un único `numero_identificacion` y `cuit` denormalizado en la tabla `personas`, y se utiliza `fk_id_tipo_identificacion` para clasificar el tipo de identificación principal. Esta decisión priorizó la coherencia con el código JPA moderno y la experiencia usuario sobre la normalización teórica.

**Alternativas Futuras:**
- **(A) Crear V15:** Materializar `identificaciones` como tabla asociativa M:N si se requiere 3FN puro y soporte para múltiples identificaciones.
- **(B) Mantener como está:** Conservar la denormalización en `personas` como decisión de diseño aceptada.

### 6.2 Entidades de Apoyo (Supporting Entities) sin Caso de Uso Independiente

Las siguientes 19 entidades (59% de la base de datos) no poseen un Caso de Uso independiente. Se clasifican como **entidades maestras, plantillas o de compensación** que se crean y modifican indirectamente dentro de los flujos principales:

| Categoría | Entidades | Observación |
|-----------|-----------|-------------|
| **Maestros/Catálogos** | `tipos_de_documento`, `tipos_de_folio`, `tipos_de_tramite`, `tipos_identificacion`, `conceptos`, `estados_de_gestion` | Se crean vía CRUD administrativo, referenciados por CUs de negocio. |
| **Plantillas** | `plantilla_presupuestos`, `plantilla_tramites` | Se definen una única vez y reutilizan en múltiples CUs (presupuestación, documentación). |
| **Compensación/Seguridad** | `roles`, `roles_permisos`, `registro_auditoria`, `usuarios`, `workflow_definition`, `workflow_node`, `workflow_transition` | Se crean durante instalación/configuración del sistema o automáticamente por auditoría/workflows. |
| **Asociativas Operacionales** | `documentos_presentados`, `tramites_personas`, `folios_copias`, `movimientos_testimonio`, `suplencias`, `historial`, `inmuebles`, `items`, `pagos` | Tablas débiles/asociativas creadas como parte de CUs que gestionan entidades fuertes (trámites, escrituras, presupuestos). |

**Patrón de Cobertura:**
- 13 entidades **fuertes** (41%) poseen CUs explícitas.
- 19 entidades **de apoyo/asociativas** (59%) son creadas por las 13 CUs principales o durante operaciones administrativas.

Este patrón es esperado en sistemas notariales donde la mayoría del trabajo se concentra en trámites, escrituras y presupuestos, mientras que los catálogos y configuración son actividades de administración de bajo volumen.

### 6.3 Coherencia Flyway ↔ Documentación ↔ Use Cases

**Validación 2026-08-18:**
- ✅ 32/32 tablas Flyway presentes en Diccionario de Datos.
- ✅ 61 Foreign Keys documentadas correctamente.
- ✅ Cardinalidad V14 (presupuesto-trámite) reflejada en Diccionario.
- ✅ Workflows (V7/V8) y RBAC (V9) presentes y coherentes.
- ⚠️  1 entidad heredada (`identificaciones`) archivada; decisión de diseño documentada.
- ℹ️  19 entidades sin CU independiente; clasificadas como "supporting" (normal para dominios notariales).

**Mantenimiento:** Cada migración Flyway debe ir acompañada de actualización simultánea en este Diccionario en el mismo commit. Auditoría trimestral recomendada para detectar drift.

---

**Versión actual:** 2.4 (revisada 2026-08-18, sección 6 añadida, renumeración y coherencia Flyway verificadas, identificaciones archivada).
