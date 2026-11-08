# controller-typed-returns Delta

## ADDED Requirements

### Requirement: DocumentCostTemplate createCost handler is typed

`DocumentCostTemplateController#createCost` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: DocumentCostTemplate createCost leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `DocumentCostTemplateController#createCost` is absent from the baseline and is not loosely typed
