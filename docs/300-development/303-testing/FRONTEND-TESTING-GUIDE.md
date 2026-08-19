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

All unit tests live under `frontend/src/tests/unit/` (19 files):

```
src/tests/unit/
├── api-client.test.ts               # Authorization header propagation (issue #552)
├── auth-store.test.ts               # Zustand auth store (login, logout, isAdmin)
├── business-logic-hooks.test.ts     # gestionesKeys and related query-key contracts (CU02, CU13, CU24)
├── hooks.test.ts                    # React Query key contracts, type shapes, API path contracts
├── i18n.test.ts                     # isSupportedLocale and locale helpers
├── items-historial.test.ts          # itemsKeys query-key contracts
├── next-config-security-headers.test.ts  # next.config security headers
├── suplencias-reportes.test.ts      # suplenciasKeys query-key contracts
├── utils.test.ts                    # cn(), formatDate, formatCurrency, fullName()
├── workflow-node-meta.test.ts       # Workflow node metadata (issue #646)
├── app-header.test.tsx              # AppHeader component
├── breadcrumb.test.tsx              # Breadcrumb component
├── confirm-dialog.test.tsx          # ConfirmDialog component
├── data-table.test.tsx              # DataTable component (render, empty, loading states)
├── language-switcher.test.tsx       # LanguageSwitcher component
├── login-page.test.tsx              # LoginPage component (form, submit, error handling)
├── pages.test.tsx                   # Display helpers and form validation per use case (CU02)
├── select.test.tsx                  # Select keyboard accessibility (issue #607)
└── workflow-viewer.test.tsx         # WorkflowViewer node styling (issue #613)
```

**Total: 244 unit tests across 19 files (all passing as of 2026-08-19)**

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

All Playwright E2E tests live under `frontend/tests/e2e/` (34 spec files as of 2026-08-19,
plus `gherkin-helpers.ts` shared step helpers, `reporters/`, and `setup/`). Most files map
to one or more Use Cases and are named accordingly (`cu01-presupuesto.spec.ts`,
`cu17-18-personas-clientes.spec.ts`, `cu70-workflow-editor.spec.ts`, etc.); a handful cover
cross-cutting concerns instead of a single CU:

| File | Covers |
|------|--------|
| `00-supervised-tour.spec.ts` | Full-system smoke tour: login → every module → logout |
| `01-first-case-tutorial.spec.ts` | End-to-end tutorial: setup through a full notarial case |
| `login.spec.ts` / `logout.spec.ts` | Auth flow |
| `dashboard.spec.ts` | Dashboard navigation and route guards |
| `admin.spec.ts` | Administración hub (ADMIN-only modules) |
| `api-cycle.spec.ts` | Backend proxy connectivity/health |
| `cu-matrix.spec.ts` | CU coverage matrix health check |
| `a11y-search-inputs.spec.ts` | Accessibility — search input labels |
| `mobile-viewport.spec.ts` | Responsive/mobile layout |
| `l10n-language-switcher.spec.ts` | Localization / language switcher |
| `security-csrf-cors.spec.ts` | CSRF/CORS security headers |
| `icons-ux.spec.ts` | Icon usage/UX consistency |

See the directory itself for the full, current list — it changes often enough that a
complete static copy here would go stale quickly.

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

The frontend CI (`.github/workflows/frontend-ci.yml`) runs on every PR/push touching the
frontend:

1. **TypeScript Check** — type-check (blocking); ESLint step (`continue-on-error: true` —
   not yet a blocking gate)
2. **Unit Tests (Vitest)** — must pass (blocking)
3. **Build (Next.js)** — must compile cleanly (blocking)

E2E is **not** part of `frontend-ci.yml`. Playwright runs in a separate workflow,
`.github/workflows/playwright-e2e.yml`, on every PR into `main`, every push to `main`, a
weekday schedule (06:00 UTC), and manual dispatch. Its jobs (`backend-build`,
`frontend-build`, `api-tests` (Bruno), `e2e-tests` (Playwright UI)) run against a live
stack (PostgreSQL + backend + frontend) and **test failures fail the pipeline** — see
[DevSecOps Pipeline](../../200-architecture/208-devsecops/README.md#other-workflows).

### E2E in CI

E2E tests require a running backend and frontend, which `playwright-e2e.yml` builds and
starts as part of the job — no local setup is needed to run in CI. Unlike the frontend
unit-test/build gates, a failing E2E test blocks the PR.

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
- Seeded user (`V2__initial_data.sql`): `nombre=admin`, `contrasenia=admin` (plaintext
  login value; stored as an MD5 hash), `tipo=Escribano`
- The login endpoint validates against the `usuarios` table in the database
- The `tipo: "ADMIN"` value in the `beforeEach` example above is injected directly into
  `localStorage` for tests that need admin-only UI without a real backend round-trip — it
  does not reflect the seeded user's actual `tipo`

## Test Coverage

To generate a coverage report:

```bash
cd frontend
npx vitest run --coverage
```

Coverage is reported via the Vitest `@vitest/coverage-v8` provider configured in `vitest.config.ts`.
