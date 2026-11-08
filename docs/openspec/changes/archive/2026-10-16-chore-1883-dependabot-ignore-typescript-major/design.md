# Design — Dependabot typescript-major ignore

## C## Context

The frontend npm block groups minor/patch; majors arrive alone. TS 7 cannot be installed with openapi-typescript 7.13 and breaks the API consumers.

## Goals / Non-Goals

**Goals:** no failing typescript-major bot PRs. **Non-Goals:** TS7 migration.

## Decisions

- Ignore semver-major only, so 5.x patches keep flowing; same shape as the eclipse-temurin rule.

## Riesgos / Trade-offs

- A TS7-ready release goes unnoticed; the comment names the removal condition.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| typescript major is ignored in /frontend | unit | `test_frontend_npm_ignores_typescript_major` |

## Regression Strategy

All `security/tests/test_image_pins_and_dependabot.py` cases (grouping, prefixes, ecosystems).

laywright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway migration required: no
- Feature flag: no
- Effective on the next Dependabot run after merge.

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed

## Migration Plan

1. Merge. 2. Next Dependabot run opens no typescript major PR.

## Open Questions

None.
