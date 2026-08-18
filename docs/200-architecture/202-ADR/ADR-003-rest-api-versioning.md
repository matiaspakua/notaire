# ADR-003: Estrategia de Versionado de API REST

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-002  

## Context

El proyecto migrará de arquitectura monolítica a REST API. Esto requiere una estrategia clara de versionado para:

- Permitir evolución de la API sin romper clientes existentes
- Facilitar rollback de cambios
- Mantener múltiples versiones en paralelo si es necesario
- Comunicar cambios breaking a clientes claramente

Restricciones:
- Equipo pequeño (no recursos para mantener 10 versiones simultáneas)
- Clientes varían (GUI Swing, futuros clientes web/móvil)
- Necesidad de cambios rápidos durante refactoring

## Decision

Implementar versionado de API mediante **URL path versioning** con **semantic versioning**:

```
/api/v1/presupuestos      # Version 1 stable
/api/v2/presupuestos      # Version 2 (en desarrollo o future)
```

### Estrategia de Versiones

**Version 1 (v1):** Actual - Entidades principales
- Presupuestos, Personas, Escrituras, Gestiones
- Status: STABLE
- End of Life: A determinar (mín. 12 meses post v2)

**Version 2 (v2):** Futuro - Refactoring mayor
- Cambios en estructura de respuestas
- Status: En planificación
- Pre-requisito: Todos clientes migren a v1 primero

### Estructura de Endpoints

```java
@RestController
@RequestMapping("/api/v1")
public class PresupuestoController {
    @GetMapping("/presupuestos")
    public ResponseEntity<ApiResponse<List<DtoPresupuesto>>> list() { }
    
    @GetMapping("/presupuestos/{id}")
    public ResponseEntity<ApiResponse<DtoPresupuesto>> getById() { }
    
    @PostMapping("/presupuestos")
    public ResponseEntity<ApiResponse<DtoPresupuesto>> create() { }
}
```

### Semantic Versioning

Seguir semver para **release versions** (código), no para API:

- `1.0.0` → Primer release con API v1 estable
- `1.1.0` → Nuevo endpoint en v1 (compatible hacia atrás)
- `1.1.1` → Bug fix en v1
- `2.0.0` → Breaking changes (requiere API v2)

### Política de Breaking Changes

**Permitido sin cambio de versión:**
- Agregar campo opcional en response
- Agregar nuevo endpoint
- Deprecar endpoint (agregar `@Deprecated` + header `Warning: 299`)
- Cambiar formato de fecha (si parsing es backward compatible)

**Requiere cambio de versión:**
- Remover campo en response
- Cambiar tipo de dato en campo existente
- Cambiar significado de un campo
- Cambiar estructura de errores
- Cambiar comportamiento funcional

## Options Considered

### Option A: URL Path Versioning (SELECCIONADO)
```
GET /api/v1/presupuestos
GET /api/v2/presupuestos
```

| Dimensión | Evaluación |
|-----------|-----------|
| Claridad | Muy alta |
| SEO friendly | Sí |
| Caching | Fácil |
| Documentación | Clara |
| Complejidad implementación | Baja |

**Pros:**
- Muy legible en logs y monitoreo
- Fácil de documentar
- Browser-friendly para explorar
- Straightforward en routing

**Cons:**
- Código duplicado entre versiones
- URL path "contaminado"
- Requiere mantener controllers múltiples

### Option B: Accept Header Versioning
```
GET /api/presupuestos
Accept: application/vnd.notaire.v1+json
```

| Dimensión | Evaluación |
|-----------|-----------|
| Claridad | Media |
| Standards compliance | RESTful |
| Debugging | Difícil |
| HTTP caching | Complejo |

**Cons:**
- Menos intuitivo
- Difícil debuggear en browser
- Problemas con caching HTTP
- Requiere cliente conocer header names

### Option C: Query Parameter Versioning
```
GET /api/presupuestos?api-version=1
```

| Dimensión | Evaluación |
|-----------|-----------|
| Claridad | Alta |
| Caching | Problemático |
| REST compliance | Bajo |

**Cons:**
- Considera parámetro de routing, no versión
- Caching problemático (query strings)
- No estándar REST

### Option D: No versionado (monolithic)

**Cons:**
- Breaking changes obligan actualizar todos clientes
- Imposible rollback sin afectar usuarios
- Problemas en transición de tecnologías

## Trade-off Analysis

**URL Clarity vs. Code Duplication**

URL path versioning es muy claro (mejor debugging, logging) a costo de duplicación de código. La duplicación es inevitable de todas formas si mantenemos versiones en paralelo, así que mejor documentado.

**Standards Compliance vs. Pragmatismo**

Header versioning es más "RESTful" pero URL versioning es más práctico para un equipo pequeño. El pragmatismo gana aquí.

**Simplicity vs. Flexibility**

URL versioning es simple de entender y mantener. Si necesitamos múltiples endpoints por versión, es straightforward crear carpetas separadas.

## Consequences

### Positivas
- **Claridad absoluta**: URL muestra versión claramente
- **Debuggeabilidad**: Logs y monitoreo triviales
- **Caching HTTP**: Works with standard caching
- **Browser exploration**: URL legible sin herramientas especiales
- **Migration path clara**: Clientes saben exactamente qué cambió

### Desafíos
- **Duplicación de código**: v1 y v2 controllers coexisten
- **Mantenimiento**: Bug fixes deben aplicarse en múltiples versiones
- **URL paths más largos**: `/api/v1/` vs `/api/`
- **Documentación doble**: Swagger para v1 y v2 por separado

## Implementation Details

### Controller Structure

```java
// Backend v1 (stable)
@RestController
@RequestMapping("/api/v1/presupuestos")
public class PresupuestoControllerV1 {
    // Endpoints v1
}

// Backend v2 (future - si es necesario)
@RestController
@RequestMapping("/api/v2/presupuestos")
public class PresupuestoControllerV2 {
    // Endpoints v2 con breaking changes
}
```

### Response Structure

```json
{
  "success": true,
  "data": { /* Entity */ },
  "error": null,
  "timestamp": "2024-03-20T10:30:00Z",
  "version": "1.0.0"
}
```

### Deprecation Headers

```java
@GetMapping("/presupuestos")
@Deprecated
public ResponseEntity<?> getPresupuestos() {
    return ResponseEntity.ok()
        .header("Deprecation", "true")
        .header("Sunset", "Sun, 31 Dec 2024 23:59:59 GMT")
        .body(response);
}
```

### Swagger/OpenAPI Documentation

```yaml
# swagger-ui: /swagger-ui.html

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    urls[0]:
      name: v1 (stable)
      url: /v3/api-docs/v1
    urls[1]:
      name: v2 (beta)
      url: /v3/api-docs/v2
```

## Migration Plan

1. **Phase 1** (Current): Stabilizar v1
   - Todos endpoints en `/api/v1`
   - Swagger UI con v1 como default
   - Release 1.0.0

2. **Phase 2** (Meses 3-6): Deprecate legacy
   - Marcar endpoints legacy como `@Deprecated`
   - Agregar headers de deprecation
   - Notificar a clientes 6 meses antes de EOL

3. **Phase 3** (Meses 6+): Create v2 si es necesario
   - Branches para v1 y v2
   - Mantener v1 por mín. 12 meses
   - Define EOL para v1

4. **Phase 4** (Meses 18+): Sunset v1
   - Remover v1 endpoints
   - Keep documentación histórica

## Governance

### Change Control Board

- Cambios a v1 requieren aprobación (breaking changes bloqueadas)
- Cambios a v2+ solo requieren feature review
- Bug fixes aplicables a múltiples versiones tienen SLA de 2 semanas

### Client Migration SLA

- Breaking changes: notificación 6 meses anticipada
- Deprecation period: mín. 12 meses
- Para cliente crítico: SLA extendido por acuerdo

## Related ADRs

- ADR-001: Arquitectura de microservicios
- ADR-004: Seguridad y autenticación
- ADR-005: Testing strategy

## See Also

- [Semantic Versioning](https://semver.org/)
- [API Versioning Best Practices](https://cloud.google.com/apis/design/versioning)
- [OpenAPI Specification](https://spec.openapis.org/)
