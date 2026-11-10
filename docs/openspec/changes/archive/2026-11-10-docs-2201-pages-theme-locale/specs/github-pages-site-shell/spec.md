## ADDED Requirements

### Requirement: Theme follows the visitor and can be chosen

The shell SHALL offer light, dark, and system theme. The default SHALL follow `prefers-color-scheme` until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.theme` and applied on the document element before first paint. A theme change SHALL NOT animate when `prefers-reduced-motion: reduce` is set. Text and controls SHALL meet WCAG 2.1 AA contrast in both themes.

#### Scenario: Dark theme is saved

- **WHEN** a visitor chooses dark and reloads the page
- **THEN** the document element has `data-theme="dark"` before the body is painted and the choice remains dark

#### Scenario: Reduced motion

- **WHEN** the visitor's system asks for reduced motion
- **THEN** the theme change does not run a color transition

### Requirement: Language follows the visitor and can be chosen

The shell SHALL offer English, Spanish, and the system language. The default SHALL follow the browser language until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.lang`. The shell, the home page, and the AI Factory overview SHALL render in the selected language. A page that is not translated SHALL stay in English and, when Spanish is selected, SHALL say that it is still in English. English technical terms (Issue, pull request, gate, OpenSpec, Flyway, commit, kit, Owner) SHALL stay in English.

#### Scenario: Spanish shell

- **WHEN** a visitor selects Spanish
- **THEN** the primary navigation shows "Documentación técnica" for `/docs/` and the home page heading is in Spanish

#### Scenario: Untranslated page

- **WHEN** Spanish is selected and the visitor opens a technical-doc page other than the pages translated in this change
- **THEN** the page body stays in English and a notice says it is still in English
