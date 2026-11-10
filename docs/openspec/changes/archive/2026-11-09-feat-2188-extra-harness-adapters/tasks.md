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
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — integration may lack label permission; note in PR

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail** — `python3 -m unittest discover -s framework/ai-sdlc/tests`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Add plugin stubs + adapter.yml for claude-code, openai-codex, agent-client-protocol
- [x] 4.2 Extend harness list/show to merge harness-kind plugins; always sandbox=false
- [x] 4.3 Notaire: declare stubs trusted=false out of allow; inventory rows; docs
- [x] 4.4 Dry plan describe argv; list/validate/plan never execute runtimes
- [x] 4.5 Kit stays free of Notaire product paths; do not flip `[committer] required`

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s framework/ai-sdlc/tests` — kit suite green
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a for this kit-only change (preflight covers)
- [x] 6.3 `mvn verify -pl backend-api` — covered by preflight when run
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a HTTP surface
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Documentación

- [x] 7.1 Update FACTORY-FOUNDATION.md / FACTORY-ROADMAP.md / README / ADR-002 as needed
- [x] 7.2 Update affected Use Case docs if behavior changed
- [x] 7.3 CHANGELOG Unreleased omitted while release-please owns the fence

## 8. Commit

- [x] 8.1 Conventional Commits; reference #2188
- [x] 8.2 No secrets in the commit

## 9. Push + PR

- [ ] 9.1 Push branch; open PR with `Closes #2188`
- [ ] 9.2 PR body lists validation commands and out-of-scope honesty

## 10. Gate 3 — CI verde

- [ ] 10.1 Required checks green; heavy-CI when applicable
- [ ] 10.2 Address review feedback

## 11. Merge

- [ ] 11.1 Merge when heavy-CI exit 0 + MERGEABLE (`gh` only)
- [ ] 11.2 Do not flip required=true; no portal

## 12. Post-merge

- [ ] 12.1 Archive OpenSpec change
- [ ] 12.2 Update thin store plan
- [ ] 12.3 Confirm tip SHA and harness list sample

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
