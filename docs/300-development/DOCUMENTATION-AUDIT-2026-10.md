# Documentation audit — October 2026

Issue #921 (CU76, CU77). Every figure below comes from the command next to it, run on `main`
at `dde755e` on 2026-10-04, so the audit can be repeated. The audit is documentation-only: it changed
no product code.

## 1. Inventory and ownership

| Area | Location | Owner (role) | Active files | Source of truth for |
|------|----------|--------------|--------------|---------------------|
| Process | `CONSTITUTION.md`, `AGENTS.md`, `CLAUDE.md`, `.claude/rules/` | Owner | 4 + rules | How a change is made; the Constitution prevails |
| Requirements | `docs/100-business/101-requirements/` | Product | 3 | RF/RNF list (`requerimientos.csv`, 121 rows), SRS |
| Use Cases | `docs/100-business/102-use-cases/` | Product / Analyst | 87 files, CU01-CU87 with no gaps | Behaviour per Use Case |
| Actors, traceability, manuals | `docs/100-business/103-105` | Product | 7 | Actors, RF↔CU matrix, user manuals |
| Architecture | `docs/200-architecture/201-SAD`, `202-ADR` | Architect | SAD + 23 ADRs (001-023, contiguous, all indexed) | Structure and decisions |
| Diagrams | `docs/200-architecture/204-diagrams`, `205-data-model/ERD` | Architect | 119 PlantUML sources | Views; ERD generated from the schema (#1021) |
| Data model | `docs/200-architecture/205-data-model` | Architect / Backend | dictionary + ERD | Tables and columns (see finding F3) |
| Security, monitoring, DevSecOps, deployment | `docs/200-architecture/206-209` | Security / DevOps | 9 | Threat model, SLOs, gates, deploy |
| Development | `docs/300-development/` | Engineering | 36 | Setup, standards, testing, preflight, release |
| Testing | `docs/300-development/303-testing/`, `testing/docs/` | QA | 19 + 4 | Test plan (project level) / how to run the QA suites |
| Specifications | `openspec/specs/` (80), `openspec/changes/archive/` (107) | Whoever opens the change | — | Behaviour contracts; changes are not permanent docs |
| Skills and agents | `.claude/skills` (33), `.claude/agents` (11) | Engineering | — | Execution guidance, never policy |
| Archive | `docs/000-archive/` (64 Markdown files) | Owner | not maintained | History |

Total: 180 active Markdown files under `docs/` (`find docs -name '*.md'`, archives excluded).

## 2. Checks run and results

| Check | Command or guard | Result |
|-------|------------------|--------|
| Relative links resolve | `python3 docs/tests/test_docs_links.py` | 4 broken before this audit (3 in `FRONTEND-DESIGN-SYSTEM.md`, 1 `LICENSE`); 3 fixed, 1 exempt (F1) |
| Use Case template | `docs/tests/test_business_docs_traceability.py` (#956) | CU84 was the only file off-template |
| RF → Use Case coverage | `grep -oh 'RF #[0-9]*'` against `requerimientos.csv` | 95 of 96 functional rows referenced; the 96th was the malformed Login row (#956) |
| Use Case numbering | `ls docs/100-business/102-use-cases` | CU01-CU87, no gap, no duplicate |
| ADR index and status | each `ADR-*.md` listed in `202-ADR/README.md` and carrying a status | All 23 |
| ERD vs schema | `docs/tests/test_erd_current_schema.py` (#1021) | Regenerated; consistent |
| Dictionary vs schema | column-set comparison per table | 29 of 36 tables with stale column names, 4 tables missing (F3, #1222) |
| Stale technology statements | `git grep -E 'Spring Boot 3\|PostgreSQL 15\|com\.notaria\|Java 17\|MySQL' -- docs` | Only historical context, the Sonar database (PostgreSQL 15 is correct there) and the SRS (F4) |
| Guards CI never ran | `scripts/tests/test_guard_wrappers.py` (#1209) | 12 unwired before #1209 |

## 3. Findings

| ID | Finding | Evidence | Action |
|----|---------|----------|--------|
| F1 | Broken relative links | `docs/tests/test_docs_links.py` | Fixed (design-system links); `LICENSE` exempt with a reason and issue #1226 |
| F2 | CU84 off-template; two malformed CSV rows | #956 | Fixed in #956 |
| F3 | Data dictionary column names stale for 29 tables; 4 tables absent | #1222 | Filed; needs a generator, not hand edits |
| F4 | `SRS - Especificacion de Requerimientos.md` still prescribes MySQL and a desktop stack | `git grep MySQL` | Kept as the original business baseline; marked historical in the roadmap below |
| F5 | ADR-005 says Next.js 15; the product runs Next.js 16 | `ADR-005-modern-frontend-migration.md` | Accepted ADRs record the decision at the time; a one-line update note added |
| F6 | `CHANGELOG.md` `[Unreleased]` is over 1,000 lines | `wc -l CHANGELOG.md` | Roadmap item: curate at the next release |
| F7 | Two archive roots (`docs/000-archive` and the `docs/archive` shim) | `ls docs` | Resolved (#1289): references repointed to `docs/000-archive/`; the shim and its byte-identical duplicate are deleted |

## 4. Residual risks

- Some Use Cases mix Spanish and English headings; this is cosmetic and tracked by the language policy, not by this audit.
- The audit checks structure and links, not whether prose describes behaviour correctly; correctness stays with the per-change specs and tests.
- External URLs are not checked (slow and flaky in CI).

## 5. Prioritized roadmap

| Priority | Item | Reference |
|----------|------|-----------|
| P1 | Regenerate dictionary structure from the schema and guard it | #1222 |
| P1 | Decide the license (README links a missing file) | #1226 |
| P2 | Curate `CHANGELOG.md` into a release section at the next tag | F6 |
| P2 | Repoint scripts and rules from `docs/archive/` to `docs/000-archive/` and drop the shim | F7 |
| P3 | Mark the SRS header as a historical baseline and link the live requirements list | F4 |
| P3 | Normalise Use Case headings to one language | residual risk 1 |

## 6. Refreshing this audit

```bash
python3 docs/tests/test_docs_links.py
python3 docs/tests/test_business_docs_traceability.py
python3 docs/tests/test_erd_current_schema.py
find docs -name '*.md' -not -path 'docs/000-archive/*' | wc -l
```
