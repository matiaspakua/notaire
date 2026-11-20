> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`application.properties` defaults `ddl-auto` to `none` but still reads `SPRING_JPA_HIBERNATE_DDL_AUTO`. Compose and the production deployment already pass `none`. The placeholder is the silent override.

## Goals / Non-Goals

**Goals:** the packaged application always uses `ddl-auto=none`.
**Non-Goals:** Spring profiles, changing the native helper's explicit command-line flag, or moving CI onto Testcontainers by default.

## Decisions

1. Delete the placeholder. A command-line flag can still override a Spring property; the native helper keeps its explicit flag. The environment variable no longer can.
2. Guard the file with a unit test so the placeholder cannot return unnoticed.

## Riesgos / Trade-offs

- [An operator who set the env var to `validate` loses that knob] → production and compose already use `none`, which is the Flyway rule.

## Testing Strategy

`docs/tests/test_ddl_auto_pinned.py` reads `application.properties` and requires the literal `spring.jpa.hibernate.ddl-auto=none` and the absence of `SPRING_JPA_HIBERNATE_DDL_AUTO`.

## Regression Strategy

Existing `CiDdlAutoNoneTest` stays. JaCoCo floors are not lowered.

## Playwright Strategy

No UI change. n/a.

## Deployment Strategy

Ships with the backend image. No migration. Compose and kustomize already set the env var to `none`; that variable is no longer read.

## Rollback Strategy

Revert the commit. The API shape is unchanged.
