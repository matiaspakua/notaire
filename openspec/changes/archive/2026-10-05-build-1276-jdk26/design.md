> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1276, Use Case CU76. `main` fails `test_backend_dockerfile_bases_are_pinned` after Dependabot #1274; the toolchain mixes 21 (POM, CI), 24 (runtime) and 26 (builder).

## Goals / Non-Goals

**Goals:** JDK 26 everywhere, pinned bases, a guard against drift.
**Non-Goals:** frontend or Node changes; moving to an LTS release.

## Decisions

1. Pin `maven:3.10.0-eclipse-temurin-26-alpine`, the image the floating `3-…` tag resolves to today, and `eclipse-temurin:26.0.2.1_1-jre-alpine`. Rejected: the floating tag (fails #1045).
2. Set `java.version` to 26 so the build and the runtime match. Rejected: keeping 21, which leaves the code compiled for a different JDK than the one that builds and runs it.
3. One guard script lists the files that carry a version, so a new workflow with another JDK fails.

## Riesgos / Trade-offs

- JDK 26 is not LTS, and a library may lack support; the full build is the check.
- Dependabot may bump the pinned tags again; the pins are minor-level so it opens PRs that keep the guard green.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Build and CI use JDK 26 | unit (repo guard) | `scripts/test_jdk26_toolchain.py` |
| Docker bases are pinned JDK 26 | unit (repo guard) | `scripts/test_jdk26_toolchain.py`, `scripts/test_image_pins_and_dependabot.py` |
| The backend builds and runs on JDK 26 | integration | `mvn verify` on JDK 26, Docker build and smoke test |

- New repo guard: `scripts/test_jdk26_toolchain.py` with wrapper `scripts/tests/test_jdk26_toolchain.py`
- Coverage impact: neutral

## Regression Strategy

- Existing tests affected: none; the pin guard must pass
- Full suite command: `mvn verify -pl backend-api; python3 -m unittest discover -s scripts/tests; bash scripts/run_pipeline.sh`
- HTTP/Bruno API suite: unchanged, must stay green

## Playwright Strategy

No UI change. The suite runs against the JDK 26 image as regression evidence.

## Deployment Strategy

- Flyway migration required: no
- Configuration or `.env` keys: none
- Smoke test after deploy (Gate 5): CD green; `/actuator/health` UP on the new image

## Rollback Strategy

- Revert the PR; the Dockerfiles, POM and workflows return together.
