# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1618 | open, `in-progress` labelled |
| Use Case | CU78 | exists; permanent record will be updated |
| Specification | `docs/openspec/changes/code-scanning-remediation/` | Gate 1 passed |
| Branch | `fix/1618_code-scanning-remediation` | created from updated `main` |
| Tasks | `tasks.md` | 0/N complete |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | pending | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Read-only CD jobs declare read-only permissions | `security/tests/test_cd_workflow_permissions.py` | pending |
| Release publication retains only its required write access | `security/tests/test_cd_workflow_permissions.py` | pending |
| URL text containing a regex metacharacter is matched literally | `testing/e2e/tests/TS-0070-supervised-tour-tutorial.spec.ts` | pending |
| Escaping preserves ordinary URL matching | `testing/e2e/tests/TS-0070-supervised-tour-tutorial.spec.ts` | pending |
| Unsafe cookie request without a CSRF token is rejected | backend CSRF integration test | pending |
| Same-origin browser write includes a valid CSRF token | `TS-0044-security-csrf-cors-qa.spec.ts` | pending |
| JWT remains unreadable to browser script | `TS-0044-security-csrf-cors-qa.spec.ts` | pending |
| Planted disabled CSRF configuration fails SAST | `security/tests/test_security_cases.py` plus Semgrep fixture | pending |
| CD job without explicit permissions fails policy validation | `security/tests/test_cd_workflow_permissions.py` | pending |
| Dynamic regex construction without quoting fails SAST | `security/tests/test_security_cases.py` plus Semgrep fixture | pending |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | pending | pending |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | pending | pending |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | pending | pending |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | pending | pending |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | `openspec validate --strict`; `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | TDD has not started |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

The user (repository owner) authorized emergency-security handling for the
seven pre-existing GitHub-generated autofix PRs. The `sdlc-exception` label
waives only their missing pre-existing Gate 1 history; it does not waive the
tests, scans, review, or evidence required by this remediation.
