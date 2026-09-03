/**
 * Form Component Patterns using Apple Theme
 * 
 * This file provides reusable patterns for building forms that consistently
 * apply the Apple design system across all pages.
 * 
 * All form components should use these patterns.
 */

import { ReactNode } from "react";
import { theme } from "./tokens";

/**
 * Form wrapper - provides consistent spacing and layout
 */
export function FormContainer({ children }: { children: ReactNode }) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: theme.spacing[6],
        maxWidth: "100%",
      }}
    >
      {children}
    </div>
  );
}

/**
 * Form field group - input + label + error + helper text
 */
export function FormField({
  label,
  error,
  helperText,
  required,
  children,
  layout = "vertical",
}: {
  label?: string;
  error?: string;
  helperText?: string;
  required?: boolean;
  children: ReactNode;
  layout?: "vertical" | "horizontal";
}) {
  const isVertical = layout === "vertical";

  const containerStyle: React.CSSProperties = {
    display: "flex",
    flexDirection: isVertical ? "column" : "row",
    gap: isVertical ? theme.spacing[2] : theme.spacing[4],
    alignItems: isVertical ? "stretch" : "center",
  };

  const labelTextStyle: React.CSSProperties = {
    color: theme.semantic.form.labelText,
    fontSize: theme.typography.fontSize.sm,
    fontWeight: theme.typography.fontWeight.semibold,
    fontFamily: theme.typography.fontFamily.body,
    letterSpacing: theme.typography.letterSpacing.normal,
    minWidth: !isVertical ? "120px" : "auto",
  };

  const contentStyle: React.CSSProperties = {
    flex: isVertical ? 1 : "auto",
    width: isVertical ? "100%" : "auto",
  };

  const errorStyle: React.CSSProperties = {
    color: theme.colors.error[600],
    fontSize: theme.typography.fontSize.xs,
    marginTop: theme.spacing[1],
    fontFamily: theme.typography.fontFamily.body,
  };

  const helperStyle: React.CSSProperties = {
    color: theme.semantic.form.helperText,
    fontSize: theme.typography.fontSize.xs,
    marginTop: theme.spacing[1],
    fontFamily: theme.typography.fontFamily.body,
  };

  const content = (
    <>
      {children}
      {error && <div style={errorStyle}>⚠️ {error}</div>}
      {helperText && !error && <div style={helperStyle}>{helperText}</div>}
    </>
  );

  if (label) {
    return (
      <label style={containerStyle}>
        <span style={labelTextStyle}>
          {label} {required && <span style={{ color: theme.colors.error[500] }}>*</span>}
        </span>
        <span style={{ ...contentStyle, display: "block" }}>{content}</span>
      </label>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={contentStyle}>{content}</div>
    </div>
  );
}

/**
 * Form section divider
 */
export function FormSection({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: theme.spacing[4],
        paddingTop: theme.spacing[6],
        borderTop: `1px solid ${theme.colors.neutral[200]}`,
      }}
    >
      <div>
        <h3
          style={{
            fontSize: theme.typography.fontSize.lg,
            fontWeight: theme.typography.fontWeight.semibold,
            color: theme.colors.neutral[900],
            fontFamily: theme.typography.fontFamily.display,
            margin: 0,
            marginBottom: theme.spacing[1],
          }}
        >
          {title}
        </h3>
        {subtitle && (
          <p
            style={{
              fontSize: theme.typography.fontSize.sm,
              color: theme.colors.neutral[600],
              fontFamily: theme.typography.fontFamily.body,
              margin: 0,
            }}
          >
            {subtitle}
          </p>
        )}
      </div>

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: theme.spacing[4],
        }}
      >
        {children}
      </div>
    </div>
  );
}

/**
 * Form actions footer
 */
export function FormActions({
  children,
  align = "right",
}: {
  children: ReactNode;
  align?: "left" | "center" | "right";
}) {
  const alignMap = {
    left: "flex-start",
    center: "center",
    right: "flex-end",
  };

  return (
    <div
      style={{
        display: "flex",
        gap: theme.spacing[3],
        justifyContent: alignMap[align],
        paddingTop: theme.spacing[6],
        borderTop: `1px solid ${theme.colors.neutral[200]}`,
        flexWrap: "wrap",
      }}
    >
      {children}
    </div>
  );
}

/**
 * Form title area - for page headers
 */
export function FormHeader({
  title,
  subtitle,
  description,
}: {
  title: string;
  subtitle?: string;
  description?: string;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: theme.spacing[2],
        marginBottom: theme.spacing[6],
      }}
    >
      <div>
        <h1
          style={{
            fontSize: theme.typography.fontSize["3xl"],
            fontWeight: theme.typography.fontWeight.bold,
            color: theme.colors.neutral[900],
            fontFamily: theme.typography.fontFamily.display,
            margin: 0,
          }}
        >
          {title}
        </h1>
        {subtitle && (
          <p
            style={{
              fontSize: theme.typography.fontSize.base,
              color: theme.colors.neutral[600],
              fontFamily: theme.typography.fontFamily.body,
              margin: `${theme.spacing[1]} 0 0 0`,
            }}
          >
            {subtitle}
          </p>
        )}
      </div>
      {description && (
        <p
          style={{
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.neutral[700],
            fontFamily: theme.typography.fontFamily.body,
            margin: 0,
            lineHeight: theme.typography.lineHeight.relaxed,
          }}
        >
          {description}
        </p>
      )}
    </div>
  );
}

/**
 * Checkbox with theme styling
 */
export function CheckboxField({
  label,
  checked,
  onChange,
  disabled,
  "data-testid": dataTestId,
}: {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
  "data-testid"?: string;
}) {
  return (
    <label
      data-testid={dataTestId}
      style={{
        display: "flex",
        alignItems: "center",
        gap: theme.spacing[3],
        cursor: disabled ? "not-allowed" : "pointer",
        opacity: disabled ? 0.5 : 1,
      }}
    >
      <input
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        disabled={disabled}
        style={{
          width: "20px",
          height: "20px",
          borderRadius: theme.borderRadius.md,
          borderColor: theme.colors.neutral[400],
          accentColor: theme.colors.primary[600],
          cursor: disabled ? "not-allowed" : "pointer",
        }}
      />
      <span
        style={{
          color: theme.colors.neutral[900],
          fontSize: theme.typography.fontSize.base,
          fontFamily: theme.typography.fontFamily.body,
        }}
      >
        {label}
      </span>
    </label>
  );
}

/**
 * Radio group with theme styling
 */
export function RadioField({
  label,
  selected,
  onChange,
  disabled,
  options,
}: {
  label: string;
  selected: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  options: Array<{ value: string; label: string }>;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: theme.spacing[2],
      }}
    >
      <label
        style={{
          color: theme.colors.neutral[900],
          fontSize: theme.typography.fontSize.sm,
          fontWeight: theme.typography.fontWeight.semibold,
        }}
      >
        {label}
      </label>
      <div style={{ display: "flex", flexDirection: "column", gap: theme.spacing[2] }}>
        {options.map((option) => (
          <div
            key={option.value}
            style={{
              display: "flex",
              alignItems: "center",
              gap: theme.spacing[3],
              cursor: disabled ? "not-allowed" : "pointer",
              opacity: disabled ? 0.5 : 1,
            }}
            onClick={() => !disabled && onChange(option.value)}
          >
            <input
              type="radio"
              name={label}
              value={option.value}
              checked={selected === option.value}
              onChange={(e) => onChange(e.target.value)}
              disabled={disabled}
              style={{
                width: "20px",
                height: "20px",
                accentColor: theme.colors.primary[600],
                cursor: disabled ? "not-allowed" : "pointer",
              }}
            />
            <label
              style={{
                color: theme.colors.neutral[900],
                fontSize: theme.typography.fontSize.base,
                fontFamily: theme.typography.fontFamily.body,
                cursor: disabled ? "not-allowed" : "pointer",
              }}
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
}
