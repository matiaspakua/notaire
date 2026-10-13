> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`ProductionCredentialsGuard` (a `@PostConstruct` bean) fails startup in production when a credential equals `admin`. For the actuator password that is too weak a test: `password`, `changeme`, an empty value or a short value all pass. `.env.example` ships `ACTUATOR_PASSWORD=admin`.

## Goals / Non-Goals

**Goals:** production never starts with a guessable actuator password or user.
**Non-Goals:** strength rules for other credentials; entropy estimation; secret rotation.

## Decisions

1. **Extend the existing guard.** One class already owns production credential checks; a second validator would split the rule.
2. **Length ≥ 16 plus a small deny-list and the `change-me` placeholder prefix.** Cheap, deterministic, testable. The prefix check refuses the `.env.example` placeholder, which is longer than 16 characters.
3. **Same deny-list for the actuator user**, plus non-empty.
4. **Keep the dev `admin` defaults.** Prometheus hardcodes `admin`/`admin` for the actuator scrape; changing `.env.example` would break dev metrics. The guard already stops those values reaching production.

## Riesgos / Trade-offs

- [An operator with a valid 12-character password cannot start] → intended; the error names the property and the minimum.
- [A deny-list is never complete] → length and the placeholder prefix cover the realistic copy-paste path.

## Testing Strategy

`ProductionCredentialsGuardTest`: one test per scenario of the delta spec (short, deny-listed, placeholder, empty user/password, strong accepted, development skipped). Written first and observed failing.

## Regression Strategy

Existing guard tests keep their assertions; their safe actuator password grows from 12 to 16+ characters because the new rule makes 12 characters invalid. Full `mvn verify -pl backend-api`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Operators setting `ENVIRONMENT=production` must supply a 16+ character `ACTUATOR_PASSWORD`; `docker-compose.prod.yml` already requires the variable.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
