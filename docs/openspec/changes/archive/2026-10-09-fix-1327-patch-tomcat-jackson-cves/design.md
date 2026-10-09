# Design

## Context

Issue #1327 was filed from `trivy fs` on main (2026-10-08) with 19 HIGH/CRITICAL findings. After #1328 removed JasperReports, 13 remain, all in Tomcat and Jackson; Spring Boot 4.1.1 is the latest release and has no patch that ships the fixes.

## Goals / Non-Goals

Goal: zero HIGH/CRITICAL Trivy findings for `backend-api`. Non-goals: making Trivy block CI (#566/#680/#712, an Owner decision), frontend/npm findings.

## Decisions

Tomcat 11.0.26 (latest 11.0 patch, includes the 11.0.25 fixes) rather than 11.0.25. Jackson stays on the 2.21 and 3.1 lines Boot manages (2.21.7, 3.1.7) instead of jumping to 2.22/3.2, to keep the change a pure patch upgrade. The floor test reads each library's own version class so it keeps protecting after the overrides are removed when Boot catches up.

## Riesgos / Trade-offs

Patch releases only; full backend suite, Bruno and Playwright run on the patched jar. If a later Boot release manages a lower version than the override, the override still wins until removed.

## Testing Strategy

`PatchedDependencyFloorTest` (4 cases) committed first and observed failing: Tomcat 11.0.24.0, Jackson 2.21.5 and 3.1.5 below the floors.

## Regression Strategy

Full backend suite (`backend-api/verify.sh`), Bruno full collection against the patched jar on PostgreSQL 17, Playwright in CI.

## Playwright Strategy

No UI change; the full CI Playwright suite runs on the patched backend.

## Deployment Strategy

Backend only; no migration.

## Rollback Strategy

Revert the merge commit (restores Boot's managed, vulnerable versions).
