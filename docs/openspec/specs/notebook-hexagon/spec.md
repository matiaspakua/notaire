# notebook-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1817-notebook-hexagon. Update Purpose after archive.

## Requirements

### Requirement: NotebookController is ports-only for CRUD

The notebook mixed controller SHALL call inbound Query/Manage ports for list/get/create. Entity access SHALL live behind NotebookOpsPort. Cover PDF MAY keep ReportService.

#### Scenario: Controller has no Notebook entity import
- **WHEN** ArchUnit R1 runs after this slice
- **THEN** NotebookController baseline edge to Notebook is absent

#### Scenario: REST contract unchanged
- **WHEN** clients call /api/v1/notebooks CRUD and cover
- **THEN** status codes and JSON/PDF shapes match the pre-slice behaviour
