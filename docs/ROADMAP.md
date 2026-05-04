# Notaire - Project Roadmap

**Objetivo:** Migrar el monolito Java Swing a una arquitectura de microservicios moderna, 100% testeada, observable, segura y desplegada en Kubernetes local.

---

## Vision

```
[Java Swing Monolith]
        │
        ▼
[Spring Boot REST API] ←→ [PostgreSQL]
        │
        ▼
[React/Next.js Frontend] ←→ [Spring Boot API] ←→ [PostgreSQL]
        │                            │
        ▼                            ▼
[Playwright E2E]            [Kubernetes (k3d)]
                                     │
                            [Prometheus + Grafana]
                            [OpenTelemetry]
```

---

## Phase 0 — Foundations (Due: June 2026)
**GitHub Milestone:** [Phase 0 - Foundations](https://github.com/matiaspakua/notaire/milestone/1)

**Goal:** Base sólida: documentación completa, standards, CI/CD funcional, 80% test coverage.

| Area | Tasks | Issues |
|------|-------|--------|
| Documentation | Consolidate docs to SDLC structure | #258, #260, #261 |
| Documentation | Migrate all docs to Markdown | #258 |
| Use Cases | Document all 73 CUs with GitHub issues | #154-#221 |
| Architecture | Create ADRs for key decisions (ADR-001 to ADR-010) | #291 |
| Code Standards | Inline documentation & Javadoc | #297 |
| CI/CD | Code review process & checklist | #279 |
| Testing | Tag unit tests with requirement IDs | #251 |
| Docs | Breaking change & deprecation process | #289 |
| Docs | Production installation guide | #259 |

**Definition of Done:**
- [x] Toda la documentación en `/docs` con estructura SDLC (73 CUs)
- [ ] CI pipeline verde con: build + tests + coverage + checkstyle
- [ ] 80% coverage en backend-api
- [x] ADRs para las 10 decisiones arquitectónicas principales

---

## Phase 1 — Backend Complete (Due: August 2026)
**GitHub Milestone:** [Phase 1 - Backend Complete](https://github.com/matiaspakua/notaire/milestone/2)

**Goal:** API REST 100% funcional, segura, documentada y testeada.

| Area | Tasks | Issues |
|------|-------|--------|
| API | Complete remaining 5% of REST endpoints | #246 |
| API | Pagination in all endpoints | #247 |
| API | Swagger/OpenAPI documentation complete | #257, #287 |
| API | Error handling strategy & standards | #263 |
| API | REST API versioning & deprecation | #272 |
| Security | JWT/OAuth2 authentication | #245 |
| Security | API authentication & authorization | #262 |
| Security | Rate limiting configuration | #280 |
| Database | Flyway for schema migration | #252 |
| Database | DB schema documentation & ER diagrams | #292 |
| Database | Data migration guide (MySQL → PostgreSQL) | #264 |
| Documentation | Full Use Case documentation (73 CUs) | #313 |
| Architecture | Complete ADRs and Diagrams | #314 |
| Testing | Spring Boot testing best practices | #276 |
| Testing | Integration test isolation strategy | #296 |
| Testing | Test data management & fixtures | #295 |
| Testing | Complete business workflows testing | #242 |
| Testing | Verify PDF report generation via REST | #243 |
| Backend | Spring transaction management guide | #277 |
| Backend | JPA lazy/eager loading optimization | #278 |
| Backend | Custom exception & error handling | #299 |
| Backend | Caching strategy | #298 |
| Backend | DTO mapping guide | #300 |

**Definition of Done:**
- [ ] Todos los endpoints REST con tests de integración
- [ ] JWT funcional con roles y permisos
- [ ] Swagger UI completo en /swagger-ui.html
- [ ] Flyway para todas las migraciones DB
- [ ] 0 vulnerabilidades CRITICAL/HIGH en Trivy

---

## Phase 2 — Kubernetes & Observability (Due: October 2026)
**GitHub Milestone:** [Phase 2 - Kubernetes & Observability](https://github.com/matiaspakua/notaire/milestone/3)

**Goal:** Sistema desplegado en k3d local con observabilidad completa.

### Stack de Infraestructura

```
k3d (Kubernetes local)
├── notaire-backend     (Spring Boot)
├── notaire-postgres    (PostgreSQL 16)
├── prometheus          (metrics)
├── grafana             (dashboards)
├── jaeger              (tracing)
└── nginx-ingress       (routing)
```

| Area | Tasks | Issues |
|------|-------|--------|
| Kubernetes | K8s manifests & Helm charts | #253, #301 |
| Kubernetes | Infrastructure as Code (IaC) | #302 |
| Kubernetes | Environment config management | #265, #284 |
| Monitoring | Prometheus logging format | #249 |
| Monitoring | Grafana dashboard | #250 |
| Monitoring | Metrics collection & custom metrics | #285 |
| Monitoring | Log aggregation & analysis | #305 |
| Tracing | Distributed tracing with OpenTelemetry | #304 |
| Alerting | Alert rules & escalation | #286 |
| Alerting | SLOs and SLIs definition | #306 |
| Security | Secrets management (Vault/K8s secrets) | #283 |
| Security | Dependency vulnerability management | #282 |
| Security | HTTPS/TLS configuration | #254 |
| Operations | Incident response & rollback | #266 |
| Operations | Disaster recovery with RTO/RPO | #270 |
| Operations | On-call runbooks | #288 |
| Operations | DB maintenance procedures | #271 |
| Operations | DB backups automated | #256 |
| Performance | Load testing procedures & baseline | #303 |
| Performance | DB and app performance tuning | #290 |
| Logging | Logging & monitoring config guide | #273 |

**Definition of Done:**
- [ ] `kubectl apply` despliega todo el stack en k3d
- [ ] Grafana dashboard mostrando métricas en tiempo real
- [ ] Traces distribuidos visibles en Jaeger/Tempo
- [ ] Alertas configuradas para SLOs definidos
- [ ] Runbooks documentados y validados

---

## Phase 3 — Modern Frontend (Due: January 2027)
**GitHub Milestone:** [Phase 3 - Modern Frontend](https://github.com/matiaspakua/notaire/milestone/4)

**Goal:** Reemplazar Java Swing con React/Next.js. 100% testeado.

### Stack Frontend

```
Next.js 15 (App Router)
├── TypeScript
├── Tailwind CSS
├── shadcn/ui components
├── React Query (server state)
├── Zustand (client state)
├── Vitest (unit tests)
└── Playwright (E2E tests)
```

### Módulos a migrar

Todos los 68 casos de uso del sistema Swing, incluyendo:
- Gestión de escrituras y presupuestos
- Clientes y personas
- Folios y protocolos
- Reportes (PDF via JasperReports API)
- Administración (usuarios, escribanos, tipos, conceptos)

| Area | Tasks | Issues |
|------|-------|--------|
| Migration | Complete remaining Swing form migration | #240 |
| Migration | Validate all forms with REST API | #241 |
| Frontend | REST client error handling & retry | #293 |
| Frontend | Swing component patterns (reference) | #294 |
| Frontend | REST client implementation guide | #274 |

**New Issues to Create:**
- [ ] Setup Next.js project with TypeScript + Tailwind
- [ ] Implement authentication flow (JWT)
- [ ] Create component library (shadcn/ui)
- [ ] Migrate each module (one PR per CU group)
- [ ] Vitest unit tests for all components
- [ ] Playwright E2E test suite

**Definition of Done:**
- [ ] Swing deprecated y reemplazado por Next.js
- [ ] Todos los 68 CUs funcionando en el nuevo frontend
- [ ] >80% cobertura con Vitest
- [ ] Playwright tests para los flujos principales

---

## Phase 4 — Production Ready (Due: March 2027)
**GitHub Milestone:** [Phase 4 - Production Ready](https://github.com/matiaspakua/notaire/milestone/5)

**Goal:** Auditoría completa de seguridad, E2E tests automatizados, compliance.

| Area | Tasks | Issues |
|------|-------|--------|
| Security | OWASP Top 10 compliance checklist | #267 |
| Security | Input validation strategy | #268 |
| Security | SQL injection prevention | #269 |
| Security | OWASP ZAP API security testing | #281 |
| Security | Security policy as code | #307 |
| Operations | Feature flag implementation | #308 |
| Compliance | Compliance requirements & audit trail | #309 |

**Definition of Done:**
- [ ] 0 findings CRITICAL en OWASP ZAP
- [ ] E2E tests automatizados en CI para todos los flujos
- [ ] Compliance checklist completado y auditado
- [ ] Feature flags implementados para rollout gradual

---

## Workflow de Trabajo

### Branch Strategy

```
main
├── docs/consolidate-new-structure    (PR #313)
├── feat/backend-jwt-auth             (#245)
├── feat/backend-flyway               (#252)
├── feat/k8s-manifests                (#253, #301)
├── feat/frontend-nextjs-setup        (nuevo)
└── ...
```

### PR Convention

```
<type>(<scope>): <description>

Types: feat, fix, docs, refactor, test, build, ci, chore
Scope: backend, frontend, k8s, docs, api, db, security

Examples:
  feat(backend): implement JWT authentication
  docs(arch): add ADR-005 for frontend tech choice
  ci(k8s): add Kubernetes deployment workflow
```

### Issue Convention

Cada issue debe tener:
- Label de área: BACKEND, FRONTEND, DEVOPS, TEST, DOC, DB, SECURITY
- Label de prioridad: priority:critical, priority:high, priority:medium, priority:low
- Milestone asignado (Phase 0-4)
- Referencia a documentación cuando aplica

---

## Tools & Technologies

| Category | Tool | Purpose |
|----------|------|---------|
| Backend | Spring Boot 4.0.4 + Java 21 | REST API |
| Database | PostgreSQL 16 | Persistence |
| ORM | Hibernate 6 + Flyway | DB access + migrations |
| Security | Spring Security + JWT | Authentication |
| Docs | Swagger/OpenAPI 3 | API docs |
| Build | Maven 3.9 | Build tool |
| Container | Docker + Docker Compose | Local dev |
| Orchestration | k3d (Kubernetes in Docker) | K8s local |
| IaC | Helm | K8s package manager |
| Monitoring | Prometheus + Grafana | Metrics |
| Tracing | OpenTelemetry + Jaeger | Distributed tracing |
| Logging | ELK / Loki + Grafana | Log aggregation |
| Frontend | Next.js 15 + TypeScript | Modern UI |
| UI Library | shadcn/ui + Tailwind | Components |
| Frontend Tests | Vitest + Testing Library | Unit tests |
| E2E Tests | Playwright | Browser automation |
| CI/CD | GitHub Actions | Pipeline |
| Security Scan | Trivy + OWASP ZAP | Vulnerability scanning |
| Code Quality | JaCoCo + Checkstyle + SpotBugs | Static analysis |
| Diagrams | draw.io + PlantUML | Architecture diagrams |

---

*Last updated: 2026-04-28*
*Maintainer: Matías Miguez*
