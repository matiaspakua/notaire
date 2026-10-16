// @vitest-environment node
/**
 * HTTP gateway for folio instance admin (#1785).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { folioHttpGateway } from "./folio-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("folioHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists, searches, writes and loads notaries", async () => {
    apiGetMock.mockResolvedValueOnce([{ idFolio: 1, number: 10, status: "Nuevo" }]);
    await expect(folioHttpGateway.list()).resolves.toEqual([
      { idFolio: 1, number: 10, status: "Nuevo" },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/folios");

    apiGetMock.mockResolvedValueOnce([{ idFolio: 2, number: 11, status: "Utilizado" }]);
    await expect(folioHttpGateway.searchByStatus("Utilizado")).resolves.toHaveLength(1);
    expect(apiGetMock).toHaveBeenCalledWith("/folios/search?status=Utilizado");

    const body = {
      number: 12,
      year: 2026,
      status: "Nuevo",
      notes: "",
      typeFolioId: 1,
      notaryId: 5,
    };
    apiPostMock.mockResolvedValue(undefined);
    await folioHttpGateway.create(body);
    expect(apiPostMock).toHaveBeenCalledWith("/folios", body);

    apiPutMock.mockResolvedValue(undefined);
    await folioHttpGateway.update(3, body);
    expect(apiPutMock).toHaveBeenCalledWith("/folios/3", body);

    apiDeleteMock.mockResolvedValue(undefined);
    await folioHttpGateway.delete(3);
    expect(apiDeleteMock).toHaveBeenCalledWith("/folios/3");

    apiGetMock.mockResolvedValueOnce([
      { personId: 5, firstName: "Ana", lastName: "Notary", notaryRegistrationNumber: 99 },
    ]);
    await expect(folioHttpGateway.listAvailableNotaries()).resolves.toEqual([
      {
        personId: 5,
        firstName: "Ana",
        lastName: "Notary",
        notaryRegistrationNumber: 99,
      },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/deeds/available-notaries");
  });
});
