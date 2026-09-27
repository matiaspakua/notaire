# Tasks: Remove Dead Default Credentials

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific implementation work inside group 4; do not renumber the
> mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1069 exists, labeled `bug, security, DEVOPS`
- [x] 1.2 Use Case registered: CU78 — Security, Privacy and Compliance
- [x] 1.3 Acceptance Criteria defined as Issue checklist (2 items, see `triage.md`)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural — no ADR required (configuration cleanup)
- [x] 1.6 Issue moved to IN PROGRESS (`gh issue edit 1069 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/1069_remove_dead_credentials`
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

Two new unit tests will be added and made to fail before implementation:

- [x] 3.1 Write `ApplicationPropertiesHygieneTest.shouldNotDefineDeadSecurityUserKeys`
  - Arrange: Create a `Properties` object with `spring.security.user.name`,
    `spring.security.user.password`, `spring.security.user.roles` keys
  - Act: Call `ApplicationPropertiesHygiene.validateProperties(properties)`
  - Assert: Test fails with `No key shall start with "spring.security.user."`
  
- [x] 3.2 Write `ApplicationResourceTest.shouldNotIncludeConfigProperties`
  - Arrange: Assume classpath resource `/config.properties` exists
  - Act: Look up resource `"/config.properties"`
  - Assert: Test fails with `Resource is not absent`
  
- [x] 3.3 Run tests — both fail (dead keys present)
- [ ] 3.4 n/a — no integration tests apply
- [x] 3.5 n/a — no delta spec scenarios (`skip_specs: true`)

## 4. Implementación

### 4a. Remove dead keys from application.properties

- [ ] 4a.1 Edit `backend-api/src/main/resources/application.properties`:
  - Remove lines 92-94 containing:
    ```
    # Prometheus will use admin/admin for scraping
    spring.security.user.name=admin
    spring.security.user.password=admin
    spring.security.user.roles=ACTUATOR,ADMIN
    ```
  - Remove the misleading Prometheus comment line
  - Use `sed` or manual edit to preserve formatting

### 4b. Delete config.properties

- [ ] 4b.1 Delete `backend-api/src/main/resources/config.properties`
  - Backup first: `git show HEAD:backend-api/src/main/resources/config.properties > config.properties.bak`

### 4c. Run new tests — verify they now pass

- [ ] 4c.1 `mvn test -pl backend-api -Dtest=ApplicationPropertiesHygieneTest`
- [ ] 4c.2 `mvn test -pl backend-api -Dtest=ApplicationResourceTest`
- [ ] 4c.3 Both tests pass (100%)

### 4d. Static verification

- [ ] 4d.1 `grep -E "^spring\.security\.user\." backend-api/src/main/resources/application.properties`
  - Should return nothing (exit code 1)
- [ ] 4d.2 `test ! -f backend-api/src/main/resources/config.properties`
  - Should return 0 (true)

## 5. Actualizar tests existentes

- [ ] 5.1 Check if any existing tests reference removed config keys
  - Search: `grep -r "spring.security.user" src/test/`
  - Update tests that use the removed keys, if any exist
- [ ] 5.2 n/a
- [ ] 5.3 n/a

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — all backend tests pass
- [ ] 6.2/6.3 `mvn verify -pl backend-api -DskipTests` — BUILD SUCCESS
- [ ] 6.4 Run frontend E2E: `cd frontend && npx playwright test` — all pass
- [ ] 6.5 No `@Disabled` or skipped tests introduced

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface. This change touches only backend configuration files

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update CHANGELOG.md: add entry `chore(security): remove dead default credentials`
- [ ] 8.2 n/a — no endpoint changed
- [ ] 8.3 n/a — not user-visible
- [ ] 8.4 Nothing to archive
- [ ] 8.5 Confirmed: no duplication introduced
- [ ] 8.6 `bash scripts/preflight.sh --fix` — PREFLIGHT PASSED

## 9. Commits atómicos

- [ ] 9.1 Commit 1: `test(security): add application properties hygiene tests`
- [ ] 9.2 Commit 2: `chore(security): remove dead default credentials`
  - Changes: `application.properties` (remove dead keys)
  - Deletes: `config.properties`
  - Ends with `Closes #1069`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin chore/1069_remove_dead_credentials`
- [ ] 10.2 Open PR titled `[#1069] chore(security): remove dead default credentials`
- [ ] 10.3 All 26 checks passed: Build, Tests, Sonar, Checkstyle, Spotless, etc.
- [ ] 10.4 Gate 4 — CI green, no merge conflicts, docs complete; PR merged

## 11. Deploy

- [ ] 11.1 Merged via PR #1069 (merge commit recorded in `traceability.md`)
- [ ] 11.2 Verify `cd.yml` ran successfully (Docker rebuild/publish)
- [ ] 11.3 Record merge commit SHA in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: run `mvn verify` — all quality gates green (70%+ JaCoCo)
- [ ] 12.2 Rollback path confirmed available (plain `git revert`)
- [ ] 12.3 Issue #1069 closed (auto-closed via `Closes #1069` on merge commit)
- [ ] 12.4 Archive the change: `openspec archive remove-dead-credentials-1069`

## Definition of Done

- [ ] Issue linked (CU78 registered; dead credentials not linked to business behavior)
- [ ] Specification written and reviewed (Gate 1: OpenSpec validation passed)
- [ ] Verification designed and run first via live reproduction, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E: n/a, no UI surface
- [ ] Permanent documentation: updated, confirmed (CHANGELOG.md entry)
- [ ] Commit atomic and conventional, referencing the Issue
- [ ] PR created, CI green (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
