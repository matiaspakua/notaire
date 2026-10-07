# admin-bootstrap — delta

## Purpose

Initial administrator seeding at startup.

## MODIFIED Requirements

### Requirement: No default admin password outside dev/test

The seeder SHALL NOT create the admin user with a blank or `admin` password unless `app.environment` is development, dev, local or test.

#### Scenario: Default password in staging

- **WHEN** environment is staging and APP_ADMIN_PASSWORD is admin
- **THEN** no user is created and an error is logged

#### Scenario: Blank password in production

- **WHEN** environment is production and the password is blank
- **THEN** no user is created

#### Scenario: Custom password in staging

- **WHEN** environment is staging and a non-default password is set
- **THEN** the admin user is created with that password hashed

#### Scenario: Default password in test

- **WHEN** environment is test and the password is admin
- **THEN** the admin user is created as before
