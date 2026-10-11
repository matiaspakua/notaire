# Design

## Context

#1339 made date-only values show the stored calendar day in any browser zone (`lib/dates.ts`), but the display locale stayed `es-AR`. The issue proposed next-intl `useFormatter()`; the calendar-date logic already lives in `lib/dates.ts`, so the hook reuses it.

## Goals / Non-Goals

Goals: one formatter source; English and Spanish formats for dates, instants, numbers and ARS money; no locale literals in screens. Non-goals: a currency other than ARS, server-side formats in `i18n/request.ts`, PDF reports (backend).

## Decisions

`useFormat()` wraps `useLocale()` and the existing `lib/dates.ts` functions instead of next-intl `useFormatter()`. The calendar-date rule from #1339 (format the stored day in UTC) stays in one place, and plain `Intl` gives the same output. `en` maps to `en-GB` because the Owner chose day-first dates. Its currency style is `ARS 75,000.00`, the format the issue asked for. The es-AR helpers stay as wrappers for non-React code; a guard keeps screens off them.

## Riesgos / Trade-offs

`en-GB` pads days and months (`08/07/2026`), while es-AR does not (`8/7/2026`). Both are day first, which is the agreed format. Columns render on every locale change because the hook's memo depends on the Intl locale.

## Testing Strategy

Vitest `use-format.test.tsx` renders under `NextIntlClientProvider` for es and en: date, empty value, currency, number and instant. Guards: no locale literal in `src/app` or `src/components`, and no es-AR helper calls there. Written first (module missing, red).

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

TS-0040 `#1355` cases seed a payment on 2026-07-08 with a unique amount and check the pagos row in en and es. Red on the pre-change build.

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commits.
