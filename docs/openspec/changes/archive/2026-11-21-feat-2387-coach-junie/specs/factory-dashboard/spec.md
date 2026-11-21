<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## ADDED Requirements

### Requirement: The coach reads Junie CLI sessions

`aisdlc coach` MUST read Junie CLI session stores (`~/.junie/sessions/index.jsonl`,
per-session `events.jsonl`, and `~/.junie/history.jsonl`) and report them alongside
the other harnesses. The report MUST include session counts, prompt counts, tool
calls (one per unique `stepId`), and model usage. The report MUST NOT contain prompt
text. The reader MUST NOT read `EnvironmentVariablesUpdatedEvent` (contains secrets).

#### Scenario: Junie session appears in the coach report

- **WHEN** a Junie session index entry with matching events and history exists
- **THEN** the coach report includes a `"junie"` harness row with the session count, prompt count, tool calls and models

#### Scenario: Junie prompts matched by project path

- **WHEN** `history.jsonl` contains prompts for multiple projects
- **THEN** only prompts whose `projectPath` matches the session's `projectDir` are attributed to that session

#### Scenario: tool calls counted once per stepId

- **WHEN** events.jsonl contains multiple `ToolBlockUpdatedEvent` and `TerminalBlockUpdatedEvent` entries sharing the same `stepId`
- **THEN** each unique `stepId` is counted as exactly one tool call

#### Scenario: model from modelUsage

- **WHEN** a `SessionA2uxEvent` with `LlmResponseMetadataEvent` contains `modelUsage`
- **THEN** the model name appears in the harness row's models map

#### Scenario: EnvironmentVariablesUpdatedEvent never read

- **WHEN** events.jsonl contains an `EnvironmentVariablesUpdatedEvent` with a sentinel secret value
- **THEN** the sentinel secret never appears in the coach JSON output

#### Scenario: prompt text never emitted in report

- **WHEN** `history.jsonl` contains a sentinel prompt text
- **THEN** the sentinel prompt text never appears in the coach JSON output

#### Scenario: --days filters Junie sessions

- **WHEN** a Junie session was created more than N days ago and `--days N` is passed
- **THEN** that session is excluded from the report

#### Scenario: project filter excludes non-matching Junie sessions

- **WHEN** a Junie session's `projectDir` does not match the project root
- **THEN** that session is excluded from the default (project-only) report
