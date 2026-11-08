// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the management-status domain (#1769).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { managementStatusHttpGateway } from "./management-status-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("managementStatusHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists and maps idManagementStatus to domain id", async () => {
    apiGetMock.mockResolvedValue([
      { idManagementStatus: 1, name: "Started", notes: null, version: 0 },
    ]);
    await expect(managementStatusHttpGateway.list()).resolves.toEqual([
      { id: 1, name: "Started", notes: null, version: 0 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/management-statuses");
  });

  it("searches, creates, updates, deletes and checks in-use", async () => {
    apiGetMock.mockResolvedValueOnce([
      { idManagementStatus: 2, name: "Archived", version: 0 },
    ]);
    await expect(managementStatusHttpGateway.searchByName("Arch")).resolves.toEqual([
      { id: 2, name: "Archived", notes: undefined, version: 0 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/management-statuses/search?name=Arch");

    apiPostMock.mockResolvedValue({
      idManagementStatus: 9,
      name: "Review",
      notes: "",
      version: 0,
    });
    await expect(
      managementStatusHttpGateway.create({ name: "Review", notes: "" }),
    ).resolves.toMatchObject({ id: 9, name: "Review" });

    apiPutMock.mockResolvedValue({
      idManagementStatus: 9,
      name: "Reviewed",
      notes: "ok",
      version: 1,
    });
    await expect(
      managementStatusHttpGateway.update(9, { name: "Reviewed", notes: "ok", version: 0 }),
    ).resolves.toMatchObject({ id: 9, name: "Reviewed", version: 1 });

    apiDeleteMock.mockResolvedValue(undefined);
    await managementStatusHttpGateway.delete(9);
    expect(apiDeleteMock).toHaveBeenCalledWith("/management-statuses/9");

    apiGetMock.mockResolvedValueOnce({ inUse: true });
    await expect(managementStatusHttpGateway.isInUse(1)).resolves.toBe(true);
    expect(apiGetMock).toHaveBeenCalledWith("/management-statuses/1/in-use");
  });
});
