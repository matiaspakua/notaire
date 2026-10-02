## Purpose

Ensure all monetary amounts in Notaire are exact decimal values (`BigDecimal` /
`NUMERIC`) so CU15 payment limits, budget totals, and installment sums do not
suffer binary floating-point rounding errors.

## ADDED Requirements

### Requirement: Monetary amounts MUST use BigDecimal at scale 2

Entity fields, shared DTOs, payment domain value objects, ports, and REST
request/response money fields SHALL use `java.math.BigDecimal` (not
`float`/`Float`/`double`) for currency amounts. Currency arithmetic SHALL use
scale 2 with `RoundingMode.HALF_UP` when scaling inputs. Comparisons SHALL use
`compareTo`, not `==` or `equals` alone for magnitude checks.

#### Scenario: Charge lines 0.1 and 0.2 sum exactly to 0.3
- **WHEN** a budget has two charge lines valued `0.1` and `0.2` with no
  percentages or document costs
- **THEN** `BudgetCharges.total()` equals `0.30` exactly (scale 2), not a
  binary-float approximation

#### Scenario: Overpayment against an exact tenths balance is rejected
- **WHEN** a budget total is `0.30`, payments of `0.10` and `0.20` are already
  recorded, and a client attempts another payment of `0.01`
- **THEN** the server rejects the payment with HTTP `409 Conflict` (same
  overpayment contract as #848)

### Requirement: Monetary database columns MUST be NUMERIC with explicit scale

All monetary columns listed in issue #1061 (including `document_types.amount_to_pay`)
SHALL be stored as `NUMERIC` with explicit precision/scale: currency
`NUMERIC(19,2)`; `variable_percentage` as `NUMERIC(7,4)`. A single forward
Flyway migration SHALL perform the conversion. Layout coordinates
(`workflow_nodes` position columns) SHALL remain non-monetary floating types.

#### Scenario: Payment amount column is NUMERIC scale 2 after migration
- **WHEN** Flyway applies the money migration on an empty or existing database
- **THEN** `payments.amount` (and peer money columns) are `numeric` with scale 2
  and schema validation passes

### Requirement: Payment receipt MUST format amounts from BigDecimal

`ReportService.generateReceiptPdf` (and related receipt formatting) SHALL format
the payment amount from `BigDecimal` at scale 2 without `String.valueOf(float)`
artifacts.

#### Scenario: Receipt text shows two decimal places for amount
- **WHEN** a payment receipt PDF is generated for amount `100.50`
- **THEN** the rendered amount text uses two decimal places derived from
  `BigDecimal`, not a float string artifact
