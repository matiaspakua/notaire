/**
 * Apple-Inspired Design Tokens
 * 
 * Centralized theme system for Notaire Frontend
 * Based on macOS Sequoia / iOS 18 design language
 * 
 * All UI components, forms, and pages should reference these tokens exclusively.
 * Never hardcode colors, spacing, or other design values.
 */

// ============================================================================
// COLOR PALETTE
// ============================================================================

export const colors = {
  // Neutrals - Apple San Francisco palette
  neutral: {
    0: "#FFFFFF",     // Pure white
    50: "#FBFBFD",    // Ultra light gray (background)
    100: "#F5F5F7",   // Light gray
    200: "#EBEBF0",   // Light gray
    300: "#E5E5EA",   // Light gray
    400: "#D2D2D7",   // Gray
    500: "#A2A2A6",   // Medium gray
    600: "#86868B",   // Dark gray
    700: "#5A5A5F",   // Darker gray
    800: "#3A3A3C",   // Very dark gray
    900: "#1D1D1F",   // Almost black (foreground)
  },

  // Apple Blue (Primary) - San Francisco Blue
  primary: {
    50: "#EBF4FF",
    100: "#D4E6FF",
    200: "#A3CDFF",
    300: "#72B4FF",
    400: "#419BFF",
    500: "#0A84FF", // Apple blue standard
    600: "#0071E3", // Apple blue dark
    700: "#0066D6",
    800: "#005CC2",
    900: "#004BA0",
  },

  // Semantic colors
  success: {
    50: "#E8F6EB",
    100: "#D1ECDA",
    500: "#34C759", // Apple green
    600: "#30B14B",
  },

  warning: {
    50: "#FFF8E6",
    100: "#FFF0CC",
    500: "#FF9500", // Apple orange
    600: "#E68400",
  },

  error: {
    50: "#FFE8E8",
    100: "#FFD1D1",
    500: "#FF453A", // Apple red
    600: "#FF3B30",
  },

  info: {
    50: "#E8F4FF",
    100: "#D1E8FF",
    500: "#0A84FF", // Apple blue
    600: "#0071E3",
  },
} as const;

// ============================================================================
// TYPOGRAPHY
// ============================================================================

export const typography = {
  // Font families - Apple system fonts
  fontFamily: {
    display: "\"SF Pro Display\", \"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
    body: "\"SF Pro Text\", -apple-system, BlinkMacSystemFont, system-ui, sans-serif",
    mono: "\"Menlo\", \"Monaco\", \"Courier New\", monospace",
  },

  // Font sizes - iOS/macOS scale
  fontSize: {
    xs: "0.75rem",    // 12px
    sm: "0.875rem",   // 14px
    base: "1rem",     // 16px
    lg: "1.125rem",   // 18px
    xl: "1.25rem",    // 20px
    "2xl": "1.5rem",  // 24px
    "3xl": "1.875rem", // 30px
    "4xl": "2.25rem", // 36px
  },

  // Font weights - Apple's recommended weights
  fontWeight: {
    regular: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },

  // Line heights - Apple standard
  lineHeight: {
    tight: 1.2,
    snug: 1.375,
    normal: 1.5,
    relaxed: 1.625,
    loose: 2,
  },

  // Letter spacing - Apple's negative tracking
  letterSpacing: {
    tight: "-0.015em",
    normal: "-0.01em",
    relaxed: "0em",
  },
} as const;

// ============================================================================
// SPACING
// ============================================================================

export const spacing = {
  0: "0",
  1: "0.25rem",   // 4px
  2: "0.5rem",    // 8px
  3: "0.75rem",   // 12px
  4: "1rem",      // 16px
  5: "1.25rem",   // 20px
  6: "1.5rem",    // 24px
  8: "2rem",      // 32px
  10: "2.5rem",   // 40px
  12: "3rem",     // 48px
  16: "4rem",     // 64px
  20: "5rem",     // 80px
  24: "6rem",     // 96px
} as const;

// ============================================================================
// BORDER RADIUS
// ============================================================================

export const borderRadius = {
  none: "0",
  sm: "0.5rem",     // 8px
  md: "0.75rem",    // 12px
  lg: "1rem",       // 16px
  xl: "1.5rem",     // 24px
  "2xl": "1.75rem", // 28px (Apple cards)
  full: "9999px",
} as const;

// ============================================================================
// SHADOWS
// ============================================================================

export const shadows = {
  // Subtle Apple-style shadows
  sm: "0 0 0 1px rgba(0,0,0,0.06), 0 2px 4px rgba(0,0,0,0.04), 0 8px 16px rgba(0,0,0,0.04)",
  md: "0 0 0 1px rgba(0,0,0,0.06), 0 4px 8px rgba(0,0,0,0.06), 0 16px 32px rgba(0,0,0,0.08)",
  lg: "0 0 0 1px rgba(0,0,0,0.06), 0 8px 16px rgba(0,0,0,0.08), 0 32px 64px rgba(0,0,0,0.12)",
  xl: "0 0 0 1px rgba(0,0,0,0.06), 0 16px 32px rgba(0,0,0,0.1), 0 64px 128px rgba(0,0,0,0.16)",
  none: "none",
} as const;

// ============================================================================
// TRANSITIONS & ANIMATIONS
// ============================================================================

export const transitions = {
  duration: {
    instant: "0ms",
    fast: "150ms",
    base: "200ms",
    slow: "300ms",
    slower: "500ms",
  },
  timing: {
    // Apple's standard easing function
    ease: "cubic-bezier(0.4, 0, 0.2, 1)",
    easeInOut: "cubic-bezier(0.4, 0, 0.2, 1)",
    easeOut: "cubic-bezier(0, 0, 0.2, 1)",
    easeIn: "cubic-bezier(0.4, 0, 1, 1)",
  },
} as const;

// ============================================================================
// COMPONENT SIZES
// ============================================================================

export const sizes = {
  button: {
    sm: {
      height: "2rem",      // 32px
      padding: "0 0.75rem", // 0 12px
      fontSize: typography.fontSize.xs,
      borderRadius: borderRadius.md,
    },
    md: {
      height: "2.5rem",    // 40px
      padding: "0 1.25rem", // 0 20px
      fontSize: typography.fontSize.sm,
      borderRadius: borderRadius.lg,
    },
    lg: {
      height: "3rem",      // 48px
      padding: "0 1.5rem", // 0 24px
      fontSize: typography.fontSize.base,
      borderRadius: borderRadius.lg,
    },
  },
  input: {
    height: "3rem",        // 48px
    padding: "0 1rem",     // 0 16px
    borderRadius: borderRadius.md,
    fontSize: typography.fontSize.base,
    borderColor: colors.neutral[400],
    borderWidth: "1px",
  },
  card: {
    borderRadius: borderRadius["2xl"],
    padding: spacing[8],
    gap: spacing[4],
  },
  workflowViewer: {
    height: "27.5rem", // 440px
  },
  workflowEditor: {
    height: "32.5rem", // 520px
  },
} as const;

// ============================================================================
// Z-INDEX SCALE
// ============================================================================

export const zIndex = {
  hide: -1,
  auto: 0,
  base: 0,
  dropdown: 1000,
  sticky: 1020,
  fixed: 1030,
  modalBackdrop: 1040,
  modal: 1050,
  popover: 1060,
  tooltip: 1070,
} as const;

// ============================================================================
// SEMANTIC TOKENS (Intent-based)
// ============================================================================

export const semantic = {
  // Form inputs
  form: {
    inputBg: colors.neutral[0],
    inputBorder: colors.neutral[400],
    inputBorderHover: colors.neutral[500],
    inputBorderFocus: colors.primary[600],
    inputText: colors.neutral[900],
    inputPlaceholder: colors.neutral[600],
    inputDisabledBg: colors.neutral[100],
    inputDisabledText: colors.neutral[500],
    labelText: colors.neutral[900],
    labelSecondary: colors.neutral[600],
    errorBg: colors.error[50],
    errorBorder: colors.error[500],
    errorText: colors.error[600],
    helperText: colors.neutral[600],
  },

  // Buttons
  button: {
    primaryBg: colors.primary[600],
    primaryText: colors.neutral[0],
    primaryHover: colors.primary[500],
    primaryActive: colors.primary[700],
    secondaryBg: colors.neutral[100],
    secondaryText: colors.primary[600],
    secondaryHover: colors.neutral[200],
    ghostText: colors.primary[600],
    ghostHover: colors.neutral[100],
    disabledBg: colors.neutral[100],
    disabledText: colors.neutral[500],
  },

  // Cards & Containers
  card: {
    bg: colors.neutral[0],
    border: colors.neutral[300],
    shadow: shadows.sm,
  },

  // Overlays
  overlay: {
    bgLight: "rgba(0, 0, 0, 0.4)",
    bgDark: "rgba(0, 0, 0, 0.7)",
  },

  // Status
  status: {
    success: colors.success[500],
    warning: colors.warning[500],
    error: colors.error[500],
    info: colors.info[500],
  },
} as const;

// ============================================================================
// EXPORT COMPLETE THEME OBJECT
// ============================================================================

export const theme = {
  colors,
  typography,
  spacing,
  borderRadius,
  shadows,
  transitions,
  sizes,
  zIndex,
  semantic,
} as const;

export type Theme = typeof theme;
