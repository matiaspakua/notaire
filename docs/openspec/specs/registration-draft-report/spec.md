# registration-draft-report Specification

## Purpose
CU82 / RF-91 printed registration draft: a generated draft becomes a `ReportDocument` PDF of recorded draft, deed, party, and property fields. The system SHALL NOT invent official-form geometry, a signature block, or Owner legal wording.

## Requirements

### Requirement: Printed draft content

`GET /api/v1/reports/registration-draft/{idRegistrationDraft}` SHALL return `application/pdf` for an existing draft. Extracted text SHALL include the draft number and, when present, deed number, parties, cadastral designation, registration number, volume/folio, boundaries, fiscal appraisal, address, and operation price. Missing recorded values SHALL use the shared `N/A` fallback. The system SHALL NOT invent a signature, legal footer, or official-form legend.

#### Scenario: Generated draft prints recorded cadastral designation

- **WHEN** a registration draft is linked to a deed whose procedure property has cadastral designation `NC-CU82-PRINT`
- **THEN** extracted PDF text contains the draft number and that cadastral designation

#### Scenario: Missing optional fields stay N/A

- **WHEN** a generated draft has no recorded operation price and no involved people
- **THEN** price and parties are `N/A` and the PDF still contains the draft number

### Requirement: Missing draft stays rejected

A missing id SHALL keep HTTP 404. The system SHALL NOT return a placeholder 200 for that case.

#### Scenario: Unknown draft is 404

- **WHEN** the id does not exist
- **THEN** the response is 404

### Requirement: Recorded registry notes when present

When the draft stores registry notes, the PDF SHALL include those notes. When empty, notes are `N/A`.

#### Scenario: Registry notes appear

- **WHEN** a draft has registry notes `Missing cadastral certificate`
- **THEN** extracted text contains that note string

### Requirement: UI download contract

The registration-drafts screen SHALL keep `btn-download-registration-draft` after a draft is generated. Download SHALL call the existing report path and use filename `minuta_inscripcion_{id}.pdf`.

#### Scenario: Download contains cadastral text

- **WHEN** an authenticated user generates a draft for a property with a unique cadastral designation then clicks download
- **THEN** the browser receives a PDF named `minuta_inscripcion_*.pdf` that starts with `%PDF-` and extracted text contains that cadastral designation
