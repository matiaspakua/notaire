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
| Issue | #879 | in-progress |
| Use Case | CU69 – Gestión de Inmuebles | exists |
| Specification | `openspec/changes/fix-inmueble-valuacion-fiscal-type/` | done |
| Branch | `fix/879_inmueble-valuacion-fiscal-type` | created |
| Tasks | `tasks.md` | Gate 1-3 complete |
| Commits | `0200996`, `a9d4879`, `606a59c`, `4bd075b`, `c6c7ef9` | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | none planned — continuous deploy off `main` | n/a |
| Smoke test | — | pending |

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
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None taken.
