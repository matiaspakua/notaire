> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1047 (audit-2026-09, CU74 + CU76): weekly k6 Performance workflow always
fails because the script was deleted.

Verified on workspace tip (2026-10-03):

| Location | Finding |
|----------|---------|
| `performance-test/k6/load-test.js` | **missing** (entire `performance-test/` dir gone) |
| Commit `b822a18` | deleted the 60-line k6 script (“Clean up”, 2026-08-11) |
| `.github/workflows/performance-test.yml` | still points at that path; schedule Mon 04:00 UTC + `workflow_dispatch` |
| Recent Actions runs | job fails at **Run k6 load test**; Build/Start backend succeed |
| `scripts/test_performance_test_assets.py` | `FileNotFoundError` in `K6LoadTestScriptTest.setUpClass` |
| Original script (pre-delete) | Spanish login body `{ nombre, contrasenia }`; thresholds `p(95)<500`, `http_req_failed rate<0.01` |
| Current login DTO | English `{ name, password }` (`DtoUser`); response includes `token` |
| CU74 SLO text | objective p95 &lt; 2s; absolute response &lt; 10s under load |
| Artifact upload | uploads `summary.json` with `if-no-files-found: ignore` — old script never wrote it |
| #594 | CLOSED — originally added the suite |

Fleet serialize (coordinator): **#1057** (PR #1150) → **#1048** → then **#1047**.
This Gate 1 draft is prep-only; no product branch/PR until #1048 is on `main`.

## Goals / Non-Goals

**Goals:**

- Restore `performance-test/k6/load-test.js` for the English API.
- Thresholds tied to CU74 SLOs (p95 latency + error rate).
- Weekly / manual Performance workflow can go green.
- Publish a real k6 summary artifact from the job.
- Asset unittest green and asserts English fields + summary output.

**Non-Goals:**

- Per-PR load-test gating.
- Rewriting Performance workflow onto full Docker Compose / observability stack.
- Changing caching or JPA performance (other CU74 work).
- Closing unrelated open docs issue #303 as the primary deliverable.
- Implementing before #1048 merges.

## Decisions

1. **Rewrite (not blind restore) for English login DTO**
   - Why: Current API expects `name`/`password`; Spanish fields would soft-fail
     login and blow error-rate / checks even if the file existed.
   - Alternative rejected: restore `b822a18^` byte-for-byte — incompatible with
     post-English DTO.

2. **SLO thresholds: p95 &lt; 2000ms and error rate &lt; 1%**
   - Why: CU74 documents p95 &lt; 2s as the objective; #1047 AC requires thresholds
     tied to SLOs. Keep `http_req_failed: ['rate<0.01']` from #594.
   - Alternative rejected: keep only `p(95)<500` as the sole bar — tighter than
     CU74 objective and may flake on cold CI runners; may still use ≤500 as an
     optional aspirational check only if proven stable at implement time.
   - Normative for Gate 1: **MUST** enforce p95 ≤ 2000ms; MAY keep a secondary
     tighter threshold if CI evidence supports it.

3. **Minimal stages profile from #594 (≈2 min)**
   - Why: Workflow `timeout-minutes: 20`; original 30s/1m/30s @ 10 VUs is enough
     for weekly smoke load without redesign.
   - Alternative rejected: large soak — out of scope for restore.

4. **Write `summary.json` via `handleSummary`**
   - Why: AC requires published results; upload step currently ignores missing
     files. Script MUST emit the artifact path the workflow uploads.
   - Alternative rejected: change upload to a different path without script
     output — still empty.

5. **Keep workflow topology; only touch YAML if needed**
   - Why: Build + Start backend already succeed; primary defect is missing
     script. Prefer not to expand into Flyway-bootstrap rewrite in this issue.
   - Risk note: workflow still passes `--spring.jpa.hibernate.ddl-auto=update`
     while Flyway is repo SSOT — document as residual risk / optional follow-up,
     not AC-blocking if health check already passes.

6. **TDD via asset/workflow asserts first**
   - Extend `scripts/test_performance_test_assets.py` (failing while file
     missing / while Spanish fields absent) before adding the script.
   - Prove red with `python3 scripts/test_performance_test_assets.py`, then green.

7. **Serialize after #1048**
   - Why: coordinator queue and runner contention preference; #1047 is CI/test
     asset work that should not race another frontend/CI PR.

## Riesgos / Trade-offs

- **[Risk] Login/admin seed mismatch in CI** → Workflow already sets
  `--app.admin.username/password` matching k6 `ADMIN_*` env; verify
  `DataInitializer` creates user before k6; fail fast in `setup()` checks.
- **[Risk] p95 flaky on cold GitHub-hosted runners** → Use CU74 2s objective as
  hard gate; keep stages modest; if flakes, tune stages before loosening SLO.
- **[Risk] Empty artifact despite green k6** → Require `handleSummary` + tighten
  upload `if-no-files-found` to `warn`/`error` if compatible with action.
- **[Risk] `ddl-auto=update` vs Flyway SSOT drift** → Out of AC scope; open
  follow-up if schema bootstrap becomes fragile; do not block restore.
- **[Risk] Endpoint empty-list 200 vs auth 401** → Ensure Bearer token path;
  English login body is mandatory.
- **[Trade-off] Weekly-only signal** → Acceptable; #594/#1047 explicitly avoid
  per-PR load gates.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| k6 script exists at workflow path | unit | `scripts/test_performance_test_assets.py` |
| English login DTO fields | unit | same — assert `name`/`password`, reject Spanish-only body |
| Stages + SLO thresholds | unit | same — `stages`, `http_req_duration` p95≤2000, `http_req_failed` |
| Endpoint coverage + Bearer | unit | same — gestiones/presupuestos/tramites + Authorization |
| Summary artifact output | unit | same — `handleSummary` / `summary.json` |
| Workflow schedule + live stack wiring | unit | existing `PerformanceTestWorkflowTest` (+ artifact path) |
| Asset suite green | command | `python3 scripts/test_performance_test_assets.py` |
| Workflow green + artifact | CI smoke | `workflow_dispatch` after merge (Gate 5) |

- New unit tests: extend the existing stdlib unittest (no new framework).
- New integration tests: n/a for product code; optional local `k6 run` if k6
  binary available in implement env (not required for Gate 2 asset asserts).
- Coverage impact (JaCoCo): n/a — no backend production code change.

## Regression Strategy

- Existing tests affected: `scripts/test_performance_test_assets.py` (currently
  red due to missing file) — update expectations for English fields + summary.
- Full suite command: `python3 scripts/test_performance_test_assets.py`;
  `bash scripts/preflight.sh` as applicable; PR heavy gate
  `bash scripts/check-heavy-ci.sh <pr>` (workflow-only change should still pass
  Integration/Coverage/Bruno/Playwright).
- HTTP/Bruno API suite: n/a for product delta; login contract already covered
  elsewhere (`api-test/00-auth`).
- Legacy paths: none; do not resurrect Spanish login fields.

## Playwright Strategy

- No UI change. No new Playwright scenarios.
- PR still must pass repository heavy CI (includes Playwright) before merge.
- Mark product E2E n/a for this capability; do not skip the PR Playwright job.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: add script (+ minor workflow/test/docs); no
  runtime deploy dependency beyond merging to `main` for the scheduled workflow
- Configuration or `.env` keys: none new
- Feature flag: no
- Smoke test after deploy (Gate 5): `workflow_dispatch` on
  `performance-test.yml`; confirm k6 step green and `k6-load-test-results`
  artifact contains `summary.json`

## Rollback Strategy

- Revert safe: yes — revert the PR restores “missing script” failure mode
  (known bad); prefer fix-forward on script/thresholds
- Database rollback: none needed
- Data written under the new behavior after revert: none lasting (CI ephemeral DB)
- Blast radius if rollback delayed: low for product users; medium for ops
  signal quality if thresholds are wrong (noisy weekly failures)

## Migration Plan

1. Wait for **#1048** merge to `main` (after #1057).
2. Copy this draft into `openspec/changes/fix-1047-k6-load-test/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh fix-1047-k6-load-test`.
4. Branch `cursor/fix-1047-k6-load-test-69d3` → failing asset tests → restore
   script → green tests → docs → PR `Closes #1047`.
5. After merge: dispatch Performance workflow; confirm artifact.

## Open Questions

- Whether CI cold-start needs a slightly longer ramp before enforcing p95≤2000
  — measure on first `workflow_dispatch`.
- Whether to set upload-artifact `if-no-files-found: error` once summary is
  guaranteed — prefer yes at implement if action supports it without breaking
  `if: always()` failure paths.
