"use client";

/**
 * FormField → control wiring (#1351, RNF-08). FormField provides the ids of its
 * error/helper text and its invalid/required state; Input, SelectTrigger and the
 * search combobox read it so the control is described by that text and exposes
 * aria-invalid/aria-required, without each caller wiring it by hand.
 */
import { createContext, useContext } from "react";

export interface FormFieldControlState {
  /** Space-separated ids of the error or helper text. */
  describedBy?: string;
  invalid: boolean;
  required: boolean;
}

export const FormFieldContext = createContext<FormFieldControlState | null>(null);

type AriaProps = {
  "aria-describedby"?: string;
  "aria-invalid"?: boolean | "true" | "false" | "grammar" | "spelling";
  "aria-required"?: boolean | "true" | "false";
};

/** Merges a FormField's state into a control's aria props; the caller's own values win. */
export function mergeFormFieldAria<P extends AriaProps>(props: P, field: FormFieldControlState | null): P {
  if (!field) return props;
  const describedBy = [props["aria-describedby"], field.describedBy].filter(Boolean).join(" ") || undefined;
  return {
    ...props,
    "aria-describedby": describedBy,
    "aria-invalid": props["aria-invalid"] ?? (field.invalid ? true : undefined),
    "aria-required": props["aria-required"] ?? (field.required ? true : undefined),
  };
}

/** The control-side hook: `<input {...useFormFieldAria(props)} />`. */
export function useFormFieldAria<P extends AriaProps>(props: P): P {
  return mergeFormFieldAria(props, useContext(FormFieldContext));
}
