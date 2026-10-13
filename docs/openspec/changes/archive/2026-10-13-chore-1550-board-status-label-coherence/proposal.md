# Board Status / label / issue-state coherence gate

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1550 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/board-status-label-coherence-b6e6` |
| Gate 1 status | passed |
| SURFACE | none |

## Objetivo

Delivery Board Status, GitHub Issue `state`, and workflow labels drift (CLOSED
leftovers on In Progress, stale `in-progress` on Done). Agents need a
Constitution-level gate and a mechanical harness check so coherence is
validated before merge/close and during board hygiene — not only in ad-hoc audits.

## What Changes

- Encode normative Status / label / state rules in CONSTITUTION (+ agent card)
  and AI SDLC Cloud docs / `docs/github/README.md`.
- Add `workspace/sdlc/check-board-status-label-coherence.py` with fixture tests;
  wire into Process Checks and preflight (`--scope pr`; skip clearly without
  `gh` auth).
- Modify capability `ai-sdlc-enforcement` (delta requirements). No product API or UI.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| CLOSED ⇒ Status Done; remove WIP labels (`in-progress`, `blocked`) | CU76, board contract | Made explicit |
| OPEN active WIP ⇒ Status In Progress ⇔ `in-progress` label | CU76, `docs/github/README.md` | Made explicit |
| Blocked work uses `blocked` label (no Blocked Status column); not Done while OPEN | CU76, board audit | New |
| AC met ⇒ prefer CLOSED + Done without WIP labels | CU76, Gate 5 | Made explicit |
| Harness fails on drift for items in scope; skips clearly without `gh` auth | CU76 | New |
| Do not invent Owner product scope (#1445 / #1438 / #1226) | CU76, R5 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ai-sdlc-enforcement`: add requirements for Issue ↔ Project Status ↔ label
  coherence and a process-gate failure (or clear skip) when drift is detected.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes | Process Checks invoke the new checker |
| `workspace` | yes | New SDLC checker + unit tests |
| `docs` | yes | CONSTITUTION, AI SDLC Cloud, github board README |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none (`gh` + Python stdlib)
- SURFACE: **none** (docs + process tooling)

### Architecture review

Follows existing AI SDLC enforcement pattern (`check-openspec-archive-hygiene.py`,
`sdlc-process.yml`, preflight). No ADR — process hygiene.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CONSTITUTION.md` / `CONSTITUTION-AGENT-CARD.md` | Gate 5 + Issue↔board↔labels rules + tooling map |
| `docs/github/README.md` | Coherence taxonomy table + validation commands |
| `docs/300-development/304-ai-sdlc-cloud/*` | Foreman/merge-gate operational rule |
| `docs/openspec/specs/ai-sdlc-enforcement/spec.md` | Sync delta at archive time |
| `CHANGELOG.md` | Unreleased note for the gate |

## Out of Scope

- Human paste execution for current board drift (agent writes 403) — tracked in
  Project store `docs/board-status-label-sync-paste.md`
- Inventing Owner ADR / LICENSE decisions
- Adding a Blocked Status column to the GitHub Project
