> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`workspace/sdlc/run_pipeline.sh` composes the existing plan validation,
security, stack, preflight and SonarQube commands into one dashboarded local
gate. The initial #1862 implementation added Sonar options but left ambiguous
outcomes: infra mode could skip the requested quality gate, fast mode still
started Docker, and contradictory flags were accepted.

## Goals / Non-Goals

**Goals:**

- Make every documented CLI mode deterministic and reflected in the dashboard.
- Keep the default full pre-PR run comprehensive and its optional Sonar behavior
  backward-compatible.
- Preserve composition of existing quality checks rather than duplicating them.

**Non-Goals:**

- Change GitHub Actions, release automation, image publishing, or the SonarQube
  analysis implementation.
- Make SonarQube mandatory for the default mode; that remains opt-in through
  `--with-infra` or `--require-sonar`.

## Decisions

- `--with-infra` sets the same internal requirement as `--require-sonar`.
  This matches the request to boot SonarQube; allowing a skip would create false
  readiness evidence. The rejected alternative was a warning after an
  infrastructure-start failure because a requested quality gate must block.
- `--fast` skips application Docker startup and DAST. Preflight owns its own
  `--fast` behavior, so the orchestration layer passes that flag and avoids work
  that cannot be fast. The rejected alternative was retaining the stack for
  convenience, which contradicts the command’s documented contract.
- Invalid Sonar option combinations return status 2 during argument parsing.
  The rejected alternative was precedence rules; those hide operator mistakes.
- Regression checks inspect the script’s executable contract. This catches
  future loss of required guards without mocking heavyweight Docker services.

## Riesgos / Trade-offs

- [SonarQube boot can take several minutes] → `--fast` and default mode do not
  boot infra; requested infra remains deliberately blocking.
- [A static invariant test does not prove external tools] → run the real
  `run_pipeline.sh` command and retain its dashboard/log as Gate 3 evidence.
- [Local Docker/tool state may reveal unrelated failures] → record the exact
  gate and fix only failures caused by this change; no bypass is used.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Infrastructure mode cannot silently skip SonarQube | script invariant | `workspace/tests/test_run_pipeline_invariants.py` |
| Required SonarQube is unavailable | script invariant | `workspace/tests/test_run_pipeline_invariants.py` |
| Fast mode skips Docker and DAST | script invariant | `workspace/tests/test_run_pipeline_invariants.py` |
| Conflicting SonarQube flags | script invariant | `workspace/tests/test_run_pipeline_invariants.py` |

- New unit tests: four Python `unittest` invariants for shell mode guards.
- New integration tests: run the dashboarded script in the local environment;
  its phase logs are the integration evidence.
- Coverage impact: no Java production code; the JaCoCo floor is unaffected.

## Regression Strategy

- Existing tests affected: `workspace/tests/test_run_pipeline_invariants.py`;
  preserve composition, dashboard and failure-exit invariants.
- Full suite command: `bash workspace/sdlc/run_pipeline.sh`.
- HTTP/Bruno API suite: executed by full preflight.
- Legacy paths at risk: none; backend, frontend and frontend-swing behavior is
  untouched.

## Playwright Strategy

n/a — no UI surface changes. The full pipeline still invokes existing Playwright
regression through its full preflight phase.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: merge after Gate 4; no runtime rollout is needed
  because this is a developer-workstation tool.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): run `bash workspace/sdlc/run_pipeline.sh
  --fast` on a fresh checkout and confirm Docker/DAST are skipped.

## Rollback Strategy

- Revert safe: yes; only local script control flow and documentation change.
- Database rollback: none needed.
- Data written under the new behavior after revert: none.
- Blast radius if rollback is delayed: `--with-infra` fails correctly rather
  than issuing false readiness evidence.

## Migration Plan

1. Add failing invariant tests for changed CLI contracts.
2. Implement minimal argument validation and phase branching.
3. Update the permanent CI/CD plan to distinguish implemented behavior from the
   remote roadmap.
4. Run plan validation, preflight and the dashboarded pipeline; publish evidence.

## Open Questions

None.
