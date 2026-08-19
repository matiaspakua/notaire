# Database Migrations (Flyway)

This directory contains all database schema migrations managed by Flyway.

## Overview

The Notaire project uses **Flyway** for database schema versioning. All database changes are version-controlled and automatically applied on application startup.

## Migration Files

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__initial_schema.sql` | Complete database schema (24 tables) |
| V2 | `V2__initial_data.sql` | Initial reference data and admin user |

## Creating a New Migration

### 1. Determine Next Version

Check the current highest version:
```bash
ls backend-api/src/main/resources/db/migration/
```

If the highest is V2, your new migration will be V3.

### 2. Create Migration File

Create a file with the naming convention: `V{n}__{description}.sql`

```bash
touch backend-api/src/main/resources/db/migration/V3__add_user_preferences.sql
```

### 3. Follow the Template

Every migration must include this header:

```sql
-- =============================================================================
-- V3__add_user_preferences.sql
-- =============================================================================
-- Author: Your Name
-- Date: 2026-04-14
-- Issue: #123
-- Description: Add user preferences table for storing user settings
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE user_preferences (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES usuarios(id_usuario),
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_prefs_user_id ON user_preferences(user_id);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM user_preferences; -- should return 0 initially
```

## Migration Naming Conventions

| Type | Format | Example |
|------|--------|---------|
| Forward | `V{n}__{description}.sql` | `V3__add_index.sql` |
| Rollback | `R{n}__{description}.sql` | `R3__rollback_index.sql` |

## Best Practices

1. **One logical change per migration** - Keep migrations focused
2. **Idempotent SQL** - Use `IF EXISTS` / `IF NOT EXISTS`
3. **Include verification** - Add SELECT queries to verify success
4. **Update sequences** - After INSERT with explicit IDs, run `setval()`
5. **Comment generously** - Explain WHY, not just WHAT

## Common Patterns

### Adding a Table

```sql
CREATE TABLE new_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Adding a Column

```sql
ALTER TABLE usuarios ADD COLUMN telefono VARCHAR(20);
ALTER TABLE usuarios ADD COLUMN celular VARCHAR(20);
```

### Adding an Index

```sql
CREATE INDEX IF NOT EXISTS idx_table_column ON table_name(column_name);
```

### Inserting Reference Data

```sql
INSERT INTO tipos_de_tramite (version, id_tipo_tramite, nombre, habilitado, se_archiva, se_inscribe, asocia_inmuebles)
SELECT 0, 99, 'New Type', true, true, true, false
WHERE NOT EXISTS (SELECT 1 FROM tipos_de_tramite WHERE nombre = 'New Type');

SELECT setval('tipos_de_tramite_id_tipo_tramite_seq', COALESCE(MAX(id_tipo_tramite), 1)) FROM tipos_de_tramite;
```

## Testing Migrations

### Validate Migrations

```bash
mvn flyway:validate
```

### Apply Migrations

```bash
mvn flyway:migrate
```

### View Migration Status

```bash
mvn flyway:info
```

## Anti-Patterns

| Don't | Do |
|-------|-----|
| Edit existing migrations | Create new migration |
| Non-idempotent SQL | Use IF EXISTS/NOT EXISTS |
| Skip version numbers | Use sequential versioning |
| Large migrations | Split into smaller ones |

## Documentation for AI Agents

AI agents should consult:
- **Skill**: `.claude/skills/flyway/SKILL.md`
- **Rules**: `.claude/rules/database-migrations.md`
- **ADR**: `docs/200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md`
- **SAR**: `docs/000-archive/200-architecture/202-ADR/SAR-007-flyway-implementation.md` (archived)

## Related Documentation

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway + Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.flyway)
- ADR-007: Database Schema Versioning with Flyway
