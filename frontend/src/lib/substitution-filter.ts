/**
 * #1546 / CU59: client-side filters for the substitutions list.
 * A substitution is "in" a year when its date range overlaps that year.
 */
import type { Substitution } from "@/types";

type Person = Substitution["fkIdSubstituted"];

/** The calendar year of an ISO date(-time) string, read from the text to avoid time-zone shifts. */
function yearOf(value: string | undefined): number | null {
  const match = /^(\d{4})-/.exec(value ?? "");
  return match ? Number(match[1]) : null;
}

export function overlapsYear(s: Pick<Substitution, "dateStart" | "dateEnd">, year: number): boolean {
  const start = yearOf(s.dateStart);
  const end = yearOf(s.dateEnd) ?? start;
  if (start === null || end === null) return false;
  return start <= year && year <= end;
}

function normalize(text: string): string {
  return text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase().trim();
}

function fullName(p: Person): string {
  return p ? [p.name, p.lastName].filter(Boolean).join(" ") : "";
}

/** Matches the substituted or the substitute notary; an empty query matches everything. */
export function matchesNotary(s: Pick<Substitution, "fkIdSubstituted" | "fkIdSubstitute">, query: string): boolean {
  const q = normalize(query);
  if (!q) return true;
  return [s.fkIdSubstituted, s.fkIdSubstitute].some((p) => normalize(fullName(p)).includes(q));
}

/** Years present in the data (every overlapped year) plus the current year, newest first. */
export function substitutionYears(list: Substitution[], currentYear: number): number[] {
  const years = new Set<number>([currentYear]);
  for (const s of list) {
    const start = yearOf(s.dateStart);
    const end = yearOf(s.dateEnd) ?? start;
    if (start === null || end === null) continue;
    for (let y = start; y <= end; y++) years.add(y);
  }
  return [...years].sort((a, b) => b - a);
}

export function filterSubstitutions(list: Substitution[], f: { year?: number; notary?: string }): Substitution[] {
  return list.filter((s) => (f.year === undefined || overlapsYear(s, f.year)) && matchesNotary(s, f.notary ?? ""));
}
