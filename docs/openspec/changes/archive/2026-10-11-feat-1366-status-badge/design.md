# Design

## Context

#1346 fixed the budget vocabulary and #1365 added `success`/`warning`/`info` tokens with AA contrast, so the badge tones exist. Six list pages still render status in six ways.

## Goals / Non-Goals

Goals: one component for every status column; tone + icon + text; translated labels where the vocabulary is fixed; never blank. Non-goals: translating admin-defined management status names; status filters and edit selects (they keep their own options); the registration-draft (`minuta-estado`) and deed screens, which have their own workflow display.

## Decisions

The mapping lives in a plain module (`lib/status-maps.ts`) so it is unit-testable without React and reusable by filters later. `labelKey` is relative to the messages root so budget reuses `presupuestos.status.*`, testimony `testimonios.estado*` and user `common.active/inactive` instead of duplicating labels; only folio needed new keys. Management names are compared case- and accent-insensitively against the seeded catalog for the tone; custom names are neutral. Folio values are accepted with or without the `Folio ` prefix (BusinessConstants spells `Folio Utilizado`, stored rows say `Utilizado`). The protocol page keeps its own meaning (available unless used), as the `availability` domain. Icons: neutral `CircleDashed`, info `CircleDot`, success `CheckCircle2`, warning `Clock`, danger `XCircle`.

## Riesgos / Trade-offs

Tone choices for management statuses are a judgement (e.g. "sin firmar" = warning); they only affect color and icon, the text is unchanged.

## Testing Strategy

Vitest `status-badge.test.tsx`: mapping per domain, case/prefix tolerance, unknown → neutral raw, icon `aria-hidden`, es and en labels, folio keys present in both locales, and a source guard that the six pages use `<StatusBadge domain=` with no ad-hoc status badge. Written first and red (module missing).

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

TS-0124: budgets, cases, testimonies, users and folios show one badge per row with a tone, a hidden icon and text; a Pendiente budget is a warning badge. Red on the pre-change build.

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commits.
