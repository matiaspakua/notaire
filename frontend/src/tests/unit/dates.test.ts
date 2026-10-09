/**
 * Calendar dates (#1339, #1338; RNF-08, RF-16/RF-17, RF-31).
 *
 * The backend stores date-only fields (`@Temporal(DATE)`) and serializes them
 * as midnight in the JVM zone: "1980-01-15" leaves a Europe/Madrid server as
 * "1980-01-14T23:00:00.000Z" and a UTC server as "1980-01-15T00:00:00.000Z".
 * The UI must show and round-trip the same calendar day in any browser zone.
 */
import { describe, it, expect } from "vitest";
import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, resolve } from "node:path";
import {
  BUSINESS_TIME_ZONE,
  formatCalendarDate,
  formatInstant,
  parseCalendarDate,
  toDateInputValue,
  todayInputValue,
} from "@/lib/dates";
import { formatDate } from "@/lib/utils";

describe("BUSINESS_TIME_ZONE", () => {
  it("is Argentina (default decision on #1339)", () => {
    expect(BUSINESS_TIME_ZONE).toBe("America/Argentina/Buenos_Aires");
  });
});

describe("parseCalendarDate()", () => {
  it.each([
    ["2026-09-05", { year: 2026, month: 9, day: 5 }],
    ["2026-09-04T22:00:00.000Z", { year: 2026, month: 9, day: 5 }], // Madrid summer midnight
    ["1980-01-14T23:00:00.000Z", { year: 1980, month: 1, day: 15 }], // Madrid winter midnight
    ["2026-09-05T00:00:00.000Z", { year: 2026, month: 9, day: 5 }], // UTC midnight
    ["2026-09-05T03:00:00.000Z", { year: 2026, month: 9, day: 5 }], // Buenos Aires midnight
    ["2026-09-05T00:00:00", { year: 2026, month: 9, day: 5 }], // zone-less midnight
    ["2026-09-05T00:00:00-03:00", { year: 2026, month: 9, day: 5 }],
  ])("%s is %o", (value, expected) => {
    expect(parseCalendarDate(value)).toEqual(expected);
  });

  it.each([null, undefined, "", "not a date"])("%s is null", (value) => {
    expect(parseCalendarDate(value)).toBeNull();
  });
});

describe("toDateInputValue()", () => {
  it.each([
    [null, ""],
    [undefined, ""],
    ["", ""],
    ["2026-09-05", "2026-09-05"],
    ["2026-09-04T22:00:00.000Z", "2026-09-05"],
    ["2026-09-05T03:00:00.000Z", "2026-09-05"],
    ["2026-08-06T22:00:00.000Z", "2026-08-07"],
  ])("%s -> %s", (value, expected) => {
    expect(toDateInputValue(value)).toBe(expected);
  });
});

describe("formatCalendarDate()", () => {
  it("shows the stored day, not the browser-zone day", () => {
    // Payment stored on 2026-08-07 by a Madrid server (#1339 evidence).
    expect(formatCalendarDate("2026-08-06T22:00:00.000Z", "es-AR")).toBe("7/8/2026");
    expect(formatCalendarDate("2026-08-07", "es-AR")).toBe("7/8/2026");
  });

  it("returns — for empty or invalid values", () => {
    expect(formatCalendarDate(null)).toBe("—");
    expect(formatCalendarDate("nope")).toBe("—");
  });
});

describe("formatDate() keeps its signature and uses calendar semantics", () => {
  it("formats yyyy-MM-dd as that day", () => {
    expect(formatDate("2024-01-15")).toBe("15/1/2024");
  });

  it("formats a server-midnight instant as the stored day", () => {
    expect(formatDate("1980-01-14T23:00:00.000Z")).toBe("15/1/1980");
  });
});

describe("formatInstant()", () => {
  it("shows a timestamp in the business time zone", () => {
    // 02:30 UTC on 2026-09-05 is 23:30 on 2026-09-04 in Buenos Aires.
    expect(formatInstant("2026-09-05T02:30:00.000Z", "es-AR")).toMatch(/^4\/9\/2026/);
    expect(formatInstant("2026-09-05T02:30:00.000Z", "es-AR")).toContain("23:30");
  });

  it("returns — for empty values", () => {
    expect(formatInstant(undefined)).toBe("—");
  });
});

describe("todayInputValue()", () => {
  it("is the business-zone calendar day, not the UTC day", () => {
    // 2026-10-10 01:00 UTC is still 2026-10-09 (22:00) in Buenos Aires.
    expect(todayInputValue(new Date("2026-10-10T01:00:00.000Z"))).toBe("2026-10-09");
    expect(todayInputValue(new Date("2026-10-09T12:00:00.000Z"))).toBe("2026-10-09");
  });
});

describe("source guard: no ad-hoc date handling in pages and components (#1339)", () => {
  const roots = ["app", "components"].map((d) => resolve(__dirname, "../..", d));
  const files: string[] = [];
  const walk = (dir: string) => {
    for (const entry of readdirSync(dir)) {
      const full = join(dir, entry);
      if (statSync(full).isDirectory()) walk(full);
      else if (/\.tsx?$/.test(entry) && !/\.test\.tsx?$/.test(entry)) files.push(full);
    }
  };
  roots.forEach(walk);

  it.each([
    ["split(\"T\")", /split\(\s*["']T["']\s*\)/],
    ["toISOString() for a calendar day", /toISOString\(\)\s*\.\s*(split|slice|substring)/],
    ["new Date(x).toLocaleDateString/String", /new Date\([^)]+\)\s*\.\s*toLocale(Date)?String/],
  ])("no %s", (_label, pattern) => {
    const offenders = files.filter((f) => pattern.test(readFileSync(f, "utf8")));
    expect(offenders.map((f) => f.slice(f.indexOf("src/")))).toEqual([]);
  });
});
