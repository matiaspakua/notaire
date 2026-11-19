# fix: retry frontend Vitest once in preflight

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2046 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/preflight-vitest-retry-47dc` |
| Gate 1 status | passed |

## Objetivo

`preflight.sh --full` has failed on a single frontend Vitest timeout and passed when that file was run again. The gate now retries that one step once. A second failure still fails the run.

## What Changes

- Add `run_retry` next to `run`.
- Call it for `frontend vitest` only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| One Vitest flake does not fail the local pull-request gate | observed full-suite timeouts | New |
| A second failure still fails the gate | existing `run` | Made explicit |

## Capabilities

### New Capabilities

- None. No product HTTP change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `workspace/sdlc` | yes | preflight retry |
| `backend-api` | no | |
| `frontend` | no | |

### Surface area

- Product API: none

### Write set

- `workspace/sdlc/preflight.sh`
- `workspace/tests/test_preflight_vitest_retry.py`
- factory comparison and learning log

### Architecture review

Factory gate only. No product ADR. #2046 is the open quality mandate this loop is executing; this change does not raise a coverage floor.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Factory learning log | the retry is now in the script |
| Product docs | none |

## Out of Scope

- Retrying Playwright or pages Vitest.
- Changing Vitest's worker isolation.
