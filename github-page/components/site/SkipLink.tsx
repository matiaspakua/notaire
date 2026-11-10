"use client";

import { chrome } from "@/lib/i18n";
import { usePreferences } from "./Preferences";

export function SkipLink() {
  const { locale } = usePreferences();
  return (
    <a href="#main" className="skip-link">
      {chrome(locale).skip}
    </a>
  );
}
