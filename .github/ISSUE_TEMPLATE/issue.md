---
name: Issue
about: Create an issue with proper Use Case reference
title: '[#XXX] type: brief description'
labels: ''
assignees: ''
---

## Description

<!-- Provide a clear and concise description of the issue -->

## Use Case (Mandatory)

<!-- Reference the Use Case this issue supports. Options:
- Business: CU-01 to CU-73 (see docs/01-business/02-use-cases/03_CU*)
- Infrastructure: CU-74 to CU-78 (Performance, Database, QA, Ops, Security)
- Requirements: RF-01 to RF-95 (Functional) or RNF-01 to RNF-24 (Non-functional)

Examples:
  CU-05: Preparar escritura
  CU-74: Performance and Caching
  RF-42: Crear nuevos usuarios
  RNF-12: Seguridad y privacidad
-->

**Use Case:** CU-XX or RF-XX or RNF-XX

## Context

<!-- Provide additional context that helps understand the issue:
- Is this blocking other work?
- What is the business value?
- Who is affected?
-->

## Acceptance Criteria

<!-- Define what needs to be done to complete this issue. Examples:
- [ ] API endpoint implemented: POST /api/v1/documentos
- [ ] Unit tests written (80%+ coverage)
- [ ] Integration tests pass
- [ ] E2E tests pass (if UI)
- [ ] Documentation updated
-->

- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

## Technical Notes

<!-- Any technical considerations or constraints:
- Dependencies on other issues?
- Architecture decisions?
- Migration strategy?
-->

## Definition of Done

- [ ] Code reviewed and approved
- [ ] All tests passing (unit + integration + E2E)
- [ ] Test coverage ≥ 80%
- [ ] Documentation updated
- [ ] PR merged to main
- [ ] Issue closed with proper reference

---

**Workflow Reference:** See `.claude/rules/ai-agent-workflow.md` for the mandatory development process.
