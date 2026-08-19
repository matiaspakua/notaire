# Apple-Inspired Design System Architecture

**Owner:** Frontend Team

Notaire's frontend uses a centralized, token-based design system inspired by
Apple's macOS Sequoia / iOS 18 design language. All visual elements (colors,
typography, spacing, shadows, etc.) are managed through a single source of
truth: `frontend/src/theme/`.

## Table of Contents

1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [Token Structure](#token-structure)
4. [Quick Reference](#quick-reference)
5. [Implementation](#implementation)
6. [Component Patterns](#component-patterns)
7. [Form Implementation Checklist](#form-implementation-checklist)
8. [Maintenance](#maintenance)
9. [Migration Guide](#migration-guide)

---

## Overview

### Key Benefits

- **Consistency**: All forms and UI components share the same design language
- **Maintainability**: Change the theme in one place; updates apply everywhere
- **Scalability**: Easy to add new components while maintaining design coherence
- **Accessibility**: Built-in color contrast and semantic color definitions
- **Dark Mode Ready**: Foundation prepared for a future dark mode implementation

### Design Language Characteristics

- **Minimalist**: Clean, uncluttered interfaces
- **Professional**: Suitable for legal/notarial services
- **Modern**: Apple's contemporary design aesthetic
- **Accessible**: WCAG AA compliant color contrasts
- **Responsive**: Works seamlessly on desktop, tablet, and mobile

---

## Design Principles

### 1. Simplicity

> "The least important thing about a window is what it looks like. The most
> important thing is what it does — content over chrome."

- Remove all non-essential elements
- Use whitespace effectively
- Avoid visual clutter
- Focus attention on user tasks

### 2. Typography Hierarchy

- **Display Font**: SF Pro Display (headings, titles)
- **Body Font**: SF Pro Text (body, forms)
- **Monospace**: Menlo/Monaco (code)

### 3. Color Strategy

| Role | Token |
|------|-------|
| Primary actions, links, focus | `theme.colors.primary[600]` |
| Status indicators | `theme.colors.success` / `theme.colors.error` / `theme.colors.warning` / `theme.colors.info` |

### 4. Layout

- **8px base unit** for consistent rhythm
- **Increments**: 4px, 8px, 12px, 16px, 24px, 32px, etc.
- **Card padding**: Always 32px (`spacing[8]`)
- **Form field gap**: Always 16px (`spacing[4]`)

### 5. Motion & Interaction

- **Duration**: 200ms (base), 300ms (slower), 150ms (fast)
- **Easing**: `cubic-bezier(0.4, 0, 0.2, 1)` (Apple standard)
- **Scale on hover**: Subtle 1.02x scale on buttons
- **No bounce**: Prefer smooth, predictable motion

### 6. Shadows & Depth

- Subtle layering, Apple-style shadows
- Shadows hint at clickability without being jarring
- Used sparingly, only on elevated components (cards, modals)

---

## Token Structure

The theme system is organized into logical categories in
`frontend/src/theme/tokens.ts`:

```typescript
// Colors
colors.neutral[0-900]   // Gray scale (white → black)
colors.primary[50-900]  // Apple blue shades
colors.success[50-600]  // Green success states
colors.warning[50-600]  // Orange warnings
colors.error[50-600]    // Red errors
colors.info[50-600]     // Blue informational content

// Typography
fontFamily.display      // "SF Pro Display" (headings)
fontFamily.body         // "SF Pro Text" (content)
fontFamily.mono         // "Menlo" (code)
fontSize.xs — fontSize["4xl"]  // 12px – 36px scale
fontWeight.regular — fontWeight.bold  // 400 – 700
lineHeight.tight — lineHeight.loose   // 1.2 – 2.0
letterSpacing.*         // Apple's negative tracking

// Spacing
spacing[0-24]            // 0px to 96px (8px increments)

// Border Radius
borderRadius.sm          // 8px (subtle)
borderRadius.md          // 12px (inputs)
borderRadius.lg          // 16px (buttons, cards)
borderRadius.xl          // 24px (large cards)
borderRadius["2xl"]      // 28px (Apple-style premium)

// Shadows
shadows.sm — shadows.xl  // Subtle to maximum elevation

// Semantic tokens (intent-based)
semantic.form.*          // Form input styling
semantic.button.*        // Button styling
semantic.card.*          // Card container styles
semantic.status.*        // Success, error, warning, info
```

---

## Quick Reference

A condensed cheat sheet for day-to-day development. For rationale and full
detail see the sections above/below.

### Essential Files

| File | Purpose |
|------|---------|
| `frontend/src/theme/tokens.ts` | Design tokens (colors, spacing, typography) |
| `frontend/src/theme/form-patterns.tsx` | Form components (`FormContainer`, `FormField`, etc.) |
| `frontend/src/theme/README.md` | Code-adjacent quick-start and troubleshooting |
| `.claude/rules/ui-ux-design.md` | Mandatory rules |
| `.claude/skills/frontend-design/SKILL.md` | Implementation patterns for AI agents |

### Full Form Example (with state, validation, API call)

```tsx
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { apiPost } from "@/lib/api-client";
import { toast } from "sonner";
import { theme } from "@/theme/tokens";

export function MyForm() {
  const [data, setData] = useState({ field: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!data.field) newErrors.field = "Required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      await apiPost("/endpoint", data);
      toast.success("Saved!");
    } catch {
      toast.error("Failed to save. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: theme.spacing[6] }}>
      <FormContainer>
        <FormSection title="Information">
          <FormField label="Field" required error={errors.field}>
            <Input
              placeholder="..."
              value={data.field}
              onChange={(e) => setData({ ...data, field: e.target.value })}
            />
          </FormField>
        </FormSection>

        <FormActions align="right">
          <Button variant="secondary" disabled={loading}>Cancel</Button>
          <Button variant="default" disabled={loading} onClick={handleSubmit}>
            {loading ? "Saving..." : "Save"}
          </Button>
        </FormActions>
      </FormContainer>
    </div>
  );
}
```

### Most-Used Tokens

```typescript
// Text colors
theme.colors.neutral[900]   // Body text (dark)
theme.colors.neutral[600]   // Secondary text (medium gray)

// Action colors
theme.colors.primary[600]   // Primary button, links
theme.colors.error[500]
theme.colors.success[500]

// Semantic form colors
theme.semantic.form.inputBorder
theme.semantic.form.inputBg
theme.semantic.form.labelText

// Semantic button colors
theme.semantic.button.primaryBg
theme.semantic.button.secondaryBg

// Spacing
theme.spacing[2]  // 8px  — label-to-input gap
theme.spacing[3]  // 12px — button group gap
theme.spacing[4]  // 16px — form field gap
theme.spacing[6]  // 24px — section gap / page padding
theme.spacing[8]  // 32px — card padding

// Border radius
theme.borderRadius.md      // 12px (inputs)
theme.borderRadius.lg      // 16px (buttons)
theme.borderRadius["2xl"]  // 28px (cards - Apple style)

// Transitions
theme.transitions.duration.fast  // 150ms
theme.transitions.duration.base  // 200ms (standard)
theme.transitions.duration.slow  // 300ms
theme.transitions.timing.ease    // cubic-bezier(0.4, 0, 0.2, 1)

// Shadows
theme.shadows.sm  // Inputs, small components
theme.shadows.md  // Cards, containers
theme.shadows.lg  // Modals, elevated
theme.shadows.xl  // Maximum elevation
```

### Component Sizing

| Component | Height | Border Radius | Padding |
|-----------|--------|----------------|---------|
| Input | 48px | 12px | 16px horizontal |
| Button (md) | 40px | 12px | 12–20px horizontal |
| Button (lg) | 48px | 12px | 20px horizontal |
| Card | auto | 28px | 32px |

### Button Variants

```tsx
<Button variant="default">Save</Button>       {/* Primary, blue */}
<Button variant="secondary">Cancel</Button>   {/* Gray */}
<Button variant="destructive">Delete</Button> {/* Red */}
<Button variant="ghost">Learn More</Button>   {/* No fill */}
<Button variant="link">Skip</Button>          {/* Link style */}
```

### Important Rules (❌ Don't Forget)

```typescript
// ❌ WRONG — hardcoded values
color: "#0071e3"
padding: "16px"
borderRadius: "12px"

// ✅ RIGHT — theme tokens
color: theme.colors.primary[600]
padding: theme.spacing[4]
borderRadius: theme.borderRadius.lg
```

```tsx
{/* ❌ WRONG — placeholder as label */}
<input placeholder="Enter name" />

{/* ✅ RIGHT */}
<FormField label="Name">
  <Input placeholder="Juan Pérez" />
</FormField>
```

```tsx
{/* ❌ WRONG — no loading indication */}
<Button onClick={handleSubmit}>Save</Button>

{/* ✅ RIGHT */}
<Button disabled={loading} onClick={handleSubmit}>
  {loading ? "Saving..." : "Save"}
</Button>
```

### Before Submitting a PR

- [ ] No hardcoded colors or spacing (use `theme.*`)
- [ ] All inputs have visible labels
- [ ] Errors shown in `theme.colors.error[600]`, associated via `aria-describedby`
- [ ] Button disabled while submitting; loading state displayed
- [ ] Focus indicators visible; color contrast ≥4.5:1
- [ ] Responsive at 320px, 768px, 1024px
- [ ] Tests passing; `npm run build` succeeds

---

## Implementation

### File Structure

```
frontend/src/theme/
├── tokens.ts         # Core design tokens (single source of truth)
├── index.ts           # Utilities & hooks
├── form-patterns.tsx  # Reusable form component patterns
└── README.md          # Code-adjacent quick-start guide
```

### How to Access Tokens

**Option 1: Direct import (TypeScript)**

```typescript
import { theme } from "@/theme/tokens";

const primaryColor = theme.colors.primary[600];
const padding = theme.spacing[4];
```

**Option 2: Hooks (React components)**

```typescript
import { useTheme } from "@/theme";

export function MyComponent() {
  const theme = useTheme();
  return <div style={{ color: theme.colors.primary[600] }}>Content</div>;
}
```

**Option 3: CSS variables (in `globals.css`)**

```css
.my-element {
  color: var(--primary-600);
  padding: var(--spacing-4);
  border-radius: var(--radius-lg);
}
```

### Dark Mode Preparation

Dark mode is not yet active, but tokens are structured to support it:

```typescript
// In globals.css, add a .dark selector:
.dark {
  --primary-600: #1B82F6;  // Lighter blue for dark mode
  --neutral-900: #FFFFFF;  // Inverted for dark mode
}
```

---

## Component Patterns

### Form Structure

All forms follow this structure — see [Quick Reference](#quick-reference)
above for a full working example with state and validation:

```tsx
import {
  FormContainer,
  FormField,
  FormSection,
  FormActions,
  FormHeader,
} from "@/theme/form-patterns";

export function MyForm() {
  return (
    <FormContainer>
      <FormHeader
        title="Create New Document"
        description="Provide all required information"
      />

      <FormSection title="Basic Information">
        <FormField label="Document Type" required>
          <Input placeholder="Type..." />
        </FormField>
        <FormField label="Description" helperText="Optional">
          <Input placeholder="Enter description..." />
        </FormField>
      </FormSection>

      <FormActions align="right">
        <Button variant="secondary">Cancel</Button>
        <Button variant="default">Create</Button>
      </FormActions>
    </FormContainer>
  );
}
```

### Semantic Colors

```tsx
import { theme } from "@/theme/tokens";

<input style={{ borderColor: theme.semantic.form.inputBorder }} />
<button style={{ backgroundColor: theme.semantic.button.primaryBg }} />
<div style={{ backgroundColor: theme.semantic.form.errorBg }}>Error text</div>
```

### UI Components

Base components (`button.tsx`, `input.tsx`, `card.tsx`, etc. under
`frontend/src/components/ui/`) consume theme tokens via Tailwind
theme-generated utility classes (e.g. `bg-primary-600 text-white rounded-lg`)
rather than inline styles, keeping usage idiomatic React/Tailwind.

---

## Form Implementation Checklist

Use this checklist when creating or updating any form in the Notaire frontend.

### Structure & Layout

- [ ] **Use FormContainer**: All form content wrapped in `<FormContainer>`
- [ ] **Use FormHeader**: Add title, subtitle, description for context
- [ ] **Use FormSection**: Group related fields logically, with a visual divider between sections
- [ ] **Responsive layout**: Content stacks vertically on mobile
- [ ] **Max width**: Form content limited to 600px for readability
- [ ] **Padding**: 24px page padding, 32px card padding via `theme.spacing`

### Form Fields

- [ ] **Proper labels**: Every input has a visible `<label>` with `htmlFor`, positioned above the input (never a placeholder-as-label)
- [ ] **Required indicator**: Marked with `*` and `aria-required`
- [ ] **Helper text**: Complex fields explain format (e.g. "DD/MM/YYYY")
- [ ] **Input types**: `type="email"`, `type="tel"`, `type="number"`, etc.
- [ ] **Focus/hover/disabled states**: Visible focus ring, subtle hover border change, reduced-opacity disabled state

### Validation & Error Handling

- [ ] **Validation on blur**, real-time for complex fields (email, phone)
- [ ] **Error display**: Below field, `theme.colors.error[600]`, linked via `aria-describedby`
- [ ] **Error persistence**: Errors clear when the user fixes the input
- [ ] **Submission blocked** while validation fails; success feedback via toast

### Buttons & Actions

- [ ] **Primary action**: `variant="default"`; cancel/reset: `variant="secondary"`; destructive: `variant="destructive"`
- [ ] **Action verbs**: "Create", "Save", "Delete" — not "Submit"/"Ok"
- [ ] **Grouped** in `<FormActions>`, primary aligned right
- [ ] **Disabled + loading state** while submitting, to prevent duplicate submits
- [ ] **Confirmation dialog** for destructive actions

### Styling & Theming

- [ ] **No hardcoded hex/pixel values** anywhere — colors from `theme.colors.*`/`theme.semantic.*`, spacing from `theme.spacing`, radius from `theme.borderRadius`, typography from `theme.typography.*`
- [ ] **Consistent gaps**: field `spacing[4]`, section `spacing[6]`, card padding `spacing[8]`, label-to-input `spacing[2]`, button group `spacing[3]`
- [ ] **Transitions**: `theme.transitions.duration.base` (200ms) with `theme.transitions.timing.ease`

### Accessibility

- [ ] **Semantic HTML**: `<form>`, `<label>`, `<input>`, `<button>`
- [ ] **Color contrast** ≥4.5:1 (verify with WebAIM, axe, or WAVE)
- [ ] **Keyboard navigation**: Tab, Enter to submit, Escape to cancel
- [ ] **Touch targets** ≥44×44px
- [ ] **No color-alone validation** — pair with icon/text
- [ ] **`lang="es"`** on the form (Spanish for Notaire)

### Responsiveness

- [ ] Usable at 320px (mobile), 768px (tablet), 1024px+ (desktop)
- [ ] Fields stack vertically on mobile; no horizontal scroll

### Testing & Code Quality

- [ ] Visual comparison with other forms in the app; tested in Chrome, Safari, Firefox
- [ ] Empty state and pre-filled data both work
- [ ] Keyboard-only and screen-reader navigation verified
- [ ] `npm run build` succeeds with no TypeScript/lint errors
- [ ] No hardcoded/untranslated strings; no leftover `console.log`

### Security

- [ ] No `dangerouslySetInnerHTML` or unsanitized user input
- [ ] No secrets/API keys hardcoded; no sensitive data logged

### Deployment Readiness

- [ ] Branch named `feat/###_form_name`; unit tests for validation logic pass
- [ ] Conventional commit referencing the issue; PR links the issue
- [ ] No merge conflicts

### TL;DR

- [ ] `FormContainer` → `FormSection` → `FormField` → `FormActions`
- [ ] All colors/spacing/typography from theme tokens, no hardcoding
- [ ] Labels on all inputs, errors shown below fields
- [ ] Button disabled + loading during submission
- [ ] Focus indicators visible, contrast ≥4.5:1, keyboard-navigable
- [ ] Responsive at 320px/768px/1024px; tests passing, build successful

---

## Maintenance

### Adding New Tokens

1. **Identify purpose**: color, spacing, size, or other property?
2. **Check existing tokens**: reuse if possible
3. **Add to the appropriate category** in `tokens.ts`
4. **Update semantic tokens** if it serves a UI purpose
5. **Document** the addition here

Example:

```typescript
// In tokens.ts
export const colors = {
  // ... existing colors ...
  accent: {
    50: "#FFF3E0",
    500: "#FF9500",
    600: "#E68400",
  },
};

// In semantic tokens
semantic.status.attention = colors.accent[500];
```

### Updating Global Styles

1. Update `tokens.ts`
2. Regenerate CSS variables in `globals.css`
3. Test in at least 3 different forms/pages before merging

---

## Migration Guide

Steps for bringing an existing component onto the theme system:

### Step 1: Audit Current Styling

```bash
grep -r "bg-\[#" frontend/src/components/
grep -r "text-\[#" frontend/src/components/
```

### Step 2: Replace with Theme Tokens

**Before:**

```tsx
className={cn(
  "flex h-12 w-full rounded-2xl border border-[#d2d2d7] bg-white px-4 py-2.5 text-base transition-all duration-300 ...",
  className
)}
```

**After (Tailwind, recommended):**

```tsx
className={cn(
  "flex h-12 w-full rounded-lg border border-neutral-400 bg-white px-4 py-2.5 text-base transition-all duration-200",
  className
)}
```

### Step 3: Update Typography

**Before:**

```tsx
className="text-xs font-semibold uppercase tracking-wider text-[#86868b]"
```

**After:**

```tsx
import { theme } from "@/theme/tokens";

style={{
  fontSize: theme.typography.fontSize.xs,
  fontWeight: theme.typography.fontWeight.semibold,
  textTransform: "uppercase",
  letterSpacing: theme.typography.letterSpacing.normal,
  color: theme.colors.neutral[600],
}}
```

### Step 4: Verify

1. Visual comparison against other forms
2. Interactive state testing (hover, focus, disabled)
3. Accessibility audit (contrast ratios)
4. Responsive check at 320px / 768px / 1024px

---

## Related Documentation

- [ADR-011: Centralized Design System](../202-ADR/ADR-011-centralized-design-system.md)
- [`frontend/src/theme/README.md`](../../../../frontend/src/theme/README.md) — code-adjacent quick-start
- [`.claude/rules/ui-ux-design.md`](../../../../.claude/rules/ui-ux-design.md) — mandatory rules
- [`.claude/skills/frontend-design/SKILL.md`](../../../../.claude/skills/frontend-design/SKILL.md) — implementation patterns
- [Apple Human Interface Guidelines](https://developer.apple.com/design/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Design Tokens W3C Community Group](https://www.designtokens.org/)
