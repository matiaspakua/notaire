/**
 * Unit test for useProcedureTemplate (CU03 — documents required for a procedure).
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
}));

import { apiGet } from "@/lib/api-client";
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
    vi.mocked(apiGet).mockImplementation((path: string) =>
      Promise.resolve(
        path.startsWith("/document-types")
          ? [{ idDocumentType: 1, name: "DNI", expires: false, deliveredBy: "Cliente" }]
          : [{ fkIdProcedureType: 7, fkIdDocumentType: 1, notes: "", version: 0 }],
      ) as never,
    );

    const { result } = renderHook(() => useProcedureTemplate(7), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.data?.[0]?.documentType?.name).toBe("DNI"));
    expect(apiGet).toHaveBeenCalledWith("/procedure-templates/procedure-types/7");
    expect(result.current.data?.[0].fkIdDocumentType).toBe(1);
  });

  it("does not fetch when procedureTypeId is undefined", () => {
    const { result } = renderHook(() => useProcedureTemplate(undefined), { wrapper: createWrapper() });

    expect(result.current.fetchStatus).toBe("idle");
    expect(apiGet).not.toHaveBeenCalledWith(expect.stringContaining("/procedure-templates"));
  });
});
