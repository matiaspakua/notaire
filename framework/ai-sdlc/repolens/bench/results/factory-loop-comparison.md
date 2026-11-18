# Repolens in the factory loop — measured comparison

Measured on 2026-11-18 by the cloud agent that shipped #2292–#2305 without Repolens and then used the CLI for the next slices. Numbers below are either taken from `gh pr view` timestamps or counted from commands run in this session. Where a number cannot be reconstructed, it is marked not recorded. None of these figures are invented.

## What was measured

- **PR open → merge** is the GitHub `createdAt` to `mergedAt` interval. It includes waiting for the owner to merge, including overnight, so it is not agent work time.
- **Locate cost** for the pre-Repolens issues was not logged. This session has no stored count of grep or file-open calls before the first edit.
- **Preflight attempts** are counted only where this session's own record (the run, or the PR text written from that run) names a failed run and a later pass.
- **Repolens** queries are the CLI commands actually run after `repolens index` finished. `repolens report` printed `0 events` because the CLI did not write `.repolens/usage.jsonl`.

## One-time index

`repolens index` on this checkout: 9,821 files, 92,922 nodes, 265,947 edges, 83,571 embeddings. The command's own timer: extract 10.03s, link 4.33s, embed 1,252.16s, total 1,267.21s. The embedder downloaded `BAAI/bge-small-en-v1.5` once. Doctor before the index warned `no store yet`. A search run before the index returned `no results` and exit 1.

## Baseline (no Repolens): PR lifetime from GitHub

| PR | Issue | Opened (UTC) | Merged (UTC) | Open→merge |
|---|---|---|---|---|
| #2292 | #2047 | 2026-11-17 12:00:52 | 2026-11-17 14:54:42 | 2h 54m |
| #2293 | #1563 | 2026-11-17 13:12:27 | 2026-11-17 14:55:09 | 1h 43m |
| #2294 | #1244 | 2026-11-17 14:28:28 | 2026-11-17 14:55:32 | 27m |
| #2295 | #288 | 2026-11-17 15:34:50 | 2026-11-17 15:57:01 | 22m |
| #2296 | #580 FolioType | 2026-11-17 16:56:41 | 2026-11-17 17:19:56 | 23m |
| #2299 | #596 payments | 2026-11-17 19:06:55 | 2026-11-17 19:37:26 | 30m |
| #2300 | #580 DocumentType | 2026-11-17 19:47:51 | 2026-11-17 21:56:49 | 2h 9m |
| #2302 | #580 ProcedureType | 2026-11-17 21:49:16 | 2026-11-17 21:57:33 | 8m |
| #2301 | #580 Concept | 2026-11-17 21:06:18 | 2026-11-18 06:41:08 | 9h 35m |
| #2304 | #580 ManagementStatus | 2026-11-17 23:53:01 | 2026-11-18 06:41:20 | 6h 48m |
| #2305 | #596 copies | 2026-11-18 01:02:47 | 2026-11-18 06:41:34 | 5h 39m |

#2301, #2304 and #2305 sat overnight until the owner merged them together. That interval is not a navigation saving.

Preflight restarts recorded in this session (not a complete count for #2292–#2295):

| PR | Recorded full-preflight runs |
|---|---|
| #2296 | 2 (ratchet baseline, then pass) |
| #2299 | 4 (integration assertions, Playwright mobile stub, stack down, then pass) |
| #2300 | 1 pass |
| #2301 | 2 (Vitest timeout, then pass) plus 1 later merge-main pass |
| #2302 | 1 pass |
| #2304 | 2 (Vitest timeout, then pass) |
| #2305 | 3 (frontend image type error, Repolens hook dry-run, then pass) |

## With Repolens

| Slice | Locate commands | What they returned | First useful answer | Preflight |
|---|---|---|---|---|
| Property `getDto` (#580, #2306) | 1 search before the index (exit 1), then `search "Property getDto DtoProperty mapping"`, `impact Property.getDto`, `search "property persistence adapter package"` | Search ranked `Property.java` 2nd and named `Procedure.java` and `DeedManagement.java`. Impact returned **0 files**. The adapter search ranked `PropertyCatalogAdapter.java` 1st. | About 3s after the index existed | 1 pass |
| Submitted documents list (#596) | `search "submitted documents list endpoint GET getAll"`, `search "frontend submitted documents page useSubmittedDocuments"` | Rank 1 was `SubmittedDocumentController.getAll`. Rank 1 of the second query was `useSubmittedDocuments`, and `documents/page.tsx` was rank 2 `via: calls -> useSubmittedDocuments`. | A few seconds | This commit's `preflight.sh --full` |

`repolens report` after those CLI calls: `0 events`. The usage log was not created. The factory signal `repolens report --gate` therefore cannot see CLI locate work until something writes `usage.jsonl`.

## Comparison

Repolens search replaced the first grep for the two slices above: one query named the entity file and the persistence adapter, and one query named the controller and the page with a `calls` edge. That is fewer locate steps than the earlier slices, which walked controllers and hooks with repeated search. The sample is two slices against eleven earlier pull requests of different sizes, so it does not support a percentage improvement.

`impact` did not list the `getDto()` call sites that the following search results and the source edit still had to read. Open-to-merge time is dominated by the owner's merge, not by search. The 21-minute embed is paid once per machine, not once per issue.

## Friction

1. `repolens search` before `repolens index` exits 1 with `no results` instead of saying the store is missing. Doctor had already warned.
2. `repolens impact Property.getDto` returned 0 callers while `Procedure` and `DeedManagement` call `getDto()`.
3. CLI searches do not append `.repolens/usage.jsonl`, so `repolens report` stays at 0 events. The MCP hook is what the factory profile expects to record usage; this cloud agent called the CLI.
4. Full index embed was 1,252s on this VM.

Issue creation from this agent has returned 403 on label writes before. These four items are the record if a GitHub issue cannot be opened.
