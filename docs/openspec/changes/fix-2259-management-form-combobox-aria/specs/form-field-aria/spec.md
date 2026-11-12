<!-- Governed by CONSTITUTION.md. CU76, issue #2259. -->

## ADDED Requirements

### Requirement: Searchable pickers carry the FormField error state

The shared `SearchCombobox` SHALL expose the enclosing FormField's error and required state on its input: `aria-invalid="true"` when the field has an error, `aria-describedby` pointing at the error element, and `aria-required="true"` when the field is required. Outside a FormField it SHALL add none of these attributes.

#### Scenario: invalid field

- **WHEN** a SearchCombobox is rendered in a FormField with an error
- **THEN** its input has aria-invalid true and aria-describedby resolving to the error text

#### Scenario: required field

- **WHEN** a SearchCombobox is rendered in a required FormField
- **THEN** its input has aria-required true

#### Scenario: no field context

- **WHEN** a SearchCombobox is rendered outside a FormField
- **THEN** its input has no aria-invalid attribute
