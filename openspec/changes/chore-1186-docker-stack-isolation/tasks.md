> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1186 exists, labeled, and linked to CU76
- [x] 1.2 Use Case documentation exists (CU76); add #1186 to its GitHub ID table at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (configuration only)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/1186_docker_stack_isolation`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh chore-1186-docker-stack-isolation`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: names, host ports, defaults render, overrides render, start.sh, docs keys
- [x] 3.2 Write failing `scripts/test_dev_stack_isolation.py`
- [x] 3.3 Observe it fail before touching `docker-compose.yml`
- [x] 3.4 Confirm every `#### Scenario:` maps to an assertion

## 4. Implementación

- [x] 4.1 Parametrise container names and host ports in `docker-compose.yml`
- [x] 4.2 Make `scripts/start.sh` follow the configured ports
- [x] 4.3 Add the eight optional keys to `.env.example`
- [x] 4.4 Guard green; defaults render unchanged

## 5. Actualizar tests existentes

- [x] 5.1 Existing compose guards (`test_image_pins_and_dependabot.py`, `test_prod_compose.py`) still pass
- [x] 5.2 Nothing else references the literal ports in `start.sh`

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 Bruno/HTTP suites via `bash scripts/run_pipeline.sh` (default ports unchanged)
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no spec edits
- [ ] 7.2 Required Playwright CI job must still pass on the PR
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `.env.example` keys documented
- [x] 8.2 `209-deployment/README.md` dev section: parallel stacks and the observability limitation
- [x] 8.3 CU76 ID table and `CHANGELOG.md`
- [x] 8.4 Confirm no information was duplicated
- [ ] 8.5 `bash scripts/preflight.sh`

## 9. Commits atómicos

- [ ] 9.1 Small, self-contained Conventional Commits
- [ ] 9.2 Only the final commit may carry `Closes #1186`; others `Refs #1186`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh`
- [ ] 10.2 `git push -u origin chore/1186_docker_stack_isolation`
- [ ] 10.3 Open PR `[#1186] chore(docker): configurable ports and container names`
- [ ] 10.4 Wait for all required workflows to pass
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: default `start.sh` still healthy; a second stack with overrides starts beside it
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1186 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive chore-1186-docker-stack-isolation`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guard observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
