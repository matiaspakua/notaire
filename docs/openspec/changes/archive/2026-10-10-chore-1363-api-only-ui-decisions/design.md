# Design

## Context

#1250 triaged the 53 allowlisted endpoints into buckets A-E; bucket E waited for an Owner call after #567, and the UI/UX audit (#1363, #1364) asked the remaining questions.

## Goals / Non-Goals

Goal: the allowlist states the decided classification. Non-goals: the testimony edit/delete UI and movement history panel (#1364 stays open for them).

## Decisions

Reuse bucket A (API-only) with the decision date and issue in the reason, instead of a new bucket. The guard looks for the `Owner to reclassify` marker so a future undecided entry is visible.

## Riesgos / Trade-offs

None at runtime.

## Testing Strategy

`test_owner_decisions_are_recorded` written first and observed failing on the bucket E entry.

## Regression Strategy

`bash contracts/verify.sh`.

## Playwright Strategy

Not applicable: no UI change.

## Deployment Strategy

Nothing to deploy: repository contract metadata only.

## Rollback Strategy

Revert the commit.
