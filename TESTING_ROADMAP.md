# Notaire Testing Roadmap: 100% Code Coverage + Functional Coverage

## Current Status (2026-06-15 - UPDATED)

### Code Coverage
- **Overall**: 31% line / 18% branch
- **Total Tests**: 812 tests passing (1 skipped)
- **Latest improvements**:
  - PagoServiceIntegrationTest: 12 → 24 tests (coverage branches for editarPago, procesarPago edge cases)
  - PersonaServiceIntegrationTest: 10 → 19 tests (coverage for search filters and updates)

### Coverage by Package
| Package | Line | Branch | Status |
|---------|------|--------|--------|
| com.licensis.notaire.service | 95% | 84% | ✅ Strong |
| com.licensis.notaire.api | 85% | 75% | ✅ Good |
| com.licensis.notaire.dto | 100% | 100% | ✅ Complete |
| com.licensis.notaire.exception | 100% | - | ✅ Complete |
| com.licensis.notaire.security | 100% | 100% | ✅ Complete |
| com.licensis.notaire.audit | 96% | 80% | ✅ Strong |
| com.licensis.notaire.observability | 91% | 75% | ✅ Good |
| com.licensis.notaire.config | 80% | 56% | 🟡 Fair |
| com.licensis.notaire.jpa | 1% | 0% | ⚠️ Legacy |
| com.licensis.notaire.servicios | 17% | 1% | ⚠️ Old |

## Test Pyramid (Completed)

### Phase 1: Unit Tests ✅ COMPLETE (730+ tests)
All service unit tests with comprehensive mocking:
- PersonaService (17 tests)
- PresupuestoService (17 tests)
- EscrituraService (12 tests)
- RegistroAuditoriaService (18 tests)
- PagoService (28 tests)
- GestionQueryService (9 tests)
- WorkflowValidationService (11 tests)
- Plus 600+ existing unit tests from entity, DTO, exception, and other components

**Coverage**: Business logic validation, exception handling, edge cases, 100% for DTO/Exception/Security packages

### Phase 2: Repository Integration Tests ✅ COMPLETE (47 tests)
- PersonaRepositoryIntegrationTest (7 tests)
- PresupuestoRepositoryIntegrationTest (9 tests)
- PagoRepositoryIntegrationTest (9 tests)
- RegistroAuditoriaRepositoryIntegrationTest (11 tests)
- GestionDeEscrituraRepositoryIntegrationTest (11 tests)

**Coverage**: H2 in-memory database persistence, JPA relationships, query methods

### Phase 3: Service Integration Tests ✅ COMPLETE (43 tests)
- PersonaServiceIntegrationTest (19 tests) - covers CRUD, search filters, updates, edge cases
- PagoServiceIntegrationTest (24 tests) - covers payment processing, saldo calculation, edits, deletions

**Coverage**: Real database transactions, service orchestration, transaction consistency, branch coverage for edge cases

### Phase 4: API Integration Testing (NEXT)
**Tools**: Bruno CLI (REST API testing)
**Target**: 100% HTTP endpoint coverage
**Location**: `/backend-api/api-test/`

**Collections to Test**:
- auth/ (login, logout)
- personas/ (CRUD + search)
- presupuestos/ (CRUD + filtering)
- pagos/ (CRUD + calculations)
- gestiones/ (workflow management)
- registros/ (audit log)
- escrituras/ (document management)
- etc.

### Phase 5: E2E UI Testing (NEXT)
**Tools**: Playwright
**Target**: Complete user workflows
**Location**: `frontend/tests/`

**Test Scenarios**:
- User authentication (login/logout)
- Presupuesto creation workflow
- Pago processing workflow
- Document management workflows
- Audit log verification

### Phase 6: Functional Requirement Coverage
**Target**: Every acceptance criterion tested
**Approach**: Map each use case (CU01-CU68) to test cases

## Implementation Strategy

### Active Code Packages (Focus Area)
```
service/           → 95% coverage (add remaining 5% gaps)
api/               → 85% coverage (add remaining 15% gaps)
repository/        → Inherit from service coverage
dto/               → 100% ✅
exception/         → 100% ✅
audit/             → 96% (close to complete)
security/          → 100% ✅
observability/     → 91% (minor gaps)
config/            → 80% (configuration coverage)
```

### Legacy Code Packages (Lower Priority)
```
jpa/               → 1% (legacy controllers, being phased out)
servicios/         → 17% (old service implementations)
jpa.exceptions/    → 0% (unused exception classes)
```

## Next Steps (Priority Order)

### IMMEDIATE (API Testing with Bruno)
```bash
# Run all Bruno collections
cd /backend-api/api-test
bru run --collection .

# Run specific collection
bru run --collection ./personas --env environments/local.json

# Run with coverage reporting
bru run --collection . --reporter cli --output test-results.json
```

### THEN (E2E Testing with Playwright)
```bash
cd /frontend
npx playwright install
npx playwright test

# Generate coverage report
npx playwright test --reporter=html
```

### FINALLY (Coverage Gaps)
- Identify remaining gaps via code coverage diff
- Add targeted unit/integration tests for uncovered branches
- Enforce 90% floor for new code via JaCoCo

## Success Criteria

- ✅ 791 tests passing (0 failures)
- ✅ 31% overall code coverage baseline
- ⏳ 100% API endpoint coverage (Bruno tests)
- ⏳ 100% user workflow coverage (Playwright)
- ⏳ 90%+ coverage for active packages (service, api, audit)
- ⏳ 100% acceptance criteria verification

## Test Execution

### Run All Tests
```bash
mvn clean verify -pl backend-api
```

### Code Coverage Report
```bash
mvn jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
```

### API Testing
```bash
cd backend-api/api-test
bru run --collection .
```

### E2E Testing
```bash
cd frontend
npx playwright test
```

## Documentation
- Unit test examples: See Phase 1 tests in `/backend-api/src/test/java/.../unit/`
- Integration tests: See Phase 2-3 tests in `/backend-api/src/test/java/.../integration/`
- API collections: `/backend-api/api-test/`
- E2E tests: `/frontend/tests/`

---

**Last Updated**: 2026-06-15
**Total Tests**: 791
**Coverage**: 31% line / 18% branch
**Strategy**: Unit → Integration → API → E2E → Coverage Optimization
