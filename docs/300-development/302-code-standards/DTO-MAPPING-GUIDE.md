# DTO Mapping and Transformation Guide

> Closes issue #300

## Overview

This document describes how Notaire maps between JPA entities and Data Transfer Objects (DTOs), and the patterns for serialization in the REST API layer.

## Current Architecture

Notaire uses **direct entity serialization** — JPA entities are serialized as JSON directly from the REST controllers, without a separate DTO layer for most endpoints. DTOs exist for specific use cases where the full entity should not be exposed.

DTO classes live in `backend-api` (`com.licensis.notaire.dto`). They used to sit in a separate `notaire-shared` module, retired in [ADR-025](../../200-architecture/202-ADR/ADR-025-retire-notaire-shared.md). External services and clients consume the REST API (`/api/v1`) through the OpenAPI contract (`backend-api/openapi/openapi.yaml`), never a Java DTO library.

### Where DTOs Are Used

| DTO class | Purpose | Endpoint |
|-----------|---------|----------|
| `DtoUser` | Login response — excludes the password hash, includes the `valid` flag | `POST /users/login` |
| `BudgetTemplate` | Composite PK mapping for budget templates (`budgetTemplatePK`) | `GET/POST /budget-templates` |
| `DtoWorkflowNode`, `DtoWorkflowDefinition`, `DtoWorkflowTransition` | Workflow graph editor payloads | `/workflow-definitions`, `/workflow-nodes`, `/workflow-transitions` |

### Where Direct Entity Serialization Is Used

Most CRUD endpoints return JPA entities directly:

- `GET /people` → `List<Person>` serialized as JSON
- `GET /managements` → `List<DeedManagement>` serialized as JSON

## Login DTO Pattern

The `DtoUser` DTO is the primary auth response. `UserController` builds it from the `User` entity. The HTTP body uses `valid`, `token`, `idUser`, `name`, `status`, `type`, `version`, and `person` (`idPerson`, `firstName`, `lastName`). The password is write-only and is not returned.

```java
DtoUser dtoUser = new DtoUser();
dtoUser.setIdUser(user.getIdUser());
dtoUser.setName(user.getName());
dtoUser.setStatus(user.getStatus());
dtoUser.setType(user.getType());
dtoUser.setValid(true);
```

## Frontend TypeScript Types

The TypeScript types in `frontend/src/types/index.ts` mirror the backend entity field names:

```typescript
// Field names come from Java getters → Jackson → JSON key
// Java: getIdProcedureType() → JSON: "idProcedureType"
export interface ProcedureType {
  idProcedureType?: number;
  name?: string;
}

// Java: getIdManagementStatus() → JSON: "idManagementStatus"
export interface ManagementStatus {
  idManagementStatus?: number;
  name?: string;
}
```

### Critical Field Name Rules

| Entity | Java field | JSON key | TypeScript field |
|--------|-----------|----------|-----------------|
| ProcedureType | `idProcedureType` | `idProcedureType` | `idProcedureType` |
| ManagementStatus | `idManagementStatus` | `idManagementStatus` | `idManagementStatus` |
| BudgetTemplate | `budgetTemplatePK` | `budgetTemplatePK` | `budgetTemplatePK` |
| AuditRecord | `idAuditRecord` | `idAuditRecord` | `idAuditRecord` |
| AuditRecord | `operationDetail` | `operationDetail` | `operationDetail` |

## Composite PK Handling

`BudgetTemplate` uses an `@EmbeddedId` composite key:

```java
@EmbeddedId
protected BudgetTemplatePK budgetTemplatePK;
// Contains: fkIdProcedureType, fkIdConcept
```

The serialized JSON:

```json
{
  "budgetTemplatePK": {
    "fkIdProcedureType": 3,
    "fkIdConcept": 7
  },
  "notes": "...",
  "procedureType": { "idProcedureType": 3, "name": "Compraventa" },
  "concept": { "idConcept": 7, "name": "Honorarios" }
}
```

PUT/DELETE endpoints use composite path variables:

```text
PUT /api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}
DELETE /api/v1/budget-templates/procedure-types/{idProcedureType}/concept/{idConcept}
```

## Lazy Loading and JSON Serialization

Some entities have `@ManyToOne` or `@OneToMany` with lazy loading. When Jackson tries to serialize a lazy proxy outside an open Hibernate session, it throws a `LazyInitializationException`.

**Current mitigation**: `spring.jpa.open-in-view=false` is configured, and controller/service methods that touch lazy-loaded associations are annotated `@Transactional(readOnly = true)` so the session stays open through the full `.map(Entity::getDto)` call. See `JPA-LAZY-LOADING-GUIDE.md` for the entity-by-entity breakdown and `SPRING-TRANSACTION-GUIDE.md` for the transaction-boundary rules.

## Adding a New DTO

When a new endpoint should return a reduced view of an entity:

1. Create the DTO class in `backend-api`, package `com.licensis.notaire.dto`
2. Add a `toDto()` method to the entity class (in `backend-api`)
3. Return the DTO from the controller instead of the entity

Real example, `WorkflowNode` → `DtoWorkflowNode`:

```java
public DtoWorkflowNode toDto() {
    DtoWorkflowNode dto = new DtoWorkflowNode();
    dto.setId(this.id);
    dto.setWorkflowDefinitionId(workflowDefinition != null ? workflowDefinition.getId() : null);
    dto.setStatusManagementId(managementStatus != null ? managementStatus.getIdManagementStatus() : null);
    dto.setStatusManagementName(managementStatus != null ? managementStatus.getName() : null);
    dto.setType(type != null ? type.name() : null);
    dto.setPositionX(this.positionX);
    dto.setPositionY(this.positionY);
    dto.setVersion(this.version);
    return dto;
}

// In the controller:
@GetMapping("/{id}")
public ResponseEntity<DtoWorkflowNode> getById(@PathVariable Integer id) {
    return repository.findById(id)
            .map(n -> ResponseEntity.ok(n.toDto()))
            .orElse(ResponseEntity.notFound().build());
}
```

## References

- `DtoUser.java` (`backend-api`) — primary auth response DTO
- `BudgetTemplate.java` (`backend-api`) — composite PK entity example
- `WorkflowNode.java` / `WorkflowNodeController.java` (`backend-api`) — `toDto()` mapping pattern
- `frontend/src/types/index.ts` — TypeScript mirrors of all entity types
- `JPA-LAZY-LOADING-GUIDE.md` — which entities have lazy associations and why
- `SPRING-TRANSACTION-GUIDE.md` — transaction boundary management
