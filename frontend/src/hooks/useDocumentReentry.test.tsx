/**
 * Unit test for useDocumentReentry (CU43 — document reentry).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
}));

import { apiGet, apiPost } from "@/lib/api-client";
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
    expect(documentReentryKeys.byManagement(9)).toEqual(["reingresoDocumentacion", "gestion", 9]);
  });
});

describe("useDocumentReentry (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the management's procedures with required documentation via GET", async () => {
    vi.mocked(apiGet).mockResolvedValue({
      idManagement: 9,
      number: 1001,
      heading: "Gestion",
      procedures: [
        {
          idProcedure: 1,
          typeProcedureName: "Compraventa",
          documentsNecesarios: [{ idDocumentType: 5, name: "Certificado de Dominio", expires: true }],
        },
      ],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentReentry(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/managements/9/document-reentry");
    expect(result.current.data?.procedures[0].documentsNecesarios[0].name).toBe("Certificado de Dominio");
  });

  it("does not fetch when gestionId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useDocumentReentry(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useReenterDocumentation (CU43)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("reingresa a document via POST", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idDocumentPresentado: 1, reingresado: true });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useReenterDocumentation(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      managementId: 9,
      reingreso: { idProcedure: 1, idDocumentType: 5 },
    });

    // Backend body is DtoDocumentReentryRequest(idProcedure, idDocumentType) —
    // not idProcedure/idTypeDocument.
    expect(apiPost).toHaveBeenCalledWith("/managements/9/document-reentry", {
      idProcedure: 1,
      idDocumentType: 5,
    });
  });
});
