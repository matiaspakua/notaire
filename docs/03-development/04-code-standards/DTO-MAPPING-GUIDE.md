# DTO Mapping and Transformation Guide

> Closes issue #300

## Overview

This document describes how Notaire maps between JPA entities and Data Transfer Objects (DTOs), and the patterns for serialization in the REST API layer.

## Current Architecture

Notaire uses **direct entity serialization** — JPA entities are serialized as JSON directly from the REST controllers, without a separate DTO layer for most endpoints. DTOs exist for specific use cases where the full entity should not be exposed.

### Where DTOs Are Used

| DTO class | Purpose | Endpoint |
|-----------|---------|----------|
| `DtoUsuario` | Login response — excludes password hash, includes `valido` flag | `POST /usuarios/login` |
| `DtoPlantillaPresupuesto` | Composite PK mapping for budget templates | `GET/POST /plantilla-presupuestos` |

### Where Direct Entity Serialization Is Used

Most CRUD endpoints return JPA entities directly:
- `GET /personas` → `List<Persona>` serialized as JSON
- `GET /gestiones` → `List<GestionDeEscritura>` serialized as JSON

## Login DTO Pattern

The `DtoUsuario` DTO is the primary auth response. It is built from the `Usuario` entity in the service layer:

```java
// DtoUsuario fields:
// idUsuario, nombre, tipo, valido (boolean), idPersona
// NOT included: contrasenia (never expose password hash)

DtoUsuario dto = new DtoUsuario();
dto.setNombre(usuario.getNombre());
dto.setTipo(usuario.getTipo());
dto.setValido(autenticacionExitosa);
```

## Frontend TypeScript Types

The TypeScript types in `frontend/src/types/index.ts` mirror the backend entity field names:

```typescript
// Field names come from Java getters → Jackson → JSON key
// Java: getIdTipoTramite() → JSON: "idTipoTramite"
export interface TipoDeTramite {
  idTipoTramite?: number;  // NOT idTipoDeTramite
  nombre?: string;
}

// Java: getIdEstadoGestion() → JSON: "idEstadoGestion"  
export interface EstadoDeGestion {
  idEstadoGestion?: number;  // NOT idEstadoDeGestion
  nombre?: string;
}
```

### Critical Field Name Rules

| Entity | Java field | JSON key | TypeScript field |
|--------|-----------|----------|-----------------|
| TipoDeTramite | `idTipoTramite` | `idTipoTramite` | `idTipoTramite` |
| EstadoDeGestion | `idEstadoGestion` | `idEstadoGestion` | `idEstadoGestion` |
| PlantillaPresupuesto | `plantillaPresupuestoPK` | `plantillaPresupuestoPK` | `plantillaPresupuestoPK` |
| RegistroAuditoria | `idRegistroAuditoria` | `idRegistroAuditoria` | `idRegistroAuditoria` |
| RegistroAuditoria | `detalleOperacion` | `detalleOperacion` | `detalleOperacion` |

## Composite PK Handling

`PlantillaPresupuesto` uses an `@EmbeddedId` composite key:

```java
@EmbeddedId
protected PlantillaPresupuestoPK plantillaPresupuestoPK;
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
```
PUT /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}
DELETE /api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}
```

## Lazy Loading and JSON Serialization

Some entities have `@ManyToOne` or `@OneToMany` with lazy loading. When Jackson tries to serialize a lazy proxy, it may throw a `LazyInitializationException`.

**Current mitigation**: `spring.jpa.open-in-view=false` is configured. Controllers use a fresh `EntityManager` per request via `getEntityManager()` from `JpaControllerProvider`.

**Risk**: Endpoints that serialize entities with `@OneToMany` lists (e.g., `GestionDeEscritura.tramiteList`) may fail if the relationship is lazy and the session is closed. Some integration tests are disabled pending a lazy loading fix.

**Recommended fix**: Add `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` to entity classes, or use Jackson's Hibernate module:

```xml
<dependency>
  <groupId>com.fasterxml.jackson.datatype</groupId>
  <artifactId>jackson-datatype-hibernate6</artifactId>
</dependency>
```

## Adding a New DTO

When a new endpoint should return a reduced view of an entity:

1. Create the DTO class in `com.licensis.notaire.dto`
2. Add a `toDtoXxx()` method to the entity class
3. Return the DTO from the controller instead of the entity

```java
// In the entity class:
public DtoPersona toDtoPersona() {
    DtoPersona dto = new DtoPersona();
    dto.setIdPersona(this.idPersona);
    dto.setNombreCompleto(this.nombre + " " + this.apellido);
    // Only include fields needed by the client
    return dto;
}

// In the controller:
@GetMapping("/buscar")
public ResponseEntity<List<DtoPersona>> buscar(@RequestParam String nombre) {
    return ResponseEntity.ok(
        personas.stream().map(Persona::toDtoPersona).toList()
    );
}
```

## References

- `DtoUsuario.java` — primary auth response DTO
- `PlantillaPresupuesto.java` — composite PK entity example
- `frontend/src/types/index.ts` — TypeScript mirrors of all entity types
