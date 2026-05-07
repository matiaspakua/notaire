# Frontend Theme System

## Quick Start

```typescript
import { theme } from "@/theme/tokens";

// Access any design token
const color = theme.colors.primary[600];     // Apple blue
const padding = theme.spacing[4];             // 16px
const radius = theme.borderRadius.lg;         // 16px
```

## Theme Files

| File | Purpose |
|------|---------|
| `tokens.ts` | All design tokens (colors, typography, spacing, etc.) |
| `index.ts` | Utilities and React hooks for using tokens |
| `form-patterns.tsx` | Reusable form component patterns |
| `README.md` | This file |

## Key Concepts

### Design Tokens

Design tokens are **named values** that define the visual design system. Instead of hardcoding `#0071e3`, we use the semantic name `theme.colors.primary[600]`.

Token categories:
- **Colors**: Organized by purpose (primary, neutral, semantic)
- **Typography**: Fonts, sizes, weights, spacing
- **Spacing**: 8px base unit scale
- **Border Radius**: Apple-style rounded corners
- **Shadows**: Subtle to prominent
- **Transitions**: Duration and easing

### Semantic Tokens

Semantic tokens have **meaning** tied to UI purpose:
- `theme.semantic.form.inputBorder` — Color for form input borders
- `theme.semantic.button.primaryBg` — Background color for primary buttons
- `theme.semantic.status.error` — Color for error states

Use semantic tokens when available (they're more maintainable).

### Form Patterns

Reusable React components that apply the theme consistently:
- `FormContainer` — Wrapper for entire form
- `FormHeader` — Title and description
- `FormSection` — Group related fields
- `FormField` — Input + label + error + helper text
- `FormActions` — Action buttons (save, cancel)

## Usage Examples

### Colors

```typescript
import { theme } from "@/theme/tokens";

// Primary brand color
<div style={{ color: theme.colors.primary[600] }}>Important</div>

// Semantic color (preferred)
<div style={{ color: theme.semantic.status.error }}>Error message</div>

// Neutral text
<p style={{ color: theme.colors.neutral[900] }}>Body text</p>
```

### Spacing

```typescript
import { theme } from "@/theme/tokens";

<div style={{
  padding: theme.spacing[6],        // 24px
  marginBottom: theme.spacing[4],   // 16px
  gap: theme.spacing[3],            // 12px
}}>
  Content
</div>
```

### Typography

```typescript
import { theme } from "@/theme/tokens";

<h1 style={{
  fontSize: theme.typography.fontSize["2xl"],  // 24px
  fontWeight: theme.typography.fontWeight.bold, // 700
  fontFamily: theme.typography.fontFamily.display, // SF Pro Display
}}>
  Heading
</h1>
```

### Border Radius

```typescript
import { theme } from "@/theme/tokens";

<button style={{ borderRadius: theme.borderRadius.lg }}>
  Button
</button>
```

### Shadows

```typescript
import { theme } from "@/theme/tokens";

<div style={{
  boxShadow: theme.shadows.md,
  borderRadius: theme.borderRadius["2xl"],
  padding: theme.spacing[8],
}}>
  Card
</div>
```

### Forms

```typescript
import { 
  FormContainer, 
  FormField, 
  FormSection, 
  FormActions 
} from "@/theme/form-patterns";

export function UserForm() {
  return (
    <FormContainer>
      <FormSection title="Personal Information">
        <FormField label="Full Name" required>
          <input placeholder="Juan Pérez" />
        </FormField>

        <FormField label="Email" required error="Invalid email">
          <input type="email" placeholder="user@example.com" />
        </FormField>
      </FormSection>

      <FormActions align="right">
        <button>Cancel</button>
        <button>Save</button>
      </FormActions>
    </FormContainer>
  );
}
```

## Common Mistakes

### ❌ Hardcoded Colors
```typescript
// WRONG - hardcoded hex value
<div style={{ color: "#0071e3" }}>Text</div>

// RIGHT - use theme token
<div style={{ color: theme.colors.primary[600] }}>Text</div>
```

### ❌ Arbitrary Spacing
```typescript
// WRONG - random pixel value
<div style={{ padding: "18px" }}>Content</div>

// RIGHT - use theme spacing
<div style={{ padding: theme.spacing[4] }}>Content</div>
```

### ❌ No Label on Input
```typescript
// WRONG - placeholder only
<input placeholder="Enter name" />

// RIGHT - proper label
<FormField label="Name">
  <input placeholder="Juan Pérez" />
</FormField>
```

### ❌ Missing Error State
```typescript
// WRONG - no error feedback
<input type="email" />

// RIGHT - show validation errors
<FormField label="Email" error={error && "Invalid email"}>
  <input type="email" />
</FormField>
```

## Token Organization

### Colors - Neutral (Grays)

```
0   → #FFFFFF (white)
50  → #FBFBFD (almost white)
100 → #F5F5F7 (light gray)
...
900 → #1D1D1F (near black)
```

Use for:
- **0**: Backgrounds
- **50-100**: Very light backgrounds
- **200-400**: Borders, dividers
- **500-600**: Secondary text
- **700-900**: Primary text, headings

### Colors - Primary (Blue)

```
50  → #EBF4FF (very light)
100 → #D4E6FF (light)
...
600 → #0071E3 (Apple Blue - use this for primary actions)
700 → #0066D6 (darker for hover)
...
900 → #004BA0 (darkest)
```

### Colors - Semantic

```
success → #34C759 (green)
warning → #FF9500 (orange)
error   → #FF453A (red)
info    → #0A84FF (blue)
```

Use semantic colors for status indicators.

## Typography Hierarchy

### Headings

| Level | Font | Size | Weight |
|-------|------|------|--------|
| H1 | SF Pro Display | 30px | Bold |
| H2 | SF Pro Display | 24px | Semibold |
| H3 | SF Pro Display | 18px | Semibold |

### Body

| Type | Font | Size | Weight |
|------|------|------|--------|
| Body | SF Pro Text | 16px | Regular |
| Label | SF Pro Text | 14px | Semibold |
| Helper | SF Pro Text | 12px | Regular |
| Button | SF Pro Text | 14px | Semibold |

All use negative letter-spacing for Apple style: `-0.015em`

## Spacing Scale

```
0px   → 0
4px   → spacing[1]
8px   → spacing[2]
12px  → spacing[3]
16px  → spacing[4]
20px  → spacing[5]
24px  → spacing[6]
32px  → spacing[8]    ← Use for card padding
40px  → spacing[10]
48px  → spacing[12]
64px  → spacing[16]
80px  → spacing[20]
96px  → spacing[24]
```

### Recommended Spacings

- **Page padding**: `spacing[6]` (24px)
- **Card padding**: `spacing[8]` (32px)
- **Section gap**: `spacing[6]` (24px)
- **Form field gap**: `spacing[4]` (16px)
- **Label to input**: `spacing[2]` (8px)
- **Button spacing**: `spacing[3]` (12px)

## Border Radius

```
8px   → borderRadius.sm     (subtle)
12px  → borderRadius.md     (inputs)
16px  → borderRadius.lg     (buttons, cards)
24px  → borderRadius.xl     (large cards)
28px  → borderRadius["2xl"] (Apple style)
```

## React Hooks

### useTheme()

```typescript
import { useTheme } from "@/theme";

export function MyComponent() {
  const theme = useTheme();
  return <div style={{ color: theme.colors.primary[600] }}>Text</div>;
}
```

### useThemeClasses()

```typescript
import { useThemeClasses } from "@/theme";

export function MyComponent() {
  const themeClasses = useThemeClasses();
  return (
    <input 
      className={themeClasses("input", "focus")}
    />
  );
}
```

## Extending the Theme

### Adding a New Color

1. Open `tokens.ts`
2. Add to appropriate color category:

```typescript
export const colors = {
  // ... existing colors ...
  accent: {
    50: "#FFF3E0",
    500: "#FF9500",
    600: "#E68400",
  }
}
```

3. Update semantic tokens if needed:

```typescript
export const semantic = {
  // ...
  status: {
    // ...
    attention: colors.accent[500],
  }
}
```

4. Update documentation

### Adding a New Spacing Value

1. Open `tokens.ts`
2. Add to spacing:

```typescript
export const spacing = {
  // ... existing ...
  18: "4.5rem",  // 72px
}
```

3. Update documentation with use case

## Testing Your Theme

1. **Visual Consistency**: All forms look similar
2. **Responsive**: Works on 320px, 768px, 1024px widths
3. **Keyboard Navigation**: Tab, Enter, Escape work
4. **Focus States**: Visible on all interactive elements
5. **Color Contrast**: Verify 4.5:1 ratio (use axe, WAVE, or Contrast Checker)
6. **Dark Mode Ready**: Plan for dark mode compatibility

## Color Contrast Checker

Use these tools to verify WCAG AA compliance:

- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Chrome DevTools](https://developer.chrome.com/docs/devtools/)
- [axe DevTools](https://www.deque.com/axe/devtools/)
- [WAVE](https://wave.webaim.org/)

Minimum ratio: **4.5:1** for normal text, **3:1** for large text.

## Troubleshooting

### Colors Look Different in Different Browsers

**Solution**: Use CSS color values directly from tokens, not CSS variables if not supported.

### TypeScript Errors Accessing Tokens

**Solution**: Import type `Theme` if needed:

```typescript
import { theme, type Theme } from "@/theme/tokens";
```

### Token Values Change on Browser Zoom

**Solution**: Theme uses absolute values (px), which scale with browser zoom. This is expected.

### Dark Mode Not Working

**Solution**: Dark mode isn't implemented yet. Foundation is ready; add `.dark` selector in `globals.css`.

## Related Files

- `frontend/src/components/ui/` — UI components that should use theme
- `docs/02-architecture/03-design/DESIGN-SYSTEM.md` — Full design system documentation
- `.claude/rules/ui-ux-design.md` — Mandatory UI/UX rules
- `.claude/skills/frontend-design/SKILL.md` — Implementation patterns

## Support

For questions about the theme:

1. Check this README
2. Review `DESIGN-SYSTEM.md`
3. Look at existing form examples
4. Check `tokens.ts` for available values
5. Ask in team communications
