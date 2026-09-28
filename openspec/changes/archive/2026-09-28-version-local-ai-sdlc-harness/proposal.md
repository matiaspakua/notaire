# Version the local-AI SDLC harness

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only the change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1074 |
| Use Case | none — internal tooling/process change, no business behavior; documented exception (precedent: #973, #1027); closest area CU76 |
| Branch | `chore/1074_local_ai_sdlc_harness` |
| Gate 1 status | passed |

## Objetivo

`local-ai/` drives a local model (Codex CLI + Qwen via oMLX) through the full
CONSTITUTION.md workflow for one GitHub issue: triage, OpenSpec, TDD, implement,
docs, preflight, pipeline, PR, CI, review and merge. A deterministic gate runs after
each phase, and a Claude Code foreman does the Gate 4 review. It delivered #1069 end
to end (PR #1073), but it lives only in one working copy. This change versions it so
it can be reviewed, reproduced and improved through PRs.

## What Changes

- Add `local-ai/setup-omlx-codex.sh`, `stop-omlx.sh`, `README.md` (local model stack).
- Add `local-ai/sdlc/foreman.sh`: phase driver, gates, retries, harness-owned push,
  CI wait on the last non-`[skip ci]` commit, `fix` mode for Gate 4 notes, merge + Gate 5.
- Add worker prompts (`prompts/`, `WORKER.md`, `templates/`).
- Add worker git guardrails (`githooks/`): no push, no branch switch or history rewrite,
  scope and forbidden paths (including `.env`).
- Add `bin/edit.py` (worker edit tool) and `bin/ledger.py` (harness-owned OpenSpec ledger).
- Add `local-ai/sdlc/AI-SDLC.md`: process, phase→gate mapping, guardrails and lessons.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — internal development tooling with no user-facing or business-data behavior. Documented exception per CONSTITUTION.md, precedent #973 / #1027. | n/a | n/a |

## Capabilities

### New Capabilities

None — no product behavior changes. `skip_specs: true` is set in `.openspec.yaml`.

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
| `local-ai/` (dev tooling) | yes | New directory, versioned for the first time |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none — the harness symlinks the main checkout's `.env` and forbids workers from committing it
- Dependencies: none added to the build; the harness needs `codex`, `gh`, `python3` and oMLX on the developer machine (installed by `setup-omlx-codex.sh`)

### Architecture review

No change to the application architecture. The harness runs outside the Spring
Boot / Next.js runtime and uses the repo only through git, `gh` and existing scripts
(`preflight.sh`, `run_pipeline.sh`, `validate-sdlc-plan.sh`). No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` (new) | Process, phases, gates, guardrails, lessons learned |
| `local-ai/README.md` (new) | Local model stack setup |
| `CHANGELOG.md` | n/a — not user visible (internal dev tooling) |

## Out of Scope

- Application code, CI workflows, GitHub settings.
- Running the harness in CI: it needs a local GPU/MLX model.
