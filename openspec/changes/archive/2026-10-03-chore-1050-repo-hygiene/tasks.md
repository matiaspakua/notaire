> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1050 OPEN; CU76; DEVOPS / chore / priority:medium / audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; point hygiene policy from permanent docs at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — **ADR-022 required at implement** (filter-repo + PDF strategy)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1050 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after** queue `#1042→#1041→#1040→#1043→#1045→#1056→#1055`
- [x] 2.2 `git checkout -b cursor/chore-1050-repo-hygiene-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1050/` into `openspec/changes/chore-1050-repo-hygiene/` and run `bash scripts/validate-sdlc-plan.sh chore-1050-repo-hygiene`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the delta spec (txt ignore, serena, CODEOWNERS, PDF blob, ADR presence)
- [x] 3.2 Write failing static unittest `scripts/test_repo_hygiene.py` (or equivalent) covering every scenario
- [x] 3.3 n/a integration tests (no API/DB)
- [x] 3.4 Run the new static tests on the pre-change tree and **observe them fail**
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test/assert

## 4. Implementación

- [x] 4.1 Narrow `.gitignore`: remove global `*.txt`; add targeted ignores; add `.serena/`
- [x] 4.2 `git rm -r --cached .serena` (leave files on disk optional)
- [x] 4.3 Verify/fix `.github/CODEOWNERS` against live tree (no `frontend-swing`)
- [x] 4.4 Relocate 13 MB user-manual PDF (Release asset and/or LFS) + stub/docs link
- [x] 4.5 Author `ADR-022` (filter-repo decision + PDF strategy); default defer rewrite
- [x] 4.6 Update ADR index + contributor docs for manuals / ignore policy

## 5. Actualizar tests existentes

- [x] 5.1 Identify any scripts/CI that assumed global `*.txt` ignore or in-tree PDF path — none found; Swing setup already referenced requirements.txt
- [x] 5.2 Update them without weakening hygiene asserts — n/a; added new guard tests only
- [x] 5.3 Remove obsolete assumptions with stated reason — n/a

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — expect unchanged green
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a delta; keep if full verify
- [x] 6.3 Frontend lint/test — n/a product change; smoke if docs-only touch
- [x] 6.4 Bruno/HTTP — n/a
- [x] 6.5 No `@Disabled` or skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no Playwright spec edits required
- [ ] 7.2 PR must still pass required workflows including Playwright job if triggered
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Do not mark heavy CI skip; serialize only if unexpected UI diffs appear

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) chore entry
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — only if a superseded manual path is retired — PDF relocated to Release, not archived as doc
- [x] 8.5 Confirm no information was duplicated — permanent docs remain SSOT
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #1050`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/chore-1050-repo-hygiene-69d3`
- [x] 10.2 Open the PR titled `[#1050] chore(repo): hygiene ignore rules, CODEOWNERS, manual PDF, history ADR`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm CD as applicable (no app image change expected)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `git check-ignore` / `git ls-files` asserts; PDF obtainable per docs; ADR present
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive chore-1050-repo-hygiene`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a product UI; CI job still green)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
