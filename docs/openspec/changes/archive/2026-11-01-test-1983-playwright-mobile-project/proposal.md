# Playwright mobile project runs real mobile specs

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1983 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `test/1983_playwright-mobile-project` |
| Gate 1 status | passed |

## Objetivo

The Playwright `mobile` project pointed at a non-existent `mobile-viewport.spec.ts`, so
iPhone SE never ran. Point it at real `*mobile*` specs, exclude them from `chromium`, and
guard zero-match projects.

## What Changes

- `playwright.config.ts`: `mobile` → `**/*mobile*.spec.ts`; chromium `testIgnore` for mobile
- `testing/tests/test_playwright_projects_nonempty.py` ratchet

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every declared Playwright project must match ≥1 test | #1983, CU76 | New |

## Capabilities

### New Capabilities

- `playwright-mobile-project`: mobile project + nonempty project guard

### Modified Capabilities

(none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `testing/e2e` | yes | playwright.config.ts |
| `testing/tests` | yes | guard |
| docs | yes | OpenSpec + CHANGELOG |

### Surface area

CI may run mobile specs under iPhone SE (previously only chromium viewport).

### Architecture review

None.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased |

## Out of Scope

Rewriting mobile specs; adding more devices.
