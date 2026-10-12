> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1571 exists and is linked to CU76
- [x] 1.2 Use Case CU76 updated with the three-component release bullet
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No new ADR — ADR-007 remains the schema source of truth; semver stays release-please
- [x] 1.6 Issue left open for the owner to move; this token may not add `in-progress`

## 2. Crear branch

- [x] 2.1 Branched from updated `origin/main` (`52b438ee`)
- [x] 2.2 `cursor/release-product-schema-db96`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: version coherence, schema ≠ semver, train map, tag prefixes, both SBOM shapes, changelog parser, actuator, OpenAPI, sidebar
- [x] 3.2 `workspace/tests/test_product_release.py`, `ProductReleaseInfoTest`, `OpenApiConfigTest`, `app-sidebar.test.tsx`
- [x] 3.3 No new Spring integration test — actuator contributor is unit-tested with a Flyway mock
- [x] 3.4 Observed red: Maven `1.0-SNAPSHOT` ≠ manifest `0.1.2`, missing `include-component-in-tag`, missing product record, `app.version` still a Spring placeholder
- [x] 3.5 Each scenario in the delta spec maps to a test named in `traceability.md`

## 4. Implementación

- [x] 4.1 `workspace/release/product_release.py` plus `release/product-release.json`
- [x] 4.2 Align pom versions, `@` resource filtering, release-please tag flag, CD filters and SBOM assets
- [x] 4.3 Actuator contributor, OpenAPI version, sidebar version, Pages history
- [x] 4.4 RELEASES.md, SBOM.md, Constitution §11, runbook, CU76

## 5. Actualizar tests existentes

- [x] 5.1 OpenAPI test expected a hardcoded `1.0.0`
- [x] 5.2 It now asserts the injected product version
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api -Dtest=ProductReleaseInfoTest,OpenApiConfigTest`
- [x] 6.2 Coverage of the new classes is by those unit tests; full jacoco ratchet is the CI coverage job
- [x] 6.3 `mvn -pl backend-api spotless:check` on the touched Java
- [x] 6.4 n/a — no HTTP behavior change beyond OpenAPI `info.version`; Bruno not required for this cut
- [x] 6.5 No `@Disabled` added

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E spec — version label is not a user flow
- [x] 7.2 n/a — no Playwright spec in this change
- [x] 7.3 n/a — sidebar unit test covers the label; no layout breakpoint change
- [x] 7.4 n/a — no new route or form; Vitest covers the version line

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 RELEASES.md, SBOM.md, RELEASE.md, Constitution, milestones, lifecycle, CU76, DevSecOps, fleet
- [x] 8.2 OpenAPI `info.version` follows `app.version`
- [x] 8.3 CHANGELOG `[Unreleased]`
- [x] 8.4 Nothing archived
- [x] 8.5 Permanent docs point at RELEASES.md and SBOM.md instead of copying the train table
- [x] 8.6 `python3 workspace/release/product_release.py --check` and the unittest module

## 9. Commits atómicos

- [ ] 9.1 Conventional commits
- [ ] 9.2 Issue reference in the message
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs recorded below after commit

## 10. Pull Request y validación CI

- [ ] 10.1 Push the branch
- [ ] 10.2 Open the PR
- [ ] 10.3 Required workflows
- [ ] 10.4 Gate 4 pending review
- [ ] 10.5 PR number pending

## 11. Deploy

- [ ] 11.1 Merge via the pull request
- [ ] 11.2 CD on the next `v*` tag attaches the SBOM set
- [ ] 11.3 Merge commit pending

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke after merge: `/actuator/info` shows `notaire.version`
- [ ] 12.2 Rollback is revert-the-merge, documented in design.md
- [ ] 12.3 Close #1571 after merge
- [ ] 12.4 Archive the change after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [x] Tests written first and observed failing (Gate 2)
- [ ] Full suite green
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright n/a, recorded in group 7
- [x] Permanent documentation updated (Gate 3)
- [ ] Commits atomic and conventional
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
