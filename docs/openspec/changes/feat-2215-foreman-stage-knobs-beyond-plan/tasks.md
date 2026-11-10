> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [ ] 3.2 Write the unit tests for every scenario in the delta spec
- [ ] 3.3 Write the integration tests where applicable (n/a — profile/catalog; kit only if verify asserts)
- [ ] 3.4 Run them and **observe them fail** — kit/adopter assertion before knobs land
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add agent catalog entries for fleet roles used by new stages (path refs to `.claude/agents/`)
- [ ] 4.2 Declare `[[stages]]` for `red`, `pre-push`, `pre-pr` with real skills/model/effort; keep `plan`; mcp empty
- [ ] 4.3 Update FACTORY-ROADMAP: mapped ≥4, green omission, remaining unmapped honesty; CHANGELOG
- [ ] 4.4 Confirm doctor clean (`signal` alias) and `foreman plan --json` mapped_count ≥ 4

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason (none expected)

## 6. Ejecutar regresión

- [ ] 6.1 `python3 -m unittest discover -s framework/ai-sdlc/tests` — kit suite (and new adopter asserts)
- [ ] 6.2 `bash framework/ai-sdlc/verify.sh` — kit + Notaire doctor
- [ ] 6.3 `mvn test -pl backend-api` — n/a for this profile/docs change (record in PR); preflight --full covers product gates
- [ ] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change); preflight --full covers when required
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Confirm whether this change touches UI — if **no**, record N/A here and in the PR
- [ ] 7.2 If UI-related: run Playwright for affected specs (n/a)
- [ ] 7.3 If UI-related: verify screenshots / evidence (n/a)
- [ ] 7.4 n/a — no UI surface (profile / catalogs / docs only)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed (n/a)
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` (n/a)
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>` (or PR body `Closes #2215`)
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR (n/a if docs/kit-only — record)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (`aisdlc foreman plan --json` + doctor)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
