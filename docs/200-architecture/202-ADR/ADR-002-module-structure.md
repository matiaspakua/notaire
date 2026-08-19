# ADR-002: Estructura de Módulos Maven y Organización de Paquetes

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-005  
**Supersedes:** Legacy monolithic structure  

> **Update (see ADR-005):** the `frontend-swing` module referenced below was later
> renamed to `deprecated-frontend-swing` and excluded from the root Maven reactor.
> `mvn clean install -pl frontend-swing -am` no longer works — see the corrected
> command in [Build Commands](#build-commands).

## Context

El sistema Notaire tiene tres responsabilidades distintas que necesitan organización clara:

1. **Backend API REST**: Lógica de negocio y persistencia
2. **Frontend Swing**: Interfaz de usuario
3. **Código Compartido**: DTOs y utilidades comunes

La estructura monolítica anterior mezclaba estas responsabilidades. Se necesita una estructura de módulos Maven que:
- Enforce la separación de responsabilidades
- Facilite el testing independiente
- Permita builds selectivos
- Facilite el versionado y release de componentes

## Decision

Implementar estructura Maven multi-módulo con 3 módulos principales:

```
notaire/ (pom.xml agregador)
├── backend-api/
│   ├── src/main/java/com/licensis/notaire/
│   │   ├── api/              # REST Controllers
│   │   ├── service/          # Lógica de negocio
│   │   ├── repository/       # Spring Data JPA
│   │   ├── negocio/          # @Entity Domain classes
│   │   ├── config/           # Spring configurations
│   │   ├── exception/        # Custom exceptions
│   │   ├── dto/              # Response DTOs
│   │   └── util/             # Utilities
│   └── src/test/java/com/licensis/notaire/
│       ├── unit/             # Unit tests
│       └── integration/      # Integration tests
│
├── frontend-swing/
│   ├── src/main/java/com/licensis/notaire/
│   │   ├── gui/              # Swing frames/panels
│   │   ├── client/           # REST client
│   │   ├── controller/       # GUI controllers/presenters
│   │   ├── model/            # GUI-specific models
│   │   └── util/             # GUI utilities
│   └── src/test/java/        # Swing tests
│
├── notaire-shared/
│   ├── src/main/java/com/licensis/notaire/
│   │   └── shared/
│   │       ├── dto/          # Common DTOs
│   │       ├── constant/     # Constants
│   │       └── util/         # Shared utilities
│   └── src/test/java/
│
└── pom.xml (agregador)
```

### Backend-API: Capas internas

**api/** (REST Controllers)
```
api/
├── PresupuestoController.java
├── PersonaController.java
├── EscrituraController.java
├── GestionController.java
├── RegistroAuditoriaController.java
└── ReporteController.java
```

**service/** (Orquestación de negocio)
```
service/
├── PresupuestoService.java
├── PersonaService.java
├── EscrituraService.java
├── GestionService.java
└── RegistroAuditoriaService.java
```

**repository/** (Spring Data JPA)
```
repository/
├── PresupuestoRepository.java
├── PersonaRepository.java
├── EscrituraRepository.java
├── GestionRepository.java
└── UsuarioRepository.java
```

**negocio/** (Domain entities)
```
negocio/
├── Presupuesto.java
├── Persona.java
├── Escritura.java
├── Gestion.java
├── Usuario.java
└── Folio.java
```

**config/** (Spring configuration)
```
config/
├── DataSourceConfig.java
├── JpaConfig.java
├── SecurityConfig.java
├── RestClientConfig.java
└── CorsConfig.java
```

**exception/** (Custom exceptions)
```
exception/
├── NotaireException.java
├── ResourceNotFoundException.java
├── BusinessValidationException.java
└── DataAccessException.java
```

**dto/** (Response DTOs)
```
dto/
├── DtoPresupuesto.java
├── DtoPersona.java
├── DtoEscritura.java
├── DtoGestion.java
└── ApiResponse.java
```

## Options Considered

### Option A: Single monolithic JAR
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Baja |
| Deploy independiente | No |
| Testabilidad | Media |

**Cons:**
- No permite separar FE/BE builds
- Dificulta testing independiente
- Legacy structure

### Option B: Multi-módulo Maven (SELECCIONADO)
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Media |
| Deploy independiente | Sí |
| Testabilidad | Alta |

**Pros:**
- Separación clara de responsabilidades
- Builds selectivos (`mvn -pl backend-api -am`)
- Dependency management centralizado
- Facilita CI/CD granular
- Versioning independiente de módulos

### Option C: Monorepo Gradle
| Dimensión | Evaluación |
|-----------|-----------|
| Complejidad | Alta |
| Performance | Mejor en monorepos grandes |

**Cons:**
- Curva de aprendizaje para equipo Maven
- Migración costosa desde Maven

## Trade-off Analysis

**Complejidad vs. Separación**

La estructura multi-módulo añade cierta complejidad inicial (gestión de dependencias entre módulos), pero el beneficio de separación es mayor. Permite que equipos trabajen independientemente sin conflictos de merge.

**Build time vs. Seguridad de cambios**

Los builds selectivos (`mvn -pl backend-api`) reducen tiempo, permitiendo builds rápidos sin esperar a la GUI. Cada módulo puede testearse independientemente.

## Consequences

### Positivas
- **Separación clara**: No hay imports cruzados BE→GUI
- **Builds independientes**: Backend sin esperar frontend
- **Testing granular**: Unidad tests por módulo
- **Deployment independiente**: Backend actualiza sin GUI

### Desafíos
- **Versionado de dependencias**: Mantener compatibilidad entre módulos
- **Shared module overhead**: Pequeño overhead de coordinación
- **Build más lento**: Multi-módulo requiere builds secuenciales

## Implementation Rules

### Backend-API

1. **NO se permiten imports de Swing**
   ```java
   // PROHIBIDO
   import javax.swing.*;
   ```

2. **NO acceso directo a BD desde controllers**
   ```java
   // INCORRECTO - Direct JDBC
   Statement stmt = connection.createStatement();
   
   // CORRECTO - Repository injection
   @Autowired private PresupuestoRepository repo;
   ```

3. **NO excepciones silenciosas**
   ```java
   // INCORRECTO
   try { ... } catch (Exception e) { }
   
   // CORRECTO
   catch (SQLException e) {
       LOGGER.error("Database error", e);
       throw new DataAccessException("Failed to fetch", e);
   }
   ```

### Frontend-Swing

1. **NO lógica de negocio en GUI**
   ```java
   // INCORRECTO
   if (presupuesto.monto > 1000) {
       // cálculo de impuestos
   }
   
   // CORRECTO
   BigDecimal impuestos = apiClient.calcularImpuestos(presupuesto);
   ```

2. **NO acceso directo a BD**
   ```java
   // INCORRECTO
   Connection conn = DriverManager.getConnection(...);
   
   // CORRECTO
   PresupuestoResponse dto = apiClient.getPresupuesto(id);
   ```

3. **Usar ApiClient para todas las operaciones**
   ```java
   @Autowired private ApiClient apiClient;
   
   void loadPresupuestos() {
       List<DtoPresupuesto> items = apiClient.listPresupuestos();
   }
   ```

### Notaire-Shared

- Sólo DTOs, constantes, y utilidades compartidas
- Versionado independiente
- No imports de backend-api o frontend-swing

## Build Commands

```bash
# Build completo
mvn clean install

# Build backend solo
mvn clean install -pl backend-api -am

# Build frontend solo (module excluded from root reactor; build standalone)
cd deprecated-frontend-swing && mvn clean install

# Build shared solo
mvn clean install -pl notaire-shared

# Tests backend
mvn test -pl backend-api

# Verificación de calidad
mvn verify -pl backend-api
```

## Related ADRs

- ADR-001: Migración a microservicios
- ADR-003: Versionado de API REST
- ADR-004: Testing strategy

## See Also

- CLAUDE.md: Convenciones de código
- Backend Architecture section
