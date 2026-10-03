# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1022 | open (in-progress label ACL may 403) |
| Use Case | none | not applicable — same technical/internal-quality exception as epic #973 |
| Specification | `openspec/changes/chore-1022-namedquery-english/` | complete |
| Branch | `cursor/chore-1022-namedquery-english-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | groups 1–8 complete; 9–12 at push/PR/merge |
| Commits | `c536c686` docs(openspec); `0dc08bbd` test hygiene; `4b9efd3d` chore rename; `9fb71537` traceability | pushed |
| Pull Request | [#1187](https://github.com/matiaspakua/notaire/pull/1187) | draft |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No Spanish NamedQuery entity prefixes remain | `NamedQueryEnglishNamesHygieneTest` | passing |
| No Persona.createNamedQuery mismatch remains | `NamedQueryEnglishNamesHygieneTest` | passing |
| English Folio/Item/Person method tails (no Spanish tails in inventory) | `NamedQueryEnglishNamesHygieneTest` | passing |
| createNamedQuery call sites match English names | `NamedQueryEnglishNamesHygieneTest` + existing JPA unit tests | passing (1941/1941) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | `4b9efd3d` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | proposal + delta spec + validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | hygiene 3 failures red, then green |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test` 1941/1941; verify BUILD SUCCESS; CHANGELOG |
| 4 | CI green, review approved, no conflicts | pending | draft PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Use Case exception: same precedent as epic #973 — cosmetic JPA NamedQuery
identifier Englishization with no user-facing/behavioral change. Documented in
issue #1022 and this proposal. No CONSTITUTION.md §12 process step skipped.
