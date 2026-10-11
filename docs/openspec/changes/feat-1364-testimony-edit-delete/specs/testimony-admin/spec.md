# testimony-admin — delta

## Purpose

Testimonies can be corrected and deleted from the UI, and their movement history is visible.

## MODIFIED Requirements

### Requirement: Testimony edit, delete and history

The testimonies page SHALL let a user edit a testimony's number and notes, delete a testimony after confirmation, and view its movements newest first; the API SHALL answer 409 and keep the testimony when it has registry movements or copies.

#### Scenario: Edit

- **WHEN** a user corrects the number and notes
- **THEN** PUT sends number, notes, flagged, verified and the version read, and the list shows the new number

#### Scenario: Delete

- **WHEN** a testimony without movements is deleted after confirmation
- **THEN** DELETE answers 204 and GET answers 404

#### Scenario: In use

- **WHEN** the testimony has a registry movement
- **THEN** DELETE answers 409, the page says why, and the movement is kept

#### Scenario: History

- **WHEN** a user opens Ver movimientos
- **THEN** the movements show newest first with the withdrawal card number, or an empty message
