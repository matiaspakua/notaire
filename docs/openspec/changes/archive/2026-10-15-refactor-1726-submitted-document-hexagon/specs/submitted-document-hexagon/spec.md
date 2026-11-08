## ADDED Requirements

### Requirement: Submitted documents are managed through domain-typed ports

The submitted-document HTTP adapter MUST call inbound ports only. Use cases MUST own transactions and due-date inheritance from the document type. Ports MUST speak domain types, not JPA entities.

#### Scenario: list and get return the same JSON shape

- **WHEN** a client lists or gets a submitted document
- **THEN** the response fields (`idSubmittedDocument`, `type`, `date`, `delivered`, `procedureId`) match the pre-refactor contract

#### Scenario: create requires type and procedure and inherits due fields

- **WHEN** a client creates a submitted document with a valid typeId and procedureId
- **THEN** the response is 201 and due/deliveredBy fields are inherited from the document type unless overridden

#### Scenario: create rejects invalid date or missing refs

- **WHEN** the date is not a real yyyy-MM-dd day, or typeId/procedureId is missing or unknown
- **THEN** the response is 400 or 404 as today

#### Scenario: partial update preserves omitted fields

- **WHEN** a PUT sends only some fields
- **THEN** omitted fields and flags stay unchanged and due date recomputes when entry date or type changes

#### Scenario: delete returns 204 or 404

- **WHEN** a client deletes an existing or missing submitted document
- **THEN** the response is 204 or 404 respectively (409 on persistence conflict as today)

#### Scenario: controller has no persistence imports

- **WHEN** ArchUnit R1 runs after the slice
- **THEN** `SubmittedDocumentController` has no baseline edges to `repository` or `business` entities
