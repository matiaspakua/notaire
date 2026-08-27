import { describe, it, expect } from "vitest";
import { cn, formatDate, formatCurrency, fullName, extractApiError } from "@/lib/utils";

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

describe("extractApiError()", () => {
  it("returns null for non-Error values", () => {
    expect(extractApiError("plain string")).toBeNull();
    expect(extractApiError(null)).toBeNull();
  });

  it("returns null when the status is neither 400 nor 409", () => {
    const err = new Error('[404] /escrituras/1: {"message":"Not found"}');
    expect(extractApiError(err)).toBeNull();
  });

  it("extracts the message field from a 400 BusinessValidationException body", () => {
    const err = new Error(
      '[400] /escrituras/1/firmar: {"status":400,"error":"Bad Request","message":"La escritura no está en estado \'Sin Firmar\'"}'
    );
    expect(extractApiError(err)).toBe("La escritura no está en estado 'Sin Firmar'");
  });

  it("extracts the error field from a 409 Conflict body as a fallback", () => {
    const err = new Error('[409] /movimiento-testimonio: {"error":"Ya existe un movimiento abierto"}');
    expect(extractApiError(err)).toBe("Ya existe un movimiento abierto");
  });

  it("returns null when the body is not parseable JSON", () => {
    const err = new Error("[400] /escrituras/1/firmar: not json");
    expect(extractApiError(err)).toBeNull();
  });
});
