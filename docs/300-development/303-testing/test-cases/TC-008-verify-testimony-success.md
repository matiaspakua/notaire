# Test Case: Verify a testimony successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-008-verify-testimony-success |
| **Title** | Verify a testimony successfully |
| **Name** | Verify a testimony successfully |
| **Reference** | CU08 |
| **Description** | Check that the status and details of an existing testimony can be reviewed. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Main flow** | Find a testimony by number, or by the deed it belongs to, and open its details. | The system shows the testimony details, including status, dates, and records. |
| **Not found** | Look up a testimony number that is not in the database. | The system reports that nothing was found for that number. |
| **Status** | The current testimony status is shown clearly. | The system shows the current status in a readable form. |
| **Dates** | Registration and presentation dates are readable. | The system shows the dates and the other details the user asked for. |
