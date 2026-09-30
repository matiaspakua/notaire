> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1105 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/*/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `chore/1105_followup_fixes`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: lost call detected, call present, final answer, no tool syntax, recovery echo shape
- [x] 3.2 `test_harmony_repair.py` and `test_patch_omlx.py` cover the recovery scenarios
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test or replay (see `design.md`)

## 4. Implementación

- [x] 4.1 `require_clean_branch`: name the add-and-amend command
- [x] 4.2 `harmony_repair.py`: `tool_call_lost`, `recovery_call`
- [x] 4.3 `patch_omlx.py`: `recover-lost-call`; rebuild from `harmony.py.orig`

## 5. Actualizar tests existentes

- [x] 5.1 n/a — no existing test covers the setup script

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/setup-omlx-codex.sh`; default preset rerun is a no-op
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 Real oMLX harmony parser: lost call gives the echo, good call and final answer unchanged

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/README.md`: lost-call recovery
- [x] 8.2 `local-ai/sdlc/AI-SDLC.md`: n/a — message wording only
- [x] 8.3 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1105`
- [x] 9.2 Only the final commit carries `Closes #1105`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1105] chore(ai-sdlc): land the #1049 follow-up fixes`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: the next autonomous issue run shows no empty turn after an EOS in a call header
- [x] 12.2 Rollback path confirmed (`patch_omlx.py --restore`, `git revert`)
- [ ] 12.3 Issue #1105 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive followup-fixes-1105`)

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
