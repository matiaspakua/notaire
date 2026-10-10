# Drop retired notaire-shared from the Constitution Impact Analysis list (#1263)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1263 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/docs-1263-drop-notaire-shared-debd` |
| Gate 1 status | draft — Constitution §12: dedicated PR, reviewed by the Owner |

## Objetivo

CONSTITUTION.md §5 step 4 (Impact Analysis) still lists `notaire-shared` as an example
affected module. The module was retired under #1255 (ADR-025). Agents that follow the
Constitution therefore treat a dead module as live. This change removes that listing and
extends the existing retirement guard so the Constitution cannot reintroduce it as live.

## What Changes

- `CONSTITUTION.md` §5 step 4 lists only live product modules (`backend-api`, `frontend`).
- `workspace/tests/test_notaire_shared_retired.py` includes `CONSTITUTION.md` in
  `DOCS_AS_NON_LIVE` so a bare `notaire-shared` mention (without retired/deprecated markers)
  fails the guard.
- `CHANGELOG.md` records the documentation fix.
- No process step, gate, or product behavior is added or removed.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Impact Analysis names only live product modules | Constitution §5 step 4; ADR-025 / #1255 | Changed (stale example removed) |
| An amendment is a dedicated PR reviewed by the code owner | Constitution §12 | Respected |
| Docs must not present `notaire-shared` as a live module | `module-structure` spec / #1255 guard | Made explicit for CONSTITUTION.md |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `module-structure`: ADDED requirement — Constitution Impact Analysis lists only live
  modules; the retirement guard covers `CONSTITUTION.md`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| Docs / Constitution | yes | §5 step 4 wording |
| `workspace/tests` | yes | guard list includes CONSTITUTION.md |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Wording + guard only. ADR-025 already records the retirement; no new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` | §5 step 4 module list — drop `notaire-shared` |
| `CHANGELOG.md` | Changed entry under `[Unreleased]` |

## Out of Scope

- Broader Englishize of Constitution §5 Spanish step titles (follow-up; Related #1247)
- Always-loaded agent context cut (#1259 / #1197 — skipped until Owner resumes)
- OpenSpec archive sweep (#1252)
- Updating every historical ADR/SAD mention of `notaire-shared` (allowed when marked retired)
