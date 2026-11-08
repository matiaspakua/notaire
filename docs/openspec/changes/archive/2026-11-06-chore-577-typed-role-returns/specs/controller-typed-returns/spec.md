# controller-typed-returns Delta

## ADDED Requirements

### Requirement: Role create and assign handlers are typed

`RoleController#createRole` and `RoleController#assignRoleToUser` SHALL NOT appear in `controller-loose-returns.txt`. Their declared return types SHALL NOT be `ResponseEntity<Object>`. Domain conflict/not-found exceptions SHALL be mapped by advice with the same HTTP shapes as before.

#### Scenario: Two role handlers leave the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** those two method descriptors are absent from the baseline and are not loosely typed

#### Scenario: Role name conflict create returns 409 error body

- **GIVEN** manage.create throws RoleNameConflictException
- **WHEN** POST /api/v1/roles
- **THEN** the response is 409 with `{"error": …}`
