# flyway-migration-docs Specification

## Purpose
TBD - created by archiving change docs-1980-migration-readme-flyway. Update Purpose after archive.
## Requirements
### Requirement: Flyway docs omit stale version ranges

The migration README, `.claude/rules/database-migrations.md`, and the Flyway comment in `application.properties` MUST NOT claim a closed range such as `V1→V11+` or list only V1/V2 as the current migration set. The README MUST point agents to the generated database docs on Pages and MUST document the `R{n}__*.sql` rollback convention.

#### Scenario: Stale V1→V11+ wording is absent

- **GIVEN** the repository tip
- **WHEN** the docs guard test scans the migration README, database-migrations rule, and application.properties Flyway comment
- **THEN** none of those paths contain the substring `V1→V11+`

