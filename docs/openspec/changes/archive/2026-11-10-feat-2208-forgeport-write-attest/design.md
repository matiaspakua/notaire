> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

CU76 deferred ForgePort **write** until ApprovalPort runtime (#2205/#2207) and
read-only lead-time (#2184) existed. Approval can mint/verify; ForgePort still
refuses all mutations. This slice adds approval-gated attest + optional scoped
write. Owner `[committer] required=false` must not flip. No portal / Wilson.

## Goals / Non-Goals

**Goals:**
- Build a `factory/gate` attest subject and verify it via ApprovalPort
- Default dry-run with JSON honesty (`write_enabled`, `attested`, `approvals_live`)
- Optional scoped live write (`comment` / `label` / `check-run`) only after verify
  + explicit `--write`
- Doctor INFO for ForgePort write honesty; ADR/FACTORY/README updates
- Keep read-only lead-time path unchanged

**Non-Goals:**
- Unscoped forge mutations (merge, close, edit PR, approve review)
- Flipping `[committer] required=true`
- Portal / Context Lake / Wilson vanity
- Production HSM claims
- Faking `attested=true` or `write_enabled=true`

## Decisions

1. **CLI** — `aisdlc forge attest` with:
   - `--gate NAME` (required) + optional `--outcome pass|fail|no-evaluable`
   - `--subject PATH` (attest JSON; default: write temp subject under cwd/tmp)
   - `--sig PATH` (Approval signature; default `SUBJECT.sig`)
   - `--write` to enable live scoped mutation (default off)
   - `--kind comment|label|check-run` (default `comment`)
   - `--pr N` and/or `--issue N` as scoped target (required when `--write`)
   - `--json` for honesty payload

2. **Custody gate** — before any write, call ApprovalPort `verify(subject, signature)`.
   If verify fails or Approval is `no-evaluable` / not live for that subject →
   refuse: `attested=false`, `write_enabled=false`, non-zero exit. Never invent a
   signature. Dry-run may still build the subject and report honesty.

3. **Dry-run default** — without `--write`, never invoke mutating `gh` argv.
   `write_enabled=false`. `attested=true` only when verify succeeds.

4. **Scoped write allowlist** — with `--write` + verified approval only:
   - `comment` → `gh pr comment` / `gh issue comment`
   - `label` → `gh pr edit --add-label` / `gh issue edit --add-label` (single label
     `aisdlc-factory-gate` or payload-derived; no arbitrary label spam without
     `--label`)
   - `check-run` → `gh api` POST to checks API with `factory/gate` name (injectable
     runner). Never merge/close/approve/review.

5. **Payload shape** — schema 1 JSON subject:
   ```json
   {
     "schema": 1,
     "kind": "factory/gate",
     "gate": "<name>",
     "outcome": "pass|fail|no-evaluable",
     "source": "aisdlc-forge-attest"
   }
   ```
   Exact bytes are what Approval signs/verifies.

6. **Runner injection** — same as #2184: unit tests inject a `gh` runner; suite
   never touches the network. Approval verify may be injected for offline TDD
   when SSH keys are absent (or use ephemeral keys like #2205).

7. **Doctor** — INFO line: dry-run available; live write only when Approval live
   *and* write flag path exists; never claims write live by default.

8. **Read path** — `lead_time_sample` / `_assert_read_only` unchanged. Write path
   is a separate entry that allowlists scoped verbs only after custody.

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Readers treat dry-run as live write | JSON `write_enabled=false` + docs |
| Accidental unscoped mutation | Explicit allowlist; refuse other verbs |
| Fake attested without keys | Require Approval verify; fail closed |
| Scope creep into portal/Wilson | Explicit non-goals |

## Testing Strategy

| Scenario | Test |
|----------|------|
| dry-run default: no gh mutation | ForgePortAttestTest |
| without verify → refuse / attested=false | ForgePortAttestTest |
| verify OK dry-run → attested=true, write_enabled=false | ForgePortAttestTest |
| --write without verify → refuse | ForgePortAttestTest |
| --write + verify → scoped write only | ForgePortAttestTest |
| unscoped verbs refused | ForgePortAttestTest |
| doctor honesty | ForgePortAttestTest |
| CLI JSON honesty | ForgePortCliTest |

TDD: write ForgePortAttestTest first and observe fail, then implement.

## Regression Strategy

- Existing ForgePort lead-time tests must stay green (read-only).
- ApprovalPort tests unchanged.
- Kit unittest suite + `bash workspace/sdlc/preflight.sh --full`.

## Playwright Strategy

n/a — no UI surface (kit CLI only).

## Deployment Strategy

Ships with the next release. Live scoped write remains opt-in (`--write` + verified
Approval). Default dry-run needs no forge credentials beyond existing Approval keys
for attest verify.

## Rollback Strategy

Revert the PR. Read-only lead-time ForgePort (#2184) is unaffected.

## Migration Plan

Additive CLI (`forge attest`) + doctor INFO. Existing `metrics lead-time` and
ApprovalPort surfaces unchanged. `[committer] required` remains false.

## Open Questions

None.

## Impacted files (expected)

- `framework/ai-sdlc/aisdlc/forge.py` — attest + scoped write
- `framework/ai-sdlc/aisdlc/cli.py` — `forge attest`
- `framework/ai-sdlc/aisdlc/adopt.py` — doctor INFO
- `framework/ai-sdlc/tests/test_forge_port.py` — attest scenarios
- ADR-002 / ADR-005 / FACTORY / README / METRICS
