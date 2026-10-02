> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1133 exists and links CU76
- [x] 1.2 Use Case CU76 exists (`docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md`)
- [x] 1.3 Acceptance Criteria defined in Issue #1133 (no delta spec — `skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no ADR (docs-only process documentation)
- [ ] 1.6 Issue #1133 labeled in-progress when permissions allow

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/docs-ci-merge-learnings-69d3` created (Cloud Agent naming per task brief)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 n/a — no delta spec scenarios (`skip_specs: true`); verification in design.md
- [ ] 3.2 n/a — no unit-testable application code
- [ ] 3.3 n/a — no integration-testable application code
- [x] 3.4 Verify docs contain heavy gate / rebase-first / CodeQL XOR (grep) after implementation
- [ ] 3.5 n/a — no spec scenarios to map

## 4. Implementación

- [x] 4.1 Extend `CI-MERGE-GATE.md` (rebase-first, docs-only Playwright, CodeQL link)
- [x] 4.2 Update fleet README / ENVIRONMENT-CHECKLIST / FLEET-ARCHITECTURE / CI-PREFLIGHT
- [x] 4.3 Document CodeQL advanced vs default in `208-devsecops/README.md`
- [x] 4.4 Harden `.claude/agents/cloud-foreman.md`
- [x] 4.5 Add this OpenSpec change folder (`skip_specs: true`) via `scripts/seed-openspec-change.sh`

## 5. Actualizar tests existentes

- [ ] 5.1 n/a — no existing tests reference these prose sections
- [ ] 5.2 n/a
- [ ] 5.3 n/a

## 6. Ejecutar regresión

- [ ] 6.1 n/a — no Java/TS code changed
- [ ] 6.2 n/a — no coverage-affecting code changed
- [ ] 6.3 n/a — no Java/TS code changed
- [ ] 6.4 n/a — no API surface changed
- [x] 6.5 No `@Disabled`/skipped tests introduced

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface (see design.md — Playwright Strategy)
- [ ] 7.2 n/a
- [ ] 7.3 n/a
- [x] 7.4 Recorded "n/a — no UI surface" in design.md; CI may still run Playwright on the tip

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Permanent docs under `304-ai-sdlc-cloud/`, `CI-PREFLIGHT.md`, `208-devsecops/` updated
- [ ] 8.2 n/a — no endpoints changed
- [ ] 8.3 n/a — `CHANGELOG.md` not updated; not user-visible
- [ ] 8.4 n/a — nothing to archive
- [x] 8.5 Confirmed no duplication — extend canonical docs + thin cross-links
- [x] 8.6 OpenSpec validators run (`openspec validate --strict` + `validate-sdlc-plan.sh`)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits for docs + OpenSpec
- [x] 9.2 Commit closing the issue ends with `Closes #1133`
- [x] 9.3 No secrets, no commented-out code, no product code, no `local-ai/`
- [x] 9.4 Commit SHAs recorded in `traceability.md` after commit

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed to origin
- [x] 10.2 PR #1138 open titled `docs(ai-sdlc): capture CI merge and CodeQL fleet learnings`
- [ ] 10.3 Wait for required workflows — `bash scripts/check-heavy-ci.sh 1138` (never light-only)
- [ ] 10.4 Gate 4 — CI green, review approved, no merge conflicts
- [x] 10.5 PR number recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only (never on light-CI-only green)
- [ ] 11.2 n/a — no image publish impact from docs
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: grep heavy gate / rebase-first / CodeQL XOR on merged `main`
- [x] 12.2 Rollback path confirmed (plain revert)
- [ ] 12.3 Close Issue #1133 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive docs-ci-merge-learnings`

## Definition of Done

- [x] Issue linked to CU76, with Acceptance Criteria
- [x] Specification written (Gate 1) — `skip_specs: true`, Acceptance Criteria in Issue #1133
- [x] Verification (grep + OpenSpec validators) in place of app tests (Gate 2 equivalent)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional with `Closes #1133`
- [ ] PR created, CI green (heavy), review approved (Gate 4)
- [ ] Merged, smoke verification recorded, Issue closed (Gate 5)
