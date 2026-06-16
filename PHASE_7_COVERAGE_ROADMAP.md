# Phase 7: Coverage Gap Analysis & Targeted Testing Roadmap

**Status**: IN PROGRESS  
**Date Started**: 2026-06-16  
**Overall Objective**: Achieve 90%+ coverage for core API layer by systematic testing of low-coverage controllers

---

## Executive Summary

Phase 6 improved PagoController coverage from 27% → 95% with 24 targeted tests. Phase 7 continues this systematic approach to eliminate remaining coverage gaps across the API layer.

**Current State**:
- Total Tests: 847+ (unit + integration)
- Pass Rate: 100%
- Test Infrastructure: Fully established
- Coverage Approach: Targeted, priority-based

---

## Controller Coverage Status

### CRITICAL (0-30% coverage)

| Controller | Current | Target | Priority | Est. Tests | Strategy |
|-----------|---------|--------|----------|-----------|----------|
| **PlantillaPresupuestoController** | 27% | 95% | P0 | 20 | Repository done (11). Need: endpoint tests via H2 integration |
| **InmuebleController** | 20% | 90% | P0 | 18 | CRUD + search endpoints |
| **SuplenciaController** | 21% | 90% | P0 | 17 | CRUD + status transitions |

### HIGH (30-60% coverage)

| Controller | Current | Target | Priority | Est. Tests | Strategy |
|-----------|---------|--------|----------|-----------|----------|
| **WorkflowTransitionController** | 57% | 85% | P1 | 12 | State transitions + validation |
| **RolController** | 69% | 90% | P1 | 8 | Permission checks + CRUD |
| **UsuarioController** | 45% | 85% | P1 | 15 | Auth + role assignment |

### MEDIUM (60-80% coverage)

| Controller | Current | Target | Priority | Est. Tests | Strategy |
|-----------|---------|--------|----------|-----------|----------|
| **GestionController** | 72% | 90% | P2 | 6 | Edge cases + status flows |
| **DocumentoController** | 68% | 85% | P2 | 8 | File ops + search |

### GOOD (80%+)

| Controller | Current | Target | Priority | Status |
|-----------|---------|--------|----------|--------|
| **PagoController** | 95% | 95% | Done | ✅ COMPLETE |
| **PersonaController** | 91% | 95% | P3 | 2-3 tests needed |
| **PresupuestoController** | 88% | 90% | P3 | 1-2 tests needed |

---

## Phase 7 Execution Plan

### Iteration 1: PlantillaPresupuestoController (WEEK 1)
**Goal**: 27% → 95%  
**Approach**: 
1. ✅ Repository integration tests (11 tests) - COMPLETE
2. 🔄 Service layer tests (8 tests) - using JPA controller
3. 🔄 Controller endpoint tests (12 tests) - via H2 integration  
**Estimated Impact**: +68% coverage  
**Branch**: `phase-7/plantilla-presupuesto-coverage`

### Iteration 2: InmuebleController (WEEK 1-2)
**Goal**: 20% → 90%  
**Approach**:
1. Create InmuebleRepositoryIntegrationTest (8 tests)
2. Create InmuebleServiceIntegrationTest (6 tests)
3. Create InmuebleControllerIntegrationTest (4 tests)
**Estimated Impact**: +70% coverage  
**Branch**: `phase-7/inmueble-coverage`

### Iteration 3: SuplenciaController (WEEK 2)
**Goal**: 21% → 90%  
**Approach**:
1. Create SuplenciaRepositoryIntegrationTest (7 tests)
2. Create SuplenciaServiceIntegrationTest (5 tests)
3. Create SuplenciaControllerIntegrationTest (5 tests)
**Estimated Impact**: +69% coverage  
**Branch**: `phase-7/suplencia-coverage`

### Iteration 4: WorkflowTransitionController (WEEK 2-3)
**Goal**: 57% → 85%  
**Approach**:
1. Enhance existing transition tests (12 tests)
2. Focus on validation and error cases
**Estimated Impact**: +28% coverage  
**Branch**: `phase-7/workflow-transition-coverage`

### Iteration 5: Top Up Remaining (WEEK 3)
**Goal**: Final push to 90%+  
**Approach**:
1. UsuarioController: 3-4 targeted tests
2. RolController: 2-3 targeted tests
3. PersonaController: 2 additional tests
4. PresupuestoController: 1 additional test

---

## Testing Methodology

### Repository Integration Tests
```java
@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
class XxxRepositoryIntegrationTest {
    // CRUD + custom query methods
    // 7-10 tests per repository
}
```

### Service Integration Tests
```java
class XxxServiceIntegrationTest extends ServiceIntegrationTest {
    // Business logic + error handling
    // 5-8 tests per service
}
```

### Controller Integration Tests
```java
@SpringBootTest
@ActiveProfiles("test-h2")
class XxxControllerIntegrationTest {
    // All HTTP endpoints
    // Error codes (200, 201, 400, 404, 409, 500)
    // Complete workflows (CRUD)
    // 8-12 tests per controller
}
```

---

## Success Criteria

- ✅ All new tests pass (100% pass rate)
- ✅ Coverage for each target controller improves to 90%+
- ✅ No regression in existing tests
- ✅ Code quality checks pass (Checkstyle, SpotBugs)
- ✅ All PRs merged without conflicts
- ✅ Documentation updated

---

## Test Infrastructure Checklist

- ✅ H2 in-memory database configured
- ✅ MockMvc patterns established
- ✅ ServiceIntegrationTest base class
- ✅ Test data builders and helpers
- ✅ Transactional test isolation
- ✅ DisplayName annotations
- ✅ AAA pattern (Arrange-Act-Assert)
- ✅ AssertJ fluent assertions

---

## Known Issues & Workarounds

### Issue: PlantillaPresupuestoRepository ID Mismatch
- **Problem**: Repository expects `Integer` ID but entity uses `PlantillaPresupuestoPK`
- **Workaround**: Use named query methods (`findByTipoDeTramiteIdTipoTramite`) instead of `findById`
- **Status**: Document for future refactoring

### Issue: JpaController Tight Coupling
- **Problem**: Controllers directly instantiate JpaController via static provider
- **Workaround**: Use integration tests with H2 instead of unit tests with mocks
- **Status**: Acceptable for legacy code; note for future refactoring

---

## Metrics & Tracking

### Weekly Progress
- **Week 1**: PlantillaPresupuesto + Inmueble = 38 tests
- **Week 2**: Suplencia + Workflow = 30 tests  
- **Week 3**: Top-up + docs = 12 tests
- **Total Phase 7**: ~80 tests

### Coverage Target
| Metric | Before Phase 7 | After Phase 7 | Target |
|--------|---|---|---|
| Overall | 31% | 40%+ | 50% |
| API Layer | 85% | 91%+ | 95%+ |
| Service Layer | 95% | 95%+ | 95%+ |
| Critical Controllers | 27% avg | 90% avg | 90%+ |

---

## Resource Allocation

- **Time**: 3-4 weeks
- **Effort**: ~100-120 hours
- **Team**: 1-2 developers
- **CI/CD**: GitHub Actions (auto-run on push)
- **Tools**: Maven, JUnit 5, AssertJ, H2

---

## Dependencies & Blockers

- None identified
- All infrastructure in place
- Test database (H2) available
- CI/CD pipeline functional

---

## Next Steps (After Phase 7)

1. **Phase 8: Performance Testing**
   - k6 load testing
   - API response time analysis
   - Database query optimization

2. **Phase 9: Security Testing**
   - OWASP Top 10 checks
   - Authentication/Authorization
   - Input validation

3. **Phase 10: Contract Testing**
   - API contract validation
   - Schema conformance
   - Backwards compatibility

---

## References

- `/COMPREHENSIVE_TEST_COMPLETION_REPORT.md` - Phase 1-6 summary
- `/TESTING_ROADMAP.md` - Overall testing strategy
- `/.claude/rules/` - Development guidelines
- `/backend-api/src/test/java/com/licensis/notaire/integration/` - Test examples

---

**Status**: Ready to execute Phase 7 iterations  
**Owner**: Notaire Development Team  
**Last Updated**: 2026-06-16
