# presupuesto-from-template Specification

## Purpose

Create a presupuesto and pre-fill its items from a procedure-type template in one step. Source: #797; owner CU01.

## Requirements

### Requirement: Create with a procedure type

The create flow MUST create the budget first and then load the selected procedure type's template items into it, so the items persist as Item rows of the new budget.

#### Scenario: A type with template is selected

- **WHEN** the user creates a presupuesto selecting a procedure type that has a template
- **THEN** the budget is created and its template items appear in its items breakdown with their subtotal

#### Scenario: No type is selected

- **WHEN** the user creates a presupuesto without a procedure type
- **THEN** only the budget is created and no template is requested

### Requirement: A missing template does not fail the creation

The create flow MUST keep the created budget and report, without an error, that no items were loaded when the template step fails.

#### Scenario: A type without template is selected

- **WHEN** the user creates a presupuesto selecting a procedure type with no template
- **THEN** the budget exists, items are empty and the user is told that no template items were loaded
