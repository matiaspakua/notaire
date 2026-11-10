# ApprovalPort runtime mint/verify

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2205 |
| Use Case | CU76 |
| Branch | `cursor/feat-2205-approvalport-runtime-1dd2` |
| Gate 1 status | passed |

## Objetivo

ApprovalPort design stub shipped (#2190/#2191): doctor `no-evaluable`,
`approvals_live=false`. Isolator sole-committer enforcement landed (#2200/#2204).
V1 axioms still need cryptographic custody (token + channel; SSH sign / DSSE) so
agents cannot self-approve and forge UI clicks are not authority. Ship one honest
CU76 slice: mint + verify with a proven round-trip before claiming
`approvals_live=true`. Never fake approvals; never ForgePort write in this PR;
never flip `[committer] required=true`.

## What Changes

- `aisdlc approval status|mint|verify` with JSON honesty (`approvals_live`,
  algorithm, subject, identity)
- SSH custody via `ssh-keygen -Y sign` / `-Y verify` (ADR-005 preferred path);
  kit tests use ephemeral local/dev keys (fixtures or temp keys) — never claim
  production HSM
- `approvals_live=true` **only** after a proven mint→verify probe; degrade to
  `no-evaluable` / `approvals_live=false` when keys or signer tooling are missing
- `agent_can_approve` stays false; mint refuses agent role
- Optional `[approval]` profile / env opt-in for Notaire without claiming prod HSM
- Update ADR-005, FACTORY-ROADMAP/FOUNDATION, kit README, main `approval` OpenSpec
- TDD kit tests; one heavy PR; `Closes #2205`

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Cryptographic approvals bound to exact subject; token+channel authority; agent never self-approves | V1 axioms 2+4 / F-02/F-03 / ADR-005 | Changed (runtime mint/verify, not design-only) |
| `approvals_live=true` only after proven mint→verify round-trip | #2205 / ADR-005 honesty | New |
| Missing keys / ssh-keygen ⇒ `no-evaluable`, never fake live | ADR-005 / #2190 | Made explicit |
| Never flip `[committer] required=true`; no ForgePort write | Owner / #2205 | Made explicit |

## Capabilities

### New Capabilities

- (none — extends existing `approval`)

### Modified Capabilities

- `approval`: ApprovalPort runtime mint/verify over SSH (`ssh-keygen -Y`);
  honesty field `approvals_live` may become true after a proven round-trip probe;
  degrades when keys/tooling missing

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | kit unittest + preflight --full only |
| `workspace` (harness) | yes | `framework/ai-sdlc` approval + CLI + tests; ADR/FACTORY/README |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: optional `[approval]` / `AISDLC_APPROVAL_*` for local/dev keys
- Dependencies: system `ssh-keygen` with `-Y` support (OpenSSH); no new Python deps

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-*.md`
- store remaining-gaps note (outside repo) when reporting

## Out of scope

ForgePort write / `factory/gate` attest, portal mega-UI, Context Lake, Wilson vanity
stages / promotion, inventing Owner identity, flipping `[committer] required=true`,
claiming approvals live without a failing→passing mint/verify test, production HSM
product, mega-PR finishing the whole factory.

## Documentation Impact

| Document | Action |
|----------|--------|
| `framework/ai-sdlc/docs/adr/ADR-005-approval-port.md` | Update — runtime mint/verify + honesty |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | Update — ApprovalPort runtime status |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | Update — move runtime out of deferred when live |
| `framework/ai-sdlc/README.md` | Update — `approval` CLI row |
| `docs/openspec/specs/approval/spec.md` | Update — runtime requirements |
| `framework/ai-sdlc/docs/adr/ADR-002-ports-and-trust.md` | Update — Approval row honesty |
