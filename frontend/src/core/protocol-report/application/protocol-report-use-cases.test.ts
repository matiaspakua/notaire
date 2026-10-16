// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { ProtocolReportGateway } from "./ports";
import { createProtocolReportUseCases } from "./use-cases";

describe("protocol report use cases (#1833)", () => {
  it("downloads a blob for the given path", async () => {
    const calls: string[] = [];
    const gateway: ProtocolReportGateway = {
      download: async (path) => {
        calls.push(path);
        return new Blob(["pdf"]);
      },
    };
    const uc = createProtocolReportUseCases(gateway);
    await expect(uc.download("/reports/index-book?year=2026")).resolves.toBeInstanceOf(Blob);
    expect(calls).toEqual(["/reports/index-book?year=2026"]);
  });
});
