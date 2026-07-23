/**
 * Theme Utilities & Hooks
 * 
 * Provides helper functions and React hooks for using the centralized theme
 * throughout the application.
 */

import { useCallback } from "react";
import { theme, type Theme } from "./tokens";

/**
 * Hook to access the current theme object
 * @returns The complete theme object
 */
export function useTheme(): Theme {
  return theme;
}

/**
 * Get a color from the theme by path
 * @param path - Dot-notation path to the color (e.g., "colors.primary.600")
 * @returns The color value
 */
export function getThemeColor(path: string): string {
  const keys = path.split(".");
  let value: unknown = theme;
  for (const key of keys) {
    value = (value as Record<string, unknown> | undefined)?.[key];
  }
  return typeof value === "string" ? value : "";
}

/**
 * Build a complete CSS variable string from theme values
 * Used to apply theme to global CSS
 */
export function generateCSSVariables(): string {
  return `
    /* Colors - Neutrals */
    --neutral-0: ${theme.colors.neutral[0]};
    --neutral-50: ${theme.colors.neutral[50]};
    --neutral-100: ${theme.colors.neutral[100]};
    --neutral-200: ${theme.colors.neutral[200]};
    --neutral-300: ${theme.colors.neutral[300]};
    --neutral-400: ${theme.colors.neutral[400]};
    --neutral-500: ${theme.colors.neutral[500]};
    --neutral-600: ${theme.colors.neutral[600]};
    --neutral-700: ${theme.colors.neutral[700]};
    --neutral-800: ${theme.colors.neutral[800]};
    --neutral-900: ${theme.colors.neutral[900]};

    /* Colors - Primary */
    --primary-50: ${theme.colors.primary[50]};
    --primary-100: ${theme.colors.primary[100]};
    --primary-200: ${theme.colors.primary[200]};
    --primary-300: ${theme.colors.primary[300]};
    --primary-400: ${theme.colors.primary[400]};
    --primary-500: ${theme.colors.primary[500]};
    --primary-600: ${theme.colors.primary[600]};
    --primary-700: ${theme.colors.primary[700]};
    --primary-800: ${theme.colors.primary[800]};
    --primary-900: ${theme.colors.primary[900]};

    /* Colors - Semantic */
    --success: ${theme.colors.success[500]};
    --warning: ${theme.colors.warning[500]};
    --error: ${theme.colors.error[500]};
    --info: ${theme.colors.info[500]};

    /* Typography */
    --font-display: ${theme.typography.fontFamily.display};
    --font-body: ${theme.typography.fontFamily.body};
    --font-mono: ${theme.typography.fontFamily.mono};

    /* Spacing */
    --spacing-0: ${theme.spacing[0]};
    --spacing-1: ${theme.spacing[1]};
    --spacing-2: ${theme.spacing[2]};
    --spacing-3: ${theme.spacing[3]};
    --spacing-4: ${theme.spacing[4]};
    --spacing-6: ${theme.spacing[6]};
    --spacing-8: ${theme.spacing[8]};

    /* Border Radius */
    --radius-md: ${theme.borderRadius.md};
    --radius-lg: ${theme.borderRadius.lg};
    --radius-xl: ${theme.borderRadius.xl};
    --radius-2xl: ${theme.borderRadius["2xl"]};

    /* Shadows */
    --shadow-sm: ${theme.shadows.sm};
    --shadow-md: ${theme.shadows.md};
    --shadow-lg: ${theme.shadows.lg};

    /* Transitions */
    --transition-duration: ${theme.transitions.duration.base};
    --transition-timing: ${theme.transitions.timing.ease};
  `;
}

/**
 * Create a style string for a component using theme values
 * Helper for creating styled components with theme tokens
 */
export const themeStyles = {
  /**
   * Get button style properties
   */
  button: (variant: "primary" | "secondary" | "ghost" = "primary") => {
    const styles = {
      primary: {
        backgroundColor: theme.semantic.button.primaryBg,
        color: theme.semantic.button.primaryText,
        borderRadius: theme.sizes.button.md.borderRadius,
        padding: theme.sizes.button.md.padding,
        fontSize: theme.sizes.button.md.fontSize,
        fontWeight: theme.typography.fontWeight.semibold,
        border: "none",
        cursor: "pointer",
        transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
      },
      secondary: {
        backgroundColor: theme.semantic.button.secondaryBg,
        color: theme.semantic.button.secondaryText,
        borderRadius: theme.sizes.button.md.borderRadius,
        padding: theme.sizes.button.md.padding,
        fontSize: theme.sizes.button.md.fontSize,
        fontWeight: theme.typography.fontWeight.semibold,
        border: `1px solid ${theme.colors.neutral[300]}`,
        cursor: "pointer",
        transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
      },
      ghost: {
        backgroundColor: "transparent",
        color: theme.semantic.button.ghostText,
        borderRadius: theme.sizes.button.md.borderRadius,
        padding: theme.sizes.button.md.padding,
        fontSize: theme.sizes.button.md.fontSize,
        fontWeight: theme.typography.fontWeight.semibold,
        border: "none",
        cursor: "pointer",
        transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
      },
    };
    return styles[variant];
  },

  /**
   * Get input style properties
   */
  input: () => ({
    backgroundColor: theme.semantic.form.inputBg,
    color: theme.semantic.form.inputText,
    borderRadius: theme.sizes.input.borderRadius,
    padding: theme.sizes.input.padding,
    fontSize: theme.sizes.input.fontSize,
    height: theme.sizes.input.height,
    border: `${theme.sizes.input.borderWidth} solid ${theme.semantic.form.inputBorder}`,
    fontFamily: theme.typography.fontFamily.body,
    transition: `all ${theme.transitions.duration.base} ${theme.transitions.timing.ease}`,
  }),

  /**
   * Get card style properties
   */
  card: () => ({
    backgroundColor: theme.semantic.card.bg,
    borderRadius: theme.sizes.card.borderRadius,
    padding: theme.sizes.card.padding,
    border: `1px solid ${theme.semantic.card.border}`,
    boxShadow: theme.semantic.card.shadow,
  }),

  /**
   * Get label style properties
   */
  label: () => ({
    color: theme.semantic.form.labelText,
    fontSize: theme.typography.fontSize.sm,
    fontWeight: theme.typography.fontWeight.semibold,
    fontFamily: theme.typography.fontFamily.body,
    letterSpacing: theme.typography.letterSpacing.normal,
  }),
} as const;

/**
 * Tailwind class builder using theme tokens
 * Use when you need theme-aware class names
 */
export function useThemeClasses() {
  return useCallback((component: string, state?: string): string => {
    const baseClasses: Record<string, string> = {
      button:
        "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-semibold transition-all duration-200 apple-button focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 active:scale-95",
      input:
        "flex h-12 w-full rounded-lg border border-neutral-400 bg-white px-4 py-2.5 text-base transition-all duration-200 file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-neutral-600/60 apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-neutral-500",
      card: "rounded-2xl border border-neutral-300 bg-white text-card-foreground apple-shadow",
      label: "text-xs font-semibold uppercase tracking-wider text-neutral-600",
      textarea:
        "flex min-h-[100px] w-full rounded-lg border border-neutral-400 bg-white px-4 py-2.5 text-base transition-all duration-200 placeholder:text-neutral-600/60 apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-neutral-500",
    };

    const stateModifiers: Record<string, string> = {
      hover: "hover:bg-neutral-100 hover:border-neutral-500",
      focus: "focus:border-primary-600 focus:ring-2 focus:ring-primary-300",
      disabled: "disabled:bg-neutral-100 disabled:text-neutral-500",
      error: "border-error-500 focus:ring-error-300",
    };

    let classes = baseClasses[component] || "";
    if (state && stateModifiers[state]) {
      classes += ` ${stateModifiers[state]}`;
    }
    return classes;
  }, []);
}
