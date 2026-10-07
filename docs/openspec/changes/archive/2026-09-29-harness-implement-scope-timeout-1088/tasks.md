> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1088 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/ai-sdlc-enforcement/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `fix/1088_harness_implement_scope_timeout`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: planned non-source file, unplanned file, non-path cell, in-time exit, TERM-ignoring worker with a child
- [x] 3.2 `local-ai/sdlc/tests/test_scope.py` and `test_watchdog.py` cover every scenario in the delta spec
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `local-ai/sdlc/bin/scope.py implement`: planned files from triage and traceability, exact-path ERE
- [x] 4.2 `local-ai/sdlc/bin/watchdog.py`: process group, TERM, KILL after grace, exit 124, forwards TERM/INT
- [x] 4.3 `foreman.sh`: `phase_implement` uses `scope.py`; `run_worker` uses `watchdog.py`

## 5. Actualizar tests existentes

- [x] 5.1 Existing harness self-tests unchanged and green

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/sdlc/foreman.sh`; `scope.py implement` on the stored #1063 state admits `CONSTITUTION.md` and `.claude/rules/code-quality.md`
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/sdlc/AI-SDLC.md`: scope guard row (implement scope) and timeout row (watchdog)
- [x] 8.2 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1088`
- [x] 9.2 Only the final commit carries `Closes #1088`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1088] fix: admit planned files in implement scope; enforce worker timeout`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: the #1063 rerun passes the implement phase
- [x] 12.2 Rollback path confirmed (`git revert`)
- [ ] 12.3 Issue #1088 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive harness-implement-scope-timeout-1088`)

## Definition of Done

- [x] Issue linked to Use Case CU76
- [x] Specification written and reviewed (Gate 1)
- [x] Tests written first and observed failing (Gate 2)
- [x] New suites green; existing suites unaffected
- [x] Coverage at or above the JaCoCo ratchet floor (unaffected — no backend code)
- [x] Playwright E2E: n/a, no UI surface
- [x] Permanent documentation updated
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green (Gate 4)
- [ ] Merged, CD green, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
