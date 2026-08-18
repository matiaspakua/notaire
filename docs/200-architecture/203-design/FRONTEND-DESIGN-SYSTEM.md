# Apple-Inspired Design System Architecture

**Version:** 1.0  
**Date:** May 2026  
**Status:** Active  
**Owner:** Frontend Team

## Table of Contents

1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [Token Structure](#token-structure)
4. [Implementation](#implementation)
5. [Component Patterns](#component-patterns)
6. [Usage Guide](#usage-guide)
7. [Maintenance](#maintenance)
8. [Migration Guide](#migration-guide)

---

## Overview

The Notaire frontend uses a **centralized, token-based design system** inspired by Apple's macOS Sequoia and iOS 18 design language. All visual elements (colors, typography, spacing, shadows, etc.) are managed through a single source of truth.

### Key Benefits

- **Consistency**: All forms and UI components share the same design language
- **Maintainability**: Change the theme in one place; updates apply everywhere
- **Scalability**: Easy to add new components while maintaining design coherence
- **Accessibility**: Built-in color contrast and semantic color definitions
- **Dark Mode Ready**: Foundation prepared for dark mode implementation

### Design Language Characteristics

- **Minimalist**: Clean, uncluttered interfaces
- **Professional**: Suitable for legal/notarial services
- **Modern**: Apple's contemporary design aesthetic
- **Accessible**: WCAG AA compliant color contrasts
- **Responsive**: Works seamlessly on desktop, tablet, mobile

---

## Design Principles

### 1. Simplicity

> "The least important thing is the window. The most important thing is your content."

- Remove all non-essential elements
- Use whitespace effectively
- Avoid visual clutter
- Focus attention on user tasks

### 2. Typography Hierarchy

- **Display Font**: SF Pro Display (headings, titles)
- **Body Font**: SF Pro Text (body, forms)
- **Monospace**: Menlo/Monaco (code)

### 3. Color Strategy

| Role | Purpose | Token |
|------|---------|-------|
| **Primary** | Key actions, focus states | `primary-600` |
| **Neutrals** | Text, borders, backgrounds | `neutral-*` |
| **Semantic** | Status indicators (success, error, warning) | `colors.success`, `colors.error` |

### 4. Spacing & Layout

- **8px base unit** for consistent rhythm
- **Increments**: 4px, 8px, 12px, 16px, 24px, 32px, etc.
- **Card padding**: Always 32px (spacing[8])
- **Form field gap**: Always 16px (spacing[4])

### 5. Motion & Interaction

- **Duration**: 200ms (base), 300ms (slower), 150ms (fast)
- **Easing**: `cubic-bezier(0.4, 0, 0.2, 1)` (Apple standard)
- **Scale on hover**: Subtle 1.02x scale for buttons
- **No bounce**: Prefer smooth, predictable motion

### 6. Shadows & Depth

- **Subtle layering** with Apple-style shadows
- Shadows hint at clickability without being jarring
- Used sparingly for elevated components (cards, modals)

---

## Token Structure

The theme system is organized into logical categories:

### Colors (`tokens.colors`)

```typescript
colors.neutral[0-900]      // Gray scale (white → black)
colors.primary[50-900]     // Apple blue shades
colors.success[50-600]     // Green for success states
colors.warning[50-600]     // Orange for warnings
colors.error[50-600]       // Red for errors
colors.info[50-600]        // Blue for informational content
```

### Typography (`tokens.typography`)

```typescript
fontFamily.display         // "SF Pro Display" (headings)
fontFamily.body            // "SF Pro Text" (content)
fontFamily.mono            // "Menlo" (code)

fontSize.xs to fontSize.4xl  // 12px to 36px scale
fontWeight.regular to bold   // 400 to 700
lineHeight.tight to loose    // 1.2 to 2.0
letterSpacing.*              // Apple's negative tracking
```

### Spacing (`tokens.spacing`)

```typescript
spacing[0-24]  // 0px to 96px (8px increments)
```

### Border Radius (`tokens.borderRadius`)

```typescript
sm: 8px      // Subtle rounding
md: 12px     // Inputs
lg: 16px     // Buttons, cards
xl: 24px     // Large cards
2xl: 28px    // Premium cards (Apple style)
```

### Shadows (`tokens.shadows`)

```typescript
sm, md, lg, xl  // Increasing shadow intensity
```

### Semantic Tokens (`tokens.semantic`)

Intent-based groupings for specific UI purposes:

```typescript
semantic.form.*      // Input, label, error styles
semantic.button.*    // Button color variants
semantic.card.*      // Card container styles
semantic.status.*    // Success, error, warning, info
```

---

## Implementation

### File Structure

```
frontend/src/theme/
├── tokens.ts              # Core design tokens (single source of truth)
├── index.ts               # Utilities & hooks
├── form-patterns.tsx      # Reusable form component patterns
└── README.md              # This file
```

### How to Access Tokens

#### Option 1: Direct Import (TypeScript)

```typescript
import { theme } from "@/theme/tokens";

const primaryColor = theme.colors.primary[600];
const padding = theme.spacing[4];
```

#### Option 2: Use Hooks (React Components)

```typescript
import { useTheme } from "@/theme";

export function MyComponent() {
  const theme = useTheme();
  return <div style={{ color: theme.colors.primary[600] }}>Content</div>;
}
```

#### Option 3: CSS Variables (in globals.css)

```css
.my-element {
  color: var(--primary-600);
  padding: var(--spacing-4);
  border-radius: var(--radius-lg);
}
```

---

## Component Patterns

### Form Component Pattern

All forms should follow this structure:

```tsx
import { 
  FormContainer, 
  FormField, 
  FormSection, 
  FormActions,
  FormHeader 
} from "@/theme/form-patterns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function MyForm() {
  return (
    <FormContainer>
      <FormHeader 
        title="Create New Document"
        subtitle="Fill in the details"
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

      <FormSection title="Document Details">
        {/* More fields */}
      </FormSection>

      <FormActions align="right">
        <Button variant="secondary">Cancel</Button>
        <Button variant="default">Create</Button>
      </FormActions>
    </FormContainer>
  );
}
```

### Using Semantic Colors

```tsx
import { theme } from "@/theme/tokens";

// For form inputs
<input style={{ borderColor: theme.semantic.form.inputBorder }} />

// For buttons
<button style={{ backgroundColor: theme.semantic.button.primaryBg }} />

// For error states
<div style={{ backgroundColor: theme.semantic.form.errorBg }} />
```

---

## Usage Guide

### For UI Components

Update all component files (`button.tsx`, `input.tsx`, `card.tsx`, etc.) to use theme tokens:

**Before:**
```tsx
className="bg-[#0071e3] text-white rounded-[12px] px-[20px]"
```

**After:**
```tsx
import { theme } from "@/theme/tokens";

// Or use Tailwind with theme-generated utilities
className="bg-primary-600 text-white rounded-lg px-5"
```

### For Form Pages

All form pages should:

1. Import form patterns
2. Structure using FormContainer, FormSection, FormField
3. Use semantic colors for status indicators
4. Apply consistent spacing via theme.spacing

**Example:**
```tsx
import { FormContainer, FormField, FormSection, FormActions } from "@/theme/form-patterns";
import { theme } from "@/theme/tokens";

export function DocumentForm() {
  return (
    <div style={{ padding: theme.spacing[6] }}>
      <FormContainer>
        {/* Your form structure */}
      </FormContainer>
    </div>
  );
}
```

### Dark Mode Preparation

While dark mode is not yet active, the token structure supports it:

```typescript
// In globals.css, add to .dark selector:
.dark {
  --primary-600: #1B82F6;  // Lighter blue for dark mode
  --neutral-900: #FFFFFF; // Inverted for dark mode
}
```

---

## Maintenance

### Adding New Tokens

1. **Identify the purpose**: Is this a color, spacing, size, or other property?
2. **Check existing tokens**: Reuse if possible
3. **Add to appropriate category** in `tokens.ts`
4. **Document in this guide**
5. **Update semantic tokens** if it serves a UI purpose

Example:

```typescript
// In tokens.ts
export const colors = {
  // ... existing colors ...
  accent: {
    50: "#FFF3E0",
    500: "#FF9500",
    600: "#E68400",
  }
}

// In semantic tokens
semantic.status.attention = colors.accent[500];
```

### Updating Global Styles

When modifying the theme:

1. Update `tokens.ts`
2. Regenerate CSS variables in `globals.css`
3. Test in at least 3 different form pages
4. Verify accessibility with contrast checker

### Version Control

- **Major**: New color palette, typography system change
- **Minor**: New tokens, token adjustments
- **Patch**: Bug fixes, documentation

---

## Migration Guide

### For Existing Components

#### Step 1: Audit Current Styling

```bash
# Find hardcoded colors
grep -r "bg-\[#" frontend/src/components/
grep -r "text-\[#" frontend/src/components/
```

#### Step 2: Replace with Theme Tokens

**Input Component Before:**
```tsx
className={cn(
  "flex h-12 w-full rounded-2xl border border-[#d2d2d7] bg-white px-4 py-2.5 text-base transition-all duration-300 ...",
  className
)}
```

**Input Component After:**
```tsx
import { theme } from "@/theme/tokens";

className={cn(
  `flex w-full px-4 py-2.5 text-base transition-all duration-200 ...`,
  "rounded-[${theme.borderRadius.lg}px]",
  `border border-[${theme.colors.neutral[400]}]`,
  `bg-[${theme.colors.neutral[0]}]`,
  className
)}
```

Or with Tailwind (recommended):
```tsx
className={cn(
  "flex h-12 w-full rounded-lg border border-neutral-400 bg-white px-4 py-2.5 text-base transition-all duration-200",
  className
)}
```

#### Step 3: Update Typography

**Button Label Before:**
```tsx
className="text-xs font-semibold uppercase tracking-wider text-[#86868b]"
```

**Button Label After:**
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

#### Step 4: Test Consistency

1. Visual comparison with other forms
2. Interactive state testing (hover, focus, disabled)
3. Accessibility audit (contrast ratios)
4. Responsive design check

---

## Architecture Decision Record

**ADR-008**: Centralized Token-Based Design System

**Problem**: Individual components used hardcoded colors and values, making consistency difficult and theme changes expensive.

**Solution**: Implement a centralized, token-based design system inspired by Apple's design language.

**Advantages**:
- Single source of truth for all design values
- Easy theme customization
- Better accessibility management
- Improved developer experience

**Trade-offs**:
- Initial migration effort
- All components must follow the pattern
- Performance overhead is negligible

**Status**: Approved and Active

---

## References

- [Apple Design Guidelines](https://developer.apple.com/design/)
- [SF Symbols Documentation](https://developer.apple.com/sf-symbols/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Token Studio](https://tokens.studio/)
- [System Design Tokens](https://www.designtokens.org/)

---

## Checklist for Component Updates

- [ ] All hardcoded colors replaced with theme tokens
- [ ] All hardcoded spacing uses `theme.spacing`
- [ ] All border radius uses `theme.borderRadius`
- [ ] Typography follows `theme.typography` standards
- [ ] Shadows use `theme.shadows`
- [ ] Transitions use theme duration and easing
- [ ] Form fields use FormField pattern
- [ ] Semantic colors for status indicators
- [ ] Hover and focus states applied
- [ ] Accessibility tested (WCAG AA)
- [ ] Responsive behavior verified
- [ ] Documentation updated

---

## Support & Questions

For questions about the design system:

1. Check this documentation first
2. Review examples in existing components
3. Consult the theme tokens file
4. Ask in team communications
