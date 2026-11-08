# System assessment refresh — 2026-10-18

| Field | Value |
|-------|-------|
| Baseline | `main` @ `7d88110f` (release **0.2.4**) |
| Supersedes figures in | [ASSESSMENT-2026-10.md](ASSESSMENT-2026-10.md) (baseline `d4473af2` / 2026-10-04) |
| Tracking | #1926 (OpenSpec archive hygiene); prior umbrella #1242 (CLOSED) |
| Use Case | CU76 |

Measured on the baseline commit above. This refresh **corrects stale PE-visible claims** from the October-04 assessment; it does not re-run every suite from scratch (see live gate evidence below).

## 1. Summary (updated)

| Dimension | Verdict | What changed since 2026-10-04 |
|-----------|---------|-------------------------------|
| Functionality | Good | 87 use cases; **40** pages; **64** controllers; case-summary UI+API shipped (slice of #774) |
| Test coverage | Backend strong; frontend much stronger | Frontend Vitest ~**1235** tests, lines ~**84%** in local Sonar/preflight runs (was ~16%) |
| Software architecture | Hex far advanced | R6 clear on non-legacy→jpa/service (#1915); **~35** `jpa` Java files remain as legacy island |
| Security strategy | Perimeter + **RBAC** | `SecurityAndCorsConfig` enforces `hasRole(ADMIN)` on admin paths/writes (#559 progress; not “no role checks”) |
| DevSecOps | Mature + local Sonar ratchet | `run_pipeline.sh --with-infra` green; Sonar token/new-code/Playwright path hardened (#1924) |
| Observability | Verified | `verify-signals.sh` **11/11** with app+infra up (Prometheus, Loki, Tempo via Grafana, Sonar projects) |
| AI SDLC / OpenSpec | Mostly clean | CLOSED-issue leftovers archived (#1923, #1926); **1** active change (`docs-1445-…`); #774 remains OPEN for further gestión slices |
| Documentation | Organised; Oct-04 prose partly stale | This refresh; SAD + **30** ADRs |

## 2. Live gate evidence (2026-10-17/18)

| Gate | Result | Evidence |
|------|--------|----------|
| `run_pipeline.sh --with-infra` | **PASSED** (~34 min) | `reports/pipeline/20261017-142511/` |
| Playwright E2E | **724** passed, **3** skipped, 0 failed | local full suite |
| Bruno / DB V&V / Docker smoke | passed | inside pipeline preflight --full |
| Sonar QG backend + frontend | **OK** | after #1924 hygiene |
| Security local catalog | PASS | `python3 security/run_gates.py --profile local` |
| `verify-signals.sh` | **11/11** | logs + metrics + traces + Sonar |
| OpenSpec inventory | no CLOSED leftovers | after archiving chore-1862 + feat-774 trees |

## 3. Inventory (live)

| Item | Count |
|------|-------|
| Use Cases CU01–CU87 | 87 |
| ADRs | 30 |
| Frontend `page.tsx` | 40 |
| `*Controller.java` | 64 |
| Playwright `*.spec.ts` | 100 |
| Product version | 0.2.4 |
| Active OpenSpec changes | 1 (`docs-1445-archive-closed-1462`) |

## 4. Residual risks a principal engineer may still note

| Topic | Status |
|-------|--------|
| Sonar legacy inventory (~700 `accepted_risk` rows) | Documented debt; new-code severity ratchet still enforced |
| #774 further slices (create linked records from gestión) | Issue OPEN; case-summary **slice** shipped and OpenSpec tree archived |
| #1445 Owner topology / Pages decisions | OPEN umbrella |
| Automated accessibility | Still gap (#1251 family) |
| Oct-04 assessment body | Historical; use **this file** for current verdicts |

## 5. Refresh commands

```bash
python3 workspace/sdlc/check-openspec-archive-hygiene.py --inventory-only
bash workspace/sdlc/validate-sdlc-plan.sh
bash workspace/sdlc/run_pipeline.sh --with-infra
bash infra/scripts/verify-signals.sh
```
