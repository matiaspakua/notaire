## MODIFIED Requirements

### Requirement: Language follows the visitor and can be chosen

The shell SHALL offer English, Spanish, and the system language. The default SHALL follow the browser language until the visitor chooses. The choice SHALL be stored in `localStorage` under `notaire.lang`. Every page in the public site map — the shell, the home page, the product, security, the AI Factory, about, and every technical-docs page — SHALL render in the selected language. That includes sidebar titles, anatomy figure plate copy and steps, Mermaid sentence labels, and Diagram captions on those routes. Identifiers (class names, commands, file paths) and the English technical terms Issue, pull request, gate, OpenSpec, Flyway, commit, kit and Owner SHALL stay in English. A route that is not in that set SHALL stay in English and, when Spanish is selected, SHALL say that it is still in English. Spanish copy SHALL come from one dictionary keyed by the English sentence.

#### Scenario: Spanish shell

- **WHEN** a visitor selects Spanish
- **THEN** the primary navigation shows "Documentación técnica" for `/docs/` and the home page heading is in Spanish

#### Scenario: Spanish product page

- **WHEN** Spanish is selected and the visitor opens `/product/`
- **THEN** the page heading is in Spanish and the page does not say that it is still in English

#### Scenario: Spanish hexagonal page

- **WHEN** Spanish is selected and the visitor opens `/docs/hexagonal/`
- **THEN** the page heading is in Spanish and the page does not say that it is still in English

#### Scenario: Anatomy figure follows Spanish

- **WHEN** a visitor selects Spanish and opens `/product/anatomy/`
- **THEN** the anatomy plate title and the step readout are Spanish, and the page does not show the English-notice banner

#### Scenario: Mermaid sentence labels follow Spanish

- **WHEN** a visitor selects Spanish and opens a factory page that renders a Mermaid diagram with sentence labels
- **THEN** those sentence labels are Spanish while commands and file paths in the same diagram stay as in the repository

#### Scenario: Playwright checks Spanish headings on anatomy and lifecycle

- **WHEN** Pages Playwright runs the Spanish preference suite at 375 and 1280 px in light and dark
- **THEN** it finds a Spanish heading on product anatomy and on the factory lifecycle pages, and those pages do not show the English-notice banner
