> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists — #976 OPEN; CU76; bug/FRONTEND/DEVOPS/TEST/audit-2026-09
- [x] 1.2 Use Case CU76 exists; update quality docs at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 ADR — n/a unless lowering thresholds (not planned)
- [x] 1.6 Move Issue to IN PROGRESS — attempted; Project Items empty / gh lacks project write (noted in status)

## 2. Crear branch

- [x] 2.1 Update main after stockpile queue ahead (`origin/main` @ `68dc2cac`)
- [x] 2.2 `git checkout -b cursor/fix-976-vitest-branch-coverage-69d3`
- [x] 2.3 Record branch in `traceability.md`
- [x] 2.4 Copy `internal/openspec-976/` → `openspec/changes/fix-976-vitest-branch-coverage/` and validate

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Re-measure coverage on updated main; record summary in PR notes
- [x] 3.2 Threshold-guard test written for intended new floors first
- [x] 3.3 n/a backend integration tests
- [x] 3.4 Observed guard test fail against old config (2 failed)
- [x] 3.5 Map every `#### Scenario:` to coverage command, CI, or docs checklist

## 4. Implementación

- [x] 4.1 Raise `vitest.config.ts` thresholds with headroom under measured coverage
- [x] 4.2 Update inline comments (date + raise-only policy)
- [x] 4.3 Document root cause + raise-only policy in `.claude/rules/code-quality.md`
- [x] 4.4 Update FRONTEND-TESTING-GUIDE + TEST-COVERAGE-STRATEGY
- [x] 4.5 Static guard test for minimum floors
- [x] 4.6 n/a — coverage above intended floor; no extra product unit tests required

## 5. Actualizar tests existentes

- [x] 5.1 No brittle coverage-only product asserts weakened
- [x] 5.2 Guard test only; no other test updates required
- [x] 5.3 Config comments no longer claim undated 6% floor without policy

## 6. Ejecutar regresión

- [x] 6.1 Backend tests — n/a delta
- [x] 6.2 JaCoCo — n/a
- [x] 6.3 `cd frontend && npm test` (via vitest) + `npx vitest run --coverage` + lint + typecheck
- [x] 6.4 Bruno — n/a
- [x] 6.5 No unjustified skips

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no Playwright edits required
- [ ] 7.2 PR must still pass required workflows (pending CI)
- [x] 7.3 n/a responsive checks
- [x] 7.4 Document n/a UI surface; do not skip heavy CI

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update docs in proposal Documentation Impact
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` entry
- [x] 8.4 Archive — none expected
- [x] 8.5 No duplicated SSOT
- [ ] 8.6 `bash scripts/preflight.sh --fix` (frontend-focused as applicable)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Every commit ends with `Closes #976`
- [x] 9.3 No unrelated changes
- [x] 9.4 Record SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Push branch
- [x] 10.2 PR title `[#976] fix(frontend): ratchet Vitest coverage floors and document policy`
- [ ] 10.3 Wait for Frontend CI Vitest job + other required workflows
- [ ] 10.4 Merge only on heavy-CI gate exit 0 (coordinator)
- [x] 10.5 Record PR in `traceability.md` (#1171)

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 CD n/a behavior change
- [ ] 11.3 Record merge commit

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `npm run test:coverage` green locally; docs show floors; CI green on main
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Close Issue
- [ ] 12.4 `openspec archive fix-976-vitest-branch-coverage`

## Definition of Done

- [x] Issue linked to CU76 with Acceptance Criteria
- [x] Specification written (Gate 1 draft)
- [x] Tests/docs checks observed failing then green (Gate 2/3)
- [ ] Full required suite green
- [x] Coverage floors held (Vitest + JaCoCo where applicable)
- [x] Playwright n/a UI; CI still green
- [x] Permanent documentation updated
- [ ] Commits atomic and conventional
- [ ] PR created, CI green, review approved
- [ ] Merged, smoke passed, Issue closed
- [ ] `traceability.md` complete
