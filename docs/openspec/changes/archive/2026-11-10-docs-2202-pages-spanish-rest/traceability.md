# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2202 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-2202-pages-spanish-rest/` | passed |
| Branch | `cursor/pages-spanish-rest-2277` | created |
| Tasks | `tasks.md` | in progress |
| Commits | `602c030a` docs(pages): translate the remaining public pages | done |
| Pull Request | pending | pending |
| CI run | local `preflight.sh --full` before push | pending |
| Merge commit | Owner merges | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Spanish product, security, docs reading path, factory results, about | e2e/preferences.spec.ts | written |
| Missing Spanish key fails | tests/i18n-coverage.test.ts | passing |
| Untranslated deeper doc keeps the notice | e2e/preferences.spec.ts (`/docs/hexagonal/`) | passing |
| English technical terms stay | dictionary review; keys such as Issue, pull request, gate, OpenSpec | passing |

## Permanent documentation updated

| Document | Why | Status |
|----------|-----|--------|
| github-page/README.md | Which routes are Spanish | updated |
| CHANGELOG.md | [Unreleased] | updated |
| docs/openspec/specs/github-pages-site-shell/spec.md | Notice only on untranslated routes | updated on archive |

## Gate log

| Gate | Result | Evidence | Date |
|------|--------|----------|------|
| Gate 1 — plan | passed | validate-sdlc-plan.sh | 2026-11-10 |
| Gate 2 — tests | passed | vitest i18n-coverage.test.ts | 2026-11-10 |
| Gate 3 — preflight | pending | preflight.sh --full on 602c030a before push | |
| Gate 4 — review | pending | Owner | |
| Gate 5 — smoke | pending | after merge | |

## Exceptions

None. The GitHub token could not add `status:in-progress` on #2202 (403). The issue stays open.
