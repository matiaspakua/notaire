# Fixes from the autonomous gpt-oss runs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1102 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1102_autonomous_run_fixes` |
| Gate 1 status | passed |

## Objetivo

The first autonomous #1049 run with gpt-oss-20b ended its spec phase without work:

- gpt-oss put an `exec_command` call on the analysis channel with an unterminated
  `cmd` string. oMLX keeps analysis-channel calls only when their JSON parses, so
  the call became reasoning text and Codex ended the turn.
- The phase still passed: `gate_spec` checked the previous spec, and the foreman's
  review note was silently ignored.

## What Changes

- `harmony_repair.py` closes an unterminated `cmd` string.
- `patch_omlx.py` adds a patch so oMLX's analysis-channel check parses the repaired arguments.
- `with_retries` fails an attempt whose worker changed nothing while a foreman
  review note for that phase is pending.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A foreman review note must be acted on, not passed over | AI-SDLC.md | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `local-ai-worker-setup`: unterminated-string repair, analysis-channel check.
- `ai-sdlc-enforcement`: review notes require a change.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai` setup and `sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Developer tooling only; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/README.md` | repaired shapes include an unterminated string on the analysis channel |
| `local-ai/sdlc/AI-SDLC.md` | review notes require a change |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Other failure modes the runs may still show; they get their own issues.
