# Complete Blockers List & Prioritized Fix Plan

**Generated:** 2026-09-01 08:35 CEST  
**Purpose:** Fix all demo E2E blockers using full CONSTITUTION.md workflow  
**Target:** 2 complete, comparable use cases (Case A & Case B) end-to-end in single demo run

---

## Status Summary

### ✅ Fixed This Session (Immediate)
- #892 — Escritura folio picker (merged PR #894)
- #879 — Inmueble valuacionFiscal type (merged PR #882)
- #883 — Presupuesto client association (merged PR #887)
- #889 — Gestión presupuesto picker labels (merged PR #890)
- **UI testid fixes** — Inmuebles page buttons + form fields
- **Demo script bugs** — Gestión number input type handling

### 🔧 Actively Being Fixed (Demo Run #3)
- Demo re-running with all fixes applied
- Expected to progress further into workflow

### ⏳ Known Blockers (Must Fix for Demo Completion)

| Priority | Issue | CU | Blocker | Status | Effort | Workflow |
|----------|-------|----|---------|---------| --------|----------|
| 🔴 P0 | #796 | CU15 | Payment form lacks saldo display | Spec exists | 2–4h | Apply spec |
| 🟡 P1 | Demo TBD | CU09 | Testimonio post-firma workflow | Unknown | TBD | Issue + spec |
| 🟡 P1 | #848 | CU15 | Payment validation (no overpay) | Spec exists | 1–2h | Apply spec |
| 🟡 P1 | #169 | CU16 | Archive requires deuda check | Unknown | 2–3h | Issue + spec |
| 🟡 P2 | Demo TBD | CU08/47 | Copia/Documentación integration | Unknown | TBD | Issue + spec |

---

## Detailed Blockers Analysis

### ISSUE #796 — Payment Saldo Visibility (HIGHEST PRIORITY)

**Status:** 📄 OpenSpec spec file exists (`openspec/changes/pago-presupuesto-picker-saldo/`)

**Description:** Payment registration form doesn't show how much is owed before charging. User must guess or check elsewhere.

**Blocker for:** Demo Step 9 (Process Payment) — form missing saldo field visibility

**Current Code State:**
- `PagoController.java` — accepts presupuesto ID but doesn't return saldo
- `frontend/src/app/dashboard/pagos/page.tsx` — has form but no saldo picker
- No endpoint to fetch presupuesto balance

**Fix Scope:**
1. Add saldo calculation to `PagoService` (monto_presupuesto - SUM(pagos))
2. Create `/api/presupuestos/{id}/saldo` endpoint
3. Update Pago form to fetch saldo when presupuesto selected
4. Display saldo visibly to user

**Effort:** 2–4 hours  
**Workflow:** OpenSpec apply → TDD → Implement → Refactor → Test → Docs → PR → Merge

**Files to Modify:**
- `backend-api/src/main/java/.../PagoService.java` (add saldo logic)
- `backend-api/src/main/java/.../PagoController.java` (add endpoint)
- `frontend/src/hooks/usePresupuestos.ts` (add saldo fetch)
- `frontend/src/app/dashboard/pagos/page.tsx` (display saldo)
- `frontend/tests/e2e/TS-0014-pagos-workflow.spec.ts` (update test)

---

### ISSUE #848 — Payment Overpayment Validation (MEDIUM PRIORITY)

**Status:** 📄 OpenSpec spec exists (`openspec/changes/pago-limite-saldo-pendiente/`)

**Description:** User can accidentally pay more than owed. System accepts it. No protection.

**Blocker for:** Data integrity; prevents demo accidents during live demo

**Current Code State:**
```java
// PagoService.java:29-73
procesarPago(idPresupuesto, monto) {
    if (monto <= 0) throw error;  // ← Only checks > 0, not ≤ saldo
    // ... saves payment
}
```

**Fix Scope:**
1. Add saldo check to `PagoService.procesarPago`
2. Return validation error if monto > saldo
3. Update API response with validation info
4. Frontend shows validation error before submit

**Effort:** 1–2 hours  
**Workflow:** OpenSpec apply → TDD → Implement → Test → PR → Merge

**Files to Modify:**
- `backend-api/src/main/java/.../PagoService.java` (add validation)
- `backend-api/src/test/.../PagoServiceTest.java` (add test)
- `frontend/src/app/dashboard/pagos/page.tsx` (handle validation response)

---

### UNKNOWN BLOCKER — Testimonio Post-Firma Integration

**Status:** ❓ Requires demo output to diagnose

**Description:** After escritura is signed, system must allow testimonio creation linked to that escritura. Unclear if this workflow is connected.

**Blocker for:** Demo Step 8 (Create Testimonio)

**Expected Workflow:**
1. Escritura created & signed
2. Testimonio created with reference to escritura
3. Testimonio records details of notarized document

**Test Coverage:**
- `frontend/tests/e2e/TS-0012-documentacion-testimonio-workflow.spec.ts` (partially implemented, some steps skipped)

**Action:** 
1. Run demo test #3 to see exact error
2. Create Issue #XXX if not already covered
3. Create OpenSpec spec with Given/When/Then
4. Follow CONSTITUTION.md TDD workflow

**Effort:** 3–6 hours (depends on scope once discovered)

---

### UNKNOWN BLOCKER — Gestión Archive with Deuda Check (#169)

**Status:** 📝 Issue exists (#169), no spec created yet

**Description:** RFC-37 requires "warning of pending debt before archiving gestión" but system allows archive regardless of balance.

**Blocker for:** Demo Step 10 (Archive Gestión)

**Current Code State:**
```java
// GestionService.java — archivar() is state-change only
public void archivar(idGestion) {
    gestion.estado = "Archivado";
    save(gestion);
    // ← No deuda verification!
}
```

**Fix Scope:**
1. Add deuda check before allowing archive
2. Calculate: presupuesto - sum(pagos) = deuda
3. If deuda > 0, warn user or block archive
4. Update tests to verify this

**Effort:** 2–3 hours  
**Workflow:** Issue → OpenSpec spec (Gate 1) → TDD → Implement → Test → Docs → PR → Merge

**Files to Modify:**
- `backend-api/src/main/java/.../GestionService.java` (add deuda check)
- `backend-api/src/test/.../GestionServiceTest.java` (test scenarios)
- `frontend/src/app/dashboard/gestiones/page.tsx` (show warning)
- `frontend/tests/e2e/TS-0011-gestiones-crud-workflow.spec.ts` (update test)

---

### UNKNOWN BLOCKER — Copia/Documentación Workflows

**Status:** ❓ Modules exist; integration unclear

**Description:** After firma, system must track:
- Copias (copies of documents issued)
- Documentación (required docs: poder, ID, etc.)

**Blocker for:** Demo Steps 9–10 (post-signature workflow)

**Action:**
1. Run demo test to identify first failure
2. Create Issues as needed
3. Spec-drive fixes per CONSTITUTION.md

---

## Recommended Implementation Sequence

### 🚀 PHASE 1 — High-Priority Blockers (Start Today)

**Goal:** Enable complete demo execution  
**Estimated Time:** 6–10 hours

#### Step 1: Apply #796 (Saldo Visibility) ← START HERE
- Spec already exists
- Biggest UX impact for demo
- Enables Step 2

```bash
# From repo root:
openspec apply pago-presupuesto-picker-saldo
# TDD: write failing test first
mvn test -pl backend-api -Dtest=PagoSaldoTest
# Implement fix
# Run full pipeline
bash scripts/run_pipeline.sh
# Commit & PR
```

#### Step 2: Apply #848 (Overpayment Validation)
- Spec already exists
- 1–2 hour fix
- Prevents demo accidents

#### Step 3: Demo Run #3
- Test with #796 + #848 fixes applied
- Identify next blocker (likely Testimonio or Copia)

---

### 🎯 PHASE 2 — Discovered Blockers (After Demo Run)

**Goal:** Fix blockers identified by demo  
**Depends On:** Demo run output

#### If Testimonio Fails
```bash
# Create issue & spec
gh issue create --title "CU09: Testimonio post-firma linking" ...
openspec new change "testimonio-post-firma-integration"
# Follow CONSTITUTION.md TDD workflow
```

#### If Copia Fails
```bash
# Similar pattern
gh issue create ...
openspec new change "copia-documentacion-integration"
```

#### If Archive Fails
```bash
# #169 is already known
openspec new change "gestion-archive-deuda-check"  # spec from Issue #169
```

---

### ✨ PHASE 3 — Demo Validation (Final)

**Goal:** Run complete demo with all fixes  
**Expected Time:** ~15 minutes for full 2-case run

```bash
# One final run to validate
npx playwright test 02-demo-two-full-cases.spec.ts

# Verify both cases in database
curl http://localhost:8080/api/v1/gestiones | jq '.[] | {numero, estado}'

# Screenshot of both cases in dashboard
# → Proof for presentation
```

---

## Files Tracking & Quick Reference

### Demo Script (Master Test File)
- **Path:** `frontend/tests/e2e/02-demo-two-full-cases.spec.ts`
- **Purpose:** Builds 2 complete cases end-to-end, reports blockers
- **Runs:** `npx playwright test 02-demo-two-full-cases.spec.ts`
- **Last Updated:** 2026-09-01 08:35 (gestión number input fix)

### OpenSpec Specs (Already Exist)
- **#796 Saldo:** `openspec/changes/pago-presupuesto-picker-saldo/`
  - `proposal.md`, `spec.md`, `design.md`, `tasks.md`, `traceability.md` ✅
- **#848 Validation:** `openspec/changes/pago-limite-saldo-pendiente/`
  - Full spec ready ✅

### Issues Requiring New Specs
- **#169 Archive Deuda:** Issue exists, spec needed (Gate 1)
- **Testimonio:** Depends on demo output

### Test Files
- `frontend/tests/e2e/TS-0012-documentacion-testimonio-workflow.spec.ts` (partial)
- `frontend/tests/e2e/TS-0014-pagos-workflow.spec.ts` (needs update for #796)
- `frontend/tests/e2e/TS-0011-gestiones-crud-workflow.spec.ts` (needs archive test)

---

## Quick Command Reference

```bash
# Run demo test
npx playwright test 02-demo-two-full-cases.spec.ts

# View test report
open frontend/playwright-report/index.html

# Check backend logs
bash scripts/logs.sh backend

# Verify database
psql -h localhost -U notaire -d notaire -c "SELECT * FROM gestiones;"

# Run full validation pipeline
bash scripts/run_pipeline.sh

# Check coverage (after TDD)
mvn jacoco:report -pl backend-api
open backend-api/target/site/jacoco/index.html
```

---

## Timeline Estimate

| Phase | Tasks | Effort | Timeline |
|-------|-------|--------|----------|
| **1** | Fix #796, #848 apply | 4–6h | 2026-09-01 14:00–20:00 |
| **2** | Demo run #3, identify blockers | 0.5h | 2026-09-01 20:00 |
| **3** | Spec-drive discovered blockers | 6–10h | 2026-09-02 |
| **4** | Final demo validation | 0.5h | 2026-09-02 |
| **Total** | | **11–17h** | **2 days** |

---

## Sign-Off

**Prepared by:** Claude Code (AI Agent Development Workflow)  
**For:** Demo E2E validation & full CONSTITUTION.md implementation  
**Status:** Ready for Phase 1 execution  
**Next Action:** Apply #796 spec → TDD → Implement
