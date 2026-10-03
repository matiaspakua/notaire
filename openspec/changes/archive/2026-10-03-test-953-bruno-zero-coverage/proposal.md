# Add Bruno API coverage for zero-coverage controllers

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #953 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/test-953-bruno-zero-coverage-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal stockpile); implement after queue ahead of this pack |

## Objetivo

After the Bruno suite audit (#952, closed), sixteen REST controllers still have
**zero** Bruno folders under `backend-api/api-test/`. Contract/regression gaps
hide defects that unit tests miss. This change adds Bruno lifecycle coverage
(operations each controller actually exposes) with chai assertions, and updates
`COVERAGE.md` / `TEST-PLAN.md` / `CU-API-MATRIX.csv` so the gap list is empty
for those controllers.

## What Changes

- Add Bruno OpenCollection folders (YAML + chai `tests`) for each zero-coverage
  controller listed in #953 / `COVERAGE.md` TODO, mapping Spanish domain names
  to current English adapter controllers and `/api/v1/...` paths (see design).
- Cover full CRUD where the controller exposes it; for read-only / action /
  PDF / workflow-validate endpoints, cover the real operation set (not fake
  DELETE).
- Keep suites idempotent (fixtures + teardown) following #1035 patterns;
  `bru run . -r --env Development` must pass including new folders.
- Update `backend-api/api-test/COVERAGE.md`,
  `docs/300-development/303-testing/TEST-PLAN.md` (§7), and
  `CU-API-MATRIX.csv` Bruno columns; CHANGELOG for test-infra.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every listed zero-coverage controller MUST have Bruno coverage for the operations it exposes, with explicit chai assertions (not bare calls) | CU76; #953 AC | New (coverage obligation) |
| Bruno suites MUST be re-runnable without leaking rows (idempotent fixtures/teardown) | CU76; #1035 | Made explicit for new folders |
| Coverage docs (`COVERAGE.md`, TEST-PLAN §7, CU-API-MATRIX) MUST reflect actual Bruno status after the change | CU76; #952/#953 | Changed (close the TODO list for these 16) |

## Capabilities

### New Capabilities

- `bruno-zero-coverage-controllers`: Bruno contract coverage and documentation
  for the sixteen controllers previously listed as zero-coverage.

### Modified Capabilities

- (none under `openspec/specs/` for Bruno folder inventory; product behavior of
  those APIs is unchanged)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (tests only) | New `api-test/<folder>/` YAML; docs in `api-test/COVERAGE.md`; product code only if a real defect is found (track separately if out of scope) |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | Existing Bruno job consumes the suite |

### Surface area

- Entities: none required for coverage-only; if Bruno discovers a defect, fix
  under this issue only when trivial and necessary for green lifecycle — else
  open a follow-up Issue
- Endpoints: exercise existing REST for the 16 controllers (see design mapping)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (use Development Bruno env)
- Dependencies: none new (Bruno CLI already used)

### Architecture review

Follows established Bruno OpenCollection layout (`backend-api/api-test/README.md`,
#952/#1035). No ADR. Prefer English folder names consistent with recent
renames (`people`, `budgets`, …).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `backend-api/api-test/COVERAGE.md` | Move the 16 from TODO into Covered; refresh counts |
| `docs/300-development/303-testing/TEST-PLAN.md` | §7 Bruno coverage numbers / notes |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | Bruno_Test / Bruno_Status for affected rows |
| `CHANGELOG.md` | Test-infra: Bruno coverage for previously uncovered controllers |

## Out of Scope

- Re-auditing already-covered folders (#952 done)
- Product UI / Playwright
- **#1067** DAST / OpenAPI diff / backup-restore
- Large product refactors discovered by Bruno (file follow-up Issues)
