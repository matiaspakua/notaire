---
title: UI/UX Design Best Practices
description: Mandatory UI/UX rules for Notaire frontend development
alwaysApply: true
---

# UI/UX Design Best Practices

This document defines mandatory UI/UX standards for all frontend development in the Notaire project.

## Core Principles

### 1. User-Centered Design

- Design for the user's needs, not technical convenience
- Minimize cognitive load
- Provide clear feedback for all actions
- Respect user time and attention

### 2. Consistency

- **ONE source of truth**: Use `frontend/src/theme/` exclusively
- **Never hardcode** colors, spacing, or dimensions
- All forms must follow `FormContainer` → `FormSection` → `FormField` pattern
- Component style variants must match across entire application

### 3. Simplicity

- Remove every non-essential element
- Use whitespace effectively
- Progressive disclosure: hide advanced options
- One clear task per page/modal

### 4. Accessibility (WCAG AA)

- Color contrast ratio ≥ 4.5:1 for normal text
- Focus indicators on all interactive elements
- Alt text for all images
- Keyboard navigation must work
- Form labels properly associated with inputs
- Semantic HTML (`<button>`, `<input>`, `<label>`, etc.)

### 5. Performance

- Images optimized (WebP, lazy loading)
- CSS/JS bundled and minified
- Form interactions respond in <100ms
- Page interactive in <2 seconds

---

## Typography Standards

### Font Selection

| Element | Font | Weight | Size | Line Height |
|---------|------|--------|------|-------------|
| Page Title (H1) | SF Pro Display | Bold (700) | 30px | 1.2 |
| Section Title (H2) | SF Pro Display | Semibold (600) | 24px | 1.2 |
| Subsection (H3) | SF Pro Display | Semibold (600) | 18px | 1.375 |
| Body Text | SF Pro Text | Regular (400) | 16px | 1.5 |
| Form Label | SF Pro Text | Semibold (600) | 14px | 1.375 |
| Helper Text | SF Pro Text | Regular (400) | 12px | 1.5 |
| Button Text | SF Pro Text | Semibold (600) | 14-16px | 1.5 |

### Rules

- ✅ Use system fonts (SF Pro Display/Text, -apple-system)
- ✅ Apply negative letter-spacing: `-0.015em` for headings
- ✅ Maintain visual hierarchy through size and weight
- ✅ Ensure sufficient line-height for readability (≥1.5)
- ❌ Never use condensed or stretched fonts
- ❌ Never mix more than 2 font families
- ❌ Avoid colored text (except status indicators)

---

## Color Standards

### Color Usage Rules

| Color | Use Case | Token |
|-------|----------|-------|
| **Primary Blue** | Buttons, links, focus states | `theme.colors.primary[600]` |
| **Neutral 900** | Body text, headings | `theme.colors.neutral[900]` |
| **Neutral 600** | Secondary text, labels | `theme.colors.neutral[600]` |
| **Neutral 400** | Borders, dividers | `theme.colors.neutral[400]` |
| **Success Green** | Success messages, checkmarks | `theme.colors.success[500]` |
| **Error Red** | Errors, destructive actions | `theme.colors.error[500]` |
| **Warning Orange** | Warnings, cautions | `theme.colors.warning[500]` |

### Color Rules

- ✅ Use semantic tokens (`theme.semantic.*`)
- ✅ Verify contrast ratios (4.5:1 minimum)
- ✅ Use consistent colors for similar states across app
- ✅ Apply color to reinforce meaning, not just decoration
- ❌ Never hardcode hex values (use theme tokens)
- ❌ Never use more than 3 accent colors
- ❌ Never use low-contrast color combinations

### Dark Mode Readiness

- All colors must have dark mode variants defined
- Test backgrounds with both light and dark text
- Use HSL format for theme colors (easier to adjust)

---

## Spacing & Layout

### Spacing Scale

Use only these values (8px base unit):

```
0px, 4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px, 80px, 96px
```

Corresponding theme tokens:
```
spacing[0-24]
```

### Layout Rules

| Component | Padding | Gap | Max-Width |
|-----------|---------|-----|-----------|
| Page | 24px | - | 1200px |
| Card | 32px | - | 100% |
| Form Section | - | 24px | 600px |
| Form Field | - | 8px (vertical) | 100% |
| Button Group | - | 12px | - |
| List Items | 16px | 8px | 100% |

### Layout Best Practices

- ✅ Use consistent padding: 32px for cards, 24px for pages
- ✅ Use consistent gaps: 24px between sections, 16px between fields
- ✅ Align elements to 8px grid
- ✅ Use whitespace to group related content
- ✅ Keep content width ≤ 600px for readability
- ❌ Never hardcode padding/margin (use theme.spacing)
- ❌ Avoid asymmetrical spacing
- ❌ Don't exceed 3 levels of nesting in layouts

---

## Forms & Inputs

### Form Structure

Every form MUST follow this pattern:

```tsx
<FormContainer>
  <FormHeader title="Page Title" description="Context" />
  
  <FormSection title="Section 1">
    <FormField label="Field Label" required>
      <Input placeholder="..." />
    </FormField>
    <FormField label="Field 2">
      <Input placeholder="..." />
    </FormField>
  </FormSection>

  <FormSection title="Section 2">
    {/* More fields */}
  </FormSection>

  <FormActions align="right">
    <Button variant="secondary">Cancel</Button>
    <Button variant="default">Submit</Button>
  </FormActions>
</FormContainer>
```

### Input Standards

| Property | Standard |
|----------|----------|
| Height | 48px (`theme.sizes.input.height`) |
| Border Radius | 12px (`theme.borderRadius.lg`) |
| Border | 1px solid `theme.colors.neutral[400]` |
| Padding | 16px (`theme.spacing[4]`) |
| Font Size | 16px (no zoom on mobile) |
| Focus Color | `theme.colors.primary[600]` |
| Error Color | `theme.colors.error[500]` |

### Form Rules

- ✅ Always include labels (above inputs)
- ✅ Use helper text for complex fields
- ✅ Show validation errors below inputs
- ✅ Disable buttons during submission
- ✅ Use proper input types (email, tel, number, etc.)
- ✅ Auto-focus first field (when appropriate)
- ✅ Preserve form data on validation errors
- ❌ Never use placeholder as label
- ❌ Don't submit on Enter unless obvious (e.g., search)
- ❌ Avoid required field asterisks without label
- ❌ Never use color alone to indicate required fields

### Field Types Best Practices

```tsx
// Text input
<FormField label="Full Name" required>
  <Input type="text" placeholder="Juan Pérez" />
</FormField>

// Email (built-in validation)
<FormField label="Email" required>
  <Input type="email" placeholder="user@example.com" />
</FormField>

// Phone (tel type)
<FormField label="Phone" required>
  <Input type="tel" placeholder="+34 XXX XX XX XX" />
</FormField>

// Number (increment/decrement)
<FormField label="Amount">
  <Input type="number" placeholder="0.00" />
</FormField>

// Date picker
<FormField label="Document Date" required>
  <Input type="date" />
</FormField>

// Select/Dropdown
<FormField label="Document Type" required>
  <Select>
    <option>Escritura de Venta</option>
    <option>Poder Notarial</option>
  </Select>
</FormField>

// Textarea for long content
<FormField label="Description">
  <textarea placeholder="Enter description..." style={{borderRadius: "12px"}} />
</FormField>

// Checkbox
<CheckboxField 
  label="I agree to terms"
  checked={checked}
  onChange={setChecked}
/>

// Radio group
<RadioField
  label="Document Status"
  selected={status}
  onChange={setStatus}
  options={[
    { value: "draft", label: "Draft" },
    { value: "signed", label: "Signed" },
  ]}
/>
```

---

## Buttons

### Button Types

| Type | Use Case | Appearance | Example |
|------|----------|-----------|---------|
| **Primary** | Main action, submit | Blue, solid | Save, Create, Send |
| **Secondary** | Alternative action | Light gray, solid | Cancel, Reset |
| **Outline** | Less important action | Border only | Back, Learn More |
| **Ghost** | Tertiary action, undo | No fill | Delete, Undo |
| **Destructive** | Dangerous action | Red | Delete, Remove |

### Button Standards

| Property | Standard |
|----------|----------|
| Height | 40px default, 48px large |
| Border Radius | 12px (`theme.borderRadius.lg`) |
| Font Weight | Semibold (600) |
| Font Size | 14px normal, 16px large |
| Padding | 12px 20px (horizontal spacing) |
| Min Width | 120px (readability) |
| Hover Scale | 1.02x |
| Transition | 200ms `cubic-bezier(0.4, 0, 0.2, 1)` |

### Button Rules

- ✅ Use primary button for main action only
- ✅ Use semantic labels (Create, Save, Delete, not Submit/Ok)
- ✅ Group related buttons together
- ✅ Disable on submission to prevent duplicates
- ✅ Show loading state with spinner
- ✅ Place destructive buttons away from primary
- ✅ Provide confirmation dialogs for destructive actions
- ❌ Never use buttons for navigation (use links)
- ❌ Don't create ghost-on-ghost buttons
- ❌ Avoid button text longer than 3 words
- ❌ Never remove focus indicators

### Button Examples

```tsx
// Primary action
<Button variant="default">Create Document</Button>

// Cancel/Alternative
<Button variant="secondary">Cancel</Button>

// Destructive
<Button variant="destructive">Delete Forever</Button>

// Ghost (low priority)
<Button variant="ghost">Learn More</Button>

// Link-style
<Button variant="link">Skip</Button>
```

---

## Cards & Containers

### Card Standards

| Property | Standard |
|----------|----------|
| Background | White (`theme.colors.neutral[0]`) |
| Border Radius | 28px (`theme.borderRadius["2xl"]`) |
| Border | 1px solid `theme.colors.neutral[300]` |
| Shadow | `theme.shadows.sm` |
| Padding | 32px (`theme.spacing[8]`) |

### Card Rules

- ✅ Use cards to group related information
- ✅ Provide visual elevation through shadows
- ✅ Consistent padding and spacing
- ✅ Use card footers for actions
- ❌ Never nest cards (use sections instead)
- ❌ Don't add excessive shadows (keep subtle)

---

## Status & Feedback

### Validation Messages

```tsx
// Error state
<FormField label="Email" error="Please enter a valid email">
  <Input type="email" />
</FormField>

// Success state
<div style={{ color: theme.colors.success[500] }}>
  ✓ Document saved successfully
</div>

// Warning state
<div style={{ color: theme.colors.warning[500] }}>
  ⚠️ This action cannot be undone
</div>

// Info state
<div style={{ color: theme.colors.info[500] }}>
  ℹ️ You need at least one signatory
</div>
```

### Toast Messages (using Sonner)

```tsx
import { toast } from "sonner";

// Success
toast.success("Document created");

// Error
toast.error("Failed to save document");

// Loading
toast.loading("Saving...");

// Custom
toast.custom((t) => <CustomToast id={t} />);
```

### Validation Rules

- ✅ Show errors immediately on blur or submit
- ✅ Use helper text for format hints (e.g., "DD/MM/YYYY")
- ✅ Clear errors when user fixes input
- ✅ Show validation in real-time for complex fields
- ✅ Provide actionable error messages
- ❌ Never show generic "Error" messages
- ❌ Don't use color alone for validation
- ❌ Avoid all-caps error messages

---

## Tables & Lists

### Table Standards

- Use semantic `<table>` HTML
- Header row with semibold text
- Alternating row backgrounds (optional, `neutral[50]`)
- 1px border between rows
- Sortable columns (if large dataset)
- Pagination for >20 items
- Search/filter capability

### List Standards

- One item per line
- Consistent height (40px minimum)
- Hover state (background highlight)
- Click/tap target ≥44px
- Icon + text combination
- Truncate long text with ellipsis

---

## Modals & Dialogs

### Modal Standards

| Property | Standard |
|----------|----------|
| Max Width | 500px for forms, 700px for content |
| Border Radius | 28px |
| Shadow | `theme.shadows.lg` |
| Padding | 32px |
| Backdrop | 40% black overlay |
| Close Button | Top right, always visible |

### Modal Rules

- ✅ Clear, descriptive titles
- ✅ Single primary action
- ✅ Close button (X) in top right
- ✅ Escape key closes modal
- ✅ Focus trap (keyboard navigation within modal)
- ✅ Scroll content, not modal
- ❌ Never open modal on page load
- ❌ Don't nest modals
- ❌ Avoid modals for simple confirmations (use dialogs)

---

## Responsive Design

### Breakpoints

```
Mobile: 0px - 640px
Tablet: 641px - 1024px
Desktop: 1025px+
```

### Rules

- ✅ Mobile-first approach
- ✅ Test on real devices
- ✅ Touch targets ≥44x44px
- ✅ Stack forms vertically on mobile
- ✅ Simplify on smaller screens
- ✅ Test with keyboard only
- ❌ Never hide essential content
- ❌ Avoid horizontal scroll

---

## Animation & Interaction

### Transition Standards

| Duration | Use Case |
|----------|----------|
| 150ms | Hover effects, opacity |
| 200ms | Default, fade in/out |
| 300ms | Larger movements |
| 500ms | Page transitions |

### Animation Rules

- ✅ Easing: `cubic-bezier(0.4, 0, 0.2, 1)` (Apple standard)
- ✅ Keep animations under 300ms (except page transitions)
- ✅ Provide `prefers-reduced-motion` support
- ✅ Use transforms for performance (translate, scale)
- ❌ Avoid autoplaying animations
- ❌ No more than 2 simultaneous animations
- ❌ Never disable on mobile (use CSS media queries)

### Interaction Feedback

```tsx
// Hover feedback
<Button 
  className="hover:bg-neutral-100 transition-colors duration-200"
/>

// Focus feedback
<Input 
  className="focus:ring-2 focus:ring-primary-300 focus:border-primary-600"
/>

// Active feedback
<Button 
  className="active:scale-95 transition-transform duration-150"
/>

// Disabled feedback
<Button 
  disabled
  className="opacity-50 cursor-not-allowed"
/>

// Loading feedback
{loading && <Spinner />}
```

---

## Accessibility (WCAG AA)

### Essential Checks

- [ ] Color contrast ≥4.5:1 (normal text)
- [ ] Focus indicators visible on all interactive elements
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Form labels properly associated
- [ ] Alt text on all images
- [ ] Semantic HTML (`<button>`, `<label>`, `<input>`)
- [ ] Error messages associated with fields
- [ ] No auto-playing sounds/videos
- [ ] Sufficient white space between targets

### Accessibility Code Examples

```tsx
// Proper label association
<label htmlFor="email">Email:</label>
<input id="email" type="email" />

// Semantic button
<button type="button">Delete</button>

// Alt text
<img src="document.png" alt="Document preview" />

// ARIA attributes when needed
<div role="alert" aria-live="polite">
  Error: Please fill all required fields
</div>

// Skip link
<a href="#main" className="sr-only">
  Skip to main content
</a>
```

---

## Common Violations to Avoid

| Violation | Impact | Fix |
|-----------|--------|-----|
| Hardcoded colors | Inconsistency, hard to theme | Use `theme.colors.*` |
| Missing focus states | Keyboard inaccessible | Add focus rings |
| Placeholder as label | Accessibility, UX | Use proper `<label>` |
| No error handling | User confusion | Show error messages |
| Loading state missing | Appears frozen | Add spinner/disable |
| Color alone for meaning | Not accessible | Add icons/text |
| No confirmation for destructive | Accidental loss | Require confirmation |
| Very long lines of text | Hard to read | Limit to 600px width |
| No mobile testing | Unusable on phone | Test responsive |
| Missing spacing | Cluttered | Use `theme.spacing` |

---

## Implementation Checklist

Every UI component must pass:

- [ ] Consistent with Apple design system
- [ ] Uses `theme` tokens exclusively
- [ ] Follows form/button/card patterns
- [ ] WCAG AA contrast and accessibility
- [ ] Responsive on mobile, tablet, desktop
- [ ] Keyboard navigation works
- [ ] Focus states visible
- [ ] Hover/active states applied
- [ ] Loading states handled
- [ ] Error states shown clearly
- [ ] 200ms transitions applied
- [ ] Spacing aligned to 8px grid
- [ ] Typography follows standards
- [ ] Tested in Chrome, Safari, Firefox

---

## References

- [Apple Design Guidelines](https://developer.apple.com/design/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Web Content Accessibility Guidelines](https://www.w3.org/WAI/fundamentals/)
- [Notaire Design System](../03-design/DESIGN-SYSTEM.md)
