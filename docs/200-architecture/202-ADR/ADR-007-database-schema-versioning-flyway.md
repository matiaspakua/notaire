# ADR-007: Schema versioning with Flyway

**Status:** Accepted
**Date:** 2026-04-14
**Deciders:** Matías Miguez
**Supersedes:** ADR-004 (Liquibase section)
**Related:** ADR-001 (Microservices Architecture), ADR-004 (Database Migration)

## Context

Notaire used to apply the database schema with static SQL in `init-db/`, run once when the PostgreSQL volume was created. That approach had several problems:

### Problems

1. **No schema history**: there was no record of schema changes
2. **No easy rollback**: there was no mechanism to reverse a bad change
3. **Manual migrations**: schema changes needed a person to apply them
4. **Weak CI/CD**: the scripts did not fit a deploy pipeline
5. **Production risk**: a schema change could leave environments inconsistent

### Former layout

```text
init-db/
├── 01-schema.sql    # table creation (371 lines)
├── 02-data.sql      # seed data (94 lines)
├── migrate.load     # historical notes
└── README.md
```

Those scripts ran through:

```yaml
# former docker-compose.yml volume
volumes:
  - ./init-db:/docker-entrypoint-initdb.d:ro
```

They ran only on the **first creation** of the Docker volume. The folder is now archived at `docs/000-archive/init-db/`. Compose no longer mounts it.

## Decision

**Use Flyway** to version the database schema.

### Why Flyway, not Liquibase

| Criterion | Flyway | Liquibase |
|-----------|--------|-----------|
| Learning curve | Low (plain SQL) | Medium (XML/YAML/JSON) |
| Maintenance | Simple (SQL files) | Higher (declarative formats) |
| Spring Boot | `spring-boot-starter-flyway` | Extra setup |
| Team familiarity | High (standard SQL) | Medium |
| Rollback | Hand-written SQL | Needs a changelog |

### ADR-004 had recommended Liquibase

ADR-004 named Liquibase in its original sketch. After review:

1. **Plain SQL is simpler**: the team already writes SQL
2. **Less abstraction**: Flyway runs SQL directly
3. **Spring Boot**: the project depends on `spring-boot-starter-flyway`, plus `flyway-database-postgresql`
4. **Straight conversion**: the old scripts became versioned SQL files

### Flyway configuration

#### 1. Maven dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

#### 2. Migration location

```text
backend-api/src/main/resources/
├── db/migration/
│   ├── V1__initial_schema.sql
│   ├── V2__initial_data.sql
│   └── V45__rename_remaining_spanish_identifiers.sql
└── application.properties
```

`V1__initial_schema.sql`, `V2__initial_data.sql`, and `V45__rename_remaining_spanish_identifiers.sql` exist. Applied filenames V1–V44 stay as recorded, including Spanish words in those names. New migrations use English descriptions. Undo scripts use the `R{version}__` prefix (`R45__rename_remaining_spanish_identifiers.sql`).

#### 3. Spring Boot settings

These match `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.locations=classpath:db/migration
spring.flyway.out-of-order=false
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true
```

`clean` is disabled. Do not run `mvn flyway:clean` against a shared database.

#### 4. Naming

```text
V{version}__{description}.sql
  version: 1, 2, 3... (integers)
  description: snake_case

Examples:
- V1__initial_schema.sql
- V10__seed_workflow_demo_data.sql
- V45__rename_remaining_spanish_identifiers.sql
```

Seeded labels inside a migration, such as `Workflow de Gestión Estándar`, stay as stored data. Applied migration files are not edited.

#### 5. Baseline

For a database that already has the schema, baseline it:

```sql
-- Baseline (records the current state)
INSERT INTO flyway_schema_history (version, description, type, installed_on)
VALUES (0, '<< BASELINE >>', 'BASELINE', NOW());
```

Prefer `mvn flyway:baseline` or `baseline-on-migrate` over a hand-written insert. The insert above is the idea, not a script to apply.

## Options considered

### Option A: Keep static scripts (status quo)

| Pros | Cons |
|------|------|
| No change | No versioning |
| Simple at first | Rollback is hard |
| Direct SQL | Does not fit CI/CD |
| | Production risk |

### Option B: Liquibase (named in ADR-004)

| Pros | Cons |
|------|------|
| Automatic rollback | XML/YAML overhead |
| Cross-database | More complexity |
| Versioned changelog | Learning curve |

### Option C: Flyway (selected)

| Pros | Cons |
|------|------|
| Plain SQL | Rollback is manual |
| Simple | No declarative changelog |
| Spring Boot starter | |
| Low maintenance | |

### Option D: Doctrine migrations

| Pros | Cons |
|------|------|
| PHP/Doctrine | Outside the Java stack |
| TypeScript | Does not apply |

## Trade-off analysis

### Effort versus benefit

| Aspect | Before | After |
|--------|--------|-------|
| Schema versioning | No | Yes (V1, V2, V3...) |
| Rollback | Manual | A new undo script |
| CI/CD | Manual | Runs on startup and in pipelines |
| Schema tests | Ad hoc | Repeatable |

### Migration path

1. **Phase 1**: move the current scripts into Flyway (V1, V2)
2. **Phase 2**: baseline existing databases
3. **Phase 3**: update Docker Compose
4. **Phase 4**: add a new migration for each later change

## Consequences

### Positive

- **Full history**: each change is a migration
- **Repeatable schema**: the same files run in every environment
- **CI/CD**: migrations run as part of startup
- **Audit**: `flyway_schema_history` records each apply
- **Rollback**: an `R{version}` script can reverse a change
- **Schema tests**: migrations can be exercised by the test suite

### Negative

- **Learning curve**: the team follows Flyway naming and immutability
- **Upfront work**: the old scripts had to be converted
- **Discipline**: every schema change is a new migration
- **No automatic rollback**: an undo script is written by hand

## Implementation plan

### Phase 1: setup (sprint 1)

- [x] Add the Flyway dependency to `pom.xml`
- [x] Create `db/migration/`
- [x] Move `01-schema.sql` to `V1__initial_schema.sql`
- [x] Move `02-data.sql` to `V2__initial_data.sql` (the archived copy stays at `docs/000-archive/init-db/`)
- [x] Configure baseline in `application.properties`
- [ ] Prove a local startup applies the pending migrations

### Phase 2: Docker (sprint 1) — done

- [x] `docker-compose.yml` enables Flyway (`SPRING_FLYWAY_ENABLED`)
- [x] Remove the `init-db` volume (archived at `docs/000-archive/init-db/`)
- [ ] Add a Flyway-specific healthcheck
- [ ] Test migrate and clean in containers (`clean` is disabled)

### Phase 3: CI/CD (sprint 2)

- [ ] Add a Flyway step in GitHub Actions
- [ ] Validate migrations on pull requests
- [ ] Document rollback procedures

### Phase 4: testing (sprint 2)

- [ ] Integration tests with Testcontainers
- [ ] Rollback tests
- [ ] Idempotence tests

The checkboxes above are the original plan. They were not all kept current. What the repo shows today is listed in the checked items.

## Technical details

### Schema history table

Flyway creates:

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

### Commands

```bash
# Validate migrations
mvn flyway:validate

# Apply migrations
mvn flyway:migrate

# Show state
mvn flyway:info

# Clean is disabled (spring.flyway.clean-disabled=true)
# mvn flyway:clean

# Baseline an existing database
mvn flyway:baseline
```

### Spring Boot

Spring Boot configures Flyway when the starter is on the classpath. Startup is `NotaireBackendApplication`.

## Migration script templates

### Forward migration

```sql
-- V{version}__{description}.sql
-- Author: {name}
-- Date: {date}
-- Description: {description}

-- =============================================================================
-- UP migration
-- =============================================================================

-- SQL here

-- =============================================================================
-- Verification
-- =============================================================================
-- SELECT COUNT(*) FROM table_name; -- expect X rows
```

### Undo migration

```sql
-- R{version}__{description}.sql
-- Author: {name}
-- Date: {date}
-- Description: undo of V{version}

-- =============================================================================
-- DOWN migration
-- =============================================================================

-- Undo SQL here
```

Do not edit an applied `V` or `R` file. Add the next version instead.

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway and PostgreSQL](https://flywaydb.org/documentation/database/postgresql/)
- [Flyway Best Practices](https://flywaydb.org/documentation/bestPractices.html)
- ADR-004: Database migration from MySQL to PostgreSQL
- Issue #252: Implement Flyway for database schema migration

## AI agent guidelines

Reference files AI coding agents must consult before writing a migration:

| File | Description |
|------|-------------|
| `.claude/skills/flyway/SKILL.md` | Flyway skill with examples |
| `.claude/rules/database-migrations.md` | Mandatory rules for migrations |

Mandatory rules for agents:

1. **Never modify existing migrations** — always create a new one.
2. **Always version** — format `V{n}__{description}.sql`.
3. **Mandatory location** — `backend-api/src/main/resources/db/migration/`.
4. **Idempotent SQL** — use `IF EXISTS` / `IF NOT EXISTS`.
5. **Sequences** — include `setval` after any `INSERT` with an explicit ID.

Agent workflow: analyze the required schema change → check the current version (`ls db/migration/`) → create `V{n+1}__{description}.sql` with the standard header template (see [Migration script templates](#migration-script-templates) above) → validate with `mvn flyway:validate` → commit referencing the issue.

## Related ADRs

- ADR-001: Microservices architecture
- ADR-004: Database migration (MySQL to PostgreSQL)
- ADR-005: Frontend migration from Java Swing to Next.js
- ADR-006: Testing strategy
