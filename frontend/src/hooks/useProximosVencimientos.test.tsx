/**
 * Unit test for useProximosVencimientos (CU42 — informar próximos vencimientos).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
import { proximosVencimientosKeys, useProximosVencimientos } from "@/hooks/useProximosVencimientos";

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("proximosVencimientosKeys", () => {
  it("byDias key includes the window", () => {
    expect(proximosVencimientosKeys.byDias(15)).toEqual(["proximosVencimientos", 15]);
  });
});

describe("useProximosVencimientos (CU42)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the upcoming expirations of the window via GET with the dias parameter", async () => {
    vi.mocked(apiGet).mockResolvedValue([{ idSubmittedDocument: 1, documentName: "Certificado", daysRemaining: 10 }]);

    const { result } = renderHook(() => useProximosVencimientos(30), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/documento-presentado/proximos-vencimientos?dias=30");
    expect(result.current.data).toEqual([{ idSubmittedDocument: 1, documentName: "Certificado", daysRemaining: 10 }]);
  });
});
