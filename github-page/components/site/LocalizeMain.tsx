"use client";

import { usePathname } from "next/navigation";
import { useEffect } from "react";
import { observeLocalized } from "@/lib/i18n/dom";
import "@/lib/i18n/strings";
import { usePreferences } from "./Preferences";

/** Localises every page's main content, including what renders after mount (#2202, #2475). */
export function LocalizeMain() {
  const { locale } = usePreferences();
  const pathname = usePathname();
  useEffect(() => {
    const main = document.getElementById("main");
    return main ? observeLocalized(main, locale) : undefined;
  }, [locale, pathname]);
  return null;
}
