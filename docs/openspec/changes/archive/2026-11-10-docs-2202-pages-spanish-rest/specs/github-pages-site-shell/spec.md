## MODIFIED Requirements

### Requirement: Language follows the visitor and can be chosen

The shell SHALL offer English, Spanish, and the system language. The default SHALL follow the browser language until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.lang`. The shell, the home page, the product section, the security section, the AI Factory, the about page, and the technical-docs reading path (`/docs/`, `/docs/artifacts/`, `/docs/architecture/`, `/docs/engineering/`, `/docs/testing/`, `/docs/releases/`) SHALL render in the selected language. A page that is not in that set SHALL stay in English and, when Spanish is selected, SHALL say that it is still in English. English technical terms (Issue, pull request, gate, OpenSpec, Flyway, commit, kit, Owner) SHALL stay in English. Spanish copy SHALL come from one dictionary keyed by the English sentence.

#### Scenario: Spanish shell

- **WHEN** a visitor selects Spanish
- **THEN** the primary navigation shows "Documentación técnica" for `/docs/` and the home page heading is in Spanish

#### Scenario: Spanish product page

- **WHEN** Spanish is selected and the visitor opens `/product/`
- **THEN** the page heading is in Spanish and the page does not say that it is still in English

#### Scenario: Untranslated page

- **WHEN** Spanish is selected and the visitor opens `/docs/hexagonal/`
- **THEN** the page body stays in English and a notice says it is still in English
