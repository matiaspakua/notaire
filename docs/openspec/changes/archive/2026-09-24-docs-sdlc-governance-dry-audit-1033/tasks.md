> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1033 exists, labeled `docs`/`tech-debt`/`priority:low`, linked to CU76
- [x] 1.2 Use Case documentation (CU76) exists and is accurate — no update needed
- [x] 1.3 Acceptance Criteria defined in the Issue body
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — not an architectural change
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1033 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b docs/1033_sdlc_governance_dry_audit`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 n/a — docs-only change, `skip_specs: true`, no testable application behavior
- [x] 3.2 n/a
- [x] 3.3 n/a
- [x] 3.4 n/a
- [x] 3.5 n/a

## 4. Implementación

- [x] 4.1 Fix `AGENTS.md:88` coverage comment to state the enforced ratchet floor, not "≥ 80%"
- [x] 4.2 Fix `.claude/rules/ai-agent-workflow.md:336` ("Coverage ≥ 80% (JaCoCo).") to floor-vs-target wording
- [x] 4.3 Fix `.claude/rules/ai-agent-workflow.md:469` (PR checklist "Coverage ≥ 80%") to floor-vs-target wording
- [x] 4.4 Remove the `frontend-swing` line and catalog row from `.claude/skills/maven-build/SKILL.md`
- [x] 4.5 Add `hexagonal-arch` to the Architecture decisions row of `CONSTITUTION.md` §5's skill composition table
- [x] 4.6 Add `hexagonal-arch` to the matching row of `.claude/skills/README.md`'s composition table

## 5. Actualizar tests existentes

- [x] 5.1 n/a — no tests exist for prose content
- [x] 5.2 n/a
- [x] 5.3 n/a

## 6. Ejecutar regresión

- [x] 6.1 n/a — no Java/JS code touched, skipped
- [x] 6.2 n/a
- [x] 6.3 n/a
- [x] 6.4 n/a
- [x] 6.5 No skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Recorded here: n/a — no UI surface, this change touches only Markdown files

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Every permanent document in proposal.md's Documentation Impact table updated
- [ ] 8.2 n/a — no OpenAPI/endpoint change
- [ ] 8.3 n/a — not user-visible, no `CHANGELOG.md` entry needed (recorded in proposal.md)
- [ ] 8.4 n/a — nothing to archive
- [x] 8.5 Confirm no new duplication introduced by the fixes
- [ ] 8.6 `bash scripts/preflight.sh` run before push (markdown-lint + non-server-backed gates)

## 9. Commits atómicos

- [ ] 9.1 One commit per logical fix group (coverage wording; stale skill ref; skill catalog gap), Conventional Commits
- [ ] 9.2 Commit(s) closing #1033 end with `Closes #1033`
- [ ] 9.3 No secrets, no unrelated changes
- [ ] 9.4 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin docs/1033_sdlc_governance_dry_audit`
- [ ] 10.2 PR opened, titled `[#1033] docs: DRY/consistency audit of SDLC governance layer`
- [ ] 10.3 Required workflows pass
- [ ] 10.4 Gate 4 — CI green, no merge conflicts (left for human review/approval)
- [ ] 10.5 PR number recorded in `traceability.md`

## 11. Deploy

- [ ] 11.1 n/a — merges directly, no deploy/runtime surface; left to human merge decision (this task deliberately does not merge)
- [ ] 11.2 n/a
- [ ] 11.3 n/a until merged by a human reviewer

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 n/a — no runtime surface; "smoke test" is a read-through of the rendered Markdown
- [ ] 12.2 n/a — rollback is a plain `git revert`, documented in design.md
- [ ] 12.3 Issue #1033 closed on merge, referencing the PR (left for human merge)
- [ ] 12.4 `openspec archive docs-sdlc-governance-dry-audit-1033` after merge

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests n/a — docs-only, `skip_specs: true` (Gate 2)
- [x] Full suite n/a — no code touched
- [x] Coverage n/a — no code touched
- [x] Playwright n/a — no UI surface
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, Issue closed (Gate 5 — left for human merge per workflow)
- [ ] `traceability.md` complete from Issue through PR
