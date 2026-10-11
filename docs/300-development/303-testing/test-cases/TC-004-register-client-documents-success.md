# Test Case: Register a client's documents

| Attribute | Detail |
|---|---|
| **ID** | TC-004-register-client-documents-success |
| **Title** | Register a client's documents |
| **Name** | Register a client's documents |
| **Reference** | CU04 |
| **Description** | Check that technical and legal documents can be recorded for a client. |

## Test scenarios

| Scenario | Description | Expected result |
|---|---|---|
| **Main flow** | Select a client and record the associated documents. | The system validates the information and attaches the document to the client. |
| **File type** | Try to upload a document in a format that is not allowed. | The system reports that the file format is not allowed. |
| **Required fields** | Enter document data without the required fields. | The system blocks the save and shows the validation errors. |
| **Several documents** | More than one document can be recorded for the same client. | The system attaches each document to that client. |
