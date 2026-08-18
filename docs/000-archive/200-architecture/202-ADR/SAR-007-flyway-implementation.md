> **Archived:** this Solution Architecture Report was previously misfiled as
> `docs/200-architecture/202-ADR/ADR-012-flyway-implementation.md`. It duplicated the decision already
> recorded in [ADR-007](../../../../200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md),
> which now includes this document's unique "AI Agent Guidelines" section. Kept here for historical
> reference only.

# SAR-007: Flyway Implementation for Database Schema Versioning

**Date:** 2026-04-14
**Status:** Implemented
**Author:** Matias Miguez
**Issue:** #252

---

## 1. Executive Summary

This document details the implementation of Flyway as a schema version control system for the Notaire project's PostgreSQL database.

### 1.1 Objective

Replace static SQL scripts in `init-db/` (now archived at `docs/archive/init-db/`) with versioned Flyway migrations to enable:
- Schema version control
- Safe rollbacks
- CI/CD integration
- Reproducibility across environments

### 1.2 Impact

| Aspect | Before | After |
|--------|--------|--------|
| Schema versioning | No | Yes (Flyway) |
| Rollback | Manual | Native SQL |
| Schema deploy | Manual scripts | Automated |
| Testing | Ad-hoc | Reproducible |

---

## 2. Technical Analysis

### 2.1 Current State

**File structure:**
```
init-db/ (now at docs/archive/init-db/)
├── 01-schema.sql      # 371 lines - Complete schema
├── 02-data.sql        # 94 lines - Initial data
├── migrate.load        # Historical documentation
└── README.md          # Documentation
```

**Current execution:**
- Previously Docker Compose mounted `./init-db` to `/docker-entrypoint-initdb.d/` (now removed — Flyway is sole source)
- Scripts run ONLY on first container creation
- No control over subsequent changes

### 2.2 Migration to Flyway

**Strategy:** Direct conversion
- `01-schema.sql` → `V1__initial_schema.sql`
- `02-data.sql` → `V2__initial_data.sql`

**Baseline:** Required for existing databases
```sql
INSERT INTO flyway_schema_history (version, description, type, installed_on)
VALUES (0, '<< BASELINE >>', 'BASELINE', NOW());
```

---

## 3. Solution Design

### 3.1 Migration Architecture

```
backend-api/src/main/resources/db/migration/
├── V1__initial_schema.sql     # Complete schema
├── V2__initial_data.sql       # Initial data
├── V3__add_indexes.sql        # Future performance indexes
└── R2__rollback_data.sql     # Optional rollback
```

### 3.2 Spring Boot Configuration

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

### 3.3 Maven Dependencies

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

## 4. AI Agent Guidelines

### 4.1 Configuration Files

The project includes the following files for AI agents to understand Flyway conventions:

| File | Description |
|------|-------------|
| `.claude/skills/flyway/SKILL.md` | Complete Flyway skill with examples |
| `.claude/rules/database-migrations.md` | Mandatory rules for migrations |

### 4.2 Mandatory Rules for Agents

1. **NEVER modify existing migrations** - Always create new migrations
2. **Always use versioning** - Format `V{n}__{description}.sql`
3. **Mandatory location** - `backend-api/src/main/resources/db/migration/`
4. **Idempotent SQL** - Use `IF EXISTS` / `IF NOT EXISTS`
5. **Sequences** - Include `setval` after INSERT

### 4.3 Workflow for Agents

```
1. Analyze required schema change
2. Verify current version: ls db/migration/
3. Create new migration: V{n+1}__{description}.sql
4. Include template with header and comments
5. Validate with: mvn flyway:validate
6. Commit with descriptive message referencing issue
```

### 4.4 Migration Template for Agents

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

### 4.5 Convention Verification

Before committing, verify:
- [ ] Filename follows `V{n}__{description}.sql` format
- [ ] Migration located in `db/migration/`
- [ ] Complete header with Author, Date, Description
- [ ] SQL is idempotent (IF EXISTS/NOT EXISTS)
- [ ] Sequences updated after INSERT
- [ ] Unit tests include script validation

### 4.6 Additional Resources

For more information, see:
- **Skill**: `.claude/skills/flyway/SKILL.md`
- **Rules**: `.claude/rules/database-migrations.md`
- **ADR**: `docs/02-architecture/01-adr/ADR-007-database-schema-versioning-flyway.md`

---

## 5. References

- ADR-007: Database Schema Versioning with Flyway
- ADR-004: Database Migration MySQL → PostgreSQL
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway + Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.flyway)
