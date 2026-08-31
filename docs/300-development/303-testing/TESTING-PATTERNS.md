# Testing Patterns & Fixture Strategies

**Comprehensive guide** for implementing tests at every level (Unit → Component → Integration → E2E) with concrete code examples, fixture patterns, and best practices.

---

## 1. Unit Testing Patterns (Backend - Java/Spring)

### 1.1 AAA Pattern (Arrange-Act-Assert)

All unit tests follow the **AAA pattern** for clarity and maintainability:

```java
/**
 * Unit test for PresupuestoService.calcularMontoTotal()
 * 
 * Business rule: Total = Monto + (Monto * IVA%)
 * Example: $100 + (100 * 0.21) = $121
 */
@Test
@DisplayName("Should calculate total presupuesto with correct IVA")
void shouldCalculateTotalWithIVA() {
  // ARRANGE: Set up test data
  BigDecimal monto = new BigDecimal("100.00");
  BigDecimal ivaPorcentaje = new BigDecimal("0.21"); // 21% IVA
  PresupuestoService service = new PresupuestoService();
  
  // ACT: Execute the business logic
  BigDecimal total = service.calcularMontoTotal(monto, ivaPorcentaje);
  
  // ASSERT: Verify the result
  BigDecimal expected = new BigDecimal("121.00");
  assertEquals(expected, total, "Total debe incluir IVA del 21%");
}
```

**Pattern Rules**:
- One logical behavior per test
- Clear section comments (ARRANGE, ACT, ASSERT)
- Meaningful test names: `shouldXxxWhenYyy()`
- Use `@DisplayName` for business-readable descriptions
- Assert one thing per test (or related assertions)

### 1.2 Test Data Builders

Avoid hardcoding test data; use **builder patterns** for complex objects:

```java
public class PresupuestoBuilder {
  private String numero = "PRE-001-TEST";
  private LocalDate fecha = LocalDate.now();
  private BigDecimal monto = new BigDecimal("1000.00");
  private String estado = "DRAFT";
  private Long clienteId = 1L;
  
  // Fluent API for flexibility
  public PresupuestoBuilder withNumero(String numero) {
    this.numero = numero;
    return this;
  }
  
  public PresupuestoBuilder withMonto(BigDecimal monto) {
    this.monto = monto;
    return this;
  }
  
  public PresupuestoBuilder withEstado(String estado) {
    this.estado = estado;
    return this;
  }
  
  public Presupuesto build() {
    Presupuesto p = new Presupuesto();
    p.setNumero(numero);
    p.setFecha(fecha);
    p.setMonto(monto);
    p.setEstado(estado);
    p.setClienteId(clienteId);
    return p;
  }
}

// Usage in tests:
@Test
void shouldHandleLargePresupuesto() {
  Presupuesto presupuesto = new PresupuestoBuilder()
    .withMonto(new BigDecimal("999999.99"))
    .withEstado("APPROVED")
    .build();
  
  // Test with large presupuesto...
}
```

**Benefits**:
- Readable test setup
- Reusable across multiple tests
- Easy to modify test data
- Reduces duplication

### 1.3 Mocking Strategy (Mockito)

Mock **external dependencies**, test **business logic** in isolation:

```java
@ExtendWith(MockitoExtension.class)
class GestionServiceTest {
  
  @Mock
  private GestionRepository gestionRepository;
  
  @Mock
  private PersonaRepository personaRepository;
  
  @InjectMocks
  private GestionService gestionService;
  
  @Test
  @DisplayName("Should create gestion when persona exists")
  void shouldCreateGestionWhenPersonaExists() {
    // ARRANGE
    Long personaId = 1L;
    Persona persona = new PersonaBuilder().withId(personaId).build();
    
    // Mock the repository call
    when(personaRepository.findById(personaId))
      .thenReturn(Optional.of(persona));
    
    when(gestionRepository.save(any(Gestion.class)))
      .thenAnswer(inv -> {
        Gestion g = inv.getArgument(0);
        g.setId(100L); // Simulate DB auto-increment
        return g;
      });
    
    // ACT
    Gestion result = gestionService.crearGestion(personaId, "GES-001");
    
    // ASSERT
    assertNotNull(result.getId());
    assertEquals("GES-001", result.getNumero());
    
    // Verify mocks were called correctly
    verify(personaRepository).findById(personaId);
    verify(gestionRepository).save(any(Gestion.class));
  }
  
  @Test
  @DisplayName("Should throw exception when persona not found")
  void shouldThrowWhenPersonaNotFound() {
    // ARRANGE
    Long invalidPersonaId = 999L;
    when(personaRepository.findById(invalidPersonaId))
      .thenReturn(Optional.empty());
    
    // ACT & ASSERT
    assertThrows(PersonaNotFoundException.class, () -> {
      gestionService.crearGestion(invalidPersonaId, "GES-001");
    });
  }
}
```

**Mocking Rules**:
- Mock **external dependencies** (DB, APIs, services)
- **Never mock** the class under test
- Verify behavior with `verify()` (interaction testing)
- Use `ArgumentCaptor` for complex argument validation

### 1.4 Unit Test Coverage Rules

| Category | Minimum | Target | Examples |
|----------|---------|--------|----------|
| Business Logic (Service) | 70% | 85% | Calculation, validation, state transitions |
| Repository (Data Access) | 60% | 75% | CRUD operations, custom queries |
| Utilities (Helpers) | 50% | 70% | String formatting, date utils |
| Controllers | 40% | 60% | Request/response mapping (low ROI to mock HTTP) |

**How to check coverage**:
```bash
mvn jacoco:check -pl backend-api
mvn jacoco:report -pl backend-api
# Open: backend-api/target/site/jacoco/index.html
```

---

## 2. Component Testing Patterns (Frontend - React)

### 2.1 Setup & Fixture Factories

Create **wrapper factories** for consistent test setup:

```typescript
import { render, screen, RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactElement } from 'react';

// Fixture: QueryClient with safe defaults for tests
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Don't retry in tests
        gcTime: 0,    // Disable garbage collection
      },
      mutations: {
        retry: false,
      },
    },
  });

// Fixture: Wrapper factory pattern
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  queryClient?: QueryClient;
  initialZustandState?: any;
}

export function renderWithProviders(
  ui: ReactElement,
  {
    queryClient = createTestQueryClient(),
    initialZustandState,
    ...renderOptions
  }: CustomRenderOptions = {}
) {
  function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        {children}
      </QueryClientProvider>
    );
  }

  return {
    ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    queryClient,
  };
}

// Usage in tests:
export { screen } from '@testing-library/react';
export { renderWithProviders as render };
```

### 2.2 Component Testing Patterns

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MyFormComponent } from './MyFormComponent';

describe('MyFormComponent', () => {
  test('should render form with required fields', () => {
    // ARRANGE & ACT
    render(<MyFormComponent />);
    
    // ASSERT
    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /guardar/i })).toBeInTheDocument();
  });

  test('should show validation error on empty submission', async () => {
    // ARRANGE
    const user = userEvent.setup();
    render(<MyFormComponent />);
    
    // ACT
    await user.click(screen.getByRole('button', { name: /guardar/i }));
    
    // ASSERT
    await waitFor(() => {
      expect(screen.getByText(/nombre es requerido/i)).toBeInTheDocument();
    });
  });

  test('should submit form with valid data', async () => {
    // ARRANGE
    const handleSubmit = vi.fn();
    const user = userEvent.setup();
    render(<MyFormComponent onSubmit={handleSubmit} />);
    
    // ACT
    await user.type(screen.getByLabelText(/nombre/i), 'Juan Pérez');
    await user.type(screen.getByLabelText(/email/i), 'juan@example.com');
    await user.click(screen.getByRole('button', { name: /guardar/i }));
    
    // ASSERT
    await waitFor(() => {
      expect(handleSubmit).toHaveBeenCalledWith({
        nombre: 'Juan Pérez',
        email: 'juan@example.com',
      });
    });
  });
});
```

**Component Testing Rules**:
- Test **user interactions**, not implementation details
- Use `screen` queries (getByRole, getByLabelText) instead of `querySelector`
- Use `userEvent` for realistic interactions (not `fireEvent`)
- Avoid testing internal state; test outputs instead
- Mock APIs with **MSW** (Mock Service Worker), not component internals

### 2.3 Hook Testing Patterns

```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { useGestiones } from './useGestiones';

describe('useGestiones', () => {
  test('should load gestiones on mount', async () => {
    // ARRANGE & ACT
    const { result } = renderHook(() => useGestiones());
    
    // ASSERT: Initial state
    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();
    
    // ASSERT: After loading
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
      expect(result.current.data).toBeDefined();
      expect(result.current.data).toHaveLength(3);
    });
  });

  test('should handle fetch error', async () => {
    // ARRANGE: Mock API to fail
    const { result } = renderHook(() => useGestiones());
    
    // ASSERT: Error state
    await waitFor(() => {
      expect(result.current.isError).toBe(true);
      expect(result.current.error).toContain('Failed to fetch');
    });
  });
});
```

### 2.4 Component Coverage Rules

| Category | Minimum | Target |
|----------|---------|--------|
| Business Components | 70% | 85% |
| Custom Hooks | 75% | 90% |
| Utility Components | 50% | 65% |
| Page Components | 40% | 60% |

---

## 3. API Testing Patterns (Bruno/REST)

### 3.1 Contract Validation Pattern

Every REST endpoint test validates the **contract** (request format, response format, status codes):

```
# Bruno test: POST /api/v1/gestiones - Create Gestion
# CU02: Iniciar Gestión

@name createGestion
POST http://{{baseUrl}}/api/v1/gestiones
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "numero": "GES-TEST-{{$timestamp}}",
  "presupuestoId": {{presupuestoId}},
  "estado": "DRAFT",
  "descripcion": "Test gestion"
}

# Response validation
? res.status === 201
? res.headers["content-type"] === "application/json"
? res.body.id != null
? res.body.numero != null
? res.body.createdAt != null
? res.body.estado === "DRAFT"

# Performance assertion
? res.responseTime < 500
```

### 3.2 Error Scenario Testing

Test **all error paths** (400, 401, 403, 404, 500):

```
# Test: POST /api/v1/gestiones - Missing required field (400)
POST http://{{baseUrl}}/api/v1/gestiones
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "presupuestoId": {{presupuestoId}}
  # "numero" intentionally omitted
}

? res.status === 400
? res.body.error === "numero es requerido"
? res.body.code === "VALIDATION_ERROR"

---

# Test: GET /api/v1/gestiones/999 - Not found (404)
GET http://{{baseUrl}}/api/v1/gestiones/999
Authorization: Bearer {{adminToken}}

? res.status === 404
? res.body.error.contains("Gestion no encontrada")

---

# Test: POST /api/v1/gestiones - Unauthorized (401)
POST http://{{baseUrl}}/api/v1/gestiones
Content-Type: application/json

{...}

? res.status === 401
? res.body.error === "Token inválido o expirado"
```

### 3.3 Idempotency Testing

POST operations should be **idempotent** when submitted multiple times:

```
# Test: Create presupuesto twice with same data
# Expected: First request → 201 Created, Second → 409 Conflict (or 200 OK)

@name createPresupuesto
POST http://{{baseUrl}}/api/v1/presupuestos
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "numero": "PRE-IDEMPOTENT-{{idempotencyKey}}",
  "fecha": "2026-08-31",
  "monto": 5000,
  "clienteId": {{clienteId}}
}

? res.status === 201

# Second request with same data
? res.status === 409 || res.status === 200
# Should return same ID, not create duplicate
? res.body.id === prev-response.id
```

### 3.4 API Coverage Rules

| Endpoint Type | Minimum | Target |
|---------------|---------|--------|
| CREATE (POST) | 3 tests | 5 (golden path + errors + idempotency) |
| READ (GET) | 3 tests | 5 (single, list, filter, not found, unauthorized) |
| UPDATE (PUT) | 3 tests | 4 (success, not found, validation, conflict) |
| DELETE (DELETE) | 2 tests | 3 (success, not found, cascade effects) |

---

## 4. E2E Testing Patterns (Playwright - Already Documented)

### 4.1 GherkinSteps Pattern (TS-nnnn Tests)

See [`TEST-PLAN.md`](TEST-PLAN.md) §3 (Fixture Patterns & Test Architecture) and [`E2E-TEST-MAPPING.md`](E2E-TEST-MAPPING.md) §8 (Fixture Patterns by Category).

**Summary**:
- **GherkinSteps**: Given-When-Then helpers (26 suites)
- **API Helpers**: Data setup via REST (createPersona, createPresupuesto, etc.)
- **Global Setup**: Auth token, test data seeding
- **Global Teardown**: Cleanup, fixture removal

### 4.2 E2E Coverage Rules

| Test Category | Minimum | Target |
|---------------|---------|--------|
| Core Workflows (CU01–CU23) | 1 per CU | 1–2 per CU (golden + edge) |
| Admin Operations (CU24+) | 1 per module | 1 (smoke test OK) |
| Quality Assurance | — | 1 each (accessibility, responsive, security) |
| Regression | — | 1 (full app tour) |

---

## 5. Testing Pyramid & Execution Strategy

```
                     ╱╲
                    ╱  ╲  E2E (Playwright)
                   ╱    ╲  ~5% of tests, 60 min weekly
                  ╱──────╲ 35 suites, 448 tests
                 ╱        ╲
                ╱──────────╲
               ╱  API Tests ╱  Bruno + Contract
              ╱   (REST)   ╱   ~15% of tests, 10 min daily
             ╱────────────╲
            ╱  Component  ╱  React + Vitest
           ╱   Testing   ╱   ~20% of tests, 5 min per commit
          ╱──────────────╲
         ╱  Unit Testing ╱  Java + Mockito
        ╱   (Backend)   ╱   ~60% of tests, 2 min per commit
       ╱────────────────╲
      ╲__________________╲

Execution Strategy:
1. **Per Commit**: Unit tests (instant feedback) → Component tests
2. **Per PR**: Unit + Component + API tests (10 min total)
3. **Pre-Merge**: Add E2E tests (60 min, full validation)
4. **Pre-Release**: Performance + security tests (optional)
```

---

## 6. Coverage Targets by Level

| Test Level | Minimum | Target | Command |
|------------|---------|--------|---------|
| **Unit (Backend)** | 70% line | 80% line + 75% branch | `mvn jacoco:check` |
| **Component (Frontend)** | 60% line | 75% line | `npm run test:coverage` |
| **API (REST)** | 80% endpoints | 100% endpoints + error paths | `bru run . -r` |
| **E2E (Workflows)** | 1 per CU | 1–2 per CU (golden + edge) | `npx playwright test` |
| **Overall** | — | **Ratchet floor enforced** | `bash scripts/preflight.sh` |

---

## 7. Test Lifecycle & Data Management

### Test Data Setup (Fixtures)

```typescript
// Global Setup (runs once per test worker)
export async function globalSetup(config: FullConfig) {
  // 1. Authenticate as admin → get JWT token
  // 2. Seed base data (roles, tipos, estados) via API
  // 3. Save token to process.env.E2E_ADMIN_TOKEN
  // 4. Save localStorage state for browser
}

// Per-Test Setup (beforeEach)
test.beforeEach(async ({ page }) => {
  // 1. Login (reuse token from global setup)
  // 2. Navigate to page under test
  // 3. Seed test-specific data (persona, presupuesto) via API
  // 4. Wait for page to load
});

// Per-Test Teardown (afterEach)
test.afterEach(async ({ page }) => {
  // 1. Capture screenshot if test failed
  // 2. Optional: Download trace for debugging
});

// Global Teardown (runs once per test worker, at end)
export async function globalTeardown(config: FullConfig) {
  // 1. Delete all test data via API (DELETE endpoints)
  // 2. Log audit trail for verification
  // 3. Close browser session
}
```

### Test Data Naming Convention

Make test data **identifiable** and **safe to delete**:

```typescript
// Good: Clearly identified as test data
const testNumeroPE = `PRE-TEST-${Date.now()}`; // PRE-TEST-1725130000
const testNumeroGE = `GES-TEST-${Date.now()}`; // GES-TEST-1725130000
const testEmailPersona = `test+${Date.now()}@example.com`;

// Bad: Could collide with real data
const numero = "PRE-001";
const email = "test@example.com";

// Cleanup: Delete by test marker
await apiDelete(`/api/v1/presupuestos?numero_startswith=PRE-TEST-`);
await apiDelete(`/api/v1/gestiones?numero_startswith=GES-TEST-`);
```

---

## 8. Debugging Failed Tests

### Unit Test Failures

```bash
# Run single test with debug output
mvn test -pl backend-api -Dtest=GestionServiceTest#shouldCreateGestion -X

# Check test report
open backend-api/target/site/surefire-report.html
```

### Component Test Failures

```bash
# Run with debug mode
npm run test:watch -- MyComponent

# Generate coverage report
npm run test:coverage -- MyComponent
```

### API Test Failures

```bash
# View Bruno response details
# (Bruno UI shows request/response in detail)

# Check backend logs
docker logs notary-backend | grep ERROR
```

### E2E Test Failures

```bash
# View Playwright trace
npx playwright show-trace test-results/trace.zip

# Run with headed mode for debugging
HEADED=1 npx playwright test TS-0001-login --debug

# View video of failed test
open test-results/TS-0001-login-should-display-login-form/video.webm
```

---

## Summary: Testing Patterns Checklist

- [x] Unit: AAA pattern, builders, mocks (Mockito)
- [x] Component: Wrapper factories, userEvent, MSW mocking
- [x] API: Contract validation, error scenarios, idempotency
- [x] E2E: GherkinSteps, API helpers, global setup/teardown
- [x] Coverage: Targets per level, ratchet floor enforced
- [x] Lifecycle: Data setup/cleanup, naming conventions
- [x] Debugging: Tools and commands for each level

See [`TEST-PLAN.md`](TEST-PLAN.md) for master testing document and [`E2E-TEST-MAPPING.md`](E2E-TEST-MAPPING.md) for E2E test inventory.
