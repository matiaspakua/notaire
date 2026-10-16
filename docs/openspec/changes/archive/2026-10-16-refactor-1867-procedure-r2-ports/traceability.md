# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1867 | open (child of #1693) |
| Use Case | CU85 / CU02/CU03 (epic #1693 / ADR-030) | exists |
| Specification | archived `2026-10-16-refactor-1867-procedure-r2-ports` | archived |
| Branch | `cursor/1867-procedure-r2-a603` | created |
| Tasks | `tasks.md` | groups 1–8 complete |
| Commits | pending | pending |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Procedure R2 edges gone | HexagonArchitectureTest | passing (R2 85→68) |
| ProcedureLookupPort remains | SubmittedDocumentManagementService / ProcedurePersistenceAdapter | kept |
| Folder / required-docs behaviour | ProcedureFolderServiceTest / RequiredDocumentationOnConfirmServiceTest | passing |

## Permanent documentation updated

| Document | Updated |
|----------|---------|
| ADR-030 | yes — procedure R2 slice (#1867) |
| CHANGELOG.md | n/a (#1766) |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1867 |
| 2 | yes | focused unit + HexagonArchitectureTest |
| 3 | yes | ADR-030; OpenSpec archived |

## Exceptions

None. RegistrationDraftService procedure edges left for a later slice.
