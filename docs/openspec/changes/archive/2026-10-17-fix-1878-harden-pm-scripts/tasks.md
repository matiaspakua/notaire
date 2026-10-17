> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/1878_harden_pm_scripts`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail**
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Whole-run fake GitHub test + ensure create_body path never sends due_on null
- [x] 4.2 Richer `--check` (desc/due drift, closed+status:*, per-bucket summary, snapshot freshness, missing milestones)
- [x] 4.3 Token scopes preflight before writes; step headers; richer HTTP errors
- [x] 4.4 Board loaders: GraphQL paginated `--scope all` + `open_prs`; R6 live-shaped test
- [x] 4.5 Tool to set Status Done for closed issues
- [x] 4.6 VIEW_LAYOUT / group-sort docs + UI-only notes; Current train derived or documented
- [x] 4.7 Closes/Fixes guard for multi_pr / Owner-pending
- [x] 4.8 Owner scripts README; bash -n / shellcheck CI wiring
- [x] 4.9 Absorb #1874 retire/stale coverage already in tree (no live --apply)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 n/a — no backend-api change; run `python3 -m unittest` for workspace/sdlc + workspace/tests
- [x] 6.2 n/a — JaCoCo unaffected
- [x] 6.3 n/a — no Maven module change
- [x] 6.4 n/a — no API surface
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 n/a — no OpenAPI change
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/`
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate (or document skip if Owner-only)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Closing commit ends with `Closes #1878`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>` (only when asked)
- [ ] 10.3 Open the PR titled `[#1878] fix(pm): harden milestone/board Owner scripts`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete

## 11. Deploy

- [ ] 11.1 Merge through the Pull Request only
- [ ] 11.2 No CD artifact expected (tooling/docs)
- [ ] 11.3 Record merge evidence in archived traceability

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Confirm `apply_milestone_policy.py --check` → mismatches: 0 (do not re-run --apply)
- [ ] 12.2 Confirm a revert remains safe
- [ ] 12.3 Close #1878 via `Closes #1878` on the final PR
- [ ] 12.4 Record deploy and smoke evidence in archived traceability

## Definition of Done

- [x] Issue linked to CU76 with acceptance criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Failing tests observed before implementation (Gate 2)
- [x] Workspace unit tests green (Gate 3)
- [x] Permanent docs + CHANGELOG updated
- [ ] Commits conventional; final `Closes #1878`
- [ ] PR ready (push when asked)
