# english-source-surface Specification

## Purpose
The live source surface is English: HTTP paths, dashboard routes, and database identifiers. The English domain model maps those identifiers to English names.
## Requirements
### Requirement: Public HTTP paths are English
The API SHALL expose its resources under English path segments. A request to a former Spanish segment SHALL NOT be served as that resource.

#### Scenario: Users login path is English
- **WHEN** a client calls `POST /api/v1/users/login` with valid credentials
- **THEN** the API authenticates the same way it did on `POST /api/v1/usuarios/login`

#### Scenario: Spanish resource segment is not mapped
- **WHEN** a controller mapping under `adapter.in.web` is scanned
- **THEN** none of its path literals contain a Spanish resource segment such as `usuarios`, `gestiones`, `tramites`, `presupuestos`, or `escrituras`

### Requirement: Dashboard routes are English
The web client SHALL address dashboard screens with English route segments.

#### Scenario: People screen lives at the English route
- **WHEN** a user opens the people screen
- **THEN** the route is `/dashboard/people`

#### Scenario: Navigation hrefs do not use Spanish segments
- **WHEN** the dashboard navigation declares an href
- **THEN** the href does not contain `personas`, `gestiones`, `presupuestos`, `escrituras`, or `administracion`

### Requirement: Remaining schema identifiers are English
Flyway SHALL rename the remaining Spanish table and columns in a new versioned migration. Applied migrations SHALL stay byte-for-byte unchanged. The JPA mappings SHALL use the English names.

#### Scenario: Identifications table is English
- **WHEN** the schema has applied `V45`
- **THEN** person identifications are stored in `identifications` with columns `number`, `fk_id_person`, and `fk_id_identification_type`

#### Scenario: Applied migrations are immutable
- **WHEN** this change is reviewed
- **THEN** no file matching `V1` through `V44` under `db/migration` differs from `main`

### Requirement: Localization catalogs stay bilingual
The change SHALL keep the Spanish message catalog and the English message catalog. Stored domain vocabulary SHALL stay as recorded.

#### Scenario: Spanish catalog is still the Spanish locale
- **WHEN** the language switcher selects Spanish
- **THEN** the `es` catalog still supplies Spanish UI copy

