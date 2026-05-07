---
title: Frontend Design System Implementation
description: Skill for implementing Apple-inspired design system in frontend forms
alwaysApply: true
---

# Frontend Design System Implementation Skill

## Skill Overview

This skill guides the implementation of forms and UI components using the centralized Apple-inspired design system in the Notaire frontend.

## When to Use This Skill

Use this skill when:
- Creating or updating form pages
- Building new UI components
- Styling input fields, buttons, cards, or modals
- Updating existing forms to match the design system
- Need to apply colors, spacing, typography, or shadows

## Quick Start

### 1. Access the Theme Tokens

```typescript
// In your component file
import { theme } from "@/theme/tokens";

// Use in JSX
<div style={{ 
  color: theme.colors.primary[600],
  padding: theme.spacing[4],
  borderRadius: theme.borderRadius.lg,
}}>
  Content
</div>
```

### 2. Use Form Patterns

Every form must use the form pattern components:

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
      <FormHeader title="Form Title" description="Form description" />
      
      <FormSection title="Section Title" subtitle="Section description">
        <FormField label="Field Label" required>
          <Input placeholder="..." />
        </FormField>
      </FormSection>

      <FormActions align="right">
        <Button variant="secondary">Cancel</Button>
        <Button variant="default">Submit</Button>
      </FormActions>
    </FormContainer>
  );
}
```

### 3. Follow the Component Patterns

All components must follow established patterns. See below for patterns.

## Design Token Categories

### Colors

Always use semantic tokens from `theme.semantic.*`:

```typescript
// Form inputs
theme.semantic.form.inputBorder       // Input border
theme.semantic.form.inputBg           // Input background
theme.semantic.form.inputText         // Input text
theme.semantic.form.errorBg           // Error background
theme.semantic.form.errorText         // Error text

// Buttons
theme.semantic.button.primaryBg       // Primary button background
theme.semantic.button.primaryHover    // Primary button hover
theme.semantic.button.secondaryBg     // Secondary button background

// Cards
theme.semantic.card.bg                // Card background
theme.semantic.card.shadow            // Card shadow

// Status
theme.semantic.status.success         // Success color
theme.semantic.status.error           // Error color
theme.semantic.status.warning         // Warning color
```

### Spacing

Always use `theme.spacing`:

```typescript
theme.spacing[0]   // 0px
theme.spacing[1]   // 4px
theme.spacing[2]   // 8px
theme.spacing[3]   // 12px
theme.spacing[4]   // 16px (form field gap)
theme.spacing[6]   // 24px (section gap)
theme.spacing[8]   // 32px (card padding)
theme.spacing[10]  // 40px
theme.spacing[12]  // 48px
```

Standard form spacings:
- **Card padding**: `spacing[8]` (32px)
- **Section gap**: `spacing[6]` (24px)
- **Form field gap**: `spacing[4]` (16px)
- **Label to input gap**: `spacing[2]` (8px)

### Border Radius

Always use `theme.borderRadius`:

```typescript
theme.borderRadius.md      // 12px (inputs)
theme.borderRadius.lg      // 16px (buttons, cards)
theme.borderRadius.xl      // 24px (large cards)
theme.borderRadius["2xl"]  // 28px (premium cards - Apple style)
```

### Typography

Always use `theme.typography`:

```typescript
// Fonts
theme.typography.fontFamily.display  // SF Pro Display (headings)
theme.typography.fontFamily.body     // SF Pro Text (body)

// Sizes
theme.typography.fontSize.xs         // 12px
theme.typography.fontSize.sm         // 14px
theme.typography.fontSize.base       // 16px
theme.typography.fontSize.lg         // 18px
theme.typography.fontSize.xl         // 20px
theme.typography.fontSize["2xl"]     // 24px

// Weights
theme.typography.fontWeight.regular  // 400
theme.typography.fontWeight.medium   // 500
theme.typography.fontWeight.semibold // 600
theme.typography.fontWeight.bold     // 700

// Line Heights
theme.typography.lineHeight.tight    // 1.2
theme.typography.lineHeight.normal   // 1.5
theme.typography.lineHeight.relaxed  // 1.625

// Letter Spacing
theme.typography.letterSpacing.tight // -0.015em (headings)
theme.typography.letterSpacing.normal// -0.01em
```

### Shadows

Always use `theme.shadows`:

```typescript
theme.shadows.sm      // Subtle shadow (inputs, small components)
theme.shadows.md      // Standard shadow (cards)
theme.shadows.lg      // Larger shadow (modals, elevated components)
theme.shadows.xl      // Maximum shadow (top-level overlays)
```

## Component Implementation Patterns

### Input Field Pattern

```tsx
<div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[2] }}>
  <label style={{
    color: theme.colors.neutral[900],
    fontSize: theme.typography.fontSize.sm,
    fontWeight: theme.typography.fontWeight.semibold,
    fontFamily: theme.typography.fontFamily.body,
  }}>
    Label {required && <span style={{ color: theme.colors.error[500] }}>*</span>}
  </label>
  
  <input style={{
    height: theme.sizes.input.height,
    padding: theme.sizes.input.padding,
    borderRadius: theme.sizes.input.borderRadius,
    border: `1px solid ${theme.semantic.form.inputBorder}`,
    fontSize: theme.sizes.input.fontSize,
    color: theme.semantic.form.inputText,
    fontFamily: theme.typography.fontFamily.body,
    transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
  }} 
  placeholder="Placeholder text"
  />
  
  {error && (
    <p style={{ color: theme.colors.error[600], fontSize: theme.typography.fontSize.xs }}>
      {error}
    </p>
  )}
</div>
```

### Button Pattern

```tsx
<button style={{
  height: theme.sizes.button.md.height,
  padding: theme.sizes.button.md.padding,
  borderRadius: theme.sizes.button.md.borderRadius,
  backgroundColor: theme.semantic.button.primaryBg,
  color: theme.semantic.button.primaryText,
  fontSize: theme.sizes.button.md.fontSize,
  fontWeight: theme.typography.fontWeight.semibold,
  fontFamily: theme.typography.fontFamily.body,
  border: "none",
  cursor: "pointer",
  transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
}}>
  Button Text
</button>
```

### Card Pattern

```tsx
<div style={{
  backgroundColor: theme.semantic.card.bg,
  borderRadius: theme.sizes.card.borderRadius,
  padding: theme.sizes.card.padding,
  border: `1px solid ${theme.semantic.card.border}`,
  boxShadow: theme.semantic.card.shadow,
}}>
  <h2 style={{
    fontSize: theme.typography.fontSize["2xl"],
    fontWeight: theme.typography.fontWeight.semibold,
    color: theme.colors.neutral[900],
    margin: 0,
    marginBottom: theme.spacing[4],
  }}>
    Card Title
  </h2>
  
  <p style={{
    fontSize: theme.typography.fontSize.base,
    color: theme.colors.neutral[700],
    margin: 0,
    lineHeight: theme.typography.lineHeight.relaxed,
  }}>
    Card content
  </p>
</div>
```

### Form Section Pattern

```tsx
<div style={{
  display: "flex",
  flexDirection: "column",
  gap: theme.spacing[4],
  paddingTop: theme.spacing[6],
  borderTop: `1px solid ${theme.colors.neutral[200]}`,
}}>
  <div>
    <h3 style={{
      fontSize: theme.typography.fontSize.lg,
      fontWeight: theme.typography.fontWeight.semibold,
      color: theme.colors.neutral[900],
      margin: 0,
      marginBottom: theme.spacing[1],
    }}>
      Section Title
    </h3>
    <p style={{
      fontSize: theme.typography.fontSize.sm,
      color: theme.colors.neutral[600],
      margin: 0,
    }}>
      Section description
    </p>
  </div>

  <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[4] }}>
    {/* Form fields here */}
  </div>
</div>
```

## Form Implementation Steps

### Step 1: Structure with FormContainer

```tsx
<FormContainer>
  {/* All form content */}
</FormContainer>
```

### Step 2: Add FormHeader

```tsx
<FormHeader 
  title="Create New Document"
  subtitle="Add a new document to the system"
  description="Fill in the required fields below to create a new document entry."
/>
```

### Step 3: Group Fields in Sections

```tsx
<FormSection title="Basic Information" subtitle="Required fields">
  <FormField label="Document Type" required>
    <Input placeholder="Select type..." />
  </FormField>
  <FormField label="Title" required>
    <Input placeholder="Enter title..." />
  </FormField>
</FormSection>

<FormSection title="Details" subtitle="Additional information">
  {/* More fields */}
</FormSection>
```

### Step 4: Add Actions Footer

```tsx
<FormActions align="right">
  <Button variant="secondary">Cancel</Button>
  <Button variant="default">Create Document</Button>
</FormActions>
```

## Migration Guide

### Migrating Existing Components

#### Before: Hardcoded Colors and Spacing

```tsx
<div className="bg-[#0071e3] text-white px-[20px] py-[12px] rounded-[12px]">
  Button
</div>
```

#### After: Using Theme Tokens

```tsx
import { theme } from "@/theme/tokens";

<button style={{
  backgroundColor: theme.semantic.button.primaryBg,
  color: theme.semantic.button.primaryText,
  padding: `${theme.spacing[3]} ${theme.spacing[5]}`,
  borderRadius: theme.borderRadius.lg,
  border: "none",
  cursor: "pointer",
}}>
  Button
</button>
```

### Using Tailwind with Theme Values

If using Tailwind classes, always refer to theme values:

```tsx
// Create utility classes in globals.css using theme variables
.btn-primary {
  @apply bg-primary-600 text-white px-5 py-3 rounded-lg font-semibold
         transition-all duration-200 hover:bg-primary-500 active:scale-95;
}

.btn-secondary {
  @apply bg-neutral-100 text-primary-600 px-5 py-3 rounded-lg font-semibold
         transition-all duration-200 hover:bg-neutral-200;
}

// Then use in components
<button className="btn-primary">Click Me</button>
```

## Common Implementation Patterns

### Error Validation

```tsx
const [error, setError] = useState<string>("");

<FormField 
  label="Email" 
  required 
  error={error}
  helperText="We'll never share your email"
>
  <Input 
    type="email"
    placeholder="user@example.com"
    onBlur={(e) => {
      if (!e.target.value.includes("@")) {
        setError("Invalid email format");
      } else {
        setError("");
      }
    }}
  />
</FormField>
```

### Loading State

```tsx
const [loading, setLoading] = useState(false);

<Button 
  variant="default" 
  disabled={loading}
  onClick={async () => {
    setLoading(true);
    await handleSubmit();
    setLoading(false);
  }}
>
  {loading ? "Saving..." : "Save Document"}
</Button>
```

### Conditional Fields

```tsx
<FormField label="Document Type" required>
  <Select value={type} onChange={setType}>
    <option>Escritura de Venta</option>
    <option>Poder Notarial</option>
  </Select>
</FormField>

{type === "Poder Notarial" && (
  <FormField label="Power Duration" required>
    <Input type="date" />
  </FormField>
)}
```

### Multi-Step Form

```tsx
const [step, setStep] = useState(1);

<FormContainer>
  {step === 1 && (
    <>
      <FormSection title="Step 1: Basic Info">
        {/* Step 1 fields */}
      </FormSection>
      <FormActions>
        <Button variant="default" onClick={() => setStep(2)}>
          Next
        </Button>
      </FormActions>
    </>
  )}

  {step === 2 && (
    <>
      <FormSection title="Step 2: Details">
        {/* Step 2 fields */}
      </FormSection>
      <FormActions>
        <Button variant="secondary" onClick={() => setStep(1)}>
          Back
        </Button>
        <Button variant="default" onClick={handleSubmit}>
          Create
        </Button>
      </FormActions>
    </>
  )}
</FormContainer>
```

## Accessibility Checklist

Every form implementation must verify:

- [ ] Labels properly associated with inputs (htmlFor/id)
- [ ] Focus indicators visible on all interactive elements
- [ ] Error messages connected to inputs via aria-describedby
- [ ] Required fields marked with * and aria-required
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Color contrast ≥4.5:1
- [ ] Form instructions provided
- [ ] Disabled state visually distinct
- [ ] Loading state clear
- [ ] Success/error feedback provided

## Common Mistakes to Avoid

| Mistake | Why It's Wrong | Fix |
|---------|----------------|-----|
| Hardcoding colors | Inconsistent, hard to maintain | Use `theme.semantic.*` |
| Using placeholder as label | Not accessible | Use proper `<label>` |
| Missing error states | User confusion | Show validation errors |
| No focus indicators | Keyboard inaccessible | Add focus styles |
| Inconsistent spacing | Looks unprofessional | Use `theme.spacing` |
| Missing loading state | App feels frozen | Add spinner/disable button |
| No success feedback | User uncertainty | Show success toast/message |
| Text too small | Hard to read | Min 14px for body text |
| Low color contrast | Accessibility violation | 4.5:1 ratio minimum |
| Too many sections | Overwhelming | Break into steps or collapse |

## Testing Your Implementation

1. **Visual Consistency**: Compare with other forms in the app
2. **Responsive**: Test on mobile (320px), tablet (768px), desktop (1024px+)
3. **Keyboard Navigation**: Tab through all fields, Enter to submit
4. **Focus States**: All interactive elements have visible focus
5. **Error Handling**: Trigger validation, verify error display
6. **Loading State**: Verify spinner appears, button disables
7. **Success Feedback**: Toast/message displays after submission
8. **Accessibility**: Run WAVE or axe DevTools
9. **Performance**: Form interactive in <200ms
10. **Cross-browser**: Test Safari, Chrome, Firefox

## Resources

- **Theme Documentation**: `/frontend/src/theme/tokens.ts`
- **Form Patterns**: `/frontend/src/theme/form-patterns.tsx`
- **Design System Guide**: `/docs/02-architecture/03-design/DESIGN-SYSTEM.md`
- **UI/UX Rules**: `/.claude/rules/ui-ux-design.md`
- **Component Examples**: `/frontend/src/components/`
