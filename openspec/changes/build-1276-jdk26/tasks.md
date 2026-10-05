> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1276 exists, labeled, linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance Criteria defined as scenarios
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — update of ADR-017, no new ADR
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b build/1276_jdk26`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh build-1276-jdk26`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: POM, workflows, Dockerfiles, pins, docs
- [x] 3.2 Add `scripts/test_jdk26_toolchain.py` (+ wrapper); observed failing
- [x] 3.3 Every scenario maps to a test

## 4. Implementación

- [x] 4.1 `java.version` 26 in the root POM
- [x] 4.2 Every workflow `setup-java` and `JAVA_VERSION` set to 26
- [x] 4.3 Pin the Dockerfile and Dockerfile.slim bases to JDK 26
- [x] 4.4 Tests green

## 5. Actualizar tests existentes

- [x] 5.1 Update `test_image_pins_and_dependabot.py` rejected-floating list for 26 without weakening it
- [x] 5.2 No dead code or stale versions remain

## 6. Ejecutar regresión

- [x] 6.1 `mvn verify` on JDK 26 (Maven container) keeps the ratchet floor
- [x] 6.2 `python3 -m unittest discover -s scripts/tests`
- [ ] 6.3 Image builds and the smoke test passes (`bash scripts/run_pipeline.sh`)
- [ ] 6.4 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 No UI change: the suite runs against the JDK 26 image as regression evidence

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 ADR-017
- [x] 8.2 devsecops guide and CLAUDE.md
- [x] 8.3 `CHANGELOG.md` — one entry

## 9. Commits atómicos

- [x] 9.1 One logical change per commit, Conventional Commits
- [ ] 9.2 Only the final commit carries `Closes #1276`; others `Refs #1276`
- [x] 9.3 No secrets, no commented-out code

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin build/1276_jdk26`
- [ ] 10.3 Open PR `[#1276] build(jdk): move the toolchain to JDK 26 and pin the Docker bases`
- [ ] 10.4 Wait for all required workflows
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CD green on the merge commit; health UP on the new image
- [ ] 12.2 Rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1276 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive build-1276-jdk26`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Failing tests observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1276` on the last
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
