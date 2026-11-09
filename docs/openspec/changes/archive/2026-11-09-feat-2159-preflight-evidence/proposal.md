# feat: Notaire preflight feeds aisdlc evidence ledger

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2159 |
| Use Case | CU76 |
| Branch | `cursor/feat-2159-preflight-evidence-d324` |
| Gate 1 status | passed |

## Objetivo

Local `preflight.sh` already runs `aisdlc ratchet` and `aisdlc skills`, but those
direct CLI calls never go through `gate` → `record_stage`, so
`.aisdlc/evidence/ledger.jsonl` stays empty and report / HTML / improve / autonomy
have no signal.

## What Changes

- Profile declares a cheap custom stage `signal` that runs the same two checks
  (ratchet + skills).
- `preflight.sh` invokes `aisdlc gate signal --base origin/main` instead of calling
  ratchet/skills separately, so one stage record is written.
- Workspace tests lock the wiring. CI process checks keep calling ratchet/skills
  directly (no evidence write, no extra cost).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| skip ≠ pass; empty ledger is NO EVIDENCE | kit evidence axioms | Made explicit for Notaire preflight |
| Agent never commits | CONSTITUTION / factory | Unchanged |
| Evidence stays gitignored | #1958 | Unchanged |

## Capabilities

### New Capabilities

- `preflight-evidence`: Local full preflight records a `signal` stage via `aisdlc gate`.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | keeps direct ratchet/skills |
| `workspace` (harness) | yes | preflight + adoption test |
| `.aisdlc` | yes | `signal` stage gates |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `.aisdlc/aisdlc.toml`
- `workspace/sdlc/preflight.sh`
- `workspace/tests/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/**`

### Architecture review

Follows existing `gates.run_stage` → `evidence.record_stage`. No ADR. No portal.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | Note that local preflight records `signal` |
| `CHANGELOG.md` | n/a — Release Please #2150 holds Unreleased |

## Out of Scope

Portal UI; writing evidence from GitHub Actions; adding `signal` to `pre-push` /
`pre-pr` (would recurse or inflate CI); Wilson autonomy; closing epic #2134
(ASLI-31 after this merges).
