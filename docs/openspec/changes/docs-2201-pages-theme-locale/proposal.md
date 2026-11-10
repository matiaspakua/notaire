# Dark mode, Spanish, and an accurate factory reader path

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2201 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/pages-theme-locale-2277` |
| Gate 1 status | passed |

## Objetivo

A reader of the public site should choose light or dark, and English or Spanish, and should be able to start from a plain explanation and walk to the kit that is actually on `main`. The AI Factory stays a different section from the product and the technical docs.

## What Changes

- Persist a theme choice (`light`, `dark`, or the system setting) and a language choice (`en`, `es`, or the system language) in `localStorage`. Apply the theme before first paint. Honor `prefers-reduced-motion`.
- Translate the shell, the home page, and the AI Factory introduction. Other pages stay in English and say so when Spanish is selected. Keep English technical terms where a Spanish word would be worse.
- Add one factory reading order from the overview to the playbook.
- On the framework page, state what the kit on `main` can do (including the work merged in #2092 and #2127 and later merged kit work) and label planned work as planned. Counts of the migration inventory and the plugin allow-list are read from the repository at build time.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public numbers are read from the repository or from a generated snapshot that names its source and date. Nothing is invented. | CONSTITUTION.md §7 | Made explicit |
| AI Factory documentation stays separate from product documentation. | docs/openspec/specs/github-pages-ai-factory/spec.md | Made explicit |
| Text meets WCAG 2.1 AA in both themes. | docs/openspec/specs/github-pages-site-shell/spec.md | Changed |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-site-shell`: theme and language choices on the shared shell.
- `github-pages-ai-factory`: a beginner reading order, and a shipped-versus-planned account of the kit.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| github-page | yes | Shell, home, factory introduction, framework status, tests |
| docs | yes | This OpenSpec change, then the archived specs |

No backend, schema, or product UI change.

## Documentation Impact

- `github-page/README.md` — how theme and language work.
- `CHANGELOG.md` — `[Unreleased]`.
- OpenSpec main specs updated when this change is archived.
