# Test Case: Prepare a budget successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-001-prepare-budget-success |
| **Title** | Prepare a budget successfully |
| **Name** | Prepare a budget successfully |
| **Reference** | CU01, RF #4 (Prepare budgets) |
| **Description** | Check that the system can create a budget for a registered person. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Success: record a budget** | The receptionist finds a valid person, selects a procedure type, and completes every required budget field. | The system calculates the total and confirms the budget with a unique number. |
| **Data validation** | Required fields (first name, last name, identification type, identification number) must not be empty. | The system shows validation errors and does not continue when data is missing. |
| **Unknown person** | The user tries to create a budget for a person id that does not exist. | The system reports that the person was not found. |
| **Notes** | The user enters extra notes while creating the budget. | The notes are stored on the budget. |
| **Total** | The system adds the amounts of the items for the selected procedure type. | The total shown is the sum of those concepts. |
