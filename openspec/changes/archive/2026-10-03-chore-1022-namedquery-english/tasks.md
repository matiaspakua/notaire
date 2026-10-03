> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists — #1022 OPEN; DOC / REFACTOR / priority:low
- [x] 1.2 Use Case — **exception**: none (same technical/internal-quality exception as epic #973)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (cosmetic identifier rename; no architectural decision)
- [x] 1.6 Move Issue to IN PROGRESS — attempted; ACL 403 ignored

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/chore-1022-namedquery-english-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Validate: `bash scripts/validate-sdlc-plan.sh chore-1022-namedquery-english` — passed

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate: Spanish prefixes, Persona mismatch, Folio/Item/Person Spanish tails
- [x] 3.2 Write `NamedQueryEnglishNamesHygieneTest` covering every scenario
- [x] 3.3 n/a integration tests (no API/DB behavior change)
- [x] 3.4 Run hygiene test and **observe FAIL** (3 failures) before renames
- [x] 3.5 Confirm every `#### Scenario:` maps to at least one assert

## 4. Implementación

- [x] 4.1 Rename all Spanish `@NamedQuery(name=…)` in `business/*.java` per rename map
- [x] 4.2 Englishize Folio/Item/Person method tails
- [x] 4.3 Update all `createNamedQuery` + Mockito stubs (main + test), including Persona→Person
- [x] 4.4 Hygiene test green; residual Spanish NamedQuery prefix count = 0
- [x] 4.5 Spring Data collision fix: Englishize JPQL named params on colliding queries; `User.findByPersonId`

## 5. Actualizar tests existentes

- [x] 5.1 Update JPA unit mocks that stub Spanish NamedQuery names
- [x] 5.2 Do not weaken assertions — only rename expected query name strings / params
- [x] 5.3 Remove obsolete assumptions — n/a

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — 1941 tests, 0 failures
- [x] 6.2 `mvn verify -pl backend-api -DskipTests` — BUILD SUCCESS (tests already green)
- [x] 6.3 Bruno/HTTP — n/a (no API change)
- [x] 6.4 No `@Disabled` without justification
- [x] 6.5 Preflight optional — Java string renames only

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 Record "n/a — no UI surface" reason
- [x] 7.3 n/a viewports
- [x] 7.4 CI Playwright job still expected green (no frontend diff)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG chore note under `[Unreleased]`
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 No business Use Case update (exception)
- [x] 8.4 Archive — n/a
- [x] 8.5 No duplicated info
- [x] 8.6 Preflight optional if only Java string renames

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Message ends with `Closes #1022`
- [x] 9.3 No secrets / unrelated changes
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/chore-1022-namedquery-english-69d3`
- [x] 10.2 Draft PR [#1187](https://github.com/matiaspakua/notaire/pull/1187) `[#1022] chore(jpa): English NamedQuery name strings` with `Closes #1022` + rename-map checklist
- [ ] 10.3 Wait for CI — coordinator merges after `bash scripts/check-heavy-ci.sh 1187` exit 0
- [x] 10.4 Do NOT merge from this agent
- [x] 10.5 Record PR URL in `traceability.md` + `/workspace/implement-1022-status.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only (coordinator)
- [ ] 11.2 CD as applicable
- [ ] 11.3 Record merge/release in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: hygiene residual count 0; person/budget NamedQuery paths via unit suite
- [ ] 12.2 Rollback = revert PR (safe)
- [ ] 12.3 Close Issue via PR `Closes #1022`
- [ ] 12.4 Archive: `openspec archive chore-1022-namedquery-english`

## Definition of Done

- [x] Issue linked (UC exception documented like #973)
- [x] Specification written (Gate 1)
- [x] Tests written first, observed failing (Gate 2)
- [x] Full suite green (1941 tests)
- [x] Coverage at or above JaCoCo ratchet floor (verify BUILD SUCCESS)
- [x] Playwright n/a (no UI)
- [x] CHANGELOG updated
- [ ] Commits conventional with `Closes #1022`
- [ ] Draft PR created; coordinator owns merge after heavy CI
- [ ] `traceability.md` / status file updated
