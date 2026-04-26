# Frontend Testing Guide — Notaire

> Related to issues #248, #295, #276

## Testing Stack

| Layer | Tool | Config file |
|-------|------|-------------|
| Unit tests | Vitest + React Testing Library | `frontend/vitest.config.ts` |
| E2E tests | Playwright | `frontend/playwright.config.ts` |
| Test setup | `@testing-library/jest-dom` | `frontend/src/tests/setup.ts` |

## Running Tests

```bash
cd frontend

# Unit tests (Vitest)
npm run test             # watch mode
npm run test:run         # single run (CI)

# E2E tests (requires backend on :8080 + frontend on :3000)
npm run test:e2e         # run all E2E
npm run test:e2e -- --headed   # with browser visible
```

## Unit Test Structure

All unit tests live under `frontend/src/tests/unit/`:

```
src/tests/unit/
├── utils.test.ts          # formatDate, formatCurrency, cn(), fullName()
├── api-client.test.ts     # apiGet, apiPost, apiPut, apiDelete (fetch mocked)
├── auth-store.test.ts     # Zustand auth store (login, logout, isAdmin)
├── data-table.test.tsx    # DataTable component (render, empty, loading states)
├── login-page.test.tsx    # LoginPage component (form, submit, error handling)
├── hooks.test.ts          # Query key contracts, type shapes, API path contracts
└── pages.test.tsx         # Display helpers and form validation per use case
```

**Total: 80 unit tests (all passing)**

### Writing a New Unit Test

#### Utility function test

```typescript
import { describe, it, expect } from "vitest";
import { formatCurrency } from "@/lib/utils";

describe("formatCurrency()", () => {
  it("returns — for undefined", () => {
    expect(formatCurrency(undefined)).toBe("—");
  });

  it("formats number as ARS", () => {
    const result = formatCurrency(5000);
    expect(result).toContain("5");
    expect(result).toContain("000");
  });
});
```

#### Hook query key test

```typescript
import { describe, it, expect } from "vitest";
import { myEntityKeys } from "@/hooks/useMyEntity";

describe("myEntityKeys", () => {
  it("all key is ['myEntity']", () => {
    expect(myEntityKeys.all).toEqual(["myEntity"]);
  });
});
```

#### Component rendering test

```typescript
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { MyComponent } from "@/components/MyComponent";

describe("MyComponent", () => {
  it("renders label", () => {
    render(<MyComponent label="Test" />);
    expect(screen.getByText("Test")).toBeInTheDocument();
  });
});
```

## E2E Test Structure

All Playwright E2E tests live under `frontend/tests/e2e/`:

```
tests/e2e/
├── login.spec.ts          # CU03 — Login / auth flow
├── dashboard.spec.ts      # Dashboard navigation and route guards
├── crud-gestiones.spec.ts # CU02 — Gestiones CRUD
├── personas.spec.ts       # CU17, CU18 — Personas CRUD
├── presupuestos.spec.ts   # CU01, CU15, CU47 — Presupuestos + Pagos
└── admin.spec.ts          # CU26/27/28/29/39/49/55/57/64/67 — Admin modules
```

### Auth Setup in E2E Tests

Since the middleware checks for the `notaire-auth-status` cookie and the auth store uses `localStorage`, tests must inject both:

```typescript
test.beforeEach(async ({ page }) => {
  // Set auth cookie (middleware check)
  await page.context().addCookies([{
    name: "notaire-auth-status",
    value: "authenticated",
    domain: "localhost",
    path: "/",
  }]);

  // Set auth store in localStorage (components)
  await page.addInitScript(() => {
    localStorage.setItem("notaire-auth", JSON.stringify({
      state: {
        user: { nombre: "admin", tipo: "ADMIN", valido: true },
        isAuthenticated: true,
      },
      version: 0,
    }));
  });

  await page.goto("/dashboard/my-page");
});
```

### Writing a New E2E Test

```typescript
import { test, expect } from "@playwright/test";

test.describe("MyModule (CUxx)", () => {
  test.beforeEach(async ({ page }) => {
    // Auth setup (see above)
    await page.goto("/dashboard/my-module");
  });

  test("page loads correctly", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /my module/i })).toBeVisible();
  });

  test("create button opens modal", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo/i }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
  });

  test("modal can be cancelled", async ({ page }) => {
    await page.getByRole("button", { name: /nuevo/i }).click();
    await page.getByRole("button", { name: /cancelar/i }).click();
    await expect(page.getByRole("dialog")).not.toBeVisible();
  });
});
```

## CI Configuration

The frontend CI (`.github/workflows/frontend-ci.yml`) runs:

1. **Lint** — TypeScript type-check + ESLint
2. **Unit Tests (Vitest)** — must pass (blocking)
3. **Build (Next.js)** — must compile cleanly (blocking)
4. **E2E Tests (Playwright)** — `continue-on-error: true` (requires backend — non-blocking in CI)

### E2E in CI

E2E tests require a running backend on `http://localhost:8080`. In CI without a full Docker stack, they are expected to fail. The job uses `continue-on-error: true` to keep the PR green.

To run E2E locally:
```bash
# Terminal 1: Start full stack
docker-compose up -d

# Terminal 2: Run E2E
cd frontend
npm run test:e2e
```

## Test Data Strategy

Unit tests use mocked data via `vi.mock()` and direct object construction. E2E tests rely on the backend's seeded test data (initial Flyway migrations).

Key default test data:
- Admin user: `nombre=admin`, `contrasenia=admin`, `tipo=ADMIN`
- The login endpoint validates against the `usuarios` table in the database

## Test Coverage

To generate a coverage report:

```bash
cd frontend
npx vitest run --coverage
```

Coverage is reported via the Vitest `@vitest/coverage-v8` provider configured in `vitest.config.ts`.
