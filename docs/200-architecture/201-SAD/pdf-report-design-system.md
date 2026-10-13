# PDF report design system

**Issue:** [#1593](https://github.com/matiaspakua/notaire/issues/1593)  
**Use Case:** CU76 (supporting report RFs without changing their business content)  
**Owner:** backend-api PDF adapter (`adapter.out.pdf`)

This is Slice 1 of the PDF modernization plan. It defines the shared print
system. It does **not** claim PDF/UA-1, approve a print logo, define object-level
authorization, or invent legal form language.

## Tokens

| Token | Value |
|-------|--------|
| Locale / language | `es-AR` |
| Zone | `America/Argentina/Buenos_Aires` (injected `Clock`) |
| Page | A4 portrait by default; A4 landscape when `ReportDocument.Identity` requests it |
| Margins | 18 mm top/side, 16 mm bottom |
| Grid | 4 mm; section gap 8 mm; title-to-meta 4 mm; cell padding 2.5 mm |
| Type | Liberation Sans Regular/Bold, embedded (SIL OFL 1.1) |
| Sizes | title 18 pt, heading 12 pt, body 10/13, meta 9 pt, footer 8 pt |
| Color | near-black text; brand accent only on wordmark, heading, and rules |
| Money / dates | `$ 1.234,56` and `dd/MM/yyyy` |
| Logo | text wordmark **Notaire** until the Owner approves a print master |
| Footer | product + version + generated timestamp + `Página X de Y` |

Classpath assets live under `backend-api/src/main/resources/pdf/`. Request-selected
paths and remote URLs are forbidden. The 100×100 legacy file
`docs/images/logoRojoLetraMediano.png` is not used.

## Anatomy

1. Repeating header: wordmark, report type, document identifier, accent rule.
2. First-page title block: title, subtitle, period/aggregate.
3. Fields and tables. Table headers repeat on continuation pages.
4. Empty tables keep the title and show the table empty message.
5. Repeating footer with no record-level PII.

## HTTP

`PdfResponseFactory` sends `application/pdf`, a safe quoted filename,
`Content-Length`, `Cache-Control: private, no-store`, and
`X-Content-Type-Options: nosniff`. Wrapped generation failures return the
standard `ErrorResponse` JSON body.

## Accessibility baseline

Electronic PDFs declare `es-AR`, set MarkInfo.marked, and keep a logical
title/header/table/footer reading order. PDF/UA-1 is **not** claimed.

## Out of scope for later slices

CU24/CU25/CU50 business queries, CU08/CU80/CU82 legal forms, report
authorization matrix, Owner-approved logo, UI download E2E (Slice 8).
