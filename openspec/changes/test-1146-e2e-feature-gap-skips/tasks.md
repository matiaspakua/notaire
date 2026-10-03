> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`)
- [x] 1.2 Use Case documentation exists and is accurate — CU76
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (test/docs hygiene)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1146 --add-label "in-progress"`) — best-effort; label ACL may 403

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -B cursor/test-1146-e2e-feature-gap-skips-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases: citation `#\d+`, exact inventory 14 (2/3/2/7), CU21 absent
- [x] 3.2 Tighten Vitest in `e2e-test-reliability.test.ts` for those scenarios
- [x] 3.3 Integration tests — n/a (no backend / API surface)
- [x] 3.4 Run Vitest and **observe fail** when citation/count wrong, then green on restore
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to a check

## 4. Implementación

- [x] 4.1 Strengthen hygiene assertions (per-file counts + `#\d+` on each skip title)
- [x] 4.2 Sync `E2E-TEST-MAPPING.md` count/table to 14 live skips with owning CU + product tracking
- [x] 4.3 Update CU76 GitHub ID / pointer for #1146 tracker hygiene
- [x] 4.4 Add CHANGELOG `[Unreleased]` engineering note
- [x] 4.5 Do **not** unskip any scenario without real UI assertions

## 5. Actualizar tests existentes

- [x] 5.1 Identify affected tests (`e2e-test-reliability.test.ts`)
- [x] 5.2 Update without weakening assertions
- [x] 5.3 Remove obsolete tests — none expected

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a no backend delta (skip or quick sanity)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a no backend delta
- [x] 6.3 `mvn verify -pl backend-api` — n/a no backend delta; frontend Vitest + preflight
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a no API change
- [x] 6.5 No `@Disabled` or skipped tests without documented `#issue` justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface; do not unskip product-gap scenarios in this PR
- [x] 7.2 n/a — no Playwright scenario changes
- [x] 7.3 n/a — no viewport UI changes
- [x] 7.4 Recorded: no UI surface (docs + static Vitest hygiene only)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded documents — none
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1146`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/test-1146-e2e-feature-gap-skips-69d3`
- [ ] 10.2 Open draft PR `[#1146] test(e2e): track feature-gap skips inventory`, CU76
- [ ] 10.3 Wait for required workflows (do not merge in this assignment)
- [ ] 10.4 Gate 4 — CI green, review approved — pending human / foreman
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — **do not merge in this agent turn**
- [ ] 11.2 CD image publish — n/a for docs/Vitest-only until merge
- [ ] 11.3 Record merge commit / tag after merge — pending

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: Vitest hygiene green; mapping shows 14 skips
- [ ] 12.2 Rollback path: git revert of docs + test file
- [ ] 12.3 Close the GitHub Issue after merge — pending
- [ ] 12.4 Archive the change: `openspec archive test-1146-e2e-feature-gap-skips` — after Done

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes — n/a no UI
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
