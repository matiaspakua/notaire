# steward-skill Specification

## Purpose
TBD - created by archiving change docs-1662-steward-skill. Update Purpose after archive.

## Requirements

### Requirement: A steward skill documents how to drive a PR to green

`.claude/skills/steward/SKILL.md` MUST have `name` and `description` frontmatter, MUST cover the required checks, the heavy-CI rule, the behind-`main` procedure, the commit, archive and push-verification rules, who may merge and known environment-only failures, and every repository path it cites MUST exist.

#### Scenario: skill has frontmatter

- **WHEN** the skill file is read
- **THEN** it starts with frontmatter naming `steward` and a non-empty description

#### Scenario: cited paths exist

- **WHEN** every backticked repository path in the skill is resolved
- **THEN** each exists in the repository

#### Scenario: skill covers the required topics

- **WHEN** the skill text is searched
- **THEN** it mentions the heavy-CI script, merging `main` instead of rebasing, archiving in the PR and that Workers never merge
