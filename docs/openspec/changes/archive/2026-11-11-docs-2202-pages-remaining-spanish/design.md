> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

The site already has an English-keyed Spanish dictionary (`github-page/lib/i18n/`), `TRANSLATED_ROUTES`, DocShell `<Es>` / TreeWalker, and Mermaid `localizeChart` that replaces ES keys of length ≥ 18. Anatomy figures inject plate HTML via `dangerouslySetInnerHTML` and overwrite the readout in an effect, so dictionary-only fixes do not stick without `AnatomyFigure` calling `tr()`.

## Goals / Non-Goals

**Goals:**
- Spanish for anatomy figures, Mermaid sentence labels, and Diagram captions on #2202 routes.
- Shell leaks fixed (footer docs titles, AI Factory chapters label).
- Tests that fail if those keys regress.

**Non-Goals:**
- New locales; product-app messages; redrawing figures; Wilson/portal/Context Lake.

## Decisions

1. **Extend the existing `pair()` dictionary** instead of forking pages — same pattern as #2209/#2213.
2. **`AnatomyFigure` uses `tr(locale, …)`** for plate and steps — TreeWalker alone cannot keep readouts Spanish after step changes.
3. **Mermaid keeps exact `<br/>` substrings as keys** — `localizeChart` does string replace on the chart source.
4. **Keep technical terms in English** per #2202 (Issue, pull request, gate, OpenSpec, Flyway, commit, kit, Owner, paths, commands).

## Riesgos / Trade-offs

- [Long Mermaid labels drift from chart text] → Coverage test asserts exact keys present in ES; localizeChart already sorts by length.
- [PreferencesProvider applies locale after a tick in unit tests] → Spanish anatomy test awaits the Spanish aria-label.
- [CHANGELOG Unreleased fence] → Skip CHANGELOG if another PR owns it; document in PR body.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Anatomy plate and steps have Spanish | unit | `github-page/tests/i18n-coverage.test.ts` |
| Anatomy figure renders Spanish readout | unit | `github-page/tests/anatomy.test.tsx` |
| Mermaid sentence / caption keys exist | unit | `github-page/tests/i18n-coverage.test.ts` |
| Spanish heading on anatomy + lifecycle | E2E | `github-page/e2e/preferences.spec.ts` |
| Untranslated notice still absent | E2E | existing preferences cases |

- New unit tests: anatomy Spanish render; coverage for figure + Mermaid keys.
- Coverage impact: Pages Vitest only; no JaCoCo change.

## Regression Strategy

- Existing anatomy English step/readout tests stay the default (no PreferencesProvider → locale `en`).
- Diagram overflow tests still run for `en` and `es` labels.
- Never weaken an assertion to force green.

## Playwright Strategy

- Extend `SPANISH` samples with `product/anatomy/` and `ai-sdlc/lifecycle/` at 375/1280 and light/dark.
- Assert no “Esta página sigue en inglés” notice on those pages.

## Deployment Strategy

- Merge to `main` publishes GitHub Pages via the existing Pages workflow. No workflow or ruleset change.
- Only the Owner merges.

## Rollback Strategy

- Revert the PR tip. Dictionary and AnatomyFigure changes are isolated to `github-page/`.

## Migration Plan

- None (static site copy only).

## Open Questions

- None. Gate 1.6 WIP label may SKIP with honest 403 (`addLabelsToLabelable`).
