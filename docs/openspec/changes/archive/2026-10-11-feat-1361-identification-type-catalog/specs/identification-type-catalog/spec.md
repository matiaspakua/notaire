# identification-type-catalog — delta

## Purpose

ADMIN maintains the identification types the person form offers.

## ADDED Requirements

### Requirement: Identification type admin catalog

The administration area SHALL offer ADMIN users a page that lists, creates, edits and deletes identification types (name and number length); the API SHALL refuse with 409 a name that another type already has, ignoring letter case and surrounding spaces; the API SHALL refuse with 409 to delete a type that any person uses and SHALL never delete people when a type is removed; non-admin users SHALL not reach the page.

#### Scenario: Admin maintains a type

- **WHEN** an ADMIN creates a type with a name and a number length, changes the length and deletes it
- **THEN** the list shows the new type with its length, then the new length, then no longer lists it

#### Scenario: A type people use is not deleted

- **WHEN** an ADMIN deletes a type that a person uses
- **THEN** the API answers 409 with the in-use reason, the page shows the in-use message, and the type and the person remain

#### Scenario: Duplicate name is rejected

- **WHEN** an ADMIN creates a type named " dni " while "DNI" exists
- **THEN** the API answers 409, the page says the name already exists, and the catalog keeps a single DNI

#### Scenario: Non-admin gets no catalog

- **WHEN** an EMPLEADO opens /dashboard/administration/identification-types
- **THEN** the app redirects to /dashboard?forbidden=1 and no catalog actions are shown
