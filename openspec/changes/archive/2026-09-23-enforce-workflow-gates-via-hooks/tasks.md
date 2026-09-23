> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1027 exists, labeled (`TAREA`, `DEVOPS`, `in-progress`); no Use Case link — documented internal-tooling exception (precedent: #973)
- [x] 1.2 No Use Case applicable — n/a, documented in proposal.md
- [x] 1.3 Acceptance Criteria defined directly in Issue #1027 (no delta spec — `skip_specs: true`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — not an application-architecture change (see proposal.md — Architecture review)
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1027 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` (fetched `origin/main`)
- [x] 2.2 `git checkout -b chore/1027_enforce_workflow_gates_via_hooks origin/main`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 n/a — no delta spec scenarios (`skip_specs: true`); manual verification plan defined instead in design.md — Testing Strategy
- [ ] 3.2 n/a — no unit-testable application code
- [ ] 3.3 n/a — no integration-testable application code
- [x] 3.4 Manually exercised the hook behavior (see group 6 / design.md — Testing Strategy) since there is no automated test harness for Claude Code hooks in this repo
- [x] 3.5 n/a — no spec scenarios to map (documented exception, see 1.3)

## 4. Implementación

- [x] 4.1 Add `hooks.SessionStart` entry to `.claude/settings.json` running `openspec list` with a safe fallback
- [x] 4.2 Add `hooks.PreToolUse` entry matching `Bash` that detects and blocks `git push` targeting `main`/`master`
- [x] 4.3 Write the hook shell logic inline in `.claude/settings.json` (small enough not to warrant a separate script file)
- [x] 4.4 Create `.claude/rules/hooks.md` documenting both hooks, why they exist, and how they were tested
- [x] 4.5 Add a short "Claude Code hooks" pointer section to `CLAUDE.md` linking to `.claude/rules/hooks.md`

## 5. Actualizar tests existentes

- [ ] 5.1 n/a — no existing automated tests reference `.claude/settings.json`
- [ ] 5.2 n/a
- [ ] 5.3 n/a

## 6. Ejecutar regresión

- [x] 6.1 n/a — no Java/TS code changed; skipped `mvn test` (nothing to regress)
- [x] 6.2 n/a — no coverage-affecting code changed
- [x] 6.3 n/a — no Java/TS code changed
- [x] 6.4 n/a — no API surface changed
- [x] 6.5 No `@Disabled`/skipped tests introduced — n/a, no tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface (see design.md — Playwright Strategy)
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded "n/a — no UI surface" in design.md with reason

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Updated `CLAUDE.md` and added `.claude/rules/hooks.md` per proposal.md — Documentation Impact
- [x] 8.2 n/a — no endpoints changed, no OpenAPI/Swagger impact
- [x] 8.3 n/a — `CHANGELOG.md` not updated; not user-visible (internal dev tooling)
- [x] 8.4 n/a — nothing to archive
- [x] 8.5 Confirmed no duplication — `.claude/rules/hooks.md` links to CONSTITUTION.md rather than restating it
- [x] 8.6 `bash scripts/preflight.sh` run before push (see traceability.md for result)

## 9. Commits atómicos

- [x] 9.1 Committed in small, self-contained units, Conventional Commits format
- [x] 9.2 Commit(s) closing the issue end with `Closes #1027`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin chore/1027_enforce_workflow_gates_via_hooks`
- [ ] 10.2 Open PR titled `[#1027] chore: enforce workflow gates via Claude Code hooks`, referencing Issue #1027
- [ ] 10.3 Wait for required workflows to pass
- [ ] 10.4 Gate 4 — CI green, code review approved (left for human review per task instructions — PR not merged by this agent), no merge conflicts, docs complete
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via Pull Request only — left to human reviewer, not performed by this agent
- [ ] 11.2 n/a — no CD/image publish triggered by `.claude/` changes
- [ ] 11.3 Record merge commit in `traceability.md` once merged

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: open a fresh Claude Code session after merge, confirm SessionStart hook output appears (see design.md — Deployment Strategy)
- [ ] 12.2 Rollback path confirmed available (plain revert, see design.md — Rollback Strategy)
- [ ] 12.3 Close Issue #1027 once merged, referencing the PR (left to human/CI per task instructions)
- [ ] 12.4 Archive the change: `openspec archive enforce-workflow-gates-via-hooks` (after merge)

## Definition of Done

- [x] Issue linked; no Use Case applicable, documented exception recorded
- [x] Specification written and reviewed (Gate 1) — `skip_specs: true`, Acceptance Criteria in Issue #1027
- [x] Manual verification designed and performed in place of automated tests (Gate 2 equivalent — see design.md)
- [x] No application suite affected (n/a, no Java/TS changes)
- [x] Coverage unaffected (n/a)
- [x] Playwright n/a — no UI surface
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — pending human action
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5) — pending human action
- [x] `traceability.md` complete through PR-pending state
