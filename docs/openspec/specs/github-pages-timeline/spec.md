# github-pages-timeline Specification

## Purpose
TBD - created by archiving change docs-1400-github-page-timeline. Update Purpose after archive.
## Requirements
### Requirement: Prior timeline eras remain visible

The Pages Project History section MUST retain the existing eras from 2014 through May–August 2026 without deleting or replacing their titles, subtitles, or core narrative.

#### Scenario: Decade narrative intact after update

- **WHEN** a visitor opens the GitHub Pages home timeline
- **THEN** the Origin, Dormancy, Renaissance, API & Frontend, AI Acceleration, and Production Ready eras are still present

### Requirement: Sept–Oct 2026 progress is appended

The timeline MUST include at least one new era covering September–October 2026 that mentions the Cursor Cloud AI SDLC fleet (or equivalent Cloud agent delivery) and OpenSpec / heavy-CI process hardening, in English.

#### Scenario: New era appears after Production Ready

- **WHEN** a visitor scrolls the Project History timeline
- **THEN** a Sept–Oct 2026 (or "Autumn 2026") entry appears after the May–August 2026 Production Ready entry

### Requirement: Deploy workflow unchanged

This change MUST NOT modify `.github/workflows/deploy-github-page.yml`.

#### Scenario: Workflow file clean in the PR

- **WHEN** the pull request diff is reviewed
- **THEN** `deploy-github-page.yml` has no hunks

