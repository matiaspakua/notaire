> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`TelemetryDefaultOffTest.test_default_profile_does_not_emit_or_inject_traceparent` asserts
`os.environ.get("TRACEPARENT","") == ""` after a kit `cost` command. Cursor Cloud and
similar hosts export `TRACEPARENT` into the agent process; the assertion then fails even
when the kit never wrote the variable. The child subprocess also copied parent `TRACE*`
keys, which would fail the same way once the first assert is fixed.

## Goals / Non-Goals

**Goals:**
- `verify.sh` passes with `TRACEPARENT` set in the parent shell.
- The scenario still proves the kit does not inject TRACEPARENT when telemetry is off.

**Non-Goals:**
- Changing production telemetry code paths.
- Clearing TRACEPARENT for the whole verify process globally.

## Decisions

- Use `unittest.mock.patch.dict(os.environ, …, clear=False)` and remove `TRACEPARENT` /
  `OTEL_*` keys inside the test body so values restore after the scenario (matches #2264 AC).
- Build the child probe env from `_otel_env()` only — do not re-inject parent `TRACE*`.
- Prefer a test fix over documenting `env -u TRACEPARENT` forever (P12).

## Riesgos / Trade-offs

- [Risk] Over-clearing OTEL_* could hide a future regression that depends on parent OTEL flags → Mitigation: only this default-off scenario clears them; other telemetry tests keep their explicit env.
- [Risk] `patch.dict` misuse leaves env dirty → Mitigation: context manager restores; suite runs with TRACEPARENT still exported outside the test.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Parent TRACEPARENT does not fail default-off proof | unit | `test_telemetry_port.TelemetryDefaultOffTest.test_default_profile_does_not_emit_or_inject_traceparent` |

- New unit tests (`src/test/java/.../unit/`): n/a — kit Python unittest
- New integration tests (`src/test/java/.../integration/`): n/a
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): n/a (kit)

## Regression Strategy

- Existing tests affected: only the named telemetry default-off test; other `test_telemetry_port` cases unchanged.
- Full suite command: `TRACEPARENT=00-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa-bbbbbbbbbbbbbbbb-00 bash framework/ai-sdlc/verify.sh`
- HTTP/Bruno API suite: n/a — kit-only
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update under `testing/e2e/tests/`: n/a — no UI surface
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: n/a — no UI surface

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc doctor` + kit verify under TRACEPARENT

## Rollback Strategy

- Revert safe: yes — test-only revert
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: cloud verify continues to need `env -u TRACEPARENT`

## Migration Plan

None beyond merging the test fix.

## Open Questions

None.
