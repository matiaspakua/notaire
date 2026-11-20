> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The packaged property is `ddl-auto=none`. The native helper overrides it on the command line with `update`.

## Goals / Non-Goals

**Goals:** the native helper starts the backend with `ddl-auto=none`, so Flyway is the schema writer there too.
**Non-Goals:** Spring profiles, or changing which suite CI runs for schema validation.

## Decisions

1. Change the one flag. Flyway is already enabled in the packaged jar, so the helper does not need Hibernate to create tables.

## Riesgos / Trade-offs

- [A local database that was only shaped by Hibernate update, with no Flyway history, will fail to start] → that database was already outside the Flyway rule. A fresh database migrates.

## Testing Strategy

`test_native_ddl_auto.py` reads `native.sh` and rejects `ddl-auto=update`.

## Regression Strategy

`test_ddl_auto_pinned.py` stays. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

No image change. The helper is a local script.

## Rollback Strategy

Revert the commit.
