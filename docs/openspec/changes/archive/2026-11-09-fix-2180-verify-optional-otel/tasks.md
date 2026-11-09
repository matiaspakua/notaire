> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2180 exists, linked to CU76
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance Criteria via design scenarios (`skip_specs: true` — harness only)
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 ADR — n/a
- [ ] 1.6 Comment on Issue with branch link

## 2. Crear branch

- [x] 2.1 Fetch/update `main`
- [x] 2.2 Branch `cursor/fix-2180-verify-optional-otel-ca49`
- [x] 2.3 Recorded in `traceability.md`
- [x] 2.4 `bash workspace/sdlc/validate-sdlc-plan.sh`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Add `test_verify_optional_venv.py` forced-fail scenario; observe RED
- [x] 3.2 Confirm scenario maps to the design Testing Strategy row

## 4. Implementación

- [x] 4.1 Degrade optional OTel bootstrap in `verify.sh` (WARN + rm half-venv + continue)
- [x] 4.2 Support `AISDLC_TEST_FORCE_VENV_FAIL=1` for the unit test
- [x] 4.3 Observe GREEN

## 5. Actualizar tests existentes

- [x] 5.1 No existing tests weakened; telemetry skip path unchanged
- [x] 5.2 Remove dead references if any

## 6. Ejecutar regresión

- [x] 6.1 Backend Java — n/a (kit-only)
- [x] 6.2 `python3 -m unittest discover -s framework/ai-sdlc/tests`
- [x] 6.3 `bash framework/ai-sdlc/verify.sh` (must exit 0 without ensurepip)
- [ ] 6.4 `bash workspace/sdlc/preflight.sh --full` when environment allows

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Update `framework/ai-sdlc/README.md` (optional venv degrade note)
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 CHANGELOG — n/a (release-please / kit-internal)
- [x] 8.4 Archive change in the PR

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits, small units
- [ ] 9.2 Final commit `Closes #2180`
- [ ] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch
- [ ] 10.2 Open PR `[#2180] fix(aisdlc): …`
- [ ] 10.3 Required checks
- [ ] 10.4 Gate 4 — CI green / review

## 11. Deploy

- [ ] 11.1 Owner/Foreman merges via PR — never push to `main`
- [ ] 11.2 Confirm process checks on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: module verify green on merge commit
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Issue #2180 closed via PR
- [ ] 12.4 OpenSpec archived

## Definition of Done

- [ ] Issue linked to CU76 with AC
- [ ] Gate 1 plan validated
- [ ] Failing test observed then green
- [ ] Implementation + module verify pass
- [ ] Permanent docs updated
- [ ] Conventional Commits with `Closes #2180`
- [ ] Merged via PR; Issue closed
