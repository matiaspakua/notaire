# Test Plan — Notaire E2E Test Suite (TS-nnnn)

**Current**: 35 organized E2E test suites (consolidated from 41 original files) covering 68+ business Use Cases (Casos de Uso).

## 1. Test Nomenclature & Organization (TS-nnnn)

All E2E test suites follow the naming convention: **`TS-nnnn-<workflow-name>.spec.ts`**

Where:
- **TS** = Test Suite prefix (E2E, end-to-end)
- **nnnn** = 4-digit sequential number (0001, 0002, ..., 0071)
- **`<workflow-name>`** = Descriptive workflow identifier (kebab-case)

### Test Suite Categories

| Range | Category | Count | Purpose |
|-------|----------|-------|---------|
| TS-0001–TS-0003 | **Authentication & Navigation** | 3 | Login, logout, dashboard access (foundation) |
| TS-0010–TS-0035 | **Core Business Workflows** | 26 | Primary business Use Cases (high value) |
| TS-0040–TS-0050 | **Quality Assurance / Technical** | 9 | Localization, accessibility, responsive, security |
| TS-0060, TS-0070–TS-0071 | **Regression & Tutorial** | 3 | Full app tour, onboarding demos |

---

## 2. Test Inventory & Use Case Traceability

### TS-0001–TS-0003: Foundation (Authentication & Dashboard)

| Suite | Filename | Primary CU(s) | Scope | Fixture |
|-------|----------|---------------|-------|---------|
| **TS-0001** | `TS-0001-login-authentication.spec.ts` | AUTH-001 | User login via UI form | Form fill, JWT token validation |
| **TS-0002** | `TS-0002-logout-authentication.spec.ts` | AUTH-001 | Logout + redirect to /login | Session cleanup verification |
| **TS-0003** | `TS-0003-dashboard-navigation.spec.ts` | Navigation | Sidebar, module access, role-based visibility | Role-based layout rendering |

### TS-0010–TS-0035: Core Business Workflows

| Suite | Filename | Primary CU(s) | Business Domain | Workflow Type | Value |
|-------|----------|---------------|------------------|---------------|-------|
| **TS-0010** | `TS-0010-presupuesto-workflow.spec.ts` | CU01, CU39, CU45, CU49, CU55, CU60 | Presupuestos | Budget lifecycle: create → modify → view → archive | **HIGH** |
| **TS-0011** | `TS-0011-gestiones-crud-workflow.spec.ts` | CU02, CU13, CU14, CU16, CU19, CU53 | Gestiones | Case management: create → history → state → archive | **HIGH** |
| **TS-0012** | `TS-0012-documentacion-testimonio-workflow.spec.ts` | CU03–CU12 | Documentación | Document registration → testimony generation/verification → inscription | **HIGH** |
| **TS-0013** | `TS-0013-escrituras-signing-workflow.spec.ts` | CU05, CU06, CU52, CU63 | Escrituras | Deed preparation → signing → modification → search | **HIGH** |
| **TS-0014** | `TS-0014-pagos-workflow.spec.ts` | CU15, CU47 | Pagos | Payment processing + consultation | **HIGH** |
| **TS-0015** | `TS-0015-personas-clientes-workflow.spec.ts` | CU17, CU18, CU21, CU41, CU46, CU54, CU61 | Personas/Clientes | Entity lifecycle: create → modify → view → search (personas + clients) | **HIGH** |
| **TS-0016** | `TS-0016-usuarios-escribanos-workflow.spec.ts` | CU20, CU21, CU23, CU48, CU51 | Usuarios/Escribanos | User & notary lifecycle: create → modify → activity log | **HIGH** |
| **TS-0017** | `TS-0017-suplencias-workflow.spec.ts` | CU22, CU59 | Suplencias | Proxy/substitution: register → consult | **MEDIUM** |
| **TS-0018** | `TS-0018-reingreso-documentacion-workflow.spec.ts` | CU43 | Reingresar Documentación | Document re-entry workflow | **MEDIUM** |
| **TS-0019** | `TS-0019-inmuebles-valuacion-workflow.spec.ts` | CU69 | Inmuebles | Property management: valuation fiscal tracking | **MEDIUM** |
| **TS-0020** | `TS-0020-reportes-admin-workflow.spec.ts` | CU24–CU32, CU57–CU68 | Administración | Reports, catalogs: índices, tipos, folios, conceptos, estados, users | **HIGH** |
| **TS-0021** | `TS-0021-workflow-editor-admin.spec.ts` | CU70, CU71 | Workflow Admin | State definition + transition editor (workflow configuration) | **HIGH** |
| **TS-0022** | `TS-0022-workflow-assignment-admin.spec.ts` | CU73 | Workflow Admin | Assign workflow to TipoDeTramite | **MEDIUM** |
| **TS-0023** | `TS-0023-roles-permisos-admin.spec.ts` | CU43.1 | Seguridad Admin | Role & permission management | **MEDIUM** |
| **TS-0024** | `TS-0024-admin-module-smoke.spec.ts` | CU20, CU26–CU32, CU57–CU67 | Administración | Smoke test: every admin section loads, create button responds | **MEDIUM** |
| **TS-0028** | `TS-0028-gestion-historial-feature.spec.ts` | CU13 | Gestiones Feature | Bitácora (audit trail / history view) for gestión | **MEDIUM** |
| **TS-0029** | `TS-0029-gestion-estado-transition-feature.spec.ts` | CU83 | Gestiones Feature | State transition workflow (cambiar estado) with validation | **MEDIUM** |
| **TS-0031** | `TS-0031-testimonio-generacion-verificacion-feature.spec.ts` | CU07, CU08 | Documentación Feature | Testimony generation + verification (golden path + edge cases) | **HIGH** |
| **TS-0032** | `TS-0032-testimonio-inscripcion-feature.spec.ts` | CU11, CU12, CU44 | Documentación Feature | Testimony inscription circuit (enter → register → withdraw) | **HIGH** |
| **TS-0033** | `TS-0033-documentos-entidades-externas-feature.spec.ts` | CU10 | Documentación Feature | External entity document tracking | **MEDIUM** |
| **TS-0035** | `TS-0035-workflow-tracker-visualization-feature.spec.ts` | CU70, CU71 | Dashboard Feature | Animated workflow tracker on dashboard landing | **LOW** |

### TS-0040–TS-0050: Quality Assurance & Technical

| Suite | Filename | Focus | Test Type | Priority |
|-------|----------|-------|-----------|----------|
| **TS-0040** | `TS-0040-l10n-language-switching-qa.spec.ts` | Localization (i18n) | Language switcher: ES ↔ EN | Medium |
| **TS-0041** | `TS-0041-responsive-viewport-qa.spec.ts` | Responsive design | Mobile (320px) + tablet (768px) + desktop (1024px+) | Medium |
| **TS-0042** | `TS-0042-accessibility-search-labels-qa.spec.ts` | WCAG accessibility (#608) | Label association (screen reader): 9 search inputs | Medium |
| **TS-0043** | `TS-0043-icons-ux-qa.spec.ts` | Icon rendering | PNG icon load verification across all pages | Low |
| **TS-0044** | `TS-0044-security-csrf-cors-qa.spec.ts` | Security posture (#691) | CSRF + CORS enforcement (JWT Bearer pattern) | High |
| **TS-0045** | `TS-0045-breadcrumb-navigation-qa.spec.ts` | Navigation component (#434) | Breadcrumb trail in admin module | Low |
| **TS-0050** | `TS-0050-cu-health-matrix-smoke.spec.ts` | Smoke / health check | CU endpoint health matrix (all 68 CU endpoints respond) | Medium |

### TS-0060, TS-0070–TS-0071: Regression & Tutorials

| Suite | Filename | Scope | Value |
|-------|----------|-------|-------|
| **TS-0060** | `TS-0060-full-application-tour-regression.spec.ts` | Complete end-to-end tour: login → all modules → logout (create/edit/delete) | **HIGHEST** (catch regressions) |
| **TS-0070** | `TS-0070-supervised-tour-tutorial.spec.ts` | Human-watchable, paced tour for demos / recording | Reference |
| **TS-0071** | `TS-0071-first-case-tutorial-onboarding.spec.ts` | Learner-friendly: CU01–CU05, recording-ready (TUTORIAL_PAUSE_MS) | Onboarding |

---

## 3. Fixture Patterns & Test Architecture

### Fixture Framework: GherkinSteps + API Helpers

Every E2E suite uses a **Given-When-Then (Gherkin)** pattern via the `GherkinSteps` class for readability and consistency:

```typescript
/**
 * CU01 - Preparar Presupuesto
 * Test CU references: https://docs.notaire.app/casos-de-uso/cu01
 * Sequence diagram: docs/200-architecture/204-diagrams/Secuencias/CU01.puml
 * 
 * Fixtures:
 * - GherkinSteps (Given-When-Then helpers)
 * - API create helpers: createPersona(), createPresupuesto()
 * - Global auth: global-setup.ts injects E2E_ADMIN_TOKEN
 */
import { test, expect } from "@playwright/test";
import { GherkinSteps } from "./gherkin-helpers";

test.describe("TS-0010 - Presupuesto Workflow", () => {
  let steps: GherkinSteps;

  test.beforeEach(async ({ page }) => {
    steps = new GherkinSteps(page);
    await steps.givenUserIsLoggedIn();
    await steps.givenUserIsOnPage("/dashboard/presupuestos");
  });

  test("GW01: Create presupuesto from modal", async () => {
    // Given
    await steps.givenModuleIsVisible("Presupuestos");
    
    // When
    await steps.whenUserClicksButton("nuevo presupuesto");
    
    // Then
    await steps.thenModalIsVisible("Nuevo Presupuesto");
    await steps.thenFormHasField("fecha");
  });
});
```

### Fixture Components

| Component | Location | Purpose |
|-----------|----------|---------|
| **GherkinSteps** | `frontend/tests/e2e/gherkin-helpers.ts` | Given-When-Then step definitions (login, modal, form, table assertions) |
| **API Helpers** | `frontend/tests/e2e/setup/api-helpers.ts` | Shared functions: `createPersona()`, `createPresupuesto()`, `seedGestion()` |
| **Global Setup** | `frontend/tests/e2e/setup/global-setup.ts` | Runs once per worker: login as admin, extract JWT to `process.env.E2E_ADMIN_TOKEN` |
| **Global Teardown** | `frontend/tests/e2e/setup/global-teardown.ts` | Cleanup after suite (close browser, audit logs) |
| **Auth Helper** | `frontend/tests/e2e/setup/auth.ts` | Inject cached token or re-login (`authenticateAsAdmin()`) |

### Fixture Validation Rules

Every test suite **MUST**:
1. ✅ Use `GherkinSteps` for UI navigation (not inline `page.click()`)
2. ✅ Use API helpers (not browser-based form filling for expensive operations)
3. ✅ Have `test.beforeEach()` that prepares the page (login, navigate, seed data)
4. ✅ Include header comments with CU reference(s) + sequence diagram link
5. ✅ Use `await expect(...)` assertions (Playwright built-in, not custom)
6. ✅ No hardcoded auth tokens (use `process.env.E2E_ADMIN_TOKEN` from global-setup)
7. ✅ No tests with `test.skip()` without a SOLID justification comment
8. ✅ One logical behavior per test (AAA pattern: Arrange → Act → Assert)

---

## 4. Test Consolidation Strategy

### Rationale for Consolidation

To minimize E2E test count (lower runtime, higher ROI per test), 6 low-value tests were consolidated into their parent workflow suites:

| Consolidated From | Merged Into | Reason |
|-------------------|-------------|--------|
| `cu70-workflow-viewer.spec.ts` | **TS-0021** (workflow-editor) | Viewer is read-only variant; editor tests cover both paths |
| `presupuestos.spec.ts` (module CRUD) | **TS-0010** (presupuesto-workflow) | Duplicate coverage; TS-0010 has golden path + gherkin |
| `personas.spec.ts` (module CRUD) | **TS-0015** (personas-clientes-workflow) | Duplicate coverage; TS-0015 has full lifecycle |
| `suplencias.spec.ts` (module view) | **TS-0017** (suplencias-workflow) | Duplicate coverage; TS-0017 is complete |
| `escritura-firma.spec.ts` (edge cases) | **TS-0013** (escrituras-signing-workflow) | Edge paths now in TS-0013; low ROI as standalone |
| `crud-gestiones.spec.ts` (CRUD duplicate) | **TS-0011** (gestiones-crud-workflow) | Exact duplicate; TS-0011 is canonical |

**Result**: 41 files → 35 organized suites (6 deletions, 0 new tests added).

---

## 5. Test Levels & Coverage Pyramid

Per [`CONSTITUTION.md`](../../../.claude/rules/ai-agent-workflow.md), all changes flow through:

```
Unit (80% target) → Integration (80% target) → API (Bruno) → Frontend unit (Vitest) → E2E (Playwright, per CU)
```

| Level | Validates | Count |
|-------|-----------|-------|
| **Unit** | Single class/method (mocked dependencies) | Backend: `src/test/java/.../unit/` |
| **Integration** | Spring context + repo/service, real PostgreSQL | Backend: `.../integration/` |
| **API (Bruno)** | Real HTTP contract of every endpoint | `backend-api/api-test/` (~70 requests) |
| **Frontend unit (Vitest)** | React components/hooks in isolation | `frontend/src/**/*.test.ts` |
| **E2E (Playwright)** | Full Use Case through the actual browser UI | `frontend/tests/e2e/TS-nnnn-*.spec.ts` (35 suites, 448 tests) |

---

## 6. Running & Validating Tests

### Local Commands

```bash
# Run all E2E tests (35 suites, ~448 tests)
npm run test:e2e

# Run single test suite
npx playwright test TS-0001-login-authentication

# Run with headed browser (watch)
HEADED=1 npx playwright test TS-0010-presupuesto-workflow

# Run with debug inspector
npx playwright test --debug

# Generate HTML report
npx playwright show-report

# Full CI preflight (includes E2E + API + Docker build)
bash scripts/preflight.sh --full
```

### CI Pipeline

The GitHub Actions pipeline verifies all test levels before merge:
1. Build + unit tests
2. Integration tests (PostgreSQL)
3. API tests (Bruno)
4. E2E Playwright (35 suites)
5. Frontend unit (Vitest)
6. Security (Trivy)

See [`CI-PREFLIGHT.md`](../CI-PREFLIGHT.md) for local↔CI mapping.

---

## 7. Test Reporting & Metrics

| Report | Source | Location |
|--------|--------|----------|
| E2E results (Playwright HTML) | `npm run test:e2e` | `frontend/playwright-report/index.html` |
| E2E trace/artifacts | On failure | `test-results/` (traces, videos, screenshots) |
| Coverage (JaCoCo backend) | `mvn jacoco:report` | `backend-api/target/site/jacoco/` |
| Coverage (Vitest frontend) | `npm run test:coverage` | `frontend/coverage/` |
| API results (Bruno) | `bru run . -r` | `backend-api/api-test/COVERAGE.md` |

**Target coverage**: 80% line / 80% branch (enforced ratchet floor: 70% line / 25% branch).

---

## 8. Traceability: CU → Test Suite → Endpoints

Full traceability is maintained in [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv):

- **Column A**: CU ID (CU01–CU68+)
- **Column B**: CU Name
- **Column C**: REST Endpoint(s)
- **Column D**: Endpoint Status (implemented ✓ / missing ✗)
- **Column E**: Bruno API Test
- **Column F**: Bruno Status
- **Column G**: Playwright E2E Suite (TS-nnnn reference)
- **Column H**: E2E Status (passing ✓ / skipped ⚠)
- **Column I**: GitHub Issue #
- **Column J**: Notes

Use this CSV to:
1. Verify every CU has ≥1 E2E test (TS-nnnn)
2. Ensure consistency: endpoint → Bruno → E2E
3. Track issues to features
4. Audit test completeness per CU

---

## 9. Sequence Diagrams & Test Choreography

Each major workflow (TS-0010 through TS-0035) references sequence diagrams at:

```
docs/200-architecture/204-diagrams/Secuencias/
├── CU01.puml (Presupuesto)
├── CU02.puml (Gestiones)
├── CU05.puml (Escrituras)
├── CU07.puml (Testimonio)
└── ...
```

Test comments include `@sequence` links to relevant diagrams so test readers can see the actor flow:
```typescript
/**
 * TS-0010 - Presupuesto Workflow
 * Sequence: docs/200-architecture/204-diagrams/Secuencias/CU01.puml
 * Flow: Usuario → [create presupuesto] → backend API → database → response
 */
```

---

## 10. Skipped Tests Policy

Tests **MAY** be skipped only if:
1. A linked GitHub issue documents the blocker (e.g., #838: no endpoint for folio→escritura linking)
2. A clear, descriptive `test.skip()` comment explains the reason
3. It is not a design gap but a genuine technical limitation

**Example**:
```typescript
test.skip("CU01-GW02: Create presupuesto with all fields", async () => {
  // Skipped: presupuesto form does not have a "tipo tramite" dropdown per design.
  // Issue #XXX tracks form field alignment.
  // Re-enable once API and form schema are updated.
});
```

**Avoid**:
- ❌ `test.skip()` with no comment
- ❌ Skipped tests for flaky timing (fix the test, not skip it)
- ❌ Skipped tests for "future work" (remove them or implement)

---

## 11. Quality Gates for E2E Tests

Before merging a PR, all E2E tests **MUST**:
- ✅ Pass on the current Docker stack (no manual restarts)
- ✅ Have zero skipped tests (unless issue-linked per §10)
- ✅ Follow fixture patterns (GherkinSteps, no hardcoded auth)
- ✅ Include CU reference comments + sequence diagram links
- ✅ Have valid, specific assertions (no generic `expect(page).toBeTruthy()`)

Violations block merge. Use `/code-review` to audit compliance.

---

## Navigation

- [← Testing](README.md)
- [Development Plan](../DEVELOPMENT-PLAN.md)
- [Testing Patterns](TESTING-PATTERNS.md) ⭐ **NEW** - Fixture patterns & concrete examples
- [E2E Test Mapping](E2E-TEST-MAPPING.md) (detailed TS-nnnn reference)
- [CU-API-MATRIX.csv](CU-API-MATRIX.csv)
- [Sequence Diagrams](../../200-architecture/204-diagrams/Secuencias/)
