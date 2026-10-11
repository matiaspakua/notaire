# presupuesto-from-template Specification

## Purpose

Create a `Budget` and pre-fill its items from a procedure-type template in
one step. Source: #797; owner CU01.

## Requirements

### Requirement: Create with a procedure type

The create flow MUST create the `Budget` first and then load the selected
procedure type's template items into it, so the items persist as `Item` rows
of the new budget. The load path is
`POST /api/v1/budgets/{id}/items-from-template` with `procedureTypeId`.

#### Scenario: A type with a template is selected

- **WHEN** the user creates a `Budget` selecting a procedure type that has a
  template
- **THEN** the budget is created and its template items appear in its items
  breakdown with their subtotal

#### Scenario: No type is selected

- **WHEN** the user creates a `Budget` without a procedure type
- **THEN** only the budget is created and no template is requested

### Requirement: A missing template does not fail the creation

The create flow MUST keep the created `Budget` and report, without an error,
that no items were loaded when the template step fails.

#### Scenario: A type without a template is selected

- **WHEN** the user creates a `Budget` selecting a procedure type with no
  template
- **THEN** the budget exists, items are empty, and the user is told that no
  template items were loaded
