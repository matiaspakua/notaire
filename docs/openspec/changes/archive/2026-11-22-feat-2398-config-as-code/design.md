> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`aisdlc serve` currently exposes GET paths for config, plugins, harness, report,
catalogs, workflows, projects, and runs. Mutating methods answer 405 unless `--write`
is set; then PUT catalogs requires write scope, CSRF, and If-Match. There is no endpoint
to browse the factory's config file tree or read individual config files with redaction.
Catalog entries can be removed via CLI (`catalogs.remove`) but not via HTTP, and no
evidence record is appended on removal.

## Goals / Non-Goals

**Goals:**
- Expose the factory's config file tree via `GET /factory/config/files`
- Read individual config files with secret redaction via `GET /factory/config/files/{path}`
- Delete catalog entries via HTTP with optimistic concurrency (If-Match), CSRF, and evidence
- CLI parity: `aisdlc config files`, `aisdlc config show <path>`, `aisdlc catalog remove --if-match`

**Non-Goals:**
- Raw media type responses (Accept header negotiation)
- Dashboard UI surfaces (epic #2393 part G)
- Catalog import/materialize (epic #2393 part F)

## Decisions

1. **New module `config_files.py`** — keeps config file logic separate from `factory.py`
   (which handles config_view) and `serve.py` (which handles HTTP dispatch). Follows the
   pattern of `catalogs.py`, `workflows.py`, etc.

2. **Config roots derived from profile** — the profile file itself, `[catalogs]` dir,
   workflows (catalog kind "workflows"), projects dir under `.aisdlc`, policy file
   (`[project] policy`), `[context]` files, and `[skills]` dir. This matches the profile
   dataclass fields (`catalogs_dir`, `skills_dir`, `context_files`, `policy`, etc.).

3. **Path confinement reuses `serve._guard_path`** — the existing guard already blocks
   `..`, encoded separators (`%2f`, `%5c`), and leading dots. Config file paths add
   symlink escape detection (resolve and check containment in repo root).

4. **Redaction uses `factory.redact`** — existing function that checks `SECRET_KEYS` on
   keys and `SECRET_VALUES` on values. For file text, we apply the same patterns to
   key=value lines and standalone secret-looking tokens.

5. **`catalogs.delete()` vs `catalogs.remove()`** — `delete()` is the library function
   called by HTTP (raises KitError on not_found, checks version, appends evidence).
   `remove()` remains the CLI function (returns exit code, prints messages) and gains
   an optional `--if-match` flag for parity.

## Riesgos / Trade-offs

- [Risk] Exposing config files could leak secrets if redaction is incomplete → Mitigation:
  `factory.redact` covers known patterns (ghp_, github_pat_, sk-, AKIA, Bearer); file text
  redaction applies the same regex to all text content.
- [Risk] Path traversal via symlinks → Mitigation: resolve symlinks and check containment
  in repo root before reading.
- [Risk] Catalog delete without version check could cause concurrent edits → Mitigation:
  If-Match is mandatory (428 without it, 409 on mismatch).

## Testing Strategy

| Scenario (spec) | Test level | Test file |
|-----------------|------------|-----------|
| Config file tree listing | unit | `test_config_files.py` |
| Config file read with redaction | unit | `test_config_files.py` |
| Path traversal refusal (..) | unit | `test_config_files.py` |
| Absolute path refusal | unit | `test_config_files.py` |
| Encoded separator refusal (%2f, %5c) | unit | `test_config_files.py` |
| Symlink escape refusal | unit | `test_config_files.py` |
| Delete with matching If-Match | unit | `test_config_files.py` |
| Delete with stale If-Match (409) | unit | `test_config_files.py` |
| Delete without If-Match (428) | unit | `test_config_files.py` |
| Delete requires write scope | unit | `test_config_files.py` |
| Delete requires CSRF | unit | `test_config_files.py` |
| Delete appends evidence record | unit | `test_config_files.py` |
| CLI config files command | unit | `test_config_files.py` |
| CLI config show command | unit | `test_config_files.py` |

- New unit tests: `framework/ai-sdlc/tests/test_config_files.py`
- Coverage impact: new module and functions; existing `test_serve.py`, `test_catalogs.py`
  may need minor updates for new dispatch paths.

## Regression Strategy

- Existing tests affected: `test_serve.py` (new GET paths in dispatch), `test_catalogs.py`
  (new delete function), `test_serve_writes.py` (new DELETE method handling).
- Full suite command: `cd framework/ai-sdlc && python3 -m pytest tests/ -v`
- HTTP/Bruno API suite: n/a — this is framework/ai-sdlc, not backend-api
- Legacy paths at risk: none — new code only, no behavior changes to existing endpoints.

## Playwright Strategy

n/a — no UI surface. This change adds HTTP endpoints and CLI commands only.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — stdlib Python only, no deployment step
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): `python3 framework/ai-sdlc/bin/aisdlc --profile .aisdlc/aisdlc.toml serve` then curl the new endpoints

## Rollback Strategy

- Revert safe: yes — additive endpoints only, no data migration
- Database rollback: none needed
- Data written under the new behavior after revert: evidence records from catalog deletes
  (harmless — they document what was deleted)
- Blast radius if rollback is delayed: none — endpoints are additive

## Migration Plan

No migration needed — purely additive changes.

## Open Questions

None.
