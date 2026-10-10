## Purpose

Local AI models are declared once and resolved consistently for the setup script. Source: #1377; owner CU76.

## ADDED Requirements

### Requirement: The registry resolves a preset to the values it declares

`local-ai/models_config.py resolve <key|repository>` MUST return the repository, context window, Codex profile, sampling, reasoning and summary declared in `local-ai/models.yaml` for that model.

#### Scenario: Existing presets are unchanged

- **WHEN** `qwen3-coder` or `gpt-oss` is resolved
- **THEN** the values equal those the hard-coded presets had before this change

#### Scenario: The Bonsai preset is available

- **WHEN** `ternary-bonsai` is resolved
- **THEN** it returns the `prism-ml/Ternary-Bonsai-2-27B-mlx-2bit` repository and the `omlx-bonsai` profile

#### Scenario: Unknown models fail

- **WHEN** an unknown key is resolved
- **THEN** the resolver exits non-zero and the setup script stops
