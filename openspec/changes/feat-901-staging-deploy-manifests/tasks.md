> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists — CU77 exists (update GitHub ID table at implement)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (Kustomize overlay of #1044; cite SAD §11.3 / deployment docs)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 901 --add-label "in-progress"`) — attempted at implement; label ACL may 403

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — queue ahead cleared (#1067 merged)
- [x] 2.2 `git checkout -b cursor/feat-901-staging-deploy-manifests-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy Gate 1 draft into `openspec/changes/feat-901-staging-deploy-manifests/` and run `bash scripts/validate-sdlc-plan.sh feat-901-staging-deploy-manifests`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: four-service render, staging overlay, postgres isolation, secrets placeholders, ENVIRONMENT, Flyway baseline off, validator red/green
- [x] 3.2 Write failing `scripts/test_staging_kustomize.py` before manifests exist
- [x] 3.3 Optional CI workflow — deferred (cd.yml stays publish-only; static script is the gate)
- [x] 3.4 Observe fail (`python3 scripts/test_staging_kustomize.py` non-zero) before implementing
- [x] 3.5 Confirm every `#### Scenario:` maps to a script assertion or docs checklist

## 4. Implementación

- [x] 4.1 Add Kustomize base under `deploy/kustomize/` for postgres, backend, frontend, reverse-proxy
- [x] 4.2 Add staging overlay (GHCR SHA tags for backend + frontend; #1043 CLOSED)
- [x] 4.3 Wire Secret placeholders — no committed credentials
- [x] 4.4 Set backend `ENVIRONMENT=production` and Flyway baseline-on-migrate off
- [x] 4.5 Make static validator green; no fake cluster deploy job
- [x] 4.6 Do not invent Helm/operators/mesh/observability stack in this change

## 5. Actualizar tests existentes

- [x] 5.1 Confirm `scripts/test_prod_compose.py` still passes (#1044 invariants untouched)
- [x] 5.2 Update docs that claimed “no k8s manifests” / “no staging target”
- [x] 5.3 Remove obsolete “future Kubernetes only” stubs that contradict landed manifests

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [x] 6.4 Bruno — n/a (no API contract change)
- [x] 6.5 No `@Disabled` validators without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no Playwright spec edits required
- [ ] 7.2 PR must still pass required workflows including Playwright if triggered
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface" (infra/manifests only)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `209-deployment/README.md`, `DEPLOYMENT-PLAN.md`, CU77 per proposal.md
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Edit in place (SAD §11.1 / roadmap pointer)
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh` — run applicable local gates before push

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #901`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/feat-901-staging-deploy-manifests-69d3`
- [x] 10.2 Open the PR titled `[#901] feat(deploy): staging Kustomize manifests`, referencing Issue and CU77
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md` (#1176)
## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR (unchanged publish path)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `kustomize build` / validator green; if staging host exists, hit health via ingress
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive feat-901-staging-deploy-manifests`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing static validator observed (Gate 2)
- [ ] Implementation passes validators and required CI (Gate 3–4)
- [ ] Coverage gate unaffected or still satisfied
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #901`
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke/validator evidence recorded; Issue closed (Gate 5)
