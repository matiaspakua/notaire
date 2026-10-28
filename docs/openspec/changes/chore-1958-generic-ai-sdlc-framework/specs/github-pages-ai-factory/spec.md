<!-- Governed by CONSTITUTION.md. CU76, issue #1958. -->

## ADDED Requirements

### Requirement: The AI Factory tells the Notaire case study step by step

The AI Factory section MUST include a `/ai-sdlc/case-study/` page that walks from the problem to the solution in ordered chapters, shows the repository history as data read from a committed snapshot with provenance, and pairs each chapter with real evidence: an issue, a file in the repository, a before/after example or a code excerpt.

#### Scenario: the case study page exists and is linked

- **WHEN** the site is built
- **THEN** `/ai-sdlc/case-study/` renders and the section navigation links it

#### Scenario: history comes from a snapshot with provenance

- **WHEN** the case study shows commits, merged PRs or bug issues per month
- **THEN** the numbers come from `github-page/lib/ai-sdlc-history.json`, which records when and by which command it was generated

### Requirement: The AI Factory explains the reusable framework

The AI Factory section MUST include a `/ai-sdlc/framework/` page that presents the stack-agnostic framework (components, stages and gates, adoption levels, profile example) and links the framework report and kit in the repository.

#### Scenario: the framework page cites the kit

- **WHEN** the framework page is built
- **THEN** it cites `framework/ai-sdlc/` and the report through `src()` so the Pages test proves both exist

#### Scenario: animation respects reduced motion

- **WHEN** a visitor prefers reduced motion
- **THEN** the animated gate flow renders as a static diagram
