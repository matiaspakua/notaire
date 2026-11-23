## RENAMED Requirements

- FROM: `### Requirement: The dashboard reads and writes only through the factory-control contract`
- TO: `### Requirement: The dashboard reads and writes only through the aisdlc factory contract`

## ADDED Requirements

### Requirement: The dashboard is branded AI SDLC Factory

The dashboard SHALL name itself **AI SDLC Factory** in the page title, the app name of every locale and its server
start message. It SHALL NOT use the name of another factory product.

#### Scenario: the app name in both locales

- **WHEN** the UI renders in English or Spanish
- **THEN** the app name is `AI SDLC Factory`
