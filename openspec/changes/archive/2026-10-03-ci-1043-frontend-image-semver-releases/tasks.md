> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1043 OPEN; CU76; labels FRONTEND/DEVOPS/priority:high/audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update notes at implement if needed
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a unless release-please versioning policy needs a short ADR at implement
- [x] 1.6 Move the Issue to IN PROGRESS — attempted `gh issue edit 1043 --add-label "in-progress"`; ACL 403 (integration); coordinator may label

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1040 merges** (queue: #1046 → #1042 → #1041 → #1040 → #1043) or as coordinator schedules
- [x] 2.2 `git checkout -b cursor/ci-1043-frontend-image-semver-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1043/` into `openspec/changes/ci-1043-frontend-image-semver-releases/` and run `bash scripts/validate-sdlc-plan.sh ci-1043-frontend-image-semver-releases`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/frontend-ghcr-publish/spec.md` and `specs/semver-versioned-releases/spec.md`
- [x] 3.2 Write failing `scripts/test_frontend_ghcr_publish.py` asserting current `cd.yml` has no frontend Dockerfile publish / no frontend cosign+SBOM path
- [x] 3.3 Write failing `scripts/test_semver_release_process.py` asserting no automated release-please/tag release wiring for Maven+npm+CHANGELOG roll
- [x] 3.4 Run them and **observe them fail** on the pre-change tree
- [x] 3.5 Confirm every `#### Scenario:` in both delta specs maps to at least one test or explicit checklist item
- [x] 3.6 Integration tests — n/a for workflow/config (document n/a); Gate 5 covers live GHCR/release smoke

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: backend-only CD; empty tags/releases; `1.0-SNAPSHOT` / `0.1.0`; `frontend/Dockerfile` present; #1042 pin pattern present (must preserve)
- [x] 4.2 Add frontend publish path to `cd.yml` (matrix or sibling job): context `./frontend`, file `frontend/Dockerfile`, `IMAGE_NAME …/frontend`
- [x] 4.3 Wire frontend Trivy CycloneDX SBOM upload, cosign sign, cosign attest (parity with backend); distinct artifact names
- [x] 4.4 Apply pin-to-tested-SHA + SHA-before-`latest` to frontend path (inherit #1042)
- [x] 4.5 Add release automation (release-please preferred, or tag-triggered) that creates `v*` + GitHub Release
- [x] 4.6 Roll CHANGELOG `[Unreleased]` into versioned section on cut; keep Keep a Changelog structure
- [x] 4.7 Derive Maven root/reactor version and `frontend/package.json` version from tag `vX.Y.Z` → `X.Y.Z`
- [x] 4.8 Resolve softprops vs release-please duplicate Release creation (skip one)
- [x] 4.9 Document bootstrap first version and operator release steps
- [x] 4.10 Make the new unittests pass without weakening AC asserts

## 5. Actualizar tests existentes

- [x] 5.1 Identify scripts that parse `cd.yml` (`test_ci_workflow_invariants.py`, `test_cd_pin_tested_sha.py`, report-job tests)
- [x] 5.2 Update them for frontend matrix/job without weakening backend pin/success/SBOM asserts
- [x] 5.3 Remove tests made genuinely obsolete only if they assumed backend-only publish forever — state the reason

## 6. Ejecutar regresión

- [x] 6.1 `python3 scripts/test_frontend_ghcr_publish.py` — green
- [x] 6.2 `python3 scripts/test_semver_release_process.py` — green
- [x] 6.3 Related CD pin / workflow unit tests green
- [x] 6.4 Backend `mvn verify` — n/a for YAML-only unless Maven versions were touched in-tree before release; still run heavy gate on PR
- [x] 6.5 HTTP/Bruno — n/a for API delta
- [x] 6.6 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (CD + release tooling + static tests + docs)
- [x] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip)
- [x] 7.3 If unexpected E2E edits appear, serialize with other Playwright-heavy PRs
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for #1043 frontend GHCR + semver process
- [x] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `ci(cd): publish frontend image to GHCR`, `ci(release): add semver automation`, `test: …`, `docs: …`)
- [ ] 9.2 Every commit message ends with `Closes #1043`
- [ ] 9.3 No secrets, no commented-out code, no unrelated workflow rewrites
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/ci-1043-frontend-image-semver-69d3`
- [ ] 10.2 Open the PR titled `[#1043] ci(release): publish frontend image and semver releases`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Cut bootstrap release per runbook; confirm CD published **backend and frontend** images for the release/CI SHA
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: GHCR frontend package exists; cosign verify/attest OK; GitHub Release + `v*` tag exist; CHANGELOG has version section; Maven + npm versions match tag
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive ci-1043-frontend-image-semver-releases`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing tests written first (Gate 2)
- [ ] Implementation passes unit + integration + regression; heavy CI green (Gate 3/4)
- [ ] Coverage gate held (n/a JaCoCo delta; no weakened asserts)
- [ ] Playwright: product n/a; PR Playwright job still green
- [ ] Permanent documentation updated; CHANGELOG curated
- [ ] Commits Conventional Commits + `Closes #1043`
- [ ] PR merged to `main` via heavy gate; smoke on GHCR + release passed; Issue closed (Gate 5)
