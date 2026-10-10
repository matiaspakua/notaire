/**
 * Unit tests for useAssignWorkflowToTipoTramite (CU26, issue #655).
 * PUT /tipo-tramite/{id}/workflow answers 400 when the body has no `workflowDefinitionId`
 * (Owner decision Oct 9), so the hook must always send the key: a number to assign, or an
 * explicit null to unassign.
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

import { apiPut } from "@/lib/api-client";
import { useAssignWorkflowToTipoTramite } from "@/hooks/useTiposTramite";

function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }
  return Wrapper;
}

describe("useAssignWorkflowToTipoTramite (CU26, #655)", () => {
  beforeEach(() => vi.clearAllMocks());

  it("sends the workflow id to assign it", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idProcedureType: 3 });

    const { result } = renderHook(() => useAssignWorkflowToTipoTramite(), { wrapper: createWrapper() });
    result.current.mutate({ id: 3, workflowDefinitionId: 9 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(apiPut).toHaveBeenCalledWith("/tipo-tramite/3/workflow", { workflowDefinitionId: 9 });
  });

  it("sends an explicit null, never an empty body, to unassign", async () => {
    vi.mocked(apiPut).mockResolvedValue({ idProcedureType: 3 });

    const { result } = renderHook(() => useAssignWorkflowToTipoTramite(), { wrapper: createWrapper() });
    result.current.mutate({ id: 3, workflowDefinitionId: null });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    const body = vi.mocked(apiPut).mock.calls[0][1];
    expect(body).toEqual({ workflowDefinitionId: null });
    expect(JSON.stringify(body)).toBe('{"workflowDefinitionId":null}');
  });
});
