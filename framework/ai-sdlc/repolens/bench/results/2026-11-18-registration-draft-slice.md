# Registration-draft slice (Repolens)

Date: 2026-11-18. Branch `cursor/580-registration-draft-mapper-47dc`.

## Queries

Run with `framework/ai-sdlc/repolens/bin/repolens search` on the indexed checkout `/agent/repos/notaire`. Each search returned in about 2 seconds.

| Query | Top hit |
|-------|---------|
| `SubstitutionController getAll GET /api/v1/substitutions` | `SubstitutionController.java` |
| `useSubstitutions dashboard substitutions page` | `frontend/src/app/dashboard/substitutions/page.tsx` |
| `RegistrationDraft getDto DtoRegistrationDraft` | `DtoRegistrationDraft.java`, then `RegistrationDraft.getDto` |
| `RegistrationDraftOpsAdapter getDto` | `RegistrationDraftOpsAdapter.java` |

`repolens report` still records 0 events. CLI search does not write `.repolens/usage.jsonl` (#2307).

## Decision

Substitutions is a single screen, and the year and notary filters run on the full list in the browser. Paging the endpoint without those filters on the server would hide rows. Not implemented.

Registration drafts have one production caller, the ops adapter. The mapper test failed to compile because the class did not exist, then passed with the ratchet test.
