> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1114 exists and links this fleet setup
- [x] 1.2 No Use Case applicable — documented exception in proposal.md
- [x] 1.3 Acceptance Criteria defined in Issue #1114 (no delta spec — `skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — not an application-architecture change
- [ ] 1.6 Issue #1114 labeled in-progress when permissions allow

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 Branch `cursor/ai-sdlc-cloud-fleet-6890` created (Cloud Agent naming)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 n/a — no delta spec scenarios (`skip_specs: true`); verification plan in design.md
- [ ] 3.2 n/a — no unit-testable application code
- [ ] 3.3 n/a — no integration-testable application code
- [x] 3.4 Run VALIDATION-PLAN steps A/C/D (artifact presence, script --list, manifest coherence)
- [ ] 3.5 n/a — no spec scenarios to map

## 4. Implementación

- [x] 4.1 Add `docs/300-development/304-ai-sdlc-cloud/` architecture, manifest, env checklist, validation plan
- [x] 4.2 Add `.claude/agents/` cloud foreman + specialist definitions
- [x] 4.3 Index fleet in `AGENTS.md` and docs navigation; exclude `local-ai/` from cloud path
- [x] 4.4 Add this OpenSpec change folder so `sdlc-process.yml` can pass

## 5. Actualizar tests existentes

- [ ] 5.1 n/a — no existing tests reference the cloud fleet docs
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
- [x] 7.4 Recorded "n/a — no UI surface" in design.md

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Permanent docs under `docs/300-development/304-ai-sdlc-cloud/` and `AGENTS.md` updated
- [ ] 8.2 n/a — no endpoints changed
- [ ] 8.3 n/a — `CHANGELOG.md` not updated; not user-visible
- [ ] 8.4 n/a — nothing to archive
- [x] 8.5 Confirmed no duplication — architecture cites CONSTITUTION.md instead of restating it
- [ ] 8.6 `bash scripts/preflight.sh` before final push when toolchain allows

## 9. Commits atómicos

- [x] 9.1 Conventional Commits for fleet docs/agents
- [x] 9.2 Commit closing the issue ends with `Closes #1114`
- [x] 9.3 No secrets, no commented-out code, no unrelated product changes
- [ ] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Branch pushed to origin
- [x] 10.2 PR #1111 open
- [ ] 10.3 Wait for required workflows to pass
- [ ] 10.4 Gate 4 — CI green, review approved, no merge conflicts
- [x] 10.5 PR number recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 n/a — no image publish impact from docs/agent-config
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: VALIDATION-PLAN GO criteria when Cloud env toolchain is ready
- [x] 12.2 Rollback path confirmed (plain revert)
- [ ] 12.3 Close Issue #1114 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive cursor-cloud-ai-sdlc-fleet`

## Definition of Done

- [x] Issue linked; no Use Case applicable, documented exception recorded
- [x] Specification written (Gate 1) — `skip_specs: true`, Acceptance Criteria in Issue #1114
- [x] Verification performed in place of automated application tests (Gate 2 equivalent)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke validation recorded, Issue closed (Gate 5)
