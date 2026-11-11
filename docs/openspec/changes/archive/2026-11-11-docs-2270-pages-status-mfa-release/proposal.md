# Keep the public security and release story on main

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**.

| Field | Value |
|-------|-------|
| GitHub Issue | #2270 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/pages-status-audit-2277` |
| Gate 1 status | passed |

## Objetivo

A visitor reading the security posture and the release history sees the MFA that shipped in #1640 and the product version that is on main today.

## What Changes

- Identity / AT-1 / SR-17 copy matches ADR-032: TOTP MFA ships; default OPTIONAL; ENFORCE can require it for ADMIN and NOTARY.
- `github-page/lib/releases.json` is regenerated from the release-please manifest (0.4.1).
- Local `preflight.sh` runs `npm run build` for Pages so `prebuild` refreshes that file, matching `pages-ci.yml`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public claims match the repository | CONSTITUTION.md §7 | Made explicit |
| MFA is OPTIONAL by default after ADR-032 | ADR-032, #1640 | Made explicit |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-site-shell`: security Identity and the release snapshot match main.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| github-page | yes | Security copy, releases.json, tests |
| workspace | yes | preflight pages build uses npm run build |
| docs | yes | This change, then the archived spec |

No backend or schema change.

## Documentation Impact

- `CHANGELOG.md` — `[Unreleased]`.
- OpenSpec spec `github-pages-site-shell` when this change is archived.
