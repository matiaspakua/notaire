# Track intentional E2E feature-gap skips (TS-0014/16/17/20)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1146 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/test-1146-e2e-feature-gap-skips-69d3` |
| Gate 1 status | passed |

## Objetivo

After #1066 cited intentional Playwright `test.skip` gaps with #1146, the
permanent E2E mapping still under-counts those skips and does not list each
owning CU. This change makes #1146 tracker hygiene: a static citation check,
an accurate inventory, and clear ownership so product gaps stay on domain CUs.

## What Changes

- Keep (and tighten) a Vitest hygiene check that every static `test.skip` in
  TS-0014, TS-0016, TS-0017, and TS-0020 cites a `#\d+` issue (tracker #1146).
- Sync `E2E-TEST-MAPPING.md` skipped-test count and table to the live inventory
  of **14** static skips (CU21 already unskipped via #1057 — not in inventory).
- Document owning CU + product-tracking responsibility per skip; #1146 does not
  implement product UI.
- Point CU76 / CHANGELOG at this tracker hygiene work.
- OpenSpec change folder for Gate 1.

No product UI, no unskip without real assertions, no `local-ai/` changes.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Intentional feature-gap `test.skip` in TS-0014/16/17/20 MUST cite an open `#issue` | CU76 (Skips intencionales); #1146 AC | Made explicit (hygiene check) |
| Permanent E2E mapping MUST match the live static skip inventory for those suites | CU76; E2E-TEST-MAPPING policy | Changed (fix under-count) |
| Product gaps remain owned by domain CUs; #1146 is tracker hygiene only | #1146; owning CUs (CU15/23/24/…) | Made explicit |

## Capabilities

### New Capabilities

- `e2e-feature-gap-skip-tracking`: Static citation hygiene and accurate inventory
  for intentional feature-gap skips in TS-0014/16/17/20 (#1146 / CU76).

### Modified Capabilities

None under `openspec/specs/` today cover this tracker inventory.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | yes | Vitest unit hygiene check under `src/tests/unit/` |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing frontend unit CI covers Vitest |
| Docs | yes | E2E-TEST-MAPPING, CU76 pointer, CHANGELOG |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows the #1066 static Gate 2 pattern (`e2e-test-reliability.test.ts`). No ADR.
Does not ship product UI behind the gaps.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Sync skip count/table to 14 live skips; owning CU + product tracking |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | GitHub ID / pointer for #1146 tracker hygiene |
| `CHANGELOG.md` | Engineering note under `[Unreleased]` for tracker hygiene |

## Out of Scope

- Implementing missing product UI (detail/filter/search/plantillas/etc.) — stays
  on owning CUs
- Unskipping any of the 14 scenarios without real UI assertions
- Changing Playwright runtime behavior or CI retry policy (#1066)
- `local-ai/`
