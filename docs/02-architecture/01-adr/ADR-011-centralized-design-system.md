# ADR-009: Centralized Token-Based Design System

**Status:** Accepted  
**Date:** May 2026  
**Deciders:** Frontend Team, Architecture Team  
**Affected By:** All frontend development  

## Context

The Notaire frontend previously had inconsistent styling across forms and pages. Components used hardcoded colors (hex values like `#0071e3`, `#86868b`), spacing values (various pixel amounts), and typography styles. This approach created several problems:

1. **Inconsistency**: Different forms applied different styling, making the application feel unprofessional
2. **Difficult Maintenance**: Changing colors or spacing required updates across many files
3. **Accessibility Issues**: Hard to verify color contrast or enforce WCAG AA standards
4. **Developer Experience**: New developers had to search existing code for "approved" colors and spacing values
5. **Scalability Issues**: Adding dark mode or themes was nearly impossible
6. **Code Duplication**: Similar styling patterns were repeated across many components

## Decision

We will implement a **centralized, token-based design system** inspired by Apple's design language (macOS Sequoia / iOS 18). All visual elements will be defined in a single location and referenced throughout the application.

### Implementation

**Token Files:**
- `frontend/src/theme/tokens.ts` — Core design tokens (colors, typography, spacing, shadows, etc.)
- `frontend/src/theme/index.ts` — Utilities and hooks for accessing tokens
- `frontend/src/theme/form-patterns.tsx` — Reusable form component patterns

**Design System Documentation:**
- `docs/02-architecture/03-design/DESIGN-SYSTEM.md` — Complete design system guide

**AI Agent Guidance:**
- `rules/ui-ux-design.md` — Mandatory UI/UX rules
- `skills/frontend-design/SKILL.md` — Implementation skill for agents

### Token Categories

1. **Colors**: Neutral scale (0-900), primary/secondary blues, semantic colors (success, error, warning, info)
2. **Typography**: SF Pro Display/Text, sizes, weights, line heights, letter spacing
3. **Spacing**: 8px base unit scale (4px to 96px)
4. **Border Radius**: 8px to 28px (Apple-style rounded corners)
5. **Shadows**: Subtle to prominent (Apple-style layering)
6. **Transitions**: Duration and easing functions
7. **Semantic Tokens**: Intent-based groupings for specific UI purposes

### Form Component Patterns

All forms must follow this structure:

```tsx
<FormContainer>
  <FormHeader />
  <FormSection title="...">
    <FormField label="...">
      <Input />
    </FormField>
  </FormSection>
  <FormActions>
    <Button />
  </FormActions>
</FormContainer>
```

## Consequences

### Positive

- ✅ **Consistency**: All forms automatically match the design language
- ✅ **Maintainability**: Change colors/spacing in one place; updates apply everywhere
- ✅ **Accessibility**: Easy to verify and enforce WCAG AA standards
- ✅ **Developer Experience**: Clear guidance on approved colors and styling
- ✅ **Scalability**: Dark mode, themes, and future customizations are straightforward
- ✅ **Professional Appearance**: Unified, Apple-inspired aesthetic
- ✅ **Reduced Code Duplication**: Reusable form patterns
- ✅ **AI Agent Support**: Clear rules and patterns for automated code generation

### Negative

- ⚠️ **Initial Migration Effort**: Existing components need updates
- ⚠️ **Learning Curve**: Developers must understand token structure
- ⚠️ **Enforcement Required**: Team discipline to avoid hardcoded values
- ⚠️ **Module Coupling**: All components depend on theme module

## Alternatives Considered

### 1. Continue With Ad-Hoc Styling

**Rejected**: No consistency, difficult maintenance, accessibility risks.

### 2. CSS-in-JS Library (Styled Components / Emotion)

**Rejected**: Over-engineered for current needs, adds build complexity, requires runtime overhead.

### 3. Tailwind CSS with Utility Classes

**Rejected**: Limited semantic meaning, encourages arbitrary combinations, harder to enforce consistency.

### 4. CSS Variables Only

**Rejected**: No TypeScript support, harder to organize large token sets, poor IDE support.

## Implementation Plan

### Phase 1: Foundation (Complete)
- ✅ Create `theme/tokens.ts` with all design tokens
- ✅ Create `theme/index.ts` with utilities
- ✅ Create `theme/form-patterns.tsx` with reusable components
- ✅ Document in design system guide
- ✅ Create UI/UX rules for agents

### Phase 2: Migration (In Progress)
- Update all existing form components to use tokens
- Apply form patterns to all form pages
- Update UI components (Button, Input, Card, etc.)
- Verify accessibility across all forms

### Phase 3: Standardization (Planned)
- Create component library with theme-aware defaults
- Update CI/CD to check for hardcoded colors
- Train team on design system
- Create design tokens in Figma (if using for design)

## Related Decisions

- **ADR-008**: Frontend Architecture (Next.js, React, TypeScript)
- **ADR-003**: REST API Design
- **ADR-007**: Database Migrations with Flyway

## Related Standards

- `.claude/rules/ui-ux-design.md` — Mandatory UI/UX rules
- `.claude/skills/frontend-design/SKILL.md` — Design system implementation skill
- `docs/02-architecture/03-design/DESIGN-SYSTEM.md` — Complete design system documentation

## Verification

The design system is verified through:

1. **Visual Consistency**: All forms use consistent colors, spacing, typography
2. **Accessibility Audit**: WCAG AA compliance (color contrast, keyboard navigation, focus states)
3. **Responsive Testing**: Forms work on 320px (mobile), 768px (tablet), 1024px+ (desktop)
4. **Code Review**: All frontend PRs verified to use tokens, not hardcoded values
5. **Automated Checks**: ESLint rules to warn about hardcoded colors (future implementation)

## Questions & Answers

**Q: Won't TypeScript strict typing cause problems?**  
A: No, TypeScript provides excellent IDE support and type safety for token values.

**Q: What if we need to override a token for a specific component?**  
A: Override thoughtfully and document why. For small variations, consider adding a new token instead.

**Q: How do we handle dark mode?**  
A: The token structure supports dark mode variants. Add a `.dark` selector in globals.css with inverted token values.

**Q: What if we add a new component that doesn't fit existing tokens?**  
A: Add new tokens to `tokens.ts` following the established structure. Update documentation.

**Q: Will this slow down development?**  
A: Initially, yes. Once tokens are established, development is faster due to consistency and reusable patterns.

## References

- [Apple Design Guidelines](https://developer.apple.com/design/)
- [Tokens Studio - Design Tokens](https://tokens.studio/)
- [Design Tokens for Designers and Developers](https://www.designtokens.org/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

## Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | May 2026 | Initial decision: Centralized token-based design system |
