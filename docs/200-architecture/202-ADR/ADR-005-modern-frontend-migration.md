# ADR-005: Frontend migration from Java Swing to Next.js

**Status:** Accepted (Swing directory deleted under #1046)
**Date:** 2026-04-13
**Deciders:** Matías Miguez
**Supersedes:** N/A
**Related:** ADR-001 (Microservices Architecture), ADR-003 (REST API Versioning)

## Context

The `frontend-swing` module was an intermediate step in the monolith migration:
it was refactored to call the REST backend instead of the database directly,
but it was still a desktop Java Swing application.

> **Current state:** the Next.js migration is implemented in `frontend/`
> (not `frontend-nextjs/`, which this ADR originally proposed). The Swing module
> was first renamed to `deprecated-frontend-swing/` and excluded from the root
> Maven reactor, then **removed from the tree** in issue #1046 (Dependabot log4j
> noise). History remains in git; do not recreate the Swing client.

### Problems with Java Swing

- **Deprecated technology:** Java Swing has had no active improvements since Java 8. Oracle does not develop it for modern use.
- **No web or mobile support:** Desktop only, with no browser access.
- **Outdated UX:** Limited look and feel, without modern components (reactive tables, charts, dashboards).
- **Difficult testing:** Swing needs specialized tools (AssertJ-Swing, UISpec4J) that are complex and brittle.
- **Complex distribution:** Each client needs a JRE, and updates are manual.
- **Poor fit with modern tooling:** Hard to integrate with DevTools, frontend CI/CD, or the npm ecosystem.

### Options considered

| Option | Pros | Cons |
|--------|------|------|
| Keep Swing | No migration | Every problem listed above |
| JavaFX | Same Java ecosystem | Also deprecated, little adoption |
| Electron | Familiar to Java developers | Very heavy bundle, not web-native |
| Angular | Robust, typed | More verbose, steeper learning curve |
| **Next.js (React)** | Large ecosystem, SSR, TypeScript, modern testing | Requires learning JS/TS |
| Vue.js / Nuxt | Simple, progressive | Smaller ecosystem than React |

### Decision criteria

1. Native TypeScript support (type safety comparable to Java)
2. Modern testing (unit + automated E2E)
3. Performance and SEO (server-side rendering)
4. Mature UI component ecosystem
5. Easy integration with the REST API
6. Deployable as a container

## Decision

**Migrate the frontend to Next.js 15 with TypeScript** as the base technology, plus:

> Update 2026-10: the frontend now runs on Next.js 16 / React 19. The decision (Next.js + TypeScript) does not change.

- **Tailwind CSS** for utility styles
- **shadcn/ui** for accessible, customizable components
- **React Query (TanStack Query)** for server state and API cache
- **Zustand** for client state (auth, UI state)
- **Vitest + Testing Library** for component unit tests
- **Playwright** for automated E2E tests

### New frontend architecture

> The tree below is this ADR's original design. The implemented structure lives
> in `frontend/` (not `frontend-nextjs/`) under `src/`, with some folders renamed.
> See the current structure further down. The Spanish segments in the original
> tree are the routes this ADR planned; they were not shipped.

```text
frontend-nextjs/
├── app/                    # Next.js App Router
│   ├── (auth)/             # Public pages (login)
│   ├── (dashboard)/        # Protected pages
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
│   ├── forms/              # Form components by domain
│   ├── tables/             # Data tables with pagination
│   └── layout/             # Navigation, sidebar, header
├── lib/
│   ├── api-client.ts       # HTTP client (type-safe, fetch-based)
│   ├── auth.ts             # JWT management
│   └── utils.ts            # Helpers
├── hooks/                  # Custom React hooks
├── stores/                 # Zustand stores
├── types/                  # TypeScript types (mirrors backend DTOs)
├── tests/
│   ├── unit/               # Vitest tests
│   └── e2e/                # Playwright tests
└── public/                 # Static assets
```

**Current structure (`frontend/`):**

```text
frontend/
├── src/
│   ├── app/                # Next.js App Router (dashboard/, login/, audit/ redirect stub)
│   ├── components/         # ui/, layout/, motion/, shared/
│   ├── hooks/              # Custom React hooks
│   ├── lib/                # api-client, utils
│   ├── store/              # Zustand stores (auth-store, and others)
│   ├── theme/              # Design tokens + form patterns (tokens.ts, form-patterns.tsx)
│   ├── i18n/               # Internationalization
│   ├── types/              # TypeScript types (mirrors backend DTOs)
│   └── tests/              # Vitest unit/component tests
└── public/                 # Static assets
```

Dashboard routes are English (`managements/`, `people/`, `deeds/`, `budgets/`,
`protocol/`, `administration/`). Message keys such as `suplencias` and
`reportes` stay Spanish. Redirect-only stubs (`/`, `/audit`,
`administracion/items`, `administracion/audit`) stay.

### Migration strategy (Backend for Frontend)

The existing REST backend (`/api/v1`) is exposed directly to the new frontend.
Where several APIs must be composed, or a response needs a frontend-specific
shape, Next.js Route Handlers act as a light BFF (Backend for Frontend).

```text
Browser → Next.js (SSR/CSR) → Spring Boot REST API → PostgreSQL
                    ↓
             Route Handlers (BFF)
             - Session management
             - Token refresh
             - Response aggregation
```

## Consequences

### Positive

- **Full testing:** Vitest (unit) + Playwright (E2E) cover all 68 use cases
- **Modern UX:** Accessible, responsive components, with dark mode
- **Type safety:** TypeScript on the frontend and Java on the backend
- **Deploy:** A Docker container, deployable on Kubernetes the same way as the backend
- **Developer experience:** Hot reload, DevTools, and the npm ecosystem
- **CI/CD:** Native GitHub Actions integration for lint, tests, and build

### Negative

- **Learning curve:** The Java team needs to learn React/TypeScript
- **Two stacks:** Java (backend) and TypeScript (frontend) in the same repo
- **Costly migration:** All 68 use cases need to be reimplemented
- **Duplicated types:** Java DTOs and TypeScript types (mitigated by OpenAPI codegen)

### Mitigations

- Use `openapi-typescript` to generate types from the OpenAPI spec
- Incremental migration: one use-case module at a time
- The Swing module was removed from the tree (#1046); the active client is only `frontend/`

## Implementation plan

> **Status note:** this sprint plan is the ADR's original intent. The migration
> is substantially implemented in `frontend/` (Next.js 16, not a Maven module —
> see [Solution Strategy](../201-SAD/sad.md)). For real coverage by use case, use
> the traceability matrix
> [`CU-API-MATRIX.csv`](../../300-development/303-testing/CU-API-MATRIX.csv)
> instead of the sprint checkboxes below, which were not kept up to date.

### Sprint 1: Base setup

- [ ] Create the `frontend-nextjs` module in the Maven monorepo
- [ ] Set up Next.js 15 + TypeScript + Tailwind + shadcn/ui
- [ ] Implement JWT authentication with Spring Security
- [ ] Create the base layout: a sidebar with every module

### Sprint 2-3: Core workflow

- [ ] Migrate CU01-CU16: managements (the core business workflow)
- [ ] Playwright tests for the full management → deed → payment flow

### Sprint 4-5: Clients and people

- [ ] Migrate CU17-CU19, CU41, CU46, CU54, CU61

### Sprint 6-7: Administration

- [ ] Migrate CU20-CU23, CU26-CU38, CU48, CU51

### Sprint 8: Budgets and deeds

- [ ] Migrate CU39, CU45, CU49, CU52, CU55, CU60, CU62

### Sprint 9: Reports and protocol

- [ ] Migrate CU24, CU25, CU50 (PDF via the JasperReports API)
- [ ] Migrate CU28, CU33, CU36, CU40, CU63-CU68

### Sprint 10: E2E and deprecation

- [x] Playwright E2E for every flow (`testing/e2e/tests/`, 33+ `cuNN-*.spec.ts`)
- [x] Deprecate and remove `frontend-swing` / `deprecated-frontend-swing` from the tree
      (excluded from the reactor; full delete in #1046 — history remains in git)

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
