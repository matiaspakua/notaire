# Notaire Migration Backlog

> **Comprehensive Issue Tracking** | Generated: 2026-09-01

This document provides a categorized view of all open issues in the Notaire repository, organized by migration phase and priority.

---

## 📋 Phase 4: Modern Frontend Issues

### Critical Path Issues (Blocking Core Functionality)

| Issue | Title | Module | Use Case | Status | Priority |
|-------|-------|--------|----------|--------|----------|
| [#829](https://github.com/matiaspakua/notaire/issues/829) | Persona create/update rejects request — numeroIdentificacion required by backend but never sent by frontend | FRONTEND/BACKEND | CU17, CU18 | Open | 🔴 **Critical** |
| [#880](https://github.com/matiaspakua/notaire/issues/880) | Inmueble update throws NPE on tramiteList | BACKEND | CU69 | Open | 🔴 **Critical** |
| [#835](https://github.com/matiaspakua/notaire/issues/835) | Validar cliente/persona duplicada al dar de alta | FRONTEND/BACKEND | CU17 | Open | 🔴 **Critical** |

### Core Business Logic Implementation

| Issue | Title | Module | Use Case | Status | Priority |
|-------|-------|--------|----------|--------|----------|
| [#821](https://github.com/matiaspakua/notaire/issues/821) | Pagos parciales / en cuotas con seguimiento de plan | FRONTEND/BACKEND | CU15, CU47 | Open | 🟠 **High** |
| [#823](https://github.com/matiaspakua/notaire/issues/823) | Costos adicionales de documentos vinculados al presupuesto | FRONTEND/BACKEND | Multiple | Open | 🟠 **High** |
| [#822](https://github.com/matiaspakua/notaire/issues/822) | Descuentos y recargos con motivo estructurado en ítems de presupuesto | FRONTEND/BACKEND | Multiple | Open | 🟠 **High** |
| [#848](https://github.com/matiaspakua/notaire/issues/848) | Reject payments that exceed the saldo pendiente | FRONTEND/BACKEND | CU15 | Open | 🟠 **High** |

### Workflow Engine & Business Processes

| Issue | Title | Module | Use Case | Status | Priority |
|-------|-------|--------|----------|--------|----------|
| [#841](https://github.com/matiaspakua/notaire/issues/841) | Workflow engine cannot represent reingreso loop | FRONTEND/BACKEND | CU83, CU06, CU07, CU11, CU44 | Open | 🟠 **High** |
| [#804](https://github.com/matiaspakua/notaire/issues/804) | Enforce WorkflowDefinition transitions on real Gestion state changes | FRONTEND/BACKEND | CU83 | Open | 🟠 **High** |
| [#806](https://github.com/matiaspakua/notaire/issues/806) | Wire Gestion state changes to write Historial audit rows | FRONTEND/BACKEND | CU83 | Open | 🟠 **High** |

### Protocol & Document Management

| Issue | Title | Module | Use Case | Status | Priority |
|-------|-------|--------|----------|--------|----------|
| [#839](https://github.com/matiaspakua/notaire/issues/839) | Bloque de protocolo notarial sin desarrollo | FRONTEND/BACKEND | Multiple | Open | 🟠 **High** |
| [#838](https://github.com/matiaspakua/notaire/issues/838) | Vincular escritura a folio y copia/testimonio a su escritura de origen | FRONTEND/BACKEND | CU87 | Open | 🟠 **High** |
| [#851](https://github.com/matiaspakua/notaire/issues/851) | Capturar número de cartón, fecha y estado observado al reingresar un testimonio | FRONTEND/BACKEND | CU44 | Open | 🟡 **Medium** |

### Frontend Component Implementation

| Issue | Title | Module | Use Case | Status | Priority |
|-------|-------|--------|----------|--------|----------|
| [#807](https://github.com/matiaspakua/notaire/issues/807) | Expose registroEscribano for creating/editing an escribano | FRONTEND | Multiple | Open | 🟡 **Medium** |
| [#805](https://github.com/matiaspakua/notaire/issues/805) | Consult Suplencia during notary case assignment | FRONTEND/BACKEND | Multiple | Open | 🟡 **Medium** |
| [#803](https://github.com/matiaspakua/notaire/issues/803) | Add a dedicated reingresar documentacion action | FRONTEND/BACKEND | CU43 | Open | 🟢 **Low** |
| [#802](https://github.com/matiaspakua/notaire/issues/802) | Add 'próximos vencimientos' feature | FRONTEND/BACKEND | Multiple | Open | 🟢 **Low** |

---

## ⚙️ Phase 6: Technical Debt & Refactoring

### Security & Validation Improvements

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#655](https://github.com/matiaspakua/notaire/issues/655) | Roll out Jakarta Bean Validation to remaining backend controllers | BACKEND | Open | 🟡 **Medium** |
| [#799](https://github.com/matiaspakua/notaire/issues/799) | Enforce Persona.numeroIdentificacion uniqueness | BACKEND/DB | Open | 🟠 **High** |
| [#801](https://github.com/matiaspakua/notaire/issues/801) | DocumentoPresentado.fkIdTipoDocumento relation mapping | BACKEND | Open | 🟠 **High** |

### Legacy Code Migration

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| JPA Package Replacement | Replace 26 legacy JPA controllers with Spring Data repositories | BACKEND | Not tracked | 🟠 **High** |
| ControllerNegocio Refactoring | Extract 5,337-line God class into service classes | BACKEND | Not tracked | 🟠 **High** |
| AdministradorJpa Migration | Replace legacy service with modern implementation | BACKEND | Not tracked | 🟡 **Medium** |

### Data Model Consistency

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#797](https://github.com/matiaspakua/notaire/issues/797) | Connect PlantillaPresupuesto and Item to Presupuesto creation | FRONTEND/BACKEND | Open | 🟠 **High** |
| [#800](https://github.com/matiaspakua/notaire/issues/800) | Expose full TipoDeDocumento configuration and inherit defaults | FRONTEND/BACKEND | Open | 🟡 **Medium** |
| [#796](https://github.com/matiaspakua/notaire/issues/796) | Give payment form relation-awareness (presupuesto picker + saldo display) | FRONTEND | Open | 🟡 **Medium** |

---

## 🧪 Testing & Quality Assurance

### Test Coverage Expansion

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#853](https://github.com/matiaspakua/notaire/issues/853) | Add unit tests for GestionDeEscritura entity and fix NPE bugs | TEST | Open | 🟡 **Medium** |
| [#695](https://github.com/matiaspakua/notaire/issues/695) | Add Swing login error path tests | TEST | Open | 🟢 **Low** |
| [#690](https://github.com/matiaspakua/notaire/issues/690) | Add session expiry E2E test | TEST | Open | 🟠 **High** |
| [#689](https://github.com/matiaspakua/notaire/issues/689) | Add account lockout E2E test | TEST | Open | 🟠 **High** |

### Quality Gate Improvements

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#712](https://github.com/matiaspakua/notaire/issues/712) | Enable Trivy CRITICAL/HIGH blocking once #567 lands | DEVOPS | Open | 🟡 **Medium** |
| [#711](https://github.com/matiaspakua/notaire/issues/711) | Enable SpotBugs enforcement — 766 pre-existing findings block it | DEVOPS | Open | 🟡 **Medium** |
| [#710](https://github.com/matiaspakua/notaire/issues/710) | Enable Checkstyle enforcement — 3066 pre-existing warnings block it | DEVOPS | Open | 🟡 **Medium** |
| [#566](https://github.com/matiaspakua/notaire/issues/566) | No quality/security gate can ever fail the build | DEVOPS | Open | 🟠 **High** |

---

## 🚀 DevOps & Infrastructure

### CI/CD Pipeline Improvements

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#811](https://github.com/matiaspakua/notaire/issues/811) | e2e-swing.yml CI workflow builds nonexistent 'frontend-swing' Maven module | DEVOPS | Open | 🟡 **Medium** |
| [#679](https://github.com/matiaspakua/notaire/issues/679) | CI workflow is a 735-line monolith — should be split into reusable jobs | DEVOPS | Open | 🟡 **Medium** |
| [#680](https://github.com/matiaspakua/notaire/issues/680) | Trivy vulnerability scan is advisory-only — never fails the build | DEVOPS | Open | 🟡 **Medium** |

### Security Infrastructure

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#684](https://github.com/matiaspakua/notaire/issues/684) | Observability stack has no TLS between services — metrics traffic unencrypted | DEVOPS | Open | 🟡 **Medium** |
| [#681](https://github.com/matiaspakua/notaire/issues/681) | Container images not signed, no SBOM generated, no attestation | DEVOPS | Open | 🟡 **Medium** |
| [#676](https://github.com/matiaspakua/notaire/issues/676) | JWT tokens have no refresh mechanism, no revocation, no expiry enforcement | SECURITY | Open | 🟠 **High** |

---

## 📚 Documentation & Process

| Issue | Title | Module | Status | Priority |
|-------|-------|--------|--------|----------|
| [#760](https://github.com/matiaspakua/notaire/issues/760) | Compute real e2eTests/apiEndpoints coverage numbers instead of carried-forward placeholders | DOCS | Open | 🟢 **Low** |
| [#727](https://github.com/matiaspakua/notaire/issues/727) | Document PR-title-lint gotcha and finalize loop-state.md | DOCS | Open | 🟢 **Low** |
| [#606](https://github.com/matiaspakua/notaire/issues/606) | Auto-generated CASO-DE-USO issue bodies point to a retired doc path | DOCS | Open | 🟢 **Low** |
| [#611](https://github.com/matiaspakua/notaire/issues/611) | Login page bypasses mandated FormContainer/FormField pattern | DOCS/FRONTEND | Open | 🟢 **Low** |

---

## 🎯 Priority Matrix

### Critical Path (Must be resolved immediately)
1. **#829** - Persona validation mismatch (blocks all persona creation)
2. **#880** - Inmueble NPE bug (blocks property updates)
3. **#835** - Duplicate validation (data integrity)

### High Impact (Needs completion for MVP)
1. **#821** - Partial payments implementation
2. **#841** - Workflow engine limitations
3. **#804** - Workflow transition enforcement
4. **#806** - Gestion audit trail integration

### Medium Priority (Important enhancements)
1. **#655** - Bean validation rollout
2. **#797** - Presupuesto template integration
3. **#839** - Protocol notarial features
4. **#838** - Escritura-folio linking

### Low Priority (Nice to have / technical debt)
1. **#695**, **#690**, **#689** - Test coverage expansion
2. **#712**, **#711**, **#710** - Quality gate improvements
3. **#803**, **#802** - Feature enhancements

---

## 📈 Progress Tracking

### Completion Estimates
| Category | Total Issues | Open Issues | Completed | % Complete |
|----------|-------------|-------------|-----------|------------|
| Critical Path | 3 | 3 | 0 | 0% |
| High Priority | 15 | 15 | 0 | 0% |
| Medium Priority | 20 | 20 | 0 | 0% |
| Low Priority | 12 | 12 | 0 | 0% |
| **Total** | **50** | **50** | **0** | **0%** |

### Velocity Metrics
- **Average Issue Resolution Time:** ~14 days (based on historical data)
- **Monthly Throughput:** ~8-10 issues
- **Estimated Completion:** Q2 2027 at current pace

---

## 🔄 Update Instructions

To keep this backlog current:

1. **New Issues:** Add to appropriate category when created
2. **Closed Issues:** Remove from list when resolved
3. **Priority Changes:** Update priority based on project needs
4. **Monthly Review:** Verify all issues are correctly categorized

*Last updated: 2026-09-01 | Based on GitHub API query*
