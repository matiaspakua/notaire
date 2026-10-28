/**
 * requiredFieldErrors — Wave 5 / #1951
 */
import { describe, expect, it } from "vitest";
import { requiredFieldErrors } from "@/lib/required-field-errors";

describe("requiredFieldErrors (#1951)", () => {
  const msg = "Required";

  it("returns an empty object when every value is present", () => {
    expect(
      requiredFieldErrors(
        { client: 1, date: "2026-01-01", amount: 10 },
        msg,
      ),
    ).toEqual({});
  });

  it("flags null, undefined, blank string, and NaN as missing", () => {
    expect(
      requiredFieldErrors(
        {
          client: null,
          date: undefined,
          note: "   ",
          amount: Number.NaN,
          ok: "x",
        },
        msg,
      ),
    ).toEqual({
      client: msg,
      date: msg,
      note: msg,
      amount: msg,
    });
  });

  it("treats 0 and false as present", () => {
    expect(requiredFieldErrors({ zero: 0, flag: false }, msg)).toEqual({});
  });
});
