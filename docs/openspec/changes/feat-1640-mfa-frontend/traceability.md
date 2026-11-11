# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1640 | in-progress |
| Use Case | CU78 — Security and Compliance (RNF-12) | exists |
| Specification | `docs/openspec/changes/feat-1640-mfa-frontend/` | passed |
| Branch | `feat/1640_mfa-frontend` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Challenge step is shown instead of a session | login-page-mfa.test.tsx | pending |
| Valid TOTP completes the login | login-page-mfa.test.tsx | pending |
| Recovery code completes the login | login-page-mfa.test.tsx | pending |
| Wrong code is reported and the step stays | login-page-mfa.test.tsx | pending |
| Locked account is reported | login-page-mfa.test.tsx | pending |
| Malformed code is not sent | login-page-mfa.test.tsx | pending |
| Enrolment-required user enrols | login-page-mfa.test.tsx | pending |
| Loading and begin error states | mfa-enrollment-panel.test.tsx | pending |
| Confirm shows recovery codes once | mfa-enrollment-panel.test.tsx | pending |
| Wrong confirmation code | mfa-enrollment-panel.test.tsx | pending |
| Enrol from the dashboard | security-page.test.tsx; TS-0046 | pending |
| Disable requires a current code | security-page.test.tsx | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-032-multi-factor-authentication-totp.md` | no | pending |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1640, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
