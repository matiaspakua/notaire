# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2003 | in-progress |
| Use Case | CU78 – Seguridad, Privacidad y Cumplimiento | exists |
| Specification | `docs/openspec/changes/docs-2003-security-assessment/` | seeded |
| Branch | `cursor/security-assessment-portfolio-6eaf` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | local gates. GitHub product CI does not run on the pull request (#2001) | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | portfolio `/security/` after Pages deploy | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A register id is missing from the prose | `security/tests/test_threat_register.py` | pending |
| Module grants are recorded as enforced | `security/tests/test_threat_register.py` | pending |
| A classified workflow does not have that trigger | `security/tests/test_public_security_cadence.py` | pending |
| A stale phrase returns | `security/tests/test_assessment_currency.py` | pending |
| Technical docs are one click away | `github-page/e2e/site.spec.ts` | pending |
| Security is a primary entry | `github-page/tests/site.test.ts` | pending |
| A reader opens the threat explorer | `github-page/tests/security-explorer.test.tsx` | pending |
| The old docs route still resolves | `github-page/tests/site.test.ts` (`/docs/security/` stays in the docs section) | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | yes | pending |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/SECURITY-GATES.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2003, this folder |
| 2 | Failing tests written, test cases designed | yes | new tests failed on missing files; CodeQL assertion failed on `pull_request` |
| 3 | Suite green, coverage held, docs updated | partial | security tests, backend verify, pages Playwright passed. `preflight.sh --full` exit 1 on product Playwright browser binary and compose name conflict |
| 4 | CI green, review approved, no conflicts | pending | Owner |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner |

## Exceptions

No Constitution exception. Product `mvn verify` and `testing/e2e` are not run because this change does not touch the product API or the product UI. The portfolio smoke is the UI proof. `preflight.sh --full` is attempted and the result is stated on the pull request if the stack cannot run.
