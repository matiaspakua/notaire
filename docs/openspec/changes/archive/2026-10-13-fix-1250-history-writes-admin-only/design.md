# Design

## Context

Issue #1250 triage (bucket B) found that history writes were outside `ADMIN_WRITE_PATHS`. The Owner chose to restrict them to administrators rather than remove them.

## Goals / Non-Goals

Goal: protect the audit trail. Non-goals: buckets C (UI backlog) and D (removals), which stay in #1250.

## Decisions

Method-specific rule (`PUT`, `DELETE`) instead of adding the path to `ADMIN_WRITE_PATHS`, because recording history (`POST`) must stay open. Bruno probes use a non-existent id, so a regression cannot delete real data.

## Riesgos / Trade-offs

An integration that corrected history with a non-admin account now gets 403; none ships in this repository (the frontend does not write history; Bruno and Playwright use the admin account).

## Testing Strategy

`HistoryWriteAuthorizationIntegrationTest` (5 cases) written first and observed failing (employee PUT/DELETE returned 200).

## Regression Strategy

Full backend suite; `RbacIntegrationTest`; Bruno `rbac` (11/11) and `history` against this backend on PostgreSQL 17.

## Playwright Strategy

Not applicable: no UI change (no screen writes history).

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the commit.
