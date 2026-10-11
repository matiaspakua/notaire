# Test Case: Register document debts successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-009-register-document-debts-success |
| **Title** | Register document debts successfully |
| **Name** | Register document debts successfully |
| **Reference** | CU09 |
| **Description** | Check that a debt for a client's document can be recorded and followed. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Main flow** | Select a client and record a new debt for a specific document. | The system validates the information and stores the debt against that client and document. |
| **Unknown client** | Try to record a debt for a client who is not in the system. | The system reports that the client was not found. |
| **Amount** | Enter a negative or otherwise invalid debt amount. | The system requires a positive amount and blocks the save. |
| **Pending list** | The debt appears in the client's pending debts. | The system shows the debt in the client's document history. |
