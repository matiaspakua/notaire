# Design — Product section and architecture internals

## Context

Before this change the Product section was one long page (`/`) of full-bleed sections with no sidebar, while the
Technical docs and the AI Factory used `DocShell`. The Technical docs had no page about the stack, the hexagon,
Flyway, the OpenAPI contract or Bruno, and the Architecture page listed five hand-picked ADRs (one with a stale
status).

## Decisions

- **Four Product pages.** `/product/` (what Notaire does), `/product/story/` (origin, stack evolution, the AI era),
  `/product/platform/` (three tiers and the infrastructure), `/product/numbers/` (versions, coverage, statistics).
  `PRODUCT_GROUPS` in `lib/site.ts` drives the sidebar, the drawer, breadcrumbs, previous/next and search, as for the
  other sections. `/` keeps the hero and opens the three sections; it belongs to no section (`sectionOf("/")` is
  `home`).
- **Reuse the story components.** They render inside `DocShell` with a `product-body` class that trims their
  full-bleed padding and backgrounds, so no content is rewritten twice.
- **Facts at build time.** `lib/tech-facts.ts` reads `pom.xml`, `package.json`, the compose files, the migration
  folder, `application.properties`, `openapi.yaml`, the Bruno collection, the hexagon packages, the ArchUnit baseline,
  the workflows and the ADR folder. A value that cannot be read prints "—".
- **Citations are tested.** Every `src("…")` path on the new pages must exist (`test_pages_tech_docs.py`).

## Riesgos / Trade-offs

- Old `/#origin`-style anchors no longer resolve; the landing links the new pages, and the sections are still
  reachable from search.

## Rollback

Revert the PR; the site is static and has no data.
