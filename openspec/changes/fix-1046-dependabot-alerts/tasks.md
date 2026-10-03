> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1046 OPEN; CU78
- [x] 1.2 Use Case documentation exists and is accurate — CU78 exists; update notes at implement if needed
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a new ADR; update ADR-005 status line at implement
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1046 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main` — **after #1044 and #1051 merge**
- [ ] 2.2 `git checkout -b cursor/fix-1046-dependabot-alerts-69d3`
- [ ] 2.3 Record the branch name in `traceability.md`
- [ ] 2.4 Copy this draft from `internal/openspec-1046/` into `openspec/changes/fix-1046-dependabot-alerts/` and run `bash scripts/validate-sdlc-plan.sh fix-1046-dependabot-alerts`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from both delta specs (Swing absent; no log4j:log4j; smol-toml override + lockfile version; CODEOWNERS/README hygiene; npm ci)
- [ ] 3.2 Write failing hygiene script/tests that assert current `origin/main` still has `deprecated-frontend-swing/pom.xml` with log4j and lockfile `smol-toml` without override — expect FAIL on pre-change tree for the “absent/overridden” asserts
- [ ] 3.3 Add asserts for CODEOWNERS / README live-path hygiene where automatable
- [ ] 3.4 Run them and **observe them fail** on the pre-change tree
- [ ] 3.5 Confirm every `#### Scenario:` in both delta specs maps to at least one test or explicit checklist item

## 4. Implementación

- [ ] 4.1 Re-verify on updated `main`: `deprecated-frontend-swing/` still present; `frontend-swing/` absent; #585 still open for `src.old` track (do not delete `deprecated-src.old/` unless coordinator expands scope)
- [ ] 4.2 Delete `deprecated-frontend-swing/` entirely
- [ ] 4.3 Add `overrides.smol-toml` to `frontend/package.json` (`^1.9.0` or `1.9.0`); run `npm install` under `frontend/` and commit lockfile so resolved version is patched
- [ ] 4.4 Clean live references: `.github/CODEOWNERS`, root `README.md`, ADR-005/SAD status lines as needed (skip bulk `docs/000-archive/**` rewrites)
- [ ] 4.5 Make hygiene tests pass without weakening AC asserts
- [ ] 4.6 Confirm `mvn clean install -pl backend-api -am` still builds; `cd frontend && npm ci` succeeds

## 5. Actualizar tests existentes

- [ ] 5.1 Identify any CI/docs scripts that `cd deprecated-frontend-swing` or `-pl frontend-swing`
- [ ] 5.2 Update or remove those references without weakening product assertions
- [ ] 5.3 Remove tests made genuinely obsolete (Swing-only), stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration (sanity; no product Java expected)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (unchanged)
- [ ] 6.3 `mvn verify -pl backend-api` — quality gates if Java touched; else note n/a beyond sanity
- [ ] 6.4 HTTP/Bruno — n/a for API delta; run only if preflight `--full` required
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI product surface (deps + dead-tree delete + docs)
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip)
- [ ] 7.3 If unexpected E2E edits appear, serialize with other Playwright-heavy PRs
- [ ] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for security/deps cleanup
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `fix(deps): …`, `chore: remove deprecated-frontend-swing`, `docs: …`)
- [ ] 9.2 Every commit message ends with `Closes #1046`
- [ ] 9.3 No secrets, no commented-out code, no unrelated `#585` / `deprecated-src.old` deletion unless explicitly authorized
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1046-dependabot-alerts-69d3`
- [ ] 10.2 Open the PR titled `[#1046] fix(deps): clear Dependabot Swing log4j + smol-toml`, referencing Issue and CU78
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR if applicable
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `deprecated-frontend-swing` absent on `main`; lockfile `smol-toml` ≥ 1.7.1; Security/Dependabot shows no open critical/high for these findings
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR (and leave #585 open unless `deprecated-src.old` was separately handled)
- [ ] 12.4 Archive the change: `openspec archive fix-1046-dependabot-alerts`

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
