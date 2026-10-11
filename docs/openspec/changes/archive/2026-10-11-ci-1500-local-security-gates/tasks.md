## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1500 exists and references CU78
- [x] 1.2 Use Case CU78 updated with the catalog pointer
- [x] 1.3 OpenSpec proposal, spec, design, tasks, and traceability written
- [x] 1.4 `openspec validate ci-1500-local-security-gates --strict` and `validate-sdlc-plan.sh` pass

## 2. Crear branch

- [x] 2.1 Branch `cursor/local-security-sdlc-81d6` from `origin/main`
- [x] 2.2 Issue moved to in progress — not applied; the integration cannot add labels (see Exceptions)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `security/tests/test_security_cases.py` written
- [x] 3.2 Observed red: missing `security/run_gates.py` and missing pipeline wiring
- [x] 3.3 No product UI, so no Playwright test

## 4. Implementación

- [x] 4.1 Catalog, runner, Semgrep rules, DAST baseline, tool installer
- [x] 4.2 `run_pipeline.sh` and `preflight.sh` call the runner
- [x] 4.3 CI security job runs `--profile ci` and uploads the report
- [x] 4.4 AGENTS, Constitution, OpenSpec agent, and fleet item 10 name the gate

## 5. Actualizar tests existentes

- [x] 5.1 #307 policy test still passes with the added local tool keys
- [x] 5.2 `security/verify.sh` runs the new test module

## 6. Ejecutar regresión

- [x] 6.1 `python3 security/tests/test_security_cases.py` green, including real Semgrep
- [x] 6.2 `bash security/verify.sh` green
- [x] 6.3 `python3 security/run_gates.py --profile local --skip-dast` green (Trivy 0.75.0)

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable — no product UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Program, toolchain, monitoring, CU78, changelog, devsecops note
- [x] 8.2 Constitution, AGENTS, agent card, workflow rule, OpenSpec agent

## 9. Commits atómicos

- [x] 9.1 Conventional Commit
- [x] 9.2 Commit message ends with `Closes #1500`
- [x] 9.3 Record the commit SHA in traceability

## 10. Pull Request y validación CI

- [x] 10.1 Push the branch
- [x] 10.2 Open the PR referencing #1500 and CU78 — #1501
- [ ] 10.3 Required workflows run
- [x] 10.4 Record the PR number

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [ ] 11.2 No image publish required; smoke is the catalog runner
- [ ] 11.3 Record the merge commit

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, the catalog runner exits 0 on `main`
- [ ] 12.2 Revert is safe (no schema)
- [ ] 12.3 Close issue #1500 after smoke
- [ ] 12.4 Archive the change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Security module tests green
- [x] Coverage ratchet unchanged — no Java edit
- [x] Playwright not applicable
- [x] Permanent documentation updated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
