"use client";

import type { ReactNode } from "react";
import { tr } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "./Preferences";

/** Renders the English source, or its Spanish entry when that language is selected. */
export function T({ text, children }: { text: string; children?: ReactNode }) {
  const { locale } = usePreferences();
  return <>{tr(locale, text)}{children}</>;
}

/** A sentence with {placeholders}. The Spanish entry keeps the same names. */
export function Fill({ text, values }: { text: string; values: Record<string, string | number> }) {
  const { locale } = usePreferences();
  const filled = tr(locale, text).replace(/\{(\w+)\}/g, (_, key: string) => String(values[key] ?? ""));
  return <>{filled}</>;
}
