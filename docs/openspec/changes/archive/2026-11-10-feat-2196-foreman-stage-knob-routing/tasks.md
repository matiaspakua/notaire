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
- [ ] 3.3 Write the integration tests where applicable
- [ ] 3.4 Run them and **observe them fail** — kit unittest discover
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add ForemanPlanTest (failing first) for empty / mapped / filter / unknown / honesty / route alias
- [ ] 4.2 Implement `aisdlc.foreman` plan builder; wire CLI `foreman plan|route [--stage] [--json]`
- [ ] 4.3 Update cloud-foreman.md + kit foreman template; README + FACTORY docs; thin plan + remaining-gaps
- [ ] 4.4 Archive OpenSpec; preflight --full; PR Closes #2196

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Confirm whether this change touches UI — if **no**, record N/A here and in the PR
- [ ] 7.2 If UI-related: run `cd frontend && npx playwright test <affected-specs>`
- [ ] 7.3 If UI-related: verify screenshots / evidence for the affected flows
- [ ] 7.4 Confirm zero flaky failures; quarantine requires Owner approval

## 8. Documentation

- [ ] 8.1 Update permanent engineering docs (README / FACTORY / cloud-foreman / kit template)
- [ ] 8.2 Update business / user docs only if the user-facing behavior changed
- [ ] 8.3 Update ADRs only if an architectural decision changed
- [ ] 8.4 Update OpenAPI / Bruno / contracts if an API changed (N/A)

## 9. Commit

- [ ] 9.1 Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`)
- [ ] 9.2 Atomic commits; message body explains **why**
- [ ] 9.3 Every commit or the PR body includes `Closes #2196`

## 10. Pull Request

- [ ] 10.1 Open the PR with the summary of what / why / how tested
- [ ] 10.2 PR description links `Closes #2196` and Use Case `CU76`
- [ ] 10.3 All CI checks green; address every review comment
- [ ] 10.4 Do not squash without preserving the issue reference

## 11. Merge & Close

- [ ] 11.1 Merge only when `bash workspace/sdlc/check-heavy-ci.sh <pr>` exits 0
- [ ] 11.2 Confirm the Issue auto-closed via `Closes #2196`
- [ ] 11.3 Confirm board Status / labels coherent

## 12. Post-merge verification

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

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
