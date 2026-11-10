# ADR-005: ApprovalPort custody boundary

**Status:** Accepted. **Runtime mint/verify** shipped (#2205) via OpenSSH
`ssh-keygen -Y`. `approvals_live=true` only after a proven round-trip.
**Issue:** [#2205](https://github.com/matiaspakua/notaire/issues/2205) (CU76);
design stub [#2190](https://github.com/matiaspakua/notaire/issues/2190).

## Context

Gap-audit ranks 1–4 shipped (#2182–#2189). Rank 5 preferred an **ApprovalPort design ADR**
(#2190) over Wilson L1+ promotion. Isolator sole-committer enforcement landed
(#2200/#2204). This slice adds the custody **runtime**.

V1 axioms (store `ASSESSMENT_AI_FACTORYS_V1.md`):

| Axiom | Meaning for this ADR |
|-------|----------------------|
| 2 | Agent never commits **or approves** |
| 4 | Authority comes from **token + channel**, never request-body fields |
| F-02 / F-03 | Scoped session tokens; cryptographic approvals bound to exact content (SSH sign / DSSE preferred over localhost WebAuthn for CLI/CI) |

Today Notaire has:

| Surface | What it is | What it is not |
|---------|------------|----------------|
| `aisdlc improve validate` (#2141) | **Policy** refusal of agent self-approval on proposal artifacts | Cryptographic custody |
| Forge PR review / merge UI | Human process + Foreman merge rule | ApprovalPort |
| Isolator runtime (ADR-004 / #2200) | OS sole-committer / grant / write-set enforcement | Approval signatures |
| Dual-LLM “auditor” | Advisory reading aid | Authority |
| `aisdlc approval mint\|verify\|status` (#2205) | SSHSIG mint/verify custody | Production HSM |
| `aisdlc forge attest` (#2208) | Consumes Approval verify before any forge mutation | Unscoped merge/close; fake attest |

Wilson autonomy on the Notaire profile remains `no-evaluable` (`trials=0`, empty
multi-stage ledger). **This ADR refuses Wilson auto-promotion** and refuses inventing
vanity stages to game L1 (#2171 honesty).

## Decision

### ApprovalPort (custody runtime)

An **ApprovalPort** is a runtime that:

1. **Token + channel** — session identity is fixed when minted (`role:person` /
   `role:admin`). Callers cannot assert a higher role via body fields; mint
   **refuses** `role:agent`.
2. **Cryptographic custody** — authority acts mint and verify signatures over frozen
   subject bytes via **Git SSH** (`ssh-keygen -Y sign` / `-Y verify`). DSSE remains an
   Owner-chosen equivalent for a later slice; WebAuthn may unlock a local key in a
   dashboard but is not the CLI/CI system of record.
3. **Agent never self-approves** — axiom 2: an agent token cannot resolve approvals
   or mint person/admin authority. `agent_can_approve` is always false. Policy tip
   remains (`improve validate`); ApprovalPort **enforces** the refusal on mint.
4. **No fake approvals** — a TOML flag, harness permission, forge emoji reaction, or
   stub return value **never** means approvals are live. `approvals_live=true` only
   after a proven mint→verify probe. Missing keys / missing `ssh-keygen -Y` /
   `AISDLC_APPROVAL=0` → `no-evaluable` / `approvals_live=false`. Skip ≠ pass.

### Peer boundary

Isolator (ADR-004) owns **OS sole-committer / grant / write-set** enforcement.
ApprovalPort owns **authority mint/verify**. Neither claims the other.

### Kit surface (#2205)

- CLI: `aisdlc approval status|mint|verify` (JSON honesty: `approvals_live`,
  `algorithm`, `identity`, `subject`)
- Keys: env `AISDLC_APPROVAL_KEY` / `AISDLC_APPROVAL_ALLOWED_SIGNERS` /
  `AISDLC_APPROVAL_IDENTITY` (optional `AISDLC_APPROVAL_NAMESPACE`) or profile
  `[approval]` local/dev paths
- Doctor INFO line; never fails the profile solely because ApprovalPort is unevaluable
- Local/dev keys and kit test fixtures are **not** a production HSM claim

### Wilson promotion (explicit refuse)

Do **not** ship Wilson L1+ auto-promotion in this PR. Promote only after Owner-chosen
multi-stage operational evidence volume (honesty already in #2171). Empty/vanity stages
are refused.

### Out of scope here

Portal, Context Lake, flipping `[committer] required=true`, production HSM product.
ForgePort write / `factory/gate` attest shipped separately in #2208 (gated by this port).

## Consequences

- Adopters can prove custody in kit tests and opt into local/dev keys without claiming HSM.
- Readers must not treat doctor’s Approval line as “production approvals shipped.”
- ForgePort write (#2208) requires Approval verify (or honest refuse); dry-run default.

## References

- ADR-002 (ports); ADR-004 (Isolator peer)
- FACTORY-ROADMAP Deferred; FACTORY-FOUNDATION
- Store: `ASSESSMENT_AI_FACTORYS_V1.md` (axioms 2/4, F-02/F-03, ApprovalPort row);
  `ai-sdlc-factory-goal-gap-audit.md` rank 5
