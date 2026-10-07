# Harness fixes from the #1049 runs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1098 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1098_harness_fixes_1049_runs` |
| Gate 1 status | passed |

## Objetivo

The #1049 runs lost every spec attempt to failures the harness can repair itself,
and part of the fixes found on the way was never committed:

- Qwen3-Coder set `skip_specs: true` but left a delta-less `specs/` folder: 3 attempts lost.
- gpt-oss ticked tasks 4.1/4.2 in the spec phase, before any implementation.
- gpt-oss's last attempt failed only on two lint errors: a bare opening fence
  (MD040) and a table row without a trailing pipe (MD055).
- The crawl guard shims exist but `run_worker` never sets `ZDOTDIR`, so they are inactive.

## What Changes

- `gate_spec` removes a leftover `specs/` when `skip_specs: true` and KIND is not code.
- `ledger.py untick-after`: `gate_spec` unticks every task outside groups 1-2.
- New `bin/md_repair.py`, run by `md_fix`: MD040 opening fences get `text`,
  MD055 table rows get their trailing pipe.
- `run_worker` sets `ZDOTDIR=sdlc/zdot`: the worker's `grep -r`/`find` skip dependency trees.
- Pending #1049 work: Qwen3-Coder-30B-A3B worker setup and local Codex prompt,
  `git grep` search guidance, removal proofs, SLUG of 2-6 words, retry feedback
  first and last, `AGENTS.md` kept out of the worker prompt.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Repairs the harness can make mechanically never cost a worker attempt | AI-SDLC.md | Made explicit |
| In the spec phase only groups 1-2 may be ticked | AI-SDLC.md (spec) | New |
| The worker's recursive searches skip dependency and build trees | AI-SDLC.md | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: spec gate repairs, markdown repair, worker search guard.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai/sdlc` harness | yes | see What Changes |
| `local-ai` setup | yes | Qwen3-Coder-30B-A3B worker setup |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

AUDIT §4.1 layer L4 (gates). No ADR: the product architecture does not change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | spec repairs, markdown repair, crawl guard; model name |
| `local-ai/README.md` | Qwen3-Coder-30B-A3B setup and Metal memory limit |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

The gpt-oss-20b model preset and the oMLX patches (#1099). Content errors that
need judgement (a wrong file name, an invented Use Case title) stay the
worker's to fix.
