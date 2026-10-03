> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1064 exists, labeled, linked to CU76
- [x] 1.2 Use Case CU76 exists and covers QA/testing infrastructure
- [x] 1.3 Acceptance Criteria defined as scenarios in
      `specs/cu-api-matrix-validation/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no ADR (docs + process tooling; no architecture change)
- [ ] 1.6 Issue #1064 labeled in-progress when permissions allow

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/docs-1064-cu-api-matrix-refresh-69d3` created
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate scenarios from delta spec (controller names, required bases,
      Bruno_Test format, #953 on MISSING, preflight wiring)
- [x] 3.2 Write `scripts/tests/test_validate_cu_api_matrix.py` for every scenario
- [x] 3.3 n/a — no Spring/H2 integration surface
- [x] 3.4 Run unittest and observe red on stale fixture / missing script
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one test in
      `traceability.md`

## 4. Implementación

- [x] 4.1 Implement `scripts/validate-cu-api-matrix.py` (discover controllers,
      required bases, Bruno_Test rules, #953 on MISSING)
- [x] 4.2 Refresh `CU-API-MATRIX.csv`: English Controllers; eight resources;
      normalize Bruno_Test; link #953 on MISSING
- [x] 4.3 Wire validator into `scripts/preflight.sh` (+ `--list` mapping)
- [x] 4.4 Make unittest green against refreshed repo matrix + fixtures
- [x] 4.5 Update TEST-PLAN / testing README / CHANGELOG (no Bruno fills)

## 5. Actualizar tests existentes

- [x] 5.1 No product tests reference Spanish controller names in the matrix
- [x] 5.2 n/a — no weakened assertions expected
- [x] 5.3 n/a — no obsolete product tests

## 6. Ejecutar regresión

- [x] 6.1 n/a — no Java product code changed (`mvn test` not required for this
      docs/tooling change)
- [x] 6.2 n/a — no coverage-affecting code
- [x] 6.3 n/a — no backend verify surface for this change
- [x] 6.4 n/a — Bruno fills out of scope (#953); matrix validator only
- [x] 6.5 No `@Disabled`/skipped tests introduced
- [x] 6.6 `python3 -m unittest discover -s scripts/tests` green
- [x] 6.7 `bash scripts/validate-sdlc-plan.sh docs-1064-cu-api-matrix-refresh`
- [x] 6.8 `python3 scripts/validate-cu-api-matrix.py` green on repo CSV

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface (see design.md — Playwright Strategy)
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded "n/a — no UI surface" in design.md and here

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU-API-MATRIX.csv, TEST-PLAN.md, testing README.md
- [x] 8.2 n/a — no endpoint contract change
- [x] 8.3 Update `CHANGELOG.md` `[Unreleased]`
- [x] 8.4 n/a — nothing to archive
- [x] 8.5 Confirm no duplication — matrix remains SSOT for CU↔API↔Bruno
- [x] 8.6 Run OpenSpec validators + targeted preflight/process script tests

## 9. Commits atómicos

- [x] 9.1 Conventional Commits (OpenSpec, then tests, then impl/docs as needed)
- [x] 9.2 Closing commit ends with `Closes #1064`
- [x] 9.3 No secrets; no `local-ai/`; no unrelated changes
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Push branch to origin
- [x] 10.2 Open draft PR via ManagePullRequest titled
      `[#1064] docs(test): refresh CU-API-MATRIX + CI validator`
- [ ] 10.3 Wait for required workflows (do not merge)
- [ ] 10.4 Gate 4 — leave draft; do not merge
- [x] 10.5 Record PR URL/number in `traceability.md` and
      `/workspace/implement-1064-status.md`

## 11. Deploy

- [ ] 11.1 n/a this turn — do not merge (coordinator/owner merges later)
- [ ] 11.2 n/a — no image publish for docs/tooling-only until merge
- [ ] 11.3 Merge/release rows stay pending in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Smoke: run matrix validator + unittest after push
- [x] 12.2 Rollback path remains `git revert` (design.md)
- [ ] 12.3 Issue closed via PR `Closes #1064` on merge (not this turn)
- [ ] 12.4 Archive OpenSpec change after merge

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Validator + process unittest green; matrix refreshed
- [ ] Coverage n/a (no Java product change)
- [ ] Playwright n/a — no UI surface
- [ ] Permanent documentation updated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] Draft PR created (Gate 4 pending human/CI); do not merge
- [ ] Merged/deployed/closed after owner merge (Gate 5 pending)
- [ ] `traceability.md` updated through PR + smoke evidence
