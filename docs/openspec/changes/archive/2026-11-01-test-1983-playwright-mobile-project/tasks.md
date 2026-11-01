> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). Groups 1-12 mandatory.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b <type>/<issue-number>_<description>`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases
- [x] 3.2 Write unit tests for every scenario
- [x] 3.3 Integration where applicable
- [x] 3.4 Observe fail
- [x] 3.5 Scenario mapping

## 4. Implementación

- [x] 4.1 Fix playwright.config.ts mobile + chromium ignore
- [x] 4.2 Keep nonempty project guard green

## 5. Actualizar tests existentes

- [x] 5.1 Identify affected
- [x] 5.2 Update without weakening
- [x] 5.3 Remove obsolete

## 6. Ejecutar regresión

- [x] 6.1 n/a Java
- [x] 6.2 n/a
- [x] 6.3 n/a
- [x] 6.4 Playwright config list guard
- [x] 6.5 No disabled skips

## 7. Ejecutar Playwright

- [x] 7.1 Config is the Playwright surface
- [x] 7.2 `--list` proves projects nonempty
- [x] 7.3 n/a screens
- [x] 7.4 Full E2E in CI

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG
- [x] 8.2 n/a OpenAPI
- [x] 8.3 CHANGELOG
- [x] 8.4 n/a
- [x] 8.5 no duplication
- [x] 8.6 preflight on push

## 9. Commits atómicos

- [ ] 9.1 Conventional
- [ ] 9.2 Closes #1983
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs

## 10. Pull Request y validación CI

- [ ] 10.1 Archive
- [ ] 10.2 Push
- [ ] 10.3 PR
- [ ] 10.4 CI
- [ ] 10.5 Gate 4
- [ ] 10.6 Record

## 11. Deploy

- [ ] 11.1 Merge
- [ ] 11.2 CD
- [ ] 11.3 Record

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke
- [ ] 12.2 Rollback
- [ ] 12.3 Close issue
- [ ] 12.4 Record

## Definition of Done

- [ ] #1983 closed with real mobile project + nonempty guard
