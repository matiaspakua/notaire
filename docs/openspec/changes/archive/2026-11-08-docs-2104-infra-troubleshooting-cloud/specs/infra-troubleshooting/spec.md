<!-- Governed by CONSTITUTION.md. CU76, issue #2104. -->

## Purpose

The infra runbook documents known failures on constrained hosts and cloud sandboxes.

## ADDED Requirements

### Requirement: Constrained-host troubleshooting is documented

The operation runbook MUST list symptom, cause and fix for the Elasticsearch disk watermark, the POSIX locale JVM failure, builds without mirrors, a bind-mounted missing file and Docker Hub 429.

#### Scenario: Operator hits a known failure

- **WHEN** an operator reads the Troubleshooting section of `infra/docs/OPERATION.md`
- **THEN** each of the five failures has a symptom, a cause and a fix
