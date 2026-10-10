# deprecated-owner-decision Specification

## Purpose

Make the #1261 / #1197 P0.6 Owner decision request visible and non-executable by agents until the Owner records a choice in ADR-022.

## ADDED Requirements

### Requirement: ADR-022 records pending Owner options for deprecated/ and history purge

The system SHALL document in ADR-022 a pending Owner decision for issue #1261 that lists options to (A) archive-tag and remove `deprecated/` from the tip, (B) also run a history purge, or (C) keep `deprecated/`, without selecting an option until the Owner decides.

#### Scenario: Pending section present

- **WHEN** a reader opens ADR-022
- **THEN** a section titled for the pending Owner decision references `#1261` and enumerates options A, B, and C

#### Scenario: No premature acceptance of a purge or deletion

- **WHEN** the pending Owner decision is still open
- **THEN** ADR-022 does not claim that `deprecated/` has been removed or that `git filter-repo` has been executed under #1261

### Requirement: Architecture docs surface the decision request

GitHub Pages Architecture documentation SHALL link ADR-022 so the Owner decision request is discoverable beside other topology ADRs.

#### Scenario: Pages link

- **WHEN** a reader opens the GitHub Pages Architecture docs page
- **THEN** they can navigate to ADR-022 (git history / large binaries)
