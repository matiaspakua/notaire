## ADDED Requirements

### Requirement: The repair closes an unterminated cmd string

The gpt-oss repair SHALL close a `cmd` string whose closing quote is missing
before the final brace, and oMLX's analysis-channel check SHALL use the repaired arguments.

#### Scenario: Unterminated cmd string

- **WHEN** the arguments are `{"cmd":"ls -R . | sed -e '1p'}`
- **THEN** they parse as `{"cmd": "ls -R . | sed -e '1p'"}`

#### Scenario: Analysis-channel check uses the repair

- **WHEN** the patcher runs on oMLX 0.7.0
- **THEN** `_is_tool_call_message` parses the arguments returned by `repair_tool_call`
