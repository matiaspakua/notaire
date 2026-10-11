# descuentos-recargos-presupuesto Specification

## Purpose

Allows classifying a budget item as a discount or a surcharge with a
mandatory reason, and reflects that sign in the budget total and in a
readable report (CU45, CU71).

## Requirements

### Requirement: Classify a budget item by type
The system SHALL allow classifying a budget item as `NORMAL`, `DESCUENTO`,
or `RECARGO` (stored on `item_type`). An item without an explicit type
SHALL behave as `NORMAL`.

#### Scenario: Create a normal item without an explicit type
- **WHEN** a budget item is created without specifying a type
- **THEN** the system treats it as type "NORMAL"

#### Scenario: Create a discount item
- **WHEN** a budget item is created with type "DESCUENTO" and a valid
  `reason`
- **THEN** the system accepts the item classified as a discount

#### Scenario: Create a surcharge item
- **WHEN** a budget item is created with type "RECARGO" and a valid
  `reason`
- **THEN** the system accepts the item classified as a surcharge

### Requirement: Require a structured reason on discounts and surcharges
The system SHALL reject creating or updating an item of type `DESCUENTO`
or `RECARGO` when it does not include a non-empty `reason`.

#### Scenario: Reject a discount without a reason
- **WHEN** creating an item of type "DESCUENTO" without a `reason` is
  attempted
- **THEN** the system rejects the operation with a validation error

#### Scenario: Reject a surcharge without a reason
- **WHEN** creating an item of type "RECARGO" without a `reason` is
  attempted
- **THEN** the system rejects the operation with a validation error

#### Scenario: Accept a normal item without a reason
- **WHEN** an item of type "NORMAL" is created without a `reason`
- **THEN** the system accepts the item without requiring a reason

### Requirement: Calculate the budget total from the item type
The system SHALL calculate a budget total by adding items of type
`NORMAL` and `RECARGO`, and subtracting items of type `DESCUENTO`.

#### Scenario: Total with a discount item
- **WHEN** a budget has normal items and an item of type `DESCUENTO`
- **THEN** the budget total subtracts the discount item's value

#### Scenario: Total with a surcharge item
- **WHEN** a budget has normal items and an item of type `RECARGO`
- **THEN** the budget total adds the surcharge item's value

#### Scenario: Total with neither discounts nor surcharges
- **WHEN** a budget has only items of type `NORMAL`
- **THEN** the budget total is the sum of all items

### Requirement: Read a budget's discounts and surcharges
The system SHALL allow reading, for a given budget, the list of its
`DESCUENTO` and `RECARGO` items together with each `reason`. The read is
`GET /api/v1/items/budget/{idBudget}/discounts-surcharges`.

#### Scenario: Read discounts and surcharges of a budget that has both
- **WHEN** the discount and surcharge report of a budget that has at
  least one item of each type is read
- **THEN** the system responds with the list of those items and their
  reason

#### Scenario: Read discounts and surcharges of a budget that has none
- **WHEN** the discount and surcharge report of a budget that has only
  normal items is read
- **THEN** the system responds with an empty list

#### Scenario: Read discounts and surcharges of a budget that does not exist
- **WHEN** the discount and surcharge report of a budget whose id does
  not exist is read
- **THEN** the system responds with a not-found error
