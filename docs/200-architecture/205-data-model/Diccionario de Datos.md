# Notarial management system — Notaire

## Data dictionary (relational model in third normal form)

**Version:** 3.1  
**Database:** PostgreSQL 16  
**Migrations:** Flyway (V1 through V45)  
**Updated:** 11 October 2026  
**Status:** Aligned with the current PostgreSQL schema and JPA mappings (#1222, #1490). 37 Flyway tables and 49 foreign keys. Column names follow V45 (`fk_id_budget`, `birth_date`, `node_type`, and the other renames). `identificaciones` is not created by Flyway (see section 6). V14 removed `budgets.fk_id_tramite`. Workflows are V7/V8. Roles are V9.

---

## 1. Introduction

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
   - Eliminación de la clave foránea circular en `budgets` (`fk_id_tramite`, removed in V14); la relación canónica es `procedures.fk_id_budget` (1:N, donde un presupuesto puede originar o abarcar trámites vinculados).
5. **Alineación de Movimientos de Testimony y Folios (V3, V4, V5, V6, V13):**  
   - Identificador `id`, fechas de tracto registral, y soporte para documentos autónomos (`submitted_documents.fk_id_procedure` nullable).

---

## 2. Table index (37 tables, Flyway V1–V45)

**Nota sobre Entidades Heredadas:** La entidad `identificaciones` se documentó en versiones previas pero nunca fue materializada en las migraciones Flyway activas (V1–V43). Existe solo en los scripts archivados de inicialización (`docs/000-archive/init-db/`) y fue utilizada en el modelo JPA legacy. Se considera un componente de normalización 3FN planificado pero no implementado. Véase la sección "Notas Técnicas" al pie para detalles.

| # | Table | Package / module | Entity type | Description |
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
| 37 | [revoked_tokens](#37-revoked_tokens) | Seguridad | Fuerte | Identificadores de JWT revocados por cierre de sesión hasta su expiración |

---

## 3. Flyway synchronization and compensation

### Revisión del esquema activo

La base de datos actual refleja la evolución real del sistema a través de Flyway V1–V43. Los cambios relevantes para la integridad del modelo son:

- V1: esquema base relacional con entidades de sujetos, protocolo, trámites, presupuestos y documentación.
- V3/V4: se corrigen columnas faltantes en `items`, `folio_types` y `testimonies`.
- V5: se normaliza `testimony_movements` para que coincida con el nombre de la entidad JPA (`id`, `entry_date`, `registered`, `folder_number`).
- V6: `submitted_documents.fk_id_procedure` pasa a ser opcional para soportar documentos autónomos.
- V7/V8: se incorporan `workflow_definition`, `workflow_node`, `workflow_transition` y la referencia desde `procedure_types` al workflow.
- V9: se incorporan `roles` y `role_modules`, y se enlaza `users` con `fk_id_role`.
- V13: `procedures.nombre` y `procedures.numero` pasan a ser opcionales para coincidir con las entidades de negocio.
- V14: se elimina la FK redundante `budgets.fk_id_tramite`; la relación canónica quedó en `procedures.fk_id_budget` (1:N).
- V43: se incorpora `revoked_tokens`, la lista de revocación de JWT por cierre de sesión (issue #676); sin FK.

### Matriz de entidades, PK/FK y mecanismo de compensación

| Entidad | PK principal | FK relevantes | Mecanismo de compensación | Observación de sincronía |
|---|---|---|---|---|
| `concepts` | `id` | — | Sin compensación | Tabla maestra, no depende de otras entidades |
| `copies` | `id` | `fk_id_testimony`, `fk_id_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` (RESTRICT por defecto) | Efectúa copies de testimonios |
| `submitted_documents` | `id` | `fk_id_procedure`, `fk_id_document_type` | `I: Null` si no hay trámite, `M: Impedir`, `B: Impedir` | Compatible con V6: trámite opcional |
| `deeds` | `id` | — | Sin compensación | Matriz protocolares |
| `management_statuses` | `id` | — | Sin compensación | Catálogo de estados |
| `folios` | `id` | `fk_id_deed`, `fk_id_folio_type`, `fk_id_notary_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Folio del protocolo |
| `folio_copies` | `fk_id_folio + fk_id_copy` | `fk_id_folio`, `fk_id_copy` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Tabla asociativa |
| `deed_managements` | `id` | `fk_id_notary_person`, `fk_id_management_status` | `I: Impedir`, `M: Impedir`, `B: Impedir` / `SET NULL` en estado si se deja nulo | Agrupa trámites |
| `history` | `id` | `fk_id_deed_management`, `fk_id_management_status` | `I: Impedir`, `M: Impedir`, `B: Cascada` en gestión | Histórico de estados |
| `properties` | `id` | — | Sin compensación | Bien inmueble |
| `items` | `id` | `fk_id_budget` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Límite de presupuesto |
| `testimony_movements` | `id` | `fk_id_testimony` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Corresponde a V5 |
| `payments` | `id` | `fk_id_budget` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Liquidación de cobros |
| `people` | `id` | `fk_id_identification_type` | `I: Null` si corresponde, `M: Impedir`, `B: Impedir` | Entidad central del sistema |
| `budget_templates` | `fk_id_procedure_type + fk_id_concept` | `fk_id_procedure_type`, `fk_id_concept` | `I: Impedir`, `M: Impedir`, `B: Cascada` en `concepts` | Plantilla arancelaria |
| `procedure_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Impedir`, `M: Impedir`, `B: Cascada` en documento | Requisitos documentales |
| `budgets` | `id` | `fk_id_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | La FK a trámite fue removida en V14 |
| `audit_records` | `id` | `fk_id_user` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Auditoría de transacciones |
| `roles` | `id` | — | Sin compensación | Catálogo de perfiles |
| `role_modules` | `fk_id_role + modulo` | `fk_id_role` | `I: Impedir`, `M: Impedir`, `B: Cascada` | V9; permisos por módulo |
| `substitutions` | `id` | `fk_id_substituted_person`, `fk_id_substitute_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Cobertura de escribanos |
| `testimonies` | `id` | `fk_id_deed` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Testimony generado desde escritura |
| `document_types` | `id` | — | Sin compensación | Catálogo maestra de documentos |
| `folio_types` | `id` | — | Sin compensación | Catálogo maestra de folios |
| `procedure_types` | `id` | `fk_workflow_definition_id` | `I: Null`, `M: Impedir`, `B: Impedir` (por defecto, no cascade) | V8: workflow opcional |
| `identification_types` | `id` | — | Sin compensación | Catálogo de documentos de identidad |
| `procedures` | `id` | `fk_id_procedure_type`, `fk_id_deed_management`, `fk_id_deed`, `fk_id_budget`, `fk_id_property` | `I: Impedir` / `Null` según columna, `M: Impedir`, `B: Impedir` o `SET NULL` según caso | Relación canónica con presupuesto en V14 |
| `person_procedures` | `fk_id_procedure + fk_id_client_person` | `fk_id_procedure`, `fk_id_client_person` | `I: Impedir`, `M: Impedir`, `B: Cascada` en trámite | Tabla asociativa de participación |
| `users` | `id` | `fk_id_person`, `fk_id_role` | `I: Impedir` / `Null`, `M: Impedir`, `B: Impedir` | Acceso y autenticación |
| `workflow_definition` | `id_workflow_definition` | — | Sin compensación | Definición del grafo de estados |
| `workflow_node` | `id_workflow_node` | `fk_workflow_definition_id`, `fk_management_status_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` en workflow | Nodos del flujo |
| `workflow_transition` | `id_workflow_transition` | `fk_workflow_definition_id`, `fk_origin_node_id`, `fk_destination_node_id` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Transiciones del flujo |
| `document_cost_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Impedir`, `M: Impedir`, `B: Cascada` | Costo sugerido por documento |
| `notebooks` | `id` | `fk_id_notary_person` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Notebook anual de protocolo |
| `procedure_folders` | `id` | `fk_id_deed_management`, `fk_id_procedure` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Carpetas numeradas de trámites (CU85) |
| `registration_drafts` | `id` | `fk_id_deed` | `I: Impedir`, `M: Impedir`, `B: Impedir` | Borrador de inscripción registral |

> La regla general del esquema vigente es que la mayoría de las relaciones usan restricciones por defecto de PostgreSQL (RESTRICT / NO ACTION), y solo se habilitan compensaciones explícitas con ON DELETE CASCADE o columnas anulables cuando la migración lo define.

## 4. Table specifications

### 1. `concepts`

Master catalog of fee concepts, professional fees, contributions, and notarial taxes. Stored names such as `Honorarios` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del concepto arancelario |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista (Hibernate) |
| `name` | TEXT | No | No | Yes | — | — | Denominación del concepto (e.g., Honorarios, Aporte Caja Notarial) |
| `amount` | NUMERIC(19,2) | No | No | Yes | 0 | — | Importe fijo de referencia en moneda de curso legal |
| `percentage` | INTEGER | No | No | Yes | 0 | — | Porcentaje estándar aplicable sobre el monto del acto |
| `enabled` | BOOLEAN | No | No | Yes | — | — | Indica si el concepto está disponible para presupuestos |
| `fixed_concept` | BOOLEAN | No | No | Yes | — | — | `true` si es importe fijo, `false` si es liquidación porcentual |

---

### 2. `copies`

Printed copies issued from a source notarial testimony.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del ejemplar de copia |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número correlativo de copia emitida |
| `print_date` | DATE | No | No | Yes | — | — | Fecha de expedición e impresión de la copia |
| `pickup_date` | DATE | No | No | No | NULL | — | Fecha en que fue retirada por el interesado |
| `notes` | TEXT | No | No | No | NULL | — | Registro de entrega o atestaciones |
| `fk_id_testimony` | INTEGER | No | Yes | No | NULL | `testimonies(id)` | Testimony matriz originario |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Person a quien se le expide o entrega la copia |

---

### 3. `submitted_documents`

Documents and certificates filed for a procedure, or created on their own (V6).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la pieza documental |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre descriptivo del documento presentado |
| `folder_number` | INTEGER | No | No | No | NULL | — | Número de cartón de mesa de entrada de organismo externo |
| `entry_date` | DATE | No | No | No | NULL | — | Fecha en que se ingresó o solicitó ante organismo externo |
| `exit_date` | DATE | No | No | No | NULL | — | Fecha de devolución por parte del organismo |
| `prepared` | BOOLEAN | No | No | Yes | — | — | `true` si fue confeccionado en la escribanía |
| `expires` | BOOLEAN | No | No | Yes | — | — | Indica si el documento está sujeto a caducidad |
| `due_date` | DATE | No | No | No | NULL | — | Fecha límite de validez legal |
| `due_days` | INTEGER | No | No | No | NULL | — | Plazo de vigencia en días |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Monto real de sellado o tasa liquidada |
| `payment_date` | DATE | No | No | No | NULL | — | Fecha de efectivización del pago de la tasa |
| `released` | BOOLEAN | No | No | Yes | — | — | `true` si se emitió constancia de libre deuda |
| `released_date` | DATE | No | No | No | NULL | — | Fecha de acreditación de liberación |
| `observed` | BOOLEAN | No | No | Yes | — | — | `true` si el organismo formuló observaciones técnicas |
| `notes` | TEXT | No | No | No | NULL | — | Notas técnicas o requisitos de subsanación |
| `delivered` | BOOLEAN | No | No | No | false | — | `true` si ya fue entregado a la entidad requirente |
| `reentered` | BOOLEAN | No | No | No | NULL | — | `true` si fue reingresado tras subsanar (V6 nullable) |
| `delivered_by` | TEXT | No | No | Yes | — | — | Origen de provisión (`Cliente` o `Entidad Externa`) |
| `fk_id_procedure` | INTEGER | No | Yes | No | NULL | `procedures(id)` | Trámite al que pertenece (V6: opcional) |
| `fk_id_document_type` | INTEGER | No | Yes | No | NULL | `document_types(id)` | Tipo de documento maestro |

---

### 4. `deeds`

The source deed granted in the notarial protocol.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador interno de la escritura matriz |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número correlativo anual de escritura dentro del protocolo |
| `deed_date` | DATE | No | No | Yes | — | — | Fecha de otorgamiento y celebración del acto |
| `body` | TEXT | No | No | Yes | — | — | Texto legal íntegro de la escritura protocolar |
| `status` | TEXT | No | No | Yes | — | — | Estado de la escritura (`Preparada`, `Firmada`, `No Pasó`, `Errose`) |
| `registration_number` | TEXT | No | No | No | NULL | — | Matrícula registral otorgada por el Registro |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha de inscripción definitiva |
| `notes` | TEXT | No | No | No | NULL | — | Notas marginales y atestados notariales |

---

### 5. `management_statuses`

Catalog of operational statuses a deed management can move through. Stored status names stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador del estado de gestión |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre del estado (e.g., `Generado`, `En proceso`, `Listo para firmar`) |
| `notes` | TEXT | No | No | No | NULL | — | Definición funcional y condiciones |

---

### 6. `folios`

Protocol sheets supplied for recording public deeds.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del folio notarial |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número oficial correlativo impreso en el folio |
| `year_number` | INTEGER | No | No | Yes | — | — | Año calendario del protocolo correspondiente |
| `status` | TEXT | No | No | Yes | — | — | Estado (`Disponible`, `Usado`, `Errose`, `No Pasó`) |
| `notes` | TEXT | No | No | No | NULL | — | Justificación de contingencias o atestados |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Deed en la que fue utilizado |
| `fk_id_folio_type` | INTEGER | No | Yes | Yes | — | `folio_types(id)` | Clasificación de uso del folio |
| `fk_id_notary_person` | INTEGER | No | Yes | Yes | — | `people(id)` | Escribano responsable titular del registro |
| `fk_id_notebook` | INTEGER | No | Yes | No | NULL | `notebooks(id)` | Notebook al que pertenece el folio |

---

### 7. `folio_copies`

Associates testimony sheets with the copies that were issued.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_folio` | INTEGER | Yes | Yes | Yes | — | `folios(id)` | Folio especial de testimonio |
| `fk_id_copy` | INTEGER | Yes | Yes | Yes | — | `copies(id)` | Copy expedida |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |

---

### 8. `deed_managements`

File that groups one or more related procedures.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la gestión |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número correlativo anual de la gestión |
| `start_date` | DATE | No | No | Yes | — | — | Fecha de apertura de la carpeta |
| `heading` | TEXT | No | No | Yes | — | — | Carátula descriptiva del objeto de la gestión |
| `notes` | TEXT | No | No | No | NULL | — | Notas operativas de tramitación |
| `fk_id_notary_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Escribano a cargo de la gestión |
| `fk_id_management_status` | INTEGER | No | Yes | No | NULL | `management_statuses(id)` | Estado operativo consolidado |
| `pending_debt_on_archive` | BOOLEAN | No | No | No | NULL | — | `true` si la gestión se archivó con deuda pendiente |

---

### 9. `history`

Chronological history of status changes for a deed management.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador del registro histórico |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `event_date` | DATE | No | No | Yes | — | — | Fecha del cambio de estado |
| `notes` | TEXT | No | No | No | NULL | — | Motivo o atestación del cambio de estado |
| `fk_id_deed_management` | INTEGER | No | Yes | No | NULL | `deed_managements(id)` | Gestión que transitó de estado |
| `fk_id_management_status` | INTEGER | No | Yes | No | NULL | `management_statuses(id)` | Estado alcanzado |

---

### 10. `properties`

Real property and its cadastral and registry details.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del inmueble |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
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

Fee lines charged on a budget (V3/V4).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del ítem |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Concept o partida liquidada |
| `amount` | NUMERIC(19,2) | No | No | Yes | — | — | Monto resultante de la línea |
| `percentage` | INTEGER | No | No | Yes | — | — | Porcentaje aplicado en la liquidación |
| `fixed_concept` | BOOLEAN | No | No | Yes | — | — | `true` si es importe fijo, `false` si es porcentual |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Presupuesto al que pertenece |
| `notes` | TEXT | No | No | No | NULL | — | Notas justificativas de la partida (V3/V4) |
| `item_type` | VARCHAR(20) | No | No | Yes | 'NORMAL' | — | Clase de ítem presupuestario (por defecto `NORMAL`) |
| `reason` | VARCHAR(255) | No | No | No | NULL | — | Motivo declarado del ítem cuando no es un concepto ordinario |

---

### 12. `testimony_movements`

Registry presentation entries for a testimony (V5).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador del movimiento registral (V5) |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `entry_date` | DATE | No | No | Yes | — | — | Fecha de ingreso del movimiento (V5) |
| `notes` | TEXT | No | No | No | NULL | — | Notas y despachos registrales |
| `fk_id_testimony` | INTEGER | No | Yes | No | NULL | `testimonies(id)` | Testimony objeto del trámite |
| `exit_date` | DATE | No | No | No | NULL | — | Fecha de devolución del Registro (V5) |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha en que se perfeccionó la inscripción (V5) |
| `registered` | BOOLEAN | No | No | Yes | false | — | `true` si la inscripción resultó favorable (V5) |
| `folder_number` | INTEGER | No | No | Yes | 0 | — | Número de cartón de presentación (V5) |
| `observed_by_registry` | BOOLEAN | No | No | Yes | false | — | `true` si el registro devolvió observado el testimonio al reingresarlo (CU44); exige observaciones |

---

### 13. `payments`

Receipts applied to a budget.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del recibo |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `payment_date` | DATE | No | No | Yes | — | — | Fecha de realización del pago |
| `amount` | NUMERIC(19,2) | No | No | Yes | — | — | Importe percibido |
| `notes` | TEXT | No | No | No | NULL | — | Medio de pago y constancias |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Presupuesto cancelado |
| `payment_method` | TEXT | No | No | No | NULL | — | Medio de pago utilizado |

---

### 14. `people`

People, natural or legal, who take part in the practice.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la persona |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `last_name` | TEXT | No | No | Yes | — | — | Apellido o denominación societaria |
| `first_name` | TEXT | No | No | Yes | — | — | Nombres de pila o razón social |
| `identification_number` | TEXT | No | No | Yes | — | — | Identificación civil principal |
| `tax_id` | TEXT | No | No | No | NULL | — | Clave tributaria CUIT / CUIL |
| `sex` | TEXT | No | No | No | NULL | — | Sexo / Género legal |
| `birth_date` | DATE | No | No | No | NULL | — | Fecha de nacimiento |
| `marital_status` | TEXT | No | No | No | NULL | — | Estado civil |
| `marriage_count` | INTEGER | No | No | No | NULL | — | Número de nupcias si contrajo matrimonio |
| `occupation` | TEXT | No | No | No | NULL | — | Ocupación laboral |
| `address` | TEXT | No | No | No | NULL | — | Domicilio real / legal |
| `email` | TEXT | No | No | No | NULL | — | Correo electrónico principal |
| `notary_registration_number` | INTEGER | No | No | No | NULL | — | Número de Registro Notarial (escribanos) |
| `is_client` | BOOLEAN | No | No | Yes | false | — | `true` si es cliente de la escribanía |
| `locality` | TEXT | No | No | No | NULL | — | Localidad |
| `province` | TEXT | No | No | No | NULL | — | Provincia |
| `phone` | TEXT | No | No | No | NULL | — | Teléfono fijo |
| `mobile_phone` | TEXT | No | No | No | NULL | — | Teléfono celular |
| `secondary_email` | TEXT | No | No | No | NULL | — | Correo electrónico secundario/histórico |
| `nationality` | TEXT | No | No | No | NULL | — | Nacionalidad de la persona |
| `profession` | TEXT | No | No | No | NULL | — | Profesión u oficio |
| `notes` | TEXT | No | No | No | NULL | — | Legajo y antecedentes de la persona |
| `fk_id_identification_type` | INTEGER | No | Yes | No | NULL | `identification_types(id)` | Tipo de identificación principal |
| `is_notary` | BOOLEAN | No | No | Yes | false | — | `true` si es escribano habilitado |

---

### 15. `budget_templates`

Associates standard fee concepts with a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Tipo de trámite |
| `fk_id_concept` | INTEGER | Yes | Yes | Yes | — | `concepts(id)` | Concept presupuestado |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | No | NULL | — | Reglas particulares de cómputo |

---

### 16. `procedure_templates`

Associates required documents and certificates with a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Tipo de trámite |
| `fk_id_document_type` | INTEGER | Yes | Yes | Yes | — | `document_types(id)` | Documento/Certificado requerido |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | No | NULL | — | Instrucciones específicas de presentación |

---

### 17. `budgets`

Quote issued to a client. V14 removed the redundant foreign key `fk_id_tramite`. The canonical link is `procedures.fk_id_budget`. Stored statuses `BORRADOR`, `PENDIENTE`, `APROBADO`, `RECHAZADO`, and `FACTURADO` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del presupuesto |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número correlativo del presupuesto |
| `budget_date` | DATE | No | No | Yes | — | — | Fecha de emisión |
| `heading` | TEXT | No | No | Yes | — | — | Título descriptivo de la cotización |
| `notes` | TEXT | No | No | No | NULL | — | Condiciones y plazos de validez |
| `status` | TEXT | No | No | Yes | — | — | Estado (`Borrador`, `Emitido`, `Aceptado`, `Abonado`, `Cancelado`) |
| `property_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Base imponible inmobiliaria informada |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Cliente solicitante |

---

### 18. `audit_records`

Audit log of security events and business transactions.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del evento auditado |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `date` | TIMESTAMP | No | No | Yes | — | — | Marca temporal exacta de la transacción |
| `module` | TEXT | No | No | Yes | — | — | Módulo funcional afectado |
| `operation_detail` | TEXT | No | No | Yes | — | — | Detalle de datos modificados o acción ejecutada |
| `fk_id_user` | INTEGER | No | Yes | No | NULL | `users(id)` | Operador responsable de la acción |

---

### 19. `roles`

Security roles (Flyway V9). Stored names such as `ESCRIBANO` and `ADMIN` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del rol |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre único del rol (`ADMIN`, `ESCRIBANO`, `SECRETARIA`, etc.) |
| `description` | TEXT | No | No | No | NULL | — | Alcances y responsabilidades del rol |
| `active` | BOOLEAN | No | No | Yes | true | — | Indica si el rol está habilitado |

---

### 20. `role_modules`

Module permissions granted to each role (Flyway V9). The column is `module`.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_role` | INTEGER | Yes | Yes | Yes | — | `roles(id)` | Role al cual se le concede el permiso |
| `module` | TEXT | Yes | No | Yes | — | — | Identificador del módulo o acción autorizada |

---

### 21. `substitutions`

Substitution periods between notaries.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la suplencia |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `start_date` | DATE | No | No | Yes | — | — | Fecha inicial de la suplencia |
| `end_date` | DATE | No | No | No | NULL | — | Fecha de finalización |
| `notes` | TEXT | No | No | No | NULL | — | Motivo o atestación legal |
| `fk_id_substituted_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Escribano titular bajo licencia |
| `fk_id_substitute_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Escribano suplente interviniente |

---

### 22. `testimonies`

Testimonies issued from source deeds (V3/V4).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del testimonio |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número de testimonio (1º, 2º testimonio) |
| `notes` | TEXT | No | No | No | NULL | — | Notas registrales o de entrega |
| `registration_date` | DATE | No | No | No | NULL | — | Fecha de registración |
| `pickup_date` | DATE | No | No | No | NULL | — | Fecha de entrega al cliente |
| `book_entry_date` | DATE | No | No | No | NULL | — | Fecha de asiento en libro |
| `folder_number` | INTEGER | No | No | No | NULL | — | Número de carpeta registral |
| `file_number` | INTEGER | No | No | No | NULL | — | Número de expediente del Registro |
| `reentered` | BOOLEAN | No | No | Yes | false | — | `true` si fue reingresado |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Deed matriz de origen |
| `observed` | BOOLEAN | No | No | Yes | false | — | `true` si fue observado (V3/V4) |
| `verified` | BOOLEAN | No | No | Yes | false | — | `true` si el testimonio fue verificado |

---

### 23. `document_types`

Master catalog of documents, prior titles, and registry certificates.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Denominación del documento/certificado |
| `returned` | BOOLEAN | No | No | Yes | — | — | `true` si el original debe restituirse al cliente |
| `expires` | BOOLEAN | No | No | Yes | — | — | `true` si el certificado tiene vigencia temporal |
| `due_days` | INTEGER | No | No | No | NULL | — | Plazo legal de vigencia en días |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Tasa arancelaria estándar |
| `enabled` | BOOLEAN | No | No | Yes | — | — | `true` si está activo para plantillas |
| `delivered_by` | TEXT | No | No | Yes | — | — | Origen (`Cliente` o `Entidad Externa`) |

---

### 24. `folio_types`

Master catalog of protocol sheet types (V3/V4). Stored names such as `Protocolo Principal` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del tipo de folio |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre (`Protocolo Principal`, `Protocolo Auxiliar`, `Testimony`) |
| `notes` | TEXT | No | No | No | NULL | — | Notas del tipo de folio (V3/V4) |
| `enabled` | BOOLEAN | No | No | Yes | true | — | `true` si está activo para tandas de folios (V3/V4) |
| `is_auxiliary` | BOOLEAN | No | No | Yes | false | — | `true` si el tipo corresponde a folios auxiliares (de uso complementario) |

---

### 25. `procedure_types`

Master catalog of notarial act types (V8). Stored names such as `Compraventa` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador del tipo de trámite |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Denominación del trámite (e.g., `Compraventa`, `Poder`) |
| `notes` | TEXT | No | No | No | NULL | — | Normativa y descripción notarial |
| `enabled` | BOOLEAN | No | No | Yes | — | — | `true` si permite nuevas gestiones |
| `is_archived` | BOOLEAN | No | No | Yes | — | — | `true` si requiere archivo físico con bibliorato |
| `is_registered` | BOOLEAN | No | No | Yes | — | — | `true` si requiere inscripción registral |
| `associates_properties` | BOOLEAN | No | No | Yes | — | — | `true` si involucra properties |
| `fk_workflow_definition_id` | INTEGER | No | Yes | No | NULL | `workflow_definition(id_workflow_definition)` | Workflow asignado al tipo de trámite (V8) |

---

### 26. `identification_types`

Master catalog of identification document types. Stored names such as `DNI` and `CUIT` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del tipo de documento |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre (`DNI`, `CUIT`, `CUIL`, `Pasaporte`, `CI`, `LC`, `LE`) |
| `characters` | TEXT | No | No | Yes | — | — | Formato o máscara alfanumérica |

---

### 27. `procedures`

One procedure instance (V13/V14).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del trámite |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | No | NULL | — | Número correlativo de trámite (V13 opcional) |
| `name` | TEXT | No | No | No | NULL | — | Carátula descriptiva (V13 opcional) |
| `notes` | TEXT | No | No | No | NULL | — | Instrucciones u observaciones |
| `fk_id_procedure_type` | INTEGER | No | Yes | No | NULL | `procedure_types(id)` | Tipo de trámite |
| `fk_id_deed_management` | INTEGER | No | Yes | No | NULL | `deed_managements(id)` | Gestión que lo agrupa (nulo en aux.) |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Deed notarial resultante |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Presupuesto económico base (V14) |
| `fk_id_property` | INTEGER | No | Yes | No | NULL | `properties(id)` | Property objeto del acto (si aplica) |

---

### 28. `person_procedures`

Associates people with a procedure and records their legal role. Stored role labels stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure` | INTEGER | Yes | Yes | Yes | — | `procedures(id)` | Trámite en el que participa |
| `fk_id_client_person` | INTEGER | Yes | Yes | Yes | — | `people(id)` | Person que interviene |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `notes` | TEXT | No | No | Yes | '' | — | Role notarial (`Comprador`, `Vendedor`, `Donante`, `Apoderado`) |

---

### 29. `users`

Operator accounts (V9).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del usuario |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `username` | TEXT | No | No | Yes | — | — | Nombre de usuario (login único) |
| `password` | TEXT | No | No | Yes | — | — | Hash seguro de la contraseña |
| `user_type` | TEXT | No | No | Yes | — | — | Role descriptivo legacy |
| `status` | BOOLEAN | No | No | Yes | — | — | `true` si la cuenta está activa |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Person física asociada |
| `fk_id_role` | INTEGER | No | Yes | No | NULL | `roles(id)` | Role de seguridad asignado (V9) |

---

### 30. `workflow_definition`

Workflow graph definitions (Flyway V7). The seeded name `Workflow de Gestión Estándar` stays.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_definition` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del workflow |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `name` | TEXT | No | No | Yes | — | — | Nombre descriptivo del workflow |
| `description` | TEXT | No | No | No | NULL | — | Descripción funcional del proceso |
| `active` | BOOLEAN | No | No | Yes | false | — | `true` si el workflow está habilitado para uso |

---

### 31. `workflow_node`

Workflow nodes linked to management statuses (Flyway V7).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_node` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del nodo |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `fk_workflow_definition_id` | INTEGER | No | Yes | Yes | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_management_status_id` | INTEGER | No | Yes | Yes | — | `management_statuses(id)` | Estado de gestión representado por el nodo |
| `node_type` | TEXT | No | No | Yes | — | — | Tipo de nodo: `INITIAL`, `INTERMEDIATE`, `FINAL` |
| `position_x` | REAL | No | No | No | NULL | — | Coordenada visual X en el diagramador |
| `position_y` | REAL | No | No | No | NULL | — | Coordenada visual Y en el diagramador |

---

### 32. `workflow_transition`

Directed transitions between workflow nodes, with guard conditions (Flyway V7).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_transition` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la transición |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `fk_workflow_definition_id` | INTEGER | No | Yes | Yes | — | `workflow_definition(id_workflow_definition)` | Workflow contenedor (ON DELETE CASCADE) |
| `fk_origin_node_id` | INTEGER | No | Yes | Yes | — | `workflow_node(id_workflow_node)` | Nodo origen (ON DELETE CASCADE) |
| `fk_destination_node_id` | INTEGER | No | Yes | Yes | — | `workflow_node(id_workflow_node)` | Nodo destino (ON DELETE CASCADE) |
| `condition` | TEXT | No | No | No | NULL | — | Condición lógica de habilitación de la transición |
| `description` | TEXT | No | No | No | NULL | — | Descripción de la acción que dispara el avance |

---

### 33. `document_cost_templates`

Suggested cost of each required document for a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Tipo de trámite al que aplica la plantilla de costo |
| `fk_id_document_type` | INTEGER | Yes | Yes | Yes | — | `document_types(id)` | Tipo de documento al que aplica la plantilla de costo |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `fixed_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Importe fijo sugerido para el documento |
| `variable_percentage` | NUMERIC(7,4) | No | No | No | NULL | — | Porcentaje variable sugerido para el documento |

---

### 34. `notebooks`

A notary's yearly notebooks of protocol sheets.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del cuaderno |
| `version` | INTEGER | No | No | Yes | — | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número del cuaderno |
| `year_number` | INTEGER | No | No | Yes | — | — | Año al que corresponde el cuaderno |
| `notes` | TEXT | No | No | No | NULL | — | Observaciones del cuaderno |
| `fk_id_notary_person` | INTEGER | No | Yes | Yes | — | `people(id)` | Escribano titular del cuaderno |

---

### 35. `procedure_folders`

Numbered procedure folders inside a deed management (CU85).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco de la carpeta de trámite |
| `number` | INTEGER | No | No | Yes | — | — | Número correlativo de la carpeta (secuencia dedicada) |
| `status` | VARCHAR(20) | No | No | Yes | — | — | Estado de la carpeta |
| `wait_reason` | TEXT | No | No | No | NULL | — | Motivo de la espera cuando la carpeta está detenida |
| `fk_id_deed_management` | INTEGER | No | Yes | Yes | — | `deed_managements(id)` | Gestión a la que pertenece la carpeta |
| `fk_id_procedure` | INTEGER | No | Yes | Yes | — | `procedures(id)` | Trámite que origina la carpeta |

---

### 36. `registration_drafts`

Registry registration drafts for a deed, and their follow-up.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Identificador unívoco del borrador de inscripción |
| `version` | INTEGER | No | No | Yes | 0 | — | Control de concurrencia optimista |
| `number` | INTEGER | No | No | Yes | — | — | Número del borrador |
| `operation_price` | NUMERIC(19,2) | No | No | No | NULL | — | Precio de la operación a inscribir |
| `status` | VARCHAR(50) | No | No | Yes | — | — | Estado del borrador en el circuito de inscripción |
| `generation_date` | DATE | No | No | Yes | — | — | Fecha de generación del borrador |
| `submission_date` | DATE | No | No | No | NULL | — | Fecha de presentación ante el Registro |
| `registry_entry_number` | VARCHAR(255) | No | No | No | NULL | — | Número de ingreso asignado por el Registro |
| `reception_date` | DATE | No | No | No | NULL | — | Fecha de recepción por el Registro |
| `final_registration_number` | VARCHAR(255) | No | No | No | NULL | — | Número definitivo de inscripción |
| `registry_notes` | TEXT | No | No | No | NULL | — | Observaciones formuladas por el Registro |
| `correction_date` | DATE | No | No | No | NULL | — | Fecha de la corrección solicitada por el Registro |
| `fk_id_deed` | INTEGER | No | Yes | Yes | — | `deeds(id)` | Deed a inscribir |

---

### 37. `revoked_tokens`

JWT revocation list (issue #676). Logout stores the presented token's `jti` until it expires, and the authentication filter rejects every listed token. The token itself is not stored. Expired rows are purged on the next logout.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `jti` | VARCHAR(64) | Yes | No | Yes | — | — | Identificador único del JWT revocado (claim `jti`) |
| `expires_at` | TIMESTAMP WITH TIME ZONE | No | No | Yes | — | — | Vencimiento del token; después de esta fecha la fila puede purgarse |
| `revoked_at` | TIMESTAMP WITH TIME ZONE | No | No | Yes | — | — | Momento del cierre de sesión que revocó el token |

---

## 5. Referential integrity matrix

| Source table | FK column | Target table | PK column | ON DELETE |
|---|---|---|---|---|
| `copies` | `fk_id_testimony` | `testimonies` | `id` | NO ACTION |
| `copies` | `fk_id_person` | `people` | `id` | NO ACTION |
| `submitted_documents` | `fk_id_procedure` | `procedures` | `id` | NO ACTION |
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
| `items` | `fk_id_budget` | `budgets` | `id` | NO ACTION |
| `testimony_movements` | `fk_id_testimony` | `testimonies` | `id` | NO ACTION |
| `payments` | `fk_id_budget` | `budgets` | `id` | NO ACTION |
| `people` | `fk_id_identification_type` | `identification_types` | `id` | NO ACTION |
| `budget_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `budget_templates` | `fk_id_concept` | `concepts` | `id` | CASCADE |
| `procedure_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `procedure_templates` | `fk_id_document_type` | `document_types` | `id` | CASCADE |
| `budgets` | `fk_id_person` | `people` | `id` | NO ACTION |
| `audit_records` | `fk_id_user` | `users` | `id` | NO ACTION |
| `role_modules` | `fk_id_role` | `roles` | `id` | CASCADE |
| `substitutions` | `fk_id_substituted_person` | `people` | `id` | NO ACTION |
| `substitutions` | `fk_id_substitute_person` | `people` | `id` | NO ACTION |
| `testimonies` | `fk_id_deed` | `deeds` | `id` | NO ACTION |
| `procedure_types` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | NO ACTION |
| `procedures` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `procedures` | `fk_id_deed_management` | `deed_managements` | `id` | NO ACTION |
| `procedures` | `fk_id_deed` | `deeds` | `id` | NO ACTION |
| `procedures` | `fk_id_budget` | `budgets` | `id` | NO ACTION |
| `procedures` | `fk_id_property` | `properties` | `id` | NO ACTION |
| `person_procedures` | `fk_id_procedure` | `procedures` | `id` | NO ACTION |
| `person_procedures` | `fk_id_client_person` | `people` | `id` | NO ACTION |
| `users` | `fk_id_person` | `people` | `id` | NO ACTION |
| `users` | `fk_id_role` | `roles` | `id` | NO ACTION |
| `workflow_node` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_node` | `fk_management_status_id` | `management_statuses` | `id` | NO ACTION |
| `workflow_transition` | `fk_workflow_definition_id` | `workflow_definition` | `id_workflow_definition` | CASCADE |
| `workflow_transition` | `fk_origin_node_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `workflow_transition` | `fk_destination_node_id` | `workflow_node` | `id_workflow_node` | CASCADE |
| `document_cost_templates` | `fk_id_procedure_type` | `procedure_types` | `id` | NO ACTION |
| `document_cost_templates` | `fk_id_document_type` | `document_types` | `id` | NO ACTION |
| `notebooks` | `fk_id_notary_person` | `people` | `id` | NO ACTION |
| `procedure_folders` | `fk_id_deed_management` | `deed_managements` | `id` | NO ACTION |
| `procedure_folders` | `fk_id_procedure` | `procedures` | `id` | NO ACTION |
| `registration_drafts` | `fk_id_deed` | `deeds` | `id` | NO ACTION |

---

## 6. Technical notes

### 6.1 Legacy entity: `identificaciones`

**Status:** Not created by Flyway.

`identificaciones` appears in archived init scripts and in `Identification.java`, which maps to `identifications`. No migration from V1 through V44 creates that table (see the note in `V26__rename_leaf_reference_tables_to_english.sql`). V45 renames `identificaciones` to `identifications` when the legacy table is already present. It is not one of the 37 tables in this dictionary. The ERD check does not require an `identifications` entity for that conditional rename.

Each person keeps one `identification_number` and one `tax_id` on `people`, classified by `fk_id_identification_type`. Stored type names such as `DNI` and `CUIT` stay.

### 6.2 Entidades de Apoyo (Supporting Entities) sin Caso de Uso Independiente

Las siguientes 20 entidades (61% de la base de datos) no poseen un Caso de Uso independiente. Se clasifican como **entidades maestras, plantillas o de compensación** que se crean y modifican indirectamente dentro de los flujos principales:

| Categoría | Entidades | Observación |
|-----------|-----------|-------------|
| **Maestros/Catálogos** | `document_types`, `folio_types`, `procedure_types`, `identification_types`, `concepts`, `management_statuses` | Se crean vía CRUD administrativo, referenciados por CUs de negocio. |
| **Plantillas** | `budget_templates`, `procedure_templates` | Se definen una única vez y reutilizan en múltiples CUs (presupuestación, documentación). |
| **Compensación/Seguridad** | `roles`, `role_modules`, `audit_records`, `users`, `revoked_tokens`, `workflow_definition`, `workflow_node`, `workflow_transition` | Se crean durante instalación/configuración del sistema o automáticamente por auditoría/workflows. |
| **Asociativas Operacionales** | `submitted_documents`, `person_procedures`, `folio_copies`, `testimony_movements`, `substitutions`, `history`, `properties`, `items`, `payments` | Tablas débiles/asociativas creadas como parte de CUs que gestionan entidades fuertes (trámites, escrituras, presupuestos). |

**Patrón de Cobertura:**

- 13 entidades **fuertes** (39%) poseen CUs explícitas.
- 20 entidades **de apoyo/asociativas** (61%) son creadas por las 13 CUs principales o durante operaciones administrativas.

Este patrón es esperado en sistemas notariales donde la mayoría del trabajo se concentra en trámites, escrituras y presupuestos, mientras que los catálogos y configuración son actividades de administración de bajo volumen.

### 6.3 Coherencia Flyway ↔ Documentación ↔ Use Cases

**Validación 2026-10-04:**

- ✅ 37/37 tablas Flyway presentes en Diccionario de Datos (regeneradas por `docs/tools/generate_data_dictionary.py`).
- ✅ 49 Foreign Keys documentadas correctamente (matriz de la sección 5 igual al esquema).
- ✅ Cardinalidad V14 (presupuesto-trámite) reflejada en Diccionario.
- ✅ Workflows (V7/V8) y RBAC (V9) presentes y coherentes.
- `identificaciones` is not a Flyway-created table. See section 6.1.
- ℹ️  20 entidades sin CU independiente; clasificadas como "supporting" (normal para dominios notariales).

**Mantenimiento:** Cada migración Flyway debe ir acompañada, en el mismo commit, de `python3 docs/tools/generate_data_dictionary.py` (con PostgreSQL migrado) y de la descripción de cada tabla o columna nueva: el generador deja `TODO` y `docs/tests/test_data_dictionary_sync.py` (CI) falla mientras quede alguno o el Diccionario difiera del esquema. Las secciones 3 y 6 y los textos introductorios son manuales.

---

**Current version:** 3.1 (11 October 2026). Column names follow V45. Drift is guarded by `docs/tests/test_data_dictionary_sync.py`. Version 3.0 was regenerated on 4 October 2026.
