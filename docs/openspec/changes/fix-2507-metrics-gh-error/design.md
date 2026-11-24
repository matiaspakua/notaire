> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The script builds metrics.json with printf; any non-numeric value breaks it.

## Goals / Non-Goals

**Goals:** Valid output on any API failure; offline tests.
**Non-Goals:** Changing the metrics.

## Decisions

D1 Validate with a regex; fall back to the live value (never a fabricated number when one was published) or 0 on a first deploy.
D2 Stub gh via PATH in the test runner, default answer 1305.

## Riesgos / Trade-offs

None.

## Testing Strategy

New test: stub gh prints an error object and exits 1; output parses and pullRequests equals the live value. All existing tests run with the stub.

## Regression Strategy

test_generate_github_page_metrics.py stays green offline.

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
