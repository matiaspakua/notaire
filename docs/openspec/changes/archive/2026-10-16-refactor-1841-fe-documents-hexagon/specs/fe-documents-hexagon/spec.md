## ADDED Requirements

### Requirement: Document UI uses core ports only

Submitted-document, document-reentry, external-entity-document, and upcoming-expiration hooks SHALL call composition/use cases. HTTP gateways SHALL be the only paths that import `api-client` for these surfaces. Create/update payloads SHALL keep `typeId`, `procedureId`, `idDocumentType`, and movement fields unchanged.

#### Scenario: Document hooks leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for the eight document hook/test files

#### Scenario: REST behaviour unchanged
- **WHEN** clients CRUD submitted documents, reenter documentation, register external-entity movements, or list upcoming expirations
- **THEN** behaviour matches the pre-slice hooks