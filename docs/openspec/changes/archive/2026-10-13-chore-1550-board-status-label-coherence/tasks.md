> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — 403 for integration token; sync via PR `Closes #1550`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable — n/a (no Java API)
- [x] 3.4 Run them and **observe them fail** — module load / missing docs guards first
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Implement `workspace/sdlc/check-board-status-label-coherence.py` (evaluate + live scopes)
- [x] 4.2 Wire Process Checks (`sdlc-process.yml`) and `preflight.sh`
- [x] 4.3 Update CONSTITUTION, agent card, AI SDLC Cloud docs, `docs/github/README.md`
- [x] 4.4 CHANGELOG Unreleased note

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions — n/a (new module)
- [x] 5.3 Remove tests made genuinely obsolete — none

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java); ran `python3 -m unittest …test_board_status_label_coherence`
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API surface)
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy
- [x] 7.2 `cd testing/e2e && npx playwright test` — n/a — no UI surface
- [x] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a
- [x] 7.4 If the change has no UI surface, record "n/a — no UI surface" here with the reason

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations if endpoints changed — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — process subset validated via unittest + plan validate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin <branch-name>`
- [ ] 10.2 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only after heavy-CI gate
- [ ] 11.2 Confirm deploy / docs site as applicable

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke / process check green on main
- [ ] 12.2 Close issue (via `Closes #1550`)
- [ ] 12.3 Board Status Done; drop WIP labels
- [ ] 12.4 Archive OpenSpec change

## Definition of Done

- [ ] All mandatory groups addressed
- [ ] Board coherence checker green on fixtures; skips clearly without token
- [ ] Process Checks + preflight wired
- [ ] Permanent docs + CHANGELOG updated
