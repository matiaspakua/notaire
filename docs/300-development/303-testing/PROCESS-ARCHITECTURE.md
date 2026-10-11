# Testing process architecture — Notaire

**Document ID:** DOC-TESTARCH-001  
**Companion:** [Master Test Plan](TEST-PLAN.md) · [INVENTORY.md](INVENTORY.md)  
**Issue / UC:** #1511 / CU76

High-level process boxes for QA Automation in the AI SDLC. Each box has clear
inputs, outputs, owners, and gates — so Dev, Sec, Ops, and QA stay synchronized.

---

## 1. Process overview

```text
  [Prepare]     →  Issue + CU + OpenSpec Gate 1
       │
  [Define]      →  Scenarios, TC design, test level choice
       │
  [Red]         →  Failing tests first (Gate 2)
       │
  [Implement]   →  Minimum code to green
       │
  [Verify]      →  Unit → Integration → API → E2E (fail-fast)
       │
  [Evidence]    →  Reports, inventory, matrix, traceability
       │
  [Release]     →  Heavy CI + smoke (Gates 4–5)
```

---

## 2. Process boxes

| Process | Inputs | Outputs | Owner | Gate |
|---------|--------|---------|-------|------|
| **Prepare** | Idea / bug / exploration finding | GitHub Issue + CU link | Worker / Foreman | Gate 1 start |
| **Specify** | Issue | OpenSpec proposal/design/tasks/specs | openspec-planner | Gate 1 |
| **Define tests** | Scenarios | TC notes + chosen levels | testing-qa / implementer | Gate 2 |
| **Automate (red→green)** | Specs | JUnit / Vitest / Bruno / TS-nnnn | backend / frontend / QA | Gate 2–3 |
| **Integrate** | Branch | Green preflight + heavy CI | devops + workers | Gate 3–4 |
| **Secure** | Running stack / deps | SAST/SCA/DAST evidence | security-auditor | pipeline / weekly |
| **Operate evidence** | CI artifacts | Actions / Pages reports (no bot wiki commits) | devops | Gate 5 |

---

## 3. Suite ownership

| Suite | Module path | Local entry | CI workflow |
|-------|-------------|-------------|-------------|
| Unit / Integration (Java) | `backend-api/src/test` | `mvn test` / `mvn verify` | `ci.yml` |
| Vitest | `frontend/src/**/*.test.*` | `npm test` | `frontend-ci.yml` |
| Bruno | `backend-api/api-test` | `bru` / preflight `--full` | `playwright-e2e.yml` |
| OpenAPI contract | `backend-api/openapi` | `oasdiff` via preflight | `openapi-contract.yml` |
| Playwright E2E | `testing/e2e` | `bash testing/scripts/run.sh e2e` | `playwright-e2e.yml` |
| HTTP black-box | `testing/integration` | `run.sh integration` | preflight `--full` only |
| Database V&V | `testing/database` | `run.sh database` | `database-vv.yml` |
| k6 | `infra/performance/k6` | infra scripts | `performance-test.yml` |
| DAST ZAP | security catalog | `run_gates.py` / pipeline | `dast-zap.yml` |
| Process / inventory | `workspace/sdlc`, `testing/tests` | `preflight.sh` | `sdlc-process.yml` + preflight |

---

## 4. Knowledge base layout

```text
docs/300-development/303-testing/     ← process knowledge (MTP, patterns, matrix)
  TEST-PLAN.md                        ← Master Test Plan (DOC-TESTPLAN-001)
  PROCESS-ARCHITECTURE.md             ← this file
  INVENTORY.md                        ← generated living index
  CU-API-MATRIX.csv                   ← CU ↔ API ↔ Bruno
  test-cases/                         ← optional TC-* narratives
testing/                              ← black-box automation module
backend-api/.../unit|integration      ← white-box automation
frontend/src/**/*.test.*              ← component automation
```

---

## 5. Drift guards (mechanical)

| Guard | Command | Prevents |
|-------|---------|----------|
| CU-API matrix | `python3 workspace/sdlc/validate-cu-api-matrix.py` | Stale controllers / Bruno paths |
| Testing inventory | `python3 workspace/sdlc/validate-testing-inventory.py` | Stale INVENTORY / unprefixed E2E |
| OpenSpec plan | `bash workspace/sdlc/validate-sdlc-plan.sh` | Incomplete Gate 1 |
| TDD evidence | `bash workspace/sdlc/check-tdd-evidence.sh` | Prod change without tests |
| Doc traceability | `python3 workspace/sdlc/check-doc-traceability.py` | Claimed docs not in diff |

All of the above (except heavy suite runners) run in `preflight.sh`.

---

## 6. Alignment with specialization guidelines

| Practice | Notaire realization |
|----------|---------------------|
| Organización / base de conocimiento | `docs/` + `testing/` + OpenSpec |
| Identificación | CU / TC / TS / DOC / Issue IDs |
| Trazabilidad | CU-API matrix, OpenSpec traceability, TS headers |
| Pirámide / fail-fast | Levels in MTP §3; unit first in CI |
| MTP | This folder’s `TEST-PLAN.md` |
| QA Automation as SDLC software | Generators + unittest guards + preflight |

---

## Navigation

- [Master Test Plan](TEST-PLAN.md)
- [Inventory](INVENTORY.md)
- [AI SDLC cloud](../304-ai-sdlc-cloud/README.md)
- [CI preflight map](../CI-PREFLIGHT.md)
