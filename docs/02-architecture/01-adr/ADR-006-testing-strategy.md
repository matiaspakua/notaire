# ADR-006: Testing Strategy

**Status:** Accepted
**Date:** 2026-04-13
**Deciders:** Matías Miguez
**Related:** ADR-001, ADR-005

## Context

El proyecto necesita una estrategia de testing clara que cubra todos los componentes
del sistema: backend (Spring Boot), frontend (Next.js), y el stack completo desplegado
en Kubernetes.

## Decision

Adoptamos la **Testing Trophy** adaptada al stack actual:

```
        ┌──────────────────────────────┐
        │   E2E Tests (Playwright)     │  ← Flujos completos de negocio
        │   ~10-20 tests críticos      │
        └──────────────────────────────┘
      ┌────────────────────────────────────┐
      │   Integration Tests                │  ← API tests, DB tests
      │   (Spring Boot Test + H2/PG)       │
      │   ~50-100 tests                    │
      └────────────────────────────────────┘
    ┌────────────────────────────────────────────┐
    │   Unit Tests (JUnit 5 + Vitest)            │  ← Lógica de negocio
    │   Backend: servicios, validators           │
    │   Frontend: componentes, hooks, utils      │
    │   ~200+ tests                              │
    └────────────────────────────────────────────┘
```

### Backend Testing (Java)

| Tipo | Herramientas | Scope | Requisito |
|------|-------------|-------|-----------|
| Unit | JUnit 5 + Mockito + AssertJ | Services, validators | 80% coverage |
| Integration | @SpringBootTest + Testcontainers | Controllers, repositories | Todos los endpoints |
| API Contract | MockMvc + Spring Security Test | Auth, roles, status codes | Happy + sad paths |
| Performance | Gatling / k6 | API bajo carga | Baseline documented |
| Security | OWASP ZAP | Todos los endpoints | 0 CRITICAL/HIGH |

**Convenciones backend:**
- Métodos: `shouldXxxWhenYyy()` con `@DisplayName`
- Patrón: Arrange / Act / Assert
- Un assert por test cuando sea posible
- Tests en `src/test/java/.../unit/` e `integration/`
- Tagging con `@Tag("unit")` y `@Tag("integration")`

### Frontend Testing (TypeScript)

| Tipo | Herramientas | Scope |
|------|-------------|-------|
| Unit | Vitest + Testing Library | Components, hooks, utils |
| Visual | Storybook (opcional) | Component states |
| E2E | Playwright | User journeys completos |
| Accessibility | axe-core + Playwright | WCAG 2.1 compliance |

**Convenciones frontend:**
- Archivos de test: `*.test.ts` junto al código
- E2E en `tests/e2e/*.spec.ts`
- Page Object Model para E2E

### E2E Tests - Flujos Prioritarios

| Flujo | CUs | Prioridad |
|-------|-----|-----------|
| Login → Gestión completa → Pago | CU01, CU02, CU15, CU16 | Critical |
| Alta cliente → Búsqueda | CU17, CU18, CU19, CU61 | High |
| Presupuesto → Escritura → Testimonio | CU01, CU05, CU07 | High |
| Admin: alta usuario + suplencia | CU20, CU22 | Medium |
| Generar reporte mensual | CU24, CU25 | Medium |

### CI Pipeline

```yaml
# Por cada PR:
1. Backend: mvn test (unit + integration)
2. Backend: JaCoCo coverage report (80% gate)
3. Backend: Checkstyle + SpotBugs
4. Backend: Trivy vulnerability scan
5. Frontend: vitest run
6. Frontend: playwright test (headless)
7. Build: Docker images
8. Security: OWASP ZAP (weekly en main)
```

## Consequences

- Tests etiquetados permiten ejecutar solo unit tests en feedback rápido
- Testcontainers elimina diferencias entre H2 y PostgreSQL en integration tests
- Playwright E2E en CI garantiza que los 68 CUs funcionan end-to-end
- Coverage gate del 80% mantiene calidad del backend

## References

- [Testing Trophy (Kent C. Dodds)](https://kentcdodds.com/blog/the-testing-trophy-and-testing-classifications)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/test-auto-configuration.html)
- [Testcontainers for Java](https://java.testcontainers.org/)
- [Playwright Documentation](https://playwright.dev/docs/intro)
