# api-authorization Specification

## Purpose

Enforce on the server which authenticated users may use administrative API families. Source: #559; owner CU78.

## Requirements

### Requirement: The caller's authority comes from the stored user

The system MUST derive the authority of every authenticated request from the user stored in the database, granting administrator authority only to active users whose type is Administrador, Admin or Escribano (case-insensitive), and MUST reject a token whose user no longer exists or is inactive with 401.

#### Scenario: Administrator-capable types

- **WHEN** an active user of type Escribano, Admin or Administrador sends a valid token
- **THEN** the request carries administrator authority

#### Scenario: Other types

- **WHEN** an active user of type EMPLEADO sends a valid token
- **THEN** the request carries user authority only

#### Scenario: Removed or inactive user

- **WHEN** a valid token belongs to a deleted or inactive user
- **THEN** the response is 401

### Requirement: User, role and audit endpoints are administrator-only

The system MUST answer 403 to any non-administrator request to `/api/v1/usuarios` (except login and logout), `/api/v1/roles` and `/api/v1/audit-log`, for every method.

#### Scenario: Employee cannot manage users

- **WHEN** an EMPLEADO lists users or creates a user of type ESCRIBANO
- **THEN** the response is 403 and no user is created

#### Scenario: Employee cannot read roles or the audit log

- **WHEN** an EMPLEADO requests roles or the audit log
- **THEN** the response is 403

#### Scenario: Login and logout stay public

- **WHEN** an unauthenticated client posts to login or logout
- **THEN** the request is not rejected for authority

### Requirement: Administrative catalogs are read-only for non-administrators

The system MUST answer 403 to any non-administrator POST, PUT, PATCH or DELETE on workflow definitions, nodes and transitions, tipo-tramite, tipo-de-documento, tipo-folio, tipo-identificacion, conceptos, estado-gestion, plantilla-tramite, plantilla-presupuestos and plantilla-costos-documento, and MUST keep GET available to every authenticated user.

#### Scenario: Employee cannot change a catalog

- **WHEN** an EMPLEADO creates a tipo de trámite or deletes a concepto
- **THEN** the response is 403

#### Scenario: Employee can read a catalog

- **WHEN** an EMPLEADO lists tipos de trámite
- **THEN** the response is 200

#### Scenario: Administrator keeps full access

- **WHEN** an administrator creates a tipo de trámite and lists users
- **THEN** the responses are 201 and 200

### Requirement: The audit screen follows the same rule in the UI

The frontend MUST treat the audit dashboard route as an administrator route.

#### Scenario: Employee opens the audit screen

- **WHEN** an EMPLEADO navigates to the audit dashboard
- **THEN** the UI redirects to the dashboard with the forbidden message
