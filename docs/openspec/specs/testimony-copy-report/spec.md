# testimony-copy-report Specification

## Purpose
CU08 / RF-94 printed testimony copy: a verified testimony becomes a `ReportDocument` PDF of recorded testimony and deed fields. The system SHALL NOT invent special-sheet geometry, a signature block, or Owner official-form wording.

## Requirements

### Requirement: Verified copy content

`GET /api/v1/reports/testimonies/{idTestimony}/copies` SHALL return `application/pdf` for a verified testimony. Extracted text SHALL include the testimony number and, when present on the linked deed, the deed number, recording date, and deed body. Missing recorded values SHALL use the shared `N/A` fallback. The system SHALL NOT invent a signature, legal footer, or special-sheet legend.

#### Scenario: Verified testimony prints recorded deed body

- **WHEN** a verified testimony is linked to a signed deed whose body is `Signed deed for a testimony copy`
- **THEN** extracted PDF text contains the testimony number and that deed body

#### Scenario: Missing deed fields stay N/A

- **WHEN** a verified testimony has no linked deed body
- **THEN** the body field is `N/A` and the PDF still contains the testimony number

### Requirement: Unverified and missing stay rejected

An unverified testimony SHALL keep HTTP 400. A missing id SHALL keep HTTP 404. The system SHALL NOT return a placeholder 200 for those cases.

#### Scenario: Unverified testimony is 400

- **WHEN** a generated testimony has not been verified
- **THEN** `GET /api/v1/reports/testimonies/{id}/copies` is 400

#### Scenario: Unknown testimony is 404

- **WHEN** the id does not exist
- **THEN** the response is 404

### Requirement: Observed notes when recorded

When the testimony is flagged observed and notes are stored, the PDF SHALL include those notes. When not observed, notes are omitted or `N/A`.

#### Scenario: Observed notes appear

- **WHEN** a verified testimony is flagged with notes `Missing grantor signature`
- **THEN** extracted text contains that note string

### Requirement: UI download contract

The testimonies screen SHALL keep `btn-issue-copy-{id}` for verified rows only. Download SHALL call the existing copies path and use filename `testimonio_{id}_copia.pdf`.

#### Scenario: Issue-copy download contains deed text

- **WHEN** an authenticated user generates and verifies a testimony then clicks issue-copy
- **THEN** the browser receives a PDF named `testimonio_*_copia.pdf` that starts with `%PDF-` and extracted text contains the seeded deed body
