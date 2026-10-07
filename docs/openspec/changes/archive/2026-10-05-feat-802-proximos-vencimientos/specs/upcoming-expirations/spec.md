<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Report the submitted documents that are about to expire. Source: #802; owner CU42.

## ADDED Requirements

### Requirement: Documents inside the window are reported

The system MUST return every expiring, unreleased submitted document whose due date is between today and today plus the window, inclusive, ordered by due date ascending, with the CU42 data.

#### Scenario: A document due inside the window

- **WHEN** an expiring document has a due date 10 days from today and the window is 30 days
- **THEN** it is returned with its name, management number and heading, due date and 10 days remaining

#### Scenario: Several documents inside the window

- **WHEN** two expiring documents are due in 20 and 5 days
- **THEN** the one due in 5 days comes first

### Requirement: Other documents are not reported

The system MUST NOT return documents that do not expire, have no due date, are already released, are overdue, or are due after the window.

#### Scenario: Outside or irrelevant documents

- **WHEN** documents are due after the window, before today, released, non-expiring or without due date
- **THEN** none of them is returned

### Requirement: The window is validated

The system MUST reject with 400 a window smaller than 1 or larger than 365 days and MUST use 30 days when none is given.

#### Scenario: Invalid window

- **WHEN** the endpoint is called with dias=0 or dias=366
- **THEN** the response is 400

#### Scenario: Default window

- **WHEN** the endpoint is called without dias
- **THEN** a 30-day window is applied

### Requirement: The UI lists the upcoming expirations

The dashboard MUST show the upcoming expirations for a selectable window and an empty message when there are none.

#### Scenario: The screen lists the documents

- **WHEN** a document is due in 10 days and the user opens the screen
- **THEN** the document appears in the table with its due date
