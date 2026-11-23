# ai-sdlc-framework

## Modified Requirements

### Requirement: The review budget is checked on the diff

The framework MUST count added plus deleted lines since the base, excluding `[size] exclude` globs and binary files, and MUST fail `aisdlc size` when the count exceeds the budget. When a file is renamed, the framework MUST detect the rename and count only the edited lines (not the full file), reporting the file under its new path for `[size] exclude` matching.

#### Scenario: over the budget

- **WHEN** the counted lines exceed `[size] limit`
- **THEN** `aisdlc size` exits 1 and lists the largest files

#### Scenario: excluded files do not count

- **WHEN** only excluded paths make the diff large
- **THEN** `aisdlc size` exits 0 and reports the excluded count

#### Scenario: pure rename counts zero lines

- **WHEN** a file is renamed without any content changes
- **THEN** `aisdlc size` counts 0 lines for that file

#### Scenario: rename with edits counts only edited lines under new path

- **WHEN** a file is renamed and a few lines are modified
- **THEN** `aisdlc size` counts only the modified lines and reports the file under its new path
