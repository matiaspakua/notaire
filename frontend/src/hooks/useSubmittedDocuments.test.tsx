/**
 * Unit tests for the submitted-document hooks (CU04, CU72 — #773).
 * Hexagon wiring: #1841 / ADR-030 — wire mapping covered in the HTTP gateway test.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/submitted-document", () => ({
  submittedDocumentUseCases: {
    create: vi.fn(),
    update: vi.fn(),
  },
}));

import { submittedDocumentUseCases } from "@/composition/submitted-document";
import { useCreateSubmittedDocument, useUpdateSubmittedDocument } from "@/hooks/useSubmittedDocuments";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("useCreateSubmittedDocument (CU04)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("passes typeId, date, delivered and procedureId to the use case", async () => {
    vi.mocked(submittedDocumentUseCases.create).mockResolvedValue({ idSubmittedDocument: 1 });

    const { result } = renderHook(() => useCreateSubmittedDocument(), { wrapper: createWrapper() });
    result.current.mutate({ typeId: 3, date: "2026-06-01", delivered: true, procedureId: 9 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(submittedDocumentUseCases.create).toHaveBeenCalledWith({
      typeId: 3,
      date: "2026-06-01",
      delivered: true,
      procedureId: 9,
    });
  });
});

describe("useUpdateSubmittedDocument (CU72)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("passes typeId, date and delivered to the use case", async () => {
    vi.mocked(submittedDocumentUseCases.update).mockResolvedValue(undefined);

    const { result } = renderHook(() => useUpdateSubmittedDocument(), { wrapper: createWrapper() });
    result.current.mutate({ id: 5, data: { typeId: 3, date: "2026-06-01", delivered: false } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(submittedDocumentUseCases.update).toHaveBeenCalledWith(5, {
      typeId: 3,
      date: "2026-06-01",
      delivered: false,
    });
  });
});
