# Frontend Form Implementation Checklist

Use this checklist when creating or updating any form in the Notaire frontend.

## Pre-Implementation

- [ ] **Design tokens established**: All needed colors, spacing, typography available in `theme/tokens.ts`
- [ ] **Form structure sketched**: Understand what sections, fields, and actions are needed
- [ ] **Wireframe reviewed**: Confirm layout with team
- [ ] **Accessibility requirements identified**: Any special WCAG requirements?

## Structure & Layout

- [ ] **Use FormContainer**: All form content wrapped in `<FormContainer>`
- [ ] **Use FormHeader**: Add title, subtitle, description for context
- [ ] **Use FormSection**: Group related fields logically
- [ ] **Sections separated**: Visual divider between sections (`borderTop`)
- [ ] **Responsive layout**: Content stacks vertically on mobile
- [ ] **Max width set**: Form content limited to 600px width for readability
- [ ] **Padding applied**: 24px page padding, 32px card padding using `theme.spacing`

## Form Fields

- [ ] **Proper labels**: Every input has a visible `<label>` with `htmlFor` attribute
- [ ] **Labels positioned**: Above inputs (not placeholder text)
- [ ] **Required indicator**: Required fields marked with * and `aria-required`
- [ ] **Helper text**: Complex fields have helper text explaining format (e.g., "DD/MM/YYYY")
- [ ] **Placeholder usage**: Placeholders are suggestions, not instructions
- [ ] **Input types correct**: Use `type="email"`, `type="tel"`, `type="number"`, etc.
- [ ] **Input styling**: Use `theme.semantic.form.*` tokens for borders, backgrounds
- [ ] **Focus indicators**: Visible focus ring on all inputs
- [ ] **Disabled state**: Clearly marked (reduced opacity, different color)
- [ ] **Hover state**: Subtle border color change on hover

## Validation & Error Handling

- [ ] **Validation on blur**: Check input validity when user leaves field
- [ ] **Real-time validation**: For complex fields (email, phone), validate as user types
- [ ] **Error display**: Errors shown below field in `theme.colors.error[600]`
- [ ] **Error association**: `aria-describedby` links errors to inputs
- [ ] **Clear error messages**: User understands what's wrong and how to fix it
- [ ] **Error icons**: Optional, use ⚠️ or similar for visual cue
- [ ] **Error persistence**: Errors clear when user fixes input
- [ ] **Form submission**: Prevents submit if validation fails
- [ ] **Success feedback**: Toast message or in-form confirmation after submission

## Buttons & Actions

- [ ] **Primary action identified**: Main action uses `variant="default"` (blue)
- [ ] **Alternative action**: Cancel/reset uses `variant="secondary"` (gray)
- [ ] **Destructive action**: Delete/remove uses `variant="destructive"` (red)
- [ ] **Button labels clear**: Action verbs (Create, Save, Delete, not Submit/Ok)
- [ ] **Button grouping**: Related buttons grouped together in `<FormActions>`
- [ ] **Button alignment**: Primary action aligned right, secondary to the left
- [ ] **Button size**: Use `lg` for main actions (48px), `md` for secondary (40px)
- [ ] **Disabled on submit**: Button disabled while submitting to prevent duplicates
- [ ] **Loading state**: Spinner or text indicating "Saving..." while submitting
- [ ] **Confirmation dialog**: Destructive actions require confirmation
- [ ] **Button focus**: Visible focus ring on all buttons
- [ ] **Button hover**: Subtle background change and scale (1.02x)

## Styling & Theming

### Colors

- [ ] **No hardcoded hex values**: All colors use `theme.colors.*` or `theme.semantic.*`
- [ ] **Primary color**: `theme.colors.primary[600]` for main actions, links
- [ ] **Text colors**: `theme.colors.neutral[900]` for body, `neutral[600]` for secondary
- [ ] **Border colors**: `theme.semantic.form.inputBorder` for form inputs
- [ ] **Error colors**: `theme.colors.error[500]` for error messages
- [ ] **Background colors**: `theme.colors.neutral[50]` for light backgrounds
- [ ] **Semantic colors**: Use `theme.semantic.form.*`, `theme.semantic.button.*`

### Spacing

- [ ] **No hardcoded pixel values**: All spacing uses `theme.spacing`
- [ ] **Consistent gaps**: 
  - Form field gap: `spacing[4]` (16px)
  - Section gap: `spacing[6]` (24px)
  - Card padding: `spacing[8]` (32px)
- [ ] **Label to input gap**: `spacing[2]` (8px)
- [ ] **Button spacing**: `spacing[3]` (12px) between buttons

### Typography

- [ ] **Font family used**: No custom fonts, uses `theme.typography.fontFamily`
- [ ] **Font sizes from scale**: Uses `theme.typography.fontSize`
- [ ] **Font weights consistent**: Uses `theme.typography.fontWeight`
- [ ] **Line height sufficient**: Uses `theme.typography.lineHeight` (min 1.5)
- [ ] **Letter spacing applied**: Headings use `-0.015em` for Apple style

### Border Radius

- [ ] **Input radius**: `theme.borderRadius.lg` (16px) or `md` (12px)
- [ ] **Button radius**: `theme.borderRadius.lg` (16px)
- [ ] **Card radius**: `theme.borderRadius["2xl"]` (28px) for premium style
- [ ] **No hardcoded radius**: All values from `theme.borderRadius`

### Shadows

- [ ] **Card shadow**: `theme.shadows.sm` for input groups, `theme.shadows.md` for cards
- [ ] **No excessive shadows**: Keep subtle, Apple-style (not harsh drop shadows)
- [ ] **Hover shadows**: Optional, subtle increase on hover

### Transitions

- [ ] **Duration applied**: Uses `theme.transitions.duration.base` (200ms) for state changes
- [ ] **Easing consistent**: Uses `theme.transitions.timing.ease` (Apple standard)
- [ ] **No excessive animations**: Keep under 300ms (except page transitions)
- [ ] **Hardware accelerated**: Use `transform`, `opacity` for performance

## Accessibility

- [ ] **Semantic HTML**: Uses `<form>`, `<label>`, `<input>`, `<button>` elements
- [ ] **Color contrast**: Text contrast ≥4.5:1 (verify with WebAIM, axe, WAVE)
- [ ] **Focus indicators**: Visible on all interactive elements
- [ ] **Keyboard navigation**: Tab through form, Enter to submit, Escape to cancel
- [ ] **Touch targets**: All interactive elements ≥44x44px
- [ ] **Form instructions**: Clear context for complex forms
- [ ] **Error association**: Error messages linked to inputs with `aria-describedby`
- [ ] **ARIA labels**: Use `aria-label` or `aria-labelledby` for clarity
- [ ] **No color alone**: Validation uses icons/text, not color only
- [ ] **Language**: Proper `lang` attribute on form (Spanish for Notaire: `lang="es"`)

## Responsiveness

- [ ] **Mobile (320px)**: Form is usable on small screens
- [ ] **Tablet (768px)**: Form optimized for tablet size
- [ ] **Desktop (1024px+)**: Form displays properly on large screens
- [ ] **Stack vertically**: Form fields stack on mobile, not side-by-side
- [ ] **Touch friendly**: Tap targets appropriately sized (48px+ on mobile)
- [ ] **Scrolling**: Content scrolls, not cut off on small screens
- [ ] **No horizontal scroll**: Form fits viewport width
- [ ] **Responsive spacing**: Adjust padding/gaps for screen size
- [ ] **Flexible inputs**: Inputs stretch to fill available width

## Testing

- [ ] **Visual comparison**: Form looks consistent with other forms in app
- [ ] **All browsers**: Chrome, Safari, Firefox tested
- [ ] **All devices**: Tested on phone, tablet, desktop (or browser dev tools)
- [ ] **Empty state**: Form works with no pre-filled data
- [ ] **Prefilled data**: Form works with existing data
- [ ] **Validation**: Error states display correctly
- [ ] **Success feedback**: Success message displays after submission
- [ ] **Loading state**: Loading indicator appears during submission
- [ ] **Keyboard only**: Navigate entire form using Tab, Enter, Escape
- [ ] **Screen reader**: Form works with screen readers (Windows Narrator, Mac VoiceOver, NVDA)
- [ ] **Accessibility audit**: Run axe DevTools or WAVE and fix issues

## Code Quality

- [ ] **No console errors**: Check browser console for errors/warnings
- [ ] **No TypeScript errors**: `npm run build` succeeds without errors
- [ ] **No linting errors**: No ESLint warnings
- [ ] **Code formatted**: Code follows project formatting standards
- [ ] **Comments added**: Complex logic is documented
- [ ] **Imports clean**: No unused imports, proper import order
- [ ] **No hardcoded strings**: Text strings are properly localized (i18n) if needed
- [ ] **Reusable components**: Extract repeated patterns into components

## Performance

- [ ] **No layout shift**: Content doesn't jump around during load
- [ ] **Smooth scrolling**: Form scrolls smoothly (no jank)
- [ ] **Fast interactions**: Inputs respond immediately to user input
- [ ] **Images optimized**: Any images are WebP with fallback
- [ ] **No memory leaks**: Cleanup subscriptions/timers in cleanup function
- [ ] **Debounced validation**: Real-time validation debounced to avoid excessive checks
- [ ] **No unnecessary renders**: Avoid re-rendering entire form on small changes

## Documentation

- [ ] **JSDoc comments**: Component documented with purpose and props
- [ ] **Usage examples**: Show how to use the form component
- [ ] **Props documented**: All props explained with types
- [ ] **Component exported**: Form exported from index file for easy import
- [ ] **Storybook story** (optional): Add Storybook story for form if applicable
- [ ] **README updated**: Add form to component documentation

## Security

- [ ] **XSS prevention**: No `dangerouslySetInnerHTML` or unsanitized user input
- [ ] **CSRF protection**: API calls include CSRF tokens if required
- [ ] **No secrets**: No API keys or credentials hardcoded
- [ ] **Input sanitization**: User input sanitized before API submission
- [ ] **HTTPS only**: All API calls use HTTPS
- [ ] **No logging secrets**: Don't log passwords, tokens, or sensitive data

## Deployment Readiness

- [ ] **Branch created**: Feature branch created with proper naming: `feat/###_form_name`
- [ ] **Tests written**: Unit tests for form validation logic
- [ ] **Tests passing**: All tests pass locally: `npm test`
- [ ] **Build successful**: Build succeeds: `npm run build`
- [ ] **No console logs**: Remove debug `console.log` statements
- [ ] **Commit messages clear**: Conventional commits with issue reference
- [ ] **PR created**: Pull request created with description linking to issue
- [ ] **Code reviewed**: Code reviewed by teammate
- [ ] **Conflicts resolved**: No merge conflicts
- [ ] **Mergeable**: Ready to merge to main

## Post-Deployment

- [ ] **Tested in production**: Form works correctly in production
- [ ] **Analytics tracked**: Form interactions tracked (if applicable)
- [ ] **Errors monitored**: Monitor error logs for form submission errors
- [ ] **Performance monitored**: Monitor page load/interaction metrics
- [ ] **User feedback collected**: Gather user feedback on form UX
- [ ] **Issues addressed**: Fix any reported issues quickly

---

## Quick Checklist (TL;DR)

- [ ] ✅ Use `FormContainer` → `FormSection` → `FormField` → `FormActions`
- [ ] ✅ All colors from `theme.colors.*` or `theme.semantic.*`
- [ ] ✅ All spacing from `theme.spacing` array
- [ ] ✅ All typography from `theme.typography.*`
- [ ] ✅ Proper labels on all inputs (no placeholder labels)
- [ ] ✅ Error messages shown below fields
- [ ] ✅ Button disabled during submission
- [ ] ✅ Focus indicators visible
- [ ] ✅ Color contrast ≥4.5:1
- [ ] ✅ Keyboard navigation works
- [ ] ✅ Responsive on 320px, 768px, 1024px
- [ ] ✅ Tests passing, build successful
- [ ] ✅ No hardcoded values (use theme tokens)

---

## Common Patterns

### Basic Form

```tsx
import { FormContainer, FormField, FormSection, FormActions } from "@/theme/form-patterns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function MyForm() {
  const [formData, setFormData] = useState({ name: "", email: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate
    const newErrors: Record<string, string> = {};
    if (!formData.name) newErrors.name = "Required";
    if (!formData.email.includes("@")) newErrors.email = "Invalid email";
    
    if (Object.keys(newErrors).length) {
      setErrors(newErrors);
      return;
    }

    // Submit
    setLoading(true);
    try {
      await apiPost("/endpoint", formData);
      toast.success("Saved!");
    } catch (error) {
      toast.error("Failed to save");
    } finally {
      setLoading(false);
    }
  };

  return (
    <FormContainer>
      <FormSection title="Information">
        <FormField label="Name" required error={errors.name}>
          <Input 
            name="name" 
            value={formData.name}
            onChange={handleChange}
            placeholder="Juan Pérez"
          />
        </FormField>
        <FormField label="Email" required error={errors.email}>
          <Input 
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="user@example.com"
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
  );
}
```

### With Validation Feedback

```tsx
<FormField 
  label="Email"
  required
  error={errors.email}
  helperText="We'll send a confirmation to this address"
>
  <Input 
    type="email"
    value={email}
    onChange={(e) => setEmail(e.target.value)}
    placeholder="user@example.com"
  />
</FormField>
```

### With Select Dropdown

```tsx
<FormField label="Document Type" required>
  <select value={type} onChange={(e) => setType(e.target.value)}>
    <option value="">Select one...</option>
    <option value="sale">Escritura de Venta</option>
    <option value="power">Poder Notarial</option>
  </select>
</FormField>
```

---

**Last Updated:** May 2026  
**Version:** 1.0  
**Maintained By:** Frontend Team
