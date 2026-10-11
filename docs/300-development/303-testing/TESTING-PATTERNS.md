# Testing Patterns & Fixture Strategies

**Comprehensive guide** for implementing tests at every level (Unit → Component → Integration → E2E) with concrete code examples, fixture patterns, and best practices.

---

## 1. Unit Testing Patterns (Backend - Java/Spring)

### 1.1 AAA Pattern (Arrange-Act-Assert)

All unit tests follow the **AAA pattern** for clarity and maintainability:

```java
/**
 * AAA sample for a total that includes VAT.
 *
 * Budget amounts on the wire are propertyAmount. BudgetService has no
 * total-with-VAT method, so the arithmetic stays in the test.
 * Stored budget statuses are BORRADOR, PENDIENTE, APROBADO, RECHAZADO, and FACTURADO.
 *
 * Rule: total = amount + (amount * VAT rate)
 * Example: 100 + (100 * 0.21) = 121
 */
@Test
@DisplayName("Should calculate a total that includes 21 percent VAT")
void shouldCalculateTotalWithVat() {
  // ARRANGE: Set up test data
  BigDecimal amount = new BigDecimal("100.00");
  BigDecimal vatRate = new BigDecimal("0.21");

  // ACT: Execute the calculation
  BigDecimal total = amount.add(amount.multiply(vatRate));

  // ASSERT: Verify the result
  assertEquals(0, new BigDecimal("121.00").compareTo(total));
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
public class BudgetBuilder {
  private int number = 1001;
  private Date date = new Date();
  private String heading = "Test budget";
  private String status = "BORRADOR";
  private BigDecimal propertyAmount = new BigDecimal("1000.00");

  public BudgetBuilder withNumber(int number) {
    this.number = number;
    return this;
  }

  public BudgetBuilder withPropertyAmount(BigDecimal propertyAmount) {
    this.propertyAmount = propertyAmount;
    return this;
  }

  public BudgetBuilder withStatus(String status) {
    this.status = status;
    return this;
  }

  public Budget build() {
    Budget budget = new Budget();
    budget.setNumber(number);
    budget.setDate(date);
    budget.setHeading(heading);
    budget.setStatus(status);
    budget.setPropertyAmount(propertyAmount);
    return budget;
  }
}

// Usage in tests:
@Test
void shouldHandleLargeBudget() {
  Budget budget = new BudgetBuilder()
    .withPropertyAmount(new BigDecimal("999999.99"))
    .withStatus("APROBADO")
    .build();

  // Test with a large propertyAmount...
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
class BudgetServiceTest {

  @Mock
  private BudgetRepositoryPort budgetRepository;

  @InjectMocks
  private BudgetService budgetService;

  @Test
  @DisplayName("Should return budgets when the status matches")
  void shouldReturnBudgetsWhenStatusMatches() {
    // ARRANGE
    Budget budget = new BudgetBuilder().withStatus("BORRADOR").build();
    when(budgetRepository.findByStatus("BORRADOR")).thenReturn(List.of(budget));

    // ACT
    List<Budget> result = budgetService.findByStatus("BORRADOR");

    // ASSERT
    assertEquals(1, result.size());
    assertEquals("BORRADOR", result.get(0).getStatus());
    verify(budgetRepository).findByStatus("BORRADOR");
  }

  @Test
  @DisplayName("Should throw when the budget to update does not exist")
  void shouldThrowWhenBudgetMissing() {
    // ARRANGE
    when(budgetRepository.existsById(999)).thenReturn(false);

    // ACT & ASSERT
    ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class, () ->
        budgetService.update(999, new Budget()));
    assertEquals("Budget not found with ID: 999", error.getMessage());
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

The sample renders the English catalog (`First name`, `Last name`, `Save`). A test of the live people screen asserts the default Spanish catalog (`Nombre`, `Apellido`, `Guardar`). Message keys stay `personas.fields.nombre` and `personas.fields.apellido`. The submit body matches `PersonRequest`: `firstName` and `lastName`.

```typescript
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MyFormComponent } from './MyFormComponent';

describe('MyFormComponent', () => {
  test('should render form with required fields', () => {
    // ARRANGE & ACT
    render(<MyFormComponent />);

    // ASSERT
    expect(screen.getByLabelText(/first name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/last name/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument();
  });

  test('should show validation error on empty submission', async () => {
    // ARRANGE
    const user = userEvent.setup();
    render(<MyFormComponent />);

    // ACT
    await user.click(screen.getByRole('button', { name: /save/i }));

    // ASSERT
    await waitFor(() => {
      expect(screen.getByText(/first name is required/i)).toBeInTheDocument();
    });
  });

  test('should submit form with valid data', async () => {
    // ARRANGE
    const handleSubmit = vi.fn();
    const user = userEvent.setup();
    render(<MyFormComponent onSubmit={handleSubmit} />);

    // ACT
    await user.type(screen.getByLabelText(/first name/i), 'Juan');
    await user.type(screen.getByLabelText(/last name/i), 'Pérez');
    await user.click(screen.getByRole('button', { name: /save/i }));

    // ASSERT
    await waitFor(() => {
      expect(handleSubmit).toHaveBeenCalledWith({
        firstName: 'Juan',
        lastName: 'Pérez',
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

`useManagementsPage` is the list hook. It returns a React Query result whose `data` is a Spring page (`content`, `totalElements`). `createTestQueryClient` is the factory from section 2.1.

```typescript
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { useManagementsPage } from './useManagements';

function QueryClientWrapper({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={createTestQueryClient()}>
      {children}
    </QueryClientProvider>
  );
}

describe('useManagementsPage', () => {
  test('should load managements on mount', async () => {
    // ARRANGE & ACT
    const { result } = renderHook(
      () => useManagementsPage({ page: 0, size: 20 }),
      { wrapper: QueryClientWrapper }
    );

    // ASSERT: Initial state
    expect(result.current.isLoading).toBe(true);
    expect(result.current.data).toBeUndefined();

    // ASSERT: After loading
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
      expect(result.current.data?.content).toBeDefined();
    });
  });

  test('should handle fetch error', async () => {
    // ARRANGE: MSW returns 500 for GET /managements
    const { result } = renderHook(
      () => useManagementsPage({ page: 0, size: 20 }),
      { wrapper: QueryClientWrapper }
    );

    // ASSERT: Error state
    await waitFor(() => {
      expect(result.current.isError).toBe(true);
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

Every REST endpoint test validates the **contract** (request format, response format, status codes).

Error bodies are `ErrorResponse`: `timestamp`, `status`, `error` (the HTTP reason phrase), `message`, `path`, `traceId`, and `details`. There is no `code` field. A missing management id returns **404 with an empty body**. An unauthenticated call returns **401**; the entry point reason is `Unauthorized`.

```
# Bruno test: POST /api/v1/managements — create a management
# CU02

@name createManagement
POST http://{{baseUrl}}/api/v1/managements
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "number": {{managementNumber}},
  "heading": "Test management",
  "notes": "Contract test"
}

# Response validation — ManagementResponse
? res.status === 201
? res.headers["content-type"].includes("application/json")
? res.body.idManagement != null
? res.body.number != null
? res.body.heading === "Test management"

# Performance assertion
? res.responseTime < 500
```

### 3.2 Error Scenario Testing

Test **all error paths** (400, 401, 403, 404, 500):

```
# Test: POST /api/v1/managements — missing required heading (400)
POST http://{{baseUrl}}/api/v1/managements
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "number": {{managementNumber}}
}

? res.status === 400
? res.body.error === "Bad Request"
? res.body.message.includes("heading")

---

# Test: GET /api/v1/managements/999999 — not found (404, empty body)
GET http://{{baseUrl}}/api/v1/managements/999999
Authorization: Bearer {{adminToken}}

? res.status === 404

---

# Test: POST /api/v1/managements — unauthorized (401)
POST http://{{baseUrl}}/api/v1/managements
Content-Type: application/json

{
  "heading": "Test management"
}

? res.status === 401
```

### 3.3 Conflict responses

A unique-constraint violation answers **409** with `ErrorResponse.message` equal to `The submitted data duplicates an existing record`. A stale `version` on update answers **409** with `The record was modified by another user; reload it and try again`. Creating the same budget twice is not rejected: `budgets.number` is not unique, and `POST /api/v1/budgets` returns **201** with a new `idBudget` each time.

```
# Create budget — BudgetRequest / BudgetResponse
@name createBudget
POST http://{{baseUrl}}/api/v1/budgets
Content-Type: application/json
Authorization: Bearer {{adminToken}}

{
  "number": {{budgetNumber}},
  "date": "2026-08-31",
  "heading": "Contract budget",
  "status": "BORRADOR",
  "propertyAmount": 5000,
  "personId": {{personId}}
}

? res.status === 201
? res.body.idBudget != null
? res.body.status === "BORRADOR"

# Duplicate unique key
? res.status === 409
? res.body.error === "Conflict"
? res.body.message === "The submitted data duplicates an existing record"
```

### 3.4 API Coverage Rules

| Endpoint Type | Minimum | Target |
|---------------|---------|--------|
| CREATE (POST) | 3 tests | 5 (golden path + validation + conflict) |
| READ (GET) | 3 tests | 5 (single, list, filter, not found, unauthorized) |
| UPDATE (PUT) | 3 tests | 4 (success, not found, validation, conflict) |
| DELETE (DELETE) | 2 tests | 3 (success, not found, cascade effects) |

---

## 4. E2E Testing Patterns (Playwright - Already Documented)

### 4.1 GherkinSteps Pattern (TS-nnnn Tests)

See [`TEST-PLAN.md`](TEST-PLAN.md) §3 (Fixture Patterns & Test Architecture) and [`E2E-TEST-MAPPING.md`](E2E-TEST-MAPPING.md) §8 (Fixture Patterns by Category).

**Summary**:
- **GherkinSteps**: Given-When-Then helpers (26 suites)
- **API Helpers**: Data setup via REST (createPerson, createBudget, etc.)
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
| **Overall** | — | **Ratchet floor enforced** | `bash workspace/sdlc/preflight.sh` |

---

## 7. Test Lifecycle & Data Management

### Test Data Setup (Fixtures)

```typescript
// Global Setup (runs once per test worker)
export async function globalSetup(config: FullConfig) {
  // 1. Authenticate as admin → get JWT token
  // 2. Seed base data (roles, procedure types, management statuses) via API
  // 3. Save token to process.env.E2E_ADMIN_TOKEN
  // 4. Save localStorage state for browser
}

// Per-Test Setup (beforeEach)
test.beforeEach(async ({ page }) => {
  // 1. Login (reuse token from global setup)
  // 2. Navigate to page under test
  // 3. Seed test-specific data (person, budget) via API
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
// Good: Clearly identified as test data. Budget and management numbers are integers.
const testBudgetNumber = Date.now() % 1_000_000_000;
const testPersonEmail = `test+${Date.now()}@example.com`;

// Bad: Could collide with real data
const budgetNumber = 1;
const email = "test@example.com";

// Cleanup: delete the ids the create calls returned
await apiDelete(`/api/v1/budgets/${createdBudgetId}`);
await apiDelete(`/api/v1/managements/${createdManagementId}`);
```

### Backend H2 payment IT fixture isolation (#916)

H2 Spring Boot integration tests share a cached application context. Payments
against the seeded `idBudget=1` (`src/test/resources/data.sql`) accumulate across
classes and eventually trip the CU15/#848 overpayment guard (`409` instead of
`201`). Prefer arranging a dedicated budget over blanket
`@DirtiesContext(AFTER_EACH)`.

```java
// Good: mutate payments against a fixture budget
Integer budgetId = BudgetPaymentTestFixtures.createIsolatedBudget(
        mockMvc, new BigDecimal("10000.00"));
BudgetPaymentTestFixtures.createPayment(mockMvc, budgetId, new BigDecimal("1500.00"), "IT");

// Bad: hardcode seed id for POST /api/v1/payments
// {"idBudget": 1, "amount": 1500.00, ...}
```

Read-only GETs against seed id `1` remain acceptable. Patterns to follow:
`ManagementArchiveIntegrationTest`, `BudgetSummaryControllerTest`,
`BudgetPaymentTestFixtures`. Controller unit mega-tests that stub
`RuntimeException("x")` must keep happy-path and error-path methods separate
(`SimpleControllersTest`).

---

## 8. Debugging Failed Tests

### Unit Test Failures

```bash
# Run single test with debug output
mvn test -pl backend-api -Dtest=BudgetEntityTest -X

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
