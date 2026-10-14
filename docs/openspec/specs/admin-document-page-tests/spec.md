# admin-document-page-tests Specification

## Purpose
TBD - created by archiving change test-1674-admin-document-page-tests. Update Purpose after archive.

## Requirements

### Requirement: Admin and document page rules are unit tested and line coverage meets the target

The frontend unit suite MUST assert that a document type that expires needs its due days and clearing the expiry clears them, that a concept conflict message is shown as the server sent it, that a role carries exactly the modules ticked and shows a rejected field inline, that a user keeps its role when edited, that an external document movement is sent to that document's endpoint, and that required documents load only after a procedure type is chosen. Frontend line coverage MUST be at least 80% and the Vitest coverage floors MUST be at least 76% statements, 68% branches, 72% functions and 78% lines.

#### Scenario: an expiring document type needs its due days

- **WHEN** a document type that expires is saved without due days
- **THEN** nothing is sent and the due-days error is shown

#### Scenario: a user keeps its role when edited

- **WHEN** a user with a role is edited and saved
- **THEN** the update is sent and the role is assigned again

#### Scenario: required documents wait for a procedure type

- **WHEN** the page opens
- **THEN** no template is requested and no print button is offered
