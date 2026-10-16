// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the deed domain (#1831).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import { deedHttpGateway } from "./deed-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiGetPageMock = vi.mocked(apiGetPage);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("deedHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("gets by id and lists a page", async () => {
    apiGetMock.mockResolvedValue({ idDeed: 7, number: 700, status: "Sin Firmar", version: 0 });
    await expect(deedHttpGateway.getById(7)).resolves.toMatchObject({
      idDeed: 7,
      number: 700,
      status: "Sin Firmar",
    });
    expect(apiGetMock).toHaveBeenCalledWith("/deeds/7");

    apiGetPageMock.mockResolvedValue({
      content: [{ idDeed: 1, number: 100 }],
      number: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });
    await expect(
      deedHttpGateway.listPage({ page: 0, size: 20, sort: "idDeed,desc" }),
    ).resolves.toMatchObject({
      content: [{ idDeed: 1, number: 100 }],
      totalElements: 1,
    });
    expect(apiGetPageMock).toHaveBeenCalledWith("/deeds", {
      page: 0,
      size: 20,
      sort: "idDeed,desc",
      params: undefined,
    });
  });

  it("searches by number, creates, updates, signs and deletes", async () => {
    apiGetMock.mockResolvedValue([{ idDeed: 12, number: 77, status: "Firmada" }]);
    await expect(deedHttpGateway.searchByNumber("77")).resolves.toEqual([
      { idDeed: 12, number: 77, status: "Firmada" },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/deeds/search?number=77");

    apiPostMock.mockResolvedValue(undefined);
    await deedHttpGateway.create({ number: 88, status: "Sin Firmar" });
    expect(apiPostMock).toHaveBeenCalledWith("/deeds", { number: 88, status: "Sin Firmar" });

    apiPutMock.mockResolvedValue(undefined);
    await deedHttpGateway.update(12, { notes: "ok" });
    expect(apiPutMock).toHaveBeenCalledWith("/deeds/12", { notes: "ok" });

    apiPostMock.mockResolvedValue({ idDeed: 12, number: 77, status: "Firmada" });
    await expect(deedHttpGateway.sign(12)).resolves.toMatchObject({ status: "Firmada" });
    expect(apiPostMock).toHaveBeenCalledWith("/deeds/12/sign", {});

    apiDeleteMock.mockResolvedValue(undefined);
    await deedHttpGateway.delete(12);
    expect(apiDeleteMock).toHaveBeenCalledWith("/deeds/12");
  });
});
