# Design

## Context

Assessment #1242 static scan; the issue asks for a guard with an allowlist for intentional API-only endpoints.

## Goals / Non-Goals

Goal: stop new unreachable endpoints and make the existing ones visible. Non-goal: triaging the 53 baseline endpoints (expose in UI or remove), which stays in #1250.

## Decisions

Method-aware matching (stricter than the issue's prefix heuristic, hence 53 instead of 10). Guard lives in `contracts/` because it checks the backend–frontend seam. Pure static scan, no backend or frontend build needed.

## Riesgos / Trade-offs

Heuristic: URLs assembled from several variables may be missed (add an allowlist entry and say so) or over-matched. Adding an endpoint now needs a UI call or an allowlist line in the same PR.

## Testing Strategy

Nine scanner unit tests on synthetic sources plus three repository tests, observed failing first (no allowlist).

## Regression Strategy

`contracts/tests/test_seams.py`, workspace and docs guards.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

None; CI-only guard.

## Rollback Strategy

Revert the commit.
