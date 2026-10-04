> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #559 exists, labeled, linked to CU42
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless the design says otherwise
- [ ] 1.6 Move the Issue to IN PROGRESS (comment with the branch link)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b fix/559_rbac_enforcement`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh fix-559-rbac-enforcement`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: authority per user type, 403 per family and method, public login, inactive user, catalog reads
- [ ] 3.2 Add the failing unit, integration and hook tests; observed failing
- [ ] 3.3 Every scenario maps to a test

## 4. Implementación

- [ ] 4.1 Add `UserAuthorityResolver` and use it in the JWT filter
- [ ] 4.2 Add the administrator rules and the access-denied handler to the API filter chain
- [ ] 4.3 Add `/dashboard/auditoria` to the frontend admin routes
- [ ] 4.4 Tests green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or references remain

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api -Dtest=RbacIntegrationTest,UserAuthorityResolverTest,JwtAuthenticationFilterTest`
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/preflight.sh` (or the subset the environment allows)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 Extend TS-0094 with the audit route and an API 403 case

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `docs/200-architecture/202-ADR/ADR-008-security-authentication.md` — authorization section
- [ ] 8.2 `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` — roles and 403
- [ ] 8.3 `CHANGELOG.md` — one entry

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #559`; others `Refs #559`
- [ ] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/559_rbac_enforcement`
- [ ] 10.2 Open PR `[#559] fix(security): enforce role-based authorization on administrative endpoints (CU78)`
- [ ] 10.3 Wait for all required workflows
- [ ] 10.4 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI green on the merge commit; an EMPLEADO token gets 403 on the user list
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #559 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-559-rbac-enforcement`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #559` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
