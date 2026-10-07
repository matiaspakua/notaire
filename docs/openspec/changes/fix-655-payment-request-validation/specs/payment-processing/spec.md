# payment-processing — delta

## Purpose

Recording and editing payments (CU15).

## MODIFIED Requirements

### Requirement: Payment request validation

The payment endpoints SHALL answer 400 for a missing budget or a non-positive amount without calling the use case.

#### Scenario: Create without budget or amount

- **WHEN** POST /api/v1/pagos omits idBudget or amount
- **THEN** 400 and the use case is not called

#### Scenario: Create with non-positive amount

- **WHEN** POST /api/v1/pagos has amount 0 or negative
- **THEN** 400

#### Scenario: Update with non-positive amount

- **WHEN** PUT /api/v1/pagos/{id} has amount 0 or negative
- **THEN** 400 (previously 404)

#### Scenario: Partial update without amount

- **WHEN** PUT /api/v1/pagos/{id} omits amount
- **THEN** the update proceeds as before
