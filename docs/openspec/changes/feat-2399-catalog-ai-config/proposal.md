# AI configuration owned by catalogs and materialized into harnesses

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2399 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2399_catalog_ai_config` |
| Gate 1 status | passed |

## Objetivo

Centralize AI configuration (skills, agents, rules, prompts, hooks) inside versioned catalog revisions and materialize them deterministically into harness layouts (`claude-code`, `cursor`, `codex`, `junie`, `kiro`, `agy`). This eliminates disparate, unversioned, adrift agent configurations across developer workstations and CI runners while enforcing strict secret scanning, audit evidence, and telemetry.

## What Changes

- Introduce `framework/ai-sdlc/aisdlc/materialize.py` with `LAYOUTS` defining directory-unit and file-unit mapping per harness (`claude-code`, `cursor`, `codex`, `junie`, `kiro`, `agy`).
- Extend catalog revisions with a `files` dictionary `{relative_path: utf-8_text}` and add `catalogs.guard_files(files)` to block literal secrets matching `factory.SECRET_VALUES`.
- Add `aisdlc catalog import --harness <h> --from <dir> [--kind K] [--json]` to read harness layouts strictly as UTF-8 bytes and store or update catalog revisions idempotently.
- Add `aisdlc catalog materialize --harness <h> --dest <dir> [--kind K] [--check] [--json]` to project catalog entries to disk with a `.aisdlc-materialized.json` SHA-256 manifest. Support `--check` for drift detection exiting 1 on differences.
- Emit evidence (`catalog.import` / `catalog.materialize`) and telemetry records (counts only) on import and non-check materialize.
- Add harness stub plugins for `agy` and `kiro` under `framework/ai-sdlc/plugins/agy` and `framework/ai-sdlc/plugins/kiro`, registering them in `.aisdlc/aisdlc.toml`.
- Expose `import` and `materialize` subcommands in `framework/ai-sdlc/aisdlc/cli.py`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| AI configuration assets (skills, agents, rules, prompts, hooks) must be version-controlled in catalog revisions | CU76 | New |
| Catalog import must read files as UTF-8 bytes and preserve CRLF/LF newlines without alteration | CU76 | New |
| Catalog import must be idempotent and reject any files containing literal secrets | CU76 | New |
| Catalog materialize must write a stable SHA-256 manifest and report drift via `--check` exiting with code 1 | CU76 | New |
| Every import and materialize must record audit evidence and emit telemetry without leaking prompt or secret content | CU76 | New |

## Capabilities

### New Capabilities

(none — this extends an existing capability)

### Modified Capabilities

- `catalogs`: Skills, agents, rules, prompts and hooks keep content in catalog revisions; import and materialize CLI subcommands synchronize catalog-owned AI configuration with harness layouts with digest manifest, drift checking, evidence, and telemetry.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | yes | Framework AI-SDLC kit adds `materialize.py`, updates `catalogs.py`, `cli.py`, plugin stubs and tests |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: Catalog revision gains `files` mapping `{relpath: text}`. Manifest format `.aisdlc-materialized.json`.
- Endpoints: CLI subcommands `aisdlc catalog import` and `aisdlc catalog materialize`.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: Plugin registrations for `agy` and `kiro` in `.aisdlc/aisdlc.toml`.
- Dependencies: Python stdlib only.

### Write set

- `framework/ai-sdlc/aisdlc/materialize.py`
- `framework/ai-sdlc/aisdlc/catalogs.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/plugins/agy/**`
- `framework/ai-sdlc/plugins/kiro/**`
- `.aisdlc/aisdlc.toml`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `framework/ai-sdlc/docs/**`
- `docs/openspec/changes/feat-2399-catalog-ai-config/**`
- `docs/openspec/changes/archive/**feat-2399*/**`
- `docs/openspec/specs/**`

### Architecture review

Follows existing framework architecture: kit remains Python stdlib only, domain logic isolated in `materialize.py` and `catalogs.py`, CLI commands in `cli.py` remain thin adapters, immutable catalog revisions are preserved, and every materialization/import links to the hash-chained ledger and telemetry port.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document `aisdlc catalog import` and `aisdlc catalog materialize` commands and options |
| `framework/ai-sdlc/CHANGELOG.md` | Add `[Unreleased]` entry for catalog-owned AI configuration (#2399) |
| `docs/openspec/specs/catalogs/spec.md` | Update main capability spec upon archiving at Gate 5 |

## Out of Scope

- Prompts and hooks harness layouts (stored in catalog revisions only; no harness layout yet).
- AGENTS.md section slicing for codex.
- Automatic remote syncing or git commits during materialize.
- Web UI / dashboard visualizer for materialized files (handled in separate issue #2393 child).
