# Frontend Design System - Quick Reference

**Use this as a bookmark or print for quick access.**

## Essential Files

| File | Purpose |
|------|---------|
| `frontend/src/theme/tokens.ts` | Design tokens (colors, spacing, typography) |
| `frontend/src/theme/form-patterns.tsx` | Form components (FormContainer, FormField, etc.) |
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Full documentation |
| `.claude/rules/ui-ux-design.md` | Mandatory rules |

## Form Template

```tsx
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { theme } from "@/theme/tokens";

export function MyForm() {
  const [data, setData] = useState({ field: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await apiPost("/endpoint", data);
      toast.success("Saved!");
    } catch {
      toast.error("Failed");
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

## Colors (Most Used)

```typescript
// Import
import { theme } from "@/theme/tokens";

// Text colors
theme.colors.neutral[900]           // Body text (dark)
theme.colors.neutral[600]           // Secondary text (medium gray)

// Action colors
theme.colors.primary[600]           // Primary button, links
theme.colors.error[500]             // Errors, destructive
theme.colors.success[500]           // Success messages

// Form colors
theme.semantic.form.inputBorder     // Input borders
theme.semantic.form.inputBg         // Input background (white)
theme.semantic.form.labelText       // Label text

// Button colors
theme.semantic.button.primaryBg     // Primary button background
theme.semantic.button.secondaryBg   // Secondary button background
```

## Spacing (Most Used)

```typescript
theme.spacing[0]    // 0px
theme.spacing[1]    // 4px
theme.spacing[2]    // 8px   (label-to-input gap)
theme.spacing[3]    // 12px  (button spacing)
theme.spacing[4]    // 16px  (form field gap)
theme.spacing[6]    // 24px  (section gap, page padding)
theme.spacing[8]    // 32px  (card padding)
```

## Border Radius

```typescript
theme.borderRadius.md      // 12px (inputs)
theme.borderRadius.lg      // 16px (buttons)
theme.borderRadius["2xl"]  // 28px (cards - Apple style)
```

## Component Sizing

| Component | Height | Border Radius | Padding |
|-----------|--------|---------------|---------|
| Input | 48px | 12px | 16px horizontal |
| Button (md) | 40px | 12px | 12-20px horizontal |
| Button (lg) | 48px | 12px | 20px horizontal |
| Card | auto | 28px | 32px |

## Form Patterns

### Input with Label and Error

```tsx
<FormField label="Email" required error={errors.email}>
  <Input 
    type="email"
    placeholder="user@example.com"
    value={email}
    onChange={(e) => setEmail(e.target.value)}
  />
</FormField>
```

### With Helper Text

```tsx
<FormField 
  label="Password"
  helperText="At least 8 characters"
  required
>
  <Input type="password" />
</FormField>
```

### Select/Dropdown

```tsx
<FormField label="Type" required>
  <select value={type} onChange={(e) => setType(e.target.value)}>
    <option value="">Select...</option>
    <option value="a">Option A</option>
    <option value="b">Option B</option>
  </select>
</FormField>
```

### Checkbox

```tsx
<CheckboxField 
  label="I agree"
  checked={agree}
  onChange={setAgree}
/>
```

### Radio Group

```tsx
<RadioField
  label="Status"
  selected={status}
  onChange={setStatus}
  options={[
    { value: "draft", label: "Draft" },
    { value: "done", label: "Done" },
  ]}
/>
```

## Button Variants

```tsx
// Primary (blue)
<Button variant="default">Save</Button>

// Secondary (gray)
<Button variant="secondary">Cancel</Button>

// Destructive (red)
<Button variant="destructive">Delete</Button>

// Ghost (no fill)
<Button variant="ghost">Learn More</Button>

// Link style
<Button variant="link">Skip</Button>
```

## Transitions

```typescript
// Duration (milliseconds)
theme.transitions.duration.fast    // 150ms
theme.transitions.duration.base    // 200ms (standard)
theme.transitions.duration.slow    // 300ms

// Easing (Apple standard)
theme.transitions.timing.ease      // cubic-bezier(0.4, 0, 0.2, 1)
```

## Typography Hierarchy

| Element | Font | Size | Weight |
|---------|------|------|--------|
| Page Title (H1) | SF Pro Display | 30px | Bold |
| Section (H2) | SF Pro Display | 24px | Semibold |
| Subsection (H3) | SF Pro Display | 18px | Semibold |
| Body | SF Pro Text | 16px | Regular |
| Label | SF Pro Text | 14px | Semibold |
| Helper Text | SF Pro Text | 12px | Regular |

## Shadows

```typescript
theme.shadows.sm    // Inputs, small components
theme.shadows.md    // Cards, containers
theme.shadows.lg    // Modals, elevated
theme.shadows.xl    // Maximum elevation
```

## Important Rules (❌ Don't Forget)

### ❌ Never Hardcode
```typescript
// ❌ WRONG
color: "#0071e3"
padding: "16px"
borderRadius: "12px"

// ✅ RIGHT
color: theme.colors.primary[600]
padding: theme.spacing[4]
borderRadius: theme.borderRadius.lg
```

### ❌ Never Use Placeholder as Label
```typescript
// ❌ WRONG
<input placeholder="Enter name" />

// ✅ RIGHT
<FormField label="Name">
  <Input placeholder="Juan Pérez" />
</FormField>
```

### ❌ Never Skip Validation Feedback
```typescript
// ❌ WRONG - silent error
if (!email) return;

// ✅ RIGHT - show error to user
<FormField label="Email" error={errors.email}>
  <Input />
</FormField>
```

### ❌ Never Forget Loading State
```typescript
// ❌ WRONG - no indication while saving
<Button onClick={handleSubmit}>Save</Button>

// ✅ RIGHT - disable and show loading
<Button disabled={loading} onClick={handleSubmit}>
  {loading ? "Saving..." : "Save"}
</Button>
```

### ❌ Never Skip Accessibility
```typescript
// ❌ WRONG - no label association
<input id="email" />

// ✅ RIGHT - proper label
<label htmlFor="email">Email</label>
<input id="email" type="email" />
```

## Color Contrast

**Requirement**: 4.5:1 minimum (WCAG AA)

**Check with:**
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- Chrome DevTools
- axe DevTools
- WAVE browser extension

## Responsive Breakpoints

```typescript
// Mobile (test at 320px - 640px)
// Form stacks vertically, tap targets 44px+

// Tablet (641px - 1024px)
// Form optimized for medium screens

// Desktop (1025px+)
// Full form with max width ~600px for readability
```

## React Hooks

### useTheme()
```typescript
import { useTheme } from "@/theme";

const theme = useTheme();
const color = theme.colors.primary[600];
```

### useThemeClasses()
```typescript
import { useThemeClasses } from "@/theme";

const themeClasses = useThemeClasses();
const buttonClass = themeClasses("button", "hover");
```

## Common Spacing Patterns

```typescript
// Page layout
padding: theme.spacing[6]           // 24px page padding

// Card layout
padding: theme.spacing[8]           // 32px card padding

// Form section
gap: theme.spacing[6]               // 24px between sections

// Form fields
gap: theme.spacing[4]               // 16px between fields

// Label to input
gap: theme.spacing[2]               // 8px label to input

// Button group
gap: theme.spacing[3]               // 12px between buttons
```

## Validation Pattern

```typescript
const [errors, setErrors] = useState<Record<string, string>>({});

const validate = () => {
  const newErrors: Record<string, string> = {};
  if (!name) newErrors.name = "Required";
  if (!email.includes("@")) newErrors.email = "Invalid email";
  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  if (!validate()) return;
  // Proceed with submission
};

// In JSX
<FormField label="Name" required error={errors.name}>
  <Input value={name} onChange={(e) => setName(e.target.value)} />
</FormField>
```

## API Call Pattern

```typescript
import { apiPost } from "@/lib/api-client";
import { toast } from "sonner";

const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);
  try {
    const result = await apiPost("/endpoint", formData);
    toast.success("Saved!");
    // Handle success
  } catch (error) {
    toast.error("Failed to save. Please try again.");
    // Handle error
  } finally {
    setLoading(false);
  }
};
```

## Typography Usage

```typescript
import { theme } from "@/theme/tokens";

// Heading
<h2 style={{
  fontSize: theme.typography.fontSize["2xl"],
  fontWeight: theme.typography.fontWeight.semibold,
  fontFamily: theme.typography.fontFamily.display,
}}>
  Title
</h2>

// Body
<p style={{
  fontSize: theme.typography.fontSize.base,
  fontFamily: theme.typography.fontFamily.body,
  lineHeight: theme.typography.lineHeight.relaxed,
}}>
  Content
</p>

// Label
<label style={{
  fontSize: theme.typography.fontSize.sm,
  fontWeight: theme.typography.fontWeight.semibold,
}}>
  Label
</label>
```

## Checklist - Before Submitting PR

- [ ] No hardcoded colors (use theme.colors)
- [ ] No hardcoded spacing (use theme.spacing)
- [ ] All inputs have labels
- [ ] Errors shown with color (#FF453A) and icon
- [ ] Button disabled while submitting
- [ ] Loading state displays
- [ ] Focus indicators visible
- [ ] Color contrast ≥4.5:1
- [ ] Form responsive (320px, 768px, 1024px)
- [ ] Tests passing
- [ ] Build succeeds

## Quick Links

- **Design System**: `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- **Theme Source**: `frontend/src/theme/tokens.ts`
- **Form Patterns**: `frontend/src/theme/form-patterns.tsx`
- **UI/UX Rules**: `.claude/rules/ui-ux-design.md`
- **Implementation Skill**: `.claude/skills/frontend-design/SKILL.md`
- **Form Checklist**: `docs/200-architecture/203-design/FRONTEND-FORM-IMPLEMENTATION-CHECKLIST.md`

---

**Print this page or bookmark it for quick reference!**

Last Updated: May 2026 | Version: 1.0
