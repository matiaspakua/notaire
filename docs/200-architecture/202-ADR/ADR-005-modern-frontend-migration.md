# ADR-005: Migración de Frontend Java Swing a Next.js

**Status:** Accepted
**Date:** 2026-04-13
**Deciders:** Matías Miguez
**Supersedes:** N/A
**Related:** ADR-001 (Microservices Architecture), ADR-003 (REST API Versioning)

## Context

El módulo `frontend-swing` fue creado como paso intermedio en la migración del monolito:
fue refactorizado para usar el backend REST en lugar de acceso directo a la base de datos,
pero sigue siendo una aplicación Java Swing de escritorio.

> **Estado actual:** la migración a Next.js está implementada en el directorio `frontend/`
> (no `frontend-nextjs/` como se planteaba originalmente en este ADR). El módulo Swing fue
> renombrado a `deprecated-frontend-swing/` y excluido del reactor Maven raíz; ver
> `deprecated-frontend-swing/README.md`.

### Problemas actuales con Java Swing

- **Tecnología deprecated:** Java Swing no recibe mejoras activas desde Java 8. Oracle no lo desarrolla activamente para uso moderno.
- **Sin soporte web/móvil:** Aplicación de escritorio solamente, sin acceso desde navegador.
- **UX desactualizada:** Look and feel limitado, sin componentes modernos (tablas reactivas, gráficas, dashboards).
- **Testing difícil:** Swing requiere herramientas especializadas (AssertJ-Swing, UISpec4J) que son complejas y frágiles.
- **Distribución compleja:** Requiere JRE instalado en cada cliente, updates manuales.
- **Integración con herramientas modernas:** No se integra fácilmente con DevTools, CI/CD de frontend, ni ecosistema npm.

### Opciones Evaluadas

| Opción | Pros | Contras |
|--------|------|---------|
| Mantener Swing | Sin migración | Todos los problemas arriba |
| JavaFX | Mismo ecosistema Java | Igualmente deprecated, poca adopción |
| Electron | Familiar para Java devs | Bundle muy pesado, no web-native |
| Angular | Robusto, tipado | Más verboso, curva de aprendizaje |
| **Next.js (React)** | Ecosystem enorme, SSR, TypeScript, testing moderno | Requiere aprender JS/TS |
| Vue.js / Nuxt | Simple, progresivo | Menor ecosystem que React |

### Criterios de Decisión

1. Soporte TypeScript nativo (type safety comparable a Java)
2. Testing moderno (unit + E2E automatizado)
3. Rendimiento y SEO (server-side rendering)
4. Ecosistema de componentes UI maduros
5. Facilidad de integración con REST API
6. Deployable como contenedor

## Decision

**Migrar el frontend a Next.js 15 con TypeScript** como tecnología base, complementado por:

- **Tailwind CSS** para estilos utilitarios
- **shadcn/ui** para componentes accesibles y customizables
- **React Query (TanStack Query)** para manejo de estado servidor y cache de API
- **Zustand** para estado cliente (auth, UI state)
- **Vitest + Testing Library** para tests unitarios de componentes
- **Playwright** para tests E2E automatizados

### Arquitectura del Nuevo Frontend

> El árbol siguiente refleja el diseño original de este ADR. La estructura real
> implementada vive en `frontend/` (no `frontend-nextjs/`) bajo un directorio `src/`, con
> algunas carpetas renombradas — ver estructura actual más abajo.

```
frontend-nextjs/
├── app/                    # Next.js App Router
│   ├── (auth)/             # Páginas públicas (login)
│   ├── (dashboard)/        # Páginas protegidas
│   │   ├── gestiones/      # CU01-CU16 (core workflow)
│   │   ├── clientes/       # CU17-CU19, CU41, CU46
│   │   ├── escrituras/     # CU52, CU62
│   │   ├── presupuestos/   # CU01, CU39, CU45, CU49, CU55, CU60
│   │   ├── protocolos/     # CU28, CU33, CU36, CU40, CU63, CU68
│   │   ├── administracion/ # CU20-CU23, CU26-CU32, CU34-CU38
│   │   └── reportes/       # CU24, CU25, CU50
│   └── api/                # Route handlers (BFF pattern)
├── components/
│   ├── ui/                 # shadcn/ui components
│   ├── forms/              # Form components por dominio
│   ├── tables/             # Data tables con paginación
│   └── layout/             # Navigation, sidebar, header
├── lib/
│   ├── api-client.ts       # HTTP client (tipo-safe, basado en fetch)
│   ├── auth.ts             # JWT management
│   └── utils.ts            # Helpers
├── hooks/                  # Custom React hooks
├── stores/                 # Zustand stores
├── types/                  # TypeScript types (mirrors DTOs del backend)
├── tests/
│   ├── unit/               # Vitest tests
│   └── e2e/                # Playwright tests
└── public/                 # Static assets
```

**Estructura actual (`frontend/`):**

```
frontend/
├── src/
│   ├── app/                # Next.js App Router (dashboard/, login/, auditoria/)
│   ├── components/         # ui/, layout/, motion/, shared/
│   ├── hooks/               # Custom React hooks
│   ├── lib/                 # api-client, utils
│   ├── store/                # Zustand stores (auth-store, etc.)
│   ├── theme/                 # Design tokens + form patterns (tokens.ts, form-patterns.tsx)
│   ├── i18n/                  # Internationalization
│   ├── types/                 # TypeScript types (mirrors backend DTOs)
│   └── tests/                 # Vitest unit/component tests
├── tests/e2e/               # Playwright E2E tests (cuNN-*.spec.ts, per Caso de Uso)
└── public/                  # Static assets
```

### Estrategia de Migración (Backend For Frontend)

El backend REST existente (`/api/v1`) se expone directamente al nuevo frontend.
Para casos donde se necesite composición de múltiples APIs o transformaciones específicas,
se usarán Next.js Route Handlers como BFF (Backend For Frontend) liviano.

```
Browser → Next.js (SSR/CSR) → Spring Boot REST API → PostgreSQL
                    ↓
             Route Handlers (BFF)
             - Session management
             - Token refresh
             - Response aggregation
```

## Consequences

### Positivo

- **Testing completo:** Vitest (unit) + Playwright (E2E) cubren todos los 68 CUs
- **UX moderna:** Componentes accesibles, responsive, dark mode
- **Type safety:** TypeScript en frontend + Java en backend = sistema end-to-end tipado
- **Deploy:** Docker container deployable en Kubernetes igual que el backend
- **Developer experience:** Hot reload, DevTools, ecosistema npm
- **CI/CD:** Integración nativa con GitHub Actions para lint, tests, build

### Negativo

- **Curva de aprendizaje:** Equipo Java necesita aprender React/TypeScript
- **Dos stacks:** Java (backend) + TypeScript (frontend) en el mismo repo
- **Migración costosa:** Los 68 CUs necesitan reimplementarse
- **Duplicación de tipos:** DTOs en Java y types en TypeScript (mitigado con OpenAPI codegen)

### Mitigaciones

- Usar `openapi-typescript` para generar types automáticamente desde la spec de Swagger
- Migración incremental: un módulo de CUs por sprint
- El módulo `frontend-swing` (hoy `deprecated-frontend-swing`) se mantiene durante la
  transición hasta que cada CU sea validado

## Implementation Plan

> **Nota de estado:** este plan de sprints refleja la intención original del ADR. La
> migración está sustancialmente implementada en `frontend/` (Next.js 16, no un módulo
> Maven — ver [Solution Strategy](../201-SAD/sad.md)). Para el estado real de cobertura
> por Caso de Uso, ver la matriz de trazabilidad
> [`CU-API-MATRIX.csv`](../../300-development/303-testing/CU-API-MATRIX.csv) en vez de
> los checkboxes por sprint a continuación, que no se han mantenido actualizados.

### Sprint 1: Setup Base
- [ ] Crear módulo `frontend-nextjs` en el mono-repo Maven
- [ ] Setup Next.js 15 + TypeScript + Tailwind + shadcn/ui
- [ ] Implementar autenticación JWT con Spring Security
- [ ] Crear layout base: sidebar con todos los módulos

### Sprint 2-3: Core Workflow
- [ ] Migrar CU01-CU16: Gestiones (workflow principal del negocio)
- [ ] Tests Playwright para el flujo completo gestión → escritura → pago

### Sprint 4-5: Clientes & Personas
- [ ] Migrar CU17-CU19, CU41, CU46, CU54, CU61

### Sprint 6-7: Administración
- [ ] Migrar CU20-CU23, CU26-CU38, CU48, CU51

### Sprint 8: Presupuestos & Escrituras
- [ ] Migrar CU39, CU45, CU49, CU52, CU55, CU60, CU62

### Sprint 9: Reportes & Protocolos
- [ ] Migrar CU24, CU25, CU50 (PDF via JasperReports API)
- [ ] Migrar CU28, CU33, CU36, CU40, CU63-CU68

### Sprint 10: E2E & Deprecation
- [x] Playwright E2E para todos los flujos (`frontend/tests/e2e/`, 33+ `cuNN-*.spec.ts`)
- [x] Deprecar `frontend-swing` (renombrado a `deprecated-frontend-swing`, excluido del
      reactor Maven raíz — eliminación completa aún pendiente, ver issue #811)

## References

- [Next.js 15 Documentation](https://nextjs.org/docs)
- [shadcn/ui Components](https://ui.shadcn.com/)
- [TanStack Query](https://tanstack.com/query/latest)
- [Playwright Testing](https://playwright.dev/)
- [openapi-typescript](https://openapi-ts.dev/)
- ADR-001: Microservices Architecture
- ADR-003: REST API Versioning
- Issue #240: Complete remaining form migration
- Milestone: Phase 3 - Modern Frontend
