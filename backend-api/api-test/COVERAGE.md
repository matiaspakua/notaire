# API Test Coverage

Bruno YAML suite (OpenCollection) — run with Bruno CLI **≥ 4.x** (OpenCollection /
`opencollection.yml` support; CI uses unpinned `npx @usebruno/cli`, which
resolves to latest):

```bash
cd backend-api/api-test
npx @usebruno/cli run . -r --env Development
```

(backend must be up at `localhost:8080`). CLI 2.x only understands `bruno.json`
and will print “You can run only at the root of a collection” for this suite —
do not add a parent `bruno.json`.

**Current status:** 297 requests / 508 tests passing, twice in a row against the
same database with no leaked rows (verified 2026-10-03, issue #953). `00-auth`
sorts first (login/rate-limit fixtures other folders depend on).

## Backend defects found and fixed via this suite

| # | Endpoint | Defect | Fix |
|---|----------|--------|-----|
| 1 | `POST /document-types` | NPE unboxing null `Boolean expires` | `DtoDocumentType.isExpires()` null-safe |
| 2 | `POST /identification-types` | `null value in column "characters"` (entity didn't map the NOT-NULL column) | added `characters` field to `IdentificationType` |
| 3 | `PUT /procedure-types/{id}` | NPE on null `associatesProperties` / `version` | `ProcedureType.setAttributes` null-safe (booleans + preserve version) |
| 4 | `PUT /document-types/{id}` | NPE on null `enabled` / `version` | `DocumentType.setAttributes` null-safe |
| 5 | `GET /users/person/{id}` | `NonUniqueResult` 500 when a person has more than one user | `findFirstByFkIdPersonIdPerson` |
| 6 | `PUT /users/{id}` | omitting `password` → NOT-NULL 500 | preserve stored password when omitted |
| 7 | `POST/PUT /items` | `fkIdBudget` silently dropped (`@JsonIgnore` blocked the field on write, not just read) | `@JsonProperty(access = WRITE_ONLY)` |
| 8 | `DELETE /history/{id}`, `/items/{id}`, `/payments/{id}`, `/procedures/{id}` | delete silently no-op'd for rows loaded fresh from the DB (Spring Data's default `isNew()` misreads a primitive `@Version` of 0 as "new") | implement `Persistable<Integer>` with an explicit `isNew()` |
| 9 | `DELETE /history/{id}` | delete silently cancelled by Hibernate's cascade on the stale `ManagementStatus.historyList` collection | unlink the entity from that collection before `repository.delete()` |
| 10 | `DELETE /{resource}/{id}` for 30 more entities (`Role`, `DocumentType`, `FolioType`, `ProcedureType`, `IdentificationType`, `ManagementStatus`, `WorkflowDefinition`/`Node`/`Transition`, `Person`, `User`, `Deed`, `DeedManagement`, `Budget`, `Testimony`, `Notebook`, `Folio`, `Property`, `RegistrationDraft`, `TestimonyMovement`, `SubmittedDocument`, `AuditRecord`, `Substitution`, `Concept`, `Copy`, plus 5 `@EmbeddedId` join entities) | same silent-delete-via-`isNew()` bug as defect #8, present across every remaining surrogate-key and composite-key entity | implement `Persistable<Integer>` (surrogate keys) or `Persistable<XxxPK>` with `@Transient boolean isNew` + `@PostLoad`/`@PrePersist` (composite keys) on all 30 entities (#957) |
| 11 | `DELETE /budget-templates/procedure-types/{id}/concept/{id}` | returned 200 but the row survived: the `getReference` proxy was not removed from the parents' cascade-ALL lists, so flush re-persisted it | load with `em.find` before unlinking and removing (#1036) |
| 12 | `GET /testimonies/{id}`, `GET /testimony-movements` | INNER JOIN on required `Testimony.fkIdDeed` made findById/list fail when deed was null | `@ManyToOne(optional = true)` on `Testimony.fkIdDeed` (#953) |

(Earlier, the same campaign fixed `PUT /concepts`, `GET /folios`,
`DELETE /people` — merged in PR #416.)

The recurring root cause is **unboxing a nullable wrapper (`Boolean`/`Integer`)
from a DTO into a primitive entity field** on the update path; create defaulted
the value but update did not. Hardened in `setAttributes`.

## Covered resources (full lifecycle)

`[setup] → create → list → get-by-id → [filters] → update → verify → delete → verify-404 → [teardown]`

| Folder | Endpoint | CRUD | Filters / extras | Use Cases |
|--------|----------|------|------------------|-----------|
| 00-auth | `/users/login` | n/a | login, invalid, rate-limit lockout | CU78 |
| audit-records | `/audit-log` | read-only | list; user + date-range filter, bad range → 400 (#1545); POST/PUT/DELETE → 405 (#1060) | CU73, CU23, CU78 |
| auxiliary-protocol | `/auxiliary-protocol` | actions | available folios; start a deed | CU81 |
| budget-templates | `/budget-templates` | ✅ | `procedure-types/{id}`; own procedure type + concept fixtures | CU26, CU29, CU39, CU55, CU49, CU37, CU57 |
| budgets | `/budgets` | ✅ | `person/{id}`, `search?status=` | CU01, CU60, CU45 |
| concepts | `/concepts` | ✅ | — | CU29, CU66, CU34, CU37 |
| copies | `/copies` | ✅ | requires `datePrinting` | CU70 |
| deeds | `/deeds` | search + list | `search?number=`; paged list `?status=` (any letter case, unknown → 400, #1340) | CU06, CU07, CU62, CU82 |
| document-cost-templates | `/document-cost-templates` | POST + GET | by `procedure-types/{id}` (no DELETE API) | CU39 |
| document-types | `/document-types` | ✅ | — | CU27, CU65, CU32, CU38 |
| folio-types | `/folio-types` | ✅ | — | CU36, CU68, CU40, CU58 |
| folios | `/folios` | ✅ (no PUT) | own notary fixture | CU48, CU28, CU63, CU33 |
| history | `/history` | ✅ | `management/{id}`; own notary + management fixtures | CU48, CU02, CU11, CU13, CU53 |
| identification-types | `/identification-types` | ✅ | 409 duplicate name, 409 delete in use (#1361), 400 missing or non-numeric `characters` (#1526) | CU17 |
| items | `/items` | ✅ | `budget/{id}`, surcharge without reason rejected | CU01, CU71, CU45 |
| management-statuses | `/management-statuses` | ✅ | — | CU30, CU67, CU35 |
| managements | `/managements` | ✅ | by number; current-status (History latest or entity-status fallback when empty — #806); PUT update keeps same `managementStatusId` (status changes via `/transition` — #804); `/case-summary` lists the deeds, testimonies, and copies (#774) | CU02, CU07, CU13, CU14, CU53, CU83 |
| notebooks | `/notebooks` | POST/GET | 10-folio fixture; cover PDF | CU80 |
| payments | `/payments` | ✅ | `budget/{id}`, `balance`, `status`, `date` inverted range 400, over-limit 409, non-positive update 400, receipt PDF | CU01, CU15, CU47, CU45 |
| people | `/people` | ✅ | `search?lastName=`, 409 duplicate, 400 unknown identification type (#1361) | CU17, CU18, CU61, CU46, CU54, CU41 |
| procedure-folders | `/procedure-folders` | GET + wait | via complete-case fixture; `GET`/`PUT /managements/{id}/complete-case` full edit read-back (#1360) | CU85, CU53 |
| procedure-templates | `/procedure-templates` | ✅ | list, `procedure-types/{id}`, 409 duplicate pair, PUT notes, DELETE then 404 (#1362) | CU79, CU03, CU55 |
| procedure-types | `/procedure-types` | ✅ | — | CU26, CU64, CU31, CU57 |
| procedures | `/procedures` | ✅ | — | CU02, CU53 |
| properties | `/properties` | ✅ | — | CU69 |
| registration-drafts | `/registration-drafts` | actions | 404 paths for missing ids (happy Firmada+property chain deferred) | CU82 |
| report-pdfs | `/reports` | PDF GETs | index-book, monthly and tax sworn statements, procedure-document-list, budget (+ properties), management-history, document-debt; 404 for missing aggregates including documents-due (#567) | CU01, CU03, CU09, CU13, CU24, CU25, CU42, CU50 |
| roles | `/roles` | ✅ | — | CU76 |
| submitted-documents | `/submitted-documents` | ✅ | — | CU04, CU72 |
| substitutions | `/substitutions` | ✅ | own substitute + replaced person fixtures | CU48, CU22, CU59 |
| testimonies | `/testimonies` | ✅ | verify action; DELETE with a registry movement answers 409 and keeps it (#1364) | CU07, CU08, CU44 |
| testimony-movements | `/testimony-movements` | ✅ | — | CU12 |
| users | `/users` | ✅ | login (+/- credentials), case-insensitive login, JWT structure, `person/{id}` | CU20, CU78, CU21 |
| workflow-definitions | `/workflow-definition` | ✅ | — | CU83 |
| workflow-nodes | `/workflow-node` | ✅ | by-workflow | CU83 |
| workflow-transitions | `/workflow-transition` | ✅ | by-workflow | CU83 |
| workflow-validation | `/workflow-definition/{id}/validate` | POST | structured `{valid, errors}` | CU83 |

## TODO — resources not yet covered

None for the sixteen controllers tracked in #953. Optional follow-ups:
full Firmada+property happy path for `registration-drafts`, and remaining
`ReportController` PDF variants that need heavier fixtures.
