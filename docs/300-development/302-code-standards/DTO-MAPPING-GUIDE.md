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
| `DtoLoginUser` | Login response — excludes password hash, includes `valido` flag | `POST /users/login` |
| `DtoPlantillaPresupuesto` | Composite PK mapping for budget templates | `GET/POST /budget-templates` |
| `DtoWorkflowNode`, `DtoWorkflowDefinition`, `DtoWorkflowTransition` | Workflow graph editor payloads | `/workflow-definitions`, `/workflow-nodes`, `/workflow-transitions` |

### Where Direct Entity Serialization Is Used

Most CRUD endpoints return JPA entities directly:

- `GET /people` → `List<Person>` serialized as JSON
- `GET /managements` → `List<DeedManagement>` serialized as JSON

## Login DTO Pattern

The `DtoLoginUser` DTO is the primary auth response. It is built from the `UserAccount` entity in the service layer:

```java
// DtoLoginUser fields:
// idUsuario, nombre, tipo, valido (boolean), idPersona
// NOT included: contrasenia (never expose password hash)

DtoLoginUser dto = new DtoLoginUser();
dto.setNombre(usuario.getNombre());
dto.setTipo(usuario.getTipo());
dto.setValido(autenticacionExitosa);
```

## Frontend TypeScript Types

The TypeScript types in `frontend/src/types/index.ts` mirror the backend entity field names:

```typescript
// Field names come from Java getters → Jackson → JSON key
// Java: getIdTipoTramite() → JSON: "idTipoTramite"
export interface ProcedureType {
  idTipoTramite?: number;  // NOT idTipoDeTramite
  nombre?: string;
}

// Java: getIdEstadoGestion() → JSON: "idEstadoGestion"  
export interface ManagementStatus {
  idEstadoGestion?: number;  // NOT idEstadoDeGestion
  nombre?: string;
}
```

### Critical Field Name Rules

| Entity | Java field | JSON key | TypeScript field |
|--------|-----------|----------|-----------------|
| ProcedureType | `idTipoTramite` | `idTipoTramite` | `idTipoTramite` |
| ManagementStatus | `idEstadoGestion` | `idEstadoGestion` | `idEstadoGestion` |
| BudgetTemplate | `plantillaPresupuestoPK` | `plantillaPresupuestoPK` | `plantillaPresupuestoPK` |
| AuditRecord | `idRegistroAuditoria` | `idRegistroAuditoria` | `idRegistroAuditoria` |
| AuditRecord | `detalleOperacion` | `detalleOperacion` | `detalleOperacion` |

## Composite PK Handling

`BudgetTemplate` uses an `@EmbeddedId` composite key:

```java
@EmbeddedId
protected BudgetTemplateKey plantillaPresupuestoPK;
// Contains: fkIdTipoTramite, fkIdConcepto
```

The serialized JSON:

```json
{
  "plantillaPresupuestoPK": {
    "fkIdTipoTramite": 3,
    "fkIdConcepto": 7
  },
  "observaciones": "...",
  "tipoDeTramite": { "idTipoTramite": 3, "nombre": "Compraventa" },
  "concepto": { "idConcepto": 7, "nombre": "Honorario base" }
}
```

PUT/DELETE endpoints use composite path variables:

```text
PUT /api/v1/budget-templates/procedure-types/{idTipoTramite}/concept/{idConcepto}
DELETE /api/v1/budget-templates/procedure-types/{idTipoTramite}/concept/{idConcepto}
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
// In the entity class (backend-api, negocio package):
public DtoWorkflowNode toDto() {
    DtoWorkflowNode dto = new DtoWorkflowNode();
    dto.setId(this.id);
    dto.setWorkflowDefinitionId(workflowDefinition != null ? workflowDefinition.getId() : null);
    dto.setEstadoGestionId(estadoDeGestion != null ? estadoDeGestion.getIdEstadoGestion() : null);
    dto.setEstadoGestionNombre(estadoDeGestion != null ? estadoDeGestion.getNombre() : null);
    dto.setTipo(tipo != null ? tipo.name() : null);
    dto.setPosicionX(this.posicionX);
    dto.setPosicionY(this.posicionY);
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

- `DtoLoginUser.java` (`backend-api`) — primary auth response DTO
- `BudgetTemplate.java` (`backend-api`) — composite PK entity example
- `WorkflowNode.java` / `WorkflowNodeController.java` (`backend-api`) — `toDto()` mapping pattern
- `frontend/src/types/index.ts` — TypeScript mirrors of all entity types
- `JPA-LAZY-LOADING-GUIDE.md` — which entities have lazy associations and why
- `SPRING-TRANSACTION-GUIDE.md` — transaction boundary management
