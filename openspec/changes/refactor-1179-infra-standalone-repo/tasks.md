> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1179 exists, labeled, and linked to CU77
- [x] 1.2 Use Case documentation exists (CU77); add #1179 to its GitHub ID table at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (relocation + docs; ADR-009/016/017/019 get path updates only)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b refactor/1179_infra_standalone_repo`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1179-infra-standalone-repo`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: layout, legacy removal, self-containment, env example, docs, consumers, validators
- [x] 3.2 Write failing `scripts/test_infra_standalone.py`
- [x] 3.3a Add failing assertions: single `nginx.conf`, ConfigMap equals the file, prod compose mounts it
- [x] 3.3 Repoint existing guards (`test_staging_kustomize.py`, `test_prod_compose.py`, `test_infra_prometheus_hardening.py`, `test_performance_test_assets.py`, `test_image_pins_and_dependabot.py`) to the new paths
- [x] 3.4 Observe the new and repointed guards fail before the move
- [x] 3.5 Confirm every `#### Scenario:` maps to an assertion

## 4. Implementación

- [x] 4.1 `git mv` observability files into `infra/observability/` (separate commit)
- [x] 4.2 `git mv` `deploy/*` to `infra/deploy/` and `performance-test/k6` to `infra/performance/k6` (separate commit)
- [x] 4.2a `git mv deploy/nginx/nginx.conf` to `infra/deploy/kustomize/base/nginx.conf`; replace `reverse-proxy-configmap.yaml` with a `configMapGenerator`; update the `docker-compose.prod.yml` mount
- [x] 4.3 Delete `infra/tests/`; drop its `.gitignore` entries
- [x] 4.4 Fix paths in compose mounts, `infra/scripts/*`, `scripts/start-all.sh`, `docker-compose.prod.yml`, workflows, `.gitignore`, `.env.example`, agent rule files
- [x] 4.5 Add `infra/.env.example` and env-file resolution in scripts
- [x] 4.6 Make all guards green; no behaviour change to observability

## 5. Actualizar tests existentes

- [x] 5.1 Existing guards pass without weakened assertions
- [x] 5.2 Fix docs that reference legacy paths
- [x] 5.3 Remove dead references to the deleted E2E suite

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 Bruno via `bash scripts/run_pipeline.sh`
- [x] 6.5 No `@Disabled` validators

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no spec edits
- [ ] 7.2 Required Playwright CI job must still pass on the PR
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Write `infra/README.md` and `infra/docs/{PREPARATION,CONFIGURATION,DEFINITION,OPERATION}.md` (fold `CREDENTIALS.md` into CONFIGURATION)
- [x] 8.2 Update README, CLAUDE, AGENTS, SAD, ADRs, 207, 209, DEPLOYMENT-PLAN, puml, CU77 per proposal.md
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded docs — none: `infra/CREDENTIALS.md` folded into `infra/docs/CONFIGURATION.md` and removed
- [x] 8.5 Confirm no information was duplicated between `docs/` and `infra/`
- [ ] 8.6 `bash scripts/preflight.sh --fix` then `bash scripts/preflight.sh`

## 9. Commits atómicos

- [ ] 9.1 Small, self-contained Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1179`; others `Refs #1179`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` passes
- [ ] 10.2 `git push -u origin refactor/1179_infra_standalone_repo`
- [ ] 10.3 Open PR `[#1179] refactor(infra): prepare infra/ as standalone repository`
- [ ] 10.4 Wait for all required workflows to pass
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: start app + infra, `bash infra/scripts/check-infra.sh` green; staging overlay renders
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1179 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive refactor-1179-infra-standalone-repo`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guard observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1179` on the last
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
