> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1044 OPEN; CU78 + CU75
- [x] 1.2 Use Case documentation exists and is accurate — CU78/CU75 exist; update AC/docs at implement for prod compose
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (compose/deployment hardening; cite ADR-019; optional ADR only if implement chooses a novel secrets pattern)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1044 --add-label "in-progress"`) — attempted at implement; label ACL may 403

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1047 merges** (queue: #1057 → #1048 → #1047 → #1044)
- [x] 2.2 `git checkout -b cursor/feat-1044-prod-compose-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1044/` into `openspec/changes/feat-1044-prod-compose/` and run `bash scripts/validate-sdlc-plan.sh feat-1044-prod-compose`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: prod file exists; no pgAdmin; no host ports on postgres/backend/frontend; reverse-proxy-only ports; `ENVIRONMENT=production`; `${VAR:?}` secrets; least-privilege backend env; Flyway baseline off; guard unused-credential policy; docs checklist
- [x] 3.2 Add failing `scripts/test_prod_compose.py` (or equivalent) asserting those compose invariants while `docker-compose.prod.yml` is missing / non-compliant
- [x] 3.3 Extend `ProductionCredentialsGuardTest` with failing expectations for production without Grafana/pgAdmin/exporter env (must not require those defaults)
- [x] 3.4 Run them and **observe them fail** — `python3 scripts/test_prod_compose.py` and `mvn test -pl backend-api -Dtest=ProductionCredentialsGuardTest`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test or explicit docs checklist item

## 4. Implementación

- [x] 4.1 Create `docker-compose.prod.yml` with postgres + backend + frontend + reverse-proxy; **no pgAdmin**
- [x] 4.2 Omit host `ports` for postgres/backend/frontend; publish only reverse-proxy host ports
- [x] 4.3 Set backend `ENVIRONMENT=production`; wire secrets with `${VAR:?...}` (no `:-admin` defaults)
- [x] 4.4 Apply least-privilege env maps (backend without Grafana/pgAdmin/exporter secrets)
- [x] 4.5 Set `SPRING_FLYWAY_BASELINE_ON_MIGRATE` to `false` (or omit) — never `true` in prod
- [x] 4.6 Align `ProductionCredentialsGuard` with least-privilege; keep rejecting `admin` for in-scope secrets
- [x] 4.7 Add minimal reverse-proxy config (nginx or Caddy) routing frontend + API; pin image tag
- [x] 4.8 Make static compose tests + guard unit tests pass without weakening AC asserts
- [x] 4.9 Leave root `docker-compose.yml` as the documented **dev** stack

## 5. Actualizar tests existentes

- [x] 5.1 Identify affected tests (`ProductionCredentialsGuardTest`; any compose/docs scripts)
- [x] 5.2 Update assertions for unused-credential policy without weakening default-password rejection
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected for the guard’s core defaults check

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — especially `ProductionCredentialsGuardTest` + related config tests
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — hold ratchet if guard code changed
- [ ] 6.3 `mvn verify -pl backend-api` — as needed for backend delta
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API contract change); Bruno covered by heavy CI
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E required (no UI delta)
- [ ] 7.2 PR must pass full `playwright-e2e.yml` via heavy CI gate
- [x] 7.3 Viewport checks n/a
- [x] 7.4 Mark product Playwright scenarios n/a for this change; do not skip the PR Playwright job

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for production compose (#1044)
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh` (or applicable subset) — keep local/CI mapping honest if compose docs/scripts mentioned

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #1044`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/feat-1044-prod-compose-69d3`
- [x] 10.2 Open the PR titled `[#1044] feat(devops): production docker-compose without exposed DB`, referencing Issue, CU78, CU75
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR (if app image changed; compose/docs-only may be n/a)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: with non-`admin` secrets, `docker compose -f docker-compose.prod.yml config` succeeds; stack healthy; HTTP via reverse proxy reaches app/API health; postgres/backend/frontend ports not published on host; no pgAdmin container
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-1044-prod-compose`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU78 + CU75)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor (if backend guard delta)
- [ ] Playwright E2E green for UI changes (n/a product UI; heavy gate still required)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
