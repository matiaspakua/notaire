# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1490 | open |
| Use Case | CU75 — Database Management and Migrations | exists |
| Specification | `docs/openspec/changes/refactor-1490-english-codebase/` | written |
| Branch | `cursor/english-codebase-01e0` | created from `origin/main` |
| Tasks | `tasks.md` | in progress |
<<<<<<< HEAD
| Commits | `9c536900`, `329247ba`, `86b51c34`, `93300933`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `2c345ab5`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `e42e1487`, `abde581c`, `68d84c39`, `c58be179`, `dd015ac3`, `797b268c`, `ffb8fdd5`, `88a7e431`, `ffe5aaa4`, `db6a4876`, `7a9458cb`, `c7d87636`, `e48ddf2a`, `ec567185`, `d1aa0d4c`, `df1c22fc`, `14abf883`, `315c83ab`, `0cb342a1`, `503354fc`, `be66ad75`, `51c25a65`, `298d04df`, `676003db`, `4177e611`, `bd883977`, `aba4adb4`, `ee51a917`, `8908af20`, `ee860266`, `01771ab1`, `fdcf0788`, `66c8a54d`, `c690d487`, `7a10f083`, `471334a5`, `8b1b4322`, `e030d2a1`, `9935ac81`, `4470dbfc`, `eb3ccb6f`, `49c85360`, `99b66958`, `602d7019`, `fd6fa760`, `6ee0f0c1`, `0e320a6f`, `f6a28763`, `88602fcc`, `0a259320`, `0523aae6`, `fe5ee462`, `6dd60865`, `b9840ded`, `0a82b3b5`, `74671e88`, `8352fccd`, `509c9a6c`, `b418e6ce`, `2a860c47`, `ce5010fc`, `f3d76e63`, `8067d797`, `1f9bf2f9`, `03bfc567`, `1ba4d9fa`, `87b763a0`, `85a6d8cd`, `ceb44c77`, `48c6a8c1`, `9cef2ec7`, `e525076f`, `04e51c02`, `27b151eb`, `f5cba10d`, `4cf2154f`, `c284f284`, `ce6b5232`, `78ea0fd3`, `ee6039ca`, `525c7d5a`, `d19f616e`, `da546cc9`, `d4a2835f`, `d9c52f61`, `12aecc5a`, `c8b59dae`, `7f310e87`, `75ac45cb`, `32537417`, `e5ba1643`, `8dd85500`, `5e770b15`, `3f053de7`, `ddcde4c6`, `4c2aa084`, `2d1ff534`, `a82f7970`, `460faff6`, `6ad9a8e1`, `394f2a7b`, `6f1dbf8a`, `8edd4a58`, `8fc1a84f`, `93f0fea0`, `6b929449`, `5f5ef2e3`, `7c36581e`, `30b23526`, `d302d8ad`, `616c2dc8`, `b75d413f`, `3346b60e`, `70768655`, `133e0d18`, `72e4e319`, `42766598`, `5526fdcd`, `e2e77bff`, `946444e9`, `6b0939f8`, `64febfea`, `c63cd5ce`, `a890ac30`, `f28bad22`, `f89c6a27`, `5ff1eaad`, `f481f222`, `2078c1db`, `ec19f220`, `65389a43`, `e0f918d2`, `b7c28b96`, `74442f87`, `55718f0d`, `6e7e3a50`, `2b16b5f3`, `12f5e927`, `a020e895`, `b510ffdb`, `83d7d40d`, `a24bdc1f`, `b83c898f`, `9d516a9e`, `a059c070`, `cfd4ef04`, `504979c8`, `94db1685`, `65f3d68a`, `91713896`, `faa340fd`, `a7499837`, `74b403b2`, `bbaf4af5`, `ca9df71b`, `a4bdb7ab`, `6bc93d56`, `e1eee940`, `c45d7218`, `a6f3d7f5`, `b5bda30c`, `06fd49c3`, `69c49107`, `372c7f58`, `bc1495c9`, `b515ded5`, `282777a1`, `a2af5d57`, `99facf69`, `8dcc4fdb`, `e8508f2b`, `9255c01b`, `30eca4e0`, `0cf7b56a`, `87a8c294`, `2f908ff4`, `39b1fc03`, `b45818e4`, `b8ad8a96`, `26380a0b`, `1435de68`, `a018ccde`, `c749de1e`, `1db2219f`, `64c6865a`, `6d28448c`, `bf27f315`, `9910cbe8`, `3a58d7b4`, `23efc7dc`, `bcbeeb73`, `a2c7d67d`, `a92bc8e3`, `e49e1e14`, `e1fb3580`, `1ff0c98d`, `1c33655a`, `f23fd11c`, `91172e2f`, `b85893d3`, `71b50a3b`, `685196ee`, `12378608`, `a06975d1`, `cab12b90`, `8c758a94`, `9b184c0b`, `982e941d`, `4ae0b2f6`, `e37feb4a`, `2a7ea0e2`, `0a7b9002`, `425e1675`, `483fe4b4`, `ccc91452`, `08148a9c`, `031407a6`, `ef158459`, `d0bd548b`, `619aeac3`, `8ada6944`, `66dab39d`, `c62220a0`, `5c8a3477`, `60bc713e`, `3d9d3ead`, `e73a4cb4`, `33581762`, `2df0b29a`, `c74ed6a9`, `f8abaebc`, `92ddd3d6`, `d61159ff`, `7b5b2987`, `dcfe02b2`, `1dff09c8`, `c678b8eb`, `0d6aa68c`, `7696c551`, `dec17002`, `f7df6959`, `e7136a8b`, `7c6965fe`, `407149d9`, `4feca8c4`, `b985d266`, `c20fdf1e`, `f3123456`, `fee62e1f`, `c2dbf52a`, `055c0889`, `b3a1e90e`, `c6b658f1`, `90bc40fd`, `408f3402`, `81b6f900`, `0979e875`, `6aa61c6d`, `0bc47452`, `b070c19b`, `6c9ec69d`, `64690769`, `615969fa`, `7bc6c2ff`, `883c534d`, `f2eaecef`, `a67c5210`, `c002efd5`, `1187fd4b`, `6311c70f`, `139ffd4e`, `74638c58`, `b98b953d`, `898e02d9`, `f0cfa95e`, `c3fcefb9`, `111c09d3`, `5537557d`, `a2c15f02`, `722bdfe3`, `d3757bea`, `b2350ea3`, `89ad4c17`, `281f44d9`, `93b3b9e0` | created |
=======
| Commits | `9c536900`, `329247ba`, `86b51c34`, `93300933`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `2c345ab5`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `e42e1487`, `abde581c`, `68d84c39`, `c58be179`, `dd015ac3`, `797b268c`, `ffb8fdd5`, `88a7e431`, `ffe5aaa4`, `db6a4876`, `7a9458cb`, `c7d87636`, `e48ddf2a`, `ec567185`, `d1aa0d4c`, `df1c22fc`, `14abf883`, `315c83ab`, `0cb342a1`, `503354fc`, `be66ad75`, `51c25a65`, `298d04df`, `676003db`, `4177e611`, `bd883977`, `aba4adb4`, `ee51a917`, `8908af20`, `ee860266`, `01771ab1`, `fdcf0788`, `66c8a54d`, `c690d487`, `7a10f083`, `471334a5`, `8b1b4322`, `e030d2a1`, `9935ac81`, `4470dbfc`, `eb3ccb6f`, `49c85360`, `99b66958`, `602d7019`, `fd6fa760`, `6ee0f0c1`, `0e320a6f`, `f6a28763`, `88602fcc`, `0a259320`, `0523aae6`, `fe5ee462`, `6dd60865`, `b9840ded`, `0a82b3b5`, `74671e88`, `8352fccd`, `509c9a6c`, `b418e6ce`, `2a860c47`, `ce5010fc`, `f3d76e63`, `8067d797`, `1f9bf2f9`, `03bfc567`, `1ba4d9fa`, `87b763a0`, `85a6d8cd`, `ceb44c77`, `48c6a8c1`, `9cef2ec7`, `e525076f`, `04e51c02`, `27b151eb`, `f5cba10d`, `4cf2154f`, `c284f284`, `ce6b5232`, `78ea0fd3`, `ee6039ca`, `525c7d5a`, `d19f616e`, `da546cc9`, `d4a2835f`, `d9c52f61`, `12aecc5a`, `c8b59dae`, `7f310e87`, `75ac45cb`, `32537417`, `e5ba1643`, `8dd85500`, `5e770b15`, `3f053de7`, `ddcde4c6`, `4c2aa084`, `2d1ff534`, `a82f7970`, `460faff6`, `6ad9a8e1`, `394f2a7b`, `6f1dbf8a`, `8edd4a58`, `8fc1a84f`, `93f0fea0`, `6b929449`, `5f5ef2e3`, `7c36581e`, `30b23526`, `d302d8ad`, `616c2dc8`, `b75d413f`, `3346b60e`, `70768655`, `133e0d18`, `72e4e319`, `42766598`, `5526fdcd`, `e2e77bff`, `946444e9`, `6b0939f8`, `64febfea`, `c63cd5ce`, `a890ac30`, `f28bad22`, `f89c6a27`, `5ff1eaad`, `f481f222`, `2078c1db`, `ec19f220`, `65389a43`, `e0f918d2`, `b7c28b96`, `74442f87`, `55718f0d`, `6e7e3a50`, `2b16b5f3`, `12f5e927`, `a020e895`, `b510ffdb`, `83d7d40d`, `a24bdc1f`, `b83c898f`, `9d516a9e`, `a059c070`, `cfd4ef04`, `504979c8`, `94db1685`, `65f3d68a`, `91713896`, `faa340fd`, `a7499837`, `74b403b2`, `bbaf4af5`, `ca9df71b`, `a4bdb7ab`, `6bc93d56`, `e1eee940`, `c45d7218`, `a6f3d7f5`, `b5bda30c`, `06fd49c3`, `69c49107`, `372c7f58`, `bc1495c9`, `b515ded5`, `282777a1`, `a2af5d57`, `99facf69`, `8dcc4fdb`, `e8508f2b`, `9255c01b`, `30eca4e0`, `0cf7b56a`, `87a8c294`, `2f908ff4`, `39b1fc03`, `b45818e4`, `b8ad8a96`, `26380a0b`, `1435de68`, `a018ccde`, `c749de1e`, `1db2219f`, `64c6865a`, `6d28448c`, `bf27f315`, `9910cbe8`, `3a58d7b4`, `23efc7dc`, `bcbeeb73`, `a2c7d67d`, `a92bc8e3`, `e49e1e14`, `e1fb3580`, `1ff0c98d`, `1c33655a`, `f23fd11c`, `91172e2f`, `b85893d3`, `71b50a3b`, `685196ee`, `12378608`, `a06975d1`, `cab12b90`, `8c758a94`, `9b184c0b`, `982e941d`, `4ae0b2f6`, `e37feb4a`, `2a7ea0e2`, `0a7b9002`, `425e1675`, `483fe4b4`, `ccc91452`, `08148a9c`, `031407a6`, `ef158459`, `d0bd548b`, `619aeac3`, `8ada6944`, `66dab39d`, `c62220a0`, `5c8a3477`, `60bc713e`, `3d9d3ead`, `e73a4cb4`, `33581762`, `2df0b29a`, `c74ed6a9`, `f8abaebc`, `92ddd3d6`, `d61159ff`, `7b5b2987`, `dcfe02b2`, `1dff09c8`, `c678b8eb`, `0d6aa68c`, `7696c551`, `dec17002`, `f7df6959`, `e7136a8b`, `7c6965fe`, `407149d9`, `4feca8c4`, `b985d266`, `c20fdf1e`, `f3123456`, `fee62e1f`, `c2dbf52a`, `055c0889`, `b3a1e90e`, `c6b658f1`, `90bc40fd`, `408f3402`, `81b6f900`, `0979e875`, `6aa61c6d`, `0bc47452`, `b070c19b`, `6c9ec69d`, `64690769`, `615969fa`, `7bc6c2ff`, `883c534d`, `f2eaecef`, `a67c5210`, `c002efd5`, `1187fd4b`, `6311c70f`, `139ffd4e`, `74638c58`, `b98b953d`, `898e02d9`, `f0cfa95e`, `c3fcefb9`, `111c09d3`, `5537557d`, `a2c15f02`, `722bdfe3`, `d3757bea`, `b2350ea3`, `89ad4c17`, `281f44d9`, `7c02e0ad`, `574ab536` | created |
>>>>>>> 3e175b6f (docs(traceability): record the English sample names)
| Pull Request | #1495 | draft |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Users login path is English | `EnglishSurfaceRatchetTest` | passing |
| Spanish resource segment is not mapped | `EnglishSurfaceRatchetTest` | passing |
| People screen lives at the English route | dashboard folder `/dashboard/people` plus nav unit test | passing |
| Navigation hrefs do not use Spanish segments | `EnglishSurfaceRatchetTest` | passing |
| Identifications table is English | `EnglishSurfaceRatchetTest` | passing |
| Applied migrations are immutable | `git diff` against `origin/main` for V1–V44 | passing |
| Spanish catalog is still the Spanish locale | `frontend/src/tests/unit/i18n.test.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | `329247ba`, `99b66958`, `c63cd5ce` |
| `CHANGELOG.md` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `797b268c`, `88a7e431`, `db6a4876`, `c7d87636`, `ec567185`, `df1c22fc`, `0cb342a1`, `be66ad75`, `298d04df`, `4177e611`, `aba4adb4`, `8908af20`, `01771ab1`, `66c8a54d`, `7a10f083`, `8b1b4322`, `9935ac81`, `eb3ccb6f`, `99b66958`, `fd6fa760`, `0e320a6f`, `88602fcc`, `0523aae6`, `6dd60865`, `0a82b3b5`, `8352fccd`, `b418e6ce`, `ce5010fc`, `8067d797`, `03bfc567`, `87b763a0`, `ceb44c77`, `9cef2ec7`, `04e51c02`, `f5cba10d`, `c284f284`, `78ea0fd3`, `525c7d5a`, `da546cc9`, `d9c52f61`, `c8b59dae`, `75ac45cb`, `e5ba1643`, `5e770b15`, `ddcde4c6`, `2d1ff534`, `460faff6`, `394f2a7b`, `8edd4a58`, `93f0fea0`, `5f5ef2e3`, `30b23526`, `616c2dc8`, `3346b60e`, `133e0d18`, `42766598`, `e2e77bff`, `6b0939f8`, `c63cd5ce`, `f89c6a27`, `f481f222`, `ec19f220`, `e0f918d2`, `74442f87`, `6e7e3a50`, `12f5e927`, `b510ffdb`, `a24bdc1f`, `9d516a9e`, `cfd4ef04`, `94db1685`, `91713896`, `a7499837`, `bbaf4af5`, `a4bdb7ab`, `e1eee940`, `a6f3d7f5`, `06fd49c3`, `372c7f58`, `b515ded5`, `a2af5d57`, `8dcc4fdb`, `9255c01b`, `0cf7b56a`, `2f908ff4`, `b45818e4`, `26380a0b`, `a018ccde`, `1db2219f`, `6d28448c`, `9910cbe8`, `23efc7dc`, `a2c7d67d`, `e49e1e14`, `1ff0c98d`, `f23fd11c`, `b85893d3`, `685196ee`, `cab12b90`, `9b184c0b`, `4ae0b2f6`, `2a7ea0e2`, `425e1675`, `ccc91452`, `031407a6`, `d0bd548b`, `8ada6944`, `c62220a0`, `60bc713e`, `3d9d3ead`, `33581762`, `c74ed6a9`, `92ddd3d6`, `7b5b2987`, `c678b8eb`, `7696c551`, `e7136a8b`, `407149d9`, `c20fdf1e`, `fee62e1f`, `c2dbf52a`, `b3a1e90e`, `90bc40fd`, `81b6f900`, `6aa61c6d`, `b070c19b`, `64690769`, `7bc6c2ff`, `f2eaecef`, `c002efd5`, `6311c70f`, `74638c58`, `898e02d9`, `c3fcefb9`, `5537557d`, `722bdfe3`, `b2350ea3`, `281f44d9`, `574ab536` |
| `backend-api/openapi/openapi.yaml` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `ec567185`, `0cb342a1`, `be66ad75`, `aba4adb4`, `01771ab1`, `66c8a54d`, `7a10f083`, `eb3ccb6f`, `b418e6ce`, `ce5010fc`, `b510ffdb`, `b2350ea3` |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | `4a3afc10`, `9935ac81` |
| `backend-api/api-test/COVERAGE.md` | yes | `329247ba`, `0523aae6`, `bbaf4af5` |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | `8067d797` |
| `docs/300-development/303-testing/api-test/README.md` | yes | `8067d797` |
| `docs/200-architecture/201-SAD/sad.md` | yes | `03bfc567`, `2d1ff534`, `bbaf4af5`, `06fd49c3`, `a2af5d57`, `d0bd548b` |
| `docs/300-development/303-testing/test-cases/` | yes | `460faff6` |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | `394f2a7b` |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | `394f2a7b` |
| `docs/300-development/301-setup/README.md` | yes | `8edd4a58` |
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | yes | `93f0fea0` |
| `docs/300-development/302-code-standards/DTO-MAPPING-GUIDE.md` | yes | `b510ffdb` |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` | yes | `5f5ef2e3`, `42766598` |
| `docs/200-architecture/203-design/BACKEND-ERROR-HANDLING-STRATEGY.md` | yes | `5f5ef2e3` |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | yes | `5f5ef2e3` |
| `docs/300-development/ASSESSMENT-2026-10.md` | yes | `5f5ef2e3` |
| `docs/200-architecture/202-ADR/ADR-013-audit-trail.md` | yes | `03bfc567` |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-014-workflow-engine.md` | yes | `87b763a0`, `06fd49c3` |
| `docs/200-architecture/202-ADR/ADR-015-internationalization.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-001-microservices-architecture.md` | yes | `ceb44c77` |
| `docs/200-architecture/203-design/README.md` | yes | `ceb44c77`, `a2af5d57` |
| `docs/200-architecture/202-ADR/ADR-006-testing-strategy.md` | yes | `9cef2ec7` |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | yes | `9cef2ec7`, `c74ed6a9` |
| `docs/300-development/README.md` | yes | `04e51c02`, `06fd49c3` |
| `docs/300-development/303-testing/README.md` | yes | `04e51c02` |
| `docs/200-architecture/202-ADR/ADR-005-modern-frontend-migration.md` | yes | `f5cba10d` |
| `docs/200-architecture/202-ADR/ADR-002-module-structure.md` | yes | `c284f284` |
| `docs/200-architecture/202-ADR/ADR-003-rest-api-versioning.md` | yes | `78ea0fd3` |
| `docs/200-architecture/202-ADR/ADR-004-database-migration.md` | yes | `da546cc9` |
| `docs/200-architecture/202-ADR/ADR-007-database-schema-versioning-flyway.md` | yes | `d9c52f61` |
| `docs/200-architecture/202-ADR/README.md` | yes | `c8b59dae` |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes | `75ac45cb`, `e5ba1643` |
| `docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv` | yes | `5e770b15` |
| `docs/200-architecture/205-data-model/diagrama-compensacion/Diagrama de Compensacion.csv` | yes | `ddcde4c6` |
| `docs/200-architecture/205-data-model/diagrama-compensacion/Diagrama-Compensacion.puml` | yes | `ddcde4c6` |
| `docs/200-architecture/205-data-model/diagrama-compensacion/Diagrama_Compensacion_Escribania.svg` | yes | `ddcde4c6` |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | yes | `9d516a9e` |
| `docs/200-architecture/206-security/INPUT-VALIDATION-STRATEGY.md` | yes | `9d516a9e` |
| `docs/200-architecture/206-security/SQL-INJECTION-PREVENTION.md` | yes | `9d516a9e` |
| `docs/200-architecture/206-security/README.md` | yes | `9d516a9e`, `bbaf4af5`, `06fd49c3` |
| `docs/200-architecture/README.md` | yes | `06fd49c3` |
| `docs/300-development/303-testing/test-suite/LOGIN-TEST-REPORT.md` | yes | `9d516a9e` |
| `docs/300-development/303-testing/test-suite/LOGIN-TEST-SUITE.md` | yes | `9d516a9e`, `cfd4ef04` |
| `docs/300-development/302-code-standards/JPA-LAZY-LOADING-GUIDE.md` | yes | `cfd4ef04` |
| `.claude/agents/java-architect.md` | yes | `bbaf4af5` |
| `.claude/skills/motion-design/SKILL.md` | yes | `e1eee940` |
| `.claude/skills/api-rest/SKILL.md` | yes | `e1eee940` |
| `docs/200-architecture/203-design/CICLO-DE-VIDA.md` | yes | `b515ded5` |
| `docs/300-development/INGENIERIA.md` | yes | `b515ded5` |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | `b515ded5` |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/README.md` | yes | `a2af5d57`, `92ddd3d6` |
| `docs/200-architecture/206-security/THREAT-MODEL-SYSTEM.md` | yes | `a2af5d57`, `9910cbe8` |
| `docs/github/README.md` | yes | `a2af5d57` |
| `frontend/messages/en.json` | yes | `8dcc4fdb` |
| `.claude/skills/plantuml/references/diagram-types.md` | yes | `9255c01b` |
| `.claude/skills/ai-agent-workflow/SKILL.md` | yes | `9255c01b` |
| `.claude/skills/flyway/SKILL.md` | yes | `0cf7b56a` |
| `.claude/rules/database-migrations.md` | yes | `0cf7b56a` |
| `.claude/skills/plantuml/references/styling-and-layout.md` | yes | `0cf7b56a` |
| `README.md` | yes | `2f908ff4` |
| `frontend/README.md` | yes | `2f908ff4` |
| `infra/observability/prometheus/postgres_exporter.yml` | yes | `b45818e4` |
| `infra/observability/grafana/provisioning/dashboards/notaire-postgres.json` | yes | `b45818e4` |
| `infra/tests/test_postgres_exporter_english.py` | yes | `b45818e4` |
| `docs/README.md` | yes | `26380a0b` |
| `backend-api/README.md` | yes | `26380a0b` |
| `local-ai/README.md` | yes | `a018ccde` |
| `local-ai/setup-omlx-codex.sh` | yes | `1db2219f` |
| `local-ai/stop-omlx.sh` | yes | `1db2219f` |
| `docs/presentations/sdd-framework-guide.html` | yes | `6d28448c` |
| `docs/presentations/README.md` | yes | `6d28448c` |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | yes | `9910cbe8` |
| `docs/200-architecture/206-security/MONITORING.md` | yes | `9910cbe8` |
| `docs/200-architecture/206-security/diagrams/trust-boundaries.puml` | yes | `9910cbe8` |
| `github-page/components/TechEvolution.tsx` | yes | `9910cbe8` |
| `security/tests/test_security_policy.py` | yes | `9910cbe8` |
| `docs/openspec/NOTAIRE-ADAPTATIONS.md` | yes | `23efc7dc` |
| `docs/tests/test_openspec_adaptations_english.py` | yes | `23efc7dc` |
| `docs/200-architecture/204-diagrams/data-model-core.puml` | yes | `a2c7d67d` |
| `docs/200-architecture/204-diagrams/backend-package-structure.puml` | yes | `a2c7d67d` |
| `docs/200-architecture/204-diagrams/architecture-target.puml` | yes | `a2c7d67d` |
| `docs/200-architecture/204-diagrams/architecture-legacy.puml` | yes | `a2c7d67d` |
| `docs/tests/test_architecture_diagrams_english.py` | yes | `a2c7d67d` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-001-login-account-lockout.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-002-login-token-validation.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-003-login-attempt-service.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-004-login-traceability-matrix.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-005-login-happy-path.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-006-login-wrong-password.puml` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Secuencias/seq-007-login-test-suite-overview.puml` | yes | `e49e1e14` |
| `docs/tests/test_login_sequences_english.py` | yes | `e49e1e14` |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/workflow-gestion.puml` | yes | `1ff0c98d` |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml` | yes | `1ff0c98d` |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.png` | yes | `1ff0c98d` |
| `docs/tests/test_state_diagrams_english.py` | yes | `1ff0c98d` |
| `docs/200-architecture/204-diagrams/Secuencias/seq01-ingresar-gestión-de-escritura.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq02-ingresar-gestión-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq03-ingresar-gestión-de-protocolo.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq04-ingresar-documentación.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq05-preparar-escritura.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq06-firmar-escritura.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq07-inscribir-escritura.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq08-ingresar-nuevo-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq09-modificar-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq10-eliminar-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq11-ingresar-nueva-persona.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq12-modificar-persona.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq13-eliminar-persona.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq14-ingresar-nueva-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq15-modificar-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq16-eliminar-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq17-buscar-gestiones-de-un-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq18-ingresar-nuevo-estado-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq19-modificar-estado-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq20-eliminar-estado-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq21-ingresar-nuevo-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq22-modificar-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq23-eliminar-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq24-ingresar-nuevo-tipo-de-documento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq25-modificar-tipo-de-documento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq26-ingresar-nuevo-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq27-ingresar-nuevo-tipo-de-documento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq28-ingresar-nuevos-folios.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq29-ingresar-nuevo-concepto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq30-ingresar-nuevo-estado-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq31-modificar-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq32-modificar-tipo-de-documento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq33-modificar-folio.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq34-modificar-concepto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq35-modificar-estado-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq36-ingresar-tipos-de-folio.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq37-eliminar-concepto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq38-eliminar-tipo-de-documento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq39-crear-plantilla-presupuesto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq40-modificar-tipo-de-folio.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq41-informar-próximos-vencimientos.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq42-reingresar-documentación.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq43-reingresar-testimonio.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq44-modificar-presupuesto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq45-ver-detalle-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq46-consultar-pago.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq47-dar-alta-escribano.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq48-eliminar-plantilla-presupuesto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq49-generar-declaración-jurada-de-rentas.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq50-modificar-escribano.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq51-eliminar-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq52-eliminar-tipo-de-folio.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq53-buscar-presupuesto.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq54-buscar-persona-o-cliente.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq55-buscar-escritura.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq56-buscar-folios.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq57-buscar-tipo-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq58-buscar-tipos-de-documentos.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq59-buscar-conceptos.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq60-buscar-estados-de-gestión.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq61-buscar-tipos-de-folios.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq62-gestión-de-inmuebles.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq63-gestión-de-copias.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq64-gestión-de-ítems.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq65-gestión-de-documentos-presentados.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq66-registro-de-auditoría.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq67-estrategia-de-rendimiento-y-caché.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq68-gestión-de-base-de-datos-y-migraciones.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq69-aseguramiento-de-calidad-e-infraestructura-de-pruebas.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq70-monitoreo-de-operaciones-y-gestión-de-incidentes.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq71-seguridad,-privacidad-y-cumplimiento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq72-administrar-plantillas-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq73-administrar-cuadernos-de-folios.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq74-gestión-de-trámites-en-protocolo-auxiliar.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq75-generar-minuta-de-inscripción.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq76-definir-workflow-de-estados-y-transiciones.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq77-monitoreo-de-operaciones-y-gestión-de-incidentes.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq78-seguridad-privacidad-y-cumplimiento.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq79-administrar-plantillas-de-trámite.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq80-administrar-cuadernos-de-folios.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq81-gestión-de-trámites-en-protocolo-auxiliar.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq82-generar-minuta-de-inscripción.puml` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Secuencias/seq83-definir-workflow-de-estados-y-transiciones.puml` | yes | `f23fd11c` |
| `docs/tests/test_cu_sequences_english.py` | yes | `f23fd11c` |
| `docs/200-architecture/204-diagrams/Casos de Uso/actores.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-conceptos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-documentos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-escribanos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-estados-de-gestion.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-plantilla-presupuestos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-tipos-de-folio.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-tramites.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-usuarios.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/clientes.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-documentacion.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-escrituras.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-gestion.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-inscripciones.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-testimonios.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/pagos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/presupuestos.puml` | yes | `b85893d3` |
| `docs/200-architecture/204-diagrams/Casos de Uso/protocolos.puml` | yes | `b85893d3` |
| `docs/tests/test_usecase_diagrams_english.py` | yes | `b85893d3` |
| `docs/openspec/specs/escritura-firma/spec.md` | yes | `685196ee` |
| `docs/openspec/specs/presupuesto-plantilla/spec.md` | yes | `685196ee` |
| `docs/openspec/specs/presupuesto-catalogo-items/spec.md` | yes | `685196ee` |
| `docs/openspec/specs/pago-recibo/spec.md` | yes | `685196ee` |
| `docs/openspec/specs/persona-credencial-escribano/spec.md` | yes | `685196ee` |
| `docs/tests/test_openspec_requirements_english.py` | yes | `685196ee`, `cab12b90`, `9b184c0b`, `4ae0b2f6`, `2a7ea0e2`, `425e1675`, `ccc91452`, `d0bd548b` |
| `docs/openspec/specs/copia-validacion-testimonio-inscripto/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/documento-presentado-herencia-vencimiento/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/costos-documentos-presupuesto/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/numeracion-escrituras/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/folio-vinculacion-escritura/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/testimonio-generacion-verificacion/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/protocolo-auxiliar/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/pagos-parciales-cuotas/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/carpetas-de-tramite/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/descuentos-recargos-presupuesto/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/persona-validacion-duplicados/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/gestion-workflow-transicion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/gestion-bitacora/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/minuta-inscripcion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/testimonio-movimiento-inscripcion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/tipo-documento-vencimiento-config/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/cuadernos-de-folios/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/api-authorization/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/pago-presupuesto-gestion-summary/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/presupuesto-tramite-relation/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/gestion-asignacion-suplencia/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/deed-management-entity-null-safety/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/gestion-archive-debt-check/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/pago-limite-saldo-pendiente/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/pago-presupuesto-picker-saldo/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/pagos/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/presupuesto-from-template/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/procedure-fk-hydration/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/inmueble-valuacion-fiscal/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/submitted-document-update/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/testimony-reentry-data/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/budget-person-response/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/backend-test-isolation/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/admin-route-guard/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/dashboard-nav-canonical-routes/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/next-middleware-to-proxy/spec.md` | yes | `ccc91452` |
| `docs/200-architecture/203-design/GESTION.md` | yes | `031407a6` |
| `docs/tests/test_gestion_design_english.py` | yes | `031407a6` |
| `docs/openspec/specs/frontend-i18n-page-coverage/spec.md` | yes | `d0bd548b` |
| `docs/openspec/specs/icon-button-accessible-names/spec.md` | yes | `d0bd548b` |
| `docs/openspec/specs/k6-load-test-restore/spec.md` | yes | `d0bd548b` |
| `docs/openspec/specs/report-pdfs/spec.md` | yes | `d0bd548b` |
| `docs/openspec/specs/api-request-dto-binding/spec.md` | yes | `d0bd548b` |
| `frontend/next.config.ts` | yes | `8ada6944` |
| `frontend/src/lib/status-maps.ts` | yes | `8ada6944` |
| `docs/tests/test_source_comments_english.py` | yes | `8ada6944` |
| `docs/openspec/explore.md` | yes | `c62220a0` |
| `docs/tests/test_explore_index_english.py` | yes | `c62220a0` |
| `docs/tests/test_bruno_required_messages_english.py` | yes | `60bc713e` |
| `backend-api/api-test/testimonies/04a-update-empty-body-rejected.yml` | yes | `60bc713e` |
| `backend-api/api-test/testimonies/04c-update-missing-version-rejected.yml` | yes | `60bc713e` |
| `backend-api/api-test/procedures/04a-update-empty-body-rejected.yml` | yes | `60bc713e` |
| `backend-api/api-test/procedure-types/04a-assign-workflow-empty-body-rejected.yml` | yes | `60bc713e` |
| `docs/tests/test_registration_draft_statuses_english.py` | yes | `3d9d3ead` |
| `backend-api/src/main/java/com/licensis/notaire/business/RegistrationDraft.java` | yes | `3d9d3ead` |
| `backend-api/src/main/java/com/licensis/notaire/business/BusinessConstants.java` | yes | `3d9d3ead`, `33581762` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/registrationdraft/RegistrationDraftService.java` | yes | `3d9d3ead`, `33581762` |
| `docs/200-architecture/203-design/CICLO-DE-VIDA.md` | yes | `3d9d3ead` |
| `docs/300-development/EVALUACION-INGENIERIA.md` | yes | `3d9d3ead` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftServiceTest.java` | yes | `3d9d3ead`, `33581762`, `c20fdf1e`, `b070c19b`, `7bc6c2ff`, `f2eaecef` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftControllerTest.java` | yes | `3d9d3ead`, `81b6f900` |
| `docs/tests/test_business_constants_english_names.py` | yes | `33581762` |
| `backend-api/src/main/java/com/licensis/notaire/business/Deed.java` | yes | `33581762` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/deed/DeedController.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/deed/DeedSigningService.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/document/ExternalEntityDocumentService.java` | yes | `33581762` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/testimony/TestimonyGenerationVerificationService.java` | yes | `33581762` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedEntityTest.java` | yes | `33581762`, `7696c551`, `fee62e1f`, `6aa61c6d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedSigningServiceTest.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ExternalEntityDocumentServiceTest.java` | yes | `33581762`, `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyGenerationServiceTest.java` | yes | `33581762` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementExternalEntityDocumentsIntegrationTest.java` | yes | `33581762`, `898e02d9` |
| `frontend/src/hooks/useDeedSearch.ts` | yes | `33581762` |
| `docs/tests/test_workflow_tracker_stored_statuses.py` | yes | `c74ed6a9` |
| `docs/tests/test_folio_status_comment_english.py` | yes | `92ddd3d6` |
| `backend-api/src/main/java/com/licensis/notaire/business/Folio.java` | yes | `92ddd3d6` |
| `docs/tests/test_workflow_fixtures_english.py` | yes | `7b5b2987` |
| `backend-api/src/test/java/com/licensis/notaire/integration/WorkflowTransitionIntegrationTest.java` | yes | `7b5b2987`, `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/WorkflowValidationServiceTest.java` | yes | `7b5b2987` |
| `backend-api/src/test/java/com/licensis/notaire/unit/WorkflowTransitionControllerTest.java` | yes | `7b5b2987` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ManagementQueryServiceTest.java` | yes | `7b5b2987` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ControllerResponseRecordsIntegrationTest.java` | yes | `7b5b2987` |
| `docs/tests/test_living_spec_leftovers_english.py` | yes | `c678b8eb` |
| `docs/openspec/specs/history-order/spec.md` | yes | `c678b8eb` |
| `docs/openspec/specs/jpa-namedquery-english-names/spec.md` | yes | `c678b8eb` |

| `docs/tests/test_synthetic_fixture_strings_english.py` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubstitutionServiceIntegrationTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubstitutionRepositoryIntegrationTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/integration/DeedManagementRepositoryIntegrationTest.java` | yes | `7696c551`, `407149d9`, `c2dbf52a`, `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementControllerIntegrationTest.java` | yes | `7696c551`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/CatalogControllersIntegrationTest.java` | yes | `7696c551`, `e7136a8b`, `fee62e1f`, `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java` | yes | `7696c551`, `6aa61c6d`, `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/unit/FolioTypeControllerTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java` | yes | `7696c551`, `b070c19b`, `c002efd5`, `74638c58`, `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/unit/application/usecase/concept/ConceptServiceTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/unit/adapter/out/concept/ConceptPersistenceAdapterTest.java` | yes | `7696c551` |

| `docs/tests/test_remaining_fixture_names_english.py` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ConceptReferentialIntegrityTest.java` | yes | `e7136a8b`, `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementStatusReferentialIntegrityTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureTypeReferentialIntegrityTest.java` | yes | `e7136a8b`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | yes | `e7136a8b`, `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateRepositoryIntegrationTest.java` | yes | `e7136a8b`, `407149d9`, `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateSerializationIntegrationTest.java` | yes | `e7136a8b`, `b3a1e90e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureSerializationIntegrationTest.java` | yes | `e7136a8b`, `b3a1e90e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementHistoryOrphanWriteIntegrationTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementArchiveIntegrationTest.java` | yes | `e7136a8b`, `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubstitutionControllerIntegrationTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BusinessWorkflowIntegrationTest.java` | yes | `e7136a8b`, `407149d9`, `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/unit/WorkflowValidationServiceTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AdditionalControllersTest.java` | yes | `e7136a8b`, `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ManagementStatusLogServiceTest.java` | yes | `e7136a8b` |

| `docs/tests/test_type_fixture_names_english.py` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/DocumentTypeReferentialIntegrityTest.java` | yes | `407149d9`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementCaseSummaryIntegrationTest.java` | yes | `407149d9`, `574ab536` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureControllerFkHydrationIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/UpcomingExpirationControllerIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/WorkflowTraceApiH2IntegrationTest.java` | yes | `407149d9`, `b3a1e90e`, `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentControllerTest.java` | yes | `407149d9`, `c20fdf1e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentPartialUpdateIntegrationTest.java` | yes | `407149d9`, `c20fdf1e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonIdentificationTypeIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RemainingControllersIntegrationTest.java` | yes | `407149d9`, `6311c70f` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TraceabilityMatrixTest.java` | yes | `407149d9` |
| `frontend/src/tests/unit/people-identification-type.test.tsx` | yes | `407149d9` |

| `docs/tests/test_synthetic_notes_english.py` | yes | `c20fdf1e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetPersonAssociationPgIntegrationTest.java` | yes | `c20fdf1e`, `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyControllerIntegrationTest.java` | yes | `c20fdf1e`, `81b6f900`, `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyVerificacionServiceTest.java` | yes | `c20fdf1e` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftControllerTest.java` | yes | `c20fdf1e`, `81b6f900` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/DeedManagementJpaController.java` | yes | `c20fdf1e` |
| `frontend/src/hooks/useSubmittedDocuments.test.tsx` | yes | `c20fdf1e` |
| `frontend/src/tests/unit/dashboard-hero.test.tsx` | yes | `c20fdf1e` |
| `testing/e2e/tests/TS-0012-documentation-testimony-workflow.spec.ts` | yes | `c20fdf1e` |
| `testing/e2e/tests/TS-0031-testimony-generation-verification-feature.spec.ts` | yes | `c20fdf1e` |
| `testing/e2e/tests/TS-0082-registration-draft-feature.spec.ts` | yes | `c20fdf1e`, `f2eaecef` |
| `testing/e2e/tests/TS-0032-testimony-registration-feature.spec.ts` | yes | `c20fdf1e` |

| `docs/tests/test_synthetic_chrome_english.py` | yes | `fee62e1f` |
| `frontend/src/tests/unit/app-header.test.tsx` | yes | `fee62e1f` |
| `frontend/src/tests/unit/confirm-dialog.test.tsx` | yes | `fee62e1f` |
| `frontend/src/tests/unit/dialog.test.tsx` | yes | `fee62e1f` |
| `frontend/src/tests/unit/dashboard-layout.test.tsx` | yes | `fee62e1f` |
| `frontend/src/tests/unit/data-table-mobile.test.tsx` | yes | `fee62e1f` |
| `frontend/src/tests/unit/button.test.tsx` | yes | `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateServiceIntegrationTest.java` | yes | `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/unit/BudgetTemplateEntityTest.java` | yes | `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/unit/SubmittedDocumentEntityTest.java` | yes | `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ManagementCaseSummaryServiceTest.java` | yes | `fee62e1f` |

| `docs/tests/test_synthetic_labels_english.py` | yes | `c2dbf52a`, `574ab536` |
| `frontend/src/tests/unit/form-field.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/select.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/status-badge.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/person-picker.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/management-lists-pagination.test.tsx` | yes | `c2dbf52a` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/ManagementQueryServiceTest.java` | yes | `c2dbf52a`, `64690769`, `5537557d` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/DeedServiceTest.java` | yes | `c2dbf52a`, `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/BudgetServiceTest.java` | yes | `c2dbf52a` |
| `backend-api/src/test/java/com/licensis/notaire/unit/BudgetEntityTest.java` | yes | `c2dbf52a`, `6aa61c6d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ProcedureEntityTest.java` | yes | `c2dbf52a`, `6aa61c6d`, `7bc6c2ff` |

| `docs/tests/test_synthetic_phrases_english.py` | yes | `b3a1e90e` |
| `frontend/src/tests/unit/data-table.test.tsx` | yes | `b3a1e90e` |
| `frontend/src/tests/unit/pages.test.tsx` | yes | `b3a1e90e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetRepositoryIntegrationTest.java` | yes | `b3a1e90e`, `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/adapter/in/web/payment/PaymentControllerTest.java` | yes | `b3a1e90e`, `898e02d9` |

| `docs/tests/test_updated_phrases_english.py` | yes | `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ItemControllerTest.java` | yes | `90bc40fd`, `81b6f900` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentRepositoryIntegrationTest.java` | yes | `90bc40fd`, `64690769`, `6311c70f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RoleRepositoryIntegrationTest.java` | yes | `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyMovementControllerIntegrationTest.java` | yes | `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyCopyReportIntegrationTest.java` | yes | `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/NotebookControllerTest.java` | yes | `90bc40fd`, `b070c19b` |

| `docs/tests/test_mixed_phrases_english.py` | yes | `81b6f900` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ItemServiceTest.java` | yes | `81b6f900` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DataInitializerTest.java` | yes | `81b6f900` |

| `docs/tests/test_entity_roundtrip_english.py` | yes | `6aa61c6d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/PaymentEntityTest.java` | yes | `6aa61c6d`, `7bc6c2ff`, `f2eaecef` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedManagementEntityTest.java` | yes | `6aa61c6d` |

| `docs/tests/test_signing_identifiers_english.py` | yes | `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedNumberingUseCaseTest.java` | yes | `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/DeedSigningControllerIntegrationTest.java` | yes | `b070c19b` |

| `docs/tests/test_fixture_names_english.py` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentUseCaseIntegrationTest.java` | yes | `64690769`, `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentIntegrationTest.java` | yes | `64690769`, `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonServiceIntegrationTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/AuditRecordServiceTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AuditRecordServiceTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AuditRecordControllerTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/adapter/out/persistence/payment/PaymentPersistenceAdapterTest.java` | yes | `64690769`, `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/CoreBusinessControllersIntegrationTest.java` | yes | `64690769`, `7bc6c2ff`, `f2eaecef`, `6311c70f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/AuditRecordRepositoryIntegrationTest.java` | yes | `64690769` |

| `docs/tests/test_address_fixtures_english.py` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRepositoryIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyServiceIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyFiscalAppraisalPgIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RequestBodyDtoBindingIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRequestValidationIntegrationTest.java` | yes | `7bc6c2ff`, `f2eaecef` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonRepositoryIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/unit/PersonEntityTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/unit/PaymentUseCaseTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/application/usecase/payment/ProcessPaymentServiceTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RoleRequestValidationIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/IdentificationTypeValidationIntegrationTest.java` | yes | `7bc6c2ff` |

| `docs/tests/test_property_words_english.py` | yes | `f2eaecef` |
| `frontend/src/app/dashboard/properties/page.tsx` | yes | `f2eaecef` |
| `testing/e2e/tests/gherkin-helpers.ts` | yes | `f2eaecef` |
| `testing/e2e/tests/TS-0019-properties-valuation-workflow.spec.ts` | yes | `f2eaecef` |

| `docs/tests/test_helper_names_english.py` | yes | `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateControllerIntegrationTest.java` | yes | `c002efd5` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/ConceptJpaController.java` | yes | `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/unit/BudgetCatalogItemsServiceTest.java` | yes | `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/unit/BudgetTemplateServiceTest.java` | yes | `c002efd5` |
| `frontend/src/app/dashboard/external-entity-documents/page.tsx` | yes | `c002efd5` |

| `docs/tests/test_local_identifiers_english.py` | yes | `6311c70f` |
| `frontend/src/app/dashboard/payments/page.tsx` | yes | `6311c70f` |
| `frontend/src/hooks/useReports.ts` | yes | `6311c70f` |
| `frontend/src/app/dashboard/managements/page.tsx` | yes | `6311c70f` |
| `frontend/src/app/dashboard/documents/page.tsx` | yes | `6311c70f` |
| `frontend/src/app/dashboard/administration/templates/page.tsx` | yes | `6311c70f` |
| `frontend/src/tests/unit/auth-store.test.ts` | yes | `6311c70f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/AuxiliaryProtocolControllerTest.java` | yes | `6311c70f` |

| `docs/tests/test_numbering_names_english.py` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/repository/FolioRepository.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/application/port/out/folio/FolioRepositoryPort.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/folio/FolioPersistenceAdapter.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/notebook/AuxiliaryProtocolService.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/AuxiliaryProtocolServiceTest.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyMovementServiceTest.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ConceptDeleteCascadeIntegrationTest.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PreviouslyBrokenEndpointsIntegrationTest.java` | yes | `74638c58`, `c3fcefb9` |

| `docs/tests/test_leftover_fixture_words_english.py` | yes | `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/EdgeCaseBoundaryConditionsTest.java` | yes | `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ManagementArchiveDebtServiceTest.java` | yes | `898e02d9` |

| `docs/tests/test_hash_and_movement_names_english.py` | yes | `c3fcefb9` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/user/UserController.java` | yes | `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/CopyControllerTest.java` | yes | `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/FlywaySchemaValidationIntegrationTest.java` | yes | `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyEntityTest.java` | yes | `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentReceiptReportIntegrationTest.java` | yes | `c3fcefb9` |

| `docs/tests/test_cu10_comments_english.py` | yes | `5537557d` |
| `backend-api/src/main/java/com/licensis/notaire/dto/DtoManagementExternalEntityDocuments.java` | yes | `5537557d` |
| `backend-api/src/main/java/com/licensis/notaire/dto/DtoExternalEntityDocumentMovement.java` | yes | `5537557d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ReportServicePaymentReceiptTest.java` | yes | `5537557d` |

| `docs/tests/test_reentry_comments_english.py` | yes | `722bdfe3` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementDocumentReentryPgIntegrationTest.java` | yes | `722bdfe3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ConceptControllerTest.java` | yes | `722bdfe3` |

| `docs/tests/test_workflow_assignment_english.py` | yes | `b2350ea3` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/procedure/ProcedureTypeController.java` | yes | `b2350ea3` |
| `frontend/src/types/api.generated.ts` | yes | `b2350ea3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ProcedureTypeWorkflowAssignmentTest.java` | yes | `b2350ea3` |

| `docs/tests/test_workflow_stub_stored_statuses.py` | yes | `281f44d9` |
| `frontend/src/components/motion/WorkflowTracker.tsx` | yes | `281f44d9` |
| `testing/e2e/tests/TS-0138-workflow-tracker.spec.ts` | yes | `281f44d9` |

| `frontend/src/tests/unit/budget-picker.test.tsx` | yes | `574ab536` |
| `frontend/src/tests/unit/people-pagination.test.tsx` | yes | `574ab536` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonRequestValidationIntegrationTest.java` | yes | `574ab536` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetSummaryControllerTest.java` | yes | `574ab536` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementFinancialSummaryControllerTest.java` | yes | `574ab536` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ReportsUseCaseIntegrationTest.java` | yes | `574ab536` |
| `testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts` | yes | `574ab536` |
| `testing/e2e/tests/TS-0110-people-pagination.spec.ts` | yes | `574ab536` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1490, this change folder |
| 2 | Failing tests written, test cases designed | yes | `EnglishSurfaceRatchetTest` red run, then green |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

Cloud agent branch policy requires the prefix `cursor/` and the suffix `-01e0`, so the branch is `cursor/english-codebase-01e0` rather than `refactor/1490_english_codebase`. No other Constitution step is skipped. Issue labeling `in-progress` failed with `Resource not accessible by integration`; the issue remains open as #1490.
