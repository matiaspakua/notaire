# backend-test-isolation Specification

## Purpose
Define backend full-suite test isolation rules so H2 payment integration tests
and SimpleControllers unit tests remain deterministic under
`mvn test -pl backend-api` (Issue #916), without weakening CU15/#848 overpayment
rejection or CU16 archive debt checks.
## Requirements
### Requirement: Payment-mutating H2 ITs arrange their own presupuesto

Integration tests under `backend-api` that POST a payment MUST create (or obtain)
a dedicated presupuesto fixture for that test or nested class. They MUST NOT
hardcode seed `idBudget=1` from `src/test/resources/data.sql` for payment
mutation. Read-only GETs against seed id `1` remain allowed.

#### Scenario: Payment create uses dedicated presupuesto after seed budget drained

- **WHEN** the seeded presupuesto id `1` has no remaining saldo pendiente
  (prior payments exhausted it in the shared H2 context)
- **AND** a payment-create test arranges its own presupuesto with positive saldo
- **THEN** creating a payment against that dedicated presupuesto succeeds
  (HTTP 2xx / 201) and does not depend on seed id `1`

#### Scenario: BusinessWorkflow payment create is fixture-isolated

- **WHEN** `BusinessWorkflowIntegrationTest` creates a payment as part of CU15
- **THEN** the request uses a presupuesto id created by the test (not literal `1`)

#### Scenario: RemainingControllers payment create is fixture-isolated

- **WHEN** `RemainingControllersIntegrationTest` creates a payment as part of CU15
- **THEN** the request uses a presupuesto id created by the test (not literal `1`)

### Requirement: SimpleControllers mega-tests are deterministic

`SimpleControllersTest` nested controller coverage MUST separate happy-path
assertions from stubs that throw `RuntimeException("x")` (or equivalent error
stubs) so error stubbing cannot affect happy-path expectations within the same
test method under full-suite ordering.

#### Scenario: SimpleControllers happy path is separate from RuntimeException stubs

- **WHEN** a nested controller suite in `SimpleControllersTest` covers both
  success and failure save paths
- **THEN** success paths run in a test method that does not stub
  `RuntimeException("x")` on the same mock interaction used for success
- **AND** failure paths run in a dedicated error-path test (or equivalent
  reset/fresh mock setup)

### Requirement: Overpayment product guard remains enforced

The CU15/#848 overpayment guard MUST remain active. Test fixes MUST arrange
fixtures rather than disabling or bypassing the guard.

#### Scenario: Overpayment still rejected when monto exceeds saldo

- **WHEN** a payment is registered with `monto` greater than the presupuesto's
  current saldo pendiente
- **THEN** the payment is rejected (existing #848 tests remain green)

