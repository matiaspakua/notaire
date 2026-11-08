> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2121. Follows #2123 Spec Kit.

## Goals / Non-Goals

**Goals:** Written phase boundary; no auto-trust; unavailable plugin.
**Non-Goals:** Install aidlc; dual orchestration; paid AWS.

## Decisions

Boundary status without a dry-plan command; factory-wide reject of auto_trust_hooks=true.

## Riesgos / Trade-offs

Owners may confuse boundary with a ready adapter; status=boundary and DESIGN.md mitigate.

## Testing Strategy

test_aws_ai_dlc_boundary.py, test_aws_ai_dlc_adoption.py, verify.sh

## Regression Strategy

OpenSpec and Spec Kit plugins unchanged

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
