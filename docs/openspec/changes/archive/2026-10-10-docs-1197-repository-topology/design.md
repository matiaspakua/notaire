> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #1197, Use Case CU76. The proposal predates or ignores facts in the repository (the #1190 Owner decisions, the measured CI profile, the cross-area change ratio). The decision is the Owner's; this change supplies evidence and a safer plan.

## Goals / Non-Goals

**Goals:** One measured ADR and one executable plan; child issues for the work that is useful under every option.
**Non-Goals:** Executing any extraction; changing CI, code or the Constitution (each is a child issue or a gated phase).

## Decisions

1. The ADR is Proposed, not Accepted: the Owner decides between the staged plan and the original eight-repository option. Rejected: accepting on the Owner's behalf.
2. Phase 0 items are independent of the decision so value is not blocked. Rejected: bundling them into the split.
3. Every claim in the critique cites a measurement or a file. Rejected: opinion-only review.

## Riesgos / Trade-offs

- Measurements are a snapshot (`main` @ `610fa7b0`); the plan makes them reproducible through the metrics issue.

- The Spanish-text and unreachable-endpoint measurements are heuristics; the report and issues state the method and require confirmation before work.

- Description mapping from retired names is done once by position and type and reviewed by hand; a wrong mapping would be a documentation error, not a runtime one.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Documentation links stay valid | static | `docs/tests/test_docs_links.py` |
| Business documentation traceability stays valid | static | `docs/tests/test_business_docs_traceability.py` |

- New unit tests (`src/test/java/.../unit/`): n/a
- New integration tests: n/a
- Coverage impact: none

## Regression Strategy

- Existing tests affected: none
- Full suite command: `bash workspace/sdlc/preflight.sh`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit

## Rollback Strategy

- Revert the PR.
