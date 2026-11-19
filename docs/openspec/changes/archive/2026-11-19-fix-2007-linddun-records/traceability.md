# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2007 | open |
| Use Case | CU78 | exists |
| Branch | `cursor/2007-linddun-records-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| LD-7, LD-8, LD-9 are in the register and the model | `test_linddun_rows_and_module_grants_match_the_system_model` | passing |
| Testimony, folio, and submitted-document paths and modules are named | same | passing |
| LD-9 stays Open | same | passing |
| Counsel owns the statute | same | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | yes | this change |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | yes | this change |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | this change |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | LD-7, LD-8, LD-9 missing from the register |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. Read audit for these records stays the open LD-9 row. No second issue is filed.
