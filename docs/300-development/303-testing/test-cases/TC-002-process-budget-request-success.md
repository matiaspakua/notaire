# Test Case: Process a budget request

| Attribute | Detail |
|---|---|
| **ID** | TC-002-process-budget-request-success |
| **Title** | Process a budget request |
| **Name** | Process a budget request |
| **Reference** | CU01, RF #5 (Process a budget request) |
| **Description** | Check that the essential person data (first name, last name, identification, phone, email) is captured so a budget number can be issued and accepted later. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Basic data** | Enter every field required for a new budget request. | The system records the request and issues an identification number for the budget. |
| **Contact data** | Phone and email fields accept only valid formats. | The system checks the email and phone format before saving. |
| **Number** | The system assigns a unique, sequential budget number. | The system issues a valid budget number for the transaction. |
| **Identification** | Try to process a request without an identification type or number. | The system marks those fields as required and blocks submit. |
