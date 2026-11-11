## ADDED Requirements

### Requirement: The product story does not invent a measurement

The product story SHALL list each AI tool by the role it played. It SHALL NOT show a percentage of AI work unless a committed snapshot stores that share. The numbers page SHALL say the legacy `jpa` package and `JpaAdministrator` were retired, and SHALL NOT say that package is still being migrated.

#### Scenario: No unmeasured share

- **WHEN** a visitor opens the product story
- **THEN** the tools section has no "80%" share

#### Scenario: Legacy JPA is retired

- **WHEN** a visitor opens the product numbers page
- **THEN** the legacy JPA note says the package was retired and cites #576
