# ADR-004: Migración de Base de Datos MySQL a PostgreSQL

**Status:** Accepted  
**Date:** 2024-03-20  
**Deciders:** Matías Miguez  
**Related:** ADR-001, ADR-002  

## Context

El sistema Notaire originalmente usaba MySQL para persistencia. La migración a microservicios con Spring Boot moderno requiere actualizar a PostgreSQL 16 por:

- **Mejor soporte en Spring Boot/Hibernate**: PostgreSQL tiene dialect optimizado
- **JSONB support**: Útil para estructuras semi-estructuradas
- **Mejor performance en queries complejas**: Índices más sofisticados
- **Mejores herramientas para Docker**: PostgreSQL tiene excelente soporte containerizado
- **Long-term support**: PostgreSQL tiene mejor ciclo de soporte

**Restricciones:**
- Datos históricos deben preservarse
- Sistema debe mantener operativo durante migración
- Equipo no tiene experiencia en PostgreSQL (pero sí en relacional databases)
- Schema debe ser compatible con entidades JPA existentes

## Decision

Migrar completamente a **PostgreSQL 16** con siguiente estrategia:

### 1. Schema Migration

Usar **Liquibase** para versionado de schema:

```yaml
# liquibase/changelog/01-initial-schema.yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-presupuesto-table
      author: refactoring
      changes:
        - createTable:
            tableName: presupuesto
            columns:
              - column:
                  name: id
                  type: BIGINT
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: numero
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
              - column:
                  name: fecha_creacion
                  type: TIMESTAMP
                  defaultValue: CURRENT_TIMESTAMP
```

### 2. Data Migration

**Fase 1**: Export desde MySQL
```sql
-- MySQL: export tabla
mysqldump -u user -p database presupuesto > presupuesto.sql
```

**Fase 2**: Transform schema
```sql
-- Convertir tipos MySQL → PostgreSQL:
-- INT AUTO_INCREMENT → BIGINT + SERIAL/IDENTITY
-- DATETIME → TIMESTAMP
-- VARCHAR → VARCHAR
-- DECIMAL → NUMERIC
-- ENUM → VARCHAR + constraint
```

**Fase 3**: Import a PostgreSQL
```sql
-- PostgreSQL: import transformado
psql -U user -d database -f presupuesto.sql
```

**Fase 4**: Validación de integridad
```sql
-- Verificar counts
SELECT COUNT(*) FROM presupuesto;  -- MySQL vs PostgreSQL

-- Verificar constraints
SELECT constraint_name FROM information_schema.table_constraints 
WHERE table_name='presupuesto' AND constraint_type='PRIMARY KEY';
```

### 3. Hibernate Configuration

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/notaire
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate  # No auto-update in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
```

### 4. Type Mappings

| MySQL | PostgreSQL | JPA Type |
|-------|-----------|----------|
| INT AUTO_INCREMENT | BIGSERIAL | @GeneratedValue(strategy=IDENTITY) |
| DATETIME | TIMESTAMP | java.time.LocalDateTime |
| DECIMAL(10,2) | NUMERIC(10,2) | java.math.BigDecimal |
| TEXT | TEXT | String |
| ENUM('A','B') | VARCHAR(50) | String o Enum |
| BLOB | BYTEA | byte[] |
| DATE | DATE | java.time.LocalDate |
| VARCHAR(255) | VARCHAR(255) | String |

### 5. Docker Setup

```dockerfile
# Dockerfile.postgres
FROM postgres:16-alpine

ENV POSTGRES_DB=notaire
ENV POSTGRES_USER=notaire
ENV POSTGRES_PASSWORD=secure_password

COPY init-db/ /docker-entrypoint-initdb.d/
```

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: notaire-postgres
    environment:
      POSTGRES_DB: notaire
      POSTGRES_USER: notaire
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-db:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U notaire"]
      interval: 10s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4:latest
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@notaire.local
      PGADMIN_DEFAULT_PASSWORD: ${PGADMIN_PASSWORD}
    ports:
      - "5050:80"
    depends_on:
      - postgres
```

## Options Considered

### Option A: Mantener MySQL
| Dimensión | Evaluación |
|-----------|-----------|
| Riesgo | Bajo |
| Compatibilidad | Alta |
| Performance | Media |
| Support | Declinante |

**Cons:**
- MySQL 5.7 EOL próximo (Oct 2023)
- Peor performance en queries complejas
- Dialect de Hibernate no tan optimizado
- JSONB no disponible

### Option B: Migrar a PostgreSQL 16 (SELECCIONADO)
| Dimensión | Evaluación |
|-----------|-----------|
| Riesgo | Medio |
| Performance | Alta |
| Long-term support | 5 años |
| Tooling | Excelente |

**Pros:**
- Long-term support (5+ años)
- Excelente performance
- JSONB para semi-structured data
- JSON support nativo
- Array types
- Full-text search

**Cons:**
- Requiere migration effort
- Equipo menos familiarizado
- Algunos tipos de datos requieren mapeo cuidadoso

### Option C: MariaDB (MySQL moderno)
| Dimensión | Evaluación |
|-----------|-----------|
| Riesgo | Bajo |
| Compatibilidad MySQL | Alta |
| Performance | Muy buena |

**Cons:**
- Menos estándar que PostgreSQL
- Menos herramientas para Docker
- No hay ventaja de features sobre PostgreSQL

## Trade-off Analysis

**Effort de migración vs. Beneficios a largo plazo**

La migración requiere ~2-3 semanas de esfuerzo inicial, pero resulta en:
- 5+ años de soporte oficial
- Mejor performance
- Mejor tooling
- Capacidad de usar features avanzadas (JSONB, arrays, full-text search)

**Risk de corrupción de datos vs. Modernización**

El riesgo se mitiga con:
- Backup completo antes de migración
- Testing exhaustivo en environment clona
- Validación de data integrity post-migración
- Rollback plan claro

## Consequences

### Positivas
- **Mejor performance**: Queries complejas más rápidas
- **JSONB support**: Flexibilidad para datos semi-estructurados
- **Array support**: Estructuras de datos más ricas
- **Full-text search**: Búsquedas textuales eficientes
- **Long-term support**: 5+ años de actualizaciones de seguridad
- **Better tooling**: pgAdmin, psql, excelente ecosistema

### Desafíos
- **Curva de aprendizaje**: Equipo debe aprender PostgreSQL
- **Migration downtime**: ~30 mins - 2 horas de downtime (depende de tamaño)
- **Operaciones diferentes**: pg_dump vs mysqldump, diferentes herramientas
- **Testing exhaustivo**: Validar todos queries en PostgreSQL

## Migration Timeline

### Semana 1-2: Preparation
- [ ] Crear PostgreSQL 16 en desarrollo
- [ ] Crear Liquibase changelog
- [ ] Export schema desde MySQL
- [ ] Transform types MySQL → PostgreSQL
- [ ] Setup docker-compose con PostgreSQL

### Semana 3: Testing
- [ ] Import data en PostgreSQL
- [ ] Validar integridad de data
- [ ] Run todos test suites en PostgreSQL
- [ ] Performance testing
- [ ] Crear rollback procedure

### Semana 4: Migration
- [ ] Full backup de MySQL
- [ ] Final data export
- [ ] Import a PostgreSQL
- [ ] Run data validation queries
- [ ] Punto de rollback claro
- [ ] Update connection strings en prod
- [ ] Monitor logs y performance

### Post-Migration
- [ ] Validar diariamente durante 1 semana
- [ ] Backup PostgreSQL regularmente
- [ ] Decommission MySQL infrastructure

## PostgreSQL Best Practices

### Connection Pooling
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Monitoring
```bash
# Ver conexiones activas
SELECT count(*) FROM pg_stat_activity;

# Ver queries lentas
SELECT query, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

### Backup Strategy
```bash
# Full backup
pg_dump -U notaire -d notaire > backup.sql

# Compressed backup
pg_dump -U notaire -d notaire | gzip > backup.sql.gz

# Point-in-time recovery enabled
echo "wal_level = replica" >> postgresql.conf
```

## Related ADRs

- ADR-001: Arquitectura de microservicios
- ADR-002: Estructura de módulos
- ADR-005: Testing strategy

## See Also

- [PostgreSQL 16 Release Notes](https://www.postgresql.org/docs/16/release-16.html)
- [PostgreSQL vs MySQL Comparison](https://wiki.postgresql.org/wiki/PostgreSQL_vs_MySQL)
- [Liquibase Documentation](https://docs.liquibase.com/)
