## ADDED Requirements

### Requirement: A fixed path does not cover a parameterized sibling

When several OpenAPI templates fit one frontend literal, the reachability scan SHALL keep only the templates with the fewest path parameters. A call to a fixed segment such as `/payments/date` SHALL cover `GET /payments/date` and SHALL NOT cover `GET /payments/{id}`.

#### Scenario: Date route is not a call to the id route

- **WHEN** the scanner reads `apiGet('/payments/date?from=1')`
- **THEN** `GET /api/v1/payments/date` is covered and `GET /api/v1/payments/{id}` is not
