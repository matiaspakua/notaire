> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1091 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/ai-sdlc-enforcement/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `chore/1091_harness_autonomy_fixes`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: tick kept, added item dropped, `[n/a]` dropped, rows present, row missing, adapter key
- [ ] 3.2 `local-ai/sdlc/tests/test_ledger.py` and `test_adapter.py` cover every scenario in the delta spec
- [ ] 3.3 n/a — no integration tests apply
- [ ] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [ ] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [ ] 4.1 `ledger.py`: `restore-ticks` and `rows` commands
- [ ] 4.2 `adapter.py` and `.aisdlc/project.yml`: `gates.docs_lint_fix`
- [ ] 4.3 `foreman.sh`: stale `BLOCKED.md` removed; `RECHECK` never runs the worker
- [ ] 4.4 `foreman.sh`: `tasks.md` repaired and logged; spec gate checks ledger rows and lints the change
- [ ] 4.5 `foreman.sh`: lint autofix before spec and docs lint; `Closes #N` checked before the pr worker
- [ ] 4.6 `prompts/09-pr.md`: drop the worker's `Closes` check

## 5. Actualizar tests existentes

- [ ] 5.1 Existing harness self-tests unchanged and green

## 6. Ejecutar regresión

- [ ] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [ ] 6.2 `bash -n local-ai/sdlc/foreman.sh`; `restore-ticks` on the stored #1063 docs-phase `tasks.md`
- [ ] 6.3 `bash scripts/preflight.sh` green on the branch
- [ ] 6.4 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `local-ai/sdlc/AI-SDLC.md`: `RECHECK`, `tasks.md` repair, lint autofix, ledger rows, stale block
- [ ] 8.2 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [ ] 9.1 One commit per concern, Conventional Commits, `Refs #1091`
- [ ] 9.2 Only the final commit carries `Closes #1091`
- [ ] 9.3 No secrets, no commented-out code
- [ ] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1091] chore: remove foreman interventions found in #1063`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: a full harness run on the next backlog issue with no foreman intervention
- [ ] 12.2 Rollback path confirmed (`git revert`)
- [ ] 12.3 Issue #1091 closed on merge
- [ ] 12.4 Change archived in this PR (`openspec archive harness-autonomy-fixes-1091`)

## Definition of Done

- [ ] Issue linked to Use Case CU76
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests written first and observed failing (Gate 2)
- [ ] New suites green; existing suites unaffected
- [ ] Coverage at or above the JaCoCo ratchet floor (unaffected — no backend code)
- [ ] Playwright E2E: n/a, no UI surface
- [ ] Permanent documentation updated
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green (Gate 4)
- [ ] Merged, CD green, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
