---
title: Notaire — Whole-Project Explore Report
date: 2026-08-11
mode: opsx:explore (thinking artifact, not a spec)
---

# Notaire — Whole-Project Explore Report

> Captures a point-in-time read of the repository on 2026-08-11. This is exploration
> output, not a Specification — it records what was found and questions worth picking
> up, not commitments. Nothing here should be copied verbatim into a proposal; cite it
> instead.

## 1. Shape of the system

```
[Java Swing Monolith]  (src.old/ — frozen, 8.1M, still in git history)
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│  backend-api  (Spring Boot 4.1, Java 21, PostgreSQL 16)    │
│                                                             │
│   api/        31 REST controllers                          │
│   service/    15 thin services                              │
│   repository/ 31 Spring Data repos     ← target pattern     │
│   jpa/        27 legacy *JpaController classes ← being      │
│                superseded, still load-bearing                │
│   negocio/    40 @Entity classes                            │
└───────────────────────────────────────────────────────────┘
        │                                    │
        ▼                                    ▼
[frontend]  Next.js 16 / React 19       [frontend-swing]
30 pages, ES/EN i18n                    REST client shell only
(the active client)                     (transitional)
```

Modernization is a **Swing → Spring Boot + Next.js** rewrite-in-place, governed by
`CONSTITUTION.md` (496 lines, added 2026-08-08) plus an OpenSpec schema
(`notaire-sdlc`) that encodes it for every agent. `openspec/changes/` is currently
empty (only an `archive/` folder) and `openspec/specs/` has no capability specs yet —
**the Constitution/OpenSpec machinery is freshly installed and effectively unused so
far.** Every prior change in this repo's history predates it.

## 2. The gap between "green CI" and actual health

The latest CI/CD reports (auto-committed today) read as a clean bill of health:

```
Build & Compile ....... success       Tests Executed: 791   Failures: 0
Unit Tests ............ success       Coverage: 84% (target 80%)
Integration Tests ..... success       Playwright: 411/450 passed, 37 skipped
Code Coverage .......... success
Security Scan .......... success  ←  but: 4 Critical + 45 High CVEs reported
Docker Build ........... success
Code Quality ............ success  ←  Checkstyle/SpotBugs run, don't block
```

That "Security Scan: success" with 4 Critical/45 High findings is not a contradiction
in the data — it's the mechanism. Three open **priority:critical** issues describe it
directly:

- **#566** — *no quality/security gate can ever fail the build* (tests, Checkstyle,
  SpotBugs, Trivy are all advisory-only)
- **#680** — Trivy is advisory-only, never fails the build
- **#710 / #711** — Checkstyle (3,066 pre-existing warnings) and SpotBugs (766
  pre-existing findings) are both *implemented but not enforced*, because turning
  enforcement on today would immediately break the build on pre-existing debt

So the seven green checkmarks in the CI report are true and simultaneously not the
same claim as "this is safe to ship." That distinction — implemented-but-not-gating —
recurs across the codebase (see §4). It's worth deciding, as a project, whether the
CI report's presentation should change to make that visible, since right now it reads
as unqualified success.

## 3. The backlog iceberg

```
279 open issues total
├── 45 documentation / 43 DOC / 12 DOCUMENTACION  → ~36% of all issues are docs debt
├── 28 BACKEND, 10 FRONTEND, 26 DEVOPS
├── 38 priority:medium, 27 priority:high, 11 priority:critical, 10 priority:low
├── 9 CASO-DE-USO, 8 TEST, 6 DB, 5 requerimiento-no-funcional
└── only 4 labeled in-progress
```

Only 4 of 279 open issues are actively `in-progress`. That's either a labeling gap
(work happening without the label) or a genuinely small active WIP set against a very
large backlog — worth checking directly with the user rather than assuming either.

The 11 `priority:critical` issues cluster into three themes:

| Theme | Issues |
|---|---|
| Gates that don't gate | #566 (nothing can fail the build) |
| Architecture bypassing the target pattern | #575 (raw-JDBC `Conexion` singleton bypasses HikariCP/Flyway for reports), #574 (controllers instantiate legacy `JpaController`s directly via a static provider, bypassing Spring DI/transactions) |
| Authz/traceability holes | #559 (no RBAC enforcement anywhere in the backend), #598 (CU-API-MATRIX.csv has zero rows for CU69–78 — the entire Workflow feature is untraced) |
| Ops runbook docs (5 issues, #264–271) | disaster recovery, incident response, OWASP checklist, env config guide, DB maintenance — all still just "create the doc" |

## 4. Five risk clusters worth a closer look

### 4.1 Security posture
- **#676**: JWT tokens have no refresh mechanism, no revocation, no expiry
  enforcement.
- **#559**: no RBAC enforcement anywhere in the backend (critical).
- **#655** (in-progress): Bean Validation rollout to remaining controllers is
  incomplete.
- **#684**: observability stack has no TLS between services.
- Trivy scan is advisory (see §2) — 4 Critical/45 High CVEs currently unaddressed
  in the dependency tree, not blocking anything.

### 4.2 Architecture drift from the stated target
`CLAUDE.md` and the Constitution are explicit: *"New data access goes in `repository`
(Spring Data), not the legacy `jpa` package."* Reality:
- 27 legacy `*JpaController` classes still exist and are directly instantiated by
  REST controllers via a static provider (#574, critical) — not routed through Spring
  DI or `@Transactional`.
- Report generation uses a raw-JDBC `Conexion` singleton that bypasses the
  HikariCP pool and Flyway-managed schema entirely (#575, critical).
- This means the "repository over jpa" migration is a stated direction, not a
  completed one — the two data-access paths coexist and the legacy one still does
  real work outside the framework's transaction/connection boundaries.

### 4.3 Performance under the REST surface
- **#596**: 26 of 31 REST controllers return unbounded `findAll()` with no
  pagination.
- **#595**: 27 `FetchType.EAGER` associations across 16 entities — Cartesian-product
  / N+1 risk.
- **#597**: no caching layer on read-heavy catalog endpoints.
These three compound: unbounded queries × eager fan-out × no cache is a plausible
path to a slow-query incident once data volume grows past dev-fixture size.

### 4.4 Traceability gap between docs and reality
The functional baseline (`docs/01-business/00-FUNCTIONAL-BASELINE.md`, dated
2026-05-29) already does the honest work of reconciling the use-case catalog against
measured reality, and it's blunt about it: the catalog marks all 68 CUs "Terminado,"
but the baseline finds several **Gestiones** flows (document/testimonio lifecycle —
CU03, CU04, CU07, CU08, CU11, CU12) still `❌ pending` on the UI side, with backend
API present. Two open issues extend this:
- **#598** (critical): `CU-API-MATRIX.csv` has zero rows for CU69–78 — the entire
  Workflow feature is outside the traceability matrix.
- **#599** (high): 64 open issues self-report "Terminado/100%" while contradicted by
  their own test-matrix row.

This is a documentation-integrity problem more than a documentation-completeness
one — the docs exist and are detailed, but some of them assert a status the evidence
doesn't support. Given how heavily this project's own workflow leans on traceability
(Constitution P4, Gate 1–5), that's a structural risk to the process itself, not just
a docs nit.

### 4.5 Repo hygiene
- **#682**: binary files (`.jasper`, `.class`, `src.old/`) committed to `main` —
  `src.old/` alone is 8.1M and the whole `.git` directory is 109M.
- **#679**: the CI workflow is a 735-line monolith, flagged for splitting into
  reusable jobs.

## 5. What "done" looks like here, and how far the machinery is from being tested

The Constitution's SDLC (Issue → Use Case → Gate 1 Spec → TDD → Gate 3 full suite →
PR → Gate 5 smoke test) is thorough and unambiguous on paper — five hard-stop gates,
explicit non-negotiables encoded into `openspec/config.yaml` so every agent (Claude
Code, OpenCode, Copilot) sees the same rules through the CLI rather than a
per-tool prompt. But it landed six days ago (2026-08-08, commit `8c339d1`) and
`openspec/changes/` is empty — no proposal has gone through Gate 1–5 under this
process yet. Worth treating the first real change through it as a dry run for the
process itself, not just for the feature.

## 6. Open threads

These are genuine forks, not a checklist — pick what resonates:

1. **Enforcement sequencing.** #566/#710/#711/#680 all describe the same shape:
   implemented-but-not-gating. Is the plan to burn down the pre-existing findings
   first and flip enforcement on, or to grandfather existing code and enforce only
   on diffs going forward? Those are very different amounts of work and risk
   differently (a diff-only gate can still let a critical CVE sit unaddressed
   indefinitely if it's in an already-vulnerable transitive dependency).
2. **`jpa` retirement.** #574/#575 are marked critical but there's no visible issue
   for "finish migrating `jpa` → `repository`" as a tracked body of work — only the
   two symptom issues. Is there an appetite to scope that as a real migration effort,
   or is `jpa` staying as permanent legacy glue?
3. **Traceability trust.** #598/#599 suggest the CU-API-MATRIX and issue
   self-reporting have drifted from reality in ways that could mislead exactly the
   people the traceability requirement (Constitution P4) exists to protect. Worth
   asking whether a periodic reconciliation pass (like the 2026-05-29 baseline) should
   be a recurring, scheduled thing rather than a one-off.
4. **WIP visibility.** Only 4/279 open issues are `in-progress`. Is that accurate?
   If yes, this is a huge backlog relative to active capacity, which changes how
   prioritization conversations should go.
5. **`src.old`.** It's dead weight in every clone and every `git clone` going
   forward. #682 already tracks it — is there a reason it's still there (reference
   material during the migration) or is it safe to purge from history now?

---
*Generated via `/opsx:explore`. Grounded in repo state as read on 2026-08-11 —
re-verify specifics (issue numbers, counts, file paths) before acting on them, since
this snapshot ages the moment new commits land.*
