> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules.

## Context

`python3 -m unittest discover -s workspace/tests` and `-s security/tests` fail on
`main` @ b43e2eb6:

- `test_jdk26_toolchain.test_docker_bases_are_jdk_26_and_pinned`:
  `'temurin-26' not found in '3-eclipse-temurin-27-alpine'`
- `test_image_pins_and_dependabot.test_backend_dockerfile_bases_are_pinned`:
  `base must be minor-or-digest pinned (#1045); got maven:3-eclipse-temurin-27-alpine`

`sdlc-process.yml` runs only on `pull_request`, so `main` shows green. PR
Validation's title regex accepts `feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert`.
Dependabot titles are `deps(deps): …`, so they always fail.

## Goals / Non-Goals

**Goals:**

- Restore the exact pre-#1615/#1616 bases.
- Make a Dependabot PR's red mean a real problem.
- Stop Dependabot from proposing JDK majors.

**Non-Goals:**

- Exempting bots from PR Validation (a bypass hides real failures).
- Changing the allowed type list.
- Ruleset or workflow-trigger changes (#1040, #1622).

## Decisions

1. **`build` prefix, not a bot exemption.** Conventional Commits defines `build`
   for dependency and build-system changes. With `include: scope`, Dependabot
   writes `build(deps): bump …`, which matches PATTERN2. An author exemption in
   PR Validation was rejected: it would also skip the checks that caught the JDK
   drift.
2. **Ignore semver-major for `eclipse-temurin` and `maven` only.** Minor and
   patch JDK images still arrive as PRs. Node majors (frontend) are out of scope.
   No incident there, and Frontend CI covers them.
3. **Test the contract against both `TYPES` lists.** Requiring the prefix to sit
   in the intersection keeps `pr-validation.yml` and `check-commit-messages.sh`
   honest together.

## Riesgos / Trade-offs

- [Dependabot keeps old `deps(...)` PR titles open] → none open today. New PRs
  pick up the config.
- [`build` commits are hidden in release-please changelogs] → intended.
  Dependency bumps are not product changes.

## Testing Strategy

Stdlib `unittest` guards in `security/tests/test_image_pins_and_dependabot.py`:
`test_dependabot_commit_prefixes_are_allowed_conventional_types` and
`test_backend_docker_ignores_jdk_major_bumps`. The existing
`test_backend_dockerfile_bases_are_pinned` and
`test_docker_bases_are_jdk_26_and_pinned` cover the Dockerfile revert. All four
were observed failing before the change.

## Regression Strategy

Run all four process self-test suites (`local-ai/sdlc/tests`, `workspace/tests`,
`contracts/tests`, `security/tests`). CI's backend Docker build on the PR
confirms the image still builds on JDK 26.

## Playwright Strategy

n/a — no UI surface. The PR's Playwright run still confirms the backend image
boots.

## Deployment Strategy

Merge → CD rebuilds the backend image on JDK 26 bases.

## Rollback Strategy

Revert the PR. Config- and Dockerfile-only, no data or schema.

## Migration Plan

None.

## Open Questions

None.
