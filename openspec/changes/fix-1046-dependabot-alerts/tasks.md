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
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1046 --add-label "in-progress"`) — attempted; label ACL 403 for integration (noted)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — based on `origin/main` @ `c2c34de8` (#1051)
- [x] 2.2 `git checkout -b cursor/fix-1046-dependabot-alerts-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1046/` into `openspec/changes/fix-1046-dependabot-alerts/` and run `bash scripts/validate-sdlc-plan.sh fix-1046-dependabot-alerts`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from both delta specs (Swing absent; no log4j:log4j; smol-toml override + lockfile version; CODEOWNERS/README hygiene; npm ci)
- [x] 3.2 Write failing hygiene script/tests that assert current `origin/main` still has `deprecated-frontend-swing/pom.xml` with log4j and lockfile `smol-toml` without override — expect FAIL on pre-change tree for the “absent/overridden” asserts
- [x] 3.3 Add asserts for CODEOWNERS / README live-path hygiene where automatable
- [x] 3.4 Run them and **observe them fail** on the pre-change tree — 5 failures before implement
- [x] 3.5 Confirm every `#### Scenario:` in both delta specs maps to at least one test or explicit checklist item

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: `deprecated-frontend-swing/` still present; `frontend-swing/` absent; #585 still open for `src.old` track (do not delete `deprecated-src.old/` unless coordinator expands scope)
- [x] 4.2 Delete `deprecated-frontend-swing/` entirely
- [x] 4.3 Add `overrides.smol-toml` to `frontend/package.json` (`^1.9.0` or `1.9.0`); run `npm install` under `frontend/` and commit lockfile so resolved version is patched
- [x] 4.4 Clean live references: `.github/CODEOWNERS`, root `README.md`, ADR-005/SAD status lines as needed (skip bulk `docs/000-archive/**` rewrites)
- [x] 4.5 Make hygiene tests pass without weakening AC asserts
- [x] 4.6 Confirm `mvn clean install -pl backend-api -am` still builds; `cd frontend && npm ci` succeeds — `npm ci` OK; Maven sanity in regression group

## 5. Actualizar tests existentes

- [x] 5.1 Identify any CI/docs scripts that `cd deprecated-frontend-swing` or `-pl frontend-swing` — none in `.github/` or `scripts/` (only hygiene asserts)
- [x] 5.2 Update or remove those references without weakening product assertions — live CODEOWNERS/README/skills cleaned
- [x] 5.3 Remove tests made genuinely obsolete (Swing-only), stating the reason — n/a; Swing product tests lived under deleted tree (historical only)

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (sanity; no product Java expected) — see implement commits / preflight
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (unchanged) — n/a product Java; preflight covers when run
- [x] 6.3 `mvn verify -pl backend-api` — quality gates if Java touched; else note n/a beyond sanity — no Java product code touched
- [x] 6.4 HTTP/Bruno — n/a for API delta; run only if preflight `--full` required
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (deps + dead-tree delete + docs)
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip) — coordinator after PR
- [x] 7.3 If unexpected E2E edits appear, serialize with other Playwright-heavy PRs — none
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for security/deps cleanup
- [x] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced — n/a
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — frontend gates green; Maven missing in this agent env (`mvn: command not found`); repo-wide SDLC scan fails on unrelated CLOSED changes (our change validates alone)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `fix(deps): …`, `chore: remove deprecated-frontend-swing`, `docs: …`)
- [x] 9.2 Every commit message ends with `Closes #1046`
- [x] 9.3 No secrets, no commented-out code, no unrelated `#585` / `deprecated-src.old` deletion unless explicitly authorized
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/fix-1046-dependabot-alerts-69d3`
- [x] 10.2 Open the PR titled `[#1046] fix(deps): clear Dependabot Swing log4j + smol-toml`, referencing Issue and CU78 — PR #1157 draft
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [x] 10.5 Record the PR number in `traceability.md` — #1157

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

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes (n/a product UI; PR Playwright job green)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
