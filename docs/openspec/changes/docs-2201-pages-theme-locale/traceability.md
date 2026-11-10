# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> Rows below Tasks stay pending until the step happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2201 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/docs-2201-pages-theme-locale/` | passed |
| Branch | `cursor/pages-theme-locale-2277` | created |
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
| Dark theme is saved | preferences.test.ts, e2e/preferences.spec.ts | passing |
| Reduced motion | preferences.test.ts, globals.css reduce rule | passing |
| Spanish shell | i18n.test.ts, Preferences.test.tsx, e2e/preferences.spec.ts | passing |
| Untranslated page | e2e/preferences.spec.ts and i18n.test.ts | passing |
| The path is complete | reading-path.test.ts | passing |
| Loaded plugins match the profile | factory-status.test.ts | passing |
| Planned moves stay planned | factory-status.test.ts | passing |

## Permanent documentation updated

| Document | Why | Status |
|----------|-----|--------|
| github-page/README.md | Theme and language | pending |
| CHANGELOG.md | Visitor-visible | pending |
| docs/openspec/specs/github-pages-site-shell/spec.md | Archive | pending |
| docs/openspec/specs/github-pages-ai-factory/spec.md | Archive | pending |

## Gate log

| Gate | Result | Evidence | Date |
|------|--------|----------|------|
| Gate 1 — plan | passed | validate-sdlc-plan.sh | 2026-11-10 |
| Gate 2 — red | pending | | |
| Gate 3 — preflight | pending | | |
| Gate 4 — review | pending | Owner | |
| Gate 5 — smoke | pending | after merge | |

## Exceptions

None.
