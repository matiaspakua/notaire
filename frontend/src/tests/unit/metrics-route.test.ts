import { describe, expect, it } from "vitest";

import { GET } from "@/app/api/metrics/route";

describe("GET /api/metrics", () => {
  it("shouldExposeNodeRuntimeMetricsInPrometheusFormat", async () => {
    const response = await GET();
    const body = await response.text();

    expect(response.status).toBe(200);
    expect(response.headers.get("content-type")).toContain("text/plain");
    expect(body).toContain("nodejs_heap_size_used_bytes");
  });

  it("shouldCountRequestsServedByTheMetricsRoute", async () => {
    await GET();
    const body = await (await GET()).text();

    expect(body).toMatch(/notaire_frontend_metrics_scrapes_total \d+/);
  });
});
