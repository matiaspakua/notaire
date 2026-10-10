# ui-i18n — delta

## Purpose

Localized UI strings.

## MODIFIED Requirements

### Requirement: UI strings are localized

Every user-visible and assistive-technology string in src/app and src/components SHALL come from messages/es.json and messages/en.json; the breadcrumb SHALL label every static dashboard route in both locales.

#### Scenario: English breadcrumb

- **WHEN** the locale is en
- **THEN** the breadcrumb of an administration page reads Home / Administration / Users

#### Scenario: Literal re-added

- **WHEN** a page adds placeholder="Seleccionar tipo"
- **THEN** the static scan fails

#### Scenario: English screens

- **WHEN** an admin browses pagos, copias, protocolo and usuarios in en
- **THEN** headers, buttons and badges are English
