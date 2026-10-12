# procedure-template-overview — delta

## Purpose

Listing every required-document template across procedure types.

## MODIFIED Requirements

### Requirement: All templates overview

The templates page SHALL list every procedure template from GET /procedure-templates with procedure and document names, SHALL filter them client-side by procedure and document, and each row SHALL open that procedure type's required-documents editor.

#### Scenario: Listing

- **WHEN** templates exist
- **THEN** every row shows procedure and document names

#### Scenario: Filter

- **WHEN** a procedure or document filter
- **THEN** only matching rows remain; none → empty message

#### Scenario: Jump

- **WHEN** a row link is followed
- **THEN** the procedure's Documentos requeridos dialog opens

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** filter and row link visible
