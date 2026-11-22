# feat(aisdlc): factory configuration as code and full catalog lifecycle

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2398 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2398_config_as_code` |
| Gate 1 status | pending |

## Objetivo

The factory's configuration files (profile, catalogs, workflows, rules, project files) are
currently only accessible through CLI commands and catalog endpoints. There is no unified
endpoint to browse the config tree, read individual files with secret redaction, or delete
catalog entries with optimistic concurrency control. This change adds `GET /factory/config/files`
(tree), `GET /factory/config/files/{path}` (file text with redaction), and
`DELETE /factory/catalogs/{kind}/{id}` (with If-Match, CSRF, evidence) to complete the
"Configuration as Code" contract (epic #2393, part E).

## What Changes

1. **New module `config_files.py`** — lists the factory's config roots (profile file,
   `[catalogs]` dir, workflows, projects dir under `.aisdlc`, policy file, `[context]` files,
   `[skills]` dir) as a tree of rows (kind, size, media type). Reads file text with path
   confinement (no traversal, no symlink escape, no absolute paths, no encoded separators).
   Redacts secrets using `factory.redact` / `SECRET_VALUES` patterns.

2. **New HTTP endpoints** — `GET /factory/config/files` (tree listing) and
   `GET /factory/config/files/{path}` (file content as JSON `{path, kind, media_type, size, content, redacted}`).
   Both require Bearer `read` scope. Dispatched from `serve._dispatch_get`.

3. **`DELETE /factory/catalogs/{kind}/{id}`** — new mutating path on `aisdlc serve` (requires
   `--write`). Authenticates write scope, requires `X-AISDLC-CSRF`, requires `If-Match` header
   (428 if missing, 409 if stale version). Calls new `catalogs.delete()` library function that
   checks version, removes the entry, and appends an evidence record.

4. **`catalogs.delete()` library function** — checks version against current, removes the entry
   via the same file operations as `catalogs.remove`, appends evidence record (kind
   `catalog.delete`, actor, kind, id, version, digest). Unknown entry returns `not_found` (404).

5. **CLI parity** — `aisdlc catalog remove` gains optional `--if-match` flag. New subcommands:
   `aisdlc config files [--json]` (tree) and `aisdlc config show <path>` (file text).

6. **OpenAPI** — `factory-control.openapi.yaml` updated with new paths, schemas
   (`ConfigFileTree`, `ConfigFileContent`, `CatalogDeleteResponse`), and error responses
   (409 version_conflict, 428 if_match_required).

7. **Tests** — unit tests for redaction, path traversal refusal (including `..`, absolute,
   encoded `%2f`/`%5c`, symlink escape), delete with matching If-Match, delete with stale
   If-Match (409), delete without If-Match (428), delete requiring write scope and CSRF,
   and delete evidence record presence.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Config file reads require Bearer `read` scope | CU76, #2393 | New |
| Config file paths are confined to factory config roots; traversal is refused | CU76, #2393 | New |
| Secrets in config files are redacted before any response | CU76, #2393 | New |
| Symlinks escaping the repository root are refused | CU76, #2393 | New |
| Catalog delete requires write scope, CSRF, and If-Match | CU76, #2393 | New |
| Catalog delete with stale If-Match returns 409 version_conflict | CU76, #2393 | New |
| Catalog delete without If-Match returns 428 | CU76, #2393 | New |
| Catalog delete appends an evidence record | CU76, #2393 | New |

## Capabilities

### New Capabilities

- `factory-config-as-code`: Config file tree listing, file read with redaction and path confinement, catalog delete with optimistic concurrency, and CLI parity.

### Modified Capabilities

- `aisdlc-serve`: Adds `GET /factory/config/files`, `GET /factory/config/files/{path}`, and `DELETE /factory/catalogs/{kind}/{id}` to the serve host.
- `factory-contract`: OpenAPI contract extended with config file schemas and catalog delete path.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Not touched — this is framework/ai-sdlc only |
| `frontend` | no | Not touched |
| `frontend-swing` | no | Not touched |
| `infra` / observability | no | Not touched |
| CI/CD (`.github/workflows`) | no | Not touched |

### Surface area

- Entities: none (file-based config only)
- Endpoints: `GET /factory/config/files`, `GET /factory/config/files/{path}`, `DELETE /factory/catalogs/{kind}/{id}`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (stdlib only)

### Write set

- `framework/ai-sdlc/aisdlc/config_files.py`
- `framework/ai-sdlc/aisdlc/catalogs.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/contract.py`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/feat-2398-config-as-code/**`
- `docs/openspec/changes/archive/**feat-2398*/**`
- `docs/openspec/specs/**`

### Architecture review

Follows existing patterns: new module `config_files.py` mirrors `factory.py` structure;
serve dispatch follows `_dispatch_get` pattern; catalog delete follows `catalogs.remove`
file operations. No ADR required — this implements the contract defined in epic #2393.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Add config files and catalog delete commands |
| `framework/ai-sdlc/CHANGELOG.md` | Add entry under [Unreleased] for config-as-code endpoints and CLI |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | New paths and schemas |

## Out of Scope

- Raw media type responses (Accept header negotiation) — skipped per design notes
- Dashboard UI surfaces for config-as-code — tracked in epic #2393 part G
- Catalog import/materialize — tracked in epic #2393 part F
