> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

CU76 deferred ApprovalPort **runtime** after #2191 (design stub) and #2204
(Isolator sole-committer). `approvals_live` is always false. This slice adds real
SSH mint/verify custody with an honest round-trip probe. Owner
`[committer] required=false` must not flip. No ForgePort write.

## Goals / Non-Goals

**Goals:**
- Mint and verify authority signatures over a frozen subject via OpenSSH
  `ssh-keygen -Y sign` / `-Y verify`
- Prove custody with a mint→verify probe before setting `approvals_live=true`
- Degrade honestly when keys or `ssh-keygen -Y` are missing
- CLI `aisdlc approval status|mint|verify`; doctor INFO; ADR-005 + FACTORY + README

**Non-Goals:**
- Flipping `[committer] required=true`
- ForgePort write / portal / Context Lake / Wilson vanity stages
- Production HSM product or claiming Notaire already has HSM
- DSSE encoder as a second algorithm in this slice (SSH is the preferred ADR-005 path;
  DSSE remains an Owner-chosen equivalent for a later PR if needed)
- Faking `approvals_live=true`

## Decisions

1. **Custody algorithm (KISS)** — OpenSSH SSHSIG via `ssh-keygen -Y`. Subject is a
   file (exact bytes). Signature file is `<subject>.sig` by default. Namespace
   defaults to `file` (OpenSSH convention for file content). Algorithm field in
   JSON reports `ssh-ed25519` / key type from the fixture or configured key.

2. **Key resolution order** — (a) env `AISDLC_APPROVAL_KEY`,
   `AISDLC_APPROVAL_ALLOWED_SIGNERS`, `AISDLC_APPROVAL_IDENTITY` (optional
   `AISDLC_APPROVAL_NAMESPACE`); (b) optional profile `[approval]` paths relative
   to project root. Missing any required path ⇒ `no-evaluable`. Never invent keys
   for Notaire production in this PR.

3. **Live probe** — when keys resolve, run mint then verify on a temporary subject
   under a temp dir. `approvals_live=true` only when both succeed.
   Force-off: `AISDLC_APPROVAL=0|false|off` keeps `approvals_live=false`.

4. **Agent never approves** — `agent_can_approve` always false. `mint` refuses
   `--role agent` (and any identity that asserts agent authority). Person/admin
   mint requires the private key the agent must not hold.

5. **CLI** — `aisdlc approval status [--json]`, `mint --subject PATH [--role person|admin]
   [--sig PATH] [--json]`, `verify --subject PATH [--sig PATH] [--json]`.

6. **Notaire opt-in** — document `[approval]` / env without committing private keys
   or claiming HSM. Default Notaire profile may omit `[approval]`; doctor stays
   `no-evaluable` until Owner configures local/dev keys.

7. **Doctor** — Approval INFO reflects live vs no-evaluable honestly; never claims
   live without probe proof.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Readers treat fixtures as HSM | ADR + README local/dev disclaimer |
| ssh-keygen missing in minimal images | Degrade to no-evaluable; tests skip-or-fail honestly when `-Y` unavailable |
| Agent obtains private key | Ops: agent sessions never get `AISDLC_APPROVAL_KEY`; kit never mints for agent role |
| Scope into ForgePort | Explicit refuse |

## Testing Strategy

| Scenario | Test |
|----------|------|
| without keys → no-evaluable / not live | ApprovalPortTest |
| doctor reports Approval without failing for unevaluable | ApprovalPortTest |
| agent self-approval forbidden / mint refuses agent | ApprovalPortTest |
| mint→verify round-trip sets approvals_live | ApprovalPortTest |
| tampered subject fails verify | ApprovalPortTest |
| missing signer tooling / force-off degrades honestly | ApprovalPortTest |

TDD: write ApprovalPortTest first and observe fail, then implement.

## Regression Strategy

Keep `#2190` stub scenarios: without keys → no-evaluable / not live / agent false.
Extend `test_approval.py` without weakening those assertions. Kit unittest only;
no product Java/UI.

## Playwright Strategy

n/a — no UI surface (kit CLI/docs only).

## Deployment Strategy

Ships with the next release. Notaire opts into local/dev keys via env or `[approval]`
without claiming production HSM.

## Rollback Strategy

Revert the PR.

## Migration Plan

Additive CLI (`approval mint|verify|status`) + probe field semantics. Existing
profiles unchanged when `[approval]` is omitted. `[committer] required` remains false.

## Open Questions

None.
