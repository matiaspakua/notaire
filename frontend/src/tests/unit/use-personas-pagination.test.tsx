/**
 * usePersonas reads the paginated GET /people endpoint (slice of #596, CU18/CU54).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPaged: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiGet, apiGetPaged } from "@/lib/api-client";
import { usePersonas } from "@/hooks/usePersonas";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("usePersonas pagination (#596)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("unwraps the Spring page from GET /people", async () => {
    vi.mocked(apiGetPaged).mockResolvedValue([{ personId: 1, firstName: "Ana" }]);

    const { result } = renderHook(() => usePersonas(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGetPaged).toHaveBeenCalledWith("/people");
    expect(apiGet).not.toHaveBeenCalled();
    expect(result.current.data).toEqual([{ personId: 1, firstName: "Ana" }]);
  });
});
