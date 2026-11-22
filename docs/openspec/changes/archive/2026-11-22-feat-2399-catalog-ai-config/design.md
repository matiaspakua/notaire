> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Notaire uses multiple AI coding agent harnesses (`claude-code`, `cursor`, `codex`, `junie`, `kiro`, `agy`). Currently, prompt steering, skill definitions, agent descriptions, and project rules are scattered across harness-specific directories (`.claude/`, `.cursor/`, `.junie/`, `.kiro/`, `.agents/`, `.codex/`). Issue #2393 establishes that AI configuration must be catalog-owned and materialized into harnesses on use. Issue #2399 delivers the `import` and `materialize` functionality for these layouts.

## Goals / Non-Goals

**Goals:**

- Implement `framework/ai-sdlc/aisdlc/materialize.py` with a `LAYOUTS` table mapping each harness to catalog kind layouts (dir units or file units).
- Represent catalog entry content in an immutable `files` field on the revision: `{relative path: utf-8 text}`.
- Provide `aisdlc catalog import` to read harness configurations as raw UTF-8 bytes and create/update revisions idempotently.
- Guard against secrets in imported files via `catalogs.guard_files(files)` matching `factory.SECRET_VALUES` and raising `KitError(code="literal_secret")`.
- Provide `aisdlc catalog materialize` to project catalog entries into harness layouts, writing a `.aisdlc-materialized.json` manifest with stable SHA-256 digests.
- Provide `--check` drift inspection that exits 1 on missing, modified, stale, or unmanifested files.
- Record audit evidence and emit telemetry counts on import and materialize without leaking sensitive content.
- Add harness stub plugins for `agy` and `kiro`.
- Guarantee byte-for-byte round-trip fidelity including CRLF newline handling.

**Non-Goals:**

- Harness layouts for prompts and hooks (stored in catalog revisions only; no harness layout yet).
- Markdown section slicing of `AGENTS.md` for codex.
- Deleting files during materialize (stale files are reported, never silently deleted).
- Performing git operations (branching, committing, pushing) from within materialize.

## Decisions

1. **Module architecture (`materialize.py`):**
   - Place layout definitions and materialization algorithms in `framework/ai-sdlc/aisdlc/materialize.py`.
   - `cli.py` subcommands act as lightweight CLI wrappers delegating execution to `materialize.py`.
   - `catalogs.py` owns revision storage, revision advancement, validation, and secret scanning (`guard_files`).

2. **Harness Layout Table:**
   - Define a static `LAYOUTS` dictionary in `materialize.py`:
     - `claude-code`: `skills` -> dir `.claude/skills/<id>/`, `agents` -> file `.claude/agents/<id>.md`, `rules` -> file `.claude/rules/<id>.md`
     - `cursor`: `rules` -> file `.cursor/rules/<id>.mdc`
     - `codex`: `skills` -> dir `.codex/skills/<id>/`
     - `junie`: `rules` -> file `.junie/guidelines/<id>.md`
     - `kiro`: `rules` -> file `.kiro/steering/<id>.md`
     - `agy`: `skills` -> dir `.agents/skills/<id>/`
   - Kinds `prompts` and `hooks` retain content in catalog revision `files` but have no harness layout.

3. **Strict Byte/UTF-8 Handling:**
   - Read files from disk as raw bytes and decode strictly as UTF-8 without newline normalization, ensuring CRLF or LF line endings are preserved exactly upon round-trip materialization.
   - Files failing UTF-8 decoding are skipped and reported.

4. **Secret Scanning and Guarding:**
   - Add `catalogs.guard_files(files)` which validates relative paths (rejecting `..` and absolute paths) and tests all text content against `factory.SECRET_VALUES`.
   - Matching secrets trigger a `KitError(code="literal_secret")` identifying the file path but strictly concealing the secret value. Entries failing the guard are not stored, and the command exits with code 1.

5. **Manifest and Drift Detection:**
   - Materialization writes `.aisdlc-materialized.json` with schema version 1, harness name, and sorted entries with per-file SHA-256 digests.
   - When `--check` is passed, disk is inspected without modifications; missing files, modified bytes, stale files, or missing manifests are printed as drift and command exits with code 1.

6. **Audit Evidence and Telemetry:**
   - Import logs evidence record `catalog.import` and emits telemetry `aisdlc.catalog.import`.
   - Non-check materialization logs evidence record `catalog.materialize` and emits telemetry `aisdlc.catalog.materialize`.
   - Telemetry payloads carry only counts (entries, files) and digests; no file text, prompt text, or absolute paths.

7. **Harness Plugins for agy and kiro:**
   - Follow the pattern of `plugins/junie` and `plugins/openai-codex` (`plugin.toml` + `adapter.yml`).
   - Register them in `.aisdlc/aisdlc.toml` as stub harness plugins with `sandbox = false` and `capabilities = ["describe"]`.

## Riesgos / Trade-offs

- [Risk: Accidental secret leakage into catalog repositories] → Mitigation: `catalogs.guard_files` enforces strict regex scanning against `factory.SECRET_VALUES` both during import and during `catalogs.validate`.
- [Risk: Platform-dependent line-ending corruption (CRLF vs LF)] → Mitigation: File contents are decoded strictly without newline translation and written back byte-for-byte; verified with CRLF round-trip tests.
- [Risk: Unintended file deletion during materialization] → Mitigation: Materialization is additive only. Previously materialized files no longer in the catalog are reported as stale in the manifest/check, never deleted.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| import stores files in a revision | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| import is idempotent | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| import rejects a literal secret | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| materialize writes layout plus manifest | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| check reports drift and exits 1 | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| materialize is evidence and telemetry | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| round trip byte identical | integration | `framework/ai-sdlc/tests/test_materialize.py` |
| agy and kiro stubs listed by harness list | integration | `framework/ai-sdlc/tests/test_extra_harness_stubs.py` |

- New unit tests: `framework/ai-sdlc/tests/test_materialize.py` (covering layout mappings, guard_files, manifest generation, and diffing).
- New integration tests: `framework/ai-sdlc/tests/test_materialize.py` (covering full import/materialize CLI cycles, drift check, round-trip byte preservation, and telemetry).
- Coverage impact: Kit test coverage expanded in `framework/ai-sdlc/tests/`.

## Regression Strategy

- Existing catalog management tests (`framework/ai-sdlc/tests/test_catalogs.py`) must continue to pass unchanged.
- Existing harness stub tests (`framework/ai-sdlc/tests/test_extra_harness_stubs.py`) are updated to include `agy` and `kiro`.
- Full suite command: `python3 -m unittest discover -s framework/ai-sdlc/tests`

## Playwright Strategy

n/a — no UI surface: CLI and framework changes only (CU76 backend/kit).

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: Independent Python stdlib framework module.
- Configuration or `.env` keys to add: Register `agy` and `kiro` in `.aisdlc/aisdlc.toml` plugins.
- Feature flag: no
- Smoke test after deploy (Gate 5): `aisdlc harness list` and `aisdlc catalog --help`

## Rollback Strategy

- Revert safe: yes, purely additive CLI and plugin changes.
- Database rollback: none needed
- Data written under the new behavior after revert: Catalog revision files and manifests remain harmless JSON.
- Blast radius if rollback is delayed: none

## Migration Plan

1. Define `agy` and `kiro` harness plugins and update `.aisdlc/aisdlc.toml`.
2. Add secret guard `catalogs.guard_files` and `files` revision validation to `catalogs.py`.
3. Implement `materialize.py` with layout resolution, import, materialization, and drift detection.
4. Wire CLI commands `import` and `materialize` into `cli.py`.
5. Run test suite to verify idempotency, secret rejection, drift checks, and round-trip byte parity.

## Open Questions

None — Foreman design decided and unambiguous.
