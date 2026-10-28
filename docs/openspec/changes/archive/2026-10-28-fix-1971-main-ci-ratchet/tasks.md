> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1971 exists and is linked to CU-76 (labels listed in the body; token gets 403 on labels)
- [x] 1.2 Use Case CU-76 exists
- [x] 1.3 Acceptance criteria are in the Issue and the design testing table (`skip_specs: true`)
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR
- [x] 1.6 Issue claimed in its body (label edits return 403)

## 2. Crear branch

- [x] 2.1 Branched from `origin/main` at `b11667c91`
- [x] 2.2 `fix/1971_main_ci_ratchet_regressions`
- [x] 2.3 Recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: defensive copy, unmodifiable, null kept; locale; null message; create defaults; update keeps stored flags
- [x] 3.2 Unit tests written (`CatalogItemsRequestTest`, `IllegalArgumentResponsesTest`, `DocumentTypeControllerTest`, `SecurityAndCorsConfigTest`)
- [x] 3.3 Integration tests: n/a — no endpoint change
- [x] 3.4 Observed failing first (test compilation failed on the new constructor; ratchets failed at 3173 / 1187)
- [x] 3.5 Every scenario in design.md maps to a test or CI gate

## 4. Implementación

- [x] 4.1 `{}` → `{ }` record bodies (Checkstyle 3173 → 3141)
- [x] 4.2 SpotBugs fixes in `BudgetController`, `SecurityAndCorsConfig`, `ConceptController`, `DocumentTypeController`, `IllegalArgumentResponses` (1187 → 1180)
- [x] 4.3 `npm run openapi:types` and `npm run openapi:types:check` pass
- [x] 4.4 `npm audit fix --package-lock-only` (dev-only `zod`)

## 5. Actualizar tests existentes

- [x] 5.1 `SecurityAndCorsConfigTest` builds the config with the JWT filter only
- [x] 5.2 No assertion weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 Targeted unit tests green locally; full suite runs in CI
- [x] 6.2 Coverage ratchet runs in CI (Coverage Gate)
- [x] 6.3 Checkstyle 3141 ≤ 3157 and SpotBugs 1180 ≤ 1183 locally
- [x] 6.4 Bruno suite: n/a — no endpoint change
- [x] 6.5 No `@Disabled` or skipped tests

## 7. Ejecutar Playwright

- [x] 7.1 No E2E spec change
- [x] 7.2 Playwright CI runs on the PR
- [x] 7.3 n/a — no screen changed
- [x] 7.4 n/a — no UI surface (compile-time types only)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `CHANGELOG.md` updated
- [x] 8.2 OpenAPI unchanged; generated types now match it
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` entry
- [x] 8.4 Nothing superseded
- [x] 8.5 No duplication
- [x] 8.6 Pre-push checks run (`quick.sh`)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits
- [x] 9.2 Closing commit ends with `Closes #1971`
- [x] 9.3 No secrets or unrelated changes
- [x] 9.4 SHAs in the PR

## 10. Pull Request y validación CI

- [x] 10.1 Archived in this PR
- [x] 10.2 Pushed
- [x] 10.3 PR `[#1971] fix(ci): …`
- [ ] 10.4 All required workflows green
- [ ] 10.5 Gate 4 — review approved
- [ ] 10.6 PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only (Owner)
- [ ] 11.2 CD published the image
- [ ] 11.3 Merge commit recorded

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 `main` CI, Frontend CI and Deploy GitHub Page succeed after merge
- [ ] 12.2 Revert path available
- [ ] 12.3 Close #1971 referencing the PR
- [ ] 12.4 Evidence recorded

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [x] Tests written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes (n/a)
- [x] Permanent documentation updated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
