# ADR-029: Object-level authorization (record ownership)

**Status:** Proposed — needs the Owner's decision on the open question below (issue #1637 / CU78, SR-11)  
**Date:** 2026-10-14  
**Deciders:** Owner (product), Engineering  
**Related:** ADR-019 (secrets), #1638 (role split, `ROLE_NOTARY`), #559 (RBAC), SR-11 in [THREAT-MODEL-SYSTEM.md](../206-security/THREAT-MODEL-SYSTEM.md)  
**Supersedes:** nothing.

## Context

Authorization is role-based only: `ROLE_ADMIN` on administrative paths, any authenticated user on
everything else (#559, #1638). Any authenticated user can therefore read, change or delete any record by
id (IDOR, OWASP A01).

Measured on a migrated database (2026-10-14):

- Three tables carry an owner: `deed_managements`, `folios` and `notebooks` have `fk_id_notary_person`
  (the notary who owns the file). All 53, 71 and 5 sample rows have it set.
- Every other business record reaches a notary only through a chain: `procedures → deed_managements`,
  `procedures → budgets → payments`, `deeds → testimonies → copies`, `history → deed_managements`,
  `submitted_documents → procedures`. `budgets` links to `people` (the client), not to a notary.
- `users.fk_id_person` links a login to a person; `people.is_notary` marks notaries; `substitutions`
  records who covers whom.
- Notaries and employees of one notarial office normally share the office's files. A deployment with
  several independent notaries (a "registry" with separate protocols) is the case that needs isolation.

## Options

1. **Office-wide access (status quo, documented).** Every authenticated user sees every record. Simple, no
   isolation between notaries. Acceptable only for a single-notary deployment.
2. **Assigned-notary scope.** A request may touch a record only if the record's owning notary is in the
   caller's *notary scope*: the caller's own person when `is_notary`, plus persons they substitute
   (`substitutions`), plus, for employees, the notary persons assigned to them. Chains resolve to
   `deed_managements.fk_id_notary_person`. `ROLE_ADMIN` bypasses the check.
3. **Creator ownership.** Add `created_by_user` to every table and allow only the creator (and admins).
   Wrong for an office where staff prepare files for the notary, and needs a column on ~30 tables.

## Decision (proposed)

Adopt **option 2**, behind a deployment switch so single-notary offices keep option 1:

1. One `RecordAccessService` in the application layer answers `canAccess(user, resource)` for deeds,
   testimonies, budgets, payments and people (the five families in the issue). Services call it; controllers
   never do. It resolves the owning notary through the chains above and compares it with the caller's scope.
2. `security.object-authorization.enabled` (default `false`) turns the check on. With it off the behaviour is
   today's, so the upgrade is non-breaking. With it on, a record outside the scope answers **404** (not 403) so
   ids do not leak existence; lists are filtered, not rejected.
3. Employees get their notary scope from a new join table `user_notaries(fk_id_user, fk_id_notary_person)`
   (Flyway `V50`); an employee with no rows sees nothing while the switch is on. Seeded `admin` is unaffected.
4. Tests first, per family: user A gets 404 on user B's resource for GET, PUT and DELETE; admin keeps access;
   the Bruno and Playwright suites run with the switch off (default) and a dedicated suite with it on.

## Consequences

- One new table, one service, one switch; no change to the 39 controllers' signatures.
- Costs: a join per check on hot lists. Mitigation: resolve the scope once per request and push it into the
  repository query as a notary-id set.
- Risk: a missed family stays open. The test per family and a controller scan (every handler taking an id
  must go through the service) bound it.

## Open question for the Owner

Is the deployment model (a) one notary office sharing all files, (b) several independent notaries, or (c)
both? Option 2 with the switch off covers (a) and with it on covers (b). If (a) only, accept option 1 and
close SR-11 as "accepted risk" in the register instead of building the service.
