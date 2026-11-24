"use client";

import { usePreferences } from "@/components/site/Preferences";
import { tr } from "@/lib/i18n/es";

export function HowList({ steps }: { steps: string[] }) {
  const { locale } = usePreferences();
  return (
    <ol className="list-decimal space-y-2 pl-5">
      {steps.map((step) => (
        <li key={step}>{tr(locale, step)}</li>
      ))}
    </ol>
  );
}
