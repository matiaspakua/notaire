# Database Migration Rules

This file provides rules for database schema migrations using Flyway in the Notaire project.

## Overview

The Notaire project uses **Flyway** for database schema versioning. All database changes MUST be managed through Flyway migrations.

## Mandatory Rules

### 1. NEVER Modify Existing Migrations

- **DO NOT** edit existing migration files (V1, V2, etc.)
- **DO NOT** add new SQL to existing migrations
- **ALWAYS** create a new migration file for schema changes

### 2. Always Create Versioned Migrations

All schema changes MUST use the versioned migration format:

```
V{next_version}__{short_description}.sql
```

**Examples:**
```sql
-- CORRECT: New migration file
-- File: V3__add_user_preferences_table.sql
CREATE TABLE user_preferences (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES usuarios(id_usuario),
    theme VARCHAR(50) DEFAULT 'light'
);

-- INCORRECT: Modifying existing V2 file
-- NEVER do this!
```

### 3. Migration Location

All migrations MUST be placed in:
```
backend-api/src/main/resources/db/migration/
```

### 4. Required Migration Template

Every migration MUST include this header:

```sql
-- =============================================================================
-- V{version}__{description}.sql
-- =============================================================================
-- Author: {name}
-- Date: {date}
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

### 5. Idempotent SQL

Write SQL that can be run multiple times safely:

```sql
-- GOOD: Idempotent
CREATE INDEX IF NOT EXISTS idx_users_email ON usuarios(email);
DROP TABLE IF EXISTS temp_table;

-- BAD: Will fail on second run
CREATE INDEX idx_users_email ON usuarios(email);
CREATE TABLE new_table (...);
```

### 6. Sequence Management

After INSERT with SERIAL columns, always reset sequences:

```sql
-- After inserting with explicit ID
INSERT INTO conceptos (version, id_concepto, nombre, ...) VALUES (1, 99, 'New', ...);
SELECT setval('conceptos_id_concepto_seq', COALESCE(MAX(id_concepto), 1)) FROM conceptos;
```

### 7. Foreign Key Considerations

- Always create parent tables before child tables
- Use `REFERENCES` for inline FK or `ALTER TABLE` after creation
- Consider naming constraints: `fk_{table}_{referenced_table}`

```sql
-- Option 1: Inline
CREATE TABLE child (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES parent(id)
);

-- Option 2: After creation (for circular references)
CREATE TABLE child (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER
);
ALTER TABLE child ADD CONSTRAINT fk_child_parent FOREIGN KEY (parent_id) REFERENCES parent(id);
```

## Rollback Strategy

### When to Create Rollback Scripts

Create rollback scripts (`R{version}__{description}.sql`) for:
- Destructive operations (DROP TABLE, DROP COLUMN)
- Data migrations that need reversal
- Structural changes that might need reverting

### Rollback Template

```sql
-- =============================================================================
-- R{version}__{description}.sql
-- =============================================================================
-- Author: {name}
-- Date: {date}
-- Description: Rollback for V{version}
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration (Rollback)
-- -----------------------------------------------------------------------------

DROP INDEX IF EXISTS idx_users_email;
```

## Testing Migrations

### Local Testing

```bash
# Validate all migrations
mvn flyway:validate

# Apply migrations
mvn flyway:migrate

# View status
mvn flyway:info

# Clean (WARNING: destroys data!)
mvn flyway:clean
```

### Test Environments

- Tests use Testcontainers with `init-db/` scripts (Flyway disabled)
- Unit tests validate migration script content
- Integration tests validate schema structure

## Version Numbering

### How to Determine Next Version

1. Check existing migrations:
   ```bash
   ls backend-api/src/main/resources/db/migration/
   # Output: V1__initial_schema.sql, V2__initial_data.sql
   ```

2. Next version is 3 (increment from highest)

### Version Rules

- Use integers only (1, 2, 3, not 1.0, 1.1)
- Never reuse version numbers
- Never skip versions (except for emergencies)

## Common Patterns

### Adding a New Table

```sql
-- V3__add_user_preferences_table.sql

CREATE TABLE user_preferences (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES usuarios(id_usuario),
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_prefs_user_id ON user_preferences(user_id);
```

### Adding a Column

```sql
-- V4__add_users_phone_column.sql

ALTER TABLE usuarios ADD COLUMN telefono VARCHAR(20);
ALTER TABLE usuarios ADD COLUMN celular VARCHAR(20);
```

### Adding an Index

```sql
-- V5__add_common_query_indexes.sql

CREATE INDEX IF NOT EXISTS idx_escrituras_fecha ON escrituras(fecha_escrituracion);
CREATE INDEX IF NOT EXISTS idx_gestiones_estado ON gestiones_de_escrituras(fk_id_estado_de_gestion);
```

### Inserting Reference Data

```sql
-- V6__add_new_tipo_tramite.sql

INSERT INTO tipos_de_tramite (version, id_tipo_tramite, nombre, habilitado, se_archiva, se_inscribe, asocia_inmuebles)
SELECT 0, 99, 'Nueva Categoria', true, true, true, false
WHERE NOT EXISTS (SELECT 1 FROM tipos_de_tramite WHERE nombre = 'Nueva Categoria');

SELECT setval('tipos_de_tramite_id_tipo_tramite_seq', COALESCE(MAX(id_tipo_tramite), 1)) FROM tipos_de_tramite;
```

### Modifying Data (Data Migration)

```sql
-- V7__migrate_user_status_values.sql

-- Update legacy status values
UPDATE usuarios SET tipo = 'Administrador' WHERE tipo = 'Admin';
UPDATE usuarios SET tipo = 'Usuario' WHERE tipo = 'Empleado';
```

## Anti-Patterns to Avoid

| Anti-Pattern | Why Bad | Correct Approach |
|--------------|---------|-----------------|
| Edit existing migration | Breaks reproducibility | Create new migration |
| No comments | Unclear intent | Add header + inline comments |
| Non-idempotent SQL | Fails on re-run | Use IF EXISTS/NOT EXISTS |
| Missing sequences | Breaks auto-increment | Include setval after INSERT |
| Large migration | Hard to review/rollback | Split into smaller migrations |

## CI/CD Integration

Migrations are validated in CI via:
1. Maven build includes Flyway validate
2. Tests run against schema created by migrations
3. Docker startup runs Flyway migrate automatically

## Emergency Procedures

### If a Migration Fails

1. **Do NOT** modify the migration file
2. **DO** create a new rollback migration
3. **DO** document the failure in PR comments
4. **DO** notify team of the issue

### If You Need to Skip Versions

In extreme cases (corrupted history):
```bash
# Repair Flyway history
mvn flyway:repair

# Then baseline from current state
mvn flyway:baseline
```

## Related Documentation

- `.claude/skills/flyway/SKILL.md` - Detailed Flyway skill
- `ADR-007` - Architecture Decision Record for Flyway
- `SAR-007` - Solution Architecture Report
