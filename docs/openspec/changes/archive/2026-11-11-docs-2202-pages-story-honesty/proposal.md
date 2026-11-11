# Keep the product story honest

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**.

| Field | Value |
|-------|-------|
| GitHub Issue | #2202 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/pages-story-honesty-2277` |
| Gate 1 status | passed |

## Objetivo

The product story and the numbers page stop publishing a share of AI work the repository never measured, and they stop saying the legacy JPA package is still being migrated.

## What Changes

- The AI tools section lists roles. It does not draw a percentage bar.
- The numbers page says `JpaAdministrator` and `com.licensis.notaire.jpa` were retired (#576), and that Flyway is the schema source.
- The AI-era path is Issue, OpenSpec, TDD, `preflight.sh --full`, the merge rule, and a version tag. Cards are visible without a fade-in.
- The release-history lead has a Spanish sentence.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public numbers come from a measurement. Nothing is invented. | CONSTITUTION.md §7 | Made explicit |
| Flyway is the schema source. The legacy JPA package is retired. | #576 | Made explicit |
| English technical terms stay in English | #2202 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-site-shell`: the product story does not invent a share of AI work, and it does not say the retired JPA package is still being migrated.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| github-page | yes | Product story, numbers challenges, release lead, tests |
| docs | yes | This change, then the archived spec |

No backend or schema change.

## Documentation Impact

- `CHANGELOG.md` — `[Unreleased]`.
- OpenSpec spec `github-pages-site-shell` when this change is archived.
