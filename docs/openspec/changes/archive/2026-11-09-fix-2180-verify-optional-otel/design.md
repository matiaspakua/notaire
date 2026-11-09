> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules.

## Context

Issue #2180, Use Case CU76. Reproduced on Cursor Cloud without `python3-venv` / `ensurepip` during post-#2177 aisdlc re-validation.

## Goals / Non-Goals

**Goals:** `bash framework/ai-sdlc/verify.sh` exits 0 on hosts that cannot build the optional OTel venv; stdlib suite still runs; broken half-venvs are removed.
**Non-Goals:** Making OTel a hard dependency; changing TelemetryPort runtime; claiming OS isolation.

## Decisions

1. Keep bootstrap inside `verify.sh` (KISS — no new binary). On venv/pip failure: WARN to stderr, `rm -rf` the dest, continue with empty `TEST_ENV`.
2. Treat missing `pip` inside an existing `.venv-telemetry` as broken (leftover from failed ensurepip) and rebuild-or-degrade rather than assuming SITE is usable.
3. `AISDLC_TEST_FORCE_VENV_FAIL=1` forces the failure path for a unit test without needing a host without ensurepip.

## Riesgos / Trade-offs

- Cloud CI that previously relied on silent hard-fail to notice missing venv will only see WARN + 3 skipped tests — acceptable; optional means optional. Env can still install `python3-venv` for full SDK-path coverage.

## Testing Strategy

| Scenario | Test level | Test class / file |
|----------|------------|-------------------|
| Forced venv failure → verify continues, no leftover broken venv | unit | `framework/ai-sdlc/tests/test_verify_optional_venv.py` |

- New Java/frontend tests: n/a
- Coverage impact: kit Python only

## Regression Strategy

- Full module: `bash framework/ai-sdlc/verify.sh` and/or `python3 -m unittest discover -s framework/ai-sdlc/tests`
- HTTP/Bruno / Playwright: n/a — no product UI/API surface

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

- Flyway: no
- Single PR; no feature flag
- Smoke: module verify green on the PR head

## Rollback Strategy

- Revert the PR.
