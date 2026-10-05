> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1255 exists, labeled, linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios (or `skip_specs` justified)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR-024 planned in `proposal.md` (Documentation Impact)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b refactor/1255_deprecate_notaire_shared`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1255-deprecate-notaire-shared`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: class ownership, one-module reactor, no dependency, Docker files, tooling references, archived folder and manifest, dead code, docs
- [x] 3.2 Add `DtoOwnershipTest` and `scripts/test_notaire_shared_retired.py` (+ wrapper); update `test_repo_hygiene.py`; observed failing
- [x] 3.3 Every scenario maps to a test
- [x] 3.4 Baseline for the contract diff is the committed `backend-api/openapi/openapi.yaml` (no regeneration may change it)

## 4. Implementación

- [ ] 4.1 `git mv` every class of the module into `backend-api`
- [ ] 4.2 Remove the dependency from `backend-api/pom.xml` and the module from the root `pom.xml`
- [ ] 4.3 Delete `SharedModuleMetrics`, the `notaire_shared_version` gauge and the `ObservabilityTest` block
- [ ] 4.4 Update Dockerfile, `.dockerignore`, CODEOWNERS, `release-please-config.json`, `.cursor/install.sh`, `.aisdlc/project.yml`, `check-tdd-evidence.sh`, `check-agent-rules.sh`, `generate-coverage-report.sh`, `ci.yml` comment
- [ ] 4.5 Move the remaining folder to `deprecated/notaire-shared/` (`pom.xml.archived`, README pointing to the API)
- [ ] 4.6 Spotless-format the moved sources in their own commit
- [ ] 4.7 Tests green

## 5. Actualizar tests existentes

- [ ] 5.1 Existing affected tests updated without weakening assertions
- [ ] 5.2 No dead code or references remain (`git grep notaire-shared` outside archive and history)

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api -Dtest=DtoOwnershipTest,ObservabilityTest` and `python3 -m unittest discover -s scripts/tests`
- [ ] 6.2 Coverage gate — `mvn verify -pl backend-api` keeps the ratchet floor
- [ ] 6.3 `bash scripts/export-openapi.sh --maven` leaves `backend-api/openapi/openapi.yaml` unchanged
- [ ] 6.4 `bash scripts/preflight.sh --full` (Docker build, Bruno, Playwright)
- [ ] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 No UI change: run the existing suite as regression evidence (`bash scripts/preflight.sh --full`)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 ADR-024 (new) and index; status note in ADR-002; Docker note in ADR-017
- [ ] 8.2 SAD, diagrams, devsecops, setup, DTO-MAPPING-GUIDE, RELEASE, DEVELOPMENT-PLAN
- [ ] 8.3 README, backend README, CLAUDE.md, rules, agents, skills, `openspec/config.yaml` and schema, `deprecated/README.md`
- [ ] 8.4 `CHANGELOG.md` — one entry
- [ ] 8.5 Open a follow-up issue for the Constitution §5 step 4 module list (separate PR, §12)

## 9. Commits atómicos

- [ ] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1255`; others `Refs #1255`
- [ ] 9.3 No secrets, no commented-out code, unrelated working-tree files left out

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin refactor/1255_deprecate_notaire_shared`
- [ ] 10.3 Open PR `[#1255] refactor(build): deprecate notaire-shared; backend-api owns its DTOs`
- [ ] 10.4 Wait for all required workflows
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI and CD green on the merge commit; health UP; OpenAPI equals the baseline
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1255 referencing the PR; mark #581 superseded
- [ ] 12.4 Archive the change: `openspec archive refactor-1255-deprecate-notaire-shared`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1255` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
