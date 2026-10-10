# Design — Owner decision packaging for deprecated/ (#1261)

## Context

#1197 P0.6 / issue #1261 requires an Owner choice on `deprecated/` (~13.4 MB,
~767 files) and the history rewrite deferred by ADR-022 Decision §2. Agents must
not invent a deletion or `git filter-repo` run. This change only packages the
decision request so it is discoverable and guarded.

## Goals / Non-Goals

**Goals:** ADR-022 lists Option A/B/C under a Pending Owner decision; Pages
Architecture links ADR-022; a unit test prevents silent regression; REPO-SPLIT-PLAN
P0.6 points at the ADR section.

**Non-Goals:** Choosing A/B/C; deleting `deprecated/`; rewriting git history;
closing #1261 before the Owner decides; changing ADR-024 topology status.

## Decisions

| Decision | Choice | Alternative | Why alt lost |
|----------|--------|-------------|--------------|
| New ADR vs amend ADR-022 | Amend ADR-022 | New ADR-028 | Purge deferral already lives in ADR-022 |
| Choose option in this PR | No — package only | Pick Option A | Owner-only; Constitution |
| Close #1261 in PR | No — `Refs` only | `Closes #1261` | Acceptance needs Owner choice recorded |

## Riesgos / Trade-offs

| Risk | Mitigation |
|------|------------|
| Owner never decides | Visible Pages + issue + plan row; pack cost stays explicit |
| Agent deletes `deprecated/` anyway | ADR language + hygiene tests; this pack is the tripwire |
| Closed OpenSpec changes fail SDLC plan | Archive closed changes in same PR when needed |

## Testing Strategy

| Scenario | Level | Verification |
|----------|-------|--------------|
| ADR-022 pending #1261 options | Guard | `test_adr022_owner_decision_pack.py` |
| Pages links ADR-022 | Guard | same unit test |
| OpenSpec structural | Script | `openspec validate --strict` |

## Regression Strategy

`validate-sdlc-plan.sh`, Process Checks unit discovery under `workspace/tests`.

## Playwright Strategy

n/a — documentation / Pages static content only (no product UI workflow).

## Deployment Strategy

Docs + GitHub Pages source; Pages deploy follows existing `github-page` workflow on merge.

## Rollback Strategy

Revert PR; ADR-022 Pending section and Pages link disappear; #1261 remains open.
