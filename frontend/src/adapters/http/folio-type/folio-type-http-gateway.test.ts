// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the folio-type domain (#1785).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { folioTypeHttpGateway } from "./folio-type-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("folioTypeHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists and maps idFolioType to domain id", async () => {
    apiGetMock.mockResolvedValue([
      { idFolioType: 1, name: "Ordinary", enabled: true, isAuxiliary: false, version: 2 },
    ]);
    await expect(folioTypeHttpGateway.list()).resolves.toEqual([
      {
        id: 1,
        name: "Ordinary",
        isAuxiliary: false,
        notes: undefined,
        enabled: true,
        version: 2,
      },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/folio-types");
  });

  it("searches, creates, updates, deletes and checks in-use", async () => {
    apiGetMock.mockResolvedValueOnce([{ idFolioType: 2, name: "Auxiliary", isAuxiliary: true }]);
    await expect(folioTypeHttpGateway.searchByName("Aux")).resolves.toMatchObject([
      { id: 2, name: "Auxiliary", isAuxiliary: true },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/folio-types/search?name=Aux");

    apiPostMock.mockResolvedValue({ idFolioType: 9, name: "Special", isAuxiliary: false });
    await expect(
      folioTypeHttpGateway.create({ name: "Special", isAuxiliary: false }),
    ).resolves.toMatchObject({ id: 9, name: "Special" });

    apiPutMock.mockResolvedValue({
      idFolioType: 9,
      name: "Special renamed",
      isAuxiliary: true,
      version: 4,
    });
    await expect(
      folioTypeHttpGateway.update(9, {
        name: "Special renamed",
        isAuxiliary: true,
        version: 4,
      }),
    ).resolves.toMatchObject({ id: 9, name: "Special renamed", version: 4 });

    apiDeleteMock.mockResolvedValue(undefined);
    await folioTypeHttpGateway.delete(9);
    expect(apiDeleteMock).toHaveBeenCalledWith("/folio-types/9");

    apiGetMock.mockResolvedValueOnce({ inUse: true });
    await expect(folioTypeHttpGateway.isInUse(1)).resolves.toBe(true);
    expect(apiGetMock).toHaveBeenCalledWith("/folio-types/1/in-use");
  });
});
