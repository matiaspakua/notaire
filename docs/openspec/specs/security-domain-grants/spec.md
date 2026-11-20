# security-domain-grants Specification

## Purpose
TBD - created by archiving change fix-2351-security-grants. Update Purpose after archive.

## Requirements

### Requirement: Security grants use domain queries

Login authority and module grants SHALL be derived from the domain user and role queries. An inactive user SHALL receive neither an authority nor grants. A user with no role SHALL receive the grants of the type template.

#### Scenario: an assigned role supplies the modules

- **WHEN** an active user has a role whose modules are personas and presupuestos
- **THEN** the grants are those two modules

#### Scenario: an inactive user has no authority

- **WHEN** the stored user is inactive
- **THEN** authority resolution is empty
