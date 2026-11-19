# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2004 | open |
| Use Case | CU78 | exists |
| Branch | `cursor/2004-zap-on-tags-47dc` | created |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Version tags `v*` and `notaire-v*` start the baseline | `test_dast_zap_runs_on_version_tags_and_keeps_monday` | passing after the trigger |
| No pull request, no branch push | same | passing after the trigger |
| Monday cron and manual dispatch remain | same | passing after the trigger |
| `fail_action` true and `.zap/rules.tsv` | same | passing after the trigger |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | yes | this change |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | this change |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | yes | this change |
| `docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md` | yes | this change |
| `docs/200-architecture/208-devsecops/README.md` | yes | this change |
| `.claude/skills/notaire-cybersec/SKILL.md` | yes | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate |
| 2 | Failing test | yes | `AssertionError: 'v*' not found in []` |
| 3 | Suite | no | pending the workflow unit tests and preflight |

## Exceptions

No exception. #2004 is the open issue for this trigger. Authenticated ZAP stays out of this change.
