> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1045 OPEN; CU78
- [x] 1.2 Use Case documentation exists and is accurate — CU78 exists; update notes at implement if needed
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a new ADR; update ADR-017 pin policy at implement
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1045 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — based on `origin/main` @ `8ab8a6e5` (after #1043)
- [x] 2.2 `git checkout -b cursor/chore-1045-pin-images-dependabot-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1045/` into `openspec/changes/chore-1045-pin-images-dependabot/` and run `bash scripts/validate-sdlc-plan.sh chore-1045-pin-images-dependabot`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from both delta specs (image pins + Dependabot npm/docker)
- [x] 3.2 Write failing `scripts/test_image_pins_and_dependabot.py` (or equiv.) that asserts: no `:latest` in scoped compose; no bare `sonarqube:community`; postgres/Dockerfile/CI pins; dependabot has npm `/frontend`, docker `/backend-api` + `/frontend`, retains maven + github-actions
- [x] 3.3 Run the script on the pre-change tree and **observe it fail** (docker ecosystem missing; floating tags present)
- [x] 3.4 Confirm every `#### Scenario:` in both delta specs maps to at least one assert or explicit checklist item

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: floating tags still present; dependabot has npm but not docker; #1046/#1043 state as expected
- [x] 4.2 Resolve current minor tags (or digests) for: pgadmin, homer, sonarqube community, prometheus, postgres-exporter, grafana, loki, promtail, postgres 16/15, maven/temurin/node, nginx if tightening, CI postgres
- [x] 4.3 Pin `docker-compose.yml`, `docker-compose.prod.yml`, `infra/docker-compose.yml` image lines
- [x] 4.4 Pin `backend-api/Dockerfile`, `backend-api/Dockerfile.slim`, `frontend/Dockerfile` `FROM` lines within ADR-017 families
- [x] 4.5 Pin CI workflow postgres service images (`playwright-e2e.yml`, `performance-test.yml`)
- [x] 4.6 Update `.github/dependabot.yml`: keep npm `/frontend` (add only if missing); add docker for `/backend-api` and `/frontend` with weekly schedule/labels consistent with existing entries; do not duplicate
- [x] 4.7 Make hygiene tests pass without weakening AC asserts
- [x] 4.8 Smoke: Docker Hub tag API confirms all pinned tags exist (local `docker` unavailable in agent VM); full compose start deferred to CI/coordinator

## 5. Actualizar tests existentes

- [x] 5.1 Identify any existing compose/Dockerfile tests that hardcode floating tags (e.g. prod-compose tests)
- [x] 5.2 Update expected strings to pinned tags without weakening security asserts — none hardcoded floating tags; prod-compose / CD SHA pin tests still green
- [x] 5.3 Remove tests made genuinely obsolete only if they asserted `:latest` as required — n/a

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration (sanity; no product Java expected)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (unchanged)
- [x] 6.3 `mvn verify -pl backend-api` — quality gates if Java touched; else note n/a beyond sanity
- [x] 6.4 HTTP/Bruno — n/a for API delta; run only if preflight `--full` required
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (image pins + Dependabot + docs)
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip)
- [x] 7.3 If unexpected E2E edits appear, serialize with other Playwright-heavy PRs
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact (ADR-017, DevSecOps README, infra README, CU78 note as needed)
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for image pins + Dependabot docker
- [x] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `chore(docker): pin compose and Dockerfile bases`, `ci(deps): add Dependabot docker`, `docs: …`)
- [ ] 9.2 Every commit message ends with `Closes #1045`
- [ ] 9.3 No secrets; do not absorb #1046 alert fixes or #1043 CD publish work
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/chore-1045-pin-images-dependabot-69d3`
- [ ] 10.2 Open the PR titled `[#1045] chore(devops): pin images and add Dependabot docker`, referencing Issue and CU78
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) still publishes as expected with pinned Dockerfile bases
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: no scoped `:latest` / bare `sonarqube:community` / major-only postgres; Dependabot shows npm + docker; optional compose pull of pinned tags succeeds
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive chore-1045-pin-images-dependabot`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a product UI; PR Playwright job green)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
