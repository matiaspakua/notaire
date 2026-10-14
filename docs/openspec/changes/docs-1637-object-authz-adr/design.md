> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

SR-11 is the highest remaining authorization risk and #1637 depends on #1638 (done). The implementation changes the schema and every service that serves an id, so it must not start before ownership is decided.

## Goals / Non-Goals

**Goals:** a decision-ready design the Owner can accept or reject in one reply.
**Non-Goals:** any implementation.

## Decisions

1. **Propose, do not decide.** Ownership is a product question; the ADR is `Proposed` and says so.
2. **Recommend a switchable option** so the upgrade is non-breaking for single-notary offices.

## Riesgos / Trade-offs

- [The Owner prefers another model] → the options section already compares the alternatives.

## Testing Strategy

Documentation only; the ADR index and docs suites run in CI.

## Regression Strategy

`python3 -m unittest discover -s docs/tests`.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
