> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1099 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/local-ai-worker-setup/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from `chore/1098_harness_fixes_1049_runs` (stacked on #1098)
- [x] 2.2 `chore/1099_gpt_oss_preset`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: argv list, stray bracket, header tokens, valid call, command key, second patch run, missing anchor
- [x] 3.2 `test_harmony_repair.py` and `test_patch_omlx.py` cover every scenario in the delta spec
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `local-ai/omlx/harmony_repair.py`: `repair_tool_call`
- [x] 4.2 `local-ai/omlx/patch_omlx.py`: harmony header fix and repair hook, `--check`, `--restore`
- [x] 4.3 `setup-omlx-codex.sh`: `PRESET` table, per-preset oMLX settings, catalog, profile and smoke test; runs `patch_omlx.py` for gpt-oss
- [x] 4.4 `local-ai/codex-local-instructions-gpt-oss.md`

## 5. Actualizar tests existentes

- [x] 5.1 n/a — no existing test covers the setup script

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/setup-omlx-codex.sh`; default preset rerun is a no-op
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 Coding smoke task passes in at least 7 of 8 runs with `--profile omlx-gptoss` (7/8)

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/README.md`: presets, oMLX patches, gpt-oss profile
- [x] 8.2 `local-ai/sdlc/AI-SDLC.md`: per-phase model row
- [x] 8.3 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1099`
- [x] 9.2 Only the final commit carries `Closes #1099`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1099] chore(local-ai): gpt-oss-20b preset for setup-omlx-codex.sh`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `PRESET=gpt-oss bash local-ai/setup-omlx-codex.sh` on a clean profile, then the coding smoke task
- [x] 12.2 Rollback path confirmed (`patch_omlx.py --restore`, `git revert`)
- [ ] 12.3 Issue #1099 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive gpt-oss-preset-1099`)

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
