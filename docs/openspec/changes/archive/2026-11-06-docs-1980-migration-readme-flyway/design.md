> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Found during #1976 technology assessment; schema is well past V11.

## Goals / Non-Goals

**Goals:** ['No V1→V11+ in the three paths', 'README conventions + R-scripts + Pages pointer', 'Docs unit test guard']
**Non-Goals:** ['Editing historical CHANGELOG release notes', 'Changing flyway.* property values']

## Decisions

['Point to Pages /docs/database/ instead of maintaining a version table', 'Keep devops-engineer.md for a follow-on unless trivial']

## Riesgos / Trade-offs

['Agents may still see stale wording in archived CHANGELOG — out of scope']

## Testing Strategy

['test_flyway_docs_no_stale_version_range.py']

## Regression Strategy

['docs/tests suite']

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
