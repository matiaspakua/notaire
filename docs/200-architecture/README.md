# Architecture documentation

Architecture of the Notaire system: arc42 view, design decisions, diagrams, data model,
security, observability, DevSecOps, and deployment.

## Structure

| Folder | Contents |
|---------|-----------|
| [`201-SAD/`](201-SAD/) | Software Architecture Document, structured with arc42 |
| [`202-ADR/`](202-ADR/) | Architecture Decision Records — the relevant architecture decisions |
| [`203-design/`](203-design/) | API design and the frontend design system |
| [`204-diagrams/`](204-diagrams/) | PlantUML diagrams: use cases, classes, states, sequence |
| [`205-data-model/`](205-data-model/) | ERD, data dictionary, and compensation diagram (referential integrity) |
| [`206-security/`](206-security/) | Authentication, input validation, and SQL injection prevention |
| [`207-monitoring/`](207-monitoring/) | Observability: Prometheus, Grafana, Loki |
| [`208-devsecops/`](208-devsecops/) | Docker infrastructure and DevSecOps practices |
| [`209-deployment/`](209-deployment/) | Deployment strategy and guide |

## Entry point

The [SAD](201-SAD/sad.md) is the root document: context, constraints, building-block
and runtime views, and links to the ADRs and diagrams that apply.

## Source of truth for the database schema

Flyway migrations (`backend-api/src/main/resources/db/migration/V*.sql`) are the only
source of truth for the schema (see [ADR-007](202-ADR/ADR-007-database-schema-versioning-flyway.md)
and [`.claude/rules/database-migrations.md`](../../.claude/rules/database-migrations.md)).
Any discrepancy between `205-data-model/` and Flyway is resolved in favor of Flyway.

## Navigation

- [← Documentation](../)
- [Business](../100-business/)
- [Development](../300-development/)
