// @vitest-environment node
/**
 * HTTP gateway smoke for workflows (#1792 / ADR-030).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { workflowHttpGateway } from "./workflow-http-gateway";

describe("workflowHttpGateway (#1792)", () => {
  beforeEach(() => {
    vi.mocked(apiGet).mockReset();
    vi.mocked(apiPost).mockReset();
    vi.mocked(apiPut).mockReset();
    vi.mocked(apiDelete).mockReset();
  });

  it("lists definitions and validates", async () => {
    vi.mocked(apiGet).mockResolvedValueOnce([{ id: 1, name: "Default" }]);
    vi.mocked(apiPost).mockResolvedValueOnce({ valid: true, errors: [] });

    await expect(workflowHttpGateway.listDefinitions()).resolves.toEqual([{ id: 1, name: "Default" }]);
    expect(apiGet).toHaveBeenCalledWith("/workflow-definition");

    await expect(workflowHttpGateway.validateDefinition(1)).resolves.toEqual({
      valid: true,
      errors: [],
    });
    expect(apiPost).toHaveBeenCalledWith("/workflow-definition/1/validate", {});
  });

  it("lists nodes and transitions by workflow", async () => {
    vi.mocked(apiGet)
      .mockResolvedValueOnce([{ id: 10 }])
      .mockResolvedValueOnce([{ id: 20 }]);

    await expect(workflowHttpGateway.listNodesByWorkflow(3)).resolves.toEqual([{ id: 10 }]);
    expect(apiGet).toHaveBeenCalledWith("/workflow-node/by-workflow/3");

    await expect(workflowHttpGateway.listTransitionsByWorkflow(3)).resolves.toEqual([{ id: 20 }]);
    expect(apiGet).toHaveBeenCalledWith("/workflow-transition/by-workflow/3");
  });

  it("deletes a definition", async () => {
    vi.mocked(apiDelete).mockResolvedValueOnce(undefined);
    await workflowHttpGateway.deleteDefinition(9);
    expect(apiDelete).toHaveBeenCalledWith("/workflow-definition/9");
  });
});
