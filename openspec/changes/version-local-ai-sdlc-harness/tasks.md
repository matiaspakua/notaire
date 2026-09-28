> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1074 exists, labeled (`chore`, `DEVOPS`, `in-progress`)
- [x] 1.2 No Use Case applicable — documented exception in proposal.md
- [x] 1.3 Acceptance Criteria defined in Issue #1074 (no delta spec — `skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — not an application-architecture change
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 Fetched `origin/main`
- [x] 2.2 `git worktree add -b chore/1074_local_ai_sdlc_harness ../notaire-harness origin/main`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 n/a — no delta spec scenarios (`skip_specs: true`); verification plan in design.md
- [ ] 3.2 n/a — no unit-testable application code
- [ ] 3.3 n/a — no integration-testable application code
- [ ] 3.4 Run `bash -n` on every script and `py_compile` on `bin/*.py`
- [ ] 3.5 n/a — no spec scenarios to map

## 4. Implementación

- [ ] 4.1 Add `local-ai/` (model stack scripts, README) without `.serena/` or caches
- [ ] 4.2 Add `local-ai/sdlc/` (foreman, prompts, githooks, bin, templates, AI-SDLC.md)
- [ ] 4.3 Confirm no secrets or `.env` in the added files

## 5. Actualizar tests existentes

- [ ] 5.1 n/a — no existing tests reference `local-ai/`
- [ ] 5.2 n/a
- [ ] 5.3 n/a

## 6. Ejecutar regresión

- [ ] 6.1 n/a — no Java/TS code changed
- [ ] 6.2 n/a — no coverage-affecting code changed
- [ ] 6.3 n/a — no Java/TS code changed
- [ ] 6.4 n/a — no API surface changed
- [ ] 6.5 No `@Disabled`/skipped tests introduced

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface (see design.md — Playwright Strategy)
- [ ] 7.2 n/a
- [ ] 7.3 n/a
- [ ] 7.4 Recorded "n/a — no UI surface" in design.md

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `local-ai/sdlc/AI-SDLC.md` and `local-ai/README.md` added per proposal.md — Documentation Impact
- [ ] 8.2 n/a — no endpoints changed
- [ ] 8.3 n/a — `CHANGELOG.md` not updated; not user-visible (internal dev tooling)
- [ ] 8.4 n/a — nothing to archive
- [ ] 8.5 Confirmed no duplication — AI-SDLC.md links to CONSTITUTION.md instead of restating it
- [ ] 8.6 `bash scripts/preflight.sh` run before push

## 9. Commits atómicos

- [ ] 9.1 Committed in small, self-contained units, Conventional Commits format
- [ ] 9.2 Commit closing the issue ends with `Closes #1074`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin chore/1074_local_ai_sdlc_harness`
- [ ] 10.2 Open PR titled `[#1074] chore(local-ai): version the local-AI SDLC harness`
- [ ] 10.3 Wait for required workflows to pass
- [ ] 10.4 Gate 4 — CI green, review approved, no merge conflicts
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via Pull Request only
- [ ] 11.2 n/a — no image publish impact from `local-ai/`
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `local-ai/sdlc/foreman.sh <issue>` starts from a fresh clone of main
- [ ] 12.2 Rollback path confirmed (plain revert)
- [ ] 12.3 Close Issue #1074 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive version-local-ai-sdlc-harness` (after merge)

## Definition of Done

- [x] Issue linked; no Use Case applicable, documented exception recorded
- [x] Specification written (Gate 1) — `skip_specs: true`, Acceptance Criteria in Issue #1074
- [ ] Verification performed in place of automated tests (Gate 2 equivalent)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke test passed, Issue closed (Gate 5)
