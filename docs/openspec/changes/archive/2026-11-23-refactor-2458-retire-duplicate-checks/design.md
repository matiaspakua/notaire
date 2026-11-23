> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

`aisdlc commits` already takes `[commits] pattern`; `aisdlc tdd` checks only that some test changed; `aisdlc budget` already measures `[context]` files. Notaire's scripts add the `design` type, docs/infra test paths, commit order and the `sdlc-exception` label. `workspace/tests/test_pr_checks.py` pins the scripts' behaviour.

## Goals / Non-Goals

**Goals:** Notaire's three checks run on the kit with unchanged verdicts; the kit gains order and waiver for any adopter.
**Non-Goals:** Reading PR labels from a forge inside the kit (the caller passes them).

## Decisions

`order` is opt-in so existing adopters keep today's verdicts. Labels arrive by `--labels` (KISS): preflight and CI already hold them in `PR_LABELS`. The characterization tests in `test_pr_checks.py` are ported to run `aisdlc` with Notaire's real profile, so the same inputs prove the same verdicts. `repo-metrics.py` reads the always-loaded list from the profile instead of a copy.

## Riesgos / Trade-offs

A verdict drift between script and kit: covered by the ported characterization tests. CI needs the kit at runtime: it is in the checkout and stdlib-only.

## Testing Strategy

Kit unit tests for order and waiver (red first); ported characterization tests in `workspace/tests/test_pr_checks.py` against Notaire's profile (red on `design:` and order); budget test via `aisdlc budget`. Then kit verify and `preflight.sh --full`.

## Regression Strategy

Dependabot prefix test reads the profile pattern; scripts layout test records the deletion; governance test runs `aisdlc budget`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
