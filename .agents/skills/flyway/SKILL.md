# Flyway Database Migration Skill

## Overview

This skill provides guidance for implementing and managing database schema migrations using Flyway in the Notaire project.

## When to Use This Skill

Invoke this skill when creating new database tables, modifying existing database schema, adding indexes or constraints, inserting reference data that needs versioning, creating rollback scripts, or reviewing database changes in PRs.

## Core Workflow

### 1. Before Creating a Migration

1. Check existing migrations: Review `backend-api/src/main/resources/db/migration/`
2. Identify the next version: Get the highest version number (e.g., V3, V4)
3. Determine migration type: `V` prefix for forward migration, `R` prefix for rollback

### 2. Creating a Migration

**Naming Convention:**
```
V{version}__{description}.sql
R{version}__{description}.sql
```

**Examples:**
- `V3__add_users_email_index.sql`
- `V4__add_audit_log_table.sql`
- `R3__rollback_users_email_index.sql`

### 3. Migration Script Structure

```sql
-- =============================================================================
-- V{version}__{description}.sql
-- =============================================================================
-- Author: {your name}
-- Date: {date}
-- Description: {detailed description of what this migration does}
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Your SQL here
CREATE INDEX idx_users_email ON usuarios(email);

-- -----------------------------------------------------------------------------
-- Verification (optional)
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_users_email';
```

### 4. Rollback Scripts (When Needed)

```sql
-- =============================================================================
-- R{version}__{description}.sql
-- =============================================================================
-- Author: {your name}
-- Date: {date}
-- Description: Rollback for V{version}
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration (Rollback)
-- -----------------------------------------------------------------------------

DROP INDEX IF EXISTS idx_users_email;
```

### 5. Best Practices

| Practice | Reason |
|----------|--------|
| Idempotent SQL | Can be run multiple times safely |
| Use IF EXISTS | Prevents errors on re-run |
| Use IF NOT EXISTS | Prevents errors on re-run |
| Comment your SQL | Future developers understand intent |
| Small, focused migrations | Easier to review and rollback |
| One logical change per migration | Clear audit trail |
| Test rollback locally | Ensure recovery is possible |

### 6. Reference Data Migrations

For reference data that needs to be versioned:

```sql
-- V5__add_default_conceptos.sql

-- Check if data exists (idempotent)
INSERT INTO conceptos (version, id_concepto, nombre, valor, porcentaje, habilitado, concepto_fijo)
SELECT 1, 99, 'Nuevo Concepto', 100, 0, true, true
WHERE NOT EXISTS (
    SELECT 1 FROM conceptos WHERE nombre = 'Nuevo Concepto'
);

-- Update sequence
SELECT setval('conceptos_id_concepto_seq', COALESCE(MAX(id_concepto), 1)) FROM conceptos;
```

### 7. Configuration Reference

**application.properties:**
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.locations=classpath:db/migration
spring.flyway.out-of-order=false
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true
```

### 8. Command Reference

```bash
# Validate migrations
mvn flyway:validate

# Apply migrations
mvn flyway:migrate

# View status
mvn flyway:info

# Baseline existing database
mvn flyway:baseline

# Repair corrupted history
mvn flyway:repair
```

### 9. Common Pitfalls to Avoid

| Pitfall | Solution |
|---------|----------|
| Drop table without backup | Create rollback script first |
| Not using transactions | Wrap in BEGIN...COMMIT for critical changes |
| Modifying old migrations | Create new migration instead |
| Large migration files | Split into smaller, focused migrations |
| Missing setval after INSERT | Always reset sequences after manual inserts |

## Project-Specific Notes

- Migration Location: `backend-api/src/main/resources/db/migration/`
- Current Versions: V1 (schema), V2 (data)
- Baseline: Set at version 0 for existing databases
- Docker Integration: Flyway runs automatically with Spring Boot
- Test Configuration: Tests use Testcontainers with init scripts (Flyway disabled)

## Related Documentation

- ADR-007: Database Schema Versioning with Flyway
- SAR-007: Flyway Implementation Solution Architecture Report
- `.Codex/rules/database-migrations.md`: Rules for database changes
