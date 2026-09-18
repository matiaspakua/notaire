# Tasks — Procedure nested association integrity

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) §5 Official SDLC Workflow.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #981 exists and is open
- [x] 1.2 Use Case CU82 linked
- [x] 1.3 Issue moved to `in-progress`
- [x] 1.4 `proposal.md`, `design.md`, `specs/`, `traceability.md` written

## 2. Create branch

- [x] 2.1 Branch `fix/981_procedure-nested-associations-not-refetched` created from an updated `main`

## 3. Gate 2 — Write tests (TDD, failing first)

- [ ] 3.1 `ProcedureNestedAssociationIntegrationTest` written, run, and observed FAILING against current code
- [ ] 3.2 `ProcedurePersistenceAdapterTest` written, run, and observed FAILING against current code

## 4. Implement

- [ ] 4.1 `ProcedurePersistenceAdapter.save()` re-fetches non-null `fkIdDeed`/`fkIdProperty`/`fkIdBudget`/`fkIdManagement`/`fkIdProcedureType` by id
- [ ] 4.2 Missing referenced id throws `ResourceNotFoundException`

## 5. Verify

- [ ] 5.1 `mvn test -pl backend-api -Dtest=ProcedureNestedAssociationIntegrationTest,ProcedurePersistenceAdapterTest` green
- [ ] 5.2 `mvn test -pl backend-api` (full suite) green
- [ ] 5.3 `mvn checkstyle:check -pl backend-api` clean
- [ ] 5.4 `mvn -pl backend-api spotless:check` clean
- [ ] 5.5 `mvn verify -pl backend-api` (coverage gate) passes
- [ ] 5.6 `mvn test -pl backend-api -Ppg-integration` green

## 6. Documentation

- [ ] 6.1 No permanent documentation update required (see proposal.md "Documentation Impact")

## 7. Pipeline + PR

- [ ] 7.1 `bash scripts/preflight.sh` green before push
- [ ] 7.2 PR opened, `Closes #981`
- [ ] 7.3 `gh pr view --json mergeable,mergeStateStatus` confirms `MERGEABLE`
- [ ] 7.4 Merged, issue closed
- [ ] 7.5 `openspec archive procedure-nested-associations-not-refetched`
