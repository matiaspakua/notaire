# ADR-004: Isolator / sole-committer runtime boundary

**Status:** Accepted. Isolator OS **started** for probe/run when a real containment
backend works; Grant expire shipped. Still refuse fake sandbox from YAML.
**Issue:** [#2173](https://github.com/matiaspakua/notaire/issues/2173) (design stub),
[#2198](https://github.com/matiaspakua/notaire/issues/2198) (OS runtime + Grant expire). CU76.

## Context

Epic [#2134](https://github.com/matiaspakua/notaire/issues/2134) (P0–P3 spine) is
closed. Deferred work listed Isolator OS and Grant expire. Owner `[committer]
runtime_*` is set with `required=false` — this ADR does **not** flip `required=true`
and does not invent Owner identity.

| Surface | What it is | What it is not |
|---------|------------|----------------|
| `aisdlc committer` (#2167/#2169/#2175) | **Policy** dry-run of axiom 2 | OS isolation, sandbox, forge write |
| `aisdlc grant check` (#2194/#2198) | **Policy** write-set + expire fail-closed | Isolator OS complete / sandbox |
| Harness adapters (#2122) | Read runtime manifests; `sandbox=false` unless real | Isolator |
| `aisdlc isolator status\|probe\|run` (#2198) | OS containment probe/run (prefer `bwrap`) | Sole-committer interception (`enforces_committer` stays false) |

## Decision

### Isolator (runtime)

An **Isolator** is a **sole-committer / containment runtime** that:

1. **Grants** — a human/Owner process holds the only write grant to `git commit`.
2. **Write set** — the runtime may only touch paths allowed by Grant / protected rules.
3. **Agent never commits** — axiom 2 is **enforced** by the runtime when
   `enforces_committer=true` (not yet; still false in #2198).
4. **YAML ≠ OS isolation** — a flag, harness permission string, or TOML block
   **never** means the process is sandboxed (extends ADR-002).

### Isolator OS started (#2198)

- Prefer Linux **`bwrap` (bubblewrap)** when available (`AISDLC_BWRAP` may override;
  empty forces none).
- `probe` / `status` report honesty fields: `status`, `sandbox`, `backend`,
  `enforces_committer`, detail.
- `sandbox=true` **only** after a successful containment probe.
- When `bwrap` is missing or the probe fails: `backend=none` (or `bwrap` with
  `probe_ok=false`), `sandbox=false`, `status=no-evaluable` — **honest degrade**.
- `run` executes under bwrap with repo RO and Grant `write_set` paths RW, or
  **refuses** when backend missing / grants expired.

### Grant expire (#2198)

Optional `expires_at` (ISO-8601 UTC) and/or `ttl_seconds` on `[[grants]]`.
Expired grants **fail closed** for `grant check` and `isolator run`. Expire schema
alone does **not** mean Isolator OS shipped.

### Policy dry-runs (already shipped)

`aisdlc committer` and `aisdlc grant check` remain policy checks Isolator must
eventually **enforce**. Peer custody remains ApprovalPort (ADR-005).

## Consequences

- No fake sandbox, ForgePort write, or portal in this slice.
- Readers must not treat doctor Isolator line, harness flags, `[[grants]]` TOML,
  green `grant check`, or expire fields alone as “Isolator complete.”
- Hosts without `bwrap` (common in CI/agent images) correctly report
  `sandbox=false` — that is success of honesty, not a product defect.

## References

- ADR-002 (ports; what is not a sandbox); ADR-005 (ApprovalPort peer)
- FACTORY-ROADMAP Deferred; FACTORY-FOUNDATION
- Grant schema #2192; write-set check #2194; Isolator OS + expire #2198
