import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import {
  MAX_RAW_BODY_BYTES,
  RATE_LIMIT_MAX,
  __resetClientErrorBeaconForTests,
  handleClientErrorBeacon,
} from "@/lib/client-error-beacon";

function loggedEntries(spy: { mock: { calls: unknown[][] } }) {
  return spy.mock.calls.map((call) => JSON.parse(String(call[0])));
}

function post(
  body: unknown,
  init: { headers?: Record<string, string>; raw?: string } = {},
): Request {
  const payload = init.raw ?? JSON.stringify(body);
  return new Request("http://localhost:3000/api/client-errors", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      ...(init.headers ?? {}),
    },
    body: payload,
  });
}

describe("POST /api/client-errors beacon", () => {
  beforeEach(() => {
    __resetClientErrorBeaconForTests();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("logs a valid client error as structured JSON for Loki", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);

    const response = await handleClientErrorBeacon(
      post({
        level: "ERROR",
        message: "TypeError: boom",
        name: "TypeError",
        path: "/dashboard",
      }),
    );

    expect(response.status).toBe(204);
    expect(await response.text()).toBe("");
    const [entry] = loggedEntries(error);
    expect(entry).toMatchObject({
      service: "notaire-frontend",
      level: "ERROR",
      message: expect.stringMatching(/^client_error level=ERROR path=\/dashboard /),
    });
    expect(entry.message).toContain("msg=TypeError: boom");
  });

  it("rejects malformed JSON and missing or invalid level/message without logging", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);

    const badJson = await handleClientErrorBeacon(post(null, { raw: "{not-json" }));
    expect(badJson.status).toBe(400);

    const missingMessage = await handleClientErrorBeacon(
      post({ level: "ERROR" }),
    );
    expect(missingMessage.status).toBe(400);

    const badLevel = await handleClientErrorBeacon(
      post({ level: "DEBUG", message: "nope" }),
    );
    expect(badLevel.status).toBe(400);

    expect(error).not.toHaveBeenCalled();
  });

  it("rejects oversized raw bodies with 413", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);
    const huge = "x".repeat(MAX_RAW_BODY_BYTES + 1);

    const response = await handleClientErrorBeacon(post(null, { raw: huge }));

    expect(response.status).toBe(413);
    expect(error).not.toHaveBeenCalled();
  });

  it("rejects unknown fields", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);

    const response = await handleClientErrorBeacon(
      post({
        level: "WARN",
        message: "ok",
        email: "user@example.com",
      }),
    );

    expect(response.status).toBe(400);
    expect(error).not.toHaveBeenCalled();
  });

  it("rate-limits excess requests from the same client identity", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);
    const headers = { "x-forwarded-for": "203.0.113.10" };

    for (let i = 0; i < RATE_LIMIT_MAX; i += 1) {
      const ok = await handleClientErrorBeacon(
        post({ level: "ERROR", message: `n=${i}` }, { headers }),
      );
      expect(ok.status).toBe(204);
    }

    const limited = await handleClientErrorBeacon(
      post({ level: "ERROR", message: "over" }, { headers }),
    );
    expect(limited.status).toBe(429);
    expect(error.mock.calls.length).toBe(RATE_LIMIT_MAX);
  });

  it("never logs cookies, Authorization, or query fragments", async () => {
    const error = vi.spyOn(console, "error").mockImplementation(() => undefined);

    const response = await handleClientErrorBeacon(
      post(
        {
          level: "ERROR",
          message: "safe",
          path: "/cases",
        },
        {
          headers: {
            cookie: "session=super-secret",
            authorization: "Bearer tok-abc",
          },
        },
      ),
    );

    expect(response.status).toBe(204);
    const serialized = JSON.stringify(loggedEntries(error)[0]);
    expect(serialized).not.toContain("super-secret");
    expect(serialized).not.toContain("tok-abc");
    expect(serialized).not.toContain("Bearer");
    expect(serialized).not.toContain("?");
  });
});
