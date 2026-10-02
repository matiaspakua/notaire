# Gate 1 scenario counting without bc

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1108 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/chore-validate-sdlc-no-bc-69d3` |
| Gate 1 status | passed |

## Objetivo

Cloud Agents that skip `.cursor/install.sh` were false-failing Gate 1:
`scripts/validate-sdlc-plan.sh` summed `#### Scenario:` counts with `paste | bc`,
and a missing or broken `bc` fell through to count `0`. This change makes scenario
summing work with `awk` so Gate 1 does not hard-depend on `bc`, and documents that
agents must run `bash .cursor/install.sh` until the Environment card is Saved
(`openspec` + `bc` still come from that script).

Related process work under CU76 / Issue #1108 (OpenSpec Gate 1 tooling). This PR
is a chore follow-up and does **not** close #1108.

## What Changes

- Sum scenario counts in `validate-sdlc-plan.sh` with `awk` (identical numeric
  result when `bc` is present; no hard dependency).
- Add unit coverage for Gate 1 accepting a filled plan when `bc` is broken on PATH.
- Update `docs/300-development/304-ai-sdlc-cloud/` so Cloud Agents know to run
  `bash .cursor/install.sh` until Environment Save wires it automatically.
- Add this OpenSpec change folder so Process Checks pass without `sdlc-exception`.

No change to `.cursor/install.sh` — it already installs `bc` and
`@fission-ai/openspec@1.14.0` on `main`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Gate 1 scenario counting SHALL not hard-depend on `bc` | CU76 / CONSTITUTION Gate 1 | New |
| Cloud Agents SHALL run `.cursor/install.sh` until the Saved Environment card wires it | CU76 / cloud fleet docs | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: scenario-count sum without `bc`; cloud install path documented.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Process Checks pass via this change folder |
| `scripts/` | yes | `validate-sdlc-plan.sh` + unit test |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | install.sh until Saved; bc optional for Gate 1 |
| `openspec/changes/` | yes | This Gate 1 folder |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (bash + awk already on PATH)

### Architecture review

No application-architecture change. No ADR. Process tooling only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Run install.sh until Saved; bc optional for Gate 1 |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | Same process learning |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Index learning |
| `CHANGELOG.md` | n/a — not user-visible |
