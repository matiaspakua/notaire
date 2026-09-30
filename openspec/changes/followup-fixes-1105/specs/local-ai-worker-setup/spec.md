## ADDED Requirements

### Requirement: A lost gpt-oss tool call becomes a recovery call

When a gpt-oss turn addressed a tool but no call can be parsed and no final answer
exists, oMLX SHALL return an `exec_command` echo telling the model to resend the call.

#### Scenario: EOS right after the constraint token

- **WHEN** the completion ends at `to=functions.exec_command<|constrain|>`
- **THEN** one `exec_command` call is returned whose `cmd` starts with `echo`

#### Scenario: Final answer untouched

- **WHEN** the completion is a final-channel answer
- **THEN** no tool call is returned
