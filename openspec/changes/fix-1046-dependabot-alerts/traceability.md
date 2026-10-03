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
| Issue | #1046 | open (implement in progress; in-progress label ACL 403) |
| Use Case | CU78 – Security, Privacy and Compliance | exists (updated) |
| Related | #585 OPEN (`src.old` / `deprecated-src.old` — out of scope); audit-2026-09; GHSA-7w5x-hrqm-74c2; Log4j 1.x GHSAs on `log4j:log4j:1.2.17` | referenced |
| Specification | `openspec/changes/fix-1046-dependabot-alerts/` | Gate 1 validated |
| Branch | `cursor/fix-1046-dependabot-alerts-69d3` | created from `origin/main` @ `c2c34de8` |
| Tasks | `tasks.md` | implement complete locally; PR/CI pending |
| Commits | `3f050664` test; `3fcbb8f7` chore delete Swing; `d8f15b6a` smol-toml override; `85aef4eb` docs | done |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1157 | draft open |
| CI run | pending heavy gate on PR head | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| package.json declares a smol-toml override | `scripts/test_dependabot_hygiene.py` | green |
| lockfile resolves smol-toml to a patched version | same (prefer ≥1.9.0) | green (`1.9.0`) |
| npm install keeps the override | `npm ci` + `npm ls smol-toml` | green |
| deprecated-frontend-swing directory is absent | hygiene script / path assert | green |
| no active frontend-swing module returns | root `pom.xml` + tree assert | green |
| no log4j:log4j dependency remains in tracked POMs | hygiene script / ripgrep assert | green |
| CODEOWNERS has no live frontend-swing path | hygiene script / file assert | green |
| root README does not list deprecated-frontend-swing as present | hygiene script | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | yes | docs commit |
| `README.md` | yes | docs commit |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md` | yes | docs commit |
| `docs/200-architecture/201-SAD/sad.md` | yes | docs commit |
| `.github/CODEOWNERS` | yes | docs commit |
| `CHANGELOG.md` | yes | docs commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1046-dependabot-alerts` |
| 2 | Failing tests written, test cases designed | yes | 5 failures pre-implement on `scripts/test_dependabot_hygiene.py` |
| 3 | Suite green, coverage held, docs updated | local | hygiene green; docs updated; heavy CI pending |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- Playwright product specs: **n/a** — no UI product surface (deps + dead-tree delete + docs). Repository Playwright job must still pass on the PR (heavy CI).
- Issue `in-progress` label: GraphQL ACL 403 for integration token (expected).
- `#585` / `deprecated-src.old/` left untouched on purpose.
