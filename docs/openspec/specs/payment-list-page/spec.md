# payment-list-page Specification

## Purpose
The payment register is a growing table. The list endpoint returns one page.

## Requirements

### Requirement: Payments list is one page

`GET /api/v1/payments` SHALL return one page of payments. The default page is 0, the default size is 20, and the default sort is `idPayment,desc`. The body SHALL include `content` and `totalElements`. `page`, `size` and `sort` SHALL be documented query parameters.

#### Scenario: Default page

- **WHEN** a client requests `GET /api/v1/payments` with no query
- **THEN** the response is 200 and `content` holds at most 20 payments, newest id first, with `totalElements` counting every payment

#### Scenario: The list screen asks for that page

- **WHEN** the payments screen is opened with no budget and no date range
- **THEN** it requests `page`, `size` and `sort=idPayment,desc` and shows the pager
