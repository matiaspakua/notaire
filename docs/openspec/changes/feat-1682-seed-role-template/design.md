> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The module keys come from the roles administration page: administracion, usuarios, tramites, presupuestos, escrituras, personas, auditoria, workflows. users.fk_id_role already exists.

## Goals / Non-Goals

**Goals:** a working default configuration at first start; tenant edits survive upgrades.
**Non-Goals:** changing how requests are authorized.

## Decisions

1. **One versioned migration with a CTE per role.** Flyway runs it once, and the CTE inserts grants only for rows it created, so existing roles are untouched. 2. **English role names.** They are user-facing data and the project englishizes touched text.

## Riesgos / Trade-offs

- [A tenant already has a role named like a template role] → that role is kept as is and its grants are not changed.
- [Name collision hides a template role] → documented; tenant can recreate it.

## Testing Strategy

RoleTemplateMigrationIntegrationTest (pg-integration, real Flyway) covers every scenario.

## Regression Strategy

mvn verify -pl backend-api -Ppg-integration; python3 workspace/release/product_release.py --write

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
