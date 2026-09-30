# Land the #1049 follow-up fixes missed by the #1103 squash

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1105 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1105_followup_fixes` |
| Gate 1 status | passed |

## Objetivo

Two fixes found while driving #1049 to its PR were committed after #1103 was
squash-merged (the fork-point diff fix did land in it):

- The worker ran `git commit --amend --no-edit` three times without staging.
- About 2.6% of gpt-oss responses addressed a tool and yielded no parsable call;
  Codex ended the turn with nothing done.

## What Changes

- The uncommitted-changes gate names the exact `git add ... && git commit --amend --no-edit`.
- `harmony_repair.recovery_call`: a lost call becomes an `echo` telling the model
  to resend it; `patch_omlx.py` adds `recover-lost-call` and rebuilds `harmony.py`
  from `harmony.py.orig`, so a changed patch set upgrades cleanly.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A lost tool call never ends a worker turn silently | local-ai/README.md | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `local-ai-worker-setup`: lost-call recovery.
- `ai-sdlc-enforcement`: actionable uncommitted-changes message.

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
| `local-ai/README.md` | lost-call recovery |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Other failure modes; they get their own issues.
