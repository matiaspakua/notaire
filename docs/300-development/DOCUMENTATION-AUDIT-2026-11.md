# Documentation audit — November 2026

Issue #2067 (CU76). This audit repeats and extends the [October 2026 audit](DOCUMENTATION-AUDIT-2026-10.md).
Every figure comes from the command next to it, run on `main` at `54557a85` on 2026-11-07, so the audit can be repeated.

It does three things:

- checks every engineering artifact the method in [INGENIERIA.md](INGENIERIA.md) names;
- checks that the public GitHub Pages site tells the same story as the repository;
- records what the project and the code look like today.

Each finding is fixed in the same pull request or has an owner and an issue.

## 1. The artifact chain, stage by stage

The public [Engineering artifacts](https://matiaspakua.github.io/notaire/docs/artifacts/) page now walks this chain in nine stages, with the counts below read at build time (`github-page/lib/artifact-facts.ts`).

| Stage | Artifact | Where | Count (2026-11-07) | State |
|-------|----------|-------|--------------------|-------|
| Analyze | Requirements catalog (`RF #n`, RNF) | `docs/100-business/101-requirements/requerimientos.csv` | 120 rows, 96 functional | Current |
| Analyze | Survey (RS) and SRS | `docs/100-business/101-requirements/` | 2 | SRS marked as historical baseline (F4, fixed) |
| Model | Use cases (`CUxx`) | `docs/100-business/102-use-cases/` | 88, CU01–CU88, no gap | Current |
| Model | RF ↔ CU traceability matrix | `docs/100-business/104-traceability/` | 120 of 120 requirements covered | Was stale (N1, fixed) |
| Design | Lifecycle model, case walkthrough (`DOC-CICLO`, `DOC-GESTION`) | `docs/200-architecture/203-design/` | 8 design documents | Current |
| Design | Diagrams | `docs/200-architecture/204-diagrams/` | 116 PlantUML (legacy), 30 Mermaid blocks in `docs/200-architecture/` | Migrate on edit (ADR-027) |
| Design | Data model (ERD, dictionary) | `docs/200-architecture/205-data-model/` | — | Guarded by `test_erd_current_schema.py` and `test_data_dictionary_sync.py` (#1222 closed) |
| Decide | SAD (`DOC-SAD`, arc42) | `docs/200-architecture/201-SAD/sad.md` | 12 sections | Current |
| Decide | ADRs | `docs/200-architecture/202-ADR/` | 31 | ADR-030 was missing from the index (N2, fixed) |
| Specify | OpenSpec living specs | `docs/openspec/specs/` | 362 specs, 1,845 scenarios | Current |
| Specify | Archived changes | `docs/openspec/changes/archive/` | 523 | Current |
| Build | Flyway migrations | `backend-api/src/main/resources/db/migration/` | 53 | Current |
| Build | Hexagon rules (ADR-030) | `backend-api/src/test/resources/architecture/hexagon-baseline.txt` | 0 frozen violations | Current |
| Verify | Master test plan (`DOC-MTP`) | `docs/300-development/303-testing/TEST-PLAN.md` | — | Current |
| Verify | Logical test cases (`TC-nnn`) | `docs/300-development/303-testing/test-cases/` | 9 | Examples, not a catalogue (N5) |
| Verify | Executable suites | `backend-api/src/test`, `frontend/src`, `backend-api/api-test`, `testing/e2e` | 364 JUnit classes, 197 Vitest files, 383 Bruno requests, 102 Playwright specs | Current |
| Secure | System threat model, STRIDE register | `docs/200-architecture/206-security/` | 7 register entries | Current (assessed 2026-11-05) |
| Secure | Login and audit threat model (`SR-nn`) | `THREAT-MODEL-AUTH-AUDIT.md` | 11 SR identifiers | Current |
| Secure | Security case catalog | `security/cases/catalog.yml` | 7 cases | Current |
| Operate | Workflows | `.github/workflows/` | 19 | Product CI on a version tag (#2001) |
| Operate | Release, deployment, monitoring, runbooks, manuals | `docs/300-development/`, `docs/200-architecture/207-209`, `docs/100-business/105-manuals/` | — | Cluster apply is manual |

## 2. Checks run and results

| Check | Command | Result |
|-------|---------|--------|
| Relative links | `python3 docs/tests/test_docs_links.py` | Pass |
| Use-case template | `python3 docs/tests/test_business_docs_traceability.py` | Pass |
| Matrix is current (new) | `python3 docs/tests/test_traceability_matrix_current.py` | Failed before this change, passes after |
| ADR index complete (new) | `python3 docs/tests/test_adr_index_complete.py` | Failed on ADR-030, passes after |
| ERD vs schema | `python3 docs/tests/test_erd_current_schema.py` | Pass |
| Docs module | `bash docs/verify.sh` | Pass |
| Pages cited paths | `python3 workspace/tests/test_pages_tech_docs.py` | Pass, including the new artifacts page |
| Workflow triggers | read `on:` of every `.github/workflows/*.yml` | No product or process workflow runs on `pull_request` |
| Required status checks | `security/rulesets/protect-main.desired.json` | None: the ruleset requires a pull request only |

## 3. Findings

| ID | Finding | Evidence | Action |
|----|---------|----------|--------|
| N1 | The RF ↔ CU matrix said it was generated from 82 use cases; CU83–CU88 were missing. Rows for RF #22, #46, #82, #94, #96, #104–#106, #120, #121 and #1224 disagreed with the use cases. No generator existed in the tree. | The new test failed with 2 failures | Fixed: `docs/tools/generate_traceability_matrix.py` regenerates it, and `test_traceability_matrix_current.py` runs in `docs/verify.sh` |
| N2 | ADR-030, the accepted hexagonal architecture, was not in the ADR index, though the Pages architecture page listed it. | `test_adr_index_complete.py` | Fixed, and guarded |
| N3 | The SRS still prescribes MySQL and the desktop stack, without saying it is historical (October F4). | `grep -n MySQL` in the SRS | Fixed: historical-baseline note pointing to the live catalog and the stack document |
| N4 | The Pages site still said pull requests wait for product CI and that the ruleset requires five status checks. Both stopped being true with #2001. The PR sequence diagram, Guardrails layers 6–8, Gate 4 and lifecycle steps 18–19 carried the old claim. | Workflow triggers and the ruleset JSON (section 2) | Fixed in `github-page/lib/ai-sdlc-content.ts` and `app/page.tsx` |
| N5 | The logical test cases (`TC-001`…`TC-009`) cover nine flows. The test plan says they are examples, but the October audit and older pages read as if they were a catalogue. | `ls docs/300-development/303-testing/test-cases` | Stated as a gap on the artifacts page; the executable suites are the complete record |
| N6 | Legacy PlantUML still holds 116 diagrams, against 30 Mermaid blocks in the architecture tree. ADR-027 migrates them only when their document is edited. | `find docs/200-architecture/204-diagrams -name '*.puml'` | Kept by policy; tracked by ADR-027 |
| N7 | The engineering assessment (`EVALUACION-INGENIERIA.md`) and the business assessment say CU01–CU87. | `grep CU01–CU87` | Kept: both are dated October cuts; this audit is the current one |
| N8 | The repository has no license file; the README links one. | `ls LICENSE*` | Open, Owner decision: #1226 |

October findings re-checked:

- **F1 links:** pass.
- **F3 dictionary:** closed by #1222, guarded.
- **F4 SRS:** fixed here as N3.
- **F6 CHANGELOG:** `[Unreleased]` is 23 lines after release cuts; resolved.
- **F7 archive shim:** resolved by #1289.

## 4. Project and code status (2026-11-07)

| Area | State | Source |
|------|-------|--------|
| Product version | 0.2.7 | `.release-please-manifest.json` |
| Backend | 667 Java main files; hexagon baseline empty (ADR-030 DoD met) | `backend-api/src/main/java`, `hexagon-baseline.txt` |
| Frontend | 547 TypeScript files, 197 test files | `frontend/src` |
| Specifications | 362 living specs, 2 active changes | `docs/openspec/` |
| AI SDLC | 36 skills, 13 agents; the #2052 protocol is on `main`: owner-only paths, write set, review budget, anchored evidence, Stop hook | `.claude/`, `.aisdlc/aisdlc.toml` |
| Backlog | 166 open issues, 0 open pull requests | `gh issue list`, `gh pr list` |
| Milestones | v0.3 34/39 closed; v0.4 4/6; v0.5 7/21; v0.6 2/4; v1.0 0/1; the requirements catalog milestone 0/112 | `gh api …/milestones` |

## 5. Residual risks

- Structure and links are checked; whether prose describes behaviour correctly is still checked only by the per-change specs and tests.
- Counts on the public site are true at build time; the site is rebuilt on every push to `main`, so a count can lag by one deploy.
- The SRS stays in Spanish with its original decisions; it is a baseline, not a specification to implement.

## 6. Refreshing this audit

```bash
bash docs/verify.sh
python3 workspace/tests/test_pages_tech_docs.py
python3 docs/tools/generate_traceability_matrix.py --check
cd github-page && npx vitest run tests/artifacts.test.ts
```
