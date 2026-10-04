/**
 * Unit tests for the Documentos presentados hooks (CU04, CU72 — #773): the request must use the
 * field names SubmittedDocumentController reads.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiPost, apiPut } from "@/lib/api-client";
import { useCreateDocumentoPresentado, useUpdateDocumentoPresentado } from "@/hooks/useDocumentosPresentados";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("useCreateDocumentoPresentado (CU04)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts typeId, date, delivered and procedureId", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idSubmittedDocument: 1 });

    const { result } = renderHook(() => useCreateDocumentoPresentado(), { wrapper: createWrapper() });
    result.current.mutate({ typeId: 3, date: "2026-06-01", delivered: true, procedureId: 9 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPost).toHaveBeenCalledWith("/documento-presentado", {
      typeId: 3,
      date: "2026-06-01",
      delivered: true,
      procedureId: 9,
    });
  });
});

describe("useUpdateDocumentoPresentado (CU72)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("puts typeId, date and delivered", async () => {
    vi.mocked(apiPut).mockResolvedValue(undefined);

    const { result } = renderHook(() => useUpdateDocumentoPresentado(), { wrapper: createWrapper() });
    result.current.mutate({ id: 5, data: { typeId: 3, date: "2026-06-01", delivered: false } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPut).toHaveBeenCalledWith("/documento-presentado/5", { typeId: 3, date: "2026-06-01", delivered: false });
  });
});
