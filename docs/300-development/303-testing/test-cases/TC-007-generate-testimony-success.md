# Test Case: Generate a testimony successfully

| Attribute | Detail |
|---|---|
| **ID** | TC-007-generate-testimony-success |
| **Title** | Generate a testimony successfully |
| **Name** | Generate a testimony successfully |
| **Reference** | CU07 |
| **Description** | Check that a testimony can be generated for an approved deed. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Main flow** | Select a signed deed and generate its testimony. | The system creates the testimony for that deed and stores it. |
| **Status check** | Try to generate a testimony for a deed that is not signed. | The system reports that the deed must be signed before a testimony can be generated. |
| **Copy count** | The number of testimony copies can be set. | The system accepts the copy count and stores it. |
| **Data** | The generated testimony matches the deed. | The system shows the testimony data linked to the correct deed. |
