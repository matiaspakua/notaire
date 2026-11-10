# ForgePort write / factory gate attest (ApprovalPort custody)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2208 |
| Use Case | CU76 |
| Branch | `cursor/feat-2208-forgeport-write-attest-44ce` |
| Gate 1 status | passed |

## Objetivo

ForgePort is read-only today (#2184/#2185). ApprovalPort mint/verify is live
(#2205/#2207) with honest `approvals_live`. Operators still cannot attest a
`factory/gate` result to the forge without either (a) mutating without custody or
(b) faking attestations. Ship one CU76 slice: approval-gated ForgePort write /
gate attest — default dry-run; live scoped `gh` write only behind explicit flags
+ verified Approval. Never fake `attested` / `write_enabled`; never flip
`[committer] required=true`.

## What Changes

- `aisdlc forge attest` builds a `factory/gate` attest subject and verifies it via
  ApprovalPort before any forge mutation
- Default dry-run: JSON honesty `write_enabled=false`, `attested` only when verify
  succeeds, `approvals_live` from ApprovalPort; no `gh` mutation
- Optional scoped write (`comment` / `label` / `check-run`) only when Approval
  verify succeeds **and** `--write` is set; unscoped mutations stay refused
- Without Approval verify (or `no-evaluable`): refuse write honestly
- Doctor INFO line for ForgePort write honesty; ADR-002 / FACTORY / README /
  delivery-metrics + forge OpenSpec honesty; ADR-005 pointer
- TDD kit tests; one heavy PR; `Closes #2208`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No forge write without Approval verify (or honest refuse) | #2208 / ADR-002 / ADR-005 | New |
| Never fake attestations; never claim write live when dry-run only | #2208 honesty | New |
| Scoped write surfaces only (`comment`/`label`/`check-run`); default dry-run | #2208 KISS | New |
| Never flip `[committer] required=true`; no portal / Wilson / HSM claims | Owner / #2208 | Made explicit |
| Read-only lead-time ForgePort unchanged | #2184 | Made explicit |

## Capabilities

### New Capabilities

- `forge-port-write`: Approval-gated ForgePort write / `factory/gate` attest with
  dry-run default and scoped live write behind verified Approval

### Modified Capabilities

- `delivery-metrics`: clarify that lead-time ForgePort stays read-only; write/attest
  is a separate forge surface (pointer only — no lead-time behavior change)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | kit unittest + preflight --full only |
| `workspace` (harness) | yes | `framework/ai-sdlc` forge + CLI + tests; ADR/FACTORY/README |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: reuses ApprovalPort keys; optional `AISDLC_FORGE_WRITE=1` / `--write`
- Dependencies: system `gh` + existing ApprovalPort (`ssh-keygen -Y`); no new Python deps

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-*.md`
- store remaining-gaps note (outside repo) when reporting

## Out of scope

Portal mega-UI, Context Lake, Wilson vanity stages / promotion, unscoped `gh`
mutations (merge/close/edit/approve), inventing Owner identity, flipping
`[committer] required=true`, claiming write live when only dry-run exists,
production HSM product, mega-PR finishing the whole factory.

## Documentation Impact

| Document | Action |
|----------|--------|
| `framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md` | Update — ForgePort write/attest honesty |
| `framework/ai-sdlc/docs/adr/ADR-005-approval-port.md` | Update — pointer: write gated by Approval |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | Update — ForgePort write status |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Update — move scoped write out of deferred when shipped |
| `framework/ai-sdlc/README.md` | Update — `forge attest` CLI row |
| `framework/ai-sdlc/docs/METRICS.md` | Update — read-only vs write boundary |
| `docs/openspec/specs/delivery-metrics/spec.md` | Sync pointer on archive |
| `docs/openspec/specs/forge-port-write/spec.md` | New main spec on archive |
