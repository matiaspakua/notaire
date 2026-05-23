"use client";

import { useTransition } from "react";
import { useLocale } from "next-intl";
import { setLocale } from "@/i18n/actions";

const LOCALES = [
  { code: "es", label: "ES", flag: "🇦🇷" },
  { code: "en", label: "EN", flag: "🇺🇸" },
] as const;

export function LanguageSwitcher() {
  const currentLocale = useLocale();
  const [isPending, startTransition] = useTransition();

  function handleLocaleChange(locale: string) {
    startTransition(async () => {
      await setLocale(locale);
      window.location.reload();
    });
  }

  return (
    <div
      className="flex items-center gap-1 rounded-lg border border-border/50 p-1 bg-background/50"
      aria-label="Language selector"
      data-testid="language-switcher"
    >
      {LOCALES.map(({ code, label, flag }) => (
        <button
          key={code}
          onClick={() => handleLocaleChange(code)}
          disabled={isPending}
          aria-pressed={currentLocale === code}
          data-testid={`locale-${code}`}
          className={[
            "flex items-center gap-1 px-2 py-1 rounded-md text-xs font-semibold transition-all duration-150",
            currentLocale === code
              ? "bg-primary text-primary-foreground shadow-sm"
              : "text-muted-foreground hover:text-foreground hover:bg-muted",
            isPending ? "opacity-50 cursor-not-allowed" : "cursor-pointer",
          ].join(" ")}
        >
          <span aria-hidden="true">{flag}</span>
          <span>{label}</span>
        </button>
      ))}
    </div>
  );
}
