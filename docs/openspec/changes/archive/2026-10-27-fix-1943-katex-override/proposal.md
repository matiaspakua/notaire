# Frontend lockfile pins a patched katex under markdownlint-cli2

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1943 |
| Use Case | CU78 — Security |
| Branch | `fix/1943_katex_override` |
| Gate 1 status | passed |

## Objetivo

Dependabot's `katex` security update in `/frontend` fails on every run: the patched
`katex` (>= 0.18.2) is reachable only by downgrading `markdownlint-cli2` 0.24.0 to 0.16.0.
`katex` arrives only through the docs linter (`markdownlint-cli2@0.24.0` → `markdownlint@0.42.0`
→ `micromark-extension-math@3.1.0`, which declares `katex@^0.16.0`), and the lockfile resolves
`katex@0.16.47`, inside the advisory range `>= 0.11.0 < 0.18.2`.

The preferred fix, upgrading `markdownlint-cli2`, is not available: 0.24.0, `markdownlint` 0.42.0
and `micromark-extension-math` 3.1.0 are the latest releases, and none accepts katex 0.18.

## What Changes

- `frontend/package.json` `overrides`: `"katex": "^0.18.2"` (the fallback, same pattern as the
  `smol-toml` override from #1046).
- `frontend/package-lock.json` refreshed so `katex` resolves to 0.18.x.
- `security/tests/test_katex_override.py` guards the override and the resolved version.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Frontend dev dependencies resolve to patched releases even when an upstream pins a vulnerable range | #1046, #1943 | Made explicit for katex |

## Capabilities

### New Capabilities

- `frontend-katex-override`: the frontend lockfile resolves `katex` to a patched release.

### Modified Capabilities

- none

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | `package.json` override, `package-lock.json` |
| `infra` / observability | no | – |
| CI/CD (`.github`) | no | – |
| `security` | yes | `security/tests/test_katex_override.py` |
| `workspace` | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: `katex` (dev, transitive) 0.16.47 → 0.18.x via override

### Architecture review

Dependency pin only; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Bug Fixes entry |
| `docs/openspec/specs/frontend-katex-override/spec.md` | New capability (synced on archive) |

## Out of Scope

- Removing the override once `micromark-extension-math` accepts katex 0.18 (follow-up when upstream ships).
