## Context

The Pages app (`github-page/`) is a static export. It has one light theme and English copy. The kit on `main` is ahead of the framework page: config, plugins, Grant, Isolator, Foreman routing, and read-only lead time are merged. #2092 and #2127 are merged. ApprovalPort is a design with `no-evaluable` behavior. `workspace/sdlc` is still planned in the migration inventory.

The product design system (SF Pro, San Francisco Blue) does not govern this site. The Pages identity stays Fraunces, the slate ramp, brand blue, and factory violet.

## Goals / Non-Goals

**Goals**

- Theme and language controls that persist, follow the system until chosen, and do not shift the header.
- Spanish for the shell, the home page, and the factory introduction, with a clear fallback elsewhere.
- A reading order and an honest kit status.

**Non-Goals**

- Translating every long-form page in this change.
- Redrawing isometric figures for a dark plate. They keep `data-theme="light"` on their own card.
- A writable demo backend (#1986).
- Changing product `frontend/` i18n.

## Decisions

1. **CSS class overrides for dark surfaces and text, not an inverted slate scale.** `bg-slate-900` is a code block and a dark button. Inverting `--color-slate-900` would put white text on a light chip. Dark mode overrides the surface and text utilities that mean "page" and leaves the dark chips alone.
2. **`localStorage` plus a blocking boot script.** The export has no server. The script sets `data-theme` and `lang` before paint. React state starts from the same defaults and syncs in an effect, so hydration matches.
3. **One dictionary for chrome and the two translated pages.** Untranslated routes set `lang="en"` on the article and show one notice. Technical terms stay in English.
4. **Kit status is read, not typed.** Allow-list and inventory counts come from the TOML files. Command names are checked against `aisdlc --help`.

## Riesgos

- A utility we did not override stays light-on-light or dark-on-dark. Mitigation: axe on the entry pages in both themes, and contrast pairs locked in a unit test.
- Spanish copy drifts from the English. Mitigation: the same keys, and the untranslated notice rather than a half-translated paragraph presented as complete.
- A later kit command is added and the page omits it. Mitigation: the command table's first token must appear in `aisdlc --help`. The page does not claim to be the full manual; it links the README.

## Testing Strategy

- Vitest for preference resolution, contrast pairs, the reading order, inventory parsing, and the allow list.
- A component test that Spanish navigation renders "Documentación técnica".
- Playwright + axe on the entry pages in dark, and a Spanish home/factory check. Existing light English smoke stays.

## Regression Strategy

- Existing header, search, and site-map tests stay in English (the default).
- `npm run check:links` after the export.
- No change to product tests except whatever `preflight.sh --full` already runs.

## Playwright Strategy

- `github-page/e2e/preferences.spec.ts` at the existing 375 and 1280 projects: dark axe on `/`, `/ai-sdlc/`, `/docs/`, `/security/`, `/product/`; Spanish heading on `/` and `/ai-sdlc/`; English notice on `/docs/hexagonal/`.
- The current `site.spec.ts` remains the light English sweep.

## Deployment Strategy

Merge to `main` publishes GitHub Pages via `deploy-github-page.yml`. No workflow trigger change. The Owner merges.

## Rollback Strategy

Revert the merge commit. The previous export is the previous `main` build. Preferences keys are ignored by the old shell.
