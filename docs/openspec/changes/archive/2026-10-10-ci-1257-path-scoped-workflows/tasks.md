> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1257 linked to CU76
- [x] 1.2 Use Case CU76 linked
- [x] 1.3 Acceptance Criteria as scenarios in delta specs
- [x] 1.4 Impact Analysis in `proposal.md`
- [x] 1.5 Copy this offline tree into `docs/openspec/changes/ci-1257-path-scoped-workflows/` after #1419
- [x] 1.6 `openspec validate ci-1257-path-scoped-workflows --strict`
- [x] 1.7 `bash workspace/sdlc/validate-sdlc-plan.sh`
- [ ] 1.8 Move Issue to IN PROGRESS (or PR-body sync if 403)

## 2. Crear branch

- [x] 2.1 Fetch `origin/main` (branched; #1419 still open — workflows non-overlapping)
- [x] 2.2 Branch `cursor/ci-1257-path-scoped-cf98`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Verification (TDD)

- [x] 3.1 Append `PathScopedCiInvariantsTest` (stockpile `stockpile-1257-invariant-tests.py`)
- [x] 3.2 Observe invariant tests **fail** before workflow edits
- [x] 3.3 Map each Scenario to a verification step

## 4. Implementación

- [x] 4.1 Apply `1257-patches/*.patch` (or copy patched YAML)
- [x] 4.2 Confirm aggregators accept `success|skipped`
- [x] 4.3 CONSTITUTION path-scoped bullet (§10/§13)
- [x] 4.4 CI-PREFLIGHT + REPO-SPLIT-PLAN P0.2 note
- [x] 4.5 CHANGELOG Unreleased

## 5. Actualizar tests existentes

- [x] 5.1 `python3 workspace/tests/test_ci_workflow_invariants.py` green
- [x] 5.2 Existing page-deploy / playwright invariants still green

## 6. Ejecutar regresión

- [ ] 6.1–6.5 n/a product backend logic (CI YAML only)

## 7. Ejecutar Playwright

- [ ] 7.1 Product Playwright still runs on this PR (workflows changed → `ci` filter true → full suite)
- [ ] 7.2 Post-merge optional: docs-only smoke PR

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CI-PREFLIGHT / CONSTITUTION / REPO-SPLIT-PLAN / CHANGELOG

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Closes #1257` `Refs #1197`
- [ ] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch
- [ ] 10.2 Open draft PR + agent sync table
- [ ] 10.3 Wait workflows
- [ ] 10.4 `bash workspace/sdlc/check-heavy-ci.sh <pr>` exit 0
- [ ] 10.5 Mark ready; serialize if peers contend

## 11. Deploy

- [ ] 11.1 Human/coordinator merges via PR
- [ ] 11.2 Confirm next docs-only PR is cheap

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Optional docs-only wall-clock ≤3 min evidence
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Issue auto-close via `Closes`
- [ ] 12.4 Archive OpenSpec change

## Definition of Done

- [ ] All gates passed
- [ ] Path-scoped invariants green
- [ ] Heavy CI green on this PR (`check-heavy-ci.sh`)
- [ ] Permanent docs updated (CI-PREFLIGHT, CONSTITUTION, REPO-SPLIT-PLAN, CHANGELOG)
- [ ] `Closes #1257` on the merge commit / PR
