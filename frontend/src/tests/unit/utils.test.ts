import { describe, it, expect } from "vitest";
import { cn, formatDate, formatCurrency, fullName } from "@/lib/utils";

describe("cn()", () => {
  it("merges classes", () => {
    expect(cn("a", "b")).toBe("a b");
  });

  it("deduplicates Tailwind classes", () => {
    expect(cn("px-2", "px-4")).toBe("px-4");
  });

  it("handles conditional classes", () => {
    expect(cn("base", false && "nope", "ok")).toBe("base ok");
  });
});

describe("formatDate()", () => {
  it("returns — for null/undefined", () => {
    expect(formatDate(null)).toBe("—");
    expect(formatDate(undefined)).toBe("—");
  });

  it("formats a valid ISO date", () => {
    // The exact format depends on locale but should not throw
    const result = formatDate("2024-01-15");
    expect(typeof result).toBe("string");
    expect(result.length).toBeGreaterThan(0);
  });
});

describe("formatCurrency()", () => {
  it("returns — for null/undefined", () => {
    expect(formatCurrency(null)).toBe("—");
    expect(formatCurrency(undefined)).toBe("—");
  });

  it("formats a number as ARS currency", () => {
    const result = formatCurrency(1000);
    expect(result).toContain("1");
    expect(result).toContain("000");
  });
});

describe("fullName()", () => {
  it("returns — for null", () => {
    expect(fullName(null)).toBe("—");
    expect(fullName(undefined)).toBe("—");
  });

  it("concatenates nombre and apellido", () => {
    expect(fullName({ nombre: "Juan", apellido: "García" })).toBe("Juan García");
  });

  it("handles missing apellido", () => {
    expect(fullName({ nombre: "Juan" })).toBe("Juan");
  });

  it("handles empty object", () => {
    expect(fullName({})).toBe("—");
  });
});
