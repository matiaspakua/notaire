# Notarial management system — Notaire

## Data dictionary (relational model in third normal form)

**Version:** 3.1  
**Database:** PostgreSQL 16  
**Migrations:** Flyway (V1 through V45)  
**Updated:** 11 October 2026  
**Status:** Aligned with the current PostgreSQL schema and JPA mappings (#1222, #1490). 37 Flyway tables and 49 foreign keys. Column names follow V45 (`fk_id_budget`, `birth_date`, `node_type`, and the other renames). `identificaciones` is not created by Flyway (see section 6). V14 removed `budgets.fk_id_tramite`. Workflows are V7/V8. Roles are V9.

---

## 1. Introduction

This data dictionary lists the tables, columns, data types, constraints, and foreign keys of the Notaire relational database.

### Normalization and how the schema evolved

1. **Third normal form:**
   - Civil and tax identifications are separate (`identificaciones` is not created by Flyway; `identification_types` is).
   - Master attributes live on `concepts` and `document_types`. `items` and `submitted_documents` keep only the values of that transaction.
2. **Notarial workflows (V7, V8):**
   - `workflow_definition`, `workflow_node`, and `workflow_transition` model state machines over `management_statuses` and attach them to `procedure_types`.
3. **Role-based access (V9):**
   - `roles` and `role_modules` give each user one role (`fk_id_role`).
4. **Budget-to-procedure cardinality (V14):**
   - The circular foreign key on `budgets` (`fk_id_tramite`) was removed. The canonical link is `procedures.fk_id_budget` (one budget, many procedures).
5. **Testimony movements and folios (V3, V4, V5, V6, V13):**
   - Identifier `id`, registry dates, and autonomous documents (`submitted_documents.fk_id_procedure` is nullable).

---

## 2. Table index (37 tables, Flyway V1–V45)

**Legacy entity:** `identificaciones` was documented earlier and exists in archived init scripts and in the JPA entity. Flyway V1–V44 never create it. See section 6.1.

| # | Table | Package / module | Entity type | Description |
|---|---|---|---|---|
| 1 | [concepts](#1-concepts) | Budgets | Strong | Master catalog of fees, tariffs, and stamps. Stored names such as `Honorarios` stay. |
| 2 | [copies](#2-copies) | Protocol | Weak | Printed and certified copies of testimonies |
| 3 | [submitted_documents](#3-submitted_documents) | Documents | Weak | Documents and certificates filed on a management or on their own |
| 4 | [deeds](#4-deeds) | Protocol | Strong | Source public deeds granted in a protocol |
| 5 | [management_statuses](#5-management_statuses) | Deed management | Strong | Master catalog of notarial-cycle statuses. Stored names stay. |
| 6 | [folios](#6-folios) | Protocol | Strong | Numbered protocol sheets |
| 7 | [folio_copies](#7-folio_copies) | Protocol | Associative | Many-to-many link between special folios and issued copies |
| 8 | [deed_managements](#8-deed_managements) | Deed management | Strong | Deed-management files and procedure folders |
| 9 | [history](#9-history) | Deed management | Weak | Trace of status changes on deed managements |
| 10 | [properties](#10-properties) | Deed management | Strong | Real property and cadastral details |
| 11 | [items](#11-items) | Budgets | Weak | Fee lines on each budget |
| 12 | [testimony_movements](#12-testimony_movements) | Protocol | Weak | Registry presentation entries |
| 13 | [payments](#13-payments) | Budgets | Weak | Receipts and payments on account |
| 14 | [people](#14-people) | People | Strong | People (clients, notaries, grantors) |
| 15 | [budget_templates](#15-budget_templates) | Budgets | Associative | Suggested fee concepts by procedure type |
| 16 | [procedure_templates](#16-procedure_templates) | Documents | Associative | Required documents by procedure type |
| 17 | [budgets](#17-budgets) | Budgets | Strong | Quote that supports the procedure |
| 18 | [audit_records](#18-audit_records) | Security | Weak | Audit log of user transactions |
| 19 | [roles](#19-roles) | Security | Strong | Security roles. Stored names such as `ESCRIBANO` stay. |
| 20 | [role_modules](#20-role_modules) | Security | Associative | Functional permissions granted to each role |
| 21 | [substitutions](#21-substitutions) | People | Associative | Substitution periods and notarial leave |
| 22 | [testimonies](#22-testimonies) | Protocol | Weak | Testimonies issued from source deeds |
| 23 | [document_types](#23-document_types) | Documents | Strong | Master catalog of document types and certificates |
| 24 | [folio_types](#24-folio_types) | Protocol | Strong | Classification of protocol sheets |
| 25 | [procedure_types](#25-procedure_types) | Deed management | Strong | Master catalog of notarial act types. Stored names such as `Compraventa` stay. |
| 26 | [identification_types](#26-identification_types) | People | Strong | Catalog of identification document types |
| 27 | [procedures](#27-procedures) | Deed management | Strong | One notarial act in progress |
| 28 | [person_procedures](#28-person_procedures) | Deed management | Associative | People taking part, and their legal roles |
| 29 | [users](#29-users) | Security | Weak / strong | Operator accounts and credentials |
| 30 | [workflow_definition](#30-workflow_definition) | Workflow | Strong | Workflow graph definitions |
| 31 | [workflow_node](#31-workflow_node) | Workflow | Weak | Status nodes inside a workflow |
| 32 | [workflow_transition](#32-workflow_transition) | Workflow | Weak | Directed transitions and guard rules |
| 33 | [document_cost_templates](#33-document_cost_templates) | Documents | Associative | Suggested document cost by procedure type |
| 34 | [notebooks](#34-notebooks) | Protocol | Strong | A notary's yearly protocol notebooks |
| 35 | [procedure_folders](#35-procedure_folders) | Deed management | Weak | Numbered folders for the procedures of a management |
| 36 | [registration_drafts](#36-registration_drafts) | Protocol | Weak | Registry registration drafts for deeds |
| 37 | [revoked_tokens](#37-revoked_tokens) | Security | Strong | JWT ids revoked at logout until they expire |

---

## 3. Flyway synchronization and compensation

### Active schema

The database is the result of Flyway V1–V45. Changes that matter for this model:

- V1: base relational schema for people, protocol, procedures, budgets, and documents.
- V3/V4: missing columns on `items`, `folio_types`, and `testimonies`.
- V5: `testimony_movements` matches the JPA entity (`id`, `entry_date`, `registered`, `folder_number`).
- V6: `submitted_documents.fk_id_procedure` is optional so a document can stand alone.
- V7/V8: `workflow_definition`, `workflow_node`, `workflow_transition`, and the link from `procedure_types`.
- V9: `roles`, `role_modules`, and `users.fk_id_role`.
- V13: `procedures.name` and `procedures.number` become optional. Those columns were renamed from `nombre` and `numero` in V31.
- V14: `budgets.fk_id_tramite` was removed. The canonical link is `procedures.fk_id_budget` (one to many).
- V43: `revoked_tokens`, the JWT revocation list for logout (issue #676); no foreign key.
- V45: renames the remaining Spanish columns (`fk_id_budget`, `birth_date`, `node_type`, and the rest). Applied migration filenames V1–V44 stay as recorded.

### Entity, key, and compensation matrix

| Entity | Primary key | Relevant foreign keys | Compensation | Sync note |
|---|---|---|---|---|
| `concepts` | `id` | — | No compensation | Master table; it does not depend on other entities |
| `copies` | `id` | `fk_id_testimony`, `fk_id_person` | `I: Restrict`, `M: Restrict`, `B: Restrict` (RESTRICT by default) | Issues copies of testimonies |
| `submitted_documents` | `id` | `fk_id_procedure`, `fk_id_document_type` | `I: Null` when there is no procedure, `M: Restrict`, `B: Restrict` | Matches V6: the procedure is optional |
| `deeds` | `id` | — | No compensation | Source deeds |
| `management_statuses` | `id` | — | No compensation | Status catalog |
| `folios` | `id` | `fk_id_deed`, `fk_id_folio_type`, `fk_id_notary_person` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Protocol sheet |
| `folio_copies` | `fk_id_folio + fk_id_copy` | `fk_id_folio`, `fk_id_copy` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Association table |
| `deed_managements` | `id` | `fk_id_notary_person`, `fk_id_management_status` | `I: Restrict`, `M: Restrict`, `B: Restrict` / `SET NULL` on status when left null | Groups procedures |
| `history` | `id` | `fk_id_deed_management`, `fk_id_management_status` | `I: Restrict`, `M: Restrict`, `B: Cascade` on the deed management | Status history |
| `properties` | `id` | — | No compensation | Real property |
| `items` | `id` | `fk_id_budget` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Belongs to a budget |
| `testimony_movements` | `id` | `fk_id_testimony` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Matches V5 |
| `payments` | `id` | `fk_id_budget` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Payment receipts |
| `people` | `id` | `fk_id_identification_type` | `I: Null` when it applies, `M: Restrict`, `B: Restrict` | Central entity |
| `budget_templates` | `fk_id_procedure_type + fk_id_concept` | `fk_id_procedure_type`, `fk_id_concept` | `I: Restrict`, `M: Restrict`, `B: Cascade` on `concepts` | Fee template |
| `procedure_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Restrict`, `M: Restrict`, `B: Cascade` on the document | Document requirements |
| `budgets` | `id` | `fk_id_person` | `I: Restrict`, `M: Restrict`, `B: Restrict` | The procedure foreign key was removed in V14 |
| `audit_records` | `id` | `fk_id_user` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Transaction audit |
| `roles` | `id` | — | No compensation | Role catalog |
| `role_modules` | `fk_id_role + module` | `fk_id_role` | `I: Restrict`, `M: Restrict`, `B: Cascade` | V9; permissions by module |
| `substitutions` | `id` | `fk_id_substituted_person`, `fk_id_substitute_person` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Notary coverage |
| `testimonies` | `id` | `fk_id_deed` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Testimony issued from a deed |
| `document_types` | `id` | — | No compensation | Document catalog |
| `folio_types` | `id` | — | No compensation | Folio-type catalog |
| `procedure_types` | `id` | `fk_workflow_definition_id` | `I: Null`, `M: Restrict`, `B: Restrict` (default, no cascade) | V8: the workflow is optional |
| `identification_types` | `id` | — | No compensation | Identification-type catalog |
| `procedures` | `id` | `fk_id_procedure_type`, `fk_id_deed_management`, `fk_id_deed`, `fk_id_budget`, `fk_id_property` | `I: Restrict` / `Null` by column, `M: Restrict`, `B: Restrict` or `SET NULL` by case | Canonical budget link since V14 |
| `person_procedures` | `fk_id_procedure + fk_id_client_person` | `fk_id_procedure`, `fk_id_client_person` | `I: Restrict`, `M: Restrict`, `B: Cascade` on the procedure | Participation association table |
| `users` | `id` | `fk_id_person`, `fk_id_role` | `I: Restrict` / `Null`, `M: Restrict`, `B: Restrict` | Access and authentication |
| `workflow_definition` | `id_workflow_definition` | — | No compensation | State-graph definition |
| `workflow_node` | `id_workflow_node` | `fk_workflow_definition_id`, `fk_management_status_id` | `I: Restrict`, `M: Restrict`, `B: Cascade` on the workflow | Flow nodes |
| `workflow_transition` | `id_workflow_transition` | `fk_workflow_definition_id`, `fk_origin_node_id`, `fk_destination_node_id` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Flow transitions |
| `document_cost_templates` | `fk_id_procedure_type + fk_id_document_type` | `fk_id_procedure_type`, `fk_id_document_type` | `I: Restrict`, `M: Restrict`, `B: Cascade` | Suggested cost per document |
| `notebooks` | `id` | `fk_id_notary_person` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Yearly protocol notebook |
| `procedure_folders` | `id` | `fk_id_deed_management`, `fk_id_procedure` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Numbered procedure folders (CU85) |
| `registration_drafts` | `id` | `fk_id_deed` | `I: Restrict`, `M: Restrict`, `B: Restrict` | Registry registration draft |

> Most relationships use PostgreSQL's default (RESTRICT / NO ACTION). ON DELETE CASCADE and nullable columns appear only where a migration defines them.

## 4. Table specifications

### 1. `concepts`

Master catalog of fee concepts, professional fees, contributions, and notarial taxes. Stored names such as `Honorarios` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the fee concept |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock (Hibernate) |
| `name` | TEXT | No | No | Yes | — | — | Concept name. Stored values `IVA`, `Honorarios`, `Documentacion`, and `Protocolo` stay. |
| `amount` | NUMERIC(19,2) | No | No | Yes | 0 | — | Fixed reference amount |
| `percentage` | INTEGER | No | No | Yes | 0 | — | Standard percentage applied to the act amount |
| `enabled` | BOOLEAN | No | No | Yes | — | — | Whether the concept can be used on budgets |
| `fixed_concept` | BOOLEAN | No | No | Yes | — | — | `true` when the amount is fixed, `false` when it is a percentage |

---

### 2. `copies`

Printed copies issued from a source notarial testimony.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the copy |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Serial number of the issued copy |
| `print_date` | DATE | No | No | Yes | — | — | Date the copy was issued and printed |
| `pickup_date` | DATE | No | No | No | NULL | — | Date the interested party picked it up |
| `notes` | TEXT | No | No | No | NULL | — | Delivery record or attestations |
| `fk_id_testimony` | INTEGER | No | Yes | No | NULL | `testimonies(id)` | Source testimony |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Person the copy is issued or delivered to |

---

### 3. `submitted_documents`

Documents and certificates filed for a procedure, or created on their own (V6).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the document |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Name of the submitted document |
| `folder_number` | INTEGER | No | No | No | NULL | — | External-agency intake folder number |
| `entry_date` | DATE | No | No | No | NULL | — | Date it was filed with or requested from an external agency |
| `exit_date` | DATE | No | No | No | NULL | — | Date the agency returned it |
| `prepared` | BOOLEAN | No | No | Yes | — | — | `true` when it was prepared in the practice |
| `expires` | BOOLEAN | No | No | Yes | — | — | Whether the document expires |
| `due_date` | DATE | No | No | No | NULL | — | Legal expiry date |
| `due_days` | INTEGER | No | No | No | NULL | — | Validity period in days |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Stamp or tax amount actually charged |
| `payment_date` | DATE | No | No | No | NULL | — | Date the tax payment was completed |
| `released` | BOOLEAN | No | No | Yes | — | — | `true` when a clearance certificate was issued |
| `released_date` | DATE | No | No | No | NULL | — | Date the clearance was recorded |
| `observed` | BOOLEAN | No | No | Yes | — | — | `true` when the agency recorded technical observations |
| `notes` | TEXT | No | No | No | NULL | — | Technical notes or correction requirements |
| `delivered` | BOOLEAN | No | No | No | false | — | `true` when it was delivered to the requesting entity |
| `reentered` | BOOLEAN | No | No | No | NULL | — | `true` when it was re-entered after a correction (nullable since V6) |
| `delivered_by` | TEXT | No | No | Yes | — | — | Supply origin (`Cliente` or `Entidad Externa`). Those stored values stay. |
| `fk_id_procedure` | INTEGER | No | Yes | No | NULL | `procedures(id)` | Procedure it belongs to (optional since V6) |
| `fk_id_document_type` | INTEGER | No | Yes | No | NULL | `document_types(id)` | Master document type |

---

### 4. `deeds`

The source deed granted in the notarial protocol.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Internal id of the source deed |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Yearly serial number of the deed in the protocol |
| `deed_date` | DATE | No | No | Yes | — | — | Date the act was granted |
| `body` | TEXT | No | No | Yes | — | — | Full legal text of the protocol deed |
| `status` | TEXT | No | No | Yes | — | — | Deed status. Stored values `Sin Firmar`, `Firmada`, `Anulada`, `No Paso`, and `Inscripta` stay. |
| `registration_number` | TEXT | No | No | No | NULL | — | Registration number granted by the registry |
| `registration_date` | DATE | No | No | No | NULL | — | Final registration date |
| `notes` | TEXT | No | No | No | NULL | — | Marginal notes and notarial attestations |

---

### 5. `management_statuses`

Catalog of operational statuses a deed management can move through. Stored status names stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Management-status id |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Status name. Stored examples such as `Iniciada`, `En Tramite`, and `Testimonio Generado` stay. |
| `notes` | TEXT | No | No | No | NULL | — | Functional definition and conditions. Stored notes `Inicio Gestion`, `Post-signing: testimony generated from signed deed`, `Post-signing: testimony submitted for inscription`, and `Post-signing: testimony withdrawn after inscription cycle` stay. |

---

### 6. `folios`

Protocol sheets supplied for recording public deeds.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the protocol sheet |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Official serial number printed on the sheet |
| `year_number` | INTEGER | No | No | Yes | — | — | Calendar year of the protocol |
| `status` | TEXT | No | No | Yes | — | — | Status. Stored values `Nuevo`, `Utilizado`, `Errose`, `no pasó`, and `Asignado a cuaderno` stay. |
| `notes` | TEXT | No | No | No | NULL | — | Notes on exceptions or attestations |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Deed that used this sheet |
| `fk_id_folio_type` | INTEGER | No | Yes | Yes | — | `folio_types(id)` | How the sheet is classified |
| `fk_id_notary_person` | INTEGER | No | Yes | Yes | — | `people(id)` | Notary responsible for the register |
| `fk_id_notebook` | INTEGER | No | Yes | No | NULL | `notebooks(id)` | Notebook this sheet belongs to |

---

### 7. `folio_copies`

Associates testimony sheets with the copies that were issued.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_folio` | INTEGER | Yes | Yes | Yes | — | `folios(id)` | Special testimony sheet |
| `fk_id_copy` | INTEGER | Yes | Yes | Yes | — | `copies(id)` | Issued copy |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |

---

### 8. `deed_managements`

File that groups one or more related procedures.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the deed management |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Yearly serial number of the management |
| `start_date` | DATE | No | No | Yes | — | — | Date the file was opened |
| `heading` | TEXT | No | No | Yes | — | — | Heading that describes the management |
| `notes` | TEXT | No | No | No | NULL | — | Working notes |
| `fk_id_notary_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Notary in charge |
| `fk_id_management_status` | INTEGER | No | Yes | No | NULL | `management_statuses(id)` | Current operational status |
| `pending_debt_on_archive` | BOOLEAN | No | No | No | NULL | — | `true` when the management was archived with a pending debt |

---

### 9. `history`

Chronological history of status changes for a deed management.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | History-row id |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `event_date` | DATE | No | No | Yes | — | — | Date of the status change |
| `notes` | TEXT | No | No | No | NULL | — | Reason or attestation for the status change. Stored notes `Apertura de la gestión`, `Trámite de compraventa en curso`, `Documentación completa recibida`, and `Trámite de donación en curso` stay. |
| `fk_id_deed_management` | INTEGER | No | Yes | No | NULL | `deed_managements(id)` | Deed management that changed status |
| `fk_id_management_status` | INTEGER | No | Yes | No | NULL | `management_statuses(id)` | Status reached |

---

### 10. `properties`

Real property and its cadastral and registry details.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the property |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `nomenclature` | TEXT | No | No | No | NULL | — | Official cadastral nomenclature |
| `registration_number` | TEXT | No | No | No | NULL | — | Property-registry registration number |
| `valuation_year` | INTEGER | No | No | No | NULL | — | Year of the fiscal valuation |
| `fiscal_valuation` | NUMERIC(19,2) | No | No | No | NULL | — | Official fiscal valuation amount |
| `record_number` | INTEGER | No | No | No | NULL | — | Property parcel number |
| `district` | TEXT | No | No | No | NULL | — | District |
| `section` | TEXT | No | No | No | NULL | — | Section |
| `zone` | TEXT | No | No | No | NULL | — | Zone |
| `block` | TEXT | No | No | No | NULL | — | Block |
| `parcel` | TEXT | No | No | No | NULL | — | Parcel |
| `polygon` | TEXT | No | No | No | NULL | — | Polygon |
| `functional_unit` | TEXT | No | No | No | NULL | — | Functional unit |
| `address` | TEXT | No | No | No | NULL | — | Street address of the property |
| `locality` | TEXT | No | No | No | NULL | — | Locality |
| `notes` | TEXT | No | No | No | NULL | — | Boundaries and other notes |
| `registry_volume_folio` | VARCHAR(255) | No | No | No | NULL | — | Volume and folio of the property-registry inscription |
| `boundaries` | TEXT | No | No | No | NULL | — | Property boundaries |

---

### 11. `items`

Fee lines charged on a budget (V3/V4).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the line |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Concept or charged line |
| `amount` | NUMERIC(19,2) | No | No | Yes | — | — | Resulting line amount |
| `percentage` | INTEGER | No | No | Yes | — | — | Percentage applied |
| `fixed_concept` | BOOLEAN | No | No | Yes | — | — | `true` when the amount is fixed, `false` when it is a percentage |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Budget this line belongs to |
| `notes` | TEXT | No | No | No | NULL | — | Justification notes (V3/V4) |
| `item_type` | VARCHAR(20) | No | No | Yes | 'NORMAL' | — | Budget line class. Stored values `NORMAL`, `DESCUENTO`, and `RECARGO` stay. The default is `NORMAL`. |
| `reason` | VARCHAR(255) | No | No | No | NULL | — | Stated reason when the line is not an ordinary concept |

---

### 12. `testimony_movements`

Registry presentation entries for a testimony (V5).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Registry-movement id (V5) |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `entry_date` | DATE | No | No | Yes | — | — | Entry date of the movement (V5) |
| `notes` | TEXT | No | No | No | NULL | — | Registry notes |
| `fk_id_testimony` | INTEGER | No | Yes | No | NULL | `testimonies(id)` | Testimony this movement belongs to |
| `exit_date` | DATE | No | No | No | NULL | — | Date the registry returned it (V5) |
| `registration_date` | DATE | No | No | No | NULL | — | Date registration was completed (V5) |
| `registered` | BOOLEAN | No | No | Yes | false | — | `true` when registration succeeded (V5) |
| `folder_number` | INTEGER | No | No | Yes | 0 | — | Presentation folder number (V5) |
| `observed_by_registry` | BOOLEAN | No | No | Yes | false | — | `true` when the registry returned the testimony as observed on re-entry (CU44); observations are required |

---

### 13. `payments`

Receipts applied to a budget.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the receipt |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `payment_date` | DATE | No | No | Yes | — | — | Payment date |
| `amount` | NUMERIC(19,2) | No | No | Yes | — | — | Amount received |
| `notes` | TEXT | No | No | No | NULL | — | Payment method and evidence |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Budget this payment applies to |
| `payment_method` | TEXT | No | No | No | NULL | — | Payment method used |

---

### 14. `people`

People, natural or legal, who take part in the practice.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the person |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `last_name` | TEXT | No | No | Yes | — | — | Last name or legal name |
| `first_name` | TEXT | No | No | Yes | — | — | First name or trade name |
| `identification_number` | TEXT | No | No | Yes | — | — | Primary identification number |
| `tax_id` | TEXT | No | No | No | NULL | — | Tax id (`CUIT` / `CUIL`). Those stored names stay. |
| `sex` | TEXT | No | No | No | NULL | — | Legal sex. Stored values `Masculino` and `Femenino` stay. |
| `birth_date` | DATE | No | No | No | NULL | — | Birth date |
| `marital_status` | TEXT | No | No | No | NULL | — | Marital status. Stored values `Soltero` and `Casado` stay. |
| `marriage_count` | INTEGER | No | No | No | NULL | — | Marriage count |
| `occupation` | TEXT | No | No | No | NULL | — | Occupation |
| `address` | TEXT | No | No | No | NULL | — | Residential or legal address |
| `email` | TEXT | No | No | No | NULL | — | Primary email |
| `notary_registration_number` | INTEGER | No | No | No | NULL | — | Notary registration number |
| `is_client` | BOOLEAN | No | No | Yes | false | — | `true` when the person is a client of the practice |
| `locality` | TEXT | No | No | No | NULL | — | Locality |
| `province` | TEXT | No | No | No | NULL | — | Province |
| `phone` | TEXT | No | No | No | NULL | — | Landline |
| `mobile_phone` | TEXT | No | No | No | NULL | — | Mobile phone |
| `secondary_email` | TEXT | No | No | No | NULL | — | Secondary or historical email |
| `nationality` | TEXT | No | No | No | NULL | — | Nationality |
| `profession` | TEXT | No | No | No | NULL | — | Profession or trade |
| `notes` | TEXT | No | No | No | NULL | — | File notes and background |
| `fk_id_identification_type` | INTEGER | No | Yes | No | NULL | `identification_types(id)` | Primary identification type |
| `is_notary` | BOOLEAN | No | No | Yes | false | — | `true` when the person is a licensed notary |

---

### 15. `budget_templates`

Associates standard fee concepts with a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Procedure type |
| `fk_id_concept` | INTEGER | Yes | Yes | Yes | — | `concepts(id)` | Budgeted concept |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `notes` | TEXT | No | No | No | NULL | — | Special calculation rules |

---

### 16. `procedure_templates`

Associates required documents and certificates with a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Procedure type |
| `fk_id_document_type` | INTEGER | Yes | Yes | Yes | — | `document_types(id)` | Required document or certificate |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `notes` | TEXT | No | No | No | NULL | — | Specific filing instructions |

---

### 17. `budgets`

Quote issued to a client. V14 removed the redundant foreign key `fk_id_tramite`. The canonical link is `procedures.fk_id_budget`. Stored statuses `BORRADOR`, `PENDIENTE`, `APROBADO`, `RECHAZADO`, and `FACTURADO` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the budget |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Budget serial number |
| `budget_date` | DATE | No | No | Yes | — | — | Issue date |
| `heading` | TEXT | No | No | Yes | — | — | Descriptive heading of the quote |
| `notes` | TEXT | No | No | No | NULL | — | Validity conditions and deadlines |
| `status` | TEXT | No | No | Yes | — | — | Status. Stored codes `BORRADOR`, `PENDIENTE`, `APROBADO`, `RECHAZADO`, and `FACTURADO` stay. |
| `property_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Declared property amount |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Requesting client |

---

### 18. `audit_records`

Audit log of security events and business transactions.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the audited event |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `date` | TIMESTAMP | No | No | Yes | — | — | Exact timestamp of the transaction |
| `module` | TEXT | No | No | Yes | — | — | Functional module affected |
| `operation_detail` | TEXT | No | No | Yes | — | — | Changed data or the action that ran |
| `fk_id_user` | INTEGER | No | Yes | No | NULL | `users(id)` | Operator who performed the action |

---

### 19. `roles`

Security roles (Flyway V9). Stored names such as `ESCRIBANO` and `ADMIN` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the role |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Unique role name. Stored values such as `ADMIN`, `ESCRIBANO`, and `SECRETARIA` stay. |
| `description` | TEXT | No | No | No | NULL | — | Scope and responsibilities of the role |
| `active` | BOOLEAN | No | No | Yes | true | — | Whether the role is enabled |

---

### 20. `role_modules`

Module permissions granted to each role (Flyway V9). The column is `module`.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_role` | INTEGER | Yes | Yes | Yes | — | `roles(id)` | Role that receives the permission |
| `module` | TEXT | Yes | No | Yes | — | — | Id of the authorized module or action. The column is `module`. |

---

### 21. `substitutions`

Substitution periods between notaries.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the substitution |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `start_date` | DATE | No | No | Yes | — | — | Start date of the substitution |
| `end_date` | DATE | No | No | No | NULL | — | End date |
| `notes` | TEXT | No | No | No | NULL | — | Reason or legal attestation |
| `fk_id_substituted_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Notary on leave |
| `fk_id_substitute_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Substitute notary |

---

### 22. `testimonies`

Testimonies issued from source deeds (V3/V4).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the testimony |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Testimony number (first, second, and so on) |
| `notes` | TEXT | No | No | No | NULL | — | Registry or delivery notes |
| `registration_date` | DATE | No | No | No | NULL | — | Registration date |
| `pickup_date` | DATE | No | No | No | NULL | — | Date delivered to the client |
| `book_entry_date` | DATE | No | No | No | NULL | — | Date entered in the book |
| `folder_number` | INTEGER | No | No | No | NULL | — | Registry folder number |
| `file_number` | INTEGER | No | No | No | NULL | — | Registry file number |
| `reentered` | BOOLEAN | No | No | Yes | false | — | `true` when it was re-entered |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Source deed |
| `observed` | BOOLEAN | No | No | Yes | false | — | `true` when it was observed (V3/V4) |
| `verified` | BOOLEAN | No | No | Yes | false | — | `true` when the testimony was verified |

---

### 23. `document_types`

Master catalog of documents, prior titles, and registry certificates.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the document type |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Name of the document or certificate. Stored values `Libre Deuda Municipal`, `Informe de Dominio`, `Certificado Catastral`, and `Fotocopia DNI` stay. |
| `returned` | BOOLEAN | No | No | Yes | — | — | `true` when the original must be returned to the client |
| `expires` | BOOLEAN | No | No | Yes | — | — | `true` when the certificate expires |
| `due_days` | INTEGER | No | No | No | NULL | — | Legal validity in days |
| `amount_to_pay` | NUMERIC(19,2) | No | No | No | NULL | — | Standard fee |
| `enabled` | BOOLEAN | No | No | Yes | — | — | `true` when it can be used on templates |
| `delivered_by` | TEXT | No | No | Yes | — | — | Origin. Stored values `Cliente`, `Entidad`, and `Entidad Externa` stay. |

---

### 24. `folio_types`

Master catalog of protocol sheet types (V3/V4). Stored names such as `Protocolo Principal` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the folio type |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Name. Stored values `De documento`, `De actuacion`, `De certificacion`, `Protocolo Principal`, `Protocolo Auxiliar`, and `Testimonio` stay. |
| `notes` | TEXT | No | No | No | NULL | — | Folio-type notes (V3/V4) |
| `enabled` | BOOLEAN | No | No | Yes | true | — | `true` when it can be used for folio batches (V3/V4) |
| `is_auxiliary` | BOOLEAN | No | No | Yes | false | — | `true` when the type is an auxiliary sheet |

---

### 25. `procedure_types`

Master catalog of notarial act types (V8). Stored names such as `Compraventa` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Procedure-type id |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Procedure-type name. Stored examples such as `Compraventa` and `Poder` stay. |
| `notes` | TEXT | No | No | No | NULL | — | Rules and notarial description. Stored notes `Tramite de compraventa de inmuebles`, `Tramite de donacion`, `Tramite de hipoteca`, `Tramite de sucesion`, and `Otorgamiento de poderes` stay. |
| `enabled` | BOOLEAN | No | No | Yes | — | — | `true` when new managements are allowed |
| `is_archived` | BOOLEAN | No | No | Yes | — | — | `true` when a physical binder is required |
| `is_registered` | BOOLEAN | No | No | Yes | — | — | `true` when registry registration is required |
| `associates_properties` | BOOLEAN | No | No | Yes | — | — | `true` when the act involves property |
| `fk_workflow_definition_id` | INTEGER | No | Yes | No | NULL | `workflow_definition(id_workflow_definition)` | Workflow assigned to the procedure type (V8) |

---

### 26. `identification_types`

Master catalog of identification document types. Stored names such as `DNI` and `CUIT` stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the document type |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Name. Stored values `DNI`, `CUIT`, `CUIL`, `Pasaporte`, `CI`, `LC`, and `LE` stay. |
| `characters` | TEXT | No | No | Yes | — | — | Format or alphanumeric mask |

---

### 27. `procedures`

One procedure instance (V13/V14).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the procedure |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | No | NULL | — | Procedure serial number (optional since V13) |
| `name` | TEXT | No | No | No | NULL | — | Descriptive heading (optional since V13) |
| `notes` | TEXT | No | No | No | NULL | — | Instructions or observations |
| `fk_id_procedure_type` | INTEGER | No | Yes | No | NULL | `procedure_types(id)` | Procedure type |
| `fk_id_deed_management` | INTEGER | No | Yes | No | NULL | `deed_managements(id)` | Deed management that groups it (null for auxiliary work) |
| `fk_id_deed` | INTEGER | No | Yes | No | NULL | `deeds(id)` | Resulting deed |
| `fk_id_budget` | INTEGER | No | Yes | No | NULL | `budgets(id)` | Base budget (V14) |
| `fk_id_property` | INTEGER | No | Yes | No | NULL | `properties(id)` | Property the act concerns, when there is one |

---

### 28. `person_procedures`

Associates people with a procedure and records their legal role. Stored role labels stay.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure` | INTEGER | Yes | Yes | Yes | — | `procedures(id)` | Procedure the person joins |
| `fk_id_client_person` | INTEGER | Yes | Yes | Yes | — | `people(id)` | Person taking part |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `notes` | TEXT | No | No | Yes | '' | — | Notarial role. Stored labels such as `Comprador`, `Vendedor`, `Donante`, and `Apoderado` stay. |

---

### 29. `users`

Operator accounts (V9).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the user |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `username` | TEXT | No | No | Yes | — | — | Username (unique login) |
| `password` | TEXT | No | No | Yes | — | — | Password hash |
| `user_type` | TEXT | No | No | Yes | — | — | Legacy descriptive role. Stored values `Escribano`, `Empleado`, `EMPLEADO`, `ADMIN`, `ADMINISTRADOR`, and `ESCRIBANO` stay. |
| `status` | BOOLEAN | No | No | Yes | — | — | `true` when the account is active |
| `fk_id_person` | INTEGER | No | Yes | No | NULL | `people(id)` | Linked person |
| `fk_id_role` | INTEGER | No | Yes | No | NULL | `roles(id)` | Assigned security role (V9) |

---

### 30. `workflow_definition`

Workflow graph definitions (Flyway V7). The seeded name `Workflow de Gestión Estándar` stays.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_definition` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the workflow |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `name` | TEXT | No | No | Yes | — | — | Workflow name. The seeded value `Workflow de Gestión Estándar` stays. |
| `description` | TEXT | No | No | No | NULL | — | Functional description of the process. The seeded value `Ciclo de vida estándar de una gestión de escritura` stays. |
| `active` | BOOLEAN | No | No | Yes | false | — | `true` when the workflow is enabled |

---

### 31. `workflow_node`

Workflow nodes linked to management statuses (Flyway V7).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_node` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the node |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `fk_workflow_definition_id` | INTEGER | No | Yes | Yes | — | `workflow_definition(id_workflow_definition)` | Parent workflow (ON DELETE CASCADE) |
| `fk_management_status_id` | INTEGER | No | Yes | Yes | — | `management_statuses(id)` | Management status this node represents |
| `node_type` | TEXT | No | No | Yes | — | — | Node type: `INITIAL`, `INTERMEDIATE`, `FINAL` |
| `position_x` | REAL | No | No | No | NULL | — | Visual X coordinate in the designer |
| `position_y` | REAL | No | No | No | NULL | — | Visual Y coordinate in the designer |

---

### 32. `workflow_transition`

Directed transitions between workflow nodes, with guard conditions (Flyway V7).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id_workflow_transition` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the transition |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `fk_workflow_definition_id` | INTEGER | No | Yes | Yes | — | `workflow_definition(id_workflow_definition)` | Parent workflow (ON DELETE CASCADE) |
| `fk_origin_node_id` | INTEGER | No | Yes | Yes | — | `workflow_node(id_workflow_node)` | Origin node (ON DELETE CASCADE) |
| `fk_destination_node_id` | INTEGER | No | Yes | Yes | — | `workflow_node(id_workflow_node)` | Destination node (ON DELETE CASCADE) |
| `condition` | TEXT | No | No | No | NULL | — | Guard condition for the transition |
| `description` | TEXT | No | No | No | NULL | — | Description of the action that advances the flow. Stored values `Se asignan trámites y comienza la gestión`, `Toda la documentación requerida fue presentada`, `Se redacta la escritura`, `Las partes firman la escritura`, `La gestión se archiva sin escritura`, `Testimony generated after deed signing`, `Testimony submitted for inscription`, and `Testimony withdrawn after inscription cycle` stay. |

---

### 33. `document_cost_templates`

Suggested cost of each required document for a procedure type.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `fk_id_procedure_type` | INTEGER | Yes | Yes | Yes | — | `procedure_types(id)` | Procedure type this cost template applies to |
| `fk_id_document_type` | INTEGER | Yes | Yes | Yes | — | `document_types(id)` | Document type this cost template applies to |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `fixed_amount` | NUMERIC(19,2) | No | No | No | NULL | — | Suggested fixed amount for the document |
| `variable_percentage` | NUMERIC(7,4) | No | No | No | NULL | — | Suggested variable percentage for the document |

---

### 34. `notebooks`

A notary's yearly notebooks of protocol sheets.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the notebook |
| `version` | INTEGER | No | No | Yes | — | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Notebook number |
| `year_number` | INTEGER | No | No | Yes | — | — | Year of the notebook |
| `notes` | TEXT | No | No | No | NULL | — | Notebook notes |
| `fk_id_notary_person` | INTEGER | No | Yes | Yes | — | `people(id)` | Notary who owns the notebook |

---

### 35. `procedure_folders`

Numbered procedure folders inside a deed management (CU85).

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the procedure folder |
| `number` | INTEGER | No | No | Yes | — | — | Folder serial number (its own sequence) |
| `status` | VARCHAR(20) | No | No | Yes | — | — | Folder status. Stored labels `Activa`, `Espera`, and `Archivada` stay. |
| `wait_reason` | TEXT | No | No | No | NULL | — | Reason the folder is waiting |
| `fk_id_deed_management` | INTEGER | No | Yes | Yes | — | `deed_managements(id)` | Deed management this folder belongs to |
| `fk_id_procedure` | INTEGER | No | Yes | Yes | — | `procedures(id)` | Procedure that created the folder |

---

### 36. `registration_drafts`

Registry registration drafts for a deed, and their follow-up.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `id` | SERIAL (INT) | Yes | No | Yes | Auto | — | Unique id of the registration draft |
| `version` | INTEGER | No | No | Yes | 0 | — | Optimistic lock |
| `number` | INTEGER | No | No | Yes | — | — | Draft number |
| `operation_price` | NUMERIC(19,2) | No | No | No | NULL | — | Price of the operation to register |
| `status` | VARCHAR(50) | No | No | Yes | — | — | Draft status in the registration flow. Stored labels `Generada`, `Presentado para inscripción`, `Observado`, and `Inscripto` stay. |
| `generation_date` | DATE | No | No | Yes | — | — | Date the draft was created |
| `submission_date` | DATE | No | No | No | NULL | — | Date it was filed with the registry |
| `registry_entry_number` | VARCHAR(255) | No | No | No | NULL | — | Intake number assigned by the registry |
| `reception_date` | DATE | No | No | No | NULL | — | Date the registry received it |
| `final_registration_number` | VARCHAR(255) | No | No | No | NULL | — | Final registration number |
| `registry_notes` | TEXT | No | No | No | NULL | — | Observations from the registry |
| `correction_date` | DATE | No | No | No | NULL | — | Date of the correction the registry requested |
| `fk_id_deed` | INTEGER | No | Yes | Yes | — | `deeds(id)` | Deed to register |

---

### 37. `revoked_tokens`

JWT revocation list (issue #676). Logout stores the presented token's `jti` until it expires, and the authentication filter rejects every listed token. The token itself is not stored. Expired rows are purged on the next logout.

| Column | Data type | PK | FK | Not null | Default | Reference | Description |
|---|---|---|---|---|---|---|---|
| `jti` | VARCHAR(64) | Yes | No | Yes | — | — | Unique id of the revoked JWT (`jti` claim) |
| `expires_at` | TIMESTAMP WITH TIME ZONE | No | No | Yes | — | — | Token expiry; the row may be purged after this date |
| `revoked_at` | TIMESTAMP WITH TIME ZONE | No | No | Yes | — | — | Logout time that revoked the token |

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

### 6.2 Supporting entities without their own use case

These 20 entities (61% of the tables) have no use case of their own. They are **master data, templates, or compensation tables** created inside the main flows:

| Category | Entities | Note |
|-----------|-----------|-------------|
| **Master catalogs** | `document_types`, `folio_types`, `procedure_types`, `identification_types`, `concepts`, `management_statuses` | Created through administrative CRUD and referenced by business use cases. |
| **Templates** | `budget_templates`, `procedure_templates` | Defined once and reused by several use cases (budgeting, documents). |
| **Compensation and security** | `roles`, `role_modules`, `audit_records`, `users`, `revoked_tokens`, `workflow_definition`, `workflow_node`, `workflow_transition` | Created during setup, or automatically by audit and workflows. |
| **Operational associations** | `submitted_documents`, `person_procedures`, `folio_copies`, `testimony_movements`, `substitutions`, `history`, `properties`, `items`, `payments` | Weak or association tables created by use cases that manage strong entities (procedures, deeds, budgets). |

**Coverage pattern:**

- 13 **strong** entities (39%) have an explicit use case.
- 20 **supporting or association** entities (61%) are created by those use cases or by administration.

That split is expected: most of the work is procedures, deeds, and budgets, while catalogs and configuration are low-volume administration.

### 6.3 Flyway, this dictionary, and use cases

**Check on 11 October 2026:**

- 37/37 Flyway tables in this dictionary, guarded by `docs/tests/test_data_dictionary_sync.py`.
- 49 foreign keys. Section 5 matches the ERD.
- V14 cardinality (budget to procedure) is reflected here.
- Workflows (V7/V8) and RBAC (V9) are present.
- `identificaciones` is not a Flyway-created table. See section 6.1.
- 20 entities have no use case of their own and are classified as supporting.

**Maintenance:** Each Flyway migration should be followed, in the same commit, by `python3 docs/tools/generate_data_dictionary.py` against a migrated PostgreSQL, plus a description of every new table or column. The generator writes `TODO`, and `docs/tests/test_data_dictionary_sync.py` fails while any `TODO` remains or the dictionary drifts from the ERD. Sections 1, 3, and 6 are written by hand.

---

**Current version:** 3.1 (11 October 2026). Column names follow V45. Drift is guarded by `docs/tests/test_data_dictionary_sync.py`. Version 3.0 was regenerated on 4 October 2026.
