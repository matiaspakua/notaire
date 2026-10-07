> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1067 (TEST, security, priority:medium, audit-2026-09). Use Cases CU76,
CU78, CU75. Researched on `origin/main` tip `6b246a72`:

| Area | Current state |
|------|---------------|
| SCA | Trivy in `ci.yml` (report-oriented) + image scans in CD |
| DAST | Not present; ADR-006 and DevSecOps README list OWASP ZAP as future |
| OpenAPI | springdoc on classpath; Swagger UI at runtime; **no committed** `openapi.yaml` / PR diff job found |
| Backups | Docs point at #256 (OPEN) for automated `pg_dump`; manual restore examples exist |
| Related | #281 OPEN (ZAP guide docs); #256 OPEN (backup product) |

Queue ahead: `#1040 → #1043 → #1045 → #1056 → #1055 → #1050 → #1058 → #976`
(+ #945/#953 stockpile). Prefer implementing #1067 after Bruno-heavy #953 or in
a quiet CI slot — ZAP jobs are heavy.

## Goals / Non-Goals

**Goals:**

- Phase A: ZAP baseline against compose stack in CI (+ short runbook).
- Phase B: committed OpenAPI + PR diff gate.
- Phase C: backup→restore→smoke workflow **gated** until #256; executable when
  backup exists.

**Non-Goals:**

- Implementing #256 backups inside this issue.
- Full authenticated ZAP attack scan (baseline first).
- Pact consumer/provider matrix as a must-have.
- Closing every ZAP alert in-band.

## Decisions

1. **Three phases, one change / one Issue**
   - Tasks ordered A → B → C. PR may land A+B first with C scaffolded if #256
     still open — but do not close #1067 until Phase C is either green against
     #256 or explicitly deferred with a linked follow-up Issue **and** AC
     renegotiated with a human. Default: keep #1067 open until C is real or
     coordinator accepts a child issue for C-only.

2. **ZAP baseline via official GH Action / container**
   - Target `http://localhost:8080` (or compose service DNS) after
     `scripts/start.sh` (or CI compose equivalent).
   - Auth: use a test JWT / scripted login if baseline spider needs it; start
     with unauthenticated + documented authenticated follow-up if baseline
     cannot reach `/api/v1/**`.
   - Policy: initially publish report artifact; ratchet to fail on CRITICAL/HIGH
     once noise is understood (record allowlist file if needed).

3. **OpenAPI commit + diff**
   - Generate from running app or build-time springdoc export into e.g.
     `backend-api/openapi/openapi.yaml` (path chosen at implement; keep stable).
   - PR job: regenerate or compare committed file; fail on unexpected breaking
     diff (openapi-diff or oasdiff). Intentional breaks require updating the
     committed artifact in the same PR.

4. **Backup/restore gated on #256**
   - Add workflow `backup-restore-smoke.yml` (name flexible) that:
     - `if:` / concurrency gated on presence of backup script from #256, **or**
     - runs `pg_dump`/`pg_restore` only when that script exists.
   - Until #256: job documents skip reason; do not fake green restore.

5. **#281 relationship**
   - Minimal operator notes live with #1067 CI; full prose guide may remain #281.
   - Do not block #1067 on #281 closure.

## Riesgos / Trade-offs

- [ZAP flaky / slow CI] → Nightly or `workflow_dispatch` + optional PR label;
  keep PR required gate as OpenAPI diff first if ZAP too heavy for every PR.
- [Unauthenticated baseline low coverage] → Document; add auth context in
  follow-up task inside same change if feasible.
- [OpenAPI noise from non-breaking churn] → Fail only on breaking ruleset;
  allow non-breaking updates by refreshing committed file.
- [#256 never lands] → Phase C stays skipped; escalate via coordinator / split
  child issue rather than inventing backups here.
- [Secrets in ZAP reports] → Upload artifacts carefully; scrub auth headers.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| ZAP baseline job exists and runs against stack | CI / security | `.github/workflows/*zap*` + compose |
| OpenAPI artifact committed | static | `backend-api/openapi/...` (or chosen path) |
| PR OpenAPI diff catches break | CI | openapi-diff job + fixture/demo if needed |
| Backup-restore smoke when #256 ready | CI / ops | backup-restore workflow |
| Phase C skip when #256 absent | CI | workflow `if` / explicit skip log |
| Docs updated | docs | DevSecOps + TEST-PLAN |

- New unit tests: optional script tests for OpenAPI export path
- JaCoCo: unchanged expected
- TDD: add failing CI script/job assertions (actionlint / workflow tests /
  `scripts/test_*.py`) before wiring green path where the repo pattern allows

## Regression Strategy

- Existing Trivy jobs must keep running.
- Bruno/Playwright unaffected unless compose startup changes.
- Full suite: `bash scripts/preflight.sh`; ZAP job separately.
- Do not disable security jobs to force green.

## Playwright Strategy

- n/a — no product UI surface.
- PR must still pass required Playwright CI if triggered.

## Deployment Strategy

- Flyway: no
- Order: merge CI/docs first; enable failing ZAP ratchet after baseline noise
  review if starting warn-only.
- `.env.example`: document any ZAP target URL overrides (no secrets).
- Feature flag: n/a
- Smoke: ZAP report artifact present; OpenAPI diff job green; backup-restore
  skip or green per #256

## Rollback Strategy

- Revert safe: yes (remove workflows / committed OpenAPI)
- Database: none from A/B; Phase C restore tests must use disposable DB/volume
- Blast radius: temporary loss of DAST/contract signal

## Migration Plan

1. Phase A ZAP (report + docs)
2. Phase B OpenAPI commit + PR diff
3. Phase C scaffold; enable when #256 merges (rebase/follow-up commit OK)
4. Ratchet ZAP fail policy
5. Close #1067 when A+B+C acceptance met per coordinator

## Open Questions

- Whether ZAP is required on every PR vs nightly: **default** nightly +
  `workflow_dispatch`, with OpenAPI diff on every PR (lighter). Coordinator may
  require ZAP on PR — design allows either without changing specs’ outcomes.
