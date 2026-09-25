# Production-Readiness Audit — 2026-09

> Performed 2026-09-24/25 on `main` @ `5a8fa14`. Every finding is a GitHub issue
> labelled [`audit-2026-09`](https://github.com/matiaspakua/notaire/issues?q=label%3Aaudit-2026-09).
> This page is the audit's index. Status lives on the issues and the
> [Delivery Board](https://github.com/users/matiaspakua/projects/1); do not duplicate it here.

## Scope

Frontend (Next.js), backend (Spring Boot), REST API and Bruno suite, database and Flyway,
tests (unit, integration, E2E, performance), CI/CD and infrastructure, documentation,
OpenSpec and the Constitution workflow.

Before filing anything we searched for duplicates. Existing issues that were still valid got audit
evidence as a comment and the `audit-2026-09` label instead of a new issue. Stale
issues were closed: #695, #582, #572, #294 (obsolete Swing), #780, #807, #829, #272
(verified fixed), #966 (duplicate of #976) and #253 (duplicate of #901).

## Key themes

1. **Access control is enforced by convention only.** `main` has no branch protection
   (#1040), the backend has no RBAC (#559), admin screens are only hidden in the UI (#1052),
   and any user can write audit records (#1060) or set any entity field (#1068).
2. **No production deployment artifact.** The only compose file is a dev stack (#1044), there is
   no frontend image and no versioned releases (#1043), and CD may ship an untested SHA (#1042).
3. **Money uses binary floating point** (#1061).
4. **Weak quality gates.** Load tests have been silently broken (#1047), lint is non-blocking
   (#1048), coverage floors sit far below actual coverage (#1063, #976), and some tests
   accept HTTP 500 (#1062).
5. **Documentation drift.** An obsolete rules file (#1070), an outdated CU↔API matrix (#1064)
   and a legacy Swing tree that still causes critical CVE alerts (#585, #1046).

## Findings

| Issue | Priority | Area | Use Case | Title | State (at audit) |
|-------|----------|------|----------|-------|------------------|
| #559 | critical | Backend | CU78 | security(authz): no role/permission (RBAC) enforcement anywhere in the backend | open |
| #1040 | critical | DevOps | CU76, CU78 | ci(security): protect main with a ruleset — required checks, PR-only, no direct pushes | open |
| #579 | high | Backend | CU76 | arch(api): GlobalExceptionHandler is bypassed by ad-hoc try/catch in 26 of 30 controllers | open |
| #1041 | high | DevOps | CU76 | ci: stop CI bots committing reports to main (25% of history is bot commits) | open |
| #1042 | high | DevOps | CU76 | ci(cd): CD builds the tip of main instead of the SHA CI tested | open |
| #1043 | high | Frontend, DevOps | CU76 | ci(release): publish the frontend image and introduce versioned releases (semver tags) | open |
| #1044 | high | DevOps | CU78, CU75 | devops(security): add a production docker-compose — no pgAdmin, no exposed DB, required secrets | open |
| #1046 | high | DevOps | CU78 | security(deps): fix open Dependabot alerts (smol-toml high; log4j criticals from dead Swing code) | open |
| #1047 | high | DevOps, Test | CU74, CU76 | test(perf): weekly k6 load test always fails — script deleted in b822a18 | open |
| #1051 | high | Frontend | CU78, CU84 | security(frontend): JWT in localStorage + CSP allows unsafe-inline/unsafe-eval scripts | open |
| #1052 | high | Frontend | CU78, CU20, CU21 | security(frontend): admin screens reachable by any logged-in user (client-side hiding only) | open |
| #1053 | high | Frontend | CU84 | fix(frontend): expired sessions are never handled — no global 401 handling | open |
| #1054 | high | Frontend | CU15, CU20, CU21, CU26, CU30 | fix(frontend): backend error messages swallowed on 15 pages (generic toasts) | open |
| #1060 | high | Backend | CU23, CU78 | security(audit): any authenticated user can forge audit-log records via POST /audit-log | open |
| #1061 | high | Backend, DB | CU15 | fix(payments): money stored as float — use BigDecimal for Payment.amount | open |
| #1068 | high | Backend | CU78, CU01, CU08, CU22 | security(api): 13 controllers bind JPA entities as @RequestBody (mass assignment, no validation) | open |
| #585 | high | DevOps | CU76 | cleanup: 8.4MB legacy src.old/ source tree committed to main, not gitignored | open |
| #293 | medium | Frontend, Docs | CU77 | docs(frontend): Create REST client error handling and retry strategy | open |
| #953 | medium | Test | CU76 | Add Bruno API test coverage for controllers with zero coverage | open |
| #976 | medium | Frontend, DevOps, Test | CU76 | fix: Frontend CI Vitest branch coverage below 6% threshold on main | open |
| #1045 | medium | DevOps | CU78 | devops: pin container images and enable Dependabot for npm and docker | open |
| #1048 | medium | Frontend | CU76 | ci(frontend): make ESLint blocking — #701 is closed but lint is still continue-on-error | open |
| #1050 | medium | DevOps | CU76 | chore(repo): hygiene — .gitignore *.txt, .serena/, CODEOWNERS, 13 MB PDF, 292 MB history | open |
| #1055 | medium | Frontend, DevOps | CU78 | fix(frontend): backend URL baked in at build time and shown on the public login page | open |
| #1056 | medium | Frontend | CU84 | chore(frontend): migrate deprecated middleware.ts to proxy.ts (Next 16) | open |
| #1057 | medium | Frontend | RNF accessibility (cross-cutting) | a11y(frontend): 17 icon-only buttons have no accessible name | open |
| #1058 | medium | Frontend | CU22, CU59, CU24, CU25, CU50, CU23 | fix(frontend): Suplencias and Reportes unreachable from navigation; duplicate admin pages | open |
| #1062 | medium | Backend, Test | CU76, CU24, CU25 | test: tests that accept HTTP 500 as success; reportes returns 5xx for missing ids | open |
| #1063 | medium | Backend, Test | CU76 | test: raise JaCoCo branch floor from 25% to 65% (actual 74%) | open |
| #1064 | medium | Test, Docs | CU76 | docs(test): CU-API-MATRIX.csv out of date after the English rename | open |
| #1066 | medium | Frontend, Test | CU76 | test(e2e): tests skip instead of arranging data; fixed sleeps and heavy retries hide flakiness | open |
| #1067 | medium | Test | CU76, CU78, CU75 | test: add DAST, API contract and backup/restore tests | open |
| #1069 | medium | Backend | CU78 | chore(security): remove dead default credentials from backend resources | open |
| #1070 | medium | Docs | CU76 | docs(rules): .claude/rules/refactoring.md describes an obsolete target (Swing client, Boot 3, com.notaria) | open |
| #1071 | medium | Docs | CU76 | docs: publish the 2026-09 production-readiness audit and GitHub organization guide | open |
| #256 | low | DB, DevOps | CU75 | Implement automated PostgreSQL backups | open |
| #1049 | low | DevOps | CU76 | chore(ci): delete stale Jenkinsfile that contradicts the real quality gates | open |
| #1059 | low | Frontend | CU76 | i18n(frontend): 9 pages and the login page bypass translations | open |
| #1065 | low | Backend | CU76 | api: REST convention inconsistencies and an unused /pagos/params endpoint | open |

## GitHub organization changes made during the audit

See [README.md](README.md) for the resulting conventions.

- Duplicate labels merged: `DOCUMENTACION`, `documentation`, `docs` → `DOC`;
  `MEJORAS` → `enhancement`; `req:*` → `requerimiento-*`; unused `area:*` labels deleted.
- New labels: `security`, `a11y`, `i18n`, `audit-2026-09`.
- Every open work issue has a `priority:*` label and a release milestone. Phase milestones
  are closed.
- Stale `in-progress` labels were removed from issues with no branch or PR (#921, #771, #727, #655, #615).
- One board: *Notaire — Delivery Board* (project 1). *Kanban desarrollo*, *Casos de Uso* and
  *Notaire Dashboard* were closed.
