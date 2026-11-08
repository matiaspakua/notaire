# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Auxiliary protocol start-deed handler is typed

`AuxiliaryProtocolController#startDeed` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Success stays 201 with `DeedResponse`. Bad request and not found stay 400 and 404 with `{"message": …}`.

#### Scenario: startDeed leaves the loose baseline

- **WHEN** the architecture suite runs
- **THEN** the baseline has no method lines and `startDeed` is not loosely typed

#### Scenario: Start deed keeps HTTP bodies

- **WHEN** the port returns Created, BadRequest, or NotFound
- **THEN** HTTP statuses and JSON bodies match the shapes clients already receive
