## ADDED Requirements

### Requirement: Unreleased CHANGELOG must not auto-close issues

The `[Unreleased]` section of `CHANGELOG.md` SHALL NOT contain a `closes #<issue-number>` footer (case-insensitive). Release Please promotes Unreleased text into release PRs that GitHub treats as closing references.

#### Scenario: Unreleased has no closes issue footer

- **GIVEN** `CHANGELOG.md` has an `[Unreleased]` section
- **WHEN** the changelog structure docs test runs
- **THEN** that section contains no `closes #<digits>` match
