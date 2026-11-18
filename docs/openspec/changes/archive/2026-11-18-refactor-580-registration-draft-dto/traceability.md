# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #580 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/refactor-580-registration-draft-dto/` | drafted |
| Branch | `cursor/580-registration-draft-mapper-47dc` | created |
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
| Fields copy onto the DTO | `RegistrationDraftLegacyMapperTest` | passing |
| A missing deed leaves the deed id empty | `RegistrationDraftLegacyMapperTest` | passing |
| The entity has no getDto | `RegistrationDraftLegacyMapperTest` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| None. HTTP API unchanged | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Failing test | yes | testCompile missing RegistrationDraftLegacyMapper |
| 3 | Suite green | no | pending preflight |
| 4 | Review | no | pending |
| 5 | Owner merge | no | #580 stays open |

## Exceptions

No exception. Label writes return 403. CLI search does not write the usage log (#2307).

## Locate

Repolens search from the indexed checkout, 2026-11-18T09:56Z:

- `SubstitutionController getAll` ranked the controller first. The screen filters the full list by year, so paging was not implemented.
- `RegistrationDraft getDto` ranked the DTO and the entity.
- `RegistrationDraftOpsAdapter getDto` ranked the adapter first.
