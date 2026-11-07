# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Testimony write handlers are typed

`TestimonyController#create`, `TestimonyController#update`, and `TestimonyController#delete` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Domain failures SHALL be mapped by `TestimonyExceptionHandler` with the same HTTP shapes clients already receive.

#### Scenario: Three Testimony write handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those three method descriptors are absent from the baseline and are not loosely typed
