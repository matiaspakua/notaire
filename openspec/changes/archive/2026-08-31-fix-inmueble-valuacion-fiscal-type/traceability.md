# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

This is the change's ledger, created during planning; as upstream links get
filled in, the completed change moves through the gates. Rows below Tasks stay
`pending` until the corresponding step actually happens — never pre-fill them.

## Chain

```text
Issue #879 → proposal.md → spec.md → design.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #879 | closed |
| Use Case | CU69 – Gestión de Inmuebles | exists |
| Specification | `openspec/changes/fix-inmueble-valuacion-fiscal-type/` | done |
| Branch | `fix/879_inmueble-valuacion-fiscal-type` | merged, deleted |
| Tasks | `tasks.md` | all complete |
| Commits | `0200996`, `a9d4879`, `606a59c`, `4bd075b`, `c6c7ef9`, `cf9dcf9` | done |
| Pull Request | #882 | merged (squash) |
| CI run | all 25 checks green (Unit, Integration, Coverage Gate, Security Scan, Playwright E2E Full Suite, Bruno API Tests, etc.) | passed |
| Merge commit | `7563215` | done |
| Release / tag | none planned — continuous deploy off `main` | n/a |
| Smoke test | `POST /api/v1/inmueble` with `valuacionFiscal: 150000.5` against the running dev stack (rebuilt Docker image off `main`) → `201 Created`, numeric value round-tripped correctly | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create Inmueble with a numeric valuación fiscal | `InmuebleValuacionFiscalPgIntegrationTest#shouldCreateInmuebleWithNumericValuacionFiscal` | passing |
| Create Inmueble without a valuación fiscal | `InmuebleValuacionFiscalPgIntegrationTest#shouldCreateInmuebleWithNullValuacionFiscal` | passing |

An "update" scenario was deliberately dropped from scope after TDD surfaced an
unrelated, pre-existing NPE in `InmuebleJpaController.edit()` (`tramiteList`
null on any PUT) — tracked as Issue #880, out of scope here (see design.md).

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU69 – Gestión de Inmuebles.md` | n/a — no data type documented there | — |
| `CHANGELOG.md` | yes | `c6c7ef9` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #879, `proposal.md`, `specs/inmueble-valuacion-fiscal/spec.md` (2 Given/When/Then scenarios) |
| 2 | Failing tests written, test cases designed | yes | `InmuebleValuacionFiscalPgIntegrationTest` written first, observed failing against Postgres (`ERROR: column "valuacion_fiscal" is of type real but expression is of type character varying`) before implementation |
| 3 | Suite green, coverage held, docs updated | yes | 1611/1611 unit/integration tests, `mvn verify` BUILD SUCCESS (Checkstyle/SpotBugs/JaCoCo ratchet held), `bash testing/scripts/test.sh` green, 5/5 `cu69-inmuebles-valuacion-fiscal.spec.ts` Playwright tests, `bash scripts/preflight.sh --fix` 16 passed / 2 skipped |
| 4 | CI green, review approved, no conflicts | yes | PR #882, all 25 checks green, `MERGEABLE`/`CLEAN`, code-owner merge = approval per Constitution §5 step 20 |
| 5 | Deployed, smoke test passed, Issue closed | yes | `main` fast-forwarded to `7563215`, Docker backend rebuilt from `main`, smoke test `201 Created`, Issue #879 closed |

## Exceptions

None taken.
