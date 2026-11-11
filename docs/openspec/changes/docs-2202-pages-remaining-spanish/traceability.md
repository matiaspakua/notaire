# Traceability — docs-2202-pages-remaining-spanish

## Chain

| Link | Reference |
|------|-----------|
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Issue | #2202 |
| Branch | `docs/2202_pages-remaining-spanish` (PR tip `cursor/2202-pages-remaining-spanish-f3a4`) |
| OpenSpec change | `docs/openspec/changes/docs-2202-pages-remaining-spanish` |
| Prior slices | #2209, #2213, #2216, #2223, #2232 (routes + content modules; issue left open without `Closes`) |

## Requirement coverage

| Acceptance (#2202) | Spec scenario | Test |
|--------------------|---------------|------|
| Product/Security/Technical docs Spanish incl. sidebars | Site shell Spanish on translated routes | `i18n-coverage`, preferences E2E |
| Factory case study…playbook Spanish | Anatomy + Mermaid + captions on factory routes | `i18n-coverage`, anatomy Spanish unit, lifecycle E2E |
| English technical terms stay English | Kept in pair values | Manual review + dictionary |
| Diagram sentence labels translated; identifiers stay | Mermaid/caption pairs; localizeChart | `i18n-coverage` Mermaid keys |
| Playwright Spanish heading per section, light/dark, 375/1280 | preferences.spec SPANISH matrix | `e2e/preferences.spec.ts` |
| Untranslated notice gone on translated pages | pageIsTranslated | existing E2E count 0 |

## Permanent documentation updated

| Doc | Status |
|-----|--------|
| `docs/openspec/specs/github-pages-site-shell/spec.md` | on archive |
| `github-page/README.md` | if Spanish section incomplete |
| `CHANGELOG.md` | if Unreleased fence free |

## Gate log

| Gate | Result | Evidence |
|------|--------|----------|
| 1 OpenSpec | pending validate | this change |
| 1.6 WIP label | SKIP (403 addLabelsToLabelable) | `internal/foreman-ops-asli-71/gate16-wip.txt` |
| 2 TDD | pending | Vitest / Playwright |
| 3 Docs | pending | archive |
| 4 PR | pending | — |
| 5 Smoke | Owner after merge | — |

## Exceptions

- Gate 1.6 honest SKIP: Cursor GitHub App cannot `addLabelsToLabelable` (documented FLEET-IDENTITY).
- `gh` GraphQL `createPullRequest` may 403; open via ManagePullRequest on `cursor/*` (#2233).
