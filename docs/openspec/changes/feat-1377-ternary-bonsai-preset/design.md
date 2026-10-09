> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1377, Use Case CU76. The tuning rationale for each model (sampling, repetition penalty, the TurboQuant KV finding) is recorded in comments of the old script; it moves with the values into the registry.

## Goals / Non-Goals

**Goals:** one registry; identical values for existing presets; a Bonsai preset.
**Non-Goals:** choosing or benchmarking Bonsai's quality; changing oMLX itself.

## Decisions

1. YAML registry read by a stdlib-only Python resolver. Rejected: keeping `case` presets and adding a third (the pattern that made every new model a shell edit).
2. The resolver prints shell assignments that the script `eval`s, so the script keeps its existing variables.
3. Tests that need a live oMLX server or the downloaded model skip with an explicit reason; they do not fail the suite.

## Riesgos / Trade-offs

- The Bonsai sampling values are the model vendor's defaults and are not yet tuned on this harness; the preset is unproven until a run is recorded.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Existing presets are unchanged | unit | `local-ai/sdlc/tests/test_models_config.py` |
| The Bonsai preset is available | unit | `local-ai/sdlc/tests/test_models_config.py`, `test_omlx_bonsai_integration.py` |
| Unknown models fail | unit | `local-ai/sdlc/tests/test_models_config.py` |

- Coverage impact: none (local tooling)

## Regression Strategy

- `python3 -m unittest discover -s local-ai/sdlc/tests`; `bash workspace/sdlc/preflight.sh`

## Playwright Strategy

No UI change.

## Deployment Strategy

- Flyway migration required: no; configuration keys: none
- Smoke test after deploy (Gate 5): not applicable (local tooling); the Bonsai run is recorded in the issue when the model is available

## Rollback Strategy

- Revert the PR; the previous script is in git history.
