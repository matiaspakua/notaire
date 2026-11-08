// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the concept domain (#1781).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { conceptHttpGateway } from "./concept-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("conceptHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists and maps idConcept to domain id", async () => {
    apiGetMock.mockResolvedValue([{ idConcept: 1, name: "Honorarium", value: 100, version: 0 }]);
    await expect(conceptHttpGateway.list()).resolves.toEqual([
      {
        id: 1,
        name: "Honorarium",
        value: 100,
        percentage: undefined,
        fixed: undefined,
        enabled: undefined,
        version: 0,
      },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/concepts");
  });

  it("searches, creates, updates, deletes and checks in-use", async () => {
    apiGetMock.mockResolvedValueOnce([{ idConcept: 2, name: "Stamp", value: 50 }]);
    await expect(conceptHttpGateway.searchByName("Stamp")).resolves.toMatchObject([
      { id: 2, name: "Stamp", value: 50 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/concepts/search?name=Stamp");

    apiPostMock.mockResolvedValue({ idConcept: 9, name: "Fee", value: 25 });
    await expect(conceptHttpGateway.create({ name: "Fee", value: 25 })).resolves.toMatchObject({
      id: 9,
      name: "Fee",
    });

    apiPutMock.mockResolvedValue({ idConcept: 9, name: "Fee updated", value: 30, version: 1 });
    await expect(
      conceptHttpGateway.update(9, { name: "Fee updated", value: 30, version: 0 }),
    ).resolves.toMatchObject({ id: 9, name: "Fee updated", value: 30 });

    apiDeleteMock.mockResolvedValue(undefined);
    await conceptHttpGateway.delete(9);
    expect(apiDeleteMock).toHaveBeenCalledWith("/concepts/9");

    apiGetMock.mockResolvedValueOnce({ inUse: true });
    await expect(conceptHttpGateway.isInUse(1)).resolves.toBe(true);
    expect(apiGetMock).toHaveBeenCalledWith("/concepts/1/in-use");
  });
});
