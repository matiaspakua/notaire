# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1059 | open |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | ADR-015; TS-0040; audit-2026-09; #1058 redirect stubs | referenced |
| Specification | `openspec/changes/fix-1059-i18n-page-coverage/` | Gate 1 in progress |
| Branch | `cursor/fix-1059-i18n-page-coverage-69d3` | created |
| Tasks | `tasks.md` | pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Gap page namespaces exist in both catalogs | unit `i18n.test.ts` | pending |
| Login leftover keys exist in both catalogs | unit `i18n.test.ts` | pending |
| Catalog key structures stay identical (es/en) | unit `i18n.test.ts` | pending (extend) |
| Workflows list uses translations | unit + optional TS-0040 | pending |
| Workflows editor uses translations | unit + page wiring | pending |
| Suplencias / reportes / roles / items use translations | unit + page wiring | pending |
| Login leftovers use translations | unit `login-page.test.tsx` / i18n | pending |
| Auditoria all-modules filter is translated | unit keys + page wiring | pending |
| EN title visible on 1–2 former gap pages | optional TS-0040 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | pending | — |
| `docs/200-architecture/202-ADR/ADR-015-internationalization.md` | pending | — |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | this folder; `validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Store-mounted OpenSpec seed was unavailable; change scaffolded via
`openspec new change` + `seed-openspec-change.sh` from issue #1059 decisions.
