/**
 * Unit test for useDocumentReentry (CU43).
 * Hexagon wiring: #1841 / ADR-030.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/document-reentry", () => ({
  documentReentryUseCases: {
    getByManagement: vi.fn(),
    reenter: vi.fn(),
  },
}));

import { documentReentryUseCases } from "@/composition/document-reentry";
import {
  documentReentryKeys,
  useDocumentReentry,
  useReenterDocumentation,
} from "@/hooks/useDocumentReentry";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper, queryClient };
}

describe("documentReentryKeys", () => {
  it("byManagement key includes the management id", () => {
    expect(documentReentryKeys.byManagement(9)).toEqual(["documentReentry", "management", 9]);
  });
});

describe("useDocumentReentry (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the management's procedures with required documentation", async () => {
    vi.mocked(documentReentryUseCases.getByManagement).mockResolvedValue({
      idManagement: 9,
      number: 1001,
      heading: "Test management",
      procedures: [
        {
          idProcedure: 1,
          typeProcedureName: "Compraventa",
          requiredDocuments: [{ idDocumentType: 5, name: "Certificado de Dominio", expires: true }],
        },
      ],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentReentry(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(documentReentryUseCases.getByManagement).toHaveBeenCalledWith(9);
    expect(result.current.data?.procedures[0].requiredDocuments[0].name).toBe("Certificado de Dominio");
  });

  it("does not fetch when managementId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentReentry(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(documentReentryUseCases.getByManagement).not.toHaveBeenCalled();
  });
});

describe("useReenterDocumentation (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("re-enters a document via the use case", async () => {
    vi.mocked(documentReentryUseCases.reenter).mockResolvedValue({
      idSubmittedDocument: 1,
      idProcedure: 1,
      idDocumentType: 5,
      expires: false,
      reentered: true,
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useReenterDocumentation(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      managementId: 9,
      reentry: { idProcedure: 1, idDocumentType: 5 },
    });

    expect(documentReentryUseCases.reenter).toHaveBeenCalledWith(9, {
      idProcedure: 1,
      idDocumentType: 5,
    });
  });
});
