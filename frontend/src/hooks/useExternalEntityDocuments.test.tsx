/**
 * Unit test for useExternalEntityDocuments (CU10 — external-entity document movements).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPut: vi.fn(),
}));

import { apiGet, apiPut } from "@/lib/api-client";
import {
  externalEntityDocumentKeys,
  useExternalEntityDocuments,
  useRegisterExternalEntityDocumentMovement,
} from "@/hooks/useExternalEntityDocuments";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return { Wrapper, queryClient };
}

describe("externalEntityDocumentKeys", () => {
  it("byManagement key includes the management id", () => {
    expect(externalEntityDocumentKeys.byManagement(9)).toEqual(["documentosEntidadExterna", "gestion", 9]);
  });
});

describe("useExternalEntityDocuments (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the management's external-entity documents via GET", async () => {
    vi.mocked(apiGet).mockResolvedValue({
      idManagement: 9,
      numero: 1001,
      heading: "Gestion",
      cadastralDesignationCatastral: "11-22-33",
      documents: [{ idSubmittedDocument: 1, name: "Certificado de Dominio", delivered: false }],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useExternalEntityDocuments(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiGet).toHaveBeenCalledWith("/managements/9/external-entity-documents");
    expect(result.current.data?.documents[0].name).toBe("Certificado de Dominio");
  });

  it("does not fetch when managementId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useExternalEntityDocuments(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalled();
  });
});

describe("useRegisterExternalEntityDocumentMovement (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("registers a movement via PUT", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idSubmittedDocument: 1, delivered: true });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useRegisterExternalEntityDocumentMovement(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      managementId: 9,
      idDocumentPresentado: 1,
      movement: { delivered: true },
    });

    expect(apiPut).toHaveBeenCalledWith("/managements/9/external-entity-documents/1", { delivered: true });
  });
});
