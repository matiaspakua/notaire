# Notaire Testing Guide

Complete testing infrastructure and methodology for the Notaire Spring Boot microservices project.

## Quick Start

### Run All Tests
```bash
mvn test -pl backend-api
```

### Generate Coverage Report
```bash
mvn jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
```

### Run Comprehensive Test Suite (All Phases)
```bash
bash scripts/run-comprehensive-tests.sh
```

## Testing Phases

### Phase 1: Unit Tests ✅
**Status**: Complete (730+ tests)

Unit tests using Mockito for isolated business logic testing.

```bash
mvn test -pl backend-api -Dtest="**/unit/*"
```

**Key Test Files**:
- `src/test/java/.../unit/PersonaServiceTest.java`
- `src/test/java/.../unit/PagoServiceTest.java`
- `src/test/java/.../unit/EscrituraServiceTest.java`
- `src/test/java/.../unit/RegistroAuditoriaServiceTest.java`
- And 30+ more unit test files

**Coverage**:
- Service layer: 95% line / 86% branch
- DTO layer: 100% line / 100% branch
- Exception layer: 100% line
- Security layer: 100% line / 100% branch

### Phase 2: Repository Integration Tests ✅
**Status**: Complete (47 tests)

Integration tests using H2 in-memory database for data access layer testing.

```bash
mvn test -pl backend-api -Dtest="**/integration/*Repository*"
```

**Test Classes**:
1. `PersonaRepositoryIntegrationTest.java` (7 tests)
   - CRUD operations
   - Search by filters
   - Transaction rollback scenarios

2. `PresupuestoRepositoryIntegrationTest.java` (9 tests)
   - Pagination
   - Estado filtering
   - Referential integrity

3. `PagoRepositoryIntegrationTest.java` (9 tests)
   - Payment persistence
   - Aggregation queries (sum)
   - Date range queries

4. `RegistroAuditoriaRepositoryIntegrationTest.java` (11 tests)
   - Audit record persistence
   - Usuario relationships
   - FETCH JOIN queries

5. `GestionDeEscrituraRepositoryIntegrationTest.java` (11 tests)
   - Gestion CRUD
   - State and escribano filtering
   - Complex queries

**Setup**: Tests use `@SpringBootTest` with `test-h2` profile for isolated H2 database.

### Phase 3: Service Integration Tests ✅
**Status**: Complete (43 tests) - **ENHANCED June 15, 2026**

Integration tests using real database transactions to verify business logic and service orchestration.

```bash
mvn test -pl backend-api -Dtest="**/integration/*Service*"
```

**Test Classes**:

1. **PersonaServiceIntegrationTest.java** (19 tests)
   - Save and retrieve personas
   - Search with filters:
     - By nombre
     - By apellido
     - By numero_identificacion
     - By tipo_identificacion
     - By esCliente
     - Combinations of filters
   - Update operations
   - Delete and verify
   - Transaction consistency

2. **PagoServiceIntegrationTest.java** (24 tests)
   - Process payments
   - Calculate saldo pendiente (balance)
   - Multiple payment handling
   - Edit payments with null/invalid values
   - Validate payment amounts
   - Search by date range
   - Transaction consistency
   - **Recent Additions** (June 15):
     - editarPago with null parameters
     - Negative monto validation
     - Multiple simultaneous payments
     - Full payment verification

**Setup**: Tests use `@SpringBootTest` with `test-h2` profile and `@Transactional` for automatic rollback.

### Phase 4: API Integration Testing (Bruno CLI) 🔄
**Status**: Ready (104 test files)

Comprehensive API endpoint testing using Bruno CLI.

**Requirements**:
- Docker running with backend and PostgreSQL
- `bash scripts/start.sh`
- Bruno CLI: `brew install bruno`

**Run Tests**:
```bash
cd backend-api/api-test
bru run . -r --env Development
```

**Test Coverage** (104 YAML/OpenCollection files):
- `auth/` - Login/logout, token refresh
- `conceptos/` - Concept CRUD
- `documentos/` - Document management
- `escrituras/` - Legal document handling
- `estado-gestion/` - Status management
- `folios/` - Folio operations
- `gestiones/` - Case management
- `inmuebles/` - Property management
- `pagos/` - Payment processing
- `personas/` - Person/contact CRUD
- `plantillas/` - Template management
- `presupuestos/` - Budget CRUD
- `reportes/` - Report generation
- `usuarios/` - User management
- `workflow/` - Workflow orchestration

**Structure**: Each resource folder contains:
- `01-create.yml` - POST create
- `02-list.yml` - GET all
- `03-get-by-id.yml` - GET by ID
- `04-search.yml` - GET with filters (if applicable)
- `05-update.yml` - PUT/PATCH update
- `06-delete.yml` - DELETE
- `07-verify-delete.yml` - Verify deletion

### Phase 5: E2E UI Testing (Playwright) 🔄
**Status**: Ready (28 test files)

End-to-end user workflow testing using Playwright.

**Requirements**:
- Frontend dev server running
- Node.js/npm installed
- Playwright installed: `npm install @playwright/test`

**Run Tests**:
```bash
cd frontend
npx playwright test
```

**Test Coverage** (28 .spec.ts files):
- CU01: Presupuesto workflow
- CU03-04: Document management
- CU05: Escritura handling
- CU17-18: Persona management
- CU24-40: Report generation
- CU70: Workflow editor UI
- CU73: Workflow assignment
- CRUD operations for all main entities
- Gestión workflow tests

**Report**:
```bash
npx playwright show-report
```

### Phase 6: Coverage Gap Analysis 📊
**Status**: Ongoing

Identify and fill remaining code coverage gaps.

**Run Analysis**:
```bash
mvn test jacoco:report -pl backend-api
```

**View Report**:
```bash
open backend-api/target/site/jacoco/index.html
```

**Target Coverage**:
- Service layer: 95%+ (currently 95%)
- API layer: 90%+ (currently 85%)
- Audit layer: 95%+ (currently 96%)
- Config layer: 85%+ (currently 80%)
- Overall: 40%+ (currently 31% - limited by legacy packages)

## Test Architecture

### Test Pyramid

```
        ┌─────────────────────────┐
        │  E2E UI Tests (Phase 5) │
        │    28 Playwright Tests  │
        └──────────────┬──────────┘
                       │
        ┌──────────────┴──────────────┐
        │  API Tests (Phase 4)        │
        │  104 Bruno CLI Collections  │
        └──────────────┬──────────────┘
                       │
        ┌──────────────┴──────────────┐
        │  Service Integration (Ph3)  │
        │       43 Tests + H2 DB      │
        └──────────────┬──────────────┘
                       │
        ┌──────────────┴──────────────┐
        │ Repository Integration (Ph2)│
        │       47 Tests + H2 DB      │
        └──────────────┬──────────────┘
                       │
        ┌──────────────┴──────────────┐
        │    Unit Tests (Phase 1)     │
        │      730+ Mocked Tests      │
        └─────────────────────────────┘
```

### Test Data Management

**H2 In-Memory Database**:
- Used for Phase 2 and Phase 3 tests
- Automatically created from entity annotations
- Fast execution (no Docker required)
- Each test is transactional and rolls back
- Located in test-h2 Spring profile

**Setup Pattern**:
```java
@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
public class MyServiceIntegrationTest {
    @BeforeEach
    void setUp() {
        // Create test data
        testData = repository.save(new Entity());
    }
    
    @Test
    void testBusinessLogic() {
        // Test with real service/transaction
    }
    // Transaction automatically rolls back after test
}
```

### Assertion Patterns

All tests use **AssertJ** for fluent assertions:

```java
assertThat(result)
    .isNotNull()
    .hasFieldOrPropertyWithValue("id", 1)
    .extracting("name")
    .isEqualTo("Test Name");
```

### Mocking Strategy

Unit tests use **Mockito**:

```java
@Mock
private PersonaRepository repository;

@InjectMocks
private PersonaService service;

@Test
void testWithMock() {
    when(repository.findById(1))
        .thenReturn(Optional.of(persona));
    
    var result = service.findById(1);
    
    assertThat(result).isPresent();
}
```

## Code Coverage Reports

### JaCoCo Coverage Report
- **Location**: `backend-api/target/site/jacoco/index.html`
- **Metrics**: Line and branch coverage
- **Tools**: JaCoCo 0.8.15

### By Package
| Package | Line | Branch |
|---------|------|--------|
| com.licensis.notaire.service | 95% | 86% |
| com.licensis.notaire.api | 85% | 75% |
| com.licensis.notaire.audit | 96% | 80% |
| com.licensis.notaire.dto | 100% | 100% |
| com.licensis.notaire.exception | 100% | - |
| com.licensis.notaire.security | 100% | 100% |
| com.licensis.notaire.observability | 91% | 75% |
| com.licensis.notaire.config | 80% | 56% |

## Best Practices

### When Writing Tests

1. **Name Clearly**: `shouldReturnUserWhenValidIdProvided()`
2. **Use AAA Pattern**: Arrange → Act → Assert
3. **Test Behavior**: Not implementation details
4. **One Assertion**: When possible (or assertions about one concept)
5. **Meaningful Data**: Not random values
6. **Isolate Tests**: No dependencies between tests
7. **Keep Fast**: Unit tests < 100ms, integration tests < 1s

### Example Test

```java
@Test
@DisplayName("Should save persona with valid data")
void shouldSavePersonaWithValidData() {
    // Arrange
    Persona persona = new Persona();
    persona.setNombre("Juan");
    persona.setApellido("Pérez");
    
    // Act
    Persona saved = personaService.save(persona);
    
    // Assert
    assertThat(saved)
        .isNotNull()
        .hasFieldOrPropertyWithValue("nombre", "Juan")
        .hasFieldOrPropertyWithValue("apellido", "Pérez");
}
```

## Troubleshooting

### H2 Database Issues
```bash
# Clear cache and rebuild
mvn clean test -pl backend-api
```

### Test Hangs
```bash
# Run with timeout
mvn test -pl backend-api -DtestFailureIgnore=true
```

### Coverage Not Showing
```bash
# Regenerate report
mvn clean test jacoco:report -pl backend-api
```

### Docker Issues
```bash
# Start fresh
bash scripts/stop.sh
bash scripts/start.sh
docker ps  # Verify running
```

## CI/CD Integration

Tests are automatically run on:
- Every push to any branch
- Pull requests
- Scheduled nightly builds

See `.github/workflows/` for CI configuration.

## Resources

- **JUnit 5**: https://junit.org/junit5/
- **AssertJ**: https://assertj.org/
- **Mockito**: https://site.mockito.org/
- **JaCoCo**: https://www.jacoco.org/
- **Playwright**: https://playwright.dev/
- **Bruno**: https://www.usebruno.com/

## Contact

For testing questions or issues:
1. Check `TESTING_ROADMAP.md` for progress
2. Check `TESTING_STATUS_2026_06_15.md` for current status
3. Review test examples in existing test files
4. Consult `.claude/rules/` for coding standards
