# Substitutions: year (overlap) and notary filters on the list (#1546)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1546 |
| Use Case | CU59 – Consultar Suplencias; CU22 |
| Branch | `feat/1546_substitutions_filter` |
| Gate 1 status | draft |

## Objetivo

CU59 says the system asks for the year and lists the substitutions of that period. `/dashboard/substitutions` showed every substitution with no filter.

## What Changes

- `lib/substitution-filter.ts`: year overlap (a Dec→Jan span is in both years, read from the date text with no time-zone shift), notary match on either side (accent- and case-insensitive), year options (data plus the current year).
- Substitutions page: Año selector and Escribano search kept in the URL (`?year=&notary=`), a result count and a year empty state ('No hay suplencias para {year}'). Client-side; no API change.
- i18n `suplencias.filtros.*` and `noDataYear` (es/en).
- TS-0017: CU59-GW01 (filter) and CU59-GW02 (the edit dialog is the detail view) implemented, CU59-GW03 at 390px. The feature-gap skip inventory goes from 14 to 12 (test, E2E-TEST-MAPPING, living spec).
- Tests: Vitest `substitutions-filter.test.tsx` (7). CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A substitution belongs to every year its date range overlaps | CU59 steps 2–4 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `substitution-consult`: Consulting substitutions by year and notary.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | page, filter lib, i18n |
| `testing/e2e` | yes | TS-0017 |
| `testing/tests` | yes | skip inventory |

### Surface area

- Endpoints / entities / Flyway / configuration: none

### Architecture review

Frontend only; pure filter functions in `lib/` keep the page thin and unit-testable.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Added entry |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | CU59 skips resolved |
| `docs/openspec/specs/e2e-feature-gap-skip-tracking/spec.md` | count 12 |
