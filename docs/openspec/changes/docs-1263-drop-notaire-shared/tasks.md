> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1263 exists, labeled DOC, linked to CU76
- [x] 1.2 Use Case CU76 exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (ADR-025 already records retirement)
- [ ] 1.6 Move the Issue to IN PROGRESS (agent may hit `gh` 403 — Owner/coordinator)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/docs-1263-drop-notaire-shared-debd`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate: Constitution bare `notaire-shared` line must fail the retirement docs guard
- [x] 3.2 Add `CONSTITUTION.md` to `DOCS_AS_NON_LIVE`; observe fail (`CONSTITUTION.md:211`) — see `/opt/cursor/artifacts/1263-red-guard.log`
- [x] 3.3 Confirm every `#### Scenario:` maps to that guard method

## 4. Implementación

- [x] 4.1 Edit `CONSTITUTION.md` §5 step 4 to list only `backend-api`, `frontend`
- [x] 4.2 Confirm no other bare live reference remains in `CONSTITUTION.md`
- [x] 4.3 Re-run the retirement guard — green (`/opt/cursor/artifacts/1263-green-guard.log`)

## 5. Actualizar tests existentes

- [x] 5.1 No other tests weakened; only the docs list grew
- [x] 5.2 Existing retirement scenarios still pass
- [x] 5.3 No obsolete tests to remove

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [x] 6.4 HTTP/Bruno — n/a (no API surface)
- [x] 6.5 No `@Disabled` tests
- [x] 6.6 `python3 -m unittest discover -s workspace/tests -p 'test_notaire_shared_retired.py'`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [ ] 7.2 Required Playwright CI job on the PR may still run (workflow not path-scoped yet); do not weaken it
- [x] 7.3 n/a responsive check
- [x] 7.4 Recorded: no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `CONSTITUTION.md` updated
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` Changed entry
- [x] 8.4 Archive — n/a
- [x] 8.5 No duplicated permanent docs
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` as environment allows (docs/guard change)

## 9. Commits atómicos

- [ ] 9.1 Prefer: (1) OpenSpec + red guard, (2) Constitution + CHANGELOG + green, or one atomic docs commit if stacked poorly
- [ ] 9.2 Closing commit ends with `Closes #1263`
- [ ] 9.3 No secrets, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/docs-1263-drop-notaire-shared-debd`
- [ ] 10.2 Open draft PR `[#1263] docs(constitution): drop notaire-shared from Impact Analysis`
- [ ] 10.3 Wait for required workflows; coordinator runs `bash workspace/sdlc/check-heavy-ci.sh <pr>`
- [ ] 10.4 Gate 4 — Owner review/merge (agents do not merge)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm CI on `main` for the merge commit
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: retirement guard green on `main`; Issue #1263 closed via `Closes #`
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Confirm Issue closed
- [ ] 12.4 Archive: `openspec archive docs-1263-drop-notaire-shared` (from `docs/`)

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guard observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright n/a (no UI)
- [ ] Permanent documentation updated and consistent
- [ ] Commits Conventional + `Closes #1263`
- [ ] PR created; Owner merges after heavy CI
- [ ] Issue closed after merge
