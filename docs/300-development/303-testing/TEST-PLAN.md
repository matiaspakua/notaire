# Test Plan — Notaire

The master testing document: what gets tested, at which level, how it maps
back to business Use Cases, and how results are reported. It is a process
document — the suite inventory, coverage numbers, and per-suite commands
live in [`README.md`](README.md) and are not repeated here.

## 1. Test levels

Every change flows through the same pyramid, enforced by
[`.claude/rules/ai-agent-workflow.md`](../../../.claude/rules/ai-agent-workflow.md)
Step 2 (TDD) and Step 5 (full suite before commit):

```
Unit → Integration → API (Bruno) → Frontend unit (Vitest) → E2E UI/UX (Playwright, per Use Case)
```

| Level | Validates | Suite inventory |
|-------|-----------|------------------|
| Unit | Single class/method in isolation (mocked dependencies) | [`README.md`](README.md) §"Suites de test" |
| Integration | Spring context + repository/service wiring, against H2 or real PostgreSQL | same |
| API (Bruno) | Real HTTP contract of every REST endpoint, requires running backend | same |
| Frontend unit (Vitest) | React components/hooks in isolation | same |
| E2E UI/UX (Playwright) | A full Use Case exercised through the actual browser UI against the running stack | same |

The deprecated Robot Framework Swing suite (`testing/e2e-swing/`) is excluded
from this plan — it targets `frontend-swing`, which per `CLAUDE.md` is not a
target for new work or new tests.

## 2. Use-case-oriented test catalog

Traceability from business Use Case → REST endpoint → automated test is
maintained in a single source of truth:
[`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) (88 rows, one per CU/entity/operation
combination), covering all 7 functional modules: Administración, Clientes,
Gestiones, Pagos, Presupuestos, Protocolos, Transversal. For a given Use
Case, that CSV answers: which controller/endpoint implements it, whether a
Bruno API test exists (`Bruno_Test`/`Bruno_Status` columns), and the GitHub
issue that delivered it.

Per Use Case, the expected automated coverage is:

1. **Unit tests** on the service/business-logic class(es) involved (see
   `backend-api/src/test/java/.../unit/`).
2. **Integration test** exercising the real repository/controller path (see
   `.../integration/`).
3. **Bruno request** under `backend-api/api-test/` proving the HTTP contract
   — tracked per-row in `CU-API-MATRIX.csv`.
4. **Playwright E2E spec** (`frontend/tests/e2e/cuNN-*.spec.ts`) driving the
   Use Case through the UI, for any Use Case with a UI-facing flow.

A Use Case is not "Done" per `CONSTITUTION.md` §3 Definition of Done until
all four applicable levels exist and pass — `CU-API-MATRIX.csv`'s
`Endpoint_Status`/`Bruno_Status` columns are the audit trail for that check.

### Test-data setup

Integration and E2E tests seed data against the schema documented in the
[Diccionario de Datos](../../200-architecture/205-data-model/Diccionario%20de%20Datos.md)
and [ERD](../../200-architecture/205-data-model/ERD/) — both reconciled
against Flyway `V1`–`V14`. Do not hand-craft SQL fixtures that diverge from
a current migration; H2-based unit/integration tests share the same Flyway
migrations as PostgreSQL (see
[`.claude/rules/database-migrations.md`](../../../.claude/rules/database-migrations.md)).

## 3. Test-case design guidelines

- **AAA pattern** (Arrange, Act, Assert), one behavior per test, descriptive
  `@DisplayName` — full conventions in
  [`.claude/rules/programming.md`](../../../.claude/rules/programming.md) §Testing.
  golden-path AND edge/error cases, per
  [`.claude/rules/ai-agent-workflow.md`](../../../.claude/rules/ai-agent-workflow.md) Step 2.
- **Frontend**: [`FRONTEND-TESTING-GUIDE.md`](FRONTEND-TESTING-GUIDE.md) for
  Vitest/Playwright conventions and the design-system-aware test patterns.
- **API**: [`api-test/README.md`](api-test/README.md) for Bruno request/
  assertion conventions.

## 4. Running the suites

Day-to-day commands (per-suite and full local replica of CI) live in
[`README.md`](README.md) §"Comandos" — the canonical single command that
mirrors every CI gate, including the full E2E/API suites, is:

```bash
bash scripts/preflight.sh --full
```

## 5. Coverage

Enforced ratchet floor vs. long-term target, and current actuals, are
tracked in one place:
[`.claude/rules/code-quality.md`](../../../.claude/rules/code-quality.md) —
not duplicated here. `README.md` §"Cobertura" mirrors the current snapshot.

## 6. Test reporting

| Report | Source | Where |
|--------|--------|-------|
| Backend coverage (JaCoCo) | `mvn jacoco:report -pl backend-api` | `backend-api/target/site/jacoco/index.html` |
| Frontend coverage (Vitest) | `npm run test:coverage` | `frontend/coverage/index.html` |
| E2E results (Playwright) | `npm run test:e2e` | `frontend/playwright-report/index.html` |
| API test results (Bruno) | `bru run . -r` | `backend-api/api-test/COVERAGE.md` |
| Aggregated CI dashboard | GitHub Actions | [`test-coverage-report.yml`](../../../.github/workflows/test-coverage-report.yml) |
| PR-level coverage comment | madrapps/jacoco-action | posted directly on the PR |

Every PR must show all of the above green before merge — see
[`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md) for the exact local-check ↔ CI-job
mapping, and `CONSTITUTION.md` §6 Quality Gates for the policy this
enforces.

## Navigation

- [← Testing](README.md)
- [Development Plan](../DEVELOPMENT-PLAN.md)
- [CU-API-MATRIX.csv](CU-API-MATRIX.csv)
- [Data Model](../../200-architecture/205-data-model/)
