# Design

## Context

`application.properties` defaults `APP_ADMIN_PASSWORD` to `admin`; `ProductionCredentialsGuard` only acts when `app.environment=production`.

## Goals / Non-Goals

Goal: no seeder-created admin/admin outside dev/test. Non-goal: the legacy Flyway V2 MD5 seed and changing the `development` default of `app.environment` (Owner decision).

## Decisions

Skip the seed (do not fail startup) so CI/staging still boot; an error log makes the missing secret visible.

## Riesgos / Trade-offs

A launch path that relies on the default `development` environment still seeds admin/admin; Flyway V2 also seeds it on a fresh database.

## Testing Strategy

Four new unit tests in `DataInitializerTest`, written first and observed failing.

## Regression Strategy

Full backend `mvn test`.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

Ships with the next backend image; no migration.

## Rollback Strategy

Revert the commit.
