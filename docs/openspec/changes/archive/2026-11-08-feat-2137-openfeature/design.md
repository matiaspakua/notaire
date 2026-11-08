> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. Stub from #2125; lead-time #2138 landed.

## Goals / Non-Goals

**Goals:** Smallest real OpenFeature-shaped provider without an SDK dependency.
**Non-Goals:** SaaS flag console; OFREP; OTel hooks this slice.

## Decisions

Extend aisdlc/flags.py; no openfeature-sdk; JSON file only.

## Riesgos / Trade-offs

Agents inventing skip flags — required gate invariant closes that.

## Testing Strategy

test_openfeature_port.py; verify.sh; Notaire doctor

## Regression Strategy

test_feature_flags.py; test_factory.py PR ceiling

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
