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
| Issue | #<!-- number --> | <!-- open / in-progress / closed --> |
| Use Case | CU78 — Security, Privacy and Compliance | exists |
| Specification | `docs/openspec/changes/feat-1638-notary-role-separation/` | passed |
| Branch | `claude/amazing-curie-n19flr` | created |
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
| administrator types resolve to the admin authority | UserAuthorityResolverTest | passing |
| the notary type resolves to the notary authority | UserAuthorityResolverTest | passing |
| a notary is denied administrator paths | UserAuthorityResolverTest | passing |
| a notary keeps notarial endpoints | UserAuthorityResolverTest | passing |
| the seeded administrator is an administrator | UserAuthorityResolverTest | passing |
| the frontend does not treat a notary as an administrator | UserAuthorityResolverTest | passing |
| an administrator survives the migration | UserAuthorityResolverTest | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/206-security/OWASP-TOP10.md` | no | pending |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | no | pending |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1638, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
