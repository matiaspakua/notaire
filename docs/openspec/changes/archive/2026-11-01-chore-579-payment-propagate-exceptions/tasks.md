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

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Write the integration tests where applicable
- [x] 3.4 Run them and **observe them fail**
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Remove catch(Exception) from PaymentController
- [x] 4.2 Keep IllegalArgument / PendingBalance mapping

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api -Dtest=PaymentControllerTest`
- [x] 6.2 jacoco — covered by CI verify
- [x] 6.3 verify — CI
- [x] 6.4 Bruno — CI heavy path
- [x] 6.5 No `@Disabled` without justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 n/a — no UI surface; error-path unit only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG
- [x] 8.2 OpenAPI n/a
- [x] 8.3 CHANGELOG Unreleased
- [x] 8.4 n/a
- [x] 8.5 no duplication
- [x] 8.6 preflight on push

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Refs #579 (umbrella stays open)
- [ ] 9.3 No secrets
- [ ] 9.4 Record SHAs

## 10. Pull Request y validación CI

- [ ] 10.1 Archive OpenSpec
- [ ] 10.2 Push
- [ ] 10.3 Open PR
- [ ] 10.4 CI green
- [ ] 10.5 Gate 4
- [ ] 10.6 Record PR

## 11. Deploy

- [ ] 11.1 Merge via PR
- [ ] 11.2 CD
- [ ] 11.3 Record

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke
- [ ] 12.2 Rollback path
- [ ] 12.3 Keep #579 open; comment slice done
- [ ] 12.4 Record

## Definition of Done

- [ ] Payment slice of #579 shipped; issue remains open for other controllers
