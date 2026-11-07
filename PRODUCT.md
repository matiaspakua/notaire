# Product

<!-- impeccable:product-schema 1 -->

Inferences below are labeled. This file was written without a product interview:
the session had no question channel back to the owner. Repository evidence is
the source. Unlabeled sentences are direct readings of that evidence.

## Platform

web

## Users

**Inferred.** Primary users are notaries and their office staff in a single
practice, signing in to prepare budgets, open a case (gestión), track deeds,
testimonies, payments, and protocol folios. An administrator maintains users,
roles, catalogs, and workflows. The product is not a public marketplace.

## Product Purpose

Notaire is a notary-office management system. Success is a clerk completing a
case step (budget, document, deed, testimony, payment) without hunting for the
next action. Evidence: `frontend/src/app/dashboard/**` and the Use Case set
under `docs/100-business/102-use-cases/`.

## Positioning

**Inferred.** The mechanism a generic practice-management tool does not copy
is the notarial protocol: folios, testimonies, substitutions, and the state
workflow attached to a gestión. The software is the desk for that protocol,
not a marketing site. The public site lives separately in `github-page/`.

## Operating Context

Authenticated work happens in the dashboard shell (sidebar, breadcrumb, page
header) after CU84 login. Lists, forms, and the workflow tracker are the daily
surfaces. Spanish is the primary UI language (`frontend/messages/es.json`).

## Capabilities and Constraints

- Login, role-gated administration, and the case modules listed in the
  dashboard navigation.
- Visual values come from `frontend/src/theme/tokens.ts` and
  `frontend/src/theme/motion.ts`. Forms use FormContainer, FormSection,
  FormField, and FormActions.
- **Undecided:** dark mode is described as a foundation in the design-system
  doc and is not a shipped theme.
- **Undecided:** password reset. The login page tells the user to contact an
  administrator; there is no reset flow in this change.

## Brand Commitments

The product name is Notaire. The owner decision recorded in `tokens.ts`
(#1365, 2026-10-09) fixes the brand primary at `#0071E3`. The design-system
doc commits the UI to an Apple macOS Sequoia / iOS 18 language: SF Pro,
restrained blue, soft elevation. That commitment is binding for the product
app. It is not a brief to restyle GitHub Pages.

## Evidence on Hand

- Use cases: `docs/100-business/102-use-cases/`
- Token source: `frontend/src/theme/tokens.ts`, `frontend/src/theme/motion.ts`
- Design-system doc: `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- No customer quotes, pricing, or licensing text were found for this file.
  Do not invent them.

## Product Principles

- The task is the product. Chrome exists so a clerk can find the next step.
- One visual language for the authenticated app. Tokens are the only palette.
- Motion explains state. It does not perform.
- Spanish copy names the action and the recovery. Do not paraphrase legal
  terms into generic SaaS labels.
- Accessibility is part of the desk: labels, focus, contrast, and reduced
  motion are already required by the form skill and #1368.

## Accessibility & Inclusion

WCAG AA contrast is the documented bar (`tokens.ts` records ratios for
secondary and error text, #1341). `prefers-reduced-motion` is honored by
MotionConfig (`reducedMotion="user"`) and by a CSS fallback in `globals.css`.
