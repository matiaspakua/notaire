// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost } from "@/lib/api-client";
import { procedureTemplateHttpGateway } from "./procedure-template-http-gateway";

describe("procedureTemplateHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("maps wire rows and calls endpoints", async () => {
    vi.mocked(apiGet).mockResolvedValueOnce([
      { fkIdProcedureType: 7, fkIdDocumentType: 1, notes: "", version: 0 },
    ]);
    await expect(procedureTemplateHttpGateway.listByProcedureType(7)).resolves.toEqual([
      { procedureTypeId: 7, documentTypeId: 1, notes: "", version: 0 },
    ]);
    expect(apiGet).toHaveBeenCalledWith("/procedure-templates/procedure-types/7");

    vi.mocked(apiPost).mockResolvedValue({
      fkIdProcedureType: 7,
      fkIdDocumentType: 2,
      notes: "n",
    });
    await expect(
      procedureTemplateHttpGateway.create({ procedureTypeId: 7, documentTypeId: 2, notes: "n" }),
    ).resolves.toMatchObject({ procedureTypeId: 7, documentTypeId: 2 });

    vi.mocked(apiDelete).mockResolvedValue(undefined);
    await procedureTemplateHttpGateway.delete(7, 2);
    expect(apiDelete).toHaveBeenCalledWith("/procedure-templates/7/2");
  });
});
