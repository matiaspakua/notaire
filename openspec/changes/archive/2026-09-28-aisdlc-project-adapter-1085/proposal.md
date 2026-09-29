# Move project-specific harness values into an adapter

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1085 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1085_aisdlc_project_adapter` |
| Gate 1 status | passed |

## Objetivo

`local-ai/sdlc/foreman.sh` hardcodes Notaire's module names, test and gate
commands, test-file patterns, OpenSpec schema, Compose project name and
forbidden paths. Another repo cannot reuse the harness without editing the
orchestrator. `local-ai/AUDIT.md` §4.2 asks for one project file, the
*adapter*, as the only project-specific harness input. This change is that
first step.

It also closes a gap found while running #1063: the worker wrote
`TEST_CMD=mvn … -Dtest=X` without `-pl backend-api`. At the repo root that
command errors in another module, so the red gate saw a broken command, not
failing tests. The adapter knows the right form, so the gate can reject a
wrong one with a precise message.

## What Changes

- New `.aisdlc/project.yml`: paths, Codex profile, spec schema and checks,
  surfaces (`root`, `test_one`, `suite`), source and test-file patterns, the
  migrations folder, gate commands (docs lint, preflight, pipeline, start,
  health URL, main workflows), Compose project name, forbidden-path pattern.
- New `local-ai/sdlc/bin/adapter.py`: `get <key> [name=value…]`,
  `surfaces` (paths on stdin → surface list), `suite <surfaces>` and
  `check-test-cmd <surfaces> <cmd>`.
- `foreman.sh` reads every value above through the adapter. `WT`, `RUNS`,
  `PROFILE` env overrides keep working.
- The red gate calls `check-test-cmd` before the red run.
- `SURFACE` in `triage.env` becomes a comma list of adapter surface names
  (`backend`, `frontend`, `backend,frontend`, `none`). The old value `both`
  still means every surface.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The harness reads project-specific values only from the adapter | AUDIT §4.2 | New |
| TEST_CMD must use the surface's single-test command | `prompts/04-tests.md`, WORKER.md | Made explicit (now enforced) |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: adds the adapter and the TEST_CMD form check.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — (the harness self-tests already run in `sdlc-process.yml`) |
| `local-ai/sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: new `.aisdlc/project.yml` (no secrets)
- Dependencies: PyYAML, already installed with python3 on the harness host

### Architecture review

Follows AUDIT §4.1 layers L4 (gates) and L5 (guards): project values leave the
orchestrator. No ADR: no product architecture changes.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | Adapter section; `SURFACE` format |
| `local-ai/AUDIT.md` | §7: adapter step 1 done; prompts and `foreman.sh` split still open |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Splitting `foreman.sh` into modules (§4.4), project-specific text inside the
phase prompts, per-phase model routing through the adapter (H2), and the
Java-only `new test Class#method` triage format.
