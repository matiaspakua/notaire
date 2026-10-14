# Standard role template

Each installation is a single tenant (ADR-029) and starts from this template, seeded by Flyway `V50`
(#1682). A role is created only when no role has that name, and grants are inserted only for the roles
created by the migration, so a tenant's own roles and grants are never overwritten. The tenant then
customizes the roles in the administration area.

| Role | Modules |
|------|---------|
| Administrator | administracion, usuarios, tramites, presupuestos, escrituras, personas, auditoria, workflows |
| Notary | tramites, presupuestos, escrituras, personas, auditoria, workflows |
| Office Staff | tramites, presupuestos, escrituras, personas |

The module keys are the ones offered by the roles administration page. The seeded `admin` account is
linked to Administrator. Server-side enforcement of the grants is tracked in #1683.
