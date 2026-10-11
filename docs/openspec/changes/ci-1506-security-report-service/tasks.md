## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1506 exists and references CU78
- [x] 1.2 OpenSpec proposal, spec, design, tasks, and traceability written
- [x] 1.3 `openspec validate` and `validate-sdlc-plan.sh` pass

## 2. Crear branch

- [x] 2.1 Branch `cursor/security-report-service-81d6` from `origin/main`
- [ ] 2.2 Issue label in-progress — integration cannot add labels

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 `security/tests/test_security_gate_surface.py` written
- [x] 3.2 Observed red: security job still needs `build`; summary module and service missing
- [x] 3.3 Playwright not applicable

## 4. Implementación

- [x] 4.1 CI security job depends on `changes` and writes the job summary
- [x] 4.2 nginx service, Homer link, probe, Grafana panel, landing page
- [x] 4.3 `security/publish_summary.py`

## 5. Actualizar tests existentes

- [x] 5.1 Stack probe set includes `security-reports`

## 6. Ejecutar regresión

- [x] 6.1 Surface test, Grafana dashboard test, and security module tests green
- [x] 6.2 `curl` against the local report service returns 200, container healthy

## 7. Ejecutar Playwright

- [x] 7.1 Not applicable

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Monitoring, program, definition, CU78, changelog, module map

## 9. Commits atómicos

- [x] 9.1 Conventional Commit ending with `Closes #1506`
- [x] 9.2 Record the commit SHA

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open the PR
- [ ] 10.2 Record the PR number

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [ ] 11.2 No schema; the new container starts with the observability stack
- [ ] 11.3 Record the merge commit

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, port 8890 serves the landing page on a stack that includes the service
- [ ] 12.2 Revert is safe
- [ ] 12.3 Close the issue after smoke
- [ ] 12.4 Archive the change

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [x] Tests written first and observed failing (Gate 2)
- [x] Tests green
- [x] Coverage ratchet unchanged
- [x] Playwright not applicable
- [x] Permanent documentation updated
- [ ] Commits conventional and referencing the issue
- [ ] PR created and CI green (Gate 4)
- [ ] Merged and smoke tested (Gate 5)
- [ ] Traceability complete through release
