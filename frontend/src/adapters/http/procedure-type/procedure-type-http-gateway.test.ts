// @vitest-environment node
/**
 * HTTP gateway smoke for procedure types (#1796 / ADR-030).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { procedureTypeHttpGateway } from "./procedure-type-http-gateway";

describe("procedureTypeHttpGateway (#1796)", () => {
  beforeEach(() => {
    vi.mocked(apiGet).mockReset();
    vi.mocked(apiPost).mockReset();
    vi.mocked(apiPut).mockReset();
    vi.mocked(apiDelete).mockReset();
  });

  it("lists and maps wire ids", async () => {
    vi.mocked(apiGet).mockResolvedValueOnce([{ idProcedureType: 3, name: "Sale" }]);
    await expect(procedureTypeHttpGateway.list()).resolves.toEqual([
      { id: 3, name: "Sale", notes: undefined, isArchived: undefined, isRegistered: undefined, associatesProperties: undefined, enabled: undefined, workflowDefinitionId: undefined, workflowDefinitionName: undefined },
    ]);
    expect(apiGet).toHaveBeenCalledWith("/procedure-types");
  });

  it("checks in-use and assigns workflow", async () => {
    vi.mocked(apiGet).mockResolvedValueOnce({ inUse: true });
    vi.mocked(apiPut).mockResolvedValueOnce({ idProcedureType: 3, name: "Sale", workflowDefinitionId: 9 });

    await expect(procedureTypeHttpGateway.isInUse(3)).resolves.toBe(true);
    await expect(procedureTypeHttpGateway.assignWorkflow(3, 9)).resolves.toMatchObject({
      id: 3,
      workflowDefinitionId: 9,
    });
    expect(apiPut).toHaveBeenCalledWith("/procedure-types/3/workflow", { workflowDefinitionId: 9 });

    vi.mocked(apiPut).mockResolvedValueOnce({ idProcedureType: 3, name: "Sale", workflowDefinitionId: null });
    await procedureTypeHttpGateway.assignWorkflow(3, null);
    const body = vi.mocked(apiPut).mock.calls.at(-1)?.[1];
    expect(body).toEqual({ workflowDefinitionId: null });
    expect(JSON.stringify(body)).toBe('{"workflowDefinitionId":null}');
  });

  it("deletes by id", async () => {
    vi.mocked(apiDelete).mockResolvedValueOnce(undefined);
    await procedureTypeHttpGateway.delete(8);
    expect(apiDelete).toHaveBeenCalledWith("/procedure-types/8");
  });
});
