# Keep the accepted OpenAPI breaking-change list empty (#1315, slice 1)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1315 |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| Branch | `chore/1315_accepted_breaking_list_empty` |
| Gate 1 status | draft |

## Objetivo

`backend-api/openapi/accepted-breaking-changes.txt` held 5 entries (4 from #655 for `/api/v1/pagos`, 1 from #596 for `GET /api/v1/historial`) whose breaks are already on main. They accepted nothing, and could silently accept a later break with the same text. The Owner wants the contract coherent and the list empty unless a break is truly unavoidable and justified.

## What Changes

- The 5 inert entries are removed; the header states the rule (empty by default, an entry only lives in the PR that introduces its break, issue comment, CHANGELOG and Owner approval).
- New `workspace/sdlc/check-accepted-breaking-changes.py`: runs `oasdiff breaking --format json` between the base and the revision with no ignore list, and fails on every entry that matches no current breaking change. It uses the oasdiff err-ignore rule: case-insensitive, METHOD + path and the change text.
- `openapi-contract.yml`: step "Accepted breaking list has no stale entries" after the breaking diff, pull requests only, oasdiff 1.33.0 (same as `oasdiff-action@v0.1.18`) verified by checksum.
- `preflight.sh` runs the same check locally.
- `docs/200-architecture/208-devsecops/README.md` documents the rule.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The accepted-breaking list is empty on main; an entry exists only in the PR that introduces its break | #1315 | New |
| An entry needs an issue comment, the reason no working client breaks, a CHANGELOG entry and Owner approval | #1315, #1312 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `openapi-breaking-change-gate`: OpenAPI breaking changes fail CI unless accepted by the Owner, and accepted entries do not outlive their pull request.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `workspace` | yes | checker script, preflight, guard test |
| `backend-api` | yes | `openapi/accepted-breaking-changes.txt` only |
| `.github` | yes | `openapi-contract.yml` step |
| `docs` | yes | devsecops README rule |

### Surface area

- Endpoints: none (`openapi.yaml` unchanged)
- CI: new step in `openapi-contract.yml` (pull requests)
- Entities / Flyway / Configuration: none

### Architecture review

The checker is a stdlib Python script in `workspace/sdlc` next to the other process checks (ADR-026). It shells out to oasdiff and reads its JSON report, so it applies exactly the breaking-change rules CI uses.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |
| `docs/200-architecture/208-devsecops/README.md` | OpenAPI contract row |
| `backend-api/openapi/accepted-breaking-changes.txt` | header |
