# entity-dto-mapping-ratchet Specification

## Purpose

Entities stop carrying mapping code to transport DTOs; the remaining debt is listed and can only shrink. Source: #577, #580, #1282; owner CU76.

## Requirements

### Requirement: Entities do not gain mapping methods

The `business` entities MUST NOT declare a `getDto`, `setAtributo`, `setAtributos` or `toDto` method that is not listed in the baseline `entity-dto-mapping.txt`.

#### Scenario: A new mapping method is rejected

- **WHEN** an entity declares a mapping method that is missing from the baseline
- **THEN** `EntityDtoMappingRatchetTest` fails and names the method

#### Scenario: A removed method must leave the baseline

- **WHEN** the baseline lists a mapping method that no entity declares
- **THEN** `EntityDtoMappingRatchetTest` fails so the baseline shrinks with the code

### Requirement: Dead mapping methods are deleted

A mapping method with no production caller MUST NOT exist.

#### Scenario: Production code does not need the deleted methods

- **WHEN** the backend main sources are compiled
- **THEN** compilation succeeds and the exported OpenAPI document is identical to the one on `main`
