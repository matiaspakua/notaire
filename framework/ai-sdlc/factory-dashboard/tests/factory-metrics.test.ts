import { describe, expect, it } from "vitest";

import { changeLabel, costLabel, minutes, totalTokens } from "../src/ui/factory-metrics.ts";

describe("factory metrics formatting (#2447)", () => {
  it("never shows a missing price as $0", () => {
    expect(costLabel({ usd: null, fully_priced: false, rows: 0, reported_rows: 0 }, "en")).toEqual({ text: "—", note: "none" });
    expect(costLabel({ usd: 1.5, fully_priced: false, rows: 3, reported_rows: 0 }, "en")).toEqual({ text: "$1.50", note: "partial" });
    expect(costLabel({ usd: 0, fully_priced: true, rows: 2, reported_rows: 2 }, "en")).toEqual({ text: "$0.00", note: "reported" });
    expect(costLabel({ usd: 2.15, fully_priced: true, rows: 4, reported_rows: 1 }, "en")).toEqual({ text: "$2.15", note: "priced" });
  });

  it("sums every token kind", () => {
    expect(totalTokens({ input: 10, output: 5, cache_read: 100, cache_write: 1 })).toBe(116);
  });

  it("formats minutes and signed changes", () => {
    expect(minutes(2196, "en")).toBe("36.6 min");
    expect(minutes(null, "en")).toBe("—");
    expect(changeLabel(-31.9)).toBe("−31.9%");
    expect(changeLabel(2.5)).toBe("+2.5%");
    expect(changeLabel(null)).toBe("—");
  });
});
