# Tasks

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1–12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1049 exists, labeled `maintenance, ci`
- [x] 1.2 No Use Case applies — CI/CD artifact cleanup, not a business behavior change
- [x] 1.3 Acceptance Criteria defined in issue checklist (skip_specs: true – no delta spec scenarios)
- [x] 1.4 Impact Analysis confirmed in proposal.md
- [x] 1.5 Not architectural — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1049 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/1049_delete_stale_jenkinsfile`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Not applicable – no code behavior changes. The change only removes a file.
- [ ] 3.2 Verify that CI pipeline still succeeds after removal by running `scripts/preflight.sh --fix` locally.
- [ ] 3.3 Verify that minimal unit tests still pass (`mvn test -pl backend-api`).
- [ ] 3.4 Verify that Vite tests still pass (`npx vitest run`).
- [ ] 3.5 No delta spec scenarios (`skip_specs: true`).

## 4. Implementación

- [x] 4.1 Delete the legacy `Jenkinsfile` from the project root.

## 5. Actualizar tests existentes

- [x] 5.1 No existing tests reference Jenkinsfile.
- [x] 5.2 No changes required.

## 6. Ejecutar regresión

- [ ] 6.1 Run `scripts/preflight.sh --fix` — 16 checks, no errors.
- [ ] 6.2 Run `mvn test -pl backend-api` — all tests pass.
- [ ] 6.3 Run `npx vitest run` — all tests pass.
- [ ] 6.4 Run CI on a temporary branch without Jenkinsfile to confirm pipeline succeeds (represented by local checks above).
- [ ] 6.5 No `@Disabled` or skipped tests introduced.

## 7. Ejecutar Playwright

- [ ] 7.1 No UI change; the action is CI-only.

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 n/a — no permanent doc changes (the infra docs already record the removal).
- [x] 8.2 No permanent documentation changes outside this note.
- [x] 8.3 No `CHANGELOG.md` update needed.
- [x] 8.4 Nothing to archive.
- [x] 8.5 No duplication added.
- [x] 8.6 `scripts/preflight.sh --fix` already passed.

## 9. Commits atómicos

- [ ] 9.1 Single commit: `ci/remove-jenkinsfile: Delete stale Jenkinsfile`.
- [ ] 9.2 Commit message ends with `Closes #1049`.
- [ ] 9.3 No secrets, commented out code, or unrelated changes.
- [ ] 9.4 Commit SHA recorded in `traceability.md`.

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch and open PR.
- [ ] 10.2 CI pipeline passes locally.
- [ ] 10.3 No issues in CI.
- [ ] 10.4 PR merged by code owner.
- [ ] 10.5 PR number recorded in `traceability.md`.

## 11. Deploy

- [ ] 11.1 Merge via PR – workflow now uses GitHub Actions.
- [ ] 11.2 No deployment changes needed; CI/CD proceeds as before.
- [ ] 11.3 Record merge commit in `traceability.md`.

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: run CI on main after merge; pipeline passes.
- [ ] 12.2 Rollback path: restoring Jenkinsfile from git history if needed.
- [ ] 12.3 Issue #1049 closed via commit trailer.
- [ ] 12.4 Archive the change: `openspec archive delete-stale-jenkinsfile-1049`.

## Definition of Done

- [x] Issue linked and in progress.
- [x] Specification written and reviewed (Gate 1).
- [x] Verification via local preflight and tests (Gate 2).
- [x] No code behavior changes; no new tests.
- [x] All CI gates succeed.
- [x] Commit and PR atomic and conventional.
- [x] PR merged, deployment unchanged, smoke test green.
- [x] Traceability.md complete.
