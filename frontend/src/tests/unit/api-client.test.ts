/**
 * Integration-style unit tests for api-client.ts
 * Uses fetch mock — no real network calls.
 * Tests CU patterns: list, get, create, update, delete
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { apiGet, apiPost, apiPut, apiDelete, ApiError } from "@/lib/api-client";
import { resetSessionExpiryGuardForTests } from "@/lib/session-expiry";
import { useAuthStore } from "@/store/auth-store";

// Mock global fetch
const mockFetch = vi.fn();
vi.stubGlobal("fetch", mockFetch);

const assignMock = vi.fn();

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
  resetSessionExpiryGuardForTests();
  window.localStorage.clear();
  useAuthStore.setState({ user: null, token: null, isAuthenticated: false });
  Object.defineProperty(window, "location", {
    configurable: true,
    value: { ...window.location, assign: assignMock, href: "http://localhost/dashboard" },
  });
});

afterEach(() => {
  resetSessionExpiryGuardForTests();
});

describe("Cookie credentials (issue #1051)", () => {
  it("sends credentials:include and does not attach Bearer from localStorage", async () => {
    window.localStorage.setItem(
      "notaire-auth",
      JSON.stringify({
        state: { user: { nombre: "admin" }, token: "fake-jwt-token", isAuthenticated: true },
      })
    );
    mockFetch.mockReturnValueOnce(makeResponse([]));

    await apiGet("/gestiones");

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining("/gestiones"),
      expect.objectContaining({
        credentials: "include",
      })
    );
    const headers = mockFetch.mock.calls[0][1].headers as Record<string, string>;
    expect(headers.Authorization).toBeUndefined();
  });

  it("omits the Authorization header when no token is persisted", async () => {
    mockFetch.mockReturnValueOnce(makeResponse([]));

    await apiGet("/gestiones");

    const headers = mockFetch.mock.calls[0][1].headers as Record<string, string>;
    expect(headers.Authorization).toBeUndefined();
  });
});

describe("X-Notaire-User header removal (issue #678)", () => {
  it("does not send X-Notaire-User even when a user is persisted", async () => {
    window.localStorage.setItem(
      "notaire-auth",
      JSON.stringify({ state: { user: { nombre: "admin" }, isAuthenticated: true } })
    );
    mockFetch.mockReturnValueOnce(makeResponse([]));

    await apiGet("/gestiones");

    const headers = mockFetch.mock.calls[0][1].headers as Record<string, string>;
    expect(headers["X-Notaire-User"]).toBeUndefined();
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

  it("rejects with ApiError carrying status and parseable 400 body (issue #1054)", async () => {
    const body = JSON.stringify({ message: "Cannot delete: still referenced" });
    mockFetch.mockReturnValueOnce(
      Promise.resolve({
        ok: false,
        status: 400,
        text: () => Promise.resolve(body),
      } as Response)
    );

    try {
      await apiDelete("/roles/1");
      expect.fail("expected apiDelete to reject");
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      const apiErr = err as ApiError;
      expect(apiErr.status).toBe(400);
      expect(apiErr.body).toBe(body);
    }
  });

  it("rejects with ApiError carrying parseable 409 body on DELETE (issue #1054)", async () => {
    const body = JSON.stringify({ message: "Conflict: workflow has nodes" });
    mockFetch.mockReturnValueOnce(
      Promise.resolve({
        ok: false,
        status: 409,
        text: () => Promise.resolve(body),
      } as Response)
    );

    await expect(apiDelete("/workflows/1")).rejects.toMatchObject({
      name: "ApiError",
      status: 409,
      body,
    });
  });

  it("throws ApiError and triggers session expiry on authenticated 401 (issue #1053)", async () => {
    useAuthStore.setState({
      user: { nombre: "admin", tipo: "ADMIN", valido: true },
      token: "expired-jwt",
      isAuthenticated: true,
    });
    mockFetch.mockReturnValueOnce(
      Promise.resolve({
        ok: false,
        status: 401,
        text: () => Promise.resolve("Unauthorized"),
      } as Response)
    );

    await expect(apiDelete("/gestiones/1")).rejects.toBeInstanceOf(ApiError);
    expect(assignMock).toHaveBeenCalledWith("/login?expired=1");
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });
});

describe("session expiry via handleResponse (issue #1053)", () => {
  it("redirects to /login?expired=1 on authenticated GET 401", async () => {
    useAuthStore.setState({
      user: { nombre: "admin", tipo: "ADMIN", valido: true },
      token: "expired-jwt",
      isAuthenticated: true,
    });
    mockFetch.mockReturnValueOnce(makeResponse({ message: "Unauthorized" }, 401));

    await expect(apiGet("/gestiones")).rejects.toBeInstanceOf(ApiError);
    expect(assignMock).toHaveBeenCalledWith("/login?expired=1");
    expect(useAuthStore.getState().token).toBeNull();
  });

  it("does not redirect on login POST 401", async () => {
    mockFetch.mockReturnValueOnce(makeResponse({ valido: false }, 401));

    await expect(apiPost("/usuarios/login", { name: "x", password: "y" })).rejects.toBeInstanceOf(
      ApiError
    );
    expect(assignMock).not.toHaveBeenCalled();
  });

  it("does not redirect on authenticated 500", async () => {
    useAuthStore.setState({
      user: { nombre: "admin", tipo: "ADMIN", valido: true },
      token: "valid-jwt",
      isAuthenticated: true,
    });
    mockFetch.mockReturnValueOnce(makeResponse({ message: "boom" }, 500));

    await expect(apiGet("/gestiones")).rejects.toThrow("500");
    expect(assignMock).not.toHaveBeenCalled();
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
  });
});
