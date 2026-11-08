/**
 * Unit test for useUpcomingExpirations (CU42).
 * Hexagon wiring: #1841 / ADR-030.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/submitted-document", () => ({
  submittedDocumentUseCases: {
    listUpcomingExpirations: vi.fn(),
  },
}));

import { submittedDocumentUseCases } from "@/composition/submitted-document";
import { upcomingExpirationKeys, useUpcomingExpirations } from "@/hooks/useUpcomingExpirations";

function createWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("upcomingExpirationKeys", () => {
  it("byDays key includes the window", () => {
    expect(upcomingExpirationKeys.byDays(15)).toEqual(["upcomingExpirations", 15]);
  });
});

describe("useUpcomingExpirations (CU42)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches upcoming expirations for the window via the use case", async () => {
    vi.mocked(submittedDocumentUseCases.listUpcomingExpirations).mockResolvedValue([
      {
        idSubmittedDocument: 1,
        documentName: "Certificado",
        prepared: false,
        observed: false,
        dateDue: "2026-07-01",
        daysRemaining: 10,
      },
    ]);

    const { result } = renderHook(() => useUpcomingExpirations(30), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(submittedDocumentUseCases.listUpcomingExpirations).toHaveBeenCalledWith(30);
    expect(result.current.data?.[0].documentName).toBe("Certificado");
  });
});
