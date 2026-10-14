> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The audit aspect inserts rows through Hibernate and never updates or deletes them. The table has a foreign key to `users` with no cascade, so audit rows already block deleting a user who acted.

## Goals / Non-Goals

**Goals:** audit history that a compromised application role cannot rewrite.
**Non-Goals:** tamper evidence against a superuser; hash chain; retention automation.

## Decisions

1. **Trigger plus REVOKE (defence in depth).** The trigger binds even the table owner until it is disabled on purpose; REVOKE removes the privilege for everyone else.
2. **Statement trigger for TRUNCATE**, because row triggers do not fire on it and `TRUNCATE` bypasses DELETE triggers.
3. **No hash chain now.** Chaining needs serialised inserts (a lock or advisory lock on every audit write) and a verification job; the trigger plus privileges close the stated SR-09 path. Revisit if a tamper-evidence requirement appears.
4. **Purge path:** the table owner disables `audit_records_append_only`, purges under change control, and re-enables it; no application role can.

## Riesgos / Trade-offs

- [A future migration needs to rewrite audit rows] → it must disable the trigger explicitly, which makes the intent reviewable.
- [Test cleanup that truncates the table] → only the H2 profile does, which has no Flyway PostgreSQL migrations.

## Testing Strategy

`testing/database/checks/60_audit_append_only.sql` (run by `testing/database/run.sh` in CI and natively with psql). Observed failing before the migration and passing after, on a real PostgreSQL.

## Regression Strategy

`mvn verify -pl backend-api`; Playwright and Bruno through `workspace/stack/native.sh`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

`V47__audit_records_append_only.sql`, idempotent (`CREATE OR REPLACE`, `DROP TRIGGER IF EXISTS`).

## Open Questions

None.
