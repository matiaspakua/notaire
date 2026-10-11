# Test Case: Sign a deed successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-006-sign-deed-success |
| **Title** | Sign a deed successfully |
| **Name** | Sign a deed successfully |
| **Reference** | CU06 |
| **Description** | Check that the notary and the clients taking part can record the signature of a deed. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Main flow** | Select a prepared deed and mark it signed by every required party. | The system records the signature and sets the deed status to `Firmada`. |
| **Missing signatures** | Try to finish when one required party has not signed. | The system reports the validation errors and does not finish until every signature is recorded. |
| **Signature record** | The record stores the date and the user who performed the action. | The system stores the signature date and the user who authorized it. |
| **Signed deeds list** | A signed deed appears in the list of signed deeds. | The signed deed is visible in that list. |
