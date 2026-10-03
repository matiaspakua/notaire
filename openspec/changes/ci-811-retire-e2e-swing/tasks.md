> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #811 exists, linked to CU76
- [x] 1.2 Use Case CU76 accurate for QA / CI integrity
- [x] 1.3 Acceptance Criteria as scenarios in delta spec
- [x] 1.4 Impact Analysis — hygiene, testing/e2e-swing, live docs
- [x] 1.5 ADR — n/a (ADR-012 already records retirement; optional #811 cite)
- [x] 1.6 Move Issue IN PROGRESS attempted (`gh` may 403 on labels)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/ci-811-retire-e2e-swing-69d3 origin/main`
- [x] 2.3 Record branch in traceability
- [x] 2.4 Validate: `bash scripts/validate-sdlc-plan.sh ci-811-retire-e2e-swing`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Assert `.github/workflows/e2e-swing.yml` is absent
- [x] 3.2 Assert no workflow YAML builds Swing (`-pl frontend-swing` /
      `deprecated-frontend-swing`)
- [x] 3.3 Observe fail on synthetic fixture workflow, then pass on tip
- [x] 3.4 Map every `#### Scenario:` to a test

## 4. Implementación

- [x] 4.1 Extend `scripts/test_dependabot_hygiene.py` with Swing E2E retirement guards
- [x] 4.2 Hard-deprecate `testing/e2e-swing/` (README + early-exit run script)
- [x] 4.3 Clean live docs (301-setup, 303-testing, DEVELOPMENT-PLAN) and related scripts
- [x] 4.4 Optional ADR-012 cite #811; update CU76 + CHANGELOG

## 5. Actualizar tests existentes

- [x] 5.1 Keep `test_repo_hygiene` requirements.txt assert satisfied
- [x] 5.2 Do not weaken existing Swing-absence asserts from #1046
- [x] 5.3 No obsolete test removals required beyond doc/script cleanup

## 6. Ejecutar regresión

- [x] 6.1 `python3 scripts/test_dependabot_hygiene.py` green
- [x] 6.2 `python3 -m unittest discover -s scripts/tests -p 'test_*hygiene*.py'` green
- [ ] 6.3 `bash scripts/preflight.sh` (or `--fix` subset appropriate for docs/CI)
- [ ] 6.4 Backend full verify n/a for this surface; heavy CI on PR
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded: no UI surface; Playwright not required for this change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CU76, 301-setup, 303-testing, DEVELOPMENT-PLAN, e2e-swing README
- [x] 8.2 OpenAPI n/a
- [x] 8.3 CHANGELOG `[Unreleased]` for #811
- [x] 8.4 Archive only if a live doc is fully superseded (prefer in-place hard-deprecate)
- [x] 8.5 No duplication of ADR-012 body
- [ ] 8.6 `bash scripts/preflight.sh --fix` / hygiene green

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Every commit ends with `Closes #811`
- [ ] 9.3 No secrets / unrelated changes
- [ ] 9.4 Record SHAs in traceability

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/ci-811-retire-e2e-swing-69d3`
- [ ] 10.2 Draft PR via ManagePullRequest; body includes `Closes #811`
- [ ] 10.3 Wait for required workflows; coordinator runs `check-heavy-ci.sh`
- [ ] 10.4 Gate 4 — CI green, review, docs complete (coordinator merges)
- [ ] 10.5 Record PR in traceability

## 11. Deploy

- [ ] 11.1 Merge via PR only (coordinator)
- [ ] 11.2 CD n/a for docs/hygiene-only (no image change expected)
- [ ] 11.3 Record merge in traceability after coordinator merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Confirm `e2e-swing.yml` absent on main; hygiene green
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Issue closed via `Closes #811` on merge
- [ ] 12.4 Archive OpenSpec change after merge

## Definition of Done

- [ ] Issue linked to CU76 with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Tests designed and observed failing then green (Gate 2)
- [ ] Hygiene green; preflight green for this surface
- [ ] Coverage n/a (Python hygiene)
- [ ] Playwright n/a — no UI surface
- [ ] Permanent documentation updated (Gate 3)
- [ ] Commits conventional with `Closes #811`
- [ ] Draft PR created; heavy CI for coordinator (Gate 4)
- [ ] Merge/smoke/close by coordinator (Gate 5)
- [ ] `traceability.md` updated through PR
