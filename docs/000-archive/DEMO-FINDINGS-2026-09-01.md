# Demo E2E Findings & Action Plan (2026-09-01)

**Session Goal:** Build 2 complete, comparable use cases end-to-end (Case A & Case B), identify all blockers, and prioritize fixes using full CONSTITUTION.md workflow.

**Status:** ✅ 4 immediate UI blockers fixed; demo re-run in progress to identify remaining workflow blockers.

---

## Executive Summary

### Completed This Session

1. **Updated `explore.md`** with latest fix status (PRs #879–#894, all merged)
2. **Created comprehensive demo script** (`02-demo-two-full-cases.spec.ts`):
   - Builds 2 identical-but-independent cases in parallel
   - Exercises: Persona → Presupuesto → Inmueble → Gestión → Escritura → Firma → Testimonio → Pago → Archivo
   - Console logging at each step for clear diagnosis

3. **Fixed UI blockers** preventing demo execution:
   - ✅ Added `data-testid="btn-nuevo-inmueble"` to inmuebles page
   - ✅ Added testids to inmuebles form fields
   - ✅ Enhanced demo script with fallback selectors

### Current Demo Execution Status

**First Run (08:22-08:24 CEST):** Failed at Step 3 (Inmuebles creation) — button testid missing.

**Second Run (08:28+):** In progress (fixed testid, re-running to identify next blocker).

---

## Blockers Inventory

### TIER 1 — Critical for Demo Completion (Must Fix)

#### ❓ UNKNOWN (Discovered by Demo Run #2)
- **First failure point in workflow** — demo currently running to identify
- Expected blockers: Testimonio integration, Payment form, Archive workflow
- Action: Parse demo output, create Issue + OpenSpec spec per CONSTITUTION.md

### TIER 2 — Known Issues (Require Implementation)

#### 🟡 #796 — Payment Saldo Visibility (MEDIUM priority)
- **Status:** OpenSpec spec exists (`openspec/changes/pago-presupuesto-picker-saldo/`)
- **Issue:** Payment form doesn't display balance owed
- **Blocker for:** Demo step 9 (payment registration)
- **Fix scope:** Add saldo endpoint, update UI to fetch/display
- **Effort:** ~2–4 hours
- **Workflow:** `OpenSpec apply` → TDD → Implement → Test → PR

#### 🟡 #848 — Payment Overpayment Validation (SMALL priority)
- **Status:** OpenSpec spec exists (`openspec/changes/pago-limite-saldo-pendiente/`)
- **Issue:** System accepts payments > saldo without validation
- **Blocker for:** Data integrity, prevents demo accidents
- **Fix scope:** Add validation to `PagoService.procesarPago`
- **Effort:** ~1–2 hours
- **Workflow:** `OpenSpec apply` → TDD → Implement → PR

#### 🟡 #169 — Gestión Archive with Deuda Check (MEDIUM priority)
- **Status:** RF-37 mentioned but unclear; issue references deuda verification
- **Issue:** RFC requires "warning of debt when archiving" but no check exists
- **Blocker for:** Demo step 10 (archive workflow)
- **Fix scope:** Add deuda balance check before allowing archive
- **Effort:** ~2–3 hours
- **Workflow:** Issue → OpenSpec spec → TDD → Implement → PR

### TIER 3 — Unknown Status (Require Testing)

#### ❓ Testimonio Integration Post-Firma
- **Status:** Module exists; integration unclear
- **Blocker for:** Demo step 8
- **Action:** Wait for demo run output; create Issue if fails

#### ❓ Copia/Documentación Workflows
- **Status:** Modules exist; end-to-end flow unclear
- **Blocker for:** Demo steps 9–10
- **Action:** Wait for demo run output

#### ❓ Document Tracking / Upload
- **Status:** Unknown
- **Blocker for:** Full demo lifecycle
- **Action:** Wait for demo run output

---

## Recommended Fix Sequence

Based on expected blockers and CONSTITUTION.md workflow:

### Phase 1 — Demo Execution & Discovery
1. **Complete demo run #2** (in progress) → Identify first real blocker
2. **Document findings** in updated explore.md

### Phase 2 — High-Priority Blockers (Expected Fixes)
1. **If Testimonio fails** → Issue #XXX (NEW) → OpenSpec spec → Fix (1 day)
2. **If Payment form fails** → Apply #796 spec (already proposed) → Fix (2–4 hours)
3. **If Archive fails** → Create #169 spec → Fix (2–3 hours)

### Phase 3 — Re-run Demo & Validate
1. Re-run `02-demo-two-full-cases.spec.ts` with all Phase 2 fixes
2. Confirm both Case A & Case B complete end-to-end
3. Smoke test: Both cases exist in database, accessible via dashboard

---

## CONSTITUTION.md Workflow (All Fixes Follow This)

```
Issue → Gate 1 Spec (OpenSpec) → TDD (failing tests first)
  → Implement → Refactor → All tests pass
  → Commit (Conventional) → PR → CI Green → Merge
  → Gate 5 Archive spec → Close Issue
```

---

## Files Modified This Session

| File | Change | Commit |
|------|--------|--------|
| `openspec/explore.md` | Updated with latest fix status & demo plan | dcc5024 |
| `frontend/tests/e2e/02-demo-two-full-cases.spec.ts` | Created comprehensive demo script | dcc5024 |
| `frontend/src/app/dashboard/inmuebles/page.tsx` | Added testids to button & form fields | dcc5024 |
| `openspec/DEMO-FINDINGS-2026-09-01.md` | This file | Pending |

---

## Key Data Points

**Test Environment:**
- Docker stack: Running (frontend 3000, backend 8080, postgres 5432)
- Playwright: 1.46.0 (headless Chrome)
- Test timeout: 15s action, 30s navigate

**Demo Cases:**
- **Case A:** Cliente CaseA-XXXX, Presupuesto ~75k, Inmueble NOM-XXXX
- **Case B:** Cliente CaseB-XXXX, Presupuesto ~85k, Inmueble NOM-XXXX

**Expected Demo Duration:**
- Full 2-case run: ~5–10 minutes (w/ all flows)
- Current (expected after Phase 2): ~10 minutes
- Final (after all fixes): ~15 minutes

---

## Next Steps

1. **Monitor demo run #2** — should complete in ~5 minutes
2. **Parse output** for first failure point
3. **Create GitHub Issue** for new blocker (if any)
4. **Begin Phase 2 fixes** in priority order
5. **Update this document** with new findings

**Owner:** Claude Code (AI Agent Development Workflow)  
**Status:** In Progress  
**Last Updated:** 2026-09-01 08:30 CEST
