# Phase 1: Service Layer Tests (31% → 50% Coverage)

**Timeline**: Q2-Q3 2026
**Target**: 50% code coverage through service layer unit tests
**Tests Needed**: 200+ new service layer tests

> **Current status**: All 8 services have test files. See [anchored summary](https://github.com/matiaspakua/notaire/pull/479).

## Overview

Phase 1 focuses on implementing comprehensive unit tests for all service layer classes. Services contain business logic and are the ideal place to increase coverage efficiently.

## Service Layer Structure

**Location**: `src/main/java/com/licensis/notaire/service/`

### Services to Test (8 total)

1. **PersonaService** (3.0 KB)
   - findAll(), findById(), save(), deleteById()
   - buscar() - complex multi-filter search logic
   - 10+ test cases minimum

2. **PresupuestoService** (4.7 KB)
   - findAll(), findAllPaged(), findById()
   - findByPersona(), findByEstado()
   - create(), update(), deleteById()
   - Error handling (ResourceNotFoundException)
   - 15+ test cases minimum

3. **RegistroAuditoriaService** (6.0 KB)
   - findAll(), findAll(Pageable), findById()
   - findByModulo(), findByUsuarioId()
   - DTO conversion methods
   - 15+ test cases minimum

4. **EscrituraService** (2.4 KB)
   - CRUD operations
   - 10+ test cases minimum

5. **PagoService** (6.0 KB)
   - Payment operations and calculations
   - 15+ test cases minimum

6. **GestionQueryService** (1.7 KB) ✅
   - findAll() paged, findById(), findByNumero()
   - DTO mapping (all fields, null estado, null tramiteList, tramite count)
   - Edge cases (empty results, non-existent IDs)
   - 10 test cases (completed)

7. **WorkflowValidationService** (3.8 KB)
   - Workflow state validation
   - 12+ test cases minimum

8. **WorkflowTraceService** (7.9 KB)
   - Workflow tracking and trace operations
   - 15+ test cases minimum

## Test Writing Guide

### Test File Structure

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceNameService Unit Tests")
class ServiceNameServiceTest {

    private ServiceNameService service;
    
    @Mock
    private RepositoryNameRepository repository;
    
    @BeforeEach
    void setUp() {
        service = new ServiceNameService(repository);
    }
    
    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {
        @Test
        @DisplayName("Should return all entities")
        void shouldReturnAll() {
            // Arrange: Create test data
            Entity e1 = createTestEntity(1, "value1");
            Entity e2 = createTestEntity(2, "value2");
            
            when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
            
            // Act
            List<Entity> result = service.findAll();
            
            // Assert: Verify behavior with meaningful assertions
            assertThat(result)
                .hasSize(2)
                .extracting(Entity::getField)
                .containsExactly("value1", "value2");
                
            verify(repository, times(1)).findAll();
        }
    }
}
```

### Test Patterns

#### Pattern 1: CRUD Operations
```java
@Test
@DisplayName("Should find entity by ID")
void shouldFindById() {
    Entity entity = createTestEntity(1, "name");
    when(repository.findById(1)).thenReturn(Optional.of(entity));
    
    Optional<Entity> result = service.findById(1);
    
    assertThat(result)
        .isPresent()
        .hasValueSatisfying(e -> {
            assertThat(e.getId()).isEqualTo(1);
            assertThat(e.getName()).isEqualTo("name");
        });
    
    verify(repository).findById(1);
}
```

#### Pattern 2: Error Handling
```java
@Test
@DisplayName("Should throw exception when entity not found")
void shouldThrowWhenNotFound() {
    when(repository.existsById(999)).thenReturn(false);
    
    assertThatThrownBy(() -> service.update(999, new Entity()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");
        
    verify(repository, never()).save(any());
}
```

#### Pattern 3: Filter/Search Operations
```java
@Test
@DisplayName("Should find entities by filter")
void shouldFindByFilter() {
    Entity e1 = createTestEntity(1, "ACTIVE");
    Entity e2 = createTestEntity(2, "ACTIVE");
    
    when(repository.findByStatus("ACTIVE"))
        .thenReturn(Arrays.asList(e1, e2));
    
    List<Entity> result = service.findByStatus("ACTIVE");
    
    assertThat(result)
        .hasSize(2)
        .extracting(Entity::getStatus)
        .allMatch(s -> s.equals("ACTIVE"));
}
```

#### Pattern 4: Pagination
```java
@Test
@DisplayName("Should return paginated results")
void shouldReturnPaged() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Entity> page = new PageImpl<>(
        Arrays.asList(createTestEntity(1, "value")),
        pageable,
        1
    );
    
    when(repository.findAll(pageable)).thenReturn(page);
    
    Page<Entity> result = service.findAll(pageable);
    
    assertThat(result)
        .hasSize(1)
        .extracting(Entity::getId)
        .containsExactly(1);
}
```

### Real Assertions (Not Mocks)

✅ **DO** - Test actual behavior:
```java
assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
assertThat(result.getStatus()).isEqualTo("ACTIVE");
assertThat(result.getCreatedDate()).isNotNull();
assertThat(result.getItems()).hasSize(3);
```

❌ **DON'T** - Just check existence:
```java
assertThat(result).isNotNull();
assertThat(result).isPresent();
```

## Coverage Targets by Service

| Service | Classes | Methods | Lines | Target Tests |
|---------|---------|---------|-------|--------------|
| PersonaService | 1 | 6 | ~85 | 12 |
| PresupuestoService | 1 | 8 | ~137 | 18 |
| RegistroAuditoriaService | 1 | 10 | ~200 | 20 |
| EscrituraService | 1 | 4 | ~65 | 10 |
| PagoService | 1 | 8 | ~150 | 18 |
| GestionQueryService | 1 | 3 | ~50 | 8 |
| WorkflowValidationService | 1 | 6 | ~120 | 14 |
| WorkflowTraceService | 1 | 12 | ~200 | 20 |
| **TOTAL** | **8** | **57** | **~1000** | **120+** |

## Test Data Fixtures

Create reusable test builders:

```java
private Entity createTestEntity(Integer id, String status) {
    Entity entity = new Entity();
    entity.setId(id);
    entity.setStatus(status);
    entity.setCreatedDate(LocalDateTime.now());
    return entity;
}
```

## Edge Cases to Test

For each service method, test:

1. **Null/Empty inputs**
   - Null parameters
   - Empty strings
   - Empty collections

2. **Boundary conditions**
   - Zero values
   - Maximum/minimum values
   - Very long strings

3. **Error paths**
   - Resource not found
   - Invalid state transitions
   - Constraint violations

4. **Special characters**
   - Accented characters (e.g., "José García")
   - Special symbols (e.g., "O'Brien")
   - Unicode characters

5. **Concurrency**
   - Concurrent updates
   - Race conditions
   - Lock situations

## Test Execution

### Run all service layer tests:
```bash
mvn test -pl backend-api -Dtest="*ServiceTest"
```

### Generate coverage report:
```bash
mvn clean test jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
```

### Check specific service coverage:
```bash
mvn test -pl backend-api -Dtest="PersonaServiceTest"
mvn test -pl backend-api -Dtest="PresupuestoServiceTest"
# ... etc
```

## Quality Checklist

Before committing tests:

- [ ] All tests pass locally (`mvn clean test`)
- [ ] Coverage increased (measured by JaCoCo)
- [ ] Real assertions (not just null checks)
- [ ] Test names describe behavior (shouldXxx pattern)
- [ ] No @Disabled tests without documented reason
- [ ] Arrange-Act-Assert pattern followed
- [ ] Tests are independent (no order dependencies)
- [ ] Edge cases covered
- [ ] Error paths tested
- [ ] No unused test fixtures or setup

## Milestones

### Week 1-2: PersonaService + PresupuestoService
- 20-25 tests
- Coverage → 35%
- PR with test patterns established

### Week 3: RegistroAuditoriaService + EscrituraService
- 25-30 tests
- Coverage → 40%
- PR validating patterns

### Week 4: PagoService + GestionQueryService
- **GestionQueryService** ✅ (10 tests, DTO mapping + edge cases)
- **PagoService** 🔄 (pending)
- 20-25 tests total
- Coverage → 43%

### Week 5: Workflow Services
- 30-35 tests
- Coverage → 50%
- Target achieved! 🎯

## Rollout to Team

1. **Establish patterns** (Week 1)
   - Agree on test structure
   - Agree on assertion patterns
   - Agree on fixture builders

2. **Parallelize** (Week 2+)
   - Multiple developers can work on different services
   - Follow established patterns
   - Regular syncs to share learnings

3. **Review process**
   - Code review before merge
   - Verify tests actually test behavior
   - Verify assertions are meaningful
   - Verify coverage improvements

## Success Metrics

✅ Coverage: 31% → 50% (+19 percentage points)
✅ Test count: 67 → 267+ (+200 tests)
✅ Service layer: ~100% of methods tested
✅ Real assertions: All tests use meaningful assertions
✅ Documentation: Complete Phase 1 patterns guide

## References

- [AUDITORIA.md](../../AUDITORIA.md) — Mandatory test requirements
- [TEST-COVERAGE-STRATEGY.md](TEST-COVERAGE-STRATEGY.md) — Overall coverage strategy
- JUnit 5 docs: https://junit.org/junit5/
- Mockito docs: https://javadoc.io/doc/org.mockito/mockito-core
- AssertJ docs: https://assertj.org/

---

**Started**: 2026-06-11
**Target Completion**: End of Q2 2026
**Status**: 🔄 In Progress
