# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2069 | in-progress |
| Use Case | CU84 – Login al sistema; RNF-05 | exists |
| Specification | `docs/openspec/changes/fix-2069-login-operate-desk/` | drafted |
| Branch | `fix/2069_login-operate-desk` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Credentials sit on one desk card | `frontend/src/tests/unit/login-page.test.tsx` | pending |
| Seal entrance does not rotate | `login-page.test.tsx` and `testing/e2e/tests/TS-0001-login-authentication.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `DESIGN.md` | pending | pending |
| `PRODUCT.md` | pending | pending |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | pending | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | pending | pending |
| `CHANGELOG.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | login unit test failed on the missing motion import, then 16 passed |
| 3 | Suite green, coverage held, docs updated | partial | Vitest, backend verify, Bruno, and Docker smoke passed inside `preflight.sh --full`. The script still exited 1 because two audit-pagination Playwright assertions expected 498 rows and saw 499. A standalone `playwright test` on this branch exited 0. |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | after merge |

## Exceptions

No constitution exception. Commit and PR use `Refs #2069` rather than `Closes #2069` because this UI pass also files sibling backlog issues and must not auto-close them. #2069 is named in the PR and is closed only at Gate 5 after merge.
