> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1153 exists and links CU76
- [x] 1.2 Use Case CU76 exists (`docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md`)
- [x] 1.3 Acceptance Criteria defined in Issue #1153 (no delta spec — `skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 n/a — no ADR (docs-only process documentation)
- [ ] 1.6 Issue #1153 labeled in-progress when permissions allow

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/docs-foreman-heavy-ci-69d3` created (Cloud Agent naming per task brief)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 n/a — no delta spec scenarios (`skip_specs: true`); verification in design.md
- [ ] 3.2 n/a — no unit-testable application code
- [ ] 3.3 n/a — no integration-testable application code
- [x] 3.4 Verify foreman contains heavy script / wake-up / serialize / gh-401 (grep) after implementation
- [ ] 3.5 n/a — no spec scenarios to map

## 4. Implementación

- [x] 4.1 Harden `.claude/agents/cloud-foreman.md` merge section (heavy script on current head)
- [x] 4.2 Document CI subscription as wake-up only (re-run heavy script)
- [x] 4.3 Document Playwright serialization + Dependabot draft floods
- [x] 4.4 Document coordinator merge when worker `gh` returns 401
- [x] 4.5 Add this OpenSpec change folder (`skip_specs: true`) for Process Checks

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

- [x] 8.1 Agent docs updated (`.claude/agents/cloud-foreman.md`); `CI-MERGE-GATE.md` already canonical
- [ ] 8.2 n/a — no endpoints changed
- [ ] 8.3 n/a — `CHANGELOG.md` not updated; not user-visible
- [ ] 8.4 n/a — nothing to archive
- [x] 8.5 Confirmed no duplication — foreman bullets cite `CI-MERGE-GATE.md`
- [x] 8.6 OpenSpec validators run (`openspec validate --strict` + `validate-sdlc-plan.sh`)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits for docs + OpenSpec
- [x] 9.2 Commit closing the issue ends with `Closes #1153`
- [x] 9.3 No secrets, no commented-out code, no product code, no `local-ai/`
- [x] 9.4 Commit SHAs recorded in `traceability.md` after commit

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed to origin
- [x] 10.2 PR #1151 open titled `docs(agents): harden cloud-foreman merge gate ops`
- [ ] 10.3 Wait for required workflows — `bash scripts/check-heavy-ci.sh 1151` (never light-only)
- [ ] 10.4 Gate 4 — CI green, review approved, no merge conflicts
- [x] 10.5 PR number recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only (never on light-CI-only green)
- [ ] 11.2 n/a — no image publish impact from docs
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: grep heavy script / wake-up / serialize / gh-401 on merged `main` foreman
- [x] 12.2 Rollback path confirmed (plain revert)
- [ ] 12.3 Close Issue #1153 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive docs-foreman-heavy-ci-ops`

## Definition of Done

- [x] Issue linked to CU76, with Acceptance Criteria
- [x] Specification written (Gate 1) — `skip_specs: true`, Acceptance Criteria in Issue #1153
- [x] Verification (grep + OpenSpec validators) in place of app tests (Gate 2 equivalent)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional with `Closes #1153`
- [ ] PR created, CI green (heavy), review approved (Gate 4)
- [ ] Merged, smoke verification recorded, Issue closed (Gate 5)
