# button-hover-contrast Specification

## Purpose

Filled buttons keep AA text contrast in the hover state.

## Requirements

### Requirement: Filled button hover keeps text contrast

The hover background of every filled button variant (default, apple, destructive), blended on white at its opacity, SHALL reach a contrast ratio of at least 4.5:1 against the variant's foreground colour.

#### Scenario: default and apple variants

- **WHEN** the hover background of the default and apple variants is computed from the design tokens
- **THEN** white text on it has at least 4.5:1 contrast

#### Scenario: destructive variant

- **WHEN** the hover background of the destructive variant is computed
- **THEN** its text contrast is at least 4.5:1
