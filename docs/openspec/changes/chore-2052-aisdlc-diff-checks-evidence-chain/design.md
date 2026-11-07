> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Three sibling repositories enforce what Notaire states in prose: agentic-os (agents cannot change their own rules, C14-004; changed paths compared with the write set, C15-006; hash-chained journal), gentle-ai (400-line review budget) and factory-control (`protectedPaths`). A web challenge showed a hash chain alone cannot see a truncated tail (IETF draft-melegassi-opsawg-mvps-logging), hence the anchor.

## Goals / Non-Goals

**Goals:** mechanical checks any adopter configures in its profile; zero dependencies; read-only git.
**Non-Goals:** Notaire's own configuration (PR 3); signing evidence; a server-side ledger.

## Decisions

- One module, `diffchecks.py`, for the three diff checks: they share the base/diff plumbing.
- `scope` is three-state (pass, fail, not evaluable) so an absent write set is never a silent pass.
- The chain covers the last line only through an external anchor; the head digest is printed for the PR body.
- Legacy unchained records are accepted only as a prefix.

## Riesgos / Trade-offs

- Advisory gates are ignored: mitigated by `--strict` once proposals carry write sets.
- The 400-line default is a vendor-reported range (SmartBear/Cisco 200–400 LOC); configurable.

## Testing Strategy

Black-box CLI tests in throwaway git repositories (`test_diffchecks.py`, `test_evidence_chain.py`), 22 new tests; 18 of the first 20 observed failing on the previous kit.

## Regression Strategy

The full kit suite (`python3 -m unittest discover -s framework/ai-sdlc/tests`) and `workspace/tests` run in `preflight.sh`; existing commands keep their outputs (`report` reads chained records).

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release; no runtime component.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
