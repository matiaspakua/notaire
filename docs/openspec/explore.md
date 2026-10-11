# Notaire — Business finding index

> Single finding↔Issue traceability index. Project rule: every business
> finding must have an associated GitHub Issue, with no exceptions
> (`CONSTITUTION.md` §4). **The detailed description, acceptance criteria,
> and technical notes of each finding live in its GitHub Issue** — this
> file is only the summary index; do not duplicate that content here.
>
> Method: cross-check of the 78+ use cases (`docs/100-business/102-use-cases/`)
> against the 95 functional requirements of the SRS (`requerimientos.csv`),
> the state diagram of a management's lifecycle
> (`docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml`),
> and an end-to-end check that a management can be started and completed
> with the modules implemented at the time. Seven passes accumulated from
> 2026-08-11 through 2026-09-07 (pass history at the end of the file).
>
> **OpenSpec status** column: the real phase of the change under
> `docs/openspec/changes/` (`openspec status --change <id> --json`), not
> the Issue. Values used: `No change` (finding without a proposal),
> `Proposed` (proposal/traceability/specs/design/tasks = `done`, zero
> implementation tasks checked), `Applied, not archived` (code merged but
> the directory is still under `docs/openspec/changes/` instead of
> `changes/archive/`), `Archived` (change moved to `changes/archive/`),
> `Orphan/invalid` (change without a `.openspec.yaml` or a schema the
> current CLI accepts).

## Open

| Title | Short description | Issue | Severity | OpenSpec status |
|---|---|---|---|---|
| The notarial protocol cannot be assembled from the system | No action links deed↔folio or copy↔testimony | [#838](https://github.com/matiaspakua/notaire/issues/838) | Medium | Proposed (`folio-vinculacion-escritura`, 0/70 tasks) |
| The workflow tracker does not model the testimony re-entry loop | The workflow engine represents a single "current status"; it cannot capture inscription retries | [#841](https://github.com/matiaspakua/notaire/issues/841) | Medium | Proposed (`gestion-workflow-reingreso-testimonio`, 0/69 tasks) |
| CU84/CSV has no RF↔GitHub ID traceability | `CU84 - Login.md` does not follow the standard template; 2 rows of the requirements CSV have no valid `GitHub_ID` | [#956](https://github.com/matiaspakua/notaire/issues/956) | Low | No change (documentation finding) |
| Orphan OpenSpec change left uncleaned | `gestion-archive-deuda-check` was left unarchived after its code was reverted under #914 | [#169](https://github.com/matiaspakua/notaire/issues/169) | Low | Orphan/invalid (`.openspec.yaml` without `schema`; only `proposal.md`+`spec.md`, no traceability/design/tasks) |
| Frontend global branch coverage is under the CI threshold | `Frontend CI` fails `Unit Tests (Vitest)` with `branches (5.91%) does not meet global threshold (6%)`; reproducible on a clean `main` worktree, not introduced by any PR in flight | [#966](https://github.com/matiaspakua/notaire/issues/966) | Medium | No change (CI/quality infrastructure finding) |

## Resolved (merged and archived)

| Title | Short description | Issue | Severity | OpenSpec status |
|---|---|---|---|---|
| Substitutions had no practical effect on managements | Registering a substitution did not redirect new cases to the substitute notary | [#836](https://github.com/matiaspakua/notaire/issues/836) | Medium | Archived (`2026-09-07-suplencia-efecto-en-gestiones`, PR [#970](https://github.com/matiaspakua/notaire/pull/970) merged 2026-09-07) |
| Silent `DELETE` on ~30 entities | `Persistable.isNew()` was inferred badly on a primitive `version`, so several entities were not deleted even though the call returned `200 OK` | [#957](https://github.com/matiaspakua/notaire/issues/957) | High | Archived (`2026-09-07-silent-delete-persistable-fix`, PR [#965](https://github.com/matiaspakua/notaire/pull/965) merged 2026-09-07) |
| The payment receipt was never issued | The last step of CU15 (hand the receipt to the client) had no way to print or issue it | [#23](https://github.com/matiaspakua/notaire/issues/23) | High | Archived (`2026-09-07-recibo-de-pago-pdf`) |
| Budgeting had no templates or item catalog | Price templates and the item catalog existed but were not used when building a real budget | [#834](https://github.com/matiaspakua/notaire/issues/834) | Medium | Archived (`presupuesto-plantillas-y-catalogo-items`, PR [#962](https://github.com/matiaspakua/notaire/pull/962) merged 2026-09-07) |
| The business process did not check debt when archiving | `GestionArchiveDebtService` was added to management archiving | [#819](https://github.com/matiaspakua/notaire/issues/819) | High | Archived (`2026-08-20-verify-debt-on-gestion-archive`) |
| A duplicate person was not validated | No check for a repeated identification type and number when creating a client | [#835](https://github.com/matiaspakua/notaire/issues/835) | High | Archived (`2026-09-02-persona-validacion-duplicados`) |
| The post-signing legal circuit had no screen | Signing, testimony, verification, inscription, and withdrawal (CU06–CU12) had no UI | [#832](https://github.com/matiaspakua/notaire/issues/832) | High | Archived (`2026-08-27-escritura-post-firma-legal-cycle`) |
| ManagementHistory and the state engine were not connected to the real screen | Management transitions were not validated against `WorkflowDefinition`; there was no history log | [#833](https://github.com/matiaspakua/notaire/issues/833) | High | Archived (`2026-08-28-gestion-workflow-y-bitacora`) |
| A whole SRS block had no development (protocol) | Notebooks, procedure folders, auxiliary protocol, registration draft, and sequential numbering — about 20 functional requirements with no development | [#839](https://github.com/matiaspakua/notaire/issues/839) | High | Archived (`protocolo-auxiliar-tramites`, `protocolo-carpetas-de-tramite`, `protocolo-cuadernos-de-folios`, `protocolo-minuta-inscripcion`, `protocolo-numeracion-escrituras`) |
| A deed had no folio picker | It blocked signing the deed and the full E2E demo | [#892](https://github.com/matiaspakua/notaire/issues/892) | High | Archived (`2026-09-02-escritura-folio-picker-form`) |
| `GET /managements/client/{id}` returned a hardcoded empty list | Found in the Playwright suite audit | [#943](https://github.com/matiaspakua/notaire/issues/943) | High | No change formal (direct fix) |
| Collection had no management-level summary | A payment was recorded against a loose budget, with no summary per case | [#820](https://github.com/matiaspakua/notaire/issues/820) | Medium | Archived (`2026-08-26-payment-financial-tracking`) |
| The payment method was not persisted | `"Efectivo"`/`"transferencia"` was lost when the payment was saved | [#792](https://github.com/matiaspakua/notaire/issues/792) | Medium | Archived (`2026-08-20-persist-metodo-pago-on-pago`) |
| Nothing stopped an overcharge | The only real check was `monto <= 0`, with no check against the outstanding balance | [#848](https://github.com/matiaspakua/notaire/issues/848) | Medium | Archived (`2026-09-02-pago-limite-saldo-pendiente`) |
| The outstanding balance was not visible when collecting | CU15/CU47 require showing the balance before collecting; it did not exist | [#796](https://github.com/matiaspakua/notaire/issues/796) | Medium | Archived (`2026-09-03-pago-presupuesto-picker-saldo`) |
| Partial payments and installments had no circuit | RF-22 requires installment collection and a debt warning on close; neither existed | [#821](https://github.com/matiaspakua/notaire/issues/821) | Medium | Archived (`2026-09-02-pagos-parciales-cuotas`) |
| Discounts and surcharges had no structured reason | There was no way to mark an item as a discount or surcharge with a reason | [#822](https://github.com/matiaspakua/notaire/issues/822) | Medium | Archived (`2026-09-02-descuentos-recargos-presupuesto`) |
| Document costs were not connected to the budget | `importeAPagar` on `SubmittedDocument` was not added to any total or balance | [#823](https://github.com/matiaspakua/notaire/issues/823) | Medium | Archived (`2026-09-03-costos-documentos-presupuesto`) |
| Document types had no rules of their own (due date) | The screen only allowed entering the name; no due date, term, or responsible party | [#837](https://github.com/matiaspakua/notaire/issues/837) | Medium | Archived (`2026-09-04-tipo-documento-vencimiento-config`) |
| `ManagementHistory.fecha` had date-only precision | It broke history order when two events happened on the same day | [#946](https://github.com/matiaspakua/notaire/issues/946) | Medium | No change formal (direct fix) |
| Person DNI/CUIT columns were always empty | Field-name mismatch between the backend and the frontend type | [#939](https://github.com/matiaspakua/notaire/issues/939) | Medium | No change formal (direct fix) |
| The deed form had no "observaciones" field | It blocked justifying gaps in sequential numbering | [#950](https://github.com/matiaspakua/notaire/issues/950) | Medium | No change formal (direct fix) |
| E2E demo — Property type mapped badly | It blocked case B of the end-to-end demo | [#879](https://github.com/matiaspakua/notaire/issues/879) | Medium | Archived (`2026-08-31-fix-inmueble-valuacion-fiscal-type`) |
| E2E demo — budget association was broken | It blocked the full demo flow | [#883](https://github.com/matiaspakua/notaire/issues/883) | Medium | No change formal (direct fix) |
| E2E demo — picker labels were inconsistent | It blocked selection in the demo forms | [#889](https://github.com/matiaspakua/notaire/issues/889) | Low | No change formal (direct fix) |

## Pass history

1. **2026-08-11** — initial technical and functional survey (`explore_functional_report.md`).
2. **2026-08-19** — exhaustive pass on collection (partial payments, discounts, surcharges, extra costs) and the state diagram of a management.
3. **2026-08-21** — triage of the 13 original findings consolidated in this document; review of the workflow engine against the state diagram and the main screen (added the re-entry-loop finding, now #841).
4. **2026-08-26** — the overpayment sub-finding (#848) was consolidated back into this document.
5. **2026-09-05** — full check of `backend-api/` and `frontend/` against `main`; confirmed that most collection findings and the protocol block were resolved; added the orphan OpenSpec change finding (#169) and the cross-cutting silent `DELETE` (#957).
6. **2026-09-05** — RF↔CU traceability check (95/95 functional requirements covered, with the documentation exception of CU84/#956) and an end-to-end check that a management can be started and completed using real screens for each lifecycle step (CU02–CU16), not only the API. This file was rewritten as an index (detailed content moved to each GitHub Issue).
7. **2026-09-07** — the **OpenSpec status** column was added to both tables, derived from `openspec status --change <id> --json` (not from the Issue status). Confirmed and archived `presupuesto-plantillas-y-catalogo-items` (#834, PR #962 merged the same day); moved #23 and #834 to Resolved. Corrected finding #957: it was NOT merged (the issue stayed open, with no PR); the code existed only on the local unpushed branch `fix/957_silent-delete-persistable-fix` — it was moved back to Open. Confirmed that `gestion-archive-deuda-check` (#169) has a `.openspec.yaml` without a valid `schema`, so the CLI cannot read its status.
8. **2026-09-07** — merged PR [#965](https://github.com/matiaspakua/notaire/pull/965) (`fix/957_silent-delete-persistable-fix`), synced and archived the change `silent-delete-persistable-fix`; moved #957 to Resolved. The PR exposed a new finding, unrelated to the fix (confirmed reproducible on a clean `main`): `Frontend CI` had been failing the global branch-coverage threshold (5.91% < 6%) since 2026-09-02 without being detected — created issue [#966](https://github.com/matiaspakua/notaire/issues/966) and added it to Open.
9. **2026-09-07** — merged PR [#970](https://github.com/matiaspakua/notaire/pull/970) (`feat/836_suplencia-efecto-en-gestiones`); CD (`CD - Build & Publish Docker`) was green on `main@5996b3d`, issue #836 closed automatically on merge, and the smoke test was confirmed (`/actuator/health` UP, TS-0092 green in CI on the merge commit). Delta specs were synced to `docs/openspec/specs/gestion-asignacion-suplencia/` and `docs/openspec/specs/persona-credencial-escribano/` (new capabilities) and the change was archived to `2026-09-07-suplencia-efecto-en-gestiones`; moved #836 to Resolved.
