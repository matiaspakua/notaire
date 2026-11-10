# Doctor accepts `signal`; Foreman unmapped honesty; lead-time Issue hint

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2211 |
| Use Case | CU76 |
| Branch | `improve/2211_doctor-signal-foreman-knobs` |
| Gate 1 status | passed |

## Objetivo

ASLI-57 Foreman ops habit (#2210 custody chain) surfaced three kit frictions:
`doctor` warns on Notaire's intentional `signal` stage (#2159), `foreman plan`
reports only `mapped_count: 1` without saying unmapped stages are expected, and
operators pass a PR number to `metrics lead-time --issue` (Issue-keyed). Fix the
honesty surface without inventing fake stage knobs or Wilson/portal work.

## What Changes

- Doctor accepts `signal` as a documented adopter-local stage alias (no WARN);
  other unknown kebab-case stages still warn as non-canonical
- Foreman plan/doctor state clearly which gate stages lack `[[stages]]` knobs
  (`unmapped_stages` / note) — KISS: do not invent fake knobs
- Lead-time CLI help + doctor INFO: pass **Issue** number, not PR number
- FACTORY-ROADMAP documents the `signal` alias choice and Foreman mapping honesty
- TDD kit tests; one heavy PR; `Closes #2211`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `signal` is an intentional local alias for #2159 ratchet recording; doctor MUST NOT warn as if it were a mistake | #2211 / #2159 / CU76 | Made explicit |
| Unmapped Foreman stages are expected until knobs are filled; kit MUST NOT invent fake stage knobs | #2211 KISS | New honesty |
| Lead-time is Issue-keyed; operators pass Issue number, not PR number | #2138 / #2184 / #2211 | Made explicit |
| No Wilson L1, no `[committer] required=true`, no portal/Context Lake, no fake attest | Owner / #2211 out of scope | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `preflight-evidence`: doctor accepts `signal` as documented local alias (no non-canonical WARN)
- `foreman-routing`: plan/doctor honesty for unmapped stages (expected until knobs filled)
- `delivery-metrics`: lead-time help/doctor note — Issue number, not PR number

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | kit unittest + preflight --full only |
| `workspace` (harness) | yes | `framework/ai-sdlc` adopt/foreman/cli + tests; FACTORY-ROADMAP / README / METRICS |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (profile already declares `signal` gates)
- Dependencies: none

### Write set

- `framework/ai-sdlc/**`
- `.aisdlc/aisdlc.toml` (comment-only if needed)
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md`
- `CHANGELOG.md`

### Architecture review

Follows existing kit honesty patterns (doctor INFO for ports, Foreman plan
read-only). No new ADR — documents adopter-local alias + KISS unmapped honesty
in FACTORY-ROADMAP.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Document `signal` local-alias choice + Foreman unmapped honesty |
| `framework/ai-sdlc/README.md` | Custom stage / doctor alias + lead-time Issue hint |
| `framework/ai-sdlc/docs/METRICS.md` | Issue vs PR for live lead-time |
| `docs/openspec/specs/preflight-evidence/spec.md` | Sync on archive |
| `docs/openspec/specs/foreman-routing/spec.md` | Sync on archive |
| `docs/openspec/specs/delivery-metrics/spec.md` | Sync on archive |
| `CHANGELOG.md` | `[Unreleased]` kit ops honesty entry |

## Out of Scope

Wilson L1 promotion, flipping `[committer] required=true`, portal/Context Lake,
fake attest, inventing fake `[[stages]]` knobs for red/green/pre-pr, renaming
`signal` to a canonical id.
