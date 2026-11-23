---
title: UI/UX Design Best Practices
description: Mandatory UI/UX rules for Notaire frontend development
alwaysApply: false
---

# UI/UX Design: the rules every UI task needs

Details, tables and examples: `.claude/skills/frontend-design/references/ui-ux-reference.md`
(typography, color, spacing, every component standard, toasts, tables, modals, motion, the full
checklist). Token values live in `frontend/src/theme/` and
`docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`.

## Always

- **Tokens only.** Colors, spacing, radii and sizes come from `frontend/src/theme/` (`theme.*`,
  Tailwind token classes). Never hardcode a hex value, px spacing or a dimension.
- **Forms:** `FormContainer` → `FormHeader` → `FormSection` → `FormField` → `FormActions`.
  Labels above inputs, never a placeholder as the label. Errors below the field, cleared when it
  is fixed. Keep the data on a validation error. Disable submit while saving.
- **Inputs:** 48px tall, 12px radius, 16px font (no mobile zoom), the right `type`
  (email, tel, number, date).
- **Buttons:** one primary action per view. Labels name the action (Create, Save, Delete), never
  Submit or OK. Destructive actions are red, kept away from primary, and confirmed. Show a
  loading state. Links navigate; buttons act.
- **States:** every screen designs loading, empty (say what to do next), error (name the problem
  and the recovery) and success. No generic "Error".
- **Spacing:** the 4/8px scale only (`theme.spacing`). 24px page padding, 32px card padding,
  24px between sections, 16px between fields; forms ≤600px wide.
- **Cards:** never nested. A 28px radius, one hairline border, a soft shadow.

## Accessibility (WCAG AA, required)

- Text contrast ≥4.5:1 (≥3:1 large). Never use color alone for meaning: add text or an icon.
- A visible focus ring on every interactive element. Tab, Enter and Escape work. Modals trap
  focus and close on Escape.
- Labels are tied to inputs, error text is linked to its field, and icon-only buttons carry a
  translated `aria-label`.
- Semantic HTML (`button`, `label`, `table`), and touch targets ≥44px.
- Honor `prefers-reduced-motion`. Motion runs 150–300ms with `cubic-bezier(0.4, 0, 0.2, 1)`.

## Responsive

Mobile first: breakpoints are 640px and 1024px. Forms stack vertically on mobile. No horizontal
page scroll at 320px. Never hide essential content.

## Verify before done

- Playwright covers the changed UI, including 320, 768 and 1024px.
- axe reports nothing serious or critical in light and dark.
- Keyboard-only pass.
