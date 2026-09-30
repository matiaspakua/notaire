# local-ai-worker-setup Specification

## Purpose

TBD - created by archiving change gpt-oss-preset-1099. Update Purpose after archive.

## Requirements

### Requirement: The setup script installs a model by preset

`setup-omlx-codex.sh` SHALL take `PRESET` (`qwen3-coder` by default, or `gpt-oss`)
and derive the model repo, context window, sampling, reasoning effort, Codex
profile, catalog and instructions from it, leaving other presets' profiles untouched.

#### Scenario: gpt-oss preset keeps the Qwen profile

- **WHEN** `PRESET=gpt-oss bash setup-omlx-codex.sh` runs after the default preset
- **THEN** `~/.codex/omlx-gptoss.config.toml` names `gpt-oss-20b-MXFP4-Q8` and `~/.codex/omlx.config.toml` is unchanged

### Requirement: gpt-oss tool calls are repaired before Codex sees them

The oMLX harmony adapter SHALL turn a malformed gpt-oss `exec_command` call into
a valid one when the intent is unambiguous.

#### Scenario: Argv list as cmd

- **WHEN** the arguments are `{"cmd": ["bash", "-lc", "ls -R"]}`
- **THEN** they become `{"cmd": "ls -R"}`

#### Scenario: Stray bracket after a heredoc

- **WHEN** the arguments end with `PATCH"]}`
- **THEN** the bracket is dropped and the arguments parse as JSON

#### Scenario: Header tokens in the tool name

- **WHEN** the name is `exec_command<|channel|>commentary`
- **THEN** it becomes `exec_command`

#### Scenario: Valid call unchanged

- **WHEN** the arguments are `{"cmd": "ls"}` and the name is `exec_command`
- **THEN** both are returned unchanged

### Requirement: The oMLX patch is idempotent and reversible

`patch_omlx.py` SHALL apply each patch once, back up the original file, and skip
a patch whose anchor is missing instead of editing blindly.

#### Scenario: Second run changes nothing

- **WHEN** the patcher runs twice on the same oMLX tree
- **THEN** the second run reports every patch as already applied and the file is unchanged

#### Scenario: Missing anchor skipped

- **WHEN** the harmony adapter no longer contains the patched code
- **THEN** the patch is reported as skipped and the file is unchanged
