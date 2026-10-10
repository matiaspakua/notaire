# Design — #1261 Owner decision packaging

## Approach

Documentation-only packaging. Amend ADR-022 with a clearly labeled **Pending** section (options A/B/C + measured size from `repo-metrics.py`). Link it from Pages Architecture and from `REPO-SPLIT-PLAN` P0.6. Guard with a small Python unit test so the packaging cannot regress silently.

## Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Choose A/B/C in this PR? | No | Owner-only; agents package the ask |
| New ADR vs amend ADR-022 | Amend ADR-022 | History purge deferral already lives there (#1050) |
| Delete `deprecated/` now? | No | Acceptance requires Owner choice first |

## Risks

- Owner never decides → pack stays ~13 MB heavier; mitigated by visible Pages + issue link.
- Someone merges a delete PR without ADR update → hygiene/guard tests and this ADR section are the tripwire (full delete still needs its own PR after the decision).
