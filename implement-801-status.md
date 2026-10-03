# Issue #801 implementation status

| Field | Value |
|-------|-------|
| Issue | #801 |
| Use Case | CU72 – Gestión de Documentos Presentados |
| Branch | `cursor/fix-801-submitted-document-mapping-69d3` |
| PR | https://github.com/matiaspakua/notaire/pull/1195 (draft) |
| Head SHA | `9f43c37c09b1a1109a3ed8024cc0039e31257f26` (+ follow-up openspec status commit if pushed) |
| OpenSpec | `openspec/changes/fix-801-submitted-document-mapping/` |
| Store copy | Agent store not mounted (`/cursor/stores/self` missing) |

## What shipped

- `SubmittedDocument.documentType` `@ManyToOne` + `@JoinColumn(fk_id_document_type)`
- `DocumentType.submittedDocumentCollection` `mappedBy = "documentType"` (no Cascade ALL)
- Compatibility ID accessors retained
- `getDto()` null-guards optional procedure + nullable Boolean flags; dead DtoDocumentType removed
- Repository: `existsByDocumentTypeIdDocumentType`
- CU72 + CHANGELOG updated; no Flyway

## Test results

| Command | Result |
|---------|--------|
| `bash scripts/validate-sdlc-plan.sh fix-801-submitted-document-mapping` | pass |
| `mvn test -pl backend-api -Dtest=SubmittedDocumentEntityTest` | 23 tests, 0 failures |
| Related IT/unit (`SimpleControllersTest`, `SubmittedDocumentControllerTest`, `DocumentTypeReferentialIntegrityTest`) | pass |
| `mvn verify -pl backend-api -am` | **BUILD SUCCESS** — **1955** tests, 0 failures; JaCoCo instr ~**85.0%** / branch ~**73.6%** |
| Log | `/opt/cursor/artifacts/mvn-verify-801.log` |

## Commits

1. `18f43840` docs(openspec): Gate 1 plan
2. `5d9cb61b` test(jpa): entity scenarios
3. `be3fc61d` fix(jpa): mapping + getDto null-guard
4. `9f43c37c` docs(cu72): CU72 + CHANGELOG

## Coordinator next

- Do **not** merge from this agent
- Wait for `bash scripts/check-heavy-ci.sh 1195` exit 0, then merge
