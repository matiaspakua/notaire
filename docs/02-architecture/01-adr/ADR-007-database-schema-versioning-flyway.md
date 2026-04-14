# ADR-007: Control de Versiones de Schema con Flyway

**Status:** Accepted
**Date:** 2026-04-14
**Deciders:** Matías Miguez
**Supersedes:** ADR-004 (sección de Liquibase)
**Related:** ADR-001 (Microservices Architecture), ADR-004 (Database Migration)

## Context

El sistema Notaire actualmente gestiona el esquema de base de datos mediante scripts SQL estáticos en `init-db/` que se ejecutan una sola vez al crear el contenedor PostgreSQL. Esta aproximación presenta varios problemas:

### Problemas Identificados

1. **Sin control de versiones del schema**: No hay historial de cambios del schema
2. **Sin rollback fácil**: No hay mecanismo para revertir cambios problemáticos
3. **Migraciones manuales**: Los cambios de schema requieren intervención manual
4. **Difícil CI/CD**: No se integra fácilmente con pipelines de despliegue
5. **Riesgo en producción**: Cambios de schema pueden causar inconsistencias

### Estado Actual

```
init-db/
├── 01-schema.sql    # Creación de tablas (371 líneas)
├── 02-data.sql     # Datos iniciales (94 líneas)
├── migrate.load    # Documentación histórica
└── README.md
```

Los scripts se ejecutan vía:
```yaml
# docker-compose.yml
volumes:
  - ./init-db:/docker-entrypoint-initdb.d:ro
```

Esto significa que solo se ejecutan en la **primera creación** del volumen Docker.

## Decision

**Implementar Flyway** como sistema de control de versiones del schema de base de datos.

### Por qué Flyway (y no Liquibase)

| Criterio | Flyway | Liquibase |
|----------|--------|-----------|
| Curva de aprendizaje | Baja (SQL puro) | Media (XML/YAML/JSON) |
| Mantenimiento | Simple (archivos SQL) | Mayor (formatos declarativos) |
| Integración Spring Boot | Nativa (spring-boot-starter-jdbc) | Requiere configuración adicional |
| Team familiarity | Alto (SQL estándar) | Medio |
| Rollback | SQL nativo | Requiere changelog complejo |

### Contexto: ADR-004 Recomendaba Liquibase

El ADR-004 mencionaba Liquibase como herramienta preferida. Sin embargo, tras evaluación:

1. **SQL puro es más simple**: El equipo ya conoce SQL
2. **Menos abstracción**: Flyway usa SQL directo, sin XML/YAML overhead
3. **Spring Boot nativo**: `spring-boot-starter-data-jpa` incluye soporte Flyway
4. **Migración más simple**: Los scripts existentes se convierten directamente

### Configuración de Flyway

#### 1. Dependencia Maven

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

#### 2. Ubicación de Migraciones

```
backend-api/src/main/resources/
├── db/migration/
│   ├── V1__initial_schema.sql
│   ├── V2__initial_data.sql
│   └── V3__add_indexes.sql
└── application.properties
```

#### 3. Configuración Spring Boot

```properties
# application.properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.locations=classpath:db/migration
spring.flyway.out-of-order=false
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true
```

#### 4. Convenciones de Nomenclatura

```
V{version}__{description}.sql
  └── Version: 1, 2, 3... (números enteros)
  └── Description: snake_case, descriptivo

Ejemplos:
- V1__initial_schema.sql
- V2__initial_data.sql
- V3__add_performance_indexes.sql
- V4__add_users_table.sql
```

#### 5. Estrategia de Baseline

Para bases de datos existentes, usar baseline:

```sql
-- Baseline migration (replica el estado actual)
INSERT INTO flyway_schema_history (version, description, type, installed_on)
VALUES (0, '<< BASELINE >>', 'BASELINE', NOW());
```

## Options Considered

### Option A: Mantener Scripts Estáticos (Status Quo)

| Pros | Contras |
|------|---------|
| Sin cambios | Sin versionado |
| Simple initially | Difícil rollback |
| SQL directo | No integrable con CI/CD |
| | Riesgo en producción |

### Option B: Liquibase (Recomendado en ADR-004)

| Pros | Contras |
|------|---------|
| Rollback automático | XML/YAML overhead |
| Cross-database | Mayor complejidad |
| Changelog versionable | Curva de aprendizaje |

### Option C: Flyway (SELECCIONADO)

| Pros | Contras |
|------|---------|
| SQL puro | Rollback manual |
| Simple | Sin changelog declarativo |
| Spring Boot nativo | - |
| Bajo mantenimiento | - |

### Option D: Doctrine Migrations

| Pros | Contras |
|------|---------|
| PHP/Doctrine | Fuera del stack Java |
| TypeScript | No aplica |

## Trade-off Analysis

### Esfuerzo vs. Beneficios

| Aspecto | Antes | Después |
|---------|-------|---------|
| Versionado schema | No | Sí (V1, V2, V3...) |
| Rollback | Manual/Difícil | SQL nativo |
| CI/CD | Manual | Automatizado |
| Testing schema | Ad-hoc | Reproducible |

### Migration Path

1. **Fase 1**: Migrar scripts actuales a Flyway (V1, V2)
2. **Fase 2**: Configurar baseline para datos existentes
3. **Fase 3**: Actualizar Docker Compose
4. **Fase 4**: Agregar nuevas migraciones para features

## Consequences

### Positivos

- **Versionado completo**: Cada cambio tiene su migración
- **Reproducibilidad**: Same schema en todos los ambientes
- **CI/CD integrado**: Migrations en pipelines de deploy
- **Audit trail**: Tabla `flyway_schema_history` registra todo
- **Rollback seguro**: SQL revert permite volver atrás
- **Testing de schema**: Migrations pueden incluir tests

### Negativos

- **Curva de aprendizaje**: Equipo debe aprender convenciones Flyway
- **Overhead inicial**: Migrar scripts existentes toma tiempo
- **Discipline requerida**: Todas las mudanças de schema = nueva migración
- **No auto-rollback**: Rollback requiere escribir SQL manualmente

## Implementation Plan

### Fase 1: Setup (Sprint 1)

- [ ] Agregar dependencia Flyway a `pom.xml`
- [ ] Crear directorio `db/migration/`
- [ ] Migrar `01-schema.sql` → `V1__initial_schema.sql`
- [ ] Migrar `02-data.sql` → `V2__initial_data.sql`
- [ ] Configurar baseline en `application.properties`
- [ ] Probar en ambiente local

### Fase 2: Docker Integration (Sprint 1)

- [ ] Actualizar `docker-compose.yml` para usar Flyway
- [ ] Remover volúmenes de `init-db`
- [ ] Agregar healthcheck de Flyway
- [ ] Test de migrate + clean en containers

### Fase 3: CI/CD (Sprint 2)

- [ ] Agregar step de Flyway en GitHub Actions
- [ ] Configurar validación de migraciones en PR
- [ ] Documentar rollback procedures

### Fase 4: Testing (Sprint 2)

- [ ] Crear tests de integración con Testcontainers
- [ ] Tests de rollback
- [ ] Tests de idempotencia

## Technical Details

### Tabla de Historial

Flyway crea automáticamente:

```sql
CREATE TABLE flyway_schema_history (
    installed_rank INTEGER NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL
);
```

### Comandos Principales

```bash
# Validar migraciones
mvn flyway:validate

# Aplicar migraciones
mvn flyway:migrate

# Ver estado
mvn flyway:info

# Limpiar (CUIDADO!)
mvn flyway:clean

# Baseline para DB existente
mvn flyway:baseline
```

### Integración Spring Boot

Spring Boot auto-configura Flyway cuando detecta la dependencia:

```java
@SpringBootApplication
public class NotaireBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotaireBackendApplication.class, args);
    }
}
```

## Migration Scripts Templates

### Template de Migración

```sql
-- V{version}__{description}.sql
-- Author: {nombre}
-- Date: {fecha}
-- Description: {descripción}

-- =============================================================================
-- UP Migration
-- =============================================================================

-- Tu SQL aquí

-- =============================================================================
-- Verification
-- =============================================================================
-- SELECT COUNT(*) FROM table_name; -- debe retornar X rows
```

### Template de Rollback

```sql
-- R{version}__{description}.sql
-- Author: {nombre}
-- Date: {fecha}
-- Description: Rollback de V{version}

-- =============================================================================
-- DOWN Migration (Rollback)
-- =============================================================================

-- Tu SQL de rollback aquí
```

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway + Spring Boot](https://flywaydb.org/documentation/database/postgresql/)
- [Flyway Best Practices](https://flywaydb.org/documentation/bestPractices.html)
- ADR-004: Database Migration MySQL → PostgreSQL
- Issue #252: Implement Flyway for database schema migration

## Related ADRs

- ADR-001: Microservices Architecture
- ADR-004: Database Migration (MySQL → PostgreSQL)
- ADR-005: Modern Frontend Migration (Next.js)
- ADR-006: Testing Strategy
