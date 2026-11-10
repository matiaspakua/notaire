# ADR-004: Isolator / sole-committer runtime boundary

**Status:** Accepted as a **design contract**. No OS Isolator is implemented.
**Issue:** [#2173](https://github.com/matiaspakua/notaire/issues/2173) (CU76).

## Context

Epic [#2134](https://github.com/matiaspakua/notaire/issues/2134) (P0–P3 spine) is
closed. Deferred work still lists Isolator. The stop note after [#2172](https://github.com/matiaspakua/notaire/pull/2172)
said: Isolator only after Owner sets `[committer] runtime_*` and a real sole-committer
runtime exists. Owner identity is **not** invented here.

Today Notaire has:

| Surface | What it is | What it is not |
|---------|------------|----------------|
| `aisdlc committer` (#2167/#2169) | **Policy** dry-run of axiom 2 (who authored commits since base); `no-evaluable` until `runtime_names` / `runtime_emails` | OS isolation, sandbox, forge write |
| Harness adapters (#2122) | Read runtime manifests in place; always `sandbox=false` | Isolator |
| Profile / YAML flags | Configuration | Isolation |

## Decision

### Isolator (runtime)

An **Isolator** is a future **sole-committer runtime** that:

1. **Grants** — a human/Owner process holds the only write grant to `git commit` (and
   similarly privileged forge mutations). Agents propose; they do not receive that grant.
2. **Write set** — the runtime may only touch paths allowed by the change’s write set /
   protected-path rules (CONSTITUTION + `[protected]` + proposal scope).
3. **Agent never commits** — axiom 2 is **enforced** by the runtime, not merely reported.
4. **YAML ≠ OS isolation** — a flag, harness permission string, or TOML `[isolator]`
   block **never** means the process is sandboxed. Path rules and deny lists are not an
   OS Isolator (extends ADR-002).

### Policy dry-run (already shipped)

`aisdlc committer` remains the **policy** check Isolator must eventually **enforce**.
Until Owner configures `runtime_*`, committer stays `no-evaluable` / advisory
(`required = false`). This ADR does **not** set Owner identity or flip `required = true`.

### Kit stub (this slice)

`aisdlc.isolator.IsolatorPort` / `isolator_status()` report:

- `status=no-evaluable`
- `sandbox=false`
- `enforces_committer=false`

`aisdlc doctor` prints that status as an informational line and does **not** fail the
profile solely because Isolator is unevaluable.

### Grant profile data (#2192)

Optional `[[grants]]` in the adopter profile declare axiom-5 fields (worktree, write
set, required checks, budget) for **validate / list / show only**. That is **data
toward** Isolator OS — not containment, not grant expire/kill-switch, and not a
sandbox claim. Unknown `required_checks` fail closed against declared gate names;
omitting grants is a no-op. Presence of grants **must not** flip Isolator to
evaluable or `sandbox=true`. Peer custody remains ApprovalPort (ADR-005), also
unevaluable until a real runtime exists.

## Consequences

- No fake sandbox, ForgePort, or portal in this slice.
- Resume Isolator **implementation** only when: Owner `runtime_*` is real **and** an OS
  sole-committer runtime design with grants exists.
- Readers must not treat doctor’s Isolator line, harness `sandbox=false`, or
  `[[grants]]` TOML as “Isolator shipped.”

## References

- ADR-002 (ports; what is not a sandbox); ADR-005 (ApprovalPort peer)
- FACTORY-ROADMAP Deferred; FACTORY-FOUNDATION sole-committer note
- Store: `internal/factory-stop-after-2172.md`; Grant schema Issue #2192
