/**
 * Calendar dates and instants (#1339, #1338; RNF-08).
 *
 * Date-only business fields (birth date, budget, payment, deed, document
 * dates) are `@Temporal(DATE)` columns that the backend serializes as
 * midnight in its own JVM zone: 2026-09-05 leaves a Europe/Madrid server as
 * "2026-09-04T22:00:00.000Z" and a UTC server as "2026-09-05T00:00:00.000Z".
 * Reading that instant in the browser zone shows the previous day in
 * Argentina. These helpers recover the calendar day independently of both
 * the server and the browser zone, and accept plain `yyyy-MM-dd` too, so they
 * keep working once the backend moves to `LocalDate`.
 *
 * Real timestamps (the audit log) are instants: show them in the business
 * time zone with {@link formatInstant}. Management history dates are
 * TIMESTAMP columns but hold the day's midnight, so they are calendar dates.
 */

/** Business time zone of the notary office (default decision on #1339). */
export const BUSINESS_TIME_ZONE = "America/Argentina/Buenos_Aires";

export interface CalendarDate {
  year: number;
  month: number;
  day: number;
}

const DATE_ONLY = /^(\d{4})-(\d{2})-(\d{2})$/;
const LOCAL_MIDNIGHT = /^(\d{4})-(\d{2})-(\d{2})T00:00(?::00(?:\.0+)?)?$/;
const HALF_DAY_MS = 12 * 60 * 60 * 1000;

function fromMatch(m: RegExpExecArray): CalendarDate {
  return { year: Number(m[1]), month: Number(m[2]), day: Number(m[3]) };
}

/**
 * Calendar day of a date-only value: `yyyy-MM-dd` as is; an ISO instant as
 * the day of its nearest UTC midnight, which is the day the server meant for
 * any server zone between UTC-12 and UTC+12.
 */
export function parseCalendarDate(value?: string | null): CalendarDate | null {
  if (!value) return null;
  const text = value.trim();
  const literal = DATE_ONLY.exec(text) ?? LOCAL_MIDNIGHT.exec(text);
  if (literal) return fromMatch(literal);
  const ms = Date.parse(text);
  if (Number.isNaN(ms)) return null;
  const nearestMidnight = new Date(ms + HALF_DAY_MS);
  return {
    year: nearestMidnight.getUTCFullYear(),
    month: nearestMidnight.getUTCMonth() + 1,
    day: nearestMidnight.getUTCDate(),
  };
}

const pad = (n: number) => String(n).padStart(2, "0");

/** `yyyy-MM-dd` for `<input type="date">`, or "" when there is no date. */
export function toDateInputValue(value?: string | null): string {
  const d = parseCalendarDate(value);
  return d ? `${d.year}-${pad(d.month)}-${pad(d.day)}` : "";
}

/** A date-only value formatted for display ("7/8/2026" in es-AR); "—" when empty. */
export function formatCalendarDate(value?: string | null, locale = "es-AR"): string {
  const d = parseCalendarDate(value);
  if (!d) return "—";
  return new Intl.DateTimeFormat(locale, { timeZone: "UTC" }).format(
    new Date(Date.UTC(d.year, d.month - 1, d.day)),
  );
}

/** A timestamp shown as date and time in the business time zone; "—" when empty. */
export function formatInstant(value?: string | null, locale = "es-AR"): string {
  if (!value) return "—";
  const ms = Date.parse(value);
  if (Number.isNaN(ms)) return "—";
  return new Intl.DateTimeFormat(locale, {
    timeZone: BUSINESS_TIME_ZONE,
    day: "numeric",
    month: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(new Date(ms));
}

/** Today's calendar day in the business time zone, as `yyyy-MM-dd`. */
export function todayInputValue(now: Date = new Date()): string {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: BUSINESS_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(now);
  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? "";
  return `${get("year")}-${get("month")}-${get("day")}`;
}
