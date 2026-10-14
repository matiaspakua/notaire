<!-- Governed by CONSTITUTION.md. CU78, issue #1639. Evidence: ProductionCredentialsGuard rejects only the literal "admin"; .env.example ships ACTUATOR_PASSWORD=admin. -->

## ADDED Requirements

### Requirement: Production refuses weak actuator credentials

Under `app.environment=production` the backend MUST fail startup when `actuator.security.password` is empty, shorter than 16 characters, on the deny-list (`admin`, `password`, `changeme`, `secret`, `root`, case-insensitive) or starting with the `change-me` placeholder, or when `actuator.security.username` is empty or on the deny-list. Other environments MUST NOT be checked.

#### Scenario: short actuator password rejected

- **WHEN** the environment is production and the actuator password has fewer than 16 characters
- **THEN** startup fails naming `actuator.security.password`

#### Scenario: deny-listed actuator password rejected

- **WHEN** the environment is production and the actuator password is long enough but starts with the `change-me` placeholder shipped in `.env.example`
- **THEN** startup fails naming `actuator.security.password`

#### Scenario: empty actuator credentials rejected

- **WHEN** the environment is production and the actuator user or password is empty
- **THEN** startup fails naming the empty property

#### Scenario: strong actuator credentials accepted

- **WHEN** the environment is production and the user is not deny-listed and the password has at least 16 characters and is not deny-listed
- **THEN** startup succeeds

#### Scenario: development is not checked

- **WHEN** the environment is development and the actuator password is `admin`
- **THEN** startup succeeds
