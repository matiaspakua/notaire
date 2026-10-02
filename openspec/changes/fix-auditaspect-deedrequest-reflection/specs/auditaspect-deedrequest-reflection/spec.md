## MODIFIED Requirements

### Requirement: AuditAspectTest reflects DeedController request DTOs

`AuditAspectTest` SHALL resolve `DeedController.create` and `DeedController.update`
via reflection using the nested `DeedRequest` type (not `business.Deed`), matching
the live controller signatures after request-DTO binding.

#### Scenario: Create mutation reflection resolves

- **WHEN** `AuditAspectTest` looks up `DeedController.create` for an authenticated
  mutation audit assertion
- **THEN** reflection succeeds with `DeedController$DeedRequest` and the test
  exercises the aspect without `NoSuchMethodException`

#### Scenario: Update mutation reflection resolves

- **WHEN** `AuditAspectTest` looks up `DeedController.update` for the spoofed-header
  negative case
- **THEN** reflection succeeds with `(Integer, DeedController$DeedRequest)` and
  the test completes without `NoSuchMethodException`

#### Scenario: Skip-path create lookups still resolve

- **WHEN** skip-audit cases (no user / anonymous / user not found) look up `create`
- **THEN** reflection uses `DeedRequest` and those tests pass without method errors
