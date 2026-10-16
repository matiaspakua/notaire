// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the budget domain (#1827).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
  ApiError: class ApiError extends Error {
    status: number;
    constructor(status: number, body?: unknown, message = "error") {
      super(message);
      this.status = status;
    }
  },
}));

import { apiDelete, apiGet, apiGetPage, apiPost, apiPut, ApiError } from "@/lib/api-client";
import { budgetHttpGateway } from "./budget-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiGetPageMock = vi.mocked(apiGetPage);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("budgetHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("gets by id and lists a page", async () => {
    apiGetMock.mockResolvedValue({ idBudget: 7, heading: "Sale", status: "PENDIENTE", version: 0 });
    await expect(budgetHttpGateway.getById(7)).resolves.toMatchObject({
      idBudget: 7,
      heading: "Sale",
      status: "PENDIENTE",
    });
    expect(apiGetMock).toHaveBeenCalledWith("/budgets/7");

    apiGetPageMock.mockResolvedValue({
      content: [{ idBudget: 1, heading: "Latest" }],
      number: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });
    await expect(
      budgetHttpGateway.listPage({ page: 0, size: 20, sort: "idBudget,desc" }),
    ).resolves.toMatchObject({
      content: [{ idBudget: 1, heading: "Latest" }],
      totalElements: 1,
    });
  });

  it("returns null on findById 404 and lists by status", async () => {
    apiGetMock.mockRejectedValueOnce(new ApiError(404, "missing"));
    await expect(budgetHttpGateway.findById(99)).resolves.toBeNull();

    apiGetMock.mockResolvedValueOnce([{ idBudget: 3, status: "PENDIENTE" }]);
    await expect(budgetHttpGateway.listByStatus("PENDIENTE")).resolves.toMatchObject([
      { idBudget: 3, status: "PENDIENTE" },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/budgets/search?status=PENDIENTE");
  });

  it("creates, updates, deletes and loads template items", async () => {
    apiPostMock.mockResolvedValueOnce({ idBudget: 8, heading: "New" });
    await expect(budgetHttpGateway.create({ heading: "New" })).resolves.toMatchObject({ idBudget: 8 });

    apiPutMock.mockResolvedValue(undefined);
    await budgetHttpGateway.update(8, { heading: "Updated" });
    expect(apiPutMock).toHaveBeenCalledWith("/budgets/8", expect.objectContaining({ heading: "Updated" }));

    apiDeleteMock.mockResolvedValue(undefined);
    await budgetHttpGateway.delete(8);
    expect(apiDeleteMock).toHaveBeenCalledWith("/budgets/8");

    apiPostMock.mockResolvedValueOnce([{ idItem: 1, name: "Fee", value: 100 }]);
    await expect(budgetHttpGateway.loadItemsFromTemplate(8, 3)).resolves.toMatchObject([
      { idItem: 1, name: "Fee" },
    ]);
  });
});
