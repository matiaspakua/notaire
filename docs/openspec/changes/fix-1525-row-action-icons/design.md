# Design

## Context

NotaireIcon wraps next/image for the legacy PNG action icons. Lucide svg icons are not affected (the button sets [&_svg]:shrink-0 and svgs get no max-width).

## Goals / Non-Goals

Goals: every NotaireIcon visible at its size on every page and viewport, fixed in one place. Non-goals: migrating the 6 pages to size="icon-sm" or replacing the PNG icons.

## Decisions

Fix the component, not the 6 call sites: with max-w-none the image's min-content size is its real size, so flex layout keeps the button at least icon + padding wide. Two 16px icons with px-3 and gap-2 (88px) fit the w-24 column, so no column width changes.

## Risks / Trade-offs

A caller that relied on the image shrinking (none found among the 9 files using NotaireIcon) would now overflow; the admin index cards use larger fixed sizes and are unchanged.

## Testing Strategy

Written first: Vitest guard (1 failure) and TS-0140 (3 desktop failures, 3 mobile passes) on the pre-change build.

## Regression Strategy

`bash frontend/verify.sh`; full chromium suite.

## Playwright Strategy

`TS-0140` row action icons have a real size on 3 admin pages at desktop and mobile widths.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
