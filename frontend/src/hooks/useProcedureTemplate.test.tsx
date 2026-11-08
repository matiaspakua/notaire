/**
 * Unit test for useProcedureTemplate (CU03 — documents required for a procedure).
 * Mocks composition use cases (ADR-030); HTTP is covered by procedure-template-http-gateway.test.ts.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

const listByProcedureType = vi.fn();

vi.mock("@/composition/procedure-template", () => ({
  procedureTemplateUseCases: {
    listByProcedureType: (...args: unknown[]) => listByProcedureType(...args),
    listAll: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
  },
}));

vi.mock("@/hooks/useDocuments", () => ({
  useDocumentTypes: () => ({
    data: [{ idDocumentType: 1, name: "DNI", expires: false, deliveredBy: "Cliente" }],
  }),
}));

import { procedureTemplateKeys, useProcedureTemplate } from "@/hooks/useProcedureTemplate";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("procedureTemplateKeys", () => {
  it("byProcedure key includes the procedure id", () => {
    expect(procedureTemplateKeys.byProcedure(7)).toEqual(["procedureTemplate", "procedureType", 7]);
  });
});

describe("useProcedureTemplate (CU03)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("fetches the flat checklist rows and joins the document type by fkIdDocumentType (#1362)", async () => {
    listByProcedureType.mockResolvedValue([
      { procedureTypeId: 7, documentTypeId: 1, notes: "", version: 0 },
    ]);

    const { result } = renderHook(() => useProcedureTemplate(7), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.data?.[0]?.documentType?.name).toBe("DNI"));
    expect(listByProcedureType).toHaveBeenCalledWith(7);
    expect(result.current.data?.[0].fkIdDocumentType).toBe(1);
  });

  it("does not fetch when procedureTypeId is undefined", () => {
    const { result } = renderHook(() => useProcedureTemplate(undefined), { wrapper: createWrapper() });

    expect(result.current.fetchStatus).toBe("idle");
    expect(listByProcedureType).not.toHaveBeenCalled();
  });
});
