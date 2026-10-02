## ADDED Requirements

### Requirement: Budget template DTO create hydrates JPA associations

`POST /api/v1/plantilla-presupuestos` SHALL accept a request DTO with
`fkIdProcedureType` and `fkIdConcept` (or nested `budgetTemplatePK`) and SHALL
set `BudgetTemplate.concept` and `BudgetTemplate.procedureType` from those IDs
before calling legacy `BudgetTemplateJpaController.create`, so create does not
NPE on null associations.

#### Scenario: Create template with DTO IDs returns 201

- **WHEN** a client POSTs a budget template body with valid `fkIdProcedureType`
  and `fkIdConcept`
- **THEN** the API returns HTTP 201 and persists the template for that PK

#### Scenario: Duplicate create returns 409

- **WHEN** a client POSTs a template for a procedure-type/concept pair that
  already exists
- **THEN** the API returns HTTP 409 Conflict

#### Scenario: Missing FK returns 400 not 500

- **WHEN** a client POSTs a template referencing a non-existent concept or
  procedure type id
- **THEN** the API returns HTTP 400 and does not return HTTP 500 from an NPE
