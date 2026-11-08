# Type AuxiliaryProtocolController#startDeed (#577)

| GitHub Issue | #577 |
| Use Case | CU81 |
| Branch | `refactor/577_typed-auxiliary-start-deed` |

## Objetivo

Type `startDeed` as `ResponseEntity<DeedResponse>`; map errors via `AuxiliaryProtocolStartDeedExceptionHandler`. Empty the loose-return baseline.

## Capabilities

### Modified Capabilities

- `controller-typed-returns`: final baseline line removed

## Reglas de negocio

| Rule | Source |
|------|--------|
| Success 201 `DeedResponse` | CU81 |
| Errors 400/404 `{"message"}` | existing |

## Impact Analysis

### Módulos afectados

| Module | Touched |
|--------|---------|
| `backend-api` | yes |

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/notebook/*`
- `backend-api/src/main/java/com/licensis/notaire/domain/notebook/*`
- `backend-api/src/test/**`
- `CHANGELOG.md`

## Documentation Impact

| Document | Change |
|----------|--------|
| `CHANGELOG.md` | Refs #577 |
| `controller-typed-returns` spec | startDeed requirement |

## Out of Scope

Closing #577 epic DoD beyond typed-return baseline.
