# factory-dashboard

**Purpose:** Factory Control, an optional web dashboard over one or more AI SDLC factories. A Node companion
of the kit, outside its stdlib-only core: it speaks only the `aisdlc serve` contract and the RepoLens HTTP API
(ADR-006), for many workspaces, users and repositories.

**Verify:** `bash framework/ai-sdlc/factory-dashboard/verify.sh` (typecheck, server tests, build);
`--e2e` adds Playwright + axe against a seeded `aisdlc serve`.

## Contract (what other modules may rely on)

- `dashboard.json` workspaces: `id`, `label`, `kit` URL, optional `knowledge` URL (README)
- Browser API under `/api/`: workspaces, per-workspace session, `/factory/*` proxy, read-only knowledge
- Never holds a token in the browser; writes need `X-Dashboard-CSRF`

## Seams (what this module reads from outside)

- Only HTTP: `aisdlc serve` (`openapi/factory-control.openapi.yaml`) and `repolens serve`

## Must not

- Read or write a workspace's files, or call a kit command directly
- Name an adopter (guarded by the kit's `tests/test_boundaries.py`)
- Become a runtime dependency of the kit core

Manifest entry: [`workspace/modules.yaml`](../../../workspace/modules.yaml). Rationale: ADR-006.
