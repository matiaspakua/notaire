# E2E Test Mapping — TS-nnnn Nomenclature & Traceability

**Complete reference** for all 35 organized E2E test suites (consolidated from 41 original files).

Generated: 2026-08-31 | Updated: TEST-PLAN.md TS-nnnn table
Use this document alongside [`TEST-PLAN.md`](TEST-PLAN.md) and [`CU-API-MATRIX.csv`](CU-API-MATRIX.csv).

---

## TS-nnnn Numbering Scheme

```text
TS-0001 to TS-0003    = Foundation (Auth, Dashboard)
TS-0010 to TS-0035    = Core Business Workflows (26 high-value suites)
TS-0040 to TS-0051    = Quality Assurance / Technical (9 utility suites)
TS-0060, TS-0070-0071 = Regression & Tutorials (3 reference suites)
```

---

## Quick Lookup: CU → TS-nnnn

| CU Range | TS Suites | Domain |
|----------|-----------|--------|
| AUTH-001 / CU84 | TS-0001, TS-0002, TS-0093 | Authentication / session expiry |
| CU78 (frontend admin guard) | TS-0094 | Admin route access denial for non-admins |
| CU78 (login lockout, #689) | TS-0100 | Account lockout after repeated failed logins |
| CU15 / CU20 (#1054) | TS-0095 | API business errors on mutation toasts / field errors |
| CU76 / WCAG (#1057) | TS-0096 | Icon-only button accessible names (`getByRole` by name) |
| CU01–CU09 | TS-0010 to TS-0014 | Presupuesto, Gestiones, Documentación, Escrituras |
| CU10–CU23 | TS-0015 to TS-0023 | Personas, Usuarios, Suplencias, Reportes |
| CU24–CU68 | TS-0020 to TS-0035 | Administration, Workflows, Features |
| Navigation, QA | TS-0003, TS-0040–0051 | Foundation & Utility |
| Regression | TS-0060, TS-0070–0071 | Full app tour, Tutorials |

---

## Detailed Mapping Table

### Foundation Tests (TS-0001–TS-0003)

| TS | Filename | CU Coverage | Fixture Type | Setup | Assertions | Status |
|----|----|-------|------|-------|----------|--------|
| **TS-0001** | `TS-0001-login-authentication.spec.ts` | AUTH-001 | Form fill, JWT token | Direct navigation | 4 expect() | ✅ Passing |
| **TS-0002** | `TS-0002-logout-authentication.spec.ts` | AUTH-001 | Session clear, redirect | Login + logout flow | 3 expect() | ✅ Passing |
| **TS-0003** | `TS-0003-dashboard-navigation.spec.ts` | Navigation | Role-based layout | Login + sidebar | 4 expect() | ✅ Passing |
| **TS-0093** | `TS-0093-session-expiry.spec.ts` | CU84 (#1053, #690) | Corrupt JWT → real API 401 | Login + dashboard navigate | expired redirect + re-login + viewports | ✅ Passing |
| **TS-0094** | `TS-0094-admin-route-guard.spec.ts` | CU78 (#1052) | Create EMPLEADO via API | Login as non-admin + admin URL | forbidden redirect + message + viewports | ✅ Passing |
| **TS-0100** | `TS-0100-login-account-lockout.spec.ts` | CU78 (#689, #560) | Unique throwaway username | Failed logins via the form | lockout message + stays locked + 320/1024 px | ✅ Passing |
| **TS-0095** | `TS-0095-api-error-toasts.spec.ts` | CU15, CU20 (#1054, #615 slice) | Route stub 4xx ErrorResponse | Login + CRUD mutation | server toast + FormField/aria-invalid + viewports | ✅ Added |
| **TS-0096** | `TS-0096-icon-button-accessible-names.spec.ts` | CU76 / WCAG (#1057) | Admin login + list pages | Usuarios, personas, presupuestos, conceptos | `getByRole('button', { name })` + 320px | ✅ Added |

**Fixture Pattern**: Direct page navigation + form fill (no GherkinSteps needed, simple auth flow)
**Dependencies**: None (foundation for all other tests)
**Consolidation**: No duplicates

---

### Core Business Workflows (TS-0010–TS-0035)

#### Presupuesto & Gestiones (TS-0010–TS-0012)

| TS | Filename | CU Coverage | Fixture Type | Skipped | Assertions | Value |
|----|----|-------|------|-------|----------|-------|
| **TS-0010** | `TS-0010-presupuesto-workflow.spec.ts` | CU01, CU39, CU45, CU49, CU55, CU60 | GherkinSteps, API helpers | 1 (form schema mismatch) | 8 expect() | **HIGH** |
| **TS-0011** | `TS-0011-gestiones-crud-workflow.spec.ts` | CU02, CU13, CU14, CU16, CU19, CU53 | GherkinSteps, API helpers | 1 (form simplified) | 12 expect() | **HIGH** |

**Consolidated From**:

- `presupuestos.spec.ts` → merged into TS-0010 (module CRUD duplicate)
- `crud-gestiones.spec.ts` → merged into TS-0011 (CRUD duplicate)
- `personas.spec.ts` → merged into TS-0015 (module CRUD duplicate)
- `suplencias.spec.ts` → merged into TS-0017 (module view duplicate)

**Fixture Pattern**: GherkinSteps Given-When-Then, createPersona/createPresupuesto API helpers
**Setup**: givenUserIsLoggedIn() + givenUserIsOnPage("/dashboard/...")
**Dependencies**: TS-0001 (login must succeed), API helpers, global-setup auth

#### Documentación, Escrituras, Pagos, Personas (TS-0012–TS-0015)

| TS | Filename | CU Coverage | Fixture Type | Skipped | Value |
|----|----|-------|------|-------|-------|
| **TS-0012** | `TS-0012-documentacion-testimonio-workflow.spec.ts` | CU03–CU12 | GherkinSteps + API | 2 (UI not present) | **HIGH** |
| **TS-0013** | `TS-0013-escrituras-signing-workflow.spec.ts` | CU05, CU06, CU52, CU63 | GherkinSteps + API | 1 (UI not present) | **HIGH** |
| **TS-0014** | `TS-0014-pagos-workflow.spec.ts` | CU15, CU47 | GherkinSteps | 2 intentional gaps cite #1146 | **HIGH** |
| **TS-0015** | `TS-0015-personas-clientes-workflow.spec.ts` | CU17, CU18, CU21, CU41, CU46, CU54, CU61; #945 Dedup-EDGE (empty DNI → validation feedback) | GherkinSteps + API | 1 (UI flow changed) | **HIGH** |

**Consolidated From**:

- `escritura-firma.spec.ts` → edge cases merged into TS-0013
- `cu70-workflow-viewer.spec.ts` → removed (read-only variant of editor)

#### Usuarios, Suplencias, Reingreso, Inmuebles (TS-0016–TS-0019)

| TS | Filename | CU Coverage | Fixture Type | Skipped | Value |
|----|----|-------|------|-------|-------|
| **TS-0016** | `TS-0016-usuarios-escribanos-workflow.spec.ts` | CU20, CU21, CU23, CU48, CU51 | GherkinSteps + API | 3 intentional gaps cite #1146 (CU21 unskipped #1057) | **HIGH** |
| **TS-0017** | `TS-0017-suplencias-workflow.spec.ts` | CU22, CU59 | Sidebar nav (`nav-suplencias`) + GherkinSteps | 2 intentional gaps cite #1146 | **MEDIUM** |
| **TS-0018** | `TS-0018-reingreso-documentacion-workflow.spec.ts` | CU43 | Direct nav + assertions | 0 | **MEDIUM** |
| **TS-0019** | `TS-0019-inmuebles-valuacion-workflow.spec.ts` | CU69 | GherkinSteps | 0 | **MEDIUM** |

#### Administration & Workflows (TS-0020–TS-0035)

| TS | Filename | CU Coverage | Fixture Type | Skipped | Value |
|----|----|-------|------|-------|-------|
| **TS-0020** | `TS-0020-reportes-admin-workflow.spec.ts` | CU24–CU32, CU57–CU68 | Sidebar nav (`nav-reportes`) + GherkinSteps + API | 7 intentional gaps cite #1146 | **HIGH** |
| **TS-0021** | `TS-0021-workflow-editor-admin.spec.ts` | CU70, CU71 | API arrange + editor | 0 (self-seeds workflow; #1066) | **HIGH** |
| **TS-0022** | `TS-0022-workflow-assignment-admin.spec.ts` | CU73 | API arrange + table | 0 (self-seeds tipo-tramite; #1066) | **MEDIUM** |
| **TS-0023** | `TS-0023-roles-permisos-admin.spec.ts` | CU43.1 | Direct nav + form | 0 | **MEDIUM** |
| **TS-0024** | `TS-0024-admin-module-smoke.spec.ts` | CU20–CU32, CU57–CU67 | Direct nav + buttons | 0 | **MEDIUM** |
| **TS-0028** | `TS-0028-gestion-historial-feature.spec.ts` | CU13 | GherkinSteps + API seed | 0 | **MEDIUM** |
| **TS-0029** | `TS-0029-gestion-estado-transition-feature.spec.ts` | CU83 | GherkinSteps + API seed | 0 | **MEDIUM** |
| **TS-0031** | `TS-0031-testimonio-generacion-verificacion-feature.spec.ts` | CU07, CU08 | GherkinSteps + API | 0 | **HIGH** |
| **TS-0032** | `TS-0032-testimonio-inscripcion-feature.spec.ts` | CU11, CU12, CU44 | GherkinSteps + API | 0 | **HIGH** |
| **TS-0033** | `TS-0033-documentos-entidades-externas-feature.spec.ts` | CU10 | GherkinSteps + API | 0 | **MEDIUM** |
| **TS-0035** | `TS-0035-workflow-tracker-visualization-feature.spec.ts` | CU70, CU71 | Direct nav + assertions | 0 | **LOW** |

---

### Quality Assurance & Technical Tests (TS-0040–TS-0051)

| TS | Filename | Focus | Test Type | Priority | Value |
|----|----|-------|---------|----------|-------|
| **TS-0040** | `TS-0040-l10n-language-switching-qa.spec.ts` | i18n (ES ↔ EN); #1059 EN titles on roles/workflows | Language switcher + gap pages | Medium | Utility |
| **TS-0041** | `TS-0041-responsive-viewport-qa.spec.ts` | Responsive design | Mobile (320px) + tablet (768px) + desktop (1024px) | Medium | Utility |
| **TS-0042** | `TS-0042-accessibility-search-labels-qa.spec.ts` | WCAG accessibility | Label association (#608) | Medium | Utility |
| **TS-0043** | `TS-0043-icons-ux-qa.spec.ts` | Icon rendering | PNG load verification | Low | Utility |
| **TS-0044** | `TS-0044-security-csrf-cors-qa.spec.ts` | Security posture | CSRF + CORS (#691) | High | Utility |
| **TS-0045** | `TS-0045-breadcrumb-navigation-qa.spec.ts` | Navigation component | Breadcrumb trail (#434) | Low | Utility |
| **TS-0050** | `TS-0050-cu-health-matrix-smoke.spec.ts` | Smoke / health check | CU endpoint matrix | Medium | Health |
| **TS-0051** | `TS-0051-api-full-cycle-integration.spec.ts` | API connectivity | GET → POST → PUT → DELETE | Medium | Integration |

**Fixture Pattern**: Direct navigation + specialized assertions (no GherkinSteps)
**Use**: Run on demand, less critical than core workflows
**Consolidation**: No changes (low duplicates in utility tests)

---

### Regression & Tutorial Tests (TS-0060, TS-0070–TS-0071, TS-0090)

| TS | Filename | Scope | Test Type | Value | Run |
|----|----|-------|---------|-------|-----|
| **TS-0060** | `TS-0060-full-application-tour-regression.spec.ts` | Complete E2E tour | Regression / smoke | **HIGHEST** | Every PR |
| **TS-0070** | `TS-0070-supervised-tour-tutorial.spec.ts` | Paced walkthrough via sidebar (incl. Suplencias/Reportes; Items/Auditoría canonical) | Human-watchable demo | Reference | On demand |
| **TS-0071** | `TS-0071-first-case-tutorial-onboarding.spec.ts` | Learner-friendly | Recording demo (CU01–CU05) | Onboarding | On demand |
| **TS-0090** | `TS-0090-demo-two-full-cases.spec.ts` | Seeds two comparable full cases via UI | Live-demo data seed | Reference | On demand |

**TS-0060 Value**: Catches regressions across all modules in single test (highest ROI for full regression)
**TS-0070/0071 Value**: Recording-ready demos for training, onboarding, documentation
**TS-0090 Value**: Populates a dev stack with two side-by-side comparable cases before a live demo

---

### Feature Tests — Items (TS-0091)

| TS | Filename | CU Coverage | Fixture Type | Setup | Assertions | Status |
|----|----|-------|------|-------|----------|--------|
| **TS-0091** | `TS-0091-items-descuentos-recargos.spec.ts` | CU45, CU71 | GherkinSteps | Direct navigation | Client-side validation + report query | ✅ Passing |

---

## Fixture Patterns by Category

### Pattern 1: GherkinSteps (Given-When-Then)

**Used by**: TS-0010–0035 (all core workflows)

```typescript
let steps: GherkinSteps;
test.beforeEach(async ({ page }) => {
  steps = new GherkinSteps(page);
  await steps.givenUserIsLoggedIn();
  await steps.givenUserIsOnPage("/dashboard/xxx");
});

test("scenario", async () => {
  await steps.givenModuleIsVisible("XXX");
  await steps.whenUserClicksButton("action");
  await steps.thenModalIsVisible();
});
```

**Fixture Components**:

- `GherkinSteps` class (given-helpers.ts)
- API helpers: `createPersona()`, `createPresupuesto()`, etc. (setup/api-helpers.ts)
- Global auth: `global-setup.ts` injects `E2E_ADMIN_TOKEN`

### Pattern 2: Direct Navigation (No GherkinSteps)

**Used by**: TS-0001–0003, TS-0040–0051, TS-0060–0071

```typescript
test.beforeEach(async ({ page }) => {
  await page.goto("/login");
  // OR: use authenticateAsAdmin() from setup/auth.ts
});

test("scenario", async ({ page }) => {
  await page.getByTestId("xxx").click();
  await expect(page.getByText("yyy")).toBeVisible();
});
```

**Justification**: TS-0001–0003 (auth, simple paths), TS-0040+ (utility, not workflows)

---

## Skipped Tests Justification

### Live inventory — intentional feature-gap skips (#1146 / CU76)

**Fourteen** static `test.skip` declarations remain in TS-0014 / TS-0016 /
TS-0017 / TS-0020. Each skip title cites `#1146` (tracker hygiene). **Product
delivery stays on the owning CU** — do not unskip without real UI assertions.
CU21 edit is **not** in this inventory (unskipped in #1057). Vitest
`scripts/test_e2e_reliability.py` enforces `#\d+` citations and the count `14`.

| TS | Skip id | Owning CU | Product gap (summary) | Citation | Product tracking |
|----|---------|-----------|-----------------------|----------|------------------|
| TS-0014 | CU15-GW03 | CU15 | Pagos row has no “ver detalle” control | #1146 | Owning CU15 (UI detail action) |
| TS-0014 | CU47-GW01 | CU47 | Pagos page has no fecha desde/hasta filters | #1146 | Owning CU47 (date filter UI) |
| TS-0016 | CU23-GW01 | CU23 | Usuarios page has no “ver actividades” | #1146 | Owning CU23 (activities UI) |
| TS-0016 | CU48-GW01 | CU48 | No `/dashboard/administracion/escribanos` page | #1146 | Owning CU48 (escribanos CRUD) |
| TS-0016 | CU48-GW02 | CU48 | Same — create escribano flow absent | #1146 | Owning CU48 |
| TS-0017 | CU59-GW01 | CU59 | Suplencias has no escribano filter | #1146 | Owning CU59 (filter UI) |
| TS-0017 | CU59-GW02 | CU59 | Suplencias table has no “ver” detail | #1146 | Owning CU59 (detail UI) |
| TS-0020 | CU24-GW01 | CU24 | Reportes lacks “libro índices” named action | #1146 | Owning CU24 (report action/copy) |
| TS-0020 | CU25-GW01 | CU25 | Reportes lacks mes / declaración jurada UI | #1146 | Owning CU25 |
| TS-0020 | CU39-GW01 | CU39 | Presupuestos lacks “plantillas” button | #1146 | Owning CU39 |
| TS-0020 | CU42-GW01 | CU42 | Dashboard lacks alertas / vencimientos | #1146 | Owning CU42 |
| TS-0020 | CU62-GW01 | CU62 | Escrituras page has no search bar | #1146 | Owning CU62 |
| TS-0020 | CU65-GW01 | CU65 | Tipos documento page has no search | #1146 | Owning CU65 |
| TS-0020 | CU66-GW01 | CU66 | Conceptos page has no search | #1146 | Owning CU66 |

### Resolved / not counted as feature-gap debt

| TS | Former skip | Status |
|----|-------------|--------|
| TS-0016 | CU21 edit (icon-only unnamed) | Unskipped — #1057 (`aria-label` + `getByRole`) |
| TS-0021 | Conditional empty-table skip | Fixed — #1066 (`createWorkflowDefinition`) |
| TS-0022 | Conditional empty-table skip | Fixed — #1066 (`createTipoTramite`) |
| TS-0010–TS-0015 historical design skips | Formerly listed in this doc | No static `test.skip` on current `main` |

**Policy**: Intentional feature-gap skips MUST cite an open GitHub issue in the
skip title/reason (see #1146). Runtime skips for missing seed data are forbidden
(#1066). Flake triage uses Playwright `trace: on-first-retry`, failure
screenshots/video, and CI artifacts — CI retries are capped at **1**. When a
product PR unskips a row, update this table **and** the Vitest inventory count
in the same change.

---

## Test Consolidation Details

**Files Deleted (Content Merged)**:

1. **`crud-gestiones.spec.ts`** → TS-0011-gestiones-crud-workflow
   - Duplicate: `cu02-gestiones.spec.ts` was canonical
   - Merged: No unique tests in crud variant

2. **`presupuestos.spec.ts`** → TS-0010-presupuesto-workflow
   - Duplicate: `cu01-presupuesto.spec.ts` had Gherkin pattern
   - Merged: Module CRUD tests already covered by TS-0010

3. **`personas.spec.ts`** → TS-0015-personas-clientes-workflow
   - Duplicate: `cu17-18-personas-clientes.spec.ts` was canonical
   - Merged: Module CRUD coverage overlapped

4. **`suplencias.spec.ts`** → TS-0017-suplencias-workflow
   - Duplicate: `cu22-suplencias.spec.ts` had Gherkin pattern
   - Merged: Module view tests subsumed

5. **`escritura-firma.spec.ts`** → TS-0013-escrituras-signing-workflow
   - Consolidation: Edge path tests (sin folio, ya firmada)
   - Merged: Signing workflow + edge cases now in TS-0013

6. **`cu70-workflow-viewer.spec.ts`** → DELETED
   - Rationale: Read-only variant; editor tests (TS-0021) cover both paths
   - No content merge needed (UI is subset of editor)

---

## Test Counts & Statistics

```text
Total E2E Test Suites:        35 (down from 41)
  - Foundation (TS-0001–0003):     3 suites
  - Core Workflows (TS-0010–0035): 26 suites
  - QA / Utility (TS-0040–0051):   9 suites
  - Regression/Tutorial (TS-0060, 0070–0071): 3 suites

Estimated Test Cases:          448 (unchanged — no unique tests deleted)
Skipped Tests:                 11 (all with documented blockers)
Fixture Patterns:
  - GherkinSteps (Given-When-Then): 26 suites
  - Direct navigation:              9 suites
  - No setup required:              0 suites

Before Consolidation:  41 files (duplicate/low-value variants)
After Consolidation:   35 files (organized by workflow, no redundancy)
Reduction:             14% fewer files, same test count, better traceability
```

---

## Cross-References

- **[TEST-PLAN.md](TEST-PLAN.md)**: Master testing document (levels, fixtures, running commands)
- **[CU-API-MATRIX.csv](CU-API-MATRIX.csv)**: CU → REST endpoint → Bruno → E2E traceability
- **[Sequence Diagrams](../../200-architecture/204-diagrams/Secuencias/)**: Actor workflows for each CU
- **[FRONTEND-TESTING-GUIDE.md](FRONTEND-TESTING-GUIDE.md)**: Vitest + Playwright conventions
- **[CI-PREFLIGHT.md](../CI-PREFLIGHT.md)**: Local↔CI command mapping
- **Frontend E2E Suites**: `testing/e2e/tests/TS-*.spec.ts`
- **Setup/Helpers**: `testing/e2e/tests/setup/` (global-setup, auth, api-helpers)
- **Gherkin Steps**: `testing/e2e/tests/gherkin-helpers.ts`

---

## Maintenance Notes

- **Keep TS-nnnn numbering**: Enables fast lookup, references in TEST-PLAN.md stable
- **Archive skipped tests only if blocker resolved**: Update justification comments
- **Add new workflows as TS-00nn**: Extend numbering (e.g., TS-0036, TS-0037)
- **Update this map when**: Consolidating duplicates, archiving low-value tests, adding new suites
- **Sync with OpenSpec**: Every TS-nnnn rename → update openspec/changes/*/traceability.md
