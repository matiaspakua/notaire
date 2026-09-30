> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) – §5 Official SDLC Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites
- [ ] 1.1 GitHub Issue #1049 exists, labeled, and linked to Use Case CU76 – Quality Assurance and Testing Infrastructure
- [ ] 1.2 Use Case documentation exists at `docs/100-business/203-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md`
- [ ] 1.3 Acceptance Criteria defined in the issue and proposal
- [ ] 1.4 Impact analysis confirmed
- [ ] 1.5 No architectural changes
- [ ] 1.6 Move the Issue to IN PROGRESS

## 2. Crear branch
- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b chore/1049_delete_stale_jenkinsfile`
- [ ] 2.3 Record branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD)
- [ ] 3.1 No new tests required; this change only removes a legacy file
- [ ] 3.2 Run existing test suite to confirm no failures

## 4. Implementación
- [ ] 4.1 Delete stale Jenkinsfile(s) from the repository
- [ ] 4.2 Ensure CI configuration references updated pipeline configuration

## 5. Actualizar tests existentes
- [ ] 5.1 Run the preflight script and local tests to confirm no regressions

## 6. Ejecutar regresión
- [ ] 6.1 `mvn -q -B test -pl backend-api` – action & integration tests
- [ ] 6.2 `mvn -q -B verify -pl backend-api` – quality gates

## 7. Ejecutar Playwright
- [ ] 7.1 Run `frontend/tests/e2e` suite to confirm UI works

## 8. Gate 3 — Actualizar documentación permanente
- [ ] 8.1 Update any pipeline documentation to remove legacy references
- [ ] 8.2 `bash scripts/preflight.sh --fix` – lint and formatting

## 9. Commits atómicos
- [ ] 9.1 Atomic commit with conventional message
- [ ] 9.2 Record commit SHA in `traceability.md`

## 10. Pull Request y validación CI
- [ ] 10.1 Push branch to origin
- [ ] 10.2 Open pull request titled `[#1049] chore: delete stale Jenkinsfile`
- [ ] 10.3 Await CI pipeline green

## 11. Deploy
- [ ] 11.1 Merge PR (maintain workflow integrity)
- [ ] 11.2 Trigger CD pipeline

## 12. Gate 5 — Smoke test y cierre
- [ ] 12.1 Verify build and deployment succeed without the old Jenkinsfile
- [ ] 12.2 Close GitHub Issue #1049

## Definition of Done
- [ ] All 12 mandatory groups are present
- [ ] The change passes the full preflight and CI pipeline
