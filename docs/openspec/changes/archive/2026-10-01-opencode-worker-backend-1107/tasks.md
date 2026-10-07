> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1107 exists, labeled `enhancement, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/*/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `feat/1107_opencode_worker_backend`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: opencode argv, opencode env isolation, codex argv unchanged, guards for both, last message extraction
- [x] 3.2 `test_worker.py` covers every scenario in the delta spec
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `bin/worker.py`: `command`, `environment`, `last_message`, `run`
- [x] 4.2 `local-ai/opencode/opencode.json`
- [x] 4.3 Adapter keys `backend.agent`, `backend.opencode_model`; `run_worker` calls `worker.py`

## 5. Actualizar tests existentes

- [x] 5.1 n/a — no existing test covers the setup script

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/setup-omlx-codex.sh`; default preset rerun is a no-op
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 A foreman run of a real issue with `AGENT=opencode` reaches its PR

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/README.md`: running the worker with OpenCode
- [x] 8.2 `local-ai/sdlc/AI-SDLC.md`: worker agents and OpenCode isolation
- [x] 8.3 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1107`
- [x] 9.2 Only the final commit carries `Closes #1107`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1107] feat(ai-sdlc): OpenCode worker backend`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `AGENT=opencode foreman.sh <n>` completes a real issue end to end
- [x] 12.2 Rollback path confirmed (`patch_omlx.py --restore`, `git revert`)
- [ ] 12.3 Issue #1107 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive opencode-worker-backend-1107`)

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
