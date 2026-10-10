/** Curated technical-doc copy for the Pages Docs tab (#1414). Deep links point at GitHub SSOT. */

export const REPO = "https://github.com/matiaspakua/notaire";
export const DOCS = `${REPO}/blob/main/docs`;

export const MODULE_OWNERSHIP_CHART = `
flowchart TB
  subgraph product [Product]
    BE[backend-api]
    FE[frontend]
  end
  subgraph platform [Platform]
    INF[infra]
    SEC[security]
  end
  subgraph assurance [Assurance]
    QA[testing]
  end
  subgraph knowledge [Knowledge]
    DOC[docs]
  end
  subgraph ai [AI optional]
    LAI[local-ai]
  end
  subgraph control [Control plane]
    CTR[contracts]
    WS[workspace]
  end
  FE --> BE
  INF --> BE
  QA --> BE
  QA --> FE
  WS --> BE
  WS --> FE
  WS --> INF
  WS --> QA
  WS --> LAI
  WS --> DOC
  WS --> CTR
  WS --> SEC
`;

export const SYSTEM_CONTEXT_CHART = `
flowchart LR
  User[Notary staff] --> FE[frontend Next.js]
  FE -->|HTTPS JWT| BE[backend-api Spring Boot]
  BE --> PG[(PostgreSQL 16)]
  BE --> AUD[Audit trail]
  Ops[Operators] --> OBS[infra Prometheus Grafana Loki]
  OBS --> BE
  Agents[Cursor Cloud fleet] --> Repo[Monorepo modules]
  Repo --> BE
  Repo --> FE
  Repo --> DOC[docs OpenSpec]
`;

export const MODULES = [
  {
    name: "backend-api",
    fleet: "backend",
    responsibility: "REST API, business rules, persistence and Flyway schema",
    verify: "bash backend-api/verify.sh",
  },
  {
    name: "frontend",
    fleet: "frontend",
    responsibility: "Next.js web client; talks to the API over HTTP only",
    verify: "bash frontend/verify.sh",
  },
  {
    name: "infra",
    fleet: "infrastructure",
    responsibility: "Observability, quality stack, Kubernetes and load-test assets",
    verify: "bash infra/verify.sh",
  },
  {
    name: "testing",
    fleet: "qa",
    responsibility: "Black-box QA suites (E2E, integration smoke, database V&V)",
    verify: "bash testing/verify.sh",
  },
  {
    name: "local-ai",
    fleet: "ai",
    responsibility: "Local AI SDLC engine; Cursor Cloud fleet does not use it",
    verify: "bash local-ai/verify.sh",
  },
  {
    name: "docs",
    fleet: "knowledge",
    responsibility: "Business and architecture knowledge base; OpenSpec root",
    verify: "bash docs/verify.sh",
  },
  {
    name: "security",
    fleet: "security",
    responsibility: "Repository protection as code and the map of every security control",
    verify: "bash security/verify.sh",
  },
  {
    name: "contracts",
    fleet: "foreman",
    responsibility: "Seams between modules and the guards that keep both sides in step",
    verify: "bash contracts/verify.sh",
  },
  {
    name: "workspace",
    fleet: "foreman",
    responsibility: "Unifier, manifest and cross-module guards (includes .github)",
    verify: "bash workspace/verify.sh",
  },
] as const;
