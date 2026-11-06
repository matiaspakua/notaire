# Database Migrations (Flyway)

This directory contains all database schema migrations managed by Flyway.

## Overview

The Notaire project uses **Flyway** for database schema versioning. All database
changes are version-controlled and automatically applied on application startup.
PostgreSQL starts empty in Docker; Flyway applies every pending `V{n}__*.sql`
script in version order and records them in `flyway_schema_history`.

Do **not** maintain a hand-kept table of migrations here. The generated inventory
on GitHub Pages is the live list:

- **Pages:** [`/docs/database/`](https://matiaspakua.github.io/notaire/docs/database/)
- **Source tree:** `ls backend-api/src/main/resources/db/migration/V*.sql`

## Creating a New Migration

### 1. Determine Next Version

```bash
ls backend-api/src/main/resources/db/migration/V*.sql | sort -V | tail -5
```

Use the next integer after the highest `V{n}` already present (and after any
in-flight branch migrations that will land first).

### 2. Create Migration File

Naming convention: `V{n}__{description}.sql`

```bash
touch backend-api/src/main/resources/db/migration/V{n}__add_user_preferences.sql
```

### 3. Follow the Template

Every migration must include this header:

```sql
-- =============================================================================
-- V{n}__add_user_preferences.sql
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
    user_id INTEGER NOT NULL REFERENCES users(id),
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
| Forward | `V{n}__{description}.sql` | `V53__example.sql` |
| Rollback | `R{n}__{description}.sql` | `R51__rollback_example.sql` |

### Rollback scripts (`R{n}`)

Optional companion scripts undo a forward migration for local recovery or
documented rollback drills. Name them `R{n}__*.sql` with the **same `n`** as the
`V{n}` they reverse. Examples already in tree include `R14`, `R45`, `R46`, and
`R51`. Flyway does not auto-apply `R*` scripts on startup — operators run them
deliberately when rolling back.

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
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
ALTER TABLE users ADD COLUMN mobile_phone VARCHAR(20);
```

### Adding an Index

```sql
CREATE INDEX IF NOT EXISTS idx_table_column ON table_name(column_name);
```

### Inserting Reference Data

```sql
INSERT INTO procedure_types (version, id, name, enabled, is_archived, is_registered, associates_properties)
SELECT 0, 99, 'New Type', true, true, true, false
WHERE NOT EXISTS (SELECT 1 FROM procedure_types WHERE name = 'New Type');

SELECT setval('procedure_types_id_seq', COALESCE(MAX(id), 1)) FROM procedure_types;
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
| Hand-kept version tables in this README | Link to Pages `/docs/database/` |

## Documentation for AI Agents

AI agents should consult:
- **Skill**: `.claude/skills/flyway/SKILL.md`
- **Rules**: `.claude/rules/database-migrations.md`
- **ADR**: `docs/200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md`
- **SAR**: `docs/000-archive/200-architecture/202-ADR/SAR-007-flyway-implementation.md` (archived)

## Related Documentation

- [Flyway Documentation](https://flywaydb.org/documentation/)
- Generated schema inventory: GitHub Pages `/docs/database/`
