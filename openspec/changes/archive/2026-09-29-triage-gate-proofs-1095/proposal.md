# Triage gate protects derived values and rejects non-proofs

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1095 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `chore/1095_triage_gate_proofs` |
| Gate 1 status | passed |

## Objetivo

The #1064 rerun after #1094 failed triage 3 times. The #1094 fixes held; three
remaining gaps cost the attempts or would let a hollow triage through:

- The worker overwrote the pre-filled `TYPE=docs` with `fix` in 2 of 3 attempts.
- `proven by: command bash grep …` passed the format check: a search proves nothing.
- `KIND=docs` with `new test` proofs was accepted, but non-code kinds skip the tests phase.

## What Changes

- New `bin/triage_check.py`: `restore` (harness-derived keys back from the seed),
  `bad-proofs` (read-only search/print commands), `kind-conflict`.
- `seed_triage` keeps the seed in `$STATE/triage.seed.env`; `gate_triage` restores
  `ISSUE`, `USE_CASE` and a derived `TYPE` from it, logging `triage-repaired REVIEW`.
- `gate_triage` rejects search-command proofs and non-code `KIND` with new tests.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Values the harness derived are not the worker's to change | AI-SDLC.md (triage) | New |
| A proof must fail when the criterion is false | AI-SDLC.md (triage) | Made explicit |
| Promised tests must have a tests phase to be written in | AI-SDLC.md (triage) | New |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-sdlc-enforcement`: triage seed repair, proof and KIND checks.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `local-ai/sdlc` harness | yes | see What Changes |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

AUDIT §4.1 layer L4 (gates). No ADR: the product architecture does not change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `local-ai/sdlc/AI-SDLC.md` | triage row: seed repair, proof and KIND rules |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

Judging whether a test named in a proof is a good test; that is Gate 2's job.
