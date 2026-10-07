# Specification — how it is produced

> **Mandatory artifact of the SDLC workflow** (see `CONSTITUTION.md` §5 step 3 —
> *Specification*). A Specification describes **only the change** in scope. It is
> **not** permanent documentation and never replaces README / ADR / API docs.
>
> **Gate 1** — no implementation starts without a Specification plus Acceptance
> Criteria.

Specifications in this repository are produced with **OpenSpec**, using the
project schema `notaire-sdlc`, which encodes this Constitution. There is no
separate markdown specification format: this document is the map from the
Constitution's requirements to the artifact that carries each one.

## Producing a Specification

```bash
openspec new change "<kebab-case-name>"     # creates docs/openspec/changes/<name>/.openspec.yaml
bash scripts/seed-openspec-change.sh "<name>" \
  --issue <n> --use-case "CU76 — …" --branch "<type>/<n>_desc"
# copies proposal/design/tasks/traceability from notaire-sdlc templates when absent
# and fills the Issue / Use Case / Branch / change-name cells it knows

openspec status  --change "<name>"          # artifact build order
openspec instructions <artifact> --change "<name>"
# Fill every ## section in the seeded files; keep every heading.
openspec validate "<name>" --strict         # OpenSpec's own structural checks
bash scripts/validate-sdlc-plan.sh "<name>" # Constitution checks (rejects leftover <!-- --> bodies)
```

Any agent may drive this — Claude Code, OpenCode, GitHub Copilot, Codex, Cursor —
through its generated `opsx` commands, or a human may write the files directly.
The contract is the same for all of them because it lives in the CLI and the
project scripts, not in the agent. Do **not** rewrite the seeded files from
scratch: fill the template sections in place so headings stay intact.

## Where each mandatory section lives

| Constitution requirement | Artifact | Section |
|--------------------------|----------|---------|
| GitHub Issue | `proposal.md` + `traceability.md` | Header table / Chain |
| Use Case (`CU-XX`) | `proposal.md` + `traceability.md` | Header table / Chain |
| Objetivo (problem / motivation) | `proposal.md` | Objetivo |
| Scope and out of scope | `proposal.md` | What Changes / Out of Scope |
| Reglas de negocio | `proposal.md` (named) + delta spec (normative) | Reglas de negocio / Requirements |
| Proposed behavior | `specs/<capability>/spec.md` | Requirements + Scenarios |
| Acceptance Criteria | `specs/<capability>/spec.md` | `#### Scenario:` blocks |
| Constraints & compatibility | `design.md` | Context / Decisions |
| Impact Analysis | `proposal.md` | Impact Analysis |
| Módulos afectados | `proposal.md` | Impact Analysis → Módulos afectados |
| Architecture review / ADR | `proposal.md` | Impact Analysis → Architecture review |
| Riesgos | `design.md` | Riesgos / Trade-offs |
| Test cases | `design.md` | Testing Strategy |
| Regression Strategy | `design.md` | Regression Strategy |
| Playwright Strategy | `design.md` | Playwright Strategy |
| Deployment Strategy | `design.md` | Deployment Strategy |
| Rollback Strategy | `design.md` | Rollback Strategy |
| Documentation Impact | `proposal.md` | Documentation Impact |
| Definition of Done | `tasks.md` | Definition of Done |
| Traceability chain | `traceability.md` | Chain / Gate log |

Acceptance Criteria are the delta spec's `#### Scenario:` blocks — the same text
is the test contract, so a criterion cannot drift from its test. They must match
the Acceptance Criteria in the GitHub Issue.

## Approvals

OpenSpec does not model approvals. They are recorded where they are enforced:

- **Architecture** — the ADR under `docs/200-architecture/202-ADR/`, when required.
- **Engineering** — Pull Request review by the code owner (`CODEOWNERS`), Gate 4.
- **Product / Business** — the GitHub Issue, before Gate 1.

## Where the artifacts live

Active changes are in `docs/openspec/changes/<change-name>/`; once Gate 5 passes,
`openspec archive <change-name>` moves the change to `docs/openspec/changes/archive/`
and folds its delta into `docs/openspec/specs/`, which is the accumulated behavior
contract of the system.

## Related

- `CONSTITUTION.md` — the process this implements
- `docs/openspec/schemas/notaire-sdlc/` — templates and agent instructions
- `scripts/validate-sdlc-plan.sh` — the mechanical gate (`--list` explains each check);
  also rejects `##` section bodies that are still only template `<!-- ... -->` comments
- `scripts/seed-openspec-change.sh` — copies the four notaire-sdlc templates into a
  change folder when absent and fills known Issue / Use Case / Branch values
