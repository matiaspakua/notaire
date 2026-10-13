> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#1629: `maven 3.10.0-eclipse-temurin-26-alpine → 3-eclipse-temurin-27-alpine`. Dependabot parses the tag prefix (`3.10.0` → `3`) and treats the suffix as a variant, so the bump is not "semver-major" and the #1621 rule never fired. `protect-main` sets `strict_required_status_checks_policy: true`.

## Goals / Non-Goals

**Goals:** no impossible bot PRs; fewer serial CI cycles.
**Non-Goals:** ruleset changes; grouping docker or Maven ecosystems (Maven already groups minor/patch).

## Decisions

1. **Ignore maven entirely.** A `versions:` pattern would depend on Dependabot's tag parsing, which is exactly what failed. The builder pin is bumped by hand together with the JDK.
2. **Group npm minor and patch, not majors.** Majors (e.g. motion 12→14) deserve their own review and CI signal.

## Riesgos / Trade-offs

- [The maven builder misses patch releases] → acceptable: the build stage only compiles. Runtime `eclipse-temurin` minor/patch PRs continue.
- [A grouped PR fails because of one package] → Dependabot can split it again (`@dependabot ignore` / recreate), and CI names the failing package.

## Testing Strategy

`security/tests/test_image_pins_and_dependabot.py`: the updated `test_backend_docker_ignores_jdk_major_bumps` and the new `test_frontend_npm_groups_minor_and_patch_updates` (2 failures before, OK after).

## Regression Strategy

Process self-test suites.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Dependabot reads the config on its next run.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
