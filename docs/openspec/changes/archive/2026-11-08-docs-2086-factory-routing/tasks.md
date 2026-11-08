> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2086 exists and names CU76
- [x] 1.2 Use Case CU76 already exists under `docs/100-business/102-use-cases/`
- [x] 1.3 Acceptance criteria are the scenarios in `traceability.md` (skip_specs: no product behavior change)
- [x] 1.4 Impact analysis is in `proposal.md`. No module code
- [x] 1.5 No ADR. This change restates ADR-030; it does not make a new architecture decision
- [ ] 1.6 Move the Issue to IN PROGRESS — blocked. The token cannot add labels, and an unlabeled issue is not claimable

## 2. Crear branch

- [x] 2.1 `main` matches `origin/main` at `67400b5e`
- [x] 2.2 Branch `cursor/factory-phase1-routing-b7c4` (cloud runtime name; see design.md)
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Cases: ADR-030 prompts, manifest routing, handoff command, code-only form plan, marker-less WIP
- [x] 3.2 Unit tests in `workspace/tests/test_factory_phase1_routing.py` and `test_claim_issue.py`
- [x] 3.3 No integration test. No API
- [x] 3.4 Observed failing: 6 failures and 2 errors before the prompts and docs existed
- [x] 3.5 Each traceability scenario maps to one test method

## 4. Implementación

- [x] 4.1 Align `java-architect` and `backend-implementer` with ADR-030
- [x] 4.2 Route Impeccable and `notaire-cybersec` in the manifest, frontend agent, and skills catalog
- [x] 4.3 Add `LOCAL-CURSOR-HANDOFF.md` and `FORMS-AUDIT-PLAN.md`
- [x] 4.4 `claim-issue.py` reports a marker-less WIP label and does not release it

## 5. Actualizar tests existentes

- [x] 5.1 `test_fleet_lessons_1858.py` still passes
- [x] 5.2 No assertion was weakened
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — no Java change; the command is inside `preflight.sh --full`, which must pass before push
- [x] 6.2 `mvn jacoco:check -pl backend-api` — same gate. Floor unchanged
- [x] 6.3 `mvn verify -pl backend-api` — same gate
- [x] 6.4 `bash integration-test/scripts/test.sh` — inside `preflight.sh --full`
- [x] 6.5 No `@Disabled` added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no UI surface
- [x] 7.3 n/a — no UI surface. Rendered viewports are #2087
- [x] 7.4 n/a — no UI surface. Docs, prompts, and unit tests only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Permanent docs listed in `proposal.md` are updated
- [x] 8.2 No endpoint change
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` has the factory line
- [x] 8.4 Nothing to archive into `docs/000-archive/`
- [x] 8.5 Product docs were not copied into the factory plan
- [x] 8.6 `bash workspace/sdlc/preflight.sh --full` is the push gate. The branch stays unpushed if it exits non-zero

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 Closing keyword is on the PR body (`Closes #2086`). Intermediate commits use `Refs #2086`
- [ ] 9.3 No secrets
- [ ] 9.4 SHAs recorded in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR after preflight passes
- [ ] 10.2 Push only after `preflight.sh --full` exits 0
- [ ] 10.3 Open the PR
- [ ] 10.4 GitHub does not re-run the product suite on the pull request (#2001). Do not add those workflows
- [ ] 10.5 Gate 4 is the Owner's review. This run does not merge
- [ ] 10.6 Record the PR number

## 11. Deploy

- [ ] 11.1 Owner merges. This run does not
- [ ] 11.2 CD is release-only
- [ ] 11.3 Record the merge when it exists

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke after the Owner deploys
- [ ] 12.2 Rollback is a revert (design.md)
- [ ] 12.3 Owner closes #2086
- [ ] 12.4 Record deploy evidence after merge

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1 pending the validator)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green
- [x] Coverage floor unchanged (no Java)
- [x] Playwright n/a — no UI surface
- [x] Permanent documentation updated
- [ ] Commits recorded
- [ ] PR created after preflight
- [ ] Merged — out of scope for this run
- [ ] `traceability.md` complete through release — after the Owner merges
