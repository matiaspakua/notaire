/** Theme and language choices for the static site (#2201). No network. */

export const THEME_KEY = "notaire.theme";
export const LANG_KEY = "notaire.lang";

export type ThemeChoice = "light" | "dark" | "system";
export type LangChoice = "en" | "es" | "system";
export type Locale = "en" | "es";
export type ResolvedTheme = "light" | "dark";

export const THEME_CYCLE: readonly ThemeChoice[] = ["light", "dark", "system"];
export const LANG_CYCLE: readonly LangChoice[] = ["en", "es", "system"];

export function nextChoice<T>(cycle: readonly T[], current: T): T {
  const i = cycle.indexOf(current);
  return cycle[(i < 0 ? 0 : i + 1) % cycle.length];
}

export function resolveTheme(choice: ThemeChoice, prefersDark: boolean): ResolvedTheme {
  if (choice === "dark") return "dark";
  if (choice === "light") return "light";
  return prefersDark ? "dark" : "light";
}

export function resolveLang(choice: LangChoice, language: string): Locale {
  if (choice === "en" || choice === "es") return choice;
  return language.toLowerCase().startsWith("es") ? "es" : "en";
}

export function parseThemeChoice(raw: string | null): ThemeChoice {
  return raw === "light" || raw === "dark" || raw === "system" ? raw : "system";
}

export function parseLangChoice(raw: string | null): LangChoice {
  return raw === "en" || raw === "es" || raw === "system" ? raw : "system";
}
