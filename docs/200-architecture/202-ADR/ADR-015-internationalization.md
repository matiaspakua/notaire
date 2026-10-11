# ADR-015: Frontend internationalization with next-intl

**Status:** Accepted
**Date:** 2026-05-23
**Deciders:** Matías Miguez
**Related:** ADR-005 (Modern Frontend Migration), ADR-011 (Centralized Design System)

## Context

Notaire is a system for Argentine notary offices, with content, forms, and messages in Spanish. The product plan allows offering the system in other, non-Spanish-speaking jurisdictions. In any case, interface strings were hardcoded in React components, mixing presentation with content and making any copy change require a code edit.

## Decision

Adopt **next-intl** as the internationalization library for the Next.js 16 frontend (App Router), with:

- **Message catalogs** in `frontend/messages/{locale}.json` — currently `es.json` (default locale) and `en.json`.
- **Locale resolution** centralized in `frontend/src/i18n/request.ts`, integrated with the Next.js App Router (`next-intl` provides the server-side configuration plugin).
- **Language-switch server actions** in `frontend/src/i18n/actions.ts`.
- Dedicated test coverage (`frontend/src/tests/unit/i18n.test.ts`) that checks both catalogs exist and stay in sync on keys, including page-coverage namespaces (`workflows`, `roles`, `suplencias`, `reportes`, `items`) and login leftovers (issue #1059). Message keys stay Spanish.

### Why next-intl (not react-i18next, and not next-i18next)

| Criterion | next-intl (chosen) | react-i18next | next-i18next |
|----------|----------------------|----------------|----------------|
| App Router support (Next.js 13+) | Native, designed for Server Components | Needs manual wrappers for RSC | Designed for the Pages Router; limited App Router support |
| TypeScript typing of message keys | Yes, by inferring types from the JSON | Partial | Partial |
| Size / dependencies | Light, no `i18next` runtime dependency | Needs the full `i18next` ecosystem | Needs `i18next` + `react-i18next` |
| Active maintenance for Next.js 16 | Yes | Yes (framework-agnostic) | Weaker — near stale for the App Router |

## Consequences

### Positive

- Interface text lives in `frontend/messages/*.json`, separate from component code — aligned with the "do not hardcode values" principle in `.claude/rules/ui-ux-design.md`.
- Server Components can resolve messages without extra client JavaScript, keeping the SAD's interactive budget of under 2 seconds.
- `i18n.test.ts` prevents a catalog from drifting (a key present in `es.json` but missing in `en.json`), which would show blank text in production.

### Negative

- Every new component must use message keys instead of string literals. That needs code-review discipline; no automated lint forces it yet.
- Only `es` (the default) and `en` are covered. Adding a jurisdiction with another language requires a complete catalog before it can be enabled.

## Page coverage note (#1059)

Redirect-only stubs (`/`, `/audit`, `administracion/items`,
`administracion/audit`) have no UI strings and remain exempt. Canonical
dashboard pages and login leftovers consume catalogs via `useTranslations`,
mirroring the `administracion.usuarios` pattern. Those keys stay Spanish.

## Related ADRs

- ADR-005: Modern Frontend Migration (Next.js)
- ADR-011: Centralized Design System
