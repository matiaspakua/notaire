/**
 * Unit test for useExternalEntityDocuments (CU10).
 * Hexagon wiring: #1841 / ADR-030.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/composition/external-entity-document", () => ({
  externalEntityDocumentUseCases: {
    getByManagement: vi.fn(),
    registerMovement: vi.fn(),
  },
}));

import { externalEntityDocumentUseCases } from "@/composition/external-entity-document";
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
    expect(externalEntityDocumentKeys.byManagement(9)).toEqual(["externalEntityDocuments", "management", 9]);
  });
});

describe("useExternalEntityDocuments (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the management's external-entity documents", async () => {
    vi.mocked(externalEntityDocumentUseCases.getByManagement).mockResolvedValue({
      idManagement: 9,
      number: 1001,
      heading: "Test management",
      cadastralDesignation: "11-22-33",
      documents: [{ idSubmittedDocument: 1, name: "Certificado de Dominio", delivered: false }],
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useExternalEntityDocuments(9), { wrapper: Wrapper });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(externalEntityDocumentUseCases.getByManagement).toHaveBeenCalledWith(9);
    expect(result.current.data?.documents[0].name).toBe("Certificado de Dominio");
  });

  it("does not fetch when managementId is undefined", () => {
    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useExternalEntityDocuments(undefined), { wrapper: Wrapper });

    expect(result.current.fetchStatus).toBe("idle");
    expect(externalEntityDocumentUseCases.getByManagement).not.toHaveBeenCalled();
  });
});

describe("useRegisterExternalEntityDocumentMovement (CU10)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("registers a movement via the use case", async () => {
    vi.mocked(externalEntityDocumentUseCases.registerMovement).mockResolvedValue({
      idSubmittedDocument: 1,
      delivered: true,
    });

    const { Wrapper } = createWrapper();
    const { result } = renderHook(() => useRegisterExternalEntityDocumentMovement(), { wrapper: Wrapper });

    await result.current.mutateAsync({
      managementId: 9,
      idSubmittedDocument: 1,
      movement: { delivered: true },
    });

    expect(externalEntityDocumentUseCases.registerMovement).toHaveBeenCalledWith(9, 1, {
      delivered: true,
    });
  });
});
