# Sistema de Gestión Notarial — Notaire

## Diccionario de Datos (Modelo Relacional en 3ra Forma Normal)

**Versión:** 3.0  
**Motor de Base de Datos:** PostgreSQL 16  
**Mecanismo de Migración:** Flyway (V1 a V41)  
**Fecha de actualización:** 4 de Octubre de 2026  
**Estado:** Sincronizado con el esquema PostgreSQL activo, las migraciones Flyway vigentes y la modelización JPA del backend. Regenerado desde el esquema migrado (#1222): 36/36 tablas Flyway documentadas, 49 FKs coherentes, nombres de columna actuales; `identificaciones` (heredada) archivada. Incluye la corrección de cardinalidad Presupuesto–Trámite (V14), workflows (V7/V8) y roles/permisos (V9).

---

## 1. Introducción y Arquitectura del Modelo de Datos

El presente **Diccionario de Datos** documenta formalmente la totalidad de las tablas, columnas, tipos de datos, restricciones y relaciones de clave foránea que componen la base de datos relacional del sistema **Notaire**.

### Principios de Normalización y Evolución del Esquema

1. **Tercera Forma Normal (3FN):**  
   - Desacoplamiento de identificaciones civiles/tributarias (`identificaciones`, `identification_types`).
   - Eliminación de dependencias transitivas en líneas presupuestarias y documentales: los atributos maestros residen en `concepts` y `document_types`, manteniendo en `items` y `submitted_documents` únicamente los valores efectivos y atestados de la transacción.
2. **Subflujo de Workflows Notariales (V7, V8):**  
   - Tablas `workflow_definition`, `workflow_node` y `workflow_transition` para modelar y ejecutar máquinas de estados dinámicas sobre `management_statuses` y asociarlas a `procedure_types`.
3. **Control de Acceso Basado en Roles (V9):**  
   - Modelo de seguridad ampliado con tablas `roles` y `role_modules`, vinculando cada usuario con un rol granular (`fk_id_role`).
4. **Resolución de Cardinalidad Presupuesto–Trámite (V14):**  
   - Eliminación de la clave foránea circular en `budgets` (`fk_id_tramite` deprecada y eliminada en V14); la relación canónica es `procedures.fk_id_presupuesto` (1:N, donde un presupuesto puede originar o abarcar trámites vinculados).
5. **Alineación de Movimientos de Testimonio y Folios (V3, V4, V5, V6, V13):**  
   - Identificador `id`, fechas de tracto registral, y soporte para documentos autónomos (`submitted_documents.fk_id_tramite` nullable).

---

## 2. Índice General de Tablas (36 tablas, Flyway V1–V41)

**Nota sobre Entidades Heredadas:** La entidad `identificaciones` se documentó en versiones previas pero nunca fue materializada en las migraciones Flyway activas (V1–V41). Existe solo en los scripts archivados de inicialización (`docs/archive/init-db/`) y fue utilizada en el modelo JPA legacy. Se considera un componente de normalización 3FN planificado pero no implementado. Véase la sección "Notas Técnicas" al pie para detalles.

| Nº | Tabla | Paquete / Módulo | Tipo Entidad | Descripción |
|---|---|---|---|---|
| 1 | [concepts](#1-concepts) | Presupuestos | Fuerte | Catálogo maestro de honorarios, aranceles y sellados |
| 2 | [copies](#2-copies) | Protocolos | Débil | Ejemplares impresos y certificados de testimonios |
| 3 | [submitted_documents](#3-submitted_documents) | Documentación | Débil | Documentos y certificados tramitados por gestión o autónomos |
| 4 | [deeds](#4-deeds) | Protocolos | Fuerte | Escrituras públicas matrices otorgadas en protocolos |
| 5 | [management_statuses](#5-management_statuses) | Gestión Notarial | Fuerte | Catálogo maestro de estados del ciclo notarial |
| 6 | [folios](#6-folios) | Protocolos | Fuerte | Hojas de protocolo numeradas provistas por el Colegio |
| 7 | [folio_copies](#7-folio_copies) | Protocolos | Asociativa | Relación M:N entre folios especiales y copies emitidas |
| 8 | [deed_managements](#8-deed_managements) | Gestión Notarial | Fuerte | Carpetas de gestión y expedientes de trámites |
| 9 | [history](#9-history) | Gestión Notarial | Débil | Trazabilidad y auditoría de cambios de estado de gestiones |
| 10 | [properties](#10-properties) | Gestión Notarial | Fuerte | Bienes inmuebles y especificaciones catastrales |
| 11 | [items](#11-items) | Presupuestos | Débil | Desglose arancelario de líneas de cada presupuesto |
| 12 | [testimony_movements](#12-testimony_movements) | Protocolos | Débil | Asientos de presentación y tracto registral ante el Registro |
| 13 | [payments](#13-payments) | Presupuestos | Débil | Recibos de cobro y entregas dinerarias a cuenta |
| 14 | [people](#14-people) | Sujetos | Fuerte | Sujetos de derecho (clientes, escribanos, otorgantes) |
| 15 | [budget_templates](#15-budget_templates) | Presupuestos | Asociativa | Conceptos arancelarios sugeridos por tipo de trámite |
| 16 | [procedure_templates](#16-procedure_templates) | Documentación | Asociativa | Requisitos documentales obligatorios por tipo de trámite |
| 17 | [budgets](#17-budgets) | Presupuestos | Fuerte | Cotización económica que fundamenta el trámite |
| 18 | [audit_records](#18-audit_records) | Seguridad | Débil | Bitácora de auditoría de transacciones de usuarios |
| 19 | [roles](#19-roles) | Seguridad | Fuerte | Perfiles y roles de seguridad en el sistema |
| 20 | [role_modules](#20-role_modules) | Seguridad | Asociativa | Permisos funcionales asignados a cada rol |
| 21 | [substitutions](#21-substitutions) | Sujetos | Asociativa | Períodos de suplencia y licencias notariales |
| 22 | [testimonies](#22-testimonies) | Protocolos | Débil | Testimonios notariales expedidos de escrituras matrices |
| 23 | [document_types](#23-document_types) | Documentación | Fuerte | Catálogo maestro de tipos de documento y certificados |
| 24 | [folio_types](#24-folio_types) | Protocolos | Fuerte | Clasificación de hojas de protocolo |
| 25 | [procedure_types](#25-procedure_types) | Gestión Notarial | Fuerte | Catálogo maestro de actos jurídicos notariales |
| 26 | [identification_types](#26-identification_types) | Sujetos | Fuerte | Catálogo de tipos de documento de identidad |
| 27 | [procedures](#27-procedures) | Gestión Notarial | Fuerte | Instancia particular de acto notarial en ejecución |
| 28 | [person_procedures](#28-person_procedures) | Gestión Notarial | Asociativa | Personas intervinientes y sus roles jurídicos |
| 29 | [users](#29-users) | Seguridad | Débil / Fuerte | Cuentas de acceso y credenciales de operadores |
| 30 | [workflow_definition](#30-workflow_definition) | Workflow | Fuerte | Definición maestra de grafos de flujos de trabajo |
| 31 | [workflow_node](#31-workflow_node) | Workflow | Débil | Nodos de estado dentro de un flujo de trabajo |
| 32 | [workflow_transition](#32-workflow_transition) | Workflow | Débil | Transiciones dirigidas y reglas de guarda entre estados |
| 33 | [document_cost_templates](#33-document_cost_templates) | Documentación | Asociativa | Costo sugerido de cada documento por tipo de trámite |
| 34 | [notebooks](#34-notebooks) | Protocolos | Fuerte | Cuadernos anuales de protocolo de un escribano |
| 35 | [procedure_folders](#35-procedure_folders) | Gestión Notarial | Débil | Carpetas numeradas de los trámites de una gestión |
| 36 | [registration_drafts](#36-registration_drafts) | Protocolos | Débil | Borradores de inscripción registral de escrituras |

---

## 3. Sincronización con Flyway y Compensación

### Revisión del esquema activo

La base de datos actual refleja la evolución real del sistema a través de Flyway V1–V41. Los cambios relevantes para la integridad del modelo son:

- V1: esquema base relacional con entidades de sujetos, protocolo, trámites, presupuestos y documentación.
- V3/V4: se corrigen columnas faltantes en `items`, `folio_types` y `testimonies`.
- V5: se normaliza `testimony_movements` para que coincida con el nombre de la entidad JPA (`id`, `entry_date`, `registered`, `folder_number`).
- V6: `submitted_documents.fk_id_tramite` pasa a ser opcional para soportar documentos autónomos.
- V7/V8: se incorporan `workflow_definition`, `workflow_node`, `workflow_transition` y la referencia desde `procedure_types` al workflow.
- V9: se incorporan `roles` y `role_modules`, y se enlaza `users` con `fk_id_role`.
- V13: `procedures.nombre` y `procedures.numero` pasan a ser opcionales para coincidir con las entidades de negocio.
- V14: se elimina la FK redundante `budgets.fk_id_tramite`; la relación canónica quedó en `procedures.fk_id_presupuesto` (1:N).

### Matriz de entidades, PK/FK y mecanismo de compensación

| Entidad | PK principal | FK relevantes | Mecanismo de compensación | Observación de sincronía |
|---|---|---|---|---|
| `concepts` | `id` | — | Sin compensación | Tabla maestra, no depende de otras entidades |
| `copies` | `id` | `fk_id_testimonio`, `fk_id_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` (RESTRICT por defecto) | Efectúa copies de testimonios |
| `submitted_documents` | `id` | `fk_id_tramite`, `fk_id_document_type` | `I: Null` si no hay trámite, `M: Impedir`, `B: Impedir` | Compatible con V6: trámite opcional |
| `deeds` | `id` | — | Sin compensación | Matriz protocolares |
| `management_statuses` | `id` | — | Sin compensación | Catálogo de estados |
| `folios` | `id` | `fk_id_deed`, `fk_id_folio_type`, `fk_id_notary_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Folio del protocolo |
| `folio_copies` | `fk_id_folio + fk_id_copy` | `fk_id_folio`, `fk_id_copy` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Tabla asociativa |
| `deed_managements` | `id` | `fk_id_notary_person`, `fk_id_management_status` | `I: Impedir`, `M: Impedir`, `B: Impedir` / `SET NULL` en estado si se deja nulo | Agrupa trámites |
| `history` | `id` | `fk_id_deed_management`, `fk_id_management_status` | `I: Impedir`, `M: Impedir`, `B: Cascada` en gestión | Histórico de estados |
| `properties` | `id` | — | Sin compensación | Bien inmueble |
| `items` | `id` | `fk_id_presupuesto` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Límite de presupuesto |
| `testimony_movements` | `id` | `fk_id_testimonio` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Corresponde a V5 |
| `payments` | `id` | `fk_id_presupuesto` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Liquidación de cobros |
| `people` | `id` | `fk_id_tipo_identificacion` | `I: Null` si corresponde, `M: Impedir`, `B: Impedir` | Entidad central del sistema |
| `budget_templates` | `fk_id_procedure_type + fk_id_concept` | `fk_id_procedure_type`, `fk_id_concept` | `I: Impedir`, `M: Impedir`, `B: Cascada` en `concepts` | Plantilla arancelaria |
| `procedure_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Impedir`, `M: Impedir`, `B: Cascada` en documento | Requisitos documentales |
| `budgets` | `id` | `fk_id_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | La FK a trámite fue removida en V14 |
| `audit_records` | `id` | `fk_id_usuario` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Auditoría de transacciones |
| `roles` | `id` | — | Sin compensación | Catálogo de perfiles |
| `role_modules` | `fk_id_role + modulo` | `fk_id_role` | `I: Impedir`, `M: Impedir`, `B: Cascada` | V9; permisos por módulo |
| `substitutions` | `id` | `fk_id_substituted_person`, `fk_id_substitute_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Cobertura de escribanos |
| `testimonies` | `id` | `fk_id_deed` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Testimonio generado desde escritura |
| `document_types` | `id` | — | Sin compensación | Catálogo maestra de documentos |
| `folio_types` | `id` | — | Sin compensación | Catálogo maestra de folios |
| `procedure_types` | `id` | `fk_workflow_definition_id` | `I: Null`, `M: Impedir`, `B: Impedir` (por defecto, no cascade) | V8: workflow opcional |
| `identification_types` | `id` | — | Sin compensación | Catálogo de documentos de identidad |
| `procedures` | `id` | `fk_id_procedure_type`, `fk_id_gestion`, `fk_id_deed`, `fk_id_presupuesto`, `fk_id_property` | `I: Impedir` / `Null` según columna, `M: Impedir`, `B: Impedir` o `SET NULL` según caso | Relación canónica con presupuesto en V14 |
| `person_procedures` | `fk_id_procedure + fk_id_client_person` | `fk_id_procedure`, `fk_id_client_person` | `I: Impedir`, `M: Impedir`, `B: Cascada` en trámite | Tabla asociativa de participación |
| `users` | `id` | `fk_id_person`, `fk_id_role` | `I: Impedir` / `Null`, `M: Impedir`, `B: Impedir` | Acceso y autenticación |
| `workflow_definition` | `id_workflow_definition` | — | Sin compensación | Definición del grafo de estados |
| `workflow_node` | `id_workflow_node` | `fk_workflow_definition_id`, `fk_estado_gestion_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` en workflow | Nodos del flujo |
| `workflow_transition` | `id_workflow_transition` | `fk_workflow_definition_id`, `fk_nodo_origen_id`, `fk_nodo_destino_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Transiciones del flujo |
| `document_cost_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Costo sugerido por documento |
| `notebooks` | `id` | `fk_id_notary_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Cuaderno anual de protocolo |
| `procedure_folders` | `id` | `fk_id_gestion`, `fk_id_tramite` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Carpetas numeradas de trámites (CU85) |
| `registration_drafts` | `id` | `fk_id_escritura` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Borrador de inscripción registral |

> La regla general del esquema vigente es que la mayoría de las relaciones usan restricciones por defecto de PostgreSQL (RESTRICT / NO ACTION), y solo se habilitan compensaciones explícitas con ON DELETE CASCADE o columnas anulables cuando la migración lo define.

## 4. Especificación Detallada de Tablas

### 1. `concepts`

Catálogo maestro de conceptos arancelarios, honorarios profesionales, aportes y tasas notariales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del concepto arancelario |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista (Hibernate) |
| `name` | TEXT | No | No | Sí | — | — | Denominación del concepto (e.g., Honorarios, Aporte Caja Notarial) |
| `amount` | NUMERIC(19,2) | No | No | Sí | 0 | — | Importe fijo de referencia en moneda de curso legal |
| `percentage` | INTEGER | No | No | Sí | 0 | — | Porcentaje estándar aplicable sobre el monto del acto |
| `enabled` | BOOLEAN | No | No | Sí | — | — | Indica si el concepto está disponible para presupuestos |
| `fixed_concept` | BOOLEAN | No | No | Sí | — | — | `true` si es importe fijo, `false` si es liquidación porcentual |

---

### 2. `copies`

Ejemplares impresos en hojas especiales expedidos a partir de un testimonio notarial matriz.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del ejemplar de copia |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número correlativo de copia emitida |
| `print_date` | DATE | No | No | Sí | — | — | Fecha de expedición e impresión de la copia |
| `pickup_date` | DATE | No | No | No | NULL | — | Fecha en que fue retirada por el interesado |
| `notes` | TEXT | No | No | No | NULL | — | Registro de entrega o atestaciones |
| `fk_id_testimonio` | INTEGER | No | Sí | No | NULL | `testimonies(id)` | Testimonio matriz originario |
| `fk_id_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Persona a quien se le expide o entrega la copia |

---

### 3. `submitted_documents`

Documentos, constancias y certificados gestionados para un trámite o generados de manera autónoma (V6).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la pieza documental |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Nombre descriptivo del documento presentado |
| `folder_number` | INTEGER | No | No | No | NULL | — | Número de cartón de mesa de entrada de organismo externo |
| `entry_date` | DATE | No | No | No | NULL | — | Fecha en que se ingresó o solicitó ante organismo externo |
| `exit_date` | DATE | No | No | No | NULL | — | Fecha de devolución por parte del organismo |
| `prepared` | BOOLEAN | No | No | Sí | — | — | `true` si fue confeccionado en la escribanía |
| `expires` | BOOLEAN | No | No | Sí | — | — | Indica si el documento está sujeto a caducidad |
| `due_date` | DATE | No | No | No | NULL | — | Fecha límite de validez legal |
| `due_days` | INTEGER | No | No | No | NULL | — | Plazo de vigencia en días |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Monto real de sellado o tasa liquidada |
| `payment_date` | DATE | No | No | No | NULL | — | Fecha de efectivización del pago de la tasa |
| `released` | BOOLEAN | No | No | Sí | — | — | `true` si se emitió constancia de libre deuda |
| `released_date` | DATE | No | No | No | NULL | — | Fecha de acreditación de liberación |
| `observed` | BOOLEAN | No | No | Sí | — | — | `true` si el organismo formuló observaciones técnicas |
| `notes` | TEXT | No | No | No | NULL | — | Notas técnicas o requisitos de subsanación |
| `delivered` | BOOLEAN | No | No | No | false | — | `true` si ya fue entregado a la entidad requirente |
| `reentered` | BOOLEAN | No | No | No | NULL | — | `true` si fue reingresado tras subsanar (V6 nullable) |
| `delivered_by` | TEXT | No | No | Sí | — | — | Origen de provisión (`Cliente` o `Entidad Externa`) |
| `fk_id_tramite` | INTEGER | No | Sí | No | NULL | `procedures(id)` | Trámite al que pertenece (V6: opcional) |
| `fk_id_document_type` | INTEGER | No | Sí | No | NULL | `document_types(id)` | Tipo de documento maestro |

---

### 4. `deeds`

Documento formal matriz otorgado en el protocolo notarial debidamente protocolizado.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador interno de la escritura matriz |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número correlativo anual de escritura dentro del protocolo |
| `deed_date` | DATE | No | No | Sí | — | — | Fecha de otorgamiento y celebración del acto |
| `body` | TEXT | No | No | Sí | — | — | Texto legal íntegro de la escritura protocolar |
| `status` | TEXT | No | No | Sí | — | — | Estado de la escritura (`Preparada`, `Firmada`, `No Pasó`, `Errose`) |
| `registration_number` | TEXT | No | No | No | NULL | — | Matrícula registral otorgada por el Registro |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha de inscripción definitiva |
| `notes` | TEXT | No | No | No | NULL | — | Notas marginales y atestados notariales |

---

### 5. `management_statuses`

Catálogo de estados operacionales por los que puede transitar una gestión notarial.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del estado de gestión |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Nombre del estado (e.g., `Generado`, `En proceso`, `Listo para firmar`) |
| `notes` | TEXT | No | No | No | NULL | — | Definición funcional y condiciones |

---

### 6. `folios`

Hojas protocolares provistas por el Colegio Notarial para asentar escrituras públicas.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del folio notarial |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número oficial correlativo impreso en el folio |
| `year_number` | INTEGER | No | No | Sí | — | — | Año calendario del protocolo correspondiente |
| `status` | TEXT | No | No | Sí | — | — | Estado (`Disponible`, `Usado`, `Errose`, `No Pasó`) |
| `notes` | TEXT | No | No | No | NULL | — | Justificación de contingencias o atestados |
| `fk_id_deed` | INTEGER | No | Sí | No | NULL | `deeds(id)` | Escritura en la que fue utilizado |
| `fk_id_folio_type` | INTEGER | No | Sí | Sí | — | `folio_types(id)` | Clasificación de uso del folio |
| `fk_id_notary_person` | INTEGER | No | Sí | Sí | — | `people(id)` | Escribano responsable titular del registro |
| `fk_id_notebook` | INTEGER | No | Sí | No | NULL | `notebooks(id)` | Cuaderno al que pertenece el folio |

---

### 7. `folio_copies`

Tabla asociativa que vincula las hojas de testimonio con las copias expedidas.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_folio` | INTEGER | Sí | Sí | Sí | — | `folios(id)` | Folio especial de testimonio |
| `fk_id_copy` | INTEGER | Sí | Sí | Sí | — | `copies(id)` | Copia expedida |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |

---

### 8. `deed_managements`

Expediente o carpeta física que agrupa uno o varios trámites notariales afines.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la gestión |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número correlativo anual de la gestión |
| `start_date` | DATE | No | No | Sí | — | — | Fecha de apertura de la carpeta |
| `heading` | TEXT | No | No | Sí | — | — | Carátula descriptiva del objeto de la gestión |
| `notes` | TEXT | No | No | No | NULL | — | Notas operativas de tramitación |
| `fk_id_notary_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Escribano a cargo de la gestión |
| `fk_id_management_status` | INTEGER | No | Sí | No | NULL | `management_statuses(id)` | Estado operativo consolidado |
| `pending_debt_on_archive` | BOOLEAN | No | No | No | NULL | — | `true` si la gestión se archivó con deuda pendiente |

---

### 9. `history`

Registro histórico cronológico de las transiciones de estado de una gestión.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del registro histórico |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `event_date` | DATE | No | No | Sí | — | — | Fecha del cambio de estado |
| `notes` | TEXT | No | No | No | NULL | — | Motivo o atestación del cambio de estado |
| `fk_id_deed_management` | INTEGER | No | Sí | No | NULL | `deed_managements(id)` | Gestión que transitó de estado |
| `fk_id_management_status` | INTEGER | No | Sí | No | NULL | `management_statuses(id)` | Estado alcanzado |

---

### 10. `properties`

Bienes inmuebles y sus determinaciones catastrales y registrales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del inmueble |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `nomenclature` | TEXT | No | No | No | NULL | — | Nomenclatura catastral oficial |
| `registration_number` | TEXT | No | No | No | NULL | — | Matrícula del Registro de la Propiedad |
| `valuation_year` | INTEGER | No | No | No | NULL | — | Año de la valuación fiscal |
| `fiscal_valuation` | NUMERIC(19,2) | No | No | No | NULL | — | Monto fiscal oficial de tasación |
| `record_number` | INTEGER | No | No | No | NULL | — | Número de partida inmobiliaria |
| `district` | TEXT | No | No | No | NULL | — | Circunscripción |
| `section` | TEXT | No | No | No | NULL | — | Sección |
| `zone` | TEXT | No | No | No | NULL | — | Zona |
| `block` | TEXT | No | No | No | NULL | — | Manzana |
| `parcel` | TEXT | No | No | No | NULL | — | Parcela |
| `polygon` | TEXT | No | No | No | NULL | — | Polígono |
| `functional_unit` | TEXT | No | No | No | NULL | — | Unidad funcional (PH) |
| `address` | TEXT | No | No | No | NULL | — | Dirección física del inmueble |
| `locality` | TEXT | No | No | No | NULL | — | Localidad de ubicación |
| `notes` | TEXT | No | No | No | NULL | — | Linderos y especificaciones |
| `registry_volume_folio` | VARCHAR(255) | No | No | No | NULL | — | Tomo y folio de inscripción en el Registro de la Propiedad |
| `boundaries` | TEXT | No | No | No | NULL | — | Linderos del inmueble |

---

### 11. `items`

Desglose arancelario de conceptos liquidados en un presupuesto (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del ítem |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Concepto o partida liquidada |
| `amount` | NUMERIC(19,2) | No | No | Sí | — | — | Monto resultante de la línea |
| `percentage` | INTEGER | No | No | Sí | — | — | Porcentaje aplicado en la liquidación |
| `fixed_concept` | BOOLEAN | No | No | Sí | — | — | `true` si es importe fijo, `false` si es porcentual |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `budgets(id)` | Presupuesto al que pertenece |
| `notes` | TEXT | No | No | No | NULL | — | Notas justificativas de la partida (V3/V4) |
| `item_type` | VARCHAR(20) | No | No | Sí | 'NORMAL' | — | Clase de ítem presupuestario (por defecto `NORMAL`) |
| `reason` | VARCHAR(255) | No | No | No | NULL | — | Motivo declarado del ítem cuando no es un concepto ordinario |

---

### 12. `testimony_movements`

Registro del tracto y asientos de presentación registral del testimonio (V5).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del movimiento registral (V5) |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `entry_date` | DATE | No | No | Sí | — | — | Fecha de ingreso del movimiento (V5) |
| `notes` | TEXT | No | No | No | NULL | — | Notas y despachos registrales |
| `fk_id_testimonio` | INTEGER | No | Sí | No | NULL | `testimonies(id)` | Testimonio objeto del trámite |
| `exit_date` | DATE | No | No | No | NULL | — | Fecha de devolución del Registro (V5) |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha en que se perfeccionó la inscripción (V5) |
| `registered` | BOOLEAN | No | No | Sí | false | — | `true` si la inscripción resultó favorable (V5) |
| `folder_number` | INTEGER | No | No | Sí | 0 | — | Número de cartón de presentación (V5) |

---

### 13. `payments`

Recibos de cobro imputados a un presupuesto notarial.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del recibo |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `payment_date` | DATE | No | No | Sí | — | — | Fecha de realización del pago |
| `amount` | NUMERIC(19,2) | No | No | Sí | — | — | Importe percibido |
| `notes` | TEXT | No | No | No | NULL | — | Medio de pago y constancias |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `budgets(id)` | Presupuesto cancelado |
| `payment_method` | TEXT | No | No | No | NULL | — | Medio de pago utilizado |

---

### 14. `people`

Entidad unificada para personas humanas y jurídicas que intervienen en la escribanía.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la persona |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `last_name` | TEXT | No | No | Sí | — | — | Apellido o denominación societaria |
| `first_name` | TEXT | No | No | Sí | — | — | Nombres de pila o razón social |
| `identification_number` | TEXT | No | No | Sí | — | — | Identificación civil principal |
| `tax_id` | TEXT | No | No | No | NULL | — | Clave tributaria CUIT / CUIL |
| `sex` | TEXT | No | No | No | NULL | — | Sexo / Género legal |
| `fecha_nacimiento` | DATE | No | No | No | NULL | — | Fecha de nacimiento |
| `marital_status` | TEXT | No | No | No | NULL | — | Estado civil |
| `marriage_count` | INTEGER | No | No | No | NULL | — | Número de nupcias si contrajo matrimonio |
| `occupation` | TEXT | No | No | No | NULL | — | Ocupación laboral |
| `address` | TEXT | No | No | No | NULL | — | Domicilio real / legal |
| `email` | TEXT | No | No | No | NULL | — | Correo electrónico principal |
| `notary_registration_number` | INTEGER | No | No | No | NULL | — | Número de Registro Notarial (escribanos) |
| `is_client` | BOOLEAN | No | No | Sí | false | — | `true` si es cliente de la escribanía |
| `locality` | TEXT | No | No | No | NULL | — | Localidad |
| `province` | TEXT | No | No | No | NULL | — | Provincia |
| `phone` | TEXT | No | No | No | NULL | — | Teléfono fijo |
| `mobile_phone` | TEXT | No | No | No | NULL | — | Teléfono celular |
| `secondary_email` | TEXT | No | No | No | NULL | — | Correo electrónico secundario/histórico |
| `nationality` | TEXT | No | No | No | NULL | — | Nacionalidad de la persona |
| `profession` | TEXT | No | No | No | NULL | — | Profesión u oficio |
| `notes` | TEXT | No | No | No | NULL | — | Legajo y antecedentes de la persona |
| `fk_id_tipo_identificacion` | INTEGER | No | Sí | No | NULL | `identification_types(id)` | Tipo de identificación principal |
| `is_notary` | BOOLEAN | No | No | Sí | false | — | `true` si es escribano habilitado |

---

### 15. `budget_templates`

Tabla asociativa M:N que parametriza conceptos arancelarios estándar por tipo de trámite.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Sí | Sí | Sí | — | `procedure_types(id)` | Tipo de trámite |
| `fk_id_concept` | INTEGER | Sí | Sí | Sí | — | `concepts(id)` | Concepto presupuestado |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | No | NULL | — | Reglas particulares de cómputo |

---

### 16. `procedure_templates`

Tabla asociativa M:N que estipula los requisitos documentales y certificados por tipo de trámite.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Sí | Sí | Sí | — | `procedure_types(id)` | Tipo de trámite |
| `fk_id_document_type` | INTEGER | Sí | Sí | Sí | — | `document_types(id)` | Documento/Certificado requerido |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | No | NULL | — | Instrucciones específicas de presentación |

---

### 17. `budgets`

Cotización arancelaria emitida a un cliente. En V14 se eliminó la FK redundante hacia trámite (`fk_id_tramite`), estableciendo que la relación canónica es `procedures.fk_id_presupuesto`.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del presupuesto |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número correlativo del presupuesto |
| `budget_date` | DATE | No | No | Sí | — | — | Fecha de emisión |
| `heading` | TEXT | No | No | Sí | — | — | Título descriptivo de la cotización |
| `notes` | TEXT | No | No | No | NULL | — | Condiciones y plazos de validez |
| `status` | TEXT | No | No | Sí | — | — | Estado (`Borrador`, `Emitido`, `Aceptado`, `Abonado`, `Cancelado`) |
| `property_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Base imponible inmobiliaria informada |
| `fk_id_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Cliente solicitante |

---

### 18. `audit_records`

Log no repudiable de eventos de seguridad y transacciones de negocio.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del evento auditado |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `date` | TIMESTAMP | No | No | Sí | — | — | Marca temporal exacta de la transacción |
| `module` | TEXT | No | No | Sí | — | — | Módulo funcional afectado |
| `operation_detail` | TEXT | No | No | Sí | — | — | Detalle de datos modificados o acción ejecutada |
| `fk_id_usuario` | INTEGER | No | Sí | No | NULL | `users(id)` | Operador responsable de la acción |

---

### 19. `roles`

Catálogo de roles de seguridad del sistema (introducido en Flyway V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del rol |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Nombre único del rol (`ADMIN`, `ESCRIBANO`, `SECRETARIA`, etc.) |
| `description` | TEXT | No | No | No | NULL | — | Alcances y responsabilidades del rol |
| `active` | BOOLEAN | No | No | Sí | true | — | Indica si el rol está habilitado |

---

### 20. `role_modules`

Permisos por módulo asignados a cada rol (introducido en Flyway V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_role` | INTEGER | Sí | Sí | Sí | — | `roles(id)` | Rol al cual se le concede el permiso |
| `module` | TEXT | Sí | No | Sí | — | — | Identificador del módulo o acción autorizada |

---

### 21. `substitutions`

Designación de suplencias y coberturas de licencias entre escribanos.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la suplencia |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `start_date` | DATE | No | No | Sí | — | — | Fecha inicial de la suplencia |
| `end_date` | DATE | No | No | No | NULL | — | Fecha de finalización |
| `notes` | TEXT | No | No | No | NULL | — | Motivo o atestación legal |
| `fk_id_substituted_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Escribano titular bajo licencia |
| `fk_id_substitute_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Escribano suplente interviniente |

---

### 22. `testimonies`

Testimonios solemnes expedidos de escrituras públicas matrices (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del testimonio |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número de testimonio (1º, 2º testimonio) |
| `notes` | TEXT | No | No | No | NULL | — | Notas registrales o de entrega |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha de registración |
| `pickup_date` | DATE | No | No | No | NULL | — | Fecha de entrega al cliente |
| `book_entry_date` | DATE | No | No | No | NULL | — | Fecha de asiento en libro |
| `folder_number` | INTEGER | No | No | No | NULL | — | Número de carpeta registral |
| `file_number` | INTEGER | No | No | No | NULL | — | Número de expediente del Registro |
| `reentered` | BOOLEAN | No | No | Sí | false | — | `true` si fue reingresado |
| `fk_id_deed` | INTEGER | No | Sí | No | NULL | `deeds(id)` | Escritura matriz de origen |
| `observed` | BOOLEAN | No | No | Sí | false | — | `true` si fue observado (V3/V4) |
| `verified` | BOOLEAN | No | No | Sí | false | — | `true` si el testimonio fue verificado |

---

### 23. `document_types`

Catálogo maestro de documentos, títulos antecedentes y certificados registrales.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Denominación del documento/certificado |
| `returned` | BOOLEAN | No | No | Sí | — | — | `true` si el original debe restituirse al cliente |
| `expires` | BOOLEAN | No | No | Sí | — | — | `true` si el certificado tiene vigencia temporal |
| `due_days` | INTEGER | No | No | No | NULL | — | Plazo legal de vigencia en días |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Tasa arancelaria estándar |
| `enabled` | BOOLEAN | No | No | Sí | — | — | `true` si está activo para plantillas |
| `delivered_by` | TEXT | No | No | Sí | — | — | Origen (`Cliente` o `Entidad Externa`) |

---

### 24. `folio_types`

Catálogo maestro de tipos de folios notariales (V3/V4).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de folio |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Nombre (`Protocolo Principal`, `Protocolo Auxiliar`, `Testimonio`) |
| `notes` | TEXT | No | No | No | NULL | — | Notas del tipo de folio (V3/V4) |
| `enabled` | BOOLEAN | No | No | Sí | true | — | `true` si está activo para tandas de folios (V3/V4) |
| `is_auxiliary` | BOOLEAN | No | No | Sí | false | — | `true` si el tipo corresponde a folios auxiliares (de uso complementario) |

---

### 25. `procedure_types`

Catálogo maestro de actos jurídicos notariales (V8).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador del tipo de trámite |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Denominación del trámite (e.g., `Compraventa`, `Poder`) |
| `notes` | TEXT | No | No | No | NULL | — | Normativa y descripción notarial |
| `enabled` | BOOLEAN | No | No | Sí | — | — | `true` si permite nuevas gestiones |
| `is_archived` | BOOLEAN | No | No | Sí | — | — | `true` si requiere archivo físico con bibliorato |
| `is_registered` | BOOLEAN | No | No | Sí | — | — | `true` si requiere inscripción registral |
| `associates_properties` | BOOLEAN | No | No | Sí | — | — | `true` si involucra properties |
| `fk_workflow_definition_id` | INTEGER | No | Sí | No | NULL | `workflow_definition(id_workflow_definition)` | Workflow asignado al tipo de trámite (V8) |

---

### 26. `identification_types`

Catálogo maestro de documentos de identidad reconocidos.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Sí | — | — | Nombre (`DNI`, `CUIT`, `CUIL`, `Pasaporte`, `CI`, `LC`, `LE`) |
| `characters` | TEXT | No | No | Sí | — | — | Formato o máscara alfanumérica |

---

### 27. `procedures`

Instancia particular de trámite o negocio jurídico (V13/V14).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del trámite |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | No | NULL | — | Número correlativo de trámite (V13 opcional) |
| `name` | TEXT | No | No | No | NULL | — | Carátula descriptiva (V13 opcional) |
| `notes` | TEXT | No | No | No | NULL | — | Instrucciones u observaciones |
| `fk_id_procedure_type` | INTEGER | No | Sí | No | NULL | `procedure_types(id)` | Tipo de trámite |
| `fk_id_gestion` | INTEGER | No | Sí | No | NULL | `deed_managements(id)` | Gestión que lo agrupa (nulo en aux.) |
| `fk_id_escritura` | INTEGER | No | Sí | No | NULL | `deeds(id)` | Escritura notarial resultante |
| `fk_id_presupuesto` | INTEGER | No | Sí | No | NULL | `budgets(id)` | Presupuesto económico base (V14) |
| `fk_id_property` | INTEGER | No | Sí | No | NULL | `properties(id)` | Inmueble objeto del acto (si aplica) |

---

### 28. `person_procedures`

Tabla asociativa que vincula personas con el trámite e indica su rol jurídico.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure` | INTEGER | Sí | Sí | Sí | — | `procedures(id)` | Trámite en el que participa |
| `fk_id_client_person` | INTEGER | Sí | Sí | Sí | — | `people(id)` | Persona que interviene |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | Sí | '' | — | Rol notarial (`Comprador`, `Vendedor`, `Donante`, `Apoderado`) |

---

### 29. `users`

Cuentas de operadores del sistema (V9).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del usuario |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `username` | TEXT | No | No | Sí | — | — | Nombre de usuario (login único) |
| `password` | TEXT | No | No | Sí | — | — | Hash seguro de la contraseña |
| `user_type` | TEXT | No | No | Sí | — | — | Rol descriptivo legacy |
| `status` | BOOLEAN | No | No | Sí | — | — | `true` si la cuenta está activa |
| `fk_id_person` | INTEGER | No | Sí | No | NULL | `people(id)` | Persona física asociada |
| `fk_id_role` | INTEGER | No | Sí | No | NULL | `roles(id)` | Rol de seguridad asignado (V9) |

---

### 30. `workflow_definition`

Definición de grafos de flujos de trabajo notariales (introducido en Flyway V7).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_workflow_definition` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del workflow |
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
| `fk_workflow_definition_id` | INTEGER | No | Sí | Sí | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_estado_gestion_id` | INTEGER | No | Sí | Sí | — | `management_statuses(id)` | Estado de gestión representado por el nodo |
| `tipo` | TEXT | No | No | Sí | — | — | Tipo de nodo: `INITIAL`, `INTERMEDIATE`, `FINAL` |
| `posicion_x` | REAL | No | No | No | NULL | — | Coordenada visual X en el diagramador |
| `posicion_y` | REAL | No | No | No | NULL | — | Coordenada visual Y en el diagramador |

---

### 32. `workflow_transition`

Transiciones dirigidas entre nodos de workflow con condiciones de guarda (introducido en Flyway V7).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id_workflow_transition` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la transición |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fk_workflow_definition_id` | INTEGER | No | Sí | Sí | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_nodo_origen_id` | INTEGER | No | Sí | Sí | — | `workflow_node(id_workflow_node)` | Nodo origen (ON DELETE CASCADE) |
| `fk_nodo_destino_id` | INTEGER | No | Sí | Sí | — | `workflow_node(id_workflow_node)` | Nodo destino (ON DELETE CASCADE) |
| `condicion` | TEXT | No | No | No | NULL | — | Condición lógica de habilitación de la transición |
| `descripcion` | TEXT | No | No | No | NULL | — | Descripción de la acción que dispara el avance |

---

### 33. `document_cost_templates`

Plantillas de costo por documento exigido en cada tipo de trámite.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Sí | Sí | Sí | — | `procedure_types(id)` | Tipo de trámite al que aplica la plantilla de costo |
| `fk_id_document_type` | INTEGER | Sí | Sí | Sí | — | `document_types(id)` | Tipo de documento al que aplica la plantilla de costo |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `fixed_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Importe fijo sugerido para el documento |
| `variable_percentage` | NUMERIC(7,4) | No | No | No | NULL | — | Porcentaje variable sugerido para el documento |

---

### 34. `notebooks`

Cuadernos anuales de un escribano que agrupan folios de protocolo.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del cuaderno |
| `version` | INTEGER | No | No | Sí | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número del cuaderno |
| `year_number` | INTEGER | No | No | Sí | — | — | Año al que corresponde el cuaderno |
| `notes` | TEXT | No | No | No | NULL | — | Observaciones del cuaderno |
| `fk_id_notary_person` | INTEGER | No | Sí | Sí | — | `people(id)` | Escribano titular del cuaderno |

---

### 35. `procedure_folders`

Carpetas físicas de trámite numeradas por gestión (CU85).

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco de la carpeta de trámite |
| `number` | INTEGER | No | No | Sí | — | — | Número correlativo de la carpeta (secuencia dedicada) |
| `status` | VARCHAR(20) | No | No | Sí | — | — | Estado de la carpeta |
| `wait_reason` | TEXT | No | No | No | NULL | — | Motivo de la espera cuando la carpeta está detenida |
| `fk_id_gestion` | INTEGER | No | Sí | Sí | — | `deed_managements(id)` | Gestión a la que pertenece la carpeta |
| `fk_id_tramite` | INTEGER | No | Sí | Sí | — | `procedures(id)` | Trámite que origina la carpeta |

---

### 36. `registration_drafts`

Borradores de inscripción de una escritura ante el Registro y su seguimiento.

| Columna | Tipo de Dato | PK | FK | Not Null | Default | Referencia | Descripción |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Sí | No | Sí | Auto | — | Identificador unívoco del borrador de inscripción |
| `version` | INTEGER | No | No | Sí | 0 | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Sí | — | — | Número del borrador |
| `operation_price` | NUMERIC(19,2) | No | No | No | NULL | — | Precio de la operación a inscribir |
| `status` | VARCHAR(50) | No | No | Sí | — | — | Estado del borrador en el circuito de inscripción |
| `generation_date` | DATE | No | No | Sí | — | — | Fecha de generación del borrador |
| `submission_date` | DATE | No | No | No | NULL | — | Fecha de presentación ante el Registro |
| `registry_entry_number` | VARCHAR(255) | No | No | No | NULL | — | Número de ingreso asignado por el Registro |
| `reception_date` | DATE | No | No | No | NULL | — | Fecha de recepción por el Registro |
| `final_registration_number` | VARCHAR(255) | No | No | No | NULL | — | Número definitivo de inscripción |
| `registry_notes` | TEXT | No | No | No | NULL | — | Observaciones formuladas por el Registro |
| `correction_date` | DATE | No | No | No | NULL | — | Fecha de la corrección solicitada por el Registro |
| `fk_id_escritura` | INTEGER | No | Sí | Sí | — | `deeds(id)` | Escritura a inscribir |

---

## 5. Matriz de Integridad Referencial Consolidada

| Tabla Origen | Columna FK | Tabla Destino | Columna PK | Acción ON DELETE |
|---|---|---|---|---|
| `copies` | `fk_id_testimonio` | `testimonies` | `id` | NO ACTION |
| `copies` | `fk_id_person` | `people` | `id` | NO ACTION |
| `submitted_documents` | `fk_id_tramite` | `procedures` | `id` | NO ACTION |
| `submitted_documents` | `fk_id_document_type` | `document_types` | `id` | NO ACTION |
| `folios` | `fk_id_deed` | `deeds` | `id` | NO ACTION |
| `folios` | `fk_id_folio_type` | `folio_types` | `id` | NO ACTION |
| `folios` | `fk_id_notary_person` | `people` | `id` | NO ACTION |
| `folios` | `fk_id_notebook` | `notebooks` | `id` | NO ACTION |
| `folio_copies` | `fk_id_folio` | `folios` | `id` | NO ACTION |
| `folio_copies` | `fk_id_copy` | `copies` | `id` | NO ACTION |
| `deed_managements` | `fk_id_notary_person` | `people` | `id` | NO ACTION |
| `deed_managements` | `fk_id_management_status` | `management_statuses` | `id` | NO ACTION |
| `history` | `fk_id_deed_management` | `deed_managements` | `id` | NO ACTION |
| `history` | `fk_id_management_status` | `management_statuses` | `id` | NO ACTION |
| `items` | `fk_id_presupuesto` | `budgets` | `id` | NO ACTION |
| `testimony_movements` | `fk_id_testimonio` | `testimonies` | `id` | NO ACTION |
| `payments` | `fk_id_presupuesto` | `budgets` | `id` | NO ACTION |
| `people` | `fk_id_tipo_identificacion` | `identification_types` | `id` | NO ACTION |
| `budget_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `budget_templates` | `fk_id_concept` | `concepts` | `id` | CASCADE |
| `procedure_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `procedure_templates` | `fk_id_document_type` | `document_types` | `id` | CASCADE |
| `budgets` | `fk_id_person` | `people` | `id` | NO ACTION |
| `audit_records` | `fk_id_usuario` | `users` | `id` | NO ACTION |
| `role_modules` | `fk_id_role` | `roles` | `id` | CASCADE |
| `substitutions` | `fk_id_substituted_person` | `people` | `id` | NO ACTION |
| `substitutions` | `fk_id_substitute_person` | `people` | `id` | NO ACTION |
| `testimonies` | `fk_id_deed` | `deeds` | `id` | NO ACTION |
| `procedure_types` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | NO ACTION |
| `procedures` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `procedures` | `fk_id_gestion` | `deed_managements` | `id` | NO ACTION |
| `procedures` | `fk_id_escritura` | `deeds` | `id` | NO ACTION |
| `procedures` | `fk_id_presupuesto` | `budgets` | `id` | NO ACTION |
| `procedures` | `fk_id_property` | `properties` | `id` | NO ACTION |
| `person_procedures` | `fk_id_procedure` | `procedures` | `id` | NO ACTION |
| `person_procedures` | `fk_id_client_person` | `people` | `id` | NO ACTION |
| `users` | `fk_id_person` | `people` | `id` | NO ACTION |
| `users` | `fk_id_role` | `roles` | `id` | NO ACTION |
| `workflow_node` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_node` | `fk_estado_gestion_id` | `management_statuses` | `id` | NO ACTION |
| `workflow_transition` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_transition` | `fk_nodo_origen_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `workflow_transition` | `fk_nodo_destino_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `document_cost_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `document_cost_templates` | `fk_id_document_type` | `document_types` | `id` | NO ACTION |
| `notebooks` | `fk_id_notary_person` | `people` | `id` | NO ACTION |
| `procedure_folders` | `fk_id_gestion` | `deed_managements` | `id` | NO ACTION |
| `procedure_folders` | `fk_id_tramite` | `procedures` | `id` | NO ACTION |
| `registration_drafts` | `fk_id_escritura` | `deeds` | `id` | NO ACTION |

---

## 6. Notas Técnicas

### 6.1 Entidad Heredada: `identificaciones`

**Estado:** Archivada, no materializada en Flyway V1–V41.

La entidad `identificaciones` fue documentada y existe en los scripts de inicialización heredados (`docs/archive/init-db/orig/01_initial_schema.sql`), pero nunca fue creada mediante las migraciones Flyway. Esta tabla era una propuesta de normalización 3FN para permitir múltiples documentos de identidad por persona (DNI, CUIT, Pasaporte, etc.).

**Decisión:** En la fase de migración a Flyway (V1–V14), se abandonó esta normalización en favor de la simplificación operacional: cada persona mantiene un único `identification_number` y `tax_id` denormalizado en la tabla `people`, y se utiliza `fk_id_tipo_identificacion` para clasificar el tipo de identificación principal. Esta decisión priorizó la coherencia con el código JPA moderno y la experiencia usuario sobre la normalización teórica.

**Alternativas Futuras:**

- **(A) Crear V15:** Materializar `identificaciones` como tabla asociativa M:N si se requiere 3FN puro y soporte para múltiples identificaciones.
- **(B) Mantener como está:** Conservar la denormalización en `people` como decisión de diseño aceptada.

### 6.2 Entidades de Apoyo (Supporting Entities) sin Caso de Uso Independiente

Las siguientes 19 entidades (59% de la base de datos) no poseen un Caso de Uso independiente. Se clasifican como **entidades maestras, plantillas o de compensación** que se crean y modifican indirectamente dentro de los flujos principales:

| Categoría | Entidades | Observación |
|-----------|-----------|-------------|
| **Maestros/Catálogos** | `document_types`, `folio_types`, `procedure_types`, `identification_types`, `concepts`, `management_statuses` | Se crean vía CRUD administrativo, referenciados por CUs de negocio. |
| **Plantillas** | `budget_templates`, `procedure_templates` | Se definen una única vez y reutilizan en múltiples CUs (presupuestación, documentación). |
| **Compensación/Seguridad** | `roles`, `role_modules`, `audit_records`, `users`, `workflow_definition`, `workflow_node`, `workflow_transition` | Se crean durante instalación/configuración del sistema o automáticamente por auditoría/workflows. |
| **Asociativas Operacionales** | `submitted_documents`, `person_procedures`, `folio_copies`, `testimony_movements`, `substitutions`, `history`, `properties`, `items`, `payments` | Tablas débiles/asociativas creadas como parte de CUs que gestionan entidades fuertes (trámites, escrituras, presupuestos). |

**Patrón de Cobertura:**

- 13 entidades **fuertes** (41%) poseen CUs explícitas.
- 19 entidades **de apoyo/asociativas** (59%) son creadas por las 13 CUs principales o durante operaciones administrativas.

Este patrón es esperado en sistemas notariales donde la mayoría del trabajo se concentra en trámites, escrituras y presupuestos, mientras que los catálogos y configuración son actividades de administración de bajo volumen.

### 6.3 Coherencia Flyway ↔ Documentación ↔ Use Cases

**Validación 2026-10-04:**

- ✅ 36/36 tablas Flyway presentes en Diccionario de Datos (regeneradas por `scripts/generate_data_dictionary.py`).
- ✅ 49 Foreign Keys documentadas correctamente (matriz de la sección 5 igual al esquema).
- ✅ Cardinalidad V14 (presupuesto-trámite) reflejada en Diccionario.
- ✅ Workflows (V7/V8) y RBAC (V9) presentes y coherentes.
- ⚠️  1 entidad heredada (`identificaciones`) archivada; decisión de diseño documentada.
- ℹ️  19 entidades sin CU independiente; clasificadas como "supporting" (normal para dominios notariales).

**Mantenimiento:** Cada migración Flyway debe ir acompañada, en el mismo commit, de `python3 scripts/generate_data_dictionary.py` (con PostgreSQL migrado) y de la descripción de cada tabla o columna nueva: el generador deja `TODO` y `scripts/test_data_dictionary_sync.py` (CI) falla mientras quede alguno o el Diccionario difiera del esquema. Las secciones 3 y 6 y los textos introductorios son manuales.

---

**Versión actual:** 3.0 (regenerada 2026-10-04 desde el esquema, deriva vigilada por `scripts/test_data_dictionary_sync.py`; previamente 2.4, revisada 2026-08-18, sección 6 añadida, renumeración y coherencia Flyway verificadas, identificaciones archivada).
