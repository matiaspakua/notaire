# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Concept write handlers are typed

`ConceptController#createConcept`, `ConceptController#updateConcept`, and `ConceptController#deleteConcept` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`.

#### Scenario: Three Concept write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed
