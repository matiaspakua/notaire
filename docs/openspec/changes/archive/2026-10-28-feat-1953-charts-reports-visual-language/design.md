# Design — #1953

## Approach

Extract the reports page’s card shell and the home stat tile into shared
components with the same apple-shadow / rounded-28 language already used on the
dashboard. Reports catalog queries (notaries, folio types) surface LoadingState
or ErrorState above the grid when every dependent select needs them.

## Decisions

- **No chart library** in this wave — document the metric/report card language only.
- **Tailwind + theme tokens** on ReportCard (migrate off most inline styles).
- **PageSection groups**: “Quotes & cases”, “Sworn statements & books”, “Document debt”
  (or i18n keys) — keep seven cards, clearer scan.

## Out of scope

- New PDF report types
- Wave 7 axe / form validation E2E
