# Test Case: Print a budget successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-003-print-budget-success |
| **Title** | Print a budget successfully |
| **Name** | Print a budget successfully |
| **Reference** | CU01, RF #7 (Print budgets) |
| **Description** | Check that an existing budget can be printed on paper or downloaded as a PDF. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Print an existing budget** | Select a valid budget and request a print. | The system shows a preview and allows paper printing or a PDF download. |
| **Availability** | Try to print a budget that does not exist or was cancelled. | The system reports that the budget is not available to print. |
| **Print layout** | The printed information matches the stored data (name, date, concepts, totals). | The printed document matches the selected budget. |
| **Access** | Only users allowed to print can open the action. | The system blocks printing for users without that permission. |
