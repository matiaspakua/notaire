# SAR-007: Implementación de Flyway para Control de Versiones de Schema

**Fecha:** 2026-04-14
**Estado:** En Progreso
**Autor:** Matías Miguez
**Issue:** #252

---

## 1. Resumen Ejecutivo

Este documento detalla la implementación de Flyway como sistema de control de versiones para el esquema de base de datos PostgreSQL del proyecto Notaire.

### 1.1 Objetivo

Reemplazar los scripts SQL estáticos en `init-db/` con migraciones versionadas de Flyway para permitir:
- Control de versiones del schema
- Rollbacks seguros
- Integración con CI/CD
- Reproducibilidad en todos los ambientes

### 1.2 Impacto

| Aspecto | Antes | Después |
|---------|-------|---------|
| Versionado schema | No | Sí (Flyway) |
| Rollback | Manual | SQL nativo |
| Deploy schema | Scripts manuales | Automatizado |
| Testing | Ad-hoc | Reproducible |

---

## 2. Análisis Técnico

### 2.1 Estado Actual

**Estructura de archivos:**
```
init-db/
├── 01-schema.sql      # 371 líneas - Creación completa del schema
├── 02-data.sql        # 94 líneas - Datos iniciales (tipos, persona, usuario)
├── migrate.load       # 135 líneas - Documentación histórica
└── README.md         # Documentación
```

**Ejecución actual:**
- Docker Compose monta `./init-db` en `/docker-entrypoint-initdb.d/`
- Scripts se ejecutan SOLO en primera creación del contenedor
- Sin control de cambios posteriores

### 2.2 Análisis de Scripts Existentes

#### 01-schema.sql

**Estructura:**
- 24 tablas
- 4 tablas de referencia (conceptos, estados, tipos)
- 20 tablas de negocio
- Relaciones FK completas
- Sin índices explícitos

**Dependencias:**
```sql
-- Orden de creación (sin circular)
1. tablas de referencia (sin FK)
2. personas, inmuebles, escrituras
3. gestiones_de_escrituras
4. presupuestos, tramites
5. tablas dependientes (documentos, testimonios, folios, copias)
6. tablas finales (usuarios, auditoria, junction tables)
```

#### 02-data.sql

**Datos:**
- 4 conceptos (IVA, Honorarios, Documentación, Protocolo)
- 10 estados de gestión
- 5 tipos de identificación
- 3 tipos de folio
- 5 tipos de trámite
- 4 tipos de documento
- 1 persona (escribano)
- 1 usuario (admin)

### 2.3 Decisiones de Diseño

#### Migración a Flyway

**Estrategia:** Conversión directa
- `01-schema.sql` → `V1__initial_schema.sql`
- `02-data.sql` → `V2__initial_data.sql`

**Baseline:** Requerido para bases existentes
```sql
INSERT INTO flyway_schema_history (version, description, type, installed_on)
VALUES (0, '<< BASELINE >>', 'BASELINE', NOW());
```

---

## 3. Diseño de Solución

### 3.1 Arquitectura de Migraciones

```
backend-api/src/main/resources/db/migration/
├── V1__initial_schema.sql     # Schema completo
├── V2__initial_data.sql       # Datos iniciales
├── V3__add_indexes.sql        # Índices de performance (futuro)
└── R2__rollback_data.sql     # Rollback de datos (opcional)
```

### 3.2 Configuración Spring Boot

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

### 3.3 Docker Compose

**Antes:**
```yaml
volumes:
  - ./init-db:/docker-entrypoint-initdb.d:ro
```

**Después:**
```yaml
# No más mount de init-db
# Flyway se ejecuta automáticamente con Spring Boot
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/notaire
  # Flyway habilitado por defecto
```

### 3.4 Dependencias Maven

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

---

## 4. Plan de Implementación

### 4.1 Fase 1: Setup (Día 1)

- [ ] Crear directorio `db/migration/`
- [ ] Agregar dependencias a `pom.xml`
- [ ] Crear `V1__initial_schema.sql`
- [ ] Crear `V2__initial_data.sql`
- [ ] Configurar `application.properties`

### 4.2 Fase 2: Testing (Día 1)

- [ ] Probar migraciones en ambiente local
- [ ] Verificar baseline en DB existente
- [ ] Probar rollback de V2

### 4.3 Fase 3: Docker (Día 2)

- [ ] Actualizar `docker-compose.yml`
- [ ] Remover mount de `init-db`
- [ ] Test de deploy completo

### 4.4 Fase 4: CI/CD (Día 2)

- [ ] Agregar validación en GitHub Actions
- [ ] Documentar procedimientos

### 4.5 Fase 5: Documentación (Día 2)

- [ ] Actualizar README de BD
- [ ] Crear CHANGELOG.md
- [ ] Agregar ADR-007

---

## 5. Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Pérdida de datos | Baja | Alto | Backup antes de migración |
| Migración incompleta | Baja | Alto | Testing exhaustivo |
| Conflicto con JPA | Media | Medio | `ddl-auto=none` con Flyway |
| Rollback fallido | Baja | Alto | SQL manual verificado |

---

## 6. Testing Strategy

### 6.1 Tests de Integración

```java
@SpringBootTest
@FlywayTest
class DatabaseMigrationIntegrationTest {

    @Test
    void shouldApplyAllMigrations() {
        // Verificar que todas las migraciones se aplicaron
    }

    @Test
    void shouldHaveCorrectSchema() {
        // Verificar tablas existen
    }

    @Test
    void shouldHaveInitialData() {
        // Verificar datos iniciales
    }
}
```

### 6.2 Tests con Testcontainers

```java
@Testcontainers
class FlywayTestcontainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("notaire")
        .withUsername("admin")
        .withPassword("admin");

    @Test
    void shouldMigrateSuccessfully() {
        // Test completo de migración
    }
}
```

---

## 7. Métricas de Éxito

| Métrica | Target | Método de medición |
|---------|--------|-------------------|
| Migrations aplicadas | 100% | `flyway:info` |
| Tiempo de migración | < 5s | Logs de startup |
| Rollback funcional | 100% | Test manual |
| Cobertura de tests | > 80% | JaCoCo |

---

## 8. Timeline

```
Día 1:
├── Mañana: Setup y migración de scripts
└── Tarde: Testing local

Día 2:
├── Mañana: Docker y CI/CD
└── Tarde: Documentación y PR
```

---

## 9. Referencias

- ADR-007: Control de Versiones de Schema con Flyway
- ADR-004: Database Migration MySQL → PostgreSQL
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway + Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.flyway)

---

## 10. Directrices para Agentes de AI

### 10.1 Archivos de Configuración

El proyecto incluye los siguientes archivos para que los agentes de AI conozcan las convenciones de Flyway:

| Archivo | Descripción |
|---------|-------------|
| `.claude/skills/flyway/SKILL.md` | Skill completo de Flyway con ejemplos |
| `.claude/rules/database-migrations.md` | Reglas obligatorias para migraciones |

### 10.2 Reglas Obligatorias para Agentes

1. **NUNCA modificar migraciones existentes** - Siempre crear nuevas migraciones
2. **Siempre usar versionado** - Formato `V{n}__{description}.sql`
3. **Ubicación obligatoria** - `backend-api/src/main/resources/db/migration/`
4. **SQL idempotente** - Usar `IF EXISTS` / `IF NOT EXISTS`
5. **Secuencias** - Incluir `setval` después de INSERT

### 10.3 Workflow para Agentes

```
1. Analizar cambio de schema requerido
2. Verificar versión actual: ls db/migration/
3. Crear nueva migración: V{n+1}__{description}.sql
4. Incluir template con header y comentarios
5. Validar con: mvn flyway:validate
6. Commit con mensaje descriptivo referencing issue
```

### 10.4 Template de Migración para Agentes

```sql
-- =============================================================================
-- V{version}__{description}.sql
-- =============================================================================
-- Author: AI Agent
-- Date: {current_date}
-- Issue: #{issue_number}
-- Description: {what this migration does}
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Your SQL here

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM table_name;
```

### 10.5 Verificación de Convenciones

Antes de commit, verificar:
- [ ] Nombre de archivo sigue formato `V{n}__{description}.sql`
- [ ] Migration ubicada en `db/migration/`
- [ ] Header completo con Author, Date, Description
- [ ] SQL es idempotente (IF EXISTS/NOT EXISTS)
- [ ] Secuencias actualizadas después de INSERT
- [ ] Tests unitarios incluyen validación de scripts

### 10.6 Recursos Adicionales

Para más información, consultar:
- **Skill**: `.claude/skills/flyway/SKILL.md`
- **Reglas**: `.claude/rules/database-migrations.md`
- **ADR**: `docs/02-architecture/01-adr/ADR-007-database-schema-versioning-flyway.md`
