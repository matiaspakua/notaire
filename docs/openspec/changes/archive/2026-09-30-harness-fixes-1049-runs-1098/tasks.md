> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1098 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/ai-sdlc-enforcement/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `chore/1098_harness_fixes_1049_runs`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: premature tick removed, prerequisite tick kept, bare opening fence, table row without pipe, recursive grep skips node_modules
- [x] 3.2 `test_ledger.py`, `test_md_repair.py`, `test_crawl_guard.py` cover every scenario in the delta spec
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `gate_spec` removes a leftover `specs/` under `skip_specs`
- [x] 4.2 `ledger.py untick-after`; `gate_spec` unticks groups 3-12 and logs `spec-repaired`
- [x] 4.3 `bin/md_repair.py`, run by `md_fix` before the adapter's lint fix
- [x] 4.4 `bin/crawl_guard.py`, `bin/shims/`, `zdot/`; `run_worker` sets `ZDOTDIR`
- [x] 4.5 Pending #1049 work committed: Qwen3-Coder setup and local prompt, triage search guidance and removal proofs, retry feedback placement

## 5. Actualizar tests existentes

- [x] 5.1 `test_triage_check.py`: removal and negated-search proofs accepted

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/sdlc/foreman.sh` and `bash -n local-ai/setup-omlx-codex.sh`
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/sdlc/AI-SDLC.md`: spec repairs, markdown repair, crawl guard
- [x] 8.2 `local-ai/README.md`: Qwen3-Coder-30B-A3B setup
- [x] 8.3 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1098`
- [x] 9.2 Only the final commit carries `Closes #1098`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1098] chore(ai-sdlc): harness fixes from the #1049 runs`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `gate_spec` replay on the stored #1049 spec repairs ticks, lint and leftover specs
- [x] 12.2 Rollback path confirmed (`git revert`)
- [ ] 12.3 Issue #1098 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive harness-fixes-1049-runs-1098`)

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
