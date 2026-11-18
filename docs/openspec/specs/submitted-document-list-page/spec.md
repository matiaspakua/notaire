# submitted-document-list-page Specification

## Purpose
The submitted-document register is a growing table. The list endpoint returns one page.

## Requirements

### Requirement: Submitted documents list is one page

`GET /api/v1/submitted-documents` SHALL return one page. The default page is 0, the default size is 20, and the default sort is `idSubmittedDocument,desc`. The body SHALL include `content` and `totalElements`.

#### Scenario: Default page

- **WHEN** a client requests `GET /api/v1/submitted-documents` with no query
- **THEN** the response is 200 and `content` holds at most 20 documents, newest id first, with `totalElements` counting every document

#### Scenario: The list screen asks for that page

- **WHEN** the documents screen is opened
- **THEN** it requests `page`, `size` and `sort=idSubmittedDocument,desc` and shows the pager
