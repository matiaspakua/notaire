> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1008 exists, labeled `bug, DEVOPS, ci`
- [x] 1.2 No Use Case applies — CI/CD defect, not a business behavior change (precedent #566, recorded in proposal.md)
- [x] 1.3 Acceptance Criteria defined as an Issue checklist (`skip_specs: true` — no delta spec scenarios)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1008 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b ci/1008_fix-bruno-login-field-names`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

<!-- No unit/integration test harness exists for a GitHub Actions YAML `run:`
     step (see traceability.md Exceptions). The equivalent of "write the test,
     watch it fail" is: reproduce the exact failure locally, then reproduce the
     exact fix locally, before touching the workflow file. -->

- [x] 3.1 Reproduce the current failure: ran the exact broken curl command against a running backend — confirmed `token='null'`
- [x] 3.2 Confirm the corrected payload works: ran the same curl with `{"name":"admin","password":"admin"}` — confirmed a real JWT came back
- [x] 3.3 n/a — no unit tests apply (no application code changes)
- [x] 3.4 n/a — no integration tests apply
- [x] 3.5 n/a — no delta spec scenarios (`skip_specs: true`)

## 4. Implementación

- [x] 4.1 Edited `.github/workflows/playwright-e2e.yml` line 169: changed the Bruno login step's `-d` payload from `{"nombre":"admin","contrasenia":"admin"}` to `{"name":"admin","password":"admin"}`
- [x] 4.2 `grep -rn "contrasenia" .github/workflows/` — confirmed clean, no other occurrence anywhere in the workflows directory

## 5. Actualizar tests existentes

- [x] 5.1 No existing tests reference this workflow step (it is a CI script, not application code) — nothing to update
- [x] 5.2 n/a
- [x] 5.3 n/a

## 6. Ejecutar regresión

<!-- backend-api / frontend source is untouched by this change; these commands
     confirm that remains true and nothing else regressed. -->

- [x] 6.1 `mvn test -pl backend-api` — 1890/1890, unaffected
- [x] 6.2/6.3 `mvn verify -pl backend-api -DskipTests` — BUILD SUCCESS, only pre-existing legacy-file Checkstyle warnings (unrelated to this change)
- [x] 6.4 Ran the Bruno suite locally with the corrected login flow: token obtained successfully, 150/150 requests executed with real auth (128 passed / 22 failed — all 22 are `409`/`404` conflicts against this machine's already-seeded local Postgres from unrelated prior sessions, not auth or contract failures; zero requests failed with 401/403). This proves the fix: before it, the step never got past obtaining a token. The authoritative clean-DB run is CI (task 10.3).
- [x] 6.5 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface. This change touches only the Bruno-job login step in `playwright-e2e.yml`; the Playwright job in the same workflow is untouched and already green (509/0/17 on `main` per #1007)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 No permanent document references this workflow step's payload (see proposal.md — Documentation Impact); nothing to update
- [x] 8.2 n/a — no endpoint changed
- [x] 8.3 `CHANGELOG.md`: n/a — not user-visible (internal CI defect)
- [x] 8.4 Nothing to archive
- [x] 8.5 Confirmed: no duplication introduced
- [x] 8.6 `bash scripts/preflight.sh --fix` — PREFLIGHT PASSED, 16 checks (no changes needed beyond the intended one)

## 9. Commits atómicos

- [x] 9.1 Single atomic commit: `ci(playwright-e2e): fix Bruno login payload field names` (1a7c138)
- [x] 9.2 Commit message ends with `Closes #1008`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Recorded the commit SHA in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin ci/1008_fix-bruno-login-field-names`
- [ ] 10.2 Open PR titled `[#1008] ci(playwright-e2e): fix Bruno login payload field names`
- [ ] 10.3 Wait for every required workflow to pass, **specifically confirming the "API Tests (Bruno)" job in `playwright-e2e.yml` goes green** — that is this change's actual proof of fix
- [ ] 10.4 Gate 4 — CI green, no merge conflicts, docs complete (see §12.20 of the Constitution: code owner merging the PR is the review approval in this solo-maintainer repo)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 Confirm `cd.yml` still publishes normally (this change does not touch anything `cd.yml` builds or scans)
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test = the next `main`-branch CI run showing "API Tests (Bruno)" green
- [ ] 12.2 Rollback path confirmed available (plain `git revert`, see design.md)
- [ ] 12.3 Close Issue #1008, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-bruno-login-field-names`

## Definition of Done

- [ ] Issue linked (no Use Case applies; documented why)
- [ ] Specification written and reviewed (Gate 1)
- [ ] Verification designed and run first via live reproduction, observed failing (Gate 2 — substituted per traceability.md Exceptions)
- [ ] Full suite green: unit, integration, regression, and the Bruno API suite specifically now running
- [ ] Coverage at or above the JaCoCo ratchet floor (unaffected — no backend code changed)
- [ ] Playwright E2E: n/a, no UI surface
- [ ] Permanent documentation: n/a, confirmed and recorded
- [ ] Commit atomic and conventional, referencing the Issue
- [ ] PR created, CI green (Bruno job specifically verified) (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
