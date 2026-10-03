> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists — CU76, CU78, CU75 exist
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (cite ADR-006; no new ADR required)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1067 --add-label "in-progress"`) — defer until implement

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after queue ahead clears
- [x] 2.2 `git checkout -b cursor/test-1067-dast-contract-backup-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1067/` into `openspec/changes/test-1067-dast-contract-backup/` and run `bash scripts/validate-sdlc-plan.sh test-1067-dast-contract-backup`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: ZAP job present, Trivy retained, OpenAPI file present, diff fails on break, restore runs/skips correctly
- [x] 3.2 Write failing workflow/script guards (`scripts/test_*.py` or actionlint fixtures) where the repo pattern allows
- [x] 3.3 Integration: dry-run compose + ZAP in CI sandbox / local as available
- [x] 3.4 Observe fail (missing workflows / missing openapi file) before implementing
- [x] 3.5 Confirm every `#### Scenario:` maps to a CI job, script test, or docs check

## 4. Implementación

### Phase A — DAST (OWASP ZAP)

- [x] 4.1 Add workflow/job to start API stack and run OWASP ZAP baseline; upload report artifact
- [x] 4.2 Document warn vs fail policy; add allowlist file only if needed for known noise
- [x] 4.3 Confirm Trivy jobs remain; add short runbook notes (may leave full prose to #281)

### Phase B — API contract (OpenAPI)

- [x] 4.4 Add export/generation path for OpenAPI from springdoc; commit artifact at stable path
- [x] 4.5 Add PR job to diff committed OpenAPI (fail on breaking changes without artifact update)
- [x] 4.6 Document how developers regenerate/update the committed spec

### Phase C — Backup/restore (gated on #256)

- [x] 4.7 Add backup→restore→smoke workflow that skips with explicit #256 message when backup tooling is absent
- [ ] 4.8 When #256 is present (same PR later or follow-up commit): enable backup→restore→smoke and assert smoke check
- [x] 4.9 Do not implement a parallel backup product inside #1067

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing CI security jobs affected (Trivy, CI summary)
- [x] 5.2 Update them without weakening security assertions
- [x] 5.3 Remove obsolete “future ZAP” stubs in docs that contradict landed jobs

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — sanity if export tooling touches Java
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — if Java touched
- [ ] 6.3 `mvn verify -pl backend-api` — if Java touched
- [x] 6.4 Bruno — n/a unless OpenAPI export requires running API locally for generation
- [x] 6.5 No `@Disabled` security jobs without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no Playwright spec edits required
- [ ] 7.2 PR must still pass required workflows including Playwright job if triggered
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Serialize heavy ZAP with other heavy CI per `heavy-ci-merge` workflow

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update DevSecOps README, TEST-PLAN, deployment README per proposal.md
- [x] 8.2 OpenAPI committed artifact is the contract SSOT alongside springdoc annotations
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded “future only” snippets only if replaced — prefer edit in place
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate applicable locally

## 9. Commits atómicos

- [x] 9.1 Commit by phase (A/B/C), Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1067` on the final landing commit (earlier commits reference #1067)
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/test-1067-dast-contract-backup-69d3`
- [x] 10.2 Open the PR titled `[#1067] test(ci): DAST ZAP, OpenAPI diff, backup-restore gate`, referencing CU76/CU78/CU75
- [ ] 10.3 Wait for every required workflow to pass; ZAP may be nightly — ensure OpenAPI diff is on PR
- [ ] 10.4 Gate 4 — CI green, review approved; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm CD unaffected; enable scheduled ZAP/backup workflows on `main`
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: ZAP report artifact; OpenAPI diff green; backup-restore skip or green per #256
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue only when Phase C acceptance is met or coordinator-approved child issue owns C
- [ ] 12.4 Archive the change: `openspec archive test-1067-dast-contract-backup`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76/CU78/CU75)
- [x] Specification written and reviewed (Gate 1 draft)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a product UI; CI job still green)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
