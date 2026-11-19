# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2009 | open |
| Use Case | CU78 | exists |
| Branch | `cursor/2009-threat-dragon-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Model validates against the v2 schema | `test_threat_dragon_model` | passing |
| SR ids match the register in both directions | same | passing |
| No secret markers | same | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | yes | this change |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | yes | this change |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | yes | this change |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | schema file missing |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. pytm and Threagile stay unevaluated for adoption. The GitHub Pages site is not edited.
