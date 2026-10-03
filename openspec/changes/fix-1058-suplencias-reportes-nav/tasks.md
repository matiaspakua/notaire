> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case — #1058 OPEN; CU22/CU59/CU24/CU25/CU50/CU23; FRONTEND / CASO-DE-USO / audit-2026-09
- [x] 1.2 Use Case documentation exists — update permanent notes at implement for nav entry points
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (follow existing dashboard nav patterns)
- [x] 1.6 Move the Issue to IN PROGRESS — attempted; worker `gh` lacks label write (coordinator)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after stockpile queue ahead
- [x] 2.2 `git checkout -b cursor/fix-1058-suplencias-reportes-nav-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy draft from `internal/openspec-1058/` → `openspec/changes/fix-1058-suplencias-reportes-nav/` and validate

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases: nav hrefs, i18n keys, redirects, E2E nav discovery
- [x] 3.2 Write failing unit tests for sidebar nav including suplencias/reportes
- [x] 3.3 Write/adjust Playwright tests that fail until nav links exist
- [x] 3.4 Observe unit/E2E fails on pre-change tree (or unit-only red first)
- [x] 3.5 Map every `#### Scenario:` to a test

## 4. Implementación

- [x] 4.1 Add i18n `navigation.suplencias` / `navigation.reportes` (es + en)
- [x] 4.2 Add sidebar `navItems` (and optional dashboard home tiles)
- [x] 4.3 Diff admin vs canonical Items/Auditoría; merge unique UI into canonical
- [x] 4.4 Replace duplicate admin pages with redirects (or delete + next redirects)
- [x] 4.5 Ensure administración index does not revive duplicates; keep #1052 guard
- [x] 4.6 Add stable test ids on new nav links if needed for E2E

## 5. Actualizar tests existentes

- [x] 5.1 Retarget E2E URLs from `administracion/items|auditoria` to canonical
- [x] 5.2 Update TS-0070 to use sidebar navigation for Suplencias/Reportes
- [x] 5.3 Remove obsolete duplicate-page assumptions with stated reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unchanged expected (frontend-only change)
- [x] 6.2 JaCoCo — n/a delta
- [x] 6.3 `cd frontend && npm test && npm run lint && npm run typecheck` (+ build in preflight)
- [x] 6.4 Bruno — n/a
- [x] 6.5 No unjustified skips

## 7. Ejecutar Playwright

- [x] 7.1 Updated TS-0070 / TS-0017 / TS-0020 / related specs (nav discovery)
- [ ] 7.2 PR must pass `playwright-e2e.yml` via heavy CI
- [ ] 7.3 Check sidebar labels at 320 / 768 / 1024
- [x] 7.4 Serialize vs other Playwright-heavy PRs — **not n/a**

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update docs listed in proposal Documentation Impact
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` fix entry
- [x] 8.4 Archive only if a doc becomes obsolete
- [x] 8.5 No duplicated SSOT content
- [ ] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits, small units
- [ ] 9.2 Every commit ends with `Closes #1058`
- [ ] 9.3 No secrets / unrelated changes
- [ ] 9.4 Record SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Push branch
- [ ] 10.2 PR title `[#1058] fix(frontend): nav for Suplencias/Reportes; dedupe admin pages`
- [ ] 10.3 Wait for required workflows including Playwright
- [ ] 10.4 Merge only on heavy-CI gate exit 0
- [ ] 10.5 Record PR in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm frontend CD as applicable
- [ ] 11.3 Record merge commit / tag

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: sidebar → Suplencias/Reportes; Items/Auditoría single route; old admin URLs redirect
- [ ] 12.2 Rollback path available per design.md
- [ ] 12.3 Close Issue
- [ ] 12.4 `openspec archive fix-1058-suplencias-reportes-nav`

## Definition of Done

- [x] Issue linked to Use Cases with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green
- [ ] Coverage floors held
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated
- [ ] Commits atomic and conventional
- [ ] PR created, CI green, review approved
- [ ] Merged, smoke passed, Issue closed
- [ ] `traceability.md` complete
