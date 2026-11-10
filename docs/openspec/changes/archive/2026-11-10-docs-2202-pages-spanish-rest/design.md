## Context

#2201 translated the shell, the home page and the AI Factory overview, and left a notice on every other page. #2202 asks for the remaining public pages: product, security, the factory subpages, the technical-docs reading path, results, the demo shell and about.

The Pages app is a static export. It has no per-request locale. The language is `localStorage` key `notaire.lang`.

## Goals / Non-Goals

**Goals**

- Natural Spanish, with English technical terms kept where they read better.
- One string source. No second page tree.
- A test that fails when a translated route is missing Spanish for its content-module strings.
- The English notice only on routes this change does not translate.

**Non-Goals**

- A third locale, or product `frontend/messages`.
- Translating every deeper `/docs/` page in this change.
- Redrawing the isometric figures. Their step lists use the same dictionary. The drawing itself stays the light plate.

## Decisions

1. **The English sentence is the key.** `pair(en, es)` fills one record. `tr` returns Spanish or the English. A client walker (`Es`) replaces a text node when its collapsed whitespace matches a key, so pages do not each carry a locale prop.
2. **Translated routes are an explicit set** in `lib/i18n/es.ts`. The notice renders only when Spanish is selected and the path is not in that set. Deeper docs such as `/docs/hexagonal/` stay out of the set.
3. **Sidebar titles are a href map**, not a second copy of the page. Group labels share the factory act map.
4. **The demo banner reads `notaire.lang`** because the demo is a separate static export on the same origin. It does not import the Pages React tree.

## Riesgos

- A fragment split by `<Code>` does not match a full-sentence key. Page leads that must be Spanish are a single string, or a `{placeholder}` template.
- Dynamic counts (`{agents}` skills) stay as named placeholders in both languages.
- Generated metric methods are paired to the snapshot text. Regenerating the snapshot without updating the pair leaves that sentence in English.

## Testing Strategy

- Vitest `i18n-coverage.test.ts` fails when a translated route is missing, or when a content-module string (lessons, playbook, case, framework tables, STRIDE) has no Spanish entry that differs from the English.
- Existing preference and reading-path tests stay.

## Regression Strategy

- Default language remains English. Header and site-map tests that read English are unchanged.
- `/docs/hexagonal/` still shows the English notice.
- `preflight.sh --full` is the regression run. No product assertion is weakened.

## Playwright Strategy

- `github-page/e2e/preferences.spec.ts` checks a Spanish heading and no English notice on `/product/`, `/security/`, `/docs/`, `/ai-sdlc/results/` and `/about/`, in light and dark, at 375 and 1280.
- The same file still checks the notice on `/docs/hexagonal/` and dark axe on the entry pages.

## Deployment Strategy

Merge to `main` publishes GitHub Pages via `deploy-github-page.yml`. No workflow trigger change. The Owner merges.

## Rollback Strategy

Revert the merge commit. The previous export is the previous `main` build. `notaire.lang` is ignored by an older shell and harmless on this one.
