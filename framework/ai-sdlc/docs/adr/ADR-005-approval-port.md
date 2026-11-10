# ADR-005: ApprovalPort custody boundary

**Status:** Accepted as a **design contract**. No live ApprovalPort (SSH/DSSE mint/verify) is implemented.
**Issue:** [#2190](https://github.com/matiaspakua/notaire/issues/2190) (CU76).

## Context

Gap-audit ranks 1–4 shipped (#2182–#2189). Rank 5 prefers an **ApprovalPort design ADR**
over Wilson L1+ promotion. Epic [#2134](https://github.com/matiaspakua/notaire/issues/2134)
is closed; Isolator design already shipped as [#2173](https://github.com/matiaspakua/notaire/issues/2173) /
ADR-004.

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
| Isolator stub (ADR-004) | Design-only sole-committer **runtime** boundary | Approval signatures |
| Dual-LLM “auditor” | Advisory reading aid | Authority |

Wilson autonomy on the Notaire profile remains `no-evaluable` (`trials=0`, empty
multi-stage ledger). **This ADR refuses Wilson auto-promotion** and refuses inventing
vanity stages to game L1 (#2171 honesty).

## Decision

### ApprovalPort (custody runtime — future)

An **ApprovalPort** is a future runtime that:

1. **Token + channel** — session identity is fixed when minted (`role:agent` /
   `role:person` / `role:admin`). Callers cannot assert a higher role via body fields.
2. **Cryptographic custody** — authority acts mint and verify signatures over frozen
   digests (plan / tree / profile) via **Git SSH** (`ssh-keygen -Y sign`) **or DSSE**
   (or an Owner-chosen equivalent with the same properties). WebAuthn may unlock a
   local key in a dashboard; it is not the CLI/CI system of record.
3. **Agent never self-approves** — axiom 2: an agent token cannot resolve approvals
   or mint person/admin authority. Policy tip already exists (`improve validate`);
   ApprovalPort must eventually **enforce** it.
4. **No fake approvals** — a TOML flag, harness permission, forge emoji reaction, or
   stub return value **never** means approvals are live. Skip ≠ pass; unevaluable ≠ pass.

### Peer boundary

Isolator (ADR-004) owns **OS sole-committer / grant / write-set** enforcement.
ApprovalPort owns **authority mint/verify**. Neither claims the other; both stay
`no-evaluable` until real runtimes exist.

### Kit stub (this slice)

`aisdlc.approval.ApprovalPort` / `approval_status()` report:

- `status=no-evaluable`
- `approvals_live=false`
- `agent_can_approve=false`

`aisdlc doctor` prints that status as an informational line and does **not** fail the
profile solely because ApprovalPort is unevaluable.

### Wilson promotion (explicit refuse)

Do **not** ship Wilson L1+ auto-promotion in this PR. Promote only after Owner-chosen
multi-stage operational evidence volume (honesty already in #2171). Empty/vanity stages
are refused.

## Consequences

- No SSH key product, DSSE minting service, ForgePort write/attest, portal, or Isolator OS
  in this slice.
- Resume ApprovalPort **implementation** only with a custody design that agents cannot
  forge and that does not treat UI clicks as cryptographic authority.
- Readers must not treat doctor’s Approval line as “approvals shipped.”

## References

- ADR-002 (ports); ADR-004 (Isolator peer)
- FACTORY-ROADMAP Deferred; FACTORY-FOUNDATION
- Store: `ASSESSMENT_AI_FACTORYS_V1.md` (axioms 2/4, F-02/F-03, ApprovalPort row);
  `ai-sdlc-factory-goal-gap-audit.md` rank 5
