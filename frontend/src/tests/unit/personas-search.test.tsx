/**
 * Personas search (#1357, RF-39 / RNF-03): debounced, keyed by primitive
 * params only, and keeps the previous results while the next ones load.
 */
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { act, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiGetPaged: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
import { personasKeys, useSearchPersonas, PERSONAS_SEARCH_DEBOUNCE_MS, type PersonasSearchParams } from "@/hooks/usePersonas";

function wrapper() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  };
}

const EMPTY: PersonasSearchParams = { firstName: "", lastName: "", identificationNumber: "", onlyClients: false };

async function flush(ms: number) {
  await act(async () => {
    await vi.advanceTimersByTimeAsync(ms);
  });
}

describe("useSearchPersonas (#1357)", () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    vi.mocked(apiGet).mockReset();
  });
  afterEach(() => vi.useRealTimers());

  it("debounces at 300ms", () => {
    expect(PERSONAS_SEARCH_DEBOUNCE_MS).toBe(300);
  });

  it("the search key holds only primitive params", () => {
    const key = personasKeys.search({ firstName: "a", lastName: "b", identificationNumber: "1", onlyClients: true });
    expect(key.slice(0, 2)).toEqual(["personas", "search"]);
    const flat = JSON.stringify(key);
    expect(JSON.parse(flat)).toEqual(key);
    for (const part of key.slice(2)) {
      for (const value of Object.values(part as object)) expect(["string", "boolean"]).toContain(typeof value);
    }
  });

  it("is idle without criteria", async () => {
    const { result } = renderHook(() => useSearchPersonas(EMPTY), { wrapper: wrapper() });
    await flush(400);
    expect(result.current.active).toBe(false);
    expect(apiGet).not.toHaveBeenCalled();
  });

  it("typing 8 characters quickly sends one request with the final text", async () => {
    vi.mocked(apiGet).mockResolvedValue([{ personId: 1, lastName: "Martinez" }]);
    const { rerender, result } = renderHook((p: PersonasSearchParams) => useSearchPersonas(p), {
      wrapper: wrapper(),
      initialProps: EMPTY,
    });
    for (const prefix of ["M", "Ma", "Mar", "Mart", "Marti", "Martin", "Martine", "Martinez"]) {
      rerender({ ...EMPTY, lastName: prefix });
      await flush(50);
    }
    await flush(PERSONAS_SEARCH_DEBOUNCE_MS + 50);
    expect(apiGet).toHaveBeenCalledTimes(1);
    expect(vi.mocked(apiGet).mock.calls[0][0]).toBe("/people/search?lastName=Martinez");
    expect(result.current.active).toBe(true);
  });

  it("keeps the previous results while the next search loads", async () => {
    let release: (v: unknown) => void = () => {};
    vi.mocked(apiGet)
      .mockResolvedValueOnce([{ personId: 1, lastName: "Perez" }])
      .mockImplementationOnce(() => new Promise((r) => (release = r)));
    const { rerender, result } = renderHook((p: PersonasSearchParams) => useSearchPersonas(p), {
      wrapper: wrapper(),
      initialProps: { ...EMPTY, lastName: "Perez" },
    });
    await flush(PERSONAS_SEARCH_DEBOUNCE_MS + 50);
    expect(result.current.results).toEqual([{ personId: 1, lastName: "Perez" }]);
    rerender({ ...EMPTY, lastName: "Perezz" });
    await flush(PERSONAS_SEARCH_DEBOUNCE_MS + 50);
    expect(apiGet).toHaveBeenCalledTimes(2);
    expect(result.current.results).toEqual([{ personId: 1, lastName: "Perez" }]);
    await act(async () => release([]));
  });
});
