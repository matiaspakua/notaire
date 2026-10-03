> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1148 exists, linked to CU76
- [x] 1.2 Use Case CU76 accurate for CI reliability
- [x] 1.3 Acceptance Criteria as scenarios in delta spec
- [x] 1.4 Impact Analysis — workflows only
- [x] 1.5 ADR — n/a
- [ ] 1.6 Move Issue IN PROGRESS at implement

## 2. Crear branch

- [ ] 2.1 Pull main after #1147 merges
- [ ] 2.2 `git checkout -b cursor/ci-cancel-in-progress-69d3` (or rebase prep branch)
- [ ] 2.3 Record branch in traceability
- [ ] 2.4 Validate: `bash scripts/validate-sdlc-plan.sh ci-cancel-in-progress-1148`

## 3. Gate 2 — Escribir tests

- [ ] 3.1 Failing assert that ci.yml / playwright-e2e.yml have cancel-in-progress true
- [ ] 3.2 Assert deploy-github-page stays false
- [ ] 3.3 Observe fail on current main (or red→green on this branch)
- [ ] 3.4 Map scenarios to tests

## 4. Implementación

- [ ] 4.1 Flip `cancel-in-progress: true` on ci.yml
- [ ] 4.2 Flip on playwright-e2e.yml
- [ ] 4.3 Leave deploy-github-page.yml false
- [ ] 4.4 Doc note in CI-MERGE-GATE.md

## 5. Actualizar tests existentes

- [ ] 5.1 Update any workflow-config unit tests if present

## 6. Ejecutar regresión

- [ ] 6.1 Unit / config tests green
- [ ] 6.2 Backend verify not required for YAML-only (still run heavy gate on PR)

## 7. Ejecutar Playwright

- [ ] 7.1 No new E2E; existing Playwright suite must pass on PR (heavy gate)

## 8. Gate 3 — Documentación permanente

- [ ] 8.1 CI-MERGE-GATE.md concurrency note
- [ ] 8.2 CHANGELOG n/a

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits + `Closes #1148`

## 10. Pull Request y validación CI

- [ ] 10.1 ManagePullRequest draft → ready after heavy green
- [ ] 10.2 `bash scripts/check-heavy-ci.sh <pr>` exit 0

## 11. Deploy

- [ ] 11.1 Merge to main; workflows active on next push

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Optional: observe superseded cancel on a follow-up push
- [ ] 12.2 Issue closed via `Closes #1148`

## Definition of Done

- [ ] All three AC scenarios evidenced by unit assert
- [ ] Heavy CI gate exit 0
- [ ] CI-MERGE-GATE.md updated
- [ ] Issue #1148 closed via merge
