# payments-page-tests Specification

## Purpose
TBD - created by archiving change test-2114-payments-page-tests. Update Purpose after archive.

## Requirements

### Requirement: The payments page rules are unit tested

The frontend unit suite MUST assert that a payment needs a budget, a date and an amount, that an edit is saved with PUT without the budget, that a conflict without a reason explains the exceeded balance, and that the list narrows by budget and by an inclusive date range.

#### Scenario: an exceeded balance is explained

- **WHEN** a save is rejected with 409 and no reason
- **THEN** the exceeded-balance message is shown

#### Scenario: a range that ends before it starts lists nothing

- **WHEN** the start day is after the end day
- **THEN** the range error is shown and no payment is listed
