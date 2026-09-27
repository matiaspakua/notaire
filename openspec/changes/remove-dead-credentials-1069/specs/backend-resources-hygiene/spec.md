## Purpose

Ensures application resource files are free from dead default credential keys and legacy configuration files that pose security risks and confuse developers.

## ADDED Requirements

### Requirement: ApplicationPropertiesHygiene
The application SHALL reject startup properties that contain dead default security user keys.

#### Scenario: Reject dead security user keys
- **WHEN** application.properties is loaded as java.util.Properties
- **THEN** no key shall start with "spring.security.user." prefix

**Evidence**: `backend-api/src/main/resources/application.properties:92-94` — dead config with `spring.security.user.name=admin`, `spring.security.user.password=admin`, `spring.security.user.roles=ACTUATOR,ADMIN`

### Requirement: ApplicationResourceHygiene
The application SHALL reject resources that contain dead configuration files.

#### Scenario: Reject absent config files
- **WHEN** the classpath resource "/config.properties" is looked up
- **THEN** the resource must be absent (null)

**Evidence**: `backend-api/src/main/resources/config.properties` — legacy Swing-era file not referenced by any code
