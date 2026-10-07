# gpt-oss-20b preset for setup-omlx-codex.sh

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1099 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1099_gpt_oss_preset` |
| Gate 1 status | passed |

## Objetivo

gpt-oss-20b (`mlx-community/gpt-oss-20b-MXFP4-Q8`, 12.1 GB) fits the 24 GB M5 Pro
with a 64K window and cleared the #1049 spec plan checks Qwen3-Coder failed 6
times. Running it needed fixes that exist on one machine only:

- oMLX 0.7.0 drops a gpt-oss tool call that opens the completion: its harmony
  parser reads the `<|start|>assistant` header twice.
- gpt-oss writes calls for Codex's classic tools (argv `command`, `apply_patch`),
  which Codex 0.159 no longer offers: `{"cmd": [...]}`, a stray `]` in the JSON,
  header tokens in the tool name. 2 of 8 coding runs ended on such a call.
- The `omlx-gptoss` Codex profile, catalog and sampling were written by hand.

## What Changes

- `setup-omlx-codex.sh` takes `PRESET=qwen3-coder|gpt-oss` (default `qwen3-coder`).
  A preset fixes repo, context window, sampling, reasoning, Codex profile name,
  catalog file and instructions; `PRESET=gpt-oss` never touches the `omlx` profile.
- New `local-ai/omlx/`: `harmony_repair.py` (repairs a gpt-oss tool call) and
  `patch_omlx.py`, which the setup script runs to patch the installed oMLX
  idempotently (backup, anchor check, version note).
- `local-ai/codex-local-instructions-gpt-oss.md`: the tool-call shape, appended
  to the local prompt for the gpt-oss preset only.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every local model setting is reproducible from the repo | local-ai/README.md | New |
| A patch to third-party code is versioned, idempotent and reversible | local-ai/README.md | New |

## Capabilities

### New Capabilities

- `local-ai-worker-setup`: model presets and the oMLX tool-call repair.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai` setup | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (gpt-oss weights are downloaded by the setup script, not committed)

### Architecture review

Developer tooling only; no product architecture change, no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/README.md` | presets, oMLX patches, gpt-oss profile |
| `local-ai/sdlc/AI-SDLC.md` | per-phase model row names the gpt-oss preset |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Reporting the harmony parser bug upstream (jundot/omlx) — needs the Owner's approval
to post. Changing the harness default model.
