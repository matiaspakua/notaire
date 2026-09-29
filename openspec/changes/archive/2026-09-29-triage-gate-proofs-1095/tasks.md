> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1095 exists, labeled `chore, DEVOPS`, linked to CU76
- [x] 1.2 Use Case CU76 — Quality Assurance and Testing Infrastructure
- [x] 1.3 Acceptance Criteria defined as delta spec scenarios (`specs/ai-sdlc-enforcement/spec.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural for the product — no ADR required
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Branch created from updated `origin/main`
- [x] 2.2 `chore/1095_triage_gate_proofs`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: derived value restored, underivable kept, search rejected, script accepted, docs+new test rejected, code+new test accepted
- [x] 3.2 `local-ai/sdlc/tests/test_triage_check.py` covers every scenario in the delta spec
- [x] 3.3 n/a — no integration tests apply
- [x] 3.4 Run them and observe them fail — `python3 -m unittest discover -s local-ai/sdlc/tests`
- [x] 3.5 Every `#### Scenario:` maps to a test (see `design.md`)

## 4. Implementación

- [x] 4.1 `bin/triage_check.py`: `restore`, `bad-proofs`, `kind-conflict`
- [x] 4.2 `seed_triage` keeps `$STATE/triage.seed.env`; `gate_triage` restores from it and logs `triage-repaired`
- [x] 4.3 `gate_triage` rejects search-command proofs and non-code `KIND` with new tests

## 5. Actualizar tests existentes

- [x] 5.1 Existing harness self-tests unchanged and green

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s local-ai/sdlc/tests` and `-s scripts/tests` green
- [x] 6.2 `bash -n local-ai/sdlc/foreman.sh`
- [x] 6.3 `bash scripts/preflight.sh` green on the branch
- [x] 6.4 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `local-ai/sdlc/AI-SDLC.md`: triage row — seed repair, proof and KIND rules
- [x] 8.2 `CHANGELOG.md`: n/a — not user visible

## 9. Commits atómicos

- [x] 9.1 One commit per concern, Conventional Commits, `Refs #1095`
- [x] 9.2 Only the final commit carries `Closes #1095`
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Branch pushed
- [ ] 10.2 PR opened titled `[#1095] chore(ai-sdlc): triage gate protects derived values and rejects non-proofs`
- [ ] 10.3 CI green
- [ ] 10.4 Gate 4 — human review and merge by the owner
- [ ] 10.5 PR recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merged via PR, never pushed to `main` directly
- [ ] 11.2 CD run on `main` green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `RECHECK=1` of `gate_triage` on the stored #1064 retry files reports the new errors
- [ ] 12.2 Rollback path confirmed (`git revert`)
- [ ] 12.3 Issue #1095 closed on merge
- [ ] 12.4 Change archived in this PR (`openspec archive triage-gate-proofs-1095`)

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
