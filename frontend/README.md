# Notaire — Frontend

Modern web frontend for the Notaire notary management system. Replaces the Java Swing client.

## Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 15 | App Router SSR/CSR |
| TypeScript | 5.x | Type safety |
| Tailwind CSS | 4.x | Styling |
| TanStack React Query | 5.x | Server state / API calls |
| Zustand | 5.x | Client state (auth) |
| Vitest | 2.x | Unit tests |
| Playwright | 1.x | E2E tests |

## Prerequisites

- Node.js 22+
- Backend running at `http://localhost:8080` (see root `scripts/start.sh`)

## Setup

```bash
cd frontend
cp .env.local.example .env.local   # adjust API URL if needed
npm install
npm run dev                         # http://localhost:3000
```

## Available Scripts

```bash
npm run dev         # Development server
npm run build       # Production build
npm run start       # Production server
npm run lint        # ESLint (flat config, zero-warning gate)
npm run typecheck   # TypeScript type-check (tsc --noEmit)
npm run test        # Vitest unit tests
npm run test:watch  # Watch mode
npm run test:e2e    # Playwright E2E (backend must be running)
```

## Modules Implemented

| Module | CU Coverage | Path |
|--------|-------------|------|
| Login | CU — Auth | `/login` |
| Dashboard | — | `/dashboard` |
| Gestiones | CU02, CU13–16, CU19, CU53 | `/dashboard/gestiones` |
| Presupuestos | CU01, CU39, CU45, CU49, CU55, CU60 | `/dashboard/presupuestos` |
| Personas | CU17, CU18, CU21, CU41, CU46, CU48, CU51, CU54, CU61 | `/dashboard/personas` |
| Escrituras | CU05–08, CU52, CU62 | `/dashboard/escrituras` |
| Pagos | CU15, CU47 | `/dashboard/pagos` |
| Protocolo | CU24, CU28, CU63 | `/dashboard/protocolo` |
| Administración | — | `/dashboard/administracion` |
| → Usuarios | CU20, CU21, CU23 | `/dashboard/administracion/usuarios` |
| → Conceptos | CU29, CU66 | `/dashboard/administracion/conceptos` |
| → Tipos Documento | CU27, CU65 | `/dashboard/administracion/documentos` |
| → Folios | CU28, CU40, CU58, CU68 | `/dashboard/administracion/folios` |
| → Tipos Trámite | CU26, CU57, CU64 | `/dashboard/administracion/tramites` |
| → Estados Gestión | CU67 | `/dashboard/administracion/estados-gestion` |
| → Plantillas | CU39, CU49, CU55 | `/dashboard/administracion/plantillas` |

## API

All API calls go through `src/lib/api-client.ts` — never use `fetch()` directly in components.

Backend base URL is configured via `NEXT_PUBLIC_API_URL` env variable.

## Architecture

```
src/
├── app/            # Next.js App Router pages
├── components/
│   ├── ui/         # shadcn/ui primitives
│   ├── layout/     # Sidebar, header
│   └── shared/     # DataTable, ConfirmDialog
├── hooks/          # React Query hooks (one per resource)
├── lib/            # api-client, query-client, utils
├── store/          # Zustand stores
└── types/          # TypeScript interfaces matching backend DTOs
```

## Docker

```bash
docker build -t notaire-frontend \
  --build-arg NEXT_PUBLIC_API_URL=http://backend:8080/api/v1 \
  -f frontend/Dockerfile frontend/
```

Or use `docker-compose up` from the project root (after adding the frontend service to docker-compose.yml).
