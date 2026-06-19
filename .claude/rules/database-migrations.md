# Database Migration Rules

This file provides rules for database schema migrations using Flyway in the Notaire project.

## Overview

The Notaire project uses **Flyway** as the single source of truth for database schema versioning. All database changes MUST be managed through Flyway migrations.

> ✅ **Flyway is now the sole schema source in Docker.** The old `init-db/`
> scripts (formerly mounted at `/docker-entrypoint-initdb.d/`) have been archived
> at `docs/archive/init-db/`. PostgreSQL starts empty in Docker and Flyway
> applies V1→V11+ sequentially on first startup. The `flyway_schema_history`
> table tracks which migrations have run. This replaces the previous dual-source
> setup where `init-db/` created the schema first and Flyway stayed dormant.

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

## Rollback Strategy

### When to Create Rollback Scripts

Create rollback scripts (`R{version}__{description}.sql`) for:
- Destructive operations (DROP TABLE, DROP COLUMN)
- Data migrations that need reversal
- Structural changes that might need reverting

## Anti-Patterns to Avoid

| Anti-Pattern | Why Bad | Correct Approach |
|--------------|---------|-----------------|
| Edit existing migration | Breaks reproducibility | Create new migration |
| No comments | Unclear intent | Add header + inline comments |
| Non-idempotent SQL | Fails on re-run | Use IF EXISTS/NOT EXISTS |
| Missing sequences | Breaks auto-increment | Include setval after INSERT |
| Large migration | Hard to review/rollback | Split into smaller migrations |

## Related Documentation

- `.claude/skills/flyway/SKILL.md` - Detailed Flyway skill
- `ADR-007` - Architecture Decision Record for Flyway
- `SAR-007` - Solution Architecture Report
