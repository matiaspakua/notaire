> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1083 exists, labeled `chore, DEVOPS, ci, audit-2026-09`
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/ai-sdlc-enforcement/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `chore/1083_apply_ai_sdlc_audit_fixes`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `local-ai/sdlc/tests/test_envfile.py`, `test_static_checks.py`, `test_review_check.py`, `test_metrics.py` written and failing
- [x] 3.2 `scripts/tests/test_pr_checks.py` written and failing
- [x] 3.3 n/a — no backend integration tests apply
- [x] 3.4 Each scenario in the delta spec maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `bin/envfile.py`; `kv()` and the renderer use it
- [x] 4.2 `bin/static_checks.py` wired into the red gate
- [x] 4.3 `bin/review_check.py` and `foreman.sh <n> check`
- [x] 4.4 `metrics.jsonl` from `gate_log`; `PROFILE_<PHASE>`; bash ≥ 4 check
- [x] 4.5 `scripts/check-commit-messages.sh`, `check-tdd-evidence.sh`, `check-sdlc-exception.sh`, `check-agent-rules.sh`
- [x] 4.6 `validate-sdlc-plan.sh` fails on a missing `schema:` line
- [x] 4.7 `sdlc-process.yml` workflow and `preflight.sh` mirror for the new checks
- [x] 4.8 Retire `e2e-swing.yml`, `.claude/agents/issue-loop.md`, SpecKit job/script/folder

## 5. Actualizar tests existentes

- [x] 5.1 No existing test covers the changed scripts; the SpecKit validator has no tests to remove

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash scripts/validate-sdlc-plan.sh` green
- [x] 6.3 `bash scripts/preflight.sh --fast` green on the branch
- [x] 6.4 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `CONSTITUTION.md`: Roles section, Swing text, §12 label, tooling map
- [x] 8.2 `.claude/rules/ai-agent-workflow.md` rewritten as a pointer
- [x] 8.3 `docs/300-development/CI-PREFLIGHT.md`, `local-ai/sdlc/AI-SDLC.md`, `.claude/skills/README.md`
- [x] 8.4 `local-ai/AUDIT.md` status per finding; open items listed in its §7
- [x] 8.5 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1083`
- [x] 9.2 Only the final commit carries `Closes #1083`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1083] chore: apply AI SDLC audit fixes`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: the next PR runs the new checks; `foreman.sh 1063` resumes and reads its quoted `TEST_CMD`
- [x] 12.2 Rollback path confirmed (`git revert`)
- [ ] 12.3 Issue #1083 closed on merge
- [x] 12.4 Change archived in this PR (`openspec archive apply-ai-sdlc-audit-fixes`)

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
