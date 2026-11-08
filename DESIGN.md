---
name: Notaire
description: Quiet protocol desk for a notary office. One blue seal, paper neutrals, token motion.
colors:
  san-francisco-blue: "#0071E3"
  apple-blue: "#0A84FF"
  ink: "#1D1D1F"
  secondary-ink: "#6E6E73"
  paper: "#FBFBFD"
  mist: "#F5F5F7"
  hairline: "#E5E5EA"
  white: "#FFFFFF"
  success: "#34C759"
  warning: "#FF9500"
  error-text: "#C81E1E"
typography:
  display:
    fontFamily: "\"SF Pro Display\", \"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif"
    fontSize: "1.875rem"
    fontWeight: 600
    lineHeight: 1.2
    letterSpacing: "-0.015em"
  title:
    fontFamily: "\"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.375
    letterSpacing: "-0.01em"
  body:
    fontFamily: "\"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "-0.01em"
  label:
    fontFamily: "\"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 600
    lineHeight: 1.375
    letterSpacing: "-0.01em"
rounded:
  sm: "0.5rem"
  md: "0.75rem"
  lg: "1rem"
  xl: "1.5rem"
  2xl: "1.75rem"
  full: "9999px"
spacing:
  xs: "0.25rem"
  sm: "0.5rem"
  md: "1rem"
  lg: "1.5rem"
  xl: "2rem"
components:
  button-primary:
    backgroundColor: "{colors.san-francisco-blue}"
    textColor: "{colors.white}"
    rounded: "{rounded.lg}"
    padding: "0 1.5rem"
    height: "3rem"
  button-primary-hover:
    backgroundColor: "{colors.apple-blue}"
    textColor: "{colors.white}"
    rounded: "{rounded.lg}"
    padding: "0 1.5rem"
    height: "3rem"
  input:
    backgroundColor: "{colors.white}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "0 1rem"
    height: "3rem"
  card:
    backgroundColor: "{colors.white}"
    textColor: "{colors.ink}"
    rounded: "{rounded.2xl}"
    padding: "2rem"
---

# Design System: Notaire

## Overview

**Creative North Star: "The Quiet Protocol Desk"**

The name is inferred from the incumbent system, not from an interview. `tokens.ts`
already commits the product app to macOS Sequoia: SF Pro, San Francisco Blue
(`#0071E3`, owner decision #1365), paper neutrals, and soft elevation. The
desk metaphor is the notary's working surface. One seal color. Generous
margins. The task sits on a white card. Chrome recedes.

Operate mode governs login, the shell, forms, tables, and the dashboard.
Scanability and the existing tokens outrank expression. GitHub Pages
(`github-page/`) is a separate Persuade surface and is not described here.

Density is comfortable, not sparse: 16px between fields, 24px between
sections, 32px inside a card. Motion is a short rise (4px, 120–240ms) that
confirms a state. It does not spin, spring, or play a page-load sequence.

**Key Characteristics:**

- One accent, used for the primary action, the current nav item, and links.
- Paper (`#FBFBFD` / `#F5F5F7`) behind white cards.
- SF Pro for every UI role. No display face in labels or tables.
- 28px card radius (the token `2xl`), 16px buttons, 12px fields.
- Opacity and transform only, from `frontend/src/theme/motion.ts`.

## Colors

The palette is Restrained: San Francisco Blue on a warm-neutral paper, with
Apple green, orange, and red reserved for status.

### Primary

- **San Francisco Blue** (`colors.san-francisco-blue`): brand primary and the
  fill of the primary button, the active sidebar item, and focus. Owner-pinned.
- **Apple Blue** (`colors.apple-blue`): hover of that primary fill, and the
  info tone. It is not a second brand.

### Neutral

- **Ink** (`colors.ink`): primary text.
- **Secondary ink** (`colors.secondary-ink`): supporting text. Documented at
  about 4.7:1 on mist (#1341). Do not set readable text in `neutral.500`.
- **Paper** (`colors.paper`): app canvas.
- **Mist** (`colors.mist`): sidebar, secondary button fill, inset wells.
- **Hairline** (`colors.hairline`): dividers and input borders in the same family.
- **White** (`colors.white`): cards, inputs, primary button label.

### Named Rules

**The One Seal Rule.** San Francisco Blue covers the primary action, the
current navigation item, and focus. It is not a tint for every icon well, and
it is not a gradient.

**The Status Is Not Brand Rule.** Success, warning, and error-text are status
only. Error text uses `colors.error-text` so it stays above 4.5:1 on white.

## Typography

**Display Font:** SF Pro Display (with SF Pro Text, then the system sans)
**Body Font:** SF Pro Text (with the system sans)
**Label/Mono Font:** SF Pro Text for labels; Menlo / Monaco for code and ids

**Character:** One sans, tight tracking on headings (−0.015em), no uppercase
on form labels. Uppercase is limited to the existing sidebar group labels.

### Hierarchy

- **Display** (600, 1.875rem, 1.2): page titles such as the login product name and the dashboard greeting.
- **Title** (600, 1.125rem, 1.375): the task heading inside a card (`h2` on login, section titles).
- **Body** (400, 1rem, 1.5): prose and input text. Keep prose near 65–75ch. Tables may run wider.
- **Label** (600, 0.875rem, 1.375): field labels and compact meta. Sentence case.

### Named Rules

**The One Heading Rule.** A screen has one `h1`. The task title inside a card is an `h2`. Do not stack a product name, a subtitle, and the task title at neighboring sizes with no grouping.

## Layout

The authenticated app is a fixed sidebar (18rem) plus a fluid main column,
max 1600px, padding 24px (32px from the `lg` breakpoint). Login is a single
centered column, max 400px, page padding 24px. The rhythm is 8px: fields
stack at 16px, sections at 24px, card padding at 32px. Below 768px the
sidebar becomes a dialog sheet. Type sizes stay on the rem scale; they do
not fluid-scale with the viewport.

## Elevation & Depth

Hybrid, and quiet. Surfaces are white on mist. Depth is a hairline plus a
soft offset shadow (the `apple-shadow` utility mirrors `shadows.sm`). There
is no hard offset shadow and no colored halo. Glass (`backdrop-filter`) is
only the sticky page header, where content scrolls under it.

### Shadow Vocabulary

- **Resting card** (`box-shadow: 0 0 0 1px rgba(0,0,0,0.06), 0 2px 4px rgba(0,0,0,0.04), 0 8px 16px rgba(0,0,0,0.04)`): cards, the login desk, inputs at rest when a shadow is required.
- **Raised card** (`box-shadow: 0 0 0 1px rgba(0,0,0,0.06), 0 4px 8px rgba(0,0,0,0.06), 0 16px 32px rgba(0,0,0,0.08)`): a card that is hovered or a dialog.

### Named Rules

**The Flat-Until-Lifted Rule.** A control does not float at rest. Hover may
lift a dashboard card by a few pixels; press is `scale(0.98)`. Login does
not lift.

## Shapes

Corners are continuous and generous: 12px fields, 16px buttons and nav
items, 28px cards. Pills are for small status chips and the dashboard date,
not for primary buttons. Borders are 1px hairlines. The login seal well is
22px, between a button and a card, so it reads as an object rather than a button.

## Components

### Buttons

- **Shape:** gently rounded (16px), 48px tall on the large size used for sign-in.
- **Primary:** San Francisco Blue fill, white label, horizontal padding 24px.
- **Hover / Focus:** hover moves to Apple Blue. Focus is a 2px primary ring with a 1px offset. Press scales to 0.98 unless reduced motion is on.
- **Secondary:** mist fill, blue label. Ghost is a text button on mist hover.

### Cards / Containers

- **Corner Style:** 28px (`rounded.2xl`).
- **Background:** white.
- **Shadow Strategy:** resting card. The login desk uses this and does not add glass.
- **Border:** the shadow's hairline is the edge. Do not add a second colored border.
- **Internal Padding:** 32px.

### Inputs / Fields

- **Style:** 1px hairline, white fill, 12px radius, 48px tall, 16px horizontal padding.
- **Focus:** border shifts to San Francisco Blue and a 3px ring at 30% (`apple-focus`).
- **Error / Disabled:** error text is `colors.error-text` with an icon. Disabled fill is mist; disabled text is not used for readable copy (`neutral.500`).

### Navigation

Sidebar groups are sentence-or-uppercase labels at 11px, bold, secondary ink.
The current item is a blue pill (12px radius) with white label and icon.
Other items are ink on transparent, mist on hover. Below 768px the same list
is a modal sheet. Group disclosure remembers collapsed sections. Motion on
the active pill may use a shared layout id; new entrances still use the
motion tokens rather than a custom spring.

### Login desk

The sign-in task is one card: `h2`, optional session-expired status, then the
form. The product name stays outside as the only `h1`. Three motions, all
token-timed: the seal rises 4px over 240ms (`ease.emphasized`), the card
follows after 30ms over 180ms (`ease.standard`), and the session-expired
status rises over 120ms only when it is shown. Reduced motion leaves seal
and card untransformed.

## Do's and Don'ts

### Do:

- **Do** take color, type, radius, spacing, and motion from `frontend/src/theme/tokens.ts` and `motion.ts`.
- **Do** build forms as FormContainer, FormSection, FormField, FormActions.
- **Do** give an empty list a title, a reason, and one next action when the screen can offer one.
- **Do** keep primary buttons at least 44px tall on touch (`sizes.button.lg` is 48px).

### Don't:

- **Don't** introduce a second accent, a purple gradient, or a cream "AI" palette. The paper neutrals are already the warmth.
- **Don't** rotate, spring, or stagger more than six items at 30ms on an Operate surface.
- **Don't** set body or label text in `neutral.500` (`#A2A2A6`).
- **Don't** put the task title, the product name, and a subtitle in one undifferentiated stack.
- **Don't** copy this Operate restraint onto `github-page/` as if it were the same mode.
