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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU78 — Security and Compliance (RNF-12) | exists |
| Specification | `docs/openspec/changes/feat-1640-mfa-login-policy/` | passed |
| Branch | `feat/1640_mfa-login-policy` | created |
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
| Enrolled user receives a challenge instead of a token | LoginMfaPolicyTest | pending |
| Optional policy does not force enrolment | LoginMfaPolicyTest | pending |
| Enforced policy requires enrolment for admin and notary | LoginMfaPolicyTest | pending |
| Other roles are never forced | LoginMfaPolicyTest | pending |
| Policy OFF skips the challenge | LoginMfaPolicyTest | pending |
| Valid TOTP completes login | LoginMfaPolicyTest | pending |
| Recovery code completes login | LoginMfaPolicyTest | pending |
| Wrong code is rejected and counted | LoginMfaPolicyTest | pending |
| Locked account is refused | LoginMfaPolicyTest | pending |
| Scoped tokens are not sessions | LoginMfaPolicyTest | pending |
| Begin and confirm enrolment | LoginMfaPolicyTest | pending |
| Enrolment-required user can enrol | LoginMfaPolicyTest | pending |
| Wrong confirmation code is counted | LoginMfaPolicyTest | pending |
| Disable requires a current code | LoginMfaPolicyTest | pending |
| Anonymous callers are refused | LoginMfaPolicyTest | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-032-multi-factor-authentication-totp.md` | no | pending |
| `docs/200-architecture/206-security/THREAT-MODEL.md` | no | pending |
| `docs/200-architecture/206-security/OWASP-TOP10.md` | no | pending |

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
