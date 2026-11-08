## ADDED Requirements

### Requirement: The document re-entry page rules are unit tested

The frontend unit suite MUST assert that a document is re-entered for its own procedure, that due days show only for a document that expires, that a procedure without documents says so, and that a failed re-entry is reported.

#### Scenario: a document is re-entered for its procedure

- **WHEN** the re-enter action of a document is used
- **THEN** the management, procedure and document type are posted together

#### Scenario: due days show only when the document expires

- **WHEN** a document does not expire but carries due days
- **THEN** the due days are not shown
