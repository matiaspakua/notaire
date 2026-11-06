> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Release Please closed multi_pr epic #579 from CHANGELOG issue links.

## Goals / Non-Goals

**Goals:** ['Document the Unreleased rule', 'Mechanical guard against closes #N in Unreleased']
**Non-Goals:** ['Ban all #N markdown links in Unreleased (too broad for this tip)']

## Decisions

['Guard `closes #<digits>` in Unreleased only; steward documents broader multi_pr hygiene']

## Riesgos / Trade-offs

['Agents may still use [#N](url) which RP can also close — steward text covers that']

## Testing Strategy

['docs/tests/test_changelog_structure.py — Unreleased must not contain closes #N']

## Regression Strategy

['Existing changelog structure tests']

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
