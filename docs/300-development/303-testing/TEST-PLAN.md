# Master Test Plan (MTP) — Notaire

**Document ID:** DOC-TESTPLAN-001  
**Authority:** Constitution §7 (this file is the master reference)  
**Use Case:** CU76 — Quality Assurance and Testing Infrastructure  
**Related:** [PROCESS-ARCHITECTURE.md](PROCESS-ARCHITECTURE.md), [INVENTORY.md](INVENTORY.md), [ADR-006](../../200-architecture/202-ADR/ADR-006-testing-strategy.md)

This Master Test Plan defines Notaire’s product-wide testing strategy: levels,
artifact identification, traceability, environments, automation triggers,
evidence, and exit criteria. It follows DevSecOps / QA Automation practices
(fail-fast pyramid, living knowledge base, forward/backward traceability).

---

## 1. Introduction

### 1.1 Product

Notaire is a three-tier notarial management system: PostgreSQL 16, Spring Boot
REST API (`backend-api/`), Next.js frontend (`frontend/`), with black-box QA
suites under `testing/`.

### 1.2 Purpose of the Master Test Plan

- Define what is in scope for verification and validation.
- Assign test levels, owners, and CI triggers (fail-fast).
- Define unique IDs for test artifacts (identificación).
- Point to the living inventory so suite counts never rot in prose.

### 1.3 Audience

Developers, QA, AI coding agents, and reviewers operating under the AI SDLC
(`docs/300-development/304-ai-sdlc-cloud/`).

---

## 2. Scope

### 2.1 System under test (SUT)

| Surface | In scope |
|---------|----------|
| Backend domain / services | Unit + Spring integration |
| REST API | Bruno, OpenAPI contract, Spring ITs |
| Database migrations | Flyway V&V (`testing/database/`) |
| Web UI | Vitest + Playwright E2E |
| Security | SAST/SCA (gates), DAST (ZAP weekly), CSRF/CORS E2E |
| Performance | k6 (`infra/performance/k6`, weekly) |

### 2.2 Out of scope

- Retired Swing / Robot E2E (`testing/e2e-swing/`) — ADR-012 / #811
- Production data as fixtures (synthetic / seeded only)
- Raising Vitest floors to 80% in this document (tracked #1246)

---

## 3. Strategy

### 3.1 Criticality

| Priority | Meaning | Examples |
|----------|---------|----------|
| Critical | Blocks merge / revenue flows | Auth, budgets, gestiones, payments, deeds |
| High | Core CU golden paths | Testimony, people/clients, admin catalogs |
| Medium | Secondary admin / QA attributes | i18n, a11y, responsive |
| Low | Demos / tutorials | TS-0070, TS-0071, TS-0090 |

### 3.2 Test levels (fail-fast pyramid)

```text
        +-------------------------------+
        | Security / Performance        |  weekly / pipeline
        +-------------------------------+
      +-----------------------------------+
      | Playwright E2E (TS-nnnn)          |  PR (path-scoped) + --full
      +-----------------------------------+
    +---------------------------------------+
    | Bruno API + OpenAPI contract          |  PR (path-scoped)
    +---------------------------------------+
  +-------------------------------------------+
  | Integration (Spring H2 + PG) + DB V&V     |  PR / database-vv
  +-------------------------------------------+
+-----------------------------------------------+
| Unit (JUnit) + Component (Vitest)             |  every product PR
+-----------------------------------------------+
```

Live counts: **[INVENTORY.md](INVENTORY.md)** (generated; do not copy numbers here).

### 3.3 Identificación (artifact IDs)

| Kind | Pattern | Example | Role |
|------|---------|---------|------|
| Use Case | `CU##` | CU01 | Business requirement |
| Logical test case | `TC-*` | TC-001, TC-LOGIN-001 | Narrative / design (optional) |
| Automation suite (E2E) | `TS-nnnn-*.spec.ts` | TS-0010-budget-workflow | Playwright script |
| Bruno request | folder / `NN-name.yml` | `budgets/01-create.yml` | API automation |
| JUnit class | `*Test.java` | `BudgetServiceTest` | Unit / integration |
| Document | `DOC-*` | DOC-TESTPLAN-001 | Knowledge base |
| Issue | `#n` | #1511 | Work tracking |

**Traceability chain:**  
`Issue → CU → (TC) → TS / Bruno / JUnit → evidence (CI artifacts)`.

Forward/backward: every Essential CU appears in
[`CU-API-MATRIX.csv`](CU-API-MATRIX.csv); every Playwright file must match
`TS-nnnn-*.spec.ts` (guard: `validate-testing-inventory.py`).

---

## 4. Coverage floors (enforced vs aspirational)

| Tool | Enforced floor | Aspirational target | Source of truth |
|------|----------------|---------------------|-----------------|
| JaCoCo (backend) | **80% line / 65% branch** | 80% / 80% | `backend-api/pom.xml` |
| Vitest (frontend) | **raise-only** `statements/lines 14`, `branches 9`, `functions 10` | 80% line (#1246) | `frontend/vitest.config.ts` |
| Bruno | Matrix drift guard; gaps as `MISSING`+#953 | 100% endpoints | `validate-cu-api-matrix.py` |
| Playwright | ≥1 suite per critical CU workflow; TS naming | Full CU matrix | INVENTORY + E2E mapping |

Never document a floor that tools do not enforce.

---

## 5. Environments and execution

| Trigger | Suites |
|---------|--------|
| Local commit | Unit (backend/frontend) |
| `preflight.sh` | Process guards + inventory + verify + Vitest + e2e lint |
| `preflight.sh --full` | + Playwright + Bruno + DB V&V + Docker smoke |
| PR (path-scoped) | Matching leaf jobs; aggregators accept intentional skip |
| `main` / schedule | Full heavy suite; weekly ZAP / k6 |

Commands and suite ownership: [testing/README.md](../../../testing/README.md),
[CI-PREFLIGHT.md](../CI-PREFLIGHT.md), [PROCESS-ARCHITECTURE.md](PROCESS-ARCHITECTURE.md).

---

## 6. E2E automation (Playwright)

### 6.1 Nomenclature

All UI E2E specs: **`TS-nnnn-<workflow-name>.spec.ts`**.

Category bands (guidance; inventory is authoritative):

| Range | Category |
|-------|----------|
| TS-0001–0003 | Auth & navigation |
| TS-0010–0035 | Core business workflows |
| TS-0040–0051 | QA / technical / API cycle |
| TS-0060–0091 | Regression, tutorials, items |
| TS-0092–0125 | Feature / UX / a11y / motion |
| TS-0130–0138 | Former unprefixed feature specs (renamed #1511) |

Full table: [INVENTORY.md](INVENTORY.md). CU narrative mapping:
[E2E-TEST-MAPPING.md](E2E-TEST-MAPPING.md).

### 6.2 Fixtures

GherkinSteps + API helpers + global auth setup. Patterns:
[TESTING-PATTERNS.md](TESTING-PATTERNS.md).

### 6.3 Skip policy

`test.skip` only with an open `#issue` for a real product gap — never empty
tables or timing flakes (#1066 / CU76). Guard:
`testing/tests/test_e2e_reliability.py`.

### 6.4 Quality gates for E2E

Before merge (UI changes): stack green, issue-linked skips only, CU comments,
specific assertions. Heavy CI merge gate:
`bash workspace/sdlc/check-heavy-ci.sh <pr>`.

---

## 7. Traceability: CU → API → Bruno

[`CU-API-MATRIX.csv`](CU-API-MATRIX.csv) +
`python3 workspace/sdlc/validate-cu-api-matrix.py` (preflight / sdlc-process).

Logical case examples (not complete catalog):
[`test-cases/`](test-cases/README.md). Login design notes:
[`test-suite/LOGIN-TEST-SUITE.md`](test-suite/LOGIN-TEST-SUITE.md).

---

## 8. Evidence and reporting

| Evidence | Location |
|----------|----------|
| Playwright HTML / traces | `testing/e2e/playwright-report/`, Actions artifacts |
| JaCoCo / Vitest | `backend-api/target/site/jacoco/`, `frontend/coverage/` |
| Bruno | `backend-api/api-test/COVERAGE.md` |
| Pipeline dashboard | `reports/pipeline/<ts>/` via `run_pipeline.sh` |
| Aggregated coverage site | `test-coverage-report.yml` (never bot-commit wiki reports — #1041) |

---

## 9. Flaky tests and change policy

- CI Playwright retries ≤ 1; `trace: on-first-retry`.
- Quarantine requires Owner + open issue; no silent disable.
- Inventory or matrix drift fails the process gate — regenerate, do not edit
  generated tables by hand.

---

## 10. Exit criteria (release / Gate 3–5)

- [ ] Unit + integration + Vitest green at enforced floors
- [ ] Bruno + OpenAPI contract gates green (or accepted-breaking entry)
- [ ] Playwright green for UI surface; inventory validation green
- [ ] Security local gates + heavy CI for merge
- [ ] Permanent docs updated; OpenSpec change archiveable

---

## Navigation

- [← Testing README](README.md)
- [Process architecture](PROCESS-ARCHITECTURE.md)
- [Living inventory](INVENTORY.md)
- [Testing patterns](TESTING-PATTERNS.md)
- [E2E mapping](E2E-TEST-MAPPING.md)
- [CU-API-MATRIX.csv](CU-API-MATRIX.csv)
