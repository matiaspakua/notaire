# ADR-029: Tenancy and authorization model (single tenant per notary)

**Status:** Accepted — Owner decision of 2026-10-14 on issue #1637 / CU78 (SR-11)  
**Date:** 2026-10-14  
**Deciders:** Owner (product), Engineering  
**Related:** ADR-019 (secrets), ADR-028 (release identity), #1638 (role split, `ROLE_NOTARY`), #559 (RBAC), SR-11 in [THREAT-MODEL-SYSTEM.md](../206-security/THREAT-MODEL-SYSTEM.md)  
**Supersedes:** the earlier *Proposed* text of this ADR (assigned-notary scope, `RecordAccessService`, `user_notaries`, deployment switch).

## Context

Authorization was role-based only: `ROLE_ADMIN` on administrative paths, any authenticated user on everything
else (#559, #1638). The first draft of this ADR proposed object-level ownership checks and asked the Owner
which deployment model applies. Facts measured on 2026-10-14:

- Three tables carry an owning notary (`deed_managements`, `folios`, `notebooks`); every other business record
  reaches one only through foreign-key chains, so ownership checks would need a scope resolver on ~30 tables.
- `roles` and `role_modules` exist and `RoleController` plus the admin page edit them, but **nothing reads
  `role_modules` to allow or deny a request**. Access is decided only by `users.type` → `ROLE_ADMIN` /
  `ROLE_NOTARY` / `ROLE_USER`.
- No Flyway migration inserts any `roles` or `role_modules` row, so a new installation has no default roles.
- `JWT_SECRET` and the actuator credentials come from the environment with no default.

## Owner decision (2026-10-14, issue #1637)

> The system works as a single tenant for each notary. Different installations are fully independent (the data)
> from each other. The backend, the frontend and the database data model are grouped as one product release,
> but the data stored is different for each notary. The roles and permissions are defined as a standard
> template for all, part of the database initialization, so a notary system starts with a default
> configuration; later each notary tenant can customize its system as desired.

## Decision

1. **One installation = one notary = one tenant.** The product release (backend, frontend, data model;
   versioned per ADR-028, its components listed in the release SBOM) is identical for every tenant. The data is
   not: each installation has its own database, `JWT_SECRET`, actuator credentials, observability stack and
   backups. There is no tenant id column, no shared database and no cross-tenant API.
2. **Cross-tenant isolation is a deployment property, not application code.** Cross-notary IDOR is impossible
   when no two notaries share a database or a signing secret. This is verified, not assumed: see *Isolation
   checklist* below.
3. **Inside a tenant the office shares its files.** Object-level ownership (assigned-notary scope,
   `RecordAccessService`, `user_notaries`, the deployment switch) is **rejected**. Option 3 (creator ownership)
   stays rejected.
4. **Access inside a tenant is role-based and tenant-customizable.**
   - A standard role template is seeded by Flyway as **insert-if-absent**: re-running or upgrading never
     updates or deletes a role or a module grant the tenant changed.
   - The tenant customizes roles and module grants through the existing roles administration.
   - The grants in `role_modules` are enforced **server-side** (deny by default for any path mapped to a
     module). Hiding a menu in the frontend is UX only.
5. **Safeguards:** the `ADMIN` role cannot be deleted and always keeps the role-management module (no
   self-lockout); a release that adds a module grants it to `ADMIN` only, tenants grant it to other roles.

## Challenges and validation

| Challenge | Finding | Outcome |
|---|---|---|
| Does single tenant really remove SR-11? | Only the *cross-notary* part. Employees of one office can still open each other's files. | Accepted by design (the office shares files); residual risk covered by module RBAC and the PII read audit (#1639). |
| Is the customizable RBAC real? | Stored and editable, but **not enforced**; and **no default template exists**. | The real gap, now work items W1 and W2. |
| Can two tenants accept each other's tokens? | Only if they share `JWT_SECRET` (the same release means the same usernames and ids). | Per-tenant secret is mandatory; checklist item and test W3. |
| Seeded template vs. tenant edits | A later migration that rewrites roles would erase customizations. | Insert-if-absent only (Decision 4); upgrade test in W1. |
| Admin lockout | A tenant could remove every grant from its own administrators. | Safeguard in Decision 5; test in W2. |
| Shared operations tooling | A single Prometheus/Loki/Sonar stack across tenants would mix tenants' data. | One observability stack per installation; stated in the SAD deployment view. |

## Isolation checklist (per installation)

- Own PostgreSQL instance or database and credentials; no shared schema.
- Own `JWT_SECRET`, `ACTUATOR_*`, application admin password; none equal to a default
  (`ProductionCredentialsGuard`, #1639).
- Own object storage/uploads path and backups.
- Own observability stack and dashboards (no cross-tenant labels or datasources).

## Work items

- **W1** Seed the standard role template (Flyway, insert-if-absent) and prove an upgrade keeps tenant edits.
- **W2** Enforce `role_modules` server-side with deny-by-default and the admin safeguards.
- **W3** Document and test the isolation checklist (secret/credential distinctness, no default secrets).
- **W4** Close SR-11 in the register: cross-tenant *mitigated by architecture*, intra-tenant *accepted*.

## Consequences

- No new tables, no per-request scope resolution, no change to the 39 controllers' signatures for ownership.
- The effort moves to making the existing role administration real (W1, W2).
- Offering several notaries from one running instance is explicitly out of scope; it would need a new ADR.
