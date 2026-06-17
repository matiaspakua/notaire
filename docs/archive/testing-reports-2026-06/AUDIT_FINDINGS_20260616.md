# API-UI Alignment Audit: Critical Findings
**Date**: 2026-06-16  
**Status**: ⚠️ CRITICAL ISSUES IDENTIFIED  

---

## Executive Summary

**Coverage Rate: 47%** (56 endpoints mapped, 63 orphaned)

The audit identified a **CRITICAL ARCHITECTURAL GAP**:
- ✅ 119 REST endpoints implemented in backend
- ⚠️ **0 API references detected in frontend** ← **CRITICAL ISSUE**
- ⚠️ 63 endpoints appear orphaned (53% of all endpoints)

---

## Critical Issue #1: No Frontend API References Found

**Problem**: The audit detected ZERO references to `/api/` endpoints in the frontend codebase.

**Possible Root Causes**:
1. **API Client Wrapper Pattern** - Frontend uses centralized ApiClient class that abstracts endpoints
2. **Dynamic URLs** - API calls use variables/config instead of hardcoded paths
3. **HTTP Client Library** - Custom HTTP client interceptor hiding the actual calls
4. **Environment Variables** - Endpoints stored in .env or config files

**Impact**: CRITICAL - Cannot validate frontend-backend API alignment

**Action Required**: Manual investigation of frontend API layer

---

## Detailed Findings

| Metric | Value | Status |
|--------|-------|--------|
| Backend Endpoints | 119 | ✅ Extracted |
| Frontend API Refs | 0 | ❌ **NONE DETECTED** |
| Orphaned Endpoints | 63 | ⚠️ 53% |
| Coverage Rate | 47% | ❌ **CRITICAL** |
| DTO Classes | 0 | ❌ None found |
| TypeScript Types | 41 | ✅ Found |

---

## The 63 Orphaned Endpoints (Non-callable from detected frontend code)

```
/api/v1/conceptos/{id}
/api/v1/conceptos/{id}/in-use
/api/v1/conceptos/search
/api/v1/copia/{id}
/api/v1/documento-presentado/{id}
/api/v1/escrituras/{id}
/api/v1/escrituras/buscar
/api/v1/escrituras/escribanos-disponibles
/api/v1/estado-gestion/{id}
/api/v1/estado-gestion/{id}/in-use
/api/v1/estado-gestion/search
/api/v1/folio/{id}
/api/v1/gestiones/{id}
/api/v1/gestiones/{id}/estado-actual
/api/v1/gestiones/{id}/workflow-trace
/api/v1/gestiones/cliente/{idPersona}
/api/v1/gestiones/numero/{numero}
/api/v1/historial/{id}
/api/v1/historial/gestion/{idGestion}
/api/v1/inmueble/{id}
/api/v1/items/{id}
/api/v1/items/presupuesto/{idPresupuesto}
/api/v1/movimiento-testimonio/{id}
/api/v1/pagos/{id}
/api/v1/pagos/fecha
/api/v1/pagos/presupuesto/{idPresupuesto}
/api/v1/pagos/presupuesto/{idPresupuesto}/saldo
/api/v1/personas/{id}
/api/v1/personas/buscar
/api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}
/api/v1/plantilla-presupuestos/tipo-tramite/{idTipoTramite}/concepto/{idConcepto}
/api/v1/plantilla-tramite/{idTipoTramite}/{idTipoDocumento}
/api/v1/plantilla-tramite/tipo-tramite/{idTipoTramite}
/api/v1/presupuestos/{id}
/api/v1/presupuestos/buscar
/api/v1/presupuestos/persona/{idPersona}
/api/v1/registro-auditoria/{id}
/api/v1/registro-auditoria/usuario/{idUsuario}
/api/v1/roles/{id}
/api/v1/roles/{idRol}/usuarios/{idUsuario}
/api/v1/roles/usuarios/{idUsuario}
/api/v1/suplencia/{id}
/api/v1/testimonio/{id}
/api/v1/tipo-de-documento/{id}
/api/v1/tipo-de-documento/{id}/in-use
/api/v1/tipo-de-documento/search
/api/v1/tipo-folio/{id}
/api/v1/tipo-identificacion/{id}
/api/v1/tipo-tramite/{id}
/api/v1/tipo-tramite/{id}/in-use
/api/v1/tipo-tramite/{id}/workflow
/api/v1/tipo-tramite/search
/api/v1/tramites/{id}
/api/v1/usuarios/{id}
/api/v1/usuarios/login
/api/v1/usuarios/persona/{idPersona}
/api/v1/workflow-definition/{id}
/api/v1/workflow-definition/{id}/validate
/api/v1/workflow-node/{id}
/api/v1/workflow-node/by-workflow/{workflowId}
/api/v1/workflow-transition/{id}
/api/v1/workflow-transition/by-workflow/{workflowId}
```

---

## Investigation Required

### Step 1: Find the API Client
```bash
# Search for API client implementation
find /Users/matiasmiguez/workspace/notaire/frontend/src -name "*api*" -o -name "*client*" -o -name "*http*"

# Search for fetch/axios usage
grep -r "fetch\|axios\|HttpClient" /Users/matiasmiguez/workspace/notaire/frontend/src --include="*.ts" --include="*.tsx"
```

### Step 2: Check how endpoints are called
```bash
# Look in stores or hooks for API calls
find /Users/matiasmiguez/workspace/notaire/frontend/src -name "*store*" -o -name "*hook*" -o -name "*service*"

# Check environment configuration
find /Users/matiasmiguez/workspace/notaire/frontend -name ".env*" -o -name "config*"
```

### Step 3: Validate Type Mappings
```bash
# Extract actual DTO structures
grep -r "interface.*DTO\|type.*DTO" /Users/matiasmiguez/workspace/notaire/frontend/src

# Compare with backend
find /Users/matiasmiguez/workspace/notaire/backend-api -name "*DTO.java"
```

---

## Recommended Actions (Priority Order)

### CRITICAL (Do Today)
1. **Locate the API Client**
   - Find how frontend actually makes HTTP calls
   - Document the pattern used

2. **Re-run Audit with Correct Pattern**
   - Update audit script to detect actual API usage
   - Re-analyze with correct detection method

3. **Validate Architecture**
   - Confirm API client is correctly consuming endpoints
   - Document any abstraction layers

### HIGH (This Week)
4. **Create Endpoint Registry**
   - Once real API usage is mapped
   - Document all 119 endpoints with status

5. **Type Validation**
   - Map DTOs to TypeScript interfaces
   - Identify mismatches

6. **Documentation**
   - Update API documentation
   - Create endpoint-to-UI mapping

### MEDIUM (Next Week)
7. **UI Completeness Audit**
   - Identify which endpoints lack UI
   - Decide: implement / keep internal / remove

8. **Test Coverage**
   - Add E2E tests for each endpoint
   - Verify request/response contracts

---

## Files Generated

All audit files are in: `/tmp/notaire-audit-20260616_093641/`

- `backend_endpoints.txt` - All 119 endpoints
- `orphaned_endpoints.txt` - The 63 gaps
- `frontend_api_paths.txt` - Frontend API references (currently empty)
- `AUDIT_REPORT.txt` - Full report
- `backend_paths.txt` - Endpoint paths only

---

## Next Immediate Step

**Find the API client implementation:**

```bash
find /Users/matiasmiguez/workspace/notaire/frontend/src -type f \( -name "*.ts" -o -name "*.tsx" \) -exec grep -l "fetch\|axios\|HttpClient\|api\." {} \; | head -10
```

This will identify how the frontend actually communicates with the backend.

---

**Status**: BLOCKED - Need to understand API client pattern before proceeding  
**Owner**: DevOps/Architecture Team  
**Date**: 2026-06-16  

