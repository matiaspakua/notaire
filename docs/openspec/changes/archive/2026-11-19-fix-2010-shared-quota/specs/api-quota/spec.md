## MODIFIED Requirements

### Requirement: API calls have a per-caller quota

The API SHALL count requests to `/api/**` in a fixed window stored in `api_rate_windows`, shared by every API instance. The key SHALL be the authenticated username when a JWT has already been accepted, and the servlet remote address otherwise. When the count for that key exceeds the configured maximum, the API SHALL respond with HTTP 429 and a `Retry-After` header, and SHALL NOT invoke the rest of the chain. Login username lockout SHALL stay a separate control.

#### Scenario: A burst is rejected and a later window is allowed

- **WHEN** a caller exceeds the configured maximum inside one window and then calls again after that window
- **THEN** the excess call is rejected and the later call is allowed

#### Scenario: Another instance sees the same count

- **WHEN** one API instance has already counted a caller up to the maximum
- **THEN** another instance using the same table rejects the next call from that caller
