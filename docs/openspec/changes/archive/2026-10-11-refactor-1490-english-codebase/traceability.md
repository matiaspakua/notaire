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
| Commits | `9c536900`, `329247ba`, `86b51c34`, `93300933`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `2c345ab5`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `e42e1487`, `abde581c`, `68d84c39`, `c58be179`, `dd015ac3`, `797b268c`, `ffb8fdd5`, `88a7e431`, `ffe5aaa4`, `db6a4876`, `7a9458cb`, `c7d87636`, `e48ddf2a`, `ec567185`, `d1aa0d4c`, `df1c22fc`, `14abf883`, `315c83ab`, `0cb342a1`, `503354fc`, `be66ad75`, `51c25a65`, `298d04df`, `676003db`, `4177e611`, `bd883977`, `aba4adb4`, `ee51a917`, `8908af20`, `ee860266`, `01771ab1`, `fdcf0788`, `66c8a54d`, `c690d487`, `7a10f083`, `471334a5`, `8b1b4322`, `e030d2a1`, `9935ac81`, `4470dbfc`, `eb3ccb6f`, `49c85360`, `99b66958`, `602d7019`, `fd6fa760`, `6ee0f0c1`, `0e320a6f`, `f6a28763`, `88602fcc`, `0a259320`, `0523aae6`, `fe5ee462`, `6dd60865`, `b9840ded`, `0a82b3b5`, `74671e88`, `8352fccd`, `509c9a6c`, `b418e6ce`, `2a860c47`, `ce5010fc`, `f3d76e63`, `8067d797`, `1f9bf2f9`, `03bfc567`, `1ba4d9fa`, `87b763a0`, `85a6d8cd`, `ceb44c77`, `48c6a8c1`, `9cef2ec7`, `e525076f`, `04e51c02`, `27b151eb`, `f5cba10d`, `4cf2154f`, `c284f284`, `ce6b5232`, `78ea0fd3`, `ee6039ca`, `525c7d5a`, `d19f616e`, `da546cc9`, `d4a2835f`, `d9c52f61`, `12aecc5a`, `c8b59dae`, `7f310e87`, `75ac45cb`, `32537417`, `e5ba1643`, `8dd85500`, `5e770b15`, `3f053de7`, `ddcde4c6`, `4c2aa084`, `2d1ff534`, `a82f7970`, `460faff6`, `6ad9a8e1`, `394f2a7b`, `6f1dbf8a`, `8edd4a58`, `8fc1a84f`, `93f0fea0`, `6b929449`, `5f5ef2e3`, `7c36581e`, `30b23526`, `d302d8ad`, `616c2dc8`, `b75d413f`, `3346b60e`, `70768655`, `133e0d18`, `72e4e319`, `42766598`, `5526fdcd`, `e2e77bff`, `946444e9`, `6b0939f8`, `64febfea`, `c63cd5ce`, `a890ac30`, `f28bad22`, `f89c6a27`, `5ff1eaad`, `f481f222`, `2078c1db`, `ec19f220`, `65389a43`, `e0f918d2`, `b7c28b96`, `74442f87`, `55718f0d`, `6e7e3a50`, `2b16b5f3`, `12f5e927`, `a020e895`, `b510ffdb`, `83d7d40d`, `a24bdc1f`, `b83c898f`, `9d516a9e`, `a059c070`, `cfd4ef04`, `504979c8`, `94db1685`, `65f3d68a`, `91713896`, `faa340fd`, `a7499837`, `74b403b2`, `bbaf4af5`, `ca9df71b`, `a4bdb7ab`, `6bc93d56`, `e1eee940`, `c45d7218`, `a6f3d7f5`, `b5bda30c`, `06fd49c3`, `69c49107`, `372c7f58`, `bc1495c9`, `b515ded5`, `282777a1`, `a2af5d57`, `99facf69`, `8dcc4fdb`, `e8508f2b`, `9255c01b`, `30eca4e0`, `0cf7b56a`, `87a8c294`, `2f908ff4`, `39b1fc03`, `b45818e4`, `b8ad8a96`, `26380a0b`, `1435de68`, `a018ccde`, `c749de1e`, `1db2219f`, `64c6865a`, `6d28448c`, `bf27f315`, `9910cbe8`, `3a58d7b4`, `23efc7dc`, `bcbeeb73`, `a2c7d67d`, `a92bc8e3`, `e49e1e14`, `e1fb3580`, `1ff0c98d`, `1c33655a`, `f23fd11c`, `91172e2f`, `b85893d3`, `71b50a3b`, `685196ee`, `12378608`, `a06975d1`, `cab12b90`, `8c758a94`, `9b184c0b`, `982e941d`, `4ae0b2f6`, `e37feb4a`, `2a7ea0e2`, `0a7b9002`, `425e1675`, `483fe4b4`, `ccc91452`, `08148a9c`, `031407a6`, `ef158459`, `d0bd548b`, `619aeac3`, `8ada6944`, `66dab39d`, `c62220a0`, `5c8a3477`, `60bc713e`, `3d9d3ead`, `e73a4cb4`, `33581762`, `2df0b29a`, `c74ed6a9`, `f8abaebc`, `92ddd3d6`, `d61159ff`, `7b5b2987`, `dcfe02b2`, `1dff09c8`, `c678b8eb`, `0d6aa68c`, `7696c551`, `dec17002`, `f7df6959`, `e7136a8b`, `7c6965fe`, `407149d9`, `4feca8c4`, `b985d266`, `c20fdf1e`, `f3123456`, `fee62e1f`, `c2dbf52a`, `055c0889`, `b3a1e90e`, `c6b658f1`, `90bc40fd`, `408f3402`, `81b6f900`, `0979e875`, `6aa61c6d`, `0bc47452`, `b070c19b`, `6c9ec69d`, `64690769`, `615969fa`, `7bc6c2ff`, `883c534d`, `f2eaecef`, `a67c5210`, `c002efd5`, `1187fd4b`, `6311c70f`, `139ffd4e`, `74638c58`, `b98b953d`, `898e02d9`, `f0cfa95e`, `c3fcefb9`, `111c09d3`, `5537557d`, `a2c15f02`, `722bdfe3`, `d3757bea`, `b2350ea3`, `89ad4c17`, `281f44d9`, `93b3b9e0`, `d0f75bb9`, `85ff9133`, `ca508cb1`, `36196fe6`, `b62f8b43`, `8b8ccca9`, `0d2c4956`, `d70228d3`, `4c8bc5fa`, `f376d4a3`, `3f9719e3`, `66f3502c`, `eb7692dd`, `27dc16c7`, `f184335e`, `a6c82a6a`, `716ba3f8`, `052f1193`, `c28949a7`, `6c3159a8`, `25120aa1`, `1bff34d4`, `466feae0`, `ecd2df0c`, `6b1d3c16`, `316db3b7`, `96636752`, `47ceaefa`, `f00eae94`, `d0a89b0b`, `0d42e9ea`, `7b226424`, `3d603a44`, `3711b352`, `0eedb40d`, `09fdac40`, `151d6123`, `acfebea4`, `6f8ec0b4`, `9943952c`, `616c348d`, `e8bca5ac`, `6c5fca82`, `1fbb111f`, `7c3aeb82`, `ef4d2c62`, `f55164be`, `3eb9a633`, `cb0c6d45`, `7e978d63`, `f2cd7e08`, `39f8a646`, `e7b9c574`, `ffc8819e`, `b413ac7c`, `6980fc90`, `e3c030ca`, `2cf2a3e2`, `7ab8606e`, `f00fbe09`, `8e57d22f`, `7dd4455e`, `87c8535c`, `eb1d5112`, `5f17c820`, `3608771c`, `f48a7098`, `185335ae`, `fd9a5751`, `d0688a8f`, `a40bfe08`, `3044c768`, `1150e6ac`, `68383654`, `1ac61b51`, `68256a51`, `77b0f724`, `bdb0b8f8`, `12394f83`, `89367a0b`, `06939ea3`, `712c0fe7`, `bd4422ec`, `53c7df06`, `51f80fff`, `6ba425fe`, `f9d2e811`, `90e2d691`, `fe1bbeda`, `7cb21b10`, `f39dd455`, `5a62e7f3`, `e18febca`, `799a0229`, `255c4dc2`, `114da58c`, `85802a05`, `dc5bbf00`, `2c578231`, `cee401bb`, `6cfcdb06`, `c2c9dd7a`, `f00b0504`, `a5e7b6cb`, `eaee1b43`, `d78fcc4e`, `8b53e106`, `c51b97cd`, `30d053b5`, `a2cf628c`, `b72e0d19`, `02903a80`, `fd487dee`, `47401bd6`, `33ddeb1e`, `b1a571ac`, `7f9e1e81`, `b7f9bfa1`, `aaf2327d`, `cb2f02b7`, `a264891c`, `1a7d3754`, `db5398c6`, `59d7eda8`, `c24fb306`, `8406b707`, `b8a3b665`, `505daba5`, `68385f11`, `f32ad809`, `e15b3458`, `a34e9111`, `6593bee6`, `4c20f254`, `0158965a`, `82e02de1`, `fad72604`, `8f108095`, `ab13edfb`, `bdd1161f`, `91c8e352`, `6586ddbe`, `330e6617`, `90d88f1c`, `b83a784d`, `0035db46`, `913a0252`, `ed47ea59`, `a2e8b71a`, `c2aa66a5`, `a21ca799`, `616dbacf`, `ed5632e9`, `0c344ddc`, `a4c455af`, `0593a379`, `62f70ceb`, `5738a72d`, `cc7960b8`, `820b8e48`, `e0e8e381`, `4f58ca0a`, `81349b33`, `f71c9db6`, `65cd5cbe`, `d4f3407c`, `5e593205`, `6a9faf26`, `81108500`, `fa97016e`, `fc7b2d25`, `e20a2730`, `bb8f5d8e`, `67e7f119`, `7b62e1a3`, `1c03856a`, `fe17aed2`, `e878c407`, `919b28cc`, `7583a11c`, `2e88f806`, `f4009cf6`, `de113fa5`, `9afae68e`, `62a4e621`, `133a7d5a`, `8fc46b4e`, `c0576df8`, `d11c0d57`, `4dc9f29c`, `6485d7d9`, `67254e51`, `3a8b560e`, `36a11c23`, `6f659fef`, `000b4398`, `da98a949`, `2790233d`, `9747bad1`, `38c04fe1`, `d442878b`, `a034a7a8`, `5f984084`, `19c52114`, `6103d090`, `706a5da5`, `61cc5b78`, `94dbb347`, `ef8e9ec1`, `ed767b4c`, `e49931c3`, `881301fa`, `68660e3e`, `17ce57b6`, `edd90411`, `63ae35f0`, `b9fc512a`, `b332872a`, `6b725831`, `4ec5de7a`, `8ef1c17c`, `4658f973`, `708a29ac`, `4abcc8e6`, `22485157`, `c591f415`, `05a9b315`, `3cf789a4`, `cc819746`, `58bd9025`, `a0ee20e1`, `8fd5e422`, `8295d631`, `96c18bd7`, `618417df`, `7099f68d`, `17278d7e`, `4b28e38f`, `0e0d8544`, `5c6990e2`, `38a634e4`, `b6f14c80`, `4a6d8576`, `249af851`, `b7383bda`, `ff39fd08`, `c69bedb9`, `482fea65`, `4d140ea1`, `336c4868`, `3fd224cd`, `5e43f5aa`, `8b6aa281`, `cb0c93bf`, `fd86c495`, `a5db7e1a`, `147ac4c4`, `85c0513a`, `38acfa67`, `7de5ce40` | created |
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
| `CHANGELOG.md` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `9c4f1a0b`, `bba55a53`, `7f7fcb09`, `84c16209`, `4b7af885`, `1776dc9e`, `8874487e`, `45e24710`, `7f2db1d5`, `dfd44385`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `797b268c`, `88a7e431`, `db6a4876`, `c7d87636`, `ec567185`, `df1c22fc`, `0cb342a1`, `be66ad75`, `298d04df`, `4177e611`, `aba4adb4`, `8908af20`, `01771ab1`, `66c8a54d`, `7a10f083`, `8b1b4322`, `9935ac81`, `eb3ccb6f`, `99b66958`, `fd6fa760`, `0e320a6f`, `88602fcc`, `0523aae6`, `6dd60865`, `0a82b3b5`, `8352fccd`, `b418e6ce`, `ce5010fc`, `8067d797`, `03bfc567`, `87b763a0`, `ceb44c77`, `9cef2ec7`, `04e51c02`, `f5cba10d`, `c284f284`, `78ea0fd3`, `525c7d5a`, `da546cc9`, `d9c52f61`, `c8b59dae`, `75ac45cb`, `e5ba1643`, `5e770b15`, `ddcde4c6`, `2d1ff534`, `460faff6`, `394f2a7b`, `8edd4a58`, `93f0fea0`, `5f5ef2e3`, `30b23526`, `616c2dc8`, `3346b60e`, `133e0d18`, `42766598`, `e2e77bff`, `6b0939f8`, `c63cd5ce`, `f89c6a27`, `f481f222`, `ec19f220`, `e0f918d2`, `74442f87`, `6e7e3a50`, `12f5e927`, `b510ffdb`, `a24bdc1f`, `9d516a9e`, `cfd4ef04`, `94db1685`, `91713896`, `a7499837`, `bbaf4af5`, `a4bdb7ab`, `e1eee940`, `a6f3d7f5`, `06fd49c3`, `372c7f58`, `b515ded5`, `a2af5d57`, `8dcc4fdb`, `9255c01b`, `0cf7b56a`, `2f908ff4`, `b45818e4`, `26380a0b`, `a018ccde`, `1db2219f`, `6d28448c`, `9910cbe8`, `23efc7dc`, `a2c7d67d`, `e49e1e14`, `1ff0c98d`, `f23fd11c`, `b85893d3`, `685196ee`, `cab12b90`, `9b184c0b`, `4ae0b2f6`, `2a7ea0e2`, `425e1675`, `ccc91452`, `031407a6`, `d0bd548b`, `8ada6944`, `c62220a0`, `60bc713e`, `3d9d3ead`, `33581762`, `c74ed6a9`, `92ddd3d6`, `7b5b2987`, `c678b8eb`, `7696c551`, `e7136a8b`, `407149d9`, `c20fdf1e`, `fee62e1f`, `c2dbf52a`, `b3a1e90e`, `90bc40fd`, `81b6f900`, `6aa61c6d`, `b070c19b`, `64690769`, `7bc6c2ff`, `f2eaecef`, `c002efd5`, `6311c70f`, `74638c58`, `898e02d9`, `c3fcefb9`, `5537557d`, `722bdfe3`, `b2350ea3`, `281f44d9`, `85ff9133`, `36196fe6`, `d70228d3`, `3f9719e3`, `eb7692dd`, `f184335e`, `716ba3f8`, `c28949a7`, `25120aa1`, `466feae0`, `6b1d3c16`, `96636752`, `f00eae94`, `0d42e9ea`, `3d603a44`, `0eedb40d`, `151d6123`, `6f8ec0b4`, `616c348d`, `6c5fca82`, `7c3aeb82`, `f55164be`, `cb0c6d45`, `f2cd7e08`, `e7b9c574`, `b413ac7c`, `e3c030ca`, `7ab8606e`, `8e57d22f`, `87c8535c`, `5f17c820`, `f48a7098`, `fd9a5751`, `a40bfe08`, `1150e6ac`, `1ac61b51`, `77b0f724`, `12394f83`, `06939ea3`, `bd4422ec`, `51f80fff`, `f9d2e811`, `fe1bbeda`, `f39dd455`, `e18febca`, `255c4dc2`, `85802a05`, `2c578231`, `6cfcdb06`, `f00b0504`, `eaee1b43`, `c51b97cd`, `a2cf628c`, `02903a80`, `47401bd6`, `b1a571ac`, `b7f9bfa1`, `cb2f02b7`, `1a7d3754`, `59d7eda8`, `8406b707`, `505daba5`, `f32ad809`, `a34e9111`, `4c20f254`, `82e02de1`, `8f108095`, `bdd1161f`, `6586ddbe`, `90d88f1c`, `0035db46`, `ed47ea59`, `c2aa66a5`, `616dbacf`, `0c344ddc`, `0593a379`, `5738a72d`, `820b8e48`, `4f58ca0a`, `f71c9db6`, `d4f3407c`, `6a9faf26`, `fa97016e`, `e20a2730`, `67e7f119`, `1c03856a`, `e878c407`, `7583a11c`, `f4009cf6`, `9afae68e`, `133a7d5a`, `c0576df8`, `4dc9f29c`, `67254e51`, `36a11c23`, `000b4398`, `2790233d`, `38c04fe1`, `a034a7a8`, `19c52114`, `706a5da5`, `94dbb347`, `ed767b4c`, `881301fa`, `17ce57b6`, `63ae35f0`, `b332872a`, `4ec5de7a`, `4658f973`, `4abcc8e6`, `c591f415`, `3cf789a4`, `58bd9025`, `8fd5e422`, `96c18bd7`, `7099f68d`, `4b28e38f`, `5c6990e2`, `b6f14c80`, `249af851`, `ff39fd08`, `482fea65`, `336c4868`, `5e43f5aa`, `cb0c93bf`, `a5db7e1a`, `85c0513a`, `7de5ce40` |
| `backend-api/openapi/openapi.yaml` | yes | `329247ba`, `86b51c34`, `4a3afc10`, `4e3b809e`, `48dcc181`, `d5a50e3f`, `76bed5c4`, `abde581c`, `c58be179`, `ec567185`, `0cb342a1`, `be66ad75`, `aba4adb4`, `01771ab1`, `66c8a54d`, `7a10f083`, `eb3ccb6f`, `b418e6ce`, `ce5010fc`, `b510ffdb`, `b2350ea3` |
| `docs/200-architecture/203-design/REST-API-REFERENCE.md` | yes | `4a3afc10`, `9935ac81` |
| `backend-api/api-test/COVERAGE.md` | yes | `329247ba`, `0523aae6`, `bbaf4af5` |
| `docs/200-architecture/203-design/REST-API-ENDPOINT_REGISTRY.md` | yes | `8067d797`, `96636752`, `b1a571ac` |
| `docs/300-development/303-testing/api-test/README.md` | yes | `8067d797` |
| `docs/200-architecture/201-SAD/sad.md` | yes | `03bfc567`, `2d1ff534`, `bbaf4af5`, `06fd49c3`, `a2af5d57`, `d0bd548b`, `1a7d3754` |
| `docs/300-development/303-testing/test-cases/` | yes | `460faff6` |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | `394f2a7b`, `f00eae94` |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | yes | `394f2a7b` |
| `docs/300-development/301-setup/README.md` | yes | `8edd4a58` |
| `docs/300-development/303-testing/TESTING-PATTERNS.md` | yes | `93f0fea0` |
| `docs/300-development/302-code-standards/DTO-MAPPING-GUIDE.md` | yes | `b510ffdb` |
| `docs/300-development/303-testing/FRONTEND-TESTING-GUIDE.md` | yes | `5f5ef2e3`, `42766598` |
| `docs/200-architecture/203-design/BACKEND-ERROR-HANDLING-STRATEGY.md` | yes | `5f5ef2e3` |
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | yes | `5f5ef2e3` |
| `docs/300-development/ASSESSMENT-2026-10.md` | yes | `5f5ef2e3`, `b7f9bfa1` |
| `docs/200-architecture/202-ADR/ADR-013-audit-trail.md` | yes | `03bfc567` |
| `docs/200-architecture/202-ADR/ADR-012-ci-cd-pipeline.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-014-workflow-engine.md` | yes | `87b763a0`, `06fd49c3` |
| `docs/200-architecture/202-ADR/ADR-015-internationalization.md` | yes | `87b763a0` |
| `docs/200-architecture/202-ADR/ADR-001-microservices-architecture.md` | yes | `ceb44c77` |
| `docs/200-architecture/203-design/README.md` | yes | `ceb44c77`, `a2af5d57` |
| `docs/200-architecture/202-ADR/ADR-006-testing-strategy.md` | yes | `9cef2ec7`, `f00eae94` |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | yes | `9cef2ec7`, `c74ed6a9`, `5e43f5aa` |
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
| `docs/openspec/specs/presupuesto-catalogo-items/spec.md` | yes | `685196ee`, `616dbacf` |
| `docs/openspec/specs/pago-recibo/spec.md` | yes | `685196ee` |
| `docs/openspec/specs/persona-credencial-escribano/spec.md` | yes | `685196ee` |
| `docs/tests/test_openspec_requirements_english.py` | yes | `685196ee`, `cab12b90`, `9b184c0b`, `4ae0b2f6`, `2a7ea0e2`, `425e1675`, `ccc91452`, `d0bd548b` |
| `docs/openspec/specs/copia-validacion-testimonio-inscripto/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/documento-presentado-herencia-vencimiento/spec.md` | yes | `cab12b90`, `c591f415` |
| `docs/openspec/specs/costos-documentos-presupuesto/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/numeracion-escrituras/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/folio-vinculacion-escritura/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/testimonio-generacion-verificacion/spec.md` | yes | `cab12b90` |
| `docs/openspec/specs/protocolo-auxiliar/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/pagos-parciales-cuotas/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/carpetas-de-tramite/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/descuentos-recargos-presupuesto/spec.md` | yes | `9b184c0b`, `0c344ddc` |
| `docs/openspec/specs/persona-validacion-duplicados/spec.md` | yes | `9b184c0b` |
| `docs/openspec/specs/gestion-workflow-transicion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/gestion-bitacora/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/minuta-inscripcion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/testimonio-movimiento-inscripcion/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/tipo-documento-vencimiento-config/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/cuadernos-de-folios/spec.md` | yes | `4ae0b2f6` |
| `docs/openspec/specs/api-authorization/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/pago-presupuesto-gestion-summary/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/presupuesto-tramite-relation/spec.md` | yes | `2a7ea0e2`, `cb0c93bf` |
| `docs/openspec/specs/gestion-asignacion-suplencia/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/deed-management-entity-null-safety/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/gestion-archive-debt-check/spec.md` | yes | `2a7ea0e2` |
| `docs/openspec/specs/pago-limite-saldo-pendiente/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/pago-presupuesto-picker-saldo/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/pagos/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/presupuesto-from-template/spec.md` | yes | `425e1675`, `820b8e48` |
| `docs/openspec/specs/procedure-fk-hydration/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/inmueble-valuacion-fiscal/spec.md` | yes | `425e1675` |
| `docs/openspec/specs/submitted-document-update/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/testimony-reentry-data/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/budget-person-response/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/backend-test-isolation/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/admin-route-guard/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/dashboard-nav-canonical-routes/spec.md` | yes | `ccc91452` |
| `docs/openspec/specs/next-middleware-to-proxy/spec.md` | yes | `ccc91452` |
| `docs/200-architecture/203-design/GESTION.md` | yes | `031407a6`, `02903a80`, `6586ddbe`, `90d88f1c`, `0035db46`, `4f58ca0a`, `f71c9db6` |
| `docs/tests/test_gestion_design_english.py` | yes | `031407a6`, `02903a80` |
| `docs/openspec/specs/frontend-i18n-page-coverage/spec.md` | yes | `d0bd548b` |
| `docs/openspec/specs/icon-button-accessible-names/spec.md` | yes | `d0bd548b`, `8fd5e422` |
| `docs/openspec/specs/k6-load-test-restore/spec.md` | yes | `d0bd548b`, `4c20f254` |
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
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftServiceTest.java` | yes | `3d9d3ead`, `33581762`, `c20fdf1e`, `b070c19b`, `7bc6c2ff`, `f2eaecef`, `716ba3f8` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftControllerTest.java` | yes | `3d9d3ead`, `81b6f900` |
| `docs/tests/test_business_constants_english_names.py` | yes | `33581762` |
| `backend-api/src/main/java/com/licensis/notaire/business/Deed.java` | yes | `33581762`, `4658f973` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/deed/DeedController.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/deed/DeedSigningService.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/document/ExternalEntityDocumentService.java` | yes | `33581762`, `eb7692dd` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/testimony/TestimonyGenerationVerificationService.java` | yes | `33581762` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedEntityTest.java` | yes | `33581762`, `7696c551`, `fee62e1f`, `6aa61c6d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/DeedSigningServiceTest.java` | yes | `33581762`, `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ExternalEntityDocumentServiceTest.java` | yes | `33581762`, `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyGenerationServiceTest.java` | yes | `33581762` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementExternalEntityDocumentsIntegrationTest.java` | yes | `33581762`, `898e02d9` |
| `frontend/src/hooks/useDeedSearch.ts` | yes | `33581762` |
| `docs/tests/test_workflow_tracker_stored_statuses.py` | yes | `c74ed6a9` |
| `docs/tests/test_folio_status_comment_english.py` | yes | `92ddd3d6` |
| `backend-api/src/main/java/com/licensis/notaire/business/Folio.java` | yes | `92ddd3d6`, `4658f973` |
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
| `backend-api/src/test/java/com/licensis/notaire/integration/DeedManagementRepositoryIntegrationTest.java` | yes | `7696c551`, `407149d9`, `c2dbf52a`, `64690769`, `0d42e9ea` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementControllerIntegrationTest.java` | yes | `7696c551`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/CatalogControllersIntegrationTest.java` | yes | `7696c551`, `e7136a8b`, `fee62e1f`, `c3fcefb9` |
| `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java` | yes | `7696c551`, `6aa61c6d`, `74638c58`, `36196fe6` |
| `backend-api/src/test/java/com/licensis/notaire/unit/FolioTypeControllerTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/unit/SimpleControllersTest.java` | yes | `7696c551`, `b070c19b`, `c002efd5`, `74638c58`, `898e02d9`, `3f9719e3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/application/usecase/concept/ConceptServiceTest.java` | yes | `7696c551` |
| `backend-api/src/test/java/com/licensis/notaire/unit/adapter/out/concept/ConceptPersistenceAdapterTest.java` | yes | `7696c551` |

| `docs/tests/test_remaining_fixture_names_english.py` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ConceptReferentialIntegrityTest.java` | yes | `e7136a8b`, `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementStatusReferentialIntegrityTest.java` | yes | `e7136a8b`, `6f8ec0b4` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureTypeReferentialIntegrityTest.java` | yes | `e7136a8b`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentRequestValidationIntegrationTest.java` | yes | `e7136a8b`, `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateRepositoryIntegrationTest.java` | yes | `e7136a8b`, `407149d9`, `fee62e1f` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetTemplateSerializationIntegrationTest.java` | yes | `e7136a8b`, `b3a1e90e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureSerializationIntegrationTest.java` | yes | `e7136a8b`, `b3a1e90e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementHistoryOrphanWriteIntegrationTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementArchiveIntegrationTest.java` | yes | `e7136a8b`, `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubstitutionControllerIntegrationTest.java` | yes | `e7136a8b`, `151d6123` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BusinessWorkflowIntegrationTest.java` | yes | `e7136a8b`, `407149d9`, `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/unit/WorkflowValidationServiceTest.java` | yes | `e7136a8b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AdditionalControllersTest.java` | yes | `e7136a8b`, `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ManagementStatusLogServiceTest.java` | yes | `e7136a8b` |

| `docs/tests/test_type_fixture_names_english.py` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/DocumentTypeReferentialIntegrityTest.java` | yes | `407149d9`, `90bc40fd` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementCaseSummaryIntegrationTest.java` | yes | `407149d9`, `85ff9133` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureControllerFkHydrationIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/UpcomingExpirationControllerIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/WorkflowTraceApiH2IntegrationTest.java` | yes | `407149d9`, `b3a1e90e`, `898e02d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentControllerTest.java` | yes | `407149d9`, `c20fdf1e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/SubmittedDocumentPartialUpdateIntegrationTest.java` | yes | `407149d9`, `c20fdf1e`, `cb0c6d45` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonIdentificationTypeIntegrationTest.java` | yes | `407149d9` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RemainingControllersIntegrationTest.java` | yes | `407149d9`, `6311c70f` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TraceabilityMatrixTest.java` | yes | `407149d9` |
| `frontend/src/tests/unit/people-identification-type.test.tsx` | yes | `407149d9`, `6c5fca82` |

| `docs/tests/test_synthetic_notes_english.py` | yes | `c20fdf1e`, `3f9719e3` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetPersonAssociationPgIntegrationTest.java` | yes | `c20fdf1e`, `64690769`, `25120aa1` |
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyControllerIntegrationTest.java` | yes | `c20fdf1e`, `81b6f900`, `b070c19b` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyVerificationServiceTest.java` | yes | `c20fdf1e`, `3f9719e3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RegistrationDraftControllerTest.java` | yes | `c20fdf1e`, `81b6f900` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/DeedManagementJpaController.java` | yes | `c20fdf1e` |
| `frontend/src/hooks/useSubmittedDocuments.test.tsx` | yes | `c20fdf1e` |
| `frontend/src/tests/unit/dashboard-hero.test.tsx` | yes | `c20fdf1e` |
| `testing/e2e/tests/TS-0012-documentation-testimony-workflow.spec.ts` | yes | `c20fdf1e`, `249af851` |
| `testing/e2e/tests/TS-0031-testimony-generation-verification-feature.spec.ts` | yes | `c20fdf1e` |
| `testing/e2e/tests/TS-0082-registration-draft-feature.spec.ts` | yes | `c20fdf1e`, `f2eaecef`, `c28949a7` |
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

| `docs/tests/test_synthetic_labels_english.py` | yes | `c2dbf52a`, `85ff9133` |
| `frontend/src/tests/unit/form-field.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/select.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/status-badge.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/person-picker.test.tsx` | yes | `c2dbf52a` |
| `frontend/src/tests/unit/management-lists-pagination.test.tsx` | yes | `c2dbf52a` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/ManagementQueryServiceTest.java` | yes | `c2dbf52a`, `64690769`, `5537557d`, `0d42e9ea` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/DeedServiceTest.java` | yes | `c2dbf52a`, `c002efd5` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/BudgetServiceTest.java` | yes | `c2dbf52a` |
| `backend-api/src/test/java/com/licensis/notaire/unit/BudgetEntityTest.java` | yes | `c2dbf52a`, `6aa61c6d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ProcedureEntityTest.java` | yes | `c2dbf52a`, `6aa61c6d`, `7bc6c2ff`, `36196fe6` |

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
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyCopyReportIntegrationTest.java` | yes | `90bc40fd`, `f2cd7e08` |
| `backend-api/src/test/java/com/licensis/notaire/integration/NotebookControllerTest.java` | yes | `90bc40fd`, `b070c19b`, `d70228d3` |

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
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentUseCaseIntegrationTest.java` | yes | `64690769`, `7bc6c2ff`, `c28949a7` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PaymentIntegrationTest.java` | yes | `64690769`, `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonServiceIntegrationTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/AuditRecordServiceTest.java` | yes | `64690769`, `eb7692dd` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AuditRecordServiceTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/unit/AuditRecordControllerTest.java` | yes | `64690769` |
| `backend-api/src/test/java/com/licensis/notaire/adapter/out/persistence/payment/PaymentPersistenceAdapterTest.java` | yes | `64690769`, `898e02d9`, `716ba3f8` |
| `backend-api/src/test/java/com/licensis/notaire/integration/CoreBusinessControllersIntegrationTest.java` | yes | `64690769`, `7bc6c2ff`, `f2eaecef`, `6311c70f`, `7c3aeb82` |
| `backend-api/src/test/java/com/licensis/notaire/integration/AuditRecordRepositoryIntegrationTest.java` | yes | `64690769`, `3f9719e3` |

| `docs/tests/test_address_fixtures_english.py` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRepositoryIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyServiceIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyFiscalAppraisalPgIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RequestBodyDtoBindingIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PropertyRequestValidationIntegrationTest.java` | yes | `7bc6c2ff`, `f2eaecef` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonRepositoryIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/unit/PersonEntityTest.java` | yes | `7bc6c2ff`, `a5db7e1a` |
| `backend-api/src/test/java/com/licensis/notaire/unit/PaymentUseCaseTest.java` | yes | `7bc6c2ff`, `c28949a7` |
| `backend-api/src/test/java/com/licensis/notaire/application/usecase/payment/ProcessPaymentServiceTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/RoleRequestValidationIntegrationTest.java` | yes | `7bc6c2ff` |
| `backend-api/src/test/java/com/licensis/notaire/integration/IdentificationTypeValidationIntegrationTest.java` | yes | `7bc6c2ff`, `6f8ec0b4` |

| `docs/tests/test_property_words_english.py` | yes | `f2eaecef` |
| `frontend/src/app/dashboard/properties/page.tsx` | yes | `f2eaecef` |
| `testing/e2e/tests/gherkin-helpers.ts` | yes | `f2eaecef`, `716ba3f8`, `a5db7e1a`, `85c0513a` |
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
| `frontend/src/app/dashboard/managements/page.tsx` | yes | `6311c70f`, `6f8ec0b4` |
| `frontend/src/app/dashboard/documents/page.tsx` | yes | `6311c70f` |
| `frontend/src/app/dashboard/administration/templates/page.tsx` | yes | `6311c70f` |
| `frontend/src/tests/unit/auth-store.test.ts` | yes | `6311c70f`, `716ba3f8` |
| `backend-api/src/test/java/com/licensis/notaire/integration/AuxiliaryProtocolControllerTest.java` | yes | `6311c70f` |

| `docs/tests/test_numbering_names_english.py` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/repository/FolioRepository.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/application/port/out/folio/FolioRepositoryPort.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/folio/FolioPersistenceAdapter.java` | yes | `74638c58` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/notebook/AuxiliaryProtocolService.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/service/unit/AuxiliaryProtocolServiceTest.java` | yes | `74638c58` |
| `backend-api/src/test/java/com/licensis/notaire/unit/TestimonyMovementServiceTest.java` | yes | `74638c58`, `3f9719e3` |
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
| `testing/e2e/tests/TS-0138-workflow-tracker.spec.ts` | yes | `281f44d9`, `36196fe6` |

| `frontend/src/tests/unit/budget-picker.test.tsx` | yes | `85ff9133` |
| `frontend/src/tests/unit/people-pagination.test.tsx` | yes | `85ff9133`, `b413ac7c` |
| `backend-api/src/test/java/com/licensis/notaire/integration/PersonRequestValidationIntegrationTest.java` | yes | `85ff9133` |
| `backend-api/src/test/java/com/licensis/notaire/integration/BudgetSummaryControllerTest.java` | yes | `85ff9133` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementFinancialSummaryControllerTest.java` | yes | `85ff9133` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ReportsUseCaseIntegrationTest.java` | yes | `85ff9133`, `3f9719e3`, `f184335e`, `616c348d` |
| `testing/e2e/tests/TS-0015-people-clients-workflow.spec.ts` | yes | `85ff9133`, `151d6123`, `616c348d`, `6c5fca82`, `c51b97cd`, `249af851` |
| `testing/e2e/tests/TS-0110-people-pagination.spec.ts` | yes | `85ff9133` |

| `docs/tests/test_date_and_note_words_english.py` | yes | `36196fe6` |
| `backend-api/src/main/java/com/licensis/notaire/service/ValidationAdministrator.java` | yes | `36196fe6` |
| `backend-api/src/test/java/com/licensis/notaire/integration/HistoryDeleteIntegrationTest.java` | yes | `36196fe6` |
| `testing/e2e/tests/TS-0017-substitutions-workflow.spec.ts` | yes | `36196fe6` |

| `docs/tests/test_transition_fixture_words_english.py` | yes | `d70228d3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/management/TransitionManagementUseCaseTest.java` | yes | `d70228d3` |
| `backend-api/src/test/java/com/licensis/notaire/application/usecase/history/HistoryMapperTest.java` | yes | `d70228d3` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/SubstitutionJpaController.java` | yes | `d70228d3` |

| `docs/tests/test_verification_identifiers_english.py` | yes | `3f9719e3` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/testimony/TestimonyController.java` | yes | `3f9719e3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/jpa/JpaAdministratorTest.java` | yes | `3f9719e3` |
| `backend-api/src/test/java/com/licensis/notaire/unit/JacocoCoverageConfigConsistencyTest.java` | yes | `3f9719e3` |

| `docs/tests/test_sign_and_delivery_locals_english.py` | yes | `eb7692dd` |
| `frontend/src/app/dashboard/deeds/page.tsx` | yes | `eb7692dd` |

| `docs/tests/test_found_and_report_locals_english.py` | yes | `f184335e` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/ProcedureTypeJpaController.java` | yes | `f184335e` |
| `backend-api/src/main/java/com/licensis/notaire/jpa/PersonProcedureJpaController.java` | yes | `f184335e` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/in/web/folio/FolioController.java` | yes | `f184335e` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ReportControllerValidationIntegrationTest.java` | yes | `f184335e` |
| `frontend/src/app/dashboard/protocol/page.tsx` | yes | `f184335e` |
| `frontend/src/app/dashboard/administration/roles/page.tsx` | yes | `f184335e` |

| `docs/tests/test_credential_and_status_locals_english.py` | yes | `716ba3f8` |
| `testing/e2e/tests/setup/auth.ts` | yes | `716ba3f8` |
| `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` | yes | `716ba3f8` |
| `testing/e2e/tests/TS-0001-login-authentication.spec.ts` | yes | `716ba3f8` |
| `testing/e2e/tests/TS-0003-dashboard-navigation.spec.ts` | yes | `716ba3f8` |
| `testing/e2e/tests/TS-0081-auxiliary-protocol-workflow.spec.ts` | yes | `716ba3f8`, `96c18bd7` |
| `frontend/src/tests/unit/substitutions-reports.test.ts` | yes | `716ba3f8` |

| `docs/tests/test_payment_edit_locals_english.py` | yes | `c28949a7` |
| `frontend/src/app/dashboard/registration-drafts/page.tsx` | yes | `c28949a7` |

| `docs/tests/test_optional_heading_and_dead_constant_english.py` | yes | `25120aa1` |
| `testing/e2e/tests/TS-0131-budget-template.spec.ts` | yes | `25120aa1` |
| `testing/e2e/tests/TS-0013-deeds-signing-workflow.spec.ts` | yes | `25120aa1`, `249af851` |

| `docs/tests/test_report_collection_names_english.py` | yes | `466feae0` |
| `backend-api/api-test/managements/05-current-status.yml` | yes | `466feae0` |
| `backend-api/api-test/report-pdfs/11-documents-due-unknown-404.yml` | yes | `466feae0` |
| `backend-api/api-test/report-pdfs/12-document-debt.yml` | yes | `466feae0` |
| `backend-api/api-test/report-pdfs/13-document-debt-unknown-404.yml` | yes | `466feae0` |
| `docs/openspec/specs/reportes-404/spec.md` | yes | `466feae0` |

| `docs/tests/test_living_spec_dead_names_english.py` | yes | `6b1d3c16`, `336c4868` |
| `docs/openspec/specs/estado-actual-tie-break/spec.md` | yes | `6b1d3c16` |
| `docs/openspec/specs/estado-actual-tie-test/spec.md` | yes | `6b1d3c16` |
| `docs/openspec/specs/bruno-zero-coverage-controllers/spec.md` | yes | `6b1d3c16` |
| `docs/openspec/specs/e2e-test-reliability/spec.md` | yes | `6b1d3c16` |
| `docs/openspec/specs/persona-validation-error-toast/spec.md` | yes | `6b1d3c16`, `336c4868` |
| `docs/openspec/specs/api-error-toasts/spec.md` | yes | `6b1d3c16`, `3cf789a4` |

| `docs/tests/test_registry_path_names_english.py` | yes | `96636752` |
| `backend-api/api-test/auxiliary-protocol/04-start-deed.yml` | yes | `96636752` |

| `docs/tests/test_testing_practice_names_english.py` | yes | `f00eae94` |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | yes | `f00eae94` |
| `docs/200-architecture/202-ADR/ADR-024-repository-topology.md` | yes | `f00eae94` |
| `docs/300-development/304-ai-sdlc-cloud/CI-MERGE-GATE.md` | yes | `f00eae94` |
| `backend-api/api-test/properties/08-create-missing-cadastral.yml` | yes | `f00eae94` |

| `docs/tests/test_synthetic_status_names_english.py` | yes | `0d42e9ea` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ManagementCompleteCaseReadIntegrationTest.java` | yes | `0d42e9ea` |

| `docs/tests/test_started_and_substitute_names_english.py` | yes | `3d603a44` |
| `backend-api/src/test/java/com/licensis/notaire/unit/WorkflowEntityTest.java` | yes | `3d603a44`, `616c348d` |
| `backend-api/src/test/java/com/licensis/notaire/unit/WorkflowNodeControllerTest.java` | yes | `3d603a44` |
| `testing/e2e/tests/setup/api-helpers.ts` | yes | `3d603a44`, `7c3aeb82` |
| `testing/e2e/tests/TS-0092-management-substitution-redirect.spec.ts` | yes | `3d603a44`, `151d6123`, `616c348d` |
| `frontend/src/tests/unit/identification-types-admin.test.tsx` | yes | `3d603a44` |

| `docs/tests/test_substituted_parameter_names_english.py` | yes | `0eedb40d` |
| `backend-api/src/main/java/com/licensis/notaire/repository/SubstitutionRepository.java` | yes | `0eedb40d` |
| `backend-api/src/main/java/com/licensis/notaire/application/port/out/substitution/SubstitutionRepositoryPort.java` | yes | `0eedb40d` |
| `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/substitution/SubstitutionPersistenceAdapter.java` | yes | `0eedb40d` |
| `backend-api/src/main/java/com/licensis/notaire/application/usecase/substitution/SubstitutionService.java` | yes | `0eedb40d` |

| `docs/tests/test_synthetic_person_labels_english.py` | yes | `151d6123` |
| `testing/e2e/tests/TS-0126-management-full-edit.spec.ts` | yes | `151d6123`, `a2cf628c` |
| `testing/e2e/tests/TS-0014-payments-workflow.spec.ts` | yes | `151d6123` |

| `docs/tests/test_case_summary_and_status_names_english.py` | yes | `6f8ec0b4` |

| `docs/tests/test_workflow_fixture_phrases_english.py` | yes | `616c348d`, `482fea65` |

| `docs/tests/test_synthetic_person_and_concept_labels_english.py` | yes | `6c5fca82` |
| `testing/e2e/tests/TS-0105-delete-in-use-reason.spec.ts` | yes | `6c5fca82` |
| `testing/e2e/tests/TS-0109-mobile-lists.spec.ts` | yes | `6c5fca82` |
| `testing/e2e/tests/TS-0020-reports-admin-workflow.spec.ts` | yes | `6c5fca82`, `a2cf628c` |

| `docs/tests/test_synthetic_catalog_phrases_english.py` | yes | `7c3aeb82` |
| `backend-api/src/test/java/com/licensis/notaire/unit/SubstitutionEntityTest.java` | yes | `7c3aeb82` |
| `testing/e2e/tests/TS-0116-managements-columns.spec.ts` | yes | `7c3aeb82` |
| `testing/e2e/tests/TS-0033-external-entity-documents-feature.spec.ts` | yes | `7c3aeb82` |
| `testing/e2e/tests/TS-0134-document-type-expiration-config.spec.ts` | yes | `7c3aeb82` |

| `docs/tests/test_load_check_and_dialog_names_english.py` | yes | `f55164be` |
| `infra/performance/k6/load-test.js` | yes | `f55164be` |
| `testing/e2e/tests/TS-0018-document-reentry-workflow.spec.ts` | yes | `f55164be` |
| `testing/e2e/tests/TS-0091-items-discounts-surcharges.spec.ts` | yes | `f55164be` |

| `docs/tests/test_document_note_phrases_english.py` | yes | `cb0c6d45` |
| `backend-api/src/test/java/com/licensis/notaire/unit/UpcomingExpirationServiceTest.java` | yes | `cb0c6d45` |

| `docs/tests/test_cost_fixture_phrases_english.py` | yes | `f2cd7e08` |
| `backend-api/src/test/java/com/licensis/notaire/integration/DocumentCostTemplateControllerTest.java` | yes | `f2cd7e08` |

| `docs/tests/test_audit_column_keys_english.py` | yes | `e7b9c574` |
| `frontend/src/app/dashboard/audit/page.tsx` | yes | `e7b9c574` |
| `backend-api/src/test/java/com/licensis/notaire/unit/ProcedureTemplateControllerTest.java` | yes | `e7b9c574` |

| `docs/tests/test_hook_fixture_phrases_english.py` | yes | `b413ac7c` |
| `frontend/src/tests/unit/hooks.test.ts` | yes | `b413ac7c` |
| `frontend/src/tests/unit/person-save-error.test.ts` | yes | `b413ac7c` |

| `docs/tests/test_h2_seed_phrases_english.py` | yes | `e3c030ca` |
| `backend-api/src/test/resources/data.sql` | yes | `e3c030ca` |

| `docs/tests/test_cleanup_comment_and_role_description_english.py` | yes | `7ab8606e` |
| `backend-api/src/test/resources/cleanup-test-data.sql` | yes | `7ab8606e` |
| `backend-api/src/test/java/com/licensis/notaire/unit/RoleControllerTest.java` | yes | `7ab8606e` |

| `docs/tests/test_adr_payment_titles_english.py` | yes | `8e57d22f` |
| `docs/200-architecture/202-ADR/ADR-021-hexagonal-architecture-pilot.md` | yes | `8e57d22f` |

| `docs/tests/test_transaction_guide_sample_english.py` | yes | `87c8535c` |
| `docs/300-development/302-code-standards/SPRING-TRANSACTION-GUIDE.md` | yes | `87c8535c` |

| `docs/tests/test_proposal_usecase_titles_english.py` | yes | `5f17c820` |
| `docs/openspec/changes/fix-596-people-pagination/proposal.md` | yes | `5f17c820` |
| `docs/openspec/changes/fix-596-people-pagination/traceability.md` | yes | `5f17c820` |
| `docs/openspec/changes/fix-655-testimony-requires-deed/proposal.md` | yes | `5f17c820` |
| `docs/openspec/changes/fix-655-testimony-requires-deed/traceability.md` | yes | `5f17c820` |
| `docs/openspec/changes/fix-1315-delete-returns-204/proposal.md` | yes | `5f17c820` |
| `docs/openspec/changes/fix-1315-delete-returns-204/traceability.md` | yes | `5f17c820` |

| `docs/tests/test_constraint_error_usecase_title.py` | yes | `f48a7098` |
| `docs/openspec/changes/fix-579-constraint-errors-400-testimony-person/proposal.md` | yes | `f48a7098` |
| `docs/openspec/changes/fix-579-constraint-errors-400-testimony-person/traceability.md` | yes | `f48a7098` |

| `docs/tests/test_dictionary_folio_type_names.py` | yes | `fd9a5751` |
| `docs/200-architecture/205-data-model/Diccionario de Datos.md` | yes | `fd9a5751` |
| `docs/200-architecture/205-data-model/ERD/Modelo Relacional Escribania - Entidades.csv` | yes | `fd9a5751` |

| `docs/tests/test_user_manual_filename.py` | yes | `a40bfe08` |
| `docs/tools/fetch-user-manual.sh` | yes | `a40bfe08` |
| `docs/200-architecture/202-ADR/ADR-022-git-history-rewrite-and-large-binaries.md` | yes | `a40bfe08` |
| `docs/openspec/specs/repo-hygiene/spec.md` | yes | `a40bfe08` |
| `workspace/tests/test_repo_hygiene.py` | yes | `a40bfe08` |

| `docs/tests/test_namedquery_persona_prefix.py` | yes | `1150e6ac` |
| `docs/openspec/specs/jpa-namedquery-english-names/spec.md` | yes | `1150e6ac` |

| `docs/tests/test_document_link_specs_english.py` | yes | `1ac61b51` |
| `docs/openspec/specs/document-procedure-link/spec.md` | yes | `1ac61b51` |
| `docs/openspec/specs/required-docs-on-confirm/spec.md` | yes | `1ac61b51` |

| `docs/tests/test_e2e_header_comments_english.py` | yes | `77b0f724` |
| `testing/e2e/tests/TS-0141-required-docs-on-confirm.spec.ts` | yes | `77b0f724` |
| `testing/e2e/tests/TS-0094-admin-route-guard.spec.ts` | yes | `77b0f724` |
| `testing/e2e/tests/TS-0024-admin-module-smoke.spec.ts` | yes | `77b0f724`, `85802a05` |
| `testing/e2e/tests/TS-0070-supervised-tour-tutorial.spec.ts` | yes | `77b0f724` |
| `testing/e2e/tests/TS-0121-grouped-sidebar.spec.ts` | yes | `77b0f724` |
| `testing/e2e/tests/TS-0139-identification-type-catalog.spec.ts` | yes | `77b0f724` |

| `docs/tests/test_open_change_specs_english.py` | yes | `12394f83` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/specs/management-case-summary/spec.md` | yes | `12394f83` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/design.md` | yes | `12394f83`, `4b28e38f` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/traceability.md` | yes | `12394f83`, `4b28e38f` |
| `docs/openspec/changes/fix-655-submitted-document-required-fields/specs/submitted-documents/spec.md` | yes | `12394f83` |

| `docs/tests/test_property_designation_spec_english.py` | yes | `06939ea3` |
| `docs/openspec/changes/fix-655-property-cadastral-designation-required/specs/properties/spec.md` | yes | `06939ea3` |

| `docs/tests/test_change_contract_english.py` | yes | `bd4422ec` |
| `docs/openspec/changes/gestion-archive-deuda-check/spec.md` | yes | `bd4422ec` |
| `docs/openspec/changes/fix-655-put-rejects-incomplete-bodies/specs/api-error-contract/spec.md` | yes | `bd4422ec` |
| `docs/openspec/changes/fix-596-people-pagination/specs/people-management/spec.md` | yes | `bd4422ec` |

| `docs/tests/test_change_design_english.py` | yes | `51f80fff`, `4b28e38f` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/design.md` | yes | `51f80fff`, `4b28e38f` |
| `docs/openspec/changes/feat-1362-procedure-template-admin/specs/ui-procedure-template-admin/spec.md` | yes | `51f80fff` |
| `docs/openspec/changes/feat-1362-procedure-template-admin/design.md` | yes | `51f80fff` |
| `docs/openspec/changes/fix-655-property-cadastral-designation-required/design.md` | yes | `51f80fff` |
| `docs/openspec/changes/fix-655-submitted-document-required-fields/design.md` | yes | `51f80fff` |

| `docs/tests/test_change_tasks_english.py` | yes | `f9d2e811` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/tasks.md` | yes | `f9d2e811` |
| `docs/openspec/changes/docs-921-business-assessment/tasks.md` | yes | `f9d2e811` |

| `docs/tests/test_change_proposals_english.py` | yes | `fe1bbeda` |
| `docs/openspec/changes/feat-774-gestion-resumen-caso/proposal.md` | yes | `fe1bbeda` |
| `docs/openspec/changes/fix-596-pageable-parameter-object/proposal.md` | yes | `fe1bbeda` |
| `docs/openspec/changes/fix-655-put-rejects-incomplete-bodies/proposal.md` | yes | `fe1bbeda` |

| `docs/tests/test_remaining_proposals_english.py` | yes | `f39dd455` |
| `docs/openspec/changes/gestion-archive-deuda-check/proposal.md` | yes | `f39dd455` |
| `docs/openspec/changes/fix-655-submitted-document-required-fields/proposal.md` | yes | `f39dd455` |
| `docs/openspec/changes/fix-655-property-cadastral-designation-required/proposal.md` | yes | `f39dd455` |

| `docs/tests/test_lifecycle_proposals_english.py` | yes | `e18febca` |
| `docs/openspec/changes/docs-921-business-assessment/proposal.md` | yes | `e18febca` |
| `docs/openspec/changes/docs-921-domain-lifecycle/proposal.md` | yes | `e18febca` |

| `docs/tests/test_retired_test_filenames_english.py` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-655-required-ids-copy-draft-reentry/proposal.md` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-655-required-ids-copy-draft-reentry/design.md` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-655-required-ids-copy-draft-reentry/tasks.md` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-655-required-ids-copy-draft-reentry/traceability.md` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-596-people-pagination/design.md` | yes | `255c4dc2` |
| `docs/openspec/changes/fix-596-people-pagination/traceability.md` | yes | `255c4dc2` |

| `docs/tests/test_checklist_fixture_notes_english.py` | yes | `85802a05` |
| `testing/e2e/tests/TS-0142-procedure-required-documents.spec.ts` | yes | `85802a05` |
| `frontend/src/tests/unit/procedure-template-admin.test.tsx` | yes | `85802a05`, `6cfcdb06` |
| `backend-api/src/test/java/com/licensis/notaire/integration/ProcedureTemplateAdminIntegrationTest.java` | yes | `85802a05` |
| `backend-api/api-test/procedure-templates/06-update.yml` | yes | `85802a05` |

| `docs/tests/test_catalog_playwright_filenames_english.py` | yes | `2c578231` |
| `docs/openspec/changes/fix-1315-budget-template-delete-204/design.md` | yes | `2c578231` |
| `docs/openspec/changes/fix-579-constraint-errors-400-catalogs/design.md` | yes | `2c578231` |
| `docs/openspec/changes/fix-655-put-rejects-incomplete-bodies/tasks.md` | yes | `2c578231` |
| `docs/tests/test_property_title_fixture_english.py` | yes | `6cfcdb06` |
| `docs/tests/test_usecase_module_pointers_english.py` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-gestion.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/pagos.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/presupuestos.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-plantilla-presupuestos.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-escrituras.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-inscripciones.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-testimonios.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/gestiones-documentacion.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/protocolos.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-escribanos.puml` | yes | `f00b0504` |
| `docs/200-architecture/204-diagrams/Casos de Uso/administracion-usuarios.puml` | yes | `f00b0504` |
| `docs/tests/test_required_documents_comment_english.py` | yes | `eaee1b43` |
| `frontend/src/app/dashboard/administration/procedures/RequiredDocumentsDialog.tsx` | yes | `eaee1b43` |
| `docs/tests/test_people_client_comments_english.py` | yes | `c51b97cd` |
| `docs/tests/test_field_label_comments_english.py` | yes | `a2cf628c` |
| `docs/tests/test_adr_registration_draft_english.py` | yes | `47401bd6`, `b1a571ac` |
| `docs/200-architecture/202-ADR/ADR-023-rest-resource-naming.md` | yes | `47401bd6`, `b1a571ac` |
| `docs/tests/test_adr_retired_paths_english.py` | yes | `b1a571ac` |
| `docs/tests/test_assessment_baseline_citations_english.py` | yes | `b7f9bfa1` |
| `docs/github/PRODUCTION-READINESS-AUDIT-2026-09.md` | yes | `cb2f02b7` |
| `docs/tests/test_audit_index_citations_english.py` | yes | `cb2f02b7` |
| `docs/tests/test_sad_api_design_english.py` | yes | `1a7d3754` |
| `frontend/src/hooks/useTestimonyMovements.ts` | yes | `59d7eda8` |
| `docs/tests/test_withdraw_comment_english.py` | yes | `59d7eda8` |
| `docs/openspec/specs/english-source-surface/spec.md` | yes | `8406b707` |
| `docs/tests/test_english_source_surface_purpose_english.py` | yes | `8406b707` |
| `docs/openspec/changes/docs-921-engineering-sequences/design.md` | yes | `505daba5`, `f32ad809`, `82e02de1`, `8f108095`, `bdd1161f`, `6586ddbe`, `90d88f1c`, `0035db46`, `ed47ea59`, `c2aa66a5`, `616dbacf`, `0c344ddc`, `0593a379`, `5738a72d`, `820b8e48`, `4f58ca0a`, `f71c9db6`, `d4f3407c`, `6a9faf26`, `fa97016e`, `e20a2730`, `67e7f119`, `1c03856a`, `e878c407`, `7583a11c`, `f4009cf6`, `9afae68e`, `133a7d5a`, `c0576df8`, `4dc9f29c`, `67254e51`, `36a11c23`, `000b4398`, `2790233d`, `38c04fe1`, `a034a7a8`, `19c52114`, `706a5da5`, `94dbb347`, `ed767b4c`, `881301fa`, `17ce57b6`, `63ae35f0`, `b332872a`, `4ec5de7a` |
| `docs/tests/test_document_reentry_path_citation_english.py` | yes | `505daba5`, `f32ad809`, `82e02de1`, `8f108095`, `bdd1161f`, `6586ddbe`, `90d88f1c`, `0035db46`, `ed47ea59` |
| `security/dast_baseline.py` | yes | `a34e9111` |
| `security/cases/catalog.yml` | yes | `a34e9111` |
| `security/tests/test_security_cases.py` | yes | `a34e9111` |
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | yes | `a34e9111` |
| `docs/tests/test_k6_script_path_english.py` | yes | `4c20f254` |
| `docs/openspec/specs/upcoming-expirations/spec.md` | yes | `c2aa66a5`, `5738a72d` |
| `docs/tests/test_upcoming_expirations_query_english.py` | yes | `c2aa66a5`, `5738a72d` |
| `docs/tests/test_catalog_items_path_english.py` | yes | `616dbacf` |
| `docs/tests/test_discounts_surcharges_path_english.py` | yes | `0c344ddc` |
| `docs/tests/test_payment_consult_paths_english.py` | yes | `0593a379` |
| `docs/tests/test_budget_screen_english.py` | yes | `820b8e48` |
| `docs/tests/test_external_entity_screen_english.py` | yes | `4f58ca0a` |
| `docs/tests/test_document_reentry_screen_english.py` | yes | `f71c9db6` |
| `docs/tests/test_substitutions_screen_english.py` | yes | `d4f3407c` |
| `docs/tests/test_reports_screen_english.py` | yes | `6a9faf26` |
| `docs/tests/test_audit_screen_english.py` | yes | `fa97016e` |
| `docs/tests/test_flat_management_put_english.py` | yes | `e20a2730` |
| `docs/tests/test_admin_screen_english.py` | yes | `67e7f119` |
| `docs/tests/test_short_management_put_english.py` | yes | `1c03856a` |
| `docs/tests/test_short_create_and_status_english.py` | yes | `e878c407` |
| `docs/tests/test_retired_case_create_english.py` | yes | `7583a11c` |
| `docs/tests/test_retired_document_services_english.py` | yes | `f4009cf6` |
| `docs/tests/test_retired_procedure_template_english.py` | yes | `9afae68e` |
| `docs/tests/test_retired_submitted_document_english.py` | yes | `133a7d5a` |
| `docs/tests/test_retired_procedure_type_english.py` | yes | `c0576df8` |
| `docs/tests/test_retired_management_status_english.py` | yes | `4dc9f29c` |
| `docs/tests/test_retired_budget_template_english.py` | yes | `67254e51` |
| `docs/tests/test_retired_document_cost_template_english.py` | yes | `36a11c23` |
| `docs/tests/test_retired_amount_to_pay_english.py` | yes | `000b4398` |
| `docs/tests/test_retired_budget_foreign_key_english.py` | yes | `2790233d` |
| `docs/tests/test_retired_deed_link_field_english.py` | yes | `38c04fe1` |
| `docs/tests/test_retired_substitution_dates_english.py` | yes | `a034a7a8` |
| `docs/tests/test_retired_current_status_field_english.py` | yes | `19c52114` |
| `docs/tests/test_retired_reentered_flag_english.py` | yes | `706a5da5` |
| `docs/tests/test_retired_due_date_formula_english.py` | yes | `94dbb347` |
| `docs/tests/test_retired_status_log_service_english.py` | yes | `ed767b4c` |
| `docs/tests/test_retired_submitted_document_id_english.py` | yes | `881301fa` |
| `docs/tests/test_retired_procedure_type_query_english.py` | yes | `17ce57b6` |
| `docs/tests/test_retired_budget_id_english.py` | yes | `63ae35f0` |
| `docs/tests/test_retired_item_type_reason_english.py` | yes | `b332872a` |
| `docs/tests/test_retired_validation_detail_english.py` | yes | `4ec5de7a` |
| `backend-api/src/test/java/com/licensis/notaire/unit/EnglishSurfaceRatchetTest.java` | yes | `4658f973`, `4abcc8e6` |
| `backend-api/src/main/java/com/licensis/notaire/business/Copy.java` | yes | `4658f973` |
| `backend-api/src/main/java/com/licensis/notaire/business/Testimony.java` | yes | `4658f973` |
| `backend-api/src/main/java/com/licensis/notaire/business/IdentificationType.java` | yes | `4658f973` |
| `backend-api/src/main/java/com/licensis/notaire/dto/DtoManagementStatus.java` | yes | `4abcc8e6` |
| `docs/tests/test_upcoming_expiration_purpose_english.py` | yes | `c591f415` |
| `docs/tests/test_error_toast_exemplars_english.py` | yes | `3cf789a4` |
| `frontend/src/tests/unit/testimony-admin.test.tsx` | yes | `58bd9025` |
| `backend-api/src/test/java/com/licensis/notaire/integration/TestimonyDeleteKeepsHistoryIntegrationTest.java` | yes | `58bd9025` |
| `backend-api/src/test/java/com/licensis/notaire/application/usecase/payment/EditPaymentServiceTest.java` | yes | `58bd9025` |
| `testing/e2e/tests/TS-0143-testimony-edit-delete-movements.spec.ts` | yes | `58bd9025` |
| `docs/tests/test_corrected_notes_english.py` | yes | `58bd9025` |
| `docs/tests/test_icon_button_users_screen_english.py` | yes | `8fd5e422` |
| `docs/tests/test_auxiliary_deed_body_english.py` | yes | `96c18bd7` |
| `frontend/src/app/dashboard/testimonies/TestimonyMovementsDialog.tsx` | yes | `7099f68d` |
| `docs/tests/test_testimony_movements_comment_english.py` | yes | `7099f68d` |
| `docs/openspec/changes/fix-655-submitted-document-required-fields/traceability.md` | yes | `4b28e38f` |
| `docs/openspec/changes/fix-655-property-cadastral-designation-required/traceability.md` | yes | `4b28e38f` |
| `docs/tests/test_open_playwright_filenames_english.py` | yes | `4b28e38f` |
| `frontend/src/tests/unit/document-type-enabled-returned.test.ts` | yes | `5c6990e2` |
| `docs/tests/test_document_type_unit_filename_english.py` | yes | `5c6990e2` |
| `backend-api/api-test/rbac/04-employee-create-notary.yml` | yes | `b6f14c80` |
| `docs/tests/test_rbac_notary_filename_english.py` | yes | `b6f14c80` |
| `testing/e2e/tests/TS-0010-budget-workflow.spec.ts` | yes | `249af851` |
| `testing/e2e/tests/TS-0011-managements-crud-workflow.spec.ts` | yes | `249af851` |
| `docs/tests/test_e2e_sequence_pointers_english.py` | yes | `249af851` |
| `docs/openspec/specs/runtime-backend-url-proxy/spec.md` | yes | `ff39fd08` |
| `docs/tests/test_runtime_proxy_edge_english.py` | yes | `ff39fd08` |
| `frontend/src/tests/unit/useReports-contract.test.ts` | yes | `482fea65` |
| `docs/tests/test_workflow_tracker_e2e_filename_english.py` | yes | `5e43f5aa` |
| `docs/tests/test_budget_procedure_survey_english.py` | yes | `cb0c93bf` |
| `docs/tests/test_synthetic_occupation_english.py` | yes | `a5db7e1a` |
| `docs/tests/test_success_toast_default_english.py` | yes | `85c0513a` |
| `docs/openspec/changes/fix-655-testimony-requires-deed/design.md` | yes | `7de5ce40` |
| `docs/tests/test_testimony_seed_helper_english.py` | yes | `7de5ce40` |

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
