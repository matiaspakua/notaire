import { describe, expect, it } from "vitest";
import { FACTORY_METRICS, minutes, pct, usd } from "@/lib/factory-metrics";

describe("factory metrics on Pages (#2447)", () => {
  it("reads the committed snapshot", () => {
    expect(FACTORY_METRICS.overall.intents).toBeGreaterThan(0);
    expect(FACTORY_METRICS.intents.length).toBe(FACTORY_METRICS.overall.intents);
    expect(FACTORY_METRICS.method).toContain("aisdlc metrics factory");
  });

  it("never prints a missing cost as $0", () => {
    expect(usd(null)).toBe("n/a");
    expect(usd(0)).toBe("$0.00");
    expect(usd(122.129)).toBe("$122.13");
  });

  it("formats time and changes", () => {
    expect(minutes(2196)).toBe("36.6 min");
    expect(minutes(null)).toBe("n/a");
    expect(pct(-31.9)).toBe("−31.9%");
    expect(pct(2.5)).toBe("+2.5%");
  });
});
