/**
 * Integration-style unit tests for api-client.ts
 * Uses fetch mock — no real network calls.
 * Tests CU patterns: list, get, create, update, delete
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";

// Mock global fetch
const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

function makeResponse(body: unknown, status = 200) {
  const json = JSON.stringify(body);
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    text: () => Promise.resolve(json),
    json: () => Promise.resolve(body),
  } as Response);
}

beforeEach(() => {
  vi.clearAllMocks();
  window.localStorage.clear();
});

describe("Authorization header (issue #552)", () => {
  it("attaches the persisted JWT as a Bearer token", async () => {
    window.localStorage.setItem(
      "notaire-auth",
      JSON.stringify({ state: { user: { nombre: "admin" }, token: "fake-jwt-token" } })
    );
    mockFetch.mockReturnValueOnce(makeResponse([]));

    await apiGet("/gestiones");

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones"),
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: "Bearer fake-jwt-token" }),
      })
    );
  });

  it("omits the Authorization header when no token is persisted", async () => {
    mockFetch.mockReturnValueOnce(makeResponse([]));

    await apiGet("/gestiones");

    const headers = mockFetch.mock.calls[0][1].headers as Record<string, string>;
    expect(headers.Authorization).toBeUndefined();
  });
});

describe("apiGet()", () => {
  it("fetches and returns parsed JSON", async () => {
    mockFetch.mockReturnValueOnce(makeResponse([{ idGestion: 1 }]));
    const result = await apiGet<{ idGestion: number }[]>("/gestiones");
    expect(result).toEqual([{ idGestion: 1 }]);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones"),
      expect.objectContaining({ cache: "no-store" })
    );
  });

  it("throws on non-2xx response", async () => {
    mockFetch.mockReturnValueOnce(makeResponse({ error: "not found" }, 404));
    await expect(apiGet("/gestiones/999")).rejects.toThrow("404");
  });
});

describe("apiPost()", () => {
  it("sends POST with JSON body", async () => {
    mockFetch.mockReturnValueOnce(makeResponse(""));
    await apiPost("/gestiones", { numero: 100 });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ numero: 100 }),
      })
    );
  });

  it("returns parsed response when server returns JSON", async () => {
    mockFetch.mockReturnValueOnce(makeResponse({ valido: true, nombre: "admin" }));
    const result = await apiPost<{ valido: boolean; nombre: string }>("/usuarios/login", {
      nombre: "admin",
      contrasenia: "admin",
    });
    expect(result.valido).toBe(true);
    expect(result.nombre).toBe("admin");
  });

  it("throws on server error", async () => {
    mockFetch.mockReturnValueOnce(makeResponse({ message: "server error" }, 500));
    await expect(apiPost("/gestiones", {})).rejects.toThrow("500");
  });
});

describe("apiPut()", () => {
  it("sends PUT with correct method and body", async () => {
    mockFetch.mockReturnValueOnce(makeResponse(""));
    await apiPut("/gestiones/1", { numero: 999 });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones/1"),
      expect.objectContaining({ method: "PUT" })
    );
  });
});

describe("apiDelete()", () => {
  it("sends DELETE request", async () => {
    mockFetch.mockReturnValueOnce(
      Promise.resolve({ ok: true, status: 204 } as Response)
    );
    await apiDelete("/gestiones/1");
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones/1"),
      expect.objectContaining({ method: "DELETE" })
    );
  });

  it("throws when server returns error", async () => {
    mockFetch.mockReturnValueOnce(
      Promise.resolve({ ok: false, status: 500, text: () => Promise.resolve("") } as Response)
    );
    await expect(apiDelete("/gestiones/1")).rejects.toThrow("500");
  });
});
