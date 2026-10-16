# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1851 | open (child of #1693) |
| Use Case | CU23 (epic #1693 / ADR-030) | exists |
| Specification | `docs/openspec/changes/archive/2026-10-16-refactor-1851-workflow-r2-ports/` | archived |
| Branch | `cursor/1851-workflow-r2-a603` | created |
| Tasks | `tasks.md` | groups 1–8 done |
| Commits | pending | pending |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Application workflow R2 edges are gone | HexagonArchitectureTest R2 shrink | passing |
| Inbound adapters do not import outbound workflow helpers | HexagonArchitectureTest R5 | passing |
| Trace/reentry REST behaviour unchanged | WorkflowTraceServiceTest / DocumentReentryServiceTest | passing |

## Permanent documentation updated

| Document | Updated |
|----------|---------|
| ADR-030 | yes — workflow R2 slice log |
| CHANGELOG.md | n/a (#1766) |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1851, this folder |
| 2 | yes | HexagonArchitectureTest + Trace/Reentry tests |
| 3 | yes | ADR-030 + archived OpenSpec |

## Exceptions

None.
