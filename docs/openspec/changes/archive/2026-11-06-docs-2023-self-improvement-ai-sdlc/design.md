> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Owner: be a self-improvement AI SDLC agent overseeing the whole system.

## Goals / Non-Goals

**Goals:** ['P12 + §10 in CONSTITUTION', 'framework template Learning loop as principle', 'docs/tests in TDD gate + test', 'steward PREFLIGHT_SKIP note']
**Non-Goals:** ['Auto stamp-file for PREFLIGHT_SKIP', 'Closing unrelated epics']

## Decisions

['Extend framework §7 Learning loop into an explicit principle P11', 'Allow PREFLIGHT_SKIP only after a recorded green --full on the same tip in the same session']

## Riesgos / Trade-offs

['Constitution PRs need Owner merge']

## Testing Strategy

['test_pr_checks.py docs/tests TDD evidence case']

## Regression Strategy

['workspace/tests/test_pr_checks.py']

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
