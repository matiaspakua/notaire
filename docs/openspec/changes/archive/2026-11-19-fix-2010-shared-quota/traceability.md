# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2010 | open |
| Use Case | CU78 | exists |
| Branch | `cursor/2010-shared-quota-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Burst rejected, later window allowed | `ApiRequestQuotaTest` and `ApiQuotaSharedStoreIntegrationTest` | passing |
| Another instance sees the count | `ApiQuotaSharedStoreIntegrationTest` | passing |
| Table columns stay minimal | same | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | yes | this change |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | yes | this change |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | yes | this change |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | yes | this change |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | this change |
| `docs/200-architecture/202-ADR/ADR-018-rate-limiting-policy.md` | yes | this change |
| `docs/openspec/specs/api-quota/spec.md` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | `JpaApiRateWindowStore` did not compile |
| 3 | Suite | no | pending preflight |

## Exceptions

No exception. Lockout-griefing of a known username stays SR-05. No second issue is filed.
