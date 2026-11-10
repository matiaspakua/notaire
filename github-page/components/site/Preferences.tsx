"use client";

import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import {
  LANG_CYCLE,
  LANG_KEY,
  THEME_CYCLE,
  THEME_KEY,
  nextChoice,
  parseLangChoice,
  parseThemeChoice,
  resolveLang,
  resolveTheme,
  type LangChoice,
  type Locale,
  type ResolvedTheme,
  type ThemeChoice,
} from "@/lib/preferences";

type Preferences = {
  themeChoice: ThemeChoice;
  langChoice: LangChoice;
  theme: ResolvedTheme;
  locale: Locale;
  cycleTheme: () => void;
  cycleLang: () => void;
};

const PrefContext = createContext<Preferences | null>(null);

export function PreferencesProvider({ children }: { children: ReactNode }) {
  const [themeChoice, setThemeChoice] = useState<ThemeChoice>("system");
  const [langChoice, setLangChoice] = useState<LangChoice>("system");
  const [prefersDark, setPrefersDark] = useState(false);
  const [systemLang, setSystemLang] = useState("en");

  useEffect(() => {
    const darkMq = window.matchMedia("(prefers-color-scheme: dark)");
    const apply = () => setPrefersDark(darkMq.matches);
    apply();
    darkMq.addEventListener("change", apply);
    // The static HTML cannot read localStorage. The boot script has already painted
    // the theme; this catch-up only updates the controls after hydration.
    const id = window.setTimeout(() => {
      setThemeChoice(parseThemeChoice(localStorage.getItem(THEME_KEY)));
      setLangChoice(parseLangChoice(localStorage.getItem(LANG_KEY)));
      setSystemLang(navigator.language || "en");
    }, 0);
    return () => {
      darkMq.removeEventListener("change", apply);
      window.clearTimeout(id);
    };
  }, []);

  const theme = resolveTheme(themeChoice, prefersDark);
  const locale = resolveLang(langChoice, systemLang);

  useEffect(() => {
    const root = document.documentElement;
    root.dataset.theme = theme;
    root.dataset.themeChoice = themeChoice;
    root.lang = locale;
    root.dataset.langChoice = langChoice;
    document.querySelector('meta[name="theme-color"]')?.setAttribute("content", theme === "dark" ? "#0b1220" : "#ffffff");
  }, [theme, themeChoice, locale, langChoice]);

  const value = useMemo<Preferences>(
    () => ({
      themeChoice,
      langChoice,
      theme,
      locale,
      cycleTheme: () => {
        const next = nextChoice(THEME_CYCLE, themeChoice);
        localStorage.setItem(THEME_KEY, next);
        setThemeChoice(next);
      },
      cycleLang: () => {
        const next = nextChoice(LANG_CYCLE, langChoice);
        localStorage.setItem(LANG_KEY, next);
        setLangChoice(next);
      },
    }),
    [themeChoice, langChoice, theme, locale],
  );

  return <PrefContext.Provider value={value}>{children}</PrefContext.Provider>;
}

export function usePreferences(): Preferences {
  const ctx = useContext(PrefContext);
  if (ctx) return ctx;
  return {
    themeChoice: "system",
    langChoice: "system",
    theme: "light",
    locale: "en",
    cycleTheme: () => undefined,
    cycleLang: () => undefined,
  };
}

export function PreferenceButtons() {
  const { themeChoice, langChoice, theme, locale, cycleTheme, cycleLang } = usePreferences();
  const nextTheme = nextChoice(THEME_CYCLE, themeChoice);
  const nextLang = nextChoice(LANG_CYCLE, langChoice);
  const es = locale === "es";
  const themeWord = { light: es ? "claro" : "light", dark: es ? "oscuro" : "dark", system: es ? "sistema" : "system" };
  const langWord = { en: es ? "inglés" : "English", es: es ? "español" : "Spanish", system: es ? "sistema" : "system" };
  const themeLabel = es
    ? `Tema: ${themeChoice === "system" ? `sistema (${themeWord[theme]})` : themeWord[themeChoice]}. Cambiar a ${themeWord[nextTheme]}.`
    : `Theme: ${themeChoice === "system" ? `system (${theme})` : themeChoice}. Switch to ${nextTheme}.`;
  const currentLang = langChoice === "system" ? (es ? `sistema (${langWord[locale]})` : `system (${langWord[locale]})`) : langWord[langChoice];
  const langLabel = es ? `Idioma: ${currentLang}. Cambiar a ${langWord[nextLang]}.` : `Language: ${currentLang}. Switch to ${langWord[nextLang]}.`;
  const button = "flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-100";
  return (
    <>
      <button type="button" className={button} aria-label={themeLabel} onClick={cycleTheme}>
        {theme === "dark" ? <MoonIcon /> : <SunIcon />}
      </button>
      <button type="button" className={button} aria-label={langLabel} onClick={cycleLang}>
        {locale === "es" ? "ES" : "EN"}
      </button>
    </>
  );
}

function SunIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.8">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 3v2M12 19v2M3 12h2M19 12h2M5.6 5.6l1.4 1.4M17 17l1.4 1.4M18.4 5.6 17 7M7 17l-1.4 1.4" strokeLinecap="round" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.8">
      <path d="M20 14.5A7.5 7.5 0 1 1 9.5 4 6 6 0 0 0 20 14.5Z" strokeLinejoin="round" />
    </svg>
  );
}
