> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1035 exists, labeled `TEST`/`REFACTOR`, linked to CU76
- [x] 1.2 Use Case documentation (CU76) exists and is accurate — no update needed
- [x] 1.3 Acceptance Criteria defined in the Issue body
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — not an architectural change
- [x] 1.6 Issue moved to IN PROGRESS

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b test/1035_bruno_suite_english_idempotent`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Baseline run on a reused DB recorded: 21/152 requests failing (non-idempotent fixtures)
- [x] 3.2 Failure causes catalogued in `design.md` Context

## 4. Implementación

- [ ] 4.1 Rename folders and request files to English; renumber `concepts`
- [ ] 4.2 Translate request/test names, descriptions, free-text data; rename variables
- [ ] 4.3 Rename environment `Developmen` → `Development`
- [ ] 4.4 Fix `existingPersonId`, `dueDays`, budget `?status=` query
- [ ] 4.5 Unique per-run data + teardown in every folder; no seed mutation
- [ ] 4.6 `Traceability:` line (CU + RF) in every request description
- [ ] 4.7 Remove / ignore stray report artifacts in `api-test/`

## 5. Actualizar tests existentes

- [ ] 5.1 Assertions kept or strengthened; none removed

## 6. Ejecutar regresión

- [ ] 6.1 Full Bruno suite run 1 green
- [ ] 6.2 Full Bruno suite run 2 green (idempotent)
- [ ] 6.3 No fixture rows leaked (row counts before/after)
- [ ] 6.4 No skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Every document in proposal.md's Documentation Impact table updated
- [ ] 8.2 `CHANGELOG.md` entry
- [ ] 8.3 `bash scripts/preflight.sh` run before push

## 9. Commits atómicos

- [ ] 9.1 One commit per logical step, Conventional Commits
- [ ] 9.2 Closing commit ends with `Closes #1035`
- [ ] 9.3 Commit SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin test/1035_bruno_suite_english_idempotent`
- [ ] 10.2 PR opened, titled `[#1035] test: translate Bruno suite to English and make it idempotent`
- [ ] 10.3 Mergeability verified (`gh pr view --json mergeable,mergeStateStatus`)

## 11. Deploy

- [x] 11.1 n/a — test suite only

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 CI "API Tests (Bruno)" job green
- [ ] 12.2 Issue #1035 closed on merge
- [ ] 12.3 `openspec archive bruno-suite-english-idempotent-1035` after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [ ] Suite green twice in a row
- [ ] Permanent documentation updated
- [ ] Commits atomic and conventional
- [ ] PR created, CI green
- [ ] `traceability.md` complete from Issue through PR
